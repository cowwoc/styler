package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

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
			compilationUnit( 0, 52),
			typeDeclaration(CLASS_DECLARATION, 0, 51, "Test"),
			methodDeclaration( 14, 49),
			parameterizedType( 27, 38),
			qualifiedName( 27, 35),
			wildcardType( 36, 37),
			parameterNode( 27, 42, "opt"),
			block( 45, 49));
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
			compilationUnit( 0, 67),
			typeDeclaration(CLASS_DECLARATION, 0, 66, "Test"),
			methodDeclaration( 14, 64),
			parameterizedType( 27, 49),
			qualifiedName( 27, 31),
			wildcardType( 32, 48),
			qualifiedName( 42, 48),
			parameterNode( 27, 57, "numbers"),
			block( 60, 64));
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
			compilationUnit( 0, 70),
			typeDeclaration(CLASS_DECLARATION, 0, 69, "Test"),
			methodDeclaration( 14, 67),
			parameterizedType( 26, 51),
			qualifiedName( 26, 34),
			wildcardType( 35, 50),
			qualifiedName( 43, 50),
			parameterNode( 26, 60, "consumer"),
			block( 63, 67));
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
			compilationUnit( 0, 42),
			typeDeclaration(CLASS_DECLARATION, 0, 41, "Test"),
			fieldDeclaration( 14, 39),
			qualifiedName( 18, 24),
			parameterizedType( 26, 34),
			qualifiedName( 26, 30),
			qualifiedName( 26, 34),
			wildcardType( 31, 32));
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
			compilationUnit( 0, 31),
			typeDeclaration(CLASS_DECLARATION, 0, 30, "Test"),
			fieldDeclaration( 14, 28),
			wildcardType( 18, 19),
			wildcardType( 21, 22));
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
			compilationUnit( 0, 31),
			typeDeclaration(CLASS_DECLARATION, 0, 30, "Test"),
			fieldDeclaration( 14, 28),
			wildcardType( 19, 20));
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
			compilationUnit( 0, 57),
			typeDeclaration(CLASS_DECLARATION, 0, 56, "Test"),
			fieldDeclaration( 14, 54),
			qualifiedName( 18, 24),
			parameterizedType( 26, 49),
			qualifiedName( 26, 30),
			qualifiedName( 26, 49),
			wildcardType( 31, 47),
			qualifiedName( 41, 47));
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
			compilationUnit( 0, 60),
			typeDeclaration(CLASS_DECLARATION, 0, 59, "Test"),
			fieldDeclaration( 14, 57),
			qualifiedName( 18, 24),
			parameterizedType( 26, 52),
			qualifiedName( 26, 34),
			qualifiedName( 26, 52),
			wildcardType( 35, 50),
			qualifiedName( 43, 50));
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
			compilationUnit( 0, 56),
			typeDeclaration(CLASS_DECLARATION, 0, 55, "Test"),
			methodDeclaration( 14, 53),
			wildcardType( 19, 20),
			block( 34, 53),
			returnStatement( 38, 50),
			nullLiteral( 45, 49));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
