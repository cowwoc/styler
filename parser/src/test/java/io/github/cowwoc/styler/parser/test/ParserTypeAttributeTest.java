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
 * Tests for type declaration attribute population during parsing.
 */
public class ParserTypeAttributeTest
{
	/**
	 * Verifies that a class declaration has its type name attribute populated.
	 */
	@Test
	public void shouldPopulateTypeNameForClass()
	{
		String source = """
			class MyClass {}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateClassDeclaration(0, 16, new TypeDeclarationAttribute("MyClass"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 17);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a class declaration has its name position attribute correctly set.
	 */
	@Test
	public void shouldPopulateNamePositionForClass()
	{
		String source = """
			class MyClass {}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateClassDeclaration(0, 16, new TypeDeclarationAttribute("MyClass"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 17);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that an interface declaration has its type name attribute populated.
	 */
	@Test
	public void shouldPopulateTypeNameForInterface()
	{
		String source = """
			interface MyInterface {}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateInterfaceDeclaration(0, 24, new TypeDeclarationAttribute("MyInterface"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 25);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that an enum declaration has its type name attribute populated.
	 */
	@Test
	public void shouldPopulateTypeNameForEnum()
	{
		String source = """
			enum MyEnum { A, B }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.ENUM_CONSTANT, 14, 15);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 17, 18);
			expected.allocateEnumDeclaration(0, 20, new TypeDeclarationAttribute("MyEnum"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 21);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a record declaration has its type name attribute populated.
	 */
	@Test
	public void shouldPopulateTypeNameForRecord()
	{
		String source = """
			record MyRecord(int x) {}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateParameterDeclaration(16, 21, new ParameterAttribute("x", false, false, false));
			expected.allocateRecordDeclaration(0, 25, new TypeDeclarationAttribute("MyRecord"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 26);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that nested type declarations each have their own attributes.
	 */
	@Test
	public void shouldPopulateAttributesForNestedTypes()
	{
		String source = """
			class Outer { class Inner {} }
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateClassDeclaration(14, 28, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(0, 30, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 31);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a generic class has only its simple name in the type name attribute.
	 */
	@Test
	public void shouldHandleTypeNameWithGenerics()
	{
		String source = """
			class Box<T> {}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateClassDeclaration(0, 15, new TypeDeclarationAttribute("Box"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 16);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a class with modifiers has its type name correctly extracted.
	 */
	@Test
	public void shouldHandleTypeWithModifiers()
	{
		String source = """
			public abstract class MyClass {}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateClassDeclaration(16, 32, new TypeDeclarationAttribute("MyClass"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 33);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
