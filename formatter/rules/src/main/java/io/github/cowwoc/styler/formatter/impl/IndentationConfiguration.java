package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.formatter.api.ConfigurationException;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;

import java.util.Objects;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Configuration for the indentation formatting rule.
 * <p>
 * This immutable configuration object controls how the indentation rule applies
 * consistent indentation to Java source code. It supports multiple indentation
 * modes (tabs, spaces, or mixed), configurable depth, and options for continuation
 * lines and alignment.
 * <p>
 * The configuration uses a fluent builder API for construction and includes
 * validation to ensure all parameters are within acceptable ranges.
 * <p>
 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.6">JLS §3.6</a>,
 * white space is defined as space (U+0020) and horizontal tab (U+0009) characters.
 */
public final class IndentationConfiguration extends RuleConfiguration
{
	private static final int DEFAULT_INDENT_SIZE = 4;
	private static final int DEFAULT_CONTINUATION_INDENT = 4;
	private static final int DEFAULT_TAB_WIDTH = 4;
	private static final int MIN_INDENT_SIZE = 1;
	private static final int MAX_INDENT_SIZE = 16;
	private static final int MIN_TAB_WIDTH = 1;
	private static final int MAX_TAB_WIDTH = 8;

	private final IndentationMode mode;
	private final int indentSize;
	private final int continuationIndent;
	private final int tabWidth;
	private final boolean alignArrayElements;
	private final boolean alignParameters;
	private final boolean preserveCommentIndentation;

	/**
	 * Creates a new indentation configuration.
	 *
	 * @param builder the builder containing configuration values, never {@code null}
	 */
	private IndentationConfiguration(Builder builder)
	{
		this.mode = builder.mode;
		this.indentSize = builder.indentSize;
		this.continuationIndent = builder.continuationIndent;
		this.tabWidth = builder.tabWidth;
		this.alignArrayElements = builder.alignArrayElements;
		this.alignParameters = builder.alignParameters;
		this.preserveCommentIndentation = builder.preserveCommentIndentation;
	}

	/**
	 * Creates a new builder for constructing indentation configuration.
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
	 * The default configuration uses 4-space indentation (Oracle/Sun style),
	 * which is the most widely adopted Java formatting convention. All
	 * alignment options are enabled by default for consistent formatting.
	 *
	 * @return a default configuration, never {@code null}
	 */
	public static IndentationConfiguration createDefault()
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
	 * Returns the indentation mode.
	 *
	 * @return the indentation mode (SPACES, TABS, or MIXED), never {@code null}
	 */
	public IndentationMode getMode()
	{
		return mode;
	}

	/**
	 * Returns the indentation size in spaces per level.
	 * <p>
	 * For {@link IndentationMode#SPACES} mode, this is the number of spaces
	 * added for each indentation level. For {@link IndentationMode#TABS} mode,
	 * this value is used for mixed-mode calculations. For {@link IndentationMode#MIXED}
	 * mode, this determines the structural tab positions.
	 *
	 * @return the indent size, always between {@value MIN_INDENT_SIZE}
	 *         and {@value MAX_INDENT_SIZE}
	 */
	public int getIndentSize()
	{
		return indentSize;
	}

	/**
	 * Returns the continuation indent for wrapped lines.
	 * <p>
	 * This is the additional indentation applied to lines that continue a
	 * previous statement, such as method chains, wrapped parameters, or
	 * multi-line expressions.
	 *
	 * @return the continuation indent, always between {@value MIN_INDENT_SIZE}
	 *         and {@value MAX_INDENT_SIZE}
	 */
	public int getContinuationIndent()
	{
		return continuationIndent;
	}

	/**
	 * Returns the tab width for measurement and mixed mode.
	 * <p>
	 * For {@link IndentationMode#TABS} and {@link IndentationMode#MIXED} modes,
	 * this defines the visual width of a tab character when calculating positions
	 * for alignment.
	 *
	 * @return the tab width, always between {@value MIN_TAB_WIDTH}
	 *         and {@value MAX_TAB_WIDTH}
	 */
	public int getTabWidth()
	{
		return tabWidth;
	}

	/**
	 * Returns whether array elements should be vertically aligned.
	 * <p>
	 * When enabled, array initializers format with elements aligned:
	 * <pre>{@code
	 * int[][] matrix = {
	 *     {1, 2, 3},
	 *     {4, 5, 6}
	 * };
	 * }</pre>
	 * When disabled, standard continuation indent is used.
	 *
	 * @return {@code true} if array elements should be aligned, {@code false} otherwise
	 */
	public boolean isAlignArrayElements()
	{
		return alignArrayElements;
	}

	/**
	 * Returns whether method parameters should be aligned.
	 * <p>
	 * When enabled, wrapped method parameters align with the first parameter:
	 * <pre>{@code
	 * void method(String param1,
	 *             String param2)
	 * }</pre>
	 * When disabled, continuation indent is used for all wrapped parameters.
	 *
	 * @return {@code true} if parameters should be aligned, {@code false} otherwise
	 */
	public boolean isAlignParameters()
	{
		return alignParameters;
	}

	/**
	 * Returns whether existing comment indentation should be preserved.
	 * <p>
	 * When enabled, comments retain their current indentation relative to
	 * surrounding code. When disabled, comments are normalized to match
	 * the structural indentation level.
	 * <p>
	 * Note: Javadoc comments always follow structural indentation rules
	 * regardless of this setting.
	 *
	 * @return {@code true} if comment indentation should be preserved,
	 *         {@code false} to normalize
	 */
	public boolean isPreserveCommentIndentation()
	{
		return preserveCommentIndentation;
	}

	/**
	 * Validates the configuration parameters.
	 *
	 * @throws ConfigurationException if any parameter is invalid
	 */
	@Override
	public void validate() throws ConfigurationException
	{
		requireThat(mode, "mode").isNotNull();
		validateNumericRange("indentSize", indentSize, MIN_INDENT_SIZE, MAX_INDENT_SIZE);
		validateNumericRange("continuationIndent", continuationIndent, MIN_INDENT_SIZE, MAX_INDENT_SIZE);
		validateNumericRange("tabWidth", tabWidth, MIN_TAB_WIDTH, MAX_TAB_WIDTH);
	}

	@Override
	public RuleConfiguration merge(RuleConfiguration override)
	{
		requireThat(override, "override").isNotNull();

		if (!(override instanceof IndentationConfiguration other))
		{
			throw new IllegalArgumentException(
				"Cannot merge IndentationConfiguration with " + override.getClass().getSimpleName());
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
			"IndentationConfiguration[mode=%s, indentSize=%d, continuationIndent=%d, " +
			"tabWidth=%d, alignArrayElements=%b, alignParameters=%b, " +
			"preserveCommentIndentation=%b]",
			mode, indentSize, continuationIndent, tabWidth,
			alignArrayElements, alignParameters, preserveCommentIndentation);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof IndentationConfiguration other))
		{
			return false;
		}
		return mode == other.mode &&
			indentSize == other.indentSize &&
			continuationIndent == other.continuationIndent &&
			tabWidth == other.tabWidth &&
			alignArrayElements == other.alignArrayElements &&
			alignParameters == other.alignParameters &&
			preserveCommentIndentation == other.preserveCommentIndentation;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(mode, indentSize, continuationIndent, tabWidth,
			alignArrayElements, alignParameters, preserveCommentIndentation);
	}

	/**
	 * Builder for constructing indentation configuration instances.
	 * <p>
	 * This builder provides a fluent API for configuring all indentation
	 * parameters with validation at build time.
	 */
	public static final class Builder
	{
		private IndentationMode mode = IndentationMode.SPACES;
		private int indentSize = DEFAULT_INDENT_SIZE;
		private int continuationIndent = DEFAULT_CONTINUATION_INDENT;
		private int tabWidth = DEFAULT_TAB_WIDTH;
		private boolean alignArrayElements = true;
		private boolean alignParameters = true;
		private boolean preserveCommentIndentation;

		/**
		 * Creates a new builder with default values.
		 */
		private Builder()
		{
		}

		/**
		 * Sets the indentation mode.
		 *
		 * @param mode the indentation mode, never {@code null}
		 * @return this builder for method chaining, never {@code null}
		 * @throws NullPointerException if {@code mode} is {@code null}
		 */
		public Builder withMode(IndentationMode mode)
		{
			requireThat(mode, "mode").isNotNull();
			this.mode = mode;
			return this;
		}

		/**
		 * Sets the indentation size.
		 *
		 * @param indentSize the indent size in spaces per level, must be between
		 *                   {@value MIN_INDENT_SIZE} and {@value MAX_INDENT_SIZE}
		 * @return this builder for method chaining, never {@code null}
		 * @throws IllegalArgumentException if {@code indentSize} is out of range
		 */
		public Builder withIndentSize(int indentSize)
		{
			requireThat(indentSize, "indentSize").
				isBetween(MIN_INDENT_SIZE, true, MAX_INDENT_SIZE, true);
			this.indentSize = indentSize;
			return this;
		}

		/**
		 * Sets the continuation indent.
		 *
		 * @param continuationIndent the continuation indent in spaces, must be between
		 *                           {@value MIN_INDENT_SIZE} and {@value MAX_INDENT_SIZE}
		 * @return this builder for method chaining, never {@code null}
		 * @throws IllegalArgumentException if {@code continuationIndent} is out of range
		 */
		public Builder withContinuationIndent(int continuationIndent)
		{
			requireThat(continuationIndent, "continuationIndent").
				isBetween(MIN_INDENT_SIZE, true, MAX_INDENT_SIZE, true);
			this.continuationIndent = continuationIndent;
			return this;
		}

		/**
		 * Sets the tab width.
		 *
		 * @param tabWidth the tab width in spaces, must be between
		 *                 {@value MIN_TAB_WIDTH} and {@value MAX_TAB_WIDTH}
		 * @return this builder for method chaining, never {@code null}
		 * @throws IllegalArgumentException if {@code tabWidth} is out of range
		 */
		public Builder withTabWidth(int tabWidth)
		{
			requireThat(tabWidth, "tabWidth").
				isBetween(MIN_TAB_WIDTH, true, MAX_TAB_WIDTH, true);
			this.tabWidth = tabWidth;
			return this;
		}

		/**
		 * Sets whether array elements should be aligned.
		 *
		 * @param align {@code true} to enable array element alignment, {@code false} otherwise
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withAlignArrayElements(boolean align)
		{
			this.alignArrayElements = align;
			return this;
		}

		/**
		 * Sets whether method parameters should be aligned.
		 *
		 * @param align {@code true} to enable parameter alignment, {@code false} otherwise
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withAlignParameters(boolean align)
		{
			this.alignParameters = align;
			return this;
		}

		/**
		 * Sets whether comment indentation should be preserved.
		 *
		 * @param preserve {@code true} to preserve existing comment indentation,
		 *                 {@code false} to normalize
		 * @return this builder for method chaining, never {@code null}
		 */
		public Builder withPreserveCommentIndentation(boolean preserve)
		{
			this.preserveCommentIndentation = preserve;
			return this;
		}

		/**
		 * Builds the indentation configuration.
		 *
		 * @return a new indentation configuration, never {@code null}
		 * @throws ConfigurationException if the configuration is invalid
		 */
		public IndentationConfiguration build() throws ConfigurationException
		{
			IndentationConfiguration config = new IndentationConfiguration(this);
			config.validate();
			return config;
		}
	}
}
