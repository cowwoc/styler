package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.ast.FormattingHints;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.WhitespaceInfo;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.MutableFormattingContext;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ConfigurationException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Shared test utilities for formatter-api unit tests.
 * <p>
 * All methods are stateless and parallel-safe for concurrent test execution.
 */
public final class TestUtilities
{
	private TestUtilities()
	{
		// Prevent instantiation
	}

	/**
	 * Creates a minimal valid CompilationUnitNode for testing.
	 * <p>
	 * Pattern from FormattingContextTest.createTestAST()
	 *
	 * @return a minimal test AST
	 */
	public static CompilationUnitNode createTestAST()
	{
		SourcePosition start = new SourcePosition(1, 1);
		SourcePosition end = new SourcePosition(1, 20);
		SourceRange range = new SourceRange(start, end);

		return new CompilationUnitNode(
			range,
			List.of(), // imports
			List.of(), // typeDeclarations
			new WhitespaceInfo(0, 0, 0, 0, false),
			new FormattingHints(false, Optional.empty(), Optional.empty(), Map.of()),
			Optional.empty(), // packageDeclaration
			Optional.empty(), // moduleDeclaration
			List.of(), // leadingComments
			List.of()); // trailingComments
	}

	/**
	 * Creates a FormattingContext for testing with default values.
	 *
	 * @param ast the AST root node
	 * @param sourceText the source text
	 * @return a test FormattingContext
	 */
	public static FormattingContext createTestContext(CompilationUnitNode ast, String sourceText)
	{
		Path filePath = Paths.get("/test/Example.java");
		RuleConfiguration config = createTestConfiguration();
		Set<String> enabledRules = Set.of("test-rule");
		Map<String, Object> metadata = Map.of();

		return new FormattingContext(ast, sourceText, filePath, config, enabledRules, metadata);
	}

	/**
	 * Creates a MutableFormattingContext for testing AST modifications.
	 *
	 * @param ast the AST root node
	 * @param sourceText the source text
	 * @return a test MutableFormattingContext
	 */
	public static MutableFormattingContext createMutableTestContext(CompilationUnitNode ast,
		String sourceText)
	{
		Path filePath = Paths.get("/test/Example.java");
		RuleConfiguration config = createTestConfiguration();
		Set<String> enabledRules = Set.of("test-rule");
		Map<String, Object> metadata = Map.of();

		return new MutableFormattingContext(ast, sourceText, filePath, config, enabledRules,
			metadata);
	}

	/**
	 * Creates a test RuleConfiguration instance.
	 * <p>
	 * Follows pattern from FormattingContextTest.TestRuleConfiguration
	 *
	 * @return a test configuration
	 */
	public static RuleConfiguration createTestConfiguration()
	{
		return new TestRuleConfiguration();
	}

	/**
	 * Test implementation of RuleConfiguration.
	 */
	private static final class TestRuleConfiguration extends RuleConfiguration
	{
		@Override
		public void validate() throws ConfigurationException
		{
			// No validation for test configuration
		}

		@Override
		public RuleConfiguration merge(RuleConfiguration override)
		{
			return new TestRuleConfiguration();
		}

		@Override
		public String getDescription()
		{
			return "Test configuration";
		}

		@Override
		public boolean equals(Object other)
		{
			return other instanceof TestRuleConfiguration;
		}

		@Override
		public int hashCode()
		{
			return 1;
		}
	}
}
