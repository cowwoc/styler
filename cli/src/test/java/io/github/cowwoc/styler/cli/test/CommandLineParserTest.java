package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.CommandLineParser;
import io.github.cowwoc.styler.cli.ParsedArguments;
import io.github.cowwoc.styler.cli.ArgumentParsingException;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for CommandLineParser.
 * <p>
 * Tests cover argument parsing, validation, error handling, and edge cases
 * to ensure the parser correctly handles all supported CLI patterns.
 * <p>
 * Thread-safe: All tests use method-local variables and UUID-based temp directories.
 */
public class CommandLineParserTest
{
	/**
	 * Helper class to hold test directory structure for tests that need temp files.
	 */
	private static class TestDirectory
	{
		final Path tempDir;
		final Path testFile1;
		final Path testFile2;

		TestDirectory() throws IOException
		{
			// Use UUID for parallel test isolation
			this.tempDir = Files.createTempDirectory("parser-test-" + UUID.randomUUID());
			Path srcMain = tempDir.resolve("src/main/java");
			Path srcTest = tempDir.resolve("src/test/java");
			Files.createDirectories(srcMain);
			Files.createDirectories(srcTest);

			this.testFile1 = srcMain.resolve("Test.java");
			this.testFile2 = srcTest.resolve("TestTest.java");
			Files.writeString(testFile1, "public class Test {}");
			Files.writeString(testFile2, "public class TestTest {}");
		}

		void cleanup() throws IOException
		{
			if (tempDir != null && Files.exists(tempDir))
			{
				// Delete test files and directories
				Files.walk(tempDir).
					// Reverse order for directory deletion
					sorted((a, b) -> -a.compareTo(b)).
					forEach(path ->
					{
						try
						{
							Files.delete(path);
						}
						catch (IOException e)
						{
							// Ignore cleanup errors
							@SuppressWarnings("unused")
							String ignored = e.getMessage();
						}
					});
			}
		}
	}

	/**
	 * Verifies that the parser correctly handles a basic format command with a single input file.
	 */
	@Test
	public void parseFormatCommand() throws ArgumentParsingException, IOException
	{
		CommandLineParser parser = new CommandLineParser();
		TestDirectory testDir = new TestDirectory();
		try
		{
			String[] args = {"format", testDir.testFile1.toString()};
			ParsedArguments result = parser.parse(args);

			assertThat(result.command()).isEqualTo(ParsedArguments.Command.FORMAT);
			assertThat(result.inputFiles()).hasSize(1);
			assertThat(result.inputFiles().get(0).getFileName()).isEqualTo(testDir.testFile1.getFileName());
			assertThat(result.verbose()).isFalse();
			assertThat(result.quiet()).isFalse();
			assertThat(result.dryRun()).isFalse();
			assertThat(result.configPath()).isEmpty();
			assertThat(result.outputDirectory()).isEmpty();
		}
		finally
		{
			testDir.cleanup();
		}
	}

	/**
	 * Verifies that the parser correctly handles a format command with multiple options and input files.
	 */
	@Test
	public void parseFormatCommandWithOptions() throws ArgumentParsingException, IOException
	{
		CommandLineParser parser = new CommandLineParser();
		TestDirectory testDir = new TestDirectory();
		try
		{
			// Create config and output directory
			Path customConfig = testDir.tempDir.resolve("custom.toml");
			Path outputDir = testDir.tempDir.resolve("target/formatted");
			Files.writeString(customConfig, "# test config");
			Files.createDirectories(outputDir);

			String[] args = {
				"--verbose",
				"format",
				"--config", customConfig.toString(),
				"--dry-run",
				"--output", outputDir.toString(),
				testDir.testFile1.toString(),
				testDir.testFile2.toString()
			};
			ParsedArguments result = parser.parse(args);

			assertThat(result.command()).isEqualTo(ParsedArguments.Command.FORMAT);
			assertThat(result.inputFiles()).hasSize(2);
			assertThat(result.inputFiles().get(0).getFileName()).isEqualTo(testDir.testFile1.getFileName());
			assertThat(result.inputFiles().get(1).getFileName()).isEqualTo(testDir.testFile2.getFileName());
			assertThat(result.verbose()).isTrue();
			assertThat(result.quiet()).isFalse();
			assertThat(result.dryRun()).isTrue();
			assertThat(result.configPath()).isPresent();
			assertThat(result.configPath().get().getFileName()).isEqualTo(customConfig.getFileName());
			assertThat(result.outputDirectory()).isPresent();
			assertThat(result.outputDirectory().get().getFileName()).isEqualTo(outputDir.getFileName());
		}
		finally
		{
			testDir.cleanup();
		}
	}

	/**
	 * Verifies that the parser correctly handles a basic check command with a single file.
	 */
	@Test
	public void parseCheckCommand() throws ArgumentParsingException, IOException
	{
		CommandLineParser parser = new CommandLineParser();
		TestDirectory testDir = new TestDirectory();
		try
		{
			String[] args = {"check", testDir.testFile1.toString()};
			ParsedArguments result = parser.parse(args);

			assertThat(result.command()).isEqualTo(ParsedArguments.Command.CHECK);
			assertThat(result.inputFiles()).hasSize(1);
			assertThat(result.inputFiles().get(0).getFileName()).isEqualTo(testDir.testFile1.getFileName());
			assertThat(result.verbose()).isFalse();
			assertThat(result.dryRun()).isFalse();
		}
		finally
		{
			testDir.cleanup();
		}
	}

	/**
	 * Verifies that the parser correctly handles a check command with quiet mode and custom config.
	 */
	@Test
	public void parseCheckCommandWithConfig() throws ArgumentParsingException, IOException
	{
		CommandLineParser parser = new CommandLineParser();
		TestDirectory testDir = new TestDirectory();
		try
		{
			// Create config file
			Path configFile = testDir.tempDir.resolve(".styler.toml");
			Files.writeString(configFile, "# test config");

			String[] args = {
				"--quiet",
				"check",
				"--config", configFile.toString(),
				testDir.testFile1.toString()
			};
			ParsedArguments result = parser.parse(args);

			assertThat(result.command()).isEqualTo(ParsedArguments.Command.CHECK);
			assertThat(result.inputFiles()).hasSize(1);
			assertThat(result.inputFiles().get(0).getFileName()).isEqualTo(testDir.testFile1.getFileName());
			assertThat(result.verbose()).isFalse();
			assertThat(result.quiet()).isTrue();
			assertThat(result.configPath()).isPresent();
			assertThat(result.configPath().get().getFileName()).isEqualTo(configFile.getFileName());
		}
		finally
		{
			testDir.cleanup();
		}
	}

	/**
	 * Verifies that the parser correctly handles the config command without arguments.
	 */
	@Test
	public void parseConfigCommand() throws ArgumentParsingException
	{
		CommandLineParser parser = new CommandLineParser();
		String[] args = {"config"};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.CONFIG);
		assertThat(result.inputFiles()).isEmpty();
		assertThat(result.configPath()).isEmpty();
	}

	/**
	 * Verifies that the parser correctly handles the help subcommand.
	 */
	@Test
	public void parseHelpCommand() throws ArgumentParsingException
	{
		CommandLineParser parser = new CommandLineParser();
		String[] args = {"help"};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.HELP);
		// help subcommand, not --help flag
		assertThat(result.helpRequested()).isFalse();
		assertThat(result.inputFiles()).isEmpty();
	}

	/**
	 * Verifies that the parser correctly handles the --help flag.
	 */
	@Test
	public void parseHelpFlag() throws ArgumentParsingException
	{
		CommandLineParser parser = new CommandLineParser();
		String[] args = {"--help"};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.HELP);
		assertThat(result.helpRequested()).isTrue();
		assertThat(result.inputFiles()).isEmpty();
	}

	/**
	 * Verifies that the parser correctly handles the --version flag.
	 */
	@Test
	public void parseVersionFlag() throws ArgumentParsingException
	{
		CommandLineParser parser = new CommandLineParser();
		String[] args = {"--version"};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.VERSION);
		assertThat(result.versionRequested()).isTrue();
		assertThat(result.inputFiles()).isEmpty();
	}

	/**
	 * Verifies that the parser defaults to help when no arguments are provided.
	 */
	@Test
	public void parseNoArgs() throws ArgumentParsingException
	{
		CommandLineParser parser = new CommandLineParser();
		String[] args = {};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.HELP);
		assertThat(result.helpRequested()).isFalse();
		assertThat(result.inputFiles()).isEmpty();
	}

	/**
	 * Verifies that the parser correctly handles the -v short flag for verbose mode.
	 */
	@Test
	public void parseVerboseShortFlag() throws ArgumentParsingException, IOException
	{
		CommandLineParser parser = new CommandLineParser();
		TestDirectory testDir = new TestDirectory();
		try
		{
			String[] args = {"-v", "format", testDir.testFile1.toString()};
			ParsedArguments result = parser.parse(args);

			assertThat(result.verbose()).isTrue();
			assertThat(result.quiet()).isFalse();
		}
		finally
		{
			testDir.cleanup();
		}
	}

	/**
	 * Verifies that the parser correctly handles the -q short flag for quiet mode.
	 */
	@Test
	public void parseQuietShortFlag() throws ArgumentParsingException, IOException
	{
		CommandLineParser parser = new CommandLineParser();
		TestDirectory testDir = new TestDirectory();
		try
		{
			String[] args = {"-q", "format", testDir.testFile1.toString()};
			ParsedArguments result = parser.parse(args);

			assertThat(result.verbose()).isFalse();
			assertThat(result.quiet()).isTrue();
		}
		finally
		{
			testDir.cleanup();
		}
	}

	/**
	 * Verifies that the parser throws an exception when format command is missing required input files.
	 */
	@Test
	public void errorOnMissingInputFiles()
	{
		CommandLineParser parser = new CommandLineParser();
		String[] args = {"format"};

		assertThatThrownBy(() -> parser.parse(args)).
			isInstanceOf(ArgumentParsingException.class).
			hasMessageContaining("Input files are required");
	}

	/**
	 * Verifies that the parser throws an exception when check command is missing required input files.
	 */
	@Test
	public void errorOnMissingInputFilesForCheck()
	{
		CommandLineParser parser = new CommandLineParser();
		String[] args = {"check"};

		assertThatThrownBy(() -> parser.parse(args)).
			isInstanceOf(ArgumentParsingException.class).
			hasMessageContaining("Input files are required");
	}

	/**
	 * Verifies that the parser throws an exception when both --verbose and --quiet are specified.
	 */
	@Test
	public void errorOnConflictingVerboseQuiet()
	{
		CommandLineParser parser = new CommandLineParser();
		String[] args = {"--verbose", "--quiet", "format", "Test.java"};

		assertThatThrownBy(() -> parser.parse(args)).
			isInstanceOf(ArgumentParsingException.class).
			hasMessageContaining("Cannot specify both --verbose and --quiet");
	}

	/**
	 * Verifies that the parser throws an exception when both -v and -q short flags are specified.
	 */
	@Test
	public void errorOnConflictingVerboseQuietShortFlags()
	{
		CommandLineParser parser = new CommandLineParser();
		String[] args = {"-v", "-q", "format", "Test.java"};

		assertThatThrownBy(() -> parser.parse(args)).
			isInstanceOf(ArgumentParsingException.class).
			hasMessageContaining("Cannot specify both --verbose and --quiet");
	}

	/**
	 * Verifies that the parser throws an exception when an unknown option is provided.
	 */
	@Test
	public void errorOnInvalidOption()
	{
		CommandLineParser parser = new CommandLineParser();
		String[] args = {"--invalid-option", "format", "Test.java"};

		assertThatThrownBy(() -> parser.parse(args)).
			isInstanceOf(ArgumentParsingException.class).
			hasMessageContaining("Unknown option");
	}

	/**
	 * Verifies that the parser throws an exception when an invalid command is provided.
	 */
	@Test
	public void errorOnInvalidCommand()
	{
		CommandLineParser parser = new CommandLineParser();
		String[] args = {"invalid-command", "Test.java"};

		assertThatThrownBy(() -> parser.parse(args)).
			isInstanceOf(ArgumentParsingException.class);
	}

	/**
	 * Verifies that the usage text contains all expected commands and options.
	 */
	@Test
	public void getUsageText()
	{
		CommandLineParser parser = new CommandLineParser();
		String usage = parser.getUsageText();

		assertThat(usage).contains("styler");
		assertThat(usage).contains("format");
		assertThat(usage).contains("check");
		assertThat(usage).contains("config");
		assertThat(usage).contains("--verbose");
		assertThat(usage).contains("--quiet");
	}

	/**
	 * Verifies that the version text contains the application name and version number.
	 */
	@Test
	public void getVersionText()
	{
		CommandLineParser parser = new CommandLineParser();
		String version = parser.getVersionText();

		assertThat(version).contains("styler");
		assertThat(version).contains("1.0-SNAPSHOT");
	}

	/**
	 * Verifies that ArgumentParsingException formats error messages with usage text correctly.
	 */
	@Test
	public void argumentParsingExceptionFormatting()
	{
		ArgumentParsingException exception = new ArgumentParsingException(
			"Test error message",
			"Usage: styler [options] command");

		assertThat(exception.getMessage()).isEqualTo("Test error message");
		assertThat(exception.getUsageText()).isEqualTo("Usage: styler [options] command");

		String formatted = exception.getFormattedMessage();
		assertThat(formatted).contains("Error: Test error message");
		assertThat(formatted).contains("Usage:");
		assertThat(formatted).contains("styler [options] command");
	}

	/**
	 * Verifies that ArgumentParsingException correctly preserves the underlying cause.
	 */
	@Test
	public void argumentParsingExceptionWithCause()
	{
		RuntimeException cause = new RuntimeException("Root cause");
		ArgumentParsingException exception = new ArgumentParsingException(
			"Test error",
			"Usage text",
			cause);

		assertThat(exception.getCause()).isEqualTo(cause);
	}

	/**
	 * Verifies that ParsedArguments validation rejects conflicting verbose and quiet flags.
	 */
	@Test
	public void parsedArgumentsValidation()
	{
		assertThatThrownBy(() -> ParsedArguments.of(
			List.of(Paths.get("test.java")),
			null,
			null,
			// verbose
			true,
			// quiet - conflict!
			true,
			false,
			false,
			false,
			ParsedArguments.Command.FORMAT,
			null,
			null)).
		isInstanceOf(IllegalArgumentException.class).
		  hasMessageContaining("Cannot enable both verbose and quiet modes");
	}

	/**
	 * Verifies that ParsedArguments performs defensive copying and returns immutable collections.
	 */
	@Test
	public void parsedArgumentsDefensiveCopying()
	{
		List<Path> originalList = new java.util.ArrayList<>();
		originalList.add(Paths.get("test.java"));
		ParsedArguments args = ParsedArguments.of(
			originalList,
			null,
			null,
			false,
			false,
			false,
			false,
			false,
			ParsedArguments.Command.FORMAT,
			null,
			null);

		// Verify defensive copying worked - lists should be immutable
		assertThat(args.inputFiles()).isNotSameAs(originalList);
		assertThat(args.inputFiles()).isEqualTo(originalList);

		// Verify the result is immutable
		assertThatThrownBy(() -> args.inputFiles().add(Paths.get("another.java"))).
			isInstanceOf(UnsupportedOperationException.class);
	}

	/**
	 * Verifies that ParsedArguments helper methods correctly determine command types and log levels.
	 */
	@Test
	public void parsedArgumentsHelperMethods()
	{
		ParsedArguments helpArgs = ParsedArguments.of(
			List.of(),
			null,
			null,
			false,
			false,
			false,
			true,
			false,
			ParsedArguments.Command.HELP,
			null, null);

		assertThat(helpArgs.isInformationalRequest()).isTrue();
		assertThat(helpArgs.requiresInputFiles()).isFalse();
		assertThat(helpArgs.getLogLevel()).isEqualTo(ParsedArguments.LogLevel.NORMAL);

		ParsedArguments formatArgs = ParsedArguments.of(
			List.of(Paths.get("test.java")),
			null,
			null,
			true,
			false,
			false,
			false,
			false,
			ParsedArguments.Command.FORMAT,
			null, null);

		assertThat(formatArgs.isInformationalRequest()).isFalse();
		assertThat(formatArgs.requiresInputFiles()).isTrue();
		assertThat(formatArgs.getLogLevel()).isEqualTo(ParsedArguments.LogLevel.VERBOSE);

		ParsedArguments quietArgs = ParsedArguments.of(
			List.of(Paths.get("test.java")),
			null,
			null,
			false,
			true,
			false,
			false,
			false,
			ParsedArguments.Command.FORMAT,
			null, null);

		assertThat(quietArgs.getLogLevel()).isEqualTo(ParsedArguments.LogLevel.QUIET);
	}

	/**
	 * Verifies that empty or whitespace-only rules filters are handled as empty optionals.
	 */
	@Test
	public void emptyRulesFilterHandling()
	{
		ParsedArguments args = ParsedArguments.of(
			List.of(),
			null,
			// empty string
			"",
			false,
			false,
			false,
			false,
			false,
			ParsedArguments.Command.HELP,
			null, null);

		assertThat(args.rulesFilter()).isEmpty();

		args = ParsedArguments.of(
			List.of(),
			null,
			// whitespace only
			"   ",
			false,
			false,
			false,
			false,
			false,
			ParsedArguments.Command.HELP,
			null, null);

		assertThat(args.rulesFilter()).isEmpty();
	}

	/**
	 * Verifies that a null command parameter defaults to the HELP command.
	 */
	@Test
	public void nullCommandDefaultsToHelp()
	{
		ParsedArguments args = ParsedArguments.of(
			List.of(),
			null,
			null,
			false,
			false,
			false,
			false,
			false,
			// null command
			null,
			null, null);

		assertThat(args.command()).isEqualTo(ParsedArguments.Command.HELP);
	}
}
