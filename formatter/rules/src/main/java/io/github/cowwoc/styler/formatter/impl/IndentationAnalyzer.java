package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.node.AnnotationDeclarationNode;
import io.github.cowwoc.styler.ast.node.AssignmentExpressionNode;
import io.github.cowwoc.styler.ast.node.BlockStatementNode;
import io.github.cowwoc.styler.ast.node.ClassDeclarationNode;
import io.github.cowwoc.styler.ast.node.CompactMainMethodNode;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.ast.node.ConstructorDeclarationNode;
import io.github.cowwoc.styler.ast.node.DoWhileStatementNode;
import io.github.cowwoc.styler.ast.node.EnhancedForStatementNode;
import io.github.cowwoc.styler.ast.node.EnumConstantNode;
import io.github.cowwoc.styler.ast.node.EnumDeclarationNode;
import io.github.cowwoc.styler.ast.node.ExpressionNode;
import io.github.cowwoc.styler.ast.node.FieldDeclarationNode;
import io.github.cowwoc.styler.ast.node.FlexibleConstructorBodyNode;
import io.github.cowwoc.styler.ast.node.ForStatementNode;
import io.github.cowwoc.styler.ast.node.IfStatementNode;
import io.github.cowwoc.styler.ast.node.InstanceMainMethodNode;
import io.github.cowwoc.styler.ast.node.InterfaceDeclarationNode;
import io.github.cowwoc.styler.ast.node.IntersectionTypeNode;
import io.github.cowwoc.styler.ast.node.MethodDeclarationNode;
import io.github.cowwoc.styler.ast.node.ModuleDeclarationNode;
import io.github.cowwoc.styler.ast.node.ModuleExportsDirectiveNode;
import io.github.cowwoc.styler.ast.node.ModuleImportDeclarationNode;
import io.github.cowwoc.styler.ast.node.ModuleOpensDirectiveNode;
import io.github.cowwoc.styler.ast.node.ModuleProvidesDirectiveNode;
import io.github.cowwoc.styler.ast.node.ModuleQualifierNode;
import io.github.cowwoc.styler.ast.node.ModuleRequiresDirectiveNode;
import io.github.cowwoc.styler.ast.node.ModuleUsesDirectiveNode;
import io.github.cowwoc.styler.ast.node.PrimitivePatternNode;
import io.github.cowwoc.styler.ast.node.RecordDeclarationNode;
import io.github.cowwoc.styler.ast.node.StringTemplateExpressionNode;
import io.github.cowwoc.styler.ast.node.SwitchExpressionNode;
import io.github.cowwoc.styler.ast.node.SwitchStatementNode;
import io.github.cowwoc.styler.ast.node.SynchronizedStatementNode;
import io.github.cowwoc.styler.ast.node.TemplateProcessorExpressionNode;
import io.github.cowwoc.styler.ast.node.TryStatementNode;
import io.github.cowwoc.styler.ast.node.UnionTypeNode;
import io.github.cowwoc.styler.ast.node.UnnamedClassNode;
import io.github.cowwoc.styler.ast.node.UnnamedVariableNode;
import io.github.cowwoc.styler.ast.node.VarTypeNode;
import io.github.cowwoc.styler.ast.node.WhileStatementNode;
import io.github.cowwoc.styler.ast.node.YieldStatementNode;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import io.github.cowwoc.styler.formatter.api.FormattingContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Analyzes Java source code for indentation violations.
 * <p>
 * This class implements an AST-based analysis strategy to detect indentation issues.
 * It traverses the syntax tree using the visitor pattern, calculating expected
 * indentation for each structural element based on nesting depth and the configured
 * indentation mode.
 * <p>
 * The analyzer produces a list of {@link IndentationViolation} objects that can be
 * used by {@link IndentationCorrector} to generate fixes.
 * <p>
 * This implementation handles standard Java constructs including classes, methods,
 * control flow statements, and blocks. Edge cases like lambdas, array initializers,
 * and text blocks are handled in Phase 4.
 */
public final class IndentationAnalyzer
{
	private final FormattingContext context;
	private final IndentationConfiguration config;
	private final IndentationCalculator calculator;

	/**
	 * Creates a new indentation analyzer.
	 *
	 * @param context the formatting context containing AST and source text, never {@code null}
	 * @param config the indentation configuration to apply, never {@code null}
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	public IndentationAnalyzer(FormattingContext context, IndentationConfiguration config)
	{
		requireThat(context, "context").isNotNull();
		requireThat(config, "config").isNotNull();

		this.context = context;
		this.config = config;
		this.calculator = new IndentationCalculator(config.getTabWidth(), config.getContinuationIndent());
	}

	/**
	 * Analyzes the source code for indentation violations.
	 * <p>
	 * This method traverses the AST to build a map of expected indentation levels,
	 * then compares them against the actual indentation in the source text.
	 *
	 * @return a list of indentation violations, never {@code null} but may be empty
	 */
	public List<IndentationViolation> analyze()
	{
		// Build expected indentation map by visiting AST
		Map<Integer, Integer> expectedIndentation = buildExpectedIndentationMap();

		// Compare with actual indentation and collect violations
		return detectViolations(expectedIndentation);
	}

	/**
	 * Builds a map of line numbers to expected indentation levels.
	 *
	 * @return a map from line number to expected indentation in spaces, never {@code null}
	 */
	private Map<Integer, Integer> buildExpectedIndentationMap()
	{
		Map<Integer, Integer> indentationMap = new HashMap<>();
		DepthTrackingVisitor visitor = new DepthTrackingVisitor(indentationMap, config);
		context.getRootNode().accept(visitor, 0);
		return indentationMap;
	}

	/**
	 * Detects violations by comparing expected vs actual indentation.
	 *
	 * @param expectedIndentation map of line numbers to expected indentation levels
	 * @return list of violations, never {@code null}
	 */
	private List<IndentationViolation> detectViolations(Map<Integer, Integer> expectedIndentation)
	{
		List<IndentationViolation> violations = new ArrayList<>();
		String[] lines = context.getSourceText().split("\n", -1);

		for (Map.Entry<Integer, Integer> entry : expectedIndentation.entrySet())
		{
			int lineNumber = entry.getKey();
			int expected = entry.getValue();

			// Convert to 0-based index for array access
			int lineIndex = lineNumber - 1;
			if (lineIndex < 0 || lineIndex >= lines.length)
			{
				continue;
			}

			String line = lines[lineIndex];
			int actual = calculator.calculateIndentationLevel(line);

			if (expected != actual)
			{
				SourcePosition position = new SourcePosition(lineNumber, 1);
				violations.add(new IndentationViolation(position, expected, actual, line));
			}
		}

		return violations;
	}

	/**
	 * AST visitor that tracks nesting depth and records expected indentation.
	 * <p>
	 * This visitor implements a simplified depth tracking algorithm suitable for
	 * Phase 2. It handles standard Java constructs and calculates indentation
	 * based on structural nesting level.
	 */
	private static final class DepthTrackingVisitor implements ASTVisitor<Void, Integer>
	{
		private final Map<Integer, Integer> indentationMap;
		private final int indentSize;

		/**
		 * Creates a new depth tracking visitor.
		 *
		 * @param indentationMap the map to populate with line numbers and indentation levels
		 * @param config the indentation configuration
		 */
		DepthTrackingVisitor(Map<Integer, Integer> indentationMap, IndentationConfiguration config)
		{
			this.indentationMap = indentationMap;
			this.indentSize = config.getIndentSize();
		}

		/**
		 * Records the expected indentation for a node's start line.
		 *
		 * @param node the AST node to record
		 * @param depth the current nesting depth
		 */
		private void recordIndentation(ASTNode node, int depth)
		{
			int lineNumber = node.getStartPosition().line();
			int indentation = depth * indentSize;
			indentationMap.put(lineNumber, indentation);
		}

		/**
		 * Visits a node that introduces a new nesting level (e.g., class, method, block).
		 * Records the node's indentation and visits children at increased depth.
		 *
		 * @param node the AST node to visit
		 * @param depth the current nesting depth
		 */
		private void visitAndIncrementDepth(ASTNode node, int depth)
		{
			recordIndentation(node, depth);
			node.getChildren().forEach(child -> child.accept(this, depth + 1));
		}

		/**
		 * Visits a node that preserves nesting level (e.g., if statement, loop).
		 * Records the node's indentation and visits children at same depth.
		 *
		 * @param node the AST node to visit
		 * @param depth the current nesting depth
		 */
		private void visitAndPreserveDepth(ASTNode node, int depth)
		{
			recordIndentation(node, depth);
			node.getChildren().forEach(child -> child.accept(this, depth));
		}

		/**
		 * Visits a leaf node that only requires indentation recording (e.g., field, variable).
		 *
		 * @param node the AST node to visit
		 * @param depth the current nesting depth
		 */
		private void visitAndRecordOnly(ASTNode node, int depth)
		{
			recordIndentation(node, depth);
		}

		@Override
		public Void visitCompilationUnit(CompilationUnitNode node, Integer depth)
		{
			// Package and imports are at depth 0, type declarations start at depth 0
			node.getChildren().forEach(child -> child.accept(this, 0));
			return null;
		}

		@Override
		public Void visitClassDeclaration(ClassDeclarationNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitInterfaceDeclaration(InterfaceDeclarationNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitEnumDeclaration(EnumDeclarationNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitRecordDeclaration(RecordDeclarationNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitMethodDeclaration(MethodDeclarationNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitConstructorDeclaration(ConstructorDeclarationNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitFieldDeclaration(FieldDeclarationNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitBlockStatement(BlockStatementNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitIfStatement(IfStatementNode node, Integer depth)
		{
			visitAndPreserveDepth(node, depth);
			return null;
		}

		@Override
		public Void visitForStatement(ForStatementNode node, Integer depth)
		{
			visitAndPreserveDepth(node, depth);
			return null;
		}

		@Override
		public Void visitEnhancedForStatement(EnhancedForStatementNode node, Integer depth)
		{
			visitAndPreserveDepth(node, depth);
			return null;
		}

		@Override
		public Void visitWhileStatement(WhileStatementNode node, Integer depth)
		{
			visitAndPreserveDepth(node, depth);
			return null;
		}

		@Override
		public Void visitDoWhileStatement(DoWhileStatementNode node, Integer depth)
		{
			visitAndPreserveDepth(node, depth);
			return null;
		}

		@Override
		public Void visitSwitchStatement(SwitchStatementNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitTryStatement(TryStatementNode node, Integer depth)
		{
			visitAndPreserveDepth(node, depth);
			return null;
		}

		@Override
		public Void visitSynchronizedStatement(SynchronizedStatementNode node, Integer depth)
		{
			visitAndPreserveDepth(node, depth);
			return null;
		}

		// Default implementation for all other node types: record and traverse children
		// These methods are required by the ASTVisitor interface but don't need special handling
		// for basic indentation analysis in Phase 2

		@Override
		public Void visitVariableDeclaration(io.github.cowwoc.styler.ast.node.VariableDeclarationNode node,
			Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitParameterDeclaration(io.github.cowwoc.styler.ast.node.ParameterDeclarationNode node,
			Integer depth)
		{
			return null; // Parameters don't affect indentation structure
		}

		@Override
		public Void visitExpressionStatement(io.github.cowwoc.styler.ast.node.ExpressionStatementNode node,
			Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitReturnStatement(io.github.cowwoc.styler.ast.node.ReturnStatementNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitBreakStatement(io.github.cowwoc.styler.ast.node.BreakStatementNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitContinueStatement(io.github.cowwoc.styler.ast.node.ContinueStatementNode node,
			Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitThrowStatement(io.github.cowwoc.styler.ast.node.ThrowStatementNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		// Expression and type nodes don't typically start new lines, so we don't record them
		@Override
		public Void visitBinaryExpression(io.github.cowwoc.styler.ast.node.BinaryExpressionNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitUnaryExpression(io.github.cowwoc.styler.ast.node.UnaryExpressionNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitMethodCall(io.github.cowwoc.styler.ast.node.MethodCallNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitFieldAccess(io.github.cowwoc.styler.ast.node.FieldAccessNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitArrayAccess(io.github.cowwoc.styler.ast.node.ArrayAccessNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitLambdaExpression(io.github.cowwoc.styler.ast.node.LambdaExpressionNode node, Integer depth)
		{
			// Phase 4: Lambda handling
			return null;
		}

		@Override
		public Void visitMethodReference(io.github.cowwoc.styler.ast.node.MethodReferenceNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitConditionalExpression(io.github.cowwoc.styler.ast.node.ConditionalExpressionNode node,
			Integer depth)
		{
			return null;
		}

		@Override
		public Void visitCastExpression(io.github.cowwoc.styler.ast.node.CastExpressionNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitInstanceofExpression(io.github.cowwoc.styler.ast.node.InstanceofExpressionNode node,
			Integer depth)
		{
			return null;
		}

		@Override
		public Void visitNewExpression(io.github.cowwoc.styler.ast.node.NewExpressionNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitArrayInitializer(io.github.cowwoc.styler.ast.node.ArrayInitializerNode node, Integer depth)
		{
			// Phase 4: Array alignment
			return null;
		}

		@Override
		public Void visitStringLiteral(io.github.cowwoc.styler.ast.node.StringLiteralNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitNumberLiteral(io.github.cowwoc.styler.ast.node.NumberLiteralNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitBooleanLiteral(io.github.cowwoc.styler.ast.node.BooleanLiteralNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitNullLiteral(io.github.cowwoc.styler.ast.node.NullLiteralNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitCharLiteral(io.github.cowwoc.styler.ast.node.CharLiteralNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitTextBlock(io.github.cowwoc.styler.ast.node.TextBlockNode node, Integer depth)
		{
			// Phase 4: Text block preservation
			return null;
		}

		@Override
		public Void visitPrimitiveType(io.github.cowwoc.styler.ast.node.PrimitiveTypeNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitClassType(io.github.cowwoc.styler.ast.node.ClassTypeNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitArrayType(io.github.cowwoc.styler.ast.node.ArrayTypeNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitGenericType(io.github.cowwoc.styler.ast.node.GenericTypeNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitWildcardType(io.github.cowwoc.styler.ast.node.WildcardTypeNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitTypeParameter(io.github.cowwoc.styler.ast.node.TypeParameterNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitPatternMatch(io.github.cowwoc.styler.ast.node.PatternMatchNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitGuardedPattern(io.github.cowwoc.styler.ast.node.GuardedPatternNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitRecordPattern(io.github.cowwoc.styler.ast.node.RecordPatternNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitIdentifier(io.github.cowwoc.styler.ast.node.IdentifierNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitQualifiedName(io.github.cowwoc.styler.ast.node.QualifiedNameNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitPackageDeclaration(io.github.cowwoc.styler.ast.node.PackageDeclarationNode node,
			Integer depth)
		{
			recordIndentation(node, 0); // Package declarations are always at depth 0
			return null;
		}

		@Override
		public Void visitImportDeclaration(io.github.cowwoc.styler.ast.node.ImportDeclarationNode node, Integer depth)
		{
			recordIndentation(node, 0); // Import declarations are always at depth 0
			return null;
		}

		@Override
		public Void visitAnnotation(io.github.cowwoc.styler.ast.node.AnnotationNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitAnnotationElement(io.github.cowwoc.styler.ast.node.AnnotationElementNode node,
			Integer depth)
		{
			return null;
		}

		@Override
		public Void visitModifier(io.github.cowwoc.styler.ast.node.ModifierNode node, Integer depth)
		{
			return null;
		}

		// Java 21-25 features and module system
		@Override
		public Void visitAnnotationDeclaration(AnnotationDeclarationNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitEnumConstant(EnumConstantNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitSwitchExpression(SwitchExpressionNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitYieldStatement(YieldStatementNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitExpression(ExpressionNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitAssignmentExpression(AssignmentExpressionNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitStringTemplateExpression(StringTemplateExpressionNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitTemplateProcessorExpression(TemplateProcessorExpressionNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitUnionType(UnionTypeNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitIntersectionType(IntersectionTypeNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitVarType(VarTypeNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitUnnamedClass(UnnamedClassNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitUnnamedVariable(UnnamedVariableNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitModuleImportDeclaration(ModuleImportDeclarationNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitFlexibleConstructorBody(FlexibleConstructorBodyNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitPrimitivePattern(PrimitivePatternNode node, Integer depth)
		{
			return null;
		}

		@Override
		public Void visitCompactMainMethod(CompactMainMethodNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitInstanceMainMethod(InstanceMainMethodNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitModuleDeclaration(ModuleDeclarationNode node, Integer depth)
		{
			visitAndIncrementDepth(node, depth);
			return null;
		}

		@Override
		public Void visitModuleRequiresDirective(ModuleRequiresDirectiveNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitModuleExportsDirective(ModuleExportsDirectiveNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitModuleOpensDirective(ModuleOpensDirectiveNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitModuleProvidesDirective(ModuleProvidesDirectiveNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitModuleUsesDirective(ModuleUsesDirectiveNode node, Integer depth)
		{
			visitAndRecordOnly(node, depth);
			return null;
		}

		@Override
		public Void visitModuleQualifier(ModuleQualifierNode node, Integer depth)
		{
			return null;
		}
	}
}
