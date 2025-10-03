package io.github.cowwoc.styler.parser;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Lightweight performance metrics collection for the parser.
 *
 * This uses atomic operations and minimal object allocation to avoid
 * impacting parser performance while still collecting valuable data
 * for evidence-based optimization decisions.
 *
 * Metrics can be completely disabled via system property for production use.
 */
public final class ParseMetrics
{
	private static final boolean METRICS_ENABLED =
		Boolean.parseBoolean(System.getProperty("styler.metrics.enabled", "false"));

	// Parse timing metrics
	private static final LongAdder TOTAL_PARSE_TIME_NANOS = new LongAdder();
	private static final LongAdder TOTAL_TOKENIZATION_TIME_NANOS = new LongAdder();
	private static final LongAdder TOTAL_FILES_PROCESSED = new LongAdder();

	// Memory metrics
	private static final LongAdder TOTAL_NODES_ALLOCATED = new LongAdder();
	private static final LongAdder TOTAL_TOKENS_CREATED = new LongAdder();
	private static final AtomicLong PEAK_MEMORY_USAGE = new AtomicLong(0);

	// Error metrics
	private static final LongAdder PARSE_ERRORS = new LongAdder();
	private static final LongAdder RECOVERED_ERRORS = new LongAdder();

	// File size distribution (for understanding workloads)
	private static final LongAdder SMALL_FILES = new LongAdder(); // < 1KB
	private static final LongAdder MEDIUM_FILES = new LongAdder(); // 1KB - 10KB
	private static final LongAdder LARGE_FILES = new LongAdder(); // > 10KB

	/**
	 * Records parsing time for a file.
	 * No-op if metrics are disabled.
	 *
	 * @param nanos the parsing time in nanoseconds
	 * @param fileSizeBytes the size of the parsed file in bytes
	 */
	public static void recordParseTime(long nanos, int fileSizeBytes)
{
		if (!METRICS_ENABLED) return;

		TOTAL_PARSE_TIME_NANOS.add(nanos);
		TOTAL_FILES_PROCESSED.increment();

		// Categorize by file size
		if (fileSizeBytes < 1024)
{
			SMALL_FILES.increment();
		}
		else if (fileSizeBytes < 10_240)
{
			MEDIUM_FILES.increment();
		}
		else
{
			LARGE_FILES.increment();
		}
	}

	/**
	 * Records tokenization time.
	 *
	 * @param nanos the tokenization time in nanoseconds
	 * @param tokenCount the number of tokens created
	 */
	public static void recordTokenizationTime(long nanos, int tokenCount)
{
		if (!METRICS_ENABLED) return;

		TOTAL_TOKENIZATION_TIME_NANOS.add(nanos);
		TOTAL_TOKENS_CREATED.add(tokenCount);
	}

	/**
	 * Records node allocation.
	 *
	 * @param nodeCount the number of nodes allocated
	 */
	public static void recordNodeAllocation(int nodeCount)
{
		if (!METRICS_ENABLED) return;
		TOTAL_NODES_ALLOCATED.add(nodeCount);
	}

	/**
	 * Records memory usage high-water mark.
	 *
	 * @param bytes the current memory usage in bytes
	 */
	public static void recordMemoryUsage(long bytes)
{
		if (!METRICS_ENABLED) return;
		PEAK_MEMORY_USAGE.accumulateAndGet(bytes, Long::max);
	}

	/**
	 * Records a parsing error.
	 *
	 * @param wasRecovered {@code true} if the error was recovered, {@code false} otherwise
	 */
	public static void recordParseError(boolean wasRecovered)
{
		if (!METRICS_ENABLED) return;

		PARSE_ERRORS.increment();
		if (wasRecovered)
{
			RECOVERED_ERRORS.increment();
		}
	}

	/**
	 * Gets a snapshot of current metrics.
	 *
	 * @return a {@link MetricsSnapshot} containing current metric values
	 */
	public static MetricsSnapshot getSnapshot()
{
		if (!METRICS_ENABLED)
{
			return new MetricsSnapshot(); // All zeros
		}

		return new MetricsSnapshot(
			TOTAL_PARSE_TIME_NANOS.sum(),
			TOTAL_TOKENIZATION_TIME_NANOS.sum(),
			TOTAL_FILES_PROCESSED.sum(),
			TOTAL_NODES_ALLOCATED.sum(),
			TOTAL_TOKENS_CREATED.sum(),
			PEAK_MEMORY_USAGE.get(),
			PARSE_ERRORS.sum(),
			RECOVERED_ERRORS.sum(),
			SMALL_FILES.sum(),
			MEDIUM_FILES.sum(),
			LARGE_FILES.sum());
	}

	/**
	 * Immutable snapshot of metrics data.
	 *
	 * @param totalParseTimeNanos the total parsing time in nanoseconds
	 * @param totalTokenizationTimeNanos the total tokenization time in nanoseconds
	 * @param totalFilesProcessed the total number of files processed
	 * @param totalNodesAllocated the total number of AST nodes allocated
	 * @param totalTokensCreated the total number of tokens created
	 * @param peakMemoryUsageBytes the peak memory usage in bytes
	 * @param parseErrors the number of parse errors encountered
	 * @param recoveredErrors the number of errors recovered from
	 * @param smallFilesProcessed the number of small files processed
	 * @param mediumFilesProcessed the number of medium files processed
	 * @param largeFilesProcessed the number of large files processed
	 */
	public record MetricsSnapshot(
		long totalParseTimeNanos,
		long totalTokenizationTimeNanos,
		long totalFilesProcessed,
		long totalNodesAllocated,
		long totalTokensCreated,
		long peakMemoryUsageBytes,
		long parseErrors,
		long recoveredErrors,
		long smallFilesProcessed,
		long mediumFilesProcessed,
		long largeFilesProcessed)
{
		/**
		 * Creates a default metrics snapshot with all values set to zero.
		 * Used when metrics are disabled.
		 */
		public MetricsSnapshot()
{
			this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
		}

		/**
		 * Calculates the average parse time per file in milliseconds.
		 *
		 * @return the average parse time in milliseconds, or {@code 0.0} if no files processed
		 */
		public double getAverageParseTimeMs()
{
			if (totalFilesProcessed > 0)
			{
				return (totalParseTimeNanos / 1_000_000.0) / totalFilesProcessed;
			}
			return 0.0;
		}

		/**
		 * Calculates the ratio of tokenization time to total parse time.
		 *
		 * @return the tokenization ratio (0.0 to 1.0), or {@code 0.0} if no parsing occurred
		 */
		public double getTokenizationRatio()
{
			if (totalParseTimeNanos > 0)
			{
				return (double) totalTokenizationTimeNanos / totalParseTimeNanos;
			}
			return 0.0;
		}

		/**
		 * Calculates the error recovery rate as a ratio.
		 *
		 * @return the error recovery rate (0.0 to 1.0), or {@code 0.0} if no errors occurred
		 */
		public double getErrorRecoveryRate()
{
			if (parseErrors > 0)
			{
				return (double) recoveredErrors / parseErrors;
			}
			return 0.0;
		}

		/**
		 * Calculates the average number of AST nodes allocated per file.
		 *
		 * @return the average nodes per file, or {@code 0.0} if no files processed
		 */
		public double getAverageNodesPerFile()
{
			if (totalFilesProcessed > 0)
			{
				return (double) totalNodesAllocated / totalFilesProcessed;
			}
			return 0.0;
		}

		/**
		 * Calculates the average number of tokens created per file.
		 *
		 * @return the average tokens per file, or {@code 0.0} if no files processed
		 */
		public double getAverageTokensPerFile()
{
			if (totalFilesProcessed > 0)
			{
				return (double) totalTokensCreated / totalFilesProcessed;
			}
			return 0.0;
		}

		@Override
		public String toString()
{
			return String.format("""
				Parse Metrics Summary:
				  Files processed: %,d
				  Average parse time: %.2f ms
				  Peak memory usage: %,d bytes
				  Total nodes allocated: %,d (avg %.1f per file)
				  Total tokens created: %,d (avg %.1f per file)
				  Tokenization overhead: %.1f%%
				  Parse errors: %,d (%.1f%% recovery rate)
				  File size distribution:
					Small (< 1KB): %,d
					Medium (1-10KB): %,d
					Large (> 10KB): %,d
				""",
				totalFilesProcessed,
				getAverageParseTimeMs(),
				peakMemoryUsageBytes,
				totalNodesAllocated,
				getAverageNodesPerFile(),
				totalTokensCreated,
				getAverageTokensPerFile(),
				getTokenizationRatio() * 100,
				parseErrors,
				getErrorRecoveryRate() * 100,
				smallFilesProcessed,
				mediumFilesProcessed,
				largeFilesProcessed);
		}
	}

	// Prevent instantiation
	private ParseMetrics()
	{ }
}