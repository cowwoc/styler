/**
 * Test module for Styler CLI.
 */
module io.github.cowwoc.styler.cli.test
{
	requires io.github.cowwoc.styler.cli;
	requires io.github.cowwoc.requirements12.java;
	requires org.testng;
	requires ch.qos.logback.classic;

	opens io.github.cowwoc.styler.cli.test to org.testng;
}
