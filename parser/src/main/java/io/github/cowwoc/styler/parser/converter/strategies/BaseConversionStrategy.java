package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.converter.ConversionContext;
import io.github.cowwoc.styler.parser.converter.ConversionStrategy;
import io.github.cowwoc.styler.parser.converter.InvalidNodeStructureException;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for conversion strategies providing common utility methods.
 * <p>
 * This abstract class handles common conversion patterns like child node recursion,
 * source range mapping, and metadata extraction. Concrete strategies extend this class
 * and implement node-specific construction logic.
 * </p>
 *
 * @param <T> the specific AST node type this strategy produces
 * @since 1.0
 */
public abstract class BaseConversionStrategy<T extends ASTNode> implements ConversionStrategy<T>
{
	private final byte handledNodeType;
	private final Class<T> producedNodeClass;

	/**
	 * Creates a base conversion strategy.
	 *
	 * @param handledNodeType the node type byte constant this strategy handles
	 * @param producedNodeClass the AST node class this strategy produces
	 */
	protected BaseConversionStrategy(byte handledNodeType, Class<T> producedNodeClass)
{
		this.handledNodeType = handledNodeType;
		this.producedNodeClass = producedNodeClass;
	}

	@Override
	public byte getHandledNodeType()
{
		return handledNodeType;
	}

	@Override
	public Class<T> getProducedNodeClass()
{
		return producedNodeClass;
	}

	/**
	 * Converts child nodes recursively using the conversion context.
	 *
	 * @param childIds list of child node IDs from Arena
	 * @param nodeStorage the Arena node storage
	 * @param context the conversion context
	 * @return list of converted AST child nodes
	 */
	protected List<ASTNode> convertChildren(
		List<Integer> childIds,
		ArenaNodeStorage nodeStorage,
		ConversionContext context)
{
		List<ASTNode> children = new ArrayList<>(childIds.size());
		for (int childId : childIds)
{
			children.add(context.convertNode(childId, nodeStorage));
		}
		return children;
	}

	/**
	 * Gets the source range for a node from Arena storage.
	 *
	 * @param nodeInfo the Arena node info
	 * @param context the conversion context
	 * @return the source range
	 */
	protected SourceRange getSourceRange(
		ArenaNodeStorage.NodeInfo nodeInfo,
		ConversionContext context)
{
		return context.getSourceRange(nodeInfo.startOffset(), nodeInfo.endOffset());
	}

	/**
	 * Validates that a node has the expected number of children.
	 *
	 * @param nodeInfo the Arena node info
	 * @param expectedCount the expected child count
	 * @param context the conversion context
	 * @throws InvalidNodeStructureException if child count doesn't match
	 */
	protected void validateChildCount(
		ArenaNodeStorage.NodeInfo nodeInfo,
		int expectedCount,
		ConversionContext context)
{
		int actualCount = nodeInfo.childIds().size();
		if (actualCount != expectedCount)
{
			SourceRange range = getSourceRange(nodeInfo, context);
			throw new InvalidNodeStructureException(
				"Expected " + expectedCount + " children but found " + actualCount,
				nodeInfo.nodeId(),
				nodeInfo.nodeType(),
				range);
		}
	}

	/**
	 * Validates that a node has at least the specified number of children.
	 *
	 * @param nodeInfo the Arena node info
	 * @param minCount the minimum expected child count
	 * @param context the conversion context
	 * @throws InvalidNodeStructureException if child count is less than minimum
	 */
	protected void validateMinChildCount(
		ArenaNodeStorage.NodeInfo nodeInfo,
		int minCount,
		ConversionContext context)
{
		int actualCount = nodeInfo.childIds().size();
		if (actualCount < minCount)
{
			SourceRange range = getSourceRange(nodeInfo, context);
			throw new InvalidNodeStructureException(
				"Expected at least " + minCount + " children but found " + actualCount,
				nodeInfo.nodeId(),
				nodeInfo.nodeType(),
				range);
		}
	}

	/**
	 * Builds an error message for missing required child nodes, indicating a parser bug.
	 * <p>
	 * This method generates a detailed diagnostic message that helps developers immediately
	 * identify parser bugs by showing what was expected versus what was actually created.
	 * </p>
	 *
	 * @param nodeInfo the node information from Arena storage containing node type and ID
	 * @param nodeStorage the Arena node storage for retrieving child node information
	 * @param expectedTypes array of expected node type names (e.g., ["IDENTIFIER_EXPRESSION",
	 *     "FIELD_ACCESS_EXPRESSION"])
	 * @param description human-readable description of what's missing (e.g., "package name")
	 * @return formatted error message with parser bug prefix, expected/actual comparison, and
	 *     parser method reference
	 */
	protected String buildMissingChildError(ArenaNodeStorage.NodeInfo nodeInfo, ArenaNodeStorage nodeStorage,
		String[] expectedTypes, String description)
	{
		String nodeTypeName = nodeInfo.getTypeName();
		String parserMethod = getParserMethodName(nodeInfo.nodeType());
		String expectedList = String.join(" or ", expectedTypes);

		return "Parser bug: " + nodeTypeName + " node " + nodeInfo.nodeId() +
			" has no child nodes for " + description + ". " +
			"Expected: [" + expectedList + "] child. " +
			"Actual: []. " +
			"Parser method: " + parserMethod + "() in IndexOverlayParser. " +
			"Diagnostic: Parser failed to create/attach child nodes";
	}

	/**
	 * Builds an error message for wrong child node type at specified position.
	 * <p>
	 * This method generates a detailed diagnostic message showing the type mismatch between
	 * expected and actual child node types, helping developers pinpoint parser bugs.
	 * </p>
	 *
	 * @param nodeInfo the parent node information from Arena storage
	 * @param nodeStorage the Arena node storage for retrieving child node information
	 * @param childIndex the zero-based index of the child with wrong type
	 * @param expectedType the expected node type name
	 * @return formatted error message with parser bug prefix and type mismatch details
	 */
	protected String buildWrongChildTypeError(ArenaNodeStorage.NodeInfo nodeInfo, ArenaNodeStorage nodeStorage,
		int childIndex, String expectedType)
	{
		List<Integer> children = nodeInfo.childIds();
		ArenaNodeStorage.NodeInfo childInfo = nodeStorage.getNode(children.get(childIndex));
		String nodeTypeName = nodeInfo.getTypeName();
		String actualType = childInfo.getTypeName();
		String parserMethod = getParserMethodName(nodeInfo.nodeType());

		return "Parser bug: " + nodeTypeName + " node " + nodeInfo.nodeId() +
			" has wrong child type at position " + childIndex + ". " +
			"Expected: " + expectedType + ". " +
			"Actual: " + actualType + "(id:" + childInfo.nodeId() + "). " +
			"Parser method: " + parserMethod + "() in IndexOverlayParser. " +
			"Diagnostic: Parser created wrong node type";
	}

	/**
	 * Builds an error message for child count mismatch (expected exact count).
	 * <p>
	 * This method generates a detailed diagnostic message showing the child count mismatch
	 * and listing the actual children that were created by the parser.
	 * </p>
	 *
	 * @param nodeInfo the node information from Arena storage
	 * @param nodeStorage the Arena node storage for retrieving child node information
	 * @param expectedCount the exact number of children expected
	 * @return formatted error message with parser bug prefix and count mismatch details
	 */
	protected String buildChildCountError(ArenaNodeStorage.NodeInfo nodeInfo, ArenaNodeStorage nodeStorage,
		int expectedCount)
	{
		int actualCount = nodeInfo.childIds().size();
		String nodeTypeName = nodeInfo.getTypeName();
		String parserMethod = getParserMethodName(nodeInfo.nodeType());
		String childList = formatChildList(nodeInfo.childIds(), nodeStorage);

		return "Parser bug: " + nodeTypeName + " node " + nodeInfo.nodeId() +
			" has " + actualCount + " children but expected exactly " + expectedCount + ". " +
			"Actual children: " + childList + ". " +
			"Parser method: " + parserMethod + "() in IndexOverlayParser. " +
			"Diagnostic: Parser created wrong number of child nodes";
	}

	/**
	 * Formats a list of child node IDs as human-readable node type and ID pairs.
	 * <p>
	 * This method creates a compact, readable representation of child nodes for inclusion in
	 * error messages, showing both the node type and ID for each child.
	 * </p>
	 *
	 * @param childIds list of child node IDs from Arena storage
	 * @param nodeStorage the Arena node storage for retrieving child node information
	 * @return formatted string like "[IDENTIFIER(id:5), OPERATOR(id:7)]" or "[]" for empty
	 */
	protected String formatChildList(List<Integer> childIds, ArenaNodeStorage nodeStorage)
	{
		if (childIds.isEmpty())
		{
			return "[]";
		}

		StringBuilder result = new StringBuilder("[");
		for (int i = 0; i < childIds.size(); i += 1)
		{
			if (i > 0)
			{
				result.append(", ");
			}
			ArenaNodeStorage.NodeInfo childInfo = nodeStorage.getNode(childIds.get(i));
			result.append(childInfo.getTypeName()).
				append("(id:").append(childInfo.nodeId()).append(')');
		}
		result.append(']');
		return result.toString();
	}

	/**
	 * Maps node type byte constant to corresponding parser method name in IndexOverlayParser.
	 * <p>
	 * This method uses a simple naming convention: node type names are prepended with "parse"
	 * to form the parser method name. For example, "PackageDeclaration" becomes
	 * "parsePackageDeclaration".
	 * </p>
	 *
	 * @param nodeType the node type byte constant to map
	 * @return parser method name (e.g., "parsePackageDeclaration" for PACKAGE_DECLARATION)
	 */
	protected String getParserMethodName(byte nodeType)
	{
		// Get human-readable type name (e.g., "PackageDeclaration")
		String typeName = io.github.cowwoc.styler.parser.NodeType.getTypeName(nodeType);

		// Parser methods follow "parse" + TypeName pattern
		return "parse" + typeName;
	}
}
