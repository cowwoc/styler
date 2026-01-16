package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for handling comments in field and method declaration contexts.
 */
public class CommentInFieldDeclarationTest
{
	/**
	 * Tests block comment between type and field name.
	 * Reproduces Spring Framework BridgeMethodResolver pattern: Map/* comment * /declToBridge
	 */
	@Test
	public void testBlockCommentBetweenTypeAndFieldName()
	{
		String source = """
			class Test
			{
				private final Map/* <Class, Set<Signature>> */declToBridge;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 31, 60);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 73);
			expected.allocateClassDeclaration(0, 75, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 76);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests block comment in method return type position.
	 */
	@Test
	public void testBlockCommentInMethodReturnType()
	{
		String source = """
			class Test
			{
				public Map/*<Signature, Signature>*/resolveAll()
				{
					return null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 24, 50);
			expected.allocateNode(NodeType.NULL_LITERAL, 75, 79);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 68, 80);
			expected.allocateNode(NodeType.BLOCK, 64, 83);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 83);
			expected.allocateClassDeclaration(0, 85, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 86);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests array initializer with trailing comma parses correctly.
	 */
	@Test
	public void testArrayInitializerWithTrailingComma()
	{
		String source = """
			class Test
			{
				private final Class<?>[] testClasses = new Class<?>[]
				{
					String.class,
					Integer.class,
				};
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.WILDCARD_TYPE, 34, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 62);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 63, 64);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 57, 65);
			expected.allocateNode(NodeType.IDENTIFIER, 73, 79);
			expected.allocateNode(NodeType.CLASS_LITERAL, 73, 85);
			expected.allocateNode(NodeType.IDENTIFIER, 89, 96);
			expected.allocateNode(NodeType.CLASS_LITERAL, 89, 102);
			expected.allocateNode(NodeType.ARRAY_CREATION, 53, 106);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 107);
			expected.allocateClassDeclaration(0, 109, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 110);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests lambda expression in generic variable declaration.
	 */
	@Test
	public void testLambdaInGenericVariableDeclaration()
	{
		String source = """
			class Test
			{
				void m()
				{
					java.util.function.Function<String, Integer> f = s -> s.length();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 62);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 62);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 64, 71);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 64, 71);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 72);
			expected.allocateNode(NodeType.IDENTIFIER, 82, 83);
			expected.allocateNode(NodeType.FIELD_ACCESS, 82, 90);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 82, 92);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 77, 92);
			expected.allocateNode(NodeType.BLOCK, 24, 96);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 96);
			expected.allocateClassDeclaration(0, 98, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 99);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests generic method call chain parses correctly.
	 */
	@Test
	public void testGenericMethodCallChain()
	{
		String source = """
			class Test
			{
				void m()
				{
					java.util.Map<String, Integer> map = java.util.Map.of("a", 13);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 57);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 57);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 58);
			expected.allocateNode(NodeType.IDENTIFIER, 65, 69);
			expected.allocateNode(NodeType.FIELD_ACCESS, 65, 74);
			expected.allocateNode(NodeType.FIELD_ACCESS, 65, 78);
			expected.allocateNode(NodeType.FIELD_ACCESS, 65, 81);
			expected.allocateNode(NodeType.STRING_LITERAL, 82, 85);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 87, 89);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 65, 90);
			expected.allocateNode(NodeType.BLOCK, 24, 94);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 94);
			expected.allocateClassDeclaration(0, 96, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 97);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
