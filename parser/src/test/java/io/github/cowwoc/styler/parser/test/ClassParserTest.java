package io.github.cowwoc.styler.parser.test;

import org.testng.annotations.Test;

import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseSucceeds;

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
		assertParseSucceeds("public class HelloWorld { }");
	}

	/**
	 * Tests class declarations containing method definitions.
	 * Validates that the parser correctly handles method declarations within class bodies,
	 * including return types, names, parameter lists, and method bodies.
	 */
	@Test
	public void testClassWithMethod()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
				}
			}
			""");
	}

	/**
	 * Tests class declarations containing field declarations.
	 * Validates parsing of instance variables with access modifiers and types,
	 * which are fundamental to object-oriented programming.
	 */
	@Test
	public void testClassWithField()
	{
		assertParseSucceeds("""
			public class Test {
				private int x;
			}
			""");
	}

	/**
	 * Tests class declarations containing constructors.
	 * Validates parsing of constructor syntax where method name matches class name
	 * and has no return type declaration.
	 */
	@Test
	public void testClassWithConstructor()
	{
		assertParseSucceeds("""
			public class Test {
				public Test() {
				}
			}
			""");
	}

	/**
	 * Tests class inheritance using extends keyword.
	 * Validates parsing of single-parent inheritance declarations,
	 * which is the foundation of Java's class hierarchy.
	 */
	@Test
	public void testClassWithExtends()
	{
		assertParseSucceeds("public class Child extends Parent { }");
	}

	/**
	 * Tests class declarations with implements clauses.
	 * Validates parsing of interface implementation, allowing classes
	 * to fulfill interface contracts.
	 */
	@Test
	public void testClassWithImplements()
	{
		assertParseSucceeds("public class Test implements Runnable { }");
	}

	/**
	 * Tests class declarations with generic type parameters.
	 * Validates parsing of type parameter lists in angle brackets,
	 * enabling type-safe generic programming.
	 */
	@Test
	public void testClassWithGenerics()
	{
		assertParseSucceeds("public class Box<T> { }");
	}

	/**
	 * Tests interface declaration syntax.
	 * Validates parsing of interface keyword and contract definitions,
	 * which are fundamental to Java's type system.
	 */
	@Test
	public void testInterface()
	{
		assertParseSucceeds("public interface Test { }");
	}

	/**
	 * Tests enum declaration syntax.
	 * Validates parsing of enum keyword and constant declarations,
	 * providing type-safe enumerated values.
	 */
	@Test
	public void testEnum()
	{
		assertParseSucceeds("public enum Color { RED, GREEN, BLUE }");
	}

	/**
	 * Tests package declaration syntax.
	 * Validates parsing of package statements that organize classes into namespaces,
	 * which must appear before any type declarations.
	 */
	@Test
	public void testPackageDeclaration()
	{
		assertParseSucceeds("""
			package com.example;
			public class Test { }
			""");
	}

	/**
	 * Tests import declaration syntax for single types.
	 * Validates parsing of import statements that make external classes
	 * available without fully qualified names.
	 */
	@Test
	public void testImportDeclaration()
	{
		assertParseSucceeds("""
			import java.util.List;
			public class Test { }
			""");
	}

	/**
	 * Tests static import syntax for importing static members.
	 * Validates parsing of static imports that allow direct access to static
	 * fields and methods without class qualification.
	 */
	@Test
	public void testStaticImport()
	{
		assertParseSucceeds("""
			import static java.lang.Math.PI;
			public class Test { }
			""");
	}

	/**
	 * Tests wildcard import syntax using asterisk.
	 * Validates parsing of on-demand imports that make all types in a package
	 * available, though this is generally discouraged in production code.
	 */
	@Test
	public void testWildcardImport()
	{
		assertParseSucceeds("""
			import java.util.*;
			public class Test { }
			""");
	}

	/**
	 * Tests method declarations with multiple parameters.
	 * Validates parsing of parameter lists with different types,
	 * separated by commas in the method signature.
	 */
	@Test
	public void testMethodWithParameters()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo(int x, String y) {
				}
			}
			""");
	}

	/**
	 * Tests varargs syntax for variable-length parameter lists.
	 * Validates parsing of the ellipsis (...) notation that allows methods
	 * to accept zero or more arguments of the specified type.
	 */
	@Test
	public void testMethodWithVarargs()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo(String... args) {
				}
			}
			""");
	}

	/**
	 * Tests method declarations with throws clauses.
	 * Validates parsing of exception declarations that specify which checked
	 * exceptions a method may propagate to its callers.
	 */
	@Test
	public void testMethodWithThrows()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() throws Exception {
				}
			}
			""");
	}
}
