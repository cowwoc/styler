package io.github.cowwoc.styler.cli.config.exceptions;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for configuration discovery exception hierarchy.
 * Tests exception creation, message formatting, and business context preservation.
 */
@SuppressWarnings("PMD.MethodNamingConventions") // Test methods use descriptive_scenario_outcome pattern
public class ConfigExceptionsTest
{
	/**
	 * Verifies that ConfigDiscoveryException preserves error message.
	 */
	@Test
	public void configDiscoveryException_withMessage_preservesMessage()
	{
		// Given: error message
		String message = "Configuration discovery failed";

		// When: creating exception
		ConfigDiscoveryException exception = new ConfigDiscoveryException(message);

		// Then: message is preserved
		assertThat(exception.getMessage()).isEqualTo(message);
		assertThat(exception.getCause()).isNull();
	}

	/**
	 * Verifies that ConfigDiscoveryException preserves both message and cause.
	 */
	@Test
	public void configDiscoveryException_withMessageAndCause_preservesBoth()
	{
		// Given: error message and cause
		String message = "Configuration discovery failed";
		Throwable cause = new RuntimeException("Underlying error");

		// When: creating exception
		ConfigDiscoveryException exception = new ConfigDiscoveryException(message, cause);

		// Then: both message and cause are preserved
		assertThat(exception.getMessage()).isEqualTo(message);
		assertThat(exception.getCause()).isEqualTo(cause);
	}

	/**
	 * Verifies that null message throws NullPointerException.
	 */
	@Test
	public void configDiscoveryException_withNullMessage_throwsNullPointerException()
	{
		// When/Then: null message throws exception
		assertThatThrownBy(() -> new ConfigDiscoveryException(null)).
			isInstanceOf(NullPointerException.class);
	}

	/**
	 * Verifies that ConfigNotFoundException builds descriptive message with all searched paths.
	 */
	@Test
	public void configNotFoundException_withSearchedPaths_buildsDescriptiveMessage()
	{
		// Given: list of searched paths
		List<Path> searchedPaths = List.of(
			Paths.get("/project/.styler.toml"),
			Paths.get("/home/user/.config/styler/.styler.toml"),
			Paths.get("/etc/styler/.styler.toml"));

		// When: creating exception
		ConfigNotFoundException exception = new ConfigNotFoundException(searchedPaths);

		// Then: message includes all searched paths and helpful guidance
		String message = exception.getMessage();
		assertThat(message).contains("Configuration files (.styler.toml, .styler.yaml) not found");
		assertThat(message).contains("/project/.styler.toml");
		assertThat(message).contains("/home/user/.config/styler/.styler.toml");
		assertThat(message).contains("/etc/styler/.styler.toml");
		assertThat(message).contains("create a .styler.toml file");
		assertThat(message).contains("--config");

		// And: searched paths are accessible and immutable
		assertThat(exception.getSearchedPaths()).isEqualTo(searchedPaths);
		// Note: List.copyOf may return same instance if input is already immutable (optimization)
	}

	/**
	 * Verifies that ConfigNotFoundException handles empty search paths appropriately.
	 */
	@Test
	public void configNotFoundException_withEmptySearchPaths_buildsAppropriateMessage()
	{
		// Given: empty list of searched paths
		List<Path> searchedPaths = List.of();

		// When: creating exception
		ConfigNotFoundException exception = new ConfigNotFoundException(searchedPaths);

		// Then: message indicates no search paths were provided
		String message = exception.getMessage();
		assertThat(message).contains("No configuration files found - no search paths were provided");
		assertThat(exception.getSearchedPaths()).isEmpty();
	}

	/**
	 * Verifies that null searched paths throws NullPointerException.
	 */
	@Test
	public void configNotFoundException_withNullSearchedPaths_throwsNullPointerException()
	{
		// When/Then: null searched paths throws exception
		assertThatThrownBy(() -> new ConfigNotFoundException(null)).
			isInstanceOf(NullPointerException.class);
	}

	/**
	 * Verifies that ConfigValidationException builds descriptive message with file path and validation details.
	 */
	@Test
	public void configValidationException_withConfigFileAndMessage_buildsDescriptiveMessage()
	{
		// Given: config file and validation message
		Path configFile = Paths.get("/project/.styler.toml");
		String validationMessage = "Invalid TOML syntax at line 5";

		// When: creating exception
		ConfigValidationException exception = new ConfigValidationException(configFile, validationMessage);

		// Then: message includes file path and validation details
		String message = exception.getMessage();
		assertThat(message).contains("Configuration file validation failed");
		assertThat(message).contains("/project/.styler.toml");
		assertThat(message).contains("Invalid TOML syntax at line 5");
		assertThat(message).contains("valid TOML or YAML");

		// And: config file is accessible
		assertThat(exception.getConfigFile()).isEqualTo(configFile);
		assertThat(exception.getCause()).isNull();
	}

	/**
	 * Verifies that ConfigValidationException preserves config file, message, and cause.
	 */
	@Test
	public void configValidationException_withConfigFileMessageAndCause_preservesAllDetails()
	{
		// Given: config file, validation message, and cause
		Path configFile = Paths.get("/project/.styler.toml");
		String validationMessage = "Parsing failed";
		Throwable cause = new IllegalArgumentException("Invalid character");

		// When: creating exception
		ConfigValidationException exception = new ConfigValidationException(configFile, validationMessage, cause);

		// Then: all details are preserved
		String message = exception.getMessage();
		assertThat(message).contains("Configuration file validation failed");
		assertThat(message).contains("/project/.styler.toml");
		assertThat(message).contains("Parsing failed");
		assertThat(exception.getConfigFile()).isEqualTo(configFile);
		assertThat(exception.getCause()).isEqualTo(cause);
	}

	/**
	 * Verifies that null config file throws NullPointerException.
	 */
	@Test
	public void configValidationException_withNullConfigFile_throwsNullPointerException()
	{
		// When/Then: null config file throws exception
		assertThatThrownBy(() -> new ConfigValidationException(null, "message")).
			isInstanceOf(NullPointerException.class);
	}

	/**
	 * Verifies that null validation message throws NullPointerException.
	 */
	@Test
	public void configValidationException_withNullMessage_throwsNullPointerException()
	{
		// Given: valid config file
		Path configFile = Paths.get("/project/.styler.toml");

		// When/Then: null message throws exception
		assertThatThrownBy(() -> new ConfigValidationException(configFile, null)).
			isInstanceOf(NullPointerException.class);
	}

	/**
	 * Verifies that FileAccessException builds descriptive message with file path and access details.
	 */
	@Test
	public void fileAccessException_withConfigFileAndMessage_buildsDescriptiveMessage()
	{
		// Given: config file and access message
		Path configFile = Paths.get("/project/.styler.toml");
		String accessMessage = "Permission denied";

		// When: creating exception
		FileAccessException exception = new FileAccessException(configFile, accessMessage);

		// Then: message includes file path and access details
		String message = exception.getMessage();
		assertThat(message).contains("Cannot access configuration file");
		assertThat(message).contains("/project/.styler.toml");
		assertThat(message).contains("Permission denied");
		assertThat(message).contains("check file permissions");

		// And: config file is accessible
		assertThat(exception.getConfigFile()).isEqualTo(configFile);
		assertThat(exception.getCause()).isNull();
	}

	/**
	 * Verifies that FileAccessException preserves config file, message, and cause.
	 */
	@Test
	public void fileAccessException_withConfigFileMessageAndCause_preservesAllDetails()
	{
		// Given: config file, access message, and cause
		Path configFile = Paths.get("/project/.styler.toml");
		String accessMessage = "Access denied";
		Throwable cause = new SecurityException("Insufficient permissions");

		// When: creating exception
		FileAccessException exception = new FileAccessException(configFile, accessMessage, cause);

		// Then: all details are preserved
		String message = exception.getMessage();
		assertThat(message).contains("Cannot access configuration file");
		assertThat(message).contains("/project/.styler.toml");
		assertThat(message).contains("Access denied");
		assertThat(exception.getConfigFile()).isEqualTo(configFile);
		assertThat(exception.getCause()).isEqualTo(cause);
	}

	/**
	 * Verifies that null config file throws NullPointerException for FileAccessException.
	 */
	@Test
	public void fileAccessException_withNullConfigFile_throwsNullPointerException()
	{
		// When/Then: null config file throws exception
		assertThatThrownBy(() -> new FileAccessException(null, "message")).
			isInstanceOf(NullPointerException.class);
	}

	/**
	 * Verifies that null access message throws NullPointerException for FileAccessException.
	 */
	@Test
	public void fileAccessException_withNullMessage_throwsNullPointerException()
	{
		// Given: valid config file
		Path configFile = Paths.get("/project/.styler.toml");

		// When/Then: null message throws exception
		assertThatThrownBy(() -> new FileAccessException(configFile, null)).
			isInstanceOf(NullPointerException.class);
	}
}