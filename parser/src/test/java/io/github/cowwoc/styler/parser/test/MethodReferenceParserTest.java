package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

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
		String source = """
			class Test
			{
				Object f = String::valueOf;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 44),
			typeDeclaration(CLASS_DECLARATION, 0, 43, "Test"),
			fieldDeclaration( 14, 41),
			methodReference( 25, 40),
			identifier( 25, 31));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests instance method reference on a variable.
	 * Instance method references on variables have the form {@code variable::methodName}
	 * and bind to a specific object instance.
	 */
	@Test
	public void testInstanceMethodReferenceOnVariable()
	{
		String source = """
			class Test
			{
				void method()
				{
					String str = "test";
					Object c = str::length;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 85),
			typeDeclaration(CLASS_DECLARATION, 0, 84, "Test"),
			methodDeclaration( 14, 82),
			block( 29, 82),
			qualifiedName( 33, 39),
			stringLiteral( 46, 52),
			qualifiedName( 56, 62),
			methodReference( 67, 78),
			identifier( 67, 70));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests instance method reference using {@code this} keyword.
	 * The {@code this::methodName} form references an instance method on the current object.
	 */
	@Test
	public void testInstanceMethodReferenceOnThis()
	{
		String source = """
			class Test
			{
				void helper()
				{
				}

				void method()
				{
					Runnable r = this::helper;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 87),
			typeDeclaration(CLASS_DECLARATION, 0, 86, "Test"),
			methodDeclaration( 14, 33),
			block( 29, 33),
			methodDeclaration( 36, 84),
			block( 51, 84),
			qualifiedName( 55, 63),
			methodReference( 68, 80),
			thisExpression( 68, 72));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests instance method reference using {@code super} keyword.
	 * The {@code super::methodName} form references an inherited method from the superclass.
	 */
	@Test
	public void testInstanceMethodReferenceOnSuper()
	{
		String source = """
			class Parent
			{
				void helper()
				{
				}
			}

			class Test extends Parent
			{
				void method()
				{
					Runnable r = super::helper;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 120),
			typeDeclaration(CLASS_DECLARATION, 0, 37, "Parent"),
			methodDeclaration( 16, 35),
			block( 31, 35),
			typeDeclaration(CLASS_DECLARATION, 39, 119, "Test"),
			qualifiedName( 58, 64),
			methodDeclaration( 68, 117),
			block( 83, 117),
			qualifiedName( 87, 95),
			methodReference( 100, 113),
			superExpression( 100, 105));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests unbound instance method reference.
	 * Unbound references have the form {@code ClassName::instanceMethodName} where
	 * the first parameter of the functional interface becomes the receiver.
	 */
	@Test
	public void testUnboundInstanceMethodReference()
	{
		String source = """
			class Test
			{
				Object f = String::toLowerCase;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 48),
			typeDeclaration(CLASS_DECLARATION, 0, 47, "Test"),
			fieldDeclaration( 14, 45),
			methodReference( 25, 44),
			identifier( 25, 31));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests constructor reference for a regular class.
	 * Constructor references have the form {@code ClassName::new} and create new instances.
	 */
	@Test
	public void testConstructorReference()
	{
		String source = """
			class Test
			{
				Object s = String::new;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 40),
			typeDeclaration(CLASS_DECLARATION, 0, 39, "Test"),
			fieldDeclaration( 14, 37),
			methodReference( 25, 36),
			identifier( 25, 31));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests constructor reference with explicit generic type parameters.
	 * Generic constructor references specify type arguments before {@code ::new}.
	 */
	@Test
	public void testGenericConstructorReference()
	{
		String source = """
			class Container<T>
			{
			}

			class Test
			{
				Object s = Container::new;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 67),
			typeDeclaration(CLASS_DECLARATION, 0, 22, "Container"),
			typeDeclaration(CLASS_DECLARATION, 24, 66, "Test"),
			fieldDeclaration( 38, 64),
			methodReference( 49, 63),
			identifier( 49, 58));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests constructor reference for a nested class.
	 * Nested class constructor references use qualified names like {@code Outer.Inner::new}.
	 */
	@Test
	public void testNestedClassConstructorReference()
	{
		String source = """
			class Outer
			{
				class Inner
				{
				}

				Object f = Inner::new;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 60),
			typeDeclaration(CLASS_DECLARATION, 0, 59, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 15, 32, "Inner"),
			fieldDeclaration( 35, 57),
			methodReference( 46, 56),
			identifier( 46, 51));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests method reference with a parameterized type receiver.
	 * Method references can be applied to generic class methods.
	 */
	@Test
	public void testMethodReferenceWithGenericReceiver()
	{
		String source = """
			class Container<T>
			{
				T get()
				{
					return null;
				}
			}

			class Test
			{
				void method(Container<String> container)
				{
					Object f = container::get;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 146),
			typeDeclaration(CLASS_DECLARATION, 0, 52, "Container"),
			methodDeclaration( 22, 50),
			block( 31, 50),
			returnStatement( 35, 47),
			nullLiteral( 42, 46),
			typeDeclaration(CLASS_DECLARATION, 54, 145, "Test"),
			methodDeclaration( 68, 143),
			parameterizedType( 80, 97),
			qualifiedName( 80, 89),
			qualifiedName( 90, 96),
			parameterNode( 80, 107, "container"),
			block( 110, 143),
			qualifiedName( 114, 120),
			methodReference( 125, 139),
			identifier( 125, 134));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests method reference targeting an overloaded method.
	 * When multiple overloads exist, the compiler selects the correct one based on context.
	 */
	@Test
	public void testMethodReferenceToOverloadedMethod()
	{
		String source = """
			class Test
			{
				void accept(Object o)
				{
				}

				void accept(String s)
				{
				}

				void method()
				{
					Object f = this::accept;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 123),
			typeDeclaration(CLASS_DECLARATION, 0, 122, "Test"),
			methodDeclaration( 14, 41),
			qualifiedName( 26, 32),
			parameterNode( 26, 34, "o"),
			block( 37, 41),
			methodDeclaration( 44, 71),
			qualifiedName( 56, 62),
			parameterNode( 56, 64, "s"),
			block( 67, 71),
			methodDeclaration( 74, 120),
			block( 89, 120),
			qualifiedName( 93, 99),
			methodReference( 104, 116),
			thisExpression( 104, 108));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests method reference with multiple chained method calls before the reference.
	 * Method references can follow complex expression chains.
	 */
	@Test
	public void testMethodReferenceOnChainedCalls()
	{
		String source = """
			class Builder
			{
				Builder append(String s)
				{
					return this;
				}

				String build()
				{
					return null;
				}
			}

			class Test
			{
				void method(Builder b)
				{
					Object f = b.append("a").append("b")::build;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 196),
			typeDeclaration(CLASS_DECLARATION, 0, 102, "Builder"),
			methodDeclaration( 17, 62),
			qualifiedName( 32, 38),
			parameterNode( 32, 40, "s"),
			block( 43, 62),
			returnStatement( 47, 59),
			thisExpression( 54, 58),
			methodDeclaration( 65, 100),
			block( 81, 100),
			returnStatement( 85, 97),
			nullLiteral( 92, 96),
			typeDeclaration(CLASS_DECLARATION, 104, 195, "Test"),
			methodDeclaration( 118, 193),
			qualifiedName( 130, 137),
			parameterNode( 130, 139, "b"),
			block( 142, 193),
			qualifiedName( 146, 152),
			methodReference( 157, 189),
			methodInvocation( 157, 182),
			fieldAccess( 157, 177),
			methodInvocation( 157, 170),
			fieldAccess( 157, 165),
			identifier( 157, 158),
			stringLiteral( 166, 169),
			stringLiteral( 178, 181));

		requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Test
			{
				void accept(Object o)
				{
				}

				void method()
				{
					accept(String::valueOf);
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 93),
			typeDeclaration(CLASS_DECLARATION, 0, 92, "Test"),
			methodDeclaration( 14, 41),
			qualifiedName( 26, 32),
			parameterNode( 26, 34, "o"),
			block( 37, 41),
			methodDeclaration( 44, 90),
			block( 59, 90),
			methodInvocation( 63, 86),
			qualifiedName( 63, 69),
			identifier( 63, 69),
			methodReference( 70, 85),
			identifier( 70, 76));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests method reference in a stream map operation.
	 * Method references are commonly used in stream pipelines for transformations.
	 */
	@Test
	public void testMethodReferenceInStreamMap()
	{
		String source = """
			class Stream
			{
				Stream map(Object mapper)
				{
					return this;
				}
			}

			class Test
			{
				void method(Stream stream)
				{
					stream.map(String::toLowerCase);
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 150),
			typeDeclaration(CLASS_DECLARATION, 0, 64, "Stream"),
			methodDeclaration( 16, 62),
			qualifiedName( 27, 33),
			parameterNode( 27, 40, "mapper"),
			block( 43, 62),
			returnStatement( 47, 59),
			thisExpression( 54, 58),
			typeDeclaration(CLASS_DECLARATION, 66, 149, "Test"),
			methodDeclaration( 80, 147),
			qualifiedName( 92, 98),
			parameterNode( 92, 105, "stream"),
			block( 108, 147),
			methodInvocation( 112, 143),
			fieldAccess( 112, 122),
			qualifiedName( 112, 122),
			identifier( 112, 118),
			methodReference( 123, 142),
			identifier( 123, 129));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests method reference in a stream filter operation.
	 * Method references returning boolean can be used as predicates in filter operations.
	 */
	@Test
	public void testMethodReferenceInStreamFilter()
	{
		String source = """
			class Stream
			{
				Stream filter(Object predicate)
				{
					return this;
				}
			}

			class Test
			{
				void method(Stream stream)
				{
					stream.filter(String::isEmpty);
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 155),
			typeDeclaration(CLASS_DECLARATION, 0, 70, "Stream"),
			methodDeclaration( 16, 68),
			qualifiedName( 30, 36),
			parameterNode( 30, 46, "predicate"),
			block( 49, 68),
			returnStatement( 53, 65),
			thisExpression( 60, 64),
			typeDeclaration(CLASS_DECLARATION, 72, 154, "Test"),
			methodDeclaration( 86, 152),
			qualifiedName( 98, 104),
			parameterNode( 98, 111, "stream"),
			block( 114, 152),
			methodInvocation( 118, 148),
			fieldAccess( 118, 131),
			qualifiedName( 118, 131),
			identifier( 118, 124),
			methodReference( 132, 147),
			identifier( 132, 138));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests method reference on a chained expression.
	 * Method references can be applied to the result of method chains.
	 */
	@Test
	public void testMethodReferenceChainedExpression()
	{
		String source = """
			class Container
			{
				String getValue()
				{
					return null;
				}
			}

			class Test
			{
				void method(Container obj)
				{
					Object c = obj.getValue()::length;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 147),
			typeDeclaration(CLASS_DECLARATION, 0, 59, "Container"),
			methodDeclaration( 19, 57),
			block( 38, 57),
			returnStatement( 42, 54),
			nullLiteral( 49, 53),
			typeDeclaration(CLASS_DECLARATION, 61, 146, "Test"),
			methodDeclaration( 75, 144),
			qualifiedName( 87, 96),
			parameterNode( 87, 100, "obj"),
			block( 103, 144),
			qualifiedName( 107, 113),
			methodReference( 118, 140),
			methodInvocation( 118, 132),
			fieldAccess( 118, 130),
			identifier( 118, 121));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests method reference as part of a ternary expression.
	 * Method references can appear in the branches of conditional expressions.
	 */
	@Test
	public void testMethodReferenceInTernary()
	{
		String source = """
			class Test
			{
				void method(boolean flag)
				{
					Object f = flag ? String::length : String::hashCode;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 103),
			typeDeclaration(CLASS_DECLARATION, 0, 102, "Test"),
			methodDeclaration( 14, 100),
			parameterNode( 26, 38, "flag"),
			block( 41, 100),
			qualifiedName( 45, 51),
			conditionalExpression( 56, 96),
			identifier( 56, 60),
			methodReference( 63, 77),
			identifier( 63, 69),
			methodReference( 80, 96),
			identifier( 80, 86));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests method reference in a return statement.
	 * Method references can be returned directly from methods with functional interface return types.
	 */
	@Test
	public void testMethodReferenceInReturnStatement()
	{
		String source = """
			class Test
			{
				Object method()
				{
					return Integer::parseInt;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 66),
			typeDeclaration(CLASS_DECLARATION, 0, 65, "Test"),
			methodDeclaration( 14, 63),
			block( 31, 63),
			returnStatement( 35, 60),
			methodReference( 42, 59),
			identifier( 42, 49));

		requireThat(actual, "actual").isEqualTo(expected);
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
		String source = """
			class Stream
			{
				Stream filter(Object p)
				{
					return this;
				}

				Stream map(Object m)
				{
					return this;
				}

				void forEach(Object c)
				{
				}
			}

			class Test
			{
				void method(Stream names)
				{
					names
						.filter(String::isEmpty)
						.map(String::toLowerCase)
						.forEach(Object::hashCode);
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 283),
			typeDeclaration(CLASS_DECLARATION, 0, 137, "Stream"),
			methodDeclaration( 16, 60),
			qualifiedName( 30, 36),
			parameterNode( 30, 38, "p"),
			block( 41, 60),
			returnStatement( 45, 57),
			thisExpression( 52, 56),
			methodDeclaration( 63, 104),
			qualifiedName( 74, 80),
			parameterNode( 74, 82, "m"),
			block( 85, 104),
			returnStatement( 89, 101),
			thisExpression( 96, 100),
			methodDeclaration( 107, 135),
			qualifiedName( 120, 126),
			parameterNode( 120, 128, "c"),
			block( 131, 135),
			typeDeclaration(CLASS_DECLARATION, 139, 282, "Test"),
			methodDeclaration( 153, 280),
			qualifiedName( 165, 171),
			parameterNode( 165, 177, "names"),
			block( 180, 280),
			methodInvocation( 184, 276),
			fieldAccess( 184, 258),
			methodInvocation( 184, 246),
			fieldAccess( 184, 225),
			methodInvocation( 184, 217),
			fieldAccess( 184, 200),
			qualifiedName( 184, 200),
			identifier( 184, 189),
			methodReference( 201, 216),
			identifier( 201, 207),
			methodReference( 226, 245),
			identifier( 226, 232),
			methodReference( 259, 275),
			identifier( 259, 265));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests comparator creation using method references.
	 * The {@code Comparator.comparing} method commonly takes method references for key extraction.
	 */
	@Test
	public void testRealWorldComparatorCreation()
	{
		String source = """
			class Person
			{
				String getName()
				{
					return null;
				}
			}

			class Comparator
			{
				static Object comparing(Object keyExtractor)
				{
					return null;
				}
			}

			class Test
			{
				void method()
				{
					Object c = Comparator.comparing(Person::getName);
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 234),
			typeDeclaration(CLASS_DECLARATION, 0, 55, "Person"),
			methodDeclaration( 16, 53),
			block( 34, 53),
			returnStatement( 38, 50),
			nullLiteral( 45, 49),
			typeDeclaration(CLASS_DECLARATION, 57, 144, "Comparator"),
			methodDeclaration( 77, 142),
			qualifiedName( 101, 107),
			parameterNode( 101, 120, "keyExtractor"),
			block( 123, 142),
			returnStatement( 127, 139),
			nullLiteral( 134, 138),
			typeDeclaration(CLASS_DECLARATION, 146, 233, "Test"),
			methodDeclaration( 160, 231),
			block( 175, 231),
			qualifiedName( 179, 185),
			methodInvocation( 190, 227),
			fieldAccess( 190, 210),
			identifier( 190, 200),
			methodReference( 211, 226),
			identifier( 211, 217));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests collector with method references for grouping.
	 * Complex collection operations often use method references with collectors.
	 */
	@Test
	public void testRealWorldCollectorWithMethodReferences()
	{
		String source = """
			class Person
			{
				String getName()
				{
					return null;
				}
			}

			class Stream
			{
				Object collect(Object collector)
				{
					return null;
				}
			}

			class Collectors
			{
				static Object groupingBy(Object classifier)
				{
					return null;
				}
			}

			class Test
			{
				void method(Stream people)
				{
					Object grouped = people.collect(Collectors.groupingBy(Person::getName));
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 342),
			typeDeclaration(CLASS_DECLARATION, 0, 55, "Person"),
			methodDeclaration( 16, 53),
			block( 34, 53),
			returnStatement( 38, 50),
			nullLiteral( 45, 49),
			typeDeclaration(CLASS_DECLARATION, 57, 128, "Stream"),
			methodDeclaration( 73, 126),
			qualifiedName( 88, 94),
			parameterNode( 88, 104, "collector"),
			block( 107, 126),
			returnStatement( 111, 123),
			nullLiteral( 118, 122),
			typeDeclaration(CLASS_DECLARATION, 130, 216, "Collectors"),
			methodDeclaration( 150, 214),
			qualifiedName( 175, 181),
			parameterNode( 175, 192, "classifier"),
			block( 195, 214),
			returnStatement( 199, 211),
			nullLiteral( 206, 210),
			typeDeclaration(CLASS_DECLARATION, 218, 341, "Test"),
			methodDeclaration( 232, 339),
			qualifiedName( 244, 250),
			parameterNode( 244, 257, "people"),
			block( 260, 339),
			qualifiedName( 264, 270),
			methodInvocation( 281, 335),
			fieldAccess( 281, 295),
			identifier( 281, 287),
			methodInvocation( 296, 334),
			fieldAccess( 296, 317),
			identifier( 296, 306),
			methodReference( 318, 333),
			identifier( 318, 324));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests Optional operations with method references.
	 * Method references work naturally with Optional's functional methods.
	 */
	@Test
	public void testRealWorldOptionalWithMethodReferences()
	{
		String source = """
			class Optional
			{
				Optional map(Object mapper)
				{
					return this;
				}

				Optional filter(Object predicate)
				{
					return this;
				}

				void ifPresent(Object consumer)
				{
				}

				static Optional of(Object value)
				{
					return null;
				}
			}

			class Test
			{
				void method()
				{
					Optional opt = Optional.of("test");
					opt.map(String::toUpperCase)
						.filter(String::isEmpty)
						.ifPresent(Object::hashCode);
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 389),
			typeDeclaration(CLASS_DECLARATION, 0, 221, "Optional"),
			methodDeclaration( 18, 66),
			qualifiedName( 31, 37),
			parameterNode( 31, 44, "mapper"),
			block( 47, 66),
			returnStatement( 51, 63),
			thisExpression( 58, 62),
			methodDeclaration( 69, 123),
			qualifiedName( 85, 91),
			parameterNode( 85, 101, "predicate"),
			block( 104, 123),
			returnStatement( 108, 120),
			thisExpression( 115, 119),
			methodDeclaration( 126, 163),
			qualifiedName( 141, 147),
			parameterNode( 141, 156, "consumer"),
			block( 159, 163),
			methodDeclaration( 166, 219),
			qualifiedName( 185, 191),
			parameterNode( 185, 197, "value"),
			block( 200, 219),
			returnStatement( 204, 216),
			nullLiteral( 211, 215),
			typeDeclaration(CLASS_DECLARATION, 223, 388, "Test"),
			methodDeclaration( 237, 386),
			block( 252, 386),
			qualifiedName( 256, 264),
			methodInvocation( 271, 290),
			fieldAccess( 271, 282),
			identifier( 271, 279),
			stringLiteral( 283, 289),
			methodInvocation( 294, 382),
			fieldAccess( 294, 364),
			methodInvocation( 294, 350),
			fieldAccess( 294, 333),
			methodInvocation( 294, 322),
			qualifiedName( 294, 301),
			fieldAccess( 294, 301),
			identifier( 294, 297),
			methodReference( 302, 321),
			identifier( 302, 308),
			methodReference( 334, 349),
			identifier( 334, 340),
			methodReference( 365, 381),
			identifier( 365, 371));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
