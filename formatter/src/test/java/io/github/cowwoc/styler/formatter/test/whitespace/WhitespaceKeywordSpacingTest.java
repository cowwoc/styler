package io.github.cowwoc.styler.formatter.test.whitespace;

import java.util.List;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingRule;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for control flow and type keyword spacing rules.
 */
public class WhitespaceKeywordSpacingTest
{
	/**
	 * Tests that space is added after if keyword.
	 */
	@Test
	public void shouldAddSpaceAfterIf()
	{
		String source = "if(x){}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("if (x)");
	}

	/**
	 * Tests that space is added after else if keywords.
	 */
	@Test
	public void shouldAddSpaceAfterElseIf()
	{
		String source = "else if(x){}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("else if (x)");
	}

	/**
	 * Tests that space is added after while keyword.
	 */
	@Test
	public void shouldAddSpaceAfterWhile()
	{
		String source = "while(x){}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("while (x)");
	}

	/**
	 * Tests that space is added after for keyword.
	 */
	@Test
	public void shouldAddSpaceAfterFor()
	{
		String source = "for(int i=0;i<10;i++){}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("for (");
	}

	/**
	 * Tests that space is added after switch keyword.
	 */
	@Test
	public void shouldAddSpaceAfterSwitch()
	{
		String source = "switch(x){}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("switch (x)");
	}

	/**
	 * Tests that space is added after synchronized keyword.
	 */
	@Test
	public void shouldAddSpaceAfterSynchronized()
	{
		String source = "synchronized(lock){}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("synchronized (lock)");
	}

	/**
	 * Tests that space is added after try keyword.
	 */
	@Test
	public void shouldAddSpaceAfterTry()
	{
		String source = "try{}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("try");
	}

	/**
	 * Tests that space is added after catch keyword.
	 */
	@Test
	public void shouldAddSpaceAfterCatch()
	{
		String source = "catch(Exception e){}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("catch (Exception e)");
	}

	/**
	 * Tests that space is added after new keyword.
	 */
	@Test
	public void shouldAddSpaceAfterNew()
	{
		String source = "new  ArrayList()";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("new ArrayList");
	}

	/**
	 * Tests that instanceof keyword has proper spacing.
	 */
	@Test
	public void shouldHandleInstanceof()
	{
		String source = "obj instanceof String";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("obj instanceof String");
	}

	/**
	 * Tests that extends keyword has proper spacing.
	 */
	@Test
	public void shouldHandleExtends()
	{
		String source = "class Foo extends Bar {}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("extends Bar");
	}

	/**
	 * Tests that implements keyword has proper spacing.
	 */
	@Test
	public void shouldHandleImplements()
	{
		String source = "class Foo implements Bar {}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("implements Bar");
	}

	/**
	 * Tests that return keyword has proper spacing.
	 */
	@Test
	public void shouldHandleReturn()
	{
		String source = "return x;";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("return x");
	}

	/**
	 * Tests that throw keyword has proper spacing.
	 */
	@Test
	public void shouldHandleThrow()
	{
		String source = "throw e;";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("throw e");
	}

	/**
	 * Tests that do-while keyword has proper spacing.
	 */
	@Test
	public void shouldHandleDoWhile()
	{
		String source = "do{}while(true);";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("while (true)");
	}
}
