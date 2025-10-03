package io.github.cowwoc.styler.cli;

/**
 * Exception thrown when command-line argument parsing fails.
 * <p>
 * This exception wraps Picocli parsing errors and provides user-friendly
 * error messages along with usage information to help users correct
 * their command-line invocations.
 */
public class ArgumentParsingException extends Exception
{
	private static final long serialVersionUID = 1L;
	private final String usageText;

	/**
	 * Creates a new argument parsing exception.
	 *
	 * @param message the error {@code message} describing what went wrong
	 * @param usageText the usage text to display to help the user
	 */
	public ArgumentParsingException(String message, String usageText)
	{
		super(message);
		this.usageText = usageText;
	}

	/**
	 * Creates a new argument parsing exception with a cause.
	 *
	 * @param message the error {@code message} describing what went wrong
	 * @param usageText the usage text to display to help the user
	 * @param cause the underlying {@code cause} of the parsing failure
	 */
	public ArgumentParsingException(String message, String usageText, Throwable cause)
	{
		super(message, cause);
		this.usageText = usageText;
	}

	/**
	 * Returns the usage text that should be displayed to help the user.
	 *
	 * @return the usage text
	 */
	public String getUsageText()
	{
		return usageText;
	}

	/**
	 * Returns a formatted error message that includes both the error
	 * description and usage information.
	 *
	 * @return the complete error message with usage help
	 */
	public String getFormattedMessage()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Error: ").append(getMessage()).append("\n\n");
		if (usageText != null && !usageText.isEmpty())
		{
			builder.append("Usage:\n").append(usageText);
		}
		return builder.toString();
	}
}