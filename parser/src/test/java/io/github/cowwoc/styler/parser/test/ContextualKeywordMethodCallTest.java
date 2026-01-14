package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for parsing method calls where contextual keywords are used as method names.
 */
public class ContextualKeywordMethodCallTest
{
	/**
	 * Tests parsing method call with 'with' as method name.
	 */
	@Test
	public void testWithMethodCall()
	{
		String source = """
			class Test
			{
				void method()
				{
					builder.with(arg);
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);
		}
	}

	/**
	 * Tests parsing method call with 'to' as method name.
	 */
	@Test
	public void testToMethodCall()
	{
		String source = """
			class Test
			{
				void method()
				{
					message.to(recipients);
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);
		}
	}

	/**
	 * Tests parsing method call with 'requires' as method name.
	 */
	@Test
	public void testRequiresMethodCall()
	{
		String source = """
			class Test
			{
				void method()
				{
					someModule.requires(dependency);
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);
		}
	}

	/**
	 * Tests parsing chained method calls with contextual keywords.
	 */
	@Test
	public void testChainedContextualKeywordMethods()
	{
		String source = """
			class Test
			{
				void method()
				{
					TestCompiler.forSystem().with(files).to(output);
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);
		}
	}
}
