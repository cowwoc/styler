package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.importNode;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.moduleImportNode;

/**
 * Tests for parsing module import declarations (JEP 511).
 */
public final class ModuleImportParserTest
{
	/**
	 * Validates parsing of a basic module import with a single-segment module name.
	 * Tests the fundamental module import syntax: {@code import module java.base;}.
	 */
	@Test
	public void shouldParseBasicModuleImport()
	{
		String source = """
			import module java.base;

			class Test
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, source.length()),
			moduleImportNode( 0, 24, "java.base"),
			typeDeclaration(CLASS_DECLARATION, 26, source.length() - 1, "Test"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a module import with a multi-segment module name.
	 * Tests module names with multiple components separated by dots.
	 */
	@Test
	public void shouldParseModuleImportWithMultiSegmentName()
	{
		String source = """
			import module com.example.app;

			class Test
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, source.length()),
			moduleImportNode( 0, 30, "com.example.app"),
			typeDeclaration(CLASS_DECLARATION, 32, source.length() - 1, "Test"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of multiple module import declarations.
	 * Tests that multiple module imports are correctly parsed and tracked individually.
	 */
	@Test
	public void shouldParseMultipleModuleImports()
	{
		String source = """
			import module java.base;
			import module java.sql;
			import module java.logging;

			class Test
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, source.length()),
			moduleImportNode( 0, 24, "java.base"),
			moduleImportNode( 25, 48, "java.sql"),
			moduleImportNode( 49, 76, "java.logging"),
			typeDeclaration(CLASS_DECLARATION, 78, source.length() - 1, "Test"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of module imports mixed with regular type imports.
	 * Tests that module imports and regular imports can coexist in the same file.
	 */
	@Test
	public void shouldParseModuleImportWithRegularImports()
	{
		String source = """
			import module java.base;
			import java.util.List;
			import java.util.Map;

			class Test
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, source.length()),
			moduleImportNode( 0, 24, "java.base"),
			importNode(25, 47, "java.util.List", false),
			importNode(48, 69, "java.util.Map", false),
			typeDeclaration(CLASS_DECLARATION, 71, source.length() - 1, "Test"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of module imports mixed with static imports.
	 * Tests that module imports and static imports can coexist in the same file.
	 */
	@Test
	public void shouldParseModuleImportWithStaticImports()
	{
		String source = """
			import module java.base;
			import static java.lang.Math.PI;
			import static java.lang.Math.abs;

			class Test
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, source.length()),
			moduleImportNode( 0, 24, "java.base"),
			importNode(25, 57, "java.lang.Math.PI", true),
			importNode(58, 91, "java.lang.Math.abs", true),
			typeDeclaration(CLASS_DECLARATION, 93, source.length() - 1, "Test"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of all import types mixed together in arbitrary order.
	 * Tests that module imports, regular imports, and static imports can appear in any order.
	 */
	@Test
	public void shouldParseMixedImportsInAnyOrder()
	{
		String source = """
			import java.util.List;
			import module java.base;
			import static java.lang.Math.PI;
			import java.util.Map;
			import module java.sql;

			class Test
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, source.length()),
			importNode(0, 22, "java.util.List", false),
			moduleImportNode( 23, 47, "java.base"),
			importNode(48, 80, "java.lang.Math.PI", true),
			importNode(81, 102, "java.util.Map", false),
			moduleImportNode( 103, 126, "java.sql"),
			typeDeclaration(CLASS_DECLARATION, 128, source.length() - 1, "Test"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a module import without a trailing semicolon is rejected.
	 * The syntax {@code import module java.base} without semicolon is invalid.
	 */
	@Test
	public void shouldRejectModuleImportWithMissingSemicolon()
	{
		assertParseFails("""
			import module java.base

			class Test
			{
			}
			""");
	}

	/**
	 * Validates that a module import without a module name is rejected.
	 * The syntax {@code import module ;} is invalid.
	 */
	@Test
	public void shouldRejectModuleImportWithMissingModuleName()
	{
		assertParseFails("""
			import module ;

			class Test
			{
			}
			""");
	}
}
