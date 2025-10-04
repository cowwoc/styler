package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.node.*;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * AST visitor that collects brace context information from nodes containing braces.
 *
 * <p>This visitor traverses the AST and identifies all Java constructs with braces (classes, methods,
 * control structures, lambdas, etc.), extracting their brace positions and categorization information
 * for formatting analysis.
 *
 * <p><strong>Usage Pattern:</strong>
 * <pre>{@code
 * BraceNodeCollector collector = new BraceNodeCollector(sourceText);
 * List<BraceContext> contexts = new ArrayList<>();
 * compilationUnit.accept(collector, contexts);
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong> This class is stateless regarding instance fields and thread-safe.
 * The {@code sourceText} parameter is immutable, and all mutable state is passed through the visitor argument.
 */
public final class BraceNodeCollector implements ASTVisitor<Void, List<BraceContext>>
{
	private final String sourceText;

	/**
	 * Creates a new brace node collector.
	 *
	 * @param sourceText the source text being analyzed, never {@code null}
	 * @throws NullPointerException if {@code sourceText} is {@code null}
	 */
	public BraceNodeCollector(String sourceText)
	{
		requireThat(sourceText, "sourceText").isNotNull();
		this.sourceText = sourceText;
	}

	/**
	 * Visits all children of a node recursively.
	 *
	 * @param node the parent node, never {@code null}
	 * @param contexts the list to accumulate brace contexts, never {@code null}
	 */
	private void visitChildren(ASTNode node, List<BraceContext> contexts)
	{
		for (ASTNode child : node.getChildren())
		{
			child.accept(this, contexts);
		}
	}

	@Override
	public Void visitCompilationUnit(CompilationUnitNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitClassDeclaration(ClassDeclarationNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitInterfaceDeclaration(InterfaceDeclarationNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitEnumDeclaration(EnumDeclarationNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitRecordDeclaration(RecordDeclarationNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitMethodDeclaration(MethodDeclarationNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitConstructorDeclaration(ConstructorDeclarationNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitFieldDeclaration(FieldDeclarationNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitVariableDeclaration(VariableDeclarationNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitParameterDeclaration(ParameterDeclarationNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitClassType(ClassTypeNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitGenericType(GenericTypeNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitArrayType(ArrayTypeNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitPrimitiveType(PrimitiveTypeNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitWildcardType(WildcardTypeNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitTypeParameter(TypeParameterNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitCastExpression(CastExpressionNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitNewExpression(NewExpressionNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitMethodReference(MethodReferenceNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitAnnotation(AnnotationNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitInstanceofExpression(InstanceofExpressionNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitPackageDeclaration(PackageDeclarationNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitImportDeclaration(ImportDeclarationNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitBlockStatement(BlockStatementNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitIfStatement(IfStatementNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitWhileStatement(WhileStatementNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitDoWhileStatement(DoWhileStatementNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitForStatement(ForStatementNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitEnhancedForStatement(EnhancedForStatementNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitSwitchStatement(SwitchStatementNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitTryStatement(TryStatementNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitSynchronizedStatement(SynchronizedStatementNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitReturnStatement(ReturnStatementNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitThrowStatement(ThrowStatementNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitBreakStatement(BreakStatementNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitContinueStatement(ContinueStatementNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitExpressionStatement(ExpressionStatementNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitMethodCall(MethodCallNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitFieldAccess(FieldAccessNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitBinaryExpression(BinaryExpressionNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitUnaryExpression(UnaryExpressionNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitConditionalExpression(ConditionalExpressionNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitLambdaExpression(LambdaExpressionNode node, List<BraceContext> arg)
	{
		collectBraceContext(node, arg);
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitArrayAccess(ArrayAccessNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitArrayInitializer(ArrayInitializerNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitPatternMatch(PatternMatchNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitRecordPattern(RecordPatternNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitGuardedPattern(GuardedPatternNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitAnnotationElement(AnnotationElementNode node, List<BraceContext> arg)
	{
		visitChildren(node, arg);
		return null;
	}

	@Override
	public Void visitModifier(ModifierNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitIdentifier(IdentifierNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitQualifiedName(QualifiedNameNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitStringLiteral(StringLiteralNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitNumberLiteral(NumberLiteralNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitBooleanLiteral(BooleanLiteralNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitCharLiteral(CharLiteralNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitNullLiteral(NullLiteralNode node, List<BraceContext> arg)
	{
		return null;
	}

	@Override
	public Void visitTextBlock(TextBlockNode node, List<BraceContext> arg)
	{
		return null;
	}

	/**
	 * Collects brace context information from an AST node if it contains braces.
	 *
	 * <p>This method finds the opening and closing brace positions in the source text,
	 * categorizes the node, and creates a {@link BraceContext} record for formatting analysis.
	 *
	 * @param node the AST node to analyze, never {@code null}
	 * @param contexts the list to accumulate brace contexts, never {@code null}
	 * @throws NullPointerException if {@code node} or {@code contexts} is {@code null}
	 */
	private void collectBraceContext(ASTNode node, List<BraceContext> contexts)
	{
		requireThat(node, "node").isNotNull();
		requireThat(contexts, "contexts").isNotNull();

		// Find braces in source text within node's range
		SourceRange nodeRange = node.getRange();
		int startOffset = SourceTextUtil.positionToOffset(sourceText,
			nodeRange.start().line(), nodeRange.start().column());
		int endOffset = SourceTextUtil.positionToOffset(sourceText,
			nodeRange.end().line(), nodeRange.end().column());

		String nodeText = sourceText.substring(startOffset, endOffset);

		// Find opening brace
		int openingBraceIndex = nodeText.indexOf('{');
		if (openingBraceIndex == -1)
		{
			// Node has no braces (e.g., abstract method, interface method)
			return;
		}

		int openingBraceOffset = startOffset + openingBraceIndex;
		int[] openingPos = SourceTextUtil.offsetToPosition(sourceText, openingBraceOffset);
		SourcePosition openingPosition = new SourcePosition(openingPos[0], openingPos[1]);
		SourceRange openingBraceRange = new SourceRange(openingPosition, openingPosition);

		// Find closing brace (last occurrence in node text)
		int closingBraceIndex = nodeText.lastIndexOf('}');
		if (closingBraceIndex == -1)
		{
			// Malformed node - opening brace without closing
			return;
		}

		int closingBraceOffset = startOffset + closingBraceIndex;
		int[] closingPos = SourceTextUtil.offsetToPosition(sourceText, closingBraceOffset);
		SourcePosition closingPosition = new SourcePosition(closingPos[0], closingPos[1]);
		SourceRange closingBraceRange = new SourceRange(closingPosition, closingPosition);

		// Determine if block is empty
		boolean isEmpty = false;
		if (node instanceof BlockStatementNode blockNode)
		{
			isEmpty = blockNode.getStatements().isEmpty();
		}

		// Categorize node and create context
		NodeCategory category = NodeCategory.categorize(node);
		BraceContext context = new BraceContext(node, category, openingBraceRange,
			closingBraceRange, isEmpty);
		contexts.add(context);
	}
}
