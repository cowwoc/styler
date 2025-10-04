package io.github.cowwoc.styler.formatter.api.conflict;

/**
 * Severity classification for conflicts between formatting rule modifications.
 * <p>
 * Severity helps prioritize resolution strategies and error reporting:
 * <ul>
 * <li>MINOR: Both modifications affect different aspects (e.g., whitespace vs comments)</li>
 * <li>MODERATE: Modifications affect the same aspect but may be mergeable</li>
 * <li>SEVERE: Direct conflict requiring resolution strategy intervention</li>
 * </ul>
 */
public enum ConflictSeverity
{
	/**
	 * Minor conflict: modifications affect different aspects of the same AST node.
	 * <p>
	 * Example: One rule modifies whitespace, another modifies comments.
	 * Generally safe to merge.
	 */
	MINOR,

	/**
	 * Moderate conflict: modifications affect related aspects.
	 * <p>
	 * Example: One rule changes indentation, another changes line breaks.
	 * May be mergeable depending on context.
	 */
	MODERATE,

	/**
	 * Severe conflict: direct incompatible modifications.
	 * <p>
	 * Example: Two rules both want to change the same whitespace differently.
	 * Requires explicit resolution strategy.
	 */
	SEVERE
}
