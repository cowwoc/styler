package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.ANNOTATION_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;

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
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 78),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 18, "Bar"),
			typeDeclaration(ANNOTATION_DECLARATION, 19, 51, "Foo"),
			methodDeclaration( 37, 49),
			annotation( 52, 62),
			qualifiedName( 53, 56),
			annotation( 57, 61),
			qualifiedName( 58, 61),
			typeDeclaration(CLASS_DECLARATION, 63, 77, "Test"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a nested annotation that has its own value.
	 * Tests the pattern {@code @Foo(@Bar("value"))} where the nested annotation has a string value.
	 */
	@Test
	public void shouldParseNestedAnnotationWithValue()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 104),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 35, "Bar"),
			methodDeclaration( 18, 33),
			typeDeclaration(ANNOTATION_DECLARATION, 36, 68, "Foo"),
			methodDeclaration( 54, 66),
			annotation( 69, 88),
			qualifiedName( 70, 73),
			annotation( 74, 87),
			qualifiedName( 75, 78),
			stringLiteral( 79, 86),
			typeDeclaration(CLASS_DECLARATION, 89, 103, "Test"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of an array of nested annotations.
	 * Tests the pattern {@code @Foo({@Bar, @Baz})} where an annotation contains an array of annotations.
	 */
	@Test
	public void shouldParseNestedAnnotationsInArray()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 107),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 18, "Bar"),
			typeDeclaration(ANNOTATION_DECLARATION, 19, 37, "Baz"),
			typeDeclaration(ANNOTATION_DECLARATION, 38, 72, "Foo"),
			methodDeclaration( 56, 70),
			annotation( 73, 91),
			qualifiedName( 74, 77),
			arrayInitializer( 78, 90),
			annotation( 79, 83),
			qualifiedName( 80, 83),
			annotation( 85, 89),
			qualifiedName( 86, 89),
			typeDeclaration(CLASS_DECLARATION, 92, 106, "Test"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of deeply nested annotations (three levels).
	 * Tests the pattern {@code @Foo(@Bar(@Baz))} where annotations are nested three levels deep.
	 */
	@Test
	public void shouldParseDeeplyNestedAnnotation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 117),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 18, "Baz"),
			typeDeclaration(ANNOTATION_DECLARATION, 19, 51, "Bar"),
			methodDeclaration( 37, 49),
			typeDeclaration(ANNOTATION_DECLARATION, 52, 84, "Foo"),
			methodDeclaration( 70, 82),
			annotation( 85, 101),
			qualifiedName( 86, 89),
			annotation( 90, 100),
			qualifiedName( 91, 94),
			annotation( 95, 99),
			qualifiedName( 96, 99),
			typeDeclaration(CLASS_DECLARATION, 102, 116, "Test"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of an array with mixed annotation and literal elements.
	 * Tests the pattern {@code @Foo({@Bar, "string", 42})} where an annotation array contains
	 * different element types.
	 */
	@Test
	public void shouldParseMixedArrayElements()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 99),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 18, "Bar"),
			typeDeclaration(ANNOTATION_DECLARATION, 19, 56, "Foo"),
			methodDeclaration( 37, 54),
			annotation( 57, 83),
			qualifiedName( 58, 61),
			arrayInitializer( 62, 82),
			annotation( 63, 67),
			qualifiedName( 64, 67),
			stringLiteral( 69, 77),
			integerLiteral( 79, 81),
			typeDeclaration(CLASS_DECLARATION, 84, 98, "Test"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a nested annotation as a default value in annotation element declaration.
	 * Tests the pattern {@code Bar value() default @Bar;} where the default is a nested annotation.
	 */
	@Test
	public void shouldParseNestedAnnotationInDefaultValue()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			@interface Bar
			{
			}
			@interface Foo
			{
				Bar value() default @Bar;
			}
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 65),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 18, "Bar"),
			typeDeclaration(ANNOTATION_DECLARATION, 19, 64, "Foo"),
			methodDeclaration( 37, 62),
			annotation( 57, 61),
			qualifiedName( 58, 61));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
