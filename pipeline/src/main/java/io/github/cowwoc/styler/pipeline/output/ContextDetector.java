package io.github.cowwoc.styler.pipeline.output;

/**
 * Interface for detecting whether code is executing in an AI agent context.
 * <p>
 * This detector examines environment variables and terminal interactivity to determine
 * the most appropriate output format. Useful for automatic format selection without
 * explicit user configuration.
 * <p>
 * <b>Thread-safety</b>: Implementations must be immutable and thread-safe.
 */
public interface ContextDetector
{
	/**
	 * Detects the execution context and returns the recommended output format.
	 * <p>
	 * Detection strategy:
	 * <ul>
	 *   <li>JSON for AI agents (ANTHROPIC_API_KEY, OPENAI_API_KEY set)</li>
	 *   <li>JSON for CI systems (CI, GITHUB_ACTIONS set)</li>
	 *   <li>JSON for non-interactive terminals (System.console() == null)</li>
	 *   <li>HUMAN for interactive terminals (default)</li>
	 * </ul>
	 *
	 * @return the recommended output format (JSON for AI/CI, HUMAN for interactive)
	 */
	OutputFormat detectContext();

	/**
	 * Checks if code is executing in an AI agent environment.
	 *
	 * @return true if AI environment variables are detected
	 */
	boolean isAiEnvironment();

	/**
	 * Checks if code is executing in a CI/CD system.
	 *
	 * @return true if CI environment variables are detected
	 */
	boolean isCiEnvironment();

	/**
	 * Checks if code is executing in an interactive terminal.
	 *
	 * @return true if terminal is interactive (System.console() != null)
	 */
	boolean isInteractiveTerminal();
}
