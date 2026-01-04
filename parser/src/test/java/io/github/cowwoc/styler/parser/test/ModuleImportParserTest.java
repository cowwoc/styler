package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.ImportAttribute;
import io.github.cowwoc.styler.ast.core.ModuleImportAttribute;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateModuleImportDeclaration(0, 24, new ModuleImportAttribute("java.base"));
			expected.allocateClassDeclaration(26, source.length() - 1, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, source.length());
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateModuleImportDeclaration(0, 30, new ModuleImportAttribute("com.example.app"));
			expected.allocateClassDeclaration(32, source.length() - 1, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, source.length());
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateModuleImportDeclaration(0, 24, new ModuleImportAttribute("java.base"));
			expected.allocateModuleImportDeclaration(25, 48, new ModuleImportAttribute("java.sql"));
			expected.allocateModuleImportDeclaration(49, 76, new ModuleImportAttribute("java.logging"));
			expected.allocateClassDeclaration(78, source.length() - 1, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, source.length());
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateModuleImportDeclaration(0, 24, new ModuleImportAttribute("java.base"));
			expected.allocateImportDeclaration(25, 47, new ImportAttribute("java.util.List", false));
			expected.allocateImportDeclaration(48, 69, new ImportAttribute("java.util.Map", false));
			expected.allocateClassDeclaration(71, source.length() - 1, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, source.length());
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateModuleImportDeclaration(0, 24, new ModuleImportAttribute("java.base"));
			expected.allocateImportDeclaration(25, 57, new ImportAttribute("java.lang.Math.PI", true));
			expected.allocateImportDeclaration(58, 91, new ImportAttribute("java.lang.Math.abs", true));
			expected.allocateClassDeclaration(93, source.length() - 1, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, source.length());
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 22, new ImportAttribute("java.util.List", false));
			expected.allocateModuleImportDeclaration(23, 47, new ModuleImportAttribute("java.base"));
			expected.allocateImportDeclaration(48, 80, new ImportAttribute("java.lang.Math.PI", true));
			expected.allocateImportDeclaration(81, 102, new ImportAttribute("java.util.Map", false));
			expected.allocateModuleImportDeclaration(103, 126, new ModuleImportAttribute("java.sql"));
			expected.allocateClassDeclaration(128, source.length() - 1, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, source.length());
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
