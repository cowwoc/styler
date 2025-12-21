package io.github.cowwoc.styler.formatter.linelength.internal;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.AstPositionIndex;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Detects wrapping context at character positions using AST analysis.
 * <p>
 * This class uses the shared {@link AstPositionIndex} from the {@link TransformationContext} to enable
 * efficient position-to-node lookup. The spatial index maps character positions in source code to their
 * smallest enclosing AST node, which determines the wrapping context type.
 * <p>
 * <b>Performance Characteristics</b>:
 * <ul>
 *   <li>Position lookup: O(n) linear scan to find smallest enclosing node</li>
 *   <li>Memory overhead: None - reuses shared index from TransformationContext</li>
 * </ul>
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class ContextDetector
{
	private final TransformationContext context;
	private final NodeArena arena;
	private final AstPositionIndex positionIndex;

	/**
	 * Creates detector from transformation context.
	 * Uses the shared position index from the context.
	 *
	 * @param context transformation context with AST access
	 * @throws NullPointerException if {@code context} is {@code null}
	 */
	public ContextDetector(TransformationContext context)
	{
		requireThat(context, "context").isNotNull();
		this.context = context;
		this.arena = context.arena();
		this.positionIndex = context.positionIndex();
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
		NodeIndex node = positionIndex.findEnclosingNode(position);

		if (node == null)
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
	 * @return node index, or {@code null} if no enclosing node
	 * @throws IllegalArgumentException if position out of bounds
	 */
	public NodeIndex findEnclosingNode(int position)
	{
		validatePosition(position);
		return positionIndex.findEnclosingNode(position);
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
				OBJECT_CREATION, THIS_EXPRESSION, SUPER_EXPRESSION,
				ARRAY_INITIALIZER -> WrapContext.NOT_WRAPPABLE;

			// Statements - not wrappable
			case BLOCK, EXPRESSION_STATEMENT, VARIABLE_DECLARATION,
				IF_STATEMENT, FOR_STATEMENT, ENHANCED_FOR_STATEMENT,
				WHILE_STATEMENT, DO_WHILE_STATEMENT, SWITCH_STATEMENT, SWITCH_EXPRESSION,
				SWITCH_CASE, RETURN_STATEMENT, THROW_STATEMENT,
				TRY_STATEMENT, CATCH_CLAUSE, FINALLY_CLAUSE,
				SYNCHRONIZED_STATEMENT, BREAK_STATEMENT, CONTINUE_STATEMENT,
				ASSERT_STATEMENT, EMPTY_STATEMENT -> WrapContext.NOT_WRAPPABLE;

			// Declarations - not wrappable
			case COMPILATION_UNIT, PACKAGE_DECLARATION, IMPORT_DECLARATION, STATIC_IMPORT_DECLARATION,
				CLASS_DECLARATION, INTERFACE_DECLARATION, ENUM_DECLARATION, RECORD_DECLARATION,
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
		};
	}
}
