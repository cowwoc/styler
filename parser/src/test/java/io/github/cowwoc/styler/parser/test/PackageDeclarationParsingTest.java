package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.NodeType;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for parsing package declarations and creating proper child nodes for package names.
 * <p>
 * These tests verify the fix for the bug where parsePackageDeclaration() didn't create child nodes for the
 * qualified package name, causing ArenaToAstConverter to throw "Package declaration must have a package name
 * at node X".
 */
public class PackageDeclarationParsingTest
{
	/**
	 * Tests parsing a simple two-segment package name (com.example).
	 * <p>
	 * Verifies:
	 * - PACKAGE_DECLARATION node is created
	 * - Child node (qualified name) is created and attached
	 * - Node type is FIELD_ACCESS_EXPRESSION
	 * - Source text extraction works correctly
	 */
	@Test
	public void testSimplePackageName()
	{
		String source = "package com.example;\n";
		IndexOverlayParser parser = new IndexOverlayParser(source);
		int rootId = parser.parse();
		ArenaNodeStorage storage = parser.getNodeStorage();

		// Verify COMPILATION_UNIT node exists
		ArenaNodeStorage.NodeInfo rootInfo = storage.getNode(rootId);
		List<Integer> children = rootInfo.childIds();

		// Should have 1 child (package declaration)
		assertEquals(children.size(), 1, "Expected 1 child (package declaration)");

		int packageDeclId = children.get(0);
		ArenaNodeStorage.NodeInfo packageInfo = storage.getNode(packageDeclId);
		assertEquals(packageInfo.nodeType(), NodeType.PACKAGE_DECLARATION);

		// Verify package declaration has 1 child (qualified name)
		List<Integer> packageChildren = packageInfo.childIds();
		assertEquals(packageChildren.size(), 1, "Expected 1 child (qualified name)");

		int qualifiedNameId = packageChildren.get(0);
		ArenaNodeStorage.NodeInfo qualifiedNameInfo = storage.getNode(qualifiedNameId);
		assertEquals(qualifiedNameInfo.nodeType(), NodeType.FIELD_ACCESS_EXPRESSION);

		// Verify source text extraction
		String qualifiedNameText = source.substring(
			qualifiedNameInfo.startOffset(),
			qualifiedNameInfo.startOffset() + qualifiedNameInfo.length());
		assertEquals(qualifiedNameText, "com.example");
	}

	/**
	 * Tests parsing a deeply nested multi-segment package name.
	 * <p>
	 * Verifies that long qualified names (6 segments) are handled correctly.
	 */
	@Test
	public void testDeeplyNestedPackageName()
	{
		String source = "package io.github.cowwoc.styler.ast.visitor;\n";
		IndexOverlayParser parser = new IndexOverlayParser(source);
		int rootId = parser.parse();
		ArenaNodeStorage storage = parser.getNodeStorage();

		// Navigate to package declaration
		ArenaNodeStorage.NodeInfo rootInfo = storage.getNode(rootId);
		List<Integer> children = rootInfo.childIds();
		assertEquals(children.size(), 1, "Expected 1 child (package declaration)");

		int packageDeclId = children.get(0);
		ArenaNodeStorage.NodeInfo packageInfo = storage.getNode(packageDeclId);
		assertEquals(packageInfo.nodeType(), NodeType.PACKAGE_DECLARATION);

		// Verify qualified name child
		List<Integer> packageChildren = packageInfo.childIds();
		assertEquals(packageChildren.size(), 1, "Expected 1 child (qualified name)");

		int qualifiedNameId = packageChildren.get(0);
		ArenaNodeStorage.NodeInfo qualifiedNameInfo = storage.getNode(qualifiedNameId);
		assertEquals(qualifiedNameInfo.nodeType(), NodeType.FIELD_ACCESS_EXPRESSION);

		// Verify source text
		String qualifiedNameText = source.substring(
			qualifiedNameInfo.startOffset(),
			qualifiedNameInfo.startOffset() + qualifiedNameInfo.length());
		assertEquals(qualifiedNameText, "io.github.cowwoc.styler.ast.visitor");
	}

	/**
	 * Tests parsing a single-segment package name (e.g., "package util;").
	 * <p>
	 * Verifies that simple (non-qualified) package names are handled correctly using the same node type
	 * (FIELD_ACCESS_EXPRESSION) as multi-segment names for consistency.
	 */
	@Test
	public void testSingleSegmentPackage()
	{
		String source = "package util;\n";
		IndexOverlayParser parser = new IndexOverlayParser(source);
		int rootId = parser.parse();
		ArenaNodeStorage storage = parser.getNodeStorage();

		// Navigate to package declaration
		ArenaNodeStorage.NodeInfo rootInfo = storage.getNode(rootId);
		List<Integer> children = rootInfo.childIds();
		assertEquals(children.size(), 1, "Expected 1 child (package declaration)");

		int packageDeclId = children.get(0);
		ArenaNodeStorage.NodeInfo packageInfo = storage.getNode(packageDeclId);
		assertEquals(packageInfo.nodeType(), NodeType.PACKAGE_DECLARATION);

		// Verify qualified name child (even for single segment)
		List<Integer> packageChildren = packageInfo.childIds();
		assertEquals(packageChildren.size(), 1, "Expected 1 child (qualified name)");

		int qualifiedNameId = packageChildren.get(0);
		ArenaNodeStorage.NodeInfo qualifiedNameInfo = storage.getNode(qualifiedNameId);
		assertEquals(qualifiedNameInfo.nodeType(), NodeType.FIELD_ACCESS_EXPRESSION);

		// Verify source text
		String qualifiedNameText = source.substring(
			qualifiedNameInfo.startOffset(),
			qualifiedNameInfo.startOffset() + qualifiedNameInfo.length());
		assertEquals(qualifiedNameText, "util");
	}

	/**
	 * Tests parsing a file with package declaration and verifies Arena node structure.
	 * <p>
	 * This test verifies that the fix creates proper Arena node structure that can be converted by
	 * ArenaToAstConverter without throwing "Package declaration must have a package name" error.
	 */
	@Test
	public void testPackageDeclarationNodeStructure()
	{
		String source = "package io.github.cowwoc.styler;\n\nclass Test {}";

		IndexOverlayParser parser = new IndexOverlayParser(source);
		int rootId = parser.parse();
		ArenaNodeStorage storage = parser.getNodeStorage();

		// Verify compilation unit structure
		ArenaNodeStorage.NodeInfo rootInfo = storage.getNode(rootId);
		List<Integer> children = rootInfo.childIds();

		// Should have 2 children: package declaration and class declaration
		assertTrue(!children.isEmpty(), "Expected at least 1 child (package declaration)");

		// First child should be package declaration
		int packageDeclId = children.get(0);
		ArenaNodeStorage.NodeInfo packageInfo = storage.getNode(packageDeclId);
		assertEquals(packageInfo.nodeType(), NodeType.PACKAGE_DECLARATION);

		// Package declaration MUST have qualified name child
		List<Integer> packageChildren = packageInfo.childIds();
		assertEquals(packageChildren.size(), 1, "Expected 1 child (qualified name)");

		// Qualified name should be FIELD_ACCESS_EXPRESSION
		int qualifiedNameId = packageChildren.get(0);
		ArenaNodeStorage.NodeInfo qualifiedNameInfo = storage.getNode(qualifiedNameId);
		assertEquals(qualifiedNameInfo.nodeType(), NodeType.FIELD_ACCESS_EXPRESSION);

		// Verify source text
		String qualifiedNameText = source.substring(
			qualifiedNameInfo.startOffset(),
			qualifiedNameInfo.startOffset() + qualifiedNameInfo.length());
		assertEquals(qualifiedNameText, "io.github.cowwoc.styler");
	}

	/**
	 * Tests parsing a file with no package declaration (default package).
	 * <p>
	 * Verifies that files without package declarations don't cause errors and that the COMPILATION_UNIT has
	 * no package declaration child.
	 */
	@Test
	public void testDefaultPackage()
	{
		String source = "class Test {}";

		IndexOverlayParser parser = new IndexOverlayParser(source);
		int rootId = parser.parse();
		ArenaNodeStorage storage = parser.getNodeStorage();

		// Verify no exception thrown - parsing should succeed

		// Verify compilation unit structure
		ArenaNodeStorage.NodeInfo rootInfo = storage.getNode(rootId);
		List<Integer> children = rootInfo.childIds();

		// Verify that first child (if any) is not a package declaration
		if (!children.isEmpty())
		{
			int childId = children.get(0);
			ArenaNodeStorage.NodeInfo childInfo = storage.getNode(childId);
			assertTrue(childInfo.nodeType() != NodeType.PACKAGE_DECLARATION,
				"First child should not be package declaration");
		}
	}

	/**
	 * Tests parsing a file with package declaration followed by class.
	 * <p>
	 * Verifies that package declaration is parsed correctly with child node for qualified name.
	 * Note: Class parsing is tested elsewhere; this focuses on package declaration structure.
	 */
	@Test
	public void testPackageDeclarationWithClass()
	{
		String source = "package com.example;\n\npublic class MyClass {}";

		IndexOverlayParser parser = new IndexOverlayParser(source);
		int rootId = parser.parse();
		ArenaNodeStorage storage = parser.getNodeStorage();

		ArenaNodeStorage.NodeInfo rootInfo = storage.getNode(rootId);
		List<Integer> children = rootInfo.childIds();

		// Should have at least the package declaration
		assertTrue(!children.isEmpty(), "Expected at least 1 child (package declaration)");

		// First child should be package declaration
		int packageDeclId = children.get(0);
		ArenaNodeStorage.NodeInfo packageInfo = storage.getNode(packageDeclId);
		assertEquals(packageInfo.nodeType(), NodeType.PACKAGE_DECLARATION);

		// Package declaration should have qualified name child
		List<Integer> packageChildren = packageInfo.childIds();
		assertEquals(packageChildren.size(), 1, "Expected 1 child (qualified name)");

		// Verify the qualified name is correct type
		int qualifiedNameId = packageChildren.get(0);
		ArenaNodeStorage.NodeInfo qualifiedNameInfo = storage.getNode(qualifiedNameId);
		assertEquals(qualifiedNameInfo.nodeType(), NodeType.FIELD_ACCESS_EXPRESSION);
	}
}
