package io.github.cowwoc.styler.cli.pipeline;

import java.nio.file.Path;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Base exception for all pipeline-related errors.
 * <p>
 * Pipeline exceptions include contextual information about where the error occurred (file path and
 * stage name) to enable precise error reporting and debugging.
 * <p>
 * This class is the root of a hierarchy of more specific exceptions:
 * <ul>
 *     <li>{@code ParseException} - Errors during source parsing</li>
 *     <li>{@code FormatException} - Errors during formatting transformations</li>
 *     <li>{@code ValidationException} - Errors during validation</li>
 *     <li>{@code OutputException} - Errors during file output</li>
 *     <li>{@code RecoveryFailedException} - Errors during error recovery</li>
 * </ul>
 */
public class PipelineException extends Exception
{
	private static final long serialVersionUID = 1L;

	private final transient Path filePath;
	private final String stageName;

	/**
	 * Creates a new pipeline exception.
	 *
	 * @param message   the error message explaining what went wrong (never {@code null} or empty)
	 * @param filePath  the file being processed when the error occurred (never {@code null})
	 * @param stageName the pipeline stage where the error occurred (never {@code null} or empty)
	 * @throws NullPointerException     if {@code message}, {@code filePath}, or {@code stageName} is {@code null}
	 * @throws IllegalArgumentException if {@code message} or {@code stageName} is empty
	 */
	public PipelineException(String message, Path filePath, String stageName)
	{
		super(buildMessage(message, filePath, stageName));
		requireThat(filePath, "filePath").isNotNull();
		requireThat(stageName, "stageName").isNotEmpty();
		this.filePath = filePath;
		this.stageName = stageName;
	}

	/**
	 * Creates a new pipeline exception with a root cause.
	 *
	 * @param message   the error message explaining what went wrong (never {@code null} or empty)
	 * @param filePath  the file being processed when the error occurred (never {@code null})
	 * @param stageName the pipeline stage where the error occurred (never {@code null} or empty)
	 * @param cause     the root cause of this exception (never {@code null})
	 * @throws NullPointerException     if {@code message}, {@code filePath}, {@code stageName}, or
	 *                                  {@code cause} is {@code null}
	 * @throws IllegalArgumentException if {@code message} or {@code stageName} is empty
	 */
	public PipelineException(String message, Path filePath, String stageName, Throwable cause)
	{
		super(buildMessage(message, filePath, stageName), cause);
		requireThat(filePath, "filePath").isNotNull();
		requireThat(stageName, "stageName").isNotEmpty();
		requireThat(cause, "cause").isNotNull();
		this.filePath = filePath;
		this.stageName = stageName;
	}

	/**
	 * Builds a comprehensive error message including file and stage context.
	 *
	 * @param message   the base error message (never {@code null} or empty)
	 * @param filePath  the file path (never {@code null})
	 * @param stageName the stage name (never {@code null} or empty)
	 * @return a formatted error message with context (never {@code null})
	 */
	private static String buildMessage(String message, Path filePath, String stageName)
	{
		requireThat(message, "message").isNotEmpty();
		requireThat(filePath, "filePath").isNotNull();
		requireThat(stageName, "stageName").isNotEmpty();
		return String.format("Pipeline stage '%s' failed while processing file '%s': %s",
			stageName, filePath, message);
	}

	/**
	 * Returns the file that was being processed when this error occurred.
	 *
	 * @return the file path (never {@code null})
	 */
	public final Path getFilePath()
	{
		return filePath;
	}

	/**
	 * Returns the pipeline stage where this error occurred.
	 *
	 * @return the stage name (never {@code null} or empty)
	 */
	public final String getStageName()
	{
		return stageName;
	}
}
