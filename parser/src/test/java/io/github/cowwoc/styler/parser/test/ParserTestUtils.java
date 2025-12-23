package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;

import java.util.HashSet;
import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Utility methods for parser tests.
 * <p>
 * Provides AST validation capabilities to verify that parsing produces the expected node structure,
 * not just that parsing succeeds without exceptions.
 */
public final class ParserTestUtils
{
	private ParserTestUtils()
	{
	}

	/**
	 * Represents an AST node with its type, source positions, and optional attribute value.
	 * <p>
	 * This is the semantic representation of an AST node, including its exact position in source code.
	 * Two ASTs with the same set of SemanticNodes are semantically identical, regardless of arena
	 * allocation order.
	 * <p>
	 * The tree structure is implicit in the positions: a node A contains node B if
	 * {@code A.start <= B.start && B.end <= A.end}.
	 *
	 * @param type           the node type
	 * @param start          the start position in source code (inclusive)
	 * @param end            the end position in source code (exclusive)
	 * @param attributeValue the attribute value (qualified name for imports, type name for declarations),
	 *                       or {@code null} if the node has no attribute
	 * @throws NullPointerException     if {@code type} is null
	 * @throws IllegalArgumentException if {@code start} is negative, or if {@code end} is less than
	 *                                  {@code start}, or if {@code end} equals {@code start} for any type
	 *                                  other than {@code COMPILATION_UNIT}, or if {@code attributeValue} is
	 *                                  empty
	 */
	public record SemanticNode(NodeType type, int start, int end, String attributeValue)
	{
		/**
		 * Creates a semantic node.
		 *
		 * @param type           the node type
		 * @param start          the start position in source code (inclusive)
		 * @param end            the end position in source code (exclusive)
		 * @param attributeValue the attribute value, or {@code null} if the node has no attribute
		 */
		public SemanticNode
		{
			requireThat(type, "type").isNotNull();
			requireThat(start, "start").isNotNegative();
			// Only COMPILATION_UNIT can have zero-width span (empty source file)
			if (type == NodeType.COMPILATION_UNIT)
				requireThat(end, "end").isGreaterThanOrEqualTo(start, "start");
			else
				requireThat(end, "end").isGreaterThan(start, "start");
			if (attributeValue != null)
				requireThat(attributeValue, "attributeValue").isNotEmpty();
		}

		/**
		 * Creates a semantic node with positions but no attribute value.
		 *
		 * @param type  the node type
		 * @param start the start position in source code (inclusive)
		 * @param end   the end position in source code (exclusive)
		 * @throws NullPointerException     if {@code type} is null
		 * @throws IllegalArgumentException if {@code start} is negative, or if {@code end} is less than
		 *                                  {@code start}, or if {@code end} equals {@code start} for any type
		 *                                  other than {@code COMPILATION_UNIT}
		 */
		public SemanticNode(NodeType type, int start, int end)
		{
			this(type, start, end, null);
		}
	}

	/**
	 * Parses source code and returns a set of all AST nodes with their positions.
	 * <p>
	 * The returned set contains every node created during parsing, with its type, source positions,
	 * and optional attribute value. This enables semantic comparison of ASTs independent of
	 * arena allocation order.
	 * <p>
	 * The tree structure is implicit in the positions: containment relationships can be derived
	 * from comparing position ranges.
	 *
	 * @param source the Java source code to parse
	 * @return set of semantic AST nodes
	 * @throws AssertionError if parsing fails
	 */
	public static Set<SemanticNode> parseSemanticAst(String source)
	{
		requireThat(source, "source").isNotNull();

		try (Parser parser = new Parser(source))
		{
			switch (parser.parse())
			{
				case ParseResult.Success _ ->
				{
					NodeArena arena = parser.getArena();
					int nodeCount = arena.getNodeCount();
					Set<SemanticNode> nodes = new HashSet<>(nodeCount);

					for (int i = 0; i < nodeCount; ++i)
					{
						NodeIndex index = new NodeIndex(i);
						NodeType type = arena.getType(index);
						int start = arena.getStart(index);
						int end = arena.getEnd(index);
						String attributeValue = extractAttributeValue(arena, index, type);
						nodes.add(new SemanticNode(type, start, end, attributeValue));
					}
					return nodes;
				}
				case ParseResult.Failure failure ->
					throw new AssertionError("Parsing failed: " + failure);
			}
		}
	}

	/**
	 * Extracts the attribute value for a node if applicable.
	 *
	 * @param arena the node arena
	 * @param index the node index
	 * @param type  the node type
	 * @return the attribute value, or {@code null} if the node type has no attribute
	 */
	private static String extractAttributeValue(NodeArena arena, NodeIndex index, NodeType type)
	{
		return switch (type)
		{
			case IMPORT_DECLARATION, STATIC_IMPORT_DECLARATION ->
				arena.getImportAttribute(index).qualifiedName();
			case PACKAGE_DECLARATION ->
				arena.getPackageAttribute(index).packageName();
			case CLASS_DECLARATION, INTERFACE_DECLARATION, ENUM_DECLARATION,
				RECORD_DECLARATION, ANNOTATION_DECLARATION ->
				arena.getTypeDeclarationAttribute(index).typeName();
			default -> null;
		};
	}

	/**
	 * Creates a semantic node with type and positions (no attribute value).
	 *
	 * @param type  the node type
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic AST node
	 * @throws NullPointerException     if {@code type} is null
	 * @throws IllegalArgumentException if {@code start} is negative, or if {@code end} is less than
	 *                                  {@code start}, or if {@code end} equals {@code start} for any type
	 *                                  other than {@code COMPILATION_UNIT}
	 */
	public static SemanticNode semanticNode(NodeType type, int start, int end)
	{
		return new SemanticNode(type, start, end);
	}

	/**
	 * Creates a semantic node with type, positions, and attribute value.
	 *
	 * @param type           the node type
	 * @param start          the start position in source code (inclusive)
	 * @param end            the end position in source code (exclusive)
	 * @param attributeValue the attribute value
	 * @return a new semantic AST node
	 * @throws NullPointerException     if {@code type} is null
	 * @throws IllegalArgumentException if {@code start} is negative, or if {@code end} is less than or
	 *                                  equal to {@code start}, or if {@code attributeValue} is empty
	 */
	public static SemanticNode semanticNode(NodeType type, int start, int end, String attributeValue)
	{
		// Nodes with attributes must have non-zero span
		requireThat(end, "end").isGreaterThan(start, "start");
		return new SemanticNode(type, start, end, attributeValue);
	}

	/**
	 * Asserts that the given source code parses successfully and returns a valid root node.
	 *
	 * @param source the source code to parse
	 * @throws AssertionError if parsing fails or the root node is invalid
	 */
	public static void assertParseSucceeds(String source)
	{
		try (Parser parser = new Parser(source))
		{
			switch (parser.parse())
			{
				case ParseResult.Success success ->
					requireThat(success.rootNode(), "rootNode").
						isNotNull();
				case ParseResult.Failure failure ->
					throw new AssertionError("Expected Success but got: " + failure);
			}
		}
	}

	/**
	 * Asserts that the given source code fails to parse.
	 * Used to verify that malformed syntax is correctly rejected by the parser.
	 *
	 * @param source the source code to parse
	 * @throws AssertionError if parsing succeeds when it should have failed
	 */
	public static void assertParseFails(String source)
	{
		try (Parser parser = new Parser(source))
		{
			switch (parser.parse())
			{
				case ParseResult.Success success ->
					throw new AssertionError("Expected Failure but got: " + success);
				case ParseResult.Failure _ ->
				{
					// Expected - parsing should fail for malformed input
				}
			}
		}
	}
}
