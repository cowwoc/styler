package io.github.cowwoc.styler.ast.visitor;

import io.github.cowwoc.styler.ast.node.AnnotationElementNode;
import io.github.cowwoc.styler.ast.node.AnnotationNode;
import io.github.cowwoc.styler.ast.node.ArrayAccessNode;
import io.github.cowwoc.styler.ast.node.ArrayInitializerNode;
import io.github.cowwoc.styler.ast.node.ArrayTypeNode;
import io.github.cowwoc.styler.ast.node.BinaryExpressionNode;
import io.github.cowwoc.styler.ast.node.BlockStatementNode;
import io.github.cowwoc.styler.ast.node.BooleanLiteralNode;
import io.github.cowwoc.styler.ast.node.BreakStatementNode;
import io.github.cowwoc.styler.ast.node.CastExpressionNode;
import io.github.cowwoc.styler.ast.node.CharLiteralNode;
import io.github.cowwoc.styler.ast.node.ClassDeclarationNode;
import io.github.cowwoc.styler.ast.node.ClassTypeNode;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.ast.node.ConditionalExpressionNode;
import io.github.cowwoc.styler.ast.node.ConstructorDeclarationNode;
import io.github.cowwoc.styler.ast.node.ContinueStatementNode;
import io.github.cowwoc.styler.ast.node.DoWhileStatementNode;
import io.github.cowwoc.styler.ast.node.EnhancedForStatementNode;
import io.github.cowwoc.styler.ast.node.EnumDeclarationNode;
import io.github.cowwoc.styler.ast.node.ExpressionStatementNode;
import io.github.cowwoc.styler.ast.node.FieldAccessNode;
import io.github.cowwoc.styler.ast.node.FieldDeclarationNode;
import io.github.cowwoc.styler.ast.node.ForStatementNode;
import io.github.cowwoc.styler.ast.node.GenericTypeNode;
import io.github.cowwoc.styler.ast.node.GuardedPatternNode;
import io.github.cowwoc.styler.ast.node.IdentifierNode;
import io.github.cowwoc.styler.ast.node.IfStatementNode;
import io.github.cowwoc.styler.ast.node.ImportDeclarationNode;
import io.github.cowwoc.styler.ast.node.InstanceofExpressionNode;
import io.github.cowwoc.styler.ast.node.InterfaceDeclarationNode;
import io.github.cowwoc.styler.ast.node.LambdaExpressionNode;
import io.github.cowwoc.styler.ast.node.MethodCallNode;
import io.github.cowwoc.styler.ast.node.MethodDeclarationNode;
import io.github.cowwoc.styler.ast.node.MethodReferenceNode;
import io.github.cowwoc.styler.ast.node.ModifierNode;
import io.github.cowwoc.styler.ast.node.NewExpressionNode;
import io.github.cowwoc.styler.ast.node.NullLiteralNode;
import io.github.cowwoc.styler.ast.node.NumberLiteralNode;
import io.github.cowwoc.styler.ast.node.PackageDeclarationNode;
import io.github.cowwoc.styler.ast.node.ParameterDeclarationNode;
import io.github.cowwoc.styler.ast.node.PatternMatchNode;
import io.github.cowwoc.styler.ast.node.PrimitiveTypeNode;
import io.github.cowwoc.styler.ast.node.QualifiedNameNode;
import io.github.cowwoc.styler.ast.node.RecordDeclarationNode;
import io.github.cowwoc.styler.ast.node.RecordPatternNode;
import io.github.cowwoc.styler.ast.node.ReturnStatementNode;
import io.github.cowwoc.styler.ast.node.StringLiteralNode;
import io.github.cowwoc.styler.ast.node.SwitchStatementNode;
import io.github.cowwoc.styler.ast.node.SynchronizedStatementNode;
import io.github.cowwoc.styler.ast.node.TextBlockNode;
import io.github.cowwoc.styler.ast.node.ThrowStatementNode;
import io.github.cowwoc.styler.ast.node.TryStatementNode;
import io.github.cowwoc.styler.ast.node.TypeParameterNode;
import io.github.cowwoc.styler.ast.node.UnaryExpressionNode;
import io.github.cowwoc.styler.ast.node.VariableDeclarationNode;
import io.github.cowwoc.styler.ast.node.WhileStatementNode;
import io.github.cowwoc.styler.ast.node.WildcardTypeNode;

/**
 * Visitor interface for traversing Abstract Syntax Tree nodes.
 * This interface implements the classic Visitor pattern with generic return type R and argument type A.
 * All AST nodes support the visitor pattern through their accept() method.
 *
 * @param <R> the return type of visit methods
 *
 * @param <A> the argument type passed to visit methods
 */
public interface ASTVisitor<R, A>
	{
	/**
	 * Visits a compilation unit node.
	 *
	 * @param node the compilation unit node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitCompilationUnit(CompilationUnitNode node, final A arg);

	/**
	 * Visits a class declaration node.
	 *
	 * @param node the class declaration node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitClassDeclaration(ClassDeclarationNode node, final A arg);
	/**
	 * Visits an interface declaration node.
	 *
	 * @param node the interface declaration node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitInterfaceDeclaration(InterfaceDeclarationNode node, final A arg);
	/**
	 * Visits an enum declaration node.
	 *
	 * @param node the enum declaration node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitEnumDeclaration(EnumDeclarationNode node, final A arg);
	/**
	 * Visits a record declaration node.
	 *
	 * @param node the record declaration node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitRecordDeclaration(RecordDeclarationNode node, final A arg);
	/**
	 * Visits a method declaration node.
	 *
	 * @param node the method declaration node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitMethodDeclaration(MethodDeclarationNode node, final A arg);
	/**
	 * Visits a constructor declaration node.
	 *
	 * @param node the constructor declaration node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitConstructorDeclaration(ConstructorDeclarationNode node, final A arg);
	/**
	 * Visits a field declaration node.
	 *
	 * @param node the field declaration node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitFieldDeclaration(FieldDeclarationNode node, final A arg);
	/**
	 * Visits a variable declaration node.
	 *
	 * @param node the variable declaration node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitVariableDeclaration(VariableDeclarationNode node, final A arg);
	/**
	 * Visits a parameter declaration node.
	 *
	 * @param node the parameter declaration node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitParameterDeclaration(ParameterDeclarationNode node, final A arg);

	/**
	 * Visits a block statement node.
	 *
	 * @param node the block statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitBlockStatement(BlockStatementNode node, final A arg);
	/**
	 * Visits an expression statement node.
	 *
	 * @param node the expression statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitExpressionStatement(ExpressionStatementNode node, final A arg);
	/**
	 * Visits an if statement node.
	 *
	 * @param node the if statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitIfStatement(IfStatementNode node, final A arg);
	/**
	 * Visits a for statement node.
	 *
	 * @param node the for statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitForStatement(ForStatementNode node, final A arg);
	/**
	 * Visits an enhanced for statement node.
	 *
	 * @param node the enhanced for statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitEnhancedForStatement(EnhancedForStatementNode node, final A arg);
	/**
	 * Visits a while statement node.
	 *
	 * @param node the while statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitWhileStatement(WhileStatementNode node, final A arg);
	/**
	 * Visits a do-while statement node.
	 *
	 * @param node the do-while statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitDoWhileStatement(DoWhileStatementNode node, final A arg);
	/**
	 * Visits a switch statement node.
	 *
	 * @param node the switch statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitSwitchStatement(SwitchStatementNode node, final A arg);
	/**
	 * Visits a try statement node.
	 *
	 * @param node the try statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitTryStatement(TryStatementNode node, final A arg);
	/**
	 * Visits a return statement node.
	 *
	 * @param node the return statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitReturnStatement(ReturnStatementNode node, final A arg);
	/**
	 * Visits a break statement node.
	 *
	 * @param node the break statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitBreakStatement(BreakStatementNode node, final A arg);
	/**
	 * Visits a continue statement node.
	 *
	 * @param node the continue statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitContinueStatement(ContinueStatementNode node, final A arg);
	/**
	 * Visits a throw statement node.
	 *
	 * @param node the throw statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitThrowStatement(ThrowStatementNode node, final A arg);
	/**
	 * Visits a synchronized statement node.
	 *
	 * @param node the synchronized statement node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitSynchronizedStatement(SynchronizedStatementNode node, final A arg);

	/**
	 * Visits a binary expression node.
	 *
	 * @param node the binary expression node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitBinaryExpression(BinaryExpressionNode node, final A arg);
	/**
	 * Visits a unary expression node.
	 *
	 * @param node the unary expression node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitUnaryExpression(UnaryExpressionNode node, final A arg);
	/**
	 * Visits a method call node.
	 *
	 * @param node the method call node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitMethodCall(MethodCallNode node, final A arg);
	/**
	 * Visits a field access node.
	 *
	 * @param node the field access node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitFieldAccess(FieldAccessNode node, final A arg);
	/**
	 * Visits an array access node.
	 *
	 * @param node the array access node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitArrayAccess(ArrayAccessNode node, final A arg);
	/**
	 * Visits a lambda expression node.
	 *
	 * @param node the lambda expression node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitLambdaExpression(LambdaExpressionNode node, final A arg);
	/**
	 * Visits a method reference node.
	 *
	 * @param node the method reference node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitMethodReference(MethodReferenceNode node, final A arg);
	/**
	 * Visits a conditional expression node.
	 *
	 * @param node the conditional expression node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitConditionalExpression(ConditionalExpressionNode node, final A arg);
	/**
	 * Visits a cast expression node.
	 *
	 * @param node the cast expression node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitCastExpression(CastExpressionNode node, final A arg);
	/**
	 * Visits an instanceof expression node.
	 *
	 * @param node the instanceof expression node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitInstanceofExpression(InstanceofExpressionNode node, final A arg);
	/**
	 * Visits a new expression node.
	 *
	 * @param node the new expression node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitNewExpression(NewExpressionNode node, final A arg);
	/**
	 * Visits an array initializer node.
	 *
	 * @param node the array initializer node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitArrayInitializer(ArrayInitializerNode node, final A arg);

	/**
	 * Visits a string literal node.
	 *
	 * @param node the string literal node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitStringLiteral(StringLiteralNode node, final A arg);
	/**
	 * Visits a number literal node.
	 *
	 * @param node the number literal node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitNumberLiteral(NumberLiteralNode node, final A arg);
	/**
	 * Visits a boolean literal node.
	 *
	 * @param node the boolean literal node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitBooleanLiteral(BooleanLiteralNode node, final A arg);
	/**
	 * Visits a {@code null} literal node.
	 *
	 * @param node the {@code null} literal node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitNullLiteral(NullLiteralNode node, final A arg);
	/**
	 * Visits a character literal node.
	 *
	 * @param node the character literal node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitCharLiteral(CharLiteralNode node, final A arg);
	/**
	 * Visits a text block node.
	 *
	 * @param node the text block node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitTextBlock(TextBlockNode node, final A arg);

	/**
	 * Visits a primitive type node.
	 *
	 * @param node the primitive type node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitPrimitiveType(PrimitiveTypeNode node, final A arg);
	/**
	 * Visits a class type node.
	 *
	 * @param node the class type node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitClassType(ClassTypeNode node, final A arg);
	/**
	 * Visits an array type node.
	 *
	 * @param node the array type node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitArrayType(ArrayTypeNode node, final A arg);
	/**
	 * Visits a generic type node.
	 *
	 * @param node the generic type node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitGenericType(GenericTypeNode node, final A arg);
	/**
	 * Visits a wildcard type node.
	 *
	 * @param node the wildcard type node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitWildcardType(WildcardTypeNode node, final A arg);
	/**
	 * Visits a type parameter node.
	 *
	 * @param node the type parameter node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitTypeParameter(TypeParameterNode node, final A arg);

	/**
	 * Visits a pattern match node.
	 *
	 * @param node the pattern match node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitPatternMatch(PatternMatchNode node, final A arg);
	/**
	 * Visits a guarded pattern node.
	 *
	 * @param node the guarded pattern node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitGuardedPattern(GuardedPatternNode node, final A arg);
	/**
	 * Visits a record pattern node.
	 *
	 * @param node the record pattern node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitRecordPattern(RecordPatternNode node, final A arg);

	/**
	 * Visits an identifier node.
	 *
	 * @param node the identifier node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitIdentifier(IdentifierNode node, final A arg);
	/**
	 * Visits a qualified name node.
	 *
	 * @param node the qualified name node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitQualifiedName(QualifiedNameNode node, final A arg);

	/**
	 * Visits a package declaration node.
	 *
	 * @param node the package declaration node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitPackageDeclaration(PackageDeclarationNode node, final A arg);
	/**
	 * Visits an import declaration node.
	 *
	 * @param node the import declaration node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitImportDeclaration(ImportDeclarationNode node, final A arg);

	/**
	 * Visits an annotation node.
	 *
	 * @param node the annotation node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitAnnotation(AnnotationNode node, final A arg);
	/**
	 * Visits an annotation element node.
	 *
	 * @param node the annotation element node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitAnnotationElement(AnnotationElementNode node, final A arg);

	/**
	 * Visits a modifier node.
	 *
	 * @param node the modifier node to visit
	 * @param arg visitor-specific argument passed during traversal
	 * @return visitor-specific result
	 */
	R visitModifier(ModifierNode node, final A arg);
}
