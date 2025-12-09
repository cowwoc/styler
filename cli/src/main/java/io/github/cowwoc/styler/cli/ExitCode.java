package io.github.cowwoc.styler.cli;

/**
 * Enumeration of exit codes for the Styler CLI application.
 * <p>
 * Each exit code indicates a specific outcome or error condition. Exit codes follow
 * Unix conventions: 0 for success, 1 for violations (in check mode), 2+ for errors.
 * <p>
 * <b>Thread Safety:</b> This enum is thread-safe and immutable.
 */
public enum ExitCode
{
	/**
	 * Exit code 0: Command executed successfully with no violations found or all violations fixed.
	 */
	SUCCESS(0),

	/**
	 * Exit code 0: Help message or version information was requested and displayed.
	 */
	HELP(0),

	/**
	 * Exit code 1: Check mode completed but formatting violations were found.
	 * The source code does not conform to the formatting rules.
	 */
	VIOLATIONS_FOUND(1),

	/**
	 * Exit code 2: Invalid command-line arguments or usage error.
	 * The CLI arguments could not be parsed or violated usage constraints.
	 */
	USAGE_ERROR(2),

	/**
	 * Exit code 3: Configuration file parsing or validation error.
	 * The configuration file could not be read, parsed, or contains invalid values.
	 */
	CONFIG_ERROR(3),

	/**
	 * Exit code 4: Security validation failed.
	 * A security constraint was violated (e.g., path traversal attempt, resource limit exceeded).
	 */
	SECURITY_ERROR(4),

	/**
	 * Exit code 5: File I/O error.
	 * A file could not be read or written due to filesystem issues.
	 */
	IO_ERROR(5),

	/**
	 * Exit code 127: Unexpected internal error.
	 * An unexpected exception occurred that was not handled by specific error paths.
	 */
	INTERNAL_ERROR(127);

	private final int code;

	/**
	 * Constructs an exit code with the specified numeric value.
	 *
	 * @param code the numeric exit code value
	 */
	ExitCode(int code)
	{
		this.code = code;
	}

	/**
	 * Returns the numeric value of this exit code.
	 *
	 * @return the numeric exit code
	 */
	public int code()
	{
		return code;
	}
}
