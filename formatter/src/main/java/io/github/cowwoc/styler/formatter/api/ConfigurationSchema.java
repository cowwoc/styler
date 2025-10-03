package io.github.cowwoc.styler.formatter.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TOML-based configuration schema for formatting rules and profiles.
 * <p>
 * This class provides a complete configuration framework supporting:
 * <ul>
 * <li>TOML serialization and deserialization with Jackson</li>
 * <li>Rule-specific configuration with type safety</li>
 * <li>Configuration inheritance and profile management</li>
 * <li>Security validation and input sanitization</li>
 * <li>Comprehensive error reporting and validation</li>
 * </ul>
 * <p>
 * <b>Thread Safety:</b> This class is immutable and thread-safe after construction.
 * <b>Security:</b> All configuration inputs are validated to prevent injection attacks.
 * <b>Performance:</b> Configurations are cached and reused for better performance.
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Plugin Framework Team
  * @throws NullPointerException if {@code configPath} is null
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"version", "profile", "extends", "global", "rules"})
public final class ConfigurationSchema
{
	private static final String SCHEMA_VERSION = "1.0";
	private static final ObjectMapper TOML_MAPPER = createTomlMapper();

	@JsonProperty("version")
	private final String version;

	@JsonProperty("profile")
	private final String profile;

	@JsonProperty("extends")
	private final List<String> extendsProfiles;

	@JsonProperty("global")
	private final GlobalConfiguration global;

	@JsonProperty("rules")
	private final Map<String, Map<String, Object>> rules;

	/**
	 * Creates a new configuration schema.
	 *
	 * @param version         the schema version, never {@code null}
	 * @param profile         the profile name, may be {@code null}
	 * @param extendsProfiles the list of profiles to extend, never {@code null}
	 * @param global          the global configuration settings, may be {@code null}
	 * @param rules           the rule-specific configurations, never {@code null}
	  * @throws NullPointerException if {@code configPath} is null
	 */
	public ConfigurationSchema(@JsonProperty("version") String version,
	                          @JsonProperty("profile") String profile,
	                          @JsonProperty("extends") List<String> extendsProfiles,
	                          @JsonProperty("global") GlobalConfiguration global,
	                          @JsonProperty("rules") Map<String, Map<String, Object>> rules)
	{
		if (version != null)
		{
			this.version = validateVersion(version);
		}
		else
		{
			this.version = validateVersion(SCHEMA_VERSION);
		}
		this.profile = validateProfile(profile);
		if (extendsProfiles != null)
		{
			this.extendsProfiles = List.copyOf(extendsProfiles);
		}
		else
		{
			this.extendsProfiles = List.copyOf(Collections.emptyList());
		}
		if (global != null)
		{
			this.global = global;
		}
		else
		{
			this.global = new GlobalConfiguration();
		}
		if (rules != null)
		{
			this.rules = Map.copyOf(rules);
		}
		else
		{
			this.rules = Map.copyOf(Collections.emptyMap());
		}

		try
		{
			validate();
		}
		catch (ConfigurationException e)
		{
			throw new IllegalArgumentException("Invalid configuration", e);
		}
	}

	/**
	 * Creates a default configuration schema.
	 *
	 * @throws NullPointerException if {@code resourcePath} is null
	 * @throws NullPointerException if {@code configPath} is null
	 */
	public ConfigurationSchema()
	{
		this(SCHEMA_VERSION, null, Collections.emptyList(), new GlobalConfiguration(), Collections.emptyMap());
	}

	/**
	 * Loads a configuration schema from a TOML file.
	 *
	 * @param configPath the path to the TOML configuration file, never {@code null}
	 * @return the parsed configuration schema, never {@code null}
	 * @throws ConfigurationException if the file cannot be read or parsed
	 * @throws SecurityException      if the file contains security violations
	  * @throws NullPointerException if {@code tomlContent} is null
	  * @throws NullPointerException if {@code resourcePath} is null
	  * @throws NullPointerException if {@code configPath} is null
	 */
	public static ConfigurationSchema fromFile(Path configPath) throws ConfigurationException
	{
		requireThat(configPath, "configPath").isNotNull();

		try
		{
			if (!Files.exists(configPath))
			{
				throw new ConfigurationException("Configuration file not found: " + configPath);
			}

			if (!Files.isRegularFile(configPath))
			{
				throw new ConfigurationException("Configuration path is not a regular file: " + configPath);
			}

			// Security check: validate file size
			long fileSize = Files.size(configPath);
			if (fileSize > 1024 * 1024) // 1MB limit
			{
				throw new SecurityException("Configuration file exceeds maximum size limit: " + fileSize + " bytes");
			}

			String content = Files.readString(configPath, StandardCharsets.UTF_8);
			return fromToml(content);
		}
		catch (IOException e)
		{
			throw new ConfigurationException("Failed to read configuration file: " + configPath, e);
		}
	}

	/**
	 * Loads a configuration schema from a TOML resource.
	 *
	 * @param resourcePath the classpath resource path, never {@code null}
	 * @return the parsed configuration schema, never {@code null}
	 * @throws ConfigurationException if the resource cannot be read or parsed
	  * @throws NullPointerException if {@code tomlContent} is null
	  * @throws NullPointerException if {@code resourcePath} is null
	  * @throws NullPointerException if {@code other} is null
	 */
	public static ConfigurationSchema fromResource(String resourcePath) throws ConfigurationException
	{
		requireThat(resourcePath, "resourcePath").isNotNull();

		try (InputStream inputStream = ConfigurationSchema.class.getResourceAsStream(resourcePath))
		{
			if (inputStream == null)
			{
				throw new ConfigurationException("Configuration resource not found: " + resourcePath);
			}

			String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			return fromToml(content);
		}
		catch (IOException e)
		{
			throw new ConfigurationException("Failed to read configuration resource: " + resourcePath, e);
		}
	}

	/**
	 * Parses a configuration schema from TOML content.
	 *
	 * @param tomlContent the TOML content to parse, never {@code null}
	 * @return the parsed configuration schema, never {@code null}
	 * @throws ConfigurationException if the TOML cannot be parsed
	 * @throws SecurityException      if the content contains security violations
	  * @throws NullPointerException if {@code tomlContent} is null
	  * @throws NullPointerException if {@code other} is null
	 */
	public static ConfigurationSchema fromToml(String tomlContent) throws ConfigurationException
	{
		requireThat(tomlContent, "tomlContent").isNotNull();

		// Security validation
		validateTomlContent(tomlContent);

		try
		{
			ConfigurationSchema config = TOML_MAPPER.readValue(tomlContent, ConfigurationSchema.class);
			config.validate();
			return config;
		}
		catch (IOException e)
		{
			throw new ConfigurationException("Failed to parse TOML configuration", e);
		}
	}

	/**
	 * Converts this configuration schema to TOML format.
	 *
	 * @return the TOML representation, never {@code null}
	 * @throws ConfigurationException if serialization fails
	  * @throws NullPointerException if {@code other} is null
	 */
	public String toToml() throws ConfigurationException
	{
		try
		{
			StringWriter writer = new StringWriter();
			TOML_MAPPER.writeValue(writer, this);
			return writer.toString();
		}
		catch (IOException e)
		{
			throw new ConfigurationException("Failed to serialize configuration to TOML", e);
		}
	}

	/**
	 * Merges this configuration with another configuration.
	 * <p>
	 * The other configuration takes precedence for any overlapping settings.
	 *
	 * @param other the configuration to merge with, never {@code null}
	 * @return a new merged configuration, never {@code null}
	 * @throws ConfigurationException if the configurations cannot be merged
	  * @throws NullPointerException if {@code other} is null
	 */
	public ConfigurationSchema merge(ConfigurationSchema other) throws ConfigurationException
	{
		requireThat(other, "other").isNotNull();

		// Merge rules by combining maps
		Map<String, Map<String, Object>> mergedRules = new HashMap<>(this.rules);
		for (Map.Entry<String, Map<String, Object>> entry : other.rules.entrySet())
		{
			String ruleName = entry.getKey();
			Map<String, Object> otherRuleConfig = entry.getValue();

			if (mergedRules.containsKey(ruleName))
			{
				// Merge rule-specific configurations
				Map<String, Object> existingConfig = new HashMap<>(mergedRules.get(ruleName));
				existingConfig.putAll(otherRuleConfig);
				mergedRules.put(ruleName, existingConfig);
			}
			else
			{
				mergedRules.put(ruleName, new HashMap<>(otherRuleConfig));
			}
		}

		// Other configuration takes precedence for top-level settings
		String mergedVersion;
		if (other.version != null)
		{
			mergedVersion = other.version;
		}
		else
		{
			mergedVersion = this.version;
		}

		String mergedProfile;
		if (other.profile != null)
		{
			mergedProfile = other.profile;
		}
		else
		{
			mergedProfile = this.profile;
		}

		List<String> mergedExtendsProfiles;
		if (other.extendsProfiles.isEmpty())
		{
			mergedExtendsProfiles = this.extendsProfiles;
		}
		else
		{
			mergedExtendsProfiles = other.extendsProfiles;
		}

		GlobalConfiguration mergedGlobal;
		if (other.global != null)
		{
			mergedGlobal = other.global;
		}
		else
		{
			mergedGlobal = this.global;
		}

		return new ConfigurationSchema(
			mergedVersion,
			mergedProfile,
			mergedExtendsProfiles,
			mergedGlobal,
			mergedRules);
	}

	/**
	 * Gets the configuration for a specific rule.
	 *
	 * @param ruleName the name of the rule, never {@code null}
	 * @param configClass the configuration class type, never {@code null}
	 * @param <T> the configuration type
	 * @return the rule configuration, or {@code null} if not configured
	 * @throws ConfigurationException if the configuration cannot be converted
	  * @throws NullPointerException if {@code version} is null
	  * @throws NullPointerException if {@code ruleName} is null
	  * @throws NullPointerException if {@code configClass} is null
	 */
	public <T extends RuleConfiguration> T getRuleConfiguration(String ruleName, Class<T> configClass)
		throws ConfigurationException
	{
		requireThat(ruleName, "ruleName").isNotNull();
		requireThat(configClass, "configClass").isNotNull();

		Map<String, Object> ruleData = rules.get(ruleName);
		if (ruleData == null || ruleData.isEmpty())
		{
			return null;
		}

		try
		{
			return TOML_MAPPER.convertValue(ruleData, configClass);
		}
		catch (IllegalArgumentException e)
		{
			throw new ConfigurationException("Failed to convert configuration for rule '" + ruleName + "'", e);
		}
	}

	public String getVersion()
	{ return version; }
	public String getProfile()
	{ return profile; }
	public List<String> getExtendsProfiles()
	{ return extendsProfiles; }
	public GlobalConfiguration getGlobal()
	{ return global; }
	public Map<String, Map<String, Object>> getRules()
	{ return rules; }

	/**
	 * Validates the entire configuration schema.
	 *
	 * @throws ConfigurationException if validation fails
	  * @throws NullPointerException if {@code version} is null
	  * @throws NullPointerException if {@code ruleName} is null
	 */
	private void validate() throws ConfigurationException
	{
		// Validate extends profiles
		for (String extendProfile : extendsProfiles)
		{
			validateProfile(extendProfile);
		}

		// Validate global configuration
		if (global != null)
		{
			global.validate();
		}

		// Validate individual rule configurations
		for (Map.Entry<String, Map<String, Object>> entry : rules.entrySet())
		{
			validateRuleName(entry.getKey());
			validateRuleConfiguration(entry.getKey(), entry.getValue());
		}
	}

	private static String validateVersion(String version)
	{
		requireThat(version, "version").isNotNull();
		requireThat(version.trim(), "version").isNotEmpty();

		if (!version.matches("^\\d+\\.\\d+$"))
		{
			throw new IllegalArgumentException("Version must follow format 'major.minor': " + version);
		}

		return version.trim();
	}

	private static String validateProfile(String profile)
	{
		if (profile == null) return null;

		requireThat(profile.trim(), "profile").isNotEmpty();

		if (!profile.matches("^[a-zA-Z][a-zA-Z0-9\\-_]*$"))
		{
			throw new IllegalArgumentException(
				"Profile name must contain only letters, numbers, hyphens, and underscores: " + profile);
		}

		if (profile.length() > 50)
		{
			throw new IllegalArgumentException("Profile name exceeds maximum length of 50 characters");
		}

		return profile.trim();
	}

	private static void validateRuleName(String ruleName)
	{
		requireThat(ruleName, "ruleName").isNotNull();
		requireThat(ruleName.trim(), "ruleName").isNotEmpty();

		if (!ruleName.matches("^[a-zA-Z][a-zA-Z0-9\\-_\\.]*$"))
		{
			throw new IllegalArgumentException("Rule name must follow Java identifier conventions: " + ruleName);
		}

		if (ruleName.length() > 100)
		{
			throw new IllegalArgumentException("Rule name exceeds maximum length of 100 characters");
		}
	}

	private static void validateRuleConfiguration(String ruleName, Map<String, Object> config)
	{
		if (config == null) return;

		for (Map.Entry<String, Object> entry : config.entrySet())
		{
			String paramName = entry.getKey();

			if (paramName == null || paramName.isBlank())
			{
				throw new IllegalArgumentException("Rule '" + ruleName + "' has empty parameter name");
			}

			// Security validation for parameter values
			Object paramValue = entry.getValue();
			if (paramValue instanceof String stringValue &&
				(stringValue.contains("${") || stringValue.contains("#{") || stringValue.contains("<%")))
			{
				throw new SecurityException("Rule '" + ruleName + "' parameter '" + paramName +
				                          "' contains potentially dangerous content");
			}
		}
	}

	private static void validateTomlContent(String content)
	{
		// Security checks
		// TOML doesn't support object instantiation like YAML
		// Basic content length validation is sufficient for security
		if (content.contains("${{") || content.contains("#{") || content.contains("<%"))
		{
			throw new SecurityException("TOML content contains potentially dangerous template expressions");
		}

		if (content.length() > 10 * 1024 * 1024) // 10MB limit
		{
			throw new SecurityException("TOML content exceeds maximum size limit");
		}
	}

	private static ObjectMapper createTomlMapper()
	{
		TomlFactory tomlFactory = new TomlFactory();

		ObjectMapper mapper = new ObjectMapper(tomlFactory);
		mapper.findAndRegisterModules();
		return mapper;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		ConfigurationSchema that = (ConfigurationSchema) obj;
		return Objects.equals(version, that.version) &&
		       Objects.equals(profile, that.profile) &&
		       Objects.equals(extendsProfiles, that.extendsProfiles) &&
		       Objects.equals(global, that.global) &&
		       Objects.equals(rules, that.rules);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(version, profile, extendsProfiles, global, rules);
	}

	@Override
	public String toString()
	{
		return "ConfigurationSchema{" +
		       "version='" + version + '\'' +
		       ", profile='" + profile + '\'' +
		       ", extends=" + extendsProfiles +
		       ", rules=" + rules.keySet() +
		       '}';
	}
}