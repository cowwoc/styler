package io.github.cowwoc.styler.formatter.test.whitespace;

import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingRule;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.formatter.test.whitespace.WhitespaceTestUtils.wrapInMethod;

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
		String source = wrapInMethod("method(a,b,c);");
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
		String source = wrapInMethod("method(a , b , c);");
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
		String source = wrapInMethod("int x = 1 ;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("int x = 1;");
	}

	/**
	 * Tests that space is removed after opening parenthesis.
	 */
	@Test
	public void shouldRemoveSpaceAfterOpeningParen()
	{
		String source = wrapInMethod("method( x );");
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
		String source = wrapInMethod("method( x );");
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
		String source = wrapInMethod("method ();");
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
		String source = wrapInMethod("Object x = array[ 0 ];");
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
		String source = wrapInMethod("Object x = array[ 0 ];");
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
		String source = wrapInMethod("switch(x){case 1 :break;}");
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
		String source = wrapInMethod("for(String s:list){}");
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
		String source = wrapInMethod("int x = a?b:c;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a ? b : c");
	}

	/**
	 * Tests that semicolon in for loop is properly handled.
	 */
	@Test
	public void shouldHandleSemicolonInForLoop()
	{
		String source = wrapInMethod("for(int i=0 ; i<n ; i++){}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("for (int i = 0; i < n; i++)");
	}
}
