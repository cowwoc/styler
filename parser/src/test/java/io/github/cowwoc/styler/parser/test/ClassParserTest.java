package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.*;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Thread-safe tests for Parser class declarations.
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
		String source = "public class HelloWorld { }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
			public class Test {
				public void foo() {
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
			public class Test {
				private int x;
			}
			""";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
			public class Test {
				public Test() {
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
		String source = "public class Child extends Parent { }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
		String source = "public class Test implements Runnable { }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
		String source = "public class Box<T> { }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
		String source = "public interface Test { }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
		String source = "public enum Color { RED, GREEN, BLUE }";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
			public class Test {
				public void foo(int x, String y) {
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
			public class Test {
				public void foo(String... args) {
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
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
			public class Test {
				public void foo() throws Exception {
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			NodeIndex root = parser.parse();
			requireThat(root.isValid(), "root.isValid()").isTrue();
		}
	}
}
