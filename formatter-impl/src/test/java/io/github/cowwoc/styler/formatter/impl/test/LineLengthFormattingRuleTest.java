package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ValidationResult;
import io.github.cowwoc.styler.formatter.impl.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.impl.LineLengthFormattingRule;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link LineLengthFormattingRule}.
 */
public class LineLengthFormattingRuleTest
{
	/**
	 * Verifies the rule identifier matches the expected LineLength rule ID.
	 */
	@Test
	public void getRuleIdReturnsCorrectId()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();

		assertThat(rule.getRuleId()).isEqualTo("io.github.cowwoc.styler.rules.LineLength");
	}

	/**
	 * Verifies the rule uses the standard priority of 100 for general formatting rules.
	 */
	@Test
	public void getPriorityReturnsDefaultPriority()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();

		assertThat(rule.getPriority()).isEqualTo(100);
	}

	/**
	 * Verifies the default configuration has a maximum line length of 120 characters.
	 */
	@Test
	public void getDefaultConfigurationReturnsValidConfiguration()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		RuleConfiguration config = rule.getDefaultConfiguration();

		assertThat(config).isNotNull();
		assertThat(config).isInstanceOf(LineLengthConfiguration.class);
		LineLengthConfiguration lineConfig = (LineLengthConfiguration) config;
		assertThat(lineConfig.getMaxLineLength()).isEqualTo(120);
	}

	/**
	 * Verifies the validate() method returns a failure when the context parameter is null.
	 */
	@Test
	public void validateWithNullContextReturnsFailure()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();

		ValidationResult result = rule.validate(null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.getFirstErrorMessage()).contains("cannot be null");
	}

	/**
	 * Verifies the validate() method returns success when given a properly constructed context.
	 */
	@Test
	public void validateWithValidContextReturnsSuccess()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		FormattingContext context = createMockContext("test");

		ValidationResult result = rule.validate(context);

		assertThat(result.isValid()).isTrue();
	}

	/**
	 * Verifies the apply() method produces no text edits when all source lines are within
	 * the configured maximum line length.
	 */
	@Test
	public void applyWithShortLinesReturnsNoEdits() throws Exception
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();
		String source = "public class Test\n{\n}\n";
		FormattingContext context = createMockContext(source);

		FormattingResult result = rule.apply(context);

		assertThat(result.getEdits()).isEmpty();
	}

	/**
	 * Verifies the apply() method returns an empty result when the context parameter is null,
	 * avoiding exceptions during error conditions.
	 */
	@Test
	public void applyWithInvalidContextReturnsEmptyResult()
	{
		LineLengthFormattingRule rule = new LineLengthFormattingRule();

		FormattingResult result = rule.apply(null);

		assertThat(result.getEdits()).isEmpty();
	}

	private FormattingContext createMockContext(String sourceText)
	{
		CompilationUnitNode mockRoot = new CompilationUnitNode.Builder().
			setRange(new SourceRange(new SourcePosition(1, 1),
				new SourcePosition(1, sourceText.length() + 1))).build();

		LineLengthConfiguration config = LineLengthConfiguration.createDefault();

		return new FormattingContext(mockRoot, sourceText, Path.of("/tmp/Test.java"),
			config, Set.of("io.github.cowwoc.styler.rules.LineLength"), Map.of());
	}
}
