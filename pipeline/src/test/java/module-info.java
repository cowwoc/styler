/**
 * Tests for Styler Pipeline module.
 */
module io.github.cowwoc.styler.pipeline.test
{
	requires io.github.cowwoc.styler.pipeline;
	requires io.github.cowwoc.styler.security;
	requires io.github.cowwoc.styler.formatter;
	requires io.github.cowwoc.requirements12.java;
	requires org.testng;
	requires ch.qos.logback.classic;

	opens io.github.cowwoc.styler.pipeline.parallel.test to org.testng;
	opens io.github.cowwoc.styler.pipeline.output.test to org.testng;
	opens io.github.cowwoc.styler.pipeline.test to org.testng;
	opens io.github.cowwoc.styler.pipeline.internal.test to org.testng;
}
