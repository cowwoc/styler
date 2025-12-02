/**
 * Tests for Styler Formatter API.
 */
module io.github.cowwoc.styler.formatter.test
{
	requires io.github.cowwoc.styler.formatter;
	requires io.github.cowwoc.styler.ast.core;
	requires io.github.cowwoc.styler.security;
	requires io.github.cowwoc.requirements12.java;
	requires org.testng;

	opens io.github.cowwoc.styler.formatter.test to org.testng;
	opens io.github.cowwoc.styler.formatter.linelength.test to org.testng;
}
