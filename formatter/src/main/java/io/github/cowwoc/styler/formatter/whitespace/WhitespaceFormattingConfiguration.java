package io.github.cowwoc.styler.formatter.whitespace;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;

import java.util.Objects;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Configuration for whitespace formatting rules.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class WhitespaceFormattingConfiguration implements FormattingConfiguration
{
	private final String ruleId;
	private final boolean spaceAroundBinaryOperator;
	private final boolean spaceAroundAssignmentOperators;
	private final boolean spaceAfterComma;
	private final boolean spaceAfterSemicolonInForLoop;
	private final boolean spaceAfterControlKeyword;
	private final boolean spaceBeforeOpenBrace;
	private final boolean spaceAroundColonInEnhancedForLoop;
	private final boolean spaceAroundArrowInLambda;
	private final boolean noSpaceAroundMethodReference;

	/**
	 * Creates a whitespace formatting configuration.
	 *
	 * @param ruleId                         the rule ID
	 * @param spaceAroundBinaryOperator     whether to add space around binary operators
	 * @param spaceAroundAssignmentOperators whether to add space around assignment operators
	 * @param spaceAfterComma                whether to add space after commas
	 * @param spaceAfterSemicolonInForLoop       whether to add space after semicolons in for loops
	 * @param spaceAfterControlKeyword      whether to add space after control keywords
	 * @param spaceBeforeOpenBrace           whether to add space before opening braces
	 * @param spaceAroundColonInEnhancedForLoop  whether to add space around colons in enhanced for loops
	 * @param spaceAroundArrowInLambda       whether to add space around lambda arrows
	 * @param noSpaceAroundMethodReference   whether to remove space around method references
	 * @throws NullPointerException     if {@code ruleId} is null
	 * @throws IllegalArgumentException if {@code ruleId} is blank
	 */
	private WhitespaceFormattingConfiguration(
		String ruleId,
		boolean spaceAroundBinaryOperator,
		boolean spaceAroundAssignmentOperators,
		boolean spaceAfterComma,
		boolean spaceAfterSemicolonInForLoop,
		boolean spaceAfterControlKeyword,
		boolean spaceBeforeOpenBrace,
		boolean spaceAroundColonInEnhancedForLoop,
		boolean spaceAroundArrowInLambda,
		boolean noSpaceAroundMethodReference)
	{
		requireThat(ruleId, "ruleId").isNotBlank();
		this.ruleId = ruleId;
		this.spaceAroundBinaryOperator = spaceAroundBinaryOperator;
		this.spaceAroundAssignmentOperators = spaceAroundAssignmentOperators;
		this.spaceAfterComma = spaceAfterComma;
		this.spaceAfterSemicolonInForLoop = spaceAfterSemicolonInForLoop;
		this.spaceAfterControlKeyword = spaceAfterControlKeyword;
		this.spaceBeforeOpenBrace = spaceBeforeOpenBrace;
		this.spaceAroundColonInEnhancedForLoop = spaceAroundColonInEnhancedForLoop;
		this.spaceAroundArrowInLambda = spaceAroundArrowInLambda;
		this.noSpaceAroundMethodReference = noSpaceAroundMethodReference;
	}

	/**
	 * Creates a default whitespace formatting configuration.
	 *
	 * @return a default configuration
	 */
	public static WhitespaceFormattingConfiguration defaultConfig()
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
	 * Returns the rule ID.
	 *
	 * @return the rule ID
	 */
	@Override
	public String ruleId()
	{
		return ruleId;
	}

	/**
	 * Returns whether to add space around binary operators.
	 *
	 * @return {@code true} if space should be added around binary operators
	 */
	public boolean spaceAroundBinaryOperator()
	{
		return spaceAroundBinaryOperator;
	}

	/**
	 * Returns whether to add space around assignment operators.
	 *
	 * @return {@code true} if space should be added around assignment operators
	 */
	public boolean spaceAroundAssignmentOperators()
	{
		return spaceAroundAssignmentOperators;
	}

	/**
	 * Returns whether to add space after commas.
	 *
	 * @return {@code true} if space should be added after commas
	 */
	public boolean spaceAfterComma()
	{
		return spaceAfterComma;
	}

	/**
	 * Returns whether to add space after semicolons in for loops.
	 *
	 * @return {@code true} if space should be added after semicolons in for loops
	 */
	public boolean spaceAfterSemicolonInForLoop()
	{
		return spaceAfterSemicolonInForLoop;
	}

	/**
	 * Returns whether to add space after control keywords.
	 *
	 * @return {@code true} if space should be added after control keywords
	 */
	public boolean spaceAfterControlKeyword()
	{
		return spaceAfterControlKeyword;
	}

	/**
	 * Returns whether to add space before opening braces.
	 *
	 * @return {@code true} if space should be added before opening braces
	 */
	public boolean spaceBeforeOpenBrace()
	{
		return spaceBeforeOpenBrace;
	}

	/**
	 * Returns whether to add space around colons in enhanced for loops.
	 *
	 * @return {@code true} if space should be added around colons in enhanced for loops
	 */
	public boolean spaceAroundColonInEnhancedForLoop()
	{
		return spaceAroundColonInEnhancedForLoop;
	}

	/**
	 * Returns whether to add space around lambda arrows.
	 *
	 * @return {@code true} if space should be added around lambda arrows
	 */
	public boolean spaceAroundArrowInLambda()
	{
		return spaceAroundArrowInLambda;
	}

	/**
	 * Returns whether to remove space around method references.
	 *
	 * @return {@code true} if space should be removed around method references
	 */
	public boolean noSpaceAroundMethodReference()
	{
		return noSpaceAroundMethodReference;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof WhitespaceFormattingConfiguration other))
			return false;
		return spaceAroundBinaryOperator == other.spaceAroundBinaryOperator &&
			spaceAroundAssignmentOperators == other.spaceAroundAssignmentOperators &&
			spaceAfterComma == other.spaceAfterComma &&
			spaceAfterSemicolonInForLoop == other.spaceAfterSemicolonInForLoop &&
			spaceAfterControlKeyword == other.spaceAfterControlKeyword &&
			spaceBeforeOpenBrace == other.spaceBeforeOpenBrace &&
			spaceAroundColonInEnhancedForLoop == other.spaceAroundColonInEnhancedForLoop &&
			spaceAroundArrowInLambda == other.spaceAroundArrowInLambda &&
			noSpaceAroundMethodReference == other.noSpaceAroundMethodReference &&
			Objects.equals(ruleId, other.ruleId);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(ruleId, spaceAroundBinaryOperator, spaceAroundAssignmentOperators,
			spaceAfterComma, spaceAfterSemicolonInForLoop, spaceAfterControlKeyword, spaceBeforeOpenBrace,
			spaceAroundColonInEnhancedForLoop, spaceAroundArrowInLambda, noSpaceAroundMethodReference);
	}

	@Override
	public String toString()
	{
		return "WhitespaceFormattingConfiguration[" +
			"ruleId=" + ruleId +
			", spaceAroundBinaryOperator=" + spaceAroundBinaryOperator +
			", spaceAroundAssignmentOperators=" + spaceAroundAssignmentOperators +
			", spaceAfterComma=" + spaceAfterComma +
			", spaceAfterSemicolonInForLoop=" + spaceAfterSemicolonInForLoop +
			", spaceAfterControlKeyword=" + spaceAfterControlKeyword +
			", spaceBeforeOpenBrace=" + spaceBeforeOpenBrace +
			", spaceAroundColonInEnhancedForLoop=" + spaceAroundColonInEnhancedForLoop +
			", spaceAroundArrowInLambda=" + spaceAroundArrowInLambda +
			", noSpaceAroundMethodReference=" + noSpaceAroundMethodReference +
			']';
	}

	/**
	 * Builder for creating {@code WhitespaceFormattingConfiguration} instances.
	 * <p>
	 * This is the preferred way to create configuration instances, as it provides clear naming for each
	 * parameter and sensible defaults. All fields are initialized to default values matching
	 * {@link #defaultConfig()}.
	 */
	public static final class Builder
	{
		private String ruleId = "whitespace";
		private boolean spaceAroundBinaryOperator = true;
		private boolean spaceAroundAssignmentOperators = true;
		private boolean spaceAfterComma = true;
		private boolean spaceAfterSemicolonInForLoop = true;
		private boolean spaceAfterControlKeyword = true;
		private boolean spaceBeforeOpenBrace = true;
		private boolean spaceAroundColonInEnhancedForLoop = true;
		private boolean spaceAroundArrowInLambda = true;
		private boolean noSpaceAroundMethodReference = true;

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
		 * Sets whether to add space around binary operators.
		 *
		 * @param spaceAroundBinaryOperator {@code true} to add space around binary operators
		 * @return this builder for method chaining
		 */
		public Builder spaceAroundBinaryOperator(boolean spaceAroundBinaryOperator)
		{
			this.spaceAroundBinaryOperator = spaceAroundBinaryOperator;
			return this;
		}

		/**
		 * Sets whether to add space around assignment operators.
		 *
		 * @param spaceAroundAssignmentOperators {@code true} to add space around assignment operators
		 * @return this builder for method chaining
		 */
		public Builder spaceAroundAssignmentOperators(boolean spaceAroundAssignmentOperators)
		{
			this.spaceAroundAssignmentOperators = spaceAroundAssignmentOperators;
			return this;
		}

		/**
		 * Sets whether to add space after commas.
		 *
		 * @param spaceAfterComma {@code true} to add space after commas
		 * @return this builder for method chaining
		 */
		public Builder spaceAfterComma(boolean spaceAfterComma)
		{
			this.spaceAfterComma = spaceAfterComma;
			return this;
		}

		/**
		 * Sets whether to add space after semicolons in for loops.
		 *
		 * @param spaceAfterSemicolonInForLoop {@code true} to add space after semicolons in for loops
		 * @return this builder for method chaining
		 */
		public Builder spaceAfterSemicolonInForLoop(boolean spaceAfterSemicolonInForLoop)
		{
			this.spaceAfterSemicolonInForLoop = spaceAfterSemicolonInForLoop;
			return this;
		}

		/**
		 * Sets whether to add space after control keywords.
		 *
		 * @param spaceAfterControlKeyword {@code true} to add space after control keywords
		 * @return this builder for method chaining
		 */
		public Builder spaceAfterControlKeyword(boolean spaceAfterControlKeyword)
		{
			this.spaceAfterControlKeyword = spaceAfterControlKeyword;
			return this;
		}

		/**
		 * Sets whether to add space before opening braces.
		 *
		 * @param spaceBeforeOpenBrace {@code true} to add space before opening braces
		 * @return this builder for method chaining
		 */
		public Builder spaceBeforeOpenBrace(boolean spaceBeforeOpenBrace)
		{
			this.spaceBeforeOpenBrace = spaceBeforeOpenBrace;
			return this;
		}

		/**
		 * Sets whether to add space around colons in enhanced for loops.
		 *
		 * @param spaceAroundColonInEnhancedForLoop {@code true} to add space around colons in enhanced for loops
		 * @return this builder for method chaining
		 */
		public Builder spaceAroundColonInEnhancedForLoop(boolean spaceAroundColonInEnhancedForLoop)
		{
			this.spaceAroundColonInEnhancedForLoop = spaceAroundColonInEnhancedForLoop;
			return this;
		}

		/**
		 * Sets whether to add space around lambda arrows.
		 *
		 * @param spaceAroundArrowInLambda {@code true} to add space around lambda arrows
		 * @return this builder for method chaining
		 */
		public Builder spaceAroundArrowInLambda(boolean spaceAroundArrowInLambda)
		{
			this.spaceAroundArrowInLambda = spaceAroundArrowInLambda;
			return this;
		}

		/**
		 * Sets whether to remove space around method references.
		 *
		 * @param noSpaceAroundMethodReference {@code true} to remove space around method references
		 * @return this builder for method chaining
		 */
		public Builder noSpaceAroundMethodReference(boolean noSpaceAroundMethodReference)
		{
			this.noSpaceAroundMethodReference = noSpaceAroundMethodReference;
			return this;
		}

		/**
		 * Builds the {@code WhitespaceFormattingConfiguration}.
		 *
		 * @return a new immutable configuration instance
		 */
		public WhitespaceFormattingConfiguration build()
		{
			return new WhitespaceFormattingConfiguration(ruleId, spaceAroundBinaryOperator,
				spaceAroundAssignmentOperators, spaceAfterComma, spaceAfterSemicolonInForLoop,
				spaceAfterControlKeyword, spaceBeforeOpenBrace, spaceAroundColonInEnhancedForLoop,
				spaceAroundArrowInLambda, noSpaceAroundMethodReference);
		}
	}
}
