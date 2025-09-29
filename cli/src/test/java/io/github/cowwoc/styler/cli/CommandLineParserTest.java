package io.github.cowwoc.styler.cli;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for CommandLineParser.
 * <p>
 * Tests cover argument parsing, validation, error handling, and edge cases
 * to ensure the parser correctly handles all supported CLI patterns.
 */
public class CommandLineParserTest
{
	private CommandLineParser parser;

	@BeforeMethod
	public void setUp()
	{
		parser = new CommandLineParser();
	}

	@Test
	public void testParseFormatCommand() throws ArgumentParsingException
	{
		String[] args = {"format", "src/main/java/Test.java"};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.FORMAT);
		assertThat(result.inputFiles()).containsExactly(Paths.get("src/main/java/Test.java"));
		assertThat(result.verbose()).isFalse();
		assertThat(result.quiet()).isFalse();
		assertThat(result.dryRun()).isFalse();
		assertThat(result.configPath()).isEmpty();
		assertThat(result.outputDirectory()).isEmpty();
	}

	@Test
	public void testParseFormatCommandWithOptions() throws ArgumentParsingException
	{
		String[] args = {
			"--verbose",
			"format",
			"--config", "custom.toml",
			"--dry-run",
			"--output", "target/formatted",
			"src/main/java/Test.java",
			"src/test/java/TestTest.java"
		};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.FORMAT);
		assertThat(result.inputFiles()).containsExactly(
			Paths.get("src/main/java/Test.java"),
			Paths.get("src/test/java/TestTest.java")
		);
		assertThat(result.verbose()).isTrue();
		assertThat(result.quiet()).isFalse();
		assertThat(result.dryRun()).isTrue();
		assertThat(result.configPath()).isEqualTo(Optional.of(Paths.get("custom.toml")));
		assertThat(result.outputDirectory()).isEqualTo(Optional.of(Paths.get("target/formatted")));
	}

	@Test
	public void testParseCheckCommand() throws ArgumentParsingException
	{
		String[] args = {"check", "src/main/java"};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.CHECK);
		assertThat(result.inputFiles()).containsExactly(Paths.get("src/main/java"));
		assertThat(result.verbose()).isFalse();
		assertThat(result.dryRun()).isFalse();
	}

	@Test
	public void testParseCheckCommandWithConfig() throws ArgumentParsingException
	{
		String[] args = {
			"--quiet",
			"check",
			"--config", ".styler.toml",
			"src/main/java/Test.java"
		};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.CHECK);
		assertThat(result.inputFiles()).containsExactly(Paths.get("src/main/java/Test.java"));
		assertThat(result.verbose()).isFalse();
		assertThat(result.quiet()).isTrue();
		assertThat(result.configPath()).isEqualTo(Optional.of(Paths.get(".styler.toml")));
	}

	@Test
	public void testParseConfigCommand() throws ArgumentParsingException
	{
		String[] args = {"config"};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.CONFIG);
		assertThat(result.inputFiles()).isEmpty();
		assertThat(result.configPath()).isEmpty();
	}

	@Test
	public void testParseHelpCommand() throws ArgumentParsingException
	{
		String[] args = {"help"};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.HELP);
		assertThat(result.helpRequested()).isFalse(); // help subcommand, not --help flag
		assertThat(result.inputFiles()).isEmpty();
	}

	@Test
	public void testParseHelpFlag() throws ArgumentParsingException
	{
		String[] args = {"--help"};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.HELP);
		assertThat(result.helpRequested()).isTrue();
		assertThat(result.inputFiles()).isEmpty();
	}

	@Test
	public void testParseVersionFlag() throws ArgumentParsingException
	{
		String[] args = {"--version"};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.VERSION);
		assertThat(result.versionRequested()).isTrue();
		assertThat(result.inputFiles()).isEmpty();
	}

	@Test
	public void testParseNoArgs() throws ArgumentParsingException
	{
		String[] args = {};
		ParsedArguments result = parser.parse(args);

		assertThat(result.command()).isEqualTo(ParsedArguments.Command.HELP);
		assertThat(result.helpRequested()).isFalse();
		assertThat(result.inputFiles()).isEmpty();
	}

	@Test
	public void testParseVerboseShortFlag() throws ArgumentParsingException
	{
		String[] args = {"-v", "format", "Test.java"};
		ParsedArguments result = parser.parse(args);

		assertThat(result.verbose()).isTrue();
		assertThat(result.quiet()).isFalse();
	}

	@Test
	public void testParseQuietShortFlag() throws ArgumentParsingException
	{
		String[] args = {"-q", "format", "Test.java"};
		ParsedArguments result = parser.parse(args);

		assertThat(result.verbose()).isFalse();
		assertThat(result.quiet()).isTrue();
	}

	@Test
	public void testErrorOnMissingInputFiles()
	{
		String[] args = {"format"};

		assertThatThrownBy(() -> parser.parse(args))
			.isInstanceOf(ArgumentParsingException.class)
			.hasMessageContaining("Missing required parameter");
	}

	@Test
	public void testErrorOnMissingInputFilesForCheck()
	{
		String[] args = {"check"};

		assertThatThrownBy(() -> parser.parse(args))
			.isInstanceOf(ArgumentParsingException.class)
			.hasMessageContaining("Missing required parameter");
	}

	@Test
	public void testErrorOnConflictingVerboseQuiet()
	{
		String[] args = {"--verbose", "--quiet", "format", "Test.java"};

		assertThatThrownBy(() -> parser.parse(args))
			.isInstanceOf(ArgumentParsingException.class)
			.hasMessageContaining("Cannot specify both --verbose and --quiet");
	}

	@Test
	public void testErrorOnConflictingVerboseQuietShortFlags()
	{
		String[] args = {"-v", "-q", "format", "Test.java"};

		assertThatThrownBy(() -> parser.parse(args))
			.isInstanceOf(ArgumentParsingException.class)
			.hasMessageContaining("Cannot specify both --verbose and --quiet");
	}

	@Test
	public void testErrorOnInvalidOption()
	{
		String[] args = {"--invalid-option", "format", "Test.java"};

		assertThatThrownBy(() -> parser.parse(args))
			.isInstanceOf(ArgumentParsingException.class)
			.hasMessageContaining("Unknown option");
	}

	@Test
	public void testErrorOnInvalidCommand()
	{
		String[] args = {"invalid-command", "Test.java"};

		assertThatThrownBy(() -> parser.parse(args))
			.isInstanceOf(ArgumentParsingException.class);
	}

	@Test
	public void testGetUsageText()
	{
		String usage = parser.getUsageText();

		assertThat(usage).contains("styler");
		assertThat(usage).contains("format");
		assertThat(usage).contains("check");
		assertThat(usage).contains("config");
		assertThat(usage).contains("--verbose");
		assertThat(usage).contains("--quiet");
	}

	@Test
	public void testGetVersionText()
	{
		String version = parser.getVersionText();

		assertThat(version).contains("styler");
		assertThat(version).contains("1.0-SNAPSHOT");
	}

	@Test
	public void testArgumentParsingExceptionFormatting()
	{
		ArgumentParsingException exception = new ArgumentParsingException(
			"Test error message",
			"Usage: styler [options] command"
		);

		assertThat(exception.getMessage()).isEqualTo("Test error message");
		assertThat(exception.getUsageText()).isEqualTo("Usage: styler [options] command");

		String formatted = exception.getFormattedMessage();
		assertThat(formatted).contains("Error: Test error message");
		assertThat(formatted).contains("Usage:");
		assertThat(formatted).contains("styler [options] command");
	}

	@Test
	public void testArgumentParsingExceptionWithCause()
	{
		RuntimeException cause = new RuntimeException("Root cause");
		ArgumentParsingException exception = new ArgumentParsingException(
			"Test error",
			"Usage text",
			cause
		);

		assertThat(exception.getCause()).isEqualTo(cause);
	}

	@Test
	public void testParsedArgumentsValidation()
	{
		assertThatThrownBy(() -> ParsedArguments.of(
			List.of(Paths.get("test.java")),
			null,
			null,
			true,  // verbose
			true,  // quiet - conflict!
			false,
			false,
			false,
			ParsedArguments.Command.FORMAT,
			null
		)).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("Cannot enable both verbose and quiet modes");
	}

	@Test
	public void testParsedArgumentsDefensiveCopying()
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
			null
		);

		// Verify defensive copying worked - lists should be immutable
		assertThat(args.inputFiles()).isNotSameAs(originalList);
		assertThat(args.inputFiles()).isEqualTo(originalList);

		// Verify the result is immutable
		assertThatThrownBy(() -> args.inputFiles().add(Paths.get("another.java")))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void testParsedArgumentsHelperMethods()
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
			null
		);

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
			null
		);

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
			null
		);

		assertThat(quietArgs.getLogLevel()).isEqualTo(ParsedArguments.LogLevel.QUIET);
	}

	@Test
	public void testEmptyRulesFilterHandling()
	{
		ParsedArguments args = ParsedArguments.of(
			List.of(),
			null,
			"",  // empty string
			false,
			false,
			false,
			false,
			false,
			ParsedArguments.Command.HELP,
			null
		);

		assertThat(args.rulesFilter()).isEmpty();

		args = ParsedArguments.of(
			List.of(),
			null,
			"   ",  // whitespace only
			false,
			false,
			false,
			false,
			false,
			ParsedArguments.Command.HELP,
			null
		);

		assertThat(args.rulesFilter()).isEmpty();
	}

	@Test
	public void testNullCommandDefaultsToHelp()
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
			null,  // null command
			null
		);

		assertThat(args.command()).isEqualTo(ParsedArguments.Command.HELP);
	}
}