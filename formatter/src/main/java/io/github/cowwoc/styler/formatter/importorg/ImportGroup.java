package io.github.cowwoc.styler.formatter.importorg;

/**
 * Standard import groupings for organizing Java imports.
 * <p>
 * Imports are grouped by category: Java standard library, Java extensions,
 * third-party libraries, and project-specific imports.
 * <p>
 * <b>Thread-safety</b>: This enum is immutable and thread-safe.
 */
public enum ImportGroup
{
	/**
	 * Java standard library imports (java.*).
	 */
	JAVA("java\\..*"),

	/**
	 * Java extension imports (javax.*).
	 */
	JAVAX("javax\\..*"),

	/**
	 * Third-party library imports (not java, javax, or project).
	 * Acts as catch-all for non-matching imports.
	 */
	THIRD_PARTY(null),

	/**
	 * Project-specific imports (determined by configuration).
	 */
	PROJECT(null);

	/**
	 * Regex pattern for matching imports in this group.
	 * Null means this group is configured per-project or acts as catch-all.
	 */
	private final String pattern;

	/**
	 * Creates an import group with an optional pattern.
	 *
	 * @param pattern regex pattern for matching imports, or null if not used
	 */
	ImportGroup(String pattern)
	{
		this.pattern = pattern;
	}

	/**
	 * Returns the regex pattern for matching imports in this group.
	 *
	 * @return the pattern, or null if not pattern-based
	 */
	public String pattern()
	{
		return pattern;
	}
}
