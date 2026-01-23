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
	 * Returns examples demonstrating correct and incorrect code for this rule.
	 *
	 * @return a list of examples (may be empty but never null)
	 */
	List<RuleExample> getExamples();

	/**
	 * Returns the configurable properties for this rule with their default values.
	 *
	 * @return a list of properties (may be empty but never null)
	 */
	default List<RuleProperty> getProperties()
	{
		return List.of();
	}

	/**
	 * Analyzes the source code and returns any violations found.
	 * This method must not modify the AST.
	 *
	 * @param context the transformation context with AST and source access
	 * @param configs the list of rule configurations (the rule extracts its specific config type)
	 * @return an empty list if no violations are found
	 * @throws NullPointerException if {@code context} or {@code configs} is {@code null}
	 * @throws ExecutionTimeoutException if the execution deadline from
	 *         {@link TransformationContext#securityConfig() context.securityConfig()} is exceeded
	 */
	List<FormattingViolation> analyze(TransformationContext context,
		List<FormattingConfiguration> configs);

	/**
	 * Formats the source code by applying fixes for violations and returns the formatted source code.
	 *
	 * @param context the transformation context with AST and source access
	 * @param configs the list of rule configurations (the rule extracts its specific config type)
	 * @return the formatted source code
	 * @throws NullPointerException if {@code context} or {@code configs} is {@code null}
	 * @throws ExecutionTimeoutException if the execution deadline from
	 *         {@link TransformationContext#securityConfig() context.securityConfig()} is exceeded
	 */
	String format(TransformationContext context, List<FormattingConfiguration> configs);
}
