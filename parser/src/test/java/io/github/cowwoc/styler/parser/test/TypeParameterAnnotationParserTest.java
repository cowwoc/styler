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
 * Tests for parsing JSR 308 type annotations on type parameter names.
 * <p>
 * These tests verify that annotations can appear before type parameter identifiers
 * in generic declarations, such as {@code class Box<@Nullable T>}.
 */
public class TypeParameterAnnotationParserTest
{
	/**
	 * Tests parsing of a simple annotated type parameter.
	 * The {@code @Nullable} annotation appears before the type parameter name {@code T}.
	 */
	@Test
	public void testSimpleAnnotatedTypeParameter()
	{
		String source = """
			class Box<@Nullable T>
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 11, 19);
			expected.allocateNode(NodeType.ANNOTATION, 10, 19);
			expected.allocateClassDeclaration(0, 26, new TypeDeclarationAttribute("Box"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 27);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of an annotated type parameter with a bound.
	 * The {@code @NonNull} annotation is on the type parameter name, with {@code Object} as the bound.
	 */
	@Test
	public void testAnnotatedTypeParameterWithBound()
	{
		String source = """
			class Container<@NonNull T extends Object>
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 17, 24);
			expected.allocateNode(NodeType.ANNOTATION, 16, 24);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 35, 41);
			expected.allocateClassDeclaration(0, 46, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 47);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing where both the type parameter and its bound are annotated.
	 * The parameter has {@code @Ann1} and the bound has {@code @Ann2}.
	 */
	@Test
	public void testBothParameterAndBoundAnnotated()
	{
		String source = """
			class Wrapper<@Ann1 T extends @Ann2 Object>
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 15, 19);
			expected.allocateNode(NodeType.ANNOTATION, 14, 19);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 35);
			expected.allocateNode(NodeType.ANNOTATION, 30, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 36, 42);
			expected.allocateClassDeclaration(0, 47, new TypeDeclarationAttribute("Wrapper"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 48);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of multiple annotated type parameters.
	 * Both {@code K} and {@code V} have their own annotations.
	 */
	@Test
	public void testMultipleAnnotatedTypeParameters()
	{
		String source = """
			class Pair<@NonNull K, @Nullable V>
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 12, 19);
			expected.allocateNode(NodeType.ANNOTATION, 11, 19);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 24, 32);
			expected.allocateNode(NodeType.ANNOTATION, 23, 32);
			expected.allocateClassDeclaration(0, 39, new TypeDeclarationAttribute("Pair"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 40);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of an annotated type parameter on a method declaration.
	 * The type parameter is declared at the method level with {@code @NonNull}.
	 */
	@Test
	public void testAnnotatedTypeParameterOnMethod()
	{
		String source = """
			class Util
			{
				<@NonNull T> T identity(T value)
				{
					return value;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 16, 23);
			expected.allocateNode(NodeType.ANNOTATION, 15, 23);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 39);
			expected.allocateParameterDeclaration(38, 45, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 59, 64);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 52, 65);
			expected.allocateNode(NodeType.BLOCK, 48, 68);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 68);
			expected.allocateClassDeclaration(0, 70, new TypeDeclarationAttribute("Util"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 71);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of the Spring Boot Accessor pattern with nullable type parameter and bound.
	 * This pattern appears in Spring Data's property accessors.
	 */
	@Test
	public void testSpringBootAccessorPattern()
	{
		String source = """
			interface Accessor<S, @Nullable P extends @Nullable Object>
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 31);
			expected.allocateNode(NodeType.ANNOTATION, 22, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 51);
			expected.allocateNode(NodeType.ANNOTATION, 42, 51);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 52, 58);
			expected.allocateInterfaceDeclaration(0, 63, new TypeDeclarationAttribute("Accessor"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 64);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Regression test: verifies that unannotated type parameters continue to parse correctly.
	 * Ensures the annotation parsing changes do not break basic generics.
	 */
	@Test
	public void testUnannotatedTypeParameterStillWorks()
	{
		String source = """
			class Simple<T>
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(0, 19, new TypeDeclarationAttribute("Simple"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 20);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of multiple annotations on a single type parameter.
	 * Both {@code @Immutable} and {@code @NonNull} are applied to {@code T}.
	 */
	@Test
	public void testMultipleAnnotationsOnTypeParameter()
	{
		String source = """
			class Holder<@Immutable @NonNull T>
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 14, 23);
			expected.allocateNode(NodeType.ANNOTATION, 13, 23);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 32);
			expected.allocateNode(NodeType.ANNOTATION, 24, 32);
			expected.allocateClassDeclaration(0, 39, new TypeDeclarationAttribute("Holder"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 40);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
