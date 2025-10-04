package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.formatter.impl.IndentationCalculator;
import io.github.cowwoc.styler.formatter.impl.IndentationMode;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link IndentationCalculator}.
 */
public final class IndentationCalculatorTest
{
	/**
	 * Verifies that calculateIndentationLevel counts spaces correctly.
	 */
	@Test
	public void calculateIndentationLevelCountsSpaces()
	{
		String line = "    public void method()";
		int level = IndentationCalculator.calculateIndentationLevel(line, 4);

		assertThat(level).isEqualTo(4);
	}

	/**
	 * Verifies that calculateIndentationLevel counts tabs correctly.
	 */
	@Test
	public void calculateIndentationLevelCountsTabs()
	{
		String line = "\t\tpublic void method()";
		int level = IndentationCalculator.calculateIndentationLevel(line, 4);

		assertThat(level).isEqualTo(8);
	}

	/**
	 * Verifies that calculateIndentationLevel counts mixed tabs and spaces.
	 */
	@Test
	public void calculateIndentationLevelCountsMixed()
	{
		String line = "\t  public void method()";
		int level = IndentationCalculator.calculateIndentationLevel(line, 4);

		assertThat(level).isEqualTo(6);
	}

	/**
	 * Verifies that calculateIndentationLevel returns zero for unindented line.
	 */
	@Test
	public void calculateIndentationLevelReturnsZeroForUnindentedLine()
	{
		String line = "public void method()";
		int level = IndentationCalculator.calculateIndentationLevel(line, 4);

		assertThat(level).isEqualTo(0);
	}

	/**
	 * Verifies that calculateIndentationLevel handles empty line.
	 */
	@Test
	public void calculateIndentationLevelHandlesEmptyLine()
	{
		String line = "";
		int level = IndentationCalculator.calculateIndentationLevel(line, 4);

		assertThat(level).isEqualTo(0);
	}

	/**
	 * Verifies that generateIndentation creates correct number of spaces.
	 */
	@Test
	public void generateIndentationCreatesSpaces()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String indentation = calculator.generateIndentation(8);

		assertThat(indentation).isEqualTo("        ");
		assertThat(indentation.length()).isEqualTo(8);
	}

	/**
	 * Verifies that generateIndentation handles zero spaces.
	 */
	@Test
	public void generateIndentationHandlesZeroSpaces()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String indentation = calculator.generateIndentation(0);

		assertThat(indentation).isEmpty();
	}

	/**
	 * Verifies that generateIndentation with SPACES mode uses only spaces.
	 */
	@Test
	public void generateIndentationWithSpacesModeUsesOnlySpaces()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String indentation = calculator.generateIndentation(8, IndentationMode.SPACES, 4);

		assertThat(indentation).isEqualTo("        ");
		assertThat(indentation).doesNotContain("\t");
	}

	/**
	 * Verifies that generateIndentation with TABS mode uses only tabs.
	 */
	@Test
	public void generateIndentationWithTabsModeUsesOnlyTabs()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String indentation = calculator.generateIndentation(8, IndentationMode.TABS, 4);

		assertThat(indentation).isEqualTo("\t\t");
		assertThat(indentation).doesNotContain(" ");
	}

	/**
	 * Verifies that generateIndentation with TABS mode rounds up partial tabs.
	 */
	@Test
	public void generateIndentationWithTabsModeRoundsUpPartialTabs()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String indentation = calculator.generateIndentation(6, IndentationMode.TABS, 4);

		assertThat(indentation).isEqualTo("\t\t");
	}

	/**
	 * Verifies that generateIndentation with MIXED mode uses tabs and spaces.
	 */
	@Test
	public void generateIndentationWithMixedModeUsesTabsAndSpaces()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String indentation = calculator.generateIndentation(10, IndentationMode.MIXED, 4);

		assertThat(indentation).isEqualTo("\t\t  ");
		assertThat(indentation).startsWith("\t\t");
		assertThat(indentation).endsWith("  ");
	}

	/**
	 * Verifies that generateIndentation with MIXED mode uses only tabs for exact multiples.
	 */
	@Test
	public void generateIndentationWithMixedModeUsesOnlyTabsForExactMultiples()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String indentation = calculator.generateIndentation(8, IndentationMode.MIXED, 4);

		assertThat(indentation).isEqualTo("\t\t");
		assertThat(indentation).doesNotContain(" ");
	}

	/**
	 * Verifies that generateContinuationIndentation adds continuation spaces.
	 */
	@Test
	public void generateContinuationIndentationAddsContinuationSpaces()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String indentation = calculator.generateContinuationIndentation(4);

		assertThat(indentation).isEqualTo("        ");
		assertThat(indentation.length()).isEqualTo(8);
	}

	/**
	 * Verifies that extractIndentation returns leading whitespace.
	 */
	@Test
	public void extractIndentationReturnsLeadingWhitespace()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String line = "    public void method()";
		String indentation = calculator.extractIndentation(line);

		assertThat(indentation).isEqualTo("    ");
	}

	/**
	 * Verifies that extractIndentation handles tabs.
	 */
	@Test
	public void extractIndentationHandlesTabs()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String line = "\t\tpublic void method()";
		String indentation = calculator.extractIndentation(line);

		assertThat(indentation).isEqualTo("\t\t");
	}

	/**
	 * Verifies that extractIndentation handles mixed whitespace.
	 */
	@Test
	public void extractIndentationHandlesMixedWhitespace()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String line = "\t  public void method()";
		String indentation = calculator.extractIndentation(line);

		assertThat(indentation).isEqualTo("\t  ");
	}

	/**
	 * Verifies that extractIndentation returns empty for unindented line.
	 */
	@Test
	public void extractIndentationReturnsEmptyForUnindentedLine()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String line = "public void method()";
		String indentation = calculator.extractIndentation(line);

		assertThat(indentation).isEmpty();
	}

	/**
	 * Verifies that extractIndentation handles empty line.
	 */
	@Test
	public void extractIndentationHandlesEmptyLine()
	{
		IndentationCalculator calculator = new IndentationCalculator(4, 4);
		String line = "";
		String indentation = calculator.extractIndentation(line);

		assertThat(indentation).isEmpty();
	}
}
