package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.ImportAttribute;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.PackageAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for {@link ParserTestUtils} utility methods.
 * <p>
 * Validates that the AST parsing and comparison utilities work correctly for test assertions.
 */
public class ParserTestUtilsTest
{
	/**
	 * Verifies that NodeArena comparison works correctly.
	 * The comparison uses structural equality, so allocation order matters.
	 * Position information is included to distinguish nodes at different locations.
	 */
	@Test
	public void shouldCompareNodeArena()
	{
		String source = "class Test { }";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(0, 14, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 14);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comparison correctly identifies different ASTs.
	 * Two different source files should produce different node arenas.
	 */
	@Test
	public void nodeArenaShouldDistinguishDifferentSources()
	{
		try (Parser parser1 = parse("class Foo { }");
			Parser parser2 = parse("class Bar { }"))
		{
			NodeArena ast1 = parser1.getArena();
			NodeArena ast2 = parser2.getArena();

			// Same structure, different type names
			requireThat(ast1, "ast1").isNotEqualTo(ast2);
		}
	}

	/**
	 * Verifies that comparison with imports works correctly.
	 * The expected arena can be constructed with proper node allocation order.
	 */
	@Test
	public void nodeArenaShouldHandleImports()
	{
		String source = """
			import java.util.List;

			class Test
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 22, new ImportAttribute("java.util.List", false));
			expected.allocateClassDeclaration(24, 38, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 39);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies comparison distinguishes nested classes by position.
	 * Two classes with the same name at different positions should be distinct nodes.
	 */
	@Test
	public void nodeArenaShouldDistinguishNestedClasses()
	{
		String source = """
			class Outer
			{
				class Inner { }
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(15, 30, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(0, 32, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 33);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies comparison correctly handles multiple top-level types.
	 * Each type should be distinguishable by both name and position.
	 */
	@Test
	public void nodeArenaShouldHandleMultipleTopLevelTypes()
	{
		String source = """
			class First { }
			class Second { }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(0, 15, new TypeDeclarationAttribute("First"));
			expected.allocateClassDeclaration(16, 32, new TypeDeclarationAttribute("Second"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 33);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies comparison distinguishes nodes with same type but different positions.
	 * Position information is critical for distinguishing structurally similar nodes.
	 */
	@Test
	public void nodeArenaShouldDistinguishByPosition()
	{
		String source = """
			class Test
			{
				void foo() { }
				void bar() { }
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 25, 28);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 28);
			expected.allocateNode(NodeType.BLOCK, 41, 44);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 30, 44);
			expected.allocateClassDeclaration(0, 46, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 47);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies comparison with package, imports, and class together.
	 * Tests a realistic compilation unit structure with all common elements.
	 */
	@Test
	public void nodeArenaShouldHandleFullCompilationUnit()
	{
		String source = """
			package com.example;

			import java.util.List;
			import java.util.Map;

			class Test { }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 8, 19);
			expected.allocatePackageDeclaration(0, 20, new PackageAttribute("com.example"));
			expected.allocateImportDeclaration(22, 44, new ImportAttribute("java.util.List", false));
			expected.allocateImportDeclaration(45, 66, new ImportAttribute("java.util.Map", false));
			expected.allocateClassDeclaration(68, 82, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 83);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that NodeArena instances are value-based (equal by content, not identity).
	 * Two independently created arenas with same values should be equal.
	 */
	@Test
	public void nodeArenaShouldBeValueBased()
	{
		try (NodeArena arena1 = new NodeArena();
			NodeArena arena2 = new NodeArena())
		{
			arena1.allocateClassDeclaration(0, 10, new TypeDeclarationAttribute("Test"));
			arena2.allocateClassDeclaration(0, 10, new TypeDeclarationAttribute("Test"));

			requireThat(arena1, "arena1").isEqualTo(arena2);
		}
	}

	/**
	 * Verifies that arenas with different positions are not equal.
	 * Even with same type and name, position difference makes arenas distinct.
	 */
	@Test
	public void nodeArenaShouldDifferByPosition()
	{
		try (NodeArena arena1 = new NodeArena();
			NodeArena arena2 = new NodeArena())
		{
			arena1.allocateClassDeclaration(0, 10, new TypeDeclarationAttribute("Test"));
			arena2.allocateClassDeclaration(20, 30, new TypeDeclarationAttribute("Test"));

			requireThat(arena1, "arena1").isNotEqualTo(arena2);
		}
	}

	/**
	 * Verifies that simple nodes without attributes are handled correctly.
	 * Type-only nodes (no name/value) should be distinguishable by position.
	 */
	@Test
	public void nodeArenaShouldHandleSimpleNodes()
	{
		try (NodeArena arena1 = new NodeArena();
			NodeArena arena2 = new NodeArena();
			NodeArena arena3 = new NodeArena())
		{
			arena1.allocateNode(NodeType.BLOCK, 0, 10);
			arena2.allocateNode(NodeType.BLOCK, 0, 10);
			arena3.allocateNode(NodeType.BLOCK, 5, 15);

			requireThat(arena1, "arena1").isEqualTo(arena2);
			requireThat(arena1, "arena1").isNotEqualTo(arena3);
		}
	}
}
