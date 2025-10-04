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
public final class IndentationCalculator
{
	private static final char SPACE = ' ';
	private static final char TAB = '\t';

	private final int tabWidth;
	private final int continuationIndentSpaces;

	/**
	 * Creates a new indentation calculator with the specified configuration.
	 *
	 * @param tabWidth the number of spaces equivalent to one tab character,
	 *                 must be positive
	 * @param continuationIndentSpaces the number of spaces to add for continuation lines,
	 *                                 must be positive
	 * @throws IllegalArgumentException if any parameter is not positive
	 */
	public IndentationCalculator(int tabWidth, int continuationIndentSpaces)
	{
		requireThat(tabWidth, "tabWidth").isPositive();
		requireThat(continuationIndentSpaces, "continuationIndentSpaces").isPositive();
		this.tabWidth = tabWidth;
		this.continuationIndentSpaces = continuationIndentSpaces;
	}

	/**
	 * Calculates the indentation level of a line in spaces.
	 * <p>
	 * This method measures the leading whitespace of a line, expanding tabs
	 * to their equivalent space count based on the configured tab width.
	 *
	 * @param line the line to analyze, never {@code null}
	 * @param tabWidth the number of spaces equivalent to one tab character
	 * @return the indentation level in spaces, always non-negative
	 * @throws NullPointerException if {@code line} is {@code null}
	 * @throws IllegalArgumentException if {@code tabWidth} is not positive
	 */
	public static int calculateIndentationLevel(String line, int tabWidth)
	{
		requireThat(line, "line").isNotNull();
		requireThat(tabWidth, "tabWidth").isPositive();

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
	 * Calculates the indentation level of a line in spaces.
	 * <p>
	 * This is an instance method that delegates to the static method using
	 * the configured tab width.
	 *
	 * @param line the line to analyze, never {@code null}
	 * @return the indentation level in spaces, always non-negative
	 * @throws NullPointerException if {@code line} is {@code null}
	 */
	public int calculateIndentationLevel(String line)
	{
		return calculateIndentationLevel(line, tabWidth);
	}

	/**
	 * Generates an indentation string with the specified number of spaces.
	 * <p>
	 * This method creates a string containing only space characters to represent
	 * the specified indentation level. For mode-aware generation, use
	 * {@link #generateIndentation(int, IndentationMode, int)}.
	 *
	 * @param spaces the number of spaces to generate, must not be negative
	 * @return an indentation string using only spaces, never {@code null}
	 * @throws IllegalArgumentException if {@code spaces} is negative
	 */
	public String generateIndentation(int spaces)
	{
		requireThat(spaces, "spaces").isNotNegative();
		return " ".repeat(spaces);
	}

	/**
	 * Generates an indentation string according to the specified mode.
	 * <p>
	 * This method supports three indentation modes:
	 * <ul>
	 *   <li>{@link IndentationMode#SPACES} - Uses only space characters</li>
	 *   <li>{@link IndentationMode#TABS} - Uses only tab characters</li>
	 *   <li>{@link IndentationMode#MIXED} - Uses tabs for structural indentation,
	 *       spaces for alignment within the final indentation level</li>
	 * </ul>
	 *
	 * @param spaces the total indentation level in spaces, must not be negative
	 * @param mode the indentation mode to use, never {@code null}
	 * @param indentSize the number of spaces per indentation level, must be positive
	 * @return an indentation string in the specified mode, never {@code null}
	 * @throws NullPointerException if {@code mode} is {@code null}
	 * @throws IllegalArgumentException if {@code spaces} is negative or {@code indentSize}
	 *         is not positive
	 */
	public String generateIndentation(int spaces, IndentationMode mode, int indentSize)
	{
		requireThat(spaces, "spaces").isNotNegative();
		requireThat(mode, "mode").isNotNull();
		requireThat(indentSize, "indentSize").isPositive();

		return switch (mode)
		{
			case SPACES -> " ".repeat(spaces);
			case TABS ->
			{
				// Convert spaces to tabs, rounding up partial tabs
				int tabCount = (spaces + indentSize - 1) / indentSize;
				yield String.valueOf(TAB).repeat(tabCount);
			}
			case MIXED ->
			{
				// Use tabs for full indentation levels, spaces for remainder
				int fullLevels = spaces / indentSize;
				int remainingSpaces = spaces % indentSize;
				yield String.valueOf(TAB).repeat(fullLevels) + " ".repeat(remainingSpaces);
			}
		};
	}

	/**
	 * Generates a continuation indentation string based on the base indentation.
	 * <p>
	 * Continuation indentation is used for wrapped lines that are part of the same
	 * statement or expression. This method adds the configured continuation indent
	 * spaces to the base indentation level.
	 *
	 * @param baseIndentation the base indentation level in spaces, must not be negative
	 * @return a continuation indentation string, never {@code null}
	 * @throws IllegalArgumentException if {@code baseIndentation} is negative
	 */
	public String generateContinuationIndentation(int baseIndentation)
	{
		requireThat(baseIndentation, "baseIndentation").isNotNegative();
		return generateIndentation(baseIndentation + continuationIndentSpaces);
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
	public String extractIndentation(String line)
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
