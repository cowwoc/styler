package io.github.cowwoc.styler.formatter;

import java.util.List;

/**
 * Base interface for rule-specific configuration.
 * Implementations must be immutable after construction.
 */
public interface FormattingConfiguration
{
	/**
	 * Validates this configuration and returns any error messages.
	 * Called before rule execution to ensure configuration is valid.
	 *
	 * @return an empty list if the configuration is valid
	 */
	List<String> validate();

	/**
	 * Returns the rule ID this configuration applies to.
	 *
	 * @return the rule identifier
	 */
	String ruleId();
}
