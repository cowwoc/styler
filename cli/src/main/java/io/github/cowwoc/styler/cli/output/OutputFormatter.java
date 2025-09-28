package io.github.cowwoc.styler.cli.output;

import java.util.List;

/**
 * Interface for formatting CLI output in different formats.
 * <p>
 * Provides a pluggable architecture for output formatting, allowing
 * the same data to be presented in human-readable or machine-readable
 * formats based on user preferences and automation requirements.
 */
public interface OutputFormatter
{
	/**
	 * Formats the results of a formatting or checking operation.
	 *
	 * @param results the operation results to format
	 * @return formatted output string
	 */
	String formatResults(FormattingResults results);

	/**
	 * Formats a single violation for display.
	 *
	 * @param violation the violation to format
	 * @return formatted violation string
	 */
	String formatViolation(FormattingViolation violation);

	/**
	 * Formats a summary of the operation.
	 *
	 * @param summary the summary information
	 * @return formatted summary string
	 */
	String formatSummary(OperationSummary summary);

	/**
	 * Returns the MIME type for this formatter's output.
	 *
	 * @return the MIME type (e.g., "text/plain", "application/json")
	 */
	String getMimeType();

	/**
	 * Results of a formatting or checking operation.
	 */
	record FormattingResults(
		List<FormattingViolation> violations,
		List<String> processedFiles,
		OperationSummary summary
	) {}

	/**
	 * A single formatting violation.
	 */
	record FormattingViolation(
		String filePath,
		int line,
		int column,
		String ruleId,
		String message,
		SeverityLevel severity,
		String suggestedFix
	) {}

	/**
	 * Summary of the operation.
	 */
	record OperationSummary(
		int totalFiles,
		int processedFiles,
		int violationCount,
		int errorCount,
		long processingTimeMs,
		String operationType
	) {}

	/**
	 * Severity levels for violations.
	 */
	enum SeverityLevel
	{
		ERROR, WARN, INFO, DEBUG
	}
}