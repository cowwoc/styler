package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Verifies that AST nodes are properly allocated for record and annotation declarations.
 * Tests that the parser allocates RECORD_DECLARATION and ANNOTATION_DECLARATION node types
 * instead of treating these declarations as void operations.
 */
public class NodeAllocationTest
{
	/**
	 * Verifies that parsing a record declaration allocates a RECORD_DECLARATION node.
	 * Tests the simplest form: record with component list and empty body.
	 */
	@Test
	public void shouldAllocateRecordDeclarationNode()
	{
		String source = """
			record Point(int x, int y) { }
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			boolean found = containsNodeType(arena, NodeType.RECORD_DECLARATION);
			requireThat(found, "recordDeclFound").isTrue();
		}
	}

	/**
	 * Verifies that parsing a record declaration with generic type parameters
	 * allocates a RECORD_DECLARATION node.
	 * Tests that generic records are properly recognized as record declarations.
	 */
	@Test
	public void shouldAllocateRecordDeclarationNodeForGenericRecord()
	{
		String source = """
			record Box<T>(T value) { }
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			boolean found = containsNodeType(arena, NodeType.RECORD_DECLARATION);
			requireThat(found, "recordDeclFound").isTrue();
		}
	}

	/**
	 * Verifies that parsing a record declaration with an implements clause
	 * allocates a RECORD_DECLARATION node.
	 * Tests that records implementing interfaces are properly allocated.
	 */
	@Test
	public void shouldAllocateRecordDeclarationNodeForRecordWithImplements()
	{
		String source = """
			record Point(int x, int y) implements Comparable<Point> { }
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			boolean found = containsNodeType(arena, NodeType.RECORD_DECLARATION);
			requireThat(found, "recordDeclFound").isTrue();
		}
	}

	/**
	 * Verifies that parsing a nested record declaration allocates a RECORD_DECLARATION node.
	 * Tests that records declared inside classes are properly recognized.
	 */
	@Test
	public void shouldAllocateRecordDeclarationNodeForNestedRecord()
	{
		String source = """
			public class Container {
				record Inner(int value) { }
			}
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			boolean found = containsNodeType(arena, NodeType.RECORD_DECLARATION);
			requireThat(found, "recordDeclFound").isTrue();
		}
	}

	/**
	 * Verifies that parsing an annotation declaration allocates an ANNOTATION_DECLARATION node.
	 * Tests the simplest form: annotation with empty body.
	 */
	@Test
	public void shouldAllocateAnnotationDeclarationNode()
	{
		String source = """
			@interface MyAnnotation { }
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			boolean found = containsNodeType(arena, NodeType.ANNOTATION_DECLARATION);
			requireThat(found, "annotationDeclFound").isTrue();
		}
	}

	/**
	 * Verifies that parsing an annotation declaration with element definitions
	 * allocates an ANNOTATION_DECLARATION node.
	 * Tests that annotations with methods/elements are properly allocated.
	 */
	@Test
	public void shouldAllocateAnnotationDeclarationNodeWithElements()
	{
		String source = """
			@interface Config {
				String name();
				int value();
			}
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			boolean found = containsNodeType(arena, NodeType.ANNOTATION_DECLARATION);
			requireThat(found, "annotationDeclFound").isTrue();
		}
	}

	/**
	 * Verifies that parsing an annotation declaration with retention policy allocates
	 * an ANNOTATION_DECLARATION node.
	 * Tests that annotations used on annotation declarations are properly handled.
	 */
	@Test
	public void shouldAllocateAnnotationDeclarationNodeWithAnnotation()
	{
		String source = """
			@Retention(RetentionPolicy.RUNTIME) @interface Marker { }
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			boolean found = containsNodeType(arena, NodeType.ANNOTATION_DECLARATION);
			requireThat(found, "annotationDeclFound").isTrue();
		}
	}

	/**
	 * Verifies that parsing a public annotation declaration allocates an ANNOTATION_DECLARATION node.
	 * Tests that access modifiers on annotations are properly handled.
	 */
	@Test
	public void shouldAllocateAnnotationDeclarationNodeForPublicAnnotation()
	{
		String source = """
			public @interface PublicConfig { }
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);

			NodeArena arena = parser.getArena();
			boolean found = containsNodeType(arena, NodeType.ANNOTATION_DECLARATION);
			requireThat(found, "annotationDeclFound").isTrue();
		}
	}

	/**
	 * Helper method to check if a given NodeType exists in the arena.
	 * Iterates through all allocated nodes and returns true if the target type is found.
	 *
	 * @param arena the NodeArena to search
	 * @param targetType the NodeType to search for
	 * @return true if a node of the target type exists in the arena, false otherwise
	 */
	private boolean containsNodeType(NodeArena arena, NodeType targetType)
	{
		int nodeCount = arena.getNodeCount();
		for (int i = 0; i < nodeCount; ++i)
		{
			NodeIndex index = new NodeIndex(i);
			if (arena.getType(index) == targetType)
				return true;
		}
		return false;
	}
}
