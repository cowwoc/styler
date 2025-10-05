package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.StylerCLI;

import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

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
	 * Verifies that the help command executes and returns the expected exit code.
	 */
	@Test
	public void helpCommand()
	{
		// Test that help command executes and prints help
		// Note: picocli returns 0 for help requests when using mixinStandardHelpOptions
		StylerCLI cli = new StylerCLI();
		CommandLine commandLine = new CommandLine(cli);

		// Redirect output to suppress test output
		StringWriter sw = new StringWriter();
		commandLine.setOut(new PrintWriter(sw));

		int exitCode = commandLine.execute("--help");
		assertThat(exitCode).isEqualTo(0); // picocli help exit code with mixin
	}

	/**
	 * Verifies that the version command executes and returns the expected exit code.
	 */
	@Test
	public void versionCommand()
	{
		// Test that version command executes and prints version
		// Note: picocli returns 0 for version requests when using mixinStandardHelpOptions
		StylerCLI cli = new StylerCLI();
		CommandLine commandLine = new CommandLine(cli);

		// Redirect output to suppress test output
		StringWriter sw = new StringWriter();
		commandLine.setOut(new PrintWriter(sw));

		int exitCode = commandLine.execute("--version");
		assertThat(exitCode).isEqualTo(0); // picocli version exit code with mixin
	}

	/**
	 * Verifies that calling CLI without subcommand returns successful exit code.
	 */
	@Test
	public void callWithoutSubcommand()
	{
		// Test that calling without subcommand shows help
		StylerCLI cli = new StylerCLI();
		CommandLine commandLine = new CommandLine(cli);

		// Redirect output to suppress test output (thread-safe per-instance)
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		cli.setOut(new PrintStream(baos, true, StandardCharsets.UTF_8));

		int exitCode = commandLine.execute(); // No args - triggers default behavior
		assertThat(exitCode).isEqualTo(0);
	}

	/**
	 * Verifies that the verbose flag is correctly recognized and parsed.
	 */
	@Test
	public void verboseFlag()
	{
		// Test that verbose flag is recognized
		StylerCLI cli = new StylerCLI();
		CommandLine commandLine = new CommandLine(cli);

		commandLine.parseArgs("--verbose");
		assertThat(cli.isVerbose()).isTrue();
	}

	/**
	 * Verifies that the quiet flag is correctly recognized and parsed.
	 */
	@Test
	public void quietFlag()
	{
		// Test that quiet flag is recognized
		StylerCLI cli = new StylerCLI();
		CommandLine commandLine = new CommandLine(cli);

		commandLine.parseArgs("--quiet");
		assertThat(cli.isQuiet()).isTrue();
	}
}