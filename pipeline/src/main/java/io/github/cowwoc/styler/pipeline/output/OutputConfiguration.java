package io.github.cowwoc.styler.pipeline.output;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.time.Duration;

/**
 * Immutable configuration for violation report output.
 * <p>
 * This record encapsulates output formatting preferences. Validation occurs in the compact
 * constructor using requireThat() for fail-fast error handling.
 * <p>
 * <b>Thread-safety</b>: This record is immutable.
 *
 * @param outputFormat the desired output format (JSON or HUMAN)
 * @param includeSourceSnippets whether to include source code snippets in output
 * @param maxViolationsPerFile maximum violations to report per file (0 = unlimited)
 * @param processingDuration the time spent processing the file
 */
public record OutputConfiguration(
	OutputFormat outputFormat,
	boolean includeSourceSnippets,
	int maxViolationsPerFile,
	Duration processingDuration)
{
	/**
	 * Creates an OutputConfiguration with validation of all parameters.
	 *
	 * @param outputFormat the desired output format (JSON or HUMAN)
	 * @param includeSourceSnippets whether to include source code snippets in output
	 * @param maxViolationsPerFile maximum violations to report per file (0 = unlimited)
	 * @param processingDuration the time spent processing the file
	 * @throws NullPointerException if {@code outputFormat} or {@code processingDuration} is {@code null}
	 * @throws IllegalArgumentException if {@code maxViolationsPerFile} is negative
	 */
	public OutputConfiguration
	{
		requireThat(outputFormat, "outputFormat").isNotNull();
		requireThat(processingDuration, "processingDuration").isNotNull();
		requireThat(maxViolationsPerFile, "maxViolationsPerFile").isGreaterThanOrEqualTo(0);
	}
}
