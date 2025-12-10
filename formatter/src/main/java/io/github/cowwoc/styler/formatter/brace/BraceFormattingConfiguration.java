package io.github.cowwoc.styler.formatter.brace;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Configuration for brace formatting rules.
 * <p>
 * Specifies a single brace style that applies to all constructs (classes, methods, control structures,
 * lambdas).
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param ruleId     the rule ID for this configuration
 * @param braceStyle the brace style to apply to all constructs
 * @throws NullPointerException     if any parameter is null
 * @throws IllegalArgumentException if {@code ruleId} is blank
 */
public record BraceFormattingConfiguration(String ruleId,
	BraceStyle braceStyle) implements FormattingConfiguration
{
	private static final String DEFAULT_RULE_ID = "brace-style";

	/**
	 * Creates a brace formatting configuration.
	 */
	public BraceFormattingConfiguration
	{
		requireThat(ruleId, "ruleId").isNotBlank();
		requireThat(braceStyle, "braceStyle").isNotNull();
	}

	/**
	 * Returns the default configuration with Allman style (brace on new line).
	 *
	 * @return the default configuration
	 */
	public static BraceFormattingConfiguration defaultConfig()
	{
		return new BraceFormattingConfiguration(DEFAULT_RULE_ID, BraceStyle.NEW_LINE);
	}
}
