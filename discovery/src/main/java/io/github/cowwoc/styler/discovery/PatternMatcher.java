package io.github.cowwoc.styler.discovery;

import java.nio.file.Path;

/**
 * Matches paths against glob patterns.
 * <p>
 * Supports standard glob syntax: *, **, ?, [abc], {a,b,c}
 *
 * <b>Thread-safety</b>: This interface is thread-safe. Implementations must be immutable.
 */
public interface PatternMatcher
{
	/**
	 * Tests if the path matches this pattern.
	 *
	 * @param path the path to test (relative to root)
	 * @return true if path matches the pattern
	 * @throws NullPointerException if {@code path} is {@code null}
	 */
	boolean matches(Path path);

	/**
	 * Returns the original pattern string.
	 *
	 * @return the pattern string
	 */
	String pattern();
}
