package io.github.cowwoc.styler.formatter.test.importorg;

import io.github.cowwoc.styler.formatter.test.TestTransformationContext;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.importorg.ImportOrganizerFormattingRule;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for ImportOrganizerFormattingRule interface compliance.
 */
public class ImportOrganizerFormattingRuleTest
{
	/**
	 * Tests that the rule returns the correct ID.
	 */
	@Test
	void shouldReturnCorrectRuleId()
	{
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		requireThat(rule.getId(), "ruleId").isEqualTo("import-organizer");
	}

	/**
	 * Tests that the rule returns a non-empty name.
	 */
	@Test
	void shouldReturnCorrectRuleName()
	{
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		requireThat(rule.getName(), "name").isNotBlank();
	}

	/**
	 * Tests that the rule returns a non-empty description.
	 */
	@Test
	void shouldReturnCorrectRuleDescription()
	{
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		requireThat(rule.getDescription(), "description").isNotBlank();
	}

	/**
	 * Tests that the rule returns WARNING as the default severity.
	 */
	@Test
	void shouldReturnWarningAsDefaultSeverity()
	{
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		requireThat(rule.getDefaultSeverity(), "severity").isEqualTo(ViolationSeverity.WARNING);
	}

	/**
	 * Tests that null context is rejected in analyze.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullContextInAnalyze()
	{
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();
		rule.analyze(null, null);
	}

	/**
	 * Tests that null context is rejected in format.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullContextInFormat()
	{
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();
		rule.format(null, null);
	}

	/**
	 * Tests that wrong config type is rejected in analyze.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectWrongConfigTypeInAnalyze()
	{
		String source = """
			import java.util.List;
			class Test {}""";
		TestTransformationContext context = new TestTransformationContext(source);
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		FormattingConfiguration wrongConfig = new FormattingConfiguration()
		{
			@Override
			public String ruleId()
			{
				return "wrong-id";
			}
		};

		rule.analyze(context, wrongConfig);
	}

	/**
	 * Tests that wrong config type is rejected in format.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectWrongConfigTypeInFormat()
	{
		String source = """
			import java.util.List;
			class Test {}""";
		TestTransformationContext context = new TestTransformationContext(source);
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		FormattingConfiguration wrongConfig = new FormattingConfiguration()
		{
			@Override
			public String ruleId()
			{
				return "wrong-id";
			}
		};

		rule.format(context, wrongConfig);
	}

	/**
	 * Tests that analyze returns empty list for source without imports.
	 */
	@Test
	void shouldReturnEmptyListForNoImports()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		List<FormattingViolation> violations = rule.analyze(context, null);

		requireThat(violations, "violations").isEmpty();
	}

	/**
	 * Tests that analyze returns empty list for already organized imports.
	 */
	@Test
	void shouldReturnEmptyListForAlreadyOrganizedImports()
	{
		String source = """
			import java.io.File;
			import java.util.List;

			class Test { File f; List<String> items; }""";
		TestTransformationContext context = new TestTransformationContext(source);
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		List<FormattingViolation> violations = rule.analyze(context, null);

		requireThat(violations, "violations").isEmpty();
	}

	/**
	 * Tests that analyze detects misordered imports.
	 */
	@Test
	void shouldDetectMisorderedImports()
	{
		String source = """
			import java.util.List;
			import java.io.File;

			class Test {}""";
		TestTransformationContext context = new TestTransformationContext(source);
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		List<FormattingViolation> violations = rule.analyze(context, null);

		requireThat(violations, "violations").isNotEmpty();
		requireThat(violations.get(0).severity(), "severity").isEqualTo(ViolationSeverity.WARNING);
	}

	/**
	 * Tests that format uses default config when null.
	 */
	@Test
	void shouldUseDefaultConfigWhenNull()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		String result = rule.format(context, null);

		requireThat(result, "result").isNotNull();
	}
}
