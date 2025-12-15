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
 * Tests for preservation of string content, comments, and other non-indentation text.
 */
public final class IndentationPreservationTest
{
	private static final String RULE_ID = "indentation";

	/**
	 * Verifies that string literal content is preserved during formatting.
	 */
	@Test
	public void shouldPreserveStringLiteralContent()
	{
		String sourceCode = """
			class Test {
			    String s = "  leading spaces  ";
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(formatted, "formatted").contains("leading spaces");
	}

	/**
	 * Verifies that text block content is preserved during formatting.
	 */
	@Test
	public void shouldPreserveTextBlockContent()
	{
		String sourceCode = """
			class Test {
			    String s = \"""
			        indented
			        content
			        \""";
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(formatted, "formatted").contains("indented");
	}

	/**
	 * Verifies that line comment content is preserved during formatting.
	 */
	@Test
	public void shouldPreserveLineCommentContent()
	{
		String sourceCode = """
			class Test {
			    int x = 1;  //  comment with spaces
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(formatted, "formatted").contains("comment with spaces");
	}

	/**
	 * Verifies that block comment content is preserved during formatting.
	 */
	@Test
	public void shouldPreserveBlockCommentContent()
	{
		String sourceCode = """
			class Test {
			    /* multi
			       line
			       comment */
			    int x = 1;
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(formatted, "formatted").contains("comment");
	}

	/**
	 * Verifies that Javadoc content is preserved during formatting.
	 */
	@Test
	public void shouldPreserveJavadocContent()
	{
		String sourceCode = """
			class Test {
			    /**
			     * This is a Javadoc comment.
			     * It has multiple lines.
			     */
			    void method() {
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(formatted, "formatted").contains("Javadoc");
	}

	/**
	 * Verifies that annotation string values are preserved during formatting.
	 */
	@Test
	public void shouldPreserveAnnotationStringValues()
	{
		String sourceCode = """
			@SuppressWarnings("  unused  ")
			class Test {
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(formatted, "formatted").contains("unused");
	}

	/**
	 * Verifies that escape sequences are preserved during formatting.
	 */
	@Test
	public void shouldPreserveEscapeSequences()
	{
		String sourceCode = """
			class Test {
			    String s = "line1\\nline2\\ttab";
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(formatted, "formatted").contains("\\n").contains("\\t");
	}

	/**
	 * Verifies that comment content without leading spaces is preserved.
	 */
	@Test
	public void shouldPreserveCommentWithoutLeadingSpaces()
	{
		String sourceCode = """
			class Test {
			    // This is a comment
			    int x = 1;
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, List.of(config));

		requireThat(formatted, "formatted").contains("This is a comment");
	}
}
