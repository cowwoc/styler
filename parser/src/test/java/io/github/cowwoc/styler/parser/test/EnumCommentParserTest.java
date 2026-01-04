package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 21, 37);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 39, 42);
			expected.allocateEnumDeclaration(7, 44, new TypeDeclarationAttribute("Color"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 45);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 22, 41);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 43, 49);
			expected.allocateEnumDeclaration(7, 51, new TypeDeclarationAttribute("Status"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 52);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.JAVADOC_COMMENT, 24, 56);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 58, 62);
			expected.allocateEnumDeclaration(7, 64, new TypeDeclarationAttribute("Priority"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ENUM_CONSTANT, 25, 30);
			expected.allocateNode(NodeType.LINE_COMMENT, 33, 57);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 59, 63);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 66, 70);
			expected.allocateEnumDeclaration(7, 72, new TypeDeclarationAttribute("Direction"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 73);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ENUM_CONSTANT, 22, 28);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 31, 48);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 50, 56);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 59, 63);
			expected.allocateEnumDeclaration(7, 65, new TypeDeclarationAttribute("Season"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 21, 37);
			expected.allocateNode(NodeType.LINE_COMMENT, 39, 56);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 58, 61);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 64, 70);
			expected.allocateEnumDeclaration(7, 72, new TypeDeclarationAttribute("Level"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 73);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 20, 35);
			expected.allocateNode(NodeType.JAVADOC_COMMENT, 37, 51);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 53, 58);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 61, 80);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 82, 88);
			expected.allocateEnumDeclaration(7, 90, new TypeDeclarationAttribute("Type"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 91);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ENUM_CONSTANT, 20, 23);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 26, 29);
			expected.allocateNode(NodeType.LINE_COMMENT, 31, 43);
			expected.allocateEnumDeclaration(7, 48, new TypeDeclarationAttribute("Item"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 49);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ENUM_CONSTANT, 20, 23);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 26, 28);
			expected.allocateNode(NodeType.LINE_COMMENT, 29, 44);
			expected.allocateEnumDeclaration(7, 46, new TypeDeclarationAttribute("Flag"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 47);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 25, 49);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 51, 54);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 57, 65);
			expected.allocateParameterDeclaration(86, 91, new ParameterAttribute("a", false, false, false));
			expected.allocateParameterDeclaration(93, 98, new ParameterAttribute("b", false, false, false));
			expected.allocateNode(NodeType.INTEGER_LITERAL, 112, 113);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 105, 114);
			expected.allocateNode(NodeType.BLOCK, 101, 117);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 69, 117);
			expected.allocateEnumDeclaration(7, 119, new TypeDeclarationAttribute("Operation"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 120);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 21, 34);
			expected.allocateEnumDeclaration(7, 36, new TypeDeclarationAttribute("Empty"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 37);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.JAVADOC_COMMENT, 21, 37);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 39, 42);
			expected.allocateNode(NodeType.JAVADOC_COMMENT, 45, 63);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 65, 70);
			expected.allocateNode(NodeType.JAVADOC_COMMENT, 73, 90);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 92, 96);
			expected.allocateEnumDeclaration(7, 98, new TypeDeclarationAttribute("Color"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 99);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 45, 68);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 71, 76);
			expected.allocateEnumDeclaration(29, 79, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(7, 81, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 82);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 22, 30);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 32, 33);
			expected.allocateNode(NodeType.LINE_COMMENT, 36, 45);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 47, 48);
			expected.allocateEnumDeclaration(7, 50, new TypeDeclarationAttribute("Sample"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 51);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 22, 39);
			expected.allocateNode(NodeType.DOUBLE_LITERAL, 49, 52);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 41, 53);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 56, 71);
			expected.allocateNode(NodeType.DOUBLE_LITERAL, 79, 83);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 73, 84);
			expected.allocateNode(NodeType.JAVADOC_COMMENT, 87, 104);
			expected.allocateNode(NodeType.DOUBLE_LITERAL, 111, 114);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 106, 115);
			expected.allocateEnumDeclaration(7, 117, new TypeDeclarationAttribute("Planet"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 118);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
