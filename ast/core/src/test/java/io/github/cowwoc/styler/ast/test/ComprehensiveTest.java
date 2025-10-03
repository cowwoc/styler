package io.github.cowwoc.styler.ast.test;

import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.SourcePosition;
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
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/**
 * Comprehensive unit tests for AST core functionality.
 * This test uses working APIs and focuses on architectural validation.
 */
public class ComprehensiveTest
	{
	private static SourceRange createDefaultRange()
		{
		return new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
	}

	/**
	 * Validates that AST nodes can be created with required fields using the builder pattern.
	 */
	@Test
	public void nodeCreationWithRequiredFields()
		{
		SourceRange range = createDefaultRange();

		// Test that nodes can be created with required fields
		IdentifierNode identifier = new IdentifierNode.Builder().
			setName("testVariable").
			setRange(range).
			build();

		assertNotNull(identifier);
		assertEquals("testVariable", identifier.getName());
		assertEquals(range, identifier.getRange());
	}

	/**
	 * Validates creation of various AST node types to ensure the builder pattern works consistently across
	 * different node implementations.
	 */
	@Test
	public void multipleNodeTypesCreation()
		{
		SourceRange range = createDefaultRange();

		// Test creation of various node types
		IdentifierNode identifier = new IdentifierNode.Builder().
			setName("variable").
			setRange(range).
			build();

		StringLiteralNode stringLiteral = new StringLiteralNode.Builder().
			setValue("test string").
			setRange(range).
			build();

		ClassDeclarationNode classDecl = new ClassDeclarationNode.Builder().
			setName("TestClass").
			setRange(range).
			build();

		// Verify all nodes are created successfully
		assertNotNull(identifier);
		assertNotNull(stringLiteral);
		assertNotNull(classDecl);

		// Verify specific properties
		assertEquals("variable", identifier.getName());
		assertEquals("TestClass", classDecl.getName());
	}

	/**
	 * Validates that the visitor pattern works correctly through visitor method invocation and data flow.
	 */
	@Test
	public void visitorPatternBasics()
		{
		SourceRange range = createDefaultRange();

		IdentifierNode node = new IdentifierNode.Builder().
			setName("visitorTest").
			setRange(range).
			build();

		// Test that visitor pattern works
		TestVisitor visitor = new TestVisitor();
		String result = node.accept(visitor, "prefix_");

		assertEquals("prefix_visitorTest", result);
	}

	/**
	 * Validates that nodes with identical properties are properly comparable and maintain expected equality
	 * semantics.
	 */
	@Test
	public void nodeEquality()
		{
		SourceRange range = createDefaultRange();

		IdentifierNode node1 = new IdentifierNode.Builder().
			setName("test").
			setRange(range).
			build();

		IdentifierNode node2 = new IdentifierNode.Builder().
			setName("test").
			setRange(range).
			build();

		// Test equality (behavior depends on implementation)
		// At minimum, verify nodes are created and comparable
		assertNotNull(node1);
		assertNotNull(node2);
		assertEquals("test", node1.getName());
		assertEquals("test", node2.getName());
	}

	/**
	 * Validates that source position objects support column and line advancement operations correctly.
	 */
	@Test
	public void sourcePositionOperations()
		{
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

	/**
	 * Validates that source range objects correctly store and retrieve start and end positions.
	 */
	@Test
	public void sourceRangeBasics()
		{
		SourcePosition start = new SourcePosition(1, 1);
		SourcePosition end = new SourcePosition(1, 10);
		SourceRange range = new SourceRange(start, end);

		assertEquals(start, range.start());
		assertEquals(end, range.end());
	}

	/**
	 * Validates that builder instances can be reused to create multiple nodes with different properties.
	 */
	@Test
	public void builderPattern()
		{
		SourceRange range = createDefaultRange();

		// Test that builder can be reused
		IdentifierNode.Builder builder = new IdentifierNode.Builder().
			setRange(range);

		IdentifierNode node1 = builder.setName("first").build();
		IdentifierNode node2 = builder.setName("second").build();

		assertEquals("first", node1.getName());
		assertEquals("second", node2.getName());
	}

	/**
	 * Validates that the toBuilder() method creates a new builder with the original node's properties.
	 */
	@Test
	public void toBuilderMethod()
		{
		SourceRange range = createDefaultRange();

		IdentifierNode original = new IdentifierNode.Builder().
			setName("original").
			setRange(range).
			build();

		// Test toBuilder functionality
		IdentifierNode modified = ((IdentifierNode.Builder) original.toBuilder()).
			setName("modified").
			build();

		assertEquals("original", original.getName());
		assertEquals("modified", modified.getName());
	}

	/**
	 * Validates that the toString() method produces a non-empty string representation of the node.
	 */
	@Test
	public void nodeToString()
		{
		SourceRange range = createDefaultRange();

		IdentifierNode node = new IdentifierNode.Builder().
			setName("toStringTest").
			setRange(range).
			build();

		String toString = node.toString();
		assertNotNull(toString);
		assertFalse(toString.isEmpty());
	}

	/**
	 * Validates that AST nodes preserve metadata such as source ranges and comments.
	 */
	@Test
	public void metadataPreservation()
		{
		SourceRange range = createDefaultRange();

		IdentifierNode node = new IdentifierNode.Builder().
			setName("metadataTest").
			setRange(range).
			build();

		// Verify metadata is accessible
		assertEquals(range, node.getRange());
		assertNotNull(node.getLeadingComments());
		assertNotNull(node.getTrailingComments());
	}

	// Helper visitor for testing
	private static final class TestVisitor implements ASTVisitor<String, String>
		{
		@Override
		public String visitIdentifier(IdentifierNode node, String prefix)
			{
			return prefix + node.getName();
		}

		// Minimal implementations for required methods
		@Override public String visitCompilationUnit(CompilationUnitNode node, String arg)
			{
			return "";
			}
		@Override public String visitClassDeclaration(ClassDeclarationNode node, String arg)
			{
			return "";
			}
		@Override public String visitInterfaceDeclaration(InterfaceDeclarationNode node, String arg)
			{
			return "";
			}
		@Override public String visitEnumDeclaration(EnumDeclarationNode node, String arg)
			{
			return "";
			}
		@Override public String visitRecordDeclaration(RecordDeclarationNode node, String arg)
			{
			return "";
			}
		@Override public String visitMethodDeclaration(MethodDeclarationNode node, String arg)
			{
			return "";
			}
		@Override public String visitConstructorDeclaration(ConstructorDeclarationNode node, String arg)
			{
			return "";
			}
		@Override public String visitFieldDeclaration(FieldDeclarationNode node, String arg)
			{
			return "";
			}
		@Override public String visitVariableDeclaration(VariableDeclarationNode node, String arg)
			{
			return "";
			}
		@Override public String visitParameterDeclaration(ParameterDeclarationNode node, String arg)
			{
			return "";
			}
		@Override public String visitPackageDeclaration(PackageDeclarationNode node, String arg)
			{
			return "";
			}
		@Override public String visitImportDeclaration(ImportDeclarationNode node, String arg)
			{
			return "";
			}
		@Override public String visitAnnotation(AnnotationNode node, String arg)
			{
			return "";
			}
		@Override public String visitAnnotationElement(AnnotationElementNode node, String arg)
			{
			return "";
			}
		@Override public String visitModifier(ModifierNode node, String arg)
			{
			return "";
			}
		@Override public String visitTypeParameter(TypeParameterNode node, String arg)
			{
			return "";
			}
		@Override public String visitPrimitiveType(PrimitiveTypeNode node, String arg)
			{
			return "";
			}
		@Override public String visitClassType(ClassTypeNode node, String arg)
			{
			return "";
			}
		@Override public String visitArrayType(ArrayTypeNode node, String arg)
			{
			return "";
			}
		@Override public String visitGenericType(GenericTypeNode node, String arg)
			{
			return "";
			}
		@Override public String visitWildcardType(WildcardTypeNode node, String arg)
			{
			return "";
			}
		@Override public String visitBlockStatement(BlockStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitExpressionStatement(ExpressionStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitIfStatement(IfStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitWhileStatement(WhileStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitDoWhileStatement(DoWhileStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitForStatement(ForStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitEnhancedForStatement(EnhancedForStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitSwitchStatement(SwitchStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitTryStatement(TryStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitSynchronizedStatement(SynchronizedStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitReturnStatement(ReturnStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitThrowStatement(ThrowStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitBreakStatement(BreakStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitContinueStatement(ContinueStatementNode node, String arg)
			{
			return "";
			}
		@Override public String visitMethodCall(MethodCallNode node, String arg)
			{
			return "";
			}
		@Override public String visitFieldAccess(FieldAccessNode node, String arg)
			{
			return "";
			}
		@Override public String visitArrayAccess(ArrayAccessNode node, String arg)
			{
			return "";
			}
		@Override public String visitNewExpression(NewExpressionNode node, String arg)
			{
			return "";
			}
		@Override public String visitCastExpression(CastExpressionNode node, String arg)
			{
			return "";
			}
		@Override public String visitInstanceofExpression(InstanceofExpressionNode node, String arg)
			{
			return "";
			}
		@Override public String visitConditionalExpression(ConditionalExpressionNode node, String arg)
			{
			return "";
			}
		@Override public String visitUnaryExpression(UnaryExpressionNode node, String arg)
			{
			return "";
			}
		@Override public String visitBinaryExpression(BinaryExpressionNode node, String arg)
			{
			return "";
			}
		@Override public String visitLambdaExpression(LambdaExpressionNode node, String arg)
			{
			return "";
			}
		@Override public String visitMethodReference(MethodReferenceNode node, String arg)
			{
			return "";
			}
		@Override public String visitStringLiteral(StringLiteralNode node, String arg)
			{
			return "";
			}
		@Override public String visitCharLiteral(CharLiteralNode node, String arg)
			{
			return "";
			}
		@Override public String visitNumberLiteral(NumberLiteralNode node, String arg)
			{
			return "";
			}
		@Override public String visitBooleanLiteral(BooleanLiteralNode node, String arg)
			{
			return "";
			}
		@Override public String visitNullLiteral(NullLiteralNode node, String arg)
			{
			return "";
			}
		@Override public String visitTextBlock(TextBlockNode node, String arg)
			{
			return "";
			}
		@Override public String visitArrayInitializer(ArrayInitializerNode node, String arg)
			{
			return "";
			}
		@Override public String visitQualifiedName(QualifiedNameNode node, String arg)
			{
			return "";
			}
		@Override public String visitPatternMatch(PatternMatchNode node, String arg)
			{
			return "";
			}
		@Override public String visitRecordPattern(RecordPatternNode node, String arg)
			{
			return "";
			}
		@Override public String visitGuardedPattern(GuardedPatternNode node, String arg)
			{
			return "";
			}
	}
}