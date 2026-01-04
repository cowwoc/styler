package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing nested annotations in annotation element values.
 */
public final class NestedAnnotationParserTest
{
	/**
	 * Validates parsing of an annotation with a single nested annotation as its value.
	 * Tests the pattern {@code @Foo(@Bar)} where an annotation contains another annotation.
	 */
	@Test
	public void shouldParseSingleNestedAnnotation()
	{
		String source = """
			@interface Bar
			{
			}
			@interface Foo
			{
				Bar value();
			}
			@Foo(@Bar)
			class Test
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateAnnotationTypeDeclaration(0, 18, new TypeDeclarationAttribute("Bar"));
			expected.allocateNode(NodeType.METHOD_DECLARATION, 37, 49);
			expected.allocateAnnotationTypeDeclaration(19, 51, new TypeDeclarationAttribute("Foo"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 53, 56);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 58, 61);
			expected.allocateNode(NodeType.ANNOTATION, 57, 61);
			expected.allocateNode(NodeType.ANNOTATION, 52, 62);
			expected.allocateClassDeclaration(63, 77, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 78);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a nested annotation that has its own value.
	 * Tests the pattern {@code @Foo(@Bar("value"))} where the nested annotation has a string value.
	 */
	@Test
	public void shouldParseNestedAnnotationWithValue()
	{
		String source = """
			@interface Bar
			{
				String value();
			}
			@interface Foo
			{
				Bar value();
			}
			@Foo(@Bar("value"))
			class Test
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.METHOD_DECLARATION, 18, 33);
			expected.allocateAnnotationTypeDeclaration(0, 35, new TypeDeclarationAttribute("Bar"));
			expected.allocateNode(NodeType.METHOD_DECLARATION, 54, 66);
			expected.allocateAnnotationTypeDeclaration(36, 68, new TypeDeclarationAttribute("Foo"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 70, 73);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 75, 78);
			expected.allocateNode(NodeType.STRING_LITERAL, 79, 86);
			expected.allocateNode(NodeType.ANNOTATION, 74, 87);
			expected.allocateNode(NodeType.ANNOTATION, 69, 88);
			expected.allocateClassDeclaration(89, 103, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 104);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of an array of nested annotations.
	 * Tests the pattern {@code @Foo({@Bar, @Baz})} where an annotation contains an array of annotations.
	 */
	@Test
	public void shouldParseNestedAnnotationsInArray()
	{
		String source = """
			@interface Bar
			{
			}
			@interface Baz
			{
			}
			@interface Foo
			{
				Bar[] value();
			}
			@Foo({@Bar, @Baz})
			class Test
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateAnnotationTypeDeclaration(0, 18, new TypeDeclarationAttribute("Bar"));
			expected.allocateAnnotationTypeDeclaration(19, 37, new TypeDeclarationAttribute("Baz"));
			expected.allocateNode(NodeType.METHOD_DECLARATION, 56, 70);
			expected.allocateAnnotationTypeDeclaration(38, 72, new TypeDeclarationAttribute("Foo"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 74, 77);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 80, 83);
			expected.allocateNode(NodeType.ANNOTATION, 79, 83);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 86, 89);
			expected.allocateNode(NodeType.ANNOTATION, 85, 89);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 78, 90);
			expected.allocateNode(NodeType.ANNOTATION, 73, 91);
			expected.allocateClassDeclaration(92, 106, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 107);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of deeply nested annotations (three levels).
	 * Tests the pattern {@code @Foo(@Bar(@Baz))} where annotations are nested three levels deep.
	 */
	@Test
	public void shouldParseDeeplyNestedAnnotation()
	{
		String source = """
			@interface Baz
			{
			}
			@interface Bar
			{
				Baz value();
			}
			@interface Foo
			{
				Bar value();
			}
			@Foo(@Bar(@Baz))
			class Test
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateAnnotationTypeDeclaration(0, 18, new TypeDeclarationAttribute("Baz"));
			expected.allocateNode(NodeType.METHOD_DECLARATION, 37, 49);
			expected.allocateAnnotationTypeDeclaration(19, 51, new TypeDeclarationAttribute("Bar"));
			expected.allocateNode(NodeType.METHOD_DECLARATION, 70, 82);
			expected.allocateAnnotationTypeDeclaration(52, 84, new TypeDeclarationAttribute("Foo"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 86, 89);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 91, 94);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 96, 99);
			expected.allocateNode(NodeType.ANNOTATION, 95, 99);
			expected.allocateNode(NodeType.ANNOTATION, 90, 100);
			expected.allocateNode(NodeType.ANNOTATION, 85, 101);
			expected.allocateClassDeclaration(102, 116, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 117);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of an array with mixed annotation and literal elements.
	 * Tests the pattern {@code @Foo({@Bar, "string", 42})} where an annotation array contains
	 * different element types.
	 */
	@Test
	public void shouldParseMixedArrayElements()
	{
		String source = """
			@interface Bar
			{
			}
			@interface Foo
			{
				Object[] value();
			}
			@Foo({@Bar, "string", 42})
			class Test
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateAnnotationTypeDeclaration(0, 18, new TypeDeclarationAttribute("Bar"));
			expected.allocateNode(NodeType.METHOD_DECLARATION, 37, 54);
			expected.allocateAnnotationTypeDeclaration(19, 56, new TypeDeclarationAttribute("Foo"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 58, 61);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 64, 67);
			expected.allocateNode(NodeType.ANNOTATION, 63, 67);
			expected.allocateNode(NodeType.STRING_LITERAL, 69, 77);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 79, 81);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 62, 82);
			expected.allocateNode(NodeType.ANNOTATION, 57, 83);
			expected.allocateClassDeclaration(84, 98, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 99);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a nested annotation as a default value in annotation element declaration.
	 * Tests the pattern {@code Bar value() default @Bar;} where the default is a nested annotation.
	 */
	@Test
	public void shouldParseNestedAnnotationInDefaultValue()
	{
		String source = """
			@interface Bar
			{
			}
			@interface Foo
			{
				Bar value() default @Bar;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateAnnotationTypeDeclaration(0, 18, new TypeDeclarationAttribute("Bar"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 58, 61);
			expected.allocateNode(NodeType.ANNOTATION, 57, 61);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 37, 62);
			expected.allocateAnnotationTypeDeclaration(19, 64, new TypeDeclarationAttribute("Foo"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
