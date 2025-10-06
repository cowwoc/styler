package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.FormattingRule;
import io.github.cowwoc.styler.formatter.api.LineLengthRuleConfiguration;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ValidationResult;
import io.github.cowwoc.styler.formatter.api.WrapConfiguration;
import io.github.cowwoc.styler.formatter.impl.wrap.WrapBehavior;
import io.github.cowwoc.styler.formatter.impl.wrap.WrapPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Formatting rule that enforces maximum line length.
 * <p>
 * This rule analyzes source code lines and applies intelligent wrapping when lines exceed
 * the configured maximum length. Break points are selected semantically to preserve code
 * readability per
 * <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html">JLS Chapter 3: Lexical Structure</a>.
 */
public final class LineLengthFormattingRule implements FormattingRule
{
	private static final String RULE_ID = "io.github.cowwoc.styler.rules.LineLength";
	private static final int DEFAULT_PRIORITY = 100;

	@Override
	public String getRuleId()
	{
		return RULE_ID;
	}

	@Override
	public int getPriority()
	{
		return DEFAULT_PRIORITY;
	}

	@Override
	public RuleConfiguration getDefaultConfiguration()
	{
		return new LineLengthRuleConfiguration();
	}

	@Override
	public ValidationResult validate(FormattingContext context)
	{
		if (context == null)
		{
			return ValidationResult.failure("FormattingContext cannot be null");
		}
		if (context.getRootNode() == null)
		{
			return ValidationResult.failure("Root AST node cannot be null");
		}
		if (context.getSourceText() == null || context.getSourceText().isEmpty())
		{
			return ValidationResult.failure("Source text cannot be null or empty");
		}
		return ValidationResult.success();
	}

	@Override
	public FormattingResult apply(FormattingContext context)
	{
		ValidationResult validationResult = validate(context);
		if (validationResult.isFailure())
		{
			return FormattingResult.empty();
		}

		LineLengthRuleConfiguration config = (LineLengthRuleConfiguration) context.getConfiguration();
		WrapConfiguration wrapConfig = context.getWrapConfiguration();
		String sourceText = context.getSourceText();

		LineAnalyzer analyzer = new LineAnalyzer(sourceText, config, wrapConfig);
		List<io.github.cowwoc.styler.ast.SourceRange> violatingLines = analyzer.findViolatingLines();

		if (violatingLines.isEmpty())
		{
			return FormattingResult.empty();
		}

		WrapBehavior wrapBehavior = new WrapBehavior(wrapConfig);

		List<io.github.cowwoc.styler.formatter.api.TextEdit> edits = new ArrayList<>();

		for (io.github.cowwoc.styler.ast.SourceRange lineRange : violatingLines)
		{
			List<WrapPoint> wrapPoints = wrapBehavior.findWrapPoints(lineRange,
				context.getRootNode(), sourceText);

			if (!wrapPoints.isEmpty())
			{
				WrapPoint bestWrapPoint = wrapPoints.get(0);
				io.github.cowwoc.styler.formatter.api.TextEdit edit =
					wrapBehavior.createWrapEdit(bestWrapPoint, sourceText);
				edits.add(edit);
			}
		}

		return FormattingResult.withEdits(edits);
	}
}
