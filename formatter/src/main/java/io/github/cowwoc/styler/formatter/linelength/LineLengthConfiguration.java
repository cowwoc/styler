package io.github.cowwoc.styler.formatter.linelength;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Configuration for the line length formatting rule.
 * Specifies line length limits and per-context wrapping strategies.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 *
 * @param ruleId unique identifier for the rule
 * @param maxLineLength maximum allowed line length (40-500)
 * @param tabWidth width of a tab character (1-8)
 * @param indentContinuationLines indentation for wrapped lines (0-12)
 * @param methodChainWrap wrapping style for method chains
 * @param methodArgumentsWrap wrapping style for method arguments
 * @param binaryExpressionWrap wrapping style for binary expressions
 * @param methodParametersWrap wrapping style for method parameters
 * @param ternaryExpressionWrap wrapping style for ternary expressions
 * @param arrayInitializerWrap wrapping style for array initializers
 * @param annotationArgumentsWrap wrapping style for annotation arguments
 * @param genericTypeArgsWrap wrapping style for generic type arguments
 * @param wrapLongStrings whether to wrap long string literals
 * @throws NullPointerException     if any required parameter is null
 * @throws IllegalArgumentException if any parameter is out of valid range
 */
public record LineLengthConfiguration(
	String ruleId,
	int maxLineLength,
	int tabWidth,
	int indentContinuationLines,
	WrapStyle methodChainWrap,
	WrapStyle methodArgumentsWrap,
	WrapStyle binaryExpressionWrap,
	WrapStyle methodParametersWrap,
	WrapStyle ternaryExpressionWrap,
	WrapStyle arrayInitializerWrap,
	WrapStyle annotationArgumentsWrap,
	WrapStyle genericTypeArgsWrap,
	boolean wrapLongStrings) implements FormattingConfiguration
{
	/**
	 * Minimum allowed line length (40 characters).
	 */
	private static final int MIN_LINE_LENGTH = 40;

	/**
	 * Maximum allowed line length (500 characters).
	 */
	private static final int MAX_LINE_LENGTH = 500;

	/**
	 * Minimum tab width (1 character).
	 */
	private static final int MIN_TAB_WIDTH = 1;

	/**
	 * Maximum tab width (8 characters).
	 */
	private static final int MAX_TAB_WIDTH = 8;

	/**
	 * Minimum indentation for continuation lines (0 characters).
	 */
	private static final int MIN_INDENT_CONTINUATION = 0;

	/**
	 * Maximum indentation for continuation lines (12 characters).
	 */
	private static final int MAX_INDENT_CONTINUATION = 12;

	/**
	 * Default maximum line length (120 characters).
	 */
	private static final int DEFAULT_MAX_LENGTH = 120;

	/**
	 * Default tab width (4 characters).
	 */
	private static final int DEFAULT_TAB_WIDTH = 4;

	/**
	 * Default indentation for continuation lines (4 characters).
	 */
	private static final int DEFAULT_INDENT_CONTINUATION = 4;

	/**
	 * Creates a configuration record with validation.
	 */
	public LineLengthConfiguration
	{
		// Validate ruleId
		requireThat(ruleId, "ruleId").isNotBlank();

		// Validate maxLineLength
		requireThat(maxLineLength, "maxLineLength").
			isGreaterThanOrEqualTo(MIN_LINE_LENGTH).
			isLessThanOrEqualTo(MAX_LINE_LENGTH);

		// Validate tabWidth
		requireThat(tabWidth, "tabWidth").
			isGreaterThanOrEqualTo(MIN_TAB_WIDTH).
			isLessThanOrEqualTo(MAX_TAB_WIDTH);

		// Validate indentContinuationLines
		requireThat(indentContinuationLines, "indentContinuationLines").
			isGreaterThanOrEqualTo(MIN_INDENT_CONTINUATION).
			isLessThanOrEqualTo(MAX_INDENT_CONTINUATION);

		// Validate wrap styles
		requireThat(methodChainWrap, "methodChainWrap").isNotNull();
		requireThat(methodArgumentsWrap, "methodArgumentsWrap").isNotNull();
		requireThat(binaryExpressionWrap, "binaryExpressionWrap").isNotNull();
		requireThat(methodParametersWrap, "methodParametersWrap").isNotNull();
		requireThat(ternaryExpressionWrap, "ternaryExpressionWrap").isNotNull();
		requireThat(arrayInitializerWrap, "arrayInitializerWrap").isNotNull();
		requireThat(annotationArgumentsWrap, "annotationArgumentsWrap").isNotNull();
		requireThat(genericTypeArgsWrap, "genericTypeArgsWrap").isNotNull();
	}

	/**
	 * Creates a default configuration with recommended settings.
	 *
	 * @return default configuration instance
	 */
	public static LineLengthConfiguration defaultConfig()
	{
		return new LineLengthConfiguration("line-length", DEFAULT_MAX_LENGTH, DEFAULT_TAB_WIDTH,
			DEFAULT_INDENT_CONTINUATION, WrapStyle.AFTER, WrapStyle.AFTER,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER,
			WrapStyle.AFTER, WrapStyle.AFTER, WrapStyle.AFTER, true);
	}
}
