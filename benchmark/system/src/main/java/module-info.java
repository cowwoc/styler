module io.github.cowwoc.styler.benchmark.system
{
	requires jmh.core;
	requires jmh.generator.annprocess;
	requires io.github.cowwoc.styler.cli;
	requires java.base;

	// JMH requires these exports for reflection-based benchmark discovery
	exports io.github.cowwoc.styler.benchmark to jmh.core;
}
