module io.github.cowwoc.styler.formatter.api.test
{
	requires transitive io.github.cowwoc.styler.formatter.api;
	requires transitive io.github.cowwoc.styler.ast;
	requires org.testng;
	requires org.assertj.core;
	requires io.github.cowwoc.requirements12.java;

	// Export test utilities for other test modules
	exports io.github.cowwoc.styler.formatter.api.test;

	// CRITICAL: Use 'opens' NOT 'exports' for TestNG reflection access
	opens io.github.cowwoc.styler.formatter.api.test to org.testng;
}
