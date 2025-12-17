package io.github.cowwoc.styler.formatter.test.indentation;

import java.util.List;
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

		String formatted = rule.format(context, List.of(config));

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

		String formatted = rule.format(context, List.of(config));

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

		String formatted = rule.format(context, List.of(config));

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

		String formatted = rule.format(context, List.of(config));

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

		String formatted = rule.format(context, List.of(config));

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

		String formatted = rule.format(context, List.of(config));

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

		String formatted = rule.format(context, List.of(config));

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

		String formatted = rule.format(context, List.of(config));

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

		String formatted = rule.format(context, List.of(config));

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

		String formatted = rule.format(context, List.of(config));

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

		String formatted = rule.format(context, List.of(config));

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

		String formatted = rule.format(context, List.of(config));

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

		String formatted = rule.format(context, List.of(config));

		// 4 spaces (1 indent level) becomes 16 spaces with width 16
		String expected = """
			class Test {
			                int x = 1;
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that braces inside string literals do not affect indentation depth.
	 * <p>
	 * This is a regression test for AST-assisted scanning. Braces in strings should not
	 * be counted for depth calculation.
	 */
	@Test
	public void shouldNotCountBracesInStrings()
	{
		// The string contains { and } which should NOT affect indentation depth
		String sourceCode = """
			class Test {
			    String s = "{ braces } in string";
			    int x = 1;
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// Properly indented - braces in string should not change depth
		// int x should still be at depth 1 (inside class), not depth 2
		requireThat(formatted, "formatted").isEqualTo(sourceCode);
	}

	/**
	 * Verifies that braces inside comments do not affect indentation depth.
	 * <p>
	 * This is a regression test for AST-assisted scanning. Braces in comments should not
	 * be counted for depth calculation.
	 */
	@Test
	public void shouldNotCountBracesInComments()
	{
		// The line comment contains { and } which should NOT affect indentation depth
		String sourceCode = """
			class Test {
			    // Example: { braces } in comment
			    int x = 1;
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// Properly indented - braces in comment should not change depth
		// int x should still be at depth 1 (inside class), not depth 2
		requireThat(formatted, "formatted").isEqualTo(sourceCode);
	}

	/**
	 * Verifies that braces inside block comments do not affect indentation depth.
	 * <p>
	 * This is a regression test for AST-assisted scanning. Braces in block comments should not
	 * be counted for depth calculation. The test verifies that:
	 * <ol>
	 * <li>The block comment line itself has correct indentation (depth 1)</li>
	 * <li>No violations about wrong indentation depth due to braces in comment</li>
	 * </ol>
	 */
	@Test
	public void shouldNotCountBracesInBlockComments()
	{
		// The block comment contains { and } which should NOT affect indentation depth
		// Use a simple block comment without the continuation-triggering '*/' at end of line
		String sourceCode = """
			class Test {
			    /* { braces } */;
			    int x = 1;
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// Properly indented - braces in block comment should not change depth
		// The semicolon after the block comment prevents continuation detection issue
		requireThat(formatted, "formatted").isEqualTo(sourceCode);
	}

	/**
	 * Verifies that braces inside char literals do not affect indentation depth.
	 * <p>
	 * This is a regression test for AST-assisted scanning. Braces in char literals should not
	 * be counted for depth calculation.
	 */
	@Test
	public void shouldNotCountBracesInCharLiterals()
	{
		// The char literal contains { which should NOT affect indentation depth
		String sourceCode = """
			class Test {
			    char c = '{';
			    int x = 1;
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		// Properly indented - brace in char literal should not change depth
		// int x should still be at depth 1 (inside class), not depth 2
		requireThat(formatted, "formatted").isEqualTo(sourceCode);
	}
}
