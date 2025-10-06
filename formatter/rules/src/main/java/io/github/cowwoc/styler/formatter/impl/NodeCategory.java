package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.AnnotationDeclarationNode;
import io.github.cowwoc.styler.ast.node.BlockStatementNode;
import io.github.cowwoc.styler.ast.node.ClassDeclarationNode;
import io.github.cowwoc.styler.ast.node.CompactMainMethodNode;
import io.github.cowwoc.styler.ast.node.ConstructorDeclarationNode;
import io.github.cowwoc.styler.ast.node.DoWhileStatementNode;
import io.github.cowwoc.styler.ast.node.EnumDeclarationNode;
import io.github.cowwoc.styler.ast.node.FlexibleConstructorBodyNode;
import io.github.cowwoc.styler.ast.node.ForStatementNode;
import io.github.cowwoc.styler.ast.node.IfStatementNode;
import io.github.cowwoc.styler.ast.node.InstanceMainMethodNode;
import io.github.cowwoc.styler.ast.node.InterfaceDeclarationNode;
import io.github.cowwoc.styler.ast.node.LambdaExpressionNode;
import io.github.cowwoc.styler.ast.node.MethodDeclarationNode;
import io.github.cowwoc.styler.ast.node.ModuleDeclarationNode;
import io.github.cowwoc.styler.ast.node.NewExpressionNode;
import io.github.cowwoc.styler.ast.node.RecordDeclarationNode;
import io.github.cowwoc.styler.ast.node.SynchronizedStatementNode;
import io.github.cowwoc.styler.ast.node.TryStatementNode;
import io.github.cowwoc.styler.ast.node.UnnamedClassNode;
import io.github.cowwoc.styler.ast.node.WhileStatementNode;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Categorization of AST nodes for brace formatting rules.
 *
 * <p>Different Java constructs can have different brace styles configured (e.g., Allman for classes,
 * K&amp;R for methods). This enum categorizes nodes into groups that share brace formatting behavior.
 *
 * <p><strong>Mapping to Configuration:</strong>
 * <ul>
 *   <li>{@link #CLASS_DECLARATION} → classBraceStyle</li>
 *   <li>{@link #METHOD_DECLARATION}, {@link #CONSTRUCTOR_DECLARATION} → methodBraceStyle</li>
 *   <li>{@link #CONTROL_STRUCTURE} → controlBraceStyle</li>
 *   <li>{@link #LAMBDA_EXPRESSION}, {@link #ANONYMOUS_CLASS} → general braceStyle</li>
 *   <li>{@link #EMPTY_BLOCK} → emptyBlockStyle</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> This enum is immutable and thread-safe.
 */
public enum NodeCategory
{
	/**
	 * Class, interface, enum, record, annotation, module, or unnamed class declaration.
	 */
	CLASS_DECLARATION,

	/**
	 * Method, compact main method, or instance main method declaration.
	 */
	METHOD_DECLARATION,

	/**
	 * Constructor or flexible constructor body declaration.
	 */
	CONSTRUCTOR_DECLARATION,

	/**
	 * Control structure: if, for, while, do-while, try, catch, finally, synchronized.
	 */
	CONTROL_STRUCTURE,

	/**
	 * Lambda expression with block body.
	 */
	LAMBDA_EXPRESSION,

	/**
	 * Anonymous class instance creation.
	 */
	ANONYMOUS_CLASS,

	/**
	 * Empty block (no statements).
	 */
	EMPTY_BLOCK;

	/**
	 * Categorizes an AST node based on its type.
	 *
	 * @param node the AST node to categorize, never {@code null}
	 * @return the appropriate category for this node
	 * @throws NullPointerException if {@code node} is {@code null}
	 * @throws IllegalArgumentException if node type has no brace formatting category
	 */
	public static NodeCategory categorize(ASTNode node)
	{
		requireThat(node, "node").isNotNull();

		return switch (node)
		{
			case ClassDeclarationNode _ -> CLASS_DECLARATION;
			case InterfaceDeclarationNode _ -> CLASS_DECLARATION;
			case EnumDeclarationNode _ -> CLASS_DECLARATION;
			case RecordDeclarationNode _ -> CLASS_DECLARATION;
			case AnnotationDeclarationNode _ -> CLASS_DECLARATION;
			case ModuleDeclarationNode _ -> CLASS_DECLARATION;
			case UnnamedClassNode _ -> CLASS_DECLARATION;
			case MethodDeclarationNode _ -> METHOD_DECLARATION;
			case CompactMainMethodNode _ -> METHOD_DECLARATION;
			case InstanceMainMethodNode _ -> METHOD_DECLARATION;
			case ConstructorDeclarationNode _ -> CONSTRUCTOR_DECLARATION;
			case FlexibleConstructorBodyNode _ -> CONSTRUCTOR_DECLARATION;
			case IfStatementNode _ -> CONTROL_STRUCTURE;
			case ForStatementNode _ -> CONTROL_STRUCTURE;
			case WhileStatementNode _ -> CONTROL_STRUCTURE;
			case DoWhileStatementNode _ -> CONTROL_STRUCTURE;
			case TryStatementNode _ -> CONTROL_STRUCTURE;
			case SynchronizedStatementNode _ -> CONTROL_STRUCTURE;
			case LambdaExpressionNode _ -> LAMBDA_EXPRESSION;
			case NewExpressionNode n when hasAnonymousClassBody(n) -> ANONYMOUS_CLASS;
			case BlockStatementNode n when n.getStatements().isEmpty() -> EMPTY_BLOCK;
			default -> throw new IllegalArgumentException(
				"Node type does not have brace formatting category: " + node.getClass().getSimpleName());
		};
	}

	/**
	 * Determines if a {@link NewExpressionNode} has an anonymous class body.
	 *
	 * <p><strong>Current Limitation:</strong> The AST parser does not currently parse anonymous class
	 * bodies, so this method always returns {@code false}. Anonymous class support requires parser
	 * enhancement to detect and store class body information in {@link NewExpressionNode}.
	 *
	 * <p>Parser changes needed:
	 * <ul>
	 *   <li>Detect LBRACE token after constructor arguments in {@code parseNewExpression()}</li>
	 *   <li>Parse class body members when present</li>
	 *   <li>Store class body AST nodes in {@link NewExpressionNode}</li>
	 *   <li>Add {@code getClassBody()} method returning {@code Optional&lt;List&lt;ASTNode&gt;&gt;}</li>
	 * </ul>
	 *
	 * @param node the new expression node to check
	 * @return {@code true} if the node has an anonymous class body, {@code false} otherwise
	 */
	// Parameter reserved for parser enhancement
	@SuppressWarnings("PMD.UnusedFormalParameter")
	private static boolean hasAnonymousClassBody(NewExpressionNode node)
	{
		// Parser does not currently support anonymous class body detection
		// See class-level JavaDoc for implementation requirements
		return false;
	}

	/**
	 * Returns the configuration key for this category.
	 *
	 * <p>Used to map category to
	 * {@link io.github.cowwoc.styler.formatter.api.BraceFormatterRuleConfiguration#getEffectiveBraceStyle(String)}
	 * parameter.
	 *
	 * @return the configuration key string ({@code "class"}, {@code "method"}, {@code "control"}, or
	 *  {@code "general"})
	 */
	public String getConfigurationKey()
	{
		return switch (this)
		{
			case CLASS_DECLARATION -> "class";
			case METHOD_DECLARATION, CONSTRUCTOR_DECLARATION -> "method";
			case CONTROL_STRUCTURE -> "control";
			case LAMBDA_EXPRESSION, ANONYMOUS_CLASS, EMPTY_BLOCK -> "general";
		};
	}
}
