package io.github.cowwoc.styler.cli;

import java.io.Serial;

/**
 * Exception thrown when help or version information is requested.
 */
public final class HelpRequestedException extends CLIException
{
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * Creates a new help requested exception with the specified message.
	 *
	 * @param message the help or version text to display
	 */
	public HelpRequestedException(String message)
	{
		super(message);
	}
}
