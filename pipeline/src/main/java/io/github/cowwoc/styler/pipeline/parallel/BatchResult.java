package io.github.cowwoc.styler.pipeline.parallel;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import io.github.cowwoc.styler.pipeline.PipelineResult;

/**
 * Immutable aggregation of results from batch file processing.
 * <p>
 * Contains comprehensive metrics about batch execution including success/failure counts,
 * individual file results, errors, and performance metrics.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param totalFiles the total number of files in the batch
 * @param successCount the number of files processed successfully
 * @param failureCount the number of files that encountered errors
 * @param results unmodifiable list of {@code PipelineResult} for successful files
 * @param errors unmodifiable map of file paths to error messages for failed files
 * @param totalDuration the total time taken to process the entire batch
 * @param throughputFilesPerSecond the effective throughput as files processed per second
 */
public record BatchResult(
	int totalFiles,
	int successCount,
	int failureCount,
	List<PipelineResult> results,
	Map<Path, String> errors,
	Duration totalDuration,
	double throughputFilesPerSecond)
{
	/**
	 * Creates a batch result with validation.
	 * <p>
	 * Validates that the sum of success and failure counts equals total files, and that
	 * all counts are non-negative.
	 *
	 * @param totalFiles the total number of files in the batch
	 * @param successCount the number of successful files
	 * @param failureCount the number of failed files
	 * @param results unmodifiable list of results
	 * @param errors unmodifiable map of errors
	 * @param totalDuration the total processing duration
	 * @param throughputFilesPerSecond the throughput metric
	 * @throws IllegalArgumentException if counts don't match or are invalid
	 * @throws NullPointerException if {@code results}, {@code errors}, or {@code totalDuration} is null
	 */
	public BatchResult
	{
		requireThat(totalFiles, "totalFiles").isGreaterThanOrEqualTo(0);
		requireThat(successCount, "successCount").isGreaterThanOrEqualTo(0);
		requireThat(failureCount, "failureCount").isGreaterThanOrEqualTo(0);
		requireThat(successCount + failureCount, "completedCount").isEqualTo(totalFiles);
		requireThat(results, "results").isNotNull();
		requireThat(errors, "errors").isNotNull();
		requireThat(totalDuration, "totalDuration").isNotNull();
		requireThat(throughputFilesPerSecond, "throughputFilesPerSecond").isGreaterThanOrEqualTo(0.0);

		results = List.copyOf(results);
		errors = Map.copyOf(errors);
	}

	/**
	 * Checks if all files were processed successfully.
	 *
	 * @return true if {@code failureCount} is zero, false otherwise
	 */
	public boolean allSucceeded()
	{
		return failureCount == 0;
	}

	/**
	 * Checks if at least one file failed processing.
	 *
	 * @return true if {@code failureCount} is greater than zero, false otherwise
	 */
	public boolean hasFailed()
	{
		return failureCount > 0;
	}

	/**
	 * Calculates the success rate as a percentage.
	 *
	 * @return the success rate from 0.0 to 100.0, or 0.0 if totalFiles is zero
	 */
	public double successRate()
	{
		if (totalFiles == 0)
			return 0.0;
		return (successCount * 100.0) / totalFiles;
	}
}
