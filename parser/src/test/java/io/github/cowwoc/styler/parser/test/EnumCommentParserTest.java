package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.ENUM_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

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
			compilationUnit( 0, 45),
			typeDeclaration(ENUM_DECLARATION, 7, 44, "Color"),
			lineComment( 21, 37),
			enumConstant( 39, 42));

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
			compilationUnit( 0, 52),
			typeDeclaration(ENUM_DECLARATION, 7, 51, "Status"),
			blockComment( 22, 41),
			enumConstant( 43, 49));

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
			compilationUnit( 0, 65),
			typeDeclaration(ENUM_DECLARATION, 7, 64, "Priority"),
			javadocComment( 24, 56),
			enumConstant( 58, 62));

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
			compilationUnit( 0, 73),
			typeDeclaration(ENUM_DECLARATION, 7, 72, "Direction"),
			enumConstant( 25, 30),
			lineComment( 33, 57),
			enumConstant( 59, 63),
			enumConstant( 66, 70));

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
			compilationUnit( 0, 66),
			typeDeclaration(ENUM_DECLARATION, 7, 65, "Season"),
			enumConstant( 22, 28),
			blockComment( 31, 48),
			enumConstant( 50, 56),
			enumConstant( 59, 63));

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
			compilationUnit( 0, 73),
			typeDeclaration(ENUM_DECLARATION, 7, 72, "Level"),
			lineComment( 21, 37),
			lineComment( 39, 56),
			enumConstant( 58, 61),
			enumConstant( 64, 70));

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
			compilationUnit( 0, 91),
			typeDeclaration(ENUM_DECLARATION, 7, 90, "Type"),
			lineComment( 20, 35),
			javadocComment( 37, 51),
			enumConstant( 53, 58),
			blockComment( 61, 80),
			enumConstant( 82, 88));

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
			compilationUnit( 0, 49),
			typeDeclaration(ENUM_DECLARATION, 7, 48, "Item"),
			enumConstant( 20, 23),
			enumConstant( 26, 29),
			lineComment( 31, 43));

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
			compilationUnit( 0, 47),
			typeDeclaration(ENUM_DECLARATION, 7, 46, "Flag"),
			enumConstant( 20, 23),
			enumConstant( 26, 28),
			lineComment( 29, 44));

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
			compilationUnit( 0, 120),
			typeDeclaration(ENUM_DECLARATION, 7, 119, "Operation"),
			lineComment( 25, 49),
			enumConstant( 51, 54),
			enumConstant( 57, 65),
			methodDeclaration( 69, 117),
			parameterNode( 86, 91, "a"),
			parameterNode( 93, 98, "b"),
			block( 101, 117),
			returnStatement( 105, 114),
			integerLiteral( 112, 113));

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
			compilationUnit( 0, 37),
			typeDeclaration(ENUM_DECLARATION, 7, 36, "Empty"),
			lineComment( 21, 34));

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
			compilationUnit( 0, 99),
			typeDeclaration(ENUM_DECLARATION, 7, 98, "Color"),
			javadocComment( 21, 37),
			enumConstant( 39, 42),
			javadocComment( 45, 63),
			enumConstant( 65, 70),
			javadocComment( 73, 90),
			enumConstant( 92, 96));

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
			compilationUnit( 0, 82),
			typeDeclaration(CLASS_DECLARATION, 7, 81, "Outer"),
			typeDeclaration(ENUM_DECLARATION, 29, 79, "Inner"),
			lineComment( 45, 68),
			enumConstant( 71, 76));

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
			compilationUnit( 0, 51),
			typeDeclaration(ENUM_DECLARATION, 7, 50, "Sample"),
			lineComment( 22, 30),
			enumConstant( 32, 33),
			lineComment( 36, 45),
			enumConstant( 47, 48));

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
			compilationUnit( 0, 118),
			typeDeclaration(ENUM_DECLARATION, 7, 117, "Planet"),
			lineComment( 22, 39),
			enumConstant( 41, 53),
			doubleLiteral( 49, 52),
			blockComment( 56, 71),
			enumConstant( 73, 84),
			doubleLiteral( 79, 83),
			javadocComment( 87, 104),
			enumConstant( 106, 115),
			doubleLiteral( 111, 114));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
