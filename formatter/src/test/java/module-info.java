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
	opens io.github.cowwoc.styler.formatter.test.importorg to org.testng;
	opens io.github.cowwoc.styler.formatter.test.linelength to org.testng;
	opens io.github.cowwoc.styler.formatter.test.brace to org.testng;
	opens io.github.cowwoc.styler.formatter.test.whitespace to org.testng;
	opens io.github.cowwoc.styler.formatter.test.indentation to org.testng;
}
