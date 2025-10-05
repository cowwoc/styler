module io.github.cowwoc.styler.cli.test
{
	requires io.github.cowwoc.styler.cli;
	requires io.github.cowwoc.styler.formatter.api;
	requires io.github.cowwoc.styler.parser;
	requires org.testng;
	requires org.assertj.core;
	requires io.github.cowwoc.requirements12.java;
	requires info.picocli;

	// Open test packages for TestNG reflection access
	opens io.github.cowwoc.styler.cli.test to org.testng;
	opens io.github.cowwoc.styler.cli.test.config to org.testng;
	opens io.github.cowwoc.styler.cli.test.config.exceptions to org.testng;
	opens io.github.cowwoc.styler.cli.test.error to org.testng;
	opens io.github.cowwoc.styler.cli.test.pipeline to org.testng;
	opens io.github.cowwoc.styler.cli.test.pipeline.recovery to org.testng;
	opens io.github.cowwoc.styler.cli.test.pipeline.stages to org.testng;
}
