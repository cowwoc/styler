package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;

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
			compilationUnit( 0, 47),
			typeDeclaration(CLASS_DECLARATION, 0, 46, "Container"),
			fieldDeclaration( 19, 44),
			annotation( 26, 34),
			qualifiedName( 27, 34));
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
			compilationUnit( 0, 44),
			typeDeclaration(CLASS_DECLARATION, 0, 43, "Container"),
			fieldDeclaration( 19, 41),
			annotation( 26, 28),
			qualifiedName( 27, 28),
			annotation( 29, 31),
			qualifiedName( 30, 31));
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
			compilationUnit( 0, 45),
			typeDeclaration(CLASS_DECLARATION, 0, 44, "Container"),
			fieldDeclaration( 19, 42),
			annotation( 23, 25),
			qualifiedName( 24, 25),
			annotation( 29, 31),
			qualifiedName( 30, 31));
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
			compilationUnit( 0, 45),
			typeDeclaration(CLASS_DECLARATION, 0, 44, "Container"),
			fieldDeclaration( 19, 42),
			annotation( 23, 31),
			qualifiedName( 24, 31));
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
			compilationUnit( 0, 77),
			typeDeclaration(CLASS_DECLARATION, 0, 76, "Container"),
			fieldDeclaration( 19, 30),
			fieldDeclaration( 32, 74),
			castExpression( 49, 73),
			annotation( 57, 65),
			qualifiedName( 58, 65),
			identifier( 70, 73));
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
			compilationUnit( 0, 53),
			typeDeclaration(CLASS_DECLARATION, 0, 52, "Container"),
			fieldDeclaration( 19, 50),
			qualifiedName( 24, 42),
			qualifiedName( 24, 30),
			annotation( 31, 39),
			qualifiedName( 32, 39));
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
			compilationUnit( 0, 37),
			typeDeclaration(CLASS_DECLARATION, 0, 36, "Container"),
			fieldDeclaration( 19, 34));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
