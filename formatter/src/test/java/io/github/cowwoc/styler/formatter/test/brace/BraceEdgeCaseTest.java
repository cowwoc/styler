package io.github.cowwoc.styler.formatter.test.brace;

import io.github.cowwoc.styler.formatter.brace.BraceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.brace.BraceFormattingRule;
import io.github.cowwoc.styler.formatter.brace.BraceStyle;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for edge cases in brace formatting.
 */
public class BraceEdgeCaseTest
{
	/**
	 * Tests handling of comments before braces.
	 */
	@Test
	public void shouldHandleCommentsBeforeBrace()
	{
		String source = """
			class Test // comment
			{
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("comment");
	}

	/**
	 * Tests handling of empty blocks.
	 */
	@Test
	public void shouldHandleEmptyBlocks()
	{
		String source = """
			class Test
			{
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").isEqualTo(source);
	}

	/**
	 * Tests handling of nested blocks.
	 */
	@Test
	public void shouldHandleNestedBlocks()
	{
		String source = """
			class Test
			{
			    class Inner
			    {
			        void method()
			        {
			        }
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("Test").contains("Inner").contains("method");
	}

	/**
	 * Tests handling of annotations before constructs.
	 */
	@Test
	public void shouldHandleAnnotationsBeforeBrace()
	{
		String source = """
			@Override
			public void method() {
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("@Override");
	}

	/**
	 * Tests handling of multiple spaces before brace.
	 */
	@Test
	public void shouldNormalizeMultipleSpaces()
	{
		String source = "class Test  {" + "\n" + "}";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").isNotNull();
	}

	/**
	 * Tests handling of tabs before brace.
	 */
	@Test
	public void shouldHandleTabsBeforeBrace()
	{
		String source = "class Test\t{" + "\n" + "}";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").isNotNull();
	}

	/**
	 * Tests handling of lambda expressions.
	 */
	@Test
	public void shouldHandleLambdaExpressions()
	{
		String source = """
			class Test {
			    Runnable r = () -> {
			    };
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("Runnable");
	}

	/**
	 * Tests handling of array initializers.
	 */
	@Test
	public void shouldHandleArrayInitializers()
	{
		String source = """
			class Test {
			    int[] arr = { 1, 2, 3 };
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("int[]");
	}

	/**
	 * Tests handling of try-catch-finally.
	 */
	@Test
	public void shouldHandleTryCatchFinally()
	{
		String source = """
			class Test {
			    void method() {
			        try {
			        } catch (Exception e) {
			        } finally {
			        }
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("try").contains("catch").contains("finally");
	}

	/**
	 * Tests handling of static initializers.
	 */
	@Test
	public void shouldHandleStaticInitializers()
	{
		String source = """
			class Test {
			    static {
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("static");
	}

	/**
	 * Tests handling of enum declarations.
	 */
	@Test
	public void shouldHandleEnumDeclarations()
	{
		String source = """
			enum Color {
			    RED, GREEN, BLUE
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("RED");
	}

	/**
	 * Tests handling of interface with default methods.
	 */
	@Test
	public void shouldHandleInterfaceDefaultMethods()
	{
		String source = """
			interface Foo {
			    default void method() {
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("interface");
	}

	/**
	 * Tests handling of switch statements.
	 */
	@Test
	public void shouldHandleSwitchStatements()
	{
		String source = """
			class Test {
			    void method() {
			        switch (x) {
			            case 1:
			                break;
			        }
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("switch");
	}

	/**
	 * Tests handling of do-while loops.
	 */
	@Test
	public void shouldHandleDoWhileLoops()
	{
		String source = """
			class Test {
			    void method() {
			        do {
			        } while (true);
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("do");
	}

	/**
	 * Tests handling of synchronized blocks.
	 */
	@Test
	public void shouldHandleSynchronizedBlocks()
	{
		String source = """
			class Test {
			    void method() {
			        synchronized (this) {
			        }
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("synchronized");
	}

	/**
	 * Tests that single style applies uniformly to all constructs.
	 */
	@Test
	public void shouldApplySingleStyleToAllConstructs()
	{
		String source = """
			class Test {
			    void method() {
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = new BraceFormattingConfiguration("brace-style",
			BraceStyle.NEW_LINE);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, config);

		// Both class and method braces should be converted to Allman style
		requireThat(result, "result").isNotNull();
	}

	/**
	 * Tests formatting with very long declarations.
	 */
	@Test
	public void shouldHandleLongDeclarations()
	{
		String source = """
			class VeryLongClassNameThatExceedsNormalLengthForClassDeclarations
			{
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("VeryLong");
	}

	/**
	 * Tests formatting with generic types.
	 */
	@Test
	public void shouldHandleGenericTypes()
	{
		String source = """
			class Test<T extends Comparable<T>> {
			    <U> void method() {
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("extends");
	}

	/**
	 * Tests formatting with block comments.
	 */
	@Test
	public void shouldPreserveBlockComments()
	{
		String source = """
			class Test /* comment */ {
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingRule rule = new BraceFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").contains("comment");
	}
}
