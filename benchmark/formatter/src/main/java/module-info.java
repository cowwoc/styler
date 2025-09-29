module io.github.cowwoc.styler.formatter.benchmark
{
	requires io.github.cowwoc.styler.formatter.api;
	requires io.github.cowwoc.styler.ast;
	requires jmh.core;
	requires jmh.generator.annprocess;
	requires java.base;

	// JMH requires these exports for reflection-based benchmark discovery
	exports io.github.cowwoc.styler.formatter.benchmark to jmh.core;
}