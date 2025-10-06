module io.github.cowwoc.styler.benchmark.system
{
	requires jmh.core;
	requires jmh.generator.annprocess;
	requires io.github.cowwoc.styler.cli;
	requires io.github.cowwoc.styler.parser;
	requires java.base;

	// JMH requires these exports for reflection-based benchmark discovery
	// Export to test module for test access
	exports io.github.cowwoc.styler.benchmark to jmh.core, io.github.cowwoc.styler.benchmark.system.test;
}
