package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing nested annotation type declarations ({@code @interface}) inside classes.
 */
public final class NestedAnnotationTypeParserTest
{
	/**
	 * Validates parsing of a simple nested annotation type declaration.
	 */
	@Test
	public void shouldParseSimpleNestedAnnotationType()
	{
		String source = """
			public class Outer
			{
				public @interface Inner
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateAnnotationTypeDeclaration(29, 51, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(7, 53, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 54);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a nested annotation type with meta-annotations.
	 */
	@Test
	public void shouldParseNestedAnnotationTypeWithMetaAnnotations()
	{
		String source = """
			public class Outer
			{
				@Deprecated
				public @interface RequestScoped
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 33);
			expected.allocateNode(NodeType.ANNOTATION, 22, 33);
			expected.allocateAnnotationTypeDeclaration(42, 72, new TypeDeclarationAttribute("RequestScoped"));
			expected.allocateClassDeclaration(7, 74, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 75);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a nested annotation type with an element (method with default value).
	 */
	@Test
	public void shouldParseNestedAnnotationTypeWithElement()
	{
		String source = """
			public class Outer
			{
				public @interface Config
				{
					String value() default "";
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.STRING_LITERAL, 75, 77);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 52, 78);
			expected.allocateAnnotationTypeDeclaration(29, 81, new TypeDeclarationAttribute("Config"));
			expected.allocateClassDeclaration(7, 83, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 84);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of deeply nested annotation type (annotation inside interface inside class).
	 */
	@Test
	public void shouldParseDeeplyNestedAnnotationType()
	{
		String source = """
			public class Outer
			{
				public interface Middle
				{
					@interface Inner
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateAnnotationTypeDeclaration(51, 75, new TypeDeclarationAttribute("Inner"));
			expected.allocateInterfaceDeclaration(29, 78, new TypeDeclarationAttribute("Middle"));
			expected.allocateClassDeclaration(7, 80, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 81);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of nested annotation type alongside other members.
	 */
	@Test
	public void shouldParseNestedAnnotationTypeWithOtherMembers()
	{
		String source = """
			public class Outer
			{
				private String field;

				public @interface Inner
				{
				}

				public void method()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.FIELD_DECLARATION, 22, 43);
			expected.allocateAnnotationTypeDeclaration(53, 75, new TypeDeclarationAttribute("Inner"));
			expected.allocateNode(NodeType.BLOCK, 100, 104);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 78, 104);
			expected.allocateClassDeclaration(7, 106, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 107);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
