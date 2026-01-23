package io.github.cowwoc.styler.cli;

import java.io.Serial;

/**
 * Exception thrown when --explain-rules flag is specified.
 * <p>
 * This is a control flow exception indicating that the CLI should output
 * formatting rules documentation instead of processing files.
 */
public final class ExplainRulesException extends CLIException
{
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new explain-rules exception.
	 */
	public ExplainRulesException()
	{
		super("Explain rules requested");
	}
}
