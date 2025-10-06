package io.github.cowwoc.styler.benchmark.test;

import io.github.cowwoc.styler.parser.JavaLexer;
import io.github.cowwoc.styler.parser.TokenInfo;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Validates token counting accuracy for benchmark implementation.
 */
public class TokenCountingTest
{
	/**
	 * Verifies token counting for a simple class declaration.
	 */
	@Test
	public void countTokensInSimpleClass()
	{
		String source = """
			package test;

			public class Foo {
				private int x;
			}
			""";

		int tokenCount = countTokens(source);

		// Expected tokens: package, test, ;, public, class, Foo, {, private, int, x, ;, }
		assertEquals(tokenCount, 12, "Token count should match expected");
	}

	/**
	 * Verifies token counting handles empty input correctly.
	 */
	@Test
	public void countTokensInEmptyString()
	{
		int tokenCount = countTokens("");
		assertEquals(tokenCount, 0, "Empty string should have zero tokens");
	}

	/**
	 * Verifies token counting excludes EOF token.
	 */
	@Test
	public void tokenCountExcludesEOF()
	{
		String source = "int x;";
		JavaLexer lexer = new JavaLexer(source);
		int count = 0;
		TokenInfo lastToken = null;

		while (true)
		{
			TokenInfo token = lexer.nextToken();
			if (token.type() == TokenType.EOF)
			{
				lastToken = token;
				break;
			}
			++count;
		}

		assertEquals(count, 3, "Should count 3 tokens: int, x, ;");
		assertEquals(lastToken.type(), TokenType.EOF, "Last token should be EOF");
	}

	/**
	 * Verifies token counting for a class with multiple members.
	 */
	@Test
	public void countTokensInComplexClass()
	{
		String source = """
			public class Calculator {
				public int add(int a, int b) {
					return a + b;
				}
			}
			""";

		int tokenCount = countTokens(source);

		// Verify we get a reasonable token count (exact count may vary with whitespace handling)
		assertTrue(tokenCount > 20, "Complex class should have many tokens");
	}

	private int countTokens(String content)
	{
		return io.github.cowwoc.styler.benchmark.TokenCountingUtil.countTokens(content);
	}
}
