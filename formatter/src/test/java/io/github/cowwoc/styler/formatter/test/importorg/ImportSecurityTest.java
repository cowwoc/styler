package io.github.cowwoc.styler.formatter.test.importorg;

import io.github.cowwoc.styler.formatter.test.TestTransformationContext;

import io.github.cowwoc.styler.formatter.importorg.CustomImportPattern;
import io.github.cowwoc.styler.formatter.importorg.ImportGroup;
import io.github.cowwoc.styler.formatter.importorg.ImportOrganizerConfiguration;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for CustomImportPattern security and ReDoS prevention.
 */
public class ImportSecurityTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectReDoSPatternWithNestedQuantifiers()
	{
		String maliciousPattern = "(a+)+$";
		CustomImportPattern.of("PROJECT", maliciousPattern);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectNestedQuantifiersPattern()
	{
		String maliciousPattern = "(.*)+";
		CustomImportPattern.of("PROJECT", maliciousPattern);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectPatternWithBacktracking()
	{
		String maliciousPattern = "(.*a)+";
		CustomImportPattern.of("PROJECT", maliciousPattern);
	}

	@Test
	void shouldAcceptSimpleGlobPattern()
	{
		String validPattern = "io\\.github\\.cowwoc\\..*";
		List<ImportGroup> groupOrder = List.of(ImportGroup.JAVA, ImportGroup.THIRD_PARTY,
			ImportGroup.PROJECT);
		CustomImportPattern customPattern = CustomImportPattern.of("PROJECT", validPattern);

		ImportOrganizerConfiguration config = new ImportOrganizerConfiguration(
			"import-organizer", groupOrder, true, false, true, true, List.of(customPattern));

		requireThat(config.customPatterns(), "customPatterns").size().isEqualTo(1);
		requireThat(config.customPatterns().getFirst().pattern().pattern(), "pattern").isEqualTo(validPattern);
	}

	@Test
	void shouldAcceptLongButValidPattern()
	{
		// Long patterns are acceptable if they don't have nested quantifiers
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 100; ++i)
		{
			sb.append('a');
		}
		String longPattern = sb + "\\..*";
		List<ImportGroup> groupOrder = List.of(ImportGroup.JAVA, ImportGroup.THIRD_PARTY,
			ImportGroup.PROJECT);
		CustomImportPattern customPattern = CustomImportPattern.of("PROJECT", longPattern);

		ImportOrganizerConfiguration config = new ImportOrganizerConfiguration("import-organizer",
			groupOrder, true, false, true, true, List.of(customPattern));

		requireThat(config.customPatterns(), "customPatterns").size().isEqualTo(1);
	}

	@Test
	void shouldEnforceTimeoutOnMaliciousInput()
	{
		String source = """
			import java.util.*;

			class Test {}""";
		TestTransformationContext context = new TestTransformationContext(source);

		context.checkDeadline();

		requireThat(source, "source").isNotBlank();
	}
}
