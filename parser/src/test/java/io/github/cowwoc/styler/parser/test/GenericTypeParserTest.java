package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 32);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 33, 39);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 33, 39);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 52, 61);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 52, 63);
			expected.allocateNode(NodeType.OBJECT_CREATION, 48, 65);
			expected.allocateNode(NodeType.BLOCK, 24, 69);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 69);
			expected.allocateClassDeclaration(0, 71, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 72);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 47);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 59, 66);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 59, 68);
			expected.allocateNode(NodeType.OBJECT_CREATION, 55, 70);
			expected.allocateNode(NodeType.BLOCK, 24, 74);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 74);
			expected.allocateClassDeclaration(0, 76, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 77);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 32);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 33, 39);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 33, 39);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 52, 61);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 52, 63);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 68, 75);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 68, 77);
			expected.allocateNode(NodeType.OBJECT_CREATION, 64, 79);
			expected.allocateNode(NodeType.OBJECT_CREATION, 48, 80);
			expected.allocateNode(NodeType.BLOCK, 24, 84);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 84);
			expected.allocateClassDeclaration(0, 86, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 87);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 35);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 36, 37);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 27, 38);
			expected.allocateParameterDeclaration(27, 42, new ParameterAttribute("opt", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 45, 49);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 49);
			expected.allocateClassDeclaration(0, 51, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 52);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 32, 48);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 27, 49);
			expected.allocateParameterDeclaration(27, 57, new ParameterAttribute("numbers", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 60, 64);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 64);
			expected.allocateClassDeclaration(0, 66, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 67);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 50);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 35, 50);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 26, 51);
			expected.allocateParameterDeclaration(26, 60, new ParameterAttribute("consumer", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 63, 67);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 67);
			expected.allocateClassDeclaration(0, 69, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 70);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 30);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 38);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 26, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 40);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 45);
			expected.allocateClassDeclaration(0, 47, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 48);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 30);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 31, 47);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 26, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 49);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 54);
			expected.allocateClassDeclaration(0, 56, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 57);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 30);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 38);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 26, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 51, 58);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 51, 60);
			expected.allocateNode(NodeType.OBJECT_CREATION, 47, 62);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 63);
			expected.allocateClassDeclaration(0, 65, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 33, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 33, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 49);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 56);
			expected.allocateClassDeclaration(0, 58, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 59);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 29);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 37);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 37);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 43);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 39, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 53);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 26, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 53);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 59);
			expected.allocateClassDeclaration(0, 61, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
