package io.github.cowwoc.styler.parser.converter;

import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.strategies.AnnotationElementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.AnnotationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ArrayAccessStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ArrayInitializerStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ArrayTypeStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.BinaryExpressionStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.BlockStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.BreakStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.CastExpressionStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ClassDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ClassTypeStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.CompilationUnitStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ConditionalExpressionStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ConstructorDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ContinueStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.DoWhileStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.EnhancedForStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.EnumDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ExpressionStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.FieldAccessStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.FieldDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ForStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.GenericTypeStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.GuardedPatternStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.IdentifierStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.IfStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ImportDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.InstanceofExpressionStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.InterfaceDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.LambdaExpressionStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.LiteralStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.MethodCallStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.MethodDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.MethodReferenceStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ModifierStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.NewExpressionStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.PackageDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ParameterDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.PatternMatchStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.PrimitiveTypeStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.RecordDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.RecordPatternStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ReturnStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.SwitchStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.SynchronizedStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ThrowStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.TryStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.UnaryExpressionStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.VariableDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.WhileStatementStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.WildcardTypeStrategy;
// New Java 21-25 feature strategies
import io.github.cowwoc.styler.parser.converter.strategies.AnnotationDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.AssignmentExpressionStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.CompactMainMethodStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.EnumConstantStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ExpressionStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.FlexibleConstructorBodyStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.InstanceMainMethodStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.IntersectionTypeStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ModuleDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ModuleExportsDirectiveStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ModuleImportDeclarationStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ModuleOpensDirectiveStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ModuleProvidesDirectiveStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ModuleQualifierStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ModuleRequiresDirectiveStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.ModuleUsesDirectiveStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.PrimitivePatternStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.StringTemplateExpressionStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.SwitchExpressionStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.TemplateProcessorExpressionStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.UnionTypeStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.UnnamedClassStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.UnnamedVariableStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.VarTypeStrategy;
import io.github.cowwoc.styler.parser.converter.strategies.YieldStatementStrategy;

/**
 * Factory for creating a StrategyRegistry with all node types registered.
 * <p>
 * This class provides a complete registry covering every structural node type defined in
 * {@link NodeType}.
 * </p>
 *
 * @since 1.0
 */
public final class DefaultStrategyRegistry
{
	/**
	 * Creates a StrategyRegistry with all structural node types registered.
	 *
	 * @return a complete {@link StrategyRegistry}
	 */
	@SuppressWarnings("PMD.NcssCount")
	public static StrategyRegistry create()
	{
		StrategyRegistry.Builder builder = StrategyRegistry.builder();

		// Top-level nodes
		builder.register(new CompilationUnitStrategy());
		builder.register(new PackageDeclarationStrategy());
		builder.register(new ImportDeclarationStrategy());

		// Type declarations
		builder.register(new ClassDeclarationStrategy());
		builder.register(new InterfaceDeclarationStrategy());
		builder.register(new EnumDeclarationStrategy());
		builder.register(new RecordDeclarationStrategy());

		// Members
		builder.register(new MethodDeclarationStrategy());
		builder.register(new ConstructorDeclarationStrategy());
		builder.register(new FieldDeclarationStrategy());
		builder.register(new ParameterDeclarationStrategy());
		builder.register(new VariableDeclarationStrategy());

		// Statements
		builder.register(new BlockStatementStrategy());
		builder.register(new ExpressionStatementStrategy());
		builder.register(new IfStatementStrategy());
		builder.register(new WhileStatementStrategy());
		builder.register(new DoWhileStatementStrategy());
		builder.register(new ForStatementStrategy());
		builder.register(new EnhancedForStatementStrategy());
		builder.register(new SwitchStatementStrategy());
		builder.register(new TryStatementStrategy());
		builder.register(new ReturnStatementStrategy());
		builder.register(new ThrowStatementStrategy());
		builder.register(new BreakStatementStrategy());
		builder.register(new ContinueStatementStrategy());
		builder.register(new SynchronizedStatementStrategy());

		// Expressions
		builder.register(new LiteralStrategy(NodeType.LITERAL_EXPRESSION));
		builder.register(new IdentifierStrategy(NodeType.IDENTIFIER_EXPRESSION));
		builder.register(new MethodCallStrategy());
		builder.register(new FieldAccessStrategy());
		builder.register(new ArrayAccessStrategy());
		builder.register(new BinaryExpressionStrategy());
		builder.register(new UnaryExpressionStrategy());
		builder.register(new ConditionalExpressionStrategy());
		builder.register(new InstanceofExpressionStrategy());
		builder.register(new CastExpressionStrategy());
		builder.register(new LambdaExpressionStrategy());
		builder.register(new MethodReferenceStrategy());
		builder.register(new NewExpressionStrategy());
		builder.register(new ArrayInitializerStrategy());

		// Pattern matching
		builder.register(new PatternMatchStrategy());
		builder.register(new GuardedPatternStrategy());
		builder.register(new RecordPatternStrategy());

		// Types
		builder.register(new PrimitiveTypeStrategy());
		builder.register(new ClassTypeStrategy());
		builder.register(new ArrayTypeStrategy());
		builder.register(new GenericTypeStrategy());
		builder.register(new WildcardTypeStrategy());
		// Note: TypeParameterStrategy not registered - conflicts with GenericTypeStrategy on PARAMETERIZED_TYPE

		// Modifiers and annotations
		builder.register(new ModifierStrategy());
		builder.register(new AnnotationStrategy());
		builder.register(new AnnotationElementStrategy());

		// Literals - using LiteralStrategy for LITERAL_EXPRESSION
		// Specific literal strategies conflict with generic LiteralStrategy
		// builder.register(new BooleanLiteralStrategy());
		// builder.register(new CharLiteralStrategy());
		// builder.register(new NullLiteralStrategy());
		// builder.register(new NumberLiteralStrategy());
		// 		builder.register(new TextBlockStrategy());

		// Other
		// 		builder.register(new QualifiedNameStrategy());

		// Java 21-25 features
		builder.register(new AnnotationDeclarationStrategy());
		builder.register(new EnumConstantStrategy());
		builder.register(new SwitchExpressionStrategy());
		builder.register(new YieldStatementStrategy());
		builder.register(new ExpressionStrategy());
		builder.register(new AssignmentExpressionStrategy());
		builder.register(new StringTemplateExpressionStrategy());
		builder.register(new TemplateProcessorExpressionStrategy());
		builder.register(new UnionTypeStrategy());
		builder.register(new IntersectionTypeStrategy());
		builder.register(new VarTypeStrategy());
		builder.register(new UnnamedClassStrategy());
		builder.register(new UnnamedVariableStrategy());
		builder.register(new ModuleImportDeclarationStrategy());
		builder.register(new FlexibleConstructorBodyStrategy());
		builder.register(new PrimitivePatternStrategy());
		builder.register(new CompactMainMethodStrategy());
		builder.register(new InstanceMainMethodStrategy());
		builder.register(new ModuleDeclarationStrategy());
		builder.register(new ModuleRequiresDirectiveStrategy());
		builder.register(new ModuleExportsDirectiveStrategy());
		builder.register(new ModuleOpensDirectiveStrategy());
		builder.register(new ModuleProvidesDirectiveStrategy());
		builder.register(new ModuleUsesDirectiveStrategy());
		builder.register(new ModuleQualifierStrategy());

		return builder.build();
	}

	// Private constructor - static factory only
	private DefaultStrategyRegistry()
	{
	}
}
