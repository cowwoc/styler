package io.github.cowwoc.styler.formatter.test.importorg;

import io.github.cowwoc.styler.formatter.importorg.CustomImportPattern;
import io.github.cowwoc.styler.formatter.importorg.ImportOrganizerConfiguration;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportDeclaration;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportGrouper;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for ImportGrouper import grouping and sorting logic.
 */
public class ImportGrouperTest
{
	@Test
	void shouldGroupJavaImportsFirst()
	{
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("org.apache.commons.io.IOUtils", false, false, 0, 40, 1),
			new ImportDeclaration("java.util.List", false, false, 41, 60, 2),
			new ImportDeclaration("java.io.File", false, false, 61, 80, 3));
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		String result = ImportGrouper.organizeImports(imports, config);

		// Java imports should appear before third-party
		requireThat(result, "result").contains("import java.");
		int javaPos = result.indexOf("import java.");
		int apachePos = result.indexOf("import org.apache");
		requireThat(javaPos, "javaPos").isLessThan(apachePos);
	}

	@Test
	void shouldGroupJavaxWithJava()
	{
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("javax.swing.JFrame", false, false, 0, 25, 1),
			new ImportDeclaration("java.util.List", false, false, 26, 45, 2));
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		String result = ImportGrouper.organizeImports(imports, config);

		// Both should appear without blank line between them (same group region)
		requireThat(result, "result").contains("import java.util.List;");
		requireThat(result, "result").contains("import javax.swing.JFrame;");
	}

	@Test
	void shouldSortAlphabeticallyWithinGroup()
	{
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.Map", false, false, 0, 20, 1),
			new ImportDeclaration("java.util.ArrayList", false, false, 21, 45, 2),
			new ImportDeclaration("java.util.List", false, false, 46, 65, 3));
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		String result = ImportGrouper.organizeImports(imports, config);

		// Should be sorted: ArrayList, List, Map
		int arrayListPos = result.indexOf("ArrayList");
		int listPos = result.indexOf("List");
		int mapPos = result.indexOf("Map");
		requireThat(arrayListPos, "arrayListPos").isLessThan(listPos);
		requireThat(listPos, "listPos").isLessThan(mapPos);
	}

	@Test
	void shouldPlaceStaticImportsLast()
	{
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("org.testng.Assert.assertEquals", true, false, 0, 45, 1),
			new ImportDeclaration("java.util.List", false, false, 46, 65, 2));
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		String result = ImportGrouper.organizeImports(imports, config);

		// Regular imports should come before static (staticImportsFirst=false by default)
		int regularPos = result.indexOf("import java.util.List;");
		int staticPos = result.indexOf("import static");
		requireThat(regularPos, "regularPos").isLessThan(staticPos);
	}

	@Test
	void shouldIdentifyProjectPackage()
	{
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("io.github.cowwoc.styler.Foo", false, false, 0, 35, 1),
			new ImportDeclaration("org.apache.Bar", false, false, 36, 55, 2));
		CustomImportPattern customPattern = CustomImportPattern.of("PROJECT", "io\\.github\\.cowwoc\\..*");
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.builder().
			customPatterns(List.of(customPattern)).
			build();

		String result = ImportGrouper.organizeImports(imports, config);

		requireThat(result, "result").contains("io.github.cowwoc.styler.Foo");
		requireThat(result, "result").contains("org.apache.Bar");
	}

	@Test
	void shouldInsertBlankLineBetweenGroups()
	{
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.List", false, false, 0, 20, 1),
			new ImportDeclaration("org.apache.Foo", false, false, 21, 40, 2));
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		String result = ImportGrouper.organizeImports(imports, config);

		// Should have blank line between java and third-party groups
		requireThat(result, "result").contains("\n\n");
	}

	@Test
	void shouldHandleEmptyImportList()
	{
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		String result = ImportGrouper.organizeImports(List.of(), config);

		requireThat(result, "result").isEmpty();
	}

	@Test
	void shouldHandleSingleImport()
	{
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.List", false, false, 0, 20, 1));
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		String result = ImportGrouper.organizeImports(imports, config);

		requireThat(result, "result").contains("import java.util.List;");
	}

	@Test
	void shouldRespectStaticImportsFirst()
	{
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.List", false, false, 0, 20, 1),
			new ImportDeclaration("org.testng.Assert.assertEquals", true, false, 21, 55, 2),
			new ImportDeclaration("org.apache.Foo", false, false, 56, 75, 3));
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.builder().
			staticImportsFirst(true).
			build();

		String result = ImportGrouper.organizeImports(imports, config);

		// Static imports should come first
		int staticPos = result.indexOf("import static");
		int regularPos = result.indexOf("import java.util.List;");
		requireThat(staticPos, "staticPos").isLessThan(regularPos);
	}

	@Test
	void shouldPreserveWildcardImports()
	{
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.*", false, false, 0, 18, 1));
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		String result = ImportGrouper.organizeImports(imports, config);

		requireThat(result, "result").contains("import java.util.*;");
	}
}
