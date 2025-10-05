/**
 * Structured violation reporting for AI agent feedback and learning.
 * <p>
 * This package provides classes for generating machine-readable violation reports in JSON and XML
 * formats. The reports include priority scores, statistics, and fix suggestions to enable AI agents
 * to learn from formatting feedback and improve code quality over time.
 * </p>
 * <h2>Core Components</h2>
 * <ul>
 * <li>{@link io.github.cowwoc.styler.formatter.api.report.ViolationReport} - Immutable container
 * for violation data, statistics, and metadata</li>
 * <li>{@link io.github.cowwoc.styler.formatter.api.report.ViolationEntry} - Single violation with
 * priority score and fix suggestion</li>
 * <li>{@link io.github.cowwoc.styler.formatter.api.report.PriorityScore} - Priority calculation for
 * violation ordering (ERROR=100, WARNING=10, INFO=1)</li>
 * <li>{@link io.github.cowwoc.styler.formatter.api.report.ViolationStatistics} - Aggregated counts
 * by severity and rule ID</li>
 * <li>{@link io.github.cowwoc.styler.formatter.api.report.ViolationSerializer} - Strategy interface
 * for pluggable serialization formats</li>
 * <li>{@link io.github.cowwoc.styler.formatter.api.report.JsonViolationSerializer} - JSON format
 * serializer using Jackson</li>
 * <li>{@link io.github.cowwoc.styler.formatter.api.report.XmlViolationSerializer} - XML format
 * serializer using Jackson XML</li>
 * <li>{@link io.github.cowwoc.styler.formatter.api.report.ViolationReportGenerator} - Adapter for
 * converting {@link io.github.cowwoc.styler.formatter.api.FormattingViolation} to reports</li>
 * </ul>
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Generate report from formatting result
 * FormattingResult result = formatter.format(sourceCode);
 * ViolationReport report = result.generateReport();
 *
 * // Serialize to JSON
 * ViolationSerializer jsonSerializer = new JsonViolationSerializer();
 * String json = jsonSerializer.serialize(report);
 *
 * // Serialize to XML
 * ViolationSerializer xmlSerializer = new XmlViolationSerializer();
 * String xml = xmlSerializer.serialize(report);
 *
 * // Deserialize and verify round-trip fidelity
 * ViolationReport restored = jsonSerializer.deserialize(json);
 * assert report.equals(restored);
 * }</pre>
 * <h2>Priority Scoring</h2>
 * <p>
 * Violations are assigned priority scores based on severity and frequency:
 * </p>
 * <ul>
 * <li><strong>ERROR</strong>: weight = 100</li>
 * <li><strong>WARNING</strong>: weight = 10</li>
 * <li><strong>INFO</strong>: weight = 1</li>
 * </ul>
 * <p>
 * Priority formula: {@code priority = severity_weight × frequency}
 * </p>
 * <p>
 * This ensures ERROR violations are 10× more important than WARNING, and WARNING violations are
 * 10× more important than INFO. Exact integer arithmetic prevents floating-point errors.
 * </p>
 * <h2>Thread Safety</h2>
 * <p>
 * All classes in this package are immutable and thread-safe. Domain objects use defensive copying
 * with {@link java.util.List#copyOf(java.util.Collection)} and
 * {@link java.util.Map#copyOf(java.util.Map)}. Serializers are stateless and can be safely shared
 * across multiple threads without synchronization.
 * </p>
 * <h2>Serialization Formats</h2>
 * <p>
 * Two serialization formats are supported:
 * </p>
 * <ul>
 * <li><strong>JSON</strong>: Lightweight, widely supported, best for programmatic consumption</li>
 * <li><strong>XML</strong>: Structured, self-describing, best for enterprise integration</li>
 * </ul>
 * <p>
 * Both formats guarantee round-trip fidelity: deserializing a serialized report produces a report
 * equal to the original.
 * </p>
 * <h2>Integration</h2>
 * <p>
 * This package integrates with the existing formatter API through:
 * </p>
 * <ul>
 * <li>{@link io.github.cowwoc.styler.formatter.api.FormattingResult#generateReport()} - Generates
 * structured report from formatting violations</li>
 * <li>{@link io.github.cowwoc.styler.formatter.api.FormattingViolation} - Source of violation data</li>
 * <li>{@link io.github.cowwoc.styler.formatter.api.ViolationSeverity} - Severity levels used for
 * priority calculation</li>
 * </ul>
 * <h2>AI Agent Learning</h2>
 * <p>
 * The structured reports enable AI agents to:
 * </p>
 * <ul>
 * <li>Learn formatting patterns from violation frequency and severity distribution</li>
 * <li>Apply fix suggestions to automatically correct common violations</li>
 * <li>Prioritize learning based on violation priority scores</li>
 * <li>Track improvement over time using statistics aggregation</li>
 * </ul>
 *
 * @see io.github.cowwoc.styler.formatter.api.FormattingResult
 * @see io.github.cowwoc.styler.formatter.api.FormattingViolation
 * @see io.github.cowwoc.styler.formatter.api.ViolationSeverity
 */
package io.github.cowwoc.styler.formatter.api.report;
