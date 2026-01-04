package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.ImportAttribute;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates in post-order: imports, class, compilation unit
			expected.allocateImportDeclaration(0, 22, new ImportAttribute("java.util.List", false));
			expected.allocateClassDeclaration(23, 36, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 37);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 32, new ImportAttribute("java.lang.Math.PI", true));
			expected.allocateClassDeclaration(33, 46, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 47);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 19, new ImportAttribute("java.util.*", false));
			expected.allocateClassDeclaration(20, 33, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 34);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 31, new ImportAttribute("java.lang.Math.*", true));
			expected.allocateClassDeclaration(32, 45, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 46);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 45,
				new ImportAttribute("com.example.app.internal.utils.Helper", false));
			expected.allocateClassDeclaration(46, 59, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 22, new ImportAttribute("java.util.List", false));
			expected.allocateImportDeclaration(23, 44, new ImportAttribute("java.util.Map", false));
			expected.allocateImportDeclaration(45, 66, new ImportAttribute("java.util.Set", false));
			expected.allocateClassDeclaration(67, 80, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 80);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
