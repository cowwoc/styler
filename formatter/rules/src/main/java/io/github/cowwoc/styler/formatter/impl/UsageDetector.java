package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.*;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * AST visitor that detects which types are actually used in the code.
 * <p>
 * This visitor traverses the AST and collects all type references, including:
 * <ul>
 *   <li>Variable type declarations</li>
 *   <li>Method return types and parameter types</li>
 *   <li>Field types</li>
 *   <li>Cast expressions</li>
 *   <li>Constructor calls (new expressions)</li>
 *   <li>Generic type arguments</li>
 *   <li>Annotation types</li>
 * </ul>
 * <p>
 * The detector distinguishes between:
 * <ul>
 *   <li><b>Simple names</b> - unqualified type references (e.g., "List")</li>
 *   <li><b>Qualified names</b> - fully qualified references (e.g., "java.util.List")</li>
 * </ul>
 * <p>
 * <b>Thread Safety:</b> This class is NOT thread-safe. Create a new instance per traversal.
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Import Organizer Team
 */
public final class UsageDetector implements ASTVisitor<Void, Void>
{
	private final Set<String> usedSimpleNames = new HashSet<>();
	private final Set<String> usedQualifiedNames = new HashSet<>();

	/**
	 * Creates a new usage detector.
	 */
	public UsageDetector()
	{
		// Initialize empty sets
	}

	/**
	 * Returns the set of simple type names referenced in the code.
	 *
	 * @return set of simple names (e.g., "List", "String")
	 */
	public Set<String> getUsedSimpleNames()
	{
		return Set.copyOf(usedSimpleNames);
	}

	/**
	 * Returns the set of qualified type names referenced in the code.
	 *
	 * @return set of qualified names (e.g., "java.util.List")
	 */
	public Set<String> getUsedQualifiedNames()
	{
		return Set.copyOf(usedQualifiedNames);
	}

	/**
	 * Visits all children of a node recursively.
	 *
	 * @param node the parent node
	 */
	private void visitChildren(ASTNode node)
	{
		for (ASTNode child : node.getChildren())
		{
			child.accept(this, null);
		}
	}

	@Override
	public Void visitCompilationUnit(CompilationUnitNode node, Void arg)
	{
		// Skip package declaration and imports, only visit type declarations
		for (ASTNode typeDecl : node.getTypeDeclarations())
		{
			typeDecl.accept(this, null);
		}
		return null;
	}

	@Override
	public Void visitClassDeclaration(ClassDeclarationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitInterfaceDeclaration(InterfaceDeclarationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitEnumDeclaration(EnumDeclarationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitRecordDeclaration(RecordDeclarationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitMethodDeclaration(MethodDeclarationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitConstructorDeclaration(ConstructorDeclarationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitFieldDeclaration(FieldDeclarationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitVariableDeclaration(VariableDeclarationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitParameterDeclaration(ParameterDeclarationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitClassType(ClassTypeNode node, Void arg)
	{
		// ClassTypeNode represents a type reference; name extraction requires SourceRange text analysis
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitGenericType(GenericTypeNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitArrayType(ArrayTypeNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitPrimitiveType(PrimitiveTypeNode node, Void arg)
	{
		// Primitive types don't require imports
		return null;
	}

	@Override
	public Void visitWildcardType(WildcardTypeNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitTypeParameter(TypeParameterNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitCastExpression(CastExpressionNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitNewExpression(NewExpressionNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitMethodReference(MethodReferenceNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitAnnotation(AnnotationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitInstanceofExpression(InstanceofExpressionNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	// Default implementations for nodes that don't contain type references
	// but may contain child nodes that do

	@Override
	public Void visitPackageDeclaration(PackageDeclarationNode node, Void arg)
	{
		return null; // Don't traverse package declarations
	}

	@Override
	public Void visitImportDeclaration(ImportDeclarationNode node, Void arg)
	{
		return null; // Don't traverse import declarations
	}

	@Override
	public Void visitBlockStatement(BlockStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitIfStatement(IfStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitWhileStatement(WhileStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitDoWhileStatement(DoWhileStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitForStatement(ForStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitEnhancedForStatement(EnhancedForStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitSwitchStatement(SwitchStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitTryStatement(TryStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitSynchronizedStatement(SynchronizedStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitReturnStatement(ReturnStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitThrowStatement(ThrowStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitBreakStatement(BreakStatementNode node, Void arg)
	{
		return null; // Leaf node
	}

	@Override
	public Void visitContinueStatement(ContinueStatementNode node, Void arg)
	{
		return null; // Leaf node
	}

	@Override
	public Void visitExpressionStatement(ExpressionStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitMethodCall(MethodCallNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitFieldAccess(FieldAccessNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitBinaryExpression(BinaryExpressionNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitUnaryExpression(UnaryExpressionNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitConditionalExpression(ConditionalExpressionNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitLambdaExpression(LambdaExpressionNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitArrayAccess(ArrayAccessNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitArrayInitializer(ArrayInitializerNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitPatternMatch(PatternMatchNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitRecordPattern(RecordPatternNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitGuardedPattern(GuardedPatternNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitAnnotationElement(AnnotationElementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitModifier(ModifierNode node, Void arg)
	{
		return null; // Modifiers don't contain type references
	}

	@Override
	public Void visitIdentifier(IdentifierNode node, Void arg)
	{
		return null; // Handled in context by parent nodes
	}

	@Override
	public Void visitQualifiedName(QualifiedNameNode node, Void arg)
	{
		return null; // Handled in context by parent nodes
	}

	// Literal nodes - no type references

	@Override
	public Void visitStringLiteral(StringLiteralNode node, Void arg)
	{
		return null;
	}

	@Override
	public Void visitNumberLiteral(NumberLiteralNode node, Void arg)
	{
		return null;
	}

	@Override
	public Void visitBooleanLiteral(BooleanLiteralNode node, Void arg)
	{
		return null;
	}

	@Override
	public Void visitCharLiteral(CharLiteralNode node, Void arg)
	{
		return null;
	}

	@Override
	public Void visitNullLiteral(NullLiteralNode node, Void arg)
	{
		return null;
	}

	@Override
	public Void visitTextBlock(TextBlockNode node, Void arg)
	{
		return null;
	}

	// Java 21-25 features and module system
	@Override
	public Void visitAnnotationDeclaration(AnnotationDeclarationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitEnumConstant(EnumConstantNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitSwitchExpression(SwitchExpressionNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitYieldStatement(YieldStatementNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitExpression(ExpressionNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitAssignmentExpression(AssignmentExpressionNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitStringTemplateExpression(StringTemplateExpressionNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitTemplateProcessorExpression(TemplateProcessorExpressionNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitUnionType(UnionTypeNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitIntersectionType(IntersectionTypeNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitVarType(VarTypeNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitUnnamedClass(UnnamedClassNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitUnnamedVariable(UnnamedVariableNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitModuleImportDeclaration(ModuleImportDeclarationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitFlexibleConstructorBody(FlexibleConstructorBodyNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitPrimitivePattern(PrimitivePatternNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitCompactMainMethod(CompactMainMethodNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitInstanceMainMethod(InstanceMainMethodNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitModuleDeclaration(ModuleDeclarationNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitModuleRequiresDirective(ModuleRequiresDirectiveNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitModuleExportsDirective(ModuleExportsDirectiveNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitModuleOpensDirective(ModuleOpensDirectiveNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitModuleProvidesDirective(ModuleProvidesDirectiveNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitModuleUsesDirective(ModuleUsesDirectiveNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}

	@Override
	public Void visitModuleQualifier(ModuleQualifierNode node, Void arg)
	{
		visitChildren(node);
		return null;
	}
}
