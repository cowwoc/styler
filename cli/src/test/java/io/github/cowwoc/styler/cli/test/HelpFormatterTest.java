package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.HelpFormatter;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link HelpFormatter} help and version text generation.
 * <p>
 * All tests are parallel-safe with no shared mutable state.
 */
public class HelpFormatterTest
{
	// ========== Help Text Tests ==========

	/**
	 * Validates that help text always contains usage line with application name.
	 */
	@Test
	public void formatHelpAlwaysContainsUsageLine()
	{
		// Arrange
		HelpFormatter formatter = new HelpFormatter();

		// Act
		String help = formatter.formatHelp();

		// Assert
		requireThat(help, "help").contains("Usage:");
		requireThat(help, "help").contains("styler");
	}

	/**
	 * Validates that help text contains complete options section with all flags.
	 */
	@Test
	public void formatHelpAlwaysContainsOptionsSection()
	{
		// Arrange
		HelpFormatter formatter = new HelpFormatter();

		// Act
		String help = formatter.formatHelp();

		// Assert
		requireThat(help, "help").contains("OPTIONS:");
		requireThat(help, "help").contains("--config");
		requireThat(help, "help").contains("--check");
		requireThat(help, "help").contains("--fix");
		requireThat(help, "help").contains("--help");
		requireThat(help, "help").contains("--version");
	}

	/**
	 * Validates that help text contains examples section showing common usage patterns.
	 */
	@Test
	public void formatHelpAlwaysContainsExamplesSection()
	{
		// Arrange
		HelpFormatter formatter = new HelpFormatter();

		// Act
		String help = formatter.formatHelp();

		// Assert
		requireThat(help, "help").contains("EXAMPLES:");
	}

	/**
	 * Validates that help text contains exit codes section with all exit codes.
	 */
	@Test
	public void formatHelpAlwaysContainsExitCodesSection()
	{
		// Arrange
		HelpFormatter formatter = new HelpFormatter();

		// Act
		String help = formatter.formatHelp();

		// Assert
		requireThat(help, "help").contains("EXIT CODES:");
		requireThat(help, "help").contains("0");
		requireThat(help, "help").contains("1");
		requireThat(help, "help").contains("2");
	}

	/**
	 * Validates that help text is non-empty and has substantial content.
	 */
	@Test
	public void formatHelpAlwaysNonEmpty()
	{
		// Arrange
		HelpFormatter formatter = new HelpFormatter();

		// Act
		String help = formatter.formatHelp();

		// Assert
		requireThat(help.isEmpty(), "value").isFalse();
		requireThat(help.length() > 100, "value").isTrue();
	}

	// ========== Version Text Tests ==========

	/**
	 * Validates that version text contains the application name.
	 */
	@Test
	public void formatVersionAlwaysContainsStylerName()
	{
		// Arrange
		HelpFormatter formatter = new HelpFormatter();

		// Act
		String version = formatter.formatVersion();

		// Assert
		requireThat(version, "version").contains("Styler");
	}

	/**
	 * Validates that version text contains a version number in numeric format.
	 */
	@Test
	public void formatVersionAlwaysContainsVersionNumber()
	{
		// Arrange
		HelpFormatter formatter = new HelpFormatter();

		// Act
		String version = formatter.formatVersion();

		// Assert
		requireThat(version.matches("(?s).*\\d+\\.\\d+.*"),
			"value").isTrue();
	}

	/**
	 * Validates that version text contains Java version information.
	 */
	@Test
	public void formatVersionAlwaysContainsJavaVersion()
	{
		// Arrange
		HelpFormatter formatter = new HelpFormatter();

		// Act
		String version = formatter.formatVersion();

		// Assert
		requireThat(version, "version").contains("Java");
		requireThat(version, "version").contains(System.getProperty("java.version"));
	}

	/**
	 * Validates that version text is non-empty.
	 */
	@Test
	public void formatVersionAlwaysNonEmpty()
	{
		// Arrange
		HelpFormatter formatter = new HelpFormatter();

		// Act
		String version = formatter.formatVersion();

		// Assert
		requireThat(version.isEmpty(), "value").isFalse();
	}

	// ========== Consistency Tests ==========

	/**
	 * Validates that formatHelp() returns consistent results across multiple calls.
	 */
	@Test
	public void formatHelpCalledTwiceReturnsSameText()
	{
		// Arrange
		HelpFormatter formatter = new HelpFormatter();

		// Act
		String help1 = formatter.formatHelp();
		String help2 = formatter.formatHelp();

		// Assert
		requireThat(help1, "help1").isEqualTo(help2);
	}

	/**
	 * Validates that formatVersion() returns consistent results across multiple calls.
	 */
	@Test
	public void formatVersionCalledTwiceReturnsSameText()
	{
		// Arrange
		HelpFormatter formatter = new HelpFormatter();

		// Act
		String version1 = formatter.formatVersion();
		String version2 = formatter.formatVersion();

		// Assert
		requireThat(version1, "version1").isEqualTo(version2);
	}
}
