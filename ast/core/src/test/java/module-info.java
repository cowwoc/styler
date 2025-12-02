/**
 * Tests for Styler AST Core module.
 */
module io.github.cowwoc.styler.ast.core.test
{
	requires io.github.cowwoc.styler.ast.core;
	requires io.github.cowwoc.requirements12.java;
	requires org.testng;
	requires ch.qos.logback.classic;

	opens io.github.cowwoc.styler.ast.core.test to org.testng;
}
