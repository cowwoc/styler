package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing JSR 308 type annotations on array dimensions.
 * <p>
 * JSR 308 (JDK 8+) allows annotations to appear on array dimensions, enabling
 * finer-grained nullability and type annotations like {@code String @NonNull []}.
 */
public class ArrayDimensionAnnotationParserTest
{
	/**
	 * Tests parsing of a single annotation on an array dimension.
	 * The {@code @NonNull} annotation appears between the element type and the brackets.
	 */
	@Test
	public void shouldParseSingleAnnotationOnArrayDimension()
	{
		String source = """
			class Container
			{
				String @NonNull [] names;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 34);
			expected.allocateNode(NodeType.ANNOTATION, 26, 34);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 19, 44);
			expected.allocateClassDeclaration(0, 46, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 47);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of multiple annotations on a single array dimension.
	 * Both {@code @A} and {@code @B} are applied to the same dimension.
	 */
	@Test
	public void shouldParseMultipleAnnotationsOnSingleDimension()
	{
		String source = """
			class Container
			{
				String @A @B [] names;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 28);
			expected.allocateNode(NodeType.ANNOTATION, 26, 28);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 31);
			expected.allocateNode(NodeType.ANNOTATION, 29, 31);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 19, 41);
			expected.allocateClassDeclaration(0, 43, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 44);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of annotations on multiple array dimensions.
	 * Each dimension has its own annotation: {@code @A} on the first, {@code @B} on the second.
	 */
	@Test
	public void shouldParseAnnotationsOnMultipleDimensions()
	{
		String source = """
			class Container
			{
				int @A [] @B [] matrix;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 24, 25);
			expected.allocateNode(NodeType.ANNOTATION, 23, 25);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 31);
			expected.allocateNode(NodeType.ANNOTATION, 29, 31);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 19, 42);
			expected.allocateClassDeclaration(0, 44, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 45);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of a primitive array with a dimension annotation.
	 * The {@code @NonNull} annotation is on the dimension, not the primitive type.
	 */
	@Test
	public void shouldParsePrimitiveArrayWithDimensionAnnotation()
	{
		String source = """
			class Container
			{
				int @NonNull [] values;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 24, 31);
			expected.allocateNode(NodeType.ANNOTATION, 23, 31);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 19, 42);
			expected.allocateClassDeclaration(0, 44, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 45);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of a cast expression with an annotated array type.
	 * The cast target type includes an annotation on the array dimension.
	 */
	@Test
	public void shouldParseCastExpressionWithAnnotatedArrayType()
	{
		String source = """
			class Container
			{
				Object obj;
				String[] names = (String @NonNull []) obj;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.FIELD_DECLARATION, 19, 30);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 58, 65);
			expected.allocateNode(NodeType.ANNOTATION, 57, 65);
			expected.allocateNode(NodeType.IDENTIFIER, 70, 73);
			expected.allocateNode(NodeType.CAST_EXPRESSION, 49, 73);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 32, 74);
			expected.allocateClassDeclaration(0, 76, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 77);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of a generic type with an annotated array component.
	 * The type argument is an array type with a dimension annotation.
	 */
	@Test
	public void shouldParseGenericTypeWithAnnotatedArrayComponent()
	{
		String source = """
			class Container
			{
				List<String @NonNull []> items;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 24, 30);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 39);
			expected.allocateNode(NodeType.ANNOTATION, 31, 39);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 24, 42);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 19, 50);
			expected.allocateClassDeclaration(0, 52, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 53);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Regression test: verifies that unannotated arrays continue to parse correctly.
	 * Ensures the annotation parsing changes do not break basic array declarations.
	 */
	@Test
	public void shouldParseUnannotatedArrayUnaffected()
	{
		String source = """
			class Container
			{
				String[] names;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.FIELD_DECLARATION, 19, 34);
			expected.allocateClassDeclaration(0, 36, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 37);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
