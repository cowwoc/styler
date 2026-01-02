package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.ANNOTATION_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;

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
		Set<SemanticNode> actual = parseSemanticAst("""
			@interface Config
			{
				String name() default "test";
			}
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 53),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 52, "Config"),
			methodDeclaration( 21, 50),
			stringLiteral( 43, 49));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of annotation element with an int default value.
	 * Tests integer literal as the default value.
	 */
	@Test
	public void shouldParseIntDefault()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			@interface Priority
			{
				int value() default 5;
			}
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 48),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 47, "Priority"),
			methodDeclaration( 23, 45),
			integerLiteral( 43, 44));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of annotation element with a boolean default value.
	 * Tests boolean literal as the default value.
	 */
	@Test
	public void shouldParseBooleanDefault()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			@interface Enabled
			{
				boolean active() default true;
			}
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 55),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 54, "Enabled"),
			methodDeclaration( 22, 52),
			booleanLiteral( 47, 51));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of annotation element with an empty array default value.
	 * Tests the empty array initializer syntax.
	 */
	@Test
	public void shouldParseEmptyArrayDefault()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			@interface Tags
			{
				String[] values() default {};
			}
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 51),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 50, "Tags"),
			methodDeclaration( 19, 48),
			arrayInitializer( 45, 47));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of annotation element with a non-empty array default value.
	 * Tests array initializer with multiple string elements.
	 */
	@Test
	public void shouldParseNonEmptyArrayDefault()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			@interface Tags
			{
				String[] values() default {"a", "b"};
			}
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 59),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 58, "Tags"),
			methodDeclaration( 19, 56),
			arrayInitializer( 45, 55),
			stringLiteral( 46, 49),
			stringLiteral( 51, 54));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of annotation element with a class literal default value.
	 * Tests the {@code Object.class} syntax as default.
	 */
	@Test
	public void shouldParseClassLiteralDefault()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			@interface TypeRef
			{
				Class<?> type() default Object.class;
			}
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 62),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 61, "TypeRef"),
			methodDeclaration( 22, 59),
			wildcardType( 28, 29),
			classLiteral( 46, 58),
			identifier( 46, 52));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of annotation element without a default value.
	 * Tests that required elements parse correctly without default clause.
	 */
	@Test
	public void shouldParseElementWithoutDefault()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			@interface Required
			{
				String value();
			}
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 41),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 40, "Required"),
			methodDeclaration( 23, 38));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of annotation with multiple elements, some with defaults and some without.
	 * Tests mixed required and optional elements.
	 */
	@Test
	public void shouldParseMixedElementsWithAndWithoutDefaults()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			@interface Config
			{
				String name();
				int priority() default 1;
				boolean enabled() default true;
			}
			""");
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 98),
			typeDeclaration(ANNOTATION_DECLARATION, 0, 97, "Config"),
			methodDeclaration( 21, 35),
			methodDeclaration( 37, 62),
			integerLiteral( 60, 61),
			methodDeclaration( 64, 95),
			booleanLiteral( 90, 94));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
