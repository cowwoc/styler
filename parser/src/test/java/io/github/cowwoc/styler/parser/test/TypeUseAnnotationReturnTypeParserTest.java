package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing type-use annotations on method return types.
 */
public class TypeUseAnnotationReturnTypeParserTest
{
	/**
	 * Tests parsing a method with a type-use annotation on the return type.
	 * Pattern: public @Nullable String getValue()
	 */
	@Test
	public void shouldParseAnnotatedReturnType()
	{
		String source = """
			class Service
			{
				public @Nullable String getValue()
				{
					return null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 33);
			expected.allocateNode(NodeType.ANNOTATION, 24, 33);
			expected.allocateNode(NodeType.NULL_LITERAL, 64, 68);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 57, 69);
			expected.allocateNode(NodeType.BLOCK, 53, 72);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 17, 72);
			expected.allocateClassDeclaration(0, 74, new TypeDeclarationAttribute("Service"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 75);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing a type-use annotation on a primitive return type.
	 * Pattern: public @Positive int getCount()
	 */
	@Test
	public void shouldParseAnnotatedPrimitiveReturnType()
	{
		String source = """
			class Counter
			{
				public @Positive int getCount()
				{
					return 1;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 33);
			expected.allocateNode(NodeType.ANNOTATION, 24, 33);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 61, 62);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 54, 63);
			expected.allocateNode(NodeType.BLOCK, 50, 66);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 17, 66);
			expected.allocateClassDeclaration(0, 68, new TypeDeclarationAttribute("Counter"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 69);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Regression test: verifies that methods without type-use annotations still parse correctly.
	 */
	@Test
	public void shouldParseUnannotatedReturnTypeUnaffected()
	{
		String source = """
			class Service
			{
				public String getValue()
				{
					return "";
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.STRING_LITERAL, 54, 56);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 47, 57);
			expected.allocateNode(NodeType.BLOCK, 43, 60);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 17, 60);
			expected.allocateClassDeclaration(0, 62, new TypeDeclarationAttribute("Service"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 63);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
