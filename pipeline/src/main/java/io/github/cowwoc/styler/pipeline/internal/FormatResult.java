package io.github.cowwoc.styler.pipeline.internal;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.linemapping.LineMapping;

import java.util.List;

/**
 * Data produced by the FormatStage.
 * <p>
 * Contains the formatted source code, any violations found during formatting, and the line mapping
 * between original and formatted content.
 *
 * @param formattedSource the formatted source code
 * @param violations      the list of formatting violations found
 * @param lineMapping     the mapping between original and formatted line numbers
 */
public record FormatResult(String formattedSource, List<FormattingViolation> violations, LineMapping lineMapping)
{
	/**
	 * Creates a format result.
	 *
	 * @param formattedSource the formatted source code
	 * @param violations      the list of formatting violations found
	 * @param lineMapping     the mapping between original and formatted line numbers
	 * @throws AssertionError if any argument is null
	 */
	public FormatResult
	{
		assert that(formattedSource, "formattedSource").isNotNull().elseThrow();
		assert that(violations, "violations").isNotNull().elseThrow();
		assert that(lineMapping, "lineMapping").isNotNull().elseThrow();
	}
}
