package io.github.cowwoc.styler.config.test;

import io.github.cowwoc.styler.config.Config;
import io.github.cowwoc.styler.config.ConfigurationLoader;
import io.github.cowwoc.styler.config.exception.ConfigurationException;
import io.github.cowwoc.styler.config.exception.ConfigurationValidationException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.config.test.TestFileSystemUtils.deleteDirectory;
import static org.testng.Assert.expectThrows;

/**
 * Tests for {@link ConfigurationLoader} focusing on integration scenarios.
 * <p>
 * Priority 1: Business logic tests validate proper file loading, validation, and error reporting.
 */
public final class ConfigurationLoaderTest
{
	private final ConfigurationLoader loader = new ConfigurationLoader();

	/**
	 * RISK: Valid config file doesn't load successfully.
	 * <p>
	 * IMPACT: Critical - core happy path must work.
	 */
	@Test
	public void load_validConfigFile_succeeds() throws IOException, ConfigurationException
	{
		Path tempDir = Files.createTempDirectory("styler-test");
		try
		{
			Path configFile = tempDir.resolve(".styler.toml");
			Files.writeString(configFile, "maxLineLength = 100");

			Config config = loader.load(tempDir);

			requireThat(config.maxLineLength(), "maxLineLength").isEqualTo(100);
		}
		finally
		{
			deleteDirectory(tempDir);
		}
	}

	/**
	 * RISK: Invalid value doesn't include file path in error message
	 * IMPACT: High - users need to know which file contains the invalid value
	 * <p>
	 * Note: Line number tracking is not currently supported by Jackson TOML parser,
	 * but the file path must be included in error messages.
	 */
	@Test
	public void load_invalidValue_includesFilePathInError() throws IOException
	{
		Path tempDir = Files.createTempDirectory("styler-test");
		try
		{
			Path configFile = tempDir.resolve(".styler.toml");

			// Create TOML with invalid value
			String toml = "# This is a comment\n" +
				"# maxLineLength should be positive\n" +
				"maxLineLength = -10\n";
			Files.writeString(configFile, toml);

			ConfigurationValidationException exception = expectThrows(
				ConfigurationValidationException.class,
				() -> loader.load(tempDir)
			);

			// Error message must include file path and validation error
			requireThat(exception.getMessage(), "exception.getMessage()")
				.contains(configFile.toString());
			requireThat(exception.getMessage(), "exception.getMessage()")
				.contains("must be positive");
		}
		finally
		{
			deleteDirectory(tempDir);
		}
	}

	/**
	 * RISK: No config files found throws exception.
	 * <p>
	 * IMPACT: Medium - should return default configuration.
	 */
	@Test
	public void load_noConfigFiles_usesDefaults() throws IOException, ConfigurationException
	{
		Path tempDir = Files.createTempDirectory("styler-test");
		try
		{
			// No config file created
			Config config = loader.load(tempDir);

			requireThat(config.maxLineLength(), "maxLineLength")
				.isEqualTo(Config.DEFAULT_MAX_LINE_LENGTH);
		}
		finally
		{
			deleteDirectory(tempDir);
		}
	}

	/**
	 * RISK: Empty config file doesn't use defaults.
	 * <p>
	 * IMPACT: Medium - empty files are valid and should use system defaults.
	 */
	@Test
	public void load_emptyConfigFile_usesDefaults() throws IOException, ConfigurationException
	{
		Path tempDir = Files.createTempDirectory("styler-test");
		try
		{
			Path configFile = tempDir.resolve(".styler.toml");
			Files.writeString(configFile, "");

			Config config = loader.load(tempDir);

			requireThat(config.maxLineLength(), "maxLineLength")
				.isEqualTo(Config.DEFAULT_MAX_LINE_LENGTH);
		}
		finally
		{
			deleteDirectory(tempDir);
		}
	}
}
