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
	requires io.github.cowwoc.styler.parser;
	requires info.picocli;
	requires tools.jackson.dataformat.toml;
	requires tools.jackson.databind;
	requires org.slf4j;
	requires ch.qos.logback.classic;
	requires io.github.cowwoc.requirements12.java;

	// For ServiceLoader discovery of formatting rules
	uses io.github.cowwoc.styler.formatter.api.plugin.FormatterPlugin;

	// Export CLI package for picocli access and test module
	exports io.github.cowwoc.styler.cli to info.picocli, io.github.cowwoc.styler.cli.test;

	// Export security packages publicly
	exports io.github.cowwoc.styler.cli.security;
	exports io.github.cowwoc.styler.cli.security.exceptions;

	// Export internal packages to test module and benchmark module
	exports io.github.cowwoc.styler.cli.commands to io.github.cowwoc.styler.cli.test;
	exports io.github.cowwoc.styler.cli.config to io.github.cowwoc.styler.cli.test;
	exports io.github.cowwoc.styler.cli.config.exceptions to io.github.cowwoc.styler.cli.test;
	exports io.github.cowwoc.styler.cli.error to io.github.cowwoc.styler.cli.test;
	exports io.github.cowwoc.styler.cli.output to io.github.cowwoc.styler.cli.test;
	exports io.github.cowwoc.styler.cli.pipeline to io.github.cowwoc.styler.cli.test, io.github.cowwoc.styler.cli.benchmark;
	exports io.github.cowwoc.styler.cli.pipeline.progress to io.github.cowwoc.styler.cli.test, io.github.cowwoc.styler.cli.benchmark;
	exports io.github.cowwoc.styler.cli.pipeline.recovery to io.github.cowwoc.styler.cli.test;
	exports io.github.cowwoc.styler.cli.pipeline.stages to io.github.cowwoc.styler.cli.test;
	exports io.github.cowwoc.styler.cli.util to io.github.cowwoc.styler.cli.test;
}