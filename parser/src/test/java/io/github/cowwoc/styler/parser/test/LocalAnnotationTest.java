package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ANNOTATION;
import static io.github.cowwoc.styler.ast.core.NodeType.ARRAY_INITIALIZER;
import static io.github.cowwoc.styler.ast.core.NodeType.ASSIGNMENT_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BINARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CATCH_CLAUSE;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.ENHANCED_FOR_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.FOR_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.LINE_COMMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.NULL_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.OBJECT_CREATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETERIZED_TYPE;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.STRING_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.TRY_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.UNARY_EXPRESSION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void method()
				{
					@SuppressWarnings("unchecked") java.util.List<String> result = null;
				}
			}
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 121),
			semanticNode(CLASS_DECLARATION, 7, 120, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 118),
			semanticNode(BLOCK, 43, 118),
			semanticNode(ANNOTATION, 47, 77),
			semanticNode(QUALIFIED_NAME, 48, 64),
			semanticNode(STRING_LITERAL, 65, 76),
			semanticNode(PARAMETERIZED_TYPE, 78, 100),
			semanticNode(QUALIFIED_NAME, 78, 92),
			semanticNode(QUALIFIED_NAME, 93, 99),
			semanticNode(NULL_LITERAL, 110, 114));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of local variable declaration with {@code final} keyword only (no annotations).
	 * Tests basic final modifier handling without annotation complexity.
	 */
	@Test
	public void shouldParseLocalVariableWithFinalModifier()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void method()
				{
					final int x = 1;
				}
			}
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 69),
			semanticNode(CLASS_DECLARATION, 7, 68, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 66),
			semanticNode(BLOCK, 43, 66),
			semanticNode(INTEGER_LITERAL, 61, 62));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of local variable declaration with nullable annotation.
	 * Tests common null-safety annotation pattern used in frameworks like JSpecify or JetBrains annotations.
	 */
	@Test
	public void shouldParseLocalVariableWithNullableAnnotation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void method()
				{
					@Nullable String value = null;
				}
			}
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 83),
			semanticNode(CLASS_DECLARATION, 7, 82, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 80),
			semanticNode(BLOCK, 43, 80),
			semanticNode(ANNOTATION, 47, 56),
			semanticNode(QUALIFIED_NAME, 48, 56),
			semanticNode(QUALIFIED_NAME, 57, 63),
			semanticNode(NULL_LITERAL, 72, 76));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of local variable with {@code final} followed by type annotation.
	 * Tests the order: final keyword is consumed before parseType() handles the type annotation.
	 */
	@Test
	public void shouldParseLocalVariableWithFinalAndTypeAnnotation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void method()
				{
					final @NonNull Object obj = new Object();
				}
			}
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 94),
			semanticNode(CLASS_DECLARATION, 7, 93, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 91),
			semanticNode(BLOCK, 43, 91),
			semanticNode(ANNOTATION, 53, 61),
			semanticNode(QUALIFIED_NAME, 54, 61),
			semanticNode(QUALIFIED_NAME, 62, 68),
			semanticNode(OBJECT_CREATION, 75, 87),
			semanticNode(QUALIFIED_NAME, 79, 85));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of local variable declaration with multiple annotations before type.
	 * Tests that the parser correctly consumes all declaration annotations in sequence.
	 */
	@Test
	public void shouldParseLocalVariableWithMultipleDeclarationAnnotations()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void method()
				{
					@Nonnull @SuppressWarnings("unused") int x = 1;
				}
			}
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 100),
			semanticNode(CLASS_DECLARATION, 7, 99, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 97),
			semanticNode(BLOCK, 43, 97),
			semanticNode(ANNOTATION, 47, 55),
			semanticNode(QUALIFIED_NAME, 48, 55),
			semanticNode(ANNOTATION, 56, 83),
			semanticNode(QUALIFIED_NAME, 57, 73),
			semanticNode(STRING_LITERAL, 74, 82),
			semanticNode(INTEGER_LITERAL, 92, 93));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing with full annotation chain: declaration annotation + final + type annotation.
	 * Tests the most complex local variable declaration pattern.
	 */
	@Test
	public void shouldParseLocalVariableWithDeclarationAndTypeAnnotations()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void method()
				{
					@SuppressWarnings("unused") final @NonNull String s = "";
				}
			}
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 110),
			semanticNode(CLASS_DECLARATION, 7, 109, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 107),
			semanticNode(BLOCK, 43, 107),
			semanticNode(ANNOTATION, 47, 74),
			semanticNode(QUALIFIED_NAME, 48, 64),
			semanticNode(STRING_LITERAL, 65, 73),
			semanticNode(ANNOTATION, 81, 89),
			semanticNode(QUALIFIED_NAME, 82, 89),
			semanticNode(QUALIFIED_NAME, 90, 96),
			semanticNode(STRING_LITERAL, 101, 103));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of enhanced for-loop with annotated loop variable.
	 * Tests that looksLikeTypeStart() recognizes annotations in for-each context.
	 */
	@Test
	public void shouldParseEnhancedForLoopWithAnnotatedVariable()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 155),
			semanticNode(CLASS_DECLARATION, 7, 154, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 152),
			semanticNode(BLOCK, 43, 152),
			semanticNode(PARAMETERIZED_TYPE, 47, 69),
			semanticNode(QUALIFIED_NAME, 47, 61),
			semanticNode(QUALIFIED_NAME, 62, 68),
			semanticNode(NULL_LITERAL, 77, 81),
			semanticNode(ENHANCED_FOR_STATEMENT, 85, 149),
			semanticNode(ANNOTATION, 90, 98),
			semanticNode(QUALIFIED_NAME, 91, 98),
			semanticNode(QUALIFIED_NAME, 99, 105),
			semanticNode(IDENTIFIER, 110, 114),
			semanticNode(BLOCK, 118, 149),
			semanticNode(IDENTIFIER, 123, 129),
			semanticNode(METHOD_INVOCATION, 123, 144),
			semanticNode(FIELD_ACCESS, 123, 133),
			semanticNode(FIELD_ACCESS, 123, 141),
			semanticNode(QUALIFIED_NAME, 123, 141),
			semanticNode(IDENTIFIER, 142, 143));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of enhanced for-loop with final modifier and annotation.
	 * Tests combined final and annotation handling in enhanced for-loop context.
	 */
	@Test
	public void shouldParseEnhancedForLoopWithFinalAndAnnotation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 161),
			semanticNode(CLASS_DECLARATION, 7, 160, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 158),
			semanticNode(BLOCK, 43, 158),
			semanticNode(PARAMETERIZED_TYPE, 47, 69),
			semanticNode(QUALIFIED_NAME, 47, 61),
			semanticNode(QUALIFIED_NAME, 62, 68),
			semanticNode(NULL_LITERAL, 77, 81),
			semanticNode(ENHANCED_FOR_STATEMENT, 85, 155),
			semanticNode(ANNOTATION, 96, 104),
			semanticNode(QUALIFIED_NAME, 97, 104),
			semanticNode(QUALIFIED_NAME, 105, 111),
			semanticNode(IDENTIFIER, 116, 120),
			semanticNode(BLOCK, 124, 155),
			semanticNode(IDENTIFIER, 129, 135),
			semanticNode(METHOD_INVOCATION, 129, 150),
			semanticNode(FIELD_ACCESS, 129, 139),
			semanticNode(FIELD_ACCESS, 129, 147),
			semanticNode(QUALIFIED_NAME, 129, 147),
			semanticNode(IDENTIFIER, 148, 149));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of try-with-resources with annotated resource declaration.
	 * Tests that parseResource() consumes annotation before type parsing.
	 */
	@Test
	public void shouldParseTryWithResourcesWithAnnotation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 192),
			semanticNode(CLASS_DECLARATION, 7, 191, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 189),
			semanticNode(BLOCK, 43, 189),
			semanticNode(TRY_STATEMENT, 47, 186),
			semanticNode(ANNOTATION, 52, 81),
			semanticNode(QUALIFIED_NAME, 53, 69),
			semanticNode(STRING_LITERAL, 70, 80),
			semanticNode(QUALIFIED_NAME, 82, 101),
			semanticNode(NULL_LITERAL, 107, 111),
			semanticNode(BLOCK, 115, 142),
			semanticNode(IDENTIFIER, 128, 130),
			semanticNode(METHOD_INVOCATION, 128, 137),
			semanticNode(FIELD_ACCESS, 128, 135),
			semanticNode(CATCH_CLAUSE, 145, 186),
			semanticNode(QUALIFIED_NAME, 152, 161),
			semanticNode(PARAMETER_DECLARATION, 152, 163, "e"),
			semanticNode(BLOCK, 167, 186),
			semanticNode(LINE_COMMENT, 172, 182));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of try-with-resources with final modifier and annotation.
	 * Tests combined final and annotation handling in resource declaration context.
	 */
	@Test
	public void shouldParseTryWithResourcesWithFinalAndAnnotation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 177),
			semanticNode(CLASS_DECLARATION, 7, 176, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 174),
			semanticNode(BLOCK, 43, 174),
			semanticNode(TRY_STATEMENT, 47, 171),
			semanticNode(ANNOTATION, 58, 66),
			semanticNode(QUALIFIED_NAME, 59, 66),
			semanticNode(QUALIFIED_NAME, 67, 86),
			semanticNode(NULL_LITERAL, 92, 96),
			semanticNode(BLOCK, 100, 127),
			semanticNode(FIELD_ACCESS, 113, 120),
			semanticNode(IDENTIFIER, 113, 115),
			semanticNode(METHOD_INVOCATION, 113, 122),
			semanticNode(CATCH_CLAUSE, 130, 171),
			semanticNode(QUALIFIED_NAME, 137, 146),
			semanticNode(PARAMETER_DECLARATION, 137, 148, "e"),
			semanticNode(BLOCK, 152, 171),
			semanticNode(LINE_COMMENT, 157, 167));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of try-with-resources with multiple annotations on resource.
	 * Tests that parseResource() correctly consumes multiple declaration annotations.
	 */
	@Test
	public void shouldParseTryWithResourcesWithMultipleAnnotations()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 201),
			semanticNode(CLASS_DECLARATION, 7, 200, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 198),
			semanticNode(BLOCK, 43, 198),
			semanticNode(TRY_STATEMENT, 47, 195),
			semanticNode(ANNOTATION, 52, 81),
			semanticNode(QUALIFIED_NAME, 53, 69),
			semanticNode(STRING_LITERAL, 70, 80),
			semanticNode(ANNOTATION, 82, 90),
			semanticNode(QUALIFIED_NAME, 83, 90),
			semanticNode(QUALIFIED_NAME, 91, 110),
			semanticNode(NULL_LITERAL, 116, 120),
			semanticNode(BLOCK, 124, 151),
			semanticNode(IDENTIFIER, 137, 139),
			semanticNode(METHOD_INVOCATION, 137, 146),
			semanticNode(FIELD_ACCESS, 137, 144),
			semanticNode(CATCH_CLAUSE, 154, 195),
			semanticNode(QUALIFIED_NAME, 161, 170),
			semanticNode(PARAMETER_DECLARATION, 161, 172, "e"),
			semanticNode(BLOCK, 176, 195),
			semanticNode(LINE_COMMENT, 181, 191));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of annotation before var keyword for type inference.
	 * Tests annotation handling with Java 10+ local variable type inference.
	 */
	@Test
	public void shouldParseVarWithAnnotation()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void method()
				{
					@SuppressWarnings("unchecked") var x = java.util.List.of(1, 2, 3);
				}
			}
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 119),
			semanticNode(CLASS_DECLARATION, 7, 118, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 116),
			semanticNode(BLOCK, 43, 116),
			semanticNode(ANNOTATION, 47, 77),
			semanticNode(QUALIFIED_NAME, 48, 64),
			semanticNode(STRING_LITERAL, 65, 76),
			semanticNode(METHOD_INVOCATION, 86, 112),
			semanticNode(IDENTIFIER, 86, 90),
			semanticNode(FIELD_ACCESS, 86, 100),
			semanticNode(FIELD_ACCESS, 86, 95),
			semanticNode(FIELD_ACCESS, 86, 103),
			semanticNode(INTEGER_LITERAL, 104, 105),
			semanticNode(INTEGER_LITERAL, 107, 108),
			semanticNode(INTEGER_LITERAL, 110, 111));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of annotation in regular for-loop initializer.
	 * Tests that parseExpressionOrVariableStatement() recognizes annotations in for-loop context.
	 */
	@Test
	public void shouldParseRegularForLoopWithAnnotatedInitializer()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
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
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 143),
			semanticNode(CLASS_DECLARATION, 7, 142, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 140),
			semanticNode(BLOCK, 43, 140),
			semanticNode(FOR_STATEMENT, 47, 137),
			semanticNode(ANNOTATION, 52, 79),
			semanticNode(QUALIFIED_NAME, 53, 69),
			semanticNode(STRING_LITERAL, 70, 78),
			semanticNode(INTEGER_LITERAL, 88, 89),
			semanticNode(IDENTIFIER, 91, 92),
			semanticNode(BINARY_EXPRESSION, 91, 97),
			semanticNode(INTEGER_LITERAL, 95, 97),
			semanticNode(UNARY_EXPRESSION, 99, 102),
			semanticNode(IDENTIFIER, 101, 102),
			semanticNode(BLOCK, 106, 137),
			semanticNode(FIELD_ACCESS, 111, 129),
			semanticNode(IDENTIFIER, 111, 117),
			semanticNode(METHOD_INVOCATION, 111, 132),
			semanticNode(QUALIFIED_NAME, 111, 129),
			semanticNode(FIELD_ACCESS, 111, 121),
			semanticNode(IDENTIFIER, 130, 131));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of annotation with named argument in local variable declaration.
	 * Tests annotation element-value pair syntax.
	 */
	@Test
	public void shouldParseAnnotationWithArguments()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void method()
				{
					@SuppressWarnings(value = "unchecked") java.util.List<String> list = null;
				}
			}
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 127),
			semanticNode(CLASS_DECLARATION, 7, 126, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 124),
			semanticNode(BLOCK, 43, 124),
			semanticNode(ANNOTATION, 47, 85),
			semanticNode(QUALIFIED_NAME, 48, 64),
			semanticNode(IDENTIFIER, 65, 70),
			semanticNode(ASSIGNMENT_EXPRESSION, 65, 84),
			semanticNode(STRING_LITERAL, 73, 84),
			semanticNode(PARAMETERIZED_TYPE, 86, 108),
			semanticNode(QUALIFIED_NAME, 86, 100),
			semanticNode(QUALIFIED_NAME, 101, 107),
			semanticNode(NULL_LITERAL, 116, 120));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of annotation with array argument in local variable declaration.
	 * Tests annotation array initializer syntax.
	 */
	@Test
	public void shouldParseAnnotationWithMultipleArguments()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void method()
				{
					@SuppressWarnings({"unchecked", "rawtypes"}) java.util.List list = null;
				}
			}
			""");
		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 125),
			semanticNode(CLASS_DECLARATION, 7, 124, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 122),
			semanticNode(BLOCK, 43, 122),
			semanticNode(ANNOTATION, 47, 91),
			semanticNode(QUALIFIED_NAME, 48, 64),
			semanticNode(ARRAY_INITIALIZER, 65, 90),
			semanticNode(STRING_LITERAL, 66, 77),
			semanticNode(STRING_LITERAL, 79, 89),
			semanticNode(QUALIFIED_NAME, 92, 106),
			semanticNode(NULL_LITERAL, 114, 118));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
