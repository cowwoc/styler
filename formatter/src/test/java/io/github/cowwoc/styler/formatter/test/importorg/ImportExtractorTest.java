package io.github.cowwoc.styler.formatter.test.importorg;

import io.github.cowwoc.styler.formatter.importorg.internal.ImportDeclaration;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportExtractor;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for ImportExtractor AST-based extraction.
 * <p>
 * These tests verify that the AST-based approach correctly handles edge cases that
 * would cause false positives with regex-based extraction.
 */
public class ImportExtractorTest
{
	/**
	 * Verifies that "import foo.bar;" appearing in a string literal is not extracted as an import.
	 * <p>
	 * Regex-based extraction would incorrectly match this as an import statement.
	 * The AST-based approach correctly identifies that this is inside a string literal.
	 */
	@Test
	void shouldNotMatchImportInString()
	{
		String source = """
			import java.util.List;

			class Test
			{
				String code = "import foo.bar;";
				List<String> items;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);

		List<ImportDeclaration> imports = ImportExtractor.extract(context);

		// Only the real import should be extracted, not the one in the string
		requireThat(imports.size(), "imports.size()").isEqualTo(1);
		requireThat(imports.get(0).qualifiedName(), "qualifiedName").isEqualTo("java.util.List");
	}

	/**
	 * Verifies that "import foo.bar;" appearing in a comment is not extracted as an import.
	 * <p>
	 * Regex-based extraction would incorrectly match this as an import statement.
	 * The AST-based approach correctly identifies that this is inside a comment.
	 */
	@Test
	void shouldNotMatchImportInComment()
	{
		String source = """
			import java.util.List;

			// This is a comment: import fake.Import;
			class Test
			{
				List<String> items;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);

		List<ImportDeclaration> imports = ImportExtractor.extract(context);

		// Only the real import should be extracted, not the one in the comment
		requireThat(imports.size(), "imports.size()").isEqualTo(1);
		requireThat(imports.get(0).qualifiedName(), "qualifiedName").isEqualTo("java.util.List");
	}

	/**
	 * Verifies that "import foo.bar;" appearing in a block comment is not extracted.
	 */
	@Test
	void shouldNotMatchImportInBlockComment()
	{
		String source = """
			import java.util.List;

			/*
			 * Example:
			 * import fake.Import;
			 */
			class Test
			{
				List<String> items;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);

		List<ImportDeclaration> imports = ImportExtractor.extract(context);

		requireThat(imports.size(), "imports.size()").isEqualTo(1);
		requireThat(imports.get(0).qualifiedName(), "qualifiedName").isEqualTo("java.util.List");
	}

	/**
	 * Verifies basic import extraction works correctly.
	 */
	@Test
	void shouldExtractRegularImport()
	{
		String source = """
			import java.util.List;

			class Test { List<String> items; }""";
		TestTransformationContext context = new TestTransformationContext(source);

		List<ImportDeclaration> imports = ImportExtractor.extract(context);

		requireThat(imports.size(), "imports.size()").isEqualTo(1);
		ImportDeclaration imp = imports.get(0);
		requireThat(imp.qualifiedName(), "qualifiedName").isEqualTo("java.util.List");
		requireThat(imp.isStatic(), "isStatic").isFalse();
		requireThat(imp.lineNumber(), "lineNumber").isEqualTo(1);
	}

	/**
	 * Verifies static import extraction works correctly.
	 */
	@Test
	void shouldExtractStaticImport()
	{
		String source = """
			import static java.lang.Math.max;

			class Test { int x = max(1, 2); }""";
		TestTransformationContext context = new TestTransformationContext(source);

		List<ImportDeclaration> imports = ImportExtractor.extract(context);

		requireThat(imports.size(), "imports.size()").isEqualTo(1);
		ImportDeclaration imp = imports.get(0);
		requireThat(imp.qualifiedName(), "qualifiedName").isEqualTo("java.lang.Math.max");
		requireThat(imp.isStatic(), "isStatic").isTrue();
	}

	/**
	 * Verifies wildcard import extraction works correctly.
	 */
	@Test
	void shouldExtractWildcardImport()
	{
		String source = """
			import java.util.*;

			class Test { List<String> items; }""";
		TestTransformationContext context = new TestTransformationContext(source);

		List<ImportDeclaration> imports = ImportExtractor.extract(context);

		requireThat(imports.size(), "imports.size()").isEqualTo(1);
		ImportDeclaration imp = imports.get(0);
		requireThat(imp.qualifiedName(), "qualifiedName").isEqualTo("java.util.*");
		requireThat(imp.isWildcard(), "isWildcard").isTrue();
	}

	/**
	 * Verifies multiple imports are extracted in order.
	 */
	@Test
	void shouldExtractMultipleImports()
	{
		String source = """
			import java.util.List;
			import java.util.Map;
			import java.util.Set;

			class Test { }""";
		TestTransformationContext context = new TestTransformationContext(source);

		List<ImportDeclaration> imports = ImportExtractor.extract(context);

		requireThat(imports.size(), "imports.size()").isEqualTo(3);
		requireThat(imports.get(0).qualifiedName(), "first").isEqualTo("java.util.List");
		requireThat(imports.get(1).qualifiedName(), "second").isEqualTo("java.util.Map");
		requireThat(imports.get(2).qualifiedName(), "third").isEqualTo("java.util.Set");
	}
}
