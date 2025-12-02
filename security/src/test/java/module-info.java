module io.github.cowwoc.styler.security.test
{
	requires io.github.cowwoc.styler.security;
	requires org.testng;
	requires io.github.cowwoc.requirements12.java;
	requires ch.qos.logback.classic;

	opens io.github.cowwoc.styler.security.test to org.testng;
}
