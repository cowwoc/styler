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
	 *
	 * @param violations the list of formatting violations found
	 * @param processedFiles the list of files that were processed
	 * @param summary the operation summary statistics
	 */
	record FormattingResults(
		List<FormattingViolation> violations,
		List<String> processedFiles,
		OperationSummary summary)
	{
	}

	/**
	 * A single formatting violation.
	 *
	 * @param filePath the path to the file containing the violation
	 * @param line the line number where the violation occurs
	 * @param column the column number where the violation occurs
	 * @param ruleId the identifier of the rule that was violated
	 * @param message the description of the violation
	 * @param severity the severity level of the violation
	 * @param suggestedFix the suggested fix for the violation, or {@code null} if none available
	 */
	record FormattingViolation(
		String filePath,
		int line,
		int column,
		String ruleId,
		String message,
		SeverityLevel severity,
		String suggestedFix)
	{
	}

	/**
	 * Summary of the operation.
	 *
	 * @param totalFiles the total number of files to process
	 * @param processedFiles the number of files successfully processed
	 * @param violationCount the total number of violations found
	 * @param errorCount the number of errors encountered
	 * @param processingTimeMs the processing time in milliseconds
	 * @param operationType the type of operation performed (e.g., "format", "check")
	 */
	record OperationSummary(
		int totalFiles,
		int processedFiles,
		int violationCount,
		int errorCount,
		long processingTimeMs,
		String operationType)
	{
	}

	/**
	 * Severity levels for violations.
	 */
	enum SeverityLevel
	{
		ERROR, WARN, INFO, DEBUG
	}
}