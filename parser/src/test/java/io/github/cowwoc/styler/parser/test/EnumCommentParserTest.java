package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK_COMMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.DOUBLE_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.ENUM_CONSTANT;
import static io.github.cowwoc.styler.ast.core.NodeType.ENUM_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.JAVADOC_COMMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.LINE_COMMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

/**
 * Tests for parsing enums with comments in constant lists.
 */
public class EnumCommentParserTest
{
	/**
	 * Verifies that line comments before the first enum constant are correctly parsed.
	 */
	@Test
	public void shouldParseEnumWithLineCommentBeforeFirstConstant()
	{
		String source = """
			public enum Color
			{
				// Primary color
				RED
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 45),
			semanticNode(ENUM_DECLARATION, 7, 44, "Color"),
			semanticNode(LINE_COMMENT, 21, 37),
			semanticNode(ENUM_CONSTANT, 39, 42));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that block comments before the first enum constant are correctly parsed.
	 */
	@Test
	public void shouldParseEnumWithBlockCommentBeforeFirstConstant()
	{
		String source = """
			public enum Status
			{
				/* Current state */
				ACTIVE
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 52),
			semanticNode(ENUM_DECLARATION, 7, 51, "Status"),
			semanticNode(BLOCK_COMMENT, 22, 41),
			semanticNode(ENUM_CONSTANT, 43, 49));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that JavaDoc comments before enum constants are correctly parsed.
	 */
	@Test
	public void shouldParseEnumWithJavadocBeforeConstant()
	{
		String source = """
			public enum Priority
			{
				/**
				 * High priority level
				 */
				HIGH
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 65),
			semanticNode(ENUM_DECLARATION, 7, 64, "Priority"),
			semanticNode(JAVADOC_COMMENT, 24, 56),
			semanticNode(ENUM_CONSTANT, 58, 62));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that line comments between enum constants are correctly parsed.
	 */
	@Test
	public void shouldParseEnumWithLineCommentBetweenConstants()
	{
		String source = """
			public enum Direction
			{
				NORTH,
				// Horizontal directions
				EAST,
				WEST
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 73),
			semanticNode(ENUM_DECLARATION, 7, 72, "Direction"),
			semanticNode(ENUM_CONSTANT, 25, 30),
			semanticNode(LINE_COMMENT, 33, 57),
			semanticNode(ENUM_CONSTANT, 59, 63),
			semanticNode(ENUM_CONSTANT, 66, 70));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that block comments between enum constants are correctly parsed.
	 */
	@Test
	public void shouldParseEnumWithBlockCommentBetweenConstants()
	{
		String source = """
			public enum Season
			{
				SPRING,
				/* Warm months */
				SUMMER,
				FALL
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 66),
			semanticNode(ENUM_DECLARATION, 7, 65, "Season"),
			semanticNode(ENUM_CONSTANT, 22, 28),
			semanticNode(BLOCK_COMMENT, 31, 48),
			semanticNode(ENUM_CONSTANT, 50, 56),
			semanticNode(ENUM_CONSTANT, 59, 63));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that multiple consecutive line comments are correctly parsed.
	 */
	@Test
	public void shouldParseEnumWithMultipleConsecutiveComments()
	{
		String source = """
			public enum Level
			{
				// First comment
				// Second comment
				LOW,
				MEDIUM
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 73),
			semanticNode(ENUM_DECLARATION, 7, 72, "Level"),
			semanticNode(LINE_COMMENT, 21, 37),
			semanticNode(LINE_COMMENT, 39, 56),
			semanticNode(ENUM_CONSTANT, 58, 61),
			semanticNode(ENUM_CONSTANT, 64, 70));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that mixed comment types are correctly parsed.
	 */
	@Test
	public void shouldParseEnumWithMixedCommentTypes()
	{
		String source = """
			public enum Type
			{
				// Line comment
				/** JavaDoc */
				FIRST,
				/* Block comment */
				SECOND
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 91),
			semanticNode(ENUM_DECLARATION, 7, 90, "Type"),
			semanticNode(LINE_COMMENT, 20, 35),
			semanticNode(JAVADOC_COMMENT, 37, 51),
			semanticNode(ENUM_CONSTANT, 53, 58),
			semanticNode(BLOCK_COMMENT, 61, 80),
			semanticNode(ENUM_CONSTANT, 82, 88));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that comments after a trailing comma are correctly parsed.
	 */
	@Test
	public void shouldParseEnumWithTrailingCommaAndComment()
	{
		String source = """
			public enum Item
			{
				ONE,
				TWO, // Last item
				;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 49),
			semanticNode(ENUM_DECLARATION, 7, 48, "Item"),
			semanticNode(ENUM_CONSTANT, 20, 23),
			semanticNode(ENUM_CONSTANT, 26, 29),
			semanticNode(LINE_COMMENT, 31, 43));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that comments after the last constant without trailing comma are correctly parsed.
	 */
	@Test
	public void shouldParseEnumWithCommentAfterLastConstantNoTrailingComma()
	{
		String source = """
			public enum Flag
			{
				YES,
				NO // Final option
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 47),
			semanticNode(ENUM_DECLARATION, 7, 46, "Flag"),
			semanticNode(ENUM_CONSTANT, 20, 23),
			semanticNode(ENUM_CONSTANT, 26, 28),
			semanticNode(LINE_COMMENT, 29, 44));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that enums with methods and comments in constants are correctly parsed.
	 */
	@Test
	public void shouldParseEnumWithMethodsAndCommentsInConstants()
	{
		String source = """
			public enum Operation
			{
				// Arithmetic operations
				ADD,
				SUBTRACT;

				public int apply(int a, int b)
				{
					return 0;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 120),
			semanticNode(ENUM_DECLARATION, 7, 119, "Operation"),
			semanticNode(LINE_COMMENT, 25, 49),
			semanticNode(ENUM_CONSTANT, 51, 54),
			semanticNode(ENUM_CONSTANT, 57, 65),
			semanticNode(METHOD_DECLARATION, 69, 117),
			semanticNode(PARAMETER_DECLARATION, 86, 91, "a"),
			semanticNode(PARAMETER_DECLARATION, 93, 98, "b"),
			semanticNode(BLOCK, 101, 117),
			semanticNode(RETURN_STATEMENT, 105, 114),
			semanticNode(INTEGER_LITERAL, 112, 113));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that empty enums containing only a comment are correctly parsed.
	 */
	@Test
	public void shouldParseEmptyEnumWithOnlyComment()
	{
		String source = """
			public enum Empty
			{
				// Empty enum
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 37),
			semanticNode(ENUM_DECLARATION, 7, 36, "Empty"),
			semanticNode(LINE_COMMENT, 21, 34));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that JavaDoc comments on multiple enum constants are correctly parsed.
	 */
	@Test
	public void shouldParseEnumWithJavadocOnEachConstant()
	{
		String source = """
			public enum Color
			{
				/** Red color */
				RED,
				/** Green color */
				GREEN,
				/** Blue color */
				BLUE
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 99),
			semanticNode(ENUM_DECLARATION, 7, 98, "Color"),
			semanticNode(JAVADOC_COMMENT, 21, 37),
			semanticNode(ENUM_CONSTANT, 39, 42),
			semanticNode(JAVADOC_COMMENT, 45, 63),
			semanticNode(ENUM_CONSTANT, 65, 70),
			semanticNode(JAVADOC_COMMENT, 73, 90),
			semanticNode(ENUM_CONSTANT, 92, 96));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that nested enums within classes with comments are correctly parsed.
	 */
	@Test
	public void shouldParseNestedEnumWithComments()
	{
		String source = """
			public class Outer
			{
				public enum Inner
				{
					// Nested enum constant
					VALUE
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 82),
			semanticNode(CLASS_DECLARATION, 7, 81, "Outer"),
			semanticNode(ENUM_DECLARATION, 29, 79, "Inner"),
			semanticNode(LINE_COMMENT, 45, 68),
			semanticNode(ENUM_CONSTANT, 71, 76));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that comment positions in the AST are correct and consistent.
	 */
	@Test
	public void shouldPreserveCommentPositionsInAst()
	{
		String source = """
			public enum Sample
			{
				// First
				A,
				// Second
				B
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 51),
			semanticNode(ENUM_DECLARATION, 7, 50, "Sample"),
			semanticNode(LINE_COMMENT, 22, 30),
			semanticNode(ENUM_CONSTANT, 32, 33),
			semanticNode(LINE_COMMENT, 36, 45),
			semanticNode(ENUM_CONSTANT, 47, 48));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that enum constants with constructor arguments and preceding comments are correctly parsed.
	 */
	@Test
	public void shouldParseEnumConstantWithArgumentsAndPrecedingComment()
	{
		String source = """
			public enum Planet
			{
				// Closest to sun
				MERCURY(3.8),
				/* Habitable */
				EARTH(10.0),
				/** Red planet */
				MARS(5.5)
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 118),
			semanticNode(ENUM_DECLARATION, 7, 117, "Planet"),
			semanticNode(LINE_COMMENT, 22, 39),
			semanticNode(ENUM_CONSTANT, 41, 53),
			semanticNode(DOUBLE_LITERAL, 49, 52),
			semanticNode(BLOCK_COMMENT, 56, 71),
			semanticNode(ENUM_CONSTANT, 73, 84),
			semanticNode(DOUBLE_LITERAL, 79, 83),
			semanticNode(JAVADOC_COMMENT, 87, 104),
			semanticNode(ENUM_CONSTANT, 106, 115),
			semanticNode(DOUBLE_LITERAL, 111, 114));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
