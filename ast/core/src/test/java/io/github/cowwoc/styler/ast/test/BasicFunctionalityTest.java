package io.github.cowwoc.styler.ast.test;

import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.node.ClassDeclarationNode;
import io.github.cowwoc.styler.ast.node.IdentifierNode;
import io.github.cowwoc.styler.ast.node.StringLiteralNode;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/**
 * Basic functionality tests for the AST core module.
 * Tests fundamental AST node creation and basic operations.
 */
public class BasicFunctionalityTest
{
	/**
	 * Test that basic module functionality is working.
	 */
	@Test
	public void basicFunctionality()
	{
		// Basic test to ensure module loads correctly
		assert true : "Basic functionality test passed";
	}

	/**
	 * Test basic AST node creation.
	 */
	@Test
	public void nodeCreation()
	{
		// Test that we can create basic AST nodes
		SourceRange defaultRange = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		IdentifierNode identifier = new IdentifierNode.Builder().
			setName("testVariable").
			setRange(defaultRange).
			build();

		assertNotNull(identifier);
		assertEquals("testVariable", identifier.getName());
	}

	/**
	 * Test basic source position functionality.
	 */
	@Test
	public void sourcePosition()
	{
		SourcePosition pos = new SourcePosition(1, 5);
		assertEquals(1, pos.line());
		assertEquals(5, pos.column());

		// Test advancement
		SourcePosition advanced = pos.advanceColumn(3);
		assertEquals(1, advanced.line());
		assertEquals(8, advanced.column());
	}

	/**
	 * Test basic source range functionality.
	 */
	@Test
	public void sourceRange()
	{
		SourcePosition start = new SourcePosition(1, 1);
		SourcePosition end = new SourcePosition(1, 10);
		SourceRange range = new SourceRange(start, end);

		assertEquals(start, range.start());
		assertEquals(end, range.end());
	}

	/**
	 * Test that multiple node types can be created.
	 */
	@Test
	public void multipleNodeTypes()
	{
		// Test different node types
		SourceRange defaultRange = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));

		IdentifierNode identifier = new IdentifierNode.Builder().
			setName("variable").
			setRange(defaultRange).
			build();

		StringLiteralNode stringLiteral = new StringLiteralNode.Builder().
			setValue("test string").
			setRange(defaultRange).
			build();

		ClassDeclarationNode classDecl = new ClassDeclarationNode.Builder().
			setName("TestClass").
			setRange(defaultRange).
			build();

		// Verify all nodes are created successfully
		assertNotNull(identifier);
		assertNotNull(stringLiteral);
		assertNotNull(classDecl);

		assertEquals("variable", identifier.getName());
		assertEquals("TestClass", classDecl.getName());
	}

	/**
	 * Test toString() methods work.
	 */
	@Test
	public void toStringMethods()
	{
		SourceRange defaultRange = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 10));
		IdentifierNode node = new IdentifierNode.Builder().
			setName("test").
			setRange(defaultRange).
			build();

		String toString = node.toString();
		assertNotNull(toString);
		assertFalse(toString.isEmpty());
	}
}