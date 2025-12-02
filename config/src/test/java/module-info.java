module io.github.cowwoc.styler.config.test
{
	requires io.github.cowwoc.styler.config;
	requires org.testng;
	requires io.github.cowwoc.requirements12.java;
	requires ch.qos.logback.classic;

	opens io.github.cowwoc.styler.config.test to org.testng;
}
