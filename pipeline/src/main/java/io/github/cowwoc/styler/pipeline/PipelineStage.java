package io.github.cowwoc.styler.pipeline;

/**
 * Internal interface for pipeline stages.
 * <p>
 * This is a package-private interface defining the contract for all pipeline stages.
 * Implementations follow the Template Method pattern via AbstractPipelineStage.
 *
 * @see AbstractPipelineStage
 */
interface PipelineStage
{
	/**
	 * Executes this stage with the given processing context.
	 *
	 * @param context the processing context (immutable)
	 * @param previousStageData data produced by the previous stage, or null if this is the first stage
	 * @return the stage result (success, failure, or skipped)
	 */
	StageResult execute(ProcessingContext context, Object previousStageData);

	/**
	 * Returns the human-readable name of this stage.
	 *
	 * @return the stage name (e.g., "parse", "format", "validate")
	 */
	String getStageName();
}
