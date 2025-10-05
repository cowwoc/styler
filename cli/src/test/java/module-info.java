module io.github.cowwoc.styler.cli.test
{
	requires io.github.cowwoc.styler.cli;
	requires io.github.cowwoc.styler.formatter.api;
	requires io.github.cowwoc.styler.parser;
	requires org.testng;
	requires io.github.cowwoc.requirements12.java;

	// CRITICAL: Use 'opens' NOT 'exports' for TestNG reflection access
	opens io.github.cowwoc.styler.cli to org.testng;
	opens io.github.cowwoc.styler.cli.config to org.testng;
	opens io.github.cowwoc.styler.cli.config.exceptions to org.testng;
	opens io.github.cowwoc.styler.cli.error to org.testng;
	opens io.github.cowwoc.styler.cli.pipeline to org.testng;
	opens io.github.cowwoc.styler.cli.pipeline.stages to org.testng;
}
