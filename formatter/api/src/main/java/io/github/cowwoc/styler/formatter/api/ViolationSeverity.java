package io.github.cowwoc.styler.formatter.api;

/**
 * Defines severity levels for formatting violations.
 * <p>
 * Severity levels help categorize violations by their importance and enable
 * different handling strategies (e.g., warnings vs errors, filtering, reporting).
 */
public enum ViolationSeverity
{
	/**
	 * Informational violations for minor style issues.
	 * <p>
	 * These violations indicate minor deviations from style guidelines that
	 * don't significantly impact code readability or maintainability.
	 * Examples: trailing whitespace, minor spacing inconsistencies
	 */
	INFO,

	/**
	 * Warning violations for style issues that should be addressed.
	 * <p>
	 * These violations indicate style issues that reduce code quality or
	 * readability but don't prevent compilation or execution.
	 * Examples: inconsistent indentation, line length violations
	 */
	WARNING,

	/**
	 * Error violations for serious style or structural issues.
	 * <p>
	 * These violations indicate significant problems that should be fixed
	 * promptly as they impact code maintainability or team standards.
	 * Examples: missing documentation, major style violations
	 */
	ERROR;

	/**
	 * Returns whether this severity level is at least as severe as another level.
	 *
	 * @param other the severity level to compare with, never {@code null}
	 * @return {@code true} if this severity is at least as severe as the other
	 */
	public boolean isAtLeast(ViolationSeverity other)
	{
		return this.ordinal() >= other.ordinal();
	}

	/**
	 * Returns whether this is an error level severity.
	 *
	 * @return {@code true} if this is ERROR severity, {@code false} otherwise
	 */
	public boolean isError()
	{
		return this == ERROR;
	}

	/**
	 * Returns whether this is a warning level severity.
	 *
	 * @return {@code true} if this is WARNING severity, {@code false} otherwise
	 */
	public boolean isWarning()
	{
		return this == WARNING;
	}

	/**
	 * Returns whether this is an info level severity.
	 *
	 * @return {@code true} if this is INFO severity, {@code false} otherwise
	 */
	public boolean isInfo()
	{
		return this == INFO;
	}
}