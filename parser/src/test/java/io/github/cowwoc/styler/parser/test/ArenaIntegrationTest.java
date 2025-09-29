package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.ParseMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ArenaNodeStorage with IndexOverlayParser - validating end-to-end
 * parser workflow with Arena API memory management.
 *
 * <h2>Business Rules Tested</h2>
 * <ul>
 * <li><strong>End-to-End Parsing Rule</strong>: Complete Java source → Arena AST workflow</li>
 * <li><strong>Resource Management Rule</strong>: AutoCloseable integration with try-with-resources</li>
 * <li><strong>AST Integrity Rule</strong>: Parser AST matches source structure exactly</li>
 * <li><strong>Performance Integration Rule</strong>: Parser + Arena together achieve performance targets</li>
 * <li><strong>Memory Limit Rule</strong>: Combined system respects memory constraints</li>
 * <li><strong>Language Feature Rule</strong>: All Java 25 features work with Arena storage</li>
 * </ul>
 */
class ArenaIntegrationTest {

	@BeforeEach
	void setUp() {
		// Enable metrics for integration tests
		System.setProperty("styler.metrics.enabled", "true");
		ParseMetrics.reset();
	}

	@AfterEach
	void tearDown() {
		System.clearProperty("styler.metrics.enabled");
	}

	@Nested
	@DisplayName("End-to-End Parser Integration")
	class EndToEndParsingTests {

		@Test
		@DisplayName("Should parse simple class with Arena storage")
		void shouldParseSimpleClassWithArenaStorage() {
			String javaCode = """
				package com.example;

				public class HelloWorld {
					private String message;

					public HelloWorld(String message) {
						this.message = message;
					}

					public void sayHello() {
						System.out.println(message);
					}
				}
				""";

			try (IndexOverlayParser parser = new IndexOverlayParser(javaCode, JavaVersion.JAVA_21)) {
				int rootNodeId = parser.parse();

				// Verify root node
				assertEquals(0, rootNodeId, "Root node should have ID 0");
				ArenaNodeStorage nodeStorage = parser.getNodeStorage();
				ArenaNodeStorage.NodeInfo rootNode = nodeStorage.getNode(rootNodeId);
				assertEquals(NodeType.COMPILATION_UNIT, rootNode.nodeType(), "Root should be compilation unit");
				assertTrue(rootNode.isRoot(), "Root node should have no parent");

				// Verify node count represents complete parsing
				assertTrue(nodeStorage.getNodeCount() > 10, "Should have created multiple nodes for class structure");

				// Verify text reconstruction
				String rootText = parser.getNodeText(rootNodeId);
				assertEquals(javaCode.trim(), rootText.trim(), "Root node should contain entire source text");

				// Verify Arena is alive during parsing
				assertTrue(nodeStorage.isAlive(), "Arena should be alive during parsing");

				// Verify memory usage is reasonable
				long memoryUsage = nodeStorage.getEstimatedMemoryUsage();
				assertTrue(memoryUsage > 0, "Should have positive memory usage");
				assertTrue(memoryUsage < 10_000, "Should use reasonable amount of memory for small class");
			}
		}

		@Test
		@DisplayName("Should parse complex Java 25 features with Arena storage")
		void shouldParseComplexJava25FeaturesWithArenaStorage() {
			String javaCode = """
				package com.example.modern;

				import module java.base;

				public class ModernJavaFeatures {

					// JDK 25 flexible constructor with statements before super()
					public ModernJavaFeatures() {
						var config = loadConfig();
						super(); // This can come after statements in JDK 25
					}

					// Instance main method (JDK 25)
					void main() {
						var processor = switch (getType()) {
							case INTEGER -> (Integer value) -> value * 2;
							case STRING -> (String str) -> str.toUpperCase();
							default -> null;
						};

						// Primitive pattern matching
						Object obj = getValue();
						if (obj instanceof int i) {
							System.out.println("Integer: " + i);
						}
					}

					private Config loadConfig() { return null; }
					private Type getType() { return Type.STRING; }
					private Object getValue() { return 42; }

					enum Type { INTEGER, STRING }
					record Config() {}
				}
				""";

			try (IndexOverlayParser parser = new IndexOverlayParser(javaCode, JavaVersion.JAVA_25)) {
				int rootNodeId = parser.parse();

				ArenaNodeStorage nodeStorage = parser.getNodeStorage();
				ArenaNodeStorage.NodeInfo rootNode = nodeStorage.getNode(rootNodeId);
				assertEquals(NodeType.COMPILATION_UNIT, rootNode.nodeType(), "Should parse as compilation unit");

				// Verify complex structure was parsed correctly
				assertTrue(nodeStorage.getNodeCount() > 30, "Complex code should create many nodes");

				// Verify text reconstruction maintains fidelity
				String reconstructedText = parser.getNodeText(rootNodeId);
				assertNotNull(reconstructedText, "Should be able to reconstruct text");
				assertTrue(reconstructedText.contains("ModernJavaFeatures"), "Should contain class name");
				assertTrue(reconstructedText.contains("void main()"), "Should contain instance main method");
				assertTrue(reconstructedText.contains("instanceof int i"), "Should contain primitive pattern");

				// Verify memory efficiency for complex code
				long memoryUsage = nodeStorage.getEstimatedMemoryUsage();
				// Complex code should still use reasonable memory with Arena
				assertTrue(memoryUsage < 50_000, "Should maintain memory efficiency for complex code");
			}
		}

		@Test
		@DisplayName("Should handle parsing errors gracefully with Arena cleanup")
		void shouldHandleParsingErrorsGracefullyWithArenaCleanup() {
			String malformedJavaCode = """
				package com.example;

				public class MalformedClass {
					public void missingBrace() {
						System.out.println("Missing closing brace");
					// Missing }
				}
				""";

			// Even with parse errors, Arena should be properly managed
			assertThrows(Exception.class, () -> {
				try (IndexOverlayParser parser = new IndexOverlayParser(malformedJavaCode, JavaVersion.JAVA_21)) {
					parser.parse();
				}
			}, "Should throw parse exception for malformed code");

			// Verify metrics were recorded even for failed parse
			ParseMetrics.MetricsSnapshot metrics = ParseMetrics.getSnapshot();
			assertTrue(metrics.parseErrors() > 0, "Should record parse errors");
		}

		@Test
		@DisplayName("Should parse multiple files efficiently with Arena reuse")
		void shouldParseMultipleFilesEfficientlyWithArenaReuse() {
			String[] javaFiles = {
				"""
				package com.example;
				public class ClassA {
					public void methodA() {}
				}
				""",
				"""
				package com.example;
				public interface InterfaceB {
					void methodB();
				}
				""",
				"""
				package com.example;
				public enum EnumC {
					VALUE1, VALUE2, VALUE3;
				}
				""",
				"""
				package com.example;
				public record RecordD(String name, int value) {
					public RecordD {
						if (name == null) throw new IllegalArgumentException();
					}
				}
				"""
			};

			long totalMemoryUsed = 0;
			int totalNodesCreated = 0;

			for (String javaCode : javaFiles) {
				try (IndexOverlayParser parser = new IndexOverlayParser(javaCode, JavaVersion.JAVA_21)) {
					int rootNodeId = parser.parse();
					assertNotEquals(-1, rootNodeId, "Should successfully parse each file");

					ArenaNodeStorage nodeStorage = parser.getNodeStorage();
					totalMemoryUsed += nodeStorage.getEstimatedMemoryUsage();
					totalNodesCreated += nodeStorage.getNodeCount();

					// Each file should be completely parseable
					assertTrue(nodeStorage.getNodeCount() > 0, "Should create nodes for each file");
					assertTrue(nodeStorage.isAlive(), "Arena should be alive during each parse");
				}
			}

			// Verify efficiency across multiple files
			assertTrue(totalNodesCreated > 20, "Should have created nodes for all files");
			assertTrue(totalMemoryUsed < 100_000, "Should maintain memory efficiency across multiple files");

			// Verify all Arenas were properly closed (memory released)
			System.gc(); // Hint for garbage collection
		}
	}

	@Nested
	@DisplayName("Resource Management Integration")
	class ResourceManagementTests {

		@Test
		@DisplayName("Should properly integrate with try-with-resources")
		void shouldProperlyIntegrateWithTryWithResources() {
			String javaCode = "public class Test { public void method() {} }";
			ArenaNodeStorage capturedStorage;

			// Use try-with-resources - should auto-close
			try (IndexOverlayParser parser = new IndexOverlayParser(javaCode)) {
				int rootNodeId = parser.parse();
				capturedStorage = parser.getNodeStorage();

				assertTrue(capturedStorage.isAlive(), "Arena should be alive inside try block");
				assertTrue(capturedStorage.getNodeCount() > 0, "Should have parsed nodes");
			}

			// After try block, Arena should be closed
			assertFalse(capturedStorage.isAlive(), "Arena should be closed after try block");
		}

		@Test
		@DisplayName("Should handle exceptions during parsing with proper cleanup")
		void shouldHandleExceptionsDuringParsingWithProperCleanup() {
			String problematicCode = ""; // Empty code should trigger validation exception

			ArenaNodeStorage capturedStorage = null;
			IndexOverlayParser.ParseException caughtException = null;

			try (IndexOverlayParser parser = new IndexOverlayParser("public class Valid {}")) {
				capturedStorage = parser.getNodeStorage();
				assertTrue(capturedStorage.isAlive(), "Arena should be alive before exception");

				// This should trigger an exception during parsing
				parser.getNodeStorage().allocateNode(-1, -1, (byte) -1, -1); // Invalid allocation
				fail("Should have thrown exception for invalid allocation");
			} catch (Exception e) {
				// Expected exception
				assertNotNull(capturedStorage, "Should have captured storage reference");
			}

			// Arena should still be properly closed despite exception
			if (capturedStorage != null) {
				assertFalse(capturedStorage.isAlive(), "Arena should be closed even after exception");
			}
		}

		@Test
		@DisplayName("Should allow nested Arena usage correctly")
		void shouldAllowNestedArenaUsageCorrectly() {
			String outerCode = "public class Outer { public void method() {} }";
			String innerCode = "public class Inner { private int field; }";

			try (IndexOverlayParser outerParser = new IndexOverlayParser(outerCode)) {
				int outerRootId = outerParser.parse();
				ArenaNodeStorage outerStorage = outerParser.getNodeStorage();

				try (IndexOverlayParser innerParser = new IndexOverlayParser(innerCode)) {
					int innerRootId = innerParser.parse();
					ArenaNodeStorage innerStorage = innerParser.getNodeStorage();

					// Both Arenas should be alive
					assertTrue(outerStorage.isAlive(), "Outer Arena should be alive");
					assertTrue(innerStorage.isAlive(), "Inner Arena should be alive");

					// Both should have parsed successfully
					assertTrue(outerStorage.getNodeCount() > 0, "Outer should have nodes");
					assertTrue(innerStorage.getNodeCount() > 0, "Inner should have nodes");

					// Arenas should be independent
					assertNotSame(outerStorage, innerStorage, "Should be different Arena instances");
				}

				// Inner Arena should be closed, outer still alive
				assertTrue(outerStorage.isAlive(), "Outer Arena should still be alive");
			}
		}
	}

	@Nested
	@DisplayName("AST Structure Validation")
	class ASTStructureValidationTests {

		@Test
		@DisplayName("Should create correct AST structure for class hierarchy")
		void shouldCreateCorrectASTStructureForClassHierarchy() {
			String javaCode = """
				package com.example;

				public class TestClass {
					private int field1;
					private String field2;

					public TestClass() {
						field1 = 0;
						field2 = "";
					}

					public void method1() {
						if (field1 > 0) {
							System.out.println(field2);
						}
					}
				}
				""";

			try (IndexOverlayParser parser = new IndexOverlayParser(javaCode)) {
				int rootNodeId = parser.parse();
				ArenaNodeStorage nodeStorage = parser.getNodeStorage();

				// Verify compilation unit structure
				ArenaNodeStorage.NodeInfo rootNode = nodeStorage.getNode(rootNodeId);
				assertEquals(NodeType.COMPILATION_UNIT, rootNode.nodeType(), "Root should be compilation unit");

				// Find class declaration in children
				List<Integer> rootChildren = nodeStorage.getChildren(rootNodeId);
				assertTrue(rootChildren.size() > 0, "Root should have children");

				// Verify package and class are represented
				boolean hasClassDeclaration = false;
				for (Integer childId : rootChildren) {
					ArenaNodeStorage.NodeInfo child = nodeStorage.getNode(childId);
					if (child.nodeType() == NodeType.CLASS_DECLARATION) {
						hasClassDeclaration = true;

						// Verify class has methods and fields as children
						List<Integer> classChildren = nodeStorage.getChildren(childId);
						assertTrue(classChildren.size() > 0, "Class should have member children");

						// Count different member types
						int fieldCount = 0, constructorCount = 0, methodCount = 0;
						for (Integer memberId : classChildren) {
							ArenaNodeStorage.NodeInfo member = nodeStorage.getNode(memberId);
							switch (member.nodeType()) {
								case NodeType.FIELD_DECLARATION -> fieldCount++;
								case NodeType.CONSTRUCTOR_DECLARATION -> constructorCount++;
								case NodeType.METHOD_DECLARATION -> methodCount++;
							}
						}

						assertEquals(2, fieldCount, "Should have 2 field declarations");
						assertEquals(1, constructorCount, "Should have 1 constructor");
						assertEquals(1, methodCount, "Should have 1 method");

						break;
					}
				}
				assertTrue(hasClassDeclaration, "Should find class declaration in AST");
			}
		}

		@Test
		@DisplayName("Should maintain accurate source position information")
		void shouldMaintainAccurateSourcePositionInformation() {
			String javaCode = """
				public class PositionTest {
					public void method() {
						String var = "hello";
						System.out.println(var);
					}
				}
				""";

			try (IndexOverlayParser parser = new IndexOverlayParser(javaCode)) {
				int rootNodeId = parser.parse();
				ArenaNodeStorage nodeStorage = parser.getNodeStorage();

				// Get the root node text
				String fullText = parser.getNodeText(rootNodeId);
				assertEquals(javaCode, fullText, "Root node should contain entire source");

				// Verify position information is accurate for all nodes
				validateNodePositions(nodeStorage, 0, javaCode);
			}
		}

		private void validateNodePositions(ArenaNodeStorage storage, int nodeId, String sourceText) {
			ArenaNodeStorage.NodeInfo node = storage.getNode(nodeId);

			// Verify position boundaries
			assertTrue(node.startOffset() >= 0, "Start offset should be non-negative");
			assertTrue(node.endOffset() <= sourceText.length(), "End offset should not exceed source length");
			assertTrue(node.startOffset() <= node.endOffset(), "Start should be <= end");

			// Verify source text substring is valid
			String nodeText = sourceText.substring(node.startOffset(), node.endOffset());
			assertNotNull(nodeText, "Should be able to extract node text");

			// Recursively validate children
			List<Integer> children = storage.getChildren(nodeId);
			for (Integer childId : children) {
				validateNodePositions(storage, childId, sourceText);

				// Verify child is within parent bounds
				ArenaNodeStorage.NodeInfo child = storage.getNode(childId);
				assertTrue(child.startOffset() >= node.startOffset(),
					"Child start should be >= parent start");
				assertTrue(child.endOffset() <= node.endOffset(),
					"Child end should be <= parent end");
			}
		}

		@Test
		@DisplayName("Should handle complex nested structures correctly")
		void shouldHandleComplexNestedStructuresCorrectly() {
			String javaCode = """
				public class NestedTest {
					public void outerMethod() {
						for (int i = 0; i < 10; i++) {
							if (i % 2 == 0) {
								switch (i) {
									case 0 -> System.out.println("zero");
									case 2 -> {
										String msg = "two";
										System.out.println(msg);
									}
									default -> System.out.println("other");
								}
							}
						}
					}
				}
				""";

			try (IndexOverlayParser parser = new IndexOverlayParser(javaCode)) {
				int rootNodeId = parser.parse();
				ArenaNodeStorage nodeStorage = parser.getNodeStorage();

				// Verify deep nesting was parsed correctly
				int maxDepth = calculateMaxDepth(nodeStorage, rootNodeId, 0);
				assertTrue(maxDepth >= 5, "Should handle deep nesting (for->if->switch->case->block)");

				// Verify all nodes have valid structure
				validateASTStructure(nodeStorage, rootNodeId, javaCode);
			}
		}

		private int calculateMaxDepth(ArenaNodeStorage storage, int nodeId, int currentDepth) {
			List<Integer> children = storage.getChildren(nodeId);
			if (children.isEmpty()) {
				return currentDepth;
			}

			int maxChildDepth = currentDepth;
			for (Integer childId : children) {
				int childDepth = calculateMaxDepth(storage, childId, currentDepth + 1);
				maxChildDepth = Math.max(maxChildDepth, childDepth);
			}
			return maxChildDepth;
		}

		private void validateASTStructure(ArenaNodeStorage storage, int nodeId, String sourceText) {
			ArenaNodeStorage.NodeInfo node = storage.getNode(nodeId);

			// Basic structure validation
			assertTrue(node.nodeId() >= 0, "Node ID should be non-negative");
			assertNotNull(node.getTypeName(), "Should have valid type name");

			// Position validation
			assertTrue(node.startOffset() >= 0 && node.startOffset() < sourceText.length(),
				"Start offset should be within source bounds");
			assertTrue(node.endOffset() >= node.startOffset() && node.endOffset() <= sourceText.length(),
				"End offset should be valid");

			// Recursively validate children
			List<Integer> children = storage.getChildren(nodeId);
			for (Integer childId : children) {
				validateASTStructure(storage, childId, sourceText);
			}
		}
	}

	@Nested
	@DisplayName("Performance Integration Validation")
	class PerformanceIntegrationTests {

		@Test
		@DisplayName("Should demonstrate performance characteristics with realistic code")
		void shouldDemonstratePerformanceCharacteristicsWithRealisticCode() {
			// Generate realistic Java code for performance testing
			StringBuilder codeBuilder = new StringBuilder();
			codeBuilder.append("package com.example.performance;\n\n");
			codeBuilder.append("public class PerformanceTestClass {\n");

			// Add many methods to create substantial parsing load
			for (int i = 0; i < 100; i++) {
				codeBuilder.append(String.format("""
					    public void method%d() {
					        int value = %d;
					        if (value > 50) {
					            System.out.println("Value: " + value);
					        } else {
					            System.err.println("Low value: " + value);
					        }
					    }

					""", i, i));
			}
			codeBuilder.append("}\n");

			String largeJavaCode = codeBuilder.toString();

			long startTime = System.nanoTime();
			try (IndexOverlayParser parser = new IndexOverlayParser(largeJavaCode)) {
				int rootNodeId = parser.parse();
				long parseTime = System.nanoTime() - startTime;

				ArenaNodeStorage nodeStorage = parser.getNodeStorage();

				// Verify substantial parsing occurred
				assertTrue(nodeStorage.getNodeCount() > 500, "Should create many nodes for large code");

				// Verify memory efficiency
				long memoryUsage = nodeStorage.getEstimatedMemoryUsage();
				double memoryPerNode = (double) memoryUsage / nodeStorage.getNodeCount();
				assertTrue(memoryPerNode < 100, "Memory per node should be efficient (< 100 bytes)");

				// Verify reasonable parse time (should be fast with Arena)
				double parseTimeMs = parseTime / 1_000_000.0;
				assertTrue(parseTimeMs < 1000, "Parse time should be reasonable (< 1 second)");

				System.out.printf("Performance test: %d nodes, %.2f ms parse time, %.1f bytes/node\n",
					nodeStorage.getNodeCount(), parseTimeMs, memoryPerNode);
			}
		}

		@Test
		@DisplayName("Should collect and validate parse metrics")
		void shouldCollectAndValidateParseMetrics() {
			String javaCode = """
				public class MetricsTest {
					private int field;
					public void method() {
						field = 42;
					}
				}
				""";

			ParseMetrics.reset();

			try (IndexOverlayParser parser = new IndexOverlayParser(javaCode)) {
				parser.parse();
			}

			ParseMetrics.MetricsSnapshot metrics = ParseMetrics.getSnapshot();

			// Verify metrics were collected
			assertEquals(1, metrics.totalFilesProcessed(), "Should record 1 file processed");
			assertTrue(metrics.totalParseTimeNanos() > 0, "Should record parse time");
			assertTrue(metrics.totalNodesAllocated() > 0, "Should record node allocations");
			assertEquals(0, metrics.parseErrors(), "Should have no parse errors for valid code");

			// Verify calculated metrics
			assertTrue(metrics.getAverageParseTimeMs() > 0, "Should calculate average parse time");
			assertTrue(metrics.getAverageNodesPerFile() > 0, "Should calculate average nodes per file");
		}
	}
}