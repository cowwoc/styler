package io.github.cowwoc.styler.cli.error;

/**
 * Categorizes different types of errors that can occur during styler operations.
 * <p>
 * Error categories enable appropriate handling strategies and help users quickly
 * identify the source and nature of problems. Each category maps to specific
 * error handling and reporting patterns.
 */
public enum ErrorCategory
{
	/**
	 * Parse errors that occur when analyzing source code syntax.
	 * <p>
	 * These errors indicate invalid Java syntax that prevents the parser
	 * from creating a complete AST. Examples include missing semicolons,
	 * unmatched braces, or invalid language constructs.
	 */
	PARSE("Parse Error"),

	/**
	 * Configuration errors that occur when loading or validating configuration files.
	 * <p>
	 * These errors indicate problems with styler configuration such as invalid
	 * TOML/YAML syntax, unknown configuration keys, or invalid configuration values.
	 */
	CONFIG("Configuration Error"),

	/**
	 * Formatting rule violations detected during formatting operations.
	 * <p>
	 * These errors represent style guide violations or formatting issues that
	 * cannot be automatically fixed or are configured to only be reported.
	 */
	FORMAT("Formatting Violation"),

	/**
	 * Validation errors that occur during rule application or constraint checking.
	 * <p>
	 * These errors indicate problems with rule configuration, constraint violations,
	 * or inconsistencies detected during formatting validation.
	 */
	VALIDATE("Validation Error"),

	/**
	 * System errors that occur due to file I/O, permissions, or other infrastructure issues.
	 * <p>
	 * These errors indicate problems with the runtime environment such as
	 * file access permissions, disk space, or system resource limitations.
	 */
	SYSTEM("System Error");

	private final String displayName;

	ErrorCategory(String displayName)
	{
		this.displayName = displayName;
	}

	/**
	 * Returns the human-readable display name for this error category.
	 *
	 * @return the display name used in error messages, never null
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * Returns whether this category represents a recoverable error.
	 * <p>
	 * Recoverable errors allow processing to continue with other files or rules,
	 * while non-recoverable errors typically halt processing entirely.
	 *
	 * @return true if processing can continue despite this error type, false otherwise
	 */
	public boolean isRecoverable()
	{
		return switch (this)
		{
			case FORMAT, VALIDATE -> true;
			case PARSE, CONFIG, SYSTEM -> false;
		};
	}

	/**
	 * Returns the default severity level for this error category.
	 *
	 * @return the typical severity for errors in this category, never null
	 */
	public ErrorSeverity getDefaultSeverity()
	{
		return switch (this)
		{
			case PARSE, CONFIG, SYSTEM -> ErrorSeverity.ERROR;
			case FORMAT -> ErrorSeverity.WARNING;
			case VALIDATE -> ErrorSeverity.INFO;
		};
	}
}