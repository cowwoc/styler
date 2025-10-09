package io.github.cowwoc.styler.config.test;

import io.github.cowwoc.styler.config.Config;
import io.github.cowwoc.styler.config.ConfigParser;
import io.github.cowwoc.styler.config.exception.ConfigurationException;
import io.github.cowwoc.styler.config.exception.ConfigurationSyntaxException;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static org.testng.Assert.assertThrows;

/**
 * Tests for {@link ConfigParser} focusing on TOML syntax parsing and validation.
 * <p>
 * Priority 1: Business logic tests validate proper error handling for syntax vs validation errors.
 */
public final class ConfigParserTest
{
	private final ConfigParser parser = new ConfigParser();

	/**
	 * RISK: Valid TOML with valid values doesn't parse
	 * IMPACT: Critical - core happy path must work
	 */
	@Test
	public void parseToml_validSyntaxAndValues_succeeds() throws ConfigurationException
	{
		String toml = "maxLineLength = 100";

		Config config = parser.parseToml(toml, "test.toml").build();

		requireThat(config.maxLineLength(), "maxLineLength").isEqualTo(100);
	}

	/**
	 * RISK: Invalid TOML syntax throws wrong exception type
	 * IMPACT: High - users can't distinguish syntax vs validation errors
	 */
	@Test
	public void parseToml_invalidSyntax_throwsConfigurationSyntaxException()
	{
		String invalidToml = "maxLineLength = [unclosed array";

		assertThrows(ConfigurationSyntaxException.class,
			() -> parser.parseToml(invalidToml, "test.toml"));
	}

	/**
	 * RISK: Valid syntax but invalid values throws wrong exception
	 * IMPACT: High - business rule violations must be caught by Config validation
	 * <p>
	 * Note: ConfigBuilder.build() throws IllegalArgumentException for validation failures.
	 * ConfigurationLoader is responsible for converting to ConfigurationValidationException
	 * with file path and line number information.
	 */
	@Test
	public void parseToml_validSyntaxInvalidValue_throwsIllegalArgumentException()
	{
		String toml = "maxLineLength = -10";  // Negative value violates business rule

		assertThrows(IllegalArgumentException.class,
			() -> parser.parseToml(toml, "test.toml").build());
	}

	/**
	 * RISK: Empty TOML uses wrong defaults
	 * IMPACT: Medium - empty config files should use system defaults
	 */
	@Test
	public void parseToml_emptyToml_usesDefaults() throws ConfigurationException
	{
		String emptyToml = "";

		Config config = parser.parseToml(emptyToml, "test.toml").build();

		requireThat(config.maxLineLength(), "maxLineLength").
			isEqualTo(Config.DEFAULT_MAX_LINE_LENGTH);
	}

	/**
	 * RISK: TOML with only comments treated as invalid
	 * IMPACT: Low - valid TOML pattern should work
	 */
	@Test
	public void parseToml_onlyComments_usesDefaults() throws ConfigurationException
	{
		String toml = """
			# This is a comment
			# maxLineLength = 100 (commented out)
			""";

		Config config = parser.parseToml(toml, "test.toml").build();

		requireThat(config.maxLineLength(), "maxLineLength").
			isEqualTo(Config.DEFAULT_MAX_LINE_LENGTH);
	}

	/**
	 * RISK: Unknown TOML fields cause parsing failure
	 * IMPACT: Medium - forward compatibility requires ignoring unknown fields
	 */
	@Test
	public void parseToml_unknownFields_ignored() throws ConfigurationException
	{
		String toml = """
			maxLineLength = 100
			unknownField = "future feature"
			anotherUnknown = 42
			""";

		Config config = parser.parseToml(toml, "test.toml").build();

		// Should parse successfully, ignoring unknown fields
		requireThat(config.maxLineLength(), "maxLineLength").isEqualTo(100);
	}

	/**
	 * RISK: Malformed TOML with misleading error messages
	 * IMPACT: Medium - users need clear indication of what went wrong
	 */
	@Test
	public void parseToml_malformedToml_includesSourceInError()
	{
		String toml = "maxLineLength = ";  // Incomplete assignment

		assertThrows(ConfigurationSyntaxException.class,
			() -> parser.parseToml(toml, "project/.styler.toml"));
	}

	/**
	 * RISK: Boundary value (maxLineLength = 1) rejected
	 * IMPACT: Low - edge case but valid per "must be positive" rule
	 */
	@Test
	public void parseToml_boundaryValueOne_accepted() throws ConfigurationException
	{
		String toml = "maxLineLength = 1";

		Config config = parser.parseToml(toml, "test.toml").build();

		requireThat(config.maxLineLength(), "maxLineLength").isEqualTo(1);
	}

	/**
	 * RISK: Zero value treated as valid (should fail "positive" constraint)
	 * IMPACT: Medium - business rule enforcement must be strict
	 * <p>
	 * Note: ConfigBuilder.build() throws IllegalArgumentException for validation failures.
	 * ConfigurationLoader is responsible for converting to ConfigurationValidationException
	 * with file path and line number information.
	 */
	@Test
	public void parseToml_zeroValue_throwsIllegalArgumentException()
	{
		String toml = "maxLineLength = 0";

		assertThrows(IllegalArgumentException.class,
			() -> parser.parseToml(toml, "test.toml").build());
	}
}
