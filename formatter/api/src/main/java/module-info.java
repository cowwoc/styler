module io.github.cowwoc.styler.formatter.api
{
	requires transitive io.github.cowwoc.styler.ast.core;
	requires io.github.cowwoc.requirements12.java;
	requires transitive org.slf4j;
	requires com.fasterxml.jackson.annotation;
	requires tools.jackson.databind;
	requires tools.jackson.dataformat.toml;
	requires tools.jackson.dataformat.xml;
	requires java.xml;

	exports io.github.cowwoc.styler.formatter.api;
	exports io.github.cowwoc.styler.formatter.api.conflict;
	exports io.github.cowwoc.styler.formatter.api.plugin;
	exports io.github.cowwoc.styler.formatter.api.report;

	// Open packages to Jackson for serialization/deserialization
	opens io.github.cowwoc.styler.formatter.api to tools.jackson.databind;
	opens io.github.cowwoc.styler.formatter.api.report to tools.jackson.databind;
}
