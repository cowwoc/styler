package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.ImportAttribute;
import io.github.cowwoc.styler.ast.core.ModuleImportAttribute;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.PackageAttribute;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing JEP 512 implicit classes and instance main methods.
 */
public final class ImplicitClassParserTest
{
	// ========== Category 1: Minimal Implicit Classes ==========

	/**
	 * Validates parsing of the absolute minimum implicit class - just {@code void main() {}}.
	 * This is the simplest valid implicit class per JEP 512.
	 */
	@Test
	public void shouldParseMinimalImplicitClassWithVoidMain()
	{
		String source = """
			void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 12, 15);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 0, 15);
			expected.allocateImplicitClassDeclaration(0, 15);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 16);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of minimal implicit class with {@code String[] args} parameter.
	 * This is the traditional main signature but without static modifier.
	 */
	@Test
	public void shouldParseMinimalImplicitClassWithVoidMainAndArgs()
	{
		String source = """
			void main(String[] args)
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 10, 16);
			expected.allocateParameterDeclaration(10, 23, new ParameterAttribute("args", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 25, 28);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 0, 28);
			expected.allocateImplicitClassDeclaration(0, 28);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 29);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that static main is also valid in implicit class context.
	 * JEP 512 allows both instance and static main methods.
	 */
	@Test
	public void shouldParseImplicitClassWithStaticMain()
	{
		String source = """
			static void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 19, 22);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 0, 22);
			expected.allocateImplicitClassDeclaration(0, 22);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 23);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that public main is valid in implicit class context.
	 */
	@Test
	public void shouldParseImplicitClassWithPublicMain()
	{
		String source = """
			public void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 19, 22);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 0, 22);
			expected.allocateImplicitClassDeclaration(0, 22);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 23);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Category 2: Top-Level Fields ==========

	/**
	 * Validates parsing of implicit class with a single top-level field declaration.
	 */
	@Test
	public void shouldParseImplicitClassWithSingleField()
	{
		String source = """
			String greeting = "Hello";

			void main()
			{
				System.out.println(greeting);
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.STRING_LITERAL, 18, 25);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 0, 26);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 61);
			expected.allocateNode(NodeType.IDENTIFIER, 43, 49);
			expected.allocateNode(NodeType.FIELD_ACCESS, 43, 53);
			expected.allocateNode(NodeType.FIELD_ACCESS, 43, 61);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 70);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 43, 71);
			expected.allocateNode(NodeType.BLOCK, 40, 74);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 28, 74);
			expected.allocateImplicitClassDeclaration(0, 74);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 75);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of implicit class with multiple top-level field declarations.
	 */
	@Test
	public void shouldParseImplicitClassWithMultipleFields()
	{
		String source = """
			int count = 0;
			String name = "World";

			void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 12, 13);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 0, 14);
			expected.allocateNode(NodeType.STRING_LITERAL, 29, 36);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 15, 37);
			expected.allocateNode(NodeType.BLOCK, 51, 54);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 39, 54);
			expected.allocateImplicitClassDeclaration(0, 54);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 55);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Category 3: Multiple Methods ==========

	/**
	 * Validates parsing of implicit class with main and helper method.
	 */
	@Test
	public void shouldParseImplicitClassWithMultipleMethods()
	{
		String source = """
			void main()
			{
				greet();
			}

			void greet()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 15, 20);
			expected.allocateNode(NodeType.IDENTIFIER, 15, 20);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 15, 22);
			expected.allocateNode(NodeType.BLOCK, 12, 25);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 0, 25);
			expected.allocateNode(NodeType.BLOCK, 40, 43);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 27, 43);
			expected.allocateImplicitClassDeclaration(0, 43);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 44);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Category 4: With Package/Imports ==========

	/**
	 * Validates parsing of implicit class with package declaration.
	 */
	@Test
	public void shouldParseImplicitClassWithPackage()
	{
		String source = """
			package com.example;

			void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 8, 19);
			expected.allocatePackageDeclaration(0, 20, new PackageAttribute("com.example"));
			expected.allocateNode(NodeType.BLOCK, 34, 37);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 22, 37);
			expected.allocateImplicitClassDeclaration(22, 37);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 38);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of implicit class with import declaration.
	 */
	@Test
	public void shouldParseImplicitClassWithImport()
	{
		String source = """
			import java.util.List;

			void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 22, new ImportAttribute("java.util.List", false));
			expected.allocateNode(NodeType.BLOCK, 36, 39);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 24, 39);
			expected.allocateImplicitClassDeclaration(24, 39);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 40);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of implicit class with package and imports.
	 */
	@Test
	public void shouldParseImplicitClassWithPackageAndImports()
	{
		String source = """
			package test;

			import java.util.List;
			import java.util.Map;

			void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 8, 12);
			expected.allocatePackageDeclaration(0, 13, new PackageAttribute("test"));
			expected.allocateImportDeclaration(15, 37, new ImportAttribute("java.util.List", false));
			expected.allocateImportDeclaration(38, 59, new ImportAttribute("java.util.Map", false));
			expected.allocateNode(NodeType.BLOCK, 73, 76);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 61, 76);
			expected.allocateImplicitClassDeclaration(61, 76);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 77);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Category 5: Annotations ==========

	/**
	 * Validates parsing of implicit class with annotated method.
	 */
	@Test
	public void shouldParseImplicitClassWithAnnotatedMethod()
	{
		String source = """
			@SuppressWarnings("unused")
			void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 17);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 17);
			expected.allocateNode(NodeType.STRING_LITERAL, 18, 26);
			expected.allocateNode(NodeType.ANNOTATION, 0, 27);
			expected.allocateNode(NodeType.BLOCK, 40, 43);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 0, 43);
			expected.allocateImplicitClassDeclaration(0, 43);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 44);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Category 6: Comments ==========

	/**
	 * Validates parsing of implicit class with line comments.
	 */
	@Test
	public void shouldParseImplicitClassWithLineComment()
	{
		String source = """
			// Main entry point
			void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 0, 19);
			expected.allocateNode(NodeType.BLOCK, 32, 35);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 20, 35);
			expected.allocateImplicitClassDeclaration(20, 35);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 36);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of implicit class with JavaDoc comment.
	 */
	@Test
	public void shouldParseImplicitClassWithJavadoc()
	{
		String source = """
			/**
			 * Entry point.
			 */
			void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.JAVADOC_COMMENT, 0, 23);
			expected.allocateNode(NodeType.BLOCK, 36, 39);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 24, 39);
			expected.allocateImplicitClassDeclaration(24, 39);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 40);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Category 7: Negative Tests - Explicit Classes ==========

	/**
	 * Validates that explicit class declaration is NOT parsed as implicit class.
	 */
	@Test
	public void shouldNotParseExplicitClassAsImplicit()
	{
		String source = """
			public class Test
			{
				void main()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 34, 38);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 38);
			expected.allocateClassDeclaration(7, 40, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 41);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that interface declaration is NOT parsed as implicit class.
	 */
	@Test
	public void shouldNotParseInterfaceAsImplicit()
	{
		String source = """
			interface Runnable
			{
				void run();
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.METHOD_DECLARATION, 22, 33);
			expected.allocateInterfaceDeclaration(0, 35, new TypeDeclarationAttribute("Runnable"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 36);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that enum declaration is NOT parsed as implicit class.
	 */
	@Test
	public void shouldNotParseEnumAsImplicit()
	{
		String source = """
			enum Color
			{
				RED, GREEN, BLUE
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ENUM_CONSTANT, 14, 17);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 19, 24);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 26, 30);
			expected.allocateEnumDeclaration(0, 32, new TypeDeclarationAttribute("Color"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 33);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Category 8: Edge Cases ==========

	/**
	 * Validates that implicit class correctly handles method with return statement.
	 */
	@Test
	public void shouldParseImplicitClassWithReturnStatement()
	{
		String source = """
			int getValue()
			{
				return 42;
			}

			void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 25, 27);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 18, 28);
			expected.allocateNode(NodeType.BLOCK, 15, 30);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 0, 30);
			expected.allocateNode(NodeType.BLOCK, 44, 47);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 32, 47);
			expected.allocateImplicitClassDeclaration(0, 47);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 48);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that class keyword inside method body doesn't trigger class detection.
	 */
	@Test
	public void shouldHandleLocalClassInImplicitClass()
	{
		String source = """
			void main()
			{
				class Local
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(15, 32, new TypeDeclarationAttribute("Local"));
			expected.allocateNode(NodeType.BLOCK, 12, 34);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 0, 34);
			expected.allocateImplicitClassDeclaration(0, 34);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 35);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates implicit class with static field.
	 */
	@Test
	public void shouldParseImplicitClassWithStaticField()
	{
		String source = """
			static int counter = 0;

			void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 21, 22);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 0, 23);
			expected.allocateNode(NodeType.BLOCK, 37, 40);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 25, 40);
			expected.allocateImplicitClassDeclaration(0, 40);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 41);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that public static void main with modifiers before class keyword is NOT implicit.
	 */
	@Test
	public void shouldNotParsePublicClassAsImplicit()
	{
		String source = """
			public class Main
			{
				public static void main(String[] args)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 45, 51);
			expected.allocateParameterDeclaration(45, 58, new ParameterAttribute("args", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 61, 65);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 65);
			expected.allocateClassDeclaration(7, 67, new TypeDeclarationAttribute("Main"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 68);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Category 9: Complex Scenarios ==========

	/**
	 * Validates complex implicit class with fields, methods, and statements.
	 */
	@Test
	public void shouldParseComplexImplicitClass()
	{
		String source = """
			int x = 1;

			void main()
			{
				int y = x + 1;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 8, 9);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 0, 10);
			expected.allocateNode(NodeType.IDENTIFIER, 35, 36);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 39, 40);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 35, 40);
			expected.allocateNode(NodeType.BLOCK, 24, 43);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 12, 43);
			expected.allocateImplicitClassDeclaration(0, 43);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 44);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates module import in implicit class context.
	 */
	@Test
	public void shouldParseImplicitClassWithModuleImport()
	{
		String source = """
			import module java.base;

			void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateModuleImportDeclaration(0, 24, new ModuleImportAttribute("java.base"));
			expected.allocateNode(NodeType.BLOCK, 38, 41);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 26, 41);
			expected.allocateImplicitClassDeclaration(26, 41);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 42);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates static import in implicit class context.
	 */
	@Test
	public void shouldParseImplicitClassWithStaticImport()
	{
		String source = """
			import static java.lang.System.out;

			void main()
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 35, new ImportAttribute("java.lang.System.out", true));
			expected.allocateNode(NodeType.BLOCK, 49, 52);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 37, 52);
			expected.allocateImplicitClassDeclaration(37, 52);
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 53);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
