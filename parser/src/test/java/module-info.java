module io.github.styler.parser.test
{
	requires io.github.styler.parser;
	requires io.github.cowwoc.requirements12.java;
	requires org.testng;

	opens io.github.styler.parser.test to org.testng;
}