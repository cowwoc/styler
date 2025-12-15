package io.github.cowwoc.styler.formatter;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.util.List;

/**
 * Base interface for rule-specific configuration.
 * <p>
 * Implementations must be immutable and validate all fields in their constructors using fail-fast
 * validation. This ensures that invalid configurations cannot exist.
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface FormattingConfiguration
{
	/**
	 * Returns the rule ID this configuration applies to.
	 *
	 * @return the rule identifier
	 */
	String ruleId();

	/**
	 * Finds a configuration of the specified type from a list of configurations.
	 *
	 * @param <T>           the configuration type to find
	 * @param configs       the list of configurations to search
	 * @param configType    the class of the configuration type to find
	 * @param defaultConfig the default configuration to return if not found
	 * @return the first configuration matching {@code configType}, or {@code defaultConfig} if none
	 *         found
	 * @throws IllegalArgumentException if {@code configs} is null, {@code configType} is null,
	 *                                  {@code defaultConfig} is null, or if multiple configurations
	 *                                  of the same type are found
	 */
	static <T extends FormattingConfiguration> T findConfig(
		List<FormattingConfiguration> configs,
		Class<T> configType,
		T defaultConfig)
	{
		requireThat(configs, "configs").isNotNull();
		requireThat(configType, "configType").isNotNull();
		requireThat(defaultConfig, "defaultConfig").isNotNull();

		T found = null;
		for (FormattingConfiguration config : configs)
		{
			if (configType.isInstance(config))
			{
				if (found != null)
				{
					throw new IllegalArgumentException(
						"Multiple configurations of type " + configType.getName() + " found");
				}
				found = configType.cast(config);
			}
		}
		if (found != null)
		{
			return found;
		}
		return defaultConfig;
	}
}
