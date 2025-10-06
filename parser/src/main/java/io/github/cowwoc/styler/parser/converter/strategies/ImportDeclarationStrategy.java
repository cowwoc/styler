package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.ImportDeclarationNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import java.util.List;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for import declaration nodes.
 * <p>
 * Converts Arena IMPORT_DECLARATION nodes to {@link ImportDeclarationNode},
 * detecting static imports and on-demand (wildcard) imports.
 * </p>
 *
 * @since 1.0
 */
public final class ImportDeclarationStrategy extends BaseConversionStrategy<ImportDeclarationNode>
{
	/**
	 * Creates an import declaration conversion strategy.
	 */
	public ImportDeclarationStrategy()
	{
		super(NodeType.IMPORT_DECLARATION, ImportDeclarationNode.class);
	}

	@Override
	public ImportDeclarationNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		// Detect import characteristics from source text
		String sourceText = context.getSourceText(
			nodeInfo.startOffset(),
			nodeInfo.endOffset());
		boolean isStatic = sourceText.contains("static");
		boolean isOnDemand = sourceText.endsWith("*") || sourceText.contains(".*");

		// Find the imported name (last non-modifier child)
		ASTNode importName = null;
		for (int childId : childIds)
		{
			ArenaNodeStorage.NodeInfo childInfo = nodeStorage.getNode(childId);
			byte childType = childInfo.nodeType();

			// Skip modifiers, focus on the actual import path
			if (childType != NodeType.MODIFIER)
			{
				importName = context.convertNode(childId, nodeStorage);
			}
		}

		if (importName == null)
		{
			throw new IllegalStateException(
				"Import declaration must have an import name at node " + nodeId);
		}

		return new ImportDeclarationNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			isStatic,
			importName,
			isOnDemand);
	}
}
