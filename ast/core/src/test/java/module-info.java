module io.github.cowwoc.styler.ast.test
{
	requires io.github.cowwoc.styler.ast.core;
	requires org.slf4j;
	requires org.testng;
	requires io.github.cowwoc.requirements12.java;

	opens io.github.cowwoc.styler.ast.test to org.testng;
}