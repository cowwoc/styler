package io.github.cowwoc.styler.cli.pipeline;

import io.github.cowwoc.styler.cli.pipeline.recovery.ErrorRecoveryStrategy;
import io.github.cowwoc.styler.cli.pipeline.recovery.SkipFileStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Abstract base class implementing the Template Method pattern for pipeline stages.
 * <p>
 * This class provides a common lifecycle for all pipeline stages, eliminating code duplication
 * and ensuring consistent resource management. Subclasses override only the core processing logic
 * via {@link #process(Object, ProcessingContext)}.
 * <p>
 * The template method {@link #execute(Object, ProcessingContext)} enforces this lifecycle:
 * <ol>
 *     <li>Pre-execution validation - {@link #validateInput(Object, ProcessingContext)}</li>
 *     <li>Resource acquisition - {@link #acquireResources(ProcessingContext)}</li>
 *     <li>Core processing - {@link #process(Object, ProcessingContext)} (subclass override)</li>
 *     <li>Resource cleanup - {@link #releaseResources(ProcessingContext)} (in finally block)</li>
 *     <li>Result packaging - {@link StageResult}</li>
 * </ol>
 * <p>
 * Example subclass:
 * <pre>{@code
 * public final class ParseStage extends AbstractPipelineStage<String, AST> {
 *     private final Parser parser;
 *
 *     public ParseStage(Parser parser) {
 *         this.parser = parser;
 *     }
 *
 *     {@literal @}Override
 *     protected AST process(String sourceCode, ProcessingContext context) throws PipelineException {
 *         return parser.parse(sourceCode);
 *     }
 * }
 * }</pre>
 *
 * @param <INPUT>  the input type consumed by this stage
 * @param <OUTPUT> the output type produced by this stage
 */
// Descriptive names improve pipeline readability
@SuppressWarnings("PMD.TypeParameterNamingConventions")
public abstract class AbstractPipelineStage<INPUT, OUTPUT> implements PipelineStage<INPUT, OUTPUT>
{
	/**
	 * Logger instance for subclass access.
	 */
	// Standard SLF4J logger naming convention
	@SuppressWarnings("PMD.FieldNamingConventions")
	protected static final Logger logger = LoggerFactory.getLogger(AbstractPipelineStage.class);

	private final ErrorRecoveryStrategy recoveryStrategy;

	/**
	 * Creates a new pipeline stage with default error recovery (skip file on error).
	 */
	protected AbstractPipelineStage()
	{
		this(new SkipFileStrategy());
	}

	/**
	 * Creates a new pipeline stage with the specified error recovery strategy.
	 *
	 * @param recoveryStrategy the error recovery strategy to use (never {@code null})
	 * @throws NullPointerException if {@code recoveryStrategy} is {@code null}
	 */
	protected AbstractPipelineStage(ErrorRecoveryStrategy recoveryStrategy)
	{
		requireThat(recoveryStrategy, "recoveryStrategy").isNotNull();
		this.recoveryStrategy = recoveryStrategy;
	}

	@Override
	public final StageResult<OUTPUT> execute(INPUT input, ProcessingContext context)
		throws PipelineException
	{
		requireThat(input, "input").isNotNull();
		requireThat(context, "context").isNotNull();

		logger.debug("Executing {} stage for file: {}", getStageId(), context.sourceFile());

		try
		{
			// Step 1: Pre-execution validation
			validateInput(input, context);

			// Step 2: Resource acquisition
			acquireResources(context);

			try
			{
				// Step 3: Core processing (subclass-specific)
				OUTPUT output = process(input, context);
				logger.debug("{} stage completed successfully for file: {}", getStageId(), context.sourceFile());
				return StageResult.success(output);
			}
			finally
			{
				// Step 4: Resource cleanup (always executed)
				releaseResources(context);
			}
		}
		catch (PipelineException e)
		{
			return handleError(e);
		}
		catch (Exception e)
		{
			PipelineException pipelineException = new PipelineException(
				"Unexpected error: " + e.getMessage(),
				context.sourceFile(),
				getStageId(),
				e);
			return handleError(pipelineException);
		}
	}

	/**
	 * Validates the input before processing.
	 * <p>
	 * The default implementation performs no validation. Subclasses can override this to add
	 * stage-specific input validation.
	 *
	 * @param input   the input to validate (never {@code null})
	 * @param context the processing context (never {@code null})
	 * @throws PipelineException if validation fails
	 */
	@SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
	protected void validateInput(INPUT input, ProcessingContext context) throws PipelineException
	{
		// Default: no validation
	}

	/**
	 * Acquires resources needed for processing.
	 * <p>
	 * The default implementation performs no resource acquisition. Subclasses can override this to
	 * acquire stage-specific resources (parsers, formatters, etc.).
	 * <p>
	 * Resources acquired here MUST be released in {@link #releaseResources(ProcessingContext)}.
	 *
	 * @param context the processing context (never {@code null})
	 * @throws PipelineException if resource acquisition fails
	 */
	@SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
	protected void acquireResources(ProcessingContext context) throws PipelineException
	{
		// Default: no resources to acquire
	}

	/**
	 * Processes the input and produces output.
	 * <p>
	 * This is the core processing logic that subclasses must implement. The implementation should:
	 * <ul>
	 *     <li>Not modify {@code input} or {@code context} (immutability)</li>
	 *     <li>Return a new output object (never {@code null})</li>
	 *     <li>Throw {@link PipelineException} on recoverable errors</li>
	 *     <li>Let unchecked exceptions propagate for programming errors</li>
	 * </ul>
	 *
	 * @param input   the input to process (never {@code null})
	 * @param context the processing context providing configuration (never {@code null})
	 * @return the processed output (never {@code null})
	 * @throws PipelineException if processing fails
	 */
	protected abstract OUTPUT process(INPUT input, ProcessingContext context) throws PipelineException;

	/**
	 * Releases resources acquired in {@link #acquireResources(ProcessingContext)}.
	 * <p>
	 * The default implementation performs no resource cleanup. Subclasses that acquire resources
	 * MUST override this to ensure proper cleanup.
	 * <p>
	 * This method is called in a finally block, so it executes even if processing fails.
	 *
	 * @param context the processing context (never {@code null})
	 */
	@SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
	protected void releaseResources(ProcessingContext context)
	{
		// Default: no resources to release
	}

	@Override
	public boolean supportsErrorRecovery()
	{
		return true;
	}

	/**
	 * Handles errors using the configured recovery strategy.
	 *
	 * @param exception the exception to handle (never {@code null})
	 * @return a stage result representing the recovery outcome (never {@code null})
	 */
	private StageResult<OUTPUT> handleError(PipelineException exception)
	{
		logger.warn("{} stage failed for file {}: {}", getStageId(),
			exception.getFilePath(), exception.getMessage());

		return recoveryStrategy.recover(exception, getStageId(), () ->
		{
			throw exception; // For SkipFileStrategy, this just returns failure
		});
	}
}
