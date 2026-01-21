package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing JSR 308 type-use annotations after the dot in qualified type names.
 * <p>
 * JSR 308 allows type-use annotations at any type-use position, including after the dot in qualified types
 * such as {@code java.security.@Nullable Principal} or {@code Outer.@NonNull Inner}.
 */
public class QualifiedTypeAnnotationParserTest
{
	/**
	 * Tests parsing a package-qualified type with annotation before the simple type name.
	 * Pattern: java.security.@Nullable Principal
	 */
	@Test
	public void shouldParsePackageQualifiedTypeWithAnnotation()
	{
		String source = """
			class Test
			{
				java.security.@Nullable Principal field;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 37);
			expected.allocateNode(NodeType.ANNOTATION, 28, 37);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 54);
			expected.allocateClassDeclaration(0, 56, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 57);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing a nested/inner type with annotation before the inner type name.
	 * Pattern: Outer.@Nullable Inner
	 */
	@Test
	public void shouldParseNestedTypeWithAnnotation()
	{
		String source = """
			class Test
			{
				Outer.@Nullable Inner field;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 21, 29);
			expected.allocateNode(NodeType.ANNOTATION, 20, 29);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 42);
			expected.allocateClassDeclaration(0, 44, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 45);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing a qualified type with multiple annotations before the final type name.
	 * Pattern: pkg.@A @B Type
	 */
	@Test
	public void shouldParseMultipleAnnotationsAfterDot()
	{
		String source = """
			class Test
			{
				pkg.@A @B Type field;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 19, 20);
			expected.allocateNode(NodeType.ANNOTATION, 18, 20);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 23);
			expected.allocateNode(NodeType.ANNOTATION, 21, 23);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 35);
			expected.allocateClassDeclaration(0, 37, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 38);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Regression test: verifies that unqualified types without annotations still parse correctly.
	 */
	@Test
	public void shouldParseUnqualifiedTypeUnaffected()
	{
		String source = """
			class Test
			{
				String field;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 27);
			expected.allocateClassDeclaration(0, 29, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 30);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Regression test: verifies that qualified types without annotations still parse correctly.
	 */
	@Test
	public void shouldParseQualifiedTypeWithoutAnnotationUnaffected()
	{
		String source = """
			class Test
			{
				java.lang.String field;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 37);
			expected.allocateClassDeclaration(0, 39, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 40);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
