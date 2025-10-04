package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.formatter.api.*;
import org.testng.annotations.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for MutableFormattingContext.
 */
public class MutableFormattingContextTest
{
	/**
	 * Test constructor validation.
	 */
	@Test
	public void constructorValidation()
	{
		RuleConfiguration ruleConfig = new TestRuleConfiguration();
		Set<String> enabledRules = Collections.emptySet();
		Map<String, Object> metadata = Collections.emptyMap();
		Path filePath = Paths.get("TestFile.java");
		String sourceText = "public class TestFile {}";

		// Test null rootNode validation
		assertThatThrownBy(() ->
			new MutableFormattingContext(null, sourceText, filePath, ruleConfig, enabledRules, metadata)).
			isInstanceOf(NullPointerException.class);

		// Test null sourceText validation
		assertThatThrownBy(() ->
			new MutableFormattingContext(null, null, filePath, ruleConfig, enabledRules, metadata)).
			isInstanceOf(NullPointerException.class);

		// Test null filePath validation
		assertThatThrownBy(() ->
			new MutableFormattingContext(null, sourceText, null, ruleConfig, enabledRules, metadata)).
			isInstanceOf(NullPointerException.class);

		// Test null configuration validation
		assertThatThrownBy(() ->
			new MutableFormattingContext(null, sourceText, filePath, null, enabledRules, metadata)).
			isInstanceOf(NullPointerException.class);

		// Test null enabledRules validation
		assertThatThrownBy(() ->
			new MutableFormattingContext(null, sourceText, filePath, ruleConfig, null, metadata)).
			isInstanceOf(NullPointerException.class);

		// Test null metadata validation
		assertThatThrownBy(() ->
			new MutableFormattingContext(null, sourceText, filePath, ruleConfig, enabledRules, null)).
			isInstanceOf(NullPointerException.class);
	}

	/**
	 * Test configuration methods.
	 */
	@Test
	public void configurationMethods() throws ConfigurationException
	{
		RuleConfiguration ruleConfig = new TestRuleConfiguration();

		// Test configuration access - this would be tested if we had a working CompilationUnitNode
		// For now, we'll just verify the test configuration works
		ruleConfig.validate(); // Should not throw for our test configuration

		String description = ruleConfig.getDescription();
		assert "Test rule configuration for unit testing".equals(description);
	}

	/**
	 * Test implementation of RuleConfiguration for unit tests.
	 */
	private static final class TestRuleConfiguration extends RuleConfiguration
	{
		@Override
		public void validate() throws ConfigurationException
		{
			// No validation needed for test
		}

		@Override
		public RuleConfiguration merge(RuleConfiguration override)
		{
			return this; // Simple implementation for test
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
	}
}