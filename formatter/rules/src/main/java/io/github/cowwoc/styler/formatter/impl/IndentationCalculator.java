package io.github.cowwoc.styler.formatter.impl;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Utility class for calculating and generating indentation strings.
 * <p>
 * This class handles both regular indentation (for nested blocks) and continuation
 * indentation (for wrapped lines within a statement). It supports both spaces and
 * tabs, with configurable tab width for measurement purposes.
 * <p>
 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.2">JLS §3.2</a>,
 * whitespace includes space (SP) and horizontal tab (HT) characters.
 */
final class IndentationCalculator
{
	private static final int CONTINUATION_INDENT = 8;
	private static final char SPACE = ' ';
	private static final char TAB = '\t';

	private final int tabWidth;

	/**
	 * Creates a new indentation calculator with the specified tab width.
	 *
	 * @param tabWidth the number of spaces equivalent to one tab character,
	 *                 must be positive
	 * @throws IllegalArgumentException if {@code tabWidth} is not positive
	 */
	IndentationCalculator(int tabWidth)
	{
		requireThat(tabWidth, "tabWidth").isPositive();
		this.tabWidth = tabWidth;
	}

	/**
	 * Calculates the indentation level of a line in spaces.
	 * <p>
	 * This method measures the leading whitespace of a line, expanding tabs
	 * to their equivalent space count based on the configured tab width.
	 *
	 * @param line the line to analyze, never {@code null}
	 * @return the indentation level in spaces, always non-negative
	 * @throws NullPointerException if {@code line} is {@code null}
	 */
	int calculateIndentationLevel(String line)
	{
		requireThat(line, "line").isNotNull();

		int indentation = 0;
		for (int i = 0; i < line.length(); i += 1)
		{
			char c = line.charAt(i);
			if (c == SPACE)
			{
				indentation += 1;
			}
			else if (c == TAB)
			{
				indentation += tabWidth;
			}
			else
			{
				break;
			}
		}
		return indentation;
	}

	/**
	 * Generates an indentation string with the specified number of spaces.
	 * <p>
	 * This method creates a string containing only space characters to represent
	 * the specified indentation level. The implementation uses spaces exclusively
	 * for generated indentation to ensure consistent rendering.
	 *
	 * @param spaces the number of spaces to generate, must not be negative
	 * @return an indentation string, never {@code null}
	 * @throws IllegalArgumentException if {@code spaces} is negative
	 */
	String generateIndentation(int spaces)
	{
		requireThat(spaces, "spaces").isNotNegative();
		return " ".repeat(spaces);
	}

	/**
	 * Generates a continuation indentation string based on the base indentation.
	 * <p>
	 * Continuation indentation is used for wrapped lines that are part of the same
	 * statement or expression. This method adds {@value CONTINUATION_INDENT} spaces
	 * to the base indentation level.
	 *
	 * @param baseIndentation the base indentation level in spaces, must not be negative
	 * @return a continuation indentation string, never {@code null}
	 * @throws IllegalArgumentException if {@code baseIndentation} is negative
	 */
	String generateContinuationIndentation(int baseIndentation)
	{
		requireThat(baseIndentation, "baseIndentation").isNotNegative();
		return generateIndentation(baseIndentation + CONTINUATION_INDENT);
	}

	/**
	 * Extracts the indentation portion of a line.
	 * <p>
	 * This method returns the leading whitespace (spaces and tabs) from the
	 * beginning of a line, preserving the original characters rather than
	 * normalizing to spaces.
	 *
	 * @param line the line to analyze, never {@code null}
	 * @return the leading whitespace, never {@code null} but may be empty
	 * @throws NullPointerException if {@code line} is {@code null}
	 */
	String extractIndentation(String line)
	{
		requireThat(line, "line").isNotNull();

		int endIndex = 0;
		for (int i = 0; i < line.length(); i += 1)
		{
			char c = line.charAt(i);
			if (c == SPACE || c == TAB)
			{
				endIndex = i + 1;
			}
			else
			{
				break;
			}
		}
		return line.substring(0, endIndex);
	}
}
