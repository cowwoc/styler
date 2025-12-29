package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ANNOTATION_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.ARRAY_INITIALIZER;
import static io.github.cowwoc.styler.ast.core.NodeType.BOOLEAN_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.STRING_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.WILDCARD_TYPE;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, 53),
			semanticNode(ANNOTATION_DECLARATION, 0, 52, "Config"),
			semanticNode(METHOD_DECLARATION, 21, 50),
			semanticNode(STRING_LITERAL, 43, 49));
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
			semanticNode(COMPILATION_UNIT, 0, 48),
			semanticNode(ANNOTATION_DECLARATION, 0, 47, "Priority"),
			semanticNode(METHOD_DECLARATION, 23, 45),
			semanticNode(INTEGER_LITERAL, 43, 44));
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
			semanticNode(COMPILATION_UNIT, 0, 55),
			semanticNode(ANNOTATION_DECLARATION, 0, 54, "Enabled"),
			semanticNode(METHOD_DECLARATION, 22, 52),
			semanticNode(BOOLEAN_LITERAL, 47, 51));
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
			semanticNode(COMPILATION_UNIT, 0, 51),
			semanticNode(ANNOTATION_DECLARATION, 0, 50, "Tags"),
			semanticNode(METHOD_DECLARATION, 19, 48),
			semanticNode(ARRAY_INITIALIZER, 45, 47));
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
			semanticNode(COMPILATION_UNIT, 0, 59),
			semanticNode(ANNOTATION_DECLARATION, 0, 58, "Tags"),
			semanticNode(METHOD_DECLARATION, 19, 56),
			semanticNode(ARRAY_INITIALIZER, 45, 55),
			semanticNode(STRING_LITERAL, 46, 49),
			semanticNode(STRING_LITERAL, 51, 54));
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
			semanticNode(COMPILATION_UNIT, 0, 62),
			semanticNode(ANNOTATION_DECLARATION, 0, 61, "TypeRef"),
			semanticNode(METHOD_DECLARATION, 22, 59),
			semanticNode(WILDCARD_TYPE, 28, 29),
			semanticNode(CLASS_LITERAL, 46, 58),
			semanticNode(IDENTIFIER, 46, 52));
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
			semanticNode(COMPILATION_UNIT, 0, 41),
			semanticNode(ANNOTATION_DECLARATION, 0, 40, "Required"),
			semanticNode(METHOD_DECLARATION, 23, 38));
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
			semanticNode(COMPILATION_UNIT, 0, 98),
			semanticNode(ANNOTATION_DECLARATION, 0, 97, "Config"),
			semanticNode(METHOD_DECLARATION, 21, 35),
			semanticNode(METHOD_DECLARATION, 37, 62),
			semanticNode(INTEGER_LITERAL, 60, 61),
			semanticNode(METHOD_DECLARATION, 64, 95),
			semanticNode(BOOLEAN_LITERAL, 90, 94));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
