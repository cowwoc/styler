package io.github.cowwoc.styler.formatter.test.indentation;

import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.indentation.IndentationType;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingConfiguration;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingRule;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for indentation of nested code structures.
 */
public final class IndentationNestedStructureTest
{
	private static final String RULE_ID = "indentation";

	/**
	 * Verifies that class body is indented correctly.
	 */
	@Test
	public void shouldIndentClassBody()
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

		String formatted = rule.format(context, config);

		String expected = """
			class Test {
			    int x;
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that method body is indented correctly.
	 */
	@Test
	public void shouldIndentMethodBody()
	{
		String sourceCode = """
			class Test {
			    void method() {
			return;
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		String expected = """
			class Test {
			    void method() {
			        return;
			    }
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that nested if statements are indented correctly.
	 */
	@Test
	public void shouldIndentNestedIfStatements()
	{
		String sourceCode = """
			class Test {
			    void method() {
			if (true) {
			if (false) {
			x = 1;
			}
			}
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		String expected = """
			class Test {
			    void method() {
			        if (true) {
			            if (false) {
			                x = 1;
			            }
			        }
			    }
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that inner class is indented correctly.
	 */
	@Test
	public void shouldIndentInnerClass()
	{
		String sourceCode = """
			class Outer {
			    class Inner {
			int x;
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		String expected = """
			class Outer {
			    class Inner {
			        int x;
			    }
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that anonymous class is indented correctly.
	 */
	@Test
	public void shouldIndentAnonymousClass()
	{
		String sourceCode = """
			class Test {
			    void method() {
			Object obj = new Object() {
			public String toString() {
			return "test";
			}
			};
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		String expected = """
			class Test {
			    void method() {
			        Object obj = new Object() {
			            public String toString() {
			                return "test";
			            }
			        };
			    }
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that lambda body is indented correctly.
	 */
	@Test
	public void shouldIndentLambdaBody()
	{
		String sourceCode = """
			class Test {
			    void method() {
			list.forEach(x -> {
			System.out.println(x);
			});
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		String expected = """
			class Test {
			    void method() {
			        list.forEach(x -> {
			            System.out.println(x);
			        });
			    }
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that try-catch-finally is indented correctly.
	 */
	@Test
	public void shouldIndentTryCatchFinally()
	{
		String sourceCode = """
			class Test {
			    void method() {
			try {
			x = 1;
			} catch (Exception e) {
			System.out.println(e);
			} finally {
			cleanup();
			}
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		String expected = """
			class Test {
			    void method() {
			        try {
			            x = 1;
			        } catch (Exception e) {
			            System.out.println(e);
			        } finally {
			            cleanup();
			        }
			    }
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that switch cases are indented correctly.
	 */
	@Test
	public void shouldIndentSwitchCases()
	{
		String sourceCode = """
			class Test {
			    void method() {
			switch (x) {
			case 1:
			y = 1;
			break;
			case 2:
			y = 2;
			break;
			default:
			y = 0;
			}
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		String expected = """
			class Test {
			    void method() {
			        switch (x) {
			            case 1:
			            y = 1;
			            break;
			            case 2:
			            y = 2;
			            break;
			            default:
			            y = 0;
			        }
			    }
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that for loop is indented correctly.
	 */
	@Test
	public void shouldIndentForLoop()
	{
		String sourceCode = """
			class Test {
			    void method() {
			for (int i = 0; i < 10; i++) {
			System.out.println(i);
			}
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		String expected = """
			class Test {
			    void method() {
			        for (int i = 0; i < 10; i++) {
			            System.out.println(i);
			        }
			    }
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that while loop is indented correctly.
	 */
	@Test
	public void shouldIndentWhileLoop()
	{
		String sourceCode = """
			class Test {
			    void method() {
			while (true) {
			x = 1;
			break;
			}
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		String expected = """
			class Test {
			    void method() {
			        while (true) {
			            x = 1;
			            break;
			        }
			    }
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that do-while loop is indented correctly.
	 */
	@Test
	public void shouldIndentDoWhileLoop()
	{
		String sourceCode = """
			class Test {
			    void method() {
			do {
			x = 1;
			} while (true);
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		String expected = """
			class Test {
			    void method() {
			        do {
			            x = 1;
			        } while (true);
			    }
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}

	/**
	 * Verifies that enum with bodies is indented correctly.
	 */
	@Test
	public void shouldIndentEnumWithBodies()
	{
		String sourceCode = """
			enum Color {
			    RED {
			void method() {}
			    }
			}
			""";

		FormattingRule rule = new IndentationFormattingRule();
		TestTransformationContext context = new TestTransformationContext(sourceCode);
		IndentationFormattingConfiguration config = new IndentationFormattingConfiguration(
			RULE_ID, IndentationType.SPACES, 4, 4);

		String formatted = rule.format(context, config);

		String expected = """
			enum Color {
			    RED {
			        void method() {}
			    }
			}
			""";
		requireThat(formatted, "formatted").isEqualTo(expected);
	}
}
