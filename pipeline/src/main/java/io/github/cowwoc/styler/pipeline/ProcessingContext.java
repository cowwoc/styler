package io.github.cowwoc.styler.pipeline;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Path;
import java.util.List;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.TypeResolutionConfig;
import io.github.cowwoc.styler.pipeline.output.OutputFormat;
import io.github.cowwoc.styler.security.SecurityConfig;

/**
 * Immutable configuration context for processing a single file through the pipeline.
 * <p>
 * This record encapsulates all configuration needed for pipeline execution. Validation occurs in the
 * compact constructor using requireThat() for fail-fast error handling.
 * <p>
 * Example:
 * <pre>
 * ProcessingContext context = ProcessingContext.create(
 *     Paths.get("src/Main.java"),
 *     securityConfig,
 *     List.of(lineLengthConfig, braceConfig),
 *     List.of(new LineLengthFormattingRule()),
 *     false  // not validation-only
 * );
 * </pre>
 *
 * @param filePath the path to the file being processed
 * @param securityConfig the security configuration
 * @param formattingConfigs the list of formatting configurations for all rules
 * @param formattingRules the list of formatting rules to apply (may be empty)
 * @param validationOnly true to only validate without applying fixes
 * @param outputFormatOverride override for output format, or {@code null} for automatic detection
 * @param typeResolutionConfig configuration for type resolution during formatting
 */
public record ProcessingContext(
		Path filePath,
		SecurityConfig securityConfig,
		List<FormattingConfiguration> formattingConfigs,
		List<FormattingRule> formattingRules,
		boolean validationOnly,
		OutputFormat outputFormatOverride,
		TypeResolutionConfig typeResolutionConfig)
{
	/**
	 * Creates a ProcessingContext without output format override (uses automatic detection).
	 *
	 * @param filePath the path to the file being processed
	 * @param securityConfig the security configuration
	 * @param formattingConfigs the list of formatting configurations for all rules
	 * @param formattingRules the list of formatting rules to apply (may be empty)
	 * @param validationOnly true to only validate without applying fixes
	 * @param typeResolutionConfig configuration for type resolution
	 * @return a new ProcessingContext
	 * @throws NullPointerException if any argument is {@code null}
	 */
	public static ProcessingContext create(
			Path filePath,
			SecurityConfig securityConfig,
			List<FormattingConfiguration> formattingConfigs,
			List<FormattingRule> formattingRules,
			boolean validationOnly,
			TypeResolutionConfig typeResolutionConfig)
	{
		return new ProcessingContext(filePath, securityConfig, formattingConfigs, formattingRules,
			validationOnly, null, typeResolutionConfig);
	}

	/**
	 * Creates a ProcessingContext with explicit output format override.
	 *
	 * @param filePath the path to the file being processed
	 * @param securityConfig the security configuration
	 * @param formattingConfigs the list of formatting configurations for all rules
	 * @param formattingRules the list of formatting rules to apply (may be empty)
	 * @param validationOnly true to only validate without applying fixes
	 * @param outputFormatOverride the output format to use
	 * @param typeResolutionConfig configuration for type resolution
	 * @return a new ProcessingContext
	 * @throws NullPointerException if any argument is {@code null}
	 */
	public static ProcessingContext create(
			Path filePath,
			SecurityConfig securityConfig,
			List<FormattingConfiguration> formattingConfigs,
			List<FormattingRule> formattingRules,
			boolean validationOnly,
			OutputFormat outputFormatOverride,
			TypeResolutionConfig typeResolutionConfig)
	{
		requireThat(outputFormatOverride, "outputFormatOverride").isNotNull();
		return new ProcessingContext(filePath, securityConfig, formattingConfigs, formattingRules,
			validationOnly, outputFormatOverride, typeResolutionConfig);
	}

	/**
	 * Canonical constructor with validation of required parameters.
	 */
	public ProcessingContext
	{
		requireThat(filePath, "filePath").isNotNull();
		requireThat(securityConfig, "securityConfig").isNotNull();
		requireThat(formattingConfigs, "formattingConfigs").isNotNull();
		requireThat(formattingRules, "formattingRules").isNotNull();
		requireThat(typeResolutionConfig, "typeResolutionConfig").isNotNull();
		// outputFormatOverride is intentionally nullable - null means automatic detection
	}
}
