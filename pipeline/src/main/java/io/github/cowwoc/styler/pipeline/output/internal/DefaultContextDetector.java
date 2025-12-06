package io.github.cowwoc.styler.pipeline.output.internal;

import io.github.cowwoc.styler.pipeline.output.ContextDetector;
import io.github.cowwoc.styler.pipeline.output.OutputFormat;

/**
 * Default implementation of ContextDetector with environment variable detection.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class DefaultContextDetector implements ContextDetector
{
	/**
	 * Creates a DefaultContextDetector.
	 */
	public DefaultContextDetector()
	{
	}

	@Override
	public OutputFormat detectContext()
	{
		// Prefer JSON for AI and CI environments
		if (isAiEnvironment() ||
			isCiEnvironment() ||
			!isInteractiveTerminal())
		{
			return OutputFormat.JSON;
		}
		// Default to HUMAN for interactive terminals
		return OutputFormat.HUMAN;
	}

	@Override
	public boolean isAiEnvironment()
	{
		// Check for AI API keys that indicate execution in an AI context
		return System.getenv("ANTHROPIC_API_KEY") != null ||
			System.getenv("OPENAI_API_KEY") != null ||
			System.getenv("CLAUDE_CODE") != null;
	}

	@Override
	public boolean isCiEnvironment()
	{
		// Check for common CI/CD environment variables
		return System.getenv("CI") != null ||
			System.getenv("GITHUB_ACTIONS") != null;
	}

	@Override
	public boolean isInteractiveTerminal()
	{
		// Check if running in an interactive terminal
		// System.console() returns null if output is not connected to a terminal
		return System.console() != null;
	}
}
