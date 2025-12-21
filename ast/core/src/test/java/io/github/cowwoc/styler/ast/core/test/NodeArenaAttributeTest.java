package io.github.cowwoc.styler.ast.core.test;

import io.github.cowwoc.styler.ast.core.ImportAttribute;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for NodeArena attribute storage and retrieval.
 * <p>
 * Verifies that semantic attributes can be attached to AST nodes during parsing
 * and retrieved efficiently during formatting operations.
 * <p>
 * <b>Thread-safety</b>: Thread-safe - all instances are created inside {@code @Test} methods.
 */
public class NodeArenaAttributeTest
{
	/**
	 * Verifies that an ImportAttribute can be stored and retrieved from a node.
	 */
	@Test
	public void shouldStoreAndRetrieveImportAttribute()
	{
		try (NodeArena arena = new NodeArena())
		{
			ImportAttribute attribute = new ImportAttribute("java.util.List");
			NodeIndex index = arena.allocateImportDeclaration(0, 25, attribute);

			ImportAttribute retrieved = arena.getImportAttribute(index);
			requireThat(retrieved.qualifiedName(), "qualifiedName").isEqualTo("java.util.List");
		}
	}

	/**
	 * Verifies that requesting an import attribute from a non-import node throws.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWrongNodeTypeForImportAttribute()
	{
		try (NodeArena arena = new NodeArena())
		{
			NodeIndex index = arena.allocateNode(NodeType.INTEGER_LITERAL, 0, 5);
			arena.getImportAttribute(index);
		}
	}

	/**
	 * Verifies that multiple nodes can have independent attributes.
	 */
	@Test
	public void shouldStoreIndependentAttributesForMultipleNodes()
	{
		try (NodeArena arena = new NodeArena())
		{
			ImportAttribute attr1 = new ImportAttribute("java.util.List");
			ImportAttribute attr2 = new ImportAttribute("java.util.Map");

			NodeIndex index1 = arena.allocateImportDeclaration(0, 25, attr1);
			NodeIndex index2 = arena.allocateImportDeclaration(26, 50, attr2);

			ImportAttribute retrieved1 = arena.getImportAttribute(index1);
			ImportAttribute retrieved2 = arena.getImportAttribute(index2);

			requireThat(retrieved1.qualifiedName(), "first.qualifiedName").isEqualTo("java.util.List");
			requireThat(retrieved2.qualifiedName(), "second.qualifiedName").isEqualTo("java.util.Map");
		}
	}

	/**
	 * Verifies that attributes are preserved when the arena grows.
	 */
	@Test
	public void shouldPreserveAttributesAfterArenaGrowth()
	{
		try (NodeArena arena = new NodeArena(2))
		{
			ImportAttribute attribute = new ImportAttribute("java.util.List");
			NodeIndex index = arena.allocateImportDeclaration(0, 25, attribute);

			// Allocate enough nodes to trigger growth
			arena.allocateNode(NodeType.INTEGER_LITERAL, 26, 30);
			arena.allocateNode(NodeType.INTEGER_LITERAL, 31, 35);

			// Arena should have grown
			requireThat(arena.getCapacity(), "capacity").isGreaterThan(2);

			// Attribute should still be retrievable
			ImportAttribute retrieved = arena.getImportAttribute(index);
			requireThat(retrieved.qualifiedName(), "qualifiedName").isEqualTo("java.util.List");
		}
	}

	/**
	 * Verifies that attributes are correctly associated with their nodes.
	 */
	@Test
	public void shouldRetrieveAttributesFromCorrectNodes()
	{
		try (NodeArena arena = new NodeArena())
		{
			ImportAttribute attribute = new ImportAttribute("java.util.List");
			NodeIndex importNode = arena.allocateImportDeclaration(0, 25, attribute);

			// Should successfully retrieve attribute from import node
			ImportAttribute retrieved = arena.getImportAttribute(importNode);
			requireThat(retrieved.qualifiedName(), "qualifiedName").isEqualTo("java.util.List");
		}
	}

	/**
	 * Verifies that requesting the wrong attribute type throws.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWrongAttributeType()
	{
		try (NodeArena arena = new NodeArena())
		{
			ImportAttribute attribute = new ImportAttribute("java.util.List");
			NodeIndex index = arena.allocateImportDeclaration(0, 25, attribute);

			// Requesting PackageAttribute on an import node should throw
			arena.getPackageAttribute(index);
		}
	}

	/**
	 * Verifies that TypeDeclarationAttribute can be stored and retrieved.
	 */
	@Test
	public void shouldStoreAndRetrieveTypeDeclarationAttribute()
	{
		try (NodeArena arena = new NodeArena())
		{
			TypeDeclarationAttribute attribute = new TypeDeclarationAttribute("MyClass");
			NodeIndex index = arena.allocateClassDeclaration(0, 50, attribute);

			TypeDeclarationAttribute retrieved = arena.getTypeDeclarationAttribute(index);
			requireThat(retrieved.typeName(), "typeName").isEqualTo("MyClass");
		}
	}
}
