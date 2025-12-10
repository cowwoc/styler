package io.github.cowwoc.styler.pipeline.parallel;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for processing multiple files in parallel using virtual threads.
 * <p>
 * Implementations process files concurrently while respecting memory constraints,
 * error handling strategies, and reporting progress.
 * <p>
 * Resource Management: Implementations implement {@code AutoCloseable} for use in
 * try-with-resources blocks. The processor may maintain virtual thread executors and other
 * resources that require cleanup.
 * <p>
 * Example:
 * <pre>
 * try (BatchProcessor processor = new DefaultBatchProcessor(config))
 * {
 *     BatchResult result = processor.processFiles(filePaths);
 *     System.out.printf("Processed %d files, %d succeeded, %d failed%n",
 *         result.totalFiles(), result.successCount(), result.failureCount());
 * }
 * </pre>
 * <p>
 * <b>Thread-safety</b>: Implementations must be thread-safe. Multiple threads may call
 * {@code processFiles()} concurrently on the same instance.
 */
public interface BatchProcessor extends AutoCloseable
{
	/**
	 * Processes a batch of files in parallel.
	 * <p>
	 * Files are processed concurrently according to the processor's configuration. Results
	 * include both successful completions and errors. The error handling strategy determines
	 * whether processing continues after encountering errors.
	 * <p>
	 * The operation respects the configured {@code maxConcurrency} limit to prevent
	 * out-of-memory conditions.
	 *
	 * @param files the list of file paths to process, may be empty
	 * @return a {@code BatchResult} containing all results and metrics
	 * @throws NullPointerException if {@code files} or any element is null
	 * @throws InterruptedException if the processing thread is interrupted
	 * @throws IllegalStateException if the processor has been closed
	 */
	BatchResult processFiles(List<Path> files) throws InterruptedException;

	/**
	 * Closes the processor and releases associated resources.
	 * <p>
	 * Safe to call multiple times - subsequent calls are no-ops. Virtual thread executors
	 * are properly shut down.
	 */
	@Override
	void close();
}
