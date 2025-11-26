package io.github.cowwoc.styler.formatter;

/**
 * Severity levels for formatting violations.
 * Ordered from most severe (ERROR) to least severe (INFO).
 */
public enum ViolationSeverity
{
	/**
	 * Critical issue that must be fixed.
	 * Examples: syntax-breaking changes, semantic alterations.
	 */
	ERROR,

	/**
	 * Issue that should be fixed but does not break the code.
	 * Examples: line length violations, improper indentation.
	 */
	WARNING,

	/**
	 * Informational suggestion for improvement.
	 * Examples: style preferences, optional enhancements.
	 */
	INFO
}
