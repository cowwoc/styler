package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.NodeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API compatibility tests for Arena implementation - ensuring it serves as a
 * drop-in replacement for NodeRegistry while maintaining full API compatibility.
 *
 * <h2>Compatibility Business Rules Tested</h2>
 * <ul>
 * <li><strong>API Compatibility Rule</strong>: Drop-in replacement for NodeRegistry</li>
 * <li><strong>NodeInfo Structure Rule</strong>: Identical data access patterns</li>
 * <li><strong>AutoCloseable Rule</strong>: Proper resource management integration</li>
 * <li><strong>Parser Integration Rule</strong>: Seamless IndexOverlayParser compatibility</li>
 * <li><strong>Performance Transparency Rule</strong>: No API changes required for performance gains</li>
 * <li><strong>Migration Path Rule</strong>: Easy transition from existing implementations</li>
 * </ul>
 */
class ArenaCompatibilityTest {

	@Nested
	@DisplayName("NodeInfo API Compatibility")
	class NodeInfoAPICompatibilityTests {

		@Test
		@DisplayName("Should provide identical NodeInfo interface to NodeRegistry")
		void shouldProvideIdenticalNodeInfoInterfaceToNodeRegistry() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(100)) {
				// Allocate a node
				int nodeId = storage.allocateNode(10, 20, NodeType.CLASS_DECLARATION, -1);
				ArenaNodeStorage.NodeInfo nodeInfo = storage.getNode(nodeId);

				// Verify all expected NodeInfo methods exist and work correctly
				assertEquals(nodeId, nodeInfo.nodeId(), "nodeId() should return correct ID");
				assertEquals(10, nodeInfo.startOffset(), "startOffset() should return correct value");
				assertEquals(20, nodeInfo.length(), "length() should return correct value");
				assertEquals(NodeType.CLASS_DECLARATION, nodeInfo.nodeType(), "nodeType() should return correct type");
				assertEquals(-1, nodeInfo.parentId(), "parentId() should return correct parent");
				assertNotNull(nodeInfo.childIds(), "childIds() should return non-null list");
				assertTrue(nodeInfo.childIds().isEmpty(), "childIds() should be empty for node with no children");

				// Verify calculated methods
				assertEquals(30, nodeInfo.endOffset(), "endOffset() should calculate correctly");
				assertTrue(nodeInfo.isRoot(), "isRoot() should return true for root node");
				assertEquals("ClassDeclaration", nodeInfo.getTypeName(), "getTypeName() should return readable name");

				// Verify NodeInfo is a proper record
				assertTrue(nodeInfo instanceof Record, "NodeInfo should be a record");
			}
		}

		@Test
		@DisplayName("Should handle parent-child relationships like NodeRegistry")
		void shouldHandleParentChildRelationshipsLikeNodeRegistry() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(100)) {
				// Create parent-child hierarchy
				int parentId = storage.allocateNode(0, 50, NodeType.CLASS_DECLARATION, -1);
				int child1Id = storage.allocateNode(10, 15, NodeType.METHOD_DECLARATION, parentId);
				int child2Id = storage.allocateNode(30, 10, NodeType.FIELD_DECLARATION, parentId);

				// Verify parent node
				ArenaNodeStorage.NodeInfo parentNode = storage.getNode(parentId);
				assertTrue(parentNode.isRoot(), "Parent should be root");
				List<Integer> parentChildren = parentNode.childIds();
				assertEquals(2, parentChildren.size(), "Parent should have 2 children");
				assertTrue(parentChildren.contains(child1Id), "Parent should contain child1");
				assertTrue(parentChildren.contains(child2Id), "Parent should contain child2");

				// Verify child nodes
				ArenaNodeStorage.NodeInfo child1Node = storage.getNode(child1Id);
				assertFalse(child1Node.isRoot(), "Child1 should not be root");
				assertEquals(parentId, child1Node.parentId(), "Child1 should reference parent");
				assertTrue(child1Node.childIds().isEmpty(), "Child1 should have no children");

				ArenaNodeStorage.NodeInfo child2Node = storage.getNode(child2Id);
				assertFalse(child2Node.isRoot(), "Child2 should not be root");
				assertEquals(parentId, child2Node.parentId(), "Child2 should reference parent");
				assertTrue(child2Node.childIds().isEmpty(), "Child2 should have no children");
			}
		}

		@Test
		@DisplayName("Should provide consistent getChildren() behavior")
		void shouldProvideConsistentGetChildrenBehavior() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(100)) {
				// Test node with no children
				int leafNodeId = storage.allocateNode(0, 10, NodeType.LITERAL_EXPRESSION, -1);
				List<Integer> leafChildren = storage.getChildren(leafNodeId);
				assertNotNull(leafChildren, "getChildren() should never return null");
				assertTrue(leafChildren.isEmpty(), "getChildren() should return empty list for leaf");
				assertEquals(0, leafChildren.size(), "getChildren() size should be 0 for leaf");

				// Test node with children
				int parentId = storage.allocateNode(20, 30, NodeType.CLASS_DECLARATION, -1);
				int child1Id = storage.allocateNode(25, 5, NodeType.METHOD_DECLARATION, parentId);
				int child2Id = storage.allocateNode(35, 8, NodeType.FIELD_DECLARATION, parentId);

				List<Integer> parentChildren = storage.getChildren(parentId);
				assertNotNull(parentChildren, "getChildren() should never return null");
				assertEquals(2, parentChildren.size(), "getChildren() should return correct count");
				assertEquals(child1Id, parentChildren.get(0), "First child should be correct");
				assertEquals(child2Id, parentChildren.get(1), "Second child should be correct");

				// Verify consistency with NodeInfo.childIds()
				ArenaNodeStorage.NodeInfo parentInfo = storage.getNode(parentId);
				assertEquals(parentChildren, parentInfo.childIds(), "getChildren() should match NodeInfo.childIds()");
			}
		}
	}

	@Nested
	@DisplayName("Parser Integration Compatibility")
	class ParserIntegrationCompatibilityTests {

		@Test
		@DisplayName("Should integrate seamlessly with IndexOverlayParser")
		void shouldIntegrateSeamlesslyWithIndexOverlayParser() {
			String javaCode = """
				package com.example;

				public class CompatibilityTest {
					private String field;

					public CompatibilityTest(String field) {
						this.field = field;
					}

					public String getField() {
						return field;
					}
				}
				""";

			try (IndexOverlayParser parser = new IndexOverlayParser(javaCode, JavaVersion.JAVA_21)) {
				// Parse should work exactly as before
				int rootNodeId = parser.parse();
				assertNotEquals(-1, rootNodeId, "Parser should successfully parse with Arena storage");

				// Get node storage - should be ArenaNodeStorage
				ArenaNodeStorage nodeStorage = parser.getNodeStorage();
				assertNotNull(nodeStorage, "Parser should provide node storage");
				assertTrue(nodeStorage.isAlive(), "Arena should be alive during parsing");

				// Node operations should work identically to NodeRegistry
				ArenaNodeStorage.NodeInfo rootNode = nodeStorage.getNode(rootNodeId);
				assertEquals(NodeType.COMPILATION_UNIT, rootNode.nodeType(), "Root should be compilation unit");

				// Text reconstruction should work
				String reconstructedText = parser.getNodeText(rootNodeId);
				assertEquals(javaCode, reconstructedText, "Text reconstruction should be identical");

				// Node count should be reasonable
				assertTrue(nodeStorage.getNodeCount() > 5, "Should create multiple nodes for class structure");
			}
		}

		@Test
		@DisplayName("Should support all Java language features with Arena storage")
		void shouldSupportAllJavaLanguageFeaturesWithArenaStorage() {
			String modernJavaCode = """
				package com.example.modern;

				import java.util.*;
				import java.util.function.*;

				public class ModernJavaFeatures {

					// Records (JDK 16+)
					public record Person(String name, int age) {
						public Person {
							if (age < 0) throw new IllegalArgumentException("Age cannot be negative");
						}
					}

					// Switch expressions (JDK 14+)
					public String processValue(Object value) {
						return switch (value) {
							case Integer i -> "Integer: " + i;
							case String s -> "String: " + s;
							case null -> "Null value";
							default -> "Unknown type";
						};
					}

					// Pattern matching instanceof (JDK 16+)
					public void patternMatching(Object obj) {
						if (obj instanceof String s && s.length() > 5) {
							System.out.println("Long string: " + s);
						}
					}

					// Text blocks (JDK 15+)
					private static final String JSON_TEMPLATE = \"\"\"
						{
							"name": "%s",
							"age": %d
						}
						\"\"\";

					// Lambda expressions and method references
					public void functionalProgramming() {
						List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
						names.stream()
							.filter(s -> s.length() > 3)
							.map(String::toUpperCase)
							.forEach(System.out::println);
					}
				}
				""";

			try (IndexOverlayParser parser = new IndexOverlayParser(modernJavaCode, JavaVersion.JAVA_21)) {
				int rootNodeId = parser.parse();
				ArenaNodeStorage storage = parser.getNodeStorage();

				// Should successfully parse modern Java features
				assertNotEquals(-1, rootNodeId, "Should parse modern Java features");
				assertTrue(storage.getNodeCount() > 20, "Modern features should create many nodes");

				// Verify text reconstruction maintains all features
				String reconstructedText = parser.getNodeText(rootNodeId);
				assertTrue(reconstructedText.contains("record Person"), "Should preserve record syntax");
				assertTrue(reconstructedText.contains("switch (value)"), "Should preserve switch expressions");
				assertTrue(reconstructedText.contains("instanceof String s"), "Should preserve pattern matching");
				assertTrue(reconstructedText.contains("\"\"\""), "Should preserve text blocks");
			}
		}

		@Test
		@DisplayName("Should maintain AutoCloseable behavior consistently")
		void shouldMaintainAutoCloseableBehaviorConsistently() {
			ArenaNodeStorage capturedStorage = null;

			// Test that Arena is properly closed after try-with-resources
			try (IndexOverlayParser parser = new IndexOverlayParser("public class Test {}")) {
				parser.parse();
				capturedStorage = parser.getNodeStorage();
				assertTrue(capturedStorage.isAlive(), "Arena should be alive inside try block");
			}

			// Arena should be closed after parser is closed
			assertFalse(capturedStorage.isAlive(), "Arena should be closed after parser closes");

			// Test direct ArenaNodeStorage AutoCloseable behavior
			ArenaNodeStorage directStorage = ArenaNodeStorage.create(100);
			assertTrue(directStorage.isAlive(), "Direct storage should be alive");
			directStorage.close();
			assertFalse(directStorage.isAlive(), "Direct storage should be closed after close()");
		}
	}

	@Nested
	@DisplayName("Migration Path Compatibility")
	class MigrationPathCompatibilityTests {

		@Test
		@DisplayName("Should require no code changes for existing parser usage")
		void shouldRequireNoCodeChangesForExistingParserUsage() {
			// This test simulates typical existing usage patterns
			String javaCode = "public class Migration { public void test() {} }";

			// Pattern 1: Basic parsing
			try (IndexOverlayParser parser = new IndexOverlayParser(javaCode)) {
				int rootId = parser.parse();
				assertTrue(rootId >= 0, "Basic parsing should work unchanged");
			}

			// Pattern 2: Node inspection
			try (IndexOverlayParser parser = new IndexOverlayParser(javaCode)) {
				int rootId = parser.parse();
				ArenaNodeStorage storage = parser.getNodeStorage();
				ArenaNodeStorage.NodeInfo rootNode = storage.getNode(rootId);

				// These operations should work exactly as before
				assertEquals(rootId, rootNode.nodeId());
				assertTrue(rootNode.startOffset() >= 0);
				assertTrue(rootNode.length() > 0);
				assertNotNull(rootNode.getTypeName());
			}

			// Pattern 3: Tree traversal
			try (IndexOverlayParser parser = new IndexOverlayParser(javaCode)) {
				int rootId = parser.parse();
				ArenaNodeStorage storage = parser.getNodeStorage();

				// Recursive traversal should work unchanged
				traverseTree(storage, rootId);
			}
		}

		private void traverseTree(ArenaNodeStorage storage, int nodeId) {
			ArenaNodeStorage.NodeInfo node = storage.getNode(nodeId);
			assertNotNull(node, "Node should be accessible");

			// Traverse children
			for (Integer childId : storage.getChildren(nodeId)) {
				traverseTree(storage, childId);
			}
		}

		@Test
		@DisplayName("Should provide backward-compatible error handling")
		void shouldProvideBackwardCompatibleErrorHandling() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(10)) {
				// Invalid node ID access should throw same exception type
				assertThrows(IllegalArgumentException.class,
					() -> storage.getNode(-1),
					"Should throw IllegalArgumentException for invalid node ID");

				assertThrows(IllegalArgumentException.class,
					() -> storage.getChildren(999),
					"Should throw IllegalArgumentException for non-existent node");

				assertThrows(IllegalArgumentException.class,
					() -> storage.updateNodeLength(-1, 10),
					"Should throw IllegalArgumentException for invalid update");

				// Capacity exceeded should throw appropriate exception
				for (int i = 0; i < 10; i++) {
					storage.allocateNode(i, 1, NodeType.LITERAL_EXPRESSION, -1);
				}

				assertThrows(IllegalStateException.class,
					() -> storage.allocateNode(100, 1, NodeType.LITERAL_EXPRESSION, -1),
					"Should throw IllegalStateException when capacity exceeded");
			}
		}

		@Test
		@DisplayName("Should maintain consistent performance characteristics")
		void shouldMaintainConsistentPerformanceCharacteristics() {
			// Basic operations should be faster than before, but with same time complexity
			int nodeCount = 1000;

			long startTime = System.nanoTime();
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(nodeCount)) {
				// O(1) allocation
				for (int i = 0; i < nodeCount; i++) {
					storage.allocateNode(i, 1, NodeType.METHOD_DECLARATION, -1);
				}

				// O(1) retrieval
				for (int i = 0; i < nodeCount; i++) {
					ArenaNodeStorage.NodeInfo node = storage.getNode(i);
					assertNotNull(node, "Node retrieval should be fast and reliable");
				}
			}
			long totalTime = System.nanoTime() - startTime;

			double timeMs = totalTime / 1_000_000.0;
			System.out.printf("Performance test: %d operations in %.2f ms\n", nodeCount * 2, timeMs);

			// Should be fast enough for practical use
			assertTrue(timeMs < 100.0, "Operations should complete quickly");
		}
	}

	@Nested
	@DisplayName("API Surface Compatibility")
	class APISurfaceCompatibilityTests {

		@Test
		@DisplayName("Should expose all expected public methods")
		void shouldExposeAllExpectedPublicMethods() {
			// Verify ArenaNodeStorage has all required public methods
			Class<ArenaNodeStorage> storageClass = ArenaNodeStorage.class;

			// Factory methods
			assertHasMethod(storageClass, "create", int.class);
			assertHasMethod(storageClass, "create");

			// Core operations
			assertHasMethod(storageClass, "allocateNode", int.class, int.class, byte.class, int.class);
			assertHasMethod(storageClass, "getNode", int.class);
			assertHasMethod(storageClass, "getChildren", int.class);
			assertHasMethod(storageClass, "updateNodeLength", int.class, int.class);

			// Lifecycle methods
			assertHasMethod(storageClass, "close");
			assertHasMethod(storageClass, "isAlive");
			assertHasMethod(storageClass, "reset");

			// Information methods
			assertHasMethod(storageClass, "getNodeCount");
			assertHasMethod(storageClass, "getEstimatedMemoryUsage");
		}

		private void assertHasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
			try {
				Method method = clazz.getMethod(methodName, parameterTypes);
				assertNotNull(method, "Method " + methodName + " should exist");
			} catch (NoSuchMethodException e) {
				fail("Method " + methodName + " with parameters " +
					java.util.Arrays.toString(parameterTypes) + " should exist in " + clazz.getSimpleName());
			}
		}

		@Test
		@DisplayName("Should maintain consistent return types")
		void shouldMaintainConsistentReturnTypes() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(100)) {
				// Factory methods return correct types
				assertTrue(storage instanceof ArenaNodeStorage, "create() should return ArenaNodeStorage");
				assertTrue(storage instanceof AutoCloseable, "ArenaNodeStorage should be AutoCloseable");

				// allocateNode returns int (node ID)
				int nodeId = storage.allocateNode(0, 10, NodeType.CLASS_DECLARATION, -1);
				assertTrue(nodeId >= 0, "allocateNode should return non-negative int");

				// getNode returns NodeInfo
				ArenaNodeStorage.NodeInfo nodeInfo = storage.getNode(nodeId);
				assertTrue(nodeInfo instanceof Record, "NodeInfo should be a record");

				// getChildren returns List<Integer>
				List<Integer> children = storage.getChildren(nodeId);
				assertTrue(children instanceof List, "getChildren should return List");

				// getNodeCount returns int
				int count = storage.getNodeCount();
				assertTrue(count > 0, "getNodeCount should return positive int");

				// getEstimatedMemoryUsage returns long
				long memory = storage.getEstimatedMemoryUsage();
				assertTrue(memory > 0, "getEstimatedMemoryUsage should return positive long");

				// isAlive returns boolean
				boolean alive = storage.isAlive();
				assertTrue(alive, "isAlive should return boolean");
			}
		}

		@Test
		@DisplayName("Should provide identical NodeInfo record structure")
		void shouldProvideIdenticalNodeInfoRecordStructure() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(100)) {
				int nodeId = storage.allocateNode(10, 20, NodeType.CLASS_DECLARATION, -1);
				ArenaNodeStorage.NodeInfo nodeInfo = storage.getNode(nodeId);

				// Verify record components match expected interface
				assertEquals(6, nodeInfo.getClass().getRecordComponents().length,
					"NodeInfo should have exactly 6 record components");

				// Verify component names and types
				var components = nodeInfo.getClass().getRecordComponents();
				assertEquals("nodeId", components[0].getName());
				assertEquals(int.class, components[0].getType());

				assertEquals("startOffset", components[1].getName());
				assertEquals(int.class, components[1].getType());

				assertEquals("length", components[2].getName());
				assertEquals(int.class, components[2].getType());

				assertEquals("nodeType", components[3].getName());
				assertEquals(byte.class, components[3].getType());

				assertEquals("parentId", components[4].getName());
				assertEquals(int.class, components[4].getType());

				assertEquals("childIds", components[5].getName());
				assertEquals(List.class, components[5].getType());
			}
		}
	}
}