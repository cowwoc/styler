package io.github.cowwoc.styler.formatter.test.whitespace;

import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingRule;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.formatter.test.whitespace.WhitespaceTestUtils.wrapInMethod;

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
		String source = wrapInMethod("if(x){}");
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
		String source = wrapInMethod("if(x){}else if(y){}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("else if (y)");
	}

	/**
	 * Tests that space is added after while keyword.
	 */
	@Test
	public void shouldAddSpaceAfterWhile()
	{
		String source = wrapInMethod("while(x){}");
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
		String source = wrapInMethod("for(int i=0;i<10;i++){}");
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
		String source = wrapInMethod("switch(x){}");
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
		String source = wrapInMethod("synchronized(lock){}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("synchronized (lock)");
	}

	/**
	 * Tests that space is preserved after try keyword.
	 */
	@Test
	public void shouldPreserveSpaceAfterTry()
	{
		String source = wrapInMethod("try{}catch(Exception e){}");
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
		String source = wrapInMethod("try{}catch(Exception e){}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("catch (Exception e)");
	}

	/**
	 * Tests that extra space after new keyword is normalized.
	 */
	@Test
	public void shouldNormalizeSpaceAfterNew()
	{
		String source = wrapInMethod("Object o=new  Object();");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("new Object");
	}

	/**
	 * Tests that instanceof keyword has proper spacing.
	 */
	@Test
	public void shouldHandleInstanceof()
	{
		String source = wrapInMethod("boolean b=obj instanceof String;");
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
		String source = wrapInMethod("return x;");
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
		String source = wrapInMethod("throw e;");
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
		String source = wrapInMethod("do{}while(true);");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("while (true)");
	}
}
