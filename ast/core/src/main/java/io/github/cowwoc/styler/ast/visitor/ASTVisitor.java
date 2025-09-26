package io.github.cowwoc.styler.ast.visitor;

import io.github.cowwoc.styler.ast.node.*;

/**
 * Visitor interface for traversing Abstract Syntax Tree nodes.
 * This interface implements the classic Visitor pattern with generic return type R and argument type A.
 * All AST nodes support the visitor pattern through their accept() method.
 *
 * @param <R> the return type of visit methods
 * @param <A> the argument type passed to visit methods
 */
public interface ASTVisitor<R, A> {
	// Compilation unit (root node)
	R visitCompilationUnit(CompilationUnitNode node, final A arg);

	// Declaration nodes
	R visitClassDeclaration(ClassDeclarationNode node, final A arg);
	R visitInterfaceDeclaration(InterfaceDeclarationNode node, final A arg);
	R visitEnumDeclaration(EnumDeclarationNode node, final A arg);
	R visitRecordDeclaration(RecordDeclarationNode node, final A arg);
	R visitMethodDeclaration(MethodDeclarationNode node, final A arg);
	R visitConstructorDeclaration(ConstructorDeclarationNode node, final A arg);
	R visitFieldDeclaration(FieldDeclarationNode node, final A arg);
	R visitVariableDeclaration(VariableDeclarationNode node, final A arg);
	R visitParameterDeclaration(ParameterDeclarationNode node, final A arg);

	// Statement nodes
	R visitBlockStatement(BlockStatementNode node, final A arg);
	R visitExpressionStatement(ExpressionStatementNode node, final A arg);
	R visitIfStatement(IfStatementNode node, final A arg);
	R visitForStatement(ForStatementNode node, final A arg);
	R visitEnhancedForStatement(EnhancedForStatementNode node, final A arg);
	R visitWhileStatement(WhileStatementNode node, final A arg);
	R visitDoWhileStatement(DoWhileStatementNode node, final A arg);
	R visitSwitchStatement(SwitchStatementNode node, final A arg);
	R visitTryStatement(TryStatementNode node, final A arg);
	R visitReturnStatement(ReturnStatementNode node, final A arg);
	R visitBreakStatement(BreakStatementNode node, final A arg);
	R visitContinueStatement(ContinueStatementNode node, final A arg);
	R visitThrowStatement(ThrowStatementNode node, final A arg);
	R visitSynchronizedStatement(SynchronizedStatementNode node, final A arg);

	// Expression nodes
	R visitBinaryExpression(BinaryExpressionNode node, final A arg);
	R visitUnaryExpression(UnaryExpressionNode node, final A arg);
	R visitMethodCall(MethodCallNode node, final A arg);
	R visitFieldAccess(FieldAccessNode node, final A arg);
	R visitArrayAccess(ArrayAccessNode node, final A arg);
	R visitLambdaExpression(LambdaExpressionNode node, final A arg);
	R visitMethodReference(MethodReferenceNode node, final A arg);
	R visitConditionalExpression(ConditionalExpressionNode node, final A arg);
	R visitCastExpression(CastExpressionNode node, final A arg);
	R visitInstanceofExpression(InstanceofExpressionNode node, final A arg);
	R visitNewExpression(NewExpressionNode node, final A arg);
	R visitArrayInitializer(ArrayInitializerNode node, final A arg);

	// Literal nodes
	R visitStringLiteral(StringLiteralNode node, final A arg);
	R visitNumberLiteral(NumberLiteralNode node, final A arg);
	R visitBooleanLiteral(BooleanLiteralNode node, final A arg);
	R visitNullLiteral(NullLiteralNode node, final A arg);
	R visitCharLiteral(CharLiteralNode node, final A arg);
	R visitTextBlock(TextBlockNode node, final A arg);

	// Type nodes
	R visitPrimitiveType(PrimitiveTypeNode node, final A arg);
	R visitClassType(ClassTypeNode node, final A arg);
	R visitArrayType(ArrayTypeNode node, final A arg);
	R visitGenericType(GenericTypeNode node, final A arg);
	R visitWildcardType(WildcardTypeNode node, final A arg);
	R visitTypeParameter(TypeParameterNode node, final A arg);

	// Pattern matching nodes (modern Java)
	R visitPatternMatch(PatternMatchNode node, final A arg);
	R visitGuardedPattern(GuardedPatternNode node, final A arg);
	R visitRecordPattern(RecordPatternNode node, final A arg);

	// Identifier and name nodes
	R visitIdentifier(IdentifierNode node, final A arg);
	R visitQualifiedName(QualifiedNameNode node, final A arg);

	// Import and package nodes
	R visitPackageDeclaration(PackageDeclarationNode node, final A arg);
	R visitImportDeclaration(ImportDeclarationNode node, final A arg);

	// Annotation nodes
	R visitAnnotation(AnnotationNode node, final A arg);
	R visitAnnotationElement(AnnotationElementNode node, final A arg);

	// Modifier nodes
	R visitModifier(ModifierNode node, final A arg);
}
