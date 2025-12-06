/**
 * Violation output formatting and rendering for AI agents and human users.
 *
 * This package provides a flexible output system for formatting violations in both
 * machine-readable (JSON) and human-readable formats. Key components include:
 *
 * <ul>
 *   <li><b>OutputFormat</b>: Enumeration of supported output formats (JSON, HUMAN)</li>
 *   <li><b>OutputConfiguration</b>: Immutable configuration for output preferences</li>
 *   <li><b>ViolationReport</b>: Aggregated violation data with grouping methods</li>
 *   <li><b>PriorityCalculator</b>: Interface for computing violation counts by rule</li>
 *   <li><b>DefaultPriorityCalculator</b>: Default implementation counting violations per rule</li>
 *   <li><b>ContextDetector</b>: Interface for detecting AI vs human execution context</li>
 *   <li><b>DefaultContextDetector</b>: Default implementation with environment detection</li>
 *   <li><b>ViolationReportRenderer</b>: Interface for rendering formatted output</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * ContextDetector detector = new DefaultContextDetector();
 * OutputFormat format = detector.detectContext();
 *
 * PriorityCalculator calculator = new DefaultPriorityCalculator();
 * ViolationReport report = new ViolationReport(
 *     filePath,
 *     violations,
 *     calculator.countByRule(violations)
 * );
 *
 * ViolationReportRenderer renderer = ViolationReportRenderer.create(format);
 * String output = renderer.render(report);
 * </pre>
 *
 * <h2>Thread-Safety</h2>
 * All classes in this package are immutable and thread-safe.
 */
package io.github.cowwoc.styler.pipeline.output;
