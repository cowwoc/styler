package io.github.cowwoc.styler.formatter.test.whitespace;

import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingRule;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.formatter.test.whitespace.WhitespaceTestUtils.wrapInMethod;

/**
 * Tests for binary and unary operator spacing rules.
 */
public class WhitespaceOperatorSpacingTest
{
	/**
	 * Tests that space is added around plus operator.
	 */
	@Test
	public void shouldAddSpaceAroundPlusOperator()
	{
		String source = wrapInMethod("int x = a+b;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a + b");
	}

	/**
	 * Tests that space is added around minus operator.
	 */
	@Test
	public void shouldAddSpaceAroundMinusOperator()
	{
		String source = wrapInMethod("int x = a-b;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a - b");
	}

	/**
	 * Tests that space is added around multiply operator.
	 */
	@Test
	public void shouldAddSpaceAroundMultiplyOperator()
	{
		String source = wrapInMethod("int x = a*b;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a * b");
	}

	/**
	 * Tests that space is added around divide operator.
	 */
	@Test
	public void shouldAddSpaceAroundDivideOperator()
	{
		String source = wrapInMethod("int x = a/b;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a / b");
	}

	/**
	 * Tests that space is added around modulo operator.
	 */
	@Test
	public void shouldAddSpaceAroundModuloOperator()
	{
		String source = wrapInMethod("int x = a%b;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a % b");
	}

	/**
	 * Tests that space is added around equals comparison.
	 */
	@Test
	public void shouldAddSpaceAroundEqualsComparison()
	{
		String source = wrapInMethod("if (a==b) {}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a == b");
	}

	/**
	 * Tests that space is added around not equals operator.
	 */
	@Test
	public void shouldAddSpaceAroundNotEquals()
	{
		String source = wrapInMethod("if (a!=b) {}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a != b");
	}

	/**
	 * Tests that space is added around less than operator in comparison context.
	 */
	@Test
	public void shouldAddSpaceAroundLessThanComparison()
	{
		String source = wrapInMethod("if (a<b) {}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a < b");
	}

	/**
	 * Tests that space is added around greater than operator in comparison context.
	 */
	@Test
	public void shouldAddSpaceAroundGreaterThanComparison()
	{
		String source = wrapInMethod("if (a>b) {}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a > b");
	}

	/**
	 * Tests that space is added around logical AND operator.
	 */
	@Test
	public void shouldAddSpaceAroundLogicalAnd()
	{
		String source = wrapInMethod("if (a&&b) {}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a && b");
	}

	/**
	 * Tests that space is added around logical OR operator.
	 */
	@Test
	public void shouldAddSpaceAroundLogicalOr()
	{
		String source = wrapInMethod("if (a||b) {}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a || b");
	}

	/**
	 * Tests that space is added around assignment operator.
	 */
	@Test
	public void shouldAddSpaceAroundAssignment()
	{
		String source = wrapInMethod("int x=1;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("x = 1");
	}

	/**
	 * Tests that space is added around plus equals operator.
	 */
	@Test
	public void shouldAddSpaceAroundPlusEquals()
	{
		String source = wrapInMethod("x+=1;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("x += 1");
	}

	/**
	 * Tests that space is added around minus equals operator.
	 */
	@Test
	public void shouldAddSpaceAroundMinusEquals()
	{
		String source = wrapInMethod("x-=1;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("x -= 1");
	}

	/**
	 * Tests that extra spaces around operators are normalized.
	 */
	@Test
	public void shouldRemoveExtraSpacesAroundOperator()
	{
		String source = wrapInMethod("int x = a  +  b;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a + b");
		requireThat(result, "result").doesNotContain("a  +");
		requireThat(result, "result").doesNotContain("+  b");
	}

	/**
	 * Tests that no space is added after prefix increment operator.
	 */
	@Test
	public void shouldNotAddSpaceAfterPrefixIncrement()
	{
		String source = wrapInMethod("int x = ++ y;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("++y");
	}

	/**
	 * Tests that no space is added after prefix decrement operator.
	 */
	@Test
	public void shouldNotAddSpaceAfterPrefixDecrement()
	{
		String source = wrapInMethod("int x = -- y;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("--y");
	}

	/**
	 * Tests that no space is added after logical NOT operator.
	 */
	@Test
	public void shouldNotAddSpaceAfterLogicalNot()
	{
		String source = wrapInMethod("if (! flag) {}");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("!flag");
	}

	/**
	 * Tests that no space is added before postfix increment operator.
	 */
	@Test
	public void shouldNotAddSpaceBeforePostfixIncrement()
	{
		String source = wrapInMethod("int x = y ++;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("y++");
	}

	/**
	 * Tests that no space is added before postfix decrement operator.
	 */
	@Test
	public void shouldNotAddSpaceBeforePostfixDecrement()
	{
		String source = wrapInMethod("int x = y --;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("y--");
	}

	/**
	 * Tests that space is added around ternary question mark.
	 */
	@Test
	public void shouldAddSpaceAroundTernaryQuestion()
	{
		String source = wrapInMethod("int x = a?b:c;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a ? b : c");
	}

	/**
	 * Tests that bitwise operators get proper spacing.
	 */
	@Test
	public void shouldAddSpaceAroundBitwiseAnd()
	{
		String source = wrapInMethod("int x = a&b;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a & b");
	}

	/**
	 * Tests that shift operators get proper spacing.
	 */
	@Test
	public void shouldAddSpaceAroundLeftShift()
	{
		String source = wrapInMethod("int x = a<<b;");
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a << b");
	}
}
