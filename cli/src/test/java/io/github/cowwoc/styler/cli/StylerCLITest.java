package io.github.cowwoc.styler.cli;

import org.testng.annotations.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the StylerCLI main class.
 */
public class StylerCLITest
{
	@Test
	public void testMainClassExists()
	{
		// Verify that the main class can be instantiated
		StylerCLI cli = new StylerCLI();
		assertThat(cli).isNotNull();
	}

	@Test
	public void testHelpCommand()
	{
		// Test that help command executes and prints help
		// Note: picocli returns 2 for help requests by default
		StylerCLI cli = new StylerCLI();
		CommandLine commandLine = new CommandLine(cli);

		int exitCode = commandLine.execute("--help");
		assertThat(exitCode).isEqualTo(2); // picocli help exit code
	}

	@Test
	public void testVersionCommand()
	{
		// Test that version command executes and prints version
		// Note: picocli returns 2 for version requests by default
		StylerCLI cli = new StylerCLI();
		CommandLine commandLine = new CommandLine(cli);

		int exitCode = commandLine.execute("--version");
		assertThat(exitCode).isEqualTo(0); // picocli version exit code
	}

	@Test
	public void testCallWithoutSubcommand()
	{
		// Test that calling without subcommand shows help
		StylerCLI cli = new StylerCLI();
		Integer result = cli.call();
		assertThat(result).isEqualTo(0);
	}

	@Test
	public void testVerboseFlag()
	{
		// Test that verbose flag is recognized
		StylerCLI cli = new StylerCLI();
		CommandLine commandLine = new CommandLine(cli);

		commandLine.parseArgs("--verbose");
		assertThat(cli.isVerbose()).isTrue();
	}

	@Test
	public void testQuietFlag()
	{
		// Test that quiet flag is recognized
		StylerCLI cli = new StylerCLI();
		CommandLine commandLine = new CommandLine(cli);

		commandLine.parseArgs("--quiet");
		assertThat(cli.isQuiet()).isTrue();
	}
}