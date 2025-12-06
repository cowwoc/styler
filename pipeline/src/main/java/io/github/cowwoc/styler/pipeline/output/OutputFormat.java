package io.github.cowwoc.styler.pipeline.output;

/**
 * Output format for violation reports.
 * <p>
 * <b>Thread-safety</b>: This enum is immutable and thread-safe.
 */
public enum OutputFormat
{
	/**
	 * JSON format - machine-readable, suitable for AI agents and CI systems.
	 * Includes structured metadata, violation counts, and violation details.
	 */
	JSON,

	/**
	 * Human-readable format - natural language output suitable for terminal display.
	 * Includes formatted tables, summaries, and context information.
	 */
	HUMAN
}
