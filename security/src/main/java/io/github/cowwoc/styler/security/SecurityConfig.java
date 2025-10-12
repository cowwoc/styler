package io.github.cowwoc.styler.security;

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
	long executionTimeoutMs,
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
	 * @param executionTimeoutMs maximum execution time per file in milliseconds (must be positive)
	 * @param maxRecursionDepth maximum recursion depth allowed (must be positive)
	 * @throws IllegalArgumentException if any limit is non-positive
	 */
	public SecurityConfig
	{
		requireThat(maxFileSizeBytes, "maxFileSizeBytes").isPositive();
		requireThat(maxHeapBytes, "maxHeapBytes").isPositive();
		requireThat(executionTimeoutMs, "executionTimeoutMs").isPositive();
		requireThat(maxRecursionDepth, "maxRecursionDepth").isPositive();
	}

	/**
	 * Builder for constructing SecurityConfig instances with default values.
	 */
	public static class Builder
	{
		private long maxFileSizeBytes = 10 * 1024 * 1024; // 10MB
		private long maxHeapBytes = 512 * 1024 * 1024; // 512MB
		private long executionTimeoutMs = 30_000; // 30 seconds
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
		 * @param executionTimeoutMs timeout in milliseconds (must be positive)
		 * @return this builder
		 * @throws IllegalArgumentException if timeout is non-positive
		 */
		public Builder executionTimeoutMs(long executionTimeoutMs)
		{
			requireThat(executionTimeoutMs, "executionTimeoutMs").isPositive();
			this.executionTimeoutMs = executionTimeoutMs;
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
			return new SecurityConfig(maxFileSizeBytes, maxHeapBytes, executionTimeoutMs,
				maxRecursionDepth);
		}
	}
}
