package io.github.cowwoc.styler.formatter.test.importorg;

import io.github.cowwoc.styler.formatter.importorg.internal.ImportDeclaration;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportExtractor;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests that ImportExtractor correctly reads qualified names from AST attributes.
 * <p>
 * Verifies extraction of import declarations (regular, static, wildcard) using
 * attribute-based AST traversal for accurate identification.
 * <p>
 * <b>Thread-safety</b>: Thread-safe - all instances are created inside {@code @Test} methods.
 */
public class ImportExtractorAttributeTest
{
	/**
	 * Verifies that import qualified names are extracted from AST attributes.
	 */
	@Test
	public void shouldExtractQualifiedNameFromAttribute()
	{
		String source = """
			import java.util.List;

			class Test
			{
				List<String> items;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);

		List<ImportDeclaration> imports = ImportExtractor.extract(context);

		requireThat(imports.size(), "imports.size()").isEqualTo(1);
		ImportDeclaration imp = imports.get(0);
		requireThat(imp.qualifiedName(), "qualifiedName").isEqualTo("java.util.List");
		requireThat(imp.isStatic(), "isStatic").isFalse();
	}

	/**
	 * Verifies that static import qualified names are extracted from AST attributes.
	 */
	@Test
	public void shouldExtractStaticImportFromAttribute()
	{
		String source = """
			import static java.lang.Math.PI;

			class Test
			{
				double x = PI;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);

		List<ImportDeclaration> imports = ImportExtractor.extract(context);

		requireThat(imports.size(), "imports.size()").isEqualTo(1);
		ImportDeclaration imp = imports.get(0);
		requireThat(imp.qualifiedName(), "qualifiedName").isEqualTo("java.lang.Math.PI");
		requireThat(imp.isStatic(), "isStatic").isTrue();
	}

	/**
	 * Verifies that wildcard import qualified names are extracted from AST attributes.
	 */
	@Test
	public void shouldExtractWildcardFromAttribute()
	{
		String source = """
			import java.util.*;

			class Test
			{
				List<String> items;
			}""";
		TestTransformationContext context = new TestTransformationContext(source);

		List<ImportDeclaration> imports = ImportExtractor.extract(context);

		requireThat(imports.size(), "imports.size()").isEqualTo(1);
		ImportDeclaration imp = imports.get(0);
		requireThat(imp.qualifiedName(), "qualifiedName").isEqualTo("java.util.*");
		requireThat(imp.isWildcard(), "isWildcard").isTrue();
	}

	/**
	 * Verifies extraction of multiple import types with correct ordering.
	 * <p>
	 * Tests that ImportExtractor correctly processes regular and static imports,
	 * returning them sorted by source position (not grouped by type).
	 */
	@Test
	public void shouldExtractMultipleImportTypes()
	{
		String source = """
			package com.example;

			import java.util.List;
			import java.util.Map;
			import static java.lang.Math.abs;
			import java.io.*;

			class Test
			{
				List<Map<String, Integer>> data;
				int value = abs(-1);
			}""";
		TestTransformationContext context = new TestTransformationContext(source);

		List<ImportDeclaration> imports = ImportExtractor.extract(context);

		requireThat(imports.size(), "imports.size()").isEqualTo(4);

		// Imports are sorted by source position (not grouped by type)

		// First import: java.util.List
		requireThat(imports.get(0).qualifiedName(), "first.qualifiedName").
			isEqualTo("java.util.List");
		requireThat(imports.get(0).isStatic(), "first.isStatic").isFalse();
		requireThat(imports.get(0).isWildcard(), "first.isWildcard").isFalse();

		// Second import: java.util.Map
		requireThat(imports.get(1).qualifiedName(), "second.qualifiedName").
			isEqualTo("java.util.Map");
		requireThat(imports.get(1).isStatic(), "second.isStatic").isFalse();

		// Third import: static java.lang.Math.abs (appears before java.io.* in source)
		requireThat(imports.get(2).qualifiedName(), "third.qualifiedName").
			isEqualTo("java.lang.Math.abs");
		requireThat(imports.get(2).isStatic(), "third.isStatic").isTrue();

		// Fourth import: java.io.*
		requireThat(imports.get(3).qualifiedName(), "fourth.qualifiedName").
			isEqualTo("java.io.*");
		requireThat(imports.get(3).isWildcard(), "fourth.isWildcard").isTrue();
	}
}
