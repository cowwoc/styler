package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.NodeType;
import org.testng.annotations.Test;

import java.util.List;

public class PackageDeclarationDebugTest
{
	@Test
	public void debugPackageStructure()
	{
		String source = "package com.example;\n";
		IndexOverlayParser parser = new IndexOverlayParser(source);
		int rootId = parser.parse();
		ArenaNodeStorage storage = parser.getNodeStorage();

		System.out.println("=== Root Node ===");
		printNode(storage, rootId, source, 0);
	}

	private void printNode(ArenaNodeStorage storage, int nodeId, String source, int indent)
	{
		ArenaNodeStorage.NodeInfo info = storage.getNode(nodeId);
		String indentStr = "  ".repeat(indent);

		String nodeTypeName = getNodeTypeName(info.nodeType());
		String sourceText = source.substring(info.startOffset(),
			Math.min(info.startOffset() + info.length(), source.length()));
		sourceText = sourceText.replace("\n", "\\n");

		System.out.println(indentStr + "Node " + nodeId + ": " + nodeTypeName +
			" [" + info.startOffset() + ":" + (info.startOffset() + info.length()) + "]" +
			" \"" + sourceText + "\"");

		List<Integer> children = info.childIds();
		System.out.println(indentStr + "  Children: " + children.size());

		for (int childId : children)
		{
			printNode(storage, childId, source, indent + 1);
		}
	}

	private String getNodeTypeName(byte nodeType)
	{
		if (nodeType == NodeType.COMPILATION_UNIT) return "COMPILATION_UNIT";
		if (nodeType == NodeType.PACKAGE_DECLARATION) return "PACKAGE_DECLARATION";
		if (nodeType == NodeType.FIELD_ACCESS_EXPRESSION) return "FIELD_ACCESS_EXPRESSION";
		return "NodeType" + nodeType;
	}
}
