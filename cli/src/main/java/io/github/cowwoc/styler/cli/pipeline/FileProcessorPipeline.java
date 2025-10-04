package io.github.cowwoc.styler.cli.pipeline;

import io.github.cowwoc.styler.cli.pipeline.progress.ProgressObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Coordinates the execution of a file processing pipeline.
 * <p>
 * The file processor orchestrates multiple stages in sequence, passing the output of each stage
 * as input to the next. This implements the Chain of Responsibility pattern with typed stage transitions.
 * <p>
 * Example usage:
 * <pre>{@code
 * FileProcessorPipeline pipeline = FileProcessorPipeline.builder()
 *     .addStage(parseStage)
 *     .addStage(formatStage)
 *     .addStage(writeStage)
 *     .progressObserver(consoleObserver)
 *     .build();
 *
 * ProcessingContext context = ProcessingContext.builder(sourceFile).build();
 * PipelineResult result = pipeline.process(sourceFile, context);
 * }</pre>
 *
 * @param <FINAL_OUTPUT> the final output type produced by the pipeline
 */
@SuppressWarnings("PMD.TypeParameterNamingConventions") // Descriptive names improve pipeline readability
public final class FileProcessorPipeline<FINAL_OUTPUT> implements AutoCloseable
{
	@SuppressWarnings("PMD.FieldNamingConventions") // Standard SLF4J logger naming convention
	private static final Logger logger = LoggerFactory.getLogger(FileProcessorPipeline.class);

	private final List<StageExecutor<?, ?>> stages;
	private final ProgressObserver progressObserver;
	private volatile boolean closed;

	private FileProcessorPipeline(List<StageExecutor<?, ?>> stages, ProgressObserver progressObserver)
	{
		this.stages = List.copyOf(stages);
		this.progressObserver = progressObserver;
	}

	/**
	 * Processes a file through the pipeline stages.
	 *
	 * @param sourceFile the source file to process (never {@code null})
	 * @param context    the processing context (never {@code null})
	 * @return the pipeline result containing final output or error (never {@code null})
	 * @throws NullPointerException     if {@code sourceFile} or {@code context} is {@code null}
	 * @throws IllegalStateException    if this pipeline has been closed
	 */
	@SuppressWarnings("unchecked")
	public PipelineResult<FINAL_OUTPUT> process(Path sourceFile, ProcessingContext context)
	{
		requireThat(sourceFile, "sourceFile").isNotNull();
		requireThat(context, "context").isNotNull();
		if (closed)
		{
			throw new IllegalStateException("Pipeline has been closed");
		}

		logger.info("Processing file through {} stages: {}", stages.size(), sourceFile);
		progressObserver.onProcessingStarted(sourceFile, stages.size());

		Object currentInput = sourceFile;
		int stageIndex = 0;

		for (StageExecutor<?, ?> stageExecutor : stages)
		{
			String stageName = stageExecutor.stageName();
			progressObserver.onStageStarted(sourceFile, stageName, stageIndex);

			try
			{
				StageResult<?> result = stageExecutor.execute(currentInput, context);

				if (result.isSuccess())
				{
					currentInput = result.output().orElseThrow();
					progressObserver.onStageCompleted(sourceFile, stageName, stageIndex);
					++stageIndex;
				}
				else
				{
					PipelineException exception = result.exception().orElseThrow();
					progressObserver.onProcessingFailed(sourceFile, stageName, exception);
					return PipelineResult.failure(exception);
				}
			}
			catch (Exception e)
			{
				PipelineException pipelineException = new PipelineException(
					"Stage execution failed: " + e.getMessage(),
					sourceFile,
					stageName,
					e);
				progressObserver.onProcessingFailed(sourceFile, stageName, pipelineException);
				return PipelineResult.failure(pipelineException);
			}
		}

		progressObserver.onProcessingCompleted(sourceFile);
		return PipelineResult.success((FINAL_OUTPUT) currentInput);
	}

	@Override
	public void close()
	{
		if (closed)
		{
			return; // Idempotent
		}

		logger.debug("Closing file processor pipeline");
		closed = true;
		progressObserver.onPipelineClosed();
	}

	/**
	 * Returns a new builder for constructing a file processing pipeline.
	 *
	 * @param <T> the final output type
	 * @return a new builder instance (never {@code null})
	 */
	public static <T> Builder<T> builder()
	{
		return new Builder<>();
	}

	/**
	 * Builder for creating {@link FileProcessorPipeline} instances.
	 *
	 * @param <FINAL_OUTPUT> the final output type produced by the pipeline
	 */
	@SuppressWarnings("PMD.TypeParameterNamingConventions") // Descriptive names improve pipeline readability
	public static final class Builder<FINAL_OUTPUT>
	{
		private final List<StageExecutor<?, ?>> stages = new ArrayList<>();
		private ProgressObserver progressObserver = ProgressObserver.noOp();

		private Builder()
		{
		}

		/**
		 * Adds a stage to the pipeline.
		 * <p>
		 * Stages are executed in the order they are added. The output type of each stage must
		 * match the input type of the next stage for type safety.
		 *
		 * @param stage the stage to add (never {@code null})
		 * @param <IN>  the input type for this stage
		 * @param <OUT> the output type for this stage
		 * @return this builder for method chaining (never {@code null})
		 * @throws NullPointerException if {@code stage} is {@code null}
		 */
		public <IN, OUT> Builder<FINAL_OUTPUT> addStage(PipelineStage<IN, OUT> stage)
		{
			requireThat(stage, "stage").isNotNull();
			stages.add(new StageExecutor<>(stage));
			return this;
		}

		/**
		 * Sets the progress observer for pipeline monitoring.
		 *
		 * @param observer the progress observer (never {@code null})
		 * @return this builder for method chaining (never {@code null})
		 * @throws NullPointerException if {@code observer} is {@code null}
		 */
		public Builder<FINAL_OUTPUT> progressObserver(ProgressObserver observer)
		{
			requireThat(observer, "observer").isNotNull();
			this.progressObserver = observer;
			return this;
		}

		/**
		 * Builds a new file processing pipeline.
		 *
		 * @return a new pipeline instance (never {@code null})
		 * @throws IllegalStateException if no stages have been added
		 */
		public FileProcessorPipeline<FINAL_OUTPUT> build()
		{
			if (stages.isEmpty())
			{
				throw new IllegalStateException("Pipeline must have at least one stage");
			}
			return new FileProcessorPipeline<>(stages, progressObserver);
		}
	}

	/**
	 * Internal wrapper for type-erased stage execution.
	 *
	 * @param stage the pipeline stage to execute
	 * @param <IN>  the input type
	 * @param <OUT> the output type
	 */
	@SuppressWarnings("PMD.TypeParameterNamingConventions") // Descriptive names improve pipeline readability
	private record StageExecutor<IN, OUT>(PipelineStage<IN, OUT> stage)
	{
		@SuppressWarnings("unchecked")
		StageResult<OUT> execute(Object input, ProcessingContext context) throws PipelineException
		{
			return stage.execute((IN) input, context);
		}

		String stageName()
		{
			return stage.getStageId();
		}
	}
}
