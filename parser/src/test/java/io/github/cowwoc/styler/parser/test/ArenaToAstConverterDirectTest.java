package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.converter.ArenaToAstConverter;
import io.github.cowwoc.styler.parser.NodeType;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.converter.DefaultStrategyRegistry;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Direct unit tests for ArenaToAstConverter that create Arena nodes manually.
 * <p>
 * These tests verify the converter logic without relying on the parser implementation.
 */
public class ArenaToAstConverterDirectTest
{
	/**
	 * Verifies conversion of a compilation unit with manually created Arena nodes.
	 * <p>
	 * Tests the converter can handle a simple compilation unit node with no children.
	 */
	@Test(enabled = false)
	public void testConvertEmptyCompilationUnit()
	{
		String source = "// Empty compilation unit\n";
		ArenaNodeStorage storage = ArenaNodeStorage.create(10);

		// Create compilation unit node manually
		int compilationUnitId = storage.allocateNode(0, 0, NodeType.COMPILATION_UNIT, -1);

		ArenaToAstConverter converter = ArenaToAstConverter.create(source, DefaultStrategyRegistry.create());
		CompilationUnitNode ast = (CompilationUnitNode) converter.convert(compilationUnitId, storage);

		requireThat(ast, "ast").isNotNull();
		requireThat(ast.getPackageDeclaration().isPresent(), "hasPackage").isEqualTo(false);
		requireThat(ast.getImports().isEmpty(), "hasImports").isEqualTo(true);
		requireThat(ast.getTypeDeclarations().isEmpty(), "hasTypes").isEqualTo(true);
	}

	/**
	 * Verifies conversion with a manually created class node.
	 * <p>
	 * Tests that the converter handles parent-child relationships correctly.
	 */
	@Test(enabled = false)
	public void testConvertWithClassNode()
	{
		String source = "class Test { }";
		ArenaNodeStorage storage = ArenaNodeStorage.create(10);

		// Create compilation unit (parent)
		int compilationUnitId = storage.allocateNode(0, source.length(), NodeType.COMPILATION_UNIT, -1);

		// Create class declaration (child) - allocateNode automatically adds to parent's children
		storage.allocateNode(0, source.length(), NodeType.CLASS_DECLARATION, compilationUnitId);

		ArenaToAstConverter converter = ArenaToAstConverter.create(source, DefaultStrategyRegistry.create());
		CompilationUnitNode ast = (CompilationUnitNode) converter.convert(compilationUnitId, storage);

		requireThat(ast, "ast").isNotNull();
		requireThat(ast.getTypeDeclarations().size(), "typeCount").isEqualTo(1);
	}

	/**
	 * Verifies conversion with package, import, and class nodes.
	 * <p>
	 * Tests the complete MVP node type coverage.
	 */
	@Test(enabled = false)
	public void testConvertCompleteStructure()
	{
		String source = "package com.example;\n\nimport java.util.List;\n\nclass Test { }";
		ArenaNodeStorage storage = ArenaNodeStorage.create(10);

		// Create compilation unit
		int compilationUnitId = storage.allocateNode(0, source.length(), NodeType.COMPILATION_UNIT, -1);

		// Create package declaration
		storage.allocateNode(0, 20, NodeType.PACKAGE_DECLARATION, compilationUnitId);

		// Create import declaration
		storage.allocateNode(22, 20, NodeType.IMPORT_DECLARATION, compilationUnitId);

		// Create class declaration
		storage.allocateNode(44, source.length() - 44, NodeType.CLASS_DECLARATION, compilationUnitId);

		ArenaToAstConverter converter = ArenaToAstConverter.create(source, DefaultStrategyRegistry.create());
		CompilationUnitNode ast = (CompilationUnitNode) converter.convert(compilationUnitId, storage);

		requireThat(ast, "ast").isNotNull();
		requireThat(ast.getPackageDeclaration().isPresent(), "hasPackage").isEqualTo(true);
		requireThat(ast.getImports().size(), "importCount").isEqualTo(1);
		requireThat(ast.getTypeDeclarations().size(), "typeCount").isEqualTo(1);
	}
}
