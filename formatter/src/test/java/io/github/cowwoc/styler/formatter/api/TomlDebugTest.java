package io.github.cowwoc.styler.formatter.api;

import org.testng.annotations.Test;

/**
 * Debug test for TOML parsing issues.
 */
public class TomlDebugTest
{
	@Test
	public void testSimpleTomlParsing() throws Exception
	{
		String simpleToml = """
			version = "1.0"
			""";

		try
		{
			ConfigurationSchema config = ConfigurationSchema.fromToml(simpleToml);
			System.out.println("Successfully parsed: " + config);
		}
		catch (Exception e)
		{
			System.err.println("Failed to parse simple TOML: " + e.getMessage());
			e.printStackTrace();
		}
	}
}