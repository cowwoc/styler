package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.FormattingRule;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.formatter.api.ValidationResult;

import java.util.List;

/**
 * Formatting rule that enforces consistent indentation.
 * <p>
 * This rule analyzes Java source code and applies consistent indentation according
 * to the configured mode (spaces, tabs, or mixed), depth, and alignment options.
 * It handles structural indentation for class bodies, method bodies, control structures,
 * and provides configurable behavior for continuation lines and comments.
 * <p>
 * The rule supports three indentation modes per
 * <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.6">JLS §3.6</a>:
 * <ul>
 *   <li>{@link IndentationMode#SPACES} - Use only space characters</li>
 *   <li>{@link IndentationMode#TABS} - Use only tab characters</li>
 *   <li>{@link IndentationMode#MIXED} - Use tabs for structure, spaces for alignment</li>
 * </ul>
 * <p>
 * Priority is set to 75 to run after import organization (priority 50) but before
 * line length wrapping (priority 100), ensuring proper formatting sequence.
 */
public final class IndentationFormattingRule implements FormattingRule
{
	private static final String RULE_ID = "io.github.cowwoc.styler.rules.Indentation";
	private static final int DEFAULT_PRIORITY = 75;

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
		return IndentationConfiguration.createDefault();
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

		IndentationConfiguration config;
		if (context.getConfiguration() instanceof IndentationConfiguration indentConfig)
		{
			config = indentConfig;
		}
		else
		{
			// Fall back to default configuration if wrong type provided
			config = IndentationConfiguration.createDefault();
		}

		// Phase 2: Analyze source for indentation violations
		IndentationAnalyzer analyzer = new IndentationAnalyzer(context, config);
		List<IndentationViolation> violations = analyzer.analyze();

		// Phase 3: Generate corrections for violations
		IndentationCorrector corrector = new IndentationCorrector(config);
		List<TextEdit> edits = corrector.correct(violations);

		return FormattingResult.withEdits(edits);
	}
}
