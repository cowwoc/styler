package io.github.cowwoc.styler.cli.pipeline;

/**
 * Simple identity stage for testing that passes input through unchanged.
 * <p>
 * This stage demonstrates the {@link AbstractPipelineStage} Template Method pattern and provides
 * a simple, predictable stage for testing pipeline execution.
 *
 * @param <T> the input and output type (same for identity function)
 */
public final class IdentityStage<T> extends AbstractPipelineStage<T, T>
{
	private final String stageId;

	/**
	 * Creates a new identity stage with the default stage ID.
	 */
	public IdentityStage()
	{
		this("identity");
	}

	/**
	 * Creates a new identity stage with the specified stage ID.
	 *
	 * @param stageId the stage identifier (never {@code null} or empty)
	 * @throws NullPointerException     if {@code stageId} is {@code null}
	 * @throws IllegalArgumentException if {@code stageId} is empty
	 */
	public IdentityStage(String stageId)
	{
		super();
		this.stageId = stageId;
	}

	@Override
	protected T process(T input, ProcessingContext context)
	{
		// Identity function: output = input
		return input;
	}

	@Override
	public String getStageId()
	{
		return stageId;
	}
}
