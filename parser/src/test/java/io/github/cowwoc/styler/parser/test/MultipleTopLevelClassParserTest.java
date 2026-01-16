package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing multiple top-level classes with annotations between them.
 */
public class MultipleTopLevelClassParserTest
{
	/**
	 * Verifies that multiple top-level classes with an annotation on the second class are parsed correctly.
	 */
	@Test
	public void shouldParseMultipleClassesWithAnnotationOnSecond()
	{
		String source = """
			class First
			{
			}

			@Deprecated
			class Second
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(0, 15, new TypeDeclarationAttribute("First"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 28);
			expected.allocateNode(NodeType.ANNOTATION, 17, 28);
			expected.allocateClassDeclaration(29, 45, new TypeDeclarationAttribute("Second"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 46);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that multiple top-level classes with a comment between them are parsed correctly.
	 */
	@Test
	public void shouldParseMultipleClassesWithCommentBetween()
	{
		String source = """
			class First
			{
			}

			// Comment between classes
			class Second
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(0, 15, new TypeDeclarationAttribute("First"));
			expected.allocateNode(NodeType.LINE_COMMENT, 17, 43);
			expected.allocateClassDeclaration(44, 60, new TypeDeclarationAttribute("Second"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that multiple top-level classes with both annotation and comment are parsed correctly.
	 */
	@Test
	public void shouldParseMultipleClassesWithAnnotationAndComment()
	{
		String source = """
			class First
			{
			}

			// This class is deprecated
			@Deprecated
			class Second
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(0, 15, new TypeDeclarationAttribute("First"));
			expected.allocateNode(NodeType.LINE_COMMENT, 17, 44);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 56);
			expected.allocateNode(NodeType.ANNOTATION, 45, 56);
			expected.allocateClassDeclaration(57, 73, new TypeDeclarationAttribute("Second"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 74);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that three top-level classes with annotations are parsed correctly.
	 */
	@Test
	public void shouldParseThreeClassesWithAnnotations()
	{
		String source = """
			class First
			{
			}

			@Deprecated
			class Second
			{
			}

			@SuppressWarnings("test")
			class Third
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(0, 15, new TypeDeclarationAttribute("First"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 28);
			expected.allocateNode(NodeType.ANNOTATION, 17, 28);
			expected.allocateClassDeclaration(29, 45, new TypeDeclarationAttribute("Second"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 64);
			expected.allocateNode(NodeType.STRING_LITERAL, 65, 71);
			expected.allocateNode(NodeType.ANNOTATION, 47, 72);
			expected.allocateClassDeclaration(73, 88, new TypeDeclarationAttribute("Third"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 89);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a class with modifier followed by annotated class are parsed correctly.
	 */
	@Test
	public void shouldParsePublicClassFollowedByAnnotatedClass()
	{
		String source = """
			public class Public
			{
			}

			@Deprecated
			class Second
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(7, 23, new TypeDeclarationAttribute("Public"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 36);
			expected.allocateNode(NodeType.ANNOTATION, 25, 36);
			expected.allocateClassDeclaration(37, 53, new TypeDeclarationAttribute("Second"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 54);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
