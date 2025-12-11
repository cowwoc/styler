package io.github.cowwoc.styler.formatter.indentation;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Configuration for indentation formatting rules.
 * <p>
 * Controls whether to use tabs or spaces for indentation, the size of each indent level, and the additional
 * indentation for continuation lines.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param ruleId             the rule ID
 * @param indentationType    whether to use tabs or spaces for indentation
 * @param indentSize         number of spaces per indent level (only applies when using SPACES)
 * @param continuationIndent additional spaces for continuation lines
 * @throws NullPointerException     if {@code ruleId} or {@code indentationType} is null
 * @throws IllegalArgumentException if {@code ruleId} is blank, or {@code indentSize} or
 *                                  {@code continuationIndent} is not positive
 */
public record IndentationFormattingConfiguration(String ruleId,
	IndentationType indentationType,
	int indentSize,
	int continuationIndent) implements FormattingConfiguration
{
	private static final String DEFAULT_RULE_ID = "indentation";
	private static final int DEFAULT_INDENT_SIZE = 4;
	private static final int DEFAULT_CONTINUATION_INDENT = 4;

	/**
	 * Creates an indentation formatting configuration.
	 */
	public IndentationFormattingConfiguration
	{
		requireThat(ruleId, "ruleId").isNotBlank();
		requireThat(indentationType, "indentationType").isNotNull();
		requireThat(indentSize, "indentSize").isPositive();
		requireThat(continuationIndent, "continuationIndent").isPositive();
	}

	/**
	 * Returns the default configuration with tabs.
	 *
	 * @return the default configuration
	 */
	public static IndentationFormattingConfiguration defaultConfig()
	{
		return new IndentationFormattingConfiguration(DEFAULT_RULE_ID, IndentationType.TABS,
			DEFAULT_INDENT_SIZE, DEFAULT_CONTINUATION_INDENT);
	}

	/**
	 * Returns a configuration using spaces for indentation.
	 *
	 * @param indentSize the number of spaces per indent level
	 * @return a spaces configuration
	 * @throws IllegalArgumentException if {@code indentSize} is not positive
	 */
	public static IndentationFormattingConfiguration withSpaces(int indentSize)
	{
		return new IndentationFormattingConfiguration(DEFAULT_RULE_ID, IndentationType.SPACES,
			indentSize, indentSize);
	}

	/**
	 * Returns a configuration using tabs for indentation.
	 *
	 * @return a tabs configuration
	 */
	public static IndentationFormattingConfiguration withTabs()
	{
		return new IndentationFormattingConfiguration(DEFAULT_RULE_ID, IndentationType.TABS,
			DEFAULT_INDENT_SIZE, DEFAULT_CONTINUATION_INDENT);
	}
}
