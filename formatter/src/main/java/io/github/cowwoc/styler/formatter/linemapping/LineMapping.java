package io.github.cowwoc.styler.formatter.linemapping;

import io.github.cowwoc.styler.formatter.linemapping.internal.IdentityLineMapping;

import java.util.Optional;

/**
 * Maps line numbers between original source code and formatted output.
 * <p>
 * Line mapping tracks how formatting operations change line numbers. Given a line in
 * the original source, the mapping provides the corresponding line(s) in formatted output.
 * <p>
 * <b>Thread-safety</b>: Implementations must be immutable and thread-safe.
 */
public interface LineMapping
{
	/**
	 * Returns the formatted line number corresponding to an original source line.
	 * <p>
	 * When a single original line maps to multiple formatted lines, returns the first.
	 * When an original line was deleted, returns empty.
	 *
	 * @param originalLine the 1-based line number in the original source
	 * @return the corresponding 1-based line in formatted output, or empty if deleted
	 * @throws IllegalArgumentException if {@code originalLine} is not positive or exceeds line count
	 */
	Optional<Integer> toFormattedLine(int originalLine);

	/**
	 * Returns the original line number corresponding to a formatted line.
	 * <p>
	 * When a formatted line was created by splitting an original line, returns the original.
	 * When a formatted line is entirely new, returns empty.
	 *
	 * @param formattedLine the 1-based line number in the formatted output
	 * @return the corresponding 1-based line in original source, or empty if new
	 * @throws IllegalArgumentException if {@code formattedLine} is not positive or exceeds line count
	 */
	Optional<Integer> toOriginalLine(int formattedLine);

	/**
	 * Returns the total number of lines in the original source.
	 *
	 * @return the original line count
	 */
	int originalLineCount();

	/**
	 * Returns the total number of lines in the formatted output.
	 *
	 * @return the formatted line count
	 */
	int formattedLineCount();

	/**
	 * Returns the net line difference (formatted minus original).
	 *
	 * @return the line count difference
	 */
	default int lineDelta()
	{
		return formattedLineCount() - originalLineCount();
	}

	/**
	 * Creates an identity mapping where original and formatted lines are identical.
	 *
	 * @param lineCount the number of lines in both original and formatted content
	 * @return an identity line mapping
	 * @throws IllegalArgumentException if {@code lineCount} is negative
	 */
	static LineMapping identity(int lineCount)
	{
		return new IdentityLineMapping(lineCount);
	}
}
