package io.github.cowwoc.styler.formatter.whitespace;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.whitespace.internal.WhitespaceAnalyzer;
import io.github.cowwoc.styler.formatter.whitespace.internal.WhitespaceFixer;
import io.github.cowwoc.styler.formatter.RuleExample;
import io.github.cowwoc.styler.formatter.RuleProperty;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Formatting rule for consistent whitespace around operators, keywords, and punctuation.
 * <p>
 * Enforces spacing rules such as:
 * <ul>
 * <li>Space around binary operators: {@code a + b} instead of {@code a+b}</li>
 * <li>Space after commas: {@code f(a, b)} instead of {@code f(a,b)}</li>
 * <li>Space after control keywords: {@code if (x)} instead of {@code if(x)}</li>
 * <li>No space around method references: {@code String::valueOf} instead of
 * {@code String :: valueOf}</li>
 * </ul>
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class WhitespaceFormattingRule implements FormattingRule
{
	private static final String RULE_ID = "whitespace";
	private static final String RULE_NAME = "Whitespace Formatting";
	private static final String RULE_DESCRIPTION =
		"Enforces consistent spacing around operators, keywords, and punctuation";

	/**
	 * Creates a whitespace formatting rule.
	 */
	public WhitespaceFormattingRule()
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
	public List<RuleExample> getExamples()
	{
		return List.of(
			new RuleExample(
				"Space around binary operators",
				"int x=a+b*c;",
				"int x = a + b * c;"),
			new RuleExample(
				"Space after keywords and commas",
				"if(condition){process(a,b,c);}",
				"if (condition) { process(a, b, c); }"));
	}

	@Override
	public List<RuleProperty> getProperties()
	{
		return List.of(
			new RuleProperty(
				"spaceAroundBinaryOperator",
				"boolean",
				"true",
				"Add space around +, -, *, /, etc."),
			new RuleProperty(
				"spaceAfterComma",
				"boolean",
				"true",
				"Add space after commas"),
			new RuleProperty(
				"spaceAfterControlKeyword",
				"boolean",
				"true",
				"Add space after if, for, while, etc."));
	}

	@Override
	public List<FormattingViolation> analyze(TransformationContext context,
		List<FormattingConfiguration> configs)
	{
		requireThat(context, "context").isNotNull();
		requireThat(configs, "configs").isNotNull();

		WhitespaceFormattingConfiguration wsConfig = FormattingConfiguration.findConfig(
			configs, WhitespaceFormattingConfiguration.class, WhitespaceFormattingConfiguration.defaultConfig());

		return WhitespaceAnalyzer.analyze(context, wsConfig);
	}

	@Override
	public String format(TransformationContext context, List<FormattingConfiguration> configs)
	{
		requireThat(context, "context").isNotNull();
		requireThat(configs, "configs").isNotNull();

		WhitespaceFormattingConfiguration wsConfig = FormattingConfiguration.findConfig(
			configs, WhitespaceFormattingConfiguration.class, WhitespaceFormattingConfiguration.defaultConfig());

		return WhitespaceFixer.format(context, wsConfig);
	}
}
