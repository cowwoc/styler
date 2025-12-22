/**
 * Internal implementation classes for the Styler Maven plugin.
 * <p>
 * This package contains internal helper classes that bridge Maven's configuration
 * model to Styler's configuration and result handling. These classes are not part
 * of the public API and may change without notice.
 * <p>
 * <b>Key Classes</b>:
 * <ul>
 *     <li>{@link MavenConfigAdapter} - Adapts Maven plugin parameters to Styler configuration</li>
 *     <li>{@link MavenResultHandler} - Converts pipeline results to Maven log messages</li>
 * </ul>
 */
package io.github.cowwoc.styler.maven.internal;
