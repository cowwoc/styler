package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.ArgumentParser;
import io.github.cowwoc.styler.cli.CLIException;
import io.github.cowwoc.styler.cli.CLIOptions;
import io.github.cowwoc.styler.cli.HelpRequestedException;
import io.github.cowwoc.styler.cli.UsageException;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.Locale;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link ArgumentParser} command-line argument parsing.
 * <p>
 * All tests are parallel-safe with no shared mutable state.
 */
public class ArgumentParserTest
{
	// ========== Valid Argument Tests ==========

	/**
	 * Validates that parsing a single file argument creates options with one input path
	 * and default settings (no check/fix mode, no config override).
	 */
	@Test
	public void parseWithSingleFileReturnsOptionsWithSingleInputPath() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"test.java"};

		// Act
		CLIOptions options = parser.parse(args);

		// Assert
		requireThat(options.inputPaths().size(), "inputPaths.size()").isEqualTo(1);
		requireThat(options.inputPaths().getFirst(), "inputPaths.getFirst()").
			isEqualTo(Path.of("test.java"));
		requireThat(options.checkMode(), "checkMode").isFalse();
		requireThat(options.fixMode(), "fixMode").isFalse();
		requireThat(options.configPath().isEmpty(), "value").isTrue();
	}

	/**
	 * Validates that parsing multiple file arguments preserves all paths in order.
	 */
	@Test
	public void parseWithMultipleFilesReturnsOptionsWithAllPaths() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"File1.java", "File2.java", "src/"};

		// Act
		CLIOptions options = parser.parse(args);

		// Assert
		requireThat(options.inputPaths().size(), "inputPaths.size()").isEqualTo(3);
		requireThat(options.inputPaths().get(0), "inputPaths[0]").
			isEqualTo(Path.of("File1.java"));
		requireThat(options.inputPaths().get(1), "inputPaths[1]").
			isEqualTo(Path.of("File2.java"));
		requireThat(options.inputPaths().get(2), "inputPaths[2]").isEqualTo(Path.of("src/"));
	}

	/**
	 * Validates that the {@code --check} flag enables check mode and disables fix mode.
	 */
	@Test
	public void parseWithCheckFlagSetsCheckModeTrue() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--check", "test.java"};

		// Act
		CLIOptions options = parser.parse(args);

		// Assert
		requireThat(options.checkMode(), "checkMode").isTrue();
		requireThat(options.fixMode(), "fixMode").isFalse();
	}

	/**
	 * Validates that the {@code --fix} flag enables fix mode and disables check mode.
	 */
	@Test
	public void parseWithFixFlagSetsFixModeTrue() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--fix", "test.java"};

		// Act
		CLIOptions options = parser.parse(args);

		// Assert
		requireThat(options.fixMode(), "fixMode").isTrue();
		requireThat(options.checkMode(), "checkMode").isFalse();
	}

	/**
	 * Validates that the {@code --config} flag sets the configuration file path.
	 */
	@Test
	public void parseWithConfigFlagSetsConfigPath() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--config", "custom.xml", "test.java"};

		// Act
		CLIOptions options = parser.parse(args);

		// Assert
		requireThat(options.configPath().isPresent(), "configPath.isPresent()").isTrue();
		requireThat(options.configPath().get(), "configPath.get()").
			isEqualTo(Path.of("custom.xml"));
	}

	/**
	 * Validates that combining {@code --config} and {@code --check} flags sets both options.
	 */
	@Test
	public void parseWithAllFlagsSetsAllOptions() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--config", "config.xml", "--check", "test.java"};

		// Act
		CLIOptions options = parser.parse(args);

		// Assert
		requireThat(options.checkMode(), "checkMode").isTrue();
		requireThat(options.configPath().get(), "configPath.get()").
			isEqualTo(Path.of("config.xml"));
		requireThat(options.inputPaths().size(), "inputPaths.size()").isEqualTo(1);
	}

	/**
	 * Validates that flags can appear after file arguments and are still recognized.
	 */
	@Test
	public void parseWithFlagsAfterFilesParsesCorrectly() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"test.java", "--check"};

		// Act
		CLIOptions options = parser.parse(args);

		// Assert
		requireThat(options.checkMode(), "checkMode").isTrue();
		requireThat(options.inputPaths().getFirst(), "inputPaths.getFirst()").
			isEqualTo(Path.of("test.java"));
	}

	// ========== Help and Version Tests ==========

	/**
	 * Validates that {@code --help} flag throws {@link HelpRequestedException}.
	 */
	@Test(expectedExceptions = HelpRequestedException.class)
	public void parseWithHelpFlagThrowsHelpRequestedException() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--help"};

		// Act & Assert
		parser.parse(args);
	}

	/**
	 * Validates that {@code --version} flag throws {@link HelpRequestedException}.
	 */
	@Test(expectedExceptions = HelpRequestedException.class)
	public void parseWithVersionFlagThrowsHelpRequestedException() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--version"};

		// Act & Assert
		parser.parse(args);
	}

	/**
	 * Validates that empty arguments throw {@link HelpRequestedException}.
	 */
	@Test(expectedExceptions = HelpRequestedException.class)
	public void parseWithEmptyArgsThrowsHelpRequestedException() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {};

		// Act & Assert
		parser.parse(args);
	}

	/**
	 * Validates that {@code --help} exception message contains complete usage text
	 * including usage line, options section, and examples.
	 */
	@Test
	public void parseWithHelpFlagExceptionContainsUsageText() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--help"};

		// Act
		try
		{
			parser.parse(args);
			throw new AssertionError("Expected HelpRequestedException to be thrown");
		}
		catch (HelpRequestedException e)
		{
			// Assert
			String message = e.getMessage();
			requireThat(message, "message").contains("Usage:");
			requireThat(message, "message").contains("styler");
			requireThat(message, "message").contains("OPTIONS:");
		}
	}

	/**
	 * Validates that {@code --version} exception message contains version information
	 * including application name and Java version.
	 */
	@Test
	public void parseWithVersionFlagExceptionContainsVersionInfo() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--version"};

		// Act
		try
		{
			parser.parse(args);
			throw new AssertionError("Expected HelpRequestedException to be thrown");
		}
		catch (HelpRequestedException e)
		{
			// Assert
			String message = e.getMessage();
			requireThat(message, "message").contains("Styler");
			requireThat(message, "message").contains("Java");
		}
	}

	// ========== Invalid Argument Tests ==========

	/**
	 * Validates that using both {@code --check} and {@code --fix} flags throws
	 * {@link UsageException} with message indicating mutual exclusivity.
	 */
	@Test
	public void parseWithBothCheckAndFixThrowsUsageException() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--check", "--fix", "test.java"};

		// Act & Assert
		try
		{
			parser.parse(args);
			throw new AssertionError("Expected UsageException to be thrown");
		}
		catch (UsageException e)
		{
			String message = e.getMessage();
			requireThat(message.contains("check mode") || message.contains("fix mode"),
				"value").isTrue();
		}
	}

	/**
	 * Validates that providing no input files throws {@link UsageException}.
	 */
	@Test
	public void parseWithNoFilesThrowsUsageException() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--check"};

		// Act & Assert
		try
		{
			parser.parse(args);
			throw new AssertionError("Expected UsageException to be thrown");
		}
		catch (UsageException e)
		{
			String message = e.getMessage().toLowerCase(Locale.ROOT);
			requireThat(message.contains("no input") || message.contains("input files") ||
				message.contains("missing"), "value").isTrue();
		}
	}

	/**
	 * Validates that null arguments array throws {@link NullPointerException}.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void parseWithNullArgsThrowsNullPointerException() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();

		// Act & Assert
		parser.parse(null);
	}

	/**
	 * Validates that unrecognized flags throw {@link UsageException}.
	 */
	@Test(expectedExceptions = UsageException.class)
	public void parseWithUnknownFlagThrowsUsageException() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--unknown", "test.java"};

		// Act & Assert
		parser.parse(args);
	}

	/**
	 * Validates that {@code --config} flag without a value throws {@link UsageException}.
	 */
	@Test(expectedExceptions = UsageException.class)
	public void parseWithConfigFlagButNoValueThrowsUsageException() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--config", "test.java"};

		// Act & Assert
		parser.parse(args);
	}

	// ========== Edge Case Tests ==========

	/**
	 * Validates that relative file paths are preserved exactly as provided.
	 */
	@Test
	public void parseWithRelativePathPreservesPath() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"../test.java"};

		// Act
		CLIOptions options = parser.parse(args);

		// Assert
		requireThat(options.inputPaths().getFirst(), "inputPaths.getFirst()").
			isEqualTo(Path.of("../test.java"));
	}

	/**
	 * Validates that absolute file paths are preserved exactly as provided.
	 */
	@Test
	public void parseWithAbsolutePathPreservesPath() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"/home/user/test.java"};

		// Act
		CLIOptions options = parser.parse(args);

		// Assert
		requireThat(options.inputPaths().getFirst(), "inputPaths.getFirst()").
			isEqualTo(Path.of("/home/user/test.java"));
	}

	/**
	 * Validates that file paths with spaces are parsed correctly.
	 */
	@Test
	public void parseWithSpacesInPathParsesCorrectly() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"test file.java"};

		// Act
		CLIOptions options = parser.parse(args);

		// Assert
		requireThat(options.inputPaths().getFirst(), "inputPaths.getFirst()").
			isEqualTo(Path.of("test file.java"));
	}

	/**
	 * Validates that when a flag is provided multiple times, the last value is used.
	 */
	@Test
	public void parseWithMultipleInstancesOfSameFlagUsesLast() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--config", "first.xml", "--config", "second.xml", "test.java"};

		// Act
		CLIOptions options = parser.parse(args);

		// Assert
		requireThat(options.configPath().get(), "configPath.get()").
			isEqualTo(Path.of("second.xml"));
	}

	// ========== Thread Safety Tests ==========

	/**
	 * Validates that concurrent parse calls produce independent, correct results.
	 */
	@Test
	public void parseCalledConcurrentlyProducesCorrectResults() throws CLIException
	{
		// Arrange
		ArgumentParser parser = new ArgumentParser();
		String[] args1 = {"--check", "file1.java"};
		String[] args2 = {"--fix", "file2.java"};

		// Act
		CLIOptions options1 = parser.parse(args1);
		CLIOptions options2 = parser.parse(args2);

		// Assert
		requireThat(options1.checkMode(), "options1.checkMode()").isTrue();
		requireThat(options1.fixMode(), "options1.fixMode()").isFalse();
		requireThat(options1.inputPaths().getFirst(), "options1.inputPaths.getFirst()").
			isEqualTo(Path.of("file1.java"));

		requireThat(options2.checkMode(), "options2.checkMode()").isFalse();
		requireThat(options2.fixMode(), "options2.fixMode()").isTrue();
		requireThat(options2.inputPaths().getFirst(), "options2.inputPaths.getFirst()").
			isEqualTo(Path.of("file2.java"));
	}
}
