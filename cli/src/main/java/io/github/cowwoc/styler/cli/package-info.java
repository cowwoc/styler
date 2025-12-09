/**
 * Command-line interface for the Styler Java code formatter.
 * <p>
 * This package provides the complete CLI entry point, argument parsing, output formatting,
 * and error reporting for the Styler code formatter. It orchestrates the integration between
 * command-line input and the file processing pipeline.
 * <p>
 * <b>Main API</b>:
 * <p>
 * {@link CLIMain} - The entry point class with main method. Orchestrates the complete workflow:
 * parse arguments, load configuration, build pipeline, process files, and output results.
 * <pre>
 * public class CLIMain
 * {
 *     public static void main(String[] args)
 *     {
 *         new CLIMain().run(args);
 *     }
 * }
 * </pre>
 * <p>
 * {@link OutputHandler} - Formats and displays pipeline results to stdout. Detects output
 * audience (AI or human) and renders in appropriate format:
 * <pre>
 * OutputHandler handler = new OutputHandler();
 * OutputFormat format = handler.detectOutputFormat();  // AI or HUMAN
 * handler.render(results, format);  // Renders to stdout
 * </pre>
 * <p>
 * {@link ErrorReporter} - Reports errors to stderr with appropriate exit codes. Maps exception
 * types to exit codes (HelpRequestedException → 0, UsageException → 2, etc.):
 * <pre>
 * ErrorReporter reporter = new ErrorReporter();
 * reporter.report(error);  // Formats and writes to stderr
 * int exitCode = reporter.getExitCode(error);  // Maps to exit code
 * </pre>
 * <p>
 * {@link CLIOptions} - Immutable container for parsed command-line arguments. Validates
 * mutually exclusive options (--check and --fix):
 * <pre>
 * CLIOptions options = new CLIOptions.Builder().
 *     addInputPath(path1).
 *     setCheckMode(true).
 *     build();
 * </pre>
 * <p>
 * {@link ArgumentParser} - Parses command-line arguments and handles special cases:
 * <pre>
 * CLIOptions options = ArgumentParser.parse(args);  // Throws HelpRequestedException or UsageException
 * </pre>
 * <p>
 * <b>Key Types</b>:
 * <ul>
 *     <li><strong>HelpRequestedException:</strong> Thrown when --help or --version requested (exit 0)</li>
 *     <li><strong>UsageException:</strong> Thrown for invalid arguments (exit 2)</li>
 *     <li><strong>CLIException:</strong> Base class for CLI-specific exceptions</li>
 * </ul>
 * <p>
 * <b>Workflow Pattern</b>:
 * <pre>
 * 1. Parse arguments → CLIOptions (may throw HelpRequestedException or UsageException)
 * 2. Load configuration → Config
 * 3. Build pipeline → FileProcessingPipeline
 * 4. Process files → List&lt;PipelineResult&gt;
 * 5. Output results → render() to stdout
 * 6. Exit with appropriate code based on mode (0 for success, 1 for violations in check mode)
 * </pre>
 * <p>
 * <b>Error Handling</b>:
 * <p>
 * CLI errors are categorized and mapped to exit codes:
 * <ul>
 *     <li>0: Success (no violations) or help/version displayed</li>
 *     <li>1: Violations found in --check mode</li>
 *     <li>2: Invalid arguments or file not found</li>
 *     <li>3: Configuration file errors</li>
 *     <li>4: Security validation failure</li>
 *     <li>5: File I/O errors</li>
 *     <li>127: Unexpected internal error</li>
 * </ul>
 * <p>
 * <b>Example Usage</b>:
 * <pre>
 * // Command-line invocation
 * $ styler --check src/Main.java
 * // → parses args, checks formatting, displays results, exits with 1 if violations found
 *
 * $ styler --fix src/
 * // → parses args, fixes formatting in place, exits with 0
 *
 * $ styler --help
 * // → displays help message, exits with 0 (HelpRequestedException)
 * </pre>
 * <p>
 * <b>Audience Detection</b>:
 * <p>
 * Both {@code OutputHandler} and {@code ErrorReporter} automatically detect the target
 * audience using {@code Audience.detect()}:
 * <ul>
 *     <li>AI audience: Returns JSON format, structured error details</li>
 *     <li>Human audience: Returns human-readable text, formatted tables</li>
 * </ul>
 *
 * @see CLIMain
 * @see OutputHandler
 * @see ErrorReporter
 * @see CLIOptions
 * @see ArgumentParser
 */
package io.github.cowwoc.styler.cli;
