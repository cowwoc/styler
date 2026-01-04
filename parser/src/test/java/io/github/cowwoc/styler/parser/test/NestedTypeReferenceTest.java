package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing nested type references like {@code Outer.Inner} in field and method declarations.
 */
public class NestedTypeReferenceTest
{
	/**
	 * Tests parsing of a simple nested type in a field declaration.
	 * The type {@code Outer.Inner} should be correctly parsed.
	 */
	@Test
	public void testFieldWithSimpleNestedType()
	{
		String source = """
			class Test
			{
			    Outer.Inner field;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.FIELD_DECLARATION, 17, 35);
			expected.allocateClassDeclaration(0, 37, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 38);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of a deeply nested type in a field declaration.
	 * The type {@code Outer.Middle.Inner} should be correctly parsed,
	 * verifying the parser handles multiple levels of nesting.
	 */
	@Test
	public void testFieldWithDeeplyNestedType()
	{
		String source = """
			class Test
			{
			    Outer.Middle.Inner field;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.FIELD_DECLARATION, 17, 42);
			expected.allocateClassDeclaration(0, 44, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 45);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of a method with nested type as return type.
	 * Validates that nested types work correctly in method return type position.
	 */
	@Test
	public void testMethodReturnTypeWithNestedType()
	{
		String source = """
			class Test
			{
			    Outer.Inner method()
			    {
			        return null;
			    }
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.NULL_LITERAL, 59, 63);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 52, 64);
			expected.allocateNode(NodeType.BLOCK, 42, 70);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 17, 70);
			expected.allocateClassDeclaration(0, 72, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 73);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of nested type with generic type arguments.
	 * Validates that generics work correctly with nested types.
	 */
	@Test
	public void testNestedTypeWithGenericArguments()
	{
		String source = """
			class Test
			{
			    Outer.Inner<String> field;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 35);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 17, 43);
			expected.allocateClassDeclaration(0, 45, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 46);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of nested type array declaration.
	 * Validates that array types work correctly with nested types.
	 */
	@Test
	public void testNestedTypeArray()
	{
		String source = """
			class Test
			{
			    Outer.Inner[] field;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.FIELD_DECLARATION, 17, 37);
			expected.allocateClassDeclaration(0, 39, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 40);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of the real-world {@code ValueLayout.OfInt} pattern from NodeArena.java.
	 * This test validates the actual use case that motivated the bug fix.
	 */
	@Test
	public void testValueLayoutOfIntPattern()
	{
		String source = """
			class Test
			{
			    private static final ValueLayout.OfInt INT_LAYOUT = null;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.NULL_LITERAL, 69, 73);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 17, 74);
			expected.allocateClassDeclaration(0, 76, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 77);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
