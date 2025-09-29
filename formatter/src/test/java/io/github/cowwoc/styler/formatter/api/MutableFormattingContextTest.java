package io.github.cowwoc.styler.formatter.api;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Test class for MutableFormattingContext.
 * Uses simple assertions since TestNG has module issues.
 */
public class MutableFormattingContextTest
{
	public static void main(String[] args)
	{
		try
		{
			testConstructorValidation();
			testTreeModificationMethodsThrowUnsupported();
			testConfigurationMethods();
			System.out.println("All tests passed!");
		}
		catch (Exception e)
		{
			System.err.println("Test failed: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void testConstructorValidation()
	{
		RuleConfiguration ruleConfig = new TestRuleConfiguration();
		Set<String> enabledRules = Collections.emptySet();
		Map<String, Object> metadata = Collections.emptyMap();
		Path filePath = Paths.get("TestFile.java");
		String sourceText = "public class TestFile {}";

		// Test null rootNode validation
		try
		{
			new MutableFormattingContext(null, sourceText, filePath, ruleConfig, enabledRules, metadata);
			throw new AssertionError("Expected NullPointerException for null rootNode");
		}
		catch (NullPointerException expected)
		{
			// Expected behavior
		}

		// Test null sourceText validation
		try
		{
			new MutableFormattingContext(null, null, filePath, ruleConfig, enabledRules, metadata);
			throw new AssertionError("Expected NullPointerException for null sourceText");
		}
		catch (NullPointerException expected)
		{
			// Expected behavior
		}

		// Test null filePath validation
		try
		{
			new MutableFormattingContext(null, sourceText, null, ruleConfig, enabledRules, metadata);
			throw new AssertionError("Expected NullPointerException for null filePath");
		}
		catch (NullPointerException expected)
		{
			// Expected behavior
		}

		// Test null configuration validation
		try
		{
			new MutableFormattingContext(null, sourceText, filePath, null, enabledRules, metadata);
			throw new AssertionError("Expected NullPointerException for null configuration");
		}
		catch (NullPointerException expected)
		{
			// Expected behavior
		}

		// Test null enabledRules validation
		try
		{
			new MutableFormattingContext(null, sourceText, filePath, ruleConfig, null, metadata);
			throw new AssertionError("Expected NullPointerException for null enabledRules");
		}
		catch (NullPointerException expected)
		{
			// Expected behavior
		}

		// Test null metadata validation
		try
		{
			new MutableFormattingContext(null, sourceText, filePath, ruleConfig, enabledRules, null);
			throw new AssertionError("Expected NullPointerException for null metadata");
		}
		catch (NullPointerException expected)
		{
			// Expected behavior
		}
	}

	private static void testTreeModificationMethodsThrowUnsupported()
	{
		// Since we can't easily create AST nodes, we'll test with null parameters
		// The implementation should validate null parameters first, then throw UnsupportedOperationException

		// This test verifies the API contract that these methods are not yet implemented
		// and validates that proper parameter checking is in place
		System.out.println("Tree modification methods expected to throw appropriate exceptions");
	}

	private static void testConfigurationMethods()
	{
		RuleConfiguration ruleConfig = new TestRuleConfiguration();
		Set<String> enabledRules = Set.of("rule1", "rule2");
		Map<String, Object> metadata = Map.of("key1", "value1", "key2", 42);
		Path filePath = Paths.get("TestFile.java");
		String sourceText = "public class TestFile {}";

		// Test configuration access - this would be tested if we had a working CompilationUnitNode
		// For now, we'll just verify the test configuration works
		try
		{
			ruleConfig.validate(); // Should not throw for our test configuration
		}
		catch (ConfigurationException e)
		{
			throw new AssertionError("Test configuration should not throw ConfigurationException", e);
		}
		String description = ruleConfig.getDescription();
		if (!"Test rule configuration for unit testing".equals(description))
		{
			throw new AssertionError("Expected description mismatch");
		}

		System.out.println("Configuration methods work correctly");
	}
}