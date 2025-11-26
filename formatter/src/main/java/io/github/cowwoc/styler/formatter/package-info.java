/**
 * API contracts for the Styler formatting rules engine.
 * <p>
 * This module defines the core interfaces and data types that formatting rules implement and
 * use to analyze source code for style violations and transform the abstract syntax tree (AST)
 * to apply corrections.
 * <p>
 * <b>Key Types</b>:
 * <ul>
 *     <li><b>{@link FormattingRule}</b> - The primary interface that all formatting rules must
 *         implement. Rules analyze source code, detect violations, and optionally format/transform
 *         the AST.</li>
 *     <li><b>{@link TransformationContext}</b> - Provides rules with access to the source AST,
 *         security configuration, and utility methods for analyzing and transforming code.</li>
 *     <li><b>{@link FormattingViolation}</b> - Immutable representation of a detected style
 *         violation with location, severity, rule ID, message, and suggested fixes.</li>
 *     <li><b>{@link FixStrategy}</b> - Describes a suggested fix for a violation, including
 *         whether it can be automatically applied.</li>
 *     <li><b>{@link ViolationSeverity}</b> - Enum specifying the severity level of violations
 *         (ERROR, WARNING, INFO).</li>
 *     <li><b>{@link FormattingConfiguration}</b> - Base interface for rule-specific configuration
 *         options.</li>
 * </ul>
 * <p>
 * <b>Design Patterns</b>:
 * <ul>
 *     <li><b>Thread-Safety</b>: Implementations of {@code FormattingRule} must be stateless
 *         and thread-safe to allow concurrent analysis of multiple files.</li>
 *     <li><b>Immutability</b>: {@code FormattingViolation} and {@code FixStrategy} are
 *         immutable by design to ensure violation data integrity across the formatting pipeline.</li>
 *     <li><b>Validation</b>: All parameter validation uses the {@code requireThat()} library
 *         from cowwoc/requirements to provide clear error messages and consistent validation
 *         patterns.</li>
 * </ul>
 * <p>
 * <b>Usage Example</b>:
 * <pre>
 * public class MyFormattingRule implements FormattingRule {
 *     {@literal @}Override
 *     public String getId() {
 *         return "my-formatter-rule";
 *     }
 *
 *     {@literal @}Override
 *     public List&lt;FormattingViolation&gt; analyze(TransformationContext context) {
 *         // Analyze AST via context.arena() and context.rootNode()
 *         // Return detected violations
 *     }
 *
 *     {@literal @}Override
 *     public void format(TransformationContext context) {
 *         // Transform the AST to fix violations
 *     }
 * }
 * </pre>
 * <p>
 * <b>Integration</b>: This module is designed to be consumed by the formatting pipeline (Phase B2)
 * which orchestrates rules to analyze and format source files, and by individual formatter
 * implementations (Phase B1) which provide specific formatting rules for indentation, line length,
 * import organization, and other style concerns.
 *
 * @since 1.0
 */
package io.github.cowwoc.styler.formatter;
