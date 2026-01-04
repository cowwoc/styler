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
 * Tests for parsing parameter declarations in methods, constructors, catch clauses, and records.
 */
public final class ParameterDeclarationParserTest
{
	// ==================== Simple Parameter Tests ====================

	/**
	 * Validates parsing of a method with a simple primitive parameter.
	 */
	@Test
	public void shouldParseSimplePrimitiveParameter()
	{
		String source = """
			public class Test
			{
				public void foo(int x)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 42, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 45, 49);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 49);
			expected.allocateClassDeclaration(7, 51, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 52);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a method with a simple reference type parameter.
	 */
	@Test
	public void shouldParseSimpleReferenceTypeParameter()
	{
		String source = """
			public class Test
			{
				public void foo(String name)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 43);
			expected.allocateParameterDeclaration(37, 48, new ParameterAttribute("name", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 51, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 55);
			expected.allocateClassDeclaration(7, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a method with a qualified type parameter.
	 */
	@Test
	public void shouldParseQualifiedTypeParameter()
	{
		String source = """
			public class Test
			{
				public void foo(java.util.List list)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 51);
			expected.allocateParameterDeclaration(37, 56, new ParameterAttribute("list", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 59, 63);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 63);
			expected.allocateClassDeclaration(7, 65, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a method with multiple parameters.
	 */
	@Test
	public void shouldParseMultipleParameters()
	{
		String source = """
			public class Test
			{
				public void foo(int x, String y, double z)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 42, new ParameterAttribute("x", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 52, new ParameterAttribute("y", false, false, false));
			expected.allocateParameterDeclaration(54, 62, new ParameterAttribute("z", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 65, 69);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 69);
			expected.allocateClassDeclaration(7, 71, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 72);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Modifier Tests ====================

	/**
	 * Validates parsing of a parameter with the {@code final} modifier.
	 */
	@Test
	public void shouldParseFinalParameter()
	{
		String source = """
			public class Test
			{
				public void foo(final String name)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 49);
			expected.allocateParameterDeclaration(37, 54, new ParameterAttribute("name", false, true, false));
			expected.allocateNode(NodeType.BLOCK, 57, 61);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 61);
			expected.allocateClassDeclaration(7, 63, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 64);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a final primitive parameter.
	 */
	@Test
	public void shouldParseFinalPrimitiveParameter()
	{
		String source = """
			public class Test
			{
				public void foo(final int count)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 52, new ParameterAttribute("count", false, true, false));
			expected.allocateNode(NodeType.BLOCK, 55, 59);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 59);
			expected.allocateClassDeclaration(7, 61, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of multiple final parameters.
	 */
	@Test
	public void shouldParseMultipleFinalParameters()
	{
		String source = """
			public class Test
			{
				public void foo(final int x, final String y)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 48, new ParameterAttribute("x", false, true, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 62);
			expected.allocateParameterDeclaration(50, 64, new ParameterAttribute("y", false, true, false));
			expected.allocateNode(NodeType.BLOCK, 67, 71);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 71);
			expected.allocateClassDeclaration(7, 73, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 74);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Varargs Tests ====================

	/**
	 * Validates parsing of a varargs parameter.
	 */
	@Test
	public void shouldParseVarargsParameter()
	{
		String source = """
			public class Test
			{
				public void foo(String... args)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 43);
			expected.allocateParameterDeclaration(37, 51, new ParameterAttribute("args", true, false, false));
			expected.allocateNode(NodeType.BLOCK, 54, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 58);
			expected.allocateClassDeclaration(7, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of varargs with preceding parameters.
	 */
	@Test
	public void shouldParseVarargsWithPrecedingParameters()
	{
		String source = """
			public class Test
			{
				public void foo(int first, String... rest)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 46, new ParameterAttribute("first", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 54);
			expected.allocateParameterDeclaration(48, 62, new ParameterAttribute("rest", true, false, false));
			expected.allocateNode(NodeType.BLOCK, 65, 69);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 69);
			expected.allocateClassDeclaration(7, 71, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 72);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Annotated Parameter Tests ====================

	/**
	 * Validates parsing of an annotated parameter.
	 */
	@Test
	public void shouldParseAnnotatedParameter()
	{
		String source = """
			public class Test
			{
				public void foo(@NotNull String name)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 45);
			expected.allocateNode(NodeType.ANNOTATION, 37, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateParameterDeclaration(37, 57, new ParameterAttribute("name", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 60, 64);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 64);
			expected.allocateClassDeclaration(7, 66, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 67);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a parameter with multiple annotations.
	 */
	@Test
	public void shouldParseMultipleAnnotationsOnParameter()
	{
		String source = """
			public class Test
			{
				public void foo(@NotNull @Nullable String name)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 45);
			expected.allocateNode(NodeType.ANNOTATION, 37, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 55);
			expected.allocateNode(NodeType.ANNOTATION, 46, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 62);
			expected.allocateParameterDeclaration(37, 67, new ParameterAttribute("name", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 70, 74);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 74);
			expected.allocateClassDeclaration(7, 76, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 77);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Complex Type Tests ====================

	/**
	 * Validates parsing of a generic type parameter.
	 */
	@Test
	public void shouldParseGenericTypeParameter()
	{
		String source = """
			public class Test
			{
				public void foo(List<String> items)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 48);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 37, 49);
			expected.allocateParameterDeclaration(37, 55, new ParameterAttribute("items", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 58, 62);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 62);
			expected.allocateClassDeclaration(7, 64, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a nested generic type parameter.
	 */
	@Test
	public void shouldParseNestedGenericTypeParameter()
	{
		String source = """
			public class Test
			{
				public void foo(Map<String, List<Integer>> data)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 61);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 61);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 49, 63);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 63);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 37, 63);
			expected.allocateParameterDeclaration(37, 68, new ParameterAttribute("data", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 71, 75);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 75);
			expected.allocateClassDeclaration(7, 77, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 78);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of an array type parameter.
	 */
	@Test
	public void shouldParseArrayTypeParameter()
	{
		String source = """
			public class Test
			{
				public void foo(String[] names)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 43);
			expected.allocateParameterDeclaration(37, 51, new ParameterAttribute("names", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 54, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 58);
			expected.allocateClassDeclaration(7, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a multi-dimensional array type parameter.
	 */
	@Test
	public void shouldParseMultiDimensionalArrayParameter()
	{
		String source = """
			public class Test
			{
				public void foo(int[][] matrix)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(37, 51, new ParameterAttribute("matrix", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 54, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 58);
			expected.allocateClassDeclaration(7, 60, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Context Tests ====================

	/**
	 * Validates parsing of constructor parameters.
	 */
	@Test
	public void shouldParseConstructorParameters()
	{
		String source = """
			public class Test
			{
				public Test(String name, int value)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 33, 39);
			expected.allocateParameterDeclaration(33, 44, new ParameterAttribute("name", false, false, false));
			expected.allocateParameterDeclaration(46, 55, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 58, 62);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 21, 62);
			expected.allocateClassDeclaration(7, 64, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of record components as parameters.
	 */
	@Test
	public void shouldParseRecordComponentsAsParameters()
	{
		String source = """
			public record Point(int x, int y)
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(20, 25, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(27, 32, new ParameterAttribute("y", false, false, false));
			expected.allocateRecordDeclaration(7, 37, new TypeDeclarationAttribute("Point"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 38);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of catch clause parameter.
	 */
	@Test
	public void shouldParseCatchClauseParameter()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					try
					{
					}
					catch (Exception e)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 50, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 65, 74);
			expected.allocateParameterDeclaration(65, 76, new ParameterAttribute("e", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 80, 85);
			expected.allocateNode(NodeType.CATCH_CLAUSE, 58, 85);
			expected.allocateNode(NodeType.TRY_STATEMENT, 44, 85);
			expected.allocateNode(NodeType.BLOCK, 40, 88);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 88);
			expected.allocateClassDeclaration(7, 90, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 91);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Edge Case Tests ====================

	/**
	 * Validates parsing of receiver parameter.
	 */
	@Test
	public void shouldParseReceiverParameter()
	{
		String source = """
			public class Test
			{
				public void method(Test this)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 44);
			expected.allocateParameterDeclaration(40, 49, new ParameterAttribute("this", false, false, true));
			expected.allocateNode(NodeType.BLOCK, 52, 56);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 56);
			expected.allocateClassDeclaration(7, 58, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 59);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of C-style array parameter syntax.
	 */
	@Test
	public void shouldParseCStyleArrayParameter()
	{
		String source = """
			public class Test
			{
				public void foo(String args[])
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 43);
			expected.allocateParameterDeclaration(37, 50, new ParameterAttribute("args", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 53, 57);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 57);
			expected.allocateClassDeclaration(7, 59, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
