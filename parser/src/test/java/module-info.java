/**
 * Tests for Styler Parser module.
 */
module io.github.cowwoc.styler.parser.test
{
	requires io.github.cowwoc.styler.parser;
	requires io.github.cowwoc.styler.ast.core;
	requires io.github.cowwoc.requirements12.java;
	requires org.testng;
	requires ch.qos.logback.classic;

	opens io.github.cowwoc.styler.parser.test to org.testng;
}
