package io.github.cowwoc.styler.discovery;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents a single rule from a .gitignore file.
 *
 * @param pattern the pattern string (non-null, non-empty)
 * @param negation true if this is a negation rule (starts with !)
 * @param directoryOnly true if this only matches directories (ends with /)
 * @param anchored true if pattern is anchored to gitignore location (contains /)
 * @param lineNumber line number in source .gitignore file (for debugging)
 */
public record GitignoreRule(
	String pattern,
	boolean negation,
	boolean directoryOnly,
	boolean anchored,
	int lineNumber)
{
	/**
	 * Compact constructor for validation.
	 *
	 * @param pattern the pattern string (must not be empty)
	 * @param negation whether this is a negation rule
	 * @param directoryOnly whether this only matches directories
	 * @param anchored whether pattern is anchored to location
	 * @param lineNumber line number in .gitignore file
	 * @throws NullPointerException if {@code pattern} is {@code null}
	 * @throws IllegalArgumentException if {@code pattern} is empty or {@code lineNumber} is negative
	 */
	public GitignoreRule
	{
		requireThat(pattern, "pattern").isNotBlank();
		requireThat(lineNumber, "lineNumber").isNotNegative();
	}
}
