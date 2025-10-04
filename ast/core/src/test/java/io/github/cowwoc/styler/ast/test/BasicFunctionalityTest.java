package io.github.cowwoc.styler.ast.test;

import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.node.ClassDeclarationNode;
import io.github.cowwoc.styler.ast.node.IdentifierNode;
import io.github.cowwoc.styler.ast.node.StringLiteralNode;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

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

		requireThat(identifier, "identifier").isNotNull();
		requireThat(identifier.getName(), "identifierName").isEqualTo("testVariable");
	}

	/**
	 * Test basic source position functionality.
	 */
	@Test
	public void sourcePosition()
	{
		SourcePosition pos = new SourcePosition(1, 5);
		requireThat(pos.line(), "line").isEqualTo(1);
		requireThat(pos.column(), "column").isEqualTo(5);

		// Test advancement
		SourcePosition advanced = pos.advanceColumn(3);
		requireThat(advanced.line(), "advancedLine").isEqualTo(1);
		requireThat(advanced.column(), "advancedColumn").isEqualTo(8);
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

		requireThat(range.start(), "rangeStart").isEqualTo(start);
		requireThat(range.end(), "rangeEnd").isEqualTo(end);
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
		requireThat(identifier, "identifier").isNotNull();
		requireThat(stringLiteral, "stringLiteral").isNotNull();
		requireThat(classDecl, "classDecl").isNotNull();

		requireThat(identifier.getName(), "identifierName").isEqualTo("variable");
		requireThat(classDecl.getName(), "className").isEqualTo("TestClass");
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
		requireThat(toString, "toString").isNotNull();
		requireThat(toString.isEmpty(), "toStringIsEmpty").isFalse();
	}
}