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
 * Tests for parsing wildcard type nodes.
 * Validates correct AST structure generation for unbounded, upper-bounded, and lower-bounded wildcards.
 */
public class WildcardTypeParserTest
{
	/**
	 * Tests parsing of unbounded wildcard type argument.
	 * The {@code ?} wildcard represents an unknown type, commonly used
	 * when the specific type parameter is irrelevant to the operation.
	 * This test verifies that the WILDCARD_TYPE node is created for unbounded wildcards.
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
	 * This test verifies that WILDCARD_TYPE node is created for upper bounds.
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
	 * This test verifies that WILDCARD_TYPE node is created for lower bounds.
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
	 * Tests parsing of nested wildcard in generic type.
	 * Verifies that WILDCARD_TYPE nodes are correctly created when wildcards
	 * appear inside nested generic type arguments like {@code Map<String, List<?>>}.
	 */
	@Test
	public void testNestedWildcard()
	{
		String source = """
			class Test
			{
				Map<String, List<?>> map;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 30);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 31, 32);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 26, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 34);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 39);
			expected.allocateClassDeclaration(0, 41, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 42);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of multiple wildcard type arguments.
	 * Verifies that multiple WILDCARD_TYPE nodes are created in the same generic
	 * declaration, such as {@code Map<?, ?>}.
	 */
	@Test
	public void testMultipleWildcards()
	{
		String source = """
			class Test
			{
				Map<?, ?> map;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.WILDCARD_TYPE, 18, 19);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 21, 22);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 28);
			expected.allocateClassDeclaration(0, 30, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 31);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of wildcard in field declaration.
	 * Verifies WILDCARD_TYPE node creation in field type declarations,
	 * ensuring the parser handles wildcards consistently across different declaration contexts.
	 */
	@Test
	public void testWildcardInFieldDeclaration()
	{
		String source = """
			class Test
			{
				List<?> items;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.WILDCARD_TYPE, 19, 20);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 28);
			expected.allocateClassDeclaration(0, 30, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 31);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of upper-bounded wildcard in nested generic context.
	 * Verifies WILDCARD_TYPE node creation for bounded wildcards nested inside
	 * other generic types, such as {@code Map<String, List<? extends Number>>}.
	 */
	@Test
	public void testUpperBoundedWildcardInNestedGeneric()
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
	 * Tests parsing of lower-bounded wildcard in nested generic context.
	 * Verifies WILDCARD_TYPE node creation for lower-bounded wildcards nested inside
	 * other generic types, such as {@code Map<String, Consumer<? super Integer>>}.
	 */
	@Test
	public void testLowerBoundedWildcardInNestedGeneric()
	{
		String source = """
			class Test
			{
				Map<String, Consumer<? super Integer>> map;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 50);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 35, 50);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 26, 52);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 52);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 57);
			expected.allocateClassDeclaration(0, 59, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of wildcard in method return type.
	 * Verifies that WILDCARD_TYPE nodes are created when wildcards appear
	 * in method return type declarations.
	 */
	@Test
	public void testWildcardInReturnType()
	{
		String source = """
			class Test
			{
				List<?> getItems()
				{
					return null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.WILDCARD_TYPE, 19, 20);
			expected.allocateNode(NodeType.NULL_LITERAL, 45, 49);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 38, 50);
			expected.allocateNode(NodeType.BLOCK, 34, 53);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
