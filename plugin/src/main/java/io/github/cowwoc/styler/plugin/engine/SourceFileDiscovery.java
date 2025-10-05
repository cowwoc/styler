package io.github.cowwoc.styler.plugin.engine;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Discovers Java source files in project directories matching include/exclude patterns.
 * Uses Java NIO PathMatcher with glob patterns for efficient file filtering.
 * Thread-safe and stateless for Maven parallel builds.
 *
 * <p>Example usage:
 * <pre>
 * SourceFileDiscovery discovery = new SourceFileDiscovery();
 * List&lt;Path&gt; files = discovery.discoverFiles(
 *     Paths.get("src/main/java"),
 *     List.of("**\/*.java"),
 *     List.of("**\/generated\/**")
 * );
 * </pre>
 */
public final class SourceFileDiscovery
{
	/**
	 * Discovers Java source files in specified directory matching patterns.
	 * Recursively traverses directory tree using {@code Files.walkFileTree()}.
	 * Files matching any include pattern AND not matching any exclude pattern are returned.
	 *
	 * @param sourceDirectory base directory to search (e.g., src/main/java)
	 * @param includes inclusion patterns (Ant-style globs, e.g., "**\/*.java")
	 * @param excludes exclusion patterns (Ant-style globs, e.g., "**\/generated\/**")
	 * @return list of discovered source file paths (absolute paths, immutable)
	 * @throws IOException if directory traversal fails
	 * @throws IllegalArgumentException if {@code sourceDirectory}, {@code includes}, or {@code excludes} are null
	 */
	public List<Path> discoverFiles(Path sourceDirectory, List<String> includes, List<String> excludes)
		throws IOException
	{
		Objects.requireNonNull(sourceDirectory, "sourceDirectory cannot be null");
		Objects.requireNonNull(includes, "includes cannot be null");
		Objects.requireNonNull(excludes, "excludes cannot be null");

		// Non-existent directory returns empty list (not an error condition)
		if (!Files.exists(sourceDirectory))
			return List.of();

		List<PathMatcher> includeMatchers = createMatchers(includes);
		List<PathMatcher> excludeMatchers = createMatchers(excludes);
		List<Path> discoveredFiles = new ArrayList<>();

		Files.walk(sourceDirectory).
			filter(Files::isRegularFile).
			filter(file -> matchesAny(file, includeMatchers)).
			filter(file -> !matchesAny(file, excludeMatchers)).
			forEach(discoveredFiles::add);

		return List.copyOf(discoveredFiles); // Immutable result
	}

	/**
	 * Creates PathMatcher instances from glob patterns.
	 * Uses default file system's glob pattern syntax.
	 *
	 * @param patterns glob patterns (e.g., "**\/*.java")
	 * @return list of PathMatcher instances
	 */
	private List<PathMatcher> createMatchers(List<String> patterns)
	{
		FileSystem fs = FileSystems.getDefault();
		return patterns.stream().
			map(pattern -> fs.getPathMatcher("glob:" + pattern)).
			toList();
	}

	/**
	 * Checks if file matches any of the provided PathMatchers.
	 *
	 * @param file file path to test
	 * @param matchers PathMatcher instances to test against
	 * @return {@code true} if file matches any matcher, {@code false} otherwise
	 */
	private boolean matchesAny(Path file, List<PathMatcher> matchers)
	{
		return matchers.stream().
			anyMatch(matcher -> matcher.matches(file));
	}
}
