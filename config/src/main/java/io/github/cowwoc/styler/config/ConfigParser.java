package io.github.cowwoc.styler.config;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.toml.TomlMapper;
import io.github.cowwoc.styler.config.exception.ConfigurationSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Parser for TOML configuration files.
 * <p>
 * This class converts TOML files into {@link ConfigBuilder} instances using Jackson's TOML parser.
 * It handles syntax errors (invalid TOML) by wrapping them in {@link ConfigurationSyntaxException}.
 * Validation is deferred until {@link ConfigBuilder#build()} is called.
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 */
public final class ConfigParser
{
	/**
	 * Maximum configuration file size (1MB) to prevent memory exhaustion.
	 */
	private static final long MAX_FILE_SIZE_BYTES = 1024 * 1024;

	private final ObjectMapper mapper;

	/**
	 * Creates a new TOML configuration parser.
	 */
	public ConfigParser()
	{
		// Register custom deserializer to capture line numbers
		SimpleModule module = new SimpleModule();
		module.addDeserializer(ConfigBuilder.class, new ConfigBuilderDeserializer());

		this.mapper = TomlMapper.builder()
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.addModule(module)
			.build();
	}

	/**
	 * Parses a TOML configuration file.
	 *
	 * @param path the path to the TOML file (must exist and be readable)
	 * @return the parsed configuration builder (validation deferred until build())
	 * @throws NullPointerException         if {@code path} is null
	 * @throws ConfigurationSyntaxException if the TOML syntax is invalid
	 */
	public ConfigBuilder parse(Path path) throws ConfigurationSyntaxException
	{
		try
		{
			// Check file size to prevent memory exhaustion (security)
			long fileSize = Files.size(path);
			if (fileSize > MAX_FILE_SIZE_BYTES)
				throw new ConfigurationSyntaxException(
					"Configuration file too large: " + fileSize + " bytes (max: " +
						MAX_FILE_SIZE_BYTES + " bytes) at " + path);

			String content = Files.readString(path);
			return parseToml(content, path.toString());
		}
		catch (IOException e)
		{
			throw new ConfigurationSyntaxException(
				"Failed to read TOML file: " + path + " - " + e.getMessage(), e);
		}
	}

	/**
	 * Parses TOML configuration from a string.
	 *
	 * @param toml   the TOML content
	 * @param source a description of where this TOML came from (for error messages)
	 * @return the parsed configuration builder (validation deferred until build())
	 * @throws NullPointerException         if {@code toml} or {@code source} is null
	 * @throws ConfigurationSyntaxException if the TOML syntax is invalid
	 */
	public ConfigBuilder parseToml(String toml, String source) throws ConfigurationSyntaxException
	{
		requireThat(toml, "toml").isNotNull();
		requireThat(source, "source").isNotNull();

		try
		{
			return mapper.readValue(toml, ConfigBuilder.class);
		}
		catch (JacksonException e)
		{
			throw new ConfigurationSyntaxException(
				"Invalid TOML syntax in " + source + ": " + e.getMessage(), e);
		}
	}
}
