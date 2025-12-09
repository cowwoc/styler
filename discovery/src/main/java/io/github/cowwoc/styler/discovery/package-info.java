/**
 * Recursive Java file discovery with filtering support.
 * <p>
 * This package provides utilities for discovering Java source files in directories with support for
 * glob pattern filtering, .gitignore rules, and security validation.
 * <p>
 * <b>Main API</b>
 * <p>
 * The primary entry point is {@link io.github.cowwoc.styler.discovery.FileDiscovery}, which discovers
 * Java files recursively in given directories. Configuration is provided via
 * {@link io.github.cowwoc.styler.discovery.DiscoveryConfiguration}.
 * <p>
 * <b>Key Types</b>
 * <ul>
 * <li>{@link io.github.cowwoc.styler.discovery.FileDiscovery} - Directory walker with filtering</li>
 * <li>{@link io.github.cowwoc.styler.discovery.FileDiscoveryResult} - Discovery result with files and
 * errors</li>
 * <li>{@link io.github.cowwoc.styler.discovery.DiscoveryConfiguration} - Configuration for discovery
 * operations</li>
 * <li>{@link io.github.cowwoc.styler.discovery.PatternMatcher} - Glob pattern matching interface</li>
 * <li>{@link io.github.cowwoc.styler.discovery.GitignoreParser} - Parser for .gitignore rules</li>
 * </ul>
 * <p>
 * <b>Workflow Pattern</b>
 * <p>
 * Typical usage: Create a FileDiscovery instance with security validators, call discover() with input
 * paths and configuration, check the result for files and errors, and process discovered files.
 * <p>
 * <b>Example Usage</b>
 * <p>
 * <pre>
 * // Create discovery with security validators
 * FileDiscovery discovery = new FileDiscovery(
 *     new PathSanitizer(),
 *     new FileValidator()
 * );
 *
 * // Configure discovery
 * DiscoveryConfiguration config = DiscoveryConfiguration.builder()
 *     .includePatterns(List.of("src/**&#47;*.java"))
 *     .excludePatterns(List.of("**&#47;generated/**"))
 *     .respectGitignore(true)
 *     .build();
 *
 * // Discover files
 * FileDiscoveryResult result = discovery.discover(
 *     List.of(Path.of(".")),
 *     config,
 *     SecurityConfig.DEFAULT
 * );
 *
 * // Process results
 * for (Path file : result.files()) {
 *     // Process discovered file
 * }
 * </pre>
 *
 * @see FileDiscovery
 * @see DiscoveryConfiguration
 * @see PatternMatcher
 */
package io.github.cowwoc.styler.discovery;
