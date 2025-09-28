package io.github.cowwoc.styler.cli.util;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for reporting progress during long-running operations.
 * <p>
 * Provides real-time feedback to users about processing status, estimated
 * completion time, and throughput metrics. Supports both human-readable
 * output and machine-readable JSON format.
 */
public class ProgressReporter
{
	private final int totalItems;
	private final AtomicInteger processedItems = new AtomicInteger(0);
	private final AtomicInteger errorCount = new AtomicInteger(0);
	private final AtomicLong totalBytes = new AtomicLong(0);
	private final Instant startTime;
	private final boolean quietMode;
	private final boolean jsonMode;

	private volatile Instant lastUpdateTime;
	private static final Duration UPDATE_INTERVAL = Duration.ofMillis(500);

	/**
	 * Creates a new progress reporter.
	 *
	 * @param totalItems the total number of items to process
	 * @param quietMode whether to suppress progress output
	 * @param jsonMode whether to output in JSON format
	 */
	public ProgressReporter(int totalItems, boolean quietMode, boolean jsonMode)
	{
		this.totalItems = totalItems;
		this.quietMode = quietMode;
		this.jsonMode = jsonMode;
		this.startTime = Instant.now();
		this.lastUpdateTime = startTime;
	}

	/**
	 * Reports progress for a completed item.
	 *
	 * @param itemName the name of the processed item
	 * @param itemSize the size of the processed item in bytes
	 * @param success whether the item was processed successfully
	 */
	public void reportProgress(String itemName, long itemSize, boolean success)
	{
		int processed = processedItems.incrementAndGet();
		totalBytes.addAndGet(itemSize);

		if (!success)
		{
			errorCount.incrementAndGet();
		}

		// Update output if enough time has passed or we're done
		Instant now = Instant.now();
		if (shouldUpdateOutput(now) || processed == totalItems)
		{
			updateOutput(now, itemName, success);
			lastUpdateTime = now;
		}
	}

	/**
	 * Reports an error during processing.
	 *
	 * @param itemName the name of the item that failed
	 * @param error the error message
	 */
	public void reportError(String itemName, String error)
	{
		errorCount.incrementAndGet();

		if (!quietMode)
		{
			if (jsonMode)
			{
				System.err.println(String.format(
					"{\"type\":\"error\",\"item\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
					escapeJson(itemName), escapeJson(error), Instant.now()
				));
			}
			else
			{
				System.err.println("Error processing " + itemName + ": " + error);
			}
		}
	}

	/**
	 * Generates a final summary report.
	 */
	public void reportSummary()
	{
		if (quietMode)
		{
			return;
		}

		Instant endTime = Instant.now();
		Duration elapsed = Duration.between(startTime, endTime);
		int processed = processedItems.get();
		int errors = errorCount.get();
		long bytes = totalBytes.get();

		if (jsonMode)
		{
			System.out.println(String.format(
				"{\"type\":\"summary\",\"total\":%d,\"processed\":%d,\"errors\":%d," +
				"\"bytes\":%d,\"duration\":\"%s\",\"throughput\":%.2f}",
				totalItems, processed, errors, bytes, elapsed,
				calculateThroughput(processed, elapsed)
			));
		}
		else
		{
			System.out.println();
			System.out.println("=== Processing Summary ===");
			System.out.println("Total files: " + totalItems);
			System.out.println("Processed: " + processed);
			System.out.println("Errors: " + errors);
			System.out.println("Total size: " + formatBytes(bytes));
			System.out.println("Duration: " + formatDuration(elapsed));
			System.out.println("Throughput: " + String.format("%.2f files/sec", calculateThroughput(processed, elapsed)));
		}
	}

	/**
	 * Determines if output should be updated based on time interval.
	 */
	private boolean shouldUpdateOutput(Instant now)
	{
		return !quietMode && Duration.between(lastUpdateTime, now).compareTo(UPDATE_INTERVAL) >= 0;
	}

	/**
	 * Updates the progress output.
	 */
	private void updateOutput(Instant now, String currentItem, boolean success)
	{
		if (quietMode)
		{
			return;
		}

		int processed = processedItems.get();
		int errors = errorCount.get();
		Duration elapsed = Duration.between(startTime, now);

		if (jsonMode)
		{
			System.out.println(String.format(
				"{\"type\":\"progress\",\"processed\":%d,\"total\":%d,\"errors\":%d," +
				"\"current\":\"%s\",\"success\":%s,\"elapsed\":\"%s\"}",
				processed, totalItems, errors, escapeJson(currentItem), success, elapsed
			));
		}
		else
		{
			double percentage = (double) processed / totalItems * 100;
			Duration estimatedTotal = estimateRemainingTime(elapsed, processed);

			System.out.printf("\r[%3.0f%%] %d/%d files (%d errors) - %s - ETA: %s",
				percentage, processed, totalItems, errors,
				currentItem.length() > 40 ? "..." + currentItem.substring(currentItem.length() - 37) : currentItem,
				formatDuration(estimatedTotal)
			);

			if (processed == totalItems)
			{
				System.out.println(); // New line when complete
			}
		}
	}

	/**
	 * Estimates remaining time based on current progress.
	 */
	private Duration estimateRemainingTime(Duration elapsed, int processed)
	{
		if (processed == 0)
		{
			return Duration.ZERO;
		}

		double avgTimePerItem = elapsed.toMillis() / (double) processed;
		long remainingItems = totalItems - processed;
		return Duration.ofMillis((long) (remainingItems * avgTimePerItem));
	}

	/**
	 * Calculates throughput in items per second.
	 */
	private double calculateThroughput(int items, Duration elapsed)
	{
		if (elapsed.isZero())
		{
			return 0.0;
		}
		return items / (elapsed.toMillis() / 1000.0);
	}

	/**
	 * Formats byte count in human-readable form.
	 */
	private String formatBytes(long bytes)
	{
		if (bytes < 1024)
		{
			return bytes + " B";
		}
		else if (bytes < 1024 * 1024)
		{
			return String.format("%.1f KB", bytes / 1024.0);
		}
		else if (bytes < 1024 * 1024 * 1024)
		{
			return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
		}
		else
		{
			return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
		}
	}

	/**
	 * Formats duration in human-readable form.
	 */
	private String formatDuration(Duration duration)
	{
		long seconds = duration.getSeconds();
		if (seconds < 60)
		{
			return seconds + "s";
		}
		else if (seconds < 3600)
		{
			return String.format("%dm %ds", seconds / 60, seconds % 60);
		}
		else
		{
			return String.format("%dh %dm %ds", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
		}
	}

	/**
	 * Escapes string for JSON output.
	 */
	private String escapeJson(String str)
	{
		return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}

	/**
	 * Returns the current progress statistics.
	 *
	 * @return progress statistics
	 */
	public ProgressStats getStats()
	{
		return new ProgressStats(
			totalItems,
			processedItems.get(),
			errorCount.get(),
			totalBytes.get(),
			Duration.between(startTime, Instant.now())
		);
	}

	/**
	 * Immutable progress statistics snapshot.
	 */
	public record ProgressStats(
		int totalItems,
		int processedItems,
		int errorCount,
		long totalBytes,
		Duration elapsed
	) {}
}