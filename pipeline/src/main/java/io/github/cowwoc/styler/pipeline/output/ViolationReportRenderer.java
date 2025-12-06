package io.github.cowwoc.styler.pipeline.output;

import io.github.cowwoc.styler.pipeline.output.internal.HumanViolationRenderer;
import io.github.cowwoc.styler.pipeline.output.internal.JsonViolationRenderer;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Renders a violation report in a specified output format.
 * <p>
 * Implementations of this interface provide format-specific rendering of violation reports
 * for consumption by different audiences (AI agents, CI systems, human users). Each renderer
 * handles serialization to its target format with appropriate structure and detail levels.
 * <p>
 * <b>Thread-safety</b>: Implementations must be immutable and thread-safe.
 */
public interface ViolationReportRenderer
{
	/**
	 * Renders a violation report to a string in the supported output format.
	 * <p>
	 * The output format (JSON, HUMAN, etc.) is determined by the implementation.
	 *
	 * @param report the violation report to render
	 * @return the formatted report as a string
	 * @throws NullPointerException if {@code report} is null
	 */
	String render(ViolationReport report);

	/**
	 * Returns the output format supported by this renderer.
	 *
	 * @return the supported output format
	 */
	OutputFormat supportedFormat();

	/**
	 * Creates a renderer for the specified output format.
	 * <p>
	 * This factory method returns the appropriate renderer implementation based on
	 * the requested format. All created renderers are immutable and thread-safe.
	 *
	 * @param format the desired output format
	 * @return a renderer for the specified format
	 * @throws NullPointerException if {@code format} is null
	 * @throws IllegalArgumentException if the format is not supported
	 */
	static ViolationReportRenderer create(OutputFormat format)
	{
		requireThat(format, "format").isNotNull();

		return switch (format)
		{
			case JSON -> new JsonViolationRenderer();
			case HUMAN -> new HumanViolationRenderer();
		};
	}
}
