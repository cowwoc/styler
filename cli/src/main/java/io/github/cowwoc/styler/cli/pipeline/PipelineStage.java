package io.github.cowwoc.styler.cli.pipeline;


/**
 * Defines a single processing stage in the file formatting pipeline.
 * <p>
 * Each pipeline stage transforms input from the previous stage into output for the next stage,
 * following the Chain of Responsibility design pattern. Stages are composable and independently
 * testable.
 * <p>
 * Implementations must be thread-safe and stateless, with all state passed via {@code ProcessingContext}.
 * Stages should be immutable with all dependencies injected via constructor.
 *
 * @param <INPUT>  the input type consumed by this stage
 * @param <OUTPUT> the output type produced by this stage
 * @see AbstractPipelineStage
 * @see StageResult
 */
@FunctionalInterface
@SuppressWarnings("PMD.TypeParameterNamingConventions") // Descriptive names improve pipeline readability
public interface PipelineStage<INPUT, OUTPUT>
{
	/**
	 * Executes this pipeline stage on the given input.
	 * <p>
	 * The implementation must not modify {@code input} or {@code context}. All state transformations
	 * should create new immutable objects returned via {@link StageResult}.
	 *
	 * @param input   the input to process (never {@code null})
	 * @param context the processing context providing configuration and shared state (never {@code null})
	 * @return the result of stage execution containing output or error information (never {@code null})
	 * @throws NullPointerException if {@code input} or {@code context} is {@code null}
	 * @throws PipelineException    if stage execution fails and cannot be recovered
	 */
	StageResult<OUTPUT> execute(INPUT input, ProcessingContext context) throws PipelineException;

	/**
	 * Returns the unique identifier for this stage.
	 * <p>
	 * The stage ID is used for logging, error reporting, and progress tracking. It should be
	 * a short, descriptive name like "parse", "format", or "validate".
	 *
	 * @return the stage identifier (never {@code null} or empty)
	 */
	default String getStageId()
	{
		return getClass().getSimpleName();
	}

	/**
	 * Returns whether this stage supports error recovery.
	 * <p>
	 * Stages that support recovery can use {@link ErrorRecoveryStrategy} to handle failures
	 * gracefully. Stages that don't support recovery will propagate exceptions directly to
	 * the pipeline coordinator.
	 *
	 * @return {@code true} if this stage supports error recovery, {@code false} otherwise
	 */
	default boolean supportsErrorRecovery()
	{
		return false;
	}
}
