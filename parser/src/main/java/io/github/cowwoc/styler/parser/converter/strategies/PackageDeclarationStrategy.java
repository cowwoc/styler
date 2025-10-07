package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.PackageDeclarationNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion strategy for package declaration nodes.
 * <p>
 * Converts Arena PACKAGE_DECLARATION nodes to {@link PackageDeclarationNode},
 * processing annotations and qualified package name.
 * </p>
 *
 * @since 1.0
 */
public final class PackageDeclarationStrategy extends BaseConversionStrategy<PackageDeclarationNode>
{
	/**
	 * Creates a package declaration conversion strategy.
	 */
	public PackageDeclarationStrategy()
	{
		super(NodeType.PACKAGE_DECLARATION, PackageDeclarationNode.class);
	}

	@Override
	public PackageDeclarationNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		// Parse children: annotations (if any) followed by package name
		List<ASTNode> annotations = new ArrayList<>();
		ASTNode packageName = null;

		for (int childId : childIds)
		{
			ArenaNodeStorage.NodeInfo childInfo = nodeStorage.getNode(childId);
			byte childType = childInfo.nodeType();

			if (childType == NodeType.ANNOTATION)
			{
				annotations.add(context.convertNode(childId, nodeStorage));
			}
			else if (childType == NodeType.IDENTIFIER_EXPRESSION ||
				childType == NodeType.FIELD_ACCESS_EXPRESSION)
			{
				// Package name can be simple identifier or qualified name (field access chain)
				packageName = context.convertNode(childId, nodeStorage);
			}
		}

		// Package name is required
		if (packageName == null)
		{
			throw new AssertionError(buildMissingChildError(nodeInfo, nodeStorage,
				new String[]{"IDENTIFIER_EXPRESSION", "FIELD_ACCESS_EXPRESSION"},
				"package name"));
		}

		return new PackageDeclarationNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			annotations,
			packageName);
	}
}
