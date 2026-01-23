package io.github.cowwoc.styler.formatter;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * A configurable property for a formatting rule.
 * <p>
 * Describes a single configuration option that controls rule behavior.
 *
 * @param name         the property name (e.g., "braceStyle")
 * @param type         the property type (e.g., "BraceStyle")
 * @param defaultValue the default value (e.g., "NEW_LINE")
 * @param description  a brief description of what this property controls
 * @throws NullPointerException     if any argument is null
 * @throws IllegalArgumentException if any argument is empty
 */
public record RuleProperty(String name, String type, String defaultValue, String description)
{
	/**
	 * Creates a rule property.
	 *
	 * @param name         the property name (e.g., "braceStyle")
	 * @param type         the property type (e.g., "BraceStyle")
	 * @param defaultValue the default value (e.g., "NEW_LINE")
	 * @param description  a brief description of what this property controls
	 * @throws NullPointerException     if any argument is null
	 * @throws IllegalArgumentException if any argument is empty
	 */
	public RuleProperty
	{
		requireThat(name, "name").isNotEmpty();
		requireThat(type, "type").isNotEmpty();
		requireThat(defaultValue, "defaultValue").isNotEmpty();
		requireThat(description, "description").isNotEmpty();
	}
}
