package io.github.cowwoc.styler.formatter.brace;

/**
 * Enumeration of supported brace placement styles.
 * <p>
 * <b>Thread-safety</b>: Enums are thread-safe (immutable).
 */
public enum BraceStyle
{
	/**
	 * K&R (Kernighan and Ritchie) style.
	 * Opening brace on same line as declaration, preceded by single space.
	 * Example: {@code public void method() {}}.
	 */
	SAME_LINE,

	/**
	 * Allman style (BSD style).
	 * Opening brace on new line, aligned with declaration start.
	 * <pre>
	 * public void method()
	 * {
	 * }
	 * </pre>
	 */
	NEW_LINE
}
