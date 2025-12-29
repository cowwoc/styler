package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.CONSTRUCTOR_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.ENUM_CONSTANT;
import static io.github.cowwoc.styler.ast.core.NodeType.ENUM_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.IMPORT_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.INTERFACE_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PACKAGE_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.STATIC_IMPORT_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 28),
			semanticNode(CLASS_DECLARATION, 7, 27, "HelloWorld"));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 47),
			semanticNode(CLASS_DECLARATION, 7, 46, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 44),
			semanticNode(BLOCK, 40, 44));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 38),
			semanticNode(CLASS_DECLARATION, 7, 37, "Test"),
			semanticNode(FIELD_DECLARATION, 21, 35));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 43),
			semanticNode(CLASS_DECLARATION, 7, 42, "Test"),
			semanticNode(CONSTRUCTOR_DECLARATION, 21, 40),
			semanticNode(BLOCK, 36, 40));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 38),
			semanticNode(CLASS_DECLARATION, 7, 37, "Child"),
			semanticNode(QUALIFIED_NAME, 27, 33));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 42),
			semanticNode(CLASS_DECLARATION, 7, 41, "Test"),
			semanticNode(QUALIFIED_NAME, 29, 37));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 24),
			semanticNode(CLASS_DECLARATION, 7, 23, "Box"));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 26),
			semanticNode(INTERFACE_DECLARATION, 7, 25, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 39),
			semanticNode(ENUM_DECLARATION, 7, 38, "Color"),
			semanticNode(ENUM_CONSTANT, 20, 23),
			semanticNode(ENUM_CONSTANT, 25, 30),
			semanticNode(ENUM_CONSTANT, 32, 36));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 44),
			semanticNode(PACKAGE_DECLARATION, 0, 20, "com.example"),
			semanticNode(CLASS_DECLARATION, 29, 43, "Test"),
			semanticNode(QUALIFIED_NAME, 8, 19));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 46),
			semanticNode(IMPORT_DECLARATION, 0, 22, "java.util.List"),
			semanticNode(CLASS_DECLARATION, 31, 45, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 56),
			semanticNode(STATIC_IMPORT_DECLARATION, 0, 32, "java.lang.Math.PI"),
			semanticNode(CLASS_DECLARATION, 41, 55, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 43),
			semanticNode(IMPORT_DECLARATION, 0, 19, "java.util.*"),
			semanticNode(CLASS_DECLARATION, 28, 42, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 62),
			semanticNode(CLASS_DECLARATION, 7, 61, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 59),
			semanticNode(PARAMETER_DECLARATION, 37, 42, "x"),
			semanticNode(PARAMETER_DECLARATION, 44, 52, "y"),
			semanticNode(BLOCK, 55, 59),
			semanticNode(QUALIFIED_NAME, 44, 50));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 61),
			semanticNode(CLASS_DECLARATION, 7, 60, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 58),
			semanticNode(PARAMETER_DECLARATION, 37, 51, "args"),
			semanticNode(BLOCK, 54, 58),
			semanticNode(QUALIFIED_NAME, 37, 43));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 64),
			semanticNode(CLASS_DECLARATION, 7, 63, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 61),
			semanticNode(BLOCK, 57, 61),
			semanticNode(QUALIFIED_NAME, 46, 55));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
