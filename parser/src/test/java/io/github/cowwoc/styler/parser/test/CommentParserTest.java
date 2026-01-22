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
 * Tests for parsing comments in various Java contexts.
 */
public class CommentParserTest
{
	// ========== Enum Comment Tests ==========

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

	/**
	 * Verifies that annotations on enum constants are correctly parsed.
	 */
	@Test
	public void shouldParseEnumWithAnnotationOnConstant()
	{
		String source = """
			public enum Status
			{
				@Deprecated
				OLD_VALUE,
				@SuppressWarnings("test")
				NEW_VALUE
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 33);
			expected.allocateNode(NodeType.ANNOTATION, 22, 33);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 22, 44);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 64);
			expected.allocateNode(NodeType.STRING_LITERAL, 65, 71);
			expected.allocateNode(NodeType.ANNOTATION, 47, 72);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 47, 83);
			expected.allocateEnumDeclaration(7, 85, new TypeDeclarationAttribute("Status"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 86);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Block Comment Tests ==========

	/**
	 * Verifies that comments after opening brace are correctly parsed.
	 */
	@Test
	public void shouldParseBlockWithCommentAfterOpeningBrace()
	{
		String source = """
			class Test
			{
				void m()
				{ // start of block
					return 1;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 26, 43);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 53, 54);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 46, 55);
			expected.allocateNode(NodeType.BLOCK, 24, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 58);
			expected.allocateClassDeclaration(0, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments before closing brace are correctly parsed.
	 */
	@Test
	public void shouldParseBlockWithCommentBeforeClosingBrace()
	{
		String source = """
			class Test
			{
				void m()
				{
					return 1;
					// end of block
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 35, 36);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 28, 37);
			expected.allocateNode(NodeType.LINE_COMMENT, 40, 55);
			expected.allocateNode(NodeType.BLOCK, 24, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 58);
			expected.allocateClassDeclaration(0, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that empty blocks containing only a comment are correctly parsed.
	 */
	@Test
	public void shouldParseEmptyBlockWithOnlyComment()
	{
		String source = """
			class Test
			{
				void m()
				{
					// empty method body
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 28, 48);
			expected.allocateNode(NodeType.BLOCK, 24, 51);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 51);
			expected.allocateClassDeclaration(0, 53, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 54);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments in nested blocks are correctly parsed.
	 */
	@Test
	public void shouldParseNestedBlocksWithComments()
	{
		String source = """
			class Test
			{
				void m()
				{
					// outer comment
					{
						// inner comment
						return 1;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 28, 44);
			expected.allocateNode(NodeType.LINE_COMMENT, 52, 68);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 79, 80);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 72, 81);
			expected.allocateNode(NodeType.BLOCK, 47, 85);
			expected.allocateNode(NodeType.BLOCK, 24, 88);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 88);
			expected.allocateClassDeclaration(0, 90, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 91);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that block comments between member declarations are correctly parsed.
	 */
	@Test
	public void shouldParseBlockCommentBetweenMemberDeclarations()
	{
		String source = """
			class Test
			{
				int x;
				/* separator comment */
				int y;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 20);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 22, 45);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 47, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Comment In Expression Tests ==========

	/**
	 * Verifies that line comments between binary operators are correctly handled.
	 */
	@Test
	public void shouldParseLineCommentBetweenBinaryOperators()
	{
		String source = """
			class Test
			{
				void m()
				{
					int x = 1 + // comment
					2;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 36, 37);
			expected.allocateNode(NodeType.LINE_COMMENT, 40, 50);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 53, 54);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 36, 54);
			expected.allocateNode(NodeType.BLOCK, 24, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 58);
			expected.allocateClassDeclaration(0, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that block comments between binary operators are correctly handled.
	 */
	@Test
	public void shouldParseBlockCommentBetweenBinaryOperators()
	{
		String source = """
			class Test
			{
				void m()
				{
					int x = 1 + /* comment */ 2;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 36, 37);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 40, 53);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 54, 55);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 36, 55);
			expected.allocateNode(NodeType.BLOCK, 24, 59);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 59);
			expected.allocateClassDeclaration(0, 61, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that line comments before a primary expression are correctly handled.
	 */
	@Test
	public void shouldParseCommentBeforePrimaryExpression()
	{
		String source = """
			class Test
			{
				void m()
				{
					int x = // comment
					42;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 36, 46);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 49, 51);
			expected.allocateNode(NodeType.BLOCK, 24, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments within method arguments are correctly handled.
	 */
	@Test
	public void shouldParseCommentInMethodArguments()
	{
		String source = """
			class Test
			{
				void m()
				{
					call(arg1, // comment
					arg2);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 32);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 32);
			expected.allocateNode(NodeType.IDENTIFIER, 33, 37);
			expected.allocateNode(NodeType.LINE_COMMENT, 39, 49);
			expected.allocateNode(NodeType.IDENTIFIER, 52, 56);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 57);
			expected.allocateNode(NodeType.BLOCK, 24, 61);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 61);
			expected.allocateClassDeclaration(0, 63, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 64);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments after the dot operator in field access are correctly handled.
	 */
	@Test
	public void shouldParseCommentAfterDotOperator()
	{
		String source = """
			class Test
			{
				void m()
				{
					obj. // comment
					field = 1;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 32);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 31);
			expected.allocateNode(NodeType.LINE_COMMENT, 33, 43);
			expected.allocateNode(NodeType.FIELD_ACCESS, 28, 51);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 54, 55);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 28, 55);
			expected.allocateNode(NodeType.BLOCK, 24, 59);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 59);
			expected.allocateClassDeclaration(0, 61, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that block comments within array access brackets are correctly handled.
	 */
	@Test
	public void shouldParseCommentInArrayAccess()
	{
		String source = """
			class Test
			{
				void m()
				{
					int x = array[/* comment */ 0];
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 36, 41);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 42, 55);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 56, 57);
			expected.allocateNode(NodeType.ARRAY_ACCESS, 36, 58);
			expected.allocateNode(NodeType.BLOCK, 24, 62);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 62);
			expected.allocateClassDeclaration(0, 64, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that block comments before a unary operator are correctly handled.
	 */
	@Test
	public void shouldParseCommentBeforeUnaryOperator()
	{
		String source = """
			class Test
			{
				void m()
				{
					int x = /* comment */ -5;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 36, 49);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 51, 52);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 50, 52);
			expected.allocateNode(NodeType.BLOCK, 24, 56);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 56);
			expected.allocateClassDeclaration(0, 58, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 59);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Comment In Type Context Tests ==========

	/**
	 * Tests that line comments between comma and type name in implements clause are handled.
	 */
	@Test
	public void testCommentBetweenImplementedTypes()
	{
		String source = """
			class Test implements Serializable, // comment
			    Comparable
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 34);
			expected.allocateNode(NodeType.LINE_COMMENT, 36, 46);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 51, 61);
			expected.allocateClassDeclaration(0, 65, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests that comments between comma and type in extends clause are handled.
	 */
	@Test
	public void testCommentBetweenExtendedInterfaces()
	{
		String source = """
			interface Test extends Runnable, // comment
			    Serializable
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 31);
			expected.allocateNode(NodeType.LINE_COMMENT, 33, 43);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 60);
			expected.allocateInterfaceDeclaration(0, 64, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Control Flow Comment Tests ==========

	/**
	 * Verifies that comments before if condition are correctly parsed.
	 */
	@Test
	public void shouldParseIfStatementWithCommentBeforeCondition()
	{
		String source = """
			class Test
			{
				void m()
				{
					if (/* condition */ true)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 32, 47);
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 48, 52);
			expected.allocateNode(NodeType.BLOCK, 56, 61);
			expected.allocateNode(NodeType.IF_STATEMENT, 28, 61);
			expected.allocateNode(NodeType.BLOCK, 24, 64);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 64);
			expected.allocateClassDeclaration(0, 66, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 67);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments in for loop header are correctly parsed.
	 */
	@Test
	public void shouldParseForLoopWithCommentInHeader()
	{
		String source = """
			class Test
			{
				void m()
				{
					for (int i = 0; /* loop condition */ i < 10; i++)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 41, 42);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 44, 64);
			expected.allocateNode(NodeType.IDENTIFIER, 65, 66);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 69, 71);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 65, 71);
			expected.allocateNode(NodeType.IDENTIFIER, 73, 74);
			expected.allocateNode(NodeType.POSTFIX_EXPRESSION, 73, 76);
			expected.allocateNode(NodeType.BLOCK, 80, 85);
			expected.allocateNode(NodeType.FOR_STATEMENT, 28, 85);
			expected.allocateNode(NodeType.BLOCK, 24, 88);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 88);
			expected.allocateClassDeclaration(0, 90, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 91);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments after colon in enhanced for are correctly parsed.
	 */
	@Test
	public void shouldParseEnhancedForWithCommentAfterColon()
	{
		String source = """
			class Test
			{
				void m()
				{
					for (int x : /* items */ items)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 41, 52);
			expected.allocateNode(NodeType.IDENTIFIER, 53, 58);
			expected.allocateNode(NodeType.BLOCK, 62, 67);
			expected.allocateNode(NodeType.ENHANCED_FOR_STATEMENT, 28, 67);
			expected.allocateNode(NodeType.BLOCK, 24, 70);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 70);
			expected.allocateClassDeclaration(0, 72, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 73);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments within while condition are correctly parsed.
	 */
	@Test
	public void shouldParseWhileWithCommentInCondition()
	{
		String source = """
			class Test
			{
				void m()
				{
					while (/* check */ true)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 35, 46);
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 47, 51);
			expected.allocateNode(NodeType.BLOCK, 55, 60);
			expected.allocateNode(NodeType.WHILE_STATEMENT, 28, 60);
			expected.allocateNode(NodeType.BLOCK, 24, 63);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 63);
			expected.allocateClassDeclaration(0, 65, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Else-If Comment Tests ==========

	/**
	 * Validates parsing of if-else with line comment between closing brace and else.
	 */
	@Test
	public void testLineCommentBeforeElse()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					if (x > 0)
					{
						y = 1;
					}
					// Comment before else
					else
					{
						y = 0;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 49);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 52, 53);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 48, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 63);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 63);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 66, 67);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 62, 67);
			expected.allocateNode(NodeType.BLOCK, 57, 72);
			expected.allocateNode(NodeType.LINE_COMMENT, 75, 97);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 112, 113);
			expected.allocateNode(NodeType.IDENTIFIER, 112, 113);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 116, 117);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 112, 117);
			expected.allocateNode(NodeType.BLOCK, 107, 122);
			expected.allocateNode(NodeType.IF_STATEMENT, 44, 122);
			expected.allocateNode(NodeType.BLOCK, 40, 125);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 125);
			expected.allocateClassDeclaration(7, 127, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 128);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of if-else if chain with comments between blocks.
	 */
	@Test
	public void testLineCommentBeforeElseIf()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					if (x > 0)
					{
						y = 1;
					}
					// First comment
					else if (x < 0)
					{
						y = -1;
					}
					// Second comment
					else
					{
						y = 0;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 49);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 52, 53);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 48, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 63);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 63);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 66, 67);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 62, 67);
			expected.allocateNode(NodeType.BLOCK, 57, 72);
			expected.allocateNode(NodeType.LINE_COMMENT, 75, 91);
			expected.allocateNode(NodeType.IDENTIFIER, 103, 104);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 107, 108);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 103, 108);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 117, 118);
			expected.allocateNode(NodeType.IDENTIFIER, 117, 118);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 122, 123);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 121, 123);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 117, 123);
			expected.allocateNode(NodeType.BLOCK, 112, 128);
			expected.allocateNode(NodeType.LINE_COMMENT, 131, 148);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 163, 164);
			expected.allocateNode(NodeType.IDENTIFIER, 163, 164);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 167, 168);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 163, 168);
			expected.allocateNode(NodeType.BLOCK, 158, 173);
			expected.allocateNode(NodeType.IF_STATEMENT, 99, 173);
			expected.allocateNode(NodeType.IF_STATEMENT, 44, 173);
			expected.allocateNode(NodeType.BLOCK, 40, 176);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 176);
			expected.allocateClassDeclaration(7, 178, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 179);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of if-else with block comment between closing brace and else.
	 */
	@Test
	public void testBlockCommentBeforeElse()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					if (x > 0)
					{
						y = 1;
					}
					/* Block comment */
					else
					{
						y = 0;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 49);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 52, 53);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 48, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 63);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 63);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 66, 67);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 62, 67);
			expected.allocateNode(NodeType.BLOCK, 57, 72);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 75, 94);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 109, 110);
			expected.allocateNode(NodeType.IDENTIFIER, 109, 110);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 113, 114);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 109, 114);
			expected.allocateNode(NodeType.BLOCK, 104, 119);
			expected.allocateNode(NodeType.IF_STATEMENT, 44, 119);
			expected.allocateNode(NodeType.BLOCK, 40, 122);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 122);
			expected.allocateClassDeclaration(7, 124, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 125);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Array Initializer Comment Tests ==========

	/**
	 * Verifies that comments between array elements are correctly parsed.
	 */
	@Test
	public void shouldParseArrayInitializerWithCommentBetweenElements()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = {1, /* middle */ 2, 3};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 41, 42);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 44, 56);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 57, 58);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 60, 61);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 40, 62);
			expected.allocateNode(NodeType.BLOCK, 24, 66);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 66);
			expected.allocateClassDeclaration(0, 68, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 69);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments before first array element are correctly parsed.
	 */
	@Test
	public void shouldParseArrayInitializerWithCommentBeforeFirstElement()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[] arr = {
						// first element
						1, 2, 3
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 45, 61);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 65, 66);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 68, 69);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 71, 72);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 40, 76);
			expected.allocateNode(NodeType.BLOCK, 24, 80);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 80);
			expected.allocateClassDeclaration(0, 82, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 83);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments in nested array initializers are correctly parsed.
	 */
	@Test
	public void shouldParseNestedArrayInitializerWithComments()
	{
		String source = """
			class Test
			{
				void m()
				{
					int[][] arr = {
						// row 1
						{1, 2},
						// row 2
						{3, 4}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 47, 55);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 60, 61);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 63, 64);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 59, 65);
			expected.allocateNode(NodeType.LINE_COMMENT, 70, 78);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 83, 84);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 86, 87);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 82, 88);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 42, 92);
			expected.allocateNode(NodeType.BLOCK, 24, 96);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 96);
			expected.allocateClassDeclaration(0, 98, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 99);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Extends/Implements Comment Tests ==========

	/**
	 * Tests class with line comment between extends and implements.
	 */
	@Test
	public void lineCommentBetweenExtendsAndImplements()
	{
		String source = """
			class Foo extends Base  // comment
			        implements Interface
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 22);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 63);
			expected.allocateClassDeclaration(0, 67, new TypeDeclarationAttribute("Foo"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 68);

			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests class with block comment between extends and implements.
	 */
	@Test
	public void blockCommentBetweenExtendsAndImplements()
	{
		String source = """
			class Foo extends Base /* comment */ implements Interface
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 22);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 57);
			expected.allocateClassDeclaration(0, 61, new TypeDeclarationAttribute("Foo"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);

			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Lambda Comment Tests ==========

	/**
	 * Verifies that comments after the arrow in expression lambdas are correctly parsed.
	 */
	@Test
	public void shouldParseLambdaWithCommentAfterArrow()
	{
		String source = """
			class Test
			{
				void m()
				{
					Runnable r = () -> /* comment */ System.out.println();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 36);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 47, 60);
			expected.allocateNode(NodeType.IDENTIFIER, 61, 67);
			expected.allocateNode(NodeType.FIELD_ACCESS, 61, 71);
			expected.allocateNode(NodeType.FIELD_ACCESS, 61, 79);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 61, 81);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 41, 81);
			expected.allocateNode(NodeType.BLOCK, 24, 85);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 85);
			expected.allocateClassDeclaration(0, 87, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 88);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that comments before lambda block are correctly parsed.
	 */
	@Test
	public void shouldParseLambdaWithCommentBeforeBlock()
	{
		String source = """
			class Test
			{
				void m()
				{
					Runnable r = () ->
						// body
						{
						};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 36);
			expected.allocateNode(NodeType.LINE_COMMENT, 50, 57);
			expected.allocateNode(NodeType.BLOCK, 61, 67);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 41, 67);
			expected.allocateNode(NodeType.BLOCK, 24, 71);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 71);
			expected.allocateClassDeclaration(0, 73, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 74);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Member Comment Tests ==========

	/**
	 * Verifies that a line comment after an annotation and before a method declaration is correctly parsed.
	 */
	@Test
	public void shouldParseLineCommentAfterAnnotationBeforeMethod()
	{
		String source = """
			public class Test
			{
				@Override
				// Comment explaining the method
				public void doSomething()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 30);
			expected.allocateNode(NodeType.ANNOTATION, 21, 30);
			expected.allocateNode(NodeType.LINE_COMMENT, 32, 64);
			expected.allocateNode(NodeType.BLOCK, 93, 97);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 97);
			expected.allocateClassDeclaration(7, 99, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 100);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a block comment after an annotation and before a field declaration is correctly parsed.
	 */
	@Test
	public void shouldParseBlockCommentAfterAnnotationBeforeField()
	{
		String source = """
			public class Test
			{
				@SuppressWarnings("unchecked")
				/* Block comment */
				private List<String> items;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 38);
			expected.allocateNode(NodeType.STRING_LITERAL, 39, 50);
			expected.allocateNode(NodeType.ANNOTATION, 21, 51);
			expected.allocateNode(NodeType.BLOCK_COMMENT, 53, 72);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 87, 93);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 87, 93);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 21, 101);
			expected.allocateClassDeclaration(7, 103, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 104);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a line comment after access modifiers and before a method declaration is correctly parsed.
	 */
	@Test
	public void shouldParseLineCommentAfterModifiersBeforeMethod()
	{
		String source = """
			public class Test
			{
				public static
				// Helper method
				void helper()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.LINE_COMMENT, 36, 52);
			expected.allocateNode(NodeType.BLOCK, 69, 73);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 73);
			expected.allocateClassDeclaration(7, 75, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 76);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that multiple comments between annotation and declaration are correctly parsed.
	 */
	@Test
	public void shouldParseMultipleCommentsBetweenAnnotationAndDeclaration()
	{
		String source = """
			public class Test
			{
				@Deprecated
				// First comment
				// Second comment
				public void old()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 32);
			expected.allocateNode(NodeType.ANNOTATION, 21, 32);
			expected.allocateNode(NodeType.LINE_COMMENT, 34, 50);
			expected.allocateNode(NodeType.LINE_COMMENT, 52, 69);
			expected.allocateNode(NodeType.BLOCK, 90, 94);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 94);
			expected.allocateClassDeclaration(7, 96, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 97);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a JavaDoc comment after an annotation is correctly parsed.
	 */
	@Test
	public void shouldParseJavadocAfterAnnotation()
	{
		String source = """
			public class Test
			{
				@Override
				/**
				 * Description.
				 */
				public void run()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 30);
			expected.allocateNode(NodeType.ANNOTATION, 21, 30);
			expected.allocateNode(NodeType.JAVADOC_COMMENT, 32, 57);
			expected.allocateNode(NodeType.BLOCK, 78, 82);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 82);
			expected.allocateClassDeclaration(7, 84, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 85);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a comment between annotation and field with initializer is correctly parsed.
	 */
	@Test
	public void shouldParseCommentAfterAnnotationBeforeFieldWithInitializer()
	{
		String source = """
			public class Test
			{
				@NonNull
				// Initialized value
				private String name = "test";
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 29);
			expected.allocateNode(NodeType.ANNOTATION, 21, 29);
			expected.allocateNode(NodeType.LINE_COMMENT, 31, 51);
			expected.allocateNode(NodeType.STRING_LITERAL, 75, 81);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 21, 82);
			expected.allocateClassDeclaration(7, 84, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 85);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a comment after annotation before a constructor is correctly parsed.
	 */
	@Test
	public void shouldParseCommentAfterAnnotationBeforeConstructor()
	{
		String source = """
			public class Test
			{
				@Deprecated
				// Old constructor
				public Test()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 32);
			expected.allocateNode(NodeType.ANNOTATION, 21, 32);
			expected.allocateNode(NodeType.LINE_COMMENT, 34, 52);
			expected.allocateNode(NodeType.BLOCK, 69, 73);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 21, 73);
			expected.allocateClassDeclaration(7, 75, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 76);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that a comment after annotation before a nested class is correctly parsed.
	 */
	@Test
	public void shouldParseCommentAfterAnnotationBeforeNestedClass()
	{
		String source = """
			public class Outer
			{
				@Deprecated
				// Nested type comment
				public class Inner
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 33);
			expected.allocateNode(NodeType.ANNOTATION, 22, 33);
			expected.allocateNode(NodeType.LINE_COMMENT, 35, 57);
			expected.allocateClassDeclaration(66, 83, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(7, 85, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 86);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Field Declaration Comment Tests ==========

	/**
	 * Tests block comment between type and field name.
	 */
	@Test
	public void testBlockCommentBetweenTypeAndFieldName()
	{
		String source = """
			class Test
			{
				private final Map/* <Class, Set<Signature>> */declToBridge;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 31, 60);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 73);
			expected.allocateClassDeclaration(0, 75, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 76);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests block comment in method return type position.
	 */
	@Test
	public void testBlockCommentInMethodReturnType()
	{
		String source = """
			class Test
			{
				public Map/*<Signature, Signature>*/resolveAll()
				{
					return null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK_COMMENT, 24, 50);
			expected.allocateNode(NodeType.NULL_LITERAL, 75, 79);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 68, 80);
			expected.allocateNode(NodeType.BLOCK, 64, 83);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 83);
			expected.allocateClassDeclaration(0, 85, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 86);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests array initializer with trailing comma parses correctly.
	 */
	@Test
	public void testArrayInitializerWithTrailingComma()
	{
		String source = """
			class Test
			{
				private final Class<?>[] testClasses = new Class<?>[]
				{
					String.class,
					Integer.class,
				};
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.WILDCARD_TYPE, 34, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 62);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 63, 64);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 57, 65);
			expected.allocateNode(NodeType.IDENTIFIER, 73, 79);
			expected.allocateNode(NodeType.CLASS_LITERAL, 73, 85);
			expected.allocateNode(NodeType.IDENTIFIER, 89, 96);
			expected.allocateNode(NodeType.CLASS_LITERAL, 89, 102);
			expected.allocateNode(NodeType.ARRAY_CREATION, 53, 106);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 107);
			expected.allocateClassDeclaration(0, 109, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 110);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests lambda expression in generic variable declaration.
	 */
	@Test
	public void testLambdaInGenericVariableDeclaration()
	{
		String source = """
			class Test
			{
				void m()
				{
					java.util.function.Function<String, Integer> f = s -> s.length();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 62);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 62);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 64, 71);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 64, 71);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 72);
			expected.allocateNode(NodeType.IDENTIFIER, 82, 83);
			expected.allocateNode(NodeType.FIELD_ACCESS, 82, 90);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 82, 92);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 77, 92);
			expected.allocateNode(NodeType.BLOCK, 24, 96);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 96);
			expected.allocateClassDeclaration(0, 98, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 99);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests generic method call chain parses correctly.
	 */
	@Test
	public void testGenericMethodCallChain()
	{
		String source = """
			class Test
			{
				void m()
				{
					java.util.Map<String, Integer> map = java.util.Map.of("a", 13);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 57);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 57);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 28, 58);
			expected.allocateNode(NodeType.IDENTIFIER, 65, 69);
			expected.allocateNode(NodeType.FIELD_ACCESS, 65, 74);
			expected.allocateNode(NodeType.FIELD_ACCESS, 65, 78);
			expected.allocateNode(NodeType.FIELD_ACCESS, 65, 81);
			expected.allocateNode(NodeType.STRING_LITERAL, 82, 85);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 87, 89);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 65, 90);
			expected.allocateNode(NodeType.BLOCK, 24, 94);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 94);
			expected.allocateClassDeclaration(0, 96, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 97);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
