package io.github.cowwoc.styler.cli;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Filters files and directories based on include/exclude glob patterns.
 *
 * <p>This filter uses Java NIO {@link PathMatcher} for platform-agnostic glob pattern
 * matching. It supports:
 * <ul>
 *   <li>Include patterns (whitelist)</li>
 *   <li>Exclude patterns (blacklist)</li>
 *   <li>Early directory pruning for performance</li>
 *   <li>Precedence: exclude overrides include</li>
 * </ul>
 *
 * <p>This class is immutable and thread-safe.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Create filter with include/exclude patterns
 * FileFilter filter = FileFilter.builder()
 *     .includePattern("(all Java files)")
 *     .excludePattern("(generated directories)")
 *     .excludePattern("(test directories)")
 *     .build();
 *
 * boolean shouldProcess = filter.matches(Paths.get("src/main/java/Example.java"));
 * boolean shouldPrune = filter.shouldExcludeDirectory(Paths.get("target"));
 * }</pre>
 *
 * <h2>Pattern Syntax:</h2>
 * <ul>
 *   <li>{@code *} - matches any string (except directory separator)</li>
 *   <li>{@code **} - matches any string (including directory separators)</li>
 *   <li>{@code ?} - matches a single character</li>
 *   <li>{@code [abc]} - matches one of the characters</li>
 *   <li>{@code {a,b}} - matches one of the alternatives</li>
 * </ul>
 *
 * @see PathMatcher
 * @see Builder
 */
public final class FileFilter
{
	/** Maximum pattern length (prevents excessive pattern complexity). */
	private static final int MAX_PATTERN_LENGTH = 500;

	/** Maximum number of wildcards per pattern (prevents ReDoS). */
	private static final int MAX_WILDCARDS = 50;

	private final List<PathMatcher> includePatterns;
	private final List<PathMatcher> excludePatterns;

	/**
	 * Private constructor - use {@link Builder} to create instances.
	 *
	 * @param includePatterns compiled include patterns
	 * @param excludePatterns compiled exclude patterns
	 */
	private FileFilter(List<PathMatcher> includePatterns, List<PathMatcher> excludePatterns)
	{
		this.includePatterns = List.copyOf(includePatterns);
		this.excludePatterns = List.copyOf(excludePatterns);
	}

	/**
	 * Checks if the specified file matches the filter criteria.
	 *
	 * <p>Matching logic:
	 * <ol>
	 *   <li>If file matches any exclude pattern: returns {@code false}</li>
	 *   <li>If no include patterns specified: returns {@code true}</li>
	 *   <li>If file matches any include pattern: returns {@code true}</li>
	 *   <li>Otherwise: returns {@code false}</li>
	 * </ol>
	 *
	 * <p><strong>Precedence: exclude overrides include</strong>
	 *
	 * @param file the file to check, must not be {@code null}
	 * @return {@code true} if file matches filter criteria
	 * @throws NullPointerException if file is {@code null}
	 */
	public boolean matches(Path file)
	{
		Objects.requireNonNull(file, "file must not be null");

		// Exclude patterns have precedence
		for (PathMatcher excludePattern : excludePatterns)
		{
			if (excludePattern.matches(file))
			{
				return false;
			}
		}

		// If no include patterns, accept all (that aren't excluded)
		if (includePatterns.isEmpty())
		{
			return true;
		}

		// Check include patterns
		for (PathMatcher includePattern : includePatterns)
		{
			if (includePattern.matches(file))
			{
				return true;
			}
		}

		// Not included
		return false;
	}

	/**
	 * Checks if the specified directory should be excluded from traversal.
	 *
	 * <p>This enables early pruning optimization - if a directory is excluded,
	 * there's no need to traverse its contents.
	 *
	 * @param directory the directory to check, must not be {@code null}
	 * @return {@code true} if directory should be excluded
	 * @throws NullPointerException if directory is {@code null}
	 */
	public boolean shouldExcludeDirectory(Path directory)
	{
		Objects.requireNonNull(directory, "directory must not be null");

		for (PathMatcher excludePattern : excludePatterns)
		{
			if (excludePattern.matches(directory))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns {@code true} if this filter has any include patterns.
	 *
	 * @return {@code true} if include patterns are configured
	 */
	public boolean hasIncludePatterns()
	{
		return !includePatterns.isEmpty();
	}

	/**
	 * Returns {@code true} if this filter has any exclude patterns.
	 *
	 * @return {@code true} if exclude patterns are configured
	 */
	public boolean hasExcludePatterns()
	{
		return !excludePatterns.isEmpty();
	}

	/**
	 * Creates a new builder for constructing {@link FileFilter} instances.
	 *
	 * @return a new builder
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Builder for creating {@link FileFilter} instances with fluent API.
	 */
	public static final class Builder
	{
		private final List<PathMatcher> includePatterns = new ArrayList<>();
		private final List<PathMatcher> excludePatterns = new ArrayList<>();

		private Builder()
		{
		}

		/**
		 * Adds an include pattern (whitelist).
		 *
		 * <p>Files must match at least one include pattern to be accepted
		 * (unless no include patterns are specified, which accepts all files).
		 *
		 * @param pattern glob pattern (e.g., "**\/*.java"), must not be {@code null}
		 * @return this builder
		 * @throws NullPointerException if pattern is {@code null}
		 * @throws IllegalArgumentException if pattern is invalid
		 */
		public Builder includePattern(String pattern)
		{
			Objects.requireNonNull(pattern, "pattern must not be null");
			validatePattern(pattern);

			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
			includePatterns.add(matcher);
			return this;
		}

		/**
		 * Adds multiple include patterns.
		 *
		 * @param patterns glob patterns, must not be {@code null}
		 * @return this builder
		 * @throws NullPointerException if patterns is {@code null}
		 * @throws IllegalArgumentException if any pattern is invalid
		 */
		public Builder includePatterns(List<String> patterns)
		{
			Objects.requireNonNull(patterns, "patterns must not be null");
			for (String pattern : patterns)
			{
				includePattern(pattern);
			}
			return this;
		}

		/**
		 * Adds an exclude pattern (blacklist).
		 *
		 * <p>Files matching any exclude pattern are rejected, even if they
		 * match include patterns. Exclude has precedence over include.
		 *
		 * @param pattern glob pattern (e.g., "**\/target/**"), must not be {@code null}
		 * @return this builder
		 * @throws NullPointerException if pattern is {@code null}
		 * @throws IllegalArgumentException if pattern is invalid
		 */
		public Builder excludePattern(String pattern)
		{
			Objects.requireNonNull(pattern, "pattern must not be null");
			validatePattern(pattern);

			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
			excludePatterns.add(matcher);
			return this;
		}

		/**
		 * Adds multiple exclude patterns.
		 *
		 * @param patterns glob patterns, must not be {@code null}
		 * @return this builder
		 * @throws NullPointerException if patterns is {@code null}
		 * @throws IllegalArgumentException if any pattern is invalid
		 */
		public Builder excludePatterns(List<String> patterns)
		{
			Objects.requireNonNull(patterns, "patterns must not be null");
			for (String pattern : patterns)
			{
				excludePattern(pattern);
			}
			return this;
		}

		/**
		 * Validates a glob pattern for security and complexity constraints.
		 *
		 * @param pattern the pattern to validate
		 * @throws IllegalArgumentException if pattern is invalid
		 */
		private void validatePattern(String pattern)
		{
			if (pattern.isBlank())
			{
				throw new IllegalArgumentException("pattern must not be blank");
			}

			if (pattern.length() > MAX_PATTERN_LENGTH)
			{
				throw new IllegalArgumentException(
					"pattern exceeds maximum length of " + MAX_PATTERN_LENGTH +
					": " + pattern.length());
			}

			// Count wildcards (*, ?) to prevent ReDoS
			long wildcardCount = pattern.chars().filter(c -> c == '*' || c == '?').count();
			if (wildcardCount > MAX_WILDCARDS)
			{
				throw new IllegalArgumentException(
					"pattern exceeds maximum wildcard count of " + MAX_WILDCARDS +
					": " + wildcardCount);
			}
		}

		/**
		 * Builds the {@link FileFilter} with configured patterns.
		 *
		 * @return a new immutable FileFilter
		 */
		public FileFilter build()
		{
			return new FileFilter(includePatterns, excludePatterns);
		}
	}
}
