package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.formatter.api.ConfigurationException;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;

import java.util.Objects;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Configuration for the line length formatting rule.
 * <p>
 * This immutable configuration object controls how the line length rule analyzes
 * and wraps lines that exceed the maximum length. It supports validation, merging,
 * and provides a fluent builder API for construction.
 * <p>
 * Configuration parameters include maximum line length, tab width, and wrapping
 * strategies for different code elements like method chains and parameters.
 * <p>
 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.4">JLS §3.4</a>,
 * line terminators are defined as CR, LF, or CRLF sequences.
 */
public final class LineLengthConfiguration extends RuleConfiguration
{
	private static final int DEFAULT_MAX_LINE_LENGTH = 120;
	private static final int DEFAULT_TAB_WIDTH = 4;
	private static final int MIN_LINE_LENGTH = 40;
	private static final int MAX_LINE_LENGTH = 500;
	private static final int MIN_TAB_WIDTH = 1;
	private static final int MAX_TAB_WIDTH = 8;

	private final int maxLineLength;
	private final int tabWidth;
	private final boolean wrapMethodChains;
	private final boolean wrapParameters;
	private final boolean breakBeforeOperator;

	/**
	 * Creates a new line length configuration.
	 *
	 * @param builder the builder containing configuration values, never {@code null}
	 */
	private LineLengthConfiguration(Builder builder)
	{
		this.maxLineLength = builder.maxLineLength;
		this.tabWidth = builder.tabWidth;
		this.wrapMethodChains = builder.wrapMethodChains;
		this.wrapParameters = builder.wrapParameters;
		this.breakBeforeOperator = builder.breakBeforeOperator;
	}

	/**
	 * Creates a new builder for constructing line length configuration.
	 *
	 * @return a new builder with default values, never {@code null}
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Creates a default configuration with standard values.
	 * <p>
	 * The default configuration is guaranteed to be valid and uses standard
	 * Java formatting conventions.
	 *
	 * @return a default configuration, never {@code null}
	 */
	public static LineLengthConfiguration createDefault()
	{
		try
		{
			return builder().build();
		}
		catch (ConfigurationException e)
		{
			throw new IllegalStateException("Default configuration should always be valid", e);
		}
	}

	/**
	 * Returns the maximum allowed line length in characters.
	 * <p>
	 * Lines exceeding this length will be flagged as violations and may be
	 * automatically wrapped if wrapping is enabled.
	 *
	 * @return the maximum line length, always between {@value MIN_LINE_LENGTH}
	 *         and {@value MAX_LINE_LENGTH}
	 */
	public int getMaxLineLength()
	{
		return maxLineLength;
	}

	/**
	 * Returns the tab width for calculating effective line length.
	 * <p>
	 * Tabs are expanded to spaces using this width when measuring line length.
	 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.2">JLS §3.2</a>,
	 * tab characters are white space in Java source code.
	 *
	 * @return the tab width in spaces, always between {@value MIN_TAB_WIDTH}
	 *         and {@value MAX_TAB_WIDTH}
	 */
	public int getTabWidth()
	{
		return tabWidth;
	}

	/**
	 * Returns whether method chains should be wrapped at dot operators.
	 *
	 * @return {@code true} if method chain wrapping is enabled, {@code false} otherwise
	 */
	public boolean isWrapMethodChains()
	{
		return wrapMethodChains;
	}

	/**
	 * Returns whether parameter lists should be wrapped.
	 *
	 * @return {@code true} if parameter wrapping is enabled, {@code false} otherwise
	 */
	public boolean isWrapParameters()
	{
		return wrapParameters;
	}

	/**
	 * Returns whether operators should have breaks before them rather than after.
	 *
	 * @return {@code true} to break before operators, {@code false} to break after
	 */
	public boolean isBreakBeforeOperator()
	{
		return breakBeforeOperator;
	}

	@Override
	public void validate() throws ConfigurationException
	{
		validateNumericRange("maxLineLength", maxLineLength, MIN_LINE_LENGTH, MAX_LINE_LENGTH);
		validateNumericRange("tabWidth", tabWidth, MIN_TAB_WIDTH, MAX_TAB_WIDTH);
	}

	@Override
	public RuleConfiguration merge(RuleConfiguration override)
	{
		requireThat(override, "override").isNotNull();

		if (!(override instanceof LineLengthConfiguration other))
		{
			throw new IllegalArgumentException(
				"Cannot merge LineLengthConfiguration with " + override.getClass().getSimpleName());
		}

		if (this.equals(other))
		{
			return this;
		}
		return other;
	}

	@Override
	public String getDescription()
	{
		return String.format(
			"LineLengthConfiguration[maxLineLength=%d, tabWidth=%d, " +
			"wrapMethodChains=%b, wrapParameters=%b, breakBeforeOperator=%b]",
			maxLineLength, tabWidth, wrapMethodChains, wrapParameters, breakBeforeOperator);
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other) return true;
		if (other == null || getClass() != other.getClass()) return false;

		LineLengthConfiguration that = (LineLengthConfiguration) other;
		return maxLineLength == that.maxLineLength &&
		       tabWidth == that.tabWidth &&
		       wrapMethodChains == that.wrapMethodChains &&
		       wrapParameters == that.wrapParameters &&
		       breakBeforeOperator == that.breakBeforeOperator;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(maxLineLength, tabWidth, wrapMethodChains, wrapParameters,
		                    breakBeforeOperator);
	}

	/**
	 * Builder for constructing {@code LineLengthConfiguration} instances with a fluent API.
	 * <p>
	 * This builder uses the builder pattern to provide type-safe configuration
	 * construction with validation applied during the build step.
	 */
	public static final class Builder
	{
		private int maxLineLength = DEFAULT_MAX_LINE_LENGTH;
		private int tabWidth = DEFAULT_TAB_WIDTH;
		private boolean wrapMethodChains = true;
		private boolean wrapParameters = true;
		private boolean breakBeforeOperator = true;

		/**
		 * Sets the maximum allowed line length.
		 *
		 * @param maxLineLength the maximum line length, must be between {@value MIN_LINE_LENGTH}
		 *                      and {@value MAX_LINE_LENGTH}
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withMaxLineLength(int maxLineLength)
		{
			this.maxLineLength = maxLineLength;
			return this;
		}

		/**
		 * Sets the tab width for line length calculation.
		 *
		 * @param tabWidth the tab width in spaces, must be between {@value MIN_TAB_WIDTH}
		 *                 and {@value MAX_TAB_WIDTH}
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withTabWidth(int tabWidth)
		{
			this.tabWidth = tabWidth;
			return this;
		}

		/**
		 * Sets whether method chains should be wrapped.
		 *
		 * @param wrapMethodChains {@code true} to enable method chain wrapping
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withWrapMethodChains(boolean wrapMethodChains)
		{
			this.wrapMethodChains = wrapMethodChains;
			return this;
		}

		/**
		 * Sets whether parameter lists should be wrapped.
		 *
		 * @param wrapParameters {@code true} to enable parameter wrapping
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withWrapParameters(boolean wrapParameters)
		{
			this.wrapParameters = wrapParameters;
			return this;
		}

		/**
		 * Sets whether to break before operators rather than after.
		 *
		 * @param breakBeforeOperator {@code true} to break before operators
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withBreakBeforeOperator(boolean breakBeforeOperator)
		{
			this.breakBeforeOperator = breakBeforeOperator;
			return this;
		}

		/**
		 * Builds the configuration and validates all parameters.
		 *
		 * @return a new {@code LineLengthConfiguration}, never {@code null}
		 * @throws ConfigurationException if any parameters are invalid
		 */
		public LineLengthConfiguration build() throws ConfigurationException
		{
			LineLengthConfiguration config = new LineLengthConfiguration(this);
			config.validate();
			return config;
		}
	}
}
