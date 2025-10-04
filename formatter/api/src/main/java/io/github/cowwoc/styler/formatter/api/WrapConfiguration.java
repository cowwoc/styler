package io.github.cowwoc.styler.formatter.api;

import java.util.Objects;

/**
 * Centralized configuration for line wrapping behavior across all formatting rules.
 * <p>
 * This configuration decouples wrapping decisions (when to wrap) from wrapping
 * behavior (how to wrap). Rules determine when lines need wrapping, while this
 * configuration defines operator positioning, indentation strategy, and URL handling.
 * <p>
 * Instances are immutable and constructed via the builder pattern. Configuration
 * values are validated during {@link Builder#build()} to ensure semantic correctness.
 *
 * @see Builder
 */
public final class WrapConfiguration
{
	private static final int DEFAULT_MAX_LINE_LENGTH = 120;
	private static final int DEFAULT_CONTINUATION_INDENT_SPACES = 8;
	private static final int DEFAULT_TAB_WIDTH = 4;
	private static final boolean DEFAULT_WRAP_BEFORE_OPERATOR = true;
	private static final boolean DEFAULT_WRAP_BEFORE_DOT = true;
	private static final boolean DEFAULT_WRAP_AFTER_URLS_IN_STRINGS = false;
	private static final boolean DEFAULT_URL_PROTECTION = true;

	private static final int MIN_LINE_LENGTH = 40;
	private static final int MAX_LINE_LENGTH = 500;
	private static final int MIN_CONTINUATION_INDENT = 2;
	private static final int MAX_CONTINUATION_INDENT = 16;
	private static final int MIN_TAB_WIDTH = 1;
	private static final int MAX_TAB_WIDTH = 8;

	private final int maxLineLength;
	private final boolean wrapBeforeOperator;
	private final boolean wrapBeforeDot;
	private final boolean wrapAfterUrlsInStrings;
	private final boolean urlProtection;
	private final int continuationIndentSpaces;
	private final int tabWidth;

	/**
	 * Creates a new wrap configuration.
	 *
	 * @param builder the builder containing configuration values, never {@code null}
	 */
	private WrapConfiguration(Builder builder)
	{
		this.maxLineLength = builder.maxLineLength;
		this.wrapBeforeOperator = builder.wrapBeforeOperator;
		this.wrapBeforeDot = builder.wrapBeforeDot;
		this.wrapAfterUrlsInStrings = builder.wrapAfterUrlsInStrings;
		this.urlProtection = builder.urlProtection;
		this.continuationIndentSpaces = builder.continuationIndentSpaces;
		this.tabWidth = builder.tabWidth;
	}

	/**
	 * Creates a new builder for constructing wrap configuration.
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
	public static WrapConfiguration createDefault()
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
	 *
	 * @return the maximum line length, always between {@value MIN_LINE_LENGTH}
	 *         and {@value MAX_LINE_LENGTH}
	 */
	public int getMaxLineLength()
	{
		return maxLineLength;
	}

	/**
	 * Returns whether operators should have wraps before them rather than after.
	 * <p>
	 * When {@code true}, binary and unary operators stay with the following operand.
	 * When {@code false}, operators stay with the preceding operand.
	 *
	 * @return {@code true} to wrap before operators, {@code false} to wrap after
	 */
	public boolean isWrapBeforeOperator()
	{
		return wrapBeforeOperator;
	}

	/**
	 * Returns whether method chains should wrap before dot operators.
	 * <p>
	 * When {@code true}, the dot stays on the new line with the method name.
	 * When {@code false}, the dot stays on the previous line after the object.
	 *
	 * @return {@code true} to wrap before dot, {@code false} to wrap after
	 */
	public boolean isWrapBeforeDot()
	{
		return wrapBeforeDot;
	}

	/**
	 * Returns whether URLs in string literals should allow wrapping after them.
	 * <p>
	 * When {@code true}, wrapping may occur after complete URLs in strings.
	 * When {@code false}, URLs are protected from wrapping (default behavior).
	 *
	 * @return {@code true} to allow wrapping after URLs, {@code false} to protect them
	 */
	public boolean isWrapAfterUrlsInStrings()
	{
		return wrapAfterUrlsInStrings;
	}

	/**
	 * Returns whether URL protection is enabled.
	 * <p>
	 * When enabled, wrapping points within URLs in string literals and comments
	 * are filtered out to prevent breaking URLs.
	 *
	 * @return {@code true} if URL protection is enabled, {@code false} otherwise
	 */
	public boolean isUrlProtection()
	{
		return urlProtection;
	}

	/**
	 * Returns the continuation indent in spaces for wrapped lines.
	 * <p>
	 * Wrapped lines are indented by this amount beyond the base indentation
	 * of the original line.
	 *
	 * @return the continuation indent in spaces, always between
	 *         {@value MIN_CONTINUATION_INDENT} and {@value MAX_CONTINUATION_INDENT}
	 */
	public int getContinuationIndentSpaces()
	{
		return continuationIndentSpaces;
	}

	/**
	 * Returns the tab width for calculating effective line length.
	 * <p>
	 * Tabs are expanded to spaces using this width when measuring line length.
	 *
	 * @return the tab width in spaces, always between {@value MIN_TAB_WIDTH}
	 *         and {@value MAX_TAB_WIDTH}
	 */
	public int getTabWidth()
	{
		return tabWidth;
	}

	/**
	 * Validates this configuration.
	 *
	 * @throws ConfigurationException if any parameter is invalid
	 */
	public void validate() throws ConfigurationException
	{
		if (maxLineLength < MIN_LINE_LENGTH || maxLineLength > MAX_LINE_LENGTH)
		{
			throw new ConfigurationException(String.format(
				"maxLineLength must be between %d and %d, got: %d",
				MIN_LINE_LENGTH, MAX_LINE_LENGTH, maxLineLength));
		}
		if (continuationIndentSpaces < MIN_CONTINUATION_INDENT ||
			continuationIndentSpaces > MAX_CONTINUATION_INDENT)
		{
			throw new ConfigurationException(String.format(
				"continuationIndentSpaces must be between %d and %d, got: %d",
				MIN_CONTINUATION_INDENT, MAX_CONTINUATION_INDENT, continuationIndentSpaces));
		}
		if (tabWidth < MIN_TAB_WIDTH || tabWidth > MAX_TAB_WIDTH)
		{
			throw new ConfigurationException(String.format(
				"tabWidth must be between %d and %d, got: %d",
				MIN_TAB_WIDTH, MAX_TAB_WIDTH, tabWidth));
		}
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other) return true;
		if (other == null || getClass() != other.getClass()) return false;

		WrapConfiguration that = (WrapConfiguration) other;
		return maxLineLength == that.maxLineLength &&
		       wrapBeforeOperator == that.wrapBeforeOperator &&
		       wrapBeforeDot == that.wrapBeforeDot &&
		       wrapAfterUrlsInStrings == that.wrapAfterUrlsInStrings &&
		       urlProtection == that.urlProtection &&
		       continuationIndentSpaces == that.continuationIndentSpaces &&
		       tabWidth == that.tabWidth;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(maxLineLength, wrapBeforeOperator, wrapBeforeDot, wrapAfterUrlsInStrings,
			urlProtection, continuationIndentSpaces, tabWidth);
	}

	@Override
	public String toString()
	{
		return String.format("WrapConfiguration[maxLineLength=%d, wrapBeforeOperator=%b, " +
				"wrapBeforeDot=%b, wrapAfterUrlsInStrings=%b, urlProtection=%b, " +
				"continuationIndentSpaces=%d, tabWidth=%d]",
			maxLineLength, wrapBeforeOperator, wrapBeforeDot, wrapAfterUrlsInStrings,
			urlProtection, continuationIndentSpaces, tabWidth);
	}

	/**
	 * Builder for constructing {@code WrapConfiguration} instances with a fluent API.
	 * <p>
	 * This builder uses the builder pattern to provide type-safe configuration
	 * construction with validation applied during the build step.
	 */
	public static final class Builder
	{
		private int maxLineLength = DEFAULT_MAX_LINE_LENGTH;
		private boolean wrapBeforeOperator = DEFAULT_WRAP_BEFORE_OPERATOR;
		private boolean wrapBeforeDot = DEFAULT_WRAP_BEFORE_DOT;
		private boolean wrapAfterUrlsInStrings = DEFAULT_WRAP_AFTER_URLS_IN_STRINGS;
		private boolean urlProtection = DEFAULT_URL_PROTECTION;
		private int continuationIndentSpaces = DEFAULT_CONTINUATION_INDENT_SPACES;
		private int tabWidth = DEFAULT_TAB_WIDTH;

		/**
		 * Sets the maximum allowed line length.
		 *
		 * @param maxLineLength the maximum line length, must be between
		 *                      {@value MIN_LINE_LENGTH} and {@value MAX_LINE_LENGTH}
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withMaxLineLength(int maxLineLength)
		{
			this.maxLineLength = maxLineLength;
			return this;
		}

		/**
		 * Sets whether to wrap before operators.
		 *
		 * @param wrapBeforeOperator {@code true} to wrap before operators,
		 *                           {@code false} to wrap after
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withWrapBeforeOperator(boolean wrapBeforeOperator)
		{
			this.wrapBeforeOperator = wrapBeforeOperator;
			return this;
		}

		/**
		 * Sets whether to wrap before dot operators in method chains.
		 *
		 * @param wrapBeforeDot {@code true} to wrap before dot, {@code false} to wrap after
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withWrapBeforeDot(boolean wrapBeforeDot)
		{
			this.wrapBeforeDot = wrapBeforeDot;
			return this;
		}

		/**
		 * Sets whether to allow wrapping after URLs in string literals.
		 *
		 * @param wrapAfterUrlsInStrings {@code true} to allow wrapping after URLs,
		 *                               {@code false} to protect URLs
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withWrapAfterUrlsInStrings(boolean wrapAfterUrlsInStrings)
		{
			this.wrapAfterUrlsInStrings = wrapAfterUrlsInStrings;
			return this;
		}

		/**
		 * Sets whether URL protection is enabled.
		 *
		 * @param urlProtection {@code true} to enable URL protection,
		 *                      {@code false} to disable
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withUrlProtection(boolean urlProtection)
		{
			this.urlProtection = urlProtection;
			return this;
		}

		/**
		 * Sets the continuation indent for wrapped lines.
		 *
		 * @param continuationIndentSpaces the continuation indent in spaces,
		 *                                 must be between {@value MIN_CONTINUATION_INDENT}
		 *                                 and {@value MAX_CONTINUATION_INDENT}
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withContinuationIndentSpaces(int continuationIndentSpaces)
		{
			this.continuationIndentSpaces = continuationIndentSpaces;
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
		 * Builds the configuration and validates all parameters.
		 *
		 * @return a new {@code WrapConfiguration}, never {@code null}
		 * @throws ConfigurationException if any parameters are invalid
		 */
		public WrapConfiguration build() throws ConfigurationException
		{
			WrapConfiguration config = new WrapConfiguration(this);
			config.validate();
			return config;
		}
	}
}
