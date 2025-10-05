module io.github.cowwoc.styler.formatter.api
{
	requires transitive io.github.cowwoc.styler.ast.core;
	requires io.github.cowwoc.requirements12.java;
	requires transitive org.slf4j;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.dataformat.toml;
	requires com.fasterxml.jackson.dataformat.xml;
	requires java.xml;

	exports io.github.cowwoc.styler.formatter.api;
	exports io.github.cowwoc.styler.formatter.api.conflict;
	exports io.github.cowwoc.styler.formatter.api.plugin;
	exports io.github.cowwoc.styler.formatter.api.report;

	// Open packages to Jackson for YAML serialization/deserialization
	opens io.github.cowwoc.styler.formatter.api to com.fasterxml.jackson.databind;
	opens io.github.cowwoc.styler.formatter.api.plugin to com.fasterxml.jackson.databind;
	opens io.github.cowwoc.styler.formatter.api.report to com.fasterxml.jackson.databind;
}