package io.github.cowwoc.styler.pipeline.parallel;

import java.nio.file.Path;

/**
 * Callback interface for reporting progress during batch file processing.
 * <p>
 * Invoked by {@code BatchProcessor} as files complete processing.
 * <p>
 * Example:
 * <pre>
 * ProgressCallback callback = (completed, total, currentFile) -&gt; {
 *     System.out.printf("Progress: %d/%d (%s)%n", completed, total, currentFile);
 * };
 * </pre>
 * <p>
 * <b>Thread-safety</b>: Implementations must be thread-safe. The processor may invoke this
 * callback concurrently from multiple virtual threads.
 */
@FunctionalInterface
public interface ProgressCallback
{
	/**
	 * Reports progress during batch processing.
	 * <p>
	 * Called whenever a file completes processing (success or failure). This method must not
	 * block for extended periods, as it may hold up virtual thread progress.
	 *
	 * @param completed the number of files completed so far (includes successful and failed)
	 * @param total the total number of files to process
	 * @param currentFile the {@code Path} of the file that just completed
	 * @throws NullPointerException if {@code currentFile} is null
	 */
	void onProgress(int completed, int total, Path currentFile);
}
