package io.github.cowwoc.styler.formatter.test.indentation;

/**
 * Utility methods for indentation tests.
 */
public final class IndentationTestUtils
{
	/**
	 * Prevents instantiation.
	 */
	private IndentationTestUtils()
	{
	}

	/**
	 * Returns the indented line from formatted output (second line in class structure).
	 *
	 * @param formatted the formatted source code
	 * @return the indented line, or {@code null} if the output has fewer than two lines
	 */
	public static String getIndentedLine(String formatted)
	{
		String[] lines = formatted.split("\n");
		if (lines.length > 1)
			return lines[1];
		return null;
	}
}
