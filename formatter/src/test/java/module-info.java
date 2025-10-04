module io.github.cowwoc.styler.formatter.api.test
{
	requires io.github.cowwoc.styler.formatter.api;
	requires io.github.cowwoc.styler.ast;
	requires org.testng;
	requires org.assertj.core;
	requires io.github.cowwoc.requirements12.java;

	// CRITICAL: Use 'opens' NOT 'exports' for TestNG reflection access
	opens io.github.cowwoc.styler.formatter.api.test to org.testng;
}
