package io.github.cowwoc.styler.ast;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents a range in source code from a start position to an end position.
 * This immutable record enables precise source location tracking for AST nodes.
 *
 * @param start the starting position (inclusive)
 * @param end the ending position (exclusive)
 */
public record SourceRange(SourcePosition start, SourcePosition end)
	{
	/**
	 * Compact constructor validating {@code start} position is before or at {@code end} position.
	 *
	 * @throws NullPointerException if {@code start} or {@code end} is null
	 * @throws IllegalArgumentException if {@code start} is after {@code end}
	 */
	public SourceRange
		{
		requireThat(start, "start").isNotNull();
		requireThat(end, "end").isNotNull();
		requireThat(start.line(), "start.line").isLessThanOrEqualTo(end.line());
		if (start.line() == end.line())
			{
			requireThat(start.column(), "start.column").isLessThanOrEqualTo(end.column());
		}
	}

	/**
	 * Creates a range spanning a single position.
	 *
	 * @param position the position to span
	 * @return a {@code SourceRange} covering only the specified position
	 * @throws NullPointerException if {@code position} is null
	 */
	public static SourceRange single(SourcePosition position)
		{
		requireThat(position, "position").isNotNull();
		return new SourceRange(position, position.advanceColumn(1));
	}

	/**
	 * Creates a range from the start of the first range to the end of the second range.
	 *
	 * @param first the range providing the start position
	 * @param second the range providing the end position
	 * @return a {@code SourceRange} spanning both input ranges
	 * @throws NullPointerException if {@code first} or {@code second} is null
	 */
	public static SourceRange spanning(SourceRange first, SourceRange second)
		{
		requireThat(first, "first").isNotNull();
		requireThat(second, "second").isNotNull();
		return new SourceRange(first.start, second.end);
	}

	/**
	 * Checks if this range contains the specified position.
	 *
	 * @param position the position to check
	 * @return {@code true} if the position is within this range
	 * @throws NullPointerException if {@code position} is null
	 */
	public boolean contains(SourcePosition position)
		{
		requireThat(position, "position").isNotNull();
		return (position.line() > start.line() ||
			(position.line() == start.line() && position.column() >= start.column())) &&
			(position.line() < end.line() ||
			    (position.line() == end.line() && position.column() < end.column()));
	}

	/**
	 * Gets the length of this range in characters.
	 * Note: This is an approximation for single-line ranges.
	 *
	 * @return the character length for single-line ranges, -1 for multi-line ranges
	 */
	public int length()
		{
		if (start.line() == end.line())
			{
			return end.column() - start.column();
		}
		return -1; // Multi-line range length requires full source text
	}
}
