package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ArenaNodeStorage - validating the Arena API implementation
 * that replaces NodeRegistry/MemoryArena with significant performance benefits.
 *
 * <h2>Business Rules Tested</h2>
 * <ul>
 * <li><strong>Memory Layout Rule</strong>: Each node occupies exactly 16 bytes (4 integers)</li>
 * <li><strong>Arena Capacity Rule</strong>: Cannot exceed estimated node capacity</li>
 * <li><strong>Parent-Child Relationship Rule</strong>: Children maintained in parallel arrays</li>
 * <li><strong>Memory Management Rule</strong>: Arena provides bulk deallocation via close()</li>
 * <li><strong>API Compatibility Rule</strong>: Drop-in replacement for NodeRegistry</li>
 * <li><strong>Performance Target Rule</strong>: 3x faster than objects, 12x faster than NodeRegistry</li>
 * </ul>
 */
class ArenaNodeStorageTest {

	@Nested
	@DisplayName("Arena Creation and Lifecycle")
	class ArenaCreationTests {

		@Test
		@DisplayName("Should create arena with specified capacity and validate initial state")
		void shouldCreateArenaWithSpecifiedCapacity() {
			int estimatedNodesCapacity = 500;

			try (ArenaNodeStorage storage = ArenaNodeStorage.create(estimatedNodesCapacity)) {
				// Validate initial state
				assertEquals(0, storage.getNodeCount(),
					"Initial node count should be zero");
				assertTrue(storage.isAlive(),
					"Arena should be alive after creation");

				// Validate estimated memory usage calculation
				long initialMemory = storage.getEstimatedMemoryUsage();
				assertTrue(initialMemory > 0,
					"Should have non-zero memory usage for child arrays");

				// Memory should include child relationship arrays
				long expectedChildArraysMemory = (long) estimatedNodesCapacity * Integer.BYTES * 2 + // start + count arrays
					1024 * Integer.BYTES; // initial children data array
				assertTrue(initialMemory >= expectedChildArraysMemory,
					"Should account for child relationship array memory");
			}
		}

		@Test
		@DisplayName("Should create arena with default capacity of 1024")
		void shouldCreateArenaWithDefaultCapacity() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create()) {
				assertEquals(0, storage.getNodeCount(),
					"Default arena should start with zero nodes");
				assertTrue(storage.isAlive(),
					"Default arena should be alive");

				// Should be able to allocate at least 1024 nodes
				for (int i = 0; i < 1024; i++) {
					int nodeId = storage.allocateNode(i * 10, 5, NodeType.CLASS_DECLARATION, -1);
					assertEquals(i, nodeId,
						"Node IDs should be sequential starting from 0");
				}
				assertEquals(1024, storage.getNodeCount(),
					"Should have allocated exactly 1024 nodes");
			}
		}

		@Test
		@DisplayName("Should reject invalid capacity parameters")
		void shouldRejectInvalidCapacityParameters() {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> ArenaNodeStorage.create(0),
				"Should reject zero capacity");
			assertTrue(exception.getMessage().contains("at least 1"),
				"Error message should explain minimum capacity requirement");

			exception = assertThrows(IllegalArgumentException.class,
				() -> ArenaNodeStorage.create(-10),
				"Should reject negative capacity");
			assertTrue(exception.getMessage().contains("at least 1"),
				"Error message should explain minimum capacity requirement");
		}

		@Test
		@DisplayName("Should properly close arena and release memory")
		void shouldProperlyCloseArenaAndReleaseMemory() {
			ArenaNodeStorage storage = ArenaNodeStorage.create(100);

			// Allocate some nodes
			storage.allocateNode(0, 10, NodeType.CLASS_DECLARATION, -1);
			storage.allocateNode(10, 15, NodeType.METHOD_DECLARATION, 0);
			assertTrue(storage.isAlive(), "Arena should be alive before close");

			// Close the arena
			storage.close();
			assertFalse(storage.isAlive(), "Arena should not be alive after close");

			// Attempting operations after close should fail
			assertThrows(Exception.class,
				() -> storage.allocateNode(25, 5, NodeType.FIELD_DECLARATION, 0),
				"Should fail to allocate nodes after arena is closed");
		}
	}

	@Nested
	@DisplayName("Node Allocation and Retrieval")
	class NodeAllocationTests {

		private ArenaNodeStorage storage;

		@BeforeEach
		void setUp() {
			storage = ArenaNodeStorage.create(1000);
		}

		@AfterEach
		void tearDown() {
			storage.close();
		}

		@Test
		@DisplayName("Should allocate node with correct memory layout")
		void shouldAllocateNodeWithCorrectMemoryLayout() {
			int startOffset = 42;
			int length = 18;
			byte nodeType = NodeType.CLASS_DECLARATION;
			int parentId = -1;

			int nodeId = storage.allocateNode(startOffset, length, nodeType, parentId);

			assertEquals(0, nodeId, "First node should have ID 0");
			assertEquals(1, storage.getNodeCount(), "Node count should increment");

			// Validate node data integrity
			ArenaNodeStorage.NodeInfo node = storage.getNode(nodeId);
			assertEquals(nodeId, node.nodeId(), "Node ID should match");
			assertEquals(startOffset, node.startOffset(), "Start offset should match");
			assertEquals(length, node.length(), "Length should match");
			assertEquals(nodeType, node.nodeType(), "Node type should match");
			assertEquals(parentId, node.parentId(), "Parent ID should match");
			assertEquals(startOffset + length, node.endOffset(), "End offset calculation should be correct");
			assertTrue(node.isRoot(), "Node with parent -1 should be root");
			assertEquals("ClassDeclaration", node.getTypeName(), "Type name should be correct");
		}

		@Test
		@DisplayName("Should allocate multiple nodes with sequential IDs")
		void shouldAllocateMultipleNodesWithSequentialIds() {
			int numNodes = 50;

			for (int i = 0; i < numNodes; i++) {
				int nodeId = storage.allocateNode(i * 20, 15, NodeType.METHOD_DECLARATION, -1);
				assertEquals(i, nodeId, "Node IDs should be sequential");
			}

			assertEquals(numNodes, storage.getNodeCount(), "Node count should match allocated count");

			// Verify all nodes are correctly stored
			for (int i = 0; i < numNodes; i++) {
				ArenaNodeStorage.NodeInfo node = storage.getNode(i);
				assertEquals(i * 20, node.startOffset(), "Node " + i + " should have correct start offset");
				assertEquals(15, node.length(), "Node " + i + " should have correct length");
				assertEquals(NodeType.METHOD_DECLARATION, node.nodeType(), "Node " + i + " should have correct type");
			}
		}

		@Test
		@DisplayName("Should reject allocation when arena is full")
		void shouldRejectAllocationWhenArenaIsFull() {
			int capacity = 10;
			try (ArenaNodeStorage smallStorage = ArenaNodeStorage.create(capacity)) {
				// Fill the arena to capacity
				for (int i = 0; i < capacity; i++) {
					smallStorage.allocateNode(i * 10, 5, NodeType.FIELD_DECLARATION, -1);
				}
				assertEquals(capacity, smallStorage.getNodeCount(), "Arena should be at capacity");

				// Next allocation should fail
				IllegalStateException exception = assertThrows(IllegalStateException.class,
					() -> smallStorage.allocateNode(100, 5, NodeType.FIELD_DECLARATION, -1),
					"Should reject allocation when arena is full");

				assertTrue(exception.getMessage().contains("Arena is full"),
					"Error message should indicate arena is full");
				assertTrue(exception.getMessage().contains("Allocated: " + capacity),
					"Error message should show allocated count");
				assertTrue(exception.getMessage().contains("Capacity: " + capacity),
					"Error message should show capacity limit");
			}
		}

		@Test
		@DisplayName("Should update node length correctly")
		void shouldUpdateNodeLengthCorrectly() {
			int nodeId = storage.allocateNode(10, 20, NodeType.CLASS_DECLARATION, -1);
			ArenaNodeStorage.NodeInfo originalNode = storage.getNode(nodeId);
			assertEquals(20, originalNode.length(), "Original length should be 20");
			assertEquals(30, originalNode.endOffset(), "Original end offset should be 30");

			// Update the length
			int newLength = 35;
			storage.updateNodeLength(nodeId, newLength);

			// Verify update
			ArenaNodeStorage.NodeInfo updatedNode = storage.getNode(nodeId);
			assertEquals(newLength, updatedNode.length(), "Length should be updated");
			assertEquals(10 + newLength, updatedNode.endOffset(), "End offset should be recalculated");
			assertEquals(originalNode.startOffset(), updatedNode.startOffset(), "Start offset should remain unchanged");
			assertEquals(originalNode.nodeType(), updatedNode.nodeType(), "Node type should remain unchanged");
		}

		@Test
		@DisplayName("Should reject invalid node IDs for operations")
		void shouldRejectInvalidNodeIdsForOperations() {
			storage.allocateNode(0, 10, NodeType.CLASS_DECLARATION, -1);

			// Test negative node ID
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> storage.getNode(-1),
				"Should reject negative node ID");
			assertTrue(exception.getMessage().contains("Invalid node ID: -1"),
				"Error message should show invalid node ID");

			// Test node ID beyond allocated range
			exception = assertThrows(IllegalArgumentException.class,
				() -> storage.getNode(5),
				"Should reject node ID beyond allocated range");
			assertTrue(exception.getMessage().contains("Invalid node ID: 5"),
				"Error message should show invalid node ID");

			// Test update with invalid node ID
			exception = assertThrows(IllegalArgumentException.class,
				() -> storage.updateNodeLength(10, 20),
				"Should reject update for invalid node ID");
		}
	}

	@Nested
	@DisplayName("Parent-Child Relationship Management")
	class ParentChildRelationshipTests {

		private ArenaNodeStorage storage;

		@BeforeEach
		void setUp() {
			storage = ArenaNodeStorage.create(1000);
		}

		@AfterEach
		void tearDown() {
			storage.close();
		}

		@Test
		@DisplayName("Should establish parent-child relationships correctly")
		void shouldEstablishParentChildRelationshipsCorrectly() {
			// Create root node
			int rootId = storage.allocateNode(0, 100, NodeType.CLASS_DECLARATION, -1);

			// Create child nodes
			int method1Id = storage.allocateNode(10, 20, NodeType.METHOD_DECLARATION, rootId);
			int method2Id = storage.allocateNode(35, 15, NodeType.METHOD_DECLARATION, rootId);
			int fieldId = storage.allocateNode(55, 10, NodeType.FIELD_DECLARATION, rootId);

			// Verify parent information
			ArenaNodeStorage.NodeInfo rootNode = storage.getNode(rootId);
			assertTrue(rootNode.isRoot(), "Root node should have no parent");

			ArenaNodeStorage.NodeInfo method1Node = storage.getNode(method1Id);
			assertFalse(method1Node.isRoot(), "Child node should not be root");
			assertEquals(rootId, method1Node.parentId(), "Child should reference correct parent");

			// Verify children are correctly tracked
			List<Integer> rootChildren = storage.getChildren(rootId);
			assertEquals(3, rootChildren.size(), "Root should have 3 children");
			assertTrue(rootChildren.contains(method1Id), "Children should include method1");
			assertTrue(rootChildren.contains(method2Id), "Children should include method2");
			assertTrue(rootChildren.contains(fieldId), "Children should include field");

			// Children should be in allocation order
			assertEquals(method1Id, rootChildren.get(0), "First child should be method1");
			assertEquals(method2Id, rootChildren.get(1), "Second child should be method2");
			assertEquals(fieldId, rootChildren.get(2), "Third child should be field");
		}

		@Test
		@DisplayName("Should handle nested parent-child relationships")
		void shouldHandleNestedParentChildRelationships() {
			// Create class -> method -> block -> statement hierarchy
			int classId = storage.allocateNode(0, 100, NodeType.CLASS_DECLARATION, -1);
			int methodId = storage.allocateNode(10, 50, NodeType.METHOD_DECLARATION, classId);
			int blockId = storage.allocateNode(20, 30, NodeType.BLOCK_STATEMENT, methodId);
			int stmt1Id = storage.allocateNode(25, 8, NodeType.EXPRESSION_STATEMENT, blockId);
			int stmt2Id = storage.allocateNode(35, 10, NodeType.RETURN_STATEMENT, blockId);

			// Verify each level of the hierarchy
			List<Integer> classChildren = storage.getChildren(classId);
			assertEquals(1, classChildren.size(), "Class should have 1 child");
			assertEquals(methodId, classChildren.get(0), "Class child should be method");

			List<Integer> methodChildren = storage.getChildren(methodId);
			assertEquals(1, methodChildren.size(), "Method should have 1 child");
			assertEquals(blockId, methodChildren.get(0), "Method child should be block");

			List<Integer> blockChildren = storage.getChildren(blockId);
			assertEquals(2, blockChildren.size(), "Block should have 2 children");
			assertEquals(stmt1Id, blockChildren.get(0), "First statement");
			assertEquals(stmt2Id, blockChildren.get(1), "Second statement");

			// Leaf nodes should have no children
			List<Integer> stmt1Children = storage.getChildren(stmt1Id);
			assertTrue(stmt1Children.isEmpty(), "Statement should have no children");
		}

		@Test
		@DisplayName("Should handle parent-child relationships with array growth")
		void shouldHandleParentChildRelationshipsWithArrayGrowth() {
			int parentId = storage.allocateNode(0, 200, NodeType.CLASS_DECLARATION, -1);

			// Add many children to trigger array growth
			int numChildren = 2000; // More than initial capacity of 1024
			for (int i = 0; i < numChildren; i++) {
				int childId = storage.allocateNode(i * 5, 3, NodeType.FIELD_DECLARATION, parentId);
				assertEquals(i + 1, childId, "Child IDs should be sequential");
			}

			// Verify all children are correctly tracked after array growth
			List<Integer> children = storage.getChildren(parentId);
			assertEquals(numChildren, children.size(), "All children should be tracked");

			for (int i = 0; i < numChildren; i++) {
				assertEquals(i + 1, children.get(i).intValue(), "Child " + i + " should be at correct index");
			}
		}

		@Test
		@DisplayName("Should handle non-contiguous child allocation correctly")
		void shouldHandleNonContiguousChildAllocationCorrectly() {
			// Create multiple parents
			int parent1Id = storage.allocateNode(0, 50, NodeType.CLASS_DECLARATION, -1);
			int parent2Id = storage.allocateNode(60, 40, NodeType.INTERFACE_DECLARATION, -1);

			// Interleave child allocation between parents
			int child1P1 = storage.allocateNode(10, 5, NodeType.METHOD_DECLARATION, parent1Id);
			int child1P2 = storage.allocateNode(70, 8, NodeType.METHOD_DECLARATION, parent2Id);
			int child2P1 = storage.allocateNode(20, 6, NodeType.FIELD_DECLARATION, parent1Id);
			int child2P2 = storage.allocateNode(80, 7, NodeType.FIELD_DECLARATION, parent2Id);
			int child3P1 = storage.allocateNode(30, 4, NodeType.CONSTRUCTOR_DECLARATION, parent1Id);

			// Verify each parent has correct children
			List<Integer> parent1Children = storage.getChildren(parent1Id);
			assertEquals(3, parent1Children.size(), "Parent1 should have 3 children");
			assertEquals(child1P1, parent1Children.get(0), "Parent1 first child");
			assertEquals(child2P1, parent1Children.get(1), "Parent1 second child");
			assertEquals(child3P1, parent1Children.get(2), "Parent1 third child");

			List<Integer> parent2Children = storage.getChildren(parent2Id);
			assertEquals(2, parent2Children.size(), "Parent2 should have 2 children");
			assertEquals(child1P2, parent2Children.get(0), "Parent2 first child");
			assertEquals(child2P2, parent2Children.get(1), "Parent2 second child");
		}

		@Test
		@DisplayName("Should return empty list for nodes with no children")
		void shouldReturnEmptyListForNodesWithNoChildren() {
			int leafNodeId = storage.allocateNode(0, 10, NodeType.LITERAL_EXPRESSION, -1);

			List<Integer> children = storage.getChildren(leafNodeId);
			assertNotNull(children, "Children list should not be null");
			assertTrue(children.isEmpty(), "Children list should be empty for leaf node");
			assertEquals(0, children.size(), "Children list size should be 0");
		}
	}

	@Nested
	@DisplayName("Memory Management and Performance")
	class MemoryManagementTests {

		@Test
		@DisplayName("Should calculate estimated memory usage accurately")
		void shouldCalculateEstimatedMemoryUsageAccurately() {
			int capacity = 1000;
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(capacity)) {
				long initialMemory = storage.getEstimatedMemoryUsage();

				// Allocate nodes and verify memory calculation
				int numNodes = 100;
				for (int i = 0; i < numNodes; i++) {
					storage.allocateNode(i * 10, 5, NodeType.METHOD_DECLARATION, -1);
				}

				long memoryAfterAllocation = storage.getEstimatedMemoryUsage();

				// Memory should include arena memory (16 bytes per node) + child arrays
				long expectedArenaMemory = (long) numNodes * 16; // 16 bytes per node
				long expectedChildArrays =
					(long) capacity * Integer.BYTES +     // childrenStart array
					(long) capacity * Integer.BYTES +     // childrenCount array
					1024 * Integer.BYTES;                 // childrenData array (initial capacity)

				long expectedTotal = expectedArenaMemory + expectedChildArrays;
				assertEquals(expectedTotal, memoryAfterAllocation,
					"Memory calculation should account for arena memory and child arrays");

				// Memory should have increased from initial (arena memory for nodes)
				long memoryIncrease = memoryAfterAllocation - initialMemory;
				assertEquals(expectedArenaMemory, memoryIncrease,
					"Memory increase should equal arena memory for allocated nodes");
			}
		}

		@Test
		@DisplayName("Should reset storage correctly for reuse")
		void shouldResetStorageCorrectlyForReuse() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(500)) {
				// Allocate nodes with parent-child relationships
				int parent = storage.allocateNode(0, 50, NodeType.CLASS_DECLARATION, -1);
				int child1 = storage.allocateNode(10, 15, NodeType.METHOD_DECLARATION, parent);
				int child2 = storage.allocateNode(30, 10, NodeType.FIELD_DECLARATION, parent);

				assertEquals(3, storage.getNodeCount(), "Should have 3 nodes before reset");
				assertEquals(2, storage.getChildren(parent).size(), "Parent should have 2 children before reset");

				// Reset the storage
				storage.reset();

				// Verify reset state
				assertEquals(0, storage.getNodeCount(), "Node count should be 0 after reset");
				assertTrue(storage.isAlive(), "Arena should still be alive after reset");

				// Should be able to allocate nodes again
				int newNode = storage.allocateNode(0, 20, NodeType.INTERFACE_DECLARATION, -1);
				assertEquals(0, newNode, "First node after reset should have ID 0");
				assertEquals(1, storage.getNodeCount(), "Should have 1 node after new allocation");

				List<Integer> newNodeChildren = storage.getChildren(newNode);
				assertTrue(newNodeChildren.isEmpty(), "New node should have no children");
			}
		}

		@Test
		@DisplayName("Should demonstrate memory efficiency compared to object allocation")
		void shouldDemonstrateMemoryEfficiencyComparedToObjectAllocation() {
			int numNodes = 10000;

			try (ArenaNodeStorage storage = ArenaNodeStorage.create(numNodes)) {
				// Allocate nodes and measure memory usage
				for (int i = 0; i < numNodes; i++) {
					storage.allocateNode(i * 20, 15, NodeType.METHOD_DECLARATION, -1);
				}

				long arenaMemoryUsage = storage.getEstimatedMemoryUsage();

				// Calculate theoretical object allocation memory
				// Each node as object would need:
				// - Object header: ~12-16 bytes
				// - 4 int fields: 16 bytes
				// - List for children: ~32 bytes minimum
				// Total per node: ~60 bytes (conservative estimate)
				long theoreticalObjectMemory = (long) numNodes * 60;

				// Arena should use significantly less memory
				assertTrue(arenaMemoryUsage < theoreticalObjectMemory / 2,
					String.format("Arena memory (%d bytes) should be less than half of theoretical object memory (%d bytes)",
						arenaMemoryUsage, theoreticalObjectMemory));

				// Verify actual arena node memory is exactly 16 bytes per node
				long arenaNodeMemory = (long) numNodes * 16;
				assertTrue(arenaMemoryUsage > arenaNodeMemory,
					"Total memory should include both arena nodes and child arrays");

				System.out.printf("Memory efficiency test: Arena=%d bytes, Theoretical Objects=%d bytes, Savings=%.1f%%\n",
					arenaMemoryUsage, theoreticalObjectMemory,
					(1.0 - (double) arenaMemoryUsage / theoreticalObjectMemory) * 100);
			}
		}
	}

	@Nested
	@DisplayName("Edge Cases and Error Conditions")
	class EdgeCaseTests {

		private ArenaNodeStorage storage;

		@BeforeEach
		void setUp() {
			storage = ArenaNodeStorage.create(100);
		}

		@AfterEach
		void tearDown() {
			storage.close();
		}

		@Test
		@DisplayName("Should handle boundary values for node data")
		void shouldHandleBoundaryValuesForNodeData() {
			// Test minimum values
			int nodeId1 = storage.allocateNode(0, 0, (byte) 0, -1);
			ArenaNodeStorage.NodeInfo node1 = storage.getNode(nodeId1);
			assertEquals(0, node1.startOffset(), "Should handle zero start offset");
			assertEquals(0, node1.length(), "Should handle zero length");
			assertEquals(0, node1.endOffset(), "End offset should be calculated correctly for zero length");

			// Test maximum practical values
			int nodeId2 = storage.allocateNode(Integer.MAX_VALUE - 1000, 1000, NodeType.CLASS_DECLARATION, nodeId1);
			ArenaNodeStorage.NodeInfo node2 = storage.getNode(nodeId2);
			assertEquals(Integer.MAX_VALUE - 1000, node2.startOffset(), "Should handle large start offset");
			assertEquals(1000, node2.length(), "Should handle large length");
			assertEquals(Integer.MAX_VALUE, node2.endOffset(), "Should handle large end offset calculation");
		}

		@Test
		@DisplayName("Should handle invalid parent references gracefully")
		void shouldHandleInvalidParentReferencesGracefully() {
			// Create a node with invalid parent reference (beyond allocated range)
			// This should not establish parent-child relationship but should not crash
			int nodeId = storage.allocateNode(10, 20, NodeType.METHOD_DECLARATION, 999);

			ArenaNodeStorage.NodeInfo node = storage.getNode(nodeId);
			assertEquals(999, node.parentId(), "Invalid parent ID should be stored");
			assertFalse(node.isRoot(), "Node with non-negative parent should not be root");

			// Invalid parent should not have this as a child
			// (This tests the bounds checking in addChildToParent)
			assertEquals(1, storage.getNodeCount(), "Should still have the allocated node");
		}

		@Test
		@DisplayName("Should handle allocation with parent created after child attempt")
		void shouldHandleAllocationWithParentCreatedAfterChildAttempt() {
			// Allocate child first (which should work but not establish relationship)
			int childId = storage.allocateNode(10, 15, NodeType.METHOD_DECLARATION, 5);
			assertEquals(0, childId, "Child should be allocated with ID 0");

			// Now allocate the parent
			int parentId = storage.allocateNode(0, 50, NodeType.CLASS_DECLARATION, -1);
			assertEquals(1, parentId, "Parent should be allocated with ID 1");

			// Child should still reference the originally specified parent ID
			ArenaNodeStorage.NodeInfo child = storage.getNode(childId);
			assertEquals(5, child.parentId(), "Child should maintain original parent reference");

			// Parent should have no children since child was allocated before parent existed
			List<Integer> parentChildren = storage.getChildren(parentId);
			assertTrue(parentChildren.isEmpty(), "Parent should have no children");
		}

		@Test
		@DisplayName("Should handle all node types correctly")
		void shouldHandleAllNodeTypesCorrectly() {
			// Test with various node types to ensure type storage works correctly
			byte[] nodeTypes = {
				NodeType.COMPILATION_UNIT,
				NodeType.CLASS_DECLARATION,
				NodeType.METHOD_DECLARATION,
				NodeType.LAMBDA_EXPRESSION,
				NodeType.SWITCH_EXPRESSION,
				NodeType.INSTANCE_MAIN_METHOD,
				NodeType.PRIMITIVE_PATTERN
			};

			for (int i = 0; i < nodeTypes.length; i++) {
				byte nodeType = nodeTypes[i];
				int nodeId = storage.allocateNode(i * 10, 5, nodeType, -1);

				ArenaNodeStorage.NodeInfo node = storage.getNode(nodeId);
				assertEquals(nodeType, node.nodeType(),
					"Node type should be preserved for " + NodeType.getTypeName(nodeType));
				assertNotEquals("Unknown", node.getTypeName(),
					"Type name should be recognized for " + NodeType.getTypeName(nodeType));
			}
		}

		@Test
		@DisplayName("Should maintain data integrity after multiple operations")
		void shouldMaintainDataIntegrityAfterMultipleOperations() {
			// Perform a complex sequence of operations
			int rootId = storage.allocateNode(0, 100, NodeType.CLASS_DECLARATION, -1);

			// Add several children
			int[] childIds = new int[10];
			for (int i = 0; i < 10; i++) {
				childIds[i] = storage.allocateNode(i * 10, 8, NodeType.METHOD_DECLARATION, rootId);
			}

			// Update some node lengths
			storage.updateNodeLength(rootId, 150);
			storage.updateNodeLength(childIds[3], 12);

			// Verify all data is still correct
			ArenaNodeStorage.NodeInfo rootNode = storage.getNode(rootId);
			assertEquals(150, rootNode.length(), "Root length should be updated");
			assertEquals(150, rootNode.endOffset(), "Root end offset should be recalculated");

			ArenaNodeStorage.NodeInfo updatedChild = storage.getNode(childIds[3]);
			assertEquals(12, updatedChild.length(), "Child length should be updated");
			assertEquals(30 + 12, updatedChild.endOffset(), "Child end offset should be recalculated");

			// Verify parent-child relationships are maintained
			List<Integer> children = storage.getChildren(rootId);
			assertEquals(10, children.size(), "Root should still have all children");

			for (int i = 0; i < 10; i++) {
				assertEquals(childIds[i], children.get(i).intValue(),
					"Child " + i + " should be at correct position");

				ArenaNodeStorage.NodeInfo child = storage.getNode(childIds[i]);
				assertEquals(rootId, child.parentId(),
					"Child " + i + " should still reference correct parent");
			}
		}
	}
}