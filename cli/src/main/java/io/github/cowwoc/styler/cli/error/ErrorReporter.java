package io.github.cowwoc.styler.cli.error;

import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.FormattingViolation;
import io.github.cowwoc.styler.formatter.api.ViolationSeverity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central coordinator for error reporting across all styler operations.
 * <p>
 * ErrorReporter provides a unified interface for collecting, formatting, and
 * presenting errors from different sources (parser, config, formatting rules).
 * It supports both human-readable terminal output and machine-readable JSON
 * formats for integration with development tools and CI/CD systems.
 */
public final class ErrorReporter
{
	private final ErrorFormatter formatter;
	private final List<ErrorContext> collectedErrors;
	private final long startTime;
	private int maxErrors;
	private boolean shouldHalt;

	/**
	 * Creates an error reporter with the specified formatter.
	 *
	 * @param formatter the error formatter to use for output, never null
	 * @throws IllegalArgumentException if {@code formatter} is null
	 */
	public ErrorReporter(ErrorFormatter formatter)
	{
		if (formatter == null)
		{
			throw new IllegalArgumentException("Error formatter cannot be null");
		}
		this.formatter = formatter;
		this.collectedErrors = new CopyOnWriteArrayList<>();
		this.startTime = System.currentTimeMillis();
		this.maxErrors = 100; // Default limit
		this.shouldHalt = false;
	}

	/**
	 * Creates an error reporter with human-readable formatting and auto-detected color support.
	 */
	public ErrorReporter()
	{
		this(new HumanErrorFormatter());
	}

	/**
	 * Creates an error reporter with the specified output format.
	 *
	 * @param machineReadable true for JSON output, false for human-readable output
	 * @param enableColors whether to enable color output (ignored for machine-readable)
	 */
	public ErrorReporter(boolean machineReadable, boolean enableColors)
	{
		this(machineReadable ? new MachineErrorFormatter() : new HumanErrorFormatter(enableColors));
	}

	/**
	 * Reports a parse error that occurred during source code analysis.
	 *
	 * @param parseException the parse exception containing error details, never null
	 * @param sourceFile the file being parsed, never null
	 * @param sourceText the source text being parsed, never null
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public void reportParseError(Exception parseException, Path sourceFile, String sourceText)
	{
		if (parseException == null)
		{
			throw new IllegalArgumentException("Parse exception cannot be null");
		}
		if (sourceFile == null)
		{
			throw new IllegalArgumentException("Source file cannot be null");
		}
		if (sourceText == null)
		{
			throw new IllegalArgumentException("Source text cannot be null");
		}

		// Extract location information from exception if available
		SourceRange location = extractLocationFromException(parseException, sourceText);
		String message = parseException.getMessage();

		if (message == null || message.trim().isEmpty())
		{
			message = "Parse error: " + parseException.getClass().getSimpleName();
		}

		ErrorContext errorContext = ErrorContext.parseError(sourceFile, location, sourceText, message);
		addError(errorContext);
	}

	/**
	 * Reports a configuration error that occurred during config file loading or validation.
	 *
	 * @param configException the configuration exception, never null
	 * @param configFile the configuration file with the error, never null
	 * @param configText the configuration file content, never null
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public void reportConfigError(Exception configException, Path configFile, String configText)
	{
		Objects.requireNonNull(configException, "Config exception cannot be null");
		Objects.requireNonNull(configFile, "Config file cannot be null");
		Objects.requireNonNull(configText, "Config text cannot be null");

		SourceRange location = extractLocationFromException(configException, configText);
		String message = configException.getMessage();

		if (message == null || message.trim().isEmpty())
		{
			message = "Configuration error: " + configException.getClass().getSimpleName();
		}

		String suggestedFix = FixSuggestionProvider.generateSuggestion(
			ErrorContext.configError(configFile, location, configText, message, null)
		);

		ErrorContext errorContext = ErrorContext.configError(
			configFile, location, configText, message, suggestedFix);
		addError(errorContext);
	}

	/**
	 * Reports a formatting violation detected by a formatting rule.
	 *
	 * @param violation the formatting violation to report, never null
	 * @param sourceFile the file containing the violation, never null
	 * @param sourceText the source text for context extraction, never null
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public void reportViolation(FormattingViolation violation, Path sourceFile, String sourceText)
	{
		if (violation == null)
		{
			throw new IllegalArgumentException("Formatting violation cannot be null");
		}
		if (sourceFile == null)
		{
			throw new IllegalArgumentException("Source file cannot be null");
		}
		if (sourceText == null)
		{
			throw new IllegalArgumentException("Source text cannot be null");
		}

		ErrorSeverity severity = convertViolationSeverity(violation.getSeverity());
		String suggestedFix = violation.hasSuggestedFix() ?
			violation.getSuggestedFix() :
			FixSuggestionProvider.generateSuggestion(
				ErrorContext.formatViolation(sourceFile, violation.getLocation(), sourceText,
					violation.getRuleId(), violation.getMessage(), severity, null)
			);

		ErrorContext errorContext = ErrorContext.formatViolation(
			sourceFile, violation.getLocation(), sourceText, violation.getRuleId(),
			violation.getMessage(), severity, suggestedFix);
		addError(errorContext);
	}

	/**
	 * Reports a system error that occurred during file processing or system operations.
	 *
	 * @param systemException the system exception, never null
	 * @param relatedFile the file being processed when the error occurred, never null
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public void reportSystemError(Exception systemException, Path relatedFile)
	{
		Objects.requireNonNull(systemException, "System exception cannot be null");
		Objects.requireNonNull(relatedFile, "Related file cannot be null");

		String message = systemException.getMessage();
		if (message == null || message.trim().isEmpty())
		{
			message = "System error: " + systemException.getClass().getSimpleName();
		}

		ErrorContext errorContext = ErrorContext.systemError(relatedFile, message);
		addError(errorContext);
	}

	/**
	 * Reports an error context directly (used by ErrorCollector integration).
	 *
	 * @param errorContext the error context to report, never null
	 * @throws IllegalArgumentException if {@code errorContext} is null
	 */
	public void reportError(ErrorContext errorContext)
	{
		if (errorContext == null)
		{
			throw new IllegalArgumentException("Error context cannot be null");
		}
		addError(errorContext);
	}

	/**
	 * Adds an error context to the collection and checks halt conditions.
	 */
	private void addError(ErrorContext errorContext)
	{
		// Check error limit before adding
		if (collectedErrors.size() >= maxErrors)
		{
			shouldHalt = true;
			return; // Don't add more errors past the limit
		}

		collectedErrors.add(errorContext);

		// Check if we should halt processing
		if (errorContext.shouldHaltProcessing())
		{
			shouldHalt = true;
		}
	}

	/**
	 * Returns all collected errors.
	 *
	 * @return an immutable list of collected errors, never null
	 */
	public List<ErrorContext> getErrors()
	{
		return new ArrayList<>(collectedErrors);
	}

	/**
	 * Returns the number of errors collected.
	 *
	 * @return the error count
	 */
	public int getErrorCount()
	{
		return collectedErrors.size();
	}

	/**
	 * Returns whether any critical errors have been reported.
	 *
	 * @return true if critical errors exist that should halt processing
	 */
	public boolean hasErrors()
	{
		return !collectedErrors.isEmpty();
	}

	/**
	 * Returns whether processing should be halted due to critical errors.
	 *
	 * @return true if processing should stop, false otherwise
	 */
	public boolean shouldHaltProcessing()
	{
		return shouldHalt;
	}

	/**
	 * Clears all collected errors and resets halt condition.
	 */
	public void clearErrors()
	{
		collectedErrors.clear();
		shouldHalt = false;
	}

	/**
	 * Sets the maximum number of errors to collect before halting.
	 *
	 * @param maxErrors the maximum error count, must be positive
	 * @throws IllegalArgumentException if {@code maxErrors} is not positive
	 */
	public void setMaxErrors(int maxErrors)
	{
		if (maxErrors <= 0)
		{
			throw new IllegalArgumentException("Max errors must be positive: " + maxErrors);
		}
		this.maxErrors = maxErrors;
	}

	/**
	 * Formats all collected errors as a complete report.
	 *
	 * @return a formatted error report, never null
	 */
	public String formatErrorReport()
	{
		return formatter.formatErrorReport(getErrors());
	}

	/**
	 * Formats a summary of all collected errors.
	 *
	 * @param operationType the type of operation that generated these errors, never null
	 * @throws IllegalArgumentException if {@code operationType} is null
	 * @return a formatted error summary, never null
	 */
	public String formatSummary(String operationType)
	{
		if (operationType == null)
		{
			throw new IllegalArgumentException("Operation type cannot be null");
		}

		long processingTime = System.currentTimeMillis() - startTime;
		ErrorFormatter.ErrorSummary summary = ErrorFormatter.ErrorSummary.from(
			getErrors(), processingTime, operationType);

		return formatter.formatSummary(summary);
	}

	/**
	 * Formats a single error for immediate display.
	 *
	 * @param errorContext the error to format, never null
	 * @throws IllegalArgumentException if {@code errorContext} is null
	 * @return a formatted error message, never null
	 */
	public String formatError(ErrorContext errorContext)
	{
		Objects.requireNonNull(errorContext, "Error context cannot be null");
		return formatter.formatError(errorContext);
	}

	/**
	 * Returns the MIME type for this reporter's output format.
	 *
	 * @return the MIME type (e.g., "text/plain", "application/json"), never null
	 */
	public String getMimeType()
	{
		return formatter.getMimeType();
	}

	/**
	 * Returns whether this reporter supports colored output.
	 *
	 * @return true if colored output is supported, false otherwise
	 */
	public boolean supportsColors()
	{
		return formatter.supportsColors();
	}

	/**
	 * Converts ViolationSeverity to ErrorSeverity for unified error handling.
	 */
	private ErrorSeverity convertViolationSeverity(ViolationSeverity violationSeverity)
	{
		return switch (violationSeverity)
		{
			case ERROR -> ErrorSeverity.ERROR;
			case WARNING -> ErrorSeverity.WARNING;
			case INFO -> ErrorSeverity.INFO;
		};
	}

	/**
	 * Extracts source location from exception when possible.
	 */
	private SourceRange extractLocationFromException(Exception exception, String sourceText)
	{
		// Try to extract line/column information from exception message
		String message = exception.getMessage();
		if (message != null)
		{
			// Look for common patterns like "line 15, column 23" or "at line 15"
			java.util.regex.Pattern linePattern = java.util.regex.Pattern.compile(
				"(?:line|row)\\s*(\\d+)(?:.*(?:column|col)\\s*(\\d+))?",
				java.util.regex.Pattern.CASE_INSENSITIVE);
			java.util.regex.Matcher matcher = linePattern.matcher(message);

			if (matcher.find())
			{
				int line = Integer.parseInt(matcher.group(1));
				int column = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 1;

				try
				{
					io.github.cowwoc.styler.ast.SourcePosition position =
						new io.github.cowwoc.styler.ast.SourcePosition(line, column);
					return SourceRange.single(position);
				}
				catch (IllegalArgumentException e)
				{
					// Invalid line/column numbers, fall back to default
				}
			}
		}

		// Default to start of file if no location can be extracted
		return SourceRange.single(io.github.cowwoc.styler.ast.SourcePosition.start());
	}
}