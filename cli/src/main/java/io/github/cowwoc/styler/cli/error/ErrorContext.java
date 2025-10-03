package io.github.cowwoc.styler.cli.error;

import io.github.cowwoc.styler.ast.SourceRange;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Immutable context information for an error that occurred during styler operations.
 * <p>
 * ErrorContext preserves all information necessary to generate meaningful error
 * messages with source location, context, and fix suggestions. This record serves
 * as the primary data transfer object for error reporting throughout the system.
 *
 * @param filePath the file where the error occurred, never {@code null}
 * @param location the precise location within the file, never {@code null}
 * @param sourceText the original source text for context extraction, never {@code null}
 * @param category the type of error that occurred, never {@code null}
 * @param severity the severity level of this error, never {@code null}
 * @param errorCode a unique identifier for this type of error, never {@code null}
 * @param message a human-readable description of the error, never {@code null}
 * @param suggestedFix an optional suggested fix for the error, may be {@code null}
 */
public record ErrorContext(
	Path filePath,
	SourceRange location,
	String sourceText,
	ErrorCategory category,
	ErrorSeverity severity,
	String errorCode,
	String message,
	String suggestedFix)
{
	/**
	 * Compact constructor that validates error context parameters.
	 */
	public ErrorContext
	{
		Objects.requireNonNull(filePath, "File path cannot be null");
		Objects.requireNonNull(location, "Source location cannot be null");
		Objects.requireNonNull(sourceText, "Source text cannot be null");
		Objects.requireNonNull(category, "Error category cannot be null");
		Objects.requireNonNull(severity, "Error severity cannot be null");
		Objects.requireNonNull(errorCode, "Error code cannot be null");
		Objects.requireNonNull(message, "Error message cannot be null");

		// Validate error code format (CATEGORY-NNNN)
		if (!errorCode.matches("^[A-Z]+-\\d{4}$"))
		{
			throw new IllegalArgumentException(
				"Error code must follow format 'CATEGORY-NNNN', got: " + errorCode);
		}
	}

	/**
	 * Creates an ErrorContext for a parse error.
	 *
	 * @param filePath the file where the parse error occurred, never {@code null}
	 * @param location the location of the syntax error, never {@code null}
	 * @param sourceText the source text being parsed, never {@code null}
	 * @param message the parse error message, never {@code null}
	 * @return a new ErrorContext for the parse error, never {@code null}
	 * @throws IllegalArgumentException if any parameter is {@code null}
	 */
	public static ErrorContext parseError(Path filePath, SourceRange location,
	                                     String sourceText, String message)
	{
		return new ErrorContext(
			filePath,
			location,
			sourceText,
			ErrorCategory.PARSE,
			ErrorSeverity.ERROR,
			"PARSE-0001",
			message,
			null);
	}

	/**
	 * Creates an ErrorContext for a configuration error.
	 *
	 * @param filePath the configuration file with the error, never {@code null}
	 * @param location the location within the config file, never {@code null}
	 * @param sourceText the configuration file content, never {@code null}
	 * @param message the configuration error message, never {@code null}
	 * @param suggestedFix an optional suggested fix, may be {@code null}
	 * @return a new ErrorContext for the configuration error, never {@code null}
	 * @throws IllegalArgumentException if any required parameter is {@code null}
	 */
	public static ErrorContext configError(Path filePath, SourceRange location,
	                                      String sourceText, String message,
	                                      String suggestedFix)
	{
		return new ErrorContext(
			filePath,
			location,
			sourceText,
			ErrorCategory.CONFIG,
			ErrorSeverity.ERROR,
			"CONFIG-0001",
			message,
			suggestedFix);
	}

	/**
	 * Creates an ErrorContext for a formatting violation.
	 *
	 * @param filePath the file with the formatting violation, never {@code null}
	 * @param location the location of the violation, never {@code null}
	 * @param sourceText the source text being formatted, never {@code null}
	 * @param ruleId the ID of the rule that detected the violation, never {@code null}
	 * @param message the violation message, never {@code null}
	 * @param severity the severity of the violation, never {@code null}
	 * @param suggestedFix an optional suggested fix, may be {@code null}
	 * @return a new ErrorContext for the formatting violation, never {@code null}
	 * @throws IllegalArgumentException if any required parameter is {@code null}
	 */
	public static ErrorContext formatViolation(Path filePath, SourceRange location,
	                                          String sourceText, String ruleId,
	                                          String message, ErrorSeverity severity,
	                                          String suggestedFix)
	{
		return new ErrorContext(
			filePath,
			location,
			sourceText,
			ErrorCategory.FORMAT,
			severity,
			"FORMAT-" + String.format("%04d", Math.abs(ruleId.hashCode() % 10_000)),
			message,
			suggestedFix);
	}

	/**
	 * Creates an ErrorContext for a system error.
	 *
	 * @param filePath the file being processed when the system error occurred, never {@code null}
	 * @param message the system error message, never {@code null}
	 * @return a new ErrorContext for the system error, never {@code null}
	 * @throws IllegalArgumentException if any parameter is {@code null}
	 */
	public static ErrorContext systemError(Path filePath, String message)
	{
		return new ErrorContext(
			filePath,
			SourceRange.single(io.github.cowwoc.styler.ast.SourcePosition.start()),
			"",
			ErrorCategory.SYSTEM,
			ErrorSeverity.ERROR,
			"SYSTEM-0001",
			message,
			null);
	}

	/**
	 * Returns whether this error has a suggested fix available.
	 *
	 * @return {@code true} if a suggested fix is provided, {@code false} otherwise
	 */
	public boolean hasSuggestedFix()
	{
		return suggestedFix != null && !suggestedFix.isBlank();
	}

	/**
	 * Returns whether this error should halt processing.
	 *
	 * @return {@code true} if processing should stop due to this error, {@code false} otherwise
	 */
	public boolean shouldHaltProcessing()
	{
		return severity.shouldHaltProcessing() && !category.isRecoverable();
	}

	/**
	 * Returns the line number where this error occurred.
	 *
	 * @return the {@code 1}-based line number
	 */
	public int getLineNumber()
	{
		return location.start().line();
	}

	/**
	 * Returns the column number where this error occurred.
	 *
	 * @return the {@code 1}-based column number
	 */
	public int getColumnNumber()
	{
		return location.start().column();
	}

	/**
	 * Returns a short identifier combining category and code for this error.
	 *
	 * @return a short error identifier in format "CATEGORY:CODE", never {@code null}
	 */
	public String getShortId()
	{
		return category.name() + ":" + errorCode;
	}
}