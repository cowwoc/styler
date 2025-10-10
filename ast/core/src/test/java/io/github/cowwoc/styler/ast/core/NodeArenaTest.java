package io.github.cowwoc.styler.ast.core;

import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import static org.testng.Assert.assertThrows;

/**
 * Tests for NodeArena functionality including construction, node allocation, data retrieval,
 * capacity management, growth behavior, and error handling for invalid inputs.
 *
 * <h2>Thread safety</h2>
 * Thread-safe - all instances are created inside @Test methods.
 */
public class NodeArenaTest
{
	/**
	 * Tests that NodeArena can be constructed with default capacity.
	 */
	@Test
	public void testDefaultConstruction()
	{
		try (NodeArena arena = new NodeArena())
		{
			requireThat(arena, "arena").isNotNull();
			requireThat(arena.getNodeCount(), "arena.getNodeCount()").isEqualTo(0);
			requireThat(arena.getCapacity(), "arena.getCapacity()").isPositive();
		}
	}

	/**
	 * Tests that NodeArena can be constructed with a custom capacity.
	 */
	@Test
	public void testCustomCapacity()
	{
		try (NodeArena arena = new NodeArena(100))
		{
			requireThat(arena.getCapacity(), "arena.getCapacity()").isEqualTo(100);
			requireThat(arena.getNodeCount(), "arena.getNodeCount()").isEqualTo(0);
		}
	}

	/**
	 * Tests that NodeArena rejects zero capacity.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidCapacityZero()
	{
		new NodeArena(0);
	}

	/**
	 * Tests that NodeArena rejects negative capacity.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidCapacityNegative()
	{
		new NodeArena(-1);
	}

	/**
	 * Tests that a node can be allocated and its index retrieved.
	 */
	@Test
	public void testAllocateNode()
	{
		try (NodeArena arena = new NodeArena())
		{
			NodeIndex index = arena.allocateNode(NodeType.INTEGER_LITERAL, 0, 5, 42);

			requireThat(index.isValid(), "index.isValid()").isTrue();
			requireThat(index.index(), "index.index()").isEqualTo(0);
			requireThat(arena.getNodeCount(), "arena.getNodeCount()").isEqualTo(1);
		}
	}

	/**
	 * Tests that node type can be retrieved correctly.
	 */
	@Test
	public void testGetNodeType()
	{
		try (NodeArena arena = new NodeArena())
		{
			NodeIndex index = arena.allocateNode(NodeType.STRING_LITERAL, 10, 20, 0);

			requireThat(arena.getType(index), "arena.getType(index)").isEqualTo(NodeType.STRING_LITERAL);
		}
	}

	/**
	 * Tests that node start position can be retrieved correctly.
	 */
	@Test
	public void testGetStartPosition()
	{
		try (NodeArena arena = new NodeArena())
		{
			NodeIndex index = arena.allocateNode(NodeType.IDENTIFIER, 15, 25, 0);

			requireThat(arena.getStart(index), "arena.getStart(index)").isEqualTo(15);
		}
	}

	/**
	 * Tests that node end position can be retrieved correctly.
	 */
	@Test
	public void testGetEndPosition()
	{
		try (NodeArena arena = new NodeArena())
		{
			NodeIndex index = arena.allocateNode(NodeType.IDENTIFIER, 15, 25, 0);

			requireThat(arena.getEnd(index), "arena.getEnd(index)").isEqualTo(25);
		}
	}

	/**
	 * Tests that node data can be retrieved and modified.
	 */
	@Test
	public void testGetAndSetData()
	{
		try (NodeArena arena = new NodeArena())
		{
			NodeIndex index = arena.allocateNode(NodeType.METHOD_INVOCATION, 0, 10, 123);

			requireThat(arena.getData(index), "arena.getData(index)").isEqualTo(123);

			arena.setData(index, 456);
			requireThat(arena.getData(index), "arena.getData(index)").isEqualTo(456);
		}
	}

	/**
	 * Tests that multiple nodes can be stored and retrieved independently.
	 */
	@Test
	public void testMultipleNodes()
	{
		try (NodeArena arena = new NodeArena())
		{
			NodeIndex first = arena.allocateNode(NodeType.CLASS_DECLARATION, 0, 100, 1);
			NodeIndex second = arena.allocateNode(NodeType.METHOD_DECLARATION, 10, 50, 2);
			NodeIndex third = arena.allocateNode(NodeType.RETURN_STATEMENT, 30, 40, 3);

			requireThat(arena.getNodeCount(), "arena.getNodeCount()").isEqualTo(3);

			requireThat(arena.getType(first), "arena.getType(first)").isEqualTo(NodeType.CLASS_DECLARATION);
			requireThat(arena.getData(first), "arena.getData(first)").isEqualTo(1);

			requireThat(arena.getType(second), "arena.getType(second)").isEqualTo(NodeType.METHOD_DECLARATION);
			requireThat(arena.getData(second), "arena.getData(second)").isEqualTo(2);

			requireThat(arena.getType(third), "arena.getType(third)").isEqualTo(NodeType.RETURN_STATEMENT);
			requireThat(arena.getData(third), "arena.getData(third)").isEqualTo(3);
		}
	}

	/**
	 * Tests that arena capacity doubles when exceeding initial capacity.
	 */
	@Test
	public void testArenaGrowth()
	{
		try (NodeArena arena = new NodeArena(2))
		{
			requireThat(arena.getCapacity(), "arena.getCapacity()").isEqualTo(2);

			arena.allocateNode(NodeType.INTEGER_LITERAL, 0, 1, 0);
			arena.allocateNode(NodeType.INTEGER_LITERAL, 2, 3, 0);
			requireThat(arena.getCapacity(), "arena.getCapacity()").isEqualTo(2);

			// This should trigger growth
			arena.allocateNode(NodeType.INTEGER_LITERAL, 4, 5, 0);
			requireThat(arena.getCapacity(), "arena.getCapacity()").isEqualTo(4);
			requireThat(arena.getNodeCount(), "arena.getNodeCount()").isEqualTo(3);
		}
	}

	/**
	 * Tests that existing node data is preserved when arena capacity grows.
	 */
	@Test
	public void testArenaGrowthPreservesData()
	{
		try (NodeArena arena = new NodeArena(2))
		{
			NodeIndex first = arena.allocateNode(NodeType.STRING_LITERAL, 0, 10, 100);
			NodeIndex second = arena.allocateNode(NodeType.IDENTIFIER, 20, 30, 200);

			// Trigger growth
			NodeIndex third = arena.allocateNode(NodeType.METHOD_INVOCATION, 40, 50, 300);

			// Verify all data is preserved
			requireThat(arena.getType(first), "arena.getType(first)").isEqualTo(NodeType.STRING_LITERAL);
			requireThat(arena.getStart(first), "arena.getStart(first)").isEqualTo(0);
			requireThat(arena.getEnd(first), "arena.getEnd(first)").isEqualTo(10);
			requireThat(arena.getData(first), "arena.getData(first)").isEqualTo(100);

			requireThat(arena.getType(second), "arena.getType(second)").isEqualTo(NodeType.IDENTIFIER);
			requireThat(arena.getStart(second), "arena.getStart(second)").isEqualTo(20);
			requireThat(arena.getEnd(second), "arena.getEnd(second)").isEqualTo(30);
			requireThat(arena.getData(second), "arena.getData(second)").isEqualTo(200);

			requireThat(arena.getType(third), "arena.getType(third)").isEqualTo(NodeType.METHOD_INVOCATION);
			requireThat(arena.getStart(third), "arena.getStart(third)").isEqualTo(40);
			requireThat(arena.getEnd(third), "arena.getEnd(third)").isEqualTo(50);
			requireThat(arena.getData(third), "arena.getData(third)").isEqualTo(300);
		}
	}

	/**
	 * Tests that memory usage is calculated correctly based on capacity.
	 */
	@Test
	public void testMemoryUsage()
	{
		try (NodeArena arena = new NodeArena(100))
		{
			// 100 nodes * 16 bytes per node = 1600 bytes
			requireThat(arena.getMemoryUsage(), "arena.getMemoryUsage()").isEqualTo(1600);
		}
	}

	/**
	 * Tests that accessing invalid node indices throws IllegalArgumentException.
	 */
	@Test
	public void testInvalidNodeAccess()
	{
		try (NodeArena arena = new NodeArena())
		{
			arena.allocateNode(NodeType.INTEGER_LITERAL, 0, 1, 0);

			// Out of bounds index
			NodeIndex invalid = new NodeIndex(10);
			assertThrows(IllegalArgumentException.class, () -> arena.getType(invalid));
			assertThrows(IllegalArgumentException.class, () -> arena.getStart(invalid));
			assertThrows(IllegalArgumentException.class, () -> arena.getEnd(invalid));
			assertThrows(IllegalArgumentException.class, () -> arena.getData(invalid));
			assertThrows(IllegalArgumentException.class, () -> arena.setData(invalid, 0));
		}
	}

	/**
	 * Tests that passing null NodeIndex throws NullPointerException.
	 */
	@Test
	public void testNullNodeIndex()
	{
		try (NodeArena arena = new NodeArena())
		{
			assertThrows(NullPointerException.class, () -> arena.getType(null));
			assertThrows(NullPointerException.class, () -> arena.getStart(null));
			assertThrows(NullPointerException.class, () -> arena.getEnd(null));
			assertThrows(NullPointerException.class, () -> arena.getData(null));
			assertThrows(NullPointerException.class, () -> arena.setData(null, 0));
		}
	}

	/**
	 * Tests that using NULL sentinel index throws IllegalArgumentException.
	 */
	@Test
	public void testInvalidNodeIndexSentinel()
	{
		try (NodeArena arena = new NodeArena())
		{
			arena.allocateNode(NodeType.INTEGER_LITERAL, 0, 1, 0);

			NodeIndex nullIndex = NodeIndex.NULL;
			requireThat(nullIndex.isValid(), "nullIndex.isValid()").isFalse();
			assertThrows(IllegalArgumentException.class, () -> arena.getType(nullIndex));
		}
	}

	/**
	 * Tests that negative start position is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeStartPosition()
	{
		try (NodeArena arena = new NodeArena())
		{
			arena.allocateNode(NodeType.INTEGER_LITERAL, -1, 5, 0);
		}
	}

	/**
	 * Tests that negative end position is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeEndPosition()
	{
		try (NodeArena arena = new NodeArena())
		{
			arena.allocateNode(NodeType.INTEGER_LITERAL, 0, -1, 0);
		}
	}

	/**
	 * Tests that null node type is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testNullNodeType()
	{
		try (NodeArena arena = new NodeArena())
		{
			arena.allocateNode(null, 0, 5, 0);
		}
	}
}
