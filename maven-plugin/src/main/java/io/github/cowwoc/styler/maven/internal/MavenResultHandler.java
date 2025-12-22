package io.github.cowwoc.styler.maven.internal;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Path;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.pipeline.PipelineResult;
import io.github.cowwoc.styler.pipeline.StageResult;

/**
 * Handles pipeline results by converting them to Maven log messages.
 * <p>
 * This class formats violation reports and processing errors into messages
 * appropriate for Maven's logging system. It respects violation severity
 * levels to choose appropriate log levels.
 * <p>
 * <b>Log Level Mapping</b>:
 * <ul>
 *   <li>{@link ViolationSeverity#ERROR} maps to {@code error}</li>
 *   <li>{@link ViolationSeverity#WARNING} maps to {@code warn}</li>
 *   <li>{@link ViolationSeverity#INFO} maps to {@code info}</li>
 * </ul>
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe. The underlying Maven Log
 * implementation is thread-safe.
 */
public final class MavenResultHandler
{
	private final Log log;

	/**
	 * Creates a new result handler that logs to the specified Maven log.
	 *
	 * @param log the Maven log to write messages to
	 * @throws NullPointerException if {@code log} is {@code null}
	 */
	public MavenResultHandler(Log log)
	{
		this.log = requireThat(log, "log").isNotNull().getValue();
	}

	/**
	 * Reports formatting violations for a file.
	 * <p>
	 * Each violation is logged with appropriate severity level and includes
	 * file location, rule ID, and message.
	 *
	 * @param file       the file containing violations
	 * @param violations the list of violations to report
	 * @throws NullPointerException if any of the arguments are {@code null}
	 */
	public void reportViolations(Path file, List<FormattingViolation> violations)
	{
		requireThat(file, "file").isNotNull();
		requireThat(violations, "violations").isNotNull();

		for (FormattingViolation violation : violations)
		{
			String message = formatViolationMessage(file, violation);

			switch (violation.severity())
			{
				case ERROR -> log.error(message);
				case WARNING -> log.warn(message);
				case INFO -> log.info(message);
			}
		}
	}

	/**
	 * Formats a single violation into a human-readable message.
	 *
	 * @param file      the file containing the violation
	 * @param violation the violation to format
	 * @return formatted message string
	 */
	private String formatViolationMessage(Path file, FormattingViolation violation)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(file.toString());

		// Add line number if available
		if (violation.lineNumber() > 0)
		{
			sb.append(':').append(violation.lineNumber());

			// Add column if available
			if (violation.columnNumber() > 0)
			{
				sb.append(':').append(violation.columnNumber());
			}
		}

		sb.append(" [").
			append(violation.ruleId()).
			append("] ").
			append(violation.message());

		return sb.toString();
	}

	/**
	 * Handles a processing error for a file.
	 * <p>
	 * Logs the error with details from the failed pipeline stage.
	 *
	 * @param file   the file that failed to process
	 * @param result the pipeline result containing error information
	 * @throws NullPointerException if any of the arguments are {@code null}
	 */
	public void handleProcessingError(Path file, PipelineResult result)
	{
		requireThat(file, "file").isNotNull();
		requireThat(result, "result").isNotNull();

		// Find the first failed stage
		for (StageResult stageResult : result.stageResults())
		{
			if (stageResult instanceof StageResult.Failure failure)
			{
				String errorMessage = String.format(
					"Failed to process %s: %s",
					file, failure.message());

				if (failure.cause() != null)
				{
					log.error(errorMessage, failure.cause());
				}
				else
				{
					log.error(errorMessage);
				}
				return;
			}
		}

		// Generic error if no specific failure found
		log.error("Failed to process " + file + ": Unknown error");
	}
}
