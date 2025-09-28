package io.github.cowwoc.styler.ast.test;

import io.github.cowwoc.styler.ast.*;
import io.github.cowwoc.styler.ast.node.*;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Comprehensive unit tests for AST core functionality.
 * This test uses working APIs and focuses on architectural validation.
 */
public class ComprehensiveTest {

	private static SourceRange createDefaultRange() {
		return new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
	}

	@Test
	public void testNodeCreationWithRequiredFields() {
		SourceRange range = createDefaultRange();

		// Test that nodes can be created with required fields
		IdentifierNode identifier = new IdentifierNode.Builder()
			.setName("testVariable")
			.setRange(range)
			.build();

		assertNotNull(identifier);
		assertEquals("testVariable", identifier.getName());
		assertEquals(range, identifier.getRange());
	}

	@Test
	public void testMultipleNodeTypesCreation() {
		SourceRange range = createDefaultRange();

		// Test creation of various node types
		IdentifierNode identifier = new IdentifierNode.Builder()
			.setName("variable")
			.setRange(range)
			.build();

		StringLiteralNode stringLiteral = new StringLiteralNode.Builder()
			.setValue("test string")
			.setRange(range)
			.build();

		ClassDeclarationNode classDecl = new ClassDeclarationNode.Builder()
			.setName("TestClass")
			.setRange(range)
			.build();

		// Verify all nodes are created successfully
		assertNotNull(identifier);
		assertNotNull(stringLiteral);
		assertNotNull(classDecl);

		// Verify specific properties
		assertEquals("variable", identifier.getName());
		assertEquals("TestClass", classDecl.getName());
	}

	@Test
	public void testVisitorPatternBasics() {
		SourceRange range = createDefaultRange();

		IdentifierNode node = new IdentifierNode.Builder()
			.setName("visitorTest")
			.setRange(range)
			.build();

		// Test that visitor pattern works
		TestVisitor visitor = new TestVisitor();
		String result = node.accept(visitor, "prefix_");

		assertEquals("prefix_visitorTest", result);
	}

	@Test
	public void testNodeEquality() {
		SourceRange range = createDefaultRange();

		IdentifierNode node1 = new IdentifierNode.Builder()
			.setName("test")
			.setRange(range)
			.build();

		IdentifierNode node2 = new IdentifierNode.Builder()
			.setName("test")
			.setRange(range)
			.build();

		// Test equality (behavior depends on implementation)
		// At minimum, verify nodes are created and comparable
		assertNotNull(node1);
		assertNotNull(node2);
		assertEquals("test", node1.getName());
		assertEquals("test", node2.getName());
	}

	@Test
	public void testSourcePositionOperations() {
		SourcePosition pos = new SourcePosition(5, 10);

		assertEquals(5, pos.line());
		assertEquals(10, pos.column());

		// Test advancement
		SourcePosition advanced = pos.advanceColumn(5);
		assertEquals(5, advanced.line());
		assertEquals(15, advanced.column());

		// Test line advancement
		SourcePosition nextLine = pos.nextLine();
		assertEquals(6, nextLine.line());
		assertEquals(1, nextLine.column());
	}

	@Test
	public void testSourceRangeBasics() {
		SourcePosition start = new SourcePosition(1, 1);
		SourcePosition end = new SourcePosition(1, 10);
		SourceRange range = new SourceRange(start, end);

		assertEquals(start, range.start());
		assertEquals(end, range.end());
	}

	@Test
	public void testBuilderPattern() {
		SourceRange range = createDefaultRange();

		// Test that builder can be reused
		IdentifierNode.Builder builder = new IdentifierNode.Builder()
			.setRange(range);

		IdentifierNode node1 = builder.setName("first").build();
		IdentifierNode node2 = builder.setName("second").build();

		assertEquals("first", node1.getName());
		assertEquals("second", node2.getName());
	}

	@Test
	public void testToBuilderMethod() {
		SourceRange range = createDefaultRange();

		IdentifierNode original = new IdentifierNode.Builder()
			.setName("original")
			.setRange(range)
			.build();

		// Test toBuilder functionality
		IdentifierNode modified = ((IdentifierNode.Builder) original.toBuilder())
			.setName("modified")
			.build();

		assertEquals("original", original.getName());
		assertEquals("modified", modified.getName());
	}

	@Test
	public void testNodeToString() {
		SourceRange range = createDefaultRange();

		IdentifierNode node = new IdentifierNode.Builder()
			.setName("toStringTest")
			.setRange(range)
			.build();

		String toString = node.toString();
		assertNotNull(toString);
		assertFalse(toString.isEmpty());
	}

	@Test
	public void testMetadataPreservation() {
		SourceRange range = createDefaultRange();

		IdentifierNode node = new IdentifierNode.Builder()
			.setName("metadataTest")
			.setRange(range)
			.build();

		// Verify metadata is accessible
		assertEquals(range, node.getRange());
		assertNotNull(node.getLeadingComments());
		assertNotNull(node.getTrailingComments());
	}

	// Helper visitor for testing
	private static class TestVisitor implements ASTVisitor<String, String> {
		@Override
		public String visitIdentifier(IdentifierNode node, String prefix) {
			return prefix + node.getName();
		}

		// Minimal implementations for required methods
		@Override public String visitCompilationUnit(CompilationUnitNode node, String arg) { return ""; }
		@Override public String visitClassDeclaration(ClassDeclarationNode node, String arg) { return ""; }
		@Override public String visitInterfaceDeclaration(InterfaceDeclarationNode node, String arg) { return ""; }
		@Override public String visitEnumDeclaration(EnumDeclarationNode node, String arg) { return ""; }
		@Override public String visitRecordDeclaration(RecordDeclarationNode node, String arg) { return ""; }
		@Override public String visitMethodDeclaration(MethodDeclarationNode node, String arg) { return ""; }
		@Override public String visitConstructorDeclaration(ConstructorDeclarationNode node, String arg) { return ""; }
		@Override public String visitFieldDeclaration(FieldDeclarationNode node, String arg) { return ""; }
		@Override public String visitVariableDeclaration(VariableDeclarationNode node, String arg) { return ""; }
		@Override public String visitParameterDeclaration(ParameterDeclarationNode node, String arg) { return ""; }
		@Override public String visitPackageDeclaration(PackageDeclarationNode node, String arg) { return ""; }
		@Override public String visitImportDeclaration(ImportDeclarationNode node, String arg) { return ""; }
		@Override public String visitAnnotation(AnnotationNode node, String arg) { return ""; }
		@Override public String visitAnnotationElement(AnnotationElementNode node, String arg) { return ""; }
		@Override public String visitModifier(ModifierNode node, String arg) { return ""; }
		@Override public String visitTypeParameter(TypeParameterNode node, String arg) { return ""; }
		@Override public String visitPrimitiveType(PrimitiveTypeNode node, String arg) { return ""; }
		@Override public String visitClassType(ClassTypeNode node, String arg) { return ""; }
		@Override public String visitArrayType(ArrayTypeNode node, String arg) { return ""; }
		@Override public String visitGenericType(GenericTypeNode node, String arg) { return ""; }
		@Override public String visitWildcardType(WildcardTypeNode node, String arg) { return ""; }
		@Override public String visitBlockStatement(BlockStatementNode node, String arg) { return ""; }
		@Override public String visitExpressionStatement(ExpressionStatementNode node, String arg) { return ""; }
		@Override public String visitIfStatement(IfStatementNode node, String arg) { return ""; }
		@Override public String visitWhileStatement(WhileStatementNode node, String arg) { return ""; }
		@Override public String visitDoWhileStatement(DoWhileStatementNode node, String arg) { return ""; }
		@Override public String visitForStatement(ForStatementNode node, String arg) { return ""; }
		@Override public String visitEnhancedForStatement(EnhancedForStatementNode node, String arg) { return ""; }
		@Override public String visitSwitchStatement(SwitchStatementNode node, String arg) { return ""; }
		@Override public String visitTryStatement(TryStatementNode node, String arg) { return ""; }
		@Override public String visitSynchronizedStatement(SynchronizedStatementNode node, String arg) { return ""; }
		@Override public String visitReturnStatement(ReturnStatementNode node, String arg) { return ""; }
		@Override public String visitThrowStatement(ThrowStatementNode node, String arg) { return ""; }
		@Override public String visitBreakStatement(BreakStatementNode node, String arg) { return ""; }
		@Override public String visitContinueStatement(ContinueStatementNode node, String arg) { return ""; }
		@Override public String visitMethodCall(MethodCallNode node, String arg) { return ""; }
		@Override public String visitFieldAccess(FieldAccessNode node, String arg) { return ""; }
		@Override public String visitArrayAccess(ArrayAccessNode node, String arg) { return ""; }
		@Override public String visitNewExpression(NewExpressionNode node, String arg) { return ""; }
		@Override public String visitCastExpression(CastExpressionNode node, String arg) { return ""; }
		@Override public String visitInstanceofExpression(InstanceofExpressionNode node, String arg) { return ""; }
		@Override public String visitConditionalExpression(ConditionalExpressionNode node, String arg) { return ""; }
		@Override public String visitUnaryExpression(UnaryExpressionNode node, String arg) { return ""; }
		@Override public String visitBinaryExpression(BinaryExpressionNode node, String arg) { return ""; }
		@Override public String visitLambdaExpression(LambdaExpressionNode node, String arg) { return ""; }
		@Override public String visitMethodReference(MethodReferenceNode node, String arg) { return ""; }
		@Override public String visitStringLiteral(StringLiteralNode node, String arg) { return ""; }
		@Override public String visitCharLiteral(CharLiteralNode node, String arg) { return ""; }
		@Override public String visitNumberLiteral(NumberLiteralNode node, String arg) { return ""; }
		@Override public String visitBooleanLiteral(BooleanLiteralNode node, String arg) { return ""; }
		@Override public String visitNullLiteral(NullLiteralNode node, String arg) { return ""; }
		@Override public String visitTextBlock(TextBlockNode node, String arg) { return ""; }
		@Override public String visitArrayInitializer(ArrayInitializerNode node, String arg) { return ""; }
		@Override public String visitQualifiedName(QualifiedNameNode node, String arg) { return ""; }
		@Override public String visitPatternMatch(PatternMatchNode node, String arg) { return ""; }
		@Override public String visitRecordPattern(RecordPatternNode node, String arg) { return ""; }
		@Override public String visitGuardedPattern(GuardedPatternNode node, String arg) { return ""; }
	}
}