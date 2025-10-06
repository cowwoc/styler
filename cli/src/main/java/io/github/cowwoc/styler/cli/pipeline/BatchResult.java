package io.github.cowwoc.styler.cli.pipeline;

import java.util.List;

/**
 * Represents the aggregated result of processing multiple files in parallel.
 * <p>
 * This immutable record contains success and error counts along with detailed
 * error information for failed files.
 *
 * @param successCount the number of files processed successfully
 * @param errorCount   the number of files that failed processing
 * @param errors       the list of exceptions from failed files (never {@code null})
 */
public record BatchResult(int successCount, int errorCount, List<PipelineException> errors)
{
	/**
	 * Creates a batch result with validation.
	 *
	 * @param successCount the number of successful files
	 * @param errorCount   the number of failed files
	 * @param errors       the list of exceptions (never {@code null})
	 * @throws IllegalArgumentException if counts are negative or errors list size doesn't match errorCount
	 */
	public BatchResult
	{
		if (successCount < 0)
		{
			throw new IllegalArgumentException("successCount must be non-negative: " + successCount);
		}
		if (errorCount < 0)
		{
			throw new IllegalArgumentException("errorCount must be non-negative: " + errorCount);
		}
		if (errors == null)
		{
			throw new NullPointerException("errors must not be null");
		}
		if (errors.size() != errorCount)
		{
			throw new IllegalArgumentException(
				"errors list size (" + errors.size() + ") must match errorCount (" + errorCount + ")");
		}
		errors = List.copyOf(errors);
	}

	/**
	 * Returns whether all files were processed successfully.
	 *
	 * @return {@code true} if no errors occurred, {@code false} otherwise
	 */
	public boolean isSuccess()
	{
		return errorCount == 0;
	}

	/**
	 * Returns whether any files were processed successfully.
	 *
	 * @return {@code true} if at least one file succeeded, {@code false} otherwise
	 */
	public boolean hasPartialSuccess()
	{
		return successCount > 0 && errorCount > 0;
	}

	/**
	 * Returns whether any errors occurred.
	 *
	 * @return {@code true} if errors exist, {@code false} otherwise
	 */
	public boolean hasErrors()
	{
		return errorCount > 0;
	}

	/**
	 * Returns the total number of files processed.
	 *
	 * @return the sum of successful and failed files
	 */
	public int totalFiles()
	{
		return successCount + errorCount;
	}

	/**
	 * Creates an empty batch result for when no files were processed.
	 *
	 * @return an empty result (never {@code null})
	 */
	public static BatchResult empty()
	{
		return new BatchResult(0, 0, List.of());
	}
}
