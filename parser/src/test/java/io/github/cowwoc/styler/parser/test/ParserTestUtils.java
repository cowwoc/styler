package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Utility methods for parser tests.
 */
public final class ParserTestUtils
{
	private ParserTestUtils()
	{
	}

	/**
	 * Parses source code and returns the Parser. Callers must close the returned Parser to release resources.
	 *
	 * @param source the source code to parse
	 * @return the Parser containing parsed nodes
	 * @throws NullPointerException if {@code source} is null
	 * @throws AssertionError       if parsing fails
	 */
	public static Parser parse(String source)
	{
		requireThat(source, "source").isNotNull();
		Parser parser = new Parser(source);
		switch (parser.parse())
		{
			case ParseResult.Success _ ->
			{
				return parser;
			}
			case ParseResult.Failure failure ->
			{
				parser.close();
				throw new AssertionError("Parsing failed: " + failure);
			}
		}
	}

	/**
	 * Asserts that parsing the given source code fails.
	 *
	 * @param source the source code that should fail to parse
	 * @throws NullPointerException if {@code source} is null
	 * @throws AssertionError       if parsing succeeds
	 */
	public static void assertParseFails(String source)
	{
		requireThat(source, "source").isNotNull();
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			if (result instanceof ParseResult.Success)
				throw new AssertionError("Expected parsing to fail but it succeeded");
		}
	}
}
