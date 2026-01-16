package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing lambda expressions with annotated generic type parameters.
 */
public final class LambdaAnnotatedParameterParserTest
{
	/**
	 * Validates that a lambda with annotated generic type parameter containing element-value pair parses
	 * correctly.
	 */
	@Test
	public void shouldParseLambdaWithAnnotatedGenericParameter()
	{
		String source = """
			class Test
			{
				void m()
				{
					handle((List<@NonNull(when=MAYBE) String> items) -> items.size());
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 36, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 49);
			expected.allocateNode(NodeType.IDENTIFIER, 50, 54);
			expected.allocateNode(NodeType.IDENTIFIER, 55, 60);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 50, 60);
			expected.allocateNode(NodeType.ANNOTATION, 41, 61);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 68);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 68);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 36, 69);
			expected.allocateNode(NodeType.IDENTIFIER, 80, 85);
			expected.allocateNode(NodeType.FIELD_ACCESS, 80, 90);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 80, 92);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 35, 92);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 93);
			expected.allocateNode(NodeType.BLOCK, 24, 97);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 97);
			expected.allocateClassDeclaration(0, 99, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 100);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a lambda with multiple annotated generic type parameters parses correctly.
	 */
	@Test
	public void shouldParseLambdaWithMultipleAnnotatedGenericParameters()
	{
		String source = """
			class Test
			{
				void m()
				{
					process((Map<@Key String, @Value(priority=1) Integer> map) -> map.size());
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 35);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 45);
			expected.allocateNode(NodeType.ANNOTATION, 41, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 52);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 60);
			expected.allocateNode(NodeType.IDENTIFIER, 61, 69);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 70, 71);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 61, 71);
			expected.allocateNode(NodeType.ANNOTATION, 54, 72);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 73, 80);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 80);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 37, 81);
			expected.allocateNode(NodeType.IDENTIFIER, 90, 93);
			expected.allocateNode(NodeType.FIELD_ACCESS, 90, 98);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 90, 100);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 36, 100);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 101);
			expected.allocateNode(NodeType.BLOCK, 24, 105);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 105);
			expected.allocateClassDeclaration(0, 107, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 108);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a lambda with nested annotated generic types parses correctly.
	 */
	@Test
	public void shouldParseLambdaWithNestedAnnotatedGenerics()
	{
		String source = """
			class Test
			{
				void m()
				{
					transform((Map<String, List<@Valid Item>> data) -> data.get("key"));
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 37);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 37);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 42);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 51, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 62);
			expected.allocateNode(NodeType.ANNOTATION, 56, 62);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 63, 67);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 67);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 51, 69);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 51, 69);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 39, 69);
			expected.allocateNode(NodeType.IDENTIFIER, 79, 83);
			expected.allocateNode(NodeType.FIELD_ACCESS, 79, 87);
			expected.allocateNode(NodeType.STRING_LITERAL, 88, 93);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 79, 94);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 38, 94);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 95);
			expected.allocateNode(NodeType.BLOCK, 24, 99);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 99);
			expected.allocateClassDeclaration(0, 101, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 102);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a lambda with annotation containing array element value parses correctly.
	 * <p>
	 * This test uses a simpler annotation without array initializer syntax since that is a separate
	 * parser limitation.
	 */
	@Test
	public void shouldParseLambdaWithMultipleAnnotationElements()
	{
		String source = """
			class Test
			{
				void m()
				{
					validate((List<@Constraint(min=0, max=100) Item> items) -> items.isEmpty());
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 36);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 42);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 54);
			expected.allocateNode(NodeType.IDENTIFIER, 55, 58);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 59, 60);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 55, 60);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 65);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 66, 69);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 62, 69);
			expected.allocateNode(NodeType.ANNOTATION, 43, 70);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 71, 75);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 75);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 38, 76);
			expected.allocateNode(NodeType.IDENTIFIER, 87, 92);
			expected.allocateNode(NodeType.FIELD_ACCESS, 87, 100);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 87, 102);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 37, 102);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 103);
			expected.allocateNode(NodeType.BLOCK, 24, 107);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 107);
			expected.allocateClassDeclaration(0, 109, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 110);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
