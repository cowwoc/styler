/**
 * Styler CLI module providing command-line interface for Java code formatting.
 */
module io.github.cowwoc.styler.cli
{
	requires info.picocli;
	requires io.github.cowwoc.requirements12.java;
	requires io.github.cowwoc.styler.config;
	requires io.github.cowwoc.styler.security;
	requires io.github.cowwoc.styler.formatter;

	requires transitive io.github.cowwoc.styler.pipeline;
	requires transitive io.github.cowwoc.styler.errorcatalog;

	exports io.github.cowwoc.styler.cli;
}
