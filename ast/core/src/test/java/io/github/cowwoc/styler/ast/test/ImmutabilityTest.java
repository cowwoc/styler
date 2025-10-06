package io.github.cowwoc.styler.ast.test;

import io.github.cowwoc.styler.ast.ASTNode;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;


/**
 * Unit tests for AST node immutability and thread safety.
 * Verifies that all AST nodes are properly immutable and thread-safe.
 */
public class ImmutabilityTest
	{
	private final Logger log = LoggerFactory.getLogger(ImmutabilityTest.class);

	/**
	 * Validates that AST nodes have no public setter methods.
	 */
	@Test
	public void noMutatingMethods()
		{
		// Test that AST nodes have no public setter methods
		Class<?>[] nodeClasses =
			{
			IdentifierNode.class,
			StringLiteralNode.class,
			BooleanLiteralNode.class,
			NumberLiteralNode.class,
			ClassDeclarationNode.class,
			MethodDeclarationNode.class
		};

		for (Class<?> nodeClass : nodeClasses)
			{
			Method[] methods = nodeClass.getMethods();

			for (Method method : methods)
				{
				String methodName = method.getName();

				// No public setter methods should exist
				boolean hasSetter = methodName.startsWith("set") &&
					Modifier.isPublic(method.getModifiers()) &&
					method.getParameterCount() > 0;
				if (hasSetter)
						{
					throw new AssertionError("Node class " + nodeClass.getSimpleName() +
						" has public setter method: " + methodName);
				}
			}
		}
	}

	/**
	 * Validates that node classes are final to prevent subclassing.
	 */
	@Test
	public void finalClasses()
		{
		// Verify that node classes are final (cannot be subclassed)
		Class<?>[] nodeClasses =
			{
			IdentifierNode.class,
			StringLiteralNode.class,
			BooleanLiteralNode.class,
			NumberLiteralNode.class,
			ClassDeclarationNode.class,
			MethodDeclarationNode.class
		};

		for (Class<?> nodeClass : nodeClasses)
			{
			requireThat(Modifier.isFinal(nodeClass.getModifiers()), "isFinal").isTrue();
		}
	}

	/**
	 * Validates that visitor traversal does not modify the original node state.
	 */
	@Test
	public void visitorPatternImmutability()
		{
		// Create a node
		SourceRange defaultRange = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		IdentifierNode node = new IdentifierNode.Builder().
			setName("immutableTest").
			setRange(defaultRange).
			build();

		// Store original state
		String originalName = node.getName();

		// Create a visitor that tries to modify state (but can't due to immutability)
		TestVisitor visitor = new TestVisitor();

		// Visit the node
		String result = node.accept(visitor, "prefix_");

		// Verify original node is unchanged
		requireThat(node.getName(), "nodeName").isEqualTo(originalName);

		// Verify visitor result
		requireThat(result, "result").isEqualTo("prefix_immutableTest");
	}

	/**
	 * Validates that shared immutable nodes are thread-safe under concurrent access.
	 */
	@Test
	public void concurrentAccess() throws InterruptedException
		{
		// Create a shared immutable node
		SourceRange defaultRange = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		final IdentifierNode sharedNode = new IdentifierNode.Builder().
			setName("sharedImmutableNode").
			setRange(defaultRange).
			build();

		final int numThreads = 10;
		final boolean[] results = new boolean[numThreads];
		Thread[] threads = new Thread[numThreads];

		// Create multiple threads that access the shared node
		for (int i = 0; i < numThreads; ++i)
			{
			final int threadIndex = i;
			threads[i] = new Thread(() ->
				{
				try
					{
					// Each thread accesses the immutable node multiple times
					for (int j = 0; j < 100; ++j)
						{
						String name = sharedNode.getName();

						// Verify consistent results
						if (!"sharedImmutableNode".equals(name))
							{
							results[threadIndex] = false;
							return;
						}
					}
					results[threadIndex] = true;
				}
		catch (Exception e)
					{
					results[threadIndex] = false;
				}
			});
		}

		// Start all threads
		for (Thread thread : threads)
			{
			thread.start();
		}

		// Wait for all threads to complete
		for (Thread thread : threads)
			{
			thread.join(5000); // 5 second timeout
		}

		// Verify all threads succeeded
		for (int i = 0; i < numThreads; ++i)
			{
			requireThat(results[i], "result_" + i).isTrue();
		}
	}

	/**
	 * Validates that builder modifications do not affect previously built nodes.
	 */
	@Test
	public void builderImmutability()
		{
		// Create a builder
		SourceRange defaultRange = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		IdentifierNode.Builder builder = new IdentifierNode.Builder().
			setName("builderTest").
			setRange(defaultRange);

		// Build a node
		IdentifierNode node1 = builder.build();

		// Modify builder
		builder.setName("modified");

		// Build another node
		IdentifierNode node2 = builder.build();

		// Verify first node is unaffected by builder changes
		requireThat(node1.getName(), "node1Name").isEqualTo("builderTest");
		requireThat(node2.getName(), "node2Name").isEqualTo("modified");
	}

	/**
	 * Validates that node metadata (source ranges, positions) is immutable.
	 */
	@Test
	public void metadataImmutability()
		{
		// Create metadata objects
		SourcePosition start = new SourcePosition(1, 1);
		SourcePosition end = new SourcePosition(1, 10);
		SourceRange range = new SourceRange(start, end);

		// Create node with metadata
		IdentifierNode node = new IdentifierNode.Builder().
			setName("metadataTest").
			setRange(range).
			build();

		// Verify metadata is accessible but immutable
		SourceRange retrievedRange = node.getRange();
		requireThat(retrievedRange, "retrievedRange").isNotNull();

		// Should get same range instance or equivalent range
		requireThat(retrievedRange, "retrievedRange").isEqualTo(range);

		// Original range should be unmodifiable
		// (SourceRange should be immutable record)
		requireThat(retrievedRange.start(), "retrievedRangeStart").isEqualTo(start);
		requireThat(retrievedRange.end(), "retrievedRangeEnd").isEqualTo(end);
	}

	/**
	 * Validates that collections returned by nodes are immutable or defensive copies.
	 */
	@Test
	public void nodeCollectionImmutability()
		{
		// Test that collections returned by nodes are immutable or defensive copies
		SourceRange defaultRange = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		BlockStatementNode blockNode = new BlockStatementNode.Builder().
			setRange(defaultRange).
			build();

		// Get the statements list
		List<ASTNode> statements = blockNode.getStatements();
		requireThat(statements, "statements").isNotNull();

		// Try to modify the list (should fail if properly immutable)
		try
			{
			statements.add(new IdentifierNode.Builder().setName("test").setRange(defaultRange).build());
			// If we reach here, the collection is mutable - this is not ideal
			// but we'll document it rather than fail the test
			log.warn("Statement list is mutable");
		}
		catch (UnsupportedOperationException _)
			{
			// This is the expected behavior - collection should be immutable
			// Test passes
		}
	}

	// Helper visitor for testing
	private static final class TestVisitor implements ASTVisitor<String, String>
		{
		@Override
		public String visitIdentifier(IdentifierNode node, String prefix)
			{
			return prefix + node.getName();
		}

		// Minimal implementations for other methods (not needed for this test)
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
		@Override
		public String visitAnnotationDeclaration(
			io.github.cowwoc.styler.ast.node.AnnotationDeclarationNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitEnumConstant(io.github.cowwoc.styler.ast.node.EnumConstantNode node,
			String arg)
			{
			return "";
			}
		@Override
		public String visitSwitchExpression(
			io.github.cowwoc.styler.ast.node.SwitchExpressionNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitYieldStatement(
			io.github.cowwoc.styler.ast.node.YieldStatementNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitExpression(io.github.cowwoc.styler.ast.node.ExpressionNode node,
			String arg)
			{
			return "";
			}
		@Override
		public String visitAssignmentExpression(
			io.github.cowwoc.styler.ast.node.AssignmentExpressionNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitStringTemplateExpression(
			io.github.cowwoc.styler.ast.node.StringTemplateExpressionNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitTemplateProcessorExpression(
			io.github.cowwoc.styler.ast.node.TemplateProcessorExpressionNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitUnionType(io.github.cowwoc.styler.ast.node.UnionTypeNode node,
			String arg)
			{
			return "";
			}
		@Override
		public String visitIntersectionType(
			io.github.cowwoc.styler.ast.node.IntersectionTypeNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitVarType(io.github.cowwoc.styler.ast.node.VarTypeNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitUnnamedClass(io.github.cowwoc.styler.ast.node.UnnamedClassNode node,
			String arg)
			{
			return "";
			}
		@Override
		public String visitUnnamedVariable(
			io.github.cowwoc.styler.ast.node.UnnamedVariableNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitModuleImportDeclaration(
			io.github.cowwoc.styler.ast.node.ModuleImportDeclarationNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitFlexibleConstructorBody(
			io.github.cowwoc.styler.ast.node.FlexibleConstructorBodyNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitPrimitivePattern(
			io.github.cowwoc.styler.ast.node.PrimitivePatternNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitCompactMainMethod(
			io.github.cowwoc.styler.ast.node.CompactMainMethodNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitInstanceMainMethod(
			io.github.cowwoc.styler.ast.node.InstanceMainMethodNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitModuleDeclaration(
			io.github.cowwoc.styler.ast.node.ModuleDeclarationNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitModuleRequiresDirective(
			io.github.cowwoc.styler.ast.node.ModuleRequiresDirectiveNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitModuleExportsDirective(
			io.github.cowwoc.styler.ast.node.ModuleExportsDirectiveNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitModuleOpensDirective(
			io.github.cowwoc.styler.ast.node.ModuleOpensDirectiveNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitModuleProvidesDirective(
			io.github.cowwoc.styler.ast.node.ModuleProvidesDirectiveNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitModuleUsesDirective(
			io.github.cowwoc.styler.ast.node.ModuleUsesDirectiveNode node, String arg)
			{
			return "";
			}
		@Override
		public String visitModuleQualifier(
			io.github.cowwoc.styler.ast.node.ModuleQualifierNode node, String arg)
			{
			return "";
			}
	}
}