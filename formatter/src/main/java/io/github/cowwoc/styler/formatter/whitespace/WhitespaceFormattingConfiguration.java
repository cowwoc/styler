package io.github.cowwoc.styler.formatter.whitespace;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Configuration for whitespace formatting rules.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param ruleId                         the rule ID
 * @param spaceAroundBinaryOperators     whether to add space around binary operators
 * @param spaceAroundAssignmentOperators whether to add space around assignment operators
 * @param spaceAfterComma                whether to add space after commas
 * @param spaceAfterSemicolonInFor       whether to add space after semicolons in for loops
 * @param spaceAfterControlKeywords      whether to add space after control keywords
 * @param spaceBeforeOpenBrace           whether to add space before opening braces
 * @param spaceAroundColonInEnhancedFor  whether to add space around colons in enhanced for loops
 * @param spaceAroundArrowInLambda       whether to add space around lambda arrows
 * @param noSpaceAroundMethodReference   whether to remove space around method references
 * @throws NullPointerException     if {@code ruleId} is null
 * @throws IllegalArgumentException if {@code ruleId} is blank
 */
public record WhitespaceFormattingConfiguration(
	String ruleId,
	boolean spaceAroundBinaryOperators,
	boolean spaceAroundAssignmentOperators,
	boolean spaceAfterComma,
	boolean spaceAfterSemicolonInFor,
	boolean spaceAfterControlKeywords,
	boolean spaceBeforeOpenBrace,
	boolean spaceAroundColonInEnhancedFor,
	boolean spaceAroundArrowInLambda,
	boolean noSpaceAroundMethodReference)
	implements FormattingConfiguration
{
	/**
	 * Creates a default whitespace formatting configuration.
	 *
	 * @return a default configuration
	 */
	public static WhitespaceFormattingConfiguration defaultConfig()
	{
		return new WhitespaceFormattingConfiguration(
			"whitespace",
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true);
	}

	/**
	 * Creates a whitespace formatting configuration.
	 */
	public WhitespaceFormattingConfiguration
	{
		requireThat(ruleId, "ruleId").isNotBlank();
	}
}
