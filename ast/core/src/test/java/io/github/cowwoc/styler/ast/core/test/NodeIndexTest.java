package io.github.cowwoc.styler.ast.core.test;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Thread-safe tests for NodeIndex.
 */
public class NodeIndexTest
{
	/**
	 * Tests that NodeIndex can be constructed with index 0.
	 */
	@Test
	public void testValidIndex()
	{
		NodeIndex index = new NodeIndex(0);
		requireThat(index.index(), "index()").isEqualTo(0);
	}

	/**
	 * Tests that NodeIndex can handle large index values.
	 */
	@Test
	public void testValidLargeIndex()
	{
		NodeIndex index = new NodeIndex(1_000_000);
		requireThat(index.index(), "index()").isEqualTo(1_000_000);
	}

	/**
	 * Tests that NodeIndex rejects negative index -1.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidIndexNegativeOne()
	{
		new NodeIndex(-1);
	}

	/**
	 * Tests that NodeIndex rejects index -2 as invalid.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidIndexNegativeTwo()
	{
		new NodeIndex(-2);
	}

	/**
	 * Tests that NodeIndex rejects large negative indices.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidIndexNegativeHundred()
	{
		new NodeIndex(-100);
	}

	/**
	 * Tests that a NodeIndex equals itself (reflexive property).
	 */
	@Test
	public void testEqualityReflexive()
	{
		NodeIndex index = new NodeIndex(5);
		requireThat(index, "index").isEqualTo(index);
	}

	/**
	 * Tests that equality is symmetric (a equals b implies b equals a).
	 */
	@Test
	public void testEqualitySymmetric()
	{
		NodeIndex index1 = new NodeIndex(10);
		NodeIndex index2 = new NodeIndex(10);
		requireThat(index1, "index1").isEqualTo(index2);
		requireThat(index2, "index2").isEqualTo(index1);
	}

	/**
	 * Tests that equality is transitive (a equals b and b equals c implies a equals c).
	 */
	@Test
	public void testEqualityTransitive()
	{
		NodeIndex index1 = new NodeIndex(15);
		NodeIndex index2 = new NodeIndex(15);
		NodeIndex index3 = new NodeIndex(15);
		requireThat(index1, "index1").isEqualTo(index2);
		requireThat(index2, "index2").isEqualTo(index3);
		requireThat(index1, "index1").isEqualTo(index3);
	}

	/**
	 * Tests that NodeIndex instances with different indices are not equal.
	 */
	@Test
	public void testInequality()
	{
		NodeIndex index1 = new NodeIndex(5);
		NodeIndex index2 = new NodeIndex(10);
		requireThat(index1, "index1").isNotEqualTo(index2);
	}

	/**
	 * Tests that equal NodeIndex instances have equal hash codes.
	 */
	@Test
	public void testHashCodeConsistency()
	{
		NodeIndex index1 = new NodeIndex(20);
		NodeIndex index2 = new NodeIndex(20);
		requireThat(index1.hashCode(), "hashCode()").isEqualTo(index2.hashCode());
	}

	/**
	 * Tests that different NodeIndex instances have different hash codes.
	 */
	@Test
	public void testHashCodeDifference()
	{
		NodeIndex index1 = new NodeIndex(20);
		NodeIndex index2 = new NodeIndex(30);
		// Not guaranteed but highly likely
		requireThat(index1.hashCode(), "index1.hashCode()").isNotEqualTo(index2.hashCode());
	}

	/**
	 * Tests toString() format for valid indices.
	 */
	@Test
	public void testToString()
	{
		NodeIndex index = new NodeIndex(42);
		requireThat(index.toString(), "toString()").isEqualTo("NodeIndex[index=42]");
	}

	/**
	 * Tests that NodeIndex record is immutable and returns consistent values.
	 */
	@Test
	public void testRecordImmutability()
	{
		NodeIndex index = new NodeIndex(100);
		requireThat(index.index(), "index()").isEqualTo(100);

		// Record is immutable, index() always returns the same value
		requireThat(index.index(), "index()").isEqualTo(100);
		requireThat(index.index(), "index()").isEqualTo(100);
	}
}
