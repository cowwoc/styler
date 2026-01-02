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
 * Tests for parsing type annotations on type parameter bounds and wildcard bounds.
 * <p>
 * The parser consumes type annotations but does not create explicit AST nodes for them.
 * Instead, the annotation positions are incorporated into the containing type's span.
 * These tests verify that the parser correctly handles type annotations without failing.
 */
public class TypeAnnotationBoundsParserTest
{
	/**
	 * Tests parsing of a simple type annotation on a type parameter bound.
	 * The {@code @Nullable} annotation appears directly before the bound type {@code Object}.
	 */
	@Test
	public void testSimpleAnnotatedBound()
	{
		String source = """
			class Container<V extends @Nullable Object>
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 48),
			typeDeclaration(CLASS_DECLARATION, 0, 47, "Container"),
			annotation( 26, 35),
			qualifiedName( 27, 35),
			qualifiedName( 36, 42));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of a type annotation on a parameterized type bound.
	 * The {@code @NonNull} annotation is applied to {@code Comparable<T>}, which is a
	 * generic type with its own type argument.
	 */
	@Test
	public void testAnnotatedGenericBound()
	{
		String source = """
			class Sorter<T extends @NonNull Comparable<T>>
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 51),
			typeDeclaration(CLASS_DECLARATION, 0, 50, "Sorter"),
			parameterizedType( 32, 46),
			annotation( 23, 31),
			qualifiedName( 24, 31),
			qualifiedName( 32, 42),
			qualifiedName( 43, 44));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of multiple type annotations on a single bound.
	 * Both {@code @Immutable} and {@code @NonNull} are applied to the {@code List<?>} bound.
	 */
	@Test
	public void testMultipleAnnotationsOnBound()
	{
		String source = """
			class Holder<T extends @Immutable @NonNull List<?>>
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 56),
			typeDeclaration(CLASS_DECLARATION, 0, 55, "Holder"),
			parameterizedType( 43, 51),
			annotation( 23, 33),
			qualifiedName( 24, 33),
			annotation( 34, 42),
			qualifiedName( 35, 42),
			qualifiedName( 43, 47),
			wildcardType( 48, 49));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of an upper-bounded wildcard with a type annotation.
	 * The {@code @NonNull} annotation is applied to {@code Number} in a wildcard bound.
	 */
	@Test
	public void testUpperBoundedWildcardWithAnnotation()
	{
		String source = """
			class Wrapper
			{
				List<? extends @NonNull Number> numbers;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 60),
			typeDeclaration(CLASS_DECLARATION, 0, 59, "Wrapper"),
			fieldDeclaration( 17, 57),
			wildcardType( 22, 47),
			annotation( 32, 40),
			qualifiedName( 33, 40),
			qualifiedName( 41, 47));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of a lower-bounded wildcard with a type annotation.
	 * The {@code @NonNull} annotation is applied to {@code Integer} in a super bound.
	 */
	@Test
	public void testLowerBoundedWildcardWithAnnotation()
	{
		String source = """
			class Sink
			{
				Consumer<? super @NonNull Integer> consumer;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 61),
			typeDeclaration(CLASS_DECLARATION, 0, 60, "Sink"),
			fieldDeclaration( 14, 58),
			wildcardType( 23, 47),
			annotation( 31, 39),
			qualifiedName( 32, 39),
			qualifiedName( 40, 47));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of intersection types with annotations on each bound.
	 * Both {@code Serializable} and {@code Cloneable} have their own type annotations.
	 */
	@Test
	public void testIntersectionTypeWithAnnotations()
	{
		String source = """
			class Copier<T extends @NonNull Serializable & @Immutable Cloneable>
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 73),
			typeDeclaration(CLASS_DECLARATION, 0, 72, "Copier"),
			annotation( 23, 31),
			qualifiedName( 24, 31),
			qualifiedName( 32, 44),
			annotation( 47, 57),
			qualifiedName( 48, 57),
			qualifiedName( 58, 67));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of a type annotation on a field type.
	 * The {@code @NonNull} annotation is applied to the type itself, not as a declaration annotation.
	 */
	@Test
	public void testAnnotatedFieldType()
	{
		String source = """
			class Person
			{
				@NonNull String name;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 40),
			typeDeclaration(CLASS_DECLARATION, 0, 39, "Person"),
			fieldDeclaration( 16, 37),
			annotation( 16, 24),
			qualifiedName( 17, 24));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of a qualified annotation name on a type bound.
	 * Uses fully qualified annotation name {@code @javax.annotation.Nullable}.
	 */
	@Test
	public void testQualifiedAnnotationOnBound()
	{
		String source = """
			class Holder<T extends @javax.annotation.Nullable Object>
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 62),
			typeDeclaration(CLASS_DECLARATION, 0, 61, "Holder"),
			annotation( 23, 49),
			qualifiedName( 24, 49),
			qualifiedName( 50, 56));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of an annotation with parameters on a type bound.
	 * The {@code @Size(min = 1)} annotation has an element-value pair.
	 */
	@Test
	public void testAnnotationWithParametersOnBound()
	{
		String source = """
			class Holder
			{
				@Size(min = 1) List<?> items;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 48),
			typeDeclaration(CLASS_DECLARATION, 0, 47, "Holder"),
			fieldDeclaration( 16, 45),
			annotation( 16, 30),
			qualifiedName( 17, 21),
			identifier( 22, 25),
			assignmentExpression( 22, 29),
			integerLiteral( 28, 29),
			wildcardType( 36, 37));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of a nested generic type with an annotated type argument.
	 * The first type parameter {@code K} has the {@code @NonNull} annotation.
	 */
	@Test
	public void testNestedGenericWithAnnotatedBound()
	{
		String source = """
			class Cache
			{
				Map<@NonNull K, V> map;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 41),
			typeDeclaration(CLASS_DECLARATION, 0, 40, "Cache"),
			fieldDeclaration( 15, 38),
			qualifiedName( 19, 29),
			annotation( 19, 27),
			qualifiedName( 20, 27),
			qualifiedName( 28, 29),
			qualifiedName( 31, 32));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of annotated type parameter bounds on a method declaration.
	 * The type parameter is declared at the method level, not the class level.
	 */
	@Test
	public void testAnnotatedBoundOnMethod()
	{
		String source = """
			class Util
			{
				<T extends @NonNull Comparable<T>> T max(T a, T b)
				{
					return a;
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 85),
			typeDeclaration(CLASS_DECLARATION, 0, 84, "Util"),
			methodDeclaration( 14, 82),
			parameterizedType( 34, 48),
			annotation( 25, 33),
			qualifiedName( 26, 33),
			qualifiedName( 34, 44),
			qualifiedName( 45, 46),
			parameterNode( 55, 58, "a"),
			qualifiedName( 55, 56),
			parameterNode( 60, 63, "b"),
			qualifiedName( 60, 61),
			block( 66, 82),
			returnStatement( 70, 79),
			identifier( 77, 78));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Regression test: verifies that unannotated type parameter bounds continue to parse correctly.
	 * Ensures the annotation parsing changes do not break basic generics.
	 */
	@Test
	public void testUnannotatedBoundStillWorks()
	{
		String source = """
			class Sorter<T extends Comparable<T>>
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 42),
			typeDeclaration(CLASS_DECLARATION, 0, 41, "Sorter"),
			parameterizedType( 23, 37),
			qualifiedName( 23, 33),
			qualifiedName( 34, 35));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Regression test: verifies that regular annotations on class declarations are not affected.
	 * The {@code @Deprecated} annotation is a declaration annotation, not a type annotation.
	 */
	@Test
	public void testRegularAnnotationOnClassNotAffected()
	{
		String source = """
			@Deprecated
			class OldClass
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 31),
			annotation( 0, 11),
			qualifiedName( 1, 11),
			typeDeclaration(CLASS_DECLARATION, 12, 30, "OldClass"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Regression test: verifies that unbounded wildcards continue to parse correctly.
	 * Ensures the annotation parsing changes do not break simple wildcard types.
	 */
	@Test
	public void testUnboundedWildcardUnaffected()
	{
		String source = """
			class Holder
			{
				List<?> items;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 33),
			typeDeclaration(CLASS_DECLARATION, 0, 32, "Holder"),
			fieldDeclaration( 16, 30),
			wildcardType( 21, 22));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Regression test: verifies that diamond operator continues to parse correctly.
	 * Ensures the annotation parsing changes do not break diamond operator inference.
	 */
	@Test
	public void testDiamondOperatorUnaffected()
	{
		String source = """
			class Factory
			{
				void create()
				{
					List<String> list = new ArrayList<>();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 80),
			typeDeclaration(CLASS_DECLARATION, 0, 79, "Factory"),
			methodDeclaration( 17, 77),
			block( 32, 77),
			parameterizedType( 36, 48),
			parameterizedType( 60, 71),
			qualifiedName( 36, 40),
			qualifiedName( 41, 47),
			objectCreation( 56, 73),
			qualifiedName( 60, 69));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
