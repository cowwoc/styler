package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing annotation type element declarations with default values.
 */
public final class AnnotationElementDefaultTest
{
	/**
	 * Validates parsing of annotation element with a String default value.
	 * Tests the most common case of providing a literal string as the default.
	 */
	@Test
	public void shouldParseStringDefault()
	{
		String source = """
			@interface Config
			{
				String name() default "test";
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.STRING_LITERAL, 43, 49);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 50);
			expected.allocateAnnotationTypeDeclaration(0, 52, new TypeDeclarationAttribute("Config"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 53);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of annotation element with an int default value.
	 * Tests integer literal as the default value.
	 */
	@Test
	public void shouldParseIntDefault()
	{
		String source = """
			@interface Priority
			{
				int value() default 5;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 43, 44);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 23, 45);
			expected.allocateAnnotationTypeDeclaration(0, 47, new TypeDeclarationAttribute("Priority"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 48);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of annotation element with a boolean default value.
	 * Tests boolean literal as the default value.
	 */
	@Test
	public void shouldParseBooleanDefault()
	{
		String source = """
			@interface Enabled
			{
				boolean active() default true;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 47, 51);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 22, 52);
			expected.allocateAnnotationTypeDeclaration(0, 54, new TypeDeclarationAttribute("Enabled"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 55);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of annotation element with an empty array default value.
	 * Tests the empty array initializer syntax.
	 */
	@Test
	public void shouldParseEmptyArrayDefault()
	{
		String source = """
			@interface Tags
			{
				String[] values() default {};
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 45, 47);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 19, 48);
			expected.allocateAnnotationTypeDeclaration(0, 50, new TypeDeclarationAttribute("Tags"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 51);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of annotation element with a non-empty array default value.
	 * Tests array initializer with multiple string elements.
	 */
	@Test
	public void shouldParseNonEmptyArrayDefault()
	{
		String source = """
			@interface Tags
			{
				String[] values() default {"a", "b"};
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.STRING_LITERAL, 46, 49);
			expected.allocateNode(NodeType.STRING_LITERAL, 51, 54);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 45, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 19, 56);
			expected.allocateAnnotationTypeDeclaration(0, 58, new TypeDeclarationAttribute("Tags"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 59);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of annotation element with a class literal default value.
	 * Tests the {@code Object.class} syntax as default.
	 */
	@Test
	public void shouldParseClassLiteralDefault()
	{
		String source = """
			@interface TypeRef
			{
				Class<?> type() default Object.class;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.WILDCARD_TYPE, 28, 29);
			expected.allocateNode(NodeType.IDENTIFIER, 46, 52);
			expected.allocateNode(NodeType.CLASS_LITERAL, 46, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 22, 59);
			expected.allocateAnnotationTypeDeclaration(0, 61, new TypeDeclarationAttribute("TypeRef"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of annotation element without a default value.
	 * Tests that required elements parse correctly without default clause.
	 */
	@Test
	public void shouldParseElementWithoutDefault()
	{
		String source = """
			@interface Required
			{
				String value();
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.METHOD_DECLARATION, 23, 38);
			expected.allocateAnnotationTypeDeclaration(0, 40, new TypeDeclarationAttribute("Required"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 41);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of annotation with multiple elements, some with defaults and some without.
	 * Tests mixed required and optional elements.
	 */
	@Test
	public void shouldParseMixedElementsWithAndWithoutDefaults()
	{
		String source = """
			@interface Config
			{
				String name();
				int priority() default 1;
				boolean enabled() default true;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 35);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 60, 61);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 37, 62);
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 90, 94);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 64, 95);
			expected.allocateAnnotationTypeDeclaration(0, 97, new TypeDeclarationAttribute("Config"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 98);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
