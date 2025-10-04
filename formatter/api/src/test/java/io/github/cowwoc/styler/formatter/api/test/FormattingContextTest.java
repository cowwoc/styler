package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.formatter.api.*;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the FormattingContext class, focusing on security features.
 */
public class FormattingContextTest
{
	/**
	 * Test valid context creation.
	 */
	@Test
	public void validContextCreation()
	{
		// Create a minimal test configuration
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		Path filePath = Paths.get("/project/src/Example.java");
		Set<String> enabledRules = Set.of("rule1", "rule2");
		Map<String, Object> metadata = Map.of("key1", "value1");

		FormattingContext context = new FormattingContext(
			ast, "public class Example {}", filePath, config, enabledRules, metadata);

		assertThat(context.getRootNode()).isEqualTo(ast);
		assertThat(context.getSourceText()).isEqualTo("public class Example {}");
		assertThat(context.getFilePath()).isEqualTo(filePath.normalize().toAbsolutePath());
		assertThat(context.getConfiguration()).isEqualTo(config);
		assertThat(context.getEnabledRules()).containsExactlyInAnyOrder("rule1", "rule2");
		assertThat(context.getMetadata()).containsEntry("key1", "value1");
	}

	/**
	 * Test directory traversal prevention.
	 */
	@Test
	public void directoryTraversalPrevention()
	{
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		Path maliciousPath = Paths.get("/project/../../../etc/passwd");
		Set<String> enabledRules = Set.of();
		Map<String, Object> metadata = Map.of();

		assertThatThrownBy(() -> new FormattingContext(
			ast, "source", maliciousPath, config, enabledRules, metadata)).
			isInstanceOf(SecurityException.class).
			hasMessageContaining("Directory traversal not allowed");
	}

	/**
	 * Test path normalization.
	 */
	@Test
	public void pathNormalization()
	{
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		Path unnormalizedPath = Paths.get("/project/src/./sub/../Example.java");
		Set<String> enabledRules = Set.of();
		Map<String, Object> metadata = Map.of();

		FormattingContext context = new FormattingContext(
			ast, "source", unnormalizedPath, config, enabledRules, metadata);

		// Path should be normalized and absolute
		assertThat(context.getFilePath()).isAbsolute();
		assertThat(context.getFilePath().toString()).doesNotContain("..");
		assertThat(context.getFilePath().toString()).doesNotContain("./");
	}

	/**
	 * Test immutable collections.
	 */
	@Test
	public void immutableCollections()
	{
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		Path filePath = Paths.get("/project/src/Example.java");
		Set<String> enabledRules = Set.of("rule1");
		Map<String, Object> metadata = Map.of("key1", "value1");

		FormattingContext context = new FormattingContext(
			ast, "source", filePath, config, enabledRules, metadata);

		// Collections should be immutable
		assertThatThrownBy(() -> context.getEnabledRules().add("rule2")).
			isInstanceOf(UnsupportedOperationException.class);

		assertThatThrownBy(() -> context.getMetadata().put("key2", "value2")).
			isInstanceOf(UnsupportedOperationException.class);
	}

	/**
	 * Test rule enabled check.
	 */
	@Test
	public void ruleEnabledCheck()
	{
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		Path filePath = Paths.get("/project/src/Example.java");
		Set<String> enabledRules = Set.of("rule1", "rule2");
		Map<String, Object> metadata = Map.of();

		FormattingContext context = new FormattingContext(
			ast, "source", filePath, config, enabledRules, metadata);

		assertThat(context.isRuleEnabled("rule1")).isTrue();
		assertThat(context.isRuleEnabled("rule2")).isTrue();
		assertThat(context.isRuleEnabled("rule3")).isFalse();
	}

	/**
	 * Test typed metadata access.
	 */
	@Test
	public void typedMetadataAccess()
	{
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		Path filePath = Paths.get("/project/src/Example.java");
		Set<String> enabledRules = Set.of();
		Map<String, Object> metadata = Map.of(
			"stringValue", "test",
			"intValue", 42,
			"boolValue", true);

		FormattingContext context = new FormattingContext(
			ast, "source", filePath, config, enabledRules, metadata);

		assertThat(context.getMetadata("stringValue", String.class)).isEqualTo("test");
		assertThat(context.getMetadata("intValue", Integer.class)).isEqualTo(42);
		assertThat(context.getMetadata("boolValue", Boolean.class)).isTrue();
		assertThat(context.getMetadata("nonexistent", String.class)).isNull();

		// Test type mismatch
		assertThatThrownBy(() -> context.getMetadata("stringValue", Integer.class)).
			isInstanceOf(ClassCastException.class);
	}

	/**
	 * Test default wrap configuration.
	 */
	@Test
	public void defaultWrapConfiguration()
	{
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		Path filePath = Paths.get("/project/src/Example.java");
		Set<String> enabledRules = Set.of();
		Map<String, Object> metadata = Map.of();

		FormattingContext context = new FormattingContext(
			ast, "source", filePath, config, enabledRules, metadata);

		// Should have default wrap configuration
		assertThat(context.getWrapConfiguration()).isNotNull();
		assertThat(context.getWrapConfiguration().getMaxLineLength()).isEqualTo(120);
		assertThat(context.getWrapConfiguration().isWrapBeforeOperator()).isTrue();
		assertThat(context.getWrapConfiguration().isWrapBeforeDot()).isTrue();
	}

	/**
	 * Test explicit wrap configuration.
	 */
	@Test
	public void explicitWrapConfiguration() throws ConfigurationException
	{
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		Path filePath = Paths.get("/project/src/Example.java");
		Set<String> enabledRules = Set.of();
		Map<String, Object> metadata = Map.of();

		WrapConfiguration wrapConfig = WrapConfiguration.builder().
			withMaxLineLength(100).
			withWrapBeforeOperator(false).
			build();

		FormattingContext context = new FormattingContext(
			ast, "source", filePath, config, enabledRules, metadata, wrapConfig);

		// Should use provided wrap configuration
		assertThat(context.getWrapConfiguration()).isSameAs(wrapConfig);
		assertThat(context.getWrapConfiguration().getMaxLineLength()).isEqualTo(100);
		assertThat(context.getWrapConfiguration().isWrapBeforeOperator()).isFalse();
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
			return "Test configuration";
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