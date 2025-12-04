package io.github.cowwoc.styler.pipeline;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Path;
import java.util.List;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.security.SecurityConfig;

/**
 * Immutable configuration context for processing a single file through the pipeline.
 * <p>
 * This record encapsulates all configuration needed for pipeline execution. Validation occurs in the
 * compact constructor using requireThat() for fail-fast error handling.
 * <p>
 * Example:
 * <pre>
 * ProcessingContext context = new ProcessingContext(
 *     Paths.get("src/Main.java"),
 *     securityConfig,
 *     formattingConfig,
 *     List.of(new LineLengthFormattingRule()),
 *     false  // not validation-only
 * );
 * </pre>
 *
 * @param filePath the path to the file being processed
 * @param securityConfig the security configuration
 * @param formattingConfig the formatting configuration
 * @param formattingRules the list of formatting rules to apply (may be empty)
 * @param validationOnly true to only validate without applying fixes
 */
public record ProcessingContext(
		Path filePath,
		SecurityConfig securityConfig,
		FormattingConfiguration formattingConfig,
		List<FormattingRule> formattingRules,
		boolean validationOnly)
{
	/**
	 * Creates a ProcessingContext with validation of all parameters.
	 *
	 * @param filePath the path to the file being processed
	 * @param securityConfig the security configuration
	 * @param formattingConfig the formatting configuration
	 * @param formattingRules the list of formatting rules to apply (may be empty)
	 * @param validationOnly true to only validate without applying fixes
	 * @throws NullPointerException if any argument is null
	 */
	public ProcessingContext
	{
		requireThat(filePath, "filePath").isNotNull();
		requireThat(securityConfig, "securityConfig").isNotNull();
		requireThat(formattingConfig, "formattingConfig").isNotNull();
		requireThat(formattingRules, "formattingRules").isNotNull();
	}
}
