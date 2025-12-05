/**
 * Test module for error catalog.
 */
module io.github.cowwoc.styler.errorcatalog.test
{
	requires io.github.cowwoc.styler.errorcatalog;
	requires org.testng;
	requires io.github.cowwoc.requirements12.java;

	opens io.github.cowwoc.styler.errorcatalog.test to org.testng;
}
