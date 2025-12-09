package io.github.cowwoc.styler.cli;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.cowwoc.styler.errorcatalog.Audience;
import io.github.cowwoc.styler.pipeline.PipelineResult;
import io.github.cowwoc.styler.pipeline.output.OutputFormat;
import io.github.cowwoc.styler.pipeline.output.ViolationReport;
import io.github.cowwoc.styler.pipeline.output.ViolationReportRenderer;

/**
 * Formats and displays pipeline processing results to stdout.
 * <p>
 * Renders pipeline results in the appropriate format (JSON for AI, human-readable for
 * TTY). Results are written to stdout for display to end users.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class OutputHandler
{
	/**
	 * Renders pipeline results in the detected output format.
	 * <p>
	 * Aggregates violations from all pipeline results and renders them using
	 * the appropriate renderer based on the output format. Writes to stdout.
	 *
	 * @param results the list of pipeline results to render (non-null, may be empty)
	 * @param format the output format to use: {@code JSON} or {@code HUMAN}
	 * @throws NullPointerException if any of the arguments are {@code null}
	 */
	@SuppressWarnings("PMD.SystemPrintln")
	public void render(List<PipelineResult> results, OutputFormat format)
	{
		requireThat(results, "results").isNotNull();
		requireThat(format, "format").isNotNull();

		ViolationReportRenderer renderer = ViolationReportRenderer.create(format);

		for (PipelineResult result : results)
		{
			// Count violations by rule ID
			Map<String, Integer> ruleCounts = new HashMap<>();
			result.violations().forEach(violation ->
				ruleCounts.merge(violation.ruleId(), 1, Integer::sum));

			// Create and render the violation report
			ViolationReport report = new ViolationReport(
				result.filePath(),
				result.violations(),
				ruleCounts);

			String output = renderer.render(report);
			System.out.println(output);
		}
	}

	/**
	 * Detects the output format based on the execution environment.
	 * <p>
	 * Determines if the target audience is AI or human by checking:
	 * <ul>
	 *     <li>CLAUDE_SESSION_ID environment variable (AI indicator)</li>
	 *     <li>AI_AGENT_MODE environment variable</li>
	 *     <li>Whether stdout is a TTY (human interactive terminal)</li>
	 * </ul>
	 * <p>
	 * Returns appropriate OutputFormat:
	 * <ul>
	 *     <li>AI audience → JSON format</li>
	 *     <li>Human audience → HUMAN format</li>
	 * </ul>
	 *
	 * @return the detected output format
	 */
	public OutputFormat detectOutputFormat()
	{
		Audience audience = Audience.detect();
		return switch (audience)
		{
			case AI -> OutputFormat.JSON;
			case HUMAN -> OutputFormat.HUMAN;
		};
	}
}
