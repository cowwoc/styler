/**
 * Maven plugin for the Styler Java code formatter.
 * <p>
 * This package provides Maven goals for checking and formatting Java source files
 * during the build process. It integrates seamlessly with Maven's lifecycle phases
 * to enable automated code style validation and formatting.
 * <p>
 * <b>Main API</b>:
 * <p>
 * {@link StylerCheckMojo} - Maven goal for validating code formatting without modifications.
 * Bound to the {@code verify} phase by default:
 * <pre>
 * &lt;plugin&gt;
 *     &lt;groupId&gt;io.github.cowwoc.styler&lt;/groupId&gt;
 *     &lt;artifactId&gt;styler-maven-plugin&lt;/artifactId&gt;
 *     &lt;executions&gt;
 *         &lt;execution&gt;
 *             &lt;goals&gt;
 *                 &lt;goal&gt;check&lt;/goal&gt;
 *             &lt;/goals&gt;
 *         &lt;/execution&gt;
 *     &lt;/executions&gt;
 * &lt;/plugin&gt;
 * </pre>
 * <p>
 * {@link StylerFormatMojo} - Maven goal for applying formatting fixes to source files.
 * Bound to the {@code process-sources} phase by default:
 * <pre>
 * &lt;plugin&gt;
 *     &lt;groupId&gt;io.github.cowwoc.styler&lt;/groupId&gt;
 *     &lt;artifactId&gt;styler-maven-plugin&lt;/artifactId&gt;
 *     &lt;executions&gt;
 *         &lt;execution&gt;
 *             &lt;goals&gt;
 *                 &lt;goal&gt;format&lt;/goal&gt;
 *             &lt;/goals&gt;
 *         &lt;/execution&gt;
 *     &lt;/executions&gt;
 * &lt;/plugin&gt;
 * </pre>
 * <p>
 * {@link AbstractStylerMojo} - Base class providing shared configuration and pipeline building.
 * Common parameters:
 * <ul>
 *     <li>{@code configFile} - Path to Styler configuration file (optional)</li>
 *     <li>{@code sourceDirectories} - Source directories to process</li>
 *     <li>{@code testSourceDirectories} - Test source directories to process</li>
 *     <li>{@code includes} - File patterns to include (default: **\/*.java)</li>
 *     <li>{@code excludes} - File patterns to exclude</li>
 *     <li>{@code failOnViolation} - Fail build on violations (default: true)</li>
 *     <li>{@code skip} - Skip plugin execution (default: false)</li>
 *     <li>{@code encoding} - File encoding (default: UTF-8)</li>
 * </ul>
 * <p>
 * <b>Configuration Discovery</b>:
 * <p>
 * If no explicit configuration file is specified, the plugin searches for
 * {@code styler.toml} starting from the project's base directory upward
 * through parent directories.
 * <p>
 * <b>Pipeline Integration</b>:
 * <p>
 * The plugin uses the same {@link io.github.cowwoc.styler.pipeline.FileProcessingPipeline}
 * as the CLI, ensuring consistent behavior across all Styler interfaces.
 *
 * @see StylerCheckMojo
 * @see StylerFormatMojo
 * @see AbstractStylerMojo
 */
package io.github.cowwoc.styler.maven;
