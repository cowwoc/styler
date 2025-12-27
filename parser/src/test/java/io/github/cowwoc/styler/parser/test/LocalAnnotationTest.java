package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;

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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
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
		requireThat(actual, "actual").isNotEmpty();
	}
}
