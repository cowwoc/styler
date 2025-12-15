package io.github.cowwoc.styler.formatter.indentation;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.indentation.internal.IndentationAnalyzer;
import io.github.cowwoc.styler.formatter.indentation.internal.IndentationFixer;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Formatting rule for enforcing consistent indentation.
 * <p>
 * Verifies that code uses consistent indentation (tabs or spaces) at the appropriate depth for each line,
 * with extra indentation for continuation lines.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class IndentationFormattingRule implements FormattingRule
{
	private static final String RULE_ID = "indentation";
	private static final String RULE_NAME = "Indentation";
	private static final String RULE_DESCRIPTION =
		"Enforces consistent indentation using tabs or spaces at the appropriate depth";

	/**
	 * Creates an indentation formatting rule.
	 */
	public IndentationFormattingRule()
	{
	}

	@Override
	public String getId()
	{
		return RULE_ID;
	}

	@Override
	public String getName()
	{
		return RULE_NAME;
	}

	@Override
	public String getDescription()
	{
		return RULE_DESCRIPTION;
	}

	@Override
	public ViolationSeverity getDefaultSeverity()
	{
		return ViolationSeverity.WARNING;
	}

	@Override
	public List<FormattingViolation> analyze(TransformationContext context,
		List<FormattingConfiguration> configs)
	{
		requireThat(context, "context").isNotNull();
		requireThat(configs, "configs").isNotNull();

		IndentationFormattingConfiguration indentConfig = FormattingConfiguration.findConfig(
			configs, IndentationFormattingConfiguration.class, IndentationFormattingConfiguration.defaultConfig());

		return IndentationAnalyzer.analyze(context, indentConfig);
	}

	@Override
	public String format(TransformationContext context, List<FormattingConfiguration> configs)
	{
		requireThat(context, "context").isNotNull();
		requireThat(configs, "configs").isNotNull();

		IndentationFormattingConfiguration indentConfig = FormattingConfiguration.findConfig(
			configs, IndentationFormattingConfiguration.class, IndentationFormattingConfiguration.defaultConfig());

		return IndentationFixer.format(context, indentConfig);
	}
}
