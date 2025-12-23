package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.CONDITIONAL_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_REFERENCE;
import static io.github.cowwoc.styler.ast.core.NodeType.NULL_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.STRING_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.SUPER_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.THIS_EXPRESSION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, 44),
			semanticNode(CLASS_DECLARATION, 0, 43, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 41),
			semanticNode(METHOD_REFERENCE, 25, 40),
			semanticNode(IDENTIFIER, 25, 31));

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
			semanticNode(COMPILATION_UNIT, 0, 85),
			semanticNode(CLASS_DECLARATION, 0, 84, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 82),
			semanticNode(BLOCK, 29, 82),
			semanticNode(QUALIFIED_NAME, 33, 39),
			semanticNode(STRING_LITERAL, 46, 52),
			semanticNode(QUALIFIED_NAME, 56, 62),
			semanticNode(METHOD_REFERENCE, 67, 78),
			semanticNode(IDENTIFIER, 67, 70));

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
			semanticNode(COMPILATION_UNIT, 0, 87),
			semanticNode(CLASS_DECLARATION, 0, 86, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 33),
			semanticNode(BLOCK, 29, 33),
			semanticNode(METHOD_DECLARATION, 36, 84),
			semanticNode(BLOCK, 51, 84),
			semanticNode(QUALIFIED_NAME, 55, 63),
			semanticNode(METHOD_REFERENCE, 68, 80),
			semanticNode(THIS_EXPRESSION, 68, 72));

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
			semanticNode(COMPILATION_UNIT, 0, 120),
			semanticNode(CLASS_DECLARATION, 0, 37, "Parent"),
			semanticNode(METHOD_DECLARATION, 16, 35),
			semanticNode(BLOCK, 31, 35),
			semanticNode(CLASS_DECLARATION, 39, 119, "Test"),
			semanticNode(QUALIFIED_NAME, 58, 64),
			semanticNode(METHOD_DECLARATION, 68, 117),
			semanticNode(BLOCK, 83, 117),
			semanticNode(QUALIFIED_NAME, 87, 95),
			semanticNode(METHOD_REFERENCE, 100, 113),
			semanticNode(SUPER_EXPRESSION, 100, 105));

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
			semanticNode(COMPILATION_UNIT, 0, 48),
			semanticNode(CLASS_DECLARATION, 0, 47, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 45),
			semanticNode(METHOD_REFERENCE, 25, 44),
			semanticNode(IDENTIFIER, 25, 31));

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
			semanticNode(COMPILATION_UNIT, 0, 40),
			semanticNode(CLASS_DECLARATION, 0, 39, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 37),
			semanticNode(METHOD_REFERENCE, 25, 36),
			semanticNode(IDENTIFIER, 25, 31));

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
			semanticNode(COMPILATION_UNIT, 0, 67),
			semanticNode(CLASS_DECLARATION, 0, 22, "Container"),
			semanticNode(CLASS_DECLARATION, 24, 66, "Test"),
			semanticNode(FIELD_DECLARATION, 38, 64),
			semanticNode(METHOD_REFERENCE, 49, 63),
			semanticNode(IDENTIFIER, 49, 58));

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
			semanticNode(COMPILATION_UNIT, 0, 60),
			semanticNode(CLASS_DECLARATION, 0, 59, "Outer"),
			semanticNode(CLASS_DECLARATION, 15, 32, "Inner"),
			semanticNode(FIELD_DECLARATION, 35, 57),
			semanticNode(METHOD_REFERENCE, 46, 56),
			semanticNode(IDENTIFIER, 46, 51));

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
			semanticNode(COMPILATION_UNIT, 0, 146),
			semanticNode(CLASS_DECLARATION, 0, 52, "Container"),
			semanticNode(METHOD_DECLARATION, 22, 50),
			semanticNode(BLOCK, 31, 50),
			semanticNode(RETURN_STATEMENT, 35, 47),
			semanticNode(NULL_LITERAL, 42, 46),
			semanticNode(CLASS_DECLARATION, 54, 145, "Test"),
			semanticNode(METHOD_DECLARATION, 68, 143),
			semanticNode(QUALIFIED_NAME, 80, 89),
			semanticNode(QUALIFIED_NAME, 90, 96),
			semanticNode(BLOCK, 110, 143),
			semanticNode(QUALIFIED_NAME, 114, 120),
			semanticNode(METHOD_REFERENCE, 125, 139),
			semanticNode(IDENTIFIER, 125, 134));

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
			semanticNode(COMPILATION_UNIT, 0, 123),
			semanticNode(CLASS_DECLARATION, 0, 122, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 41),
			semanticNode(QUALIFIED_NAME, 26, 32),
			semanticNode(BLOCK, 37, 41),
			semanticNode(METHOD_DECLARATION, 44, 71),
			semanticNode(QUALIFIED_NAME, 56, 62),
			semanticNode(BLOCK, 67, 71),
			semanticNode(METHOD_DECLARATION, 74, 120),
			semanticNode(BLOCK, 89, 120),
			semanticNode(QUALIFIED_NAME, 93, 99),
			semanticNode(METHOD_REFERENCE, 104, 116),
			semanticNode(THIS_EXPRESSION, 104, 108));

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
			semanticNode(COMPILATION_UNIT, 0, 196),
			semanticNode(CLASS_DECLARATION, 0, 102, "Builder"),
			semanticNode(METHOD_DECLARATION, 17, 62),
			semanticNode(QUALIFIED_NAME, 32, 38),
			semanticNode(BLOCK, 43, 62),
			semanticNode(RETURN_STATEMENT, 47, 59),
			semanticNode(THIS_EXPRESSION, 54, 58),
			semanticNode(METHOD_DECLARATION, 65, 100),
			semanticNode(BLOCK, 81, 100),
			semanticNode(RETURN_STATEMENT, 85, 97),
			semanticNode(NULL_LITERAL, 92, 96),
			semanticNode(CLASS_DECLARATION, 104, 195, "Test"),
			semanticNode(METHOD_DECLARATION, 118, 193),
			semanticNode(QUALIFIED_NAME, 130, 137),
			semanticNode(BLOCK, 142, 193),
			semanticNode(QUALIFIED_NAME, 146, 152),
			semanticNode(METHOD_REFERENCE, 157, 189),
			semanticNode(METHOD_INVOCATION, 157, 182),
			semanticNode(FIELD_ACCESS, 157, 177),
			semanticNode(METHOD_INVOCATION, 157, 170),
			semanticNode(FIELD_ACCESS, 157, 165),
			semanticNode(IDENTIFIER, 157, 158),
			semanticNode(STRING_LITERAL, 166, 169),
			semanticNode(STRING_LITERAL, 178, 181));

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
			semanticNode(COMPILATION_UNIT, 0, 93),
			semanticNode(CLASS_DECLARATION, 0, 92, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 41),
			semanticNode(QUALIFIED_NAME, 26, 32),
			semanticNode(BLOCK, 37, 41),
			semanticNode(METHOD_DECLARATION, 44, 90),
			semanticNode(BLOCK, 59, 90),
			semanticNode(METHOD_INVOCATION, 63, 86),
			semanticNode(QUALIFIED_NAME, 63, 69),
			semanticNode(IDENTIFIER, 63, 69),
			semanticNode(METHOD_REFERENCE, 70, 85),
			semanticNode(IDENTIFIER, 70, 76));

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
			semanticNode(COMPILATION_UNIT, 0, 150),
			semanticNode(CLASS_DECLARATION, 0, 64, "Stream"),
			semanticNode(METHOD_DECLARATION, 16, 62),
			semanticNode(QUALIFIED_NAME, 27, 33),
			semanticNode(BLOCK, 43, 62),
			semanticNode(RETURN_STATEMENT, 47, 59),
			semanticNode(THIS_EXPRESSION, 54, 58),
			semanticNode(CLASS_DECLARATION, 66, 149, "Test"),
			semanticNode(METHOD_DECLARATION, 80, 147),
			semanticNode(QUALIFIED_NAME, 92, 98),
			semanticNode(BLOCK, 108, 147),
			semanticNode(METHOD_INVOCATION, 112, 143),
			semanticNode(FIELD_ACCESS, 112, 122),
			semanticNode(QUALIFIED_NAME, 112, 122),
			semanticNode(IDENTIFIER, 112, 118),
			semanticNode(METHOD_REFERENCE, 123, 142),
			semanticNode(IDENTIFIER, 123, 129));

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
			semanticNode(COMPILATION_UNIT, 0, 155),
			semanticNode(CLASS_DECLARATION, 0, 70, "Stream"),
			semanticNode(METHOD_DECLARATION, 16, 68),
			semanticNode(QUALIFIED_NAME, 30, 36),
			semanticNode(BLOCK, 49, 68),
			semanticNode(RETURN_STATEMENT, 53, 65),
			semanticNode(THIS_EXPRESSION, 60, 64),
			semanticNode(CLASS_DECLARATION, 72, 154, "Test"),
			semanticNode(METHOD_DECLARATION, 86, 152),
			semanticNode(QUALIFIED_NAME, 98, 104),
			semanticNode(BLOCK, 114, 152),
			semanticNode(METHOD_INVOCATION, 118, 148),
			semanticNode(FIELD_ACCESS, 118, 131),
			semanticNode(QUALIFIED_NAME, 118, 131),
			semanticNode(IDENTIFIER, 118, 124),
			semanticNode(METHOD_REFERENCE, 132, 147),
			semanticNode(IDENTIFIER, 132, 138));

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
			semanticNode(COMPILATION_UNIT, 0, 147),
			semanticNode(CLASS_DECLARATION, 0, 59, "Container"),
			semanticNode(METHOD_DECLARATION, 19, 57),
			semanticNode(BLOCK, 38, 57),
			semanticNode(RETURN_STATEMENT, 42, 54),
			semanticNode(NULL_LITERAL, 49, 53),
			semanticNode(CLASS_DECLARATION, 61, 146, "Test"),
			semanticNode(METHOD_DECLARATION, 75, 144),
			semanticNode(QUALIFIED_NAME, 87, 96),
			semanticNode(BLOCK, 103, 144),
			semanticNode(QUALIFIED_NAME, 107, 113),
			semanticNode(METHOD_REFERENCE, 118, 140),
			semanticNode(METHOD_INVOCATION, 118, 132),
			semanticNode(FIELD_ACCESS, 118, 130),
			semanticNode(IDENTIFIER, 118, 121));

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
			semanticNode(COMPILATION_UNIT, 0, 103),
			semanticNode(CLASS_DECLARATION, 0, 102, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 100),
			semanticNode(BLOCK, 41, 100),
			semanticNode(QUALIFIED_NAME, 45, 51),
			semanticNode(CONDITIONAL_EXPRESSION, 56, 96),
			semanticNode(IDENTIFIER, 56, 60),
			semanticNode(METHOD_REFERENCE, 63, 77),
			semanticNode(IDENTIFIER, 63, 69),
			semanticNode(METHOD_REFERENCE, 80, 96),
			semanticNode(IDENTIFIER, 80, 86));

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
			semanticNode(COMPILATION_UNIT, 0, 66),
			semanticNode(CLASS_DECLARATION, 0, 65, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 63),
			semanticNode(BLOCK, 31, 63),
			semanticNode(RETURN_STATEMENT, 35, 60),
			semanticNode(METHOD_REFERENCE, 42, 59),
			semanticNode(IDENTIFIER, 42, 49));

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
			semanticNode(COMPILATION_UNIT, 0, 283),
			semanticNode(CLASS_DECLARATION, 0, 137, "Stream"),
			semanticNode(METHOD_DECLARATION, 16, 60),
			semanticNode(QUALIFIED_NAME, 30, 36),
			semanticNode(BLOCK, 41, 60),
			semanticNode(RETURN_STATEMENT, 45, 57),
			semanticNode(THIS_EXPRESSION, 52, 56),
			semanticNode(METHOD_DECLARATION, 63, 104),
			semanticNode(QUALIFIED_NAME, 74, 80),
			semanticNode(BLOCK, 85, 104),
			semanticNode(RETURN_STATEMENT, 89, 101),
			semanticNode(THIS_EXPRESSION, 96, 100),
			semanticNode(METHOD_DECLARATION, 107, 135),
			semanticNode(QUALIFIED_NAME, 120, 126),
			semanticNode(BLOCK, 131, 135),
			semanticNode(CLASS_DECLARATION, 139, 282, "Test"),
			semanticNode(METHOD_DECLARATION, 153, 280),
			semanticNode(QUALIFIED_NAME, 165, 171),
			semanticNode(BLOCK, 180, 280),
			semanticNode(METHOD_INVOCATION, 184, 276),
			semanticNode(FIELD_ACCESS, 184, 258),
			semanticNode(METHOD_INVOCATION, 184, 246),
			semanticNode(FIELD_ACCESS, 184, 225),
			semanticNode(METHOD_INVOCATION, 184, 217),
			semanticNode(FIELD_ACCESS, 184, 200),
			semanticNode(QUALIFIED_NAME, 184, 200),
			semanticNode(IDENTIFIER, 184, 189),
			semanticNode(METHOD_REFERENCE, 201, 216),
			semanticNode(IDENTIFIER, 201, 207),
			semanticNode(METHOD_REFERENCE, 226, 245),
			semanticNode(IDENTIFIER, 226, 232),
			semanticNode(METHOD_REFERENCE, 259, 275),
			semanticNode(IDENTIFIER, 259, 265));

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
			semanticNode(COMPILATION_UNIT, 0, 234),
			semanticNode(CLASS_DECLARATION, 0, 55, "Person"),
			semanticNode(METHOD_DECLARATION, 16, 53),
			semanticNode(BLOCK, 34, 53),
			semanticNode(RETURN_STATEMENT, 38, 50),
			semanticNode(NULL_LITERAL, 45, 49),
			semanticNode(CLASS_DECLARATION, 57, 144, "Comparator"),
			semanticNode(METHOD_DECLARATION, 77, 142),
			semanticNode(QUALIFIED_NAME, 101, 107),
			semanticNode(BLOCK, 123, 142),
			semanticNode(RETURN_STATEMENT, 127, 139),
			semanticNode(NULL_LITERAL, 134, 138),
			semanticNode(CLASS_DECLARATION, 146, 233, "Test"),
			semanticNode(METHOD_DECLARATION, 160, 231),
			semanticNode(BLOCK, 175, 231),
			semanticNode(QUALIFIED_NAME, 179, 185),
			semanticNode(METHOD_INVOCATION, 190, 227),
			semanticNode(FIELD_ACCESS, 190, 210),
			semanticNode(IDENTIFIER, 190, 200),
			semanticNode(METHOD_REFERENCE, 211, 226),
			semanticNode(IDENTIFIER, 211, 217));

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
			semanticNode(COMPILATION_UNIT, 0, 342),
			semanticNode(CLASS_DECLARATION, 0, 55, "Person"),
			semanticNode(METHOD_DECLARATION, 16, 53),
			semanticNode(BLOCK, 34, 53),
			semanticNode(RETURN_STATEMENT, 38, 50),
			semanticNode(NULL_LITERAL, 45, 49),
			semanticNode(CLASS_DECLARATION, 57, 128, "Stream"),
			semanticNode(METHOD_DECLARATION, 73, 126),
			semanticNode(QUALIFIED_NAME, 88, 94),
			semanticNode(BLOCK, 107, 126),
			semanticNode(RETURN_STATEMENT, 111, 123),
			semanticNode(NULL_LITERAL, 118, 122),
			semanticNode(CLASS_DECLARATION, 130, 216, "Collectors"),
			semanticNode(METHOD_DECLARATION, 150, 214),
			semanticNode(QUALIFIED_NAME, 175, 181),
			semanticNode(BLOCK, 195, 214),
			semanticNode(RETURN_STATEMENT, 199, 211),
			semanticNode(NULL_LITERAL, 206, 210),
			semanticNode(CLASS_DECLARATION, 218, 341, "Test"),
			semanticNode(METHOD_DECLARATION, 232, 339),
			semanticNode(QUALIFIED_NAME, 244, 250),
			semanticNode(BLOCK, 260, 339),
			semanticNode(QUALIFIED_NAME, 264, 270),
			semanticNode(METHOD_INVOCATION, 281, 335),
			semanticNode(FIELD_ACCESS, 281, 295),
			semanticNode(IDENTIFIER, 281, 287),
			semanticNode(METHOD_INVOCATION, 296, 334),
			semanticNode(FIELD_ACCESS, 296, 317),
			semanticNode(IDENTIFIER, 296, 306),
			semanticNode(METHOD_REFERENCE, 318, 333),
			semanticNode(IDENTIFIER, 318, 324));

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
			semanticNode(COMPILATION_UNIT, 0, 389),
			semanticNode(CLASS_DECLARATION, 0, 221, "Optional"),
			semanticNode(METHOD_DECLARATION, 18, 66),
			semanticNode(QUALIFIED_NAME, 31, 37),
			semanticNode(BLOCK, 47, 66),
			semanticNode(RETURN_STATEMENT, 51, 63),
			semanticNode(THIS_EXPRESSION, 58, 62),
			semanticNode(METHOD_DECLARATION, 69, 123),
			semanticNode(QUALIFIED_NAME, 85, 91),
			semanticNode(BLOCK, 104, 123),
			semanticNode(RETURN_STATEMENT, 108, 120),
			semanticNode(THIS_EXPRESSION, 115, 119),
			semanticNode(METHOD_DECLARATION, 126, 163),
			semanticNode(QUALIFIED_NAME, 141, 147),
			semanticNode(BLOCK, 159, 163),
			semanticNode(METHOD_DECLARATION, 166, 219),
			semanticNode(QUALIFIED_NAME, 185, 191),
			semanticNode(BLOCK, 200, 219),
			semanticNode(RETURN_STATEMENT, 204, 216),
			semanticNode(NULL_LITERAL, 211, 215),
			semanticNode(CLASS_DECLARATION, 223, 388, "Test"),
			semanticNode(METHOD_DECLARATION, 237, 386),
			semanticNode(BLOCK, 252, 386),
			semanticNode(QUALIFIED_NAME, 256, 264),
			semanticNode(METHOD_INVOCATION, 271, 290),
			semanticNode(FIELD_ACCESS, 271, 282),
			semanticNode(IDENTIFIER, 271, 279),
			semanticNode(STRING_LITERAL, 283, 289),
			semanticNode(METHOD_INVOCATION, 294, 382),
			semanticNode(FIELD_ACCESS, 294, 364),
			semanticNode(METHOD_INVOCATION, 294, 350),
			semanticNode(FIELD_ACCESS, 294, 333),
			semanticNode(METHOD_INVOCATION, 294, 322),
			semanticNode(QUALIFIED_NAME, 294, 301),
			semanticNode(FIELD_ACCESS, 294, 301),
			semanticNode(IDENTIFIER, 294, 297),
			semanticNode(METHOD_REFERENCE, 302, 321),
			semanticNode(IDENTIFIER, 302, 308),
			semanticNode(METHOD_REFERENCE, 334, 349),
			semanticNode(IDENTIFIER, 334, 340),
			semanticNode(METHOD_REFERENCE, 365, 381),
			semanticNode(IDENTIFIER, 365, 371));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
