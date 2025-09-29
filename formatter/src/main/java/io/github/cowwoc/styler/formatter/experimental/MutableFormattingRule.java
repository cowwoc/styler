package io.github.cowwoc.styler.formatter.experimental;

import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.ValidationResult;
import java.time.Duration;

/**
 * Defines the interface for formatting rules that directly modify the AST through a mutable context.
 * <p>
 * This interface provides a simplified approach where rules receive a mutable context and directly
 * apply transformations to the AST. Rules execute sequentially, eliminating concurrency concerns
 * and enabling straightforward AST modification patterns.
 * <p>
 * <b>Usage Pattern:</b>
 * <pre>{@code
 * public class LineWrappingRule implements MutableFormattingRule {
 *     public void apply(MutableFormattingContext context) {
 *         // Direct AST modification
 *         context.setWhitespace(node, newWhitespace);
 *         context.replaceChild(parent, oldChild, newChild);
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Execution Model:</b> Rules execute sequentially in priority order, with each rule having
 * complete access to the current AST state including modifications from previous rules.
 */
public interface MutableFormattingRule
{
    /**
     * Returns the unique identifier for this rule.
     * <p>
     * Rule IDs are used for configuration, debugging, and modification tracking.
     * They must be unique across all loaded rules and follow reverse DNS naming
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
     * This timeout provides basic resource protection aligned with the parser
     * security model to prevent accidental infinite loops or excessive processing.
     *
     * @return the maximum execution duration, never null
     */
    default Duration getMaxExecutionTime()
    {
        return Duration.ofSeconds(30);
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
     */
    ValidationResult validate(FormattingContext context);

    /**
     * Applies this formatting rule by directly modifying the AST through the mutable context.
     * <p>
     * This method receives a mutable context that provides direct access to AST modification
     * methods. Rules can immediately apply transformations without generating intermediate
     * text edit objects.
     * <p>
     * <b>Implementation Guidelines:</b>
     * <ul>
     *   <li>Use context modification methods for all AST changes</li>
     *   <li>Handle exceptions gracefully to avoid corrupting formatter pipeline</li>
     *   <li>Validate parameters before making modifications</li>
     *   <li>Keep modifications focused and rule-specific</li>
     * </ul>
     * <p>
     * <b>Resource Requirements:</b>
     * <ul>
     *   <li>Must complete within the specified timeout</li>
     *   <li>Should avoid excessive memory allocation</li>
     *   <li>Must not access external resources (files, network, etc.)</li>
     * </ul>
     *
     * @param context the mutable formatting context for direct AST modification, never null
     * @throws IllegalArgumentException if modification parameters are invalid
     * @throws IllegalStateException if AST is in an inconsistent state
     */
    void apply(MutableFormattingContext context);
}