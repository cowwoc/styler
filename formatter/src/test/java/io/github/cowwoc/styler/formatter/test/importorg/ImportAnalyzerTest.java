package io.github.cowwoc.styler.formatter.test.importorg;

import io.github.cowwoc.styler.formatter.test.TestTransformationContext;

import io.github.cowwoc.styler.formatter.importorg.internal.ImportAnalyzer;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportDeclaration;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for ImportAnalyzer unused import detection algorithm.
 */
public class ImportAnalyzerTest
{
	@Test
	void shouldDetectUnusedImportNoReference()
	{
		String source = """
			import java.util.List;

			class Test { void foo() { int x = 1; } }""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.List", false, 0, 21, 1));

		Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);

		requireThat(unused.size(), "size").isEqualTo(1);
		requireThat(unused.contains("java.util.List"), "contains").isTrue();
	}

	@Test
	void shouldNotFlagUsedImportInFieldType()
	{
		String source = """
			import java.util.List;

			class Test { List<String> items; }""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.List", false, 0, 21, 1));

		Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);

		requireThat(unused, "unused").isEmpty();
	}

	@Test
	void shouldNotFlagUsedImportInMethodReturnType()
	{
		String source = """
			import java.util.Optional;

			class Test { Optional<String> get() { return null; } }""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.Optional", false, 0, 26, 1));

		Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);

		requireThat(unused, "unused").isEmpty();
	}

	@Test
	void shouldNotFlagUsedImportInMethodParameter()
	{
		String source = """
			import java.util.Map;

			class Test { void foo(Map m) {} }""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.Map", false, 0, 20, 1));

		Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);

		requireThat(unused, "unused").isEmpty();
	}

	@Test
	void shouldNotFlagUsedImportInLocalVariable()
	{
		String source = """
			import java.util.ArrayList;

			class Test { void foo() { ArrayList list = new ArrayList(); } }""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.ArrayList", false, 0, 27, 1));

		Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);

		requireThat(unused, "unused").isEmpty();
	}

	@Test
	void shouldNotFlagUsedImportInCatchClause()
	{
		String source = """
			import java.io.IOException;

			class Test { void foo() { try {} catch (IOException e) {} } }""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.io.IOException", false, 0, 26, 1));

		Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);

		requireThat(unused, "unused").isEmpty();
	}

	@Test
	void shouldNotFlagUsedImportInInstanceOf()
	{
		String source = """
			import java.util.List;

			class Test { void foo(Object o) { if (o instanceof List) {} } }""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.List", false, 0, 21, 1));

		Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);

		requireThat(unused, "unused").isEmpty();
	}

	@Test
	void shouldNotFlagUsedImportInAnnotation()
	{
		String source = """
			import java.lang.Override;

			class Test { @Override void foo() {} }""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.lang.Override", false, 0, 25, 1));

		Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);

		requireThat(unused, "unused").isEmpty();
	}

	@Test
	void shouldNotFlagUsedImportInGenericType()
	{
		String source = """
			import java.util.Map;
			import java.util.List;

			class Test { Map<String, List<Integer>> data; }""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.Map", false, 0, 20, 1),
			new ImportDeclaration("java.util.List", false, 21, 44, 2));

		Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);

		requireThat(unused, "unused").isEmpty();
	}

	@Test
	void shouldPreserveWildcardImport()
	{
		String source = """
			import java.util.*;

			class Test { List list; }""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.*", false, 0, 18, 1));

		Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);

		// Wildcard imports are always preserved (conservative approach)
		requireThat(unused, "unused").isEmpty();
	}

	@Test
	void shouldDetectUnusedStaticImport()
	{
		String source = """
			import static java.lang.Math.max;

			class Test { int x = 5; }""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.lang.Math.max", true, 0, 32, 1));

		Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);

		requireThat(unused, "unused").isNotEmpty();
		requireThat(unused.contains("java.lang.Math.max"), "contains").isTrue();
	}

	@Test
	void shouldNotFlagUsedStaticImport()
	{
		String source = """
			import static java.lang.Math.max;

			class Test { int x = max(1, 2); }""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.lang.Math.max", true, 0, 32, 1));

		Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);

		requireThat(unused, "unused").isEmpty();
	}
}
