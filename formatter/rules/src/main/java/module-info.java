module io.github.cowwoc.styler.formatter.impl
{
	requires transitive io.github.cowwoc.styler.formatter.api;
	requires io.github.cowwoc.styler.ast;
	requires org.slf4j;
	requires io.github.cowwoc.requirements12.java;

	// Export implementation packages for plugin loading
	exports io.github.cowwoc.styler.formatter.impl;

	// Qualified export for testing - prevents external API exposure
	exports io.github.cowwoc.styler.formatter.impl.wrap to io.github.cowwoc.styler.formatter.impl.test;
}
