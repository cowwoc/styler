package io.github.cowwoc.styler.formatter.linelength;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;

import java.util.Objects;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Configuration for the line length formatting rule.
 * Specifies line length limits and per-context wrapping strategies.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class LineLengthConfiguration implements FormattingConfiguration
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

	private final String ruleId;
	private final int maxLineLength;
	private final int tabWidth;
	private final int indentContinuationLines;
	private final WrapStyle methodChainWrap;
	private final WrapStyle methodArgumentsWrap;
	private final WrapStyle binaryExpressionWrap;
	private final WrapStyle methodParametersWrap;
	private final WrapStyle ternaryExpressionWrap;
	private final WrapStyle arrayInitializerWrap;
	private final WrapStyle annotationArgumentsWrap;
	private final WrapStyle genericTypeArgsWrap;
	private final boolean wrapLongStrings;

	/**
	 * Creates a configuration with validation.
	 *
	 * @param ruleId                  unique identifier for the rule
	 * @param maxLineLength           maximum allowed line length (40-500)
	 * @param tabWidth                width of a tab character (1-8)
	 * @param indentContinuationLines indentation for wrapped lines (0-12)
	 * @param methodChainWrap         wrapping style for method chains
	 * @param methodArgumentsWrap     wrapping style for method arguments
	 * @param binaryExpressionWrap    wrapping style for binary expressions
	 * @param methodParametersWrap    wrapping style for method parameters
	 * @param ternaryExpressionWrap   wrapping style for ternary expressions
	 * @param arrayInitializerWrap    wrapping style for array initializers
	 * @param annotationArgumentsWrap wrapping style for annotation arguments
	 * @param genericTypeArgsWrap     wrapping style for generic type arguments
	 * @param wrapLongStrings         whether to wrap long string literals
	 * @throws NullPointerException     if any required argument is null
	 * @throws IllegalArgumentException if any parameter is out of valid range
	 */
	private LineLengthConfiguration(
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
		boolean wrapLongStrings)
	{
		requireThat(ruleId, "ruleId").isNotBlank();
		requireThat(maxLineLength, "maxLineLength").
			isGreaterThanOrEqualTo(MIN_LINE_LENGTH).
			isLessThanOrEqualTo(MAX_LINE_LENGTH);
		requireThat(tabWidth, "tabWidth").
			isGreaterThanOrEqualTo(MIN_TAB_WIDTH).
			isLessThanOrEqualTo(MAX_TAB_WIDTH);
		requireThat(indentContinuationLines, "indentContinuationLines").
			isGreaterThanOrEqualTo(MIN_INDENT_CONTINUATION).
			isLessThanOrEqualTo(MAX_INDENT_CONTINUATION);
		requireThat(methodChainWrap, "methodChainWrap").isNotNull();
		requireThat(methodArgumentsWrap, "methodArgumentsWrap").isNotNull();
		requireThat(binaryExpressionWrap, "binaryExpressionWrap").isNotNull();
		requireThat(methodParametersWrap, "methodParametersWrap").isNotNull();
		requireThat(ternaryExpressionWrap, "ternaryExpressionWrap").isNotNull();
		requireThat(arrayInitializerWrap, "arrayInitializerWrap").isNotNull();
		requireThat(annotationArgumentsWrap, "annotationArgumentsWrap").isNotNull();
		requireThat(genericTypeArgsWrap, "genericTypeArgsWrap").isNotNull();

		this.ruleId = ruleId;
		this.maxLineLength = maxLineLength;
		this.tabWidth = tabWidth;
		this.indentContinuationLines = indentContinuationLines;
		this.methodChainWrap = methodChainWrap;
		this.methodArgumentsWrap = methodArgumentsWrap;
		this.binaryExpressionWrap = binaryExpressionWrap;
		this.methodParametersWrap = methodParametersWrap;
		this.ternaryExpressionWrap = ternaryExpressionWrap;
		this.arrayInitializerWrap = arrayInitializerWrap;
		this.annotationArgumentsWrap = annotationArgumentsWrap;
		this.genericTypeArgsWrap = genericTypeArgsWrap;
		this.wrapLongStrings = wrapLongStrings;
	}

	/**
	 * Creates a default configuration with recommended settings.
	 *
	 * @return default configuration instance
	 */
	public static LineLengthConfiguration defaultConfig()
	{
		return builder().build();
	}

	/**
	 * Creates a builder for fluent configuration.
	 *
	 * @return a new {@code Builder} instance
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Returns the unique identifier for the rule.
	 *
	 * @return the rule ID
	 */
	@Override
	public String ruleId()
	{
		return ruleId;
	}

	/**
	 * Returns the maximum allowed line length.
	 *
	 * @return the maximum line length (40-500)
	 */
	public int maxLineLength()
	{
		return maxLineLength;
	}

	/**
	 * Returns the width of a tab character.
	 *
	 * @return the tab width (1-8)
	 */
	public int tabWidth()
	{
		return tabWidth;
	}

	/**
	 * Returns the indentation for wrapped/continuation lines.
	 *
	 * @return the indentation (0-12)
	 */
	public int indentContinuationLines()
	{
		return indentContinuationLines;
	}

	/**
	 * Returns the wrapping style for method chains.
	 *
	 * @return the wrap style
	 */
	public WrapStyle methodChainWrap()
	{
		return methodChainWrap;
	}

	/**
	 * Returns the wrapping style for method arguments.
	 *
	 * @return the wrap style
	 */
	public WrapStyle methodArgumentsWrap()
	{
		return methodArgumentsWrap;
	}

	/**
	 * Returns the wrapping style for binary expressions.
	 *
	 * @return the wrap style
	 */
	public WrapStyle binaryExpressionWrap()
	{
		return binaryExpressionWrap;
	}

	/**
	 * Returns the wrapping style for method parameters.
	 *
	 * @return the wrap style
	 */
	public WrapStyle methodParametersWrap()
	{
		return methodParametersWrap;
	}

	/**
	 * Returns the wrapping style for ternary expressions.
	 *
	 * @return the wrap style
	 */
	public WrapStyle ternaryExpressionWrap()
	{
		return ternaryExpressionWrap;
	}

	/**
	 * Returns the wrapping style for array initializers.
	 *
	 * @return the wrap style
	 */
	public WrapStyle arrayInitializerWrap()
	{
		return arrayInitializerWrap;
	}

	/**
	 * Returns the wrapping style for annotation arguments.
	 *
	 * @return the wrap style
	 */
	public WrapStyle annotationArgumentsWrap()
	{
		return annotationArgumentsWrap;
	}

	/**
	 * Returns the wrapping style for generic type arguments.
	 *
	 * @return the wrap style
	 */
	public WrapStyle genericTypeArgsWrap()
	{
		return genericTypeArgsWrap;
	}

	/**
	 * Returns whether to wrap long string literals.
	 *
	 * @return {@code true} if long strings should be wrapped
	 */
	public boolean wrapLongStrings()
	{
		return wrapLongStrings;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof LineLengthConfiguration other))
			return false;
		return maxLineLength == other.maxLineLength &&
			tabWidth == other.tabWidth &&
			indentContinuationLines == other.indentContinuationLines &&
			wrapLongStrings == other.wrapLongStrings &&
			Objects.equals(ruleId, other.ruleId) &&
			methodChainWrap == other.methodChainWrap &&
			methodArgumentsWrap == other.methodArgumentsWrap &&
			binaryExpressionWrap == other.binaryExpressionWrap &&
			methodParametersWrap == other.methodParametersWrap &&
			ternaryExpressionWrap == other.ternaryExpressionWrap &&
			arrayInitializerWrap == other.arrayInitializerWrap &&
			annotationArgumentsWrap == other.annotationArgumentsWrap &&
			genericTypeArgsWrap == other.genericTypeArgsWrap;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(ruleId, maxLineLength, tabWidth, indentContinuationLines,
			methodChainWrap, methodArgumentsWrap, binaryExpressionWrap, methodParametersWrap,
			ternaryExpressionWrap, arrayInitializerWrap, annotationArgumentsWrap, genericTypeArgsWrap,
			wrapLongStrings);
	}

	@Override
	public String toString()
	{
		return "LineLengthConfiguration[" +
			"ruleId=" + ruleId +
			", maxLineLength=" + maxLineLength +
			", tabWidth=" + tabWidth +
			", indentContinuationLines=" + indentContinuationLines +
			", methodChainWrap=" + methodChainWrap +
			", methodArgumentsWrap=" + methodArgumentsWrap +
			", binaryExpressionWrap=" + binaryExpressionWrap +
			", methodParametersWrap=" + methodParametersWrap +
			", ternaryExpressionWrap=" + ternaryExpressionWrap +
			", arrayInitializerWrap=" + arrayInitializerWrap +
			", annotationArgumentsWrap=" + annotationArgumentsWrap +
			", genericTypeArgsWrap=" + genericTypeArgsWrap +
			", wrapLongStrings=" + wrapLongStrings +
			']';
	}

	/**
	 * Builder for creating {@code LineLengthConfiguration} instances.
	 * <p>
	 * This is the preferred way to create configuration instances, as it provides clear naming for each
	 * parameter and sensible defaults. All fields are initialized to default values matching
	 * {@link #defaultConfig()}.
	 */
	public static final class Builder
	{
		private String ruleId = "line-length";
		private int maxLineLength = DEFAULT_MAX_LENGTH;
		private int tabWidth = DEFAULT_TAB_WIDTH;
		private int indentContinuationLines = DEFAULT_INDENT_CONTINUATION;
		private WrapStyle methodChainWrap = WrapStyle.AFTER;
		private WrapStyle methodArgumentsWrap = WrapStyle.AFTER;
		private WrapStyle binaryExpressionWrap = WrapStyle.AFTER;
		private WrapStyle methodParametersWrap = WrapStyle.AFTER;
		private WrapStyle ternaryExpressionWrap = WrapStyle.AFTER;
		private WrapStyle arrayInitializerWrap = WrapStyle.AFTER;
		private WrapStyle annotationArgumentsWrap = WrapStyle.AFTER;
		private WrapStyle genericTypeArgsWrap = WrapStyle.AFTER;
		private boolean wrapLongStrings = true;

		/**
		 * Sets the unique identifier for this rule.
		 *
		 * @param ruleId the rule ID, must not be blank
		 * @return this builder for method chaining
		 * @throws NullPointerException     if {@code ruleId} is null
		 * @throws IllegalArgumentException if {@code ruleId} is blank
		 */
		public Builder ruleId(String ruleId)
		{
			requireThat(ruleId, "ruleId").isNotBlank();
			this.ruleId = ruleId;
			return this;
		}

		/**
		 * Sets the maximum allowed line length.
		 *
		 * @param maxLineLength the maximum line length (40-500)
		 * @return this builder for method chaining
		 * @throws IllegalArgumentException if {@code maxLineLength} is outside valid range
		 */
		public Builder maxLineLength(int maxLineLength)
		{
			requireThat(maxLineLength, "maxLineLength").
				isGreaterThanOrEqualTo(MIN_LINE_LENGTH).
				isLessThanOrEqualTo(MAX_LINE_LENGTH);
			this.maxLineLength = maxLineLength;
			return this;
		}

		/**
		 * Sets the width of a tab character.
		 *
		 * @param tabWidth the tab width (1-8)
		 * @return this builder for method chaining
		 * @throws IllegalArgumentException if {@code tabWidth} is outside valid range
		 */
		public Builder tabWidth(int tabWidth)
		{
			requireThat(tabWidth, "tabWidth").
				isGreaterThanOrEqualTo(MIN_TAB_WIDTH).
				isLessThanOrEqualTo(MAX_TAB_WIDTH);
			this.tabWidth = tabWidth;
			return this;
		}

		/**
		 * Sets the indentation for wrapped/continuation lines.
		 *
		 * @param indentContinuationLines the indentation (0-12)
		 * @return this builder for method chaining
		 * @throws IllegalArgumentException if {@code indentContinuationLines} is outside valid range
		 */
		public Builder indentContinuationLines(int indentContinuationLines)
		{
			requireThat(indentContinuationLines, "indentContinuationLines").
				isGreaterThanOrEqualTo(MIN_INDENT_CONTINUATION).
				isLessThanOrEqualTo(MAX_INDENT_CONTINUATION);
			this.indentContinuationLines = indentContinuationLines;
			return this;
		}

		/**
		 * Sets the wrapping style for method chains.
		 *
		 * @param methodChainWrap the wrap style, must not be null
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code methodChainWrap} is null
		 */
		public Builder methodChainWrap(WrapStyle methodChainWrap)
		{
			requireThat(methodChainWrap, "methodChainWrap").isNotNull();
			this.methodChainWrap = methodChainWrap;
			return this;
		}

		/**
		 * Sets the wrapping style for method arguments.
		 *
		 * @param methodArgumentsWrap the wrap style, must not be null
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code methodArgumentsWrap} is null
		 */
		public Builder methodArgumentsWrap(WrapStyle methodArgumentsWrap)
		{
			requireThat(methodArgumentsWrap, "methodArgumentsWrap").isNotNull();
			this.methodArgumentsWrap = methodArgumentsWrap;
			return this;
		}

		/**
		 * Sets the wrapping style for binary expressions.
		 *
		 * @param binaryExpressionWrap the wrap style, must not be null
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code binaryExpressionWrap} is null
		 */
		public Builder binaryExpressionWrap(WrapStyle binaryExpressionWrap)
		{
			requireThat(binaryExpressionWrap, "binaryExpressionWrap").isNotNull();
			this.binaryExpressionWrap = binaryExpressionWrap;
			return this;
		}

		/**
		 * Sets the wrapping style for method parameters.
		 *
		 * @param methodParametersWrap the wrap style, must not be null
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code methodParametersWrap} is null
		 */
		public Builder methodParametersWrap(WrapStyle methodParametersWrap)
		{
			requireThat(methodParametersWrap, "methodParametersWrap").isNotNull();
			this.methodParametersWrap = methodParametersWrap;
			return this;
		}

		/**
		 * Sets the wrapping style for ternary expressions.
		 *
		 * @param ternaryExpressionWrap the wrap style, must not be null
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code ternaryExpressionWrap} is null
		 */
		public Builder ternaryExpressionWrap(WrapStyle ternaryExpressionWrap)
		{
			requireThat(ternaryExpressionWrap, "ternaryExpressionWrap").isNotNull();
			this.ternaryExpressionWrap = ternaryExpressionWrap;
			return this;
		}

		/**
		 * Sets the wrapping style for array initializers.
		 *
		 * @param arrayInitializerWrap the wrap style, must not be null
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code arrayInitializerWrap} is null
		 */
		public Builder arrayInitializerWrap(WrapStyle arrayInitializerWrap)
		{
			requireThat(arrayInitializerWrap, "arrayInitializerWrap").isNotNull();
			this.arrayInitializerWrap = arrayInitializerWrap;
			return this;
		}

		/**
		 * Sets the wrapping style for annotation arguments.
		 *
		 * @param annotationArgumentsWrap the wrap style, must not be null
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code annotationArgumentsWrap} is null
		 */
		public Builder annotationArgumentsWrap(WrapStyle annotationArgumentsWrap)
		{
			requireThat(annotationArgumentsWrap, "annotationArgumentsWrap").isNotNull();
			this.annotationArgumentsWrap = annotationArgumentsWrap;
			return this;
		}

		/**
		 * Sets the wrapping style for generic type arguments.
		 *
		 * @param genericTypeArgsWrap the wrap style, must not be null
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code genericTypeArgsWrap} is null
		 */
		public Builder genericTypeArgsWrap(WrapStyle genericTypeArgsWrap)
		{
			requireThat(genericTypeArgsWrap, "genericTypeArgsWrap").isNotNull();
			this.genericTypeArgsWrap = genericTypeArgsWrap;
			return this;
		}

		/**
		 * Sets whether to wrap long string literals.
		 *
		 * @param wrapLongStrings {@code true} to wrap long strings
		 * @return this builder for method chaining
		 */
		public Builder wrapLongStrings(boolean wrapLongStrings)
		{
			this.wrapLongStrings = wrapLongStrings;
			return this;
		}

		/**
		 * Builds the {@code LineLengthConfiguration}.
		 *
		 * @return a new immutable configuration instance
		 */
		public LineLengthConfiguration build()
		{
			return new LineLengthConfiguration(ruleId, maxLineLength, tabWidth, indentContinuationLines,
				methodChainWrap, methodArgumentsWrap, binaryExpressionWrap, methodParametersWrap,
				ternaryExpressionWrap, arrayInitializerWrap, annotationArgumentsWrap, genericTypeArgsWrap,
				wrapLongStrings);
		}
	}
}
