package io.github.cowwoc.styler.pipeline.internal;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

import io.github.cowwoc.styler.formatter.FormattingViolation;
import java.util.List;

/**
 * Data produced by the FormatStage.
 * <p>
 * Contains the formatted source code and any violations found during formatting.
 *
 * @param formattedSource the formatted source code
 * @param violations the list of formatting violations found
 */
public record FormatResult(String formattedSource, List<FormattingViolation> violations)
{
	/**
	 * Creates a format result.
	 *
	 * @param formattedSource the formatted source code
	 * @param violations the list of formatting violations found
	 * @throws AssertionError if any argument is null
	 */
	public FormatResult
	{
		assert that(formattedSource, "formattedSource").isNotNull().elseThrow();
		assert that(violations, "violations").isNotNull().elseThrow();
	}
}
