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
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

/**
 * Tests for parsing nested type references like {@code Outer.Inner} in field and method declarations.
 */
public class NestedTypeReferenceTest
{
	/**
	 * Tests parsing of a simple nested type in a field declaration.
	 * The type {@code Outer.Inner} should be correctly parsed.
	 */
	@Test
	public void testFieldWithSimpleNestedType()
	{
		String source = """
			class Test
			{
			    Outer.Inner field;
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 38),
			semanticNode(CLASS_DECLARATION, 0, 37, "Test"),
			semanticNode(FIELD_DECLARATION, 17, 35));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of a deeply nested type in a field declaration.
	 * The type {@code Outer.Middle.Inner} should be correctly parsed,
	 * verifying the parser handles multiple levels of nesting.
	 */
	@Test
	public void testFieldWithDeeplyNestedType()
	{
		String source = """
			class Test
			{
			    Outer.Middle.Inner field;
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 45),
			semanticNode(CLASS_DECLARATION, 0, 44, "Test"),
			semanticNode(FIELD_DECLARATION, 17, 42));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of a method with nested type as return type.
	 * Validates that nested types work correctly in method return type position.
	 */
	@Test
	public void testMethodReturnTypeWithNestedType()
	{
		String source = """
			class Test
			{
			    Outer.Inner method()
			    {
			        return null;
			    }
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 73),
			semanticNode(CLASS_DECLARATION, 0, 72, "Test"),
			semanticNode(METHOD_DECLARATION, 17, 70),
			semanticNode(BLOCK, 42, 70),
			semanticNode(RETURN_STATEMENT, 52, 64),
			semanticNode(NULL_LITERAL, 59, 63));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of nested type with generic type arguments.
	 * Validates that generics work correctly with nested types.
	 */
	@Test
	public void testNestedTypeWithGenericArguments()
	{
		String source = """
			class Test
			{
			    Outer.Inner<String> field;
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 46),
			semanticNode(CLASS_DECLARATION, 0, 45, "Test"),
			semanticNode(FIELD_DECLARATION, 17, 43),
			semanticNode(QUALIFIED_NAME, 29, 35));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of nested type array declaration.
	 * Validates that array types work correctly with nested types.
	 */
	@Test
	public void testNestedTypeArray()
	{
		String source = """
			class Test
			{
			    Outer.Inner[] field;
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 40),
			semanticNode(CLASS_DECLARATION, 0, 39, "Test"),
			semanticNode(FIELD_DECLARATION, 17, 37));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of the real-world {@code ValueLayout.OfInt} pattern from NodeArena.java.
	 * This test validates the actual use case that motivated the bug fix.
	 */
	@Test
	public void testValueLayoutOfIntPattern()
	{
		String source = """
			class Test
			{
			    private static final ValueLayout.OfInt INT_LAYOUT = null;
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 77),
			semanticNode(CLASS_DECLARATION, 0, 76, "Test"),
			semanticNode(FIELD_DECLARATION, 17, 74),
			semanticNode(NULL_LITERAL, 69, 73));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
