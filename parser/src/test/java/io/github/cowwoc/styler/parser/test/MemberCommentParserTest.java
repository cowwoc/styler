package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing comments appearing between modifiers/annotations and member declarations.
 */
public class MemberCommentParserTest
{
	/**
	 * Verifies that a line comment after an annotation and before a method declaration is correctly parsed.
	 */
	@Test
	public void shouldParseLineCommentAfterAnnotationBeforeMethod()
	{
		String source = """
			public class Test
			{
				@Override
				// Comment explaining the method
				public void doSomething()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 30);
			expected.allocateNode(NodeType.ANNOTATION, 21, 30);
			expected.allocateNode(NodeType.LINE_COMMENT, 32, 64);
			expected.allocateNode(NodeType.BLOCK, 93, 97);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 97);
			expected.allocateClassDeclaration(7, 99, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 100);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a block comment after an annotation and before a field declaration is correctly parsed.
	 */
	@Test
	public void shouldParseBlockCommentAfterAnnotationBeforeField()
	{
		String source = """
			public class Test
			{
				@SuppressWarnings("unchecked")
				/* Block comment */
				private List<String> items;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 38);
			expected.allocateNode(NodeType.STRING_LITERAL, 39, 50);
			expected.allocateNode(NodeType.ANNOTATION, 21, 51);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 53, 72);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 87, 93);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 87, 93);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 21, 101);
			expected.allocateClassDeclaration(7, 103, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 104);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a line comment after access modifiers and before a method declaration is correctly parsed.
	 */
	@Test
	public void shouldParseLineCommentAfterModifiersBeforeMethod()
	{
		String source = """
			public class Test
			{
				public static
				// Helper method
				void helper()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 36, 52);
			expected.allocateNode(NodeType.BLOCK, 69, 73);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 73);
			expected.allocateClassDeclaration(7, 75, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 76);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that multiple comments between annotation and declaration are correctly parsed.
	 */
	@Test
	public void shouldParseMultipleCommentsBetweenAnnotationAndDeclaration()
	{
		String source = """
			public class Test
			{
				@Deprecated
				// First comment
				// Second comment
				public void old()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 32);
			expected.allocateNode(NodeType.ANNOTATION, 21, 32);
			expected.allocateNode(NodeType.LINE_COMMENT, 34, 50);
			expected.allocateNode(NodeType.LINE_COMMENT, 52, 69);
			expected.allocateNode(NodeType.BLOCK, 90, 94);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 94);
			expected.allocateClassDeclaration(7, 96, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 97);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a JavaDoc comment after an annotation is correctly parsed.
	 */
	@Test
	public void shouldParseJavadocAfterAnnotation()
	{
		String source = """
			public class Test
			{
				@Override
				/**
				 * Description.
				 */
				public void run()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 30);
			expected.allocateNode(NodeType.ANNOTATION, 21, 30);
			expected.allocateNode(NodeType.JAVADOC_COMMENT, 32, 57);
			expected.allocateNode(NodeType.BLOCK, 78, 82);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 82);
			expected.allocateClassDeclaration(7, 84, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 85);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a comment between annotation and field with initializer is correctly parsed.
	 */
	@Test
	public void shouldParseCommentAfterAnnotationBeforeFieldWithInitializer()
	{
		String source = """
			public class Test
			{
				@NonNull
				// Initialized value
				private String name = "test";
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 29);
			expected.allocateNode(NodeType.ANNOTATION, 21, 29);
			expected.allocateNode(NodeType.LINE_COMMENT, 31, 51);
			expected.allocateNode(NodeType.STRING_LITERAL, 75, 81);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 21, 82);
			expected.allocateClassDeclaration(7, 84, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 85);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a comment after annotation before a constructor is correctly parsed.
	 */
	@Test
	public void shouldParseCommentAfterAnnotationBeforeConstructor()
	{
		String source = """
			public class Test
			{
				@Deprecated
				// Old constructor
				public Test()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 32);
			expected.allocateNode(NodeType.ANNOTATION, 21, 32);
			expected.allocateNode(NodeType.LINE_COMMENT, 34, 52);
			expected.allocateNode(NodeType.BLOCK, 69, 73);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 21, 73);
			expected.allocateClassDeclaration(7, 75, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 76);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a comment after annotation before a nested class is correctly parsed.
	 */
	@Test
	public void shouldParseCommentAfterAnnotationBeforeNestedClass()
	{
		String source = """
			public class Outer
			{
				@Deprecated
				// Nested type comment
				public class Inner
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
			expected.allocateNode(NodeType.LINE_COMMENT, 35, 57);
			expected.allocateClassDeclaration(66, 83, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(7, 85, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 86);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
