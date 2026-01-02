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
			compilationUnit( 0, 121),
			typeDeclaration(CLASS_DECLARATION, 7, 120, "Test"),
			methodDeclaration( 21, 118),
			block( 43, 118),
			annotation( 47, 77),
			qualifiedName( 48, 64),
			stringLiteral( 65, 76),
			parameterizedType( 78, 100),
			qualifiedName( 78, 92),
			qualifiedName( 93, 99),
			nullLiteral( 110, 114));
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
			compilationUnit( 0, 69),
			typeDeclaration(CLASS_DECLARATION, 7, 68, "Test"),
			methodDeclaration( 21, 66),
			block( 43, 66),
			integerLiteral( 61, 62));
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
			compilationUnit( 0, 83),
			typeDeclaration(CLASS_DECLARATION, 7, 82, "Test"),
			methodDeclaration( 21, 80),
			block( 43, 80),
			annotation( 47, 56),
			qualifiedName( 48, 56),
			qualifiedName( 57, 63),
			nullLiteral( 72, 76));
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
			compilationUnit( 0, 94),
			typeDeclaration(CLASS_DECLARATION, 7, 93, "Test"),
			methodDeclaration( 21, 91),
			block( 43, 91),
			annotation( 53, 61),
			qualifiedName( 54, 61),
			qualifiedName( 62, 68),
			objectCreation( 75, 87),
			qualifiedName( 79, 85));
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
			compilationUnit( 0, 100),
			typeDeclaration(CLASS_DECLARATION, 7, 99, "Test"),
			methodDeclaration( 21, 97),
			block( 43, 97),
			annotation( 47, 55),
			qualifiedName( 48, 55),
			annotation( 56, 83),
			qualifiedName( 57, 73),
			stringLiteral( 74, 82),
			integerLiteral( 92, 93));
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
			compilationUnit( 0, 110),
			typeDeclaration(CLASS_DECLARATION, 7, 109, "Test"),
			methodDeclaration( 21, 107),
			block( 43, 107),
			annotation( 47, 74),
			qualifiedName( 48, 64),
			stringLiteral( 65, 73),
			annotation( 81, 89),
			qualifiedName( 82, 89),
			qualifiedName( 90, 96),
			stringLiteral( 101, 103));
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
			compilationUnit( 0, 155),
			typeDeclaration(CLASS_DECLARATION, 7, 154, "Test"),
			methodDeclaration( 21, 152),
			block( 43, 152),
			parameterizedType( 47, 69),
			qualifiedName( 47, 61),
			qualifiedName( 62, 68),
			nullLiteral( 77, 81),
			enhancedForStatement( 85, 149),
			annotation( 90, 98),
			qualifiedName( 91, 98),
			qualifiedName( 99, 105),
			identifier( 110, 114),
			block( 118, 149),
			identifier( 123, 129),
			methodInvocation( 123, 144),
			fieldAccess( 123, 133),
			fieldAccess( 123, 141),
			qualifiedName( 123, 141),
			identifier( 142, 143));
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
			compilationUnit( 0, 161),
			typeDeclaration(CLASS_DECLARATION, 7, 160, "Test"),
			methodDeclaration( 21, 158),
			block( 43, 158),
			parameterizedType( 47, 69),
			qualifiedName( 47, 61),
			qualifiedName( 62, 68),
			nullLiteral( 77, 81),
			enhancedForStatement( 85, 155),
			annotation( 96, 104),
			qualifiedName( 97, 104),
			qualifiedName( 105, 111),
			identifier( 116, 120),
			block( 124, 155),
			identifier( 129, 135),
			methodInvocation( 129, 150),
			fieldAccess( 129, 139),
			fieldAccess( 129, 147),
			qualifiedName( 129, 147),
			identifier( 148, 149));
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
			compilationUnit( 0, 192),
			typeDeclaration(CLASS_DECLARATION, 7, 191, "Test"),
			methodDeclaration( 21, 189),
			block( 43, 189),
			tryStatement( 47, 186),
			annotation( 52, 81),
			qualifiedName( 53, 69),
			stringLiteral( 70, 80),
			qualifiedName( 82, 101),
			nullLiteral( 107, 111),
			block( 115, 142),
			identifier( 128, 130),
			methodInvocation( 128, 137),
			fieldAccess( 128, 135),
			catchClause( 145, 186),
			qualifiedName( 152, 161),
			parameterNode( 152, 163, "e"),
			block( 167, 186),
			lineComment( 172, 182));
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
			compilationUnit( 0, 177),
			typeDeclaration(CLASS_DECLARATION, 7, 176, "Test"),
			methodDeclaration( 21, 174),
			block( 43, 174),
			tryStatement( 47, 171),
			annotation( 58, 66),
			qualifiedName( 59, 66),
			qualifiedName( 67, 86),
			nullLiteral( 92, 96),
			block( 100, 127),
			fieldAccess( 113, 120),
			identifier( 113, 115),
			methodInvocation( 113, 122),
			catchClause( 130, 171),
			qualifiedName( 137, 146),
			parameterNode( 137, 148, "e"),
			block( 152, 171),
			lineComment( 157, 167));
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
			compilationUnit( 0, 201),
			typeDeclaration(CLASS_DECLARATION, 7, 200, "Test"),
			methodDeclaration( 21, 198),
			block( 43, 198),
			tryStatement( 47, 195),
			annotation( 52, 81),
			qualifiedName( 53, 69),
			stringLiteral( 70, 80),
			annotation( 82, 90),
			qualifiedName( 83, 90),
			qualifiedName( 91, 110),
			nullLiteral( 116, 120),
			block( 124, 151),
			identifier( 137, 139),
			methodInvocation( 137, 146),
			fieldAccess( 137, 144),
			catchClause( 154, 195),
			qualifiedName( 161, 170),
			parameterNode( 161, 172, "e"),
			block( 176, 195),
			lineComment( 181, 191));
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
			compilationUnit( 0, 119),
			typeDeclaration(CLASS_DECLARATION, 7, 118, "Test"),
			methodDeclaration( 21, 116),
			block( 43, 116),
			annotation( 47, 77),
			qualifiedName( 48, 64),
			stringLiteral( 65, 76),
			methodInvocation( 86, 112),
			identifier( 86, 90),
			fieldAccess( 86, 100),
			fieldAccess( 86, 95),
			fieldAccess( 86, 103),
			integerLiteral( 104, 105),
			integerLiteral( 107, 108),
			integerLiteral( 110, 111));
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
			compilationUnit( 0, 143),
			typeDeclaration(CLASS_DECLARATION, 7, 142, "Test"),
			methodDeclaration( 21, 140),
			block( 43, 140),
			forStatement( 47, 137),
			annotation( 52, 79),
			qualifiedName( 53, 69),
			stringLiteral( 70, 78),
			integerLiteral( 88, 89),
			identifier( 91, 92),
			binaryExpression( 91, 97),
			integerLiteral( 95, 97),
			unaryExpression( 99, 102),
			identifier( 101, 102),
			block( 106, 137),
			fieldAccess( 111, 129),
			identifier( 111, 117),
			methodInvocation( 111, 132),
			qualifiedName( 111, 129),
			fieldAccess( 111, 121),
			identifier( 130, 131));
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
			compilationUnit( 0, 127),
			typeDeclaration(CLASS_DECLARATION, 7, 126, "Test"),
			methodDeclaration( 21, 124),
			block( 43, 124),
			annotation( 47, 85),
			qualifiedName( 48, 64),
			identifier( 65, 70),
			assignmentExpression( 65, 84),
			stringLiteral( 73, 84),
			parameterizedType( 86, 108),
			qualifiedName( 86, 100),
			qualifiedName( 101, 107),
			nullLiteral( 116, 120));
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
			compilationUnit( 0, 125),
			typeDeclaration(CLASS_DECLARATION, 7, 124, "Test"),
			methodDeclaration( 21, 122),
			block( 43, 122),
			annotation( 47, 91),
			qualifiedName( 48, 64),
			arrayInitializer( 65, 90),
			stringLiteral( 66, 77),
			stringLiteral( 79, 89),
			qualifiedName( 92, 106),
			nullLiteral( 114, 118));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
