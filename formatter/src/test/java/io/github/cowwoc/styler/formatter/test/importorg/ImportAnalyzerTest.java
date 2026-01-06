package io.github.cowwoc.styler.formatter.test.importorg;

import io.github.cowwoc.styler.formatter.TypeResolutionConfig;
import io.github.cowwoc.styler.formatter.importorg.ImportOrganizerConfiguration;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportAnalysisResult;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportAnalyzer;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportDeclaration;
import io.github.cowwoc.styler.formatter.ClasspathScanner;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import io.github.cowwoc.styler.formatter.test.internal.ClasspathTestUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for ImportAnalyzer unused import detection algorithm.
 */
public class ImportAnalyzerTest
{
	/**
	 * Configuration for tests that don't involve wildcards.
	 * Uses {@code expandWildcardImports = false} so the scanner's classpath content is irrelevant.
	 */
	private static final ImportOrganizerConfiguration NO_WILDCARD_CONFIG =
		ImportOrganizerConfiguration.builder().
			expandWildcardImports(false).
			build();

	/**
	 * Creates an empty classpath scanner for testing.
	 * <p>
	 * The scanner contains no classes, which is suitable for tests that use
	 * {@code expandWildcardImports = false} since the scanner content is not consulted.
	 *
	 * @return a new empty classpath scanner (caller must close via try-with-resources)
	 * @throws IOException if scanner creation fails
	 */
	private static ClasspathScanner createEmptyScanner() throws IOException
	{
		Path emptyJar = ClasspathTestUtils.createTestJar();
		TypeResolutionConfig config = new TypeResolutionConfig(List.of(emptyJar), List.of());
		return ClasspathScanner.create(config);
	}

	/**
	 * Verifies that an unused import is detected when the imported class is not referenced.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldDetectUnusedImportNoReference() throws Exception
	{
		String source = """
			import java.util.List;

			class Test
			{
				void foo()
				{
					int x = 1;
				}
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.List", false, false, 0, 21, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, NO_WILDCARD_CONFIG,
				scanner);
			Set<String> unused = result.unusedImports();

			requireThat(unused.size(), "size").isEqualTo(1);
			requireThat(unused.contains("java.util.List"), "contains").isTrue();
		}
	}

	/**
	 * Verifies that an import used as a field type is not flagged as unused.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagUsedImportInFieldType() throws Exception
	{
		String source = """
			import java.util.List;

			class Test
			{
				List<String> items;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.List", false, false, 0, 21, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, NO_WILDCARD_CONFIG,
				scanner);
			requireThat(result.unusedImports(), "unused").isEmpty();
		}
	}

	/**
	 * Verifies that an import used as a method return type is not flagged as unused.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagUsedImportInMethodReturnType() throws Exception
	{
		String source = """
			import java.util.Optional;

			class Test
			{
				Optional<String> get()
				{
					return null;
				}
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.Optional", false, false, 0, 26, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, NO_WILDCARD_CONFIG,
				scanner);
			requireThat(result.unusedImports(), "unused").isEmpty();
		}
	}

	/**
	 * Verifies that an import used as a method parameter type is not flagged as unused.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagUsedImportInMethodParameter() throws Exception
	{
		String source = """
			import java.util.Map;

			class Test
			{
				void foo(Map m)
				{
				}
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.Map", false, false, 0, 20, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, NO_WILDCARD_CONFIG,
				scanner);
			requireThat(result.unusedImports(), "unused").isEmpty();
		}
	}

	/**
	 * Verifies that an import used as a local variable type is not flagged as unused.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagUsedImportInLocalVariable() throws Exception
	{
		String source = """
			import java.util.ArrayList;

			class Test
			{
				void foo()
				{
					ArrayList list = new ArrayList();
				}
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.ArrayList", false, false, 0, 27, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, NO_WILDCARD_CONFIG,
				scanner);
			requireThat(result.unusedImports(), "unused").isEmpty();
		}
	}

	/**
	 * Verifies that an import used in a catch clause is not flagged as unused.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagUsedImportInCatchClause() throws Exception
	{
		String source = """
			import java.io.IOException;

			class Test
			{
				void foo()
				{
					try
					{
					}
					catch (IOException e)
					{
					}
				}
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.io.IOException", false, false, 0, 26, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, NO_WILDCARD_CONFIG,
				scanner);
			requireThat(result.unusedImports(), "unused").isEmpty();
		}
	}

	/**
	 * Verifies that an import used in an instanceof check is not flagged as unused.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagUsedImportInInstanceOf() throws Exception
	{
		String source = """
			import java.util.List;

			class Test
			{
				void foo(Object o)
				{
					if (o instanceof List)
					{
					}
				}
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.List", false, false, 0, 21, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, NO_WILDCARD_CONFIG,
				scanner);
			requireThat(result.unusedImports(), "unused").isEmpty();
		}
	}

	/**
	 * Verifies that an import used as an annotation is not flagged as unused.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagUsedImportInAnnotation() throws Exception
	{
		String source = """
			import java.lang.Override;

			class Test
			{
				@Override
				void foo()
				{
				}
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.lang.Override", false, false, 0, 25, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, NO_WILDCARD_CONFIG,
				scanner);
			requireThat(result.unusedImports(), "unused").isEmpty();
		}
	}

	/**
	 * Verifies that imports used in generic type parameters are not flagged as unused.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagUsedImportInGenericType() throws Exception
	{
		String source = """
			import java.util.Map;
			import java.util.List;

			class Test
			{
				Map<String, List<Integer>> data;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.Map", false, false, 0, 20, 1),
			new ImportDeclaration("java.util.List", false, false, 21, 44, 2));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, NO_WILDCARD_CONFIG,
				scanner);
			requireThat(result.unusedImports(), "unused").isEmpty();
		}
	}

	@Test
	void shouldReportViolationWhenSymbolsUnresolved() throws Exception
	{
		// Configuration with wildcard expansion enabled
		ImportOrganizerConfiguration expandConfig = ImportOrganizerConfiguration.builder().
			expandWildcardImports(true).
			build();

		String source = """
			import java.util.*;

			class Test
			{
				List list;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.*", false, false, 0, 18, 1));

		// Use an empty scanner (no classpath entries) - this will cause List to be unresolved
		Path emptyJar = ClasspathTestUtils.createTestJar();
		TypeResolutionConfig typeConfig = new TypeResolutionConfig(List.of(emptyJar), List.of());

		try (ClasspathScanner scanner = ClasspathScanner.create(typeConfig))
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(
				imports, context, expandConfig, scanner);

			// Wildcard import is preserved (not marked as unused) because resolution failed
			requireThat(result.unusedImports(), "unused").isEmpty();
			// Resolution should be incomplete with unresolved symbols
			requireThat(result.isResolutionComplete(), "isResolutionComplete").isFalse();
			requireThat(result.unresolvedSymbols(), "unresolvedSymbols").contains("List");
		}
		finally
		{
			Files.deleteIfExists(emptyJar);
		}
	}

	/**
	 * Verifies that wildcard imports are preserved silently when expansion is disabled.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldPreserveWildcardSilentlyWhenExpansionDisabled() throws Exception
	{
		ImportOrganizerConfiguration noExpandConfig = ImportOrganizerConfiguration.builder().
			expandWildcardImports(false).
			build();

		String source = """
			import java.util.*;

			class Test
			{
				List list;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.*", false, false, 0, 18, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, noExpandConfig, scanner);

			// Wildcard import is preserved (not marked as unused)
			requireThat(result.unusedImports(), "unused").isEmpty();
			// Resolution is complete (no unresolved symbols) when expansion is explicitly disabled
			requireThat(result.isResolutionComplete(), "isResolutionComplete").isTrue();
		}
	}

	/**
	 * Verifies that an unused static import is detected.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldDetectUnusedStaticImport() throws Exception
	{
		String source = """
			import static java.lang.Math.max;

			class Test
			{
				int x = 5;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.lang.Math.max", true, false, 0, 32, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, NO_WILDCARD_CONFIG,
				scanner);
			Set<String> unused = result.unusedImports();

			requireThat(unused, "unused").isNotEmpty();
			requireThat(unused.contains("java.lang.Math.max"), "contains").isTrue();
		}
	}

	/**
	 * Verifies that a used static import is not flagged as unused.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagUsedStaticImport() throws Exception
	{
		String source = """
			import static java.lang.Math.max;

			class Test
			{
				int x = max(1, 2);
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.lang.Math.max", true, false, 0, 32, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, NO_WILDCARD_CONFIG,
				scanner);
			requireThat(result.unusedImports(), "unused").isEmpty();
		}
	}

	/**
	 * Verifies that local class declarations are not flagged as unresolved symbols.
	 * A nested class defined in the same file should not require an import.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagLocalClassAsUnresolved() throws Exception
	{
		// Configuration with wildcard expansion enabled
		ImportOrganizerConfiguration expandConfig = ImportOrganizerConfiguration.builder().
			expandWildcardImports(true).
			build();

		String source = """
			import java.util.*;

			class Test {
			    class Inner {}
			    void foo() { Inner x = new Inner(); }
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.*", false, false, 0, 18, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, expandConfig, scanner);

			// Resolution should be complete - "Inner" is a local class, not unresolved
			requireThat(result.isResolutionComplete(), "isResolutionComplete").isTrue();
			// The wildcard import should be marked as unused since nothing from java.util is used
			requireThat(result.unusedImports().contains("java.util.*"), "wildcardUnused").isTrue();
		}
	}

	/**
	 * Verifies that local record declarations are not flagged as unresolved symbols.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagLocalRecordAsUnresolved() throws Exception
	{
		ImportOrganizerConfiguration expandConfig = ImportOrganizerConfiguration.builder().
			expandWildcardImports(true).
			build();

		// Use int instead of String to avoid dependency on java.lang in classpath
		String source = """
			import java.util.*;

			class Test {
			    record Data(int value) {}
			    void foo() { Data d = new Data(42); }
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.*", false, false, 0, 18, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, expandConfig, scanner);

			// Resolution should be complete - "Data" is a local record, not unresolved
			requireThat(result.isResolutionComplete(), "isResolutionComplete").isTrue();
		}
	}

	/**
	 * Verifies that local interface declarations are not flagged as unresolved symbols.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagLocalInterfaceAsUnresolved() throws Exception
	{
		ImportOrganizerConfiguration expandConfig = ImportOrganizerConfiguration.builder().
			expandWildcardImports(true).
			build();

		String source = """
			import java.util.*;

			class Test implements Handler {
			    interface Handler { void handle(); }
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.*", false, false, 0, 18, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, expandConfig, scanner);

			// Resolution should be complete - "Handler" is a local interface, not unresolved
			requireThat(result.isResolutionComplete(), "isResolutionComplete").isTrue();
		}
	}

	/**
	 * Verifies that local enum declarations are not flagged as unresolved symbols.
	 * Note: Enum constants (ACTIVE, INACTIVE) are uppercase and would appear to be types,
	 * but lowercase identifiers used to access the enum value are handled correctly.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagLocalEnumAsUnresolved() throws Exception
	{
		ImportOrganizerConfiguration expandConfig = ImportOrganizerConfiguration.builder().
			expandWildcardImports(true).
			build();

		// Use a simple enum without accessing constants by simple name to avoid
		// the enum constants being flagged as types
		String source = """
			import java.util.*;

			class Test {
			    enum Status { active, inactive }
			    Status status;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.*", false, false, 0, 18, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, expandConfig, scanner);

			// Resolution should be complete - "Status" is a local enum, not unresolved
			requireThat(result.isResolutionComplete(), "isResolutionComplete").isTrue();
		}
	}

	/**
	 * Verifies that same-package types are not flagged as unresolved symbols.
	 * Types in the same package as the file being analyzed do not require imports.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagSamePackageTypeAsUnresolved() throws Exception
	{
		ImportOrganizerConfiguration expandConfig = ImportOrganizerConfiguration.builder().
			expandWildcardImports(true).
			build();

		String source = """
			package com.example;

			import java.util.*;

			class Test {
			    Helper helper;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.*", false, false, 20, 39, 2));

		// Create a jar with a class com.example.Helper (same package as the source file)
		Path jarPath = ClasspathTestUtils.createTestJar("com.example.Helper");
		TypeResolutionConfig typeConfig = new TypeResolutionConfig(List.of(jarPath), List.of());

		try (ClasspathScanner scanner = ClasspathScanner.create(typeConfig))
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, expandConfig, scanner);

			// Resolution should be complete - "Helper" is in the same package
			requireThat(result.isResolutionComplete(), "isResolutionComplete").isTrue();
		}
		finally
		{
			Files.deleteIfExists(jarPath);
		}
	}

	/**
	 * Verifies that the main class of the file (top-level class) is not flagged as unresolved.
	 *
	 * @throws Exception if an I/O error occurs
	 */
	@Test
	void shouldNotFlagTopLevelClassAsUnresolved() throws Exception
	{
		ImportOrganizerConfiguration expandConfig = ImportOrganizerConfiguration.builder().
			expandWildcardImports(true).
			build();

		String source = """
			import java.util.*;

			public class MyClass {
			    MyClass instance;
			    void foo() { MyClass x = new MyClass(); }
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		List<ImportDeclaration> imports = List.of(
			new ImportDeclaration("java.util.*", false, false, 0, 18, 1));

		try (ClasspathScanner scanner = createEmptyScanner())
		{
			ImportAnalysisResult result = ImportAnalyzer.findUnusedImports(imports, context, expandConfig, scanner);

			// Resolution should be complete - "MyClass" is the top-level class, not unresolved
			requireThat(result.isResolutionComplete(), "isResolutionComplete").isTrue();
		}
	}
}
