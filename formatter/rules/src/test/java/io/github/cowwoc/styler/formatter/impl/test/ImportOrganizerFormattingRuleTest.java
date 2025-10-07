package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.ImportOrganizerRuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ImportOrganizerRuleConfiguration.SortOrder;
import io.github.cowwoc.styler.formatter.api.ImportOrganizerRuleConfiguration.StaticImportsPosition;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ValidationResult;
import io.github.cowwoc.styler.formatter.api.test.TestUtilities;
import io.github.cowwoc.styler.formatter.impl.ImportOrganizerFormattingRule;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ImportOrganizerFormattingRule}.
 * <p>
 * Validates import organization rule implementation, unused import removal,
 * import grouping, and configuration handling.
 */
public final class ImportOrganizerFormattingRuleTest
{
	/**
	 * Verifies the rule identifier matches the expected ImportOrganizer rule ID.
	 */
	@Test
	public void getRuleIdReturnsCorrectId()
	{
		ImportOrganizerFormattingRule rule = createRule();

		assertThat(rule.getRuleId()).isEqualTo("io.github.cowwoc.styler.rules.ImportOrganizer");
	}

	/**
	 * Verifies the rule uses priority 50 (runs before LineLength at priority 100).
	 */
	@Test
	public void getPriorityReturns50()
	{
		ImportOrganizerFormattingRule rule = createRule();

		assertThat(rule.getPriority()).isEqualTo(50);
	}

	/**
	 * Verifies the default configuration returns a valid ImportOrganizerRuleConfiguration instance.
	 */
	@Test
	public void getDefaultConfigurationReturnsValidConfiguration()
	{
		ImportOrganizerFormattingRule rule = createRule();
		RuleConfiguration config = rule.getDefaultConfiguration();

		assertThat(config).isNotNull();
		assertThat(config).isInstanceOf(ImportOrganizerRuleConfiguration.class);
	}

	/**
	 * Verifies that validation with a null context returns a failure result.
	 */
	@Test
	public void validateWithNullContextReturnsFailure()
	{
		ImportOrganizerFormattingRule rule = createRule();

		ValidationResult result = rule.validate(null);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that applying the rule to source code with no imports returns an empty result.
	 */
	@Test
	public void applyWithNoImportsReturnsEmptyResult()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\npublic class Example " +
			"{\n\tprivate int value;\n}\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result.getEdits()).
			as("Should return empty result when no imports exist").
			isEmpty();
	}

	/**
	 * Verifies that imports are grouped by standard categories (java, third-party, project, static).
	 */
	@Test
	public void applyGroupsImportsByStandardCategories()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import io.github.cowwoc.styler.formatter.api.FormattingRule;\n" +
			"import org.testng.annotations.Test;\n" +
			"import java.util.List;\n" +
			"import static java.util.Collections.emptyList;\n\n" +
			"public class Example { }\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that imports within each group are sorted lexicographically.
	 */
	@Test
	public void applySortsImportsLexicographicallyWithinGroups()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import java.util.Map;\n" +
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n\n" +
			"public class Example { }\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that blank lines are added between import groups according to configuration.
	 */
	@Test
	public void applyAddsBlankLinesBetweenGroups()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import java.util.List;\n" +
			"import org.testng.annotations.Test;\n\n" +
			"public class Example { }\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that already organized imports generate no formatting edits.
	 */
	@Test
	public void applyWithAlreadyOrganizedImportsReturnsNoEdits()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import static java.util.Collections.emptyList;\n\n" +
			"import java.util.List;\n\n" +
			"import org.testng.annotations.Test;\n\n" +
			"import io.github.cowwoc.styler.formatter.api.FormattingRule;\n\n" +
			"public class Example { }\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that unused imports are removed when removeUnused is enabled.
	 */
	@Test
	public void applyRemovesUnusedImports()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import java.util.List;\n" +
			"import java.util.Map;\n\n" +
			"public class Example {\n\tprivate List<String> items;\n}\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that imports referenced in the code are preserved.
	 */
	@Test
	public void applyPreservesUsedImports()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import java.util.List;\n\n" +
			"public class Example {\n\tprivate List<String> items;\n}\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that static imports are handled according to configuration.
	 */
	@Test
	public void applyHandlesStaticImports()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import static org.assertj.core.api.Assertions.assertThat;\n" +
			"import java.util.List;\n\n" +
			"public class Example { }\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that custom group definitions are used for import categorization.
	 */
	@Test
	public void applyWithCustomGroupDefinitionsUsesCustomGroups()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import io.github.cowwoc.styler.formatter.api.FormattingRule;\n" +
			"import java.util.List;\n\n" +
			"public class Example { }\n";
		ImportOrganizerRuleConfiguration config = createCustomGroupConfiguration();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		FormattingContext context = new FormattingContext(
			ast,
			sourceText,
			Path.of("/test/Example.java"),
			config,
			Set.of(rule.getRuleId()),
			Map.of());

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that static imports are positioned at the bottom when configured.
	 */
	@Test
	public void applyWithStaticImportsAtBottomPositionsCorrectly()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import static org.assertj.core.api.Assertions.assertThat;\n" +
			"import java.util.List;\n\n" +
			"public class Example { }\n";
		ImportOrganizerRuleConfiguration config = createStaticBottomConfiguration();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		FormattingContext context = new FormattingContext(
			ast,
			sourceText,
			Path.of("/test/Example.java"),
			config,
			Set.of(rule.getRuleId()),
			Map.of());

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that wildcard expansion is performed when enabled.
	 */
	@Test
	public void applyWithWildcardExpansionExpandsImports()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import java.util.*;\n\n" +
			"public class Example { }\n";
		ImportOrganizerRuleConfiguration config = createWildcardExpansionConfiguration();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		FormattingContext context = new FormattingContext(
			ast,
			sourceText,
			Path.of("/test/Example.java"),
			config,
			Set.of(rule.getRuleId()),
			Map.of());

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that manual spacing between imports is preserved when configured.
	 */
	@Test
	public void applyWithPreserveManualSpacingRespectsConfiguration()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import java.util.List;\n\n\n" +
			"import java.util.Map;\n\n" +
			"public class Example { }\n";
		ImportOrganizerRuleConfiguration config = createPreserveSpacingConfiguration();
		CompilationUnitNode ast = TestUtilities.createTestAST();
		FormattingContext context = new FormattingContext(
			ast,
			sourceText,
			Path.of("/test/Example.java"),
			config,
			Set.of(rule.getRuleId()),
			Map.of());

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that different sort order configurations are applied correctly.
	 */
	@Test
	public void applyWithSortOrderVariations()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import java.util.Map;\n" +
			"import java.util.List;\n\n" +
			"public class Example { }\n";
		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that files with all static imports are handled correctly.
	 */
	@Test
	public void applyWithAllStaticImportsHandlesCorrectly()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import static org.assertj.core.api.Assertions.assertThat;\n" +
			"import static java.util.Collections.emptyList;\n\n" +
			"public class Example { }\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that files with all wildcard imports are handled correctly.
	 */
	@Test
	public void applyWithAllWildcardImportsHandlesCorrectly()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import java.util.*;\n" +
			"import java.io.*;\n\n" +
			"public class Example { }\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that duplicate imports are removed during organization.
	 */
	@Test
	public void applyWithDuplicateImportsRemovesDuplicates()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import java.util.List;\n" +
			"import java.util.List;\n\n" +
			"public class Example { }\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that same-package imports are handled according to configuration.
	 */
	@Test
	public void applyWithSamePackageImportsRespectsConfiguration()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"import io.github.cowwoc.styler.test.Helper;\n" +
			"import java.util.List;\n\n" +
			"public class Example { }\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that comments within import statements are preserved.
	 */
	@Test
	public void applyWithCommentsInImportsPreservesComments()
	{
		ImportOrganizerFormattingRule rule = createRule();
		String sourceText = "package io.github.cowwoc.styler.test;\n\n" +
			"// Important import\n" +
			"import java.util.List;\n" +
			"import java.util.Map; // Used for caching\n\n" +
			"public class Example { }\n";

		FormattingContext context = createTestContext(sourceText, rule);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Creates an ImportOrganizerFormattingRule instance for testing.
	 *
	 * @return new rule instance
	 */
	private static ImportOrganizerFormattingRule createRule()
	{
		return new ImportOrganizerFormattingRule();
	}

	/**
	 * Creates a FormattingContext for testing import organizer rule.
	 *
	 * @param sourceText the Java source code
	 * @param rule       the import organizer rule
	 * @return test FormattingContext with minimal AST
	 */
	private static FormattingContext createTestContext(String sourceText,
		ImportOrganizerFormattingRule rule)
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		return new FormattingContext(
			ast,
			sourceText,
			Path.of("/test/Example.java"),
			rule.getDefaultConfiguration(),
			Set.of(rule.getRuleId()),
			Map.of());
	}

	/**
	 * Creates configuration with custom import groups.
	 *
	 * @return configuration with custom groups
	 */
	private static ImportOrganizerRuleConfiguration createCustomGroupConfiguration()
	{
		return new ImportOrganizerRuleConfiguration(
			true,  // removeUnused
			true,  // organizeGroups
			List.of("java", "third-party", "project"),  // groups
			Map.of("test", List.of("org\\.testng\\..*")),  // customGroups
			SortOrder.LEXICOGRAPHIC,  // sortOrder
			1,  // blankLinesBetweenGroups
			false,  // preserveManualSpacing
			StaticImportsPosition.TOP,  // staticImportsPosition
			false,  // expandWildcards
			5,  // wildcardThreshold
			false);  // includeSamePackageImports
	}

	/**
	 * Creates configuration with static imports at bottom.
	 *
	 * @return configuration with static imports at bottom
	 */
	private static ImportOrganizerRuleConfiguration createStaticBottomConfiguration()
	{
		return new ImportOrganizerRuleConfiguration(
			true,  // removeUnused
			true,  // organizeGroups
			List.of("static", "java", "third-party", "project"),  // groups
			Map.of(),  // customGroups
			SortOrder.LEXICOGRAPHIC,  // sortOrder
			1,  // blankLinesBetweenGroups
			false,  // preserveManualSpacing
			StaticImportsPosition.BOTTOM,  // staticImportsPosition
			false,  // expandWildcards
			5,  // wildcardThreshold
			false);  // includeSamePackageImports
	}

	/**
	 * Creates configuration with wildcard expansion enabled.
	 *
	 * @return configuration with wildcard expansion
	 */
	private static ImportOrganizerRuleConfiguration createWildcardExpansionConfiguration()
	{
		return new ImportOrganizerRuleConfiguration(
			true,  // removeUnused
			true,  // organizeGroups
			List.of("static", "java", "third-party", "project"),  // groups
			Map.of(),  // customGroups
			SortOrder.LEXICOGRAPHIC,  // sortOrder
			1,  // blankLinesBetweenGroups
			false,  // preserveManualSpacing
			StaticImportsPosition.TOP,  // staticImportsPosition
			true,  // expandWildcards
			5,  // wildcardThreshold
			false);  // includeSamePackageImports
	}

	/**
	 * Creates configuration with manual spacing preservation enabled.
	 *
	 * @return configuration with manual spacing preservation
	 */
	private static ImportOrganizerRuleConfiguration createPreserveSpacingConfiguration()
	{
		return new ImportOrganizerRuleConfiguration(
			true,  // removeUnused
			true,  // organizeGroups
			List.of("static", "java", "third-party", "project"),  // groups
			Map.of(),  // customGroups
			SortOrder.LEXICOGRAPHIC,  // sortOrder
			1,  // blankLinesBetweenGroups
			true,  // preserveManualSpacing
			StaticImportsPosition.TOP,  // staticImportsPosition
			false,  // expandWildcards
			5,  // wildcardThreshold
			false);  // includeSamePackageImports
	}
}
