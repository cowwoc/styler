package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.IMPORT_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.MODULE_IMPORT_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.STATIC_IMPORT_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, source.length()),
			semanticNode(MODULE_IMPORT_DECLARATION, 0, 24, "java.base"),
			semanticNode(CLASS_DECLARATION, 26, source.length() - 1, "Test"));
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
			semanticNode(COMPILATION_UNIT, 0, source.length()),
			semanticNode(MODULE_IMPORT_DECLARATION, 0, 30, "com.example.app"),
			semanticNode(CLASS_DECLARATION, 32, source.length() - 1, "Test"));
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
			semanticNode(COMPILATION_UNIT, 0, source.length()),
			semanticNode(MODULE_IMPORT_DECLARATION, 0, 24, "java.base"),
			semanticNode(MODULE_IMPORT_DECLARATION, 25, 48, "java.sql"),
			semanticNode(MODULE_IMPORT_DECLARATION, 49, 76, "java.logging"),
			semanticNode(CLASS_DECLARATION, 78, source.length() - 1, "Test"));
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
			semanticNode(COMPILATION_UNIT, 0, source.length()),
			semanticNode(MODULE_IMPORT_DECLARATION, 0, 24, "java.base"),
			semanticNode(IMPORT_DECLARATION, 25, 47, "java.util.List"),
			semanticNode(IMPORT_DECLARATION, 48, 69, "java.util.Map"),
			semanticNode(CLASS_DECLARATION, 71, source.length() - 1, "Test"));
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
			semanticNode(COMPILATION_UNIT, 0, source.length()),
			semanticNode(MODULE_IMPORT_DECLARATION, 0, 24, "java.base"),
			semanticNode(STATIC_IMPORT_DECLARATION, 25, 57, "java.lang.Math.PI"),
			semanticNode(STATIC_IMPORT_DECLARATION, 58, 91, "java.lang.Math.abs"),
			semanticNode(CLASS_DECLARATION, 93, source.length() - 1, "Test"));
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
			semanticNode(COMPILATION_UNIT, 0, source.length()),
			semanticNode(IMPORT_DECLARATION, 0, 22, "java.util.List"),
			semanticNode(MODULE_IMPORT_DECLARATION, 23, 47, "java.base"),
			semanticNode(STATIC_IMPORT_DECLARATION, 48, 80, "java.lang.Math.PI"),
			semanticNode(IMPORT_DECLARATION, 81, 102, "java.util.Map"),
			semanticNode(MODULE_IMPORT_DECLARATION, 103, 126, "java.sql"),
			semanticNode(CLASS_DECLARATION, 128, source.length() - 1, "Test"));
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
