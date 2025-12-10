package io.github.cowwoc.styler.pipeline.parallel;

/**
 * Strategy for handling errors during batch file processing.
 * <p>
 * Defines how the batch processor responds when a file fails processing:
 * <ul>
 *     <li>FAIL_FAST: Stop immediately on first error</li>
 *     <li>CONTINUE: Process all files, collect errors, return aggregated results</li>
 *     <li>ABORT_AFTER_THRESHOLD: Continue until error count reaches threshold, then abort</li>
 * </ul>
 * <p>
 * <b>Thread-safety</b>: This enum is immutable and thread-safe.
 */
public enum ErrorStrategy
{
	/**
	 * Stop processing immediately when the first error occurs.
	 * <p>
	 * All previously completed files are included in results. Remaining files are not processed.
	 * Useful for failing fast on first problem.
	 */
	FAIL_FAST,

	/**
	 * Continue processing all files despite errors.
	 * <p>
	 * All files are processed. Errors are collected and returned in {@code BatchResult.errors()}.
	 * Useful for comprehensive error reporting across entire batch.
	 */
	CONTINUE,

	/**
	 * Abort batch after error count reaches configured threshold.
	 * <p>
	 * Allows a configurable number of errors before stopping processing.
	 * Useful for balancing error visibility with fail-fast behavior.
	 */
	ABORT_AFTER_THRESHOLD
}
