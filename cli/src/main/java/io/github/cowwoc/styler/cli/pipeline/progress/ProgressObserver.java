package io.github.cowwoc.styler.cli.pipeline.progress;

import io.github.cowwoc.styler.cli.pipeline.PipelineException;

import java.nio.file.Path;

/**
 * Observer interface for tracking pipeline execution progress.
 * <p>
 * Progress observers receive events at key points during pipeline execution, enabling real-time
 * monitoring, logging, and user feedback without coupling the pipeline to specific reporting mechanisms.
 * <p>
 * Example implementation:
 * <pre>{@code
 * public class ConsoleProgressObserver implements ProgressObserver {
 *     {@literal @}Override
 *     public void onProcessingStarted(Path file, int totalStages) {
 *         System.out.println("Processing " + file + " through " + totalStages + " stages...");
 *     }
 *
 *     {@literal @}Override
 *     public void onStageCompleted(Path file, String stageName, int stageIndex) {
 *         System.out.println("  [" + (stageIndex + 1) + "] " + stageName + " completed");
 *     }
 * }
 * }</pre>
 */
public interface ProgressObserver
{
	/**
	 * Called when pipeline processing starts for a file.
	 *
	 * @param file        the file being processed (never {@code null})
	 * @param totalStages the total number of stages in the pipeline
	 */
	void onProcessingStarted(Path file, int totalStages);

	/**
	 * Called when a pipeline stage starts executing.
	 *
	 * @param file       the file being processed (never {@code null})
	 * @param stageName  the name of the stage starting (never {@code null} or empty)
	 * @param stageIndex the zero-based index of this stage
	 */
	void onStageStarted(Path file, String stageName, int stageIndex);

	/**
	 * Called when a pipeline stage completes successfully.
	 *
	 * @param file       the file being processed (never {@code null})
	 * @param stageName  the name of the completed stage (never {@code null} or empty)
	 * @param stageIndex the zero-based index of this stage
	 */
	void onStageCompleted(Path file, String stageName, int stageIndex);

	/**
	 * Called when pipeline processing completes successfully for a file.
	 *
	 * @param file the file that was processed (never {@code null})
	 */
	void onProcessingCompleted(Path file);

	/**
	 * Called when pipeline processing fails for a file.
	 *
	 * @param file      the file being processed (never {@code null})
	 * @param stageName the name of the stage where failure occurred (never {@code null} or empty)
	 * @param exception the exception describing the failure (never {@code null})
	 */
	void onProcessingFailed(Path file, String stageName, PipelineException exception);

	/**
	 * Called when the pipeline is closed.
	 */
	void onPipelineClosed();

	/**
	 * Returns a no-op progress observer that performs no actions.
	 * <p>
	 * Use this when progress tracking is not needed.
	 *
	 * @return a no-op observer (never {@code null})
	 */
	static ProgressObserver noOp()
	{
		return NoOpProgressObserver.INSTANCE;
	}

	/**
	 * No-op implementation of {@link ProgressObserver}.
	 */
	final class NoOpProgressObserver implements ProgressObserver
	{
		private static final NoOpProgressObserver INSTANCE = new NoOpProgressObserver();

		private NoOpProgressObserver()
		{
		}

		@Override
		public void onProcessingStarted(Path file, int totalStages)
		{
		}

		@Override
		public void onStageStarted(Path file, String stageName, int stageIndex)
		{
		}

		@Override
		public void onStageCompleted(Path file, String stageName, int stageIndex)
		{
		}

		@Override
		public void onProcessingCompleted(Path file)
		{
		}

		@Override
		public void onProcessingFailed(Path file, String stageName, PipelineException exception)
		{
		}

		@Override
		public void onPipelineClosed()
		{
		}
	}
}
