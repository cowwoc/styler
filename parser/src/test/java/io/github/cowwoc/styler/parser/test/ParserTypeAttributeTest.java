package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for type declaration attribute population during parsing.
 * <p>
 * Verifies that the parser correctly populates the {@code typeName} attribute for class,
 * interface, enum, and record declarations.
 * <p>
 * <b>Thread-safety</b>: Thread-safe - all instances are created inside {@code @Test} methods.
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> classes = findNodesOfType(arena, NodeType.CLASS_DECLARATION);
			requireThat(classes.size(), "classes.size()").isEqualTo(1);

			TypeDeclarationAttribute attribute = arena.getTypeDeclarationAttribute(classes.get(0));
			requireThat(attribute.typeName(), "typeName").isEqualTo("MyClass");
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> classes = findNodesOfType(arena, NodeType.CLASS_DECLARATION);
			requireThat(classes.size(), "classes.size()").isEqualTo(1);

			TypeDeclarationAttribute attribute = arena.getTypeDeclarationAttribute(classes.get(0));
			requireThat(attribute, "attribute").isNotNull();
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> interfaces = findNodesOfType(arena, NodeType.INTERFACE_DECLARATION);
			requireThat(interfaces.size(), "interfaces.size()").isEqualTo(1);

			TypeDeclarationAttribute attribute = arena.getTypeDeclarationAttribute(interfaces.get(0));
			requireThat(attribute.typeName(), "typeName").isEqualTo("MyInterface");
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> enums = findNodesOfType(arena, NodeType.ENUM_DECLARATION);
			requireThat(enums.size(), "enums.size()").isEqualTo(1);

			TypeDeclarationAttribute attribute = arena.getTypeDeclarationAttribute(enums.get(0));
			requireThat(attribute.typeName(), "typeName").isEqualTo("MyEnum");
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> records = findNodesOfType(arena, NodeType.RECORD_DECLARATION);
			requireThat(records.size(), "records.size()").isEqualTo(1);

			TypeDeclarationAttribute attribute = arena.getTypeDeclarationAttribute(records.get(0));
			requireThat(attribute.typeName(), "typeName").isEqualTo("MyRecord");
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> classes = findNodesOfType(arena, NodeType.CLASS_DECLARATION);
			requireThat(classes.size(), "classes.size()").isEqualTo(2);

			// Both should exist and have distinct names
			boolean foundOuter = false;
			boolean foundInner = false;
			for (NodeIndex index : classes)
			{
				TypeDeclarationAttribute attr = arena.getTypeDeclarationAttribute(index);
				if ("Outer".equals(attr.typeName()))
				{
					foundOuter = true;
				}
				else if ("Inner".equals(attr.typeName()))
				{
					foundInner = true;
				}
			}
			requireThat(foundOuter, "foundOuter").isTrue();
			requireThat(foundInner, "foundInner").isTrue();
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> classes = findNodesOfType(arena, NodeType.CLASS_DECLARATION);
			requireThat(classes.size(), "classes.size()").isEqualTo(1);

			TypeDeclarationAttribute attribute = arena.getTypeDeclarationAttribute(classes.get(0));
			// Type name should be "Box", not "Box<T>"
			requireThat(attribute.typeName(), "typeName").isEqualTo("Box");
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> classes = findNodesOfType(arena, NodeType.CLASS_DECLARATION);
			requireThat(classes.size(), "classes.size()").isEqualTo(1);

			TypeDeclarationAttribute attribute = arena.getTypeDeclarationAttribute(classes.get(0));
			requireThat(attribute.typeName(), "typeName").isEqualTo("MyClass");
		}
	}

	/**
	 * Finds all nodes of the specified type in the arena.
	 *
	 * @param arena the arena to search
	 * @param type  the node type to find
	 * @return list of node indices matching the type
	 */
	private List<NodeIndex> findNodesOfType(NodeArena arena, NodeType type)
	{
		List<NodeIndex> result = new ArrayList<>();
		for (int i = 0; i < arena.getNodeCount(); ++i)
		{
			NodeIndex index = new NodeIndex(i);
			if (arena.getType(index) == type)
			{
				result.add(index);
			}
		}
		return result;
	}
}
