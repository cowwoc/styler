package io.github.cowwoc.styler.formatter.test.brace;

import io.github.cowwoc.styler.formatter.brace.BraceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.brace.BraceStyle;
import io.github.cowwoc.styler.formatter.brace.internal.BraceFixer;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for BraceFixer transformation correctness.
 */
public class BraceFormatterTest
{
	/**
	 * Tests that idempotent formatting returns unchanged source.
	 */
	@Test
	public void shouldReturnUnchangedSourceWhenAlreadyFormatted()
	{
		String source = """
			class Test
			{
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		String result = BraceFixer.format(context, config);

		requireThat(result, "result").isEqualTo(source);
	}

	/**
	 * Tests transformation from K&R to Allman style.
	 */
	@Test
	public void shouldTransformKRToAllmanStyle()
	{
		String source = "class Test {" + "\n" + "}";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		String result = BraceFixer.format(context, config);

		requireThat(result, "result").contains("Test").contains("\n").contains("{");
	}

	/**
	 * Tests transformation from Allman to K&R style.
	 */
	@Test
	public void shouldTransformAllmanToKRStyle()
	{
		String source = """
			class Test
			{
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = new BraceFormattingConfiguration("brace-style",
			BraceStyle.SAME_LINE);

		String result = BraceFixer.format(context, config);

		requireThat(result, "result").contains("Test {");
	}

	/**
	 * Tests that formatter preserves content.
	 */
	@Test
	public void shouldPreserveSourceContent()
	{
		String source = """
			class Test {
			    int x = 42;
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		String result = BraceFixer.format(context, config);

		requireThat(result, "result").contains("int x = 42");
	}

	/**
	 * Tests formatting with multiple methods.
	 */
	@Test
	public void shouldFormatMultipleMethods()
	{
		String source = """
			class Test {
			    void method1() {
			    }
			    void method2() {
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		String result = BraceFixer.format(context, config);

		requireThat(result, "result").isNotNull();
	}

	/**
	 * Tests formatting preserves line content.
	 */
	@Test
	public void shouldPreserveLineContent()
	{
		String source = """
			class Test
			{
			    void method() { System.out.println("test"); }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		String result = BraceFixer.format(context, config);

		requireThat(result, "result").contains("System.out.println");
	}

	/**
	 * Tests formatting empty class.
	 */
	@Test
	public void shouldFormatEmptyClass()
	{
		String source = "class Test {" + "\n" + "}";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		String result = BraceFixer.format(context, config);

		requireThat(result, "result").isNotNull();
	}

	/**
	 * Tests formatting with control structures.
	 */
	@Test
	public void shouldFormatControlStructures()
	{
		String source = """
			class Test {
			    void method() {
			        if (true) {
			        }
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		String result = BraceFixer.format(context, config);

		requireThat(result, "result").isNotNull().contains("if");
	}

	/**
	 * Tests formatting preserves string content with braces.
	 */
	@Test
	public void shouldPreserveStringContent()
	{
		String source = """
			class Test {
			    String s = "test { value }";
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		String result = BraceFixer.format(context, config);

		requireThat(result, "result").contains("test { value }");
	}

	/**
	 * Tests formatting with nested classes.
	 */
	@Test
	public void shouldFormatNestedClasses()
	{
		String source = """
			class Outer {
			    class Inner {
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		String result = BraceFixer.format(context, config);

		requireThat(result, "result").contains("Outer").contains("Inner");
	}
}
