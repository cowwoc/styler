package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ANNOTATION;
import static io.github.cowwoc.styler.ast.core.NodeType.CAST_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 47),
			semanticNode(CLASS_DECLARATION, 0, 46, "Container"),
			semanticNode(FIELD_DECLARATION, 19, 44),
			semanticNode(ANNOTATION, 26, 34),
			semanticNode(QUALIFIED_NAME, 27, 34));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 44),
			semanticNode(CLASS_DECLARATION, 0, 43, "Container"),
			semanticNode(FIELD_DECLARATION, 19, 41),
			semanticNode(ANNOTATION, 26, 28),
			semanticNode(QUALIFIED_NAME, 27, 28),
			semanticNode(ANNOTATION, 29, 31),
			semanticNode(QUALIFIED_NAME, 30, 31));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 45),
			semanticNode(CLASS_DECLARATION, 0, 44, "Container"),
			semanticNode(FIELD_DECLARATION, 19, 42),
			semanticNode(ANNOTATION, 23, 25),
			semanticNode(QUALIFIED_NAME, 24, 25),
			semanticNode(ANNOTATION, 29, 31),
			semanticNode(QUALIFIED_NAME, 30, 31));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 45),
			semanticNode(CLASS_DECLARATION, 0, 44, "Container"),
			semanticNode(FIELD_DECLARATION, 19, 42),
			semanticNode(ANNOTATION, 23, 31),
			semanticNode(QUALIFIED_NAME, 24, 31));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 77),
			semanticNode(CLASS_DECLARATION, 0, 76, "Container"),
			semanticNode(FIELD_DECLARATION, 19, 30),
			semanticNode(FIELD_DECLARATION, 32, 74),
			semanticNode(CAST_EXPRESSION, 49, 73),
			semanticNode(ANNOTATION, 57, 65),
			semanticNode(QUALIFIED_NAME, 58, 65),
			semanticNode(IDENTIFIER, 70, 73));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 53),
			semanticNode(CLASS_DECLARATION, 0, 52, "Container"),
			semanticNode(FIELD_DECLARATION, 19, 50),
			semanticNode(QUALIFIED_NAME, 24, 42),
			semanticNode(QUALIFIED_NAME, 24, 30),
			semanticNode(ANNOTATION, 31, 39),
			semanticNode(QUALIFIED_NAME, 32, 39));
		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 37),
			semanticNode(CLASS_DECLARATION, 0, 36, "Container"),
			semanticNode(FIELD_DECLARATION, 19, 34));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
