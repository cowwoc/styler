package io.github.cowwoc.styler.discovery;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.util.List;

/**
 * Configuration for file discovery operations.
 *
 * @param includePatterns glob patterns for files to include (empty = all .java files)
 * @param excludePatterns glob patterns for files to exclude
 * @param respectGitignore whether to honor .gitignore rules
 * @param followSymlinks whether to follow symbolic links
 * @param maxDepth maximum directory depth ({@code Integer.MAX_VALUE} for unlimited)
 */
public record DiscoveryConfiguration(
	List<String> includePatterns,
	List<String> excludePatterns,
	boolean respectGitignore,
	boolean followSymlinks,
	int maxDepth)
{
	/**
	 * Default configuration with no patterns, .gitignore respected, symlinks not followed.
	 */
	public static final DiscoveryConfiguration DEFAULT = new Builder().build();

	/**
	 * Compact constructor for validation.
	 *
	 * @param includePatterns list of include patterns (non-null)
	 * @param excludePatterns list of exclude patterns (non-null)
	 * @param respectGitignore whether to respect .gitignore
	 * @param followSymlinks whether to follow symlinks
	 * @param maxDepth maximum depth (must be positive)
	 * @throws NullPointerException if any list parameter is {@code null}
	 * @throws IllegalArgumentException if {@code maxDepth} is not positive
	 */
	public DiscoveryConfiguration
	{
		requireThat(includePatterns, "includePatterns").isNotNull();
		requireThat(excludePatterns, "excludePatterns").isNotNull();
		requireThat(maxDepth, "maxDepth").isPositive();

		includePatterns = List.copyOf(includePatterns);
		excludePatterns = List.copyOf(excludePatterns);
	}

	/**
	 * Builder for fluent configuration of discovery options.
	 */
	public static final class Builder
	{
		private List<String> includePatterns = List.of();
		private List<String> excludePatterns = List.of();
		private boolean respectGitignore = true;
		private boolean followSymlinks = false;
		private int maxDepth = 100;

		/**
		 * Creates a builder with default values.
		 */
		public Builder()
		{
		}

		/**
		 * Sets the include patterns.
		 *
		 * @param patterns glob patterns for files to include
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code patterns} is {@code null}
		 */
		public Builder includePatterns(List<String> patterns)
		{
			requireThat(patterns, "patterns").isNotNull();
			this.includePatterns = List.copyOf(patterns);
			return this;
		}

		/**
		 * Sets the exclude patterns.
		 *
		 * @param patterns glob patterns for files to exclude
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code patterns} is {@code null}
		 */
		public Builder excludePatterns(List<String> patterns)
		{
			requireThat(patterns, "patterns").isNotNull();
			this.excludePatterns = List.copyOf(patterns);
			return this;
		}

		/**
		 * Sets whether to respect .gitignore files.
		 *
		 * @param respect true to honor .gitignore rules
		 * @return this builder for method chaining
		 */
		public Builder respectGitignore(boolean respect)
		{
			this.respectGitignore = respect;
			return this;
		}

		/**
		 * Sets whether to follow symbolic links.
		 *
		 * @param follow true to follow symlinks (security risk)
		 * @return this builder for method chaining
		 */
		public Builder followSymlinks(boolean follow)
		{
			this.followSymlinks = follow;
			return this;
		}

		/**
		 * Sets the maximum traversal depth.
		 *
		 * @param depth maximum depth (must be positive, use {@code Integer.MAX_VALUE} for unlimited)
		 * @return this builder for method chaining
		 * @throws IllegalArgumentException if {@code depth} is not positive
		 */
		public Builder maxDepth(int depth)
		{
			requireThat(depth, "depth").isPositive();
			this.maxDepth = depth;
			return this;
		}

		/**
		 * Builds the configuration.
		 *
		 * @return immutable configuration object
		 */
		public DiscoveryConfiguration build()
		{
			return new DiscoveryConfiguration(
				includePatterns,
				excludePatterns,
				respectGitignore,
				followSymlinks,
				maxDepth);
		}
	}
}
