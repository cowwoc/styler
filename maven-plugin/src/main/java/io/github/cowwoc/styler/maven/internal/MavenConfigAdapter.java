package io.github.cowwoc.styler.maven.internal;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapts Maven plugin include/exclude patterns to path matching.
 * <p>
 * This class converts Ant-style glob patterns (commonly used in Maven configurations)
 * to Java NIO {@link PathMatcher} instances for efficient file filtering.
 * <p>
 * <b>Pattern Syntax</b>:
 * <ul>
 *   <li>{@code *} matches any sequence of characters within a path segment</li>
 *   <li>{@code **} matches any sequence of path segments</li>
 *   <li>{@code ?} matches a single character</li>
 * </ul>
 * <p>
 * <b>Example Patterns</b>:
 * <ul>
 *   <li>{@code **\/*.java} - All Java files in any directory</li>
 *   <li>{@code src/main/**} - All files under src/main</li>
 *   <li>{@code **\/*Test.java} - All test files ending with Test.java</li>
 * </ul>
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe. Instances are immutable after construction.
 */
public final class MavenConfigAdapter
{
	private final List<PathMatcher> includeMatchers;
	private final List<PathMatcher> excludeMatchers;

	/**
	 * Creates a new adapter with the specified include and exclude patterns.
	 *
	 * @param includes list of patterns for files to include
	 * @param excludes list of patterns for files to exclude
	 * @throws NullPointerException if any of the arguments are {@code null}
	 */
	public MavenConfigAdapter(List<String> includes, List<String> excludes)
	{
		requireThat(includes, "includes").isNotNull();
		requireThat(excludes, "excludes").isNotNull();

		this.includeMatchers = compilePatterns(includes);
		this.excludeMatchers = compilePatterns(excludes);
	}

	/**
	 * Compiles a list of glob patterns into PathMatcher instances.
	 *
	 * @param patterns the patterns to compile
	 * @return list of compiled PathMatcher instances
	 */
	private List<PathMatcher> compilePatterns(List<String> patterns)
	{
		List<PathMatcher> matchers = new ArrayList<>();
		for (String pattern : patterns)
		{
			// Convert to glob syntax expected by FileSystems
			String globPattern = "glob:" + pattern;
			matchers.add(FileSystems.getDefault().getPathMatcher(globPattern));
		}
		return List.copyOf(matchers);
	}

	/**
	 * Determines whether a file matches the configured include/exclude patterns.
	 * <p>
	 * A file matches if:
	 * <ul>
	 *   <li>It matches at least one include pattern, AND</li>
	 *   <li>It does not match any exclude pattern</li>
	 * </ul>
	 * <p>
	 * The file path is evaluated relative to the base directory for pattern matching.
	 *
	 * @param baseDir the base directory for relative path calculation
	 * @param file    the file to check
	 * @return {@code true} if the file should be processed, {@code false} otherwise
	 * @throws NullPointerException if any of the arguments are {@code null}
	 */
	public boolean matches(Path baseDir, Path file)
	{
		requireThat(baseDir, "baseDir").isNotNull();
		requireThat(file, "file").isNotNull();

		// Calculate relative path for pattern matching
		Path relativePath = baseDir.relativize(file);

		// Check if any include pattern matches
		boolean included = false;
		for (PathMatcher includeMatcher : includeMatchers)
		{
			if (includeMatcher.matches(relativePath) || includeMatcher.matches(file.getFileName()))
			{
				included = true;
				break;
			}
		}

		if (!included)
		{
			return false;
		}

		// Check if any exclude pattern matches
		for (PathMatcher excludeMatcher : excludeMatchers)
		{
			if (excludeMatcher.matches(relativePath) || excludeMatcher.matches(file.getFileName()))
			{
				return false;
			}
		}

		return true;
	}
}
