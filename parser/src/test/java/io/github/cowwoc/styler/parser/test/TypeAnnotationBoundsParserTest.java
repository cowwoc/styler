package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.ASSIGNMENT_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.OBJECT_CREATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, 48),
			semanticNode(CLASS_DECLARATION, 0, 47, "Container"),
			semanticNode(QUALIFIED_NAME, 27, 35),
			semanticNode(QUALIFIED_NAME, 36, 42));
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
			semanticNode(COMPILATION_UNIT, 0, 51),
			semanticNode(CLASS_DECLARATION, 0, 50, "Sorter"),
			semanticNode(QUALIFIED_NAME, 24, 31),
			semanticNode(QUALIFIED_NAME, 32, 42),
			semanticNode(QUALIFIED_NAME, 43, 44));
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
			semanticNode(COMPILATION_UNIT, 0, 56),
			semanticNode(CLASS_DECLARATION, 0, 55, "Holder"),
			semanticNode(QUALIFIED_NAME, 24, 33),
			semanticNode(QUALIFIED_NAME, 35, 42),
			semanticNode(QUALIFIED_NAME, 43, 47));
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
			semanticNode(COMPILATION_UNIT, 0, 60),
			semanticNode(CLASS_DECLARATION, 0, 59, "Wrapper"),
			semanticNode(FIELD_DECLARATION, 17, 57),
			semanticNode(QUALIFIED_NAME, 33, 40),
			semanticNode(QUALIFIED_NAME, 41, 47));
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
			semanticNode(COMPILATION_UNIT, 0, 61),
			semanticNode(CLASS_DECLARATION, 0, 60, "Sink"),
			semanticNode(FIELD_DECLARATION, 14, 58),
			semanticNode(QUALIFIED_NAME, 32, 39),
			semanticNode(QUALIFIED_NAME, 40, 47));
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
			semanticNode(COMPILATION_UNIT, 0, 73),
			semanticNode(CLASS_DECLARATION, 0, 72, "Copier"),
			semanticNode(QUALIFIED_NAME, 24, 31),
			semanticNode(QUALIFIED_NAME, 32, 44),
			semanticNode(QUALIFIED_NAME, 48, 57),
			semanticNode(QUALIFIED_NAME, 58, 67));
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
			semanticNode(COMPILATION_UNIT, 0, 40),
			semanticNode(CLASS_DECLARATION, 0, 39, "Person"),
			semanticNode(FIELD_DECLARATION, 16, 37),
			semanticNode(QUALIFIED_NAME, 17, 24));
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
			semanticNode(COMPILATION_UNIT, 0, 62),
			semanticNode(CLASS_DECLARATION, 0, 61, "Holder"),
			semanticNode(QUALIFIED_NAME, 24, 49),
			semanticNode(QUALIFIED_NAME, 50, 56));
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
			semanticNode(COMPILATION_UNIT, 0, 48),
			semanticNode(CLASS_DECLARATION, 0, 47, "Holder"),
			semanticNode(FIELD_DECLARATION, 16, 45),
			semanticNode(QUALIFIED_NAME, 17, 21),
			semanticNode(IDENTIFIER, 22, 25),
			semanticNode(ASSIGNMENT_EXPRESSION, 22, 29),
			semanticNode(INTEGER_LITERAL, 28, 29));
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
			semanticNode(COMPILATION_UNIT, 0, 41),
			semanticNode(CLASS_DECLARATION, 0, 40, "Cache"),
			semanticNode(FIELD_DECLARATION, 15, 38),
			semanticNode(QUALIFIED_NAME, 20, 27),
			semanticNode(QUALIFIED_NAME, 28, 29),
			semanticNode(QUALIFIED_NAME, 31, 32));
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
			semanticNode(COMPILATION_UNIT, 0, 85),
			semanticNode(CLASS_DECLARATION, 0, 84, "Util"),
			semanticNode(METHOD_DECLARATION, 14, 82),
			semanticNode(QUALIFIED_NAME, 26, 33),
			semanticNode(QUALIFIED_NAME, 34, 44),
			semanticNode(QUALIFIED_NAME, 45, 46),
			semanticNode(QUALIFIED_NAME, 55, 56),
			semanticNode(QUALIFIED_NAME, 60, 61),
			semanticNode(BLOCK, 66, 82),
			semanticNode(RETURN_STATEMENT, 70, 79),
			semanticNode(IDENTIFIER, 77, 78));
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
			semanticNode(COMPILATION_UNIT, 0, 42),
			semanticNode(CLASS_DECLARATION, 0, 41, "Sorter"),
			semanticNode(QUALIFIED_NAME, 23, 33),
			semanticNode(QUALIFIED_NAME, 34, 35));
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
			semanticNode(COMPILATION_UNIT, 0, 31),
			semanticNode(QUALIFIED_NAME, 1, 11),
			semanticNode(CLASS_DECLARATION, 12, 30, "OldClass"));
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
			semanticNode(COMPILATION_UNIT, 0, 33),
			semanticNode(CLASS_DECLARATION, 0, 32, "Holder"),
			semanticNode(FIELD_DECLARATION, 16, 30));
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
			semanticNode(COMPILATION_UNIT, 0, 80),
			semanticNode(CLASS_DECLARATION, 0, 79, "Factory"),
			semanticNode(METHOD_DECLARATION, 17, 77),
			semanticNode(BLOCK, 32, 77),
			semanticNode(QUALIFIED_NAME, 36, 40),
			semanticNode(QUALIFIED_NAME, 41, 47),
			semanticNode(OBJECT_CREATION, 56, 73),
			semanticNode(QUALIFIED_NAME, 60, 69));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
