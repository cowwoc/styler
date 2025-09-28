package io.github.cowwoc.styler.formatter.api;

import io.github.cowwoc.styler.ast.ASTNode;

import java.time.Duration;

/**
 * Defines the interface for formatting rules that can be applied to Java source code.
 * <p>
 * All formatting rules must be stateless and thread-safe to enable concurrent processing.
 * Rules receive immutable context objects and return transformation instructions without
 * modifying the AST directly.
 * <p>
 * <b>Security Note:</b> Rules are executed in a secured environment with resource limits
 * and restricted permissions to prevent malicious behavior.
 */
public interface FormattingRule
{
	/**
	 * Returns the unique identifier for this rule.
	 * <p>
	 * Rule IDs are used for configuration, conflict resolution, and audit logging.
	 * They must be unique across all loaded plugins and follow reverse DNS naming
	 * convention (e.g., "com.example.rules.LineLength").
	 *
	 * @return the rule identifier, never null or empty
	 */
	String getRuleId();

	/**
	 * Returns the execution priority for this rule.
	 * <p>
	 * Rules with higher priority values are applied later in the formatting pipeline.
	 * This enables dependent rules to build upon the results of foundational rules.
	 * Priority values should be between 0 (earliest) and 1000 (latest).
	 *
	 * @return the priority value for rule execution ordering
	 */
	int getPriority();

	/**
	 * Returns the maximum execution time allowed for this rule.
	 * <p>
	 * This timeout is enforced by the security framework to prevent resource
	 * exhaustion attacks. Rules that exceed this timeout will be terminated
	 * and marked as failed.
	 *
	 * @return the maximum execution duration, never null
	 */
	default Duration getMaxExecutionTime()
	{
		return Duration.ofSeconds(5);
	}

	/**
	 * Returns the maximum memory allocation allowed for this rule in bytes.
	 * <p>
	 * This limit is enforced to prevent memory exhaustion attacks from
	 * malicious or poorly implemented rules.
	 *
	 * @return the maximum memory allocation in bytes
	 */
	default long getMaxMemoryUsage()
	{
		return 100 * 1024 * 1024; // 100MB default limit
	}

	/**
	 * Returns the default configuration for this rule.
	 * <p>
	 * The default configuration provides sensible defaults for all rule parameters
	 * and serves as the base for user customizations.
	 *
	 * @return the default rule configuration, never null
	 */
	RuleConfiguration getDefaultConfiguration();

	/**
	 * Validates whether this rule can be applied to the given context.
	 * <p>
	 * This method performs preliminary checks without modifying anything.
	 * It verifies that the AST structure, file type, and configuration are
	 * compatible with this rule's requirements.
	 *
	 * @param context the formatting context to validate, never null
	 * @return the validation result indicating success or failure reasons
	 * @throws SecurityException if the rule attempts unauthorized operations
	 */
	ValidationResult validate(FormattingContext context);

	/**
	 * Applies this formatting rule to the given context.
	 * <p>
	 * This method examines the AST and returns a list of text edits that
	 * should be applied to achieve the desired formatting. The method must
	 * not modify the AST or any other shared state.
	 * <p>
	 * <b>Security Requirements:</b>
	 * <ul>
	 *   <li>Must not access filesystem, network, or system properties</li>
	 *   <li>Must not create threads or use reflection</li>
	 *   <li>Must complete within the specified timeout</li>
	 *   <li>Must not exceed memory allocation limits</li>
	 * </ul>
	 *
	 * @param context the formatting context containing AST and metadata, never null
	 * @return the formatting result with text edits and diagnostics, never null
	 * @throws SecurityException if the rule attempts unauthorized operations
	 */
	FormattingResult apply(FormattingContext context);
}