package io.github.cowwoc.styler.config;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.io.Serial;

/**
 * Custom Jackson deserializer for {@link ConfigBuilder} that tracks line numbers.
 * <p>
 * This deserializer captures the line number where each field is defined in the TOML file,
 * enabling error messages to reference specific locations like {@code path@lineNumber}.
 */
final class ConfigBuilderDeserializer extends StdDeserializer<ConfigBuilder>
{
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new deserializer.
	 */
	ConfigBuilderDeserializer()
	{
		super(ConfigBuilder.class);
	}

	@Override
	public ConfigBuilder deserialize(JsonParser parser, DeserializationContext context)
	{
		try
		{
			ConfigBuilder builder = new ConfigBuilder();

			// Expect START_OBJECT token
			if (parser.currentToken() != JsonToken.START_OBJECT)
				throw context.wrongTokenException(parser, ConfigBuilder.class, JsonToken.START_OBJECT,
					"Expected START_OBJECT");

			// Read all fields
			while (parser.nextToken() != JsonToken.END_OBJECT)
			{
				String fieldName = parser.currentName();
				parser.nextToken(); // Move to value token

				if ("maxLineLength".equals(fieldName))
				{
					int value = parser.getIntValue();
					builder.maxLineLength(value);

					// Capture line number (currently returns -1 with Jackson TOML parser)
					int lineNumber = parser.currentTokenLocation().getLineNr();
					builder.setFieldLineNumber("maxLineLength", lineNumber);
				}
				else if (context.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES))
				{
					// Respect FAIL_ON_UNKNOWN_PROPERTIES setting
					context.handleUnknownProperty(parser, this, ConfigBuilder.class, fieldName);
				}
				// Otherwise ignore unknown fields (FAIL_ON_UNKNOWN_PROPERTIES disabled)
			}

			return builder;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to deserialize ConfigBuilder", e);
		}
	}
}
