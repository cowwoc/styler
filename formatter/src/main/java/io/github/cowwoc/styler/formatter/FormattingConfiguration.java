package io.github.cowwoc.styler.formatter;

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
}
