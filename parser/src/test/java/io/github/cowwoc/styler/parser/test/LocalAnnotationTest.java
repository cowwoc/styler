package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing local variable declarations with annotations and the {@code final} modifier.
 */
public class LocalAnnotationTest
{
	/**
	 * Validates parsing of local variable declaration with single annotation before type.
	 * Tests the most common case of suppressing unchecked warnings on a generic variable.
	 */
	@Test
	public void shouldParseLocalVariableWithSingleAnnotation()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					@SuppressWarnings("unchecked") java.util.List<String> result = null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 64);
			expected.allocateNode(NodeType.STRING_LITERAL, 65, 76);
			expected.allocateNode(NodeType.ANNOTATION, 47, 77);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 78, 92);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 93, 99);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 93, 99);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 78, 100);
			expected.allocateNode(NodeType.NULL_LITERAL, 110, 114);
			expected.allocateNode(NodeType.BLOCK, 43, 118);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 118);
			expected.allocateClassDeclaration(7, 120, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 121);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of local variable declaration with {@code final} keyword only (no annotations).
	 * Tests basic final modifier handling without annotation complexity.
	 */
	@Test
	public void shouldParseLocalVariableWithFinalModifier()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					final int x = 1;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 61, 62);
			expected.allocateNode(NodeType.BLOCK, 43, 66);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 66);
			expected.allocateClassDeclaration(7, 68, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 69);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of local variable declaration with nullable annotation.
	 * Tests common null-safety annotation pattern used in frameworks like JSpecify or JetBrains annotations.
	 */
	@Test
	public void shouldParseLocalVariableWithNullableAnnotation()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					@Nullable String value = null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 56);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 56);
			expected.allocateNode(NodeType.ANNOTATION, 47, 56);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 63);
			expected.allocateNode(NodeType.NULL_LITERAL, 72, 76);
			expected.allocateNode(NodeType.BLOCK, 43, 80);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 80);
			expected.allocateClassDeclaration(7, 82, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 83);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of local variable with {@code final} followed by type annotation.
	 * Tests the order: final keyword is consumed before parseType() handles the type annotation.
	 */
	@Test
	public void shouldParseLocalVariableWithFinalAndTypeAnnotation()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					final @NonNull Object obj = new Object();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 61);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 61);
			expected.allocateNode(NodeType.ANNOTATION, 53, 61);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 68);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 79, 85);
			expected.allocateNode(NodeType.OBJECT_CREATION, 75, 87);
			expected.allocateNode(NodeType.BLOCK, 43, 91);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 91);
			expected.allocateClassDeclaration(7, 93, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 94);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of local variable declaration with multiple annotations before type.
	 * Tests that the parser correctly consumes all declaration annotations in sequence.
	 */
	@Test
	public void shouldParseLocalVariableWithMultipleDeclarationAnnotations()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					@Nonnull @SuppressWarnings("unused") int x = 1;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 73);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 55);
			expected.allocateNode(NodeType.ANNOTATION, 47, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 73);
			expected.allocateNode(NodeType.STRING_LITERAL, 74, 82);
			expected.allocateNode(NodeType.ANNOTATION, 56, 83);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 92, 93);
			expected.allocateNode(NodeType.BLOCK, 43, 97);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 97);
			expected.allocateClassDeclaration(7, 99, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 100);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing with full annotation chain: declaration annotation + final + type annotation.
	 * Tests the most complex local variable declaration pattern.
	 */
	@Test
	public void shouldParseLocalVariableWithDeclarationAndTypeAnnotations()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					@SuppressWarnings("unused") final @NonNull String s = "";
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 82, 89);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 64);
			expected.allocateNode(NodeType.STRING_LITERAL, 65, 73);
			expected.allocateNode(NodeType.ANNOTATION, 47, 74);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 82, 89);
			expected.allocateNode(NodeType.ANNOTATION, 81, 89);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 90, 96);
			expected.allocateNode(NodeType.STRING_LITERAL, 101, 103);
			expected.allocateNode(NodeType.BLOCK, 43, 107);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 107);
			expected.allocateClassDeclaration(7, 109, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 110);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of enhanced for-loop with annotated loop variable.
	 * Tests that looksLikeTypeStart() recognizes annotations in for-each context.
	 */
	@Test
	public void shouldParseEnhancedForLoopWithAnnotatedVariable()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					java.util.List<String> list = null;
					for (@NonNull String s : list)
					{
						System.out.println(s);
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 61);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 68);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 68);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 47, 69);
			expected.allocateNode(NodeType.NULL_LITERAL, 77, 81);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 91, 98);
			expected.allocateNode(NodeType.ANNOTATION, 90, 98);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 99, 105);
			expected.allocateNode(NodeType.IDENTIFIER, 110, 114);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 123, 141);
			expected.allocateNode(NodeType.IDENTIFIER, 123, 129);
			expected.allocateNode(NodeType.FIELD_ACCESS, 123, 133);
			expected.allocateNode(NodeType.FIELD_ACCESS, 123, 141);
			expected.allocateNode(NodeType.IDENTIFIER, 142, 143);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 123, 144);
			expected.allocateNode(NodeType.BLOCK, 118, 149);
			expected.allocateNode(NodeType.ENHANCED_FOR_STATEMENT, 85, 149);
			expected.allocateNode(NodeType.BLOCK, 43, 152);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 152);
			expected.allocateClassDeclaration(7, 154, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 155);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of enhanced for-loop with final modifier and annotation.
	 * Tests combined final and annotation handling in enhanced for-loop context.
	 */
	@Test
	public void shouldParseEnhancedForLoopWithFinalAndAnnotation()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					java.util.List<String> list = null;
					for (final @NonNull String s : list)
					{
						System.out.println(s);
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 61);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 68);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 68);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 47, 69);
			expected.allocateNode(NodeType.NULL_LITERAL, 77, 81);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 97, 104);
			expected.allocateNode(NodeType.ANNOTATION, 96, 104);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 105, 111);
			expected.allocateNode(NodeType.IDENTIFIER, 116, 120);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 129, 147);
			expected.allocateNode(NodeType.IDENTIFIER, 129, 135);
			expected.allocateNode(NodeType.FIELD_ACCESS, 129, 139);
			expected.allocateNode(NodeType.FIELD_ACCESS, 129, 147);
			expected.allocateNode(NodeType.IDENTIFIER, 148, 149);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 129, 150);
			expected.allocateNode(NodeType.BLOCK, 124, 155);
			expected.allocateNode(NodeType.ENHANCED_FOR_STATEMENT, 85, 155);
			expected.allocateNode(NodeType.BLOCK, 43, 158);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 158);
			expected.allocateClassDeclaration(7, 160, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 161);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of try-with-resources with annotated resource declaration.
	 * Tests that parseResource() consumes annotation before type parsing.
	 */
	@Test
	public void shouldParseTryWithResourcesWithAnnotation()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					try (@SuppressWarnings("resource") java.io.InputStream in = null)
					{
						int b = in.read();
					}
					catch (Exception e)
					{
						// ignored
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 53, 69);
			expected.allocateNode(NodeType.STRING_LITERAL, 70, 80);
			expected.allocateNode(NodeType.ANNOTATION, 52, 81);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 82, 101);
			expected.allocateNode(NodeType.NULL_LITERAL, 107, 111);
			expected.allocateNode(NodeType.IDENTIFIER, 128, 130);
			expected.allocateNode(NodeType.FIELD_ACCESS, 128, 135);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 128, 137);
			expected.allocateNode(NodeType.BLOCK, 115, 142);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 152, 161);
			expected.allocateParameterDeclaration(152, 163, new ParameterAttribute("e", false, false, false));
			expected.allocateNode(NodeType.LINE_COMMENT, 172, 182);
			expected.allocateNode(NodeType.BLOCK, 167, 186);
			expected.allocateNode(NodeType.CATCH_CLAUSE, 145, 186);
			expected.allocateNode(NodeType.TRY_STATEMENT, 47, 186);
			expected.allocateNode(NodeType.BLOCK, 43, 189);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 189);
			expected.allocateClassDeclaration(7, 191, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 192);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of try-with-resources with final modifier and annotation.
	 * Tests combined final and annotation handling in resource declaration context.
	 */
	@Test
	public void shouldParseTryWithResourcesWithFinalAndAnnotation()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					try (final @NonNull java.io.InputStream in = null)
					{
						int b = in.read();
					}
					catch (Exception e)
					{
						// ignored
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 59, 66);
			expected.allocateNode(NodeType.ANNOTATION, 58, 66);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 67, 86);
			expected.allocateNode(NodeType.NULL_LITERAL, 92, 96);
			expected.allocateNode(NodeType.IDENTIFIER, 113, 115);
			expected.allocateNode(NodeType.FIELD_ACCESS, 113, 120);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 113, 122);
			expected.allocateNode(NodeType.BLOCK, 100, 127);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 137, 146);
			expected.allocateParameterDeclaration(137, 148, new ParameterAttribute("e", false, false, false));
			expected.allocateNode(NodeType.LINE_COMMENT, 157, 167);
			expected.allocateNode(NodeType.BLOCK, 152, 171);
			expected.allocateNode(NodeType.CATCH_CLAUSE, 130, 171);
			expected.allocateNode(NodeType.TRY_STATEMENT, 47, 171);
			expected.allocateNode(NodeType.BLOCK, 43, 174);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 174);
			expected.allocateClassDeclaration(7, 176, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 177);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of try-with-resources with multiple annotations on resource.
	 * Tests that parseResource() correctly consumes multiple declaration annotations.
	 */
	@Test
	public void shouldParseTryWithResourcesWithMultipleAnnotations()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					try (@SuppressWarnings("resource") @NonNull java.io.InputStream in = null)
					{
						int b = in.read();
					}
					catch (Exception e)
					{
						// ignored
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 53, 69);
			expected.allocateNode(NodeType.STRING_LITERAL, 70, 80);
			expected.allocateNode(NodeType.ANNOTATION, 52, 81);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 83, 90);
			expected.allocateNode(NodeType.ANNOTATION, 82, 90);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 91, 110);
			expected.allocateNode(NodeType.NULL_LITERAL, 116, 120);
			expected.allocateNode(NodeType.IDENTIFIER, 137, 139);
			expected.allocateNode(NodeType.FIELD_ACCESS, 137, 144);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 137, 146);
			expected.allocateNode(NodeType.BLOCK, 124, 151);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 161, 170);
			expected.allocateParameterDeclaration(161, 172, new ParameterAttribute("e", false, false, false));
			expected.allocateNode(NodeType.LINE_COMMENT, 181, 191);
			expected.allocateNode(NodeType.BLOCK, 176, 195);
			expected.allocateNode(NodeType.CATCH_CLAUSE, 154, 195);
			expected.allocateNode(NodeType.TRY_STATEMENT, 47, 195);
			expected.allocateNode(NodeType.BLOCK, 43, 198);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 198);
			expected.allocateClassDeclaration(7, 200, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 201);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of annotation before var keyword for type inference.
	 * Tests annotation handling with Java 10+ local variable type inference.
	 */
	@Test
	public void shouldParseVarWithAnnotation()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					@SuppressWarnings("unchecked") var x = java.util.List.of(1, 2, 3);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 64);
			expected.allocateNode(NodeType.STRING_LITERAL, 65, 76);
			expected.allocateNode(NodeType.ANNOTATION, 47, 77);
			expected.allocateNode(NodeType.IDENTIFIER, 86, 90);
			expected.allocateNode(NodeType.FIELD_ACCESS, 86, 95);
			expected.allocateNode(NodeType.FIELD_ACCESS, 86, 100);
			expected.allocateNode(NodeType.FIELD_ACCESS, 86, 103);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 104, 105);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 107, 108);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 110, 111);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 86, 112);
			expected.allocateNode(NodeType.BLOCK, 43, 116);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 116);
			expected.allocateClassDeclaration(7, 118, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 119);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of annotation in regular for-loop initializer.
	 * Tests that parseExpressionOrVariableStatement() recognizes annotations in for-loop context.
	 */
	@Test
	public void shouldParseRegularForLoopWithAnnotatedInitializer()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					for (@SuppressWarnings("unused") int i = 0; i < 10; ++i)
					{
						System.out.println(i);
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 53, 69);
			expected.allocateNode(NodeType.STRING_LITERAL, 70, 78);
			expected.allocateNode(NodeType.ANNOTATION, 52, 79);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 53, 69);
			expected.allocateNode(NodeType.STRING_LITERAL, 70, 78);
			expected.allocateNode(NodeType.ANNOTATION, 52, 79);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 88, 89);
			expected.allocateNode(NodeType.IDENTIFIER, 91, 92);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 95, 97);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 91, 97);
			expected.allocateNode(NodeType.IDENTIFIER, 101, 102);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 99, 102);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 111, 129);
			expected.allocateNode(NodeType.IDENTIFIER, 111, 117);
			expected.allocateNode(NodeType.FIELD_ACCESS, 111, 121);
			expected.allocateNode(NodeType.FIELD_ACCESS, 111, 129);
			expected.allocateNode(NodeType.IDENTIFIER, 130, 131);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 111, 132);
			expected.allocateNode(NodeType.BLOCK, 106, 137);
			expected.allocateNode(NodeType.FOR_STATEMENT, 47, 137);
			expected.allocateNode(NodeType.BLOCK, 43, 140);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 140);
			expected.allocateClassDeclaration(7, 142, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 143);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of annotation with named argument in local variable declaration.
	 * Tests annotation element-value pair syntax.
	 */
	@Test
	public void shouldParseAnnotationWithArguments()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					@SuppressWarnings(value = "unchecked") java.util.List<String> list = null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 64);
			expected.allocateNode(NodeType.IDENTIFIER, 65, 70);
			expected.allocateNode(NodeType.STRING_LITERAL, 73, 84);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 65, 84);
			expected.allocateNode(NodeType.ANNOTATION, 47, 85);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 86, 100);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 101, 107);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 101, 107);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 86, 108);
			expected.allocateNode(NodeType.NULL_LITERAL, 116, 120);
			expected.allocateNode(NodeType.BLOCK, 43, 124);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 124);
			expected.allocateClassDeclaration(7, 126, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 127);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of annotation with array argument in local variable declaration.
	 * Tests annotation array initializer syntax.
	 */
	@Test
	public void shouldParseAnnotationWithMultipleArguments()
	{
		String source = """
			public class Test
			{
				public void method()
				{
					@SuppressWarnings({"unchecked", "rawtypes"}) java.util.List list = null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 64);
			expected.allocateNode(NodeType.STRING_LITERAL, 66, 77);
			expected.allocateNode(NodeType.STRING_LITERAL, 79, 89);
			expected.allocateNode(NodeType.ARRAY_INITIALIZER, 65, 90);
			expected.allocateNode(NodeType.ANNOTATION, 47, 91);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 92, 106);
			expected.allocateNode(NodeType.NULL_LITERAL, 114, 118);
			expected.allocateNode(NodeType.BLOCK, 43, 122);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 122);
			expected.allocateClassDeclaration(7, 124, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 125);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
