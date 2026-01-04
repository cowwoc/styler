package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.ImportAttribute;
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
 * Tests for parsing class, interface, and enum declarations.
 */
public class ClassParserTest
{
	/**
	 * Validates parsing of minimal class declaration.
	 * Tests the simplest valid class: public modifier, class keyword, name, and empty body.
	 */
	@Test
	public void testSimpleClassDeclaration()
	{
		String source = """
			public class HelloWorld { }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(7, 27, new TypeDeclarationAttribute("HelloWorld"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 28);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests class declarations containing method definitions.
	 * Validates that the parser correctly handles method declarations within class bodies,
	 * including return types, names, parameter lists, and method bodies.
	 */
	@Test
	public void testClassWithMethod()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 40, 44);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 44);
			expected.allocateClassDeclaration(7, 46, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 47);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests class declarations containing field declarations.
	 * Validates parsing of instance variables with access modifiers and types,
	 * which are fundamental to object-oriented programming.
	 */
	@Test
	public void testClassWithField()
	{
		String source = """
			public class Test
			{
				private int x;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.FIELD_DECLARATION, 21, 35);
			expected.allocateClassDeclaration(7, 37, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 38);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests class declarations containing constructors.
	 * Validates parsing of constructor syntax where method name matches class name
	 * and has no return type declaration.
	 */
	@Test
	public void testClassWithConstructor()
	{
		String source = """
			public class Test
			{
				public Test()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 36, 40);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 21, 40);
			expected.allocateClassDeclaration(7, 42, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 43);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests class inheritance using extends keyword.
	 * Validates parsing of single-parent inheritance declarations,
	 * which is the foundation of Java's class hierarchy.
	 */
	@Test
	public void testClassWithExtends()
	{
		String source = """
			public class Child extends Parent { }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 33);
			expected.allocateClassDeclaration(7, 37, new TypeDeclarationAttribute("Child"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 38);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests class declarations with implements clauses.
	 * Validates parsing of interface implementation, allowing classes
	 * to fulfill interface contracts.
	 */
	@Test
	public void testClassWithImplements()
	{
		String source = """
			public class Test implements Runnable { }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 37);
			expected.allocateClassDeclaration(7, 41, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 42);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests class declarations with generic type parameters.
	 * Validates parsing of type parameter lists in angle brackets,
	 * enabling type-safe generic programming.
	 */
	@Test
	public void testClassWithGenerics()
	{
		String source = """
			public class Box<T> { }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(7, 23, new TypeDeclarationAttribute("Box"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 24);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests interface declaration syntax.
	 * Validates parsing of interface keyword and contract definitions,
	 * which are fundamental to Java's type system.
	 */
	@Test
	public void testInterface()
	{
		String source = """
			public interface Test { }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateInterfaceDeclaration(7, 25, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 26);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests enum declaration syntax.
	 * Validates parsing of enum keyword and constant declarations,
	 * providing type-safe enumerated values.
	 */
	@Test
	public void testEnum()
	{
		String source = """
			public enum Color { RED, GREEN, BLUE }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ENUM_CONSTANT, 20, 23);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 25, 30);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 32, 36);
			expected.allocateEnumDeclaration(7, 38, new TypeDeclarationAttribute("Color"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 39);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests package declaration syntax.
	 * Validates parsing of package statements that organize classes into namespaces,
	 * which must appear before any type declarations.
	 */
	@Test
	public void testPackageDeclaration()
	{
		String source = """
			package com.example;

			public class Test { }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 8, 19);
			expected.allocatePackageDeclaration(0, 20, new PackageAttribute("com.example"));
			expected.allocateClassDeclaration(29, 43, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 44);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests import declaration syntax for single types.
	 * Validates parsing of import statements that make external classes
	 * available without fully qualified names.
	 */
	@Test
	public void testImportDeclaration()
	{
		String source = """
			import java.util.List;

			public class Test { }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 22, new ImportAttribute("java.util.List", false));
			expected.allocateClassDeclaration(31, 45, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 46);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests static import syntax for importing static members.
	 * Validates parsing of static imports that allow direct access to static
	 * fields and methods without class qualification.
	 */
	@Test
	public void testStaticImport()
	{
		String source = """
			import static java.lang.Math.PI;

			public class Test { }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 32, new ImportAttribute("java.lang.Math.PI", true));
			expected.allocateClassDeclaration(41, 55, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests wildcard import syntax using asterisk.
	 * Validates parsing of on-demand imports that make all types in a package
	 * available, though this is generally discouraged in production code.
	 */
	@Test
	public void testWildcardImport()
	{
		String source = """
			import java.util.*;

			public class Test { }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 19, new ImportAttribute("java.util.*", false));
			expected.allocateClassDeclaration(28, 42, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 43);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests method declarations with multiple parameters.
	 * Validates parsing of parameter lists with different types,
	 * separated by commas in the method signature.
	 */
	@Test
	public void testMethodWithParameters()
	{
		String source = """
			public class Test
			{
				public void foo(int x, String y)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 42, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 52, new ParameterAttribute("y", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 55, 59);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 59);
			expected.allocateClassDeclaration(7, 61, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests varargs syntax for variable-length parameter lists.
	 * Validates parsing of the ellipsis (...) notation that allows methods
	 * to accept zero or more arguments of the specified type.
	 */
	@Test
	public void testMethodWithVarargs()
	{
		String source = """
			public class Test
			{
				public void foo(String... args)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 43);
			expected.allocateParameterDeclaration(37, 51, new ParameterAttribute("args", true, false, false));
			expected.allocateNode(NodeType.BLOCK, 54, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 58);
			expected.allocateClassDeclaration(7, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests method declarations with throws clauses.
	 * Validates parsing of exception declarations that specify which checked
	 * exceptions a method may propagate to its callers.
	 */
	@Test
	public void testMethodWithThrows()
	{
		String source = """
			public class Test
			{
				public void foo() throws Exception
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 55);
			expected.allocateNode(NodeType.BLOCK, 57, 61);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 61);
			expected.allocateClassDeclaration(7, 63, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 64);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
