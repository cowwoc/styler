package io.github.cowwoc.styler.cli.pipeline.progress;

import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Thread-safe wrapper for {@link ProgressObserver} that aggregates parallel file processing progress.
 * <p>
 * This observer tracks batch-level statistics (total files, completed files, error count) using
 * atomic counters for thread safety. It delegates per-file events to the wrapped observer while
 * providing batch-level progress reporting.
 * <p>
 * <b>Thread Safety:</b> All methods are thread-safe and can be called concurrently from multiple
 * virtual threads. Uses {@link AtomicInteger} for lock-free progress counters.
 * <p>
 * Usage:
 * <pre>{@code
 * ProgressObserver fileObserver = new ConsoleProgressObserver();
 * ConcurrentProgressObserver batchObserver = new ConcurrentProgressObserver(fileObserver, 100);
 *
 * // Called from multiple virtual threads
 * batchObserver.onProcessingCompleted(file);  // Thread-safe increment
 *
 * // Get batch statistics at any time
 * int completed = batchObserver.getCompletedCount();
 * double progress = batchObserver.getProgressPercentage();
 * }</pre>
 */
public final class ConcurrentProgressObserver implements ProgressObserver
{
	@SuppressWarnings("PMD.FieldNamingConventions")
	private static final Logger logger = LoggerFactory.getLogger(ConcurrentProgressObserver.class);

	private final ProgressObserver delegate;
	private final int totalFiles;
	private final AtomicInteger completedCount;
	private final AtomicInteger errorCount;
	private final AtomicLong startTimeMillis;
	private final AtomicLong lastReportTimeMillis;

	/**
	 * Creates a concurrent progress observer.
	 *
	 * @param delegate   the observer to delegate per-file events to (never {@code null})
	 * @param totalFiles the total number of files to process (must be non-negative)
	 * @throws NullPointerException     if {@code delegate} is {@code null}
	 * @throws IllegalArgumentException if {@code totalFiles} is negative
	 */
	public ConcurrentProgressObserver(ProgressObserver delegate, int totalFiles)
	{
		requireThat(delegate, "delegate").isNotNull();
		requireThat(totalFiles, "totalFiles").isGreaterThanOrEqualTo(0);

		this.delegate = delegate;
		this.totalFiles = totalFiles;
		this.completedCount = new AtomicInteger(0);
		this.errorCount = new AtomicInteger(0);
		this.startTimeMillis = new AtomicLong(System.currentTimeMillis());
		this.lastReportTimeMillis = new AtomicLong(System.currentTimeMillis());
	}

	@Override
	public void onProcessingStarted(Path file, int totalStages)
	{
		delegate.onProcessingStarted(file, totalStages);
	}

	@Override
	public void onStageStarted(Path file, String stageName, int stageIndex)
	{
		delegate.onStageStarted(file, stageName, stageIndex);
	}

	@Override
	public void onStageCompleted(Path file, String stageName, int stageIndex)
	{
		delegate.onStageCompleted(file, stageName, stageIndex);
	}

	@Override
	public void onProcessingCompleted(Path file)
	{
		delegate.onProcessingCompleted(file);
		int completed = completedCount.incrementAndGet();
		reportBatchProgress(completed);
	}

	@Override
	public void onProcessingFailed(Path file, String stageName, PipelineException exception)
	{
		delegate.onProcessingFailed(file, stageName, exception);
		errorCount.incrementAndGet();
		int completed = completedCount.incrementAndGet();
		reportBatchProgress(completed);
	}

	@Override
	public void onPipelineClosed()
	{
		delegate.onPipelineClosed();
		logFinalStatistics();
	}

	/**
	 * Reports batch progress at intervals.
	 * <p>
	 * Logs progress every 10% completion or every 5 seconds, whichever comes first.
	 *
	 * @param completed the number of completed files
	 */
	private void reportBatchProgress(int completed)
	{
		if (totalFiles == 0)
		{
			return;
		}

		long now = System.currentTimeMillis();
		long lastReport = lastReportTimeMillis.get();
		double progressPercent = (completed * 100.0) / totalFiles;

		// Report every 10% or every 5 seconds
		boolean shouldReport = completed % Math.max(1, totalFiles / 10) == 0 ||
			now - lastReport >= 5000;

		if (shouldReport && lastReportTimeMillis.compareAndSet(lastReport, now))
		{
			long elapsedSeconds = (now - startTimeMillis.get()) / 1000;
			int remaining = totalFiles - completed;
			double filesPerSecond;
			if (elapsedSeconds > 0)
			{
				filesPerSecond = completed / (double) elapsedSeconds;
			}
			else
			{
				filesPerSecond = 0;
			}
			int estimatedSecondsRemaining;
			if (filesPerSecond > 0)
			{
				estimatedSecondsRemaining = (int) (remaining / filesPerSecond);
			}
			else
			{
				estimatedSecondsRemaining = 0;
			}

			logger.info(
				"Progress: {}/{} files ({:.1f}%) | {} errors | Throughput: {:.1f} files/sec | ETA: {}s",
				completed, totalFiles, progressPercent, errorCount.get(),
				filesPerSecond, estimatedSecondsRemaining);
		}
	}

	/**
	 * Logs final batch statistics.
	 */
	private void logFinalStatistics()
	{
		long elapsedMillis = System.currentTimeMillis() - startTimeMillis.get();
		double elapsedSeconds = elapsedMillis / 1000.0;
		int completed = completedCount.get();
		int errors = errorCount.get();
		int succeeded = completed - errors;
		double throughput;
		if (elapsedSeconds > 0)
		{
			throughput = completed / elapsedSeconds;
		}
		else
		{
			throughput = 0;
		}

		logger.info(
			"Batch complete: {}/{} files | {} succeeded, {} failed | {:.2f}s elapsed | {:.1f} files/sec",
			completed, totalFiles, succeeded, errors, elapsedSeconds, throughput);
	}

	/**
	 * Returns the number of completed files (including errors).
	 *
	 * @return completed count (never negative)
	 */
	public int getCompletedCount()
	{
		return completedCount.get();
	}

	/**
	 * Returns the number of files that failed processing.
	 *
	 * @return error count (never negative)
	 */
	public int getErrorCount()
	{
		return errorCount.get();
	}

	/**
	 * Returns the number of files remaining to process.
	 *
	 * @return remaining count (never negative)
	 */
	public int getRemainingCount()
	{
		return Math.max(0, totalFiles - completedCount.get());
	}

	/**
	 * Returns the current progress as a percentage.
	 *
	 * @return progress percentage ({@code 0}.{@code 0} to {@code 100}.{@code 0})
	 */
	public double getProgressPercentage()
	{
		if (totalFiles > 0)
		{
			return (completedCount.get() * 100.0) / totalFiles;
		}
		return 0.0;
	}

	/**
	 * Returns the current throughput in files per second.
	 *
	 * @return files per second (never negative)
	 */
	public double getThroughput()
	{
		long elapsedMillis = System.currentTimeMillis() - startTimeMillis.get();
		double elapsedSeconds = elapsedMillis / 1000.0;
		if (elapsedSeconds > 0)
		{
			return completedCount.get() / elapsedSeconds;
		}
		return 0.0;
	}
}
