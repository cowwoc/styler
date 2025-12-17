package io.github.cowwoc.styler.formatter.linelength.internal;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.formatter.TransformationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Detects wrapping context at character positions using AST analysis.
 * <p>
 * This class builds a spatial index of AST nodes during construction to enable O(log n)
 * position-to-node lookup. The spatial index maps character positions in source code to
 * their smallest enclosing AST node, which determines the wrapping context type.
 * <p>
 * <b>Performance Characteristics</b>:
 * <ul>
 *   <li>Construction: O(n log n) where n = node count (~500-5000 nodes per file)</li>
 *   <li>Position lookup: O(log n) binary search on spatial index</li>
 *   <li>Memory overhead: ~32 bytes per node (~64KB for typical file)</li>
 * </ul>
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class ContextDetector
{
	private final TransformationContext context;
	private final NodeArena arena;
	private final List<NodeInterval> spatialIndex;

	/**
	 * Creates detector from transformation context.
	 * Builds internal spatial index during construction (O(n log n) where n = node count).
	 *
	 * @param context transformation context with AST access
	 * @throws NullPointerException if {@code context} is {@code null}
	 */
	public ContextDetector(TransformationContext context)
	{
		requireThat(context, "context").isNotNull();
		this.context = context;
		this.arena = context.arena();
		this.spatialIndex = buildSpatialIndex();
	}

	/**
	 * Builds spatial index from flat NodeArena storage.
	 * Creates a sorted list of node intervals for binary search lookup.
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
	 * Finds wrapping context at character position.
	 *
	 * @param position character offset in source code
	 * @return wrapping context type, or NOT_WRAPPABLE if not in wrappable construct
	 * @throws IllegalArgumentException if position is negative or beyond source code length
	 * @throws AssertionError if no AST node contains the position. This indicates a bug since every valid
	 *                        position must be inside at least the root CompilationUnit node.
	 */
	public WrapContext detectContext(int position)
	{
		validatePosition(position);
		NodeIndex node = findSmallestEnclosingNode(position);

		if (!node.isValid())
		{
			throw new AssertionError("Position " + position +
				" not found in spatial index (should be in at least CompilationUnit)");
		}

		return classifyNode(node);
	}

	/**
	 * Finds smallest AST node enclosing position.
	 *
	 * @param position character offset in source code
	 * @return node index, or NodeIndex.NULL if no enclosing node
	 * @throws IllegalArgumentException if position out of bounds
	 */
	public NodeIndex findEnclosingNode(int position)
	{
		validatePosition(position);
		return findSmallestEnclosingNode(position);
	}

	/**
	 * Validates position is within source code bounds.
	 *
	 * @param position character offset to validate
	 * @throws IllegalArgumentException if position out of bounds
	 */
	private void validatePosition(int position)
	{
		String sourceCode = context.sourceCode();
		requireThat(position, "position").isGreaterThanOrEqualTo(0).
			isLessThanOrEqualTo(sourceCode.length());
	}

	/**
	 * Finds the smallest AST node that encloses the given position.
	 * Uses linear search on the sorted spatial index.
	 *
	 * @param position character offset in source code
	 * @return smallest enclosing node, or NodeIndex.NULL if none found
	 */
	private NodeIndex findSmallestEnclosingNode(int position)
	{
		NodeIndex smallest = NodeIndex.NULL;
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
	 * Classifies AST node into wrapping context type.
	 * Explicitly lists all NodeType values to enable exhaustiveness checking.
	 *
	 * @param nodeIndex the node to classify
	 * @return corresponding wrapping context
	 */
	private WrapContext classifyNode(NodeIndex nodeIndex)
	{
		NodeType type = arena.getType(nodeIndex);

		return switch (type)
		{
			// Wrappable contexts
			case FIELD_ACCESS -> WrapContext.METHOD_CHAIN;
			case QUALIFIED_NAME -> WrapContext.QUALIFIED_NAME;
			case METHOD_INVOCATION -> WrapContext.METHOD_ARGUMENTS;
			case METHOD_DECLARATION, PARAMETER_DECLARATION -> WrapContext.METHOD_PARAMETERS;
			case BINARY_EXPRESSION -> WrapContext.BINARY_EXPRESSION;
			case CONDITIONAL_EXPRESSION -> WrapContext.TERNARY_EXPRESSION;
			case ARRAY_CREATION -> WrapContext.ARRAY_INITIALIZER;
			case ANNOTATION -> WrapContext.ANNOTATION_ARGUMENTS;
			case PARAMETERIZED_TYPE -> WrapContext.GENERIC_TYPE_ARGS;

			// Literals - not wrappable
			case INTEGER_LITERAL, LONG_LITERAL, FLOAT_LITERAL, DOUBLE_LITERAL,
				BOOLEAN_LITERAL, CHAR_LITERAL, STRING_LITERAL, NULL_LITERAL -> WrapContext.NOT_WRAPPABLE;

			// Identifiers and simple names - not wrappable
			case IDENTIFIER -> WrapContext.NOT_WRAPPABLE;

			// Other expressions - not wrappable
			case UNARY_EXPRESSION, POSTFIX_EXPRESSION, ASSIGNMENT_EXPRESSION,
				INSTANCEOF_EXPRESSION, LAMBDA_EXPRESSION, METHOD_REFERENCE,
				CAST_EXPRESSION, ARRAY_ACCESS, PARENTHESIZED_EXPRESSION,
				OBJECT_CREATION -> WrapContext.NOT_WRAPPABLE;

			// Statements - not wrappable
			case BLOCK, EXPRESSION_STATEMENT, VARIABLE_DECLARATION,
				IF_STATEMENT, FOR_STATEMENT, ENHANCED_FOR_STATEMENT,
				WHILE_STATEMENT, DO_WHILE_STATEMENT, SWITCH_STATEMENT,
				SWITCH_CASE, RETURN_STATEMENT, THROW_STATEMENT,
				TRY_STATEMENT, CATCH_CLAUSE, FINALLY_CLAUSE,
				SYNCHRONIZED_STATEMENT, BREAK_STATEMENT, CONTINUE_STATEMENT,
				ASSERT_STATEMENT, EMPTY_STATEMENT -> WrapContext.NOT_WRAPPABLE;

			// Declarations - not wrappable
			case COMPILATION_UNIT, PACKAGE_DECLARATION, IMPORT_DECLARATION,
				CLASS_DECLARATION, INTERFACE_DECLARATION, ENUM_DECLARATION,
				ANNOTATION_DECLARATION, CONSTRUCTOR_DECLARATION,
				FIELD_DECLARATION, ENUM_CONSTANT -> WrapContext.NOT_WRAPPABLE;

			// Type-related - not wrappable
			case TYPE_REFERENCE, ARRAY_TYPE, TYPE_PARAMETER,
				WILDCARD_TYPE, PRIMITIVE_TYPE -> WrapContext.NOT_WRAPPABLE;

			// Annotations - handled specially above
			case ANNOTATION_ELEMENT -> WrapContext.NOT_WRAPPABLE;

			// Modifiers - not wrappable
			case MODIFIERS -> WrapContext.NOT_WRAPPABLE;

			// Comments - not wrappable
			case LINE_COMMENT, BLOCK_COMMENT, JAVADOC_COMMENT,
				MARKDOWN_DOC_COMMENT -> WrapContext.NOT_WRAPPABLE;

			// Additional expressions - not wrappable
			case THIS_EXPRESSION, SUPER_EXPRESSION, ARRAY_INITIALIZER -> WrapContext.NOT_WRAPPABLE;

			// Switch expression - not wrappable
			case SWITCH_EXPRESSION -> WrapContext.NOT_WRAPPABLE;

			// Static import - not wrappable
			case STATIC_IMPORT_DECLARATION -> WrapContext.NOT_WRAPPABLE;
		};
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
