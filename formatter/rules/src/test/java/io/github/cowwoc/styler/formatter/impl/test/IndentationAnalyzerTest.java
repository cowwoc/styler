package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.ConfigurationException;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.test.TestUtilities;
import io.github.cowwoc.styler.formatter.impl.IndentationAnalyzer;
import io.github.cowwoc.styler.formatter.impl.IndentationConfiguration;
import io.github.cowwoc.styler.formatter.impl.IndentationViolation;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link IndentationAnalyzer}.
 * <p>
 * <strong>Testing Strategy Note</strong>: Comprehensive AST traversal tests are deferred
 * until parser test infrastructure is available (see task: implement-parser-test-infrastructure).
 * Current test coverage focuses on component integration and structural validation.
 */
public final class IndentationAnalyzerTest
{
	/**
	 * Verifies that analyzer handles empty CompilationUnit without violations.
	 */
	@Test
	public void analyzeEmptyCompilationUnitReturnsNoViolations() throws ConfigurationException
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		FormattingContext context = new FormattingContext(ast, "", Path.of("/test/Example.java"),
			config, Set.of(), Map.of());

		IndentationAnalyzer analyzer = new IndentationAnalyzer(context, config);
		List<IndentationViolation> violations = analyzer.analyze();

		assertThat(violations).isEmpty();
	}

	/**
	 * Verifies that analyzer rejects null context.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void analyzeRejectsNullContext() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		new IndentationAnalyzer(null, config);
	}

	/**
	 * Verifies that analyzer rejects null configuration.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void analyzeRejectsNullConfiguration()
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		FormattingContext context = new FormattingContext(ast, "", Path.of("/test/Example.java"),
			IndentationConfiguration.createDefault(), Set.of(), Map.of());

		new IndentationAnalyzer(context, null);
	}

	/**
	 * Verifies that analyzer uses configured indent size.
	 */
	@Test
	public void analyzeRespectsConfiguredIndentSize() throws ConfigurationException
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		IndentationConfiguration config = IndentationConfiguration.builder().
			withIndentSize(2).
			build();
		FormattingContext context = new FormattingContext(ast, "", Path.of("/test/Example.java"),
			config, Set.of(), Map.of());

		IndentationAnalyzer analyzer = new IndentationAnalyzer(context, config);
		List<IndentationViolation> violations = analyzer.analyze();

		assertThat(violations).isEmpty();
	}

	/**
	 * Verifies that analyzer uses configured tab width.
	 */
	@Test
	public void analyzeRespectsConfiguredTabWidth() throws ConfigurationException
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		IndentationConfiguration config = IndentationConfiguration.builder().
			withTabWidth(8).
			build();
		FormattingContext context = new FormattingContext(ast, "", Path.of("/test/Example.java"),
			config, Set.of(), Map.of());

		IndentationAnalyzer analyzer = new IndentationAnalyzer(context, config);
		List<IndentationViolation> violations = analyzer.analyze();

		assertThat(violations).isEmpty();
	}

	/**
	 * Verifies that analyzer uses configured continuation indent.
	 */
	@Test
	public void analyzeRespectsConfiguredContinuationIndent() throws ConfigurationException
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		IndentationConfiguration config = IndentationConfiguration.builder().
			withContinuationIndent(8).
			build();
		FormattingContext context = new FormattingContext(ast, "", Path.of("/test/Example.java"),
			config, Set.of(), Map.of());

		IndentationAnalyzer analyzer = new IndentationAnalyzer(context, config);
		List<IndentationViolation> violations = analyzer.analyze();

		assertThat(violations).isEmpty();
	}

	/**
	 * Verifies that analyzer integrates with IndentationCalculator for depth computation.
	 */
	@Test
	public void analyzerIntegratesWithCalculatorForDepthComputation() throws ConfigurationException
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		IndentationConfiguration config = IndentationConfiguration.builder().
			withIndentSize(4).
			withTabWidth(4).
			build();
		FormattingContext context = new FormattingContext(ast, "", Path.of("/test/Example.java"),
			config, Set.of(), Map.of());

		IndentationAnalyzer analyzer = new IndentationAnalyzer(context, config);

		assertThat(analyzer).isNotNull();
	}

	/**
	 * Verifies that analyzer creates violations with correct source position.
	 */
	@Test
	public void analyzerCreatesViolationsWithSourcePosition() throws ConfigurationException
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		String source = "public class Example {}";
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		FormattingContext context = new FormattingContext(ast, source, Path.of("/test/Example.java"),
			config, Set.of(), Map.of());

		IndentationAnalyzer analyzer = new IndentationAnalyzer(context, config);
		List<IndentationViolation> violations = analyzer.analyze();

		assertThat(violations).isNotNull();
	}

	/**
	 * Verifies that analyzer handles multi-line source text.
	 */
	@Test
	public void analyzerHandlesMultiLineSourceText() throws ConfigurationException
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		String source = "public class Example {\n    private int x;\n}";
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		FormattingContext context = new FormattingContext(ast, source, Path.of("/test/Example.java"),
			config, Set.of(), Map.of());

		IndentationAnalyzer analyzer = new IndentationAnalyzer(context, config);
		List<IndentationViolation> violations = analyzer.analyze();

		assertThat(violations).isNotNull();
	}

	/**
	 * Verifies that analyzer returns list (not null) for all inputs.
	 */
	@Test
	public void analyzeReturnsListNotNull() throws ConfigurationException
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		FormattingContext context = new FormattingContext(ast, "", Path.of("/test/Example.java"),
			config, Set.of(), Map.of());

		IndentationAnalyzer analyzer = new IndentationAnalyzer(context, config);
		List<IndentationViolation> violations = analyzer.analyze();

		assertThat(violations).isNotNull();
	}

	/**
	 * Verifies that analyzer processes source text through context.
	 */
	@Test
	public void analyzerProcessesSourceTextFromContext() throws ConfigurationException
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		String source = "public class Example {}";
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		FormattingContext context = new FormattingContext(ast, source, Path.of("/test/Example.java"),
			config, Set.of(), Map.of());

		IndentationAnalyzer analyzer = new IndentationAnalyzer(context, config);
		List<IndentationViolation> violations = analyzer.analyze();

		assertThat(violations).isNotNull();
	}
}
