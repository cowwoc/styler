package io.github.cowwoc.styler.security;

import java.time.Duration;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Immutable security configuration defining resource limits and validation thresholds.
 * <p>
 * This configuration establishes boundaries for resource consumption to protect against
 * malicious inputs and resource exhaustion attacks in a single-user scenario.
 */
public record SecurityConfig(
	long maxFileSizeBytes,
	long maxHeapBytes,
	Duration executionTimeout,
	int maxRecursionDepth)
{
	/**
	 * Default configuration with recommended security limits.
	 */
	public static final SecurityConfig DEFAULT = new Builder().build();

	/**
	 * Creates a security configuration with validated limits.
	 *
	 * @param maxFileSizeBytes  maximum allowed file size in bytes (must be positive)
	 * @param maxHeapBytes      maximum allowed heap memory in bytes (must be positive)
	 * @param executionTimeout  maximum execution time per file (must be positive)
	 * @param maxRecursionDepth maximum recursion depth allowed (must be positive)
	 * @throws NullPointerException     if {@code executionTimeout} is null
	 * @throws IllegalArgumentException if any limit is non-positive
	 */
	public SecurityConfig
	{
		requireThat(maxFileSizeBytes, "maxFileSizeBytes").isPositive();
		requireThat(maxHeapBytes, "maxHeapBytes").isPositive();
		requireThat(executionTimeout, "executionTimeout").isNotNull();
		requireThat(executionTimeout.toMillis(), "executionTimeout").isPositive();
		requireThat(maxRecursionDepth, "maxRecursionDepth").isPositive();
	}

	/**
	 * Builder for constructing SecurityConfig instances with default values.
	 */
	public static class Builder
	{
		private long maxFileSizeBytes = 10 * 1024 * 1024; // 10MB
		private long maxHeapBytes = 512 * 1024 * 1024; // 512MB
		private Duration executionTimeout = Duration.ofSeconds(30);
		private int maxRecursionDepth = 1000;

		/**
		 * Sets the maximum allowed file size.
		 *
		 * @param maxFileSizeBytes maximum file size in bytes (must be positive)
		 * @return this builder
		 * @throws IllegalArgumentException if size is non-positive
		 */
		public Builder maxFileSizeBytes(long maxFileSizeBytes)
		{
			requireThat(maxFileSizeBytes, "maxFileSizeBytes").isPositive();
			this.maxFileSizeBytes = maxFileSizeBytes;
			return this;
		}

		/**
		 * Sets the maximum allowed heap memory.
		 *
		 * @param maxHeapBytes maximum heap size in bytes (must be positive)
		 * @return this builder
		 * @throws IllegalArgumentException if size is non-positive
		 */
		public Builder maxHeapBytes(long maxHeapBytes)
		{
			requireThat(maxHeapBytes, "maxHeapBytes").isPositive();
			this.maxHeapBytes = maxHeapBytes;
			return this;
		}

		/**
		 * Sets the maximum execution timeout per file.
		 *
		 * @param executionTimeout the timeout duration (must be positive)
		 * @return this builder
		 * @throws NullPointerException     if {@code executionTimeout} is null
		 * @throws IllegalArgumentException if timeout is non-positive
		 */
		public Builder executionTimeout(Duration executionTimeout)
		{
			requireThat(executionTimeout, "executionTimeout").isNotNull();
			requireThat(executionTimeout.toMillis(), "executionTimeout").isPositive();
			this.executionTimeout = executionTimeout;
			return this;
		}

		/**
		 * Sets the maximum recursion depth.
		 *
		 * @param maxRecursionDepth maximum depth (must be positive)
		 * @return this builder
		 * @throws IllegalArgumentException if depth is non-positive
		 */
		public Builder maxRecursionDepth(int maxRecursionDepth)
		{
			requireThat(maxRecursionDepth, "maxRecursionDepth").isPositive();
			this.maxRecursionDepth = maxRecursionDepth;
			return this;
		}

		/**
		 * Builds an immutable SecurityConfig with current settings.
		 *
		 * @return new SecurityConfig instance
		 */
		public SecurityConfig build()
		{
			return new SecurityConfig(maxFileSizeBytes, maxHeapBytes, executionTimeout,
				maxRecursionDepth);
		}
	}
}
