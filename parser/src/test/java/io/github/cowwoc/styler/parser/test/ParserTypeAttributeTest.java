package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.ENUM_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.INTERFACE_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.RECORD_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.compilationUnit;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.enumConstant;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;

/**
 * Tests for type declaration attribute population during parsing.
 */
public class ParserTypeAttributeTest
{
	/**
	 * Verifies that a class declaration has its type name attribute populated.
	 */
	@Test
	public void shouldPopulateTypeNameForClass()
	{
		String source = """
			class MyClass {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 17),
			typeDeclaration(CLASS_DECLARATION, 0, 16, "MyClass"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that a class declaration has its name position attribute correctly set.
	 */
	@Test
	public void shouldPopulateNamePositionForClass()
	{
		String source = """
			class MyClass {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 17),
			typeDeclaration(CLASS_DECLARATION, 0, 16, "MyClass"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that an interface declaration has its type name attribute populated.
	 */
	@Test
	public void shouldPopulateTypeNameForInterface()
	{
		String source = """
			interface MyInterface {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 25),
			typeDeclaration(INTERFACE_DECLARATION, 0, 24, "MyInterface"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that an enum declaration has its type name attribute populated.
	 */
	@Test
	public void shouldPopulateTypeNameForEnum()
	{
		String source = """
			enum MyEnum { A, B }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 21),
			typeDeclaration(ENUM_DECLARATION, 0, 20, "MyEnum"),
			enumConstant(14, 15),
			enumConstant(17, 18));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that a record declaration has its type name attribute populated.
	 */
	@Test
	public void shouldPopulateTypeNameForRecord()
	{
		String source = """
			record MyRecord(int x) {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 26),
			typeDeclaration(RECORD_DECLARATION, 0, 25, "MyRecord"),
			parameterNode(16, 21, "x"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that nested type declarations each have their own attributes.
	 */
	@Test
	public void shouldPopulateAttributesForNestedTypes()
	{
		String source = """
			class Outer { class Inner {} }
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 31),
			typeDeclaration(CLASS_DECLARATION, 0, 30, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 14, 28, "Inner"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that a generic class has only its simple name in the type name attribute.
	 */
	@Test
	public void shouldHandleTypeNameWithGenerics()
	{
		String source = """
			class Box<T> {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 16),
			typeDeclaration(CLASS_DECLARATION, 0, 15, "Box"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that a class with modifiers has its type name correctly extracted.
	 */
	@Test
	public void shouldHandleTypeWithModifiers()
	{
		String source = """
			public abstract class MyClass {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 33),
			typeDeclaration(CLASS_DECLARATION, 16, 32, "MyClass"));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
