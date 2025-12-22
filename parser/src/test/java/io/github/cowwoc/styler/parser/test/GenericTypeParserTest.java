package io.github.cowwoc.styler.parser.test;

import org.testng.annotations.Test;

import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseSucceeds;

/**
 * Tests for generic type parsing including diamond operator, wildcards, and nested generics.
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
		assertParseSucceeds("""
			class Test
			{
				void m()
				{
					List<String> list = new ArrayList<>();
				}
			}
			""");
	}

	/**
	 * Tests parsing of diamond operator with HashMap.
	 * HashMap uses two type parameters, verifying diamond operator works
	 * regardless of the number of expected type arguments.
	 */
	@Test
	public void testDiamondOperatorWithHashMap()
	{
		assertParseSucceeds("""
			class Test
			{
				void m()
				{
					Map<String, Integer> map = new HashMap<>();
				}
			}
			""");
	}

	/**
	 * Tests parsing of chained diamond operators in nested expressions.
	 * Verifies that diamond operators work correctly when one constructor
	 * argument is another generic instantiation with diamond operator.
	 */
	@Test
	public void testChainedDiamondOperator()
	{
		assertParseSucceeds("""
			class Test
			{
				void m()
				{
					List<String> list = new ArrayList<>(new HashSet<>());
				}
			}
			""");
	}

	/**
	 * Tests parsing of unbounded wildcard type argument.
	 * The {@code ?} wildcard represents an unknown type, commonly used
	 * when the specific type parameter is irrelevant to the operation.
	 */
	@Test
	public void testUnboundedWildcard()
	{
		assertParseSucceeds("""
			class Test
			{
				void process(Optional<?> opt)
				{
				}
			}
			""");
	}

	/**
	 * Tests parsing of upper-bounded wildcard type argument.
	 * The {@code ? extends T} syntax restricts the unknown type to be
	 * a subtype of T, enabling covariant type relationships.
	 */
	@Test
	public void testUpperBoundedWildcard()
	{
		assertParseSucceeds("""
			class Test
			{
				void process(List<? extends Number> numbers)
				{
				}
			}
			""");
	}

	/**
	 * Tests parsing of lower-bounded wildcard type argument.
	 * The {@code ? super T} syntax restricts the unknown type to be
	 * a supertype of T, enabling contravariant type relationships.
	 */
	@Test
	public void testLowerBoundedWildcard()
	{
		assertParseSucceeds("""
			class Test
			{
				void accept(Consumer<? super Integer> consumer)
				{
				}
			}
			""");
	}

	/**
	 * Tests parsing of nested generic types.
	 * Map with a List value type requires the parser to handle
	 * multiple levels of type argument nesting.
	 */
	@Test
	public void testNestedGenerics()
	{
		assertParseSucceeds("""
			class Test
			{
				Map<String, List<Integer>> map;
			}
			""");
	}

	/**
	 * Tests parsing of nested generic types with wildcard.
	 * Combines nested generics with bounded wildcard to verify
	 * parser handles complex nested type argument structures.
	 */
	@Test
	public void testNestedWildcard()
	{
		assertParseSucceeds("""
			class Test
			{
				Map<String, List<? extends Number>> map;
			}
			""");
	}

	/**
	 * Tests parsing of combined field declaration with diamond initialization.
	 * Common Java pattern where field type is explicit but
	 * constructor uses diamond operator for type inference.
	 */
	@Test
	public void testCombinedDeclarationAndDiamond()
	{
		assertParseSucceeds("""
			class Test
			{
				Map<String, List<Integer>> map = new HashMap<>();
			}
			""");
	}

	/**
	 * Tests parsing of multiple type parameters in a generic type.
	 * Verifies parser correctly handles comma-separated type arguments
	 * in both declaration and usage contexts.
	 */
	@Test
	public void testMultipleTypeParameters()
	{
		assertParseSucceeds("""
			class Test
			{
				BiFunction<String, Integer, Boolean> func;
			}
			""");
	}
}
