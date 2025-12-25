module io.github.cowwoc.styler.benchmarks
{
	requires io.github.cowwoc.styler.parser;
	requires io.github.cowwoc.styler.formatter;
	requires io.github.cowwoc.styler.pipeline;
	requires io.github.cowwoc.styler.security;
	requires io.github.cowwoc.styler.ast.core;
	requires io.github.cowwoc.requirements12.java;
	requires java.net.http;
	requires transitive jmh.core;

	exports io.github.cowwoc.styler.benchmarks.parsing;
	exports io.github.cowwoc.styler.benchmarks.memory;
	exports io.github.cowwoc.styler.benchmarks.formatting;
	exports io.github.cowwoc.styler.benchmarks.scalability;
	exports io.github.cowwoc.styler.benchmarks.threading;
	exports io.github.cowwoc.styler.benchmarks.realworld;
	// util package not exported - internal use only, avoids JPMS export warnings
}
