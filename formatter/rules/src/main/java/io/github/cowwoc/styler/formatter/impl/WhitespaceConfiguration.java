package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.formatter.api.ConfigurationException;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;

import java.util.Objects;

/**
 * Configuration for the whitespace formatting rule.
 * <p>
 * This immutable configuration object controls whitespace normalization around
 * operators, keywords, and punctuation in Java source code. It supports validation,
 * merging, and provides a fluent builder API for construction.
 * <p>
 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.6">JLS §3.6</a>,
 * white space is defined as the ASCII space character, horizontal tab character,
 * form feed character, and line terminator characters.
 */
public final class WhitespaceConfiguration extends RuleConfiguration
{
	private static final int DEFAULT_OPERATOR_SPACING = 1;
	private static final int DEFAULT_KEYWORD_SPACING = 1;
	private static final boolean DEFAULT_SPACE_AFTER_COMMA = true;
	private static final boolean DEFAULT_SPACE_BEFORE_BRACE = true;

	private static final int MIN_SPACING = 0;
	private static final int MAX_SPACING = 4;

	private final int operatorSpacing;
	private final int keywordSpacing;
	private final boolean spaceAfterComma;
	private final boolean spaceBeforeBrace;

	/**
	 * Creates a new whitespace configuration.
	 *
	 * @param builder the builder containing configuration values, never {@code null}
	 */
	private WhitespaceConfiguration(Builder builder)
	{
		this.operatorSpacing = builder.operatorSpacing;
		this.keywordSpacing = builder.keywordSpacing;
		this.spaceAfterComma = builder.spaceAfterComma;
		this.spaceBeforeBrace = builder.spaceBeforeBrace;
	}

	/**
	 * Creates a new builder for constructing whitespace configuration.
	 *
	 * @return a new builder with default values, never {@code null}
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Creates a default configuration with standard Java formatting conventions.
	 * <p>
	 * The default configuration is guaranteed to be valid and uses standard
	 * Java whitespace formatting rules.
	 *
	 * @return a default configuration, never {@code null}
	 */
	public static WhitespaceConfiguration createDefault()
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
	 * Returns the number of spaces around binary operators.
	 * <p>
	 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html#jls-15.17">JLS §15.17-15.24</a>,
	 * binary operators include arithmetic (+, -, *, /, %), comparison (==, !=, <, >),
	 * and logical (&&, ||) operators.
	 *
	 * @return the operator spacing in spaces, always between {@value MIN_SPACING}
	 *         and {@value MAX_SPACING}
	 */
	public int getOperatorSpacing()
	{
		return operatorSpacing;
	}

	/**
	 * Returns the number of spaces after keywords.
	 * <p>
	 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.9">JLS §3.9</a>,
	 * keywords include control flow (if, for, while) and declaration (class, interface) keywords.
	 *
	 * @return the keyword spacing in spaces, always between {@value MIN_SPACING}
	 *         and {@value MAX_SPACING}
	 */
	public int getKeywordSpacing()
	{
		return keywordSpacing;
	}

	/**
	 * Returns whether a space should appear after commas.
	 * <p>
	 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.11">JLS §3.11</a>,
	 * commas are separators used in parameter lists, argument lists, and array initializers.
	 *
	 * @return {@code true} if space after comma is required, {@code false} otherwise
	 */
	public boolean isSpaceAfterComma()
	{
		return spaceAfterComma;
	}

	/**
	 * Returns whether a space should appear before opening braces.
	 *
	 * @return {@code true} if space before brace is required, {@code false} otherwise
	 */
	public boolean isSpaceBeforeBrace()
	{
		return spaceBeforeBrace;
	}

	@Override
	public void validate() throws ConfigurationException
	{
		validateNumericRange("operatorSpacing", operatorSpacing, MIN_SPACING, MAX_SPACING);
		validateNumericRange("keywordSpacing", keywordSpacing, MIN_SPACING, MAX_SPACING);
	}

	@Override
	public RuleConfiguration merge(RuleConfiguration override)
	{
		if (override == null)
		{
			return this;
		}
		if (!(override instanceof WhitespaceConfiguration other))
		{
			throw new IllegalArgumentException(
				"Cannot merge WhitespaceConfiguration with " + override.getClass().getName());
		}

		try
		{
			return builder().
				withOperatorSpacing(other.operatorSpacing).
				withKeywordSpacing(other.keywordSpacing).
				withSpaceAfterComma(other.spaceAfterComma).
				withSpaceBeforeBrace(other.spaceBeforeBrace).
				build();
		}
		catch (ConfigurationException e)
		{
			throw new AssertionError("Merged configuration should be valid", e);
		}
	}

	@Override
	public String getDescription()
	{
		return String.format(
			"WhitespaceConfiguration[operatorSpacing=%d, keywordSpacing=%d, " +
			"spaceAfterComma=%b, spaceBeforeBrace=%b]",
			operatorSpacing, keywordSpacing, spaceAfterComma, spaceBeforeBrace);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof WhitespaceConfiguration other))
		{
			return false;
		}
		return operatorSpacing == other.operatorSpacing &&
			keywordSpacing == other.keywordSpacing &&
			spaceAfterComma == other.spaceAfterComma &&
			spaceBeforeBrace == other.spaceBeforeBrace;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(operatorSpacing, keywordSpacing, spaceAfterComma, spaceBeforeBrace);
	}

	/**
	 * Builder for constructing {@link WhitespaceConfiguration} instances.
	 * <p>
	 * This builder provides a fluent API for configuring whitespace formatting rules
	 * and validates all parameters before construction.
	 */
	public static final class Builder
	{
		private int operatorSpacing = DEFAULT_OPERATOR_SPACING;
		private int keywordSpacing = DEFAULT_KEYWORD_SPACING;
		private boolean spaceAfterComma = DEFAULT_SPACE_AFTER_COMMA;
		private boolean spaceBeforeBrace = DEFAULT_SPACE_BEFORE_BRACE;

		/**
		 * Sets the number of spaces around binary operators.
		 *
		 * @param spaces the number of spaces, must be between {@value MIN_SPACING}
		 *               and {@value MAX_SPACING}
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withOperatorSpacing(int spaces)
		{
			this.operatorSpacing = spaces;
			return this;
		}

		/**
		 * Sets the number of spaces after keywords.
		 *
		 * @param spaces the number of spaces, must be between {@value MIN_SPACING}
		 *               and {@value MAX_SPACING}
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withKeywordSpacing(int spaces)
		{
			this.keywordSpacing = spaces;
			return this;
		}

		/**
		 * Sets whether a space should appear after commas.
		 *
		 * @param required {@code true} to require space after comma, {@code false} otherwise
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withSpaceAfterComma(boolean required)
		{
			this.spaceAfterComma = required;
			return this;
		}

		/**
		 * Sets whether a space should appear before opening braces.
		 *
		 * @param required {@code true} to require space before brace, {@code false} otherwise
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withSpaceBeforeBrace(boolean required)
		{
			this.spaceBeforeBrace = required;
			return this;
		}

		/**
		 * Builds a new whitespace configuration with the specified parameters.
		 * <p>
		 * This method validates all parameters before constructing the configuration.
		 *
		 * @return a new configuration instance, never {@code null}
		 * @throws ConfigurationException if any parameter is invalid
		 */
		public WhitespaceConfiguration build() throws ConfigurationException
		{
			WhitespaceConfiguration config = new WhitespaceConfiguration(this);
			config.validate();
			return config;
		}
	}
}
