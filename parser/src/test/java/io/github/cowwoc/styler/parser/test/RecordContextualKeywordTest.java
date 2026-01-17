package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing {@code record} as a contextual keyword in variable declarations.
 */
public final class RecordContextualKeywordTest
{
	/**
	 * Verifies that {@code record} can be used as a variable type in a declaration.
	 */
	@Test
	public void recordAsVariableType()
	{
		String source = """
			class Test
			{
				void test()
				{
					record foo = "bar";
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for the type "record" being parsed as qualified name
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 37);
			expected.allocateNode(NodeType.STRING_LITERAL, 44, 49);
			expected.allocateNode(NodeType.BLOCK, 27, 53);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code record} can be used in a multi-variable declaration.
	 */
	@Test
	public void recordInMultiDeclaration()
	{
		String source = """
			class Test
			{
				void test()
				{
					record a, b;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for the type "record"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 37);
			expected.allocateNode(NodeType.BLOCK, 27, 46);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 46);
			expected.allocateClassDeclaration(0, 48, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 49);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code record} can be used as both type and variable name.
	 */
	@Test
	public void recordAsBothTypeAndVariable()
	{
		String source = """
			class Test
			{
				void test()
				{
					record record = null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for the type "record"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 37);
			expected.allocateNode(NodeType.NULL_LITERAL, 47, 51);
			expected.allocateNode(NodeType.BLOCK, 27, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code record} can be used as a variable name for method invocation.
	 * Tests real-world Spring Framework pattern where {@code record} is a variable, not a type declaration.
	 */
	@Test
	public void recordAsVariableWithMethodCall()
	{
		String source = """
			class Test
			{
				void test()
				{
					record.put("key", "value");
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 41);
			expected.allocateNode(NodeType.IDENTIFIER, 31, 37);
			expected.allocateNode(NodeType.FIELD_ACCESS, 31, 41);
			expected.allocateNode(NodeType.STRING_LITERAL, 42, 47);
			expected.allocateNode(NodeType.STRING_LITERAL, 49, 56);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 31, 57);
			expected.allocateNode(NodeType.BLOCK, 27, 61);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 61);
			expected.allocateClassDeclaration(0, 63, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 64);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
