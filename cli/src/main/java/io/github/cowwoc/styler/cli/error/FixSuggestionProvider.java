package io.github.cowwoc.styler.cli.error;

import io.github.cowwoc.styler.ast.SourceRange;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Generates actionable fix suggestions for different types of errors.
 * <p>
 * This class applies rule-based analysis to common error patterns and provides
 * specific, actionable suggestions that help developers quickly resolve issues.
 * Suggestions are contextual and based on error type, location, and content.
 */
public final class FixSuggestionProvider
{
	// Common syntax error patterns
	private static final Pattern MISSING_SEMICOLON = Pattern.compile("missing ';'");
	private static final Pattern MISSING_BRACE = Pattern.compile("missing '\\{'|missing '\\}'");
	private static final Pattern INVALID_IDENTIFIER = Pattern.compile("invalid identifier|illegal character");
	private static final Pattern UNEXPECTED_TOKEN = Pattern.compile("unexpected token|syntax error");

	// Configuration error patterns
	private static final Pattern UNKNOWN_CONFIG_KEY = Pattern.compile("unknown.*key|unrecognized.*property");
	private static final Pattern INVALID_CONFIG_VALUE = Pattern.compile("invalid.*value|illegal.*value");

	/**
	 * Generates a fix suggestion for the given error context.
	 *
	 * @param errorContext the error context containing error details, never null
	 * @throws IllegalArgumentException if {@code errorContext} is null
	 * @return a suggested fix for the error, or null if no suggestion is available
	 */
	public static String generateSuggestion(ErrorContext errorContext)
	{
		if (errorContext == null)
		{
			throw new IllegalArgumentException("Error context cannot be null");
		}

		return switch (errorContext.category())
		{
			case PARSE -> generateParseSuggestion(errorContext);
			case CONFIG -> generateConfigSuggestion(errorContext);
			case FORMAT -> generateFormatSuggestion(errorContext);
			case VALIDATE -> generateValidationSuggestion(errorContext);
			case SYSTEM -> generateSystemSuggestion(errorContext);
		};
	}

	/**
	 * Generates suggestions for parse errors based on common syntax issues.
	 */
	private static String generateParseSuggestion(ErrorContext errorContext)
	{
		String message = errorContext.message().toLowerCase();
		String sourceSnippet = SourceSnippetExtractor.extractInlineSnippet(
			errorContext.sourceText(), errorContext.location());

		if (MISSING_SEMICOLON.matcher(message).find())
		{
			return "Add a semicolon ';' at the end of the statement";
		}

		if (MISSING_BRACE.matcher(message).find())
		{
			if (message.contains("'{'"))
			{
				return "Add an opening brace '{' to start the block";
			}
			else
			{
				return "Add a closing brace '}' to end the block";
			}
		}

		if (INVALID_IDENTIFIER.matcher(message).find())
		{
			return "Use a valid Java identifier (letters, digits, underscore, dollar sign)";
		}

		if (UNEXPECTED_TOKEN.matcher(message).find())
		{
			return "Check syntax around '" + sourceSnippet.trim() + "' - may need different operator or keyword";
		}

		// Generic parse error suggestion
		return "Check Java syntax documentation for correct language constructs";
	}

	/**
	 * Generates suggestions for configuration errors.
	 */
	private static String generateConfigSuggestion(ErrorContext errorContext)
	{
		String message = errorContext.message().toLowerCase();

		if (UNKNOWN_CONFIG_KEY.matcher(message).find())
		{
			return "Check configuration documentation for valid configuration keys";
		}

		if (INVALID_CONFIG_VALUE.matcher(message).find())
		{
			return "Verify configuration value format and allowed values in documentation";
		}

		if (message.contains("toml") || message.contains("yaml"))
		{
			return "Validate configuration file syntax using a TOML/YAML validator";
		}

		return "Review configuration file format and valid options";
	}

	/**
	 * Generates suggestions for formatting violations.
	 */
	private static String generateFormatSuggestion(ErrorContext errorContext)
	{
		String ruleCode = errorContext.errorCode();
		String message = errorContext.message().toLowerCase();

		// Rule-specific suggestions based on common formatting rules
		if (message.contains("line length") || message.contains("too long"))
		{
			return "Break long lines at logical points (method calls, operators, parameters)";
		}

		if (message.contains("indentation") || message.contains("indent"))
		{
			return "Use consistent indentation (tabs or spaces, not mixed)";
		}

		if (message.contains("whitespace") || message.contains("spacing"))
		{
			return "Add or remove whitespace to match style guide requirements";
		}

		if (message.contains("import") || message.contains("unused"))
		{
			return "Remove unused imports or organize imports according to style guide";
		}

		if (message.contains("brace") || message.contains("{") || message.contains("}"))
		{
			return "Adjust brace placement to match configured style (same line vs new line)";
		}

		return "Apply automatic formatting or adjust code to match style guide";
	}

	/**
	 * Generates suggestions for validation errors.
	 */
	private static String generateValidationSuggestion(ErrorContext errorContext)
	{
		String message = errorContext.message().toLowerCase();

		if (message.contains("constraint") || message.contains("violation"))
		{
			return "Review rule configuration and adjust settings or code to meet constraints";
		}

		if (message.contains("conflict") || message.contains("inconsistent"))
		{
			return "Resolve conflicts between rules or configuration settings";
		}

		return "Check validation rules and ensure code meets configured requirements";
	}

	/**
	 * Generates suggestions for system errors.
	 */
	private static String generateSystemSuggestion(ErrorContext errorContext)
	{
		String message = errorContext.message().toLowerCase();

		if (message.contains("permission") || message.contains("access"))
		{
			return "Check file permissions and ensure read/write access to the file";
		}

		if (message.contains("not found") || message.contains("missing"))
		{
			return "Verify file path exists and is accessible";
		}

		if (message.contains("space") || message.contains("disk"))
		{
			return "Check available disk space and cleanup if necessary";
		}

		if (message.contains("memory") || message.contains("heap"))
		{
			return "Increase JVM memory limits or process smaller files";
		}

		return "Check system resources and file system access";
	}

	/**
	 * Generates context-aware suggestions based on the source code around the error.
	 *
	 * @param errorContext the error context, never null
	 * @param additionalContext extra context information, may be null
	 * @throws IllegalArgumentException if {@code errorContext} is null
	 * @return an enhanced suggestion with contextual information, or null if no suggestion available
	 */
	public static String generateContextualSuggestion(ErrorContext errorContext, String additionalContext)
	{
		if (errorContext == null)
		{
			throw new IllegalArgumentException("Error context cannot be null");
		}

		String baseSuggestion = generateSuggestion(errorContext);
		if (baseSuggestion == null)
		{
			return null;
		}

		StringBuilder suggestion = new StringBuilder(baseSuggestion);

		// Add context-specific enhancements
		if (additionalContext != null && !additionalContext.trim().isEmpty())
		{
			suggestion.append(" (Context: ").append(additionalContext.trim()).append(")");
		}

		// Add location-specific hints for parse errors
		if (errorContext.category() == ErrorCategory.PARSE)
		{
			SourceRange location = errorContext.location();
			if (location.start().column() == 1)
			{
				suggestion.append(" - Check beginning of line for proper statement start");
			}
			else if (location.length() == 1)
			{
				suggestion.append(" - Check single character at column ").append(location.start().column());
			}
		}

		return suggestion.toString();
	}

	/**
	 * Returns whether a fix suggestion is available for the given error type.
	 *
	 * @param category the error category, never null
	 * @throws IllegalArgumentException if {@code category} is null
	 * @return true if suggestions are typically available for this error type
	 */
	public static boolean hasSuggestionFor(ErrorCategory category)
	{
		if (category == null)
		{
			throw new IllegalArgumentException("Error category cannot be null");
		}
		return true; // All error categories can have suggestions
	}
}