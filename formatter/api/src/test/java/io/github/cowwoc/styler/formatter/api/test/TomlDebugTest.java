package io.github.cowwoc.styler.formatter.api.test;

import org.testng.annotations.Test;
import io.github.cowwoc.styler.formatter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for TOML parsing functionality.
 */
public class TomlDebugTest
{
	/**
	 * Test simple TOML parsing.
	 */
	@Test
	public void simpleTomlParsing() throws ConfigurationException
	{
		String simpleToml = """
			version = "1.0"
			""";

		ConfigurationSchema config = ConfigurationSchema.fromToml(simpleToml);

		assertThat(config).isNotNull();
		assertThat(config.getVersion()).isEqualTo("1.0");
	}
}