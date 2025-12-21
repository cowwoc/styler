package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.ImportAttribute;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for import declaration attribute population during parsing.
 * <p>
 * Verifies that the parser correctly populates the {@code qualifiedName} attribute
 * for both regular and static import declarations.
 * <p>
 * <b>Thread-safety</b>: Thread-safe - all instances are created inside {@code @Test} methods.
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> imports = findNodesOfType(arena, NodeType.IMPORT_DECLARATION);
			requireThat(imports.size(), "imports.size()").isEqualTo(1);

			ImportAttribute attribute = arena.getImportAttribute(imports.get(0));
			requireThat(attribute.qualifiedName(), "qualifiedName").isEqualTo("java.util.List");
		}
	}

	/**
	 * Verifies that a static import has its qualified name attribute populated.
	 */
	@Test
	public void shouldPopulateQualifiedNameForStaticImport()
	{
		String source = """
			import static java.lang.Math.PI;
			class Test {}
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> imports = findNodesOfType(arena, NodeType.STATIC_IMPORT_DECLARATION);
			requireThat(imports.size(), "imports.size()").isEqualTo(1);

			ImportAttribute attribute = arena.getImportAttribute(imports.get(0));
			requireThat(attribute.qualifiedName(), "qualifiedName").isEqualTo("java.lang.Math.PI");
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> imports = findNodesOfType(arena, NodeType.IMPORT_DECLARATION);
			requireThat(imports.size(), "imports.size()").isEqualTo(1);

			ImportAttribute attribute = arena.getImportAttribute(imports.get(0));
			requireThat(attribute.qualifiedName(), "qualifiedName").isEqualTo("java.util.*");
		}
	}

	/**
	 * Verifies that a static wildcard import has its qualified name attribute populated.
	 */
	@Test
	public void shouldPopulateQualifiedNameForStaticWildcardImport()
	{
		String source = """
			import static java.lang.Math.*;
			class Test {}
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> imports = findNodesOfType(arena, NodeType.STATIC_IMPORT_DECLARATION);
			requireThat(imports.size(), "imports.size()").isEqualTo(1);

			ImportAttribute attribute = arena.getImportAttribute(imports.get(0));
			requireThat(attribute.qualifiedName(), "qualifiedName").isEqualTo("java.lang.Math.*");
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> imports = findNodesOfType(arena, NodeType.IMPORT_DECLARATION);
			requireThat(imports.size(), "imports.size()").isEqualTo(1);

			ImportAttribute attribute = arena.getImportAttribute(imports.get(0));
			requireThat(attribute.qualifiedName(), "qualifiedName").
				isEqualTo("com.example.app.internal.utils.Helper");
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
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			List<NodeIndex> imports = findNodesOfType(arena, NodeType.IMPORT_DECLARATION);
			requireThat(imports.size(), "imports.size()").isEqualTo(3);

			// Imports are found in source order
			ImportAttribute attr1 = arena.getImportAttribute(imports.get(0));
			ImportAttribute attr2 = arena.getImportAttribute(imports.get(1));
			ImportAttribute attr3 = arena.getImportAttribute(imports.get(2));

			requireThat(attr1.qualifiedName(), "first.qualifiedName").isEqualTo("java.util.List");
			requireThat(attr2.qualifiedName(), "second.qualifiedName").isEqualTo("java.util.Map");
			requireThat(attr3.qualifiedName(), "third.qualifiedName").isEqualTo("java.util.Set");
		}
	}

	/**
	 * Finds all nodes of the specified type in the arena.
	 *
	 * @param arena the arena to search
	 * @param type  the node type to find
	 * @return list of node indices matching the type
	 */
	private List<NodeIndex> findNodesOfType(NodeArena arena, NodeType type)
	{
		List<NodeIndex> result = new ArrayList<>();
		for (int i = 0; i < arena.getNodeCount(); ++i)
		{
			NodeIndex index = new NodeIndex(i);
			if (arena.getType(index) == type)
			{
				result.add(index);
			}
		}
		return result;
	}
}
