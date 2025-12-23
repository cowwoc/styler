package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.OBJECT_CREATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, 72),
			semanticNode(CLASS_DECLARATION, 0, 71, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 69),
			semanticNode(BLOCK, 24, 69),
			semanticNode(QUALIFIED_NAME, 28, 32),
			semanticNode(QUALIFIED_NAME, 33, 39),
			semanticNode(OBJECT_CREATION, 48, 65),
			semanticNode(QUALIFIED_NAME, 52, 61));
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
			semanticNode(COMPILATION_UNIT, 0, 77),
			semanticNode(CLASS_DECLARATION, 0, 76, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 74),
			semanticNode(BLOCK, 24, 74),
			semanticNode(QUALIFIED_NAME, 28, 31),
			semanticNode(QUALIFIED_NAME, 32, 38),
			semanticNode(QUALIFIED_NAME, 40, 47),
			semanticNode(OBJECT_CREATION, 55, 70),
			semanticNode(QUALIFIED_NAME, 59, 66));
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
			semanticNode(COMPILATION_UNIT, 0, 87),
			semanticNode(CLASS_DECLARATION, 0, 86, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 84),
			semanticNode(BLOCK, 24, 84),
			semanticNode(QUALIFIED_NAME, 28, 32),
			semanticNode(QUALIFIED_NAME, 33, 39),
			semanticNode(OBJECT_CREATION, 48, 80),
			semanticNode(QUALIFIED_NAME, 52, 61),
			semanticNode(OBJECT_CREATION, 64, 79),
			semanticNode(QUALIFIED_NAME, 68, 75));
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
			semanticNode(COMPILATION_UNIT, 0, 52),
			semanticNode(CLASS_DECLARATION, 0, 51, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 49),
			semanticNode(QUALIFIED_NAME, 27, 35),
			semanticNode(BLOCK, 45, 49));
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
			semanticNode(COMPILATION_UNIT, 0, 67),
			semanticNode(CLASS_DECLARATION, 0, 66, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 64),
			semanticNode(QUALIFIED_NAME, 27, 31),
			semanticNode(QUALIFIED_NAME, 42, 48),
			semanticNode(BLOCK, 60, 64));
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
			semanticNode(COMPILATION_UNIT, 0, 70),
			semanticNode(CLASS_DECLARATION, 0, 69, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 67),
			semanticNode(QUALIFIED_NAME, 26, 34),
			semanticNode(QUALIFIED_NAME, 43, 50),
			semanticNode(BLOCK, 63, 67));
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
			semanticNode(COMPILATION_UNIT, 0, 48),
			semanticNode(CLASS_DECLARATION, 0, 47, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 45),
			semanticNode(QUALIFIED_NAME, 18, 24),
			semanticNode(QUALIFIED_NAME, 26, 30),
			semanticNode(QUALIFIED_NAME, 31, 38));
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
			semanticNode(COMPILATION_UNIT, 0, 57),
			semanticNode(CLASS_DECLARATION, 0, 56, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 54),
			semanticNode(QUALIFIED_NAME, 18, 24),
			semanticNode(QUALIFIED_NAME, 26, 30),
			semanticNode(QUALIFIED_NAME, 41, 47));
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
			semanticNode(COMPILATION_UNIT, 0, 66),
			semanticNode(CLASS_DECLARATION, 0, 65, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 63),
			semanticNode(QUALIFIED_NAME, 18, 24),
			semanticNode(QUALIFIED_NAME, 26, 30),
			semanticNode(QUALIFIED_NAME, 31, 38),
			semanticNode(OBJECT_CREATION, 47, 62),
			semanticNode(QUALIFIED_NAME, 51, 58));
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
			semanticNode(COMPILATION_UNIT, 0, 59),
			semanticNode(CLASS_DECLARATION, 0, 58, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 56),
			semanticNode(QUALIFIED_NAME, 25, 31),
			semanticNode(QUALIFIED_NAME, 33, 40),
			semanticNode(QUALIFIED_NAME, 42, 49));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
