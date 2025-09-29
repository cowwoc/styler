package io.github.cowwoc.styler.cli.error;

/**
 * Defines severity levels for error reporting across all styler operations.
 * <p>
 * Severity levels enable appropriate error handling, filtering, and presentation
 * strategies. They provide a unified severity model that bridges different
 * error sources (parse, config, formatting, validation, system).
 */
public enum ErrorSeverity
{
	/**
	 * Debug-level information for detailed troubleshooting.
	 * <p>
	 * Debug messages provide detailed internal state information useful
	 * for troubleshooting complex issues but not typically shown to users.
	 */
	DEBUG("debug"),

	/**
	 * Informational messages about operation progress or minor issues.
	 * <p>
	 * Info messages communicate normal operation status, progress updates,
	 * or minor issues that don't require immediate attention.
	 */
	INFO("info"),

	/**
	 * Warning messages for issues that should be addressed but don't prevent operation.
	 * <p>
	 * Warnings indicate problems that reduce code quality, violate style guides,
	 * or represent potential issues that should be reviewed and addressed.
	 */
	WARNING("warn"),

	/**
	 * Error messages for serious problems that prevent successful operation.
	 * <p>
	 * Errors indicate failures that prevent the requested operation from
	 * completing successfully and typically require immediate attention.
	 */
	ERROR("error");

	private final String displayName;

	ErrorSeverity(String displayName)
	{
		this.displayName = displayName;
	}

	/**
	 * Returns the human-readable display name for this severity level.
	 *
	 * @return the display name used in error messages, never null
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * Returns whether this severity level is at least as severe as another level.
	 *
	 * @param other the severity level to compare with, never null
	 * @throws IllegalArgumentException if {@code other} is null
	 * @return true if this severity is at least as severe as the other
	 */
	public boolean isAtLeast(ErrorSeverity other)
	{
		if (other == null)
		{
			throw new IllegalArgumentException("Severity level cannot be null");
		}
		return this.ordinal() >= other.ordinal();
	}

	/**
	 * Returns whether this severity represents an error condition.
	 *
	 * @return true if this is ERROR severity, false otherwise
	 */
	public boolean isError()
	{
		return this == ERROR;
	}

	/**
	 * Returns whether this severity represents a warning condition.
	 *
	 * @return true if this is WARNING severity, false otherwise
	 */
	public boolean isWarning()
	{
		return this == WARNING;
	}

	/**
	 * Returns whether this severity represents informational content.
	 *
	 * @return true if this is INFO severity, false otherwise
	 */
	public boolean isInfo()
	{
		return this == INFO;
	}

	/**
	 * Returns whether this severity represents debug information.
	 *
	 * @return true if this is DEBUG severity, false otherwise
	 */
	public boolean isDebug()
	{
		return this == DEBUG;
	}

	/**
	 * Returns whether this severity level indicates a problem that should halt processing.
	 *
	 * @return true if processing should stop for this severity level, false otherwise
	 */
	public boolean shouldHaltProcessing()
	{
		return this == ERROR;
	}
}