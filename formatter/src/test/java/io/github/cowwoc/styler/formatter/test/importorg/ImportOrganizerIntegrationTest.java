package io.github.cowwoc.styler.formatter.test.importorg;

import io.github.cowwoc.styler.formatter.test.TestTransformationContext;

import io.github.cowwoc.styler.formatter.importorg.ImportOrganizerFormattingRule;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Integration tests for ImportOrganizerFormattingRule with real-world scenarios.
 */
public class ImportOrganizerIntegrationTest
{
	@Test
	void shouldOrganizeRealWorldJavaFile()
	{
		String source = """
			package com.example;

			import java.util.Map;
			import static org.testng.Assert.assertEquals;
			import org.apache.commons.io.IOUtils;
			import java.util.List;
			import io.github.cowwoc.styler.Foo;

			public class Example {
			    Map<String, List<String>> data;
			    IOUtils utils;
			    Foo foo;
			    void test() { assertEquals(1, 1); }
			}""";

		TestTransformationContext context = new TestTransformationContext(source);
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		String result = rule.format(context, List.of());

		requireThat(result.indexOf("java.util.List"), "javaPos").
			isLessThan(result.indexOf("org.apache"));
	}

	@Test
	void shouldRemoveUnusedImportsInRealFile()
	{
		String source = """
			package com.example;

			import java.util.List;
			import java.util.Map;
			import java.util.Set;

			public class Example {
			    private List<String> items;
			}""";

		TestTransformationContext context = new TestTransformationContext(source);
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		String result = rule.format(context, List.of());

		requireThat(result, "result").contains("java.util.List");
		requireThat(result, "result").doesNotContain("java.util.Map");
		requireThat(result, "result").doesNotContain("java.util.Set");
	}

	@Test
	void shouldHandleFileWithNoPackageDeclaration()
	{
		String source = """
			import java.util.List;

			class DefaultPackageClass {}""";

		TestTransformationContext context = new TestTransformationContext(source);
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		String result = rule.format(context, List.of());

		requireThat(result, "result").isNotNull();
	}

	@Test
	void shouldPreserveSourceWithOnlyPackageAndClass()
	{
		String source = """
			package com.example;

			public class Empty {}""";

		TestTransformationContext context = new TestTransformationContext(source);
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		String result = rule.format(context, List.of());

		requireThat(result, "result").isEqualTo(source);
	}

	@Test
	void shouldHandleDuplicateImports()
	{
		String source = """
			import java.util.List;
			import java.util.List;

			class Test
			{
				List x;
			}""";

		int sourceCount = countOccurrences(source, "import java.util.List");
		requireThat(sourceCount, "sourceCount").isEqualTo(2);

		TestTransformationContext context = new TestTransformationContext(source);
		ImportOrganizerFormattingRule rule = new ImportOrganizerFormattingRule();

		String result = rule.format(context, List.of());

		int resultCount = countOccurrences(result, "import java.util.List");
		requireThat(resultCount, "resultCount").isEqualTo(1);
	}

	private int countOccurrences(String text, String search)
	{
		int count = 0;
		int index = text.indexOf(search);
		while (index != -1)
		{
			++count;
			index = text.indexOf(search, index + search.length());
		}
		return count;
	}
}
