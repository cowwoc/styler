package io.github.cowwoc.styler.formatter.test.whitespace;

import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingRule;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.formatter.test.whitespace.WhitespaceTestUtils.wrapInMethod;

/**
 * Tests for edge cases in whitespace formatting.
 */
public class WhitespaceEdgeCaseTest
{
	/**
	 * Tests that spacing inside string literals is preserved.
	 */
	@Test
	public void shouldPreserveSpacingInStringLiterals()
	{
		String source = wrapInMethod("""
			String s = "a + b";
			""");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("\"a + b\"");
	}

	/**
	 * Tests that spacing inside character literals is preserved.
	 */
	@Test
	public void shouldPreserveSpacingInCharLiterals()
	{
		String source = wrapInMethod("""
			char c = ' ';
			""");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("' '");
	}

	/**
	 * Tests handling of deeply nested expressions.
	 */
	@Test
	public void shouldHandleDeeplyNestedExpressions()
	{
		String source = wrapInMethod("""
			int x = ((a+b)*(c+d))/(e+f);
			""");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("(a + b)");
		requireThat(result, "result").contains("(c + d)");
		requireThat(result, "result").contains("(e + f)");
	}

	/**
	 * Tests handling of mixed operators.
	 */
	@Test
	public void shouldHandleMixedOperators()
	{
		String source = wrapInMethod("""
			int x = a+b*c-d/e;
			""");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a + b * c - d / e");
	}

	/**
	 * Tests handling of chained method calls.
	 */
	@Test
	public void shouldHandleChainedMethodCalls()
	{
		String source = wrapInMethod("""
			Object x = obj.method1().method2().method3();
			""");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("method1()");
		requireThat(result, "result").contains("method2()");
		requireThat(result, "result").contains("method3()");
	}

	/**
	 * Tests handling of chained ternaries.
	 */
	@Test
	public void shouldHandleChainedTernaries()
	{
		String source = wrapInMethod("""
			int x = a?b:c?d:e;
			""");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a ? b : c ? d : e");
	}
}
