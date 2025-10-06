package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.ArgumentParsingException;
import io.github.cowwoc.styler.cli.CommandLineParser;
import io.github.cowwoc.styler.cli.ParsedArguments;
import io.github.cowwoc.styler.cli.StylerCLI;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the StylerCLI main class.
 */
public class StylerCLITest
{
	/**
	 * Verifies that the main StylerCLI class can be instantiated.
	 */
	@Test
	public void mainClassExists()
	{
		// Verify that the main class can be instantiated
		StylerCLI cli = new StylerCLI();
		assertThat(cli).isNotNull();
	}

	/**
	 * Verifies that the help command is recognized.
	 */
	@Test
	public void helpCommand() throws ArgumentParsingException
	{
		CommandLineParser parser = new CommandLineParser();
		ParsedArguments args = parser.parse(new String[]{"--help"});

		assertThat(args.helpRequested()).isTrue();
		assertThat(args.versionRequested()).isFalse();
		assertThat(args.command()).isEqualTo(ParsedArguments.Command.HELP);
	}

	/**
	 * Verifies that the version command is recognized.
	 */
	@Test
	public void versionCommand() throws ArgumentParsingException
	{
		CommandLineParser parser = new CommandLineParser();
		ParsedArguments args = parser.parse(new String[]{"--version"});

		assertThat(args.versionRequested()).isTrue();
		assertThat(args.helpRequested()).isFalse();
		assertThat(args.command()).isEqualTo(ParsedArguments.Command.VERSION);
	}

	/**
	 * Verifies that calling without subcommand shows help.
	 */
	@Test
	public void callWithoutSubcommand() throws ArgumentParsingException
	{
		CommandLineParser parser = new CommandLineParser();
		ParsedArguments args = parser.parse(new String[]{});

		assertThat(args.command()).isEqualTo(ParsedArguments.Command.HELP);
	}

	/**
	 * Verifies that the verbose flag is correctly parsed.
	 */
	@Test
	public void verboseFlag() throws ArgumentParsingException
	{
		CommandLineParser parser = new CommandLineParser();
		ParsedArguments args = parser.parse(new String[]{"--verbose"});

		assertThat(args.verbose()).isTrue();
		assertThat(args.quiet()).isFalse();
	}

	/**
	 * Verifies that the quiet flag is correctly parsed.
	 */
	@Test
	public void quietFlag() throws ArgumentParsingException
	{
		CommandLineParser parser = new CommandLineParser();
		ParsedArguments args = parser.parse(new String[]{"--quiet"});

		assertThat(args.quiet()).isTrue();
		assertThat(args.verbose()).isFalse();
	}
}