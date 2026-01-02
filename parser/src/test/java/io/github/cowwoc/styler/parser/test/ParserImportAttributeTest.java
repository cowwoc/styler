package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.compilationUnit;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.importNode;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;

/**
 * Tests for import declaration attribute population during parsing.
 */
public class ParserImportAttributeTest
{
	/**
	 * Verifies that a regular import has its qualified name attribute populated.
	 */
	@Test
	public void shouldPopulateQualifiedNameForRegularImport()
	{
		String source = """
			import java.util.List;
			class Test {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 37),
			importNode(0, 22, "java.util.List", false),
			typeDeclaration(CLASS_DECLARATION, 23, 36, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that a static import has its qualified name and isStatic attributes populated.
	 */
	@Test
	public void shouldPopulateQualifiedNameForStaticImport()
	{
		String source = """
			import static java.lang.Math.PI;
			class Test {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 47),
			importNode(0, 32, "java.lang.Math.PI", true),
			typeDeclaration(CLASS_DECLARATION, 33, 46, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that a wildcard import has its qualified name attribute populated with asterisk.
	 */
	@Test
	public void shouldPopulateQualifiedNameForWildcardImport()
	{
		String source = """
			import java.util.*;
			class Test {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 34),
			importNode(0, 19, "java.util.*", false),
			typeDeclaration(CLASS_DECLARATION, 20, 33, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that a static wildcard import has its qualified name and isStatic attributes populated.
	 */
	@Test
	public void shouldPopulateQualifiedNameForStaticWildcardImport()
	{
		String source = """
			import static java.lang.Math.*;
			class Test {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 46),
			importNode(0, 31, "java.lang.Math.*", true),
			typeDeclaration(CLASS_DECLARATION, 32, 45, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that a deeply nested import has its full qualified name attribute populated.
	 */
	@Test
	public void shouldPopulateQualifiedNameForDeeplyNestedImport()
	{
		String source = """
			import com.example.app.internal.utils.Helper;
			class Test {}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 60),
			importNode(0, 45, "com.example.app.internal.utils.Helper", false),
			typeDeclaration(CLASS_DECLARATION, 46, 59, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Verifies that multiple imports each have distinct qualified name attributes.
	 */
	@Test
	public void shouldHandleMultipleImportsWithDistinctAttributes()
	{
		String source = """
			import java.util.List;
			import java.util.Map;
			import java.util.Set;
			class Test {}""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit(0, 80),
			importNode(0, 22, "java.util.List", false),
			importNode(23, 44, "java.util.Map", false),
			importNode(45, 66, "java.util.Set", false),
			typeDeclaration(CLASS_DECLARATION, 67, 80, "Test"));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
