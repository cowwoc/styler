package io.github.cowwoc.styler.formatter.test.whitespace;

/**
 * Utility methods for whitespace tests.
 */
public final class WhitespaceTestUtils
{
	/**
	 * Prevents instantiation.
	 */
	private WhitespaceTestUtils()
	{
	}

	/**
	 * Wraps a code snippet in a valid Java class/method structure.
	 *
	 * @param snippet the code snippet to wrap
	 * @return valid Java source code containing the snippet
	 */
	public static String wrapInMethod(String snippet)
	{
		return "class T{void m(){" + snippet + "}}";
	}
}
