package io.github.cowwoc.styler.parser.test;

import org.testng.annotations.Test;

import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseSucceeds;

/**
 * Tests for method reference parsing support.
 * Method references are a compact form of lambda expressions that reference existing methods.
 * They use the {@code ::} syntax introduced in Java 8.
 */
public class MethodReferenceParserTest
{
	// ========================================
	// Core Variants (11 tests)
	// ========================================

	/**
	 * Tests static method reference parsing.
	 * Static method references have the form {@code ClassName::methodName} and reference
	 * static methods that can be used in functional contexts.
	 */
	@Test
	public void testStaticMethodReference()
	{
		assertParseSucceeds("""
			class Test {
				Object f = String::valueOf;
			}
			""");
	}

	/**
	 * Tests instance method reference on a variable.
	 * Instance method references on variables have the form {@code variable::methodName}
	 * and bind to a specific object instance.
	 */
	@Test
	public void testInstanceMethodReferenceOnVariable()
	{
		assertParseSucceeds("""
			class Test {
				void method() {
					String str = "test";
					Object c = str::length;
				}
			}
			""");
	}

	/**
	 * Tests instance method reference using {@code this} keyword.
	 * The {@code this::methodName} form references an instance method on the current object.
	 */
	@Test
	public void testInstanceMethodReferenceOnThis()
	{
		assertParseSucceeds("""
			class Test {
				void helper() {}
				void method() {
					Runnable r = this::helper;
				}
			}
			""");
	}

	/**
	 * Tests instance method reference using {@code super} keyword.
	 * The {@code super::methodName} form references an inherited method from the superclass.
	 */
	@Test
	public void testInstanceMethodReferenceOnSuper()
	{
		assertParseSucceeds("""
			class Parent {
				void helper() {}
			}
			class Test extends Parent {
				void method() {
					Runnable r = super::helper;
				}
			}
			""");
	}

	/**
	 * Tests unbound instance method reference.
	 * Unbound references have the form {@code ClassName::instanceMethodName} where
	 * the first parameter of the functional interface becomes the receiver.
	 */
	@Test
	public void testUnboundInstanceMethodReference()
	{
		assertParseSucceeds("""
			class Test {
				Object f = String::toLowerCase;
			}
			""");
	}

	/**
	 * Tests constructor reference for a regular class.
	 * Constructor references have the form {@code ClassName::new} and create new instances.
	 */
	@Test
	public void testConstructorReference()
	{
		assertParseSucceeds("""
			class Test {
				Object s = String::new;
			}
			""");
	}

	/**
	 * Tests constructor reference with explicit generic type parameters.
	 * Generic constructor references specify type arguments before {@code ::new}.
	 */
	@Test
	public void testGenericConstructorReference()
	{
		assertParseSucceeds("""
			class Container<T> {}
			class Test {
				Object s = Container::new;
			}
			""");
	}

	/**
	 * Tests constructor reference for a nested class.
	 * Nested class constructor references use qualified names like {@code Outer.Inner::new}.
	 */
	@Test
	public void testNestedClassConstructorReference()
	{
		assertParseSucceeds("""
			class Outer {
				class Inner {}
				Object f = Inner::new;
			}
			""");
	}

	/**
	 * Tests method reference with a parameterized type receiver.
	 * Method references can be applied to generic class methods.
	 */
	@Test
	public void testMethodReferenceWithGenericReceiver()
	{
		assertParseSucceeds("""
			class Container<T> {
				T get() { return null; }
			}
			class Test {
				void method(Container<String> container) {
					Object f = container::get;
				}
			}
			""");
	}

	/**
	 * Tests method reference targeting an overloaded method.
	 * When multiple overloads exist, the compiler selects the correct one based on context.
	 */
	@Test
	public void testMethodReferenceToOverloadedMethod()
	{
		assertParseSucceeds("""
			class Test {
				void accept(Object o) {}
				void accept(String s) {}
				void method() {
					Object f = this::accept;
				}
			}
			""");
	}

	/**
	 * Tests method reference with multiple chained method calls before the reference.
	 * Method references can follow complex expression chains.
	 */
	@Test
	public void testMethodReferenceOnChainedCalls()
	{
		assertParseSucceeds("""
			class Builder {
				Builder append(String s) { return this; }
				String build() { return null; }
			}
			class Test {
				void method(Builder b) {
					Object f = b.append("a").append("b")::build;
				}
			}
			""");
	}

	// ========================================
	// Expression Contexts (6 tests)
	// ========================================

	/**
	 * Tests method reference as an argument to a method call.
	 * Method references can be passed directly as arguments to methods expecting functional interfaces.
	 */
	@Test
	public void testMethodReferenceAsArgument()
	{
		assertParseSucceeds("""
			class Test {
				void accept(Object o) {}
				void method() {
					accept(String::valueOf);
				}
			}
			""");
	}

	/**
	 * Tests method reference in a stream map operation.
	 * Method references are commonly used in stream pipelines for transformations.
	 */
	@Test
	public void testMethodReferenceInStreamMap()
	{
		assertParseSucceeds("""
			class Stream {
				Stream map(Object mapper) { return this; }
			}
			class Test {
				void method(Stream stream) {
					stream.map(String::toLowerCase);
				}
			}
			""");
	}

	/**
	 * Tests method reference in a stream filter operation.
	 * Method references returning boolean can be used as predicates in filter operations.
	 */
	@Test
	public void testMethodReferenceInStreamFilter()
	{
		assertParseSucceeds("""
			class Stream {
				Stream filter(Object predicate) { return this; }
			}
			class Test {
				void method(Stream stream) {
					stream.filter(String::isEmpty);
				}
			}
			""");
	}

	/**
	 * Tests method reference on a chained expression.
	 * Method references can be applied to the result of method chains.
	 */
	@Test
	public void testMethodReferenceChainedExpression()
	{
		assertParseSucceeds("""
			class Container {
				String getValue() { return null; }
			}
			class Test {
				void method(Container obj) {
					Object c = obj.getValue()::length;
				}
			}
			""");
	}

	/**
	 * Tests method reference as part of a ternary expression.
	 * Method references can appear in the branches of conditional expressions.
	 */
	@Test
	public void testMethodReferenceInTernary()
	{
		assertParseSucceeds("""
			class Test {
				void method(boolean flag) {
					Object f = flag ? String::length : String::hashCode;
				}
			}
			""");
	}

	/**
	 * Tests method reference in a return statement.
	 * Method references can be returned directly from methods with functional interface return types.
	 */
	@Test
	public void testMethodReferenceInReturnStatement()
	{
		assertParseSucceeds("""
			class Test {
				Object method() {
					return Integer::parseInt;
				}
			}
			""");
	}

	// ========================================
	// Error Cases (3 tests)
	// ========================================

	/**
	 * Tests that method reference with missing method name fails to parse.
	 * The syntax {@code String::} is malformed because no method name follows the double colon.
	 */
	@Test
	public void testMalformedMethodReferenceNoMethod()
	{
		assertParseFails("class Test { Object x = String::; }");
	}

	/**
	 * Tests that method reference with extra colon fails to parse.
	 * The syntax {@code String:::length} has too many colons and is invalid.
	 */
	@Test
	public void testMalformedMethodReferenceExtraColon()
	{
		assertParseFails("class Test { Object x = String:::length; }");
	}

	/**
	 * Tests that single colon is not confused with method reference.
	 * The syntax {@code String:length} uses single colon which is not valid for method references.
	 */
	@Test
	public void testMalformedMethodReferenceSingleColon()
	{
		assertParseFails("class Test { Object x = String:length; }");
	}

	// ========================================
	// Real-World Integration (4 tests)
	// ========================================

	/**
	 * Tests a complete stream pipeline with multiple method references.
	 * Real-world code often chains multiple stream operations using method references.
	 */
	@Test
	public void testRealWorldStreamPipeline()
	{
		assertParseSucceeds("""
			class Stream {
				Stream filter(Object p) { return this; }
				Stream map(Object m) { return this; }
				void forEach(Object c) {}
			}
			class Test {
				void method(Stream names) {
					names
						.filter(String::isEmpty)
						.map(String::toLowerCase)
						.forEach(Object::hashCode);
				}
			}
			""");
	}

	/**
	 * Tests comparator creation using method references.
	 * The {@code Comparator.comparing} method commonly takes method references for key extraction.
	 */
	@Test
	public void testRealWorldComparatorCreation()
	{
		assertParseSucceeds("""
			class Person {
				String getName() { return null; }
			}
			class Comparator {
				static Object comparing(Object keyExtractor) { return null; }
			}
			class Test {
				void method() {
					Object c = Comparator.comparing(Person::getName);
				}
			}
			""");
	}

	/**
	 * Tests collector with method references for grouping.
	 * Complex collection operations often use method references with collectors.
	 */
	@Test
	public void testRealWorldCollectorWithMethodReferences()
	{
		assertParseSucceeds("""
			class Person {
				String getName() { return null; }
			}
			class Stream {
				Object collect(Object collector) { return null; }
			}
			class Collectors {
				static Object groupingBy(Object classifier) { return null; }
			}
			class Test {
				void method(Stream people) {
					Object grouped = people.collect(Collectors.groupingBy(Person::getName));
				}
			}
			""");
	}

	/**
	 * Tests Optional operations with method references.
	 * Method references work naturally with Optional's functional methods.
	 */
	@Test
	public void testRealWorldOptionalWithMethodReferences()
	{
		assertParseSucceeds("""
			class Optional {
				Optional map(Object mapper) { return this; }
				Optional filter(Object predicate) { return this; }
				void ifPresent(Object consumer) {}
				static Optional of(Object value) { return null; }
			}
			class Test {
				void method() {
					Optional opt = Optional.of("test");
					opt.map(String::toUpperCase)
						.filter(String::isEmpty)
						.ifPresent(Object::hashCode);
				}
			}
			""");
	}
}
