package io.github.styler.parser;

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
public final class ParseMetrics {
    private static final boolean METRICS_ENABLED =
        Boolean.parseBoolean(System.getProperty("styler.metrics.enabled", "false"));

    // Parse timing metrics
    private static final LongAdder totalParseTimeNanos = new LongAdder();
    private static final LongAdder totalTokenizationTimeNanos = new LongAdder();
    private static final LongAdder totalFilesProcessed = new LongAdder();

    // Memory metrics
    private static final LongAdder totalNodesAllocated = new LongAdder();
    private static final LongAdder totalTokensCreated = new LongAdder();
    private static final AtomicLong peakMemoryUsage = new AtomicLong(0);

    // Error metrics
    private static final LongAdder parseErrors = new LongAdder();
    private static final LongAdder recoveredErrors = new LongAdder();

    // File size distribution (for understanding workloads)
    private static final LongAdder smallFiles = new LongAdder(); // < 1KB
    private static final LongAdder mediumFiles = new LongAdder(); // 1KB - 10KB
    private static final LongAdder largeFiles = new LongAdder(); // > 10KB

    /**
     * Records parsing time for a file.
     * No-op if metrics are disabled.
     */
    public static void recordParseTime(long nanos, int fileSizeBytes) {
        if (!METRICS_ENABLED) return;

        totalParseTimeNanos.add(nanos);
        totalFilesProcessed.increment();

        // Categorize by file size
        if (fileSizeBytes < 1024) {
            smallFiles.increment();
        } else if (fileSizeBytes < 10240) {
            mediumFiles.increment();
        } else {
            largeFiles.increment();
        }
    }

    /**
     * Records tokenization time.
     */
    public static void recordTokenizationTime(long nanos, int tokenCount) {
        if (!METRICS_ENABLED) return;

        totalTokenizationTimeNanos.add(nanos);
        totalTokensCreated.add(tokenCount);
    }

    /**
     * Records node allocation.
     */
    public static void recordNodeAllocation(int nodeCount) {
        if (!METRICS_ENABLED) return;
        totalNodesAllocated.add(nodeCount);
    }

    /**
     * Records memory usage high-water mark.
     */
    public static void recordMemoryUsage(long bytes) {
        if (!METRICS_ENABLED) return;
        peakMemoryUsage.accumulateAndGet(bytes, Long::max);
    }

    /**
     * Records a parsing error.
     */
    public static void recordParseError(boolean wasRecovered) {
        if (!METRICS_ENABLED) return;

        parseErrors.increment();
        if (wasRecovered) {
            recoveredErrors.increment();
        }
    }

    /**
     * Gets a snapshot of current metrics.
     */
    public static MetricsSnapshot getSnapshot() {
        if (!METRICS_ENABLED) {
            return new MetricsSnapshot(); // All zeros
        }

        return new MetricsSnapshot(
            totalParseTimeNanos.sum(),
            totalTokenizationTimeNanos.sum(),
            totalFilesProcessed.sum(),
            totalNodesAllocated.sum(),
            totalTokensCreated.sum(),
            peakMemoryUsage.get(),
            parseErrors.sum(),
            recoveredErrors.sum(),
            smallFiles.sum(),
            mediumFiles.sum(),
            largeFiles.sum()
        );
    }

    /**
     * Resets all metrics (useful for testing).
     */
    public static void reset() {
        if (!METRICS_ENABLED) return;

        totalParseTimeNanos.reset();
        totalTokenizationTimeNanos.reset();
        totalFilesProcessed.reset();
        totalNodesAllocated.reset();
        totalTokensCreated.reset();
        peakMemoryUsage.set(0);
        parseErrors.reset();
        recoveredErrors.reset();
        smallFiles.reset();
        mediumFiles.reset();
        largeFiles.reset();
    }

    /**
     * Immutable snapshot of metrics data.
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
        long largeFilesProcessed
    ) {
        // Default constructor for disabled metrics
        public MetricsSnapshot() {
            this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        public double getAverageParseTimeMs() {
            return totalFilesProcessed > 0 ?
                (totalParseTimeNanos / 1_000_000.0) / totalFilesProcessed : 0.0;
        }

        public double getTokenizationRatio() {
            return totalParseTimeNanos > 0 ?
                (double) totalTokenizationTimeNanos / totalParseTimeNanos : 0.0;
        }

        public double getErrorRecoveryRate() {
            return parseErrors > 0 ?
                (double) recoveredErrors / parseErrors : 0.0;
        }

        public double getAverageNodesPerFile() {
            return totalFilesProcessed > 0 ?
                (double) totalNodesAllocated / totalFilesProcessed : 0.0;
        }

        public double getAverageTokensPerFile() {
            return totalFilesProcessed > 0 ?
                (double) totalTokensCreated / totalFilesProcessed : 0.0;
        }

        @Override
        public String toString() {
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
                largeFilesProcessed
            );
        }
    }

    // Prevent instantiation
    private ParseMetrics() {}
}