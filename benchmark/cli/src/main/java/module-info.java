module io.github.cowwoc.styler.cli.benchmark
{
	requires io.github.cowwoc.styler.cli;
	requires io.github.cowwoc.styler.parser;
	requires jmh.core;
	requires jmh.generator.annprocess;
	requires java.base;

	// JMH requires these exports for reflection-based benchmark discovery
	exports io.github.cowwoc.styler.cli.benchmark to jmh.core;
}
