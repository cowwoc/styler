package io.github.cowwoc.styler.pipeline;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.util.logging.Logger;

/**
 * Abstract base class implementing the Template Method pattern for pipeline stages.
 * <p>
 * This class standardizes the lifecycle of all pipeline stages:
 * <ol>
 *     <li>setup() - Initialize resources (default: no-op)</li>
 *     <li>executeStage() - Perform stage logic (subclass responsibility)</li>
 *     <li>cleanup() - Clean up resources (default: no-op)</li>
 *     <li>handleError() - Handle exceptions (converts to StageResult.Failure)</li>
 * </ol>
 * <p>
 * Subclasses implement executeStage() for their specific functionality.
 * Resource cleanup is guaranteed even on exception via try-finally pattern.
 * <p>
 * Example:
 * <pre>
 * class CustomStage extends AbstractPipelineStage
 * {
 *     protected StageResult executeStage(ProcessingContext context)
 *     {
 *         // Perform actual work
 *         return new StageResult.Success(data);
 *     }
 *
 *     public String getStageName()
 *     {
 *         return "custom";
 *     }
 * }
 * </pre>
 */
public abstract class AbstractPipelineStage implements PipelineStage
{
	private final Logger log = Logger.getLogger(getClass().getName());

	/**
	 * Executes the stage with guaranteed resource cleanup.
	 * <p>
	 * This implements the Template Method pattern:
	 * <ol>
	 *     <li>Calls setup() to initialize resources</li>
	 *     <li>Calls executeStage() for stage logic</li>
	 *     <li>Calls cleanup() for resource cleanup</li>
	 *     <li>Catches exceptions and converts to StageResult.Failure</li>
	 * </ol>
	 *
	 * @param context the processing context
	 * @return the stage result
	 * @throws NullPointerException if context is null
	 */
	@Override
	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public final StageResult execute(ProcessingContext context)
	{
		requireThat(context, "context").isNotNull();

		try
		{
			setup(context);
			return executeStage(context);
		}
		catch (Exception exception)
		{
			return handleError(context, exception);
		}
		finally
		{
			try
			{
				cleanup(context);
			}
			catch (Exception exception)
			{
				log.warning("Cleanup failed in " + getStageName() + " stage: " + exception.getMessage());
			}
		}
	}

	/**
	 * Sets up resources for stage execution.
	 * <p>
	 * Default implementation does nothing. Subclasses can override to initialize
	 * resources needed for executeStage().
	 *
	 * @param context the processing context
	 * @throws Exception if setup fails
	 */
	protected abstract void setup(ProcessingContext context) throws Exception;

	/**
	 * Implements the stage-specific logic.
	 * <p>
	 * Subclasses must implement this method to perform their specific processing.
	 *
	 * @param context the processing context (immutable)
	 * @return the stage result
	 * @throws Exception if processing fails
	 */
	protected abstract StageResult executeStage(ProcessingContext context) throws Exception;

	/**
	 * Cleans up resources after stage execution.
	 * <p>
	 * Default implementation does nothing. Subclasses can override to release
	 * resources allocated during setup() or executeStage().
	 * <p>
	 * This is called even if executeStage() throws an exception.
	 *
	 * @param context the processing context
	 * @throws Exception if cleanup fails
	 */
	protected abstract void cleanup(ProcessingContext context) throws Exception;

	/**
	 * Handles exceptions from stage execution.
	 * <p>
	 * Default implementation converts exceptions to StageResult.Failure.
	 * Subclasses can override for custom error handling.
	 *
	 * @param context the processing context
	 * @param exception the exception thrown by executeStage()
	 * @return a Failure result with error details
	 */
	protected StageResult handleError(ProcessingContext context, Exception exception)
	{
		String message = String.format(
				"Stage '%s' failed for file %s: %s",
				getStageName(),
				context.filePath(),
				exception.getMessage());
		return new StageResult.Failure(message, exception);
	}
}
