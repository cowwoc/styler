package io.github.cowwoc.styler.formatter.test.whitespace;

import java.util.List;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.whitespace.WhitespaceFormattingRule;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for edge cases: generics, lambdas, method references, literals, comments.
 */
public class WhitespaceEdgeCaseTest
{
	/**
	 * Tests that spacing inside string literals is preserved.
	 */
	@Test
	public void shouldPreserveSpacingInStringLiterals()
	{
		String source = "String s = \"a + b\";";
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
		String source = "char c = ' ';";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("' '");
	}

	/**
	 * Tests that spacing in line comments is preserved.
	 */
	@Test
	public void shouldPreserveSpacingInLineComments()
	{
		String source = "int x = a+b; // a+b comment";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("// a+b comment");
		requireThat(result, "result").contains("a + b");
	}

	/**
	 * Tests that spacing in block comments is preserved.
	 */
	@Test
	public void shouldPreserveSpacingInBlockComments()
	{
		String source = "int x = a+b; /* a+b */";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("/* a+b */");
		requireThat(result, "result").contains("a + b");
	}

	/**
	 * Tests handling of nested generics.
	 */
	@Test
	public void shouldHandleNestedGenerics()
	{
		String source = "Map<String,List<Integer>>";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("Map<String, List<Integer>>");
	}

	/**
	 * Tests handling of generic type with bounds.
	 */
	@Test
	public void shouldHandleGenericTypeWithBounds()
	{
		String source = "<T extends Comparable<T>>";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("<T extends Comparable<T>>");
	}

	/**
	 * Tests that diamond operator is handled correctly.
	 */
	@Test
	public void shouldHandleDiamondOperator()
	{
		String source = "new ArrayList<>";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("new ArrayList<>");
	}

	/**
	 * Tests handling of expression lambda.
	 */
	@Test
	public void shouldHandleExpressionLambda()
	{
		String source = "x->x*2";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("x -> x * 2");
	}

	/**
	 * Tests handling of multi-parameter lambda.
	 */
	@Test
	public void shouldHandleMultiParamLambda()
	{
		String source = "(a,b)->a+b";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("(a, b) -> a + b");
	}

	/**
	 * Tests handling of static method reference.
	 */
	@Test
	public void shouldHandleStaticMethodReference()
	{
		String source = "ClassName::staticMethod";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("ClassName::staticMethod");
	}

	/**
	 * Tests handling of instance method reference.
	 */
	@Test
	public void shouldHandleInstanceMethodReference()
	{
		String source = "object::instanceMethod";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("object::instanceMethod");
	}

	/**
	 * Tests handling of constructor reference.
	 */
	@Test
	public void shouldHandleConstructorReference()
	{
		String source = "ClassName::new";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("ClassName::new");
	}

	/**
	 * Tests handling of deeply nested expressions.
	 */
	@Test
	public void shouldHandleDeeplyNestedExpressions()
	{
		String source = "int x = ((a+b)*(c+d))/(e+f);";
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
		String source = "int x = a+b*c-d/e;";
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
		String source = "obj.method1().method2().method3()";
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
		String source = "a?b:c?d:e";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a ? b : c ? d : e");
	}

	/**
	 * Tests handling of cast expression.
	 */
	@Test
	public void shouldHandleCastExpression()
	{
		String source = "(int)x";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("(int)x");
	}

	/**
	 * Tests handling of chained casts.
	 */
	@Test
	public void shouldHandleChainedCasts()
	{
		String source = "(int)(long)x";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("(int)(long)x");
	}

	/**
	 * Tests handling of array access.
	 */
	@Test
	public void shouldHandleArrayAccess()
	{
		String source = "arr[i][j]";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("arr[i][j]");
	}

	/**
	 * Tests handling of array initializer with expressions.
	 */
	@Test
	public void shouldHandleArrayInitializerWithExpressions()
	{
		String source = "{a+b,c*d}";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("{a + b, c * d}");
	}

	/**
	 * Tests annotation with parameters.
	 */
	@Test
	public void shouldHandleAnnotationWithParameters()
	{
		String source = "@SuppressWarnings(\"unused\")";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("@SuppressWarnings(\"unused\")");
	}

	/**
	 * Tests that text blocks are handled.
	 */
	@Test
	public void shouldHandleTextBlock()
	{
		String source = """
			String s = \"\"\"
			    a + b
			    c + d
			    \"\"\";
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		WhitespaceFormattingRule rule = new WhitespaceFormattingRule();
		WhitespaceFormattingConfiguration config = WhitespaceFormattingConfiguration.defaultConfig();

		String result = rule.format(context, List.of(config));

		requireThat(result, "result").contains("a + b");
	}
}
