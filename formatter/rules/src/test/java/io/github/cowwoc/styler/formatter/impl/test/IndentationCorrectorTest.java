package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.formatter.api.ConfigurationException;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.formatter.impl.IndentationConfiguration;
import io.github.cowwoc.styler.formatter.impl.IndentationCorrector;
import io.github.cowwoc.styler.formatter.impl.IndentationMode;
import io.github.cowwoc.styler.formatter.impl.IndentationViolation;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link IndentationCorrector}.
 */
public final class IndentationCorrectorTest
{
	/**
	 * Verifies that corrector generates TextEdit for violation with spaces mode.
	 */
	@Test
	public void correctGeneratesTextEditForSpacesMode() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		IndentationCorrector corrector = new IndentationCorrector(config);

		SourcePosition position = new SourcePosition(1, 1);
		IndentationViolation violation = new IndentationViolation(position, 4, 0,
			"public void method() {}");

		List<TextEdit> edits = corrector.correct(List.of(violation));

		assertThat(edits).hasSize(1);
		assertThat(edits.get(0).getReplacement()).isEqualTo("    ");
	}

	/**
	 * Verifies that corrector generates TextEdit for violation with tabs mode.
	 */
	@Test
	public void correctGeneratesTextEditForTabsMode() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.builder().
			withMode(IndentationMode.TABS).
			withTabWidth(4).
			build();
		IndentationCorrector corrector = new IndentationCorrector(config);

		SourcePosition position = new SourcePosition(1, 1);
		IndentationViolation violation = new IndentationViolation(position, 4, 0,
			"public void method() {}");

		List<TextEdit> edits = corrector.correct(List.of(violation));

		assertThat(edits).hasSize(1);
		assertThat(edits.get(0).getReplacement()).isEqualTo("\t");
	}

	/**
	 * Verifies that corrector handles removal of existing indentation.
	 */
	@Test
	public void correctHandlesRemovalOfExistingIndentation() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		IndentationCorrector corrector = new IndentationCorrector(config);

		SourcePosition position = new SourcePosition(1, 1);
		IndentationViolation violation = new IndentationViolation(position, 0, 4,
			"    public void method() {}");

		List<TextEdit> edits = corrector.correct(List.of(violation));

		assertThat(edits).hasSize(1);
		assertThat(edits.get(0).getReplacement()).isEmpty();
	}

	/**
	 * Verifies that corrector handles replacement of incorrect indentation.
	 */
	@Test
	public void correctHandlesReplacementOfIncorrectIndentation() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		IndentationCorrector corrector = new IndentationCorrector(config);

		SourcePosition position = new SourcePosition(1, 1);
		IndentationViolation violation = new IndentationViolation(position, 4, 2,
			"  public void method() {}");

		List<TextEdit> edits = corrector.correct(List.of(violation));

		assertThat(edits).hasSize(1);
		assertThat(edits.get(0).getReplacement()).isEqualTo("    ");
	}

	/**
	 * Verifies that TextEdit contains correct range for line start.
	 */
	@Test
	public void textEditContainsCorrectRangeForLineStart() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		IndentationCorrector corrector = new IndentationCorrector(config);

		SourcePosition position = new SourcePosition(1, 1);
		IndentationViolation violation = new IndentationViolation(position, 0, 4,
			"    public void method() {}");

		List<TextEdit> edits = corrector.correct(List.of(violation));

		assertThat(edits).hasSize(1);
		assertThat(edits.get(0).getRange().start().line()).isEqualTo(1);
		assertThat(edits.get(0).getRange().start().column()).isEqualTo(1);
	}

	/**
	 * Verifies that TextEdit contains correct range end character position.
	 */
	@Test
	public void textEditContainsCorrectRangeEndCharacter() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		IndentationCorrector corrector = new IndentationCorrector(config);

		SourcePosition position = new SourcePosition(1, 1);
		IndentationViolation violation = new IndentationViolation(position, 0, 4,
			"    public void method() {}");

		List<TextEdit> edits = corrector.correct(List.of(violation));

		assertThat(edits).hasSize(1);
		assertThat(edits.get(0).getRange().end().line()).isEqualTo(1);
		assertThat(edits.get(0).getRange().end().column()).isEqualTo(5);
	}

	/**
	 * Verifies that corrector handles multi-line source with correct line indexing.
	 */
	@Test
	public void correctHandlesMultiLineSourceWithCorrectLineIndexing() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		IndentationCorrector corrector = new IndentationCorrector(config);

		SourcePosition position = new SourcePosition(2, 1);
		IndentationViolation violation = new IndentationViolation(position, 4, 2,
			"  public void method() {}");

		List<TextEdit> edits = corrector.correct(List.of(violation));

		assertThat(edits).hasSize(1);
		assertThat(edits.get(0).getRange().start().line()).isEqualTo(2);
		assertThat(edits.get(0).getReplacement()).isEqualTo("    ");
	}

	/**
	 * Verifies that TextEdit contains correct rule identifier.
	 */
	@Test
	public void textEditContainsCorrectRuleId() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		IndentationCorrector corrector = new IndentationCorrector(config);

		SourcePosition position = new SourcePosition(1, 1);
		IndentationViolation violation = new IndentationViolation(position, 4, 0,
			"public void method() {}");

		List<TextEdit> edits = corrector.correct(List.of(violation));

		assertThat(edits).hasSize(1);
		assertThat(edits.get(0).getRuleId()).isEqualTo("io.github.cowwoc.styler.rules.Indentation");
	}
}
