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
 * Tests for parsing pattern matching instanceof expressions with the final modifier (Java 16+).
 */
public class FinalPatternMatchingInstanceofTest
{
	/**
	 * Validates parsing of basic pattern matching instanceof with final modifier.
	 * Tests the most common case of final pattern matching in if statements.
	 */
	@Test
	public void shouldParseFinalPatternMatchingInstanceof()
	{
		String source = """
			public class Test
			{
				public void method(Object obj)
				{
					if (obj instanceof final String s)
					{
						System.out.println(s);
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 61, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 82, 88);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 61, 90);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 99, 117);
			expected.allocateNode(NodeType.IDENTIFIER, 99, 105);
			expected.allocateNode(NodeType.FIELD_ACCESS, 99, 109);
			expected.allocateNode(NodeType.FIELD_ACCESS, 99, 117);
			expected.allocateNode(NodeType.IDENTIFIER, 118, 119);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 99, 120);
			expected.allocateNode(NodeType.BLOCK, 94, 125);
			expected.allocateNode(NodeType.IF_STATEMENT, 57, 125);
			expected.allocateNode(NodeType.BLOCK, 53, 128);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 128);
			expected.allocateClassDeclaration(7, 130, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 131);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of pattern matching instanceof with final modifier and generic type.
	 * Tests handling of parameterized types with final in instanceof patterns.
	 */
	@Test
	public void shouldParseFinalPatternMatchingWithGenerics()
	{
		String source = """
			public class Test
			{
				public void method(Object obj)
				{
					if (obj instanceof final Map<?, ?> map)
					{
						System.out.println(map.size());
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateParameterDeclaration(40, 50, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 61, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 82, 85);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 86, 87);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 89, 90);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 82, 91);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 61, 95);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 104, 122);
			expected.allocateNode(NodeType.IDENTIFIER, 104, 110);
			expected.allocateNode(NodeType.FIELD_ACCESS, 104, 114);
			expected.allocateNode(NodeType.FIELD_ACCESS, 104, 122);
			expected.allocateNode(NodeType.IDENTIFIER, 123, 126);
			expected.allocateNode(NodeType.FIELD_ACCESS, 123, 131);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 123, 133);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 104, 134);
			expected.allocateNode(NodeType.BLOCK, 99, 139);
			expected.allocateNode(NodeType.IF_STATEMENT, 57, 139);
			expected.allocateNode(NodeType.BLOCK, 53, 142);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 142);
			expected.allocateClassDeclaration(7, 144, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 145);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
