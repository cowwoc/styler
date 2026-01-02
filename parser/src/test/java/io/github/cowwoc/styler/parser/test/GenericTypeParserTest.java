package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

/**
 * Tests for parsing generic types including diamond operator, wildcards, and nested generics.
 */
public class GenericTypeParserTest
{
	/**
	 * Tests parsing of diamond operator in object creation expressions.
	 * The diamond operator {@code <>} allows type inference for generic types,
	 * eliminating the need to repeat type arguments.
	 */
	@Test
	public void testDiamondOperator()
	{
		String source = """
			class Test
			{
				void m()
				{
					List<String> list = new ArrayList<>();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 72),
			typeDeclaration(CLASS_DECLARATION, 0, 71, "Test"),
			methodDeclaration( 14, 69),
			block( 24, 69),
			parameterizedType( 28, 40),
			qualifiedName( 28, 32),
			qualifiedName( 33, 39),
			objectCreation( 48, 65),
			parameterizedType( 52, 63),
			qualifiedName( 52, 61));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of diamond operator with HashMap.
	 * HashMap uses two type parameters, verifying diamond operator works
	 * regardless of the number of expected type arguments.
	 */
	@Test
	public void testDiamondOperatorWithHashMap()
	{
		String source = """
			class Test
			{
				void m()
				{
					Map<String, Integer> map = new HashMap<>();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 77),
			typeDeclaration(CLASS_DECLARATION, 0, 76, "Test"),
			methodDeclaration( 14, 74),
			block( 24, 74),
			parameterizedType( 28, 48),
			qualifiedName( 28, 31),
			qualifiedName( 32, 38),
			qualifiedName( 40, 47),
			objectCreation( 55, 70),
			parameterizedType( 59, 68),
			qualifiedName( 59, 66));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of chained diamond operators in nested expressions.
	 * Verifies that diamond operators work correctly when one constructor
	 * argument is another generic instantiation with diamond operator.
	 */
	@Test
	public void testChainedDiamondOperator()
	{
		String source = """
			class Test
			{
				void m()
				{
					List<String> list = new ArrayList<>(new HashSet<>());
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 87),
			typeDeclaration(CLASS_DECLARATION, 0, 86, "Test"),
			methodDeclaration( 14, 84),
			block( 24, 84),
			parameterizedType( 28, 40),
			qualifiedName( 28, 32),
			qualifiedName( 33, 39),
			objectCreation( 48, 80),
			parameterizedType( 52, 63),
			qualifiedName( 52, 61),
			objectCreation( 64, 79),
			parameterizedType( 68, 77),
			qualifiedName( 68, 75));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of unbounded wildcard type argument.
	 * The {@code ?} wildcard represents an unknown type, commonly used
	 * when the specific type parameter is irrelevant to the operation.
	 */
	@Test
	public void testUnboundedWildcard()
	{
		String source = """
			class Test
			{
				void process(Optional<?> opt)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 52),
			typeDeclaration(CLASS_DECLARATION, 0, 51, "Test"),
			methodDeclaration( 14, 49),
			parameterizedType( 27, 38),
			qualifiedName( 27, 35),
			wildcardType( 36, 37),
			parameterNode( 27, 42, "opt"),
			block( 45, 49));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of upper-bounded wildcard type argument.
	 * The {@code ? extends T} syntax restricts the unknown type to be
	 * a subtype of T, enabling covariant type relationships.
	 */
	@Test
	public void testUpperBoundedWildcard()
	{
		String source = """
			class Test
			{
				void process(List<? extends Number> numbers)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 67),
			typeDeclaration(CLASS_DECLARATION, 0, 66, "Test"),
			methodDeclaration( 14, 64),
			parameterizedType( 27, 49),
			qualifiedName( 27, 31),
			wildcardType( 32, 48),
			qualifiedName( 42, 48),
			parameterNode( 27, 57, "numbers"),
			block( 60, 64));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of lower-bounded wildcard type argument.
	 * The {@code ? super T} syntax restricts the unknown type to be
	 * a supertype of T, enabling contravariant type relationships.
	 */
	@Test
	public void testLowerBoundedWildcard()
	{
		String source = """
			class Test
			{
				void accept(Consumer<? super Integer> consumer)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 70),
			typeDeclaration(CLASS_DECLARATION, 0, 69, "Test"),
			methodDeclaration( 14, 67),
			parameterizedType( 26, 51),
			qualifiedName( 26, 34),
			wildcardType( 35, 50),
			qualifiedName( 43, 50),
			parameterNode( 26, 60, "consumer"),
			block( 63, 67));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of nested generic types.
	 * Map with a List value type requires the parser to handle
	 * multiple levels of type argument nesting.
	 */
	@Test
	public void testNestedGenerics()
	{
		String source = """
			class Test
			{
				Map<String, List<Integer>> map;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 48),
			typeDeclaration(CLASS_DECLARATION, 0, 47, "Test"),
			fieldDeclaration( 14, 45),
			parameterizedType( 26, 40),
			qualifiedName( 18, 24),
			qualifiedName( 26, 30),
			qualifiedName( 26, 40),
			qualifiedName( 31, 38));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of nested generic types with wildcard.
	 * Combines nested generics with bounded wildcard to verify
	 * parser handles complex nested type argument structures.
	 */
	@Test
	public void testNestedWildcard()
	{
		String source = """
			class Test
			{
				Map<String, List<? extends Number>> map;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 57),
			typeDeclaration(CLASS_DECLARATION, 0, 56, "Test"),
			fieldDeclaration( 14, 54),
			parameterizedType( 26, 49),
			qualifiedName( 18, 24),
			qualifiedName( 26, 30),
			qualifiedName( 26, 49),
			wildcardType( 31, 47),
			qualifiedName( 41, 47));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of combined field declaration with diamond initialization.
	 * Common Java pattern where field type is explicit but
	 * constructor uses diamond operator for type inference.
	 */
	@Test
	public void testCombinedDeclarationAndDiamond()
	{
		String source = """
			class Test
			{
				Map<String, List<Integer>> map = new HashMap<>();
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 66),
			typeDeclaration(CLASS_DECLARATION, 0, 65, "Test"),
			fieldDeclaration( 14, 63),
			parameterizedType( 26, 40),
			qualifiedName( 18, 24),
			qualifiedName( 26, 30),
			qualifiedName( 26, 40),
			qualifiedName( 31, 38),
			objectCreation( 47, 62),
			parameterizedType( 51, 60),
			qualifiedName( 51, 58));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of multiple type parameters in a generic type.
	 * Verifies parser correctly handles comma-separated type arguments
	 * in both declaration and usage contexts.
	 */
	@Test
	public void testMultipleTypeParameters()
	{
		String source = """
			class Test
			{
				BiFunction<String, Integer, Boolean> func;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 59),
			typeDeclaration(CLASS_DECLARATION, 0, 58, "Test"),
			fieldDeclaration( 14, 56),
			qualifiedName( 25, 31),
			qualifiedName( 33, 40),
			qualifiedName( 42, 49));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of deeply nested generics (3+ levels) with URSHIFT token splitting.
	 * The {@code >>>} operator in nested generics must be correctly split by the parser
	 * to distinguish it from type argument closing brackets.
	 */
	@Test
	public void testDeeplyNestedGenerics()
	{
		String source = """
			class Test
			{
				Map<String, Map<Integer, List<String>>> deep;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 62),
			typeDeclaration(CLASS_DECLARATION, 0, 61, "Test"),
			fieldDeclaration( 14, 59),
			parameterizedType( 26, 53),
			parameterizedType( 39, 53),
			qualifiedName( 18, 24),
			qualifiedName( 26, 29),
			qualifiedName( 26, 53),
			qualifiedName( 30, 37),
			qualifiedName( 39, 43),
			qualifiedName( 39, 53),
			qualifiedName( 44, 50));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
