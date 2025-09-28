package io.github.cowwoc.styler.formatter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Configuration for line length formatting rules.
 * <p>
 * This rule configuration controls how lines are wrapped when they exceed
 * the maximum line length, including special handling for different types
 * of Java constructs like method parameters, array initializers, and chained
 * method calls.
 * <p>
 * <b>Thread Safety:</b> This class is immutable and thread-safe.
 * <b>Security:</b> All configuration values are validated for security compliance.
 *
 * @since 1.0.0
 * @author Plugin Framework Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"maxLineLength", "wrapStrategy", "indentContinuations", "breakBeforeOperators",
	"exceptionsIgnore", "preferBreakingBeforeParameters", "alignParameters",
	"allowLongImports", "allowLongPackageNames"
})
public final class LineLengthRuleConfiguration extends RuleConfiguration
{
	@JsonProperty("maxLineLength")
	private final int maxLineLength;

	@JsonProperty("wrapStrategy")
	private final WrapStrategy wrapStrategy;

	@JsonProperty("indentContinuations")
	private final int indentContinuations;

	@JsonProperty("breakBeforeOperators")
	private final boolean breakBeforeOperators;

	@JsonProperty("exceptionsIgnore")
	private final List<String> exceptionsIgnore;

	@JsonProperty("preferBreakingBeforeParameters")
	private final boolean preferBreakingBeforeParameters;

	@JsonProperty("alignParameters")
	private final boolean alignParameters;

	@JsonProperty("allowLongImports")
	private final boolean allowLongImports;

	@JsonProperty("allowLongPackageNames")
	private final boolean allowLongPackageNames;

	/**
	 * Creates a new line length rule configuration.
	 *
	 * @param maxLineLength               the maximum line length in characters
	 * @param wrapStrategy               the strategy for wrapping long lines
	 * @param indentContinuations        the additional indentation for continuation lines
	 * @param breakBeforeOperators       whether to break before or after operators
	 * @param exceptionsIgnore           list of patterns to ignore for line length checking
	 * @param preferBreakingBeforeParameters whether to prefer breaking before parameter lists
	 * @param alignParameters            whether to align parameters vertically
	 * @param allowLongImports          whether to allow import statements to exceed line length
	 * @param allowLongPackageNames     whether to allow package statements to exceed line length
	 */
	@JsonCreator
	public LineLengthRuleConfiguration(
		@JsonProperty("maxLineLength") Integer maxLineLength,
		@JsonProperty("wrapStrategy") WrapStrategy wrapStrategy,
		@JsonProperty("indentContinuations") Integer indentContinuations,
		@JsonProperty("breakBeforeOperators") Boolean breakBeforeOperators,
		@JsonProperty("exceptionsIgnore") List<String> exceptionsIgnore,
		@JsonProperty("preferBreakingBeforeParameters") Boolean preferBreakingBeforeParameters,
		@JsonProperty("alignParameters") Boolean alignParameters,
		@JsonProperty("allowLongImports") Boolean allowLongImports,
		@JsonProperty("allowLongPackageNames") Boolean allowLongPackageNames)
	{
		this.maxLineLength = maxLineLength != null ? maxLineLength : 120;
		this.wrapStrategy = wrapStrategy != null ? wrapStrategy : WrapStrategy.SMART;
		this.indentContinuations = indentContinuations != null ? indentContinuations : 4;
		this.breakBeforeOperators = breakBeforeOperators != null ? breakBeforeOperators : true;
		this.exceptionsIgnore = exceptionsIgnore != null ? List.copyOf(exceptionsIgnore) : Collections.emptyList();
		this.preferBreakingBeforeParameters = preferBreakingBeforeParameters != null ? preferBreakingBeforeParameters : false;
		this.alignParameters = alignParameters != null ? alignParameters : false;
		this.allowLongImports = allowLongImports != null ? allowLongImports : true;
		this.allowLongPackageNames = allowLongPackageNames != null ? allowLongPackageNames : true;

		try
		{
			validate();
		}
		catch (ConfigurationException e)
		{
			throw new IllegalArgumentException("Invalid line length configuration", e);
		}
	}

	/**
	 * Creates a default line length configuration.
	 */
	public LineLengthRuleConfiguration()
	{
		this(null, null, null, null, null, null, null, null, null);
	}

	/**
	 * Validates this configuration.
	 *
	 * @throws ConfigurationException if the configuration is invalid
	 */
	@Override
	public void validate() throws ConfigurationException
	{
		validateNumericRange("maxLineLength", maxLineLength, 40, 1000);
		validateNumericRange("indentContinuations", indentContinuations, 0, 16);

		// Validate exception patterns
		for (String pattern : exceptionsIgnore)
		{
			validateParameter("exceptionsIgnore pattern", pattern, String.class);
			if (pattern.length() > 200)
			{
				throw new ConfigurationException("Exception pattern exceeds maximum length: " + pattern);
			}
		}
	}

	@Override
	public RuleConfiguration merge(RuleConfiguration override)
	{
		if (!(override instanceof LineLengthRuleConfiguration other))
		{
			throw new IllegalArgumentException("Cannot merge LineLengthRuleConfiguration with " +
			                                 override.getClass().getSimpleName());
		}

		return new LineLengthRuleConfiguration(
			other.maxLineLength != 120 ? other.maxLineLength : this.maxLineLength,
			other.wrapStrategy != WrapStrategy.SMART ? other.wrapStrategy : this.wrapStrategy,
			other.indentContinuations != 4 ? other.indentContinuations : this.indentContinuations,
			other.breakBeforeOperators != true ? other.breakBeforeOperators : this.breakBeforeOperators,
			!other.exceptionsIgnore.isEmpty() ? other.exceptionsIgnore : this.exceptionsIgnore,
			other.preferBreakingBeforeParameters != false ? other.preferBreakingBeforeParameters : this.preferBreakingBeforeParameters,
			other.alignParameters != false ? other.alignParameters : this.alignParameters,
			other.allowLongImports != true ? other.allowLongImports : this.allowLongImports,
			other.allowLongPackageNames != true ? other.allowLongPackageNames : this.allowLongPackageNames
		);
	}

	@Override
	public String getDescription()
	{
		return String.format("Line Length Rule: max=%d, strategy=%s, continuations=%d, breakBeforeOps=%s",
		                     maxLineLength, wrapStrategy, indentContinuations, breakBeforeOperators);
	}

	/**
	 * Checks if a specific type of construct should be ignored for line length checking.
	 *
	 * @param constructType the type of construct to check
	 * @return true if this construct type should be ignored
	 */
	public boolean shouldIgnore(String constructType)
	{
		return exceptionsIgnore.contains(constructType) ||
		       (constructType.equals("import") && allowLongImports) ||
		       (constructType.equals("package") && allowLongPackageNames);
	}

	/**
	 * Returns the effective line length limit considering global configuration.
	 *
	 * @param globalConfig the global configuration, may be {@code null}
	 * @return the effective maximum line length
	 */
	public int getEffectiveMaxLineLength(GlobalConfiguration globalConfig)
	{
		if (globalConfig != null && globalConfig.getMaxLineLength() != 120)
		{
			// Global configuration overrides rule-specific setting
			return globalConfig.getMaxLineLength();
		}
		return maxLineLength;
	}

	public int getMaxLineLength() { return maxLineLength; }
	public WrapStrategy getWrapStrategy() { return wrapStrategy; }
	public int getIndentContinuations() { return indentContinuations; }
	public boolean isBreakBeforeOperators() { return breakBeforeOperators; }
	public List<String> getExceptionsIgnore() { return exceptionsIgnore; }
	public boolean isPreferBreakingBeforeParameters() { return preferBreakingBeforeParameters; }
	public boolean isAlignParameters() { return alignParameters; }
	public boolean isAllowLongImports() { return allowLongImports; }
	public boolean isAllowLongPackageNames() { return allowLongPackageNames; }

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		LineLengthRuleConfiguration that = (LineLengthRuleConfiguration) obj;
		return maxLineLength == that.maxLineLength &&
		       indentContinuations == that.indentContinuations &&
		       breakBeforeOperators == that.breakBeforeOperators &&
		       preferBreakingBeforeParameters == that.preferBreakingBeforeParameters &&
		       alignParameters == that.alignParameters &&
		       allowLongImports == that.allowLongImports &&
		       allowLongPackageNames == that.allowLongPackageNames &&
		       wrapStrategy == that.wrapStrategy &&
		       Objects.equals(exceptionsIgnore, that.exceptionsIgnore);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(maxLineLength, wrapStrategy, indentContinuations, breakBeforeOperators,
		                   exceptionsIgnore, preferBreakingBeforeParameters, alignParameters,
		                   allowLongImports, allowLongPackageNames);
	}

	@Override
	public String toString()
	{
		return "LineLengthRuleConfiguration{" +
		       "maxLineLength=" + maxLineLength +
		       ", wrapStrategy=" + wrapStrategy +
		       ", indentContinuations=" + indentContinuations +
		       ", breakBeforeOperators=" + breakBeforeOperators +
		       ", exceptionsIgnore=" + exceptionsIgnore +
		       ", preferBreakingBeforeParameters=" + preferBreakingBeforeParameters +
		       ", alignParameters=" + alignParameters +
		       ", allowLongImports=" + allowLongImports +
		       ", allowLongPackageNames=" + allowLongPackageNames +
		       '}';
	}

	/**
	 * Enumeration of line wrapping strategies.
	 */
	public enum WrapStrategy
	{
		/** Smart wrapping based on code structure and readability */
		SMART,

		/** Always wrap at the maximum line length */
		HARD_WRAP,

		/** Never wrap lines automatically */
		NO_WRAP,

		/** Wrap only at natural break points (commas, operators, etc.) */
		NATURAL_BREAKS,

		/** Wrap with preference for keeping related elements together */
		SEMANTIC_GROUPING
	}
}