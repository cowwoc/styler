package io.github.cowwoc.styler.formatter;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Spatial index for efficient position-to-node lookup in AST.
 * <p>
 * This class builds a sorted index of AST node intervals during construction to enable efficient
 * position-to-node lookup. The index maps character positions in source code to their enclosing
 * AST nodes.
 * <p>
 * <b>Performance Characteristics</b>:
 * <ul>
 *   <li>Construction: O(n log n) where n = node count</li>
 *   <li>{@link #findEnclosingNode(int)}: O(n) linear scan to find smallest enclosing node</li>
 *   <li>{@link #findNodesByType(NodeType)}: O(n) linear scan</li>
 *   <li>{@link #getDepth(int)}: O(n) to count indentation-producing ancestors</li>
 *   <li>Memory overhead: ~32 bytes per node</li>
 * </ul>
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe. All state is established during construction
 * and the spatial index is immutable.
 */
public final class AstPositionIndex
{
	private final NodeArena arena;
	private final int sourceCodeLength;
	private final List<NodeInterval> spatialIndex;

	/**
	 * Creates a position index for the given arena.
	 * Builds internal spatial index during construction (O(n log n) where n = node count).
	 *
	 * @param arena the node arena to index
	 * @param sourceCodeLength the length of the source code being indexed
	 * @throws NullPointerException if {@code arena} is {@code null}
	 * @throws IllegalArgumentException if {@code sourceCodeLength} is negative
	 */
	public AstPositionIndex(NodeArena arena, int sourceCodeLength)
	{
		requireThat(arena, "arena").isNotNull();
		requireThat(sourceCodeLength, "sourceCodeLength").isNotNegative();
		this.arena = arena;
		this.sourceCodeLength = sourceCodeLength;
		this.spatialIndex = buildSpatialIndex();
	}

	/**
	 * Builds spatial index from flat NodeArena storage.
	 * Creates a sorted list of node intervals for lookup.
	 *
	 * @return immutable sorted list of node intervals
	 */
	private List<NodeInterval> buildSpatialIndex()
	{
		List<NodeInterval> intervals = new ArrayList<>();
		int nodeCount = arena.getNodeCount();

		for (int i = 0; i < nodeCount; ++i)
		{
			NodeIndex node = new NodeIndex(i);
			int start = arena.getStart(node);
			int end = arena.getEnd(node);
			intervals.add(new NodeInterval(node, start, end));
		}

		// Sort by start position, then by size (smaller intervals first for nested contexts)
		Collections.sort(intervals);
		return List.copyOf(intervals);
	}

	/**
	 * Finds the smallest AST node enclosing the given position.
	 *
	 * @param position character offset in source code
	 * @return the smallest enclosing node, or {@code null} if no enclosing node found
	 * @throws IllegalArgumentException if position is negative
	 */
	public NodeIndex findEnclosingNode(int position)
	{
		requireThat(position, "position").isNotNegative();

		NodeIndex smallest = null;
		int smallestSize = Integer.MAX_VALUE;

		// Linear search: check all intervals, track smallest that contains position
		for (NodeInterval interval : spatialIndex)
		{
			if (interval.start <= position && position < interval.end)
			{
				int size = interval.end - interval.start;
				if (size < smallestSize)
				{
					smallest = interval.node;
					smallestSize = size;
				}
			}
		}

		return smallest;
	}

	/**
	 * Finds all AST nodes enclosing the given position, from smallest to largest.
	 * This returns the complete ancestor chain at the position.
	 *
	 * @param position character offset in source code
	 * @return list of enclosing nodes ordered from smallest to largest, empty if none found
	 * @throws IllegalArgumentException if position is negative
	 */
	public List<NodeIndex> findAllEnclosingNodes(int position)
	{
		requireThat(position, "position").isNotNegative();

		List<NodeInterval> enclosing = new ArrayList<>();

		for (NodeInterval interval : spatialIndex)
		{
			if (interval.start <= position && position < interval.end)
			{
				enclosing.add(interval);
			}
		}

		// Sort by size (smallest first)
		enclosing.sort((a, b) -> Integer.compare(a.end - a.start, b.end - b.start));

		return enclosing.stream().
			map(interval -> interval.node).
			toList();
	}

	/**
	 * Finds all nodes of the given type.
	 *
	 * @param type the node type to find
	 * @return list of node indices matching the type
	 * @throws NullPointerException if {@code type} is {@code null}
	 */
	public List<NodeIndex> findNodesByType(NodeType type)
	{
		requireThat(type, "type").isNotNull();

		List<NodeIndex> result = new ArrayList<>();
		for (NodeInterval interval : spatialIndex)
		{
			if (arena.getType(interval.node) == type)
			{
				result.add(interval.node);
			}
		}
		return result;
	}

	/**
	 * Returns the nesting depth at the given position.
	 * Depth is calculated by counting indentation-producing node types at the position:
	 * BLOCK, CLASS_DECLARATION, INTERFACE_DECLARATION, ENUM_DECLARATION, ENUM_CONSTANT (for
	 * constants with bodies), SWITCH_STATEMENT, SWITCH_EXPRESSION, OBJECT_CREATION (for
	 * anonymous class bodies).
	 * <p>
	 * For block-like nodes, position must be strictly inside the block body to be counted:
	 * <ul>
	 *   <li>Position must be greater than the node start (excludes opening line/brace)</li>
	 *   <li>Position must be less than {@code end - 1} (excludes closing brace position)</li>
	 * </ul>
	 * This ensures both the declaration line and closing brace are at the correct depth.
	 *
	 * @param position character offset in source code
	 * @return count of indentation-producing ancestors at position
	 * @throws IllegalArgumentException if position is negative
	 */
	public int getDepth(int position)
	{
		requireThat(position, "position").isNotNegative();

		int depth = 0;
		for (NodeInterval interval : spatialIndex)
		{
			NodeType type = arena.getType(interval.node);
			// Position must be strictly inside: after start and before end-1 (closing brace)
			// This excludes both the opening line and the closing brace from indentation
			boolean isIndentProducingType = type == NodeType.BLOCK || type == NodeType.CLASS_DECLARATION ||
				type == NodeType.INTERFACE_DECLARATION || type == NodeType.ENUM_DECLARATION ||
				type == NodeType.ENUM_CONSTANT || type == NodeType.SWITCH_STATEMENT ||
				type == NodeType.SWITCH_EXPRESSION || type == NodeType.OBJECT_CREATION;
			if (isIndentProducingType && interval.start < position && position < interval.end - 1)
				++depth;
		}
		return depth;
	}

	/**
	 * Returns all positions that are inside text or comments.
	 * <p>
	 * Marks positions inside:
	 * <ul>
	 *   <li>String literals ({@link NodeType#STRING_LITERAL})</li>
	 *   <li>Character literals ({@link NodeType#CHAR_LITERAL})</li>
	 *   <li>Line comments ({@link NodeType#LINE_COMMENT})</li>
	 *   <li>Markdown doc comments ({@link NodeType#MARKDOWN_DOC_COMMENT})</li>
	 *   <li>Block comments ({@link NodeType#BLOCK_COMMENT})</li>
	 *   <li>Javadoc comments ({@link NodeType#JAVADOC_COMMENT})</li>
	 * </ul>
	 *
	 * @return a BitSet where set bits indicate positions inside text or comments
	 */
	public BitSet getTextAndCommentPositions()
	{
		BitSet result = new BitSet(sourceCodeLength);

		// Mark text positions (string and character literals)
		for (NodeType type : List.of(NodeType.STRING_LITERAL, NodeType.CHAR_LITERAL))
		{
			for (NodeIndex node : findNodesByType(type))
			{
				int start = arena.getStart(node);
				int end = arena.getEnd(node);
				result.set(start, end);
			}
		}

		// Mark comment positions
		for (NodeType type : List.of(NodeType.LINE_COMMENT, NodeType.MARKDOWN_DOC_COMMENT,
			NodeType.BLOCK_COMMENT, NodeType.JAVADOC_COMMENT))
		{
			for (NodeIndex node : findNodesByType(type))
			{
				int start = arena.getStart(node);
				int end = arena.getEnd(node);
				result.set(start, end);
			}
		}

		return result;
	}

	/**
	 * Represents an interval of source code covered by an AST node.
	 * Used for spatial indexing to enable efficient position-to-node lookup.
	 *
	 * @param node the node index
	 * @param start the start position (inclusive)
	 * @param end the end position (exclusive)
	 */
	private record NodeInterval(NodeIndex node, int start, int end)
		implements Comparable<NodeInterval>
	{
		@Override
		public int compareTo(NodeInterval other)
		{
			// Sort by start position first
			int cmp = Integer.compare(start, other.start);
			if (cmp != 0)
			{
				return cmp;
			}
			// For same start, sort by size (smaller first) for nested contexts
			cmp = Integer.compare(end - start, other.end - other.start);
			if (cmp != 0)
			{
				return cmp;
			}
			// Final tiebreaker: node index ensures total ordering consistent with equals()
			return Integer.compare(node.index(), other.node.index());
		}
	}
}
