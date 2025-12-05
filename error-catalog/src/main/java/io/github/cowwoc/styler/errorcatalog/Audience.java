package io.github.cowwoc.styler.errorcatalog;

/**
 * Target audience for error message formatting.
 */
public enum Audience
{
	/**
	 * AI agent (Claude Code or other automated tools).
	 * <p>
	 * Formatting characteristics:
	 * <ul>
	 *   <li>Structured, machine-readable output
	 *   <li>Key-value pairs for error details
	 *   <li>Programmatic fix strategies
	 *   <li>Minimal prose, maximum parsability
	 * </ul>
	 */
	AI,

	/**
	 * Human developer.
	 * <p>
	 * Formatting characteristics:
	 * <ul>
	 *   <li>Narrative explanations with context
	 *   <li>Source code snippets with visual indicators
	 *   <li>Common causes and suggested fixes
	 *   <li>Examples and actionable guidance
	 * </ul>
	 */
	HUMAN;

	/**
	 * Detects the target audience based on execution environment.
	 * <p>
	 * Detection strategy:
	 * <ol>
	 *   <li>Check {@code CLAUDE_SESSION_ID} environment variable (AI agent indicator)
	 *   <li>Check {@code AI_AGENT_MODE} environment variable
	 *   <li>Check if output is a TTY (human interactive terminal)
	 *   <li>Default to HUMAN for safety
	 * </ol>
	 *
	 * @return AI if running in agent context, HUMAN otherwise
	 */
	public static Audience detect()
	{
		// Check environment variables for AI context
		String claudeSession = System.getenv("CLAUDE_SESSION_ID");
		String aiMode = System.getenv("AI_AGENT_MODE");

		if (claudeSession != null || "true".equals(aiMode))
			return AI;

		// Check if stdout is a TTY (human interactive terminal)
		// Note: System.console() returns null when output is redirected or in non-interactive mode
		boolean isTTY = System.console() != null;

		// Interactive terminal suggests human user
		if (isTTY)
			return HUMAN;

		// Default to human format for safety (more verbose but clearer)
		return HUMAN;
	}
}
