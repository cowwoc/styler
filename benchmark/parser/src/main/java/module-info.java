module io.github.cowwoc.styler.parser.benchmark
{
	requires io.github.cowwoc.styler.parser;
	requires jmh.core;
	requires jmh.generator.annprocess;
	requires java.base;

	// JMH requires these exports for reflection-based benchmark discovery
	exports io.github.cowwoc.styler.parser.benchmark to jmh.core;
}