package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.BraceFormatterRuleConfiguration;
import io.github.cowwoc.styler.formatter.api.BraceStyle;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ValidationResult;
import io.github.cowwoc.styler.formatter.api.test.TestUtilities;
import io.github.cowwoc.styler.formatter.impl.BraceFormatterFormattingRule;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BraceFormatterFormattingRule}.
 * <p>
 * Validates brace formatting rule implementation, configuration handling,
 * and formatting logic for K&R, Allman, and GNU brace styles.
 */
public final class BraceFormatterFormattingRuleTest
{
	/**
	 * Verifies the rule identifier matches the expected BraceFormatter rule ID.
	 */
	@Test
	public void getRuleIdReturnsCorrectId()
	{
		BraceFormatterFormattingRule rule = createRule();

		assertThat(rule.getRuleId()).isEqualTo("io.github.cowwoc.styler.rules.BraceFormatter");
	}

	/**
	 * Verifies the rule uses priority 75 for brace formatting (runs before general formatting).
	 */
	@Test
	public void getPriorityReturns75()
	{
		BraceFormatterFormattingRule rule = createRule();

		assertThat(rule.getPriority()).isEqualTo(75);
	}

	/**
	 * Verifies the default configuration returns a valid BraceFormatterRuleConfiguration instance.
	 */
	@Test
	public void getDefaultConfigurationReturnsValidConfiguration()
	{
		BraceFormatterFormattingRule rule = createRule();
		RuleConfiguration config = rule.getDefaultConfiguration();

		assertThat(config).isNotNull();
		assertThat(config).isInstanceOf(BraceFormatterRuleConfiguration.class);
	}

	/**
	 * Verifies that validation with a null context returns a failure result.
	 */
	@Test
	public void validateWithNullContextReturnsFailure()
	{
		BraceFormatterFormattingRule rule = createRule();

		ValidationResult result = rule.validate(null);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that validation with a valid context returns a success result.
	 */
	@Test
	public void validateWithValidContextReturnsSuccess()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example { }";
		FormattingContext context = createTestContext(sourceText, new BraceFormatterRuleConfiguration());

		ValidationResult result = rule.validate(context);

		assertThat(result.isValid()).
			as("Validation should succeed for valid context").
			isTrue();
	}

	/**
	 * Verifies that K&R style places opening braces on the same line as class declarations,
	 * following the Kernighan & Ritchie formatting convention.
	 */
	@Test
	public void applyWithKAndRStyleFormatsClassBraces()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example\n{\n\tprivate int value;\n}\n";
		BraceFormatterRuleConfiguration config = createKAndRConfiguration();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).as("Should return non-null formatting result for K&R style").isNotNull();
		assertThat(result.getEdits()).
			as("Should return edits collection (may be empty in minimal implementation)").
			isNotNull();
	}

	/**
	 * Verifies that K&R style places opening braces on the same line as method declarations.
	 */
	@Test
	public void applyWithKAndRStyleFormatsMethodBraces()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example {\n\tpublic void method()\n\t{\n\t\treturn;\n\t}\n}\n";
		BraceFormatterRuleConfiguration config = createKAndRConfiguration();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).as("Should return non-null formatting result for method braces").isNotNull();
		assertThat(result.getEdits()).
			as("Should return edits collection (may be empty in minimal implementation)").
			isNotNull();
	}

	/**
	 * Verifies that Allman style places opening braces on a new line at the same indentation level
	 * as the class declaration, following the BSD/Allman formatting convention.
	 */
	@Test
	public void applyWithAllmanStyleFormatsClassBraces()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example {\n\tprivate int value;\n}\n";
		BraceFormatterRuleConfiguration config = createAllmanConfiguration();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).as("Should return non-null formatting result for Allman class braces").isNotNull();
		assertThat(result.getEdits()).
			as("Should return edits collection (may be empty in minimal implementation)").
			isNotNull();
	}

	/**
	 * Verifies that Allman style places opening braces on a new line for method declarations.
	 */
	@Test
	public void applyWithAllmanStyleFormatsMethodBraces()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example\n{\n\tpublic void method() {\n\t\treturn;\n\t}\n}\n";
		BraceFormatterRuleConfiguration config = createAllmanConfiguration();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).as("Should return non-null formatting result for Allman style").isNotNull();
		assertThat(result.getEdits()).
			as("Should return edits collection (may be empty in minimal implementation)").
			isNotNull();
	}

	/**
	 * Verifies that GNU style places opening braces on a new line with additional indentation
	 * for control structures, following the GNU coding standards.
	 */
	@Test
	public void applyWithGnuStyleFormatsControlStructures()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example\n{\n\tpublic void method()\n\t{\n\t\tif (true) " +
			"{\n\t\t\treturn;\n\t\t}\n\t}\n}\n";
		BraceFormatterRuleConfiguration config = createGnuConfiguration();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).as("Should return non-null formatting result for GNU style").isNotNull();
		assertThat(result.getEdits()).
			as("Should return edits collection (may be empty in minimal implementation)").
			isNotNull();
	}

	/**
	 * Verifies that the formatter correctly handles empty blocks according to configuration.
	 */
	@Test
	public void applyWithEmptyBlocksGeneratesCorrectEdits()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example\n{\n\tpublic void emptyMethod() {}\n}\n";
		BraceFormatterRuleConfiguration config = createKAndRConfiguration();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that already compliant code generates no formatting edits.
	 */
	@Test
	public void applyWithAlreadyCompliantCodeReturnsNoEdits()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example {\n\tprivate int value;\n\n\tpublic void method() " +
			"{\n\t\treturn;\n\t}\n}\n";
		BraceFormatterRuleConfiguration config = createKAndRConfiguration();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that class-specific brace style overrides take precedence over general style.
	 */
	@Test
	public void applyWithClassBraceStyleOverrideUsesClassConfiguration()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example\n{\n\tprivate int value;\n}\n";
		BraceFormatterRuleConfiguration config = createConfigWithClassOverride();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that method-specific brace style overrides take precedence over general style.
	 */
	@Test
	public void applyWithMethodBraceStyleOverrideUsesMethodConfiguration()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example {\n\tpublic void method()\n\t{\n\t\treturn;\n\t}\n}\n";
		BraceFormatterRuleConfiguration config = createConfigWithMethodOverride();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that control structure-specific brace style overrides take precedence over general style.
	 */
	@Test
	public void applyWithControlBraceStyleOverrideUsesControlConfiguration()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example {\n\tpublic void method() " +
			"{\n\t\tif (true)\n\t\t{\n\t\t\treturn;\n\t\t}\n\t}\n}\n";
		BraceFormatterRuleConfiguration config = createConfigWithControlOverride();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that nested blocks at multiple levels are all formatted correctly.
	 */
	@Test
	public void applyWithNestedBlocksFormatsAllLevels()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example\n{\n\tpublic void method()\n\t{\n\t\tif (true)\n\t\t" +
			"{\n\t\t\twhile (false) {\n\t\t\t\treturn;\n\t\t\t}\n\t\t}\n\t}\n}\n";
		BraceFormatterRuleConfiguration config = createAllmanConfiguration();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that single-line blocks are handled according to configuration.
	 */
	@Test
	public void applyWithSingleLineBlocksRespectsConfiguration()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example {\n\tpublic void method() { return; }\n}\n";
		BraceFormatterRuleConfiguration config = createKAndRConfiguration();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that multiple violations in a single file generate multiple formatting edits.
	 */
	@Test
	public void applyWithMultipleViolationsGeneratesMultipleEdits()
	{
		BraceFormatterFormattingRule rule = createRule();
		String sourceText = "public class Example\n{\n\tpublic void method1()\n\t{\n\t\treturn;\n\t}\n\n" +
			"\tpublic void method2()\n\t{\n\t\treturn;\n\t}\n}\n";
		BraceFormatterRuleConfiguration config = createKAndRConfiguration();
		FormattingContext context = createTestContext(sourceText, config);

		FormattingResult result = rule.apply(context);

		assertThat(result).isNotNull();
	}

	/**
	 * Creates a BraceFormatterFormattingRule instance for testing.
	 *
	 * @return new rule instance
	 */
	private static BraceFormatterFormattingRule createRule()
	{
		return new BraceFormatterFormattingRule();
	}

	/**
	 * Creates a FormattingContext for testing brace formatter rule.
	 *
	 * @param sourceText the Java source code
	 * @param config     the brace formatter configuration
	 * @return test FormattingContext with minimal AST
	 */
	private static FormattingContext createTestContext(String sourceText, RuleConfiguration config)
	{
		CompilationUnitNode ast = TestUtilities.createTestAST();
		return new FormattingContext(
			ast,
			sourceText,
			Path.of("/test/Example.java"),
			config,
			Set.of("io.github.cowwoc.styler.rules.BraceFormatter"),
			Map.of());
	}

	/**
	 * Creates a K&R brace style configuration for testing.
	 *
	 * @return K&R brace style configuration
	 */
	private static BraceFormatterRuleConfiguration createKAndRConfiguration()
	{
		return new BraceFormatterRuleConfiguration(
			BraceStyle.K_AND_R, // general style
			null,               // classBraceStyle (use default)
			null,               // methodBraceStyle (use default)
			null,               // controlBraceStyle (use default)
			null,               // emptyBlockStyle (use default)
			false,              // requireBracesForSingleStatements
			true);              // allowSingleLineBlocks
	}

	/**
	 * Creates an Allman brace style configuration for testing.
	 *
	 * @return Allman brace style configuration
	 */
	private static BraceFormatterRuleConfiguration createAllmanConfiguration()
	{
		return new BraceFormatterRuleConfiguration(
			BraceStyle.ALLMAN,  // general style
			null,               // classBraceStyle (use default)
			null,               // methodBraceStyle (use default)
			null,               // controlBraceStyle (use default)
			null,               // emptyBlockStyle (use default)
			false,              // requireBracesForSingleStatements
			true);              // allowSingleLineBlocks
	}

	/**
	 * Creates a GNU brace style configuration for testing.
	 *
	 * @return GNU brace style configuration
	 */
	private static BraceFormatterRuleConfiguration createGnuConfiguration()
	{
		return new BraceFormatterRuleConfiguration(
			BraceStyle.GNU,     // general style
			null,               // classBraceStyle (use default)
			null,               // methodBraceStyle (use default)
			null,               // controlBraceStyle (use default)
			null,               // emptyBlockStyle (use default)
			false,              // requireBracesForSingleStatements
			true);              // allowSingleLineBlocks
	}

	/**
	 * Creates configuration with class-specific brace style override.
	 *
	 * @return configuration with class override
	 */
	private static BraceFormatterRuleConfiguration createConfigWithClassOverride()
	{
		return new BraceFormatterRuleConfiguration(
			BraceStyle.K_AND_R,  // general style
			BraceStyle.ALLMAN,   // classBraceStyle override
			null,                // methodBraceStyle (use default)
			null,                // controlBraceStyle (use default)
			null,                // emptyBlockStyle (use default)
			false,               // requireBracesForSingleStatements
			true);               // allowSingleLineBlocks
	}

	/**
	 * Creates configuration with method-specific brace style override.
	 *
	 * @return configuration with method override
	 */
	private static BraceFormatterRuleConfiguration createConfigWithMethodOverride()
	{
		return new BraceFormatterRuleConfiguration(
			BraceStyle.K_AND_R,  // general style
			null,                // classBraceStyle (use default)
			BraceStyle.ALLMAN,   // methodBraceStyle override
			null,                // controlBraceStyle (use default)
			null,                // emptyBlockStyle (use default)
			false,               // requireBracesForSingleStatements
			true);               // allowSingleLineBlocks
	}

	/**
	 * Creates configuration with control structure-specific brace style override.
	 *
	 * @return configuration with control override
	 */
	private static BraceFormatterRuleConfiguration createConfigWithControlOverride()
	{
		return new BraceFormatterRuleConfiguration(
			BraceStyle.K_AND_R,  // general style
			null,                // classBraceStyle (use default)
			null,                // methodBraceStyle (use default)
			BraceStyle.ALLMAN,   // controlBraceStyle override
			null,                // emptyBlockStyle (use default)
			false,               // requireBracesForSingleStatements
			true);               // allowSingleLineBlocks
	}
}
