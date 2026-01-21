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
 * Tests for parsing type-use annotations on wildcard type arguments (JSR 308).
 * Validates that the parser correctly handles patterns like {@code <@Nullable ?>}
 * and {@code <@NonNull ? super T>}.
 */
public class WildcardTypeAnnotationParserTest
{
	/**
	 * Tests parsing of a simple annotated unbounded wildcard.
	 * The {@code @Nullable} annotation appears directly before the {@code ?} wildcard.
	 */
	@Test
	public void testAnnotatedUnboundedWildcard()
	{
		String source = """
			class Test
			{
				Supplier<@Nullable ?> supplier;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 24, 32);
			expected.allocateNode(NodeType.ANNOTATION, 23, 32);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 23, 34);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 45);
			expected.allocateClassDeclaration(0, 47, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 48);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of an annotated upper-bounded wildcard.
	 * The {@code @Nullable} annotation appears before {@code ? extends T}.
	 */
	@Test
	public void testAnnotatedUpperBoundedWildcard()
	{
		String source = """
			class Test
			{
				List<@Nullable ? extends Number> numbers;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 20, 28);
			expected.allocateNode(NodeType.ANNOTATION, 19, 28);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 45);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 19, 45);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of an annotated lower-bounded wildcard.
	 * The {@code @Nullable} annotation appears before {@code ? super T}.
	 */
	@Test
	public void testAnnotatedLowerBoundedWildcard()
	{
		String source = """
			class Test
			{
				void process(Consumer<@Nullable ? super T> consumer)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 45);
			expected.allocateNode(NodeType.ANNOTATION, 36, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 55);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 36, 55);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 27, 56);
			expected.allocateParameterDeclaration(27, 65, new ParameterAttribute("consumer", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 68, 72);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 72);
			expected.allocateClassDeclaration(0, 74, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 75);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of multiple annotations on a wildcard.
	 * Both {@code @A} and {@code @B} are applied to the wildcard.
	 */
	@Test
	public void testMultipleAnnotationsOnWildcard()
	{
		String source = """
			class Test
			{
				List<@A @B ?> items;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 20, 21);
			expected.allocateNode(NodeType.ANNOTATION, 19, 21);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 24);
			expected.allocateNode(NodeType.ANNOTATION, 22, 24);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 19, 26);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 34);
			expected.allocateClassDeclaration(0, 36, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 37);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Regression test: verifies that unannotated wildcards continue to parse correctly.
	 * This ensures the annotation parsing changes do not break basic wildcard syntax.
	 */
	@Test
	public void testUnannotatedWildcardStillWorks()
	{
		String source = """
			class Test
			{
				List<?> items;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.WILDCARD_TYPE, 19, 20);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 28);
			expected.allocateClassDeclaration(0, 30, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 31);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
