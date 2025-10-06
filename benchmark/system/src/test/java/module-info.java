module io.github.cowwoc.styler.benchmark.system.test
{
	requires io.github.cowwoc.styler.benchmark.system;
	requires io.github.cowwoc.styler.parser;
	requires org.testng;

	opens io.github.cowwoc.styler.benchmark.test to org.testng;
}
