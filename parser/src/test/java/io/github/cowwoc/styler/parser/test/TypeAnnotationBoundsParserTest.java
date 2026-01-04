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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 35);
			expected.allocateNode(NodeType.ANNOTATION, 26, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 36, 42);
			expected.allocateClassDeclaration(0, 47, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 48);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 24, 31);
			expected.allocateNode(NodeType.ANNOTATION, 23, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 42);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 44);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 44);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 32, 46);
			expected.allocateClassDeclaration(0, 50, new TypeDeclarationAttribute("Sorter"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 51);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 24, 33);
			expected.allocateNode(NodeType.ANNOTATION, 23, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 35, 42);
			expected.allocateNode(NodeType.ANNOTATION, 34, 42);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 47);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 48, 49);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 43, 51);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Holder"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 33, 40);
			expected.allocateNode(NodeType.ANNOTATION, 32, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 22, 47);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 17, 57);
			expected.allocateClassDeclaration(0, 59, new TypeDeclarationAttribute("Wrapper"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 39);
			expected.allocateNode(NodeType.ANNOTATION, 31, 39);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 47);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 23, 47);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 58);
			expected.allocateClassDeclaration(0, 60, new TypeDeclarationAttribute("Sink"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 24, 31);
			expected.allocateNode(NodeType.ANNOTATION, 23, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 44);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 57);
			expected.allocateNode(NodeType.ANNOTATION, 47, 57);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 58, 67);
			expected.allocateClassDeclaration(0, 72, new TypeDeclarationAttribute("Copier"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 73);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 17, 24);
			expected.allocateNode(NodeType.ANNOTATION, 16, 24);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 16, 37);
			expected.allocateClassDeclaration(0, 39, new TypeDeclarationAttribute("Person"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 40);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 24, 49);
			expected.allocateNode(NodeType.ANNOTATION, 23, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 56);
			expected.allocateClassDeclaration(0, 61, new TypeDeclarationAttribute("Holder"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 17, 21);
			expected.allocateNode(NodeType.IDENTIFIER, 22, 25);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 28, 29);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 22, 29);
			expected.allocateNode(NodeType.ANNOTATION, 16, 30);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 36, 37);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 16, 45);
			expected.allocateClassDeclaration(0, 47, new TypeDeclarationAttribute("Holder"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 48);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 20, 27);
			expected.allocateNode(NodeType.ANNOTATION, 19, 27);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 29);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 19, 29);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 32);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 32);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 15, 38);
			expected.allocateClassDeclaration(0, 40, new TypeDeclarationAttribute("Cache"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 41);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 33);
			expected.allocateNode(NodeType.ANNOTATION, 25, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 34, 44);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 45, 46);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 45, 46);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 34, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 56);
			expected.allocateParameterDeclaration(55, 58, new ParameterAttribute("a", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 60, 61);
			expected.allocateParameterDeclaration(60, 63, new ParameterAttribute("b", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 77, 78);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 70, 79);
			expected.allocateNode(NodeType.BLOCK, 66, 82);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 82);
			expected.allocateClassDeclaration(0, 84, new TypeDeclarationAttribute("Util"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 85);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 34, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 34, 35);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 23, 37);
			expected.allocateClassDeclaration(0, 41, new TypeDeclarationAttribute("Sorter"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 42);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 11);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 1, 11);
			expected.allocateNode(NodeType.ANNOTATION, 0, 11);
			expected.allocateClassDeclaration(12, 30, new TypeDeclarationAttribute("OldClass"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 31);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.WILDCARD_TYPE, 21, 22);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 16, 30);
			expected.allocateClassDeclaration(0, 32, new TypeDeclarationAttribute("Holder"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 33);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 36, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 36, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 60, 69);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 60, 71);
			expected.allocateNode(NodeType.OBJECT_CREATION, 56, 73);
			expected.allocateNode(NodeType.BLOCK, 32, 77);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 17, 77);
			expected.allocateClassDeclaration(0, 79, new TypeDeclarationAttribute("Factory"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 80);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
