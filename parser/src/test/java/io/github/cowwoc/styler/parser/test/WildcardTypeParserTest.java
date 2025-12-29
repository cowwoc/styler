package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.NULL_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETERIZED_TYPE;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.WILDCARD_TYPE;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

/**
 * Tests for parsing wildcard type nodes.
 * Validates correct AST structure generation for unbounded, upper-bounded, and lower-bounded wildcards.
 */
public class WildcardTypeParserTest
{
	/**
	 * Tests parsing of unbounded wildcard type argument.
	 * The {@code ?} wildcard represents an unknown type, commonly used
	 * when the specific type parameter is irrelevant to the operation.
	 * This test verifies that the WILDCARD_TYPE node is created for unbounded wildcards.
	 */
	@Test
	public void testUnboundedWildcard()
	{
		String source = """
			class Test
			{
				void process(Optional<?> opt)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 52),
			semanticNode(CLASS_DECLARATION, 0, 51, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 49),
			semanticNode(PARAMETERIZED_TYPE, 27, 38),
			semanticNode(QUALIFIED_NAME, 27, 35),
			semanticNode(WILDCARD_TYPE, 36, 37),
			semanticNode(PARAMETER_DECLARATION, 27, 42, "opt"),
			semanticNode(BLOCK, 45, 49));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of upper-bounded wildcard type argument.
	 * The {@code ? extends T} syntax restricts the unknown type to be
	 * a subtype of T, enabling covariant type relationships.
	 * This test verifies that WILDCARD_TYPE node is created for upper bounds.
	 */
	@Test
	public void testUpperBoundedWildcard()
	{
		String source = """
			class Test
			{
				void process(List<? extends Number> numbers)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 67),
			semanticNode(CLASS_DECLARATION, 0, 66, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 64),
			semanticNode(PARAMETERIZED_TYPE, 27, 49),
			semanticNode(QUALIFIED_NAME, 27, 31),
			semanticNode(WILDCARD_TYPE, 32, 48),
			semanticNode(QUALIFIED_NAME, 42, 48),
			semanticNode(PARAMETER_DECLARATION, 27, 57, "numbers"),
			semanticNode(BLOCK, 60, 64));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of lower-bounded wildcard type argument.
	 * The {@code ? super T} syntax restricts the unknown type to be
	 * a supertype of T, enabling contravariant type relationships.
	 * This test verifies that WILDCARD_TYPE node is created for lower bounds.
	 */
	@Test
	public void testLowerBoundedWildcard()
	{
		String source = """
			class Test
			{
				void accept(Consumer<? super Integer> consumer)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 70),
			semanticNode(CLASS_DECLARATION, 0, 69, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 67),
			semanticNode(PARAMETERIZED_TYPE, 26, 51),
			semanticNode(QUALIFIED_NAME, 26, 34),
			semanticNode(WILDCARD_TYPE, 35, 50),
			semanticNode(QUALIFIED_NAME, 43, 50),
			semanticNode(PARAMETER_DECLARATION, 26, 60, "consumer"),
			semanticNode(BLOCK, 63, 67));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of nested wildcard in generic type.
	 * Verifies that WILDCARD_TYPE nodes are correctly created when wildcards
	 * appear inside nested generic type arguments like {@code Map<String, List<?>>}.
	 */
	@Test
	public void testNestedWildcard()
	{
		String source = """
			class Test
			{
				Map<String, List<?>> map;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 42),
			semanticNode(CLASS_DECLARATION, 0, 41, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 39),
			semanticNode(QUALIFIED_NAME, 18, 24),
			semanticNode(PARAMETERIZED_TYPE, 26, 34),
			semanticNode(QUALIFIED_NAME, 26, 30),
			semanticNode(QUALIFIED_NAME, 26, 34),
			semanticNode(WILDCARD_TYPE, 31, 32));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of multiple wildcard type arguments.
	 * Verifies that multiple WILDCARD_TYPE nodes are created in the same generic
	 * declaration, such as {@code Map<?, ?>}.
	 */
	@Test
	public void testMultipleWildcards()
	{
		String source = """
			class Test
			{
				Map<?, ?> map;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 31),
			semanticNode(CLASS_DECLARATION, 0, 30, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 28),
			semanticNode(WILDCARD_TYPE, 18, 19),
			semanticNode(WILDCARD_TYPE, 21, 22));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of wildcard in field declaration.
	 * Verifies WILDCARD_TYPE node creation in field type declarations,
	 * ensuring the parser handles wildcards consistently across different declaration contexts.
	 */
	@Test
	public void testWildcardInFieldDeclaration()
	{
		String source = """
			class Test
			{
				List<?> items;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 31),
			semanticNode(CLASS_DECLARATION, 0, 30, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 28),
			semanticNode(WILDCARD_TYPE, 19, 20));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of upper-bounded wildcard in nested generic context.
	 * Verifies WILDCARD_TYPE node creation for bounded wildcards nested inside
	 * other generic types, such as {@code Map<String, List<? extends Number>>}.
	 */
	@Test
	public void testUpperBoundedWildcardInNestedGeneric()
	{
		String source = """
			class Test
			{
				Map<String, List<? extends Number>> map;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 57),
			semanticNode(CLASS_DECLARATION, 0, 56, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 54),
			semanticNode(QUALIFIED_NAME, 18, 24),
			semanticNode(PARAMETERIZED_TYPE, 26, 49),
			semanticNode(QUALIFIED_NAME, 26, 30),
			semanticNode(QUALIFIED_NAME, 26, 49),
			semanticNode(WILDCARD_TYPE, 31, 47),
			semanticNode(QUALIFIED_NAME, 41, 47));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of lower-bounded wildcard in nested generic context.
	 * Verifies WILDCARD_TYPE node creation for lower-bounded wildcards nested inside
	 * other generic types, such as {@code Map<String, Consumer<? super Integer>>}.
	 */
	@Test
	public void testLowerBoundedWildcardInNestedGeneric()
	{
		String source = """
			class Test
			{
				Map<String, Consumer<? super Integer>> map;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 60),
			semanticNode(CLASS_DECLARATION, 0, 59, "Test"),
			semanticNode(FIELD_DECLARATION, 14, 57),
			semanticNode(QUALIFIED_NAME, 18, 24),
			semanticNode(PARAMETERIZED_TYPE, 26, 52),
			semanticNode(QUALIFIED_NAME, 26, 34),
			semanticNode(QUALIFIED_NAME, 26, 52),
			semanticNode(WILDCARD_TYPE, 35, 50),
			semanticNode(QUALIFIED_NAME, 43, 50));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of wildcard in method return type.
	 * Verifies that WILDCARD_TYPE nodes are created when wildcards appear
	 * in method return type declarations.
	 */
	@Test
	public void testWildcardInReturnType()
	{
		String source = """
			class Test
			{
				List<?> getItems()
				{
					return null;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 56),
			semanticNode(CLASS_DECLARATION, 0, 55, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 53),
			semanticNode(WILDCARD_TYPE, 19, 20),
			semanticNode(BLOCK, 34, 53),
			semanticNode(RETURN_STATEMENT, 38, 50),
			semanticNode(NULL_LITERAL, 45, 49));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
