package io.github.cowwoc.styler.formatter.test.whitespace;

import java.util.List;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingRule;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for comma, semicolon, colon, and parentheses spacing rules.
 */
public class WhitespacePunctuationTest
{
	/**
	 * Tests that space is added after comma.
	 */
	@Test
	public void shouldAddSpaceAfterComma()
	{
		String source = "method(a,b,c)";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a, b, c");
	}

	/**
	 * Tests that space is removed before comma.
	 */
	@Test
	public void shouldRemoveSpaceBeforeComma()
	{
		String source = "method(a , b , c)";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a, b, c");
		requireThat(result, "result").doesNotContain("a ,");
	}

	/**
	 * Tests that space is removed before semicolon.
	 */
	@Test
	public void shouldRemoveSpaceBeforeSemicolon()
	{
		String source = "int x = 1 ;";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").isEqualTo("int x = 1;");
	}

	/**
	 * Tests that space is removed after opening parenthesis.
	 */
	@Test
	public void shouldRemoveSpaceAfterOpeningParen()
	{
		String source = "method( x )";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("method(x)");
	}

	/**
	 * Tests that space is removed before closing parenthesis.
	 */
	@Test
	public void shouldRemoveSpaceBeforeClosingParen()
	{
		String source = "method( x )";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("method(x)");
	}

	/**
	 * Tests that space is removed before method opening parenthesis.
	 */
	@Test
	public void shouldRemoveSpaceBeforeMethodParen()
	{
		String source = "method ()";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("method()");
	}

	/**
	 * Tests that space is removed after opening bracket.
	 */
	@Test
	public void shouldRemoveSpaceAfterOpeningBracket()
	{
		String source = "array[ 0 ]";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("array[0]");
	}

	/**
	 * Tests that space is removed before closing bracket.
	 */
	@Test
	public void shouldRemoveSpaceBeforeClosingBracket()
	{
		String source = "array[ 0 ]";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("array[0]");
	}

	/**
	 * Tests that colon in switch case has no space before.
	 */
	@Test
	public void shouldHandleColonInSwitchCase()
	{
		String source = "case 1 :";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("case 1:");
	}

	/**
	 * Tests that colon in enhanced for has space on each side.
	 */
	@Test
	public void shouldAddSpaceAroundColonInEnhancedFor()
	{
		String source = "for(String s:list){}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("for (String s : list)");
	}

	/**
	 * Tests that colon in ternary has space on each side.
	 */
	@Test
	public void shouldAddSpaceAroundColonInTernary()
	{
		String source = "a?b:c";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a ? b : c");
	}

	/**
	 * Tests handling of comma in array initializer.
	 */
	@Test
	public void shouldHandleCommaInArrayInitializer()
	{
		String source = "{1,2,3}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("{1, 2, 3}");
	}

	/**
	 * Tests handling of comma in generic types.
	 */
	@Test
	public void shouldHandleCommaInGenericTypes()
	{
		String source = "Map<String,Integer>";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("Map<String, Integer>");
	}

	/**
	 * Tests that semicolon in for loop is properly handled.
	 */
	@Test
	public void shouldHandleSemicolonInForLoop()
	{
		String source = "for(int i=0 ; i<n ; i++){}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("for (int i = 0; i < n; i++)");
	}
}
