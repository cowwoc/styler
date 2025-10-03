package io.github.cowwoc.styler.cli.error;

/**
 * Strategy interface for formatting error messages in different output formats.
 * <p>
 * This interface enables pluggable error formatting strategies that can adapt
 * error presentation to different audiences (human developers vs AI agents)
 * and output contexts (terminal vs file vs structured data).
 */
public interface ErrorFormatter
{
	/**
	 * Formats a single error for display in the target format.
	 *
	 * @param errorContext the error to format, never {@code null}
	 * @throws NullPointerException if {@code errorContext} is {@code null}
	 * @return the formatted error message, never {@code null}
	 */
	String formatError(ErrorContext errorContext);

	/**
	 * Formats multiple errors as a cohesive report.
	 *
	 * @param errors the errors to format, never {@code null}
	 * @throws NullPointerException if {@code errors} is {@code null}
	 * @return a formatted error report containing all errors, never {@code null}
	 */
	String formatErrorReport(java.util.List<ErrorContext> errors);

	/**
	 * Formats a summary of error statistics and outcomes.
	 *
	 * @param summary the error summary information, never {@code null}
	 * @throws NullPointerException if {@code summary} is {@code null}
	 * @return a formatted summary report, never {@code null}
	 */
	String formatSummary(ErrorSummary summary);

	/**
	 * Returns the MIME type for this formatter's output format.
	 *
	 * @return the MIME type (e.g., "text/plain", "application/json"), never {@code null}
	 */
	String getMimeType();

	/**
	 * Returns whether this formatter supports colored output.
	 *
	 * @return {@code true} if this formatter can produce colored output, {@code false} otherwise
	 */
	boolean supportsColors();

	/**
	 * Summary information about a collection of errors.
	 *
	 * @param totalErrors the total number of errors
	 * @param parseErrors the number of parse errors
	 * @param configErrors the number of configuration errors
	 * @param formatViolations the number of formatting violations
	 * @param validationErrors the number of validation errors
	 * @param systemErrors the number of system errors
	 * @param criticalErrors the number of critical errors
	 * @param processingTimeMs the processing time in milliseconds
	 * @param operationType the type of operation performed
	 */
	record ErrorSummary(
		int totalErrors,
		int parseErrors,
		int configErrors,
		int formatViolations,
		int validationErrors,
		int systemErrors,
		int criticalErrors,
		long processingTimeMs,
		String operationType)
	{
		/**
		 * Compact constructor that validates error summary parameters.
		 *
		 * @param totalErrors the total number of errors
		 * @param parseErrors the number of parse errors
		 * @param configErrors the number of configuration errors
		 * @param formatViolations the number of formatting violations
		 * @param validationErrors the number of validation errors
		 * @param systemErrors the number of system errors
		 * @param criticalErrors the number of critical errors
		 * @param processingTimeMs the processing time in milliseconds
		 * @param operationType the type of operation performed
		 */
		public ErrorSummary
		{
			if (totalErrors < 0)
			{
				throw new IllegalArgumentException("Total errors cannot be negative: " + totalErrors);
			}
			if (processingTimeMs < 0)
			{
				throw new IllegalArgumentException("Processing time cannot be negative: " + processingTimeMs);
			}
			if (operationType == null)
			{
				throw new NullPointerException("Operation type cannot be null");
			}
		}

		/**
		 * Creates an error summary from a list of error contexts.
		 *
		 * @param errors the errors to summarize, never {@code null}
		 * @param processingTimeMs the total processing time in milliseconds
		 * @param operationType the type of operation that was performed, never {@code null}
		 * @throws NullPointerException if {@code errors} or {@code operationType} is {@code null}
		 * @throws IllegalArgumentException if {@code totalErrors} or {@code processingTimeMs} is negative
		 * @return a summary of the provided errors, never {@code null}
		 */
		public static ErrorSummary from(java.util.List<ErrorContext> errors,
		                               long processingTimeMs, String operationType)
		{
			if (errors == null)
			{
				throw new NullPointerException("Errors list cannot be null");
			}

			int parse = 0;
			int config = 0;
			int format = 0;
			int validation = 0;
			int system = 0;
			int critical = 0;

			for (ErrorContext error : errors)
			{
				switch (error.category())
				{
					case PARSE ->
					{
						++parse;
					}
					case CONFIG ->
					{
						++config;
					}
					case FORMAT ->
					{
						++format;
					}
					case VALIDATE ->
					{
						++validation;
					}
					case SYSTEM ->
					{
						++system;
					}
					}

			if (error.severity() == ErrorSeverity.ERROR)
				{
					++critical;
				}
			}

			return new ErrorSummary(
				errors.size(), parse, config, format, validation, system,
				critical, processingTimeMs, operationType);
		}

		/**
		 * Returns whether this summary indicates successful completion.
		 *
		 * @return {@code true} if no critical errors occurred, {@code false} otherwise
		 */
		public boolean isSuccess()
		{
			return criticalErrors == 0;
		}

		/**
		 * Returns whether this summary indicates processing should be halted.
		 *
		 * @return {@code true} if critical errors require halting processing, {@code false} otherwise
		 */
		public boolean shouldHaltProcessing()
		{
			return criticalErrors > 0;
		}
	}
}