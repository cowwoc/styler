package io.github.cowwoc.styler.formatter.linelength.internal;

import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Handles wrapping of long string literals in source code.
 * Preserves URLs and file paths that should not be broken across lines.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class StringWrapper
{
	/**
	 * Utility class constructor.
	 */
	private StringWrapper()
	{
	}

	/**
	 * Checks if a string should be wrapped based on configuration.
	 *
	 * @param stringLiteral the string literal text (including quotes)
	 * @param config the line length configuration
	 * @return true if the string should be wrapped, false otherwise
	 */
	public static boolean shouldWrap(String stringLiteral, LineLengthConfiguration config)
	{
		requireThat(stringLiteral, "stringLiteral").isNotNull();
		requireThat(config, "config").isNotNull();

		return config.wrapLongStrings() && stringLiteral.length() > config.maxLineLength();
	}

	/**
	 * Splits a long string literal into multiple parts with proper continuation.
	 * Attempts to break at word boundaries when possible.
	 * Protects URLs and file paths from being broken.
	 *
	 * @param stringLiteral the string literal to split
	 * @param maxLength the maximum allowed length per part
	 * @param indentation the indentation for continuation lines
	 * @return list of string parts to be concatenated
	 */
	public static List<String> splitString(String stringLiteral, int maxLength, int indentation)
	{
		requireThat(stringLiteral, "stringLiteral").isNotNull();
		requireThat(maxLength, "maxLength").isGreaterThan(0);
		requireThat(indentation, "indentation").isGreaterThanOrEqualTo(0);

		List<String> parts = new ArrayList<>();
		String content = removeQuotes(stringLiteral);

		if (content.isEmpty())
		{
			parts.add(stringLiteral);
			return parts;
		}

		// Check if string contains URLs or file paths - don't break them
		if (containsProtectedContent(content))
		{
			parts.add(stringLiteral);
			return parts;
		}

		// Split at word boundaries
		String[] words = content.split("\\s+");
		StringBuilder currentPart = new StringBuilder("\"");

		for (String word : words)
		{
			String toAdd;
			if (currentPart.length() == 1)
			{
				toAdd = word;
			}
			else
			{
				toAdd = " " + word;
			}

			if (currentPart.length() + toAdd.length() + 1 > maxLength)
			{
				// Finish current part and start a new one
				currentPart.append("\" +");
				parts.add(currentPart.toString());
				currentPart = new StringBuilder("\"");
				currentPart.append(word);
			}
			else
			{
				currentPart.append(toAdd);
			}
		}

		// Add the final part
		if (currentPart.length() > 1)
		{
			currentPart.append('"');
			parts.add(currentPart.toString());
		}

		List<String> result;
		if (parts.isEmpty())
		{
			result = List.of(stringLiteral);
		}
		else
		{
			result = parts;
		}
		return result;
	}

	/**
	 * Checks if the string contains protected content like URLs or file paths.
	 *
	 * @param content the string content (without quotes)
	 * @return true if the content contains protected patterns
	 */
	private static boolean containsProtectedContent(String content)
	{
		// Protect URLs
		boolean hasUrl = content.contains("://") || content.contains("http") ||
			content.contains("https");
		if (hasUrl)
		{
			return true;
		}

		// Protect file paths with slashes or backslashes
		return content.contains("/") || content.contains("\\");
	}

	/**
	 * Removes quotes from a string literal.
	 *
	 * @param stringLiteral the string literal (with quotes)
	 * @return the content without quotes
	 */
	private static String removeQuotes(String stringLiteral)
	{
		if (stringLiteral.length() >= 2 &&
			stringLiteral.startsWith("\"") &&
			stringLiteral.endsWith("\""))
		{
			return stringLiteral.substring(1, stringLiteral.length() - 1);
		}
		return stringLiteral;
	}
}
