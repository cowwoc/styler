package io.github.cowwoc.styler.formatter;

import io.github.cowwoc.styler.security.exceptions.ExecutionTimeoutException;
import java.util.List;

/**
 * Core interface for all formatting rules.
 * <p>
 * <b>Thread-safety</b>: Implementations MUST be thread-safe. Multiple threads may invoke
 * the same rule instance concurrently on different files.
 */
public interface FormattingRule
{
	/**
	 * Returns the unique identifier for this rule.
	 *
	 * @return the rule ID
	 */
	String getId();

	/**
	 * Returns a human-readable name for this rule.
	 *
	 * @return the rule name
	 */
	String getName();

	/**
	 * Returns a description of what this rule checks/fixes.
	 *
	 * @return the rule description
	 */
	String getDescription();

	/**
	 * Returns the default severity for violations from this rule.
	 *
	 * @return the default severity
	 */
	ViolationSeverity getDefaultSeverity();

	/**
	 * Analyzes the source code and returns any violations found.
	 * This method must not modify the AST.
	 *
	 * @param context the transformation context with AST and source access
	 * @param config the rule-specific configuration (may be null for defaults)
	 * @return an empty list if no violations are found
	 * @throws NullPointerException if context is null
	 * @throws ExecutionTimeoutException if the execution deadline from
	 *         {@link TransformationContext#securityConfig() context.securityConfig()} is exceeded
	 */
	List<FormattingViolation> analyze(TransformationContext context,
		FormattingConfiguration config);

	/**
	 * Formats the source code by applying fixes for violations and returns the formatted source code.
	 *
	 * @param context the transformation context with AST and source access
	 * @param config the rule-specific configuration (may be null for defaults)
	 * @return the formatted source code
	 * @throws NullPointerException if context is null
	 * @throws ExecutionTimeoutException if the execution deadline from
	 *         {@link TransformationContext#securityConfig() context.securityConfig()} is exceeded
	 */
	String format(TransformationContext context, FormattingConfiguration config);

	/**
	 * Validates the given configuration for this rule and returns any error messages.
	 *
	 * @param config the configuration to validate
	 * @return an empty list if the configuration is valid
	 * @throws NullPointerException if config is null
	 */
	List<String> validateConfiguration(FormattingConfiguration config);
}
