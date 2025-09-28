package io.github.cowwoc.styler.formatter.api;

import io.github.cowwoc.styler.ast.CompilationUnit;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
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
	@Test
	public void testValidContextCreation()
	{
		// Create a minimal test configuration
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnit ast = createTestAST();
		Path filePath = Paths.get("/project/src/Example.java");
		Set<String> enabledRules = Set.of("rule1", "rule2");
		Map<String, Object> metadata = Map.of("key1", "value1");

		FormattingContext context = new FormattingContext(
			ast, "public class Example {}", filePath, config, enabledRules, metadata
		);

		assertThat(context.getRootNode()).isEqualTo(ast);
		assertThat(context.getSourceText()).isEqualTo("public class Example {}");
		assertThat(context.getFilePath()).isEqualTo(filePath.normalize().toAbsolutePath());
		assertThat(context.getConfiguration()).isEqualTo(config);
		assertThat(context.getEnabledRules()).containsExactlyInAnyOrder("rule1", "rule2");
		assertThat(context.getMetadata()).containsEntry("key1", "value1");
	}

	@Test
	public void testDirectoryTraversalPrevention()
	{
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnit ast = createTestAST();
		Path maliciousPath = Paths.get("/project/../../../etc/passwd");
		Set<String> enabledRules = Set.of();
		Map<String, Object> metadata = Map.of();

		assertThatThrownBy(() -> new FormattingContext(
			ast, "source", maliciousPath, config, enabledRules, metadata
		))
			.isInstanceOf(SecurityException.class)
			.hasMessageContaining("Directory traversal not allowed");
	}

	@Test
	public void testPathNormalization()
	{
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnit ast = createTestAST();
		Path unnormalizedPath = Paths.get("/project/src/./sub/../Example.java");
		Set<String> enabledRules = Set.of();
		Map<String, Object> metadata = Map.of();

		FormattingContext context = new FormattingContext(
			ast, "source", unnormalizedPath, config, enabledRules, metadata
		);

		// Path should be normalized and absolute
		assertThat(context.getFilePath()).isAbsolute();
		assertThat(context.getFilePath().toString()).doesNotContain("..");
		assertThat(context.getFilePath().toString()).doesNotContain("./");
	}

	@Test
	public void testImmutableCollections()
	{
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnit ast = createTestAST();
		Path filePath = Paths.get("/project/src/Example.java");
		Set<String> enabledRules = Set.of("rule1");
		Map<String, Object> metadata = Map.of("key1", "value1");

		FormattingContext context = new FormattingContext(
			ast, "source", filePath, config, enabledRules, metadata
		);

		// Collections should be immutable
		assertThatThrownBy(() -> context.getEnabledRules().add("rule2"))
			.isInstanceOf(UnsupportedOperationException.class);

		assertThatThrownBy(() -> context.getMetadata().put("key2", "value2"))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void testRuleEnabledCheck()
	{
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnit ast = createTestAST();
		Path filePath = Paths.get("/project/src/Example.java");
		Set<String> enabledRules = Set.of("rule1", "rule2");
		Map<String, Object> metadata = Map.of();

		FormattingContext context = new FormattingContext(
			ast, "source", filePath, config, enabledRules, metadata
		);

		assertThat(context.isRuleEnabled("rule1")).isTrue();
		assertThat(context.isRuleEnabled("rule2")).isTrue();
		assertThat(context.isRuleEnabled("rule3")).isFalse();
	}

	@Test
	public void testTypedMetadataAccess()
	{
		RuleConfiguration config = new TestRuleConfiguration();
		CompilationUnit ast = createTestAST();
		Path filePath = Paths.get("/project/src/Example.java");
		Set<String> enabledRules = Set.of();
		Map<String, Object> metadata = Map.of(
			"stringValue", "test",
			"intValue", 42,
			"boolValue", true
		);

		FormattingContext context = new FormattingContext(
			ast, "source", filePath, config, enabledRules, metadata
		);

		assertThat(context.getMetadata("stringValue", String.class)).isEqualTo("test");
		assertThat(context.getMetadata("intValue", Integer.class)).isEqualTo(42);
		assertThat(context.getMetadata("boolValue", Boolean.class)).isTrue();
		assertThat(context.getMetadata("nonexistent", String.class)).isNull();

		// Test type mismatch
		assertThatThrownBy(() -> context.getMetadata("stringValue", Integer.class))
			.isInstanceOf(ClassCastException.class);
	}

	/**
	 * Creates a minimal CompilationUnit for testing.
	 */
	private CompilationUnit createTestAST()
	{
		SourceRange range = new SourceRange(
			new SourcePosition(1, 1),
			new SourcePosition(1, 10)
		);
		return new CompilationUnit.Builder()
			.range(range)
			.build();
	}

	/**
	 * Test implementation of RuleConfiguration for unit tests.
	 */
	private static class TestRuleConfiguration extends RuleConfiguration
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