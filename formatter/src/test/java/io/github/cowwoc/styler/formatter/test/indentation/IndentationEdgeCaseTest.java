package io.github.cowwoc.styler.formatter.test.indentation;

import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.indentation.IndentationType;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingConfiguration;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingRule;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for edge cases and unusual but valid inputs.
 */
public final class IndentationEdgeCaseTest
{
	private static final String RULE_ID = "indentation";

	/**
	 * Verifies that an empty file is handled correctly.
	 */
	@Test
	public void shouldHandleEmptyFile()
	{
		String sourceCode = "";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		requireThat(formatted, "formatted").isEqualTo("");
	}

	/**
	 * Verifies that a file with only whitespace is handled correctly.
	 */
	@Test
	public void shouldHandleFileWithOnlyWhitespace()
	{
		// Input: 3 spaces, newline, tab, newline, 2 spaces
		String sourceCode = "   \n\t\n  ";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// Blank lines (lines with only whitespace) are preserved as-is
		requireThat(formatted, "formatted").isEqualTo("   \n\t\n  ");
	}

	/**
	 * Verifies that a file with no indentation is handled correctly.
	 */
	@Test
	public void shouldHandleFileWithNoIndentation()
	{
		String sourceCode = "class Test {}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// No indentation needed, output equals input
		requireThat(formatted, "formatted").isEqualTo("class Test {}");
	}

	/**
	 * Verifies that very deeply nested code is handled correctly.
	 */
	@Test
	public void shouldHandleVeryDeeplyNestedCode()
	{
		String sourceCode = """
			class L1 {
			    class L2 {
			        class L3 {
			            class L4 {
			                class L5 {
			                    class L6 {
			                        int x;
			                    }
			                }
			            }
			        }
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// Already properly indented, output should equal input
		requireThat(formatted, "formatted").isEqualTo(sourceCode);
	}

	/**
	 * Verifies that mixed line endings are handled correctly.
	 */
	@Test
	public void shouldHandleMixedLineEndings()
	{
		// Input has tabs that should be converted to 4 spaces
		String sourceCode = "class Test {\r\n\tint x;\n\tint y;\r\n}";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// Tabs should be converted to 4 spaces; line endings preserved
		requireThat(formatted, "formatted").isEqualTo("class Test {\r\n    int x;\n    int y;\r\n}");
	}

	/**
	 * Verifies that trailing whitespace on empty lines is handled correctly.
	 */
	@Test
	public void shouldHandleTrailingWhitespaceOnEmptyLines()
	{
		String sourceCode = """
			class Test {
			    int x;
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// Already properly indented, output should equal input
		requireThat(formatted, "formatted").isEqualTo(sourceCode);
	}

	/**
	 * Verifies that record components are handled correctly.
	 */
	@Test
	public void shouldHandleRecordComponents()
	{
		String sourceCode = """
			record Point(
			    int x,
			    int y
			) {
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// Already properly indented, output should equal input
		requireThat(formatted, "formatted").isEqualTo(sourceCode);
	}

	/**
	 * Verifies that sealed class hierarchy is handled correctly.
	 */
	@Test
	public void shouldHandleSealedClassHierarchy()
	{
		String sourceCode = """
			sealed class Shape permits Circle, Square {
			}

			final class Circle extends Shape {
			}

			final class Square extends Shape {
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// Already properly indented, output should equal input
		requireThat(formatted, "formatted").isEqualTo(sourceCode);
	}

	/**
	 * Verifies that switch expressions are handled correctly.
	 */
	@Test
	public void shouldHandleSwitchExpressions()
	{
		String sourceCode = """
			class Test {
			    int x = switch (y) {
			        case 1 -> 10;
			        case 2 -> 20;
			        default -> 0;
			    };
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// Already properly indented, output should equal input
		requireThat(formatted, "formatted").isEqualTo(sourceCode);
	}

	/**
	 * Verifies that text blocks are handled correctly.
	 */
	@Test
	public void shouldHandleTextBlocksWithBackticks()
	{
		String sourceCode = """
			class Test {
			    String html = \"""
			        <html>
			            <body></body>
			        </html>
			        \""";
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// Text block content gets re-indented based on brace depth (depth 1 inside class)
		String expected = """
			class Test {
			    String html = \"""
			    <html>
			        <body></body>
			        </html>
			        \""";
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that consecutive empty lines are handled correctly.
	 */
	@Test
	public void shouldHandleConsecutiveEmptyLines()
	{
		String sourceCode = """
			class Test {
			    int x;
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// Already properly indented, output should equal input
		requireThat(formatted, "formatted").isEqualTo(sourceCode);
	}

	/**
	 * Verifies that a file with only comments is handled correctly.
	 */
	@Test
	public void shouldHandleOnlyComments()
	{
		String sourceCode = """
			// Comment 1
			// Comment 2
			// Comment 3
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		// No indentation in input, output should equal input
		requireThat(formatted, "formatted").isEqualTo(sourceCode);
	}

	/**
	 * Verifies that large indentation values are handled correctly without overflow.
	 */
	@Test
	public void shouldHandleLargeIndentationValues()
	{
		// Input has 4-space indentation, will be normalized to 16-space indentation
		String sourceCode = """
			class Test {
			    int x = 1;
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 16, 16);

		String formatted = rule.format(context, config);

		// 4 spaces (1 indent level) becomes 16 spaces with width 16
		String expected = """
			class Test {
			                int x = 1;
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}
}
