package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.formatter.api.*;

/**
 * Test implementation of RuleConfiguration for unit testing.
 */
public class TestRuleConfiguration extends RuleConfiguration
{
	@Override
	public void validate() throws ConfigurationException
	{
		// No validation needed for test configuration
	}

	@Override
	public RuleConfiguration merge(RuleConfiguration override)
	{
		// Simple merge - return the override if it's the same type, otherwise return this
		if (override instanceof TestRuleConfiguration)
		{
			return override;
		}
		return this;
	}

	@Override
	public String getDescription()
	{
		return "Test rule configuration for unit testing";
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof TestRuleConfiguration;
	}

	@Override
	public int hashCode()
	{
		return TestRuleConfiguration.class.hashCode();
	}

	@Override
	public String toString()
	{
		return "TestRuleConfiguration[]";
	}
}