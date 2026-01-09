package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.ArgumentParser;
import io.github.cowwoc.styler.cli.CLIException;
import io.github.cowwoc.styler.cli.CLIOptions;
import io.github.cowwoc.styler.cli.HelpRequestedException;
import io.github.cowwoc.styler.cli.UsageException;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.Locale;

import static io.github.cowwoc.requirements13.java.DefaultJavaValidators.requireThat;

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
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"test.java"};

		CLIOptions options = parser.parse(args);

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
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"File1.java", "File2.java", "src/"};

		CLIOptions options = parser.parse(args);

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
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--check", "test.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.checkMode(), "checkMode").isTrue();
		requireThat(options.fixMode(), "fixMode").isFalse();
	}

	/**
	 * Validates that the {@code --fix} flag enables fix mode and disables check mode.
	 */
	@Test
	public void parseWithFixFlagSetsFixModeTrue() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--fix", "test.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.fixMode(), "fixMode").isTrue();
		requireThat(options.checkMode(), "checkMode").isFalse();
	}

	/**
	 * Validates that the {@code --config} flag sets the configuration file path.
	 */
	@Test
	public void parseWithConfigFlagSetsConfigPath() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--config", "custom.xml", "test.java"};

		CLIOptions options = parser.parse(args);

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
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--config", "config.xml", "--check", "test.java"};

		CLIOptions options = parser.parse(args);

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
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"test.java", "--check"};

		CLIOptions options = parser.parse(args);

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
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--help"};

		parser.parse(args);
	}

	/**
	 * Validates that {@code --version} flag throws {@link HelpRequestedException}.
	 */
	@Test(expectedExceptions = HelpRequestedException.class)
	public void parseWithVersionFlagThrowsHelpRequestedException() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--version"};

		parser.parse(args);
	}

	/**
	 * Validates that empty arguments throw {@link HelpRequestedException}.
	 */
	@Test(expectedExceptions = HelpRequestedException.class)
	public void parseWithEmptyArgsThrowsHelpRequestedException() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {};

		parser.parse(args);
	}

	/**
	 * Validates that {@code --help} exception message contains complete usage text
	 * including usage line, options section, and examples.
	 */
	@Test
	public void parseWithHelpFlagExceptionContainsUsageText() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--help"};

		try
		{
			parser.parse(args);
			throw new AssertionError("Expected HelpRequestedException to be thrown");
		}
		catch (HelpRequestedException e)
		{
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
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--version"};

		try
		{
			parser.parse(args);
			throw new AssertionError("Expected HelpRequestedException to be thrown");
		}
		catch (HelpRequestedException e)
		{
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
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--check", "--fix", "test.java"};

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
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--check"};

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
		ArgumentParser parser = new ArgumentParser();

		parser.parse(null);
	}

	/**
	 * Validates that unrecognized flags throw {@link UsageException}.
	 */
	@Test(expectedExceptions = UsageException.class)
	public void parseWithUnknownFlagThrowsUsageException() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--unknown", "test.java"};

		parser.parse(args);
	}

	/**
	 * Validates that {@code --config} flag without a value throws {@link UsageException}.
	 */
	@Test(expectedExceptions = UsageException.class)
	public void parseWithConfigFlagButNoValueThrowsUsageException() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--config", "test.java"};

		parser.parse(args);
	}

	// ========== Edge Case Tests ==========

	/**
	 * Validates that relative file paths are preserved exactly as provided.
	 */
	@Test
	public void parseWithRelativePathPreservesPath() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"../test.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.inputPaths().getFirst(), "inputPaths.getFirst()").
			isEqualTo(Path.of("../test.java"));
	}

	/**
	 * Validates that absolute file paths are preserved exactly as provided.
	 */
	@Test
	public void parseWithAbsolutePathPreservesPath() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"/home/user/test.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.inputPaths().getFirst(), "inputPaths.getFirst()").
			isEqualTo(Path.of("/home/user/test.java"));
	}

	/**
	 * Validates that file paths with spaces are parsed correctly.
	 */
	@Test
	public void parseWithSpacesInPathParsesCorrectly() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"test file.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.inputPaths().getFirst(), "inputPaths.getFirst()").
			isEqualTo(Path.of("test file.java"));
	}

	/**
	 * Validates that when a flag is provided multiple times, the last value is used.
	 */
	@Test
	public void parseWithMultipleInstancesOfSameFlagUsesLast() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--config", "first.xml", "--config", "second.xml", "test.java"};

		CLIOptions options = parser.parse(args);

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
		ArgumentParser parser = new ArgumentParser();
		String[] args1 = {"--check", "file1.java"};
		String[] args2 = {"--fix", "file2.java"};

		CLIOptions options1 = parser.parse(args1);
		CLIOptions options2 = parser.parse(args2);

		requireThat(options1.checkMode(), "options1.checkMode()").isTrue();
		requireThat(options1.fixMode(), "options1.fixMode()").isFalse();
		requireThat(options1.inputPaths().getFirst(), "options1.inputPaths.getFirst()").
			isEqualTo(Path.of("file1.java"));

		requireThat(options2.checkMode(), "options2.checkMode()").isFalse();
		requireThat(options2.fixMode(), "options2.fixMode()").isTrue();
		requireThat(options2.inputPaths().getFirst(), "options2.inputPaths.getFirst()").
			isEqualTo(Path.of("file2.java"));
	}

	// ========== Max Concurrency Tests ==========

	/**
	 * Validates that the {@code --max-concurrency} flag sets max concurrency value.
	 */
	@Test
	public void parseWithMaxConcurrencySetsValue() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--max-concurrency", "4", "test.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.maxConcurrency().isPresent(), "maxConcurrency.isPresent()").isTrue();
		requireThat(options.maxConcurrency().getAsInt(), "maxConcurrency.value").isEqualTo(4);
	}

	/**
	 * Validates that parsing without {@code --max-concurrency} returns empty optional.
	 */
	@Test
	public void parseWithoutMaxConcurrencyReturnsEmpty() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"test.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.maxConcurrency().isEmpty(), "maxConcurrency.isEmpty()").isTrue();
	}

	/**
	 * Validates that {@code --max-concurrency} with zero value throws UsageException.
	 */
	@Test(expectedExceptions = UsageException.class)
	public void parseWithMaxConcurrencyZeroThrowsUsageException() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--max-concurrency", "0", "test.java"};

		parser.parse(args);
	}

	/**
	 * Validates that {@code --max-concurrency} with negative value throws UsageException.
	 */
	@Test(expectedExceptions = UsageException.class)
	public void parseWithMaxConcurrencyNegativeThrowsUsageException() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--max-concurrency", "-1", "test.java"};

		parser.parse(args);
	}

	// ========== Classpath Tests ==========

	/**
	 * Validates that the {@code --classpath} flag parses single entry correctly.
	 */
	@Test
	public void parseWithClasspathSingleEntry() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--classpath", "lib/foo.jar", "test.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.classpathEntries().size(), "classpathEntries.size()").isEqualTo(1);
		requireThat(options.classpathEntries().getFirst(), "classpathEntries[0]").
			isEqualTo(Path.of("lib/foo.jar"));
	}

	/**
	 * Validates that the {@code -cp} short form parses correctly.
	 */
	@Test
	public void parseWithCpShortForm() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"-cp", "lib/bar.jar", "test.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.classpathEntries().size(), "classpathEntries.size()").isEqualTo(1);
		requireThat(options.classpathEntries().getFirst(), "classpathEntries[0]").
			isEqualTo(Path.of("lib/bar.jar"));
	}

	/**
	 * Validates that multiple classpath entries separated by path separator are parsed correctly.
	 */
	@Test
	public void parseWithClasspathMultipleEntries() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String separator = java.io.File.pathSeparator;
		String[] args = {"--classpath", "lib/a.jar" + separator + "lib/b.jar", "test.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.classpathEntries().size(), "classpathEntries.size()").isEqualTo(2);
		requireThat(options.classpathEntries().get(0), "classpathEntries[0]").
			isEqualTo(Path.of("lib/a.jar"));
		requireThat(options.classpathEntries().get(1), "classpathEntries[1]").
			isEqualTo(Path.of("lib/b.jar"));
	}

	// ========== Module Path Tests ==========

	/**
	 * Validates that the {@code --module-path} flag parses single entry correctly.
	 */
	@Test
	public void parseWithModulePathSingleEntry() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"--module-path", "mods/foo.jar", "test.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.modulepathEntries().size(), "modulepathEntries.size()").isEqualTo(1);
		requireThat(options.modulepathEntries().getFirst(), "modulepathEntries[0]").
			isEqualTo(Path.of("mods/foo.jar"));
	}

	/**
	 * Validates that the {@code -p} short form parses correctly.
	 */
	@Test
	public void parseWithModulePathShortForm() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {"-p", "mods/bar.jar", "test.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.modulepathEntries().size(), "modulepathEntries.size()").isEqualTo(1);
		requireThat(options.modulepathEntries().getFirst(), "modulepathEntries[0]").
			isEqualTo(Path.of("mods/bar.jar"));
	}

	/**
	 * Validates that multiple module path entries separated by path separator are parsed correctly.
	 */
	@Test
	public void parseWithModulePathMultipleEntries() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String separator = java.io.File.pathSeparator;
		String[] args = {"--module-path", "mods/a.jar" + separator + "mods/b.jar", "test.java"};

		CLIOptions options = parser.parse(args);

		requireThat(options.modulepathEntries().size(), "modulepathEntries.size()").isEqualTo(2);
		requireThat(options.modulepathEntries().get(0), "modulepathEntries[0]").
			isEqualTo(Path.of("mods/a.jar"));
		requireThat(options.modulepathEntries().get(1), "modulepathEntries[1]").
			isEqualTo(Path.of("mods/b.jar"));
	}

	/**
	 * Validates that combining all CLI options works correctly.
	 */
	@Test
	public void parseWithAllOptionsSetsCombinedOptions() throws CLIException
	{
		ArgumentParser parser = new ArgumentParser();
		String[] args = {
			"--config", "custom.toml",
			"--check",
			"--classpath", "lib/dep.jar",
			"--module-path", "mods/mod.jar",
			"--max-concurrency", "8",
			"file1.java", "file2.java"
		};

		CLIOptions options = parser.parse(args);

		requireThat(options.configPath().isPresent(), "configPath.isPresent()").isTrue();
		requireThat(options.configPath().get(), "configPath").isEqualTo(Path.of("custom.toml"));
		requireThat(options.checkMode(), "checkMode").isTrue();
		requireThat(options.fixMode(), "fixMode").isFalse();
		requireThat(options.classpathEntries().size(), "classpathEntries.size()").isEqualTo(1);
		requireThat(options.modulepathEntries().size(), "modulepathEntries.size()").isEqualTo(1);
		requireThat(options.maxConcurrency().getAsInt(), "maxConcurrency").isEqualTo(8);
		requireThat(options.inputPaths().size(), "inputPaths.size()").isEqualTo(2);
	}
}
