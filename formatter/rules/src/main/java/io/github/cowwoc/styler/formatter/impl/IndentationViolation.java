package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.SourcePosition;

/**
 * Represents an indentation violation detected during analysis.
 * <p>
 * This immutable record captures the location and severity of an indentation mismatch,
 * including both the expected and actual indentation levels. It provides all information
 * needed to generate a correction via {@link IndentationCorrector}.
 * <p>
 * Indentation levels are measured in spaces, regardless of whether the source uses
 * tabs, spaces, or mixed indentation. The {@link IndentationCalculator} handles
 * conversion from physical characters to logical indentation levels.
 *
 * @param position the source position where the violation occurs, never {@code null}
 * @param expectedIndentation the expected indentation level in spaces, must not be negative
 * @param actualIndentation the actual indentation level in spaces, must not be negative
 * @param lineContent the content of the line containing the violation, never {@code null}
 */
public record IndentationViolation(
	SourcePosition position,
	int expectedIndentation,
	int actualIndentation,
	String lineContent)
{
	/**
	 * Compact constructor validating all parameters.
	 *
	 * @throws NullPointerException if {@code position} or {@code lineContent} is {@code null}
	 * @throws IllegalArgumentException if indentation values are negative
	 */
	public IndentationViolation
	{
		if (position == null)
		{
			throw new NullPointerException("position cannot be null");
		}
		if (lineContent == null)
		{
			throw new NullPointerException("lineContent cannot be null");
		}
		if (expectedIndentation < 0)
		{
			throw new IllegalArgumentException("expectedIndentation must not be negative: " +
				expectedIndentation);
		}
		if (actualIndentation < 0)
		{
			throw new IllegalArgumentException("actualIndentation must not be negative: " +
				actualIndentation);
		}
	}

	/**
	 * Returns the line number where this violation occurs.
	 *
	 * @return the line number (1-based), always positive
	 */
	public int getLineNumber()
	{
		return position.line();
	}

	/**
	 * Returns the indentation difference (expected minus actual).
	 * <p>
	 * A positive value indicates under-indentation (needs more spaces),
	 * while a negative value indicates over-indentation (needs fewer spaces).
	 *
	 * @return the indentation delta in spaces
	 */
	public int getIndentationDelta()
	{
		return expectedIndentation - actualIndentation;
	}

	/**
	 * Checks if this violation represents under-indentation.
	 *
	 * @return {@code true} if the line needs more indentation, {@code false} otherwise
	 */
	public boolean isUnderIndented()
	{
		return expectedIndentation > actualIndentation;
	}

	/**
	 * Checks if this violation represents over-indentation.
	 *
	 * @return {@code true} if the line needs less indentation, {@code false} otherwise
	 */
	public boolean isOverIndented()
	{
		return expectedIndentation < actualIndentation;
	}
}
