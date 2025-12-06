/**
 * Import organization and cleanup formatting rules.
 * <p>
 * This package provides formatting rules for organizing Java import statements,
 * grouping them by category, sorting alphabetically, and removing unused imports.
 * <p>
 * <b>Key Components</b>:
 * <ul>
 *   <li>{@link ImportOrganizerFormattingRule} - The main FormattingRule implementation</li>
 *   <li>{@link ImportOrganizerConfiguration} - Configuration for import organization rules</li>
 *   <li>{@link ImportGroup} - Standard import groupings (JAVA, JAVAX, THIRD_PARTY, PROJECT)</li>
 *   <li>{@link CustomImportPattern} - User-defined patterns for custom import groups</li>
 * </ul>
 * <p>
 * <b>Usage Example</b>:
 * <pre>{@code
 * FormattingRule rule = new ImportOrganizerFormattingRule();
 * ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();
 * List<FormattingViolation> violations = rule.analyze(context, config);
 * String formatted = rule.format(context, config);
 * }</pre>
 * <p>
 * <b>Import Grouping</b>:
 * By default, imports are organized into four groups in this order:
 * <ol>
 *   <li>Java standard library (java.*)</li>
 *   <li>Java extensions (javax.*)</li>
 *   <li>Third-party libraries</li>
 *   <li>Project-specific imports</li>
 * </ol>
 * <p>
 * Groups are separated by blank lines. Within each group, imports are sorted
 * alphabetically (if configured). Static imports can be separated from regular
 * imports and placed before or after based on configuration.
 * <p>
 * <b>Unused Import Detection</b>:
 * The rule uses a conservative algorithm to detect unused imports:
 * <ul>
 *   <li>Regular imports: Marked unused if their simple name doesn't appear in code</li>
 *   <li>Wildcard imports: Always kept (cannot determine usage without classpath)</li>
 *   <li>Static imports: Marked unused if the imported member name doesn't appear in code</li>
 * </ul>
 * <p>
 * <b>Thread-safety</b>: All public classes are immutable and thread-safe.
 */
package io.github.cowwoc.styler.formatter.importorg;
