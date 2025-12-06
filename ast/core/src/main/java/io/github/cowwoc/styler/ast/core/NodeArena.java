package io.github.cowwoc.styler.ast.core;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Arena-based storage for AST nodes using index-overlay pattern.
 * Each node occupies exactly 16 bytes with the following layout:
 * <ul>
 *   <li>Bytes 0-3: NodeType ordinal (int)</li>
 *   <li>Bytes 4-7: Start position in source (int)</li>
 *   <li>Bytes 8-11: End position in source (int)</li>
 *   <li>Bytes 12-15: Data field - meaning depends on NodeType (int)</li>
 * </ul>
 */
public final class NodeArena implements AutoCloseable
{
	private static final int BYTES_PER_NODE = 16;
	private static final int INITIAL_CAPACITY = 1024;
	private static final ValueLayout.OfInt INT_LAYOUT = ValueLayout.JAVA_INT;

	// Field offsets within each 16-byte node
	private static final int TYPE_OFFSET = 0;
	private static final int START_OFFSET = 4;
	private static final int END_OFFSET = 8;
	private static final int DATA_OFFSET = 12;

	private final Arena arena;
	private MemorySegment segment;
	private int nodeCount;
	private int capacity;
	private int allocationCheckCounter;

	/**
	 * Creates a new NodeArena with default initial capacity.
	 */
	public NodeArena()
	{
		this(INITIAL_CAPACITY);
	}

	/**
	 * Creates a new NodeArena with specified initial capacity.
	 *
	 * @param initialCapacity the initial number of nodes to allocate space for
	 * @throws IllegalArgumentException if {@code initialCapacity} is not positive
	 */
	public NodeArena(int initialCapacity)
	{
		requireThat(initialCapacity, "initialCapacity").isPositive();
		this.arena = Arena.ofConfined();
		this.capacity = initialCapacity;
		this.segment = arena.allocate(BYTES_PER_NODE * (long) capacity);
	}

	/**
	 * Allocates a new node in the arena and returns its index.
	 *
	 * @param type  the type of node to create
	 * @param start the start position in the source code
	 * @param end   the end position in the source code
	 * @param data  the data field (meaning depends on node type)
	 * @return the index of the newly created node
	 * @throws NullPointerException if {@code type} is null
	 * @throws IllegalArgumentException if {@code start}/{@code end} positions are negative
	 */
	public NodeIndex allocateNode(NodeType type, int start, int end, int data)
	{
		requireThat(type, "type").isNotNull();
		requireThat(start, "start").isNotNegative();
		requireThat(end, "end").isNotNegative();

		// SEC-005: Periodic memory checking to detect memory exhaustion
		// Note: JVM -Xmx limit will cap us anyway, this is an additional safeguard
		// Check every 100 node allocations to amortize Runtime.getRuntime() overhead (~500ns)
		++allocationCheckCounter;
		if (allocationCheckCounter >= 100)
		{
			allocationCheckCounter = 0;
			// Note: usedMemory reflects JVM heap usage, not specific to this application
			// The JVM's -Xmx setting provides the hard limit
			Runtime runtime = Runtime.getRuntime();
			long usedMemory = runtime.totalMemory() - runtime.freeMemory();
			if (usedMemory > SecurityConfig.MAX_HEAP_USAGE_BYTES)
			{
				throw new IllegalStateException(
					"Memory limit exceeded: " + usedMemory + " bytes exceeds maximum of " +
					SecurityConfig.MAX_HEAP_USAGE_BYTES + " bytes");
			}
		}

		if (nodeCount >= capacity)
		{
			grow();
		}

		long offset = (long) nodeCount * BYTES_PER_NODE;
		segment.set(INT_LAYOUT, offset + TYPE_OFFSET, type.ordinal());
		segment.set(INT_LAYOUT, offset + START_OFFSET, start);
		segment.set(INT_LAYOUT, offset + END_OFFSET, end);
		segment.set(INT_LAYOUT, offset + DATA_OFFSET, data);

		NodeIndex result = new NodeIndex(nodeCount);
		++nodeCount;
		return result;
	}

	/**
	 * Returns the memory offset for a node at the specified index.
	 * Validates the index and calculates its byte offset in the memory segment.
	 * Eliminates duplicate validation and offset calculation logic.
	 *
	 * @param index the node index
	 * @return the byte offset of the node in the memory segment
	 * @throws IllegalArgumentException if {@code index} is invalid
	 */
	private long getNodeOffset(NodeIndex index)
	{
		validateIndex(index);
		return (long) index.index() * BYTES_PER_NODE;
	}

	/**
	 * Returns the type of the node at the specified index.
	 *
	 * @param index the node index
	 * @return the node type
	 * @throws IllegalArgumentException if {@code index} is invalid
	 */
	public NodeType getType(NodeIndex index)
	{
		long offset = getNodeOffset(index);
		int typeOrdinal = segment.get(INT_LAYOUT, offset + TYPE_OFFSET);
		return NodeType.values()[typeOrdinal];
	}

	/**
	 * Returns the start position of the node at the specified index.
	 *
	 * @param index the node index
	 * @return the start position in source code
	 * @throws IllegalArgumentException if {@code index} is invalid
	 */
	public int getStart(NodeIndex index)
	{
		long offset = getNodeOffset(index);
		return segment.get(INT_LAYOUT, offset + START_OFFSET);
	}

	/**
	 * Returns the end position of the node at the specified index.
	 *
	 * @param index the node index
	 * @return the end position in source code
	 * @throws IllegalArgumentException if {@code index} is invalid
	 */
	public int getEnd(NodeIndex index)
	{
		long offset = getNodeOffset(index);
		return segment.get(INT_LAYOUT, offset + END_OFFSET);
	}

	/**
	 * Returns the data field of the node at the specified index.
	 *
	 * @param index the node index
	 * @return the data field value
	 * @throws IllegalArgumentException if {@code index} is invalid
	 */
	public int getData(NodeIndex index)
	{
		long offset = getNodeOffset(index);
		return segment.get(INT_LAYOUT, offset + DATA_OFFSET);
	}

	/**
	 * Sets the data field of the node at the specified index.
	 *
	 * @param index the node index
	 * @param data  the new data value
	 * @throws IllegalArgumentException if {@code index} is invalid
	 */
	public void setData(NodeIndex index, int data)
	{
		long offset = getNodeOffset(index);
		segment.set(INT_LAYOUT, offset + DATA_OFFSET, data);
	}

	/**
	 * Returns the current number of nodes in the arena.
	 *
	 * @return the node count
	 */
	public int getNodeCount()
	{
		return nodeCount;
	}

	/**
	 * Returns the current capacity of the arena.
	 *
	 * @return the capacity in nodes
	 */
	public int getCapacity()
	{
		return capacity;
	}

	/**
	 * Calculates the memory used by this arena in bytes.
	 *
	 * @return the memory usage in bytes
	 */
	public long getMemoryUsage()
	{
		return (long) capacity * BYTES_PER_NODE;
	}

	/**
	 * Validates that the given index is within bounds.
	 *
	 * @param index the index to validate
	 * @throws NullPointerException if {@code index} is null
	 * @throws IllegalArgumentException if {@code index} is out of bounds
	 */
	private void validateIndex(NodeIndex index)
	{
		requireThat(index, "index").isNotNull();
		requireThat(index.isValid(), "index.isValid()").isTrue();
		requireThat(index.index(), "index.index()").isLessThan(nodeCount);
	}

	/**
	 * Grows the arena capacity by doubling it.
	 *
	 * @throws IllegalStateException if growth would exceed maximum capacity
	 */
	private void grow()
	{
		int newCapacity = capacity * 2;

		// SEC-011: Arena capacity limit to prevent unbounded memory growth
		if (newCapacity > SecurityConfig.MAX_ARENA_CAPACITY)
		{
			throw new IllegalStateException(
				"Arena capacity limit exceeded: cannot grow beyond " + SecurityConfig.MAX_ARENA_CAPACITY + " nodes");
		}

		MemorySegment newSegment = arena.allocate(BYTES_PER_NODE * (long) newCapacity);

		// Copy existing data
		MemorySegment.copy(segment, 0, newSegment, 0, BYTES_PER_NODE * (long) nodeCount);

		this.segment = newSegment;
		this.capacity = newCapacity;
	}

	@Override
	public void close()
	{
		arena.close();
	}
}
