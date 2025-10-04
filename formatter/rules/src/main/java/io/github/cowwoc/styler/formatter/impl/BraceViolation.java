package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.SourceRange;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Immutable record representing a brace formatting violation.
 *
 * <p>This record captures all information needed to report and fix a brace style violation, including
 * the location, type, expected placement, and actual placement of the problematic brace.
 *
 * <p><strong>Thread Safety:</strong> This class is immutable and thread-safe.
 *
 * @param context the brace context where violation occurred, never {@code null}
 * @param violationType the type of violation ({@code "opening"} or {@code "closing"}), never {@code null}
 * @param expectedStyle description of expected brace placement, never {@code null}
 * @param actualStyle description of actual brace placement, never {@code null}
 * @param violationRange the source range of the violating brace, never {@code null}
 */
public record BraceViolation(
	BraceContext context,
	String violationType,
	String expectedStyle,
	String actualStyle,
	SourceRange violationRange)
{
	/**
	 * Compact constructor with validation.
	 *
	 * @throws NullPointerException if any parameter is {@code null}
	 * @throws IllegalArgumentException if {@code violationType} is not {@code "opening"} or {@code "closing"}
	 */
	public BraceViolation
	{
		requireThat(context, "context").isNotNull();
		requireThat(violationType, "violationType").isNotNull();
		requireThat(expectedStyle, "expectedStyle").isNotNull();
		requireThat(actualStyle, "actualStyle").isNotNull();
		requireThat(violationRange, "violationRange").isNotNull();

		if (!violationType.equals("opening") && !violationType.equals("closing"))
		{
			throw new IllegalArgumentException(
				"violationType must be 'opening' or 'closing', got: " + violationType);
		}
	}

	/**
	 * Returns whether this violation is for an opening brace.
	 *
	 * @return {@code true} if opening brace violation, {@code false} if closing brace violation
	 */
	public boolean isOpeningBraceViolation()
	{
		return violationType.equals("opening");
	}

	/**
	 * Returns whether this violation is for a closing brace.
	 *
	 * @return {@code true} if closing brace violation, {@code false} if opening brace violation
	 */
	public boolean isClosingBraceViolation()
	{
		return violationType.equals("closing");
	}

	/**
	 * Returns the line number where the violation occurred.
	 *
	 * @return line number (1-based)
	 */
	public int getViolationLine()
	{
		return violationRange.start().line();
	}

	/**
	 * Returns the column number where the violation occurred.
	 *
	 * @return column number (1-based)
	 */
	public int getViolationColumn()
	{
		return violationRange.start().column();
	}

	/**
	 * Returns a human-readable description of this violation.
	 *
	 * @return violation description including location, type, expected and actual placement
	 */
	public String getDescription()
	{
		return String.format("%s brace at line %d, column %d: expected %s, found %s",
			violationType.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + violationType.substring(1),
			getViolationLine(),
			getViolationColumn(),
			expectedStyle,
			actualStyle);
	}
}
