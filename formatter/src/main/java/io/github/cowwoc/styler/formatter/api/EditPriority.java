package io.github.cowwoc.styler.formatter.api;

/**
 * Defines priority levels for text edits to enable conflict resolution.
 * <p>
 * When multiple formatting rules generate overlapping text edits, the priority
 * determines which edit should be applied. Higher priority edits take precedence
 * over lower priority edits.
 */
public enum EditPriority
{
	/**
	 * Low priority edits for cosmetic changes that can be safely discarded.
	 * <p>
	 * Examples: optional whitespace adjustments, style preferences
	 */
	LOW(1),

	/**
	 * Normal priority edits for standard formatting operations.
	 * <p>
	 * Examples: indentation fixes, brace placement, line length adjustments
	 */
	NORMAL(5),

	/**
	 * High priority edits for important structural changes.
	 * <p>
	 * Examples: import organization, method ordering, class structure
	 */
	HIGH(8),

	/**
	 * Critical priority edits that must not be overridden.
	 * <p>
	 * Examples: syntax error fixes, compilation requirement changes
	 */
	CRITICAL(10);

	private final int numericValue;

	/**
	 * Creates a new edit priority with the specified numeric value.
	 *
	 * @param numericValue the numeric priority value (higher = more important)
	 */
	EditPriority(int numericValue)
	{
		this.numericValue = numericValue;
	}

	/**
	 * Returns the numeric priority value.
	 * <p>
	 * Higher values indicate higher priority. This enables fine-grained
	 * priority comparison between different rules and edit types.
	 *
	 * @return the numeric priority value
	 */
	public int getNumericValue()
	{
		return numericValue;
	}


	/**
	 * Returns whether this priority is higher than another priority.
	 *
	 * @param other the other priority to compare with, never {@code null}
	 * @return {@code true} if this priority is higher, {@code false} otherwise
	 */
	public boolean isHigherThan(EditPriority other)
	{
		return this.numericValue > other.numericValue;
	}

	/**
	 * Returns whether this priority is lower than another priority.
	 *
	 * @param other the other priority to compare with, never {@code null}
	 * @return {@code true} if this priority is lower, {@code false} otherwise
	 */
	public boolean isLowerThan(EditPriority other)
	{
		return this.numericValue < other.numericValue;
	}
}