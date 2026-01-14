package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for method reference parsing with parameterized types.
 */
public class ParameterizedMethodReferenceParserTest
{
	/**
	 * Tests constructor reference with type arguments.
	 * Pattern: {@code ArrayList<String>::new}
	 */
	@Test
	public void testGenericConstructorReference()
	{
		String source = """
			class Test
			{
				Object s = ArrayList<String>::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 25, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 35, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 35, 41);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 47);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 48);
			expected.allocateClassDeclaration(0, 50, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 51);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests method reference on parameterized type.
	 * Pattern: {@code List<String>::size}
	 */
	@Test
	public void testMethodReferenceOnParameterizedType()
	{
		String source = """
			class Test
			{
				Object f = List<String>::size;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 25, 29);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 43);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 44);
			expected.allocateClassDeclaration(0, 46, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 47);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests constructor reference with nested type arguments.
	 * Pattern: {@code Map<String, List<Integer>>::new}
	 */
	@Test
	public void testNestedGenericConstructorReference()
	{
		String source = """
			class Test
			{
				Object m = Map<String, List<Integer>>::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 25, 28);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 49);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 37, 51);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 51);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 56);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 57);
			expected.allocateClassDeclaration(0, 59, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests that less-than still works as comparison operator.
	 * Ensures the fix does not break normal comparison expressions.
	 */
	@Test
	public void testLessThanStillWorksAsComparison()
	{
		String source = """
			class Test
			{
				boolean b = x < y;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 26, 27);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 31);
			expected.allocateNode(NodeType.IDENTIFIER, 30, 31);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 26, 31);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 32);
			expected.allocateClassDeclaration(0, 34, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 35);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
