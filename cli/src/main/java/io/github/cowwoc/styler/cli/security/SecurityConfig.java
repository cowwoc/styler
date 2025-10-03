package io.github.cowwoc.styler.cli.security;

import java.util.Objects;
import java.util.Set;

/**
 * Immutable configuration for CLI security controls including file size limits,
 * memory bounds, execution timeouts, and path validation settings.
 *
 * <p>This configuration follows the single-user code formatting tool security model,
 * prioritizing resource exhaustion prevention and system stability over
 * multi-user security concerns.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Use defaults (recommended for most cases)
 * SecurityConfig config = SecurityConfig.defaults();
 *
 * // Customize for specific needs
 * SecurityConfig custom = SecurityConfig.builder()
 *     .maxFileSizeBytes(100 * 1024 * 1024)  // 100MB
 *     .maxMemoryBytes(1024 * 1024 * 1024)   // 1GB
 *     .build();
 * }</pre>
 *
 * @param maxFileSizeBytes the maximum file size in bytes
 * @param allowedExtensions the set of allowed file extensions
 * @param maxMemoryBytes the maximum memory usage in bytes
 * @param timeoutMillis the timeout per file in milliseconds
 * @param maxRecursionDepth the maximum recursion depth
 * @param warnRecursionDepth the recursion depth at which to warn
 * @param maxTempFiles the maximum number of temporary files
 * @param maxTempDiskBytes the maximum temporary disk usage in bytes
 * @see SecurityManager
 */
public record SecurityConfig(
	long maxFileSizeBytes,
	Set<String> allowedExtensions,
	long maxMemoryBytes,
	long timeoutMillis,
	int maxRecursionDepth,
	int warnRecursionDepth,
	int maxTempFiles,
	long maxTempDiskBytes)
{
	/** Default maximum file size: 50 MB. */
	private static final long DEFAULT_MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024;

	/** Default maximum memory usage: 512 MB. */
	private static final long DEFAULT_MAX_MEMORY_BYTES = 512 * 1024 * 1024;

	/** Default timeout per file: 30 seconds. */
	private static final long DEFAULT_TIMEOUT_MILLIS = 30_000;

	/** Default maximum recursion depth: 1000 levels. */
	private static final int DEFAULT_MAX_RECURSION_DEPTH = 1000;

	/** Default warning recursion depth: 500 levels. */
	private static final int DEFAULT_WARN_RECURSION_DEPTH = 500;

	/** Default maximum temporary files: 1000 files. */
	private static final int DEFAULT_MAX_TEMP_FILES = 1000;

	/**
	 * Default maximum temporary disk usage: {@code 1} GB.
	 */
	private static final long DEFAULT_MAX_TEMP_DISK_BYTES = 1024 * 1024 * 1024;

	/**
	 * Compact constructor with validation.
	 *
	 * @throws IllegalArgumentException if any parameter is invalid
	 * @throws NullPointerException if allowedExtensions is {@code null}
	 */
	public SecurityConfig
	{
		Objects.requireNonNull(allowedExtensions, "allowedExtensions must not be null");

		if (maxFileSizeBytes <= 0)
		{
			throw new IllegalArgumentException(
				"maxFileSizeBytes must be positive: " + maxFileSizeBytes);
		}

		if (maxMemoryBytes <= 0)
		{
			throw new IllegalArgumentException(
				"maxMemoryBytes must be positive: " + maxMemoryBytes);
		}

		if (timeoutMillis <= 0)
		{
			throw new IllegalArgumentException(
				"timeoutMillis must be positive: " + timeoutMillis);
		}

		if (allowedExtensions.isEmpty())
		{
			throw new IllegalArgumentException(
				"allowedExtensions must not be empty");
		}

		if (maxRecursionDepth <= 0)
		{
			throw new IllegalArgumentException(
				"maxRecursionDepth must be positive: " + maxRecursionDepth);
		}

		if (warnRecursionDepth <= 0 || warnRecursionDepth >= maxRecursionDepth)
		{
			throw new IllegalArgumentException(
				"warnRecursionDepth must be positive and less than maxRecursionDepth: " +
				warnRecursionDepth + " (max: " + maxRecursionDepth + ")");
		}

		if (maxTempFiles <= 0)
		{
			throw new IllegalArgumentException(
				"maxTempFiles must be positive: " + maxTempFiles);
		}

		if (maxTempDiskBytes <= 0)
		{
			throw new IllegalArgumentException(
				"maxTempDiskBytes must be positive: " + maxTempDiskBytes);
		}

		// Defensive copy to ensure immutability
		allowedExtensions = Set.copyOf(allowedExtensions);
	}

	/**
	 * Returns a configuration with recommended default values suitable for
	 * most Java code formatting scenarios.
	 *
	 * <p>Defaults:
	 * <ul>
	 *   <li>Max file size: 50 MB</li>
	 *   <li>Max memory: 512 MB</li>
	 *   <li>Timeout: 30 seconds</li>
	 *   <li>Allowed extensions: .java only</li>
	 *   <li>Max recursion depth: 1000 levels</li>
	 *   <li>Warn recursion depth: 500 levels</li>
	 *   <li>Max temp files: 1000 files</li>
	 *   <li>Max temp disk: {@code 1} GB</li>
	 * </ul>
	 *
	 * @return configuration with default security settings
	 */
	public static SecurityConfig defaults()
	{
		return new SecurityConfig(
			DEFAULT_MAX_FILE_SIZE_BYTES,
			Set.of(".java"),
			DEFAULT_MAX_MEMORY_BYTES,
			DEFAULT_TIMEOUT_MILLIS,
			DEFAULT_MAX_RECURSION_DEPTH,
			DEFAULT_WARN_RECURSION_DEPTH,
			DEFAULT_MAX_TEMP_FILES,
			DEFAULT_MAX_TEMP_DISK_BYTES);
	}

	/**
	 * Creates a new builder for customizing security configuration.
	 *
	 * @return a new builder initialized with default values
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Builder for creating customized {@link SecurityConfig} instances.
	 */
	public static final class Builder
	{
		private long maxFileSizeBytes = DEFAULT_MAX_FILE_SIZE_BYTES;
		private Set<String> allowedExtensions = Set.of(".java");
		private long maxMemoryBytes = DEFAULT_MAX_MEMORY_BYTES;
		private long timeoutMillis = DEFAULT_TIMEOUT_MILLIS;
		private int maxRecursionDepth = DEFAULT_MAX_RECURSION_DEPTH;
		private int warnRecursionDepth = DEFAULT_WARN_RECURSION_DEPTH;
		private int maxTempFiles = DEFAULT_MAX_TEMP_FILES;
		private long maxTempDiskBytes = DEFAULT_MAX_TEMP_DISK_BYTES;

		private Builder()
		{
		}

		/**
		 * Sets the maximum file size in bytes.
		 *
		 * @param maxFileSizeBytes maximum file size, must be positive
		 * @return this builder
		 * @throws IllegalArgumentException if maxFileSizeBytes is not positive
		 */
		public Builder maxFileSizeBytes(long maxFileSizeBytes)
		{
			if (maxFileSizeBytes <= 0)
			{
				throw new IllegalArgumentException(
					"maxFileSizeBytes must be positive: " + maxFileSizeBytes);
			}
			this.maxFileSizeBytes = maxFileSizeBytes;
			return this;
		}

		/**
		 * Sets the allowed file extensions.
		 *
		 * @param allowedExtensions set of allowed extensions (e.g., ".java"), must not be {@code null} or empty
		 * @return this builder
		 * @throws NullPointerException if allowedExtensions is {@code null}
		 * @throws IllegalArgumentException if allowedExtensions is empty
		 */
		public Builder allowedExtensions(Set<String> allowedExtensions)
		{
			Objects.requireNonNull(allowedExtensions, "allowedExtensions must not be null");
			if (allowedExtensions.isEmpty())
			{
				throw new IllegalArgumentException("allowedExtensions must not be empty");
			}
			this.allowedExtensions = Set.copyOf(allowedExtensions);
			return this;
		}

		/**
		 * Sets the maximum memory usage in bytes.
		 *
		 * @param maxMemoryBytes maximum memory, must be positive
		 * @return this builder
		 * @throws IllegalArgumentException if maxMemoryBytes is not positive
		 */
		public Builder maxMemoryBytes(long maxMemoryBytes)
		{
			if (maxMemoryBytes <= 0)
			{
				throw new IllegalArgumentException(
					"maxMemoryBytes must be positive: " + maxMemoryBytes);
			}
			this.maxMemoryBytes = maxMemoryBytes;
			return this;
		}

		/**
		 * Sets the execution timeout in milliseconds.
		 *
		 * @param timeoutMillis timeout duration, must be positive
		 * @return this builder
		 * @throws IllegalArgumentException if timeoutMillis is not positive
		 */
		public Builder timeoutMillis(long timeoutMillis)
		{
			if (timeoutMillis <= 0)
			{
				throw new IllegalArgumentException(
					"timeoutMillis must be positive: " + timeoutMillis);
			}
			this.timeoutMillis = timeoutMillis;
			return this;
		}

		/**
		 * Sets the maximum recursion depth.
		 *
		 * @param maxRecursionDepth maximum recursion depth, must be positive
		 * @return this builder
		 * @throws IllegalArgumentException if maxRecursionDepth is not positive
		 */
		public Builder maxRecursionDepth(int maxRecursionDepth)
		{
			if (maxRecursionDepth <= 0)
			{
				throw new IllegalArgumentException(
					"maxRecursionDepth must be positive: " + maxRecursionDepth);
			}
			this.maxRecursionDepth = maxRecursionDepth;
			return this;
		}

		/**
		 * Sets the warning recursion depth threshold.
		 *
		 * @param warnRecursionDepth warning recursion depth, must be positive and less than max
		 * @return this builder
		 * @throws IllegalArgumentException if warnRecursionDepth is invalid
		 */
		public Builder warnRecursionDepth(int warnRecursionDepth)
		{
			if (warnRecursionDepth <= 0)
			{
				throw new IllegalArgumentException(
					"warnRecursionDepth must be positive: " + warnRecursionDepth);
			}
			this.warnRecursionDepth = warnRecursionDepth;
			return this;
		}

		/**
		 * Sets the maximum number of temporary files.
		 *
		 * @param maxTempFiles maximum temporary files, must be positive
		 * @return this builder
		 * @throws IllegalArgumentException if maxTempFiles is not positive
		 */
		public Builder maxTempFiles(int maxTempFiles)
		{
			if (maxTempFiles <= 0)
			{
				throw new IllegalArgumentException(
					"maxTempFiles must be positive: " + maxTempFiles);
			}
			this.maxTempFiles = maxTempFiles;
			return this;
		}

		/**
		 * Sets the maximum temporary disk usage in bytes.
		 *
		 * @param maxTempDiskBytes maximum temporary disk usage, must be positive
		 * @return this builder
		 * @throws IllegalArgumentException if maxTempDiskBytes is not positive
		 */
		public Builder maxTempDiskBytes(long maxTempDiskBytes)
		{
			if (maxTempDiskBytes <= 0)
			{
				throw new IllegalArgumentException(
					"maxTempDiskBytes must be positive: " + maxTempDiskBytes);
			}
			this.maxTempDiskBytes = maxTempDiskBytes;
			return this;
		}

		/**
		 * Builds the {@link SecurityConfig} instance with current settings.
		 *
		 * @return a new immutable SecurityConfig
		 */
		public SecurityConfig build()
		{
			return new SecurityConfig(
				maxFileSizeBytes,
				allowedExtensions,
				maxMemoryBytes,
				timeoutMillis,
				maxRecursionDepth,
				warnRecursionDepth,
				maxTempFiles,
				maxTempDiskBytes);
		}
	}
}