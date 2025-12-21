package io.github.cowwoc.styler.parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents an error encountered during parsing of Java source code.
 * <p>
 * Each error includes its location in the source and a descriptive message, enabling precise error
 * reporting and IDE integration for error highlighting.
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 *
 * @param position the 0-based character offset in source code where the error occurred
 * @param line     the 1-based line number where the error occurred
 * @param column   the 1-based column number where the error occurred
 * @param message  the human-readable error description
 */
public record ParseError(int position, int line, int column, String message)
{
	/**
	 * Creates a new parse error.
	 *
	 * @param position the 0-based character offset in source code where the error occurred
	 * @param line     the 1-based line number where the error occurred
	 * @param column   the 1-based column number where the error occurred
	 * @param message  the human-readable error description
	 * @throws IllegalArgumentException if {@code position} is negative, {@code line} is less than 1,
	 *                                  {@code column} is less than 1, or {@code message} is empty
	 */
	public ParseError
	{
		requireThat(position, "position").isGreaterThanOrEqualTo(0);
		requireThat(line, "line").isGreaterThanOrEqualTo(1);
		requireThat(column, "column").isGreaterThanOrEqualTo(1);
		requireThat(message, "message").isNotEmpty();
	}

	@Override
	public String toString()
	{
		return "ParseError[line=" + line + ", column=" + column + ", position=" + position +
			", message=\"" + message + "\"]";
	}
}
