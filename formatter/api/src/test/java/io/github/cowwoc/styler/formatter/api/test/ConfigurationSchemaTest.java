package io.github.cowwoc.styler.formatter.api.test;

import org.testng.annotations.Test;
import io.github.cowwoc.styler.formatter.api.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for the YAML-based configuration schema.
 *
 * @author Plugin Framework Team
 */
public class ConfigurationSchemaTest
{
	/**
	 * Test default configuration.
	 */
	@Test
	public void defaultConfiguration()
	{
		ConfigurationSchema config = new ConfigurationSchema();

		assertThat(config.getVersion()).isEqualTo("1.0");
		assertThat(config.getProfile()).isNull();
		assertThat(config.getExtendsProfiles()).isEmpty();
		assertThat(config.getGlobal()).isNotNull();
		assertThat(config.getRules()).isEmpty();
	}

	/**
	 * Test configuration with all fields.
	 */
	@Test
	public void configurationWithAllFields() throws ConfigurationException
	{
		GlobalConfiguration global = new GlobalConfiguration(
			GlobalConfiguration.IndentationType.SPACES,
			4,
			120,
			GlobalConfiguration.LineEnding.LF,
			StandardCharsets.UTF_8.name(),
			true,
			true,
			4);

		Map<String, Object> lineLengthRule = new HashMap<>();
		lineLengthRule.put("maxLineLength", 120);
		lineLengthRule.put("wrapStrategy", "SMART");
		lineLengthRule.put("indentContinuations", 4);

		Map<String, Map<String, Object>> rules = new HashMap<>();
		rules.put("LineLength", lineLengthRule);

		ConfigurationSchema config = new ConfigurationSchema(
			"1.0",
			"test-profile",
			Arrays.asList("base-profile", "extended-profile"),
			global,
			rules);

		assertThat(config.getVersion()).isEqualTo("1.0");
		assertThat(config.getProfile()).isEqualTo("test-profile");
		assertThat(config.getExtendsProfiles()).containsExactly("base-profile", "extended-profile");
		assertThat(config.getGlobal()).isEqualTo(global);
		assertThat(config.getRules()).hasSize(1);
		assertThat(config.getRules().get("LineLength")).isEqualTo(lineLengthRule);
	}

	/**
	 * Test YAML serialization.
	 */
	@Test
	public void yamlSerialization() throws ConfigurationException
	{
		GlobalConfiguration global = new GlobalConfiguration();
		Map<String, Object> rules = new HashMap<>();
		rules.put("maxLineLength", 100);

		Map<String, Map<String, Object>> ruleMap = new HashMap<>();
		ruleMap.put("LineLength", rules);

		ConfigurationSchema config = new ConfigurationSchema(
			"1.0",
			"test",
			Collections.emptyList(),
			global,
			ruleMap);

		String toml = config.toToml();

		assertThat(toml).isNotNull();
		assertThat(toml).contains("version = '1.0'");
		assertThat(toml).contains("profile = 'test'");
		assertThat(toml).contains("rules.LineLength.maxLineLength = 100");
	}

	/**
	 * Test TOML deserialization.
	 */
	@Test
	public void tomlDeserialization() throws ConfigurationException
	{
		String toml = """
			version = "1.0"
			profile = "test-profile"
			extends = ["base"]

			[global]
			indentationType = "SPACES"
			indentationSize = 4
			maxLineLength = 120

			[rules.LineLength]
			maxLineLength = 100
			wrapStrategy = "SMART"
			""";

		ConfigurationSchema config = ConfigurationSchema.fromToml(toml);

		assertThat(config.getVersion()).isEqualTo("1.0");
		assertThat(config.getProfile()).isEqualTo("test-profile");
		assertThat(config.getExtendsProfiles()).containsExactly("base");
		assertThat(config.getGlobal().getIndentationType()).isEqualTo(GlobalConfiguration.IndentationType.SPACES);
		assertThat(config.getGlobal().getIndentationSize()).isEqualTo(4);
		assertThat(config.getGlobal().getMaxLineLength()).isEqualTo(120);
		assertThat(config.getRules().get("LineLength").get("maxLineLength")).isEqualTo(100);
		assertThat(config.getRules().get("LineLength").get("wrapStrategy")).isEqualTo("SMART");
	}

	/**
	 * Test resource loading.
	 */
	@Test
	public void resourceLoading() throws ConfigurationException, IOException
	{
		// Load from test resources using test classloader
		try (InputStream inputStream = getClass().getResourceAsStream("/sample-config.toml"))
		{
			assertThat(inputStream).as("sample-config.toml resource").isNotNull();
			String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			ConfigurationSchema config = ConfigurationSchema.fromToml(content);

			assertThat(config.getVersion()).isEqualTo("1.0");
			assertThat(config.getProfile()).isEqualTo("corporate-java");
			assertThat(config.getExtendsProfiles()).containsExactly("google-java-style", "checkstyle-recommended");
			assertThat(config.getGlobal().getIndentationType()).isEqualTo(GlobalConfiguration.IndentationType.SPACES);
			assertThat(config.getRules()).containsKey("LineLength");
			assertThat(config.getRules()).containsKey("ImportOrder");
		}
	}

	/**
	 * Test rule configuration conversion.
	 */
	@Test
	public void ruleConfigurationConversion() throws ConfigurationException
	{
		String toml = """
			version = "1.0"

			[rules.LineLength]
			maxLineLength = 100
			wrapStrategy = "SMART"
			indentContinuations = 4
			breakBeforeOperators = true
			allowLongImports = false
			""";

		ConfigurationSchema config = ConfigurationSchema.fromToml(toml);
		LineLengthRuleConfiguration lineConfig = config.getRuleConfiguration("LineLength",
			LineLengthRuleConfiguration.class);

		assertThat(lineConfig).isNotNull();
		assertThat(lineConfig.getMaxLineLength()).isEqualTo(100);
		assertThat(lineConfig.getWrapStrategy()).isEqualTo(LineLengthRuleConfiguration.WrapStrategy.SMART);
		assertThat(lineConfig.getIndentContinuations()).isEqualTo(4);
		assertThat(lineConfig.isBreakBeforeOperators()).isTrue();
		assertThat(lineConfig.isAllowLongImports()).isFalse();
	}

	/**
	 * Test configuration merging.
	 */
	@Test
	public void configurationMerging() throws ConfigurationException
	{
		// Base configuration
		Map<String, Object> baseRule = new HashMap<>();
		baseRule.put("maxLineLength", 80);
		baseRule.put("wrapStrategy", "HARD_WRAP");

		Map<String, Map<String, Object>> baseRules = new HashMap<>();
		baseRules.put("LineLength", baseRule);

		ConfigurationSchema base = new ConfigurationSchema(
			"1.0", "base", Collections.emptyList(), new GlobalConfiguration(), baseRules);

		// Override configuration
		Map<String, Object> overrideRule = new HashMap<>();
		overrideRule.put("maxLineLength", 120);  // Override
		overrideRule.put("indentContinuations", 4);  // Add new setting

		Map<String, Map<String, Object>> overrideRules = new HashMap<>();
		overrideRules.put("LineLength", overrideRule);

		ConfigurationSchema override = new ConfigurationSchema(
			"1.0", "override", Collections.emptyList(), new GlobalConfiguration(), overrideRules);

		// Merge configurations
		ConfigurationSchema merged = base.merge(override);

		assertThat(merged.getProfile()).isEqualTo("override");  // Override takes precedence
		Map<String, Object> mergedLineLength = merged.getRules().get("LineLength");
		assertThat(mergedLineLength.get("maxLineLength")).isEqualTo(120);  // Overridden
		assertThat(mergedLineLength.get("wrapStrategy")).isEqualTo("HARD_WRAP");  // From base
		assertThat(mergedLineLength.get("indentContinuations")).isEqualTo(4);  // New from override
	}

	/**
	 * Test invalid TOML throws exception.
	 */
	@Test
	public void invalidTomlThrowsException()
	{
		String invalidToml = """
			version = "1.0"

			[rules.LineLength]
			maxLineLength = "not-a-number"
			""";

		// Should not throw during parsing (Jackson will handle type conversion)
		// but might throw during rule configuration conversion
		assertThatThrownBy(() ->
		{
			ConfigurationSchema config = ConfigurationSchema.fromToml(invalidToml);
			config.getRuleConfiguration("LineLength", LineLengthRuleConfiguration.class);
		}).isInstanceOf(ConfigurationException.class);
	}

	/**
	 * Test security validation.
	 */
	@Test
	public void securityValidation()
	{
		// Test dangerous content detection
		assertThatThrownBy(() ->
		{
			ConfigurationSchema.fromToml("version = \"${{java.version}}\"");
		}).isInstanceOf(SecurityException.class).
		  hasMessageContaining("dangerous");

		// Test template expressions (TOML equivalent security concern)
		assertThatThrownBy(() ->
		{
			ConfigurationSchema.fromToml("version = \"<%malicious%>\"");
		}).isInstanceOf(SecurityException.class).
		  hasMessageContaining("dangerous");
	}

	/**
	 * Test version validation.
	 */
	@Test
	public void versionValidation()
	{
		assertThatThrownBy(() ->
		{
			new ConfigurationSchema("invalid-version", null, Collections.emptyList(), null, Collections.emptyMap());
		}).isInstanceOf(IllegalArgumentException.class).
		  hasMessageContaining("major.minor");
	}

	/**
	 * Test profile name validation.
	 */
	@Test
	public void profileNameValidation()
	{
		assertThatThrownBy(() ->
		{
			new ConfigurationSchema("1.0", "invalid profile name!", Collections.emptyList(), null,
				Collections.emptyMap());
		}).isInstanceOf(IllegalArgumentException.class).
		  hasMessageContaining("letters, numbers, hyphens");
	}

	/**
	 * Test equals and hash code.
	 */
	@Test
	public void equalsAndHashCode()
	{
		ConfigurationSchema config1 = new ConfigurationSchema();
		ConfigurationSchema config2 = new ConfigurationSchema();

		assertThat(config1).isEqualTo(config2);
		assertThat(config1.hashCode()).isEqualTo(config2.hashCode());

		// Test with different content
		Map<String, Map<String, Object>> rules = new HashMap<>();
		rules.put("test", new HashMap<>());
		ConfigurationSchema config3 = new ConfigurationSchema("1.0", null, Collections.emptyList(),
			new GlobalConfiguration(), rules);

		assertThat(config1).isNotEqualTo(config3);
		assertThat(config1.hashCode()).isNotEqualTo(config3.hashCode());
	}

	/**
	 * Verifies that toString() returns a meaningful string representation.
	 */
	@Test
	public void toStringRepresentation()
	{
		ConfigurationSchema config = new ConfigurationSchema();
		String toString = config.toString();

		assertThat(toString).contains("ConfigurationSchema");
		assertThat(toString).contains("version='1.0'");
		assertThat(toString).contains("rules=[]");
	}
}