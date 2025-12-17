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
	 * Asserts that the given source code parses successfully and returns a valid root node.
	 *
	 * @param source the source code to parse
	 * @throws AssertionError if parsing fails or the root node is invalid
	 */
	public static void assertParseSucceeds(String source)
	{
		try (Parser parser = new Parser(source))
		{
			switch (parser.parse())
			{
				case ParseResult.Success success ->
					requireThat(success.rootNode().isValid(), "root.isValid()").
						withContext(success.rootNode(), "rootNode").
						isTrue();
				case ParseResult.Failure failure ->
					throw new AssertionError("Expected Success but got: " + failure);
			}
		}
	}
}
