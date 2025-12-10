package io.github.cowwoc.styler.formatter.brace;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.brace.internal.BraceAnalyzer;
import io.github.cowwoc.styler.formatter.brace.internal.BraceFixer;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Formatting rule for enforcing consistent brace placement styles.
 * <p>
 * Supports multiple brace styles (K&R, Allman, GNU) with per-construct configuration.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class BraceFormattingRule implements FormattingRule
{
	private static final String RULE_ID = "brace-style";

	/**
	 * Human-readable name for this rule.
	 */
	private static final String RULE_NAME = "Brace Style";

	/**
	 * Description of what this rule checks.
	 */
	private static final String RULE_DESCRIPTION =
		"Enforces consistent brace placement (K&R, Allman, GNU) for classes, methods, and control structures";

	/**
	 * Creates a new brace formatting rule instance.
	 */
	public BraceFormattingRule()
	{
		// No-arg constructor for instantiation
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
		FormattingConfiguration config)
	{
		requireThat(context, "context").isNotNull();

		BraceFormattingConfiguration braceConfig;
		if (config == null)
			braceConfig = BraceFormattingConfiguration.defaultConfig();
		else
		{
			requireThat(config, "config").isInstanceOf(BraceFormattingConfiguration.class);
			braceConfig = (BraceFormattingConfiguration) config;
		}

		return BraceAnalyzer.analyze(context, braceConfig);
	}

	@Override
	public String format(TransformationContext context, FormattingConfiguration config)
	{
		requireThat(context, "context").isNotNull();

		BraceFormattingConfiguration braceConfig;
		if (config == null)
			braceConfig = BraceFormattingConfiguration.defaultConfig();
		else
		{
			requireThat(config, "config").isInstanceOf(BraceFormattingConfiguration.class);
			braceConfig = (BraceFormattingConfiguration) config;
		}

		return BraceFixer.format(context, braceConfig);
	}
}
