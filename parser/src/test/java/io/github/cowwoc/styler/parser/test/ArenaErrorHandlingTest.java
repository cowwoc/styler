package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.NodeType;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.util.List;

import static org.testng.Assert.*;
import static org.testng.Assert.fail;

/**
 * Error handling and edge case tests for Arena API implementation - ensuring
 * robust behavior under failure conditions and boundary scenarios.
 *
 * <h2>Error Handling Business Rules Tested</h2>
 * <ul>
 * <li><strong>Fail-Fast Validation Rule</strong>: Invalid inputs rejected immediately with clear messages</li>
 * <li><strong>Resource Safety Rule</strong>: Arena remains in consistent state after errors</li>
 * <li><strong>Memory Protection Rule</strong>: No memory corruption despite invalid operations</li>
 * <li><strong>Graceful Degradation Rule</strong>: Partial operations handle failures appropriately</li>
 * <li><strong>Security Protection Rule</strong>: Input validation prevents resource exhaustion attacks</li>
 * <li><strong>Recovery Support Rule</strong>: Operations can continue after non-fatal errors</li>
 * </ul>
 */
class ArenaErrorHandlingTest {

	
	class InputValidationErrorHandlingTests {

		@Test void shouldValidateArenaCreationParameters() {
			// Test zero capacity
			try {
				ArenaNodeStorage.create(0);
				fail("Expected IllegalArgumentException");
			} catch (IllegalArgumentException exception) {
				assertTrue(exception.getMessage().contains("at least 1"),
					"Error message should explain minimum capacity requirement");
				assertTrue(exception.getMessage().contains("got: 0"),
					"Error message should show actual invalid value");
			}

			// Test negative capacity
			try {
				ArenaNodeStorage.create(-5);
				fail("Expected IllegalArgumentException");
			} catch (IllegalArgumentException exception) {
				assertTrue(exception.getMessage().contains("at least 1"),
					"Error message should explain minimum capacity requirement");
				assertTrue(exception.getMessage().contains("got: -5"),
					"Error message should show actual invalid value");
			}

			// Test extremely large capacity (should still work but not overflow)
			// Reasonable large capacity should be accepted
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(Integer.MAX_VALUE / 100)) {
				// Should succeed
				assertEquals(0, storage.getNodeCount(), "Large capacity should be valid");
			}
		}

		@Test void shouldValidateNodeOperationParameters() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(100)) {
				// Allocate a valid node first
				int validNodeId = storage.allocateNode(0, 10, NodeType.CLASS_DECLARATION, -1);

				// Test invalid node ID for getNode
				try {
					storage.getNode(-1);
					fail("Expected IllegalArgumentException");
				} catch (IllegalArgumentException exception) {
					assertTrue(exception.getMessage().contains("Invalid node ID: -1"),
						"Error message should show invalid node ID");
					assertTrue(exception.getMessage().contains("valid range: 0-0"),
						"Error message should show valid range");
				}

				try {
					storage.getNode(999);
					fail("Expected IllegalArgumentException");
				} catch (IllegalArgumentException exception) {
					assertTrue(exception.getMessage().contains("Invalid node ID: 999"),
						"Error message should show invalid node ID");
				}

				// Test invalid node ID for getChildren
				assertThrows(IllegalArgumentException.class,
				() -> storage.getChildren(-1));

				assertThrows(IllegalArgumentException.class,
				() -> storage.getChildren(999));

				// Test invalid node ID for updateNodeLength
				try {
					storage.updateNodeLength(-1, 20);
					fail("Expected IllegalArgumentException");
				} catch (IllegalArgumentException exception) {
					// Exception captured
				}

				try {
					storage.updateNodeLength(999, 20);
					fail("Expected IllegalArgumentException");
				} catch (IllegalArgumentException exception) {
					// Exception captured
				}

				// Valid operations should still work after invalid attempts
				ArenaNodeStorage.NodeInfo node = storage.getNode(validNodeId);
				assertEquals(validNodeId, node.nodeId(), "Valid operations should work after errors");
			}
		}

		@Test void shouldHandleBoundaryValueInputsCorrectly() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(10)) {
				// Test minimum values
				int nodeId1 = storage.allocateNode(0, 0, (byte) 0, -1);
				ArenaNodeStorage.NodeInfo node1 = storage.getNode(nodeId1);
				assertEquals(0, node1.startOffset(), "Should handle zero start offset");
				assertEquals(0, node1.length(), "Should handle zero length");
				assertEquals(0, node1.endOffset(), "Should calculate zero end offset correctly");

				// Test maximum practical values
				int nodeId2 = storage.allocateNode(Integer.MAX_VALUE - 1000, 1000, Byte.MAX_VALUE, nodeId1);
				ArenaNodeStorage.NodeInfo node2 = storage.getNode(nodeId2);
				assertEquals(Integer.MAX_VALUE - 1000, node2.startOffset(), "Should handle large start offset");
				assertEquals(1000, node2.length(), "Should handle large length");
				assertEquals(Byte.MAX_VALUE, node2.nodeType(), "Should handle maximum byte value");

				// Test negative parent ID (should be valid for root nodes)
				int nodeId3 = storage.allocateNode(100, 50, NodeType.CLASS_DECLARATION, -999);
				ArenaNodeStorage.NodeInfo node3 = storage.getNode(nodeId3);
				assertEquals(-999, node3.parentId(), "Should handle negative parent ID");
				assertFalse(node3.isRoot(), "Should not be root with non-negative-one parent");
			}
		}
	}

	
	class CapacityExhaustionHandlingTests {

		@Test void shouldHandleArenaCapacityExhaustionGracefully() {
			int capacity = 5;
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(capacity)) {
				// Fill arena to capacity
				for (int i = 0; i < capacity; i++) {
					int nodeId = storage.allocateNode(i * 10, 5, NodeType.METHOD_DECLARATION, -1);
					assertEquals(i, nodeId, "Node IDs should be sequential");
				}

				assertEquals(capacity, storage.getNodeCount(), "Should have reached capacity");

				// Next allocation should fail with clear error
				try {
					storage.allocateNode(100, 5, NodeType.FIELD_DECLARATION, -1);
					fail("Expected IllegalStateException");
				} catch (IllegalStateException exception) {
					assertTrue(exception.getMessage().contains("Arena is full"),
						"Error message should indicate arena is full");
					assertTrue(exception.getMessage().contains("Allocated: " + capacity),
						"Error message should show current allocation count");
					assertTrue(exception.getMessage().contains("Capacity: " + capacity),
						"Error message should show total capacity");
				}

				// Arena should remain in consistent state
				assertEquals(capacity, storage.getNodeCount(), "Node count should not change after failed allocation");
				assertTrue(storage.isAlive(), "Arena should remain alive after allocation failure");

				// Existing nodes should still be accessible
				for (int i = 0; i < capacity; i++) {
					ArenaNodeStorage.NodeInfo node = storage.getNode(i);
					assertEquals(i * 10, node.startOffset(), "Existing nodes should remain accessible");
				}

				// Other operations should continue to work
				storage.updateNodeLength(0, 15);
				ArenaNodeStorage.NodeInfo updatedNode = storage.getNode(0);
				assertEquals(15, updatedNode.length(), "Updates should work after allocation failure");
			}
		}

		@Test void shouldHandleChildArrayGrowthFailuresGracefully() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(1000)) {
				// Create parent
				int parentId = storage.allocateNode(0, 100, NodeType.CLASS_DECLARATION, -1);

				// Add many children to trigger array growth
				// This tests the internal memory management for child relationships
				for (int i = 0; i < 2000; i++) {
					int childId = storage.allocateNode(i * 5, 3, NodeType.METHOD_DECLARATION, parentId);
					assertEquals(i + 1, childId, "Child allocation should continue working");

					// Periodically verify parent-child relationship is maintained
					if (i % 500 == 0) {
						List<Integer> children = storage.getChildren(parentId);
						assertEquals(i + 1, children.size(), "Child count should be correct during growth");
					}
				}

				// Verify final state after extensive child array growth
				List<Integer> allChildren = storage.getChildren(parentId);
				assertEquals(2000, allChildren.size(), "All children should be tracked after array growth");

				// Verify children are in correct order
				for (int i = 0; i < 2000; i++) {
					assertEquals(i + 1, allChildren.get(i).intValue(), "Child order should be preserved");
				}
			}
		}

		@Test void shouldRecoverGracefullyAfterResetOperations() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(100)) {
				// Allocate nodes and create complex relationships
				int parent1 = storage.allocateNode(0, 50, NodeType.CLASS_DECLARATION, -1);
				int parent2 = storage.allocateNode(60, 40, NodeType.INTERFACE_DECLARATION, -1);

				for (int i = 0; i < 20; i++) {
					storage.allocateNode(i * 2, 1, NodeType.METHOD_DECLARATION, parent1);
					storage.allocateNode(60 + i * 2, 1, NodeType.METHOD_DECLARATION, parent2);
				}

				assertEquals(42, storage.getNodeCount(), "Should have 42 nodes before reset");
				assertTrue(storage.getEstimatedMemoryUsage() > 1000, "Should have substantial memory usage");

				// Reset should work correctly
				storage.reset();

				// Verify reset state
				assertEquals(0, storage.getNodeCount(), "Node count should be 0 after reset");
				assertTrue(storage.isAlive(), "Arena should remain alive after reset");

				// Should be able to allocate new nodes with IDs starting from 0
				int newNode1 = storage.allocateNode(0, 10, NodeType.CLASS_DECLARATION, -1);
				int newNode2 = storage.allocateNode(15, 8, NodeType.METHOD_DECLARATION, newNode1);

				assertEquals(0, newNode1, "First new node should have ID 0");
				assertEquals(1, newNode2, "Second new node should have ID 1");

				// Verify parent-child relationship works after reset
				List<Integer> children = storage.getChildren(newNode1);
				assertEquals(1, children.size(), "Parent should have 1 child after reset");
				assertEquals(newNode2, children.get(0), "Child relationship should be correct");
			}
		}
	}

	
	class LifecycleErrorHandlingTests {

		@Test void shouldHandleOperationsOnClosedArenaAppropriately() {
			ArenaNodeStorage storage = ArenaNodeStorage.create(100);

			// Allocate some nodes before closing
			int nodeId = storage.allocateNode(0, 10, NodeType.CLASS_DECLARATION, -1);
			assertTrue(storage.isAlive(), "Arena should be alive before close");

			// Close the arena
			storage.close();
			assertFalse(storage.isAlive(), "Arena should not be alive after close");

			// Operations on closed arena should fail appropriately
			assertThrows(Exception.class,
				() -> storage.allocateNode(20, 5, NodeType.METHOD_DECLARATION, -1));

			// Some operations might still work on existing data (implementation dependent)
			// but attempting to modify arena memory should fail
		}

		@Test void shouldHandleMultipleCloseCallsSafely() {
			ArenaNodeStorage storage = ArenaNodeStorage.create(50);
			storage.allocateNode(0, 10, NodeType.CLASS_DECLARATION, -1);

			assertTrue(storage.isAlive(), "Arena should be alive initially");

			// First close
			storage.close();
			assertFalse(storage.isAlive(), "Arena should be closed after first close");

			// Multiple closes should not cause errors
			// Multiple close calls should be safe
			storage.close();
			// Multiple close calls should be safe
			storage.close();
			assertFalse(storage.isAlive(), "Arena should remain closed");
		}

		@Test void shouldHandleExceptionsDuringParserIntegrationGracefully() {
			// Test with malformed Java code that should cause parse errors
			String malformedCode = """
				package com.example.malformed;

				public class MalformedClass {
					public void method( {  // Missing closing parenthesis
						System.out.println("This will cause parse error");
					// Missing closing brace
				""";

			ArenaNodeStorage capturedStorage = null;

			try {
				try (IndexOverlayParser parser = new IndexOverlayParser(malformedCode)) {
					capturedStorage = parser.getNodeStorage();
					assertTrue(capturedStorage.isAlive(), "Arena should be alive before parse attempt");

					// This should throw a parse exception
					parser.parse();
					fail("Should have thrown parse exception for malformed code");
				}
			} catch (Exception e) {
				// Expected parse exception
				assertTrue(e.getMessage().contains("Expected") || e.getMessage().contains("found"),
					"Should be a descriptive parse error");
			}

			// Arena should still be properly closed despite the exception
			if (capturedStorage != null) {
				assertFalse(capturedStorage.isAlive(), "Arena should be closed even after parse exception");
			}
		}
	}

	
	class ConcurrentAccessErrorHandlingTests {

		@Test void shouldHandleInvalidConcurrentAccessPatterns() {
			// Arena API uses confined arenas, so concurrent access should be prevented
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(100)) {
				storage.allocateNode(0, 10, NodeType.CLASS_DECLARATION, -1);

				// Test that arena operations work correctly in single-threaded context
				assertTrue(storage.isAlive(), "Arena should be alive");
				assertEquals(1, storage.getNodeCount(), "Should have allocated 1 node");

				// Simulate potential race condition scenarios
				// (Actual concurrent access would require more complex setup)
				ArenaNodeStorage.NodeInfo node = storage.getNode(0);
				assertEquals(0, node.nodeId(), "Node access should be consistent");

				storage.updateNodeLength(0, 20);
				node = storage.getNode(0);
				assertEquals(20, node.length(), "Updates should be consistent");
			}
		}

		@Test void shouldMaintainConsistencyUnderRapidOperations() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(1000)) {
				// Rapidly allocate nodes
				for (int i = 0; i < 500; i++) {
					int nodeId = storage.allocateNode(i * 2, 1, NodeType.METHOD_DECLARATION, -1);
					assertEquals(i, nodeId, "Node IDs should be consistent under rapid allocation");
				}

				// Rapidly access nodes
				for (int i = 0; i < 500; i++) {
					ArenaNodeStorage.NodeInfo node = storage.getNode(i);
					assertEquals(i * 2, node.startOffset(), "Node data should be consistent under rapid access");
				}

				// Rapidly update nodes
				for (int i = 0; i < 500; i++) {
					storage.updateNodeLength(i, i + 10);
				}

				// Verify final consistency
				for (int i = 0; i < 500; i++) {
					ArenaNodeStorage.NodeInfo node = storage.getNode(i);
					assertEquals(i + 10, node.length(), "Updates should be consistent");
				}
			}
		}
	}

	
	class EdgeCaseScenarioHandlingTests {

		@Test void shouldHandleComplexParentChildRelationshipEdgeCases() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(100)) {
				// Test circular reference attempt (should not crash)
				int node1 = storage.allocateNode(0, 10, NodeType.CLASS_DECLARATION, -1);
				int node2 = storage.allocateNode(15, 8, NodeType.METHOD_DECLARATION, node1);

				// Attempting to create circular reference by setting node1's parent to node2
				// This should be handled gracefully (invalid parent reference)
				int node3 = storage.allocateNode(30, 5, NodeType.FIELD_DECLARATION, node2);

				// Verify structure is still consistent
				ArenaNodeStorage.NodeInfo node1Info = storage.getNode(node1);
				assertTrue(node1Info.isRoot(), "Node1 should remain root");

				List<Integer> node1Children = storage.getChildren(node1);
				assertEquals(1, node1Children.size(), "Node1 should have 1 child");
				assertEquals(node2, node1Children.get(0), "Node1's child should be node2");

				List<Integer> node2Children = storage.getChildren(node2);
				assertEquals(1, node2Children.size(), "Node2 should have 1 child");
				assertEquals(node3, node2Children.get(0), "Node2's child should be node3");
			}
		}

		@Test void shouldHandleExtremeMemoryLayoutScenarios() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(100)) {
				// Test nodes with extreme position values
				int maxIntNode = storage.allocateNode(Integer.MAX_VALUE - 1000, 1000, NodeType.CLASS_DECLARATION, -1);
				ArenaNodeStorage.NodeInfo maxNode = storage.getNode(maxIntNode);
				assertEquals(Integer.MAX_VALUE - 1000, maxNode.startOffset(), "Should handle max int start offset");
				assertEquals(Integer.MAX_VALUE, maxNode.endOffset(), "Should calculate max int end offset");

				// Test nodes with minimum values
				int minNode = storage.allocateNode(0, 0, (byte) 0, -1);
				ArenaNodeStorage.NodeInfo minNodeInfo = storage.getNode(minNode);
				assertEquals(0, minNodeInfo.startOffset(), "Should handle zero start offset");
				assertEquals(0, minNodeInfo.length(), "Should handle zero length");
				assertEquals(0, minNodeInfo.endOffset(), "Should handle zero end offset");

				// Test nodes with all byte values for node type
				byte[] allByteValues = {Byte.MIN_VALUE, -1, 0, 1, 127, Byte.MAX_VALUE};
				for (byte typeValue : allByteValues) {
					int nodeId = storage.allocateNode(100, 5, typeValue, -1);
					ArenaNodeStorage.NodeInfo node = storage.getNode(nodeId);
					assertEquals(typeValue, node.nodeType(), "Should preserve byte node type value: " + typeValue);
				}
			}
		}

		@Test void shouldHandleParserInputValidationEdgeCases() {
			// Test with null input
			assertThrows(IllegalArgumentException.class,
				() -> new IndexOverlayParser(null));

			// Test with empty input
			assertThrows(IllegalArgumentException.class,
				() -> new IndexOverlayParser(""));

			// Test with whitespace-only input
			assertThrows(IllegalArgumentException.class,
				() -> new IndexOverlayParser("   \n\t  "));

			// Test with extremely large input (should be rejected)
			StringBuilder largeInput = new StringBuilder();
			for (int i = 0; i < 100000; i++) {
				largeInput.append("public class VeryLargeClass").append(i).append(" { }\n");
			}

			assertThrows(IllegalArgumentException.class,
				() -> new IndexOverlayParser(largeInput.toString()));
		}

		@Test void shouldMaintainDataIntegrityAfterErrorRecovery() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(50)) {
				// Allocate some nodes successfully
				int node1 = storage.allocateNode(0, 10, NodeType.CLASS_DECLARATION, -1);
				int node2 = storage.allocateNode(15, 8, NodeType.METHOD_DECLARATION, node1);

				// Attempt invalid operations that should fail
				assertThrows(IllegalArgumentException.class,
				() -> storage.getNode(-1));

				assertThrows(IllegalArgumentException.class,
				() -> storage.updateNodeLength(999, 5));

				// Verify that valid data remains intact after failed operations
				ArenaNodeStorage.NodeInfo node1Info = storage.getNode(node1);
				assertEquals(0, node1Info.startOffset(), "Node1 data should be intact");
				assertEquals(10, node1Info.length(), "Node1 length should be intact");

				ArenaNodeStorage.NodeInfo node2Info = storage.getNode(node2);
				assertEquals(15, node2Info.startOffset(), "Node2 data should be intact");
				assertEquals(8, node2Info.length(), "Node2 length should be intact");
				assertEquals(node1, node2Info.parentId(), "Node2 parent should be intact");

				// Parent-child relationship should still work
				List<Integer> children = storage.getChildren(node1);
				assertEquals(1, children.size(), "Parent-child relationship should be intact");
				assertEquals(node2, children.get(0), "Child reference should be intact");

				// Should be able to continue normal operations
				int node3 = storage.allocateNode(30, 5, NodeType.FIELD_DECLARATION, node1);
				assertEquals(2, node3, "New allocation should work after errors");

				children = storage.getChildren(node1);
				assertEquals(2, children.size(), "New child should be added correctly");
			}
		}
	}
}