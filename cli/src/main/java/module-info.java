/**
 * Command-line interface module for the Styler Java Code Formatter.
 * <p>
 * This module provides the main entry point for the CLI application and manages
 * command-line argument parsing, configuration loading, and output formatting.
 * It coordinates with the formatter API to provide a complete user experience.
 */
module io.github.cowwoc.styler.cli
{
	// Required dependencies
	requires io.github.cowwoc.styler.formatter.api;
	requires info.picocli;
	requires com.fasterxml.jackson.dataformat.toml;
	requires com.fasterxml.jackson.databind;
	requires org.slf4j;
	requires ch.qos.logback.classic;

	// For ServiceLoader discovery of formatting rules
	uses io.github.cowwoc.styler.formatter.api.plugin.FormatterPlugin;

	// Export CLI package for picocli access
	exports io.github.cowwoc.styler.cli to info.picocli;

	// Open packages for picocli reflection access
	opens io.github.cowwoc.styler.cli to info.picocli;
	opens io.github.cowwoc.styler.cli.commands to info.picocli;
}