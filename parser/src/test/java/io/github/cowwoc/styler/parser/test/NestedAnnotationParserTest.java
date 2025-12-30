package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ANNOTATION;
import static io.github.cowwoc.styler.ast.core.NodeType.ANNOTATION_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.ARRAY_INITIALIZER;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.STRING_LITERAL;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, 78),
			semanticNode(ANNOTATION_DECLARATION, 0, 18, "Bar"),
			semanticNode(ANNOTATION_DECLARATION, 19, 51, "Foo"),
			semanticNode(METHOD_DECLARATION, 37, 49),
			semanticNode(ANNOTATION, 52, 62),
			semanticNode(QUALIFIED_NAME, 53, 56),
			semanticNode(ANNOTATION, 57, 61),
			semanticNode(QUALIFIED_NAME, 58, 61),
			semanticNode(CLASS_DECLARATION, 63, 77, "Test"));
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
			semanticNode(COMPILATION_UNIT, 0, 104),
			semanticNode(ANNOTATION_DECLARATION, 0, 35, "Bar"),
			semanticNode(METHOD_DECLARATION, 18, 33),
			semanticNode(ANNOTATION_DECLARATION, 36, 68, "Foo"),
			semanticNode(METHOD_DECLARATION, 54, 66),
			semanticNode(ANNOTATION, 69, 88),
			semanticNode(QUALIFIED_NAME, 70, 73),
			semanticNode(ANNOTATION, 74, 87),
			semanticNode(QUALIFIED_NAME, 75, 78),
			semanticNode(STRING_LITERAL, 79, 86),
			semanticNode(CLASS_DECLARATION, 89, 103, "Test"));
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
			semanticNode(COMPILATION_UNIT, 0, 107),
			semanticNode(ANNOTATION_DECLARATION, 0, 18, "Bar"),
			semanticNode(ANNOTATION_DECLARATION, 19, 37, "Baz"),
			semanticNode(ANNOTATION_DECLARATION, 38, 72, "Foo"),
			semanticNode(METHOD_DECLARATION, 56, 70),
			semanticNode(ANNOTATION, 73, 91),
			semanticNode(QUALIFIED_NAME, 74, 77),
			semanticNode(ARRAY_INITIALIZER, 78, 90),
			semanticNode(ANNOTATION, 79, 83),
			semanticNode(QUALIFIED_NAME, 80, 83),
			semanticNode(ANNOTATION, 85, 89),
			semanticNode(QUALIFIED_NAME, 86, 89),
			semanticNode(CLASS_DECLARATION, 92, 106, "Test"));
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
			semanticNode(COMPILATION_UNIT, 0, 117),
			semanticNode(ANNOTATION_DECLARATION, 0, 18, "Baz"),
			semanticNode(ANNOTATION_DECLARATION, 19, 51, "Bar"),
			semanticNode(METHOD_DECLARATION, 37, 49),
			semanticNode(ANNOTATION_DECLARATION, 52, 84, "Foo"),
			semanticNode(METHOD_DECLARATION, 70, 82),
			semanticNode(ANNOTATION, 85, 101),
			semanticNode(QUALIFIED_NAME, 86, 89),
			semanticNode(ANNOTATION, 90, 100),
			semanticNode(QUALIFIED_NAME, 91, 94),
			semanticNode(ANNOTATION, 95, 99),
			semanticNode(QUALIFIED_NAME, 96, 99),
			semanticNode(CLASS_DECLARATION, 102, 116, "Test"));
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
			semanticNode(COMPILATION_UNIT, 0, 99),
			semanticNode(ANNOTATION_DECLARATION, 0, 18, "Bar"),
			semanticNode(ANNOTATION_DECLARATION, 19, 56, "Foo"),
			semanticNode(METHOD_DECLARATION, 37, 54),
			semanticNode(ANNOTATION, 57, 83),
			semanticNode(QUALIFIED_NAME, 58, 61),
			semanticNode(ARRAY_INITIALIZER, 62, 82),
			semanticNode(ANNOTATION, 63, 67),
			semanticNode(QUALIFIED_NAME, 64, 67),
			semanticNode(STRING_LITERAL, 69, 77),
			semanticNode(INTEGER_LITERAL, 79, 81),
			semanticNode(CLASS_DECLARATION, 84, 98, "Test"));
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
			semanticNode(COMPILATION_UNIT, 0, 65),
			semanticNode(ANNOTATION_DECLARATION, 0, 18, "Bar"),
			semanticNode(ANNOTATION_DECLARATION, 19, 64, "Foo"),
			semanticNode(METHOD_DECLARATION, 37, 62),
			semanticNode(ANNOTATION, 57, 61),
			semanticNode(QUALIFIED_NAME, 58, 61));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
