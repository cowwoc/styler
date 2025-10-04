module io.github.cowwoc.styler.formatter.impl
{
	requires transitive io.github.cowwoc.styler.formatter.api;
	requires io.github.cowwoc.styler.ast;
	requires org.slf4j;
	requires io.github.cowwoc.requirements12.java;

	// Export implementation packages for plugin loading
	exports io.github.cowwoc.styler.formatter.impl;
}
