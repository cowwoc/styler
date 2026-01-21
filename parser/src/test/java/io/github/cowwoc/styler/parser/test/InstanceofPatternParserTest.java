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
 * Tests for parsing instanceof expressions with pattern matching (Java 16+).
 */
public final class InstanceofPatternParserTest
{
	/**
	 * Validates that a simple instanceof pattern with binding variable parses correctly.
	 */
	@Test
	public void shouldParseSimpleInstanceofPattern()
	{
		String source = """
			class Test
			{
				void m()
				{
					if (obj instanceof String s)
						System.out.println(s);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 32, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 53);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 32, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 60, 78);
			expected.allocateNode(NodeType.IDENTIFIER, 60, 66);
			expected.allocateNode(NodeType.FIELD_ACCESS, 60, 70);
			expected.allocateNode(NodeType.FIELD_ACCESS, 60, 78);
			expected.allocateNode(NodeType.IDENTIFIER, 79, 80);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 60, 81);
			expected.allocateNode(NodeType.IF_STATEMENT, 28, 82);
			expected.allocateNode(NodeType.BLOCK, 24, 85);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 85);
			expected.allocateClassDeclaration(0, 87, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 88);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an instanceof pattern with contextual keyword 'module' as binding variable parses
	 * correctly.
	 */
	@Test
	public void shouldParseInstanceofPatternWithModuleBinding()
	{
		String source = """
			class Test
			{
				void m(Object input)
				{
					if (input instanceof Module module)
						process(module);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 21, 27);
			expected.allocateParameterDeclaration(21, 33,
				new ParameterAttribute("input", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 44, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 61, 67);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 44, 74);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 79, 86);
			expected.allocateNode(NodeType.IDENTIFIER, 79, 86);
			expected.allocateNode(NodeType.IDENTIFIER, 87, 93);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 79, 94);
			expected.allocateNode(NodeType.IF_STATEMENT, 40, 95);
			expected.allocateNode(NodeType.BLOCK, 36, 98);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 98);
			expected.allocateClassDeclaration(0, 100, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 101);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an instanceof without pattern variable (backward compatibility) parses correctly.
	 */
	@Test
	public void shouldParseInstanceofWithoutPattern()
	{
		String source = """
			class Test
			{
				void m()
				{
					if (obj instanceof String)
						doSomething();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 32, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 53);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 32, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 58, 69);
			expected.allocateNode(NodeType.IDENTIFIER, 58, 69);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 58, 71);
			expected.allocateNode(NodeType.IF_STATEMENT, 28, 72);
			expected.allocateNode(NodeType.BLOCK, 24, 75);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 75);
			expected.allocateClassDeclaration(0, 77, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 78);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an instanceof pattern in a ternary conditional expression parses correctly.
	 */
	@Test
	public void shouldParseInstanceofPatternInTernary()
	{
		String source = """
			class Test
			{
				String m(Object obj)
				{
					return (obj instanceof String s) ? s : "default";
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 29);
			expected.allocateParameterDeclaration(23, 33,
				new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 48, 51);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 63, 69);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 48, 71);
			expected.allocateNode(NodeType.IDENTIFIER, 75, 76);
			expected.allocateNode(NodeType.STRING_LITERAL, 79, 88);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 48, 88);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 40, 89);
			expected.allocateNode(NodeType.BLOCK, 36, 92);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 92);
			expected.allocateClassDeclaration(0, 94, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 95);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an instanceof pattern with qualified type parses correctly.
	 */
	@Test
	public void shouldParseInstanceofPatternWithQualifiedType()
	{
		String source = """
			class Test
			{
				void m()
				{
					if (obj instanceof java.lang.String s)
						use(s);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 32, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 63);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 32, 65);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 70, 73);
			expected.allocateNode(NodeType.IDENTIFIER, 70, 73);
			expected.allocateNode(NodeType.IDENTIFIER, 74, 75);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 70, 76);
			expected.allocateNode(NodeType.IF_STATEMENT, 28, 77);
			expected.allocateNode(NodeType.BLOCK, 24, 80);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 80);
			expected.allocateClassDeclaration(0, 82, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 83);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
