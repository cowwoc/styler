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
	 * @param errorContext the error to format, never null
	 * @throws IllegalArgumentException if {@code errorContext} is null
	 * @return the formatted error message, never null
	 */
	String formatError(ErrorContext errorContext);

	/**
	 * Formats multiple errors as a cohesive report.
	 *
	 * @param errors the errors to format, never null
	 * @throws IllegalArgumentException if {@code errors} is null
	 * @return a formatted error report containing all errors, never null
	 */
	String formatErrorReport(java.util.List<ErrorContext> errors);

	/**
	 * Formats a summary of error statistics and outcomes.
	 *
	 * @param summary the error summary information, never null
	 * @throws IllegalArgumentException if {@code summary} is null
	 * @return a formatted summary report, never null
	 */
	String formatSummary(ErrorSummary summary);

	/**
	 * Returns the MIME type for this formatter's output format.
	 *
	 * @return the MIME type (e.g., "text/plain", "application/json"), never null
	 */
	String getMimeType();

	/**
	 * Returns whether this formatter supports colored output.
	 *
	 * @return true if this formatter can produce colored output, false otherwise
	 */
	boolean supportsColors();

	/**
	 * Summary information about a collection of errors.
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
		String operationType
	)
	{
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
				throw new IllegalArgumentException("Operation type cannot be null");
			}
		}

		/**
		 * Creates an error summary from a list of error contexts.
		 *
		 * @param errors the errors to summarize, never null
		 * @param processingTimeMs the total processing time in milliseconds
		 * @param operationType the type of operation that was performed, never null
		 * @throws IllegalArgumentException if any parameter is invalid
		 * @return a summary of the provided errors, never null
		 */
		public static ErrorSummary from(java.util.List<ErrorContext> errors,
		                               long processingTimeMs, String operationType)
		{
			if (errors == null)
			{
				throw new IllegalArgumentException("Errors list cannot be null");
			}

			int parse = 0, config = 0, format = 0, validation = 0, system = 0, critical = 0;

			for (ErrorContext error : errors)
			{
				switch (error.category())
				{
					case PARSE -> parse++;
					case CONFIG -> config++;
					case FORMAT -> format++;
					case VALIDATE -> validation++;
					case SYSTEM -> system++;
				}

				if (error.severity() == ErrorSeverity.ERROR)
				{
					critical++;
				}
			}

			return new ErrorSummary(
				errors.size(), parse, config, format, validation, system,
				critical, processingTimeMs, operationType
			);
		}

		/**
		 * Returns whether this summary indicates successful completion.
		 *
		 * @return true if no critical errors occurred, false otherwise
		 */
		public boolean isSuccess()
		{
			return criticalErrors == 0;
		}

		/**
		 * Returns whether this summary indicates processing should be halted.
		 *
		 * @return true if critical errors require halting processing, false otherwise
		 */
		public boolean shouldHaltProcessing()
		{
			return criticalErrors > 0;
		}
	}
}