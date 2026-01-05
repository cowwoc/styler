package io.github.cowwoc.styler.discovery.internal;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import io.github.cowwoc.styler.discovery.PatternMatcher;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

/**
 * Glob pattern matching implementation using Java's PathMatcher.
 * <p>
 * Supports standard glob patterns.
 * <p>
 * This implementation handles edge cases where the double-wildcard glob
 * does not match zero directories in standard Java PathMatcher.
 *
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class GlobPatternMatcher implements PatternMatcher
{
	private final String patternString;
	private final List<PathMatcher> pathMatchers;

	/**
	 * Creates a glob pattern matcher.
	 *
	 * @param pattern the glob pattern (non-null, non-empty)
	 * @throws NullPointerException if {@code pattern} is {@code null}
	 * @throws IllegalArgumentException if {@code pattern} is empty or invalid
	 */
	public GlobPatternMatcher(String pattern)
	{
		requireThat(pattern, "pattern").isNotBlank();
		this.patternString = pattern;
		this.pathMatchers = createPatternVariations(pattern);
	}

	/**
	 * Creates pattern variations to handle double-wildcard edge cases.
	 * <p>
	 * Java's PathMatcher with glob syntax has quirks where the double-wildcard
	 * doesn't always match zero directories. This method creates alternative
	 * patterns to handle these edge cases.
	 *
	 * @param pattern the original glob pattern
	 * @return list of PathMatchers to try (original pattern plus variations)
	 */
	private List<PathMatcher> createPatternVariations(String pattern)
	{
		List<PathMatcher> matchers = new ArrayList<>();

		// Always include the original pattern
		matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + pattern));

		// Handle ** at the start: **/file should match file at root
		if (pattern.startsWith("**/"))
		{
			String withoutLeadingDoubleWildcard = pattern.substring(3);
			matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + withoutLeadingDoubleWildcard));
		}

		// Handle ** in the middle: dir/**/*.ext should match dir/*.ext (zero subdirs)
		int doubleWildcardPos = pattern.indexOf("/**/");
		if (doubleWildcardPos >= 0)
		{
			String beforeDoubleWildcard = pattern.substring(0, doubleWildcardPos);
			String afterDoubleWildcard = pattern.substring(doubleWildcardPos + 4);
			String collapsedPattern = beforeDoubleWildcard + "/" + afterDoubleWildcard;
			matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + collapsedPattern));
		}

		return matchers;
	}

	@Override
	public boolean matches(Path path)
	{
		requireThat(path, "path").isNotNull();

		// Try all pattern variations - if any matches, return true
		for (PathMatcher matcher : pathMatchers)
			if (matcher.matches(path))
				return true;

		return false;
	}

	@Override
	public String pattern()
	{
		return patternString;
	}
}
