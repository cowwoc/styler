package io.github.cowwoc.styler.formatter.impl.wrap.test;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.formatter.impl.wrap.WrapPoint;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link WrapPoint}.
 */
public class WrapPointTest
{
	/**
	 * Verifies WrapPoint constructor stores position, priority, and context correctly.
	 */
	@Test
	public void constructorStoresAllParameters()
	{
		SourcePosition position = new SourcePosition(10, 25);
		WrapPoint.Priority priority = WrapPoint.Priority.METHOD_CHAIN;
		String context = "method call at (10, 25)";

		WrapPoint wrapPoint = new WrapPoint(position, priority, context);

		assertThat(wrapPoint.getPosition()).isEqualTo(position);
		assertThat(wrapPoint.getPriority()).isEqualTo(priority);
		assertThat(wrapPoint.getContext()).isEqualTo(context);
	}

	/**
	 * Verifies Priority enum values have correct priority ordering from highest to lowest:
	 * METHOD_CHAIN (10), PARAMETER (8), OPERATOR (5), WHITESPACE (1).
	 */
	@Test
	public void priorityEnumHasCorrectValues()
	{
		assertThat(WrapPoint.Priority.METHOD_CHAIN.getValue()).isEqualTo(10);
		assertThat(WrapPoint.Priority.PARAMETER.getValue()).isEqualTo(8);
		assertThat(WrapPoint.Priority.OPERATOR.getValue()).isEqualTo(5);
		assertThat(WrapPoint.Priority.WHITESPACE.getValue()).isEqualTo(1);
	}

	/**
	 * Verifies compareTo() orders wrap points by priority (highest first), then by position.
	 * Higher priority wrap points should come before lower priority ones.
	 */
	@Test
	public void compareToOrdersByPriorityThenPosition()
	{
		WrapPoint methodChain = new WrapPoint(
			new SourcePosition(10, 20),
			WrapPoint.Priority.METHOD_CHAIN,
			"method");
		WrapPoint operator = new WrapPoint(
			new SourcePosition(10, 15),
			WrapPoint.Priority.OPERATOR,
			"operator");
		WrapPoint whitespace = new WrapPoint(
			new SourcePosition(10, 10),
			WrapPoint.Priority.WHITESPACE,
			"whitespace");

		List<WrapPoint> wrapPoints = new ArrayList<>();
		wrapPoints.add(whitespace);
		wrapPoints.add(methodChain);
		wrapPoints.add(operator);

		Collections.sort(wrapPoints);

		assertThat(wrapPoints.get(0)).isEqualTo(methodChain);
		assertThat(wrapPoints.get(1)).isEqualTo(operator);
		assertThat(wrapPoints.get(2)).isEqualTo(whitespace);
	}

	/**
	 * Verifies compareTo() orders wrap points by position when priorities are equal.
	 * Earlier positions should come before later positions.
	 */
	@Test
	public void compareToOrdersByPositionWhenPrioritiesEqual()
	{
		WrapPoint early = new WrapPoint(
			new SourcePosition(10, 10),
			WrapPoint.Priority.OPERATOR,
			"early");
		WrapPoint late = new WrapPoint(
			new SourcePosition(10, 20),
			WrapPoint.Priority.OPERATOR,
			"late");

		List<WrapPoint> wrapPoints = new ArrayList<>();
		wrapPoints.add(late);
		wrapPoints.add(early);

		Collections.sort(wrapPoints);

		assertThat(wrapPoints.get(0)).isEqualTo(early);
		assertThat(wrapPoints.get(1)).isEqualTo(late);
	}

	/**
	 * Verifies compareTo() returns negative value when this has higher priority than other.
	 */
	@Test
	public void compareToReturnsNegativeWhenThisHasHigherPriority()
	{
		WrapPoint methodChain = new WrapPoint(
			new SourcePosition(10, 20),
			WrapPoint.Priority.METHOD_CHAIN,
			"method");
		WrapPoint operator = new WrapPoint(
			new SourcePosition(10, 20),
			WrapPoint.Priority.OPERATOR,
			"operator");

		assertThat(methodChain.compareTo(operator)).isNegative();
	}

	/**
	 * Verifies compareTo() returns positive value when this has lower priority than other.
	 */
	@Test
	public void compareToReturnsPositiveWhenThisHasLowerPriority()
	{
		WrapPoint operator = new WrapPoint(
			new SourcePosition(10, 20),
			WrapPoint.Priority.OPERATOR,
			"operator");
		WrapPoint methodChain = new WrapPoint(
			new SourcePosition(10, 20),
			WrapPoint.Priority.METHOD_CHAIN,
			"method");

		assertThat(operator.compareTo(methodChain)).isPositive();
	}

	/**
	 * Verifies compareTo() returns zero when comparing a wrap point to itself.
	 */
	@Test
	public void compareToReturnsZeroWhenComparingToSelf()
	{
		WrapPoint wrapPoint = new WrapPoint(
			new SourcePosition(10, 20),
			WrapPoint.Priority.METHOD_CHAIN,
			"method");

		assertThat(wrapPoint.compareTo(wrapPoint)).isZero();
	}

	/**
	 * Verifies equals() returns true for two wrap points with identical values.
	 */
	@Test
	public void equalsWithIdenticalValuesReturnsTrue()
	{
		SourcePosition position = new SourcePosition(10, 25);
		WrapPoint wrapPoint1 = new WrapPoint(position, WrapPoint.Priority.OPERATOR, "test");
		WrapPoint wrapPoint2 = new WrapPoint(position, WrapPoint.Priority.OPERATOR, "test");

		assertThat(wrapPoint1).isEqualTo(wrapPoint2);
	}

	/**
	 * Verifies equals() returns false for wrap points with different positions.
	 */
	@Test
	public void equalsWithDifferentPositionsReturnsFalse()
	{
		WrapPoint wrapPoint1 = new WrapPoint(
			new SourcePosition(10, 20),
			WrapPoint.Priority.OPERATOR,
			"test");
		WrapPoint wrapPoint2 = new WrapPoint(
			new SourcePosition(10, 25),
			WrapPoint.Priority.OPERATOR,
			"test");

		assertThat(wrapPoint1).isNotEqualTo(wrapPoint2);
	}

	/**
	 * Verifies equals() returns false for wrap points with different priorities.
	 */
	@Test
	public void equalsWithDifferentPrioritiesReturnsFalse()
	{
		SourcePosition position = new SourcePosition(10, 25);
		WrapPoint wrapPoint1 = new WrapPoint(position, WrapPoint.Priority.OPERATOR, "test");
		WrapPoint wrapPoint2 = new WrapPoint(position, WrapPoint.Priority.METHOD_CHAIN, "test");

		assertThat(wrapPoint1).isNotEqualTo(wrapPoint2);
	}

	/**
	 * Verifies equals() returns false for wrap points with different contexts.
	 */
	@Test
	public void equalsWithDifferentContextsReturnsFalse()
	{
		SourcePosition position = new SourcePosition(10, 25);
		WrapPoint wrapPoint1 = new WrapPoint(position, WrapPoint.Priority.OPERATOR, "context1");
		WrapPoint wrapPoint2 = new WrapPoint(position, WrapPoint.Priority.OPERATOR, "context2");

		assertThat(wrapPoint1).isNotEqualTo(wrapPoint2);
	}

	/**
	 * Verifies equals() returns true when comparing a wrap point to itself (reflexive).
	 */
	@Test
	public void equalsWithSelfReturnsTrue()
	{
		WrapPoint wrapPoint = new WrapPoint(
			new SourcePosition(10, 25),
			WrapPoint.Priority.OPERATOR,
			"test");

		assertThat(wrapPoint).isEqualTo(wrapPoint);
	}

	/**
	 * Verifies equals() returns false when comparing to null.
	 */
	@Test
	public void equalsWithNullReturnsFalse()
	{
		WrapPoint wrapPoint = new WrapPoint(
			new SourcePosition(10, 25),
			WrapPoint.Priority.OPERATOR,
			"test");

		assertThat(wrapPoint).isNotEqualTo(null);
	}

	/**
	 * Verifies equals() returns false when comparing to a different class type.
	 */
	@Test
	public void equalsWithDifferentClassReturnsFalse()
	{
		WrapPoint wrapPoint = new WrapPoint(
			new SourcePosition(10, 25),
			WrapPoint.Priority.OPERATOR,
			"test");

		assertThat(wrapPoint).isNotEqualTo("not a WrapPoint");
	}

	/**
	 * Verifies hashCode() returns the same value for two wrap points with identical values.
	 */
	@Test
	public void hashCodeWithIdenticalValuesReturnsEqualHashCodes()
	{
		SourcePosition position = new SourcePosition(10, 25);
		WrapPoint wrapPoint1 = new WrapPoint(position, WrapPoint.Priority.OPERATOR, "test");
		WrapPoint wrapPoint2 = new WrapPoint(position, WrapPoint.Priority.OPERATOR, "test");

		assertThat(wrapPoint1.hashCode()).isEqualTo(wrapPoint2.hashCode());
	}

	/**
	 * Verifies toString() returns a string containing position, priority, and context information.
	 */
	@Test
	public void toStringContainsAllFields()
	{
		WrapPoint wrapPoint = new WrapPoint(
			new SourcePosition(10, 25),
			WrapPoint.Priority.OPERATOR,
			"operator '+' at (10, 25)");

		String result = wrapPoint.toString();

		assertThat(result).contains("position=");
		assertThat(result).contains("priority=OPERATOR");
		assertThat(result).contains("context='operator '+' at (10, 25)'");
	}

	/**
	 * Verifies compareTo() handles multi-line positions correctly, ordering by line first.
	 */
	@Test
	public void compareToOrdersByLineThenColumn()
	{
		WrapPoint line10 = new WrapPoint(
			new SourcePosition(10, 50),
			WrapPoint.Priority.OPERATOR,
			"line10");
		WrapPoint line20 = new WrapPoint(
			new SourcePosition(20, 10),
			WrapPoint.Priority.OPERATOR,
			"line20");

		List<WrapPoint> wrapPoints = new ArrayList<>();
		wrapPoints.add(line20);
		wrapPoints.add(line10);

		Collections.sort(wrapPoints);

		assertThat(wrapPoints.get(0)).isEqualTo(line10);
		assertThat(wrapPoints.get(1)).isEqualTo(line20);
	}
}
