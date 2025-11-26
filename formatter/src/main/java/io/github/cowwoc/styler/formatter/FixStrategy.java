package io.github.cowwoc.styler.formatter;

/**
 * A suggested fix for a formatting violation.
 * Implementations must be immutable and thread-safe.
 */
public interface FixStrategy
{
	/**
	 * Returns a human-readable description of this fix.
	 *
	 * @return the fix description
	 */
	String description();

	/**
	 * Checks if this fix can be applied automatically.
	 * Returns false if the fix requires human review or may alter semantics.
	 *
	 * @return true if auto-applicable, false if manual review required
	 */
	boolean isAutoApplicable();

	/**
	 * Returns the replacement text for the violation range.
	 *
	 * @return an empty string if the violation should be deleted
	 */
	String replacementText();

	/**
	 * Returns the start position (character offset) for the replacement.
	 * The value is non-negative.
	 *
	 * @return the start position
	 */
	int replacementStart();

	/**
	 * Returns the end position (character offset) for the replacement.
	 * The value is greater than or equal to the start position.
	 *
	 * @return the end position
	 */
	int replacementEnd();
}
