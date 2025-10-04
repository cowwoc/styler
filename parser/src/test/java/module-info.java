module io.github.cowwoc.styler.parser.test
{
	requires io.github.cowwoc.styler.parser;
	requires io.github.cowwoc.requirements12.java;
	requires org.testng;
	requires java.management;

	opens io.github.cowwoc.styler.parser.test to org.testng;
	opens io.github.cowwoc.styler.parser.test.strategy to org.testng;
}