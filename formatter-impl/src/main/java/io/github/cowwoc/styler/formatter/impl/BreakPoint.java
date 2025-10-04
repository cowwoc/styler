package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.SourcePosition;

import java.util.Objects;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents a potential location where a line can be broken for wrapping.
 * <p>
 * Break points are identified by their position in source code and assigned a
 * priority based on semantic significance. Higher priority break points produce
 * more readable wrapped code.
 */
final class BreakPoint implements Comparable<BreakPoint>
{
	/**
	 * Priority levels for break point selection.
	 * <p>
	 * Higher priority break points are preferred when multiple options exist.
	 */
	enum Priority
	{
		/**
		 * Low priority for basic whitespace between tokens.
		 */
		WHITESPACE(1),

		/**
		 * Medium priority for operators and punctuation.
		 */
		OPERATOR(5),

		/**
		 * High priority for parameter list separators.
		 */
		PARAMETER(8),

		/**
		 * Highest priority for method chain break points.
		 */
		METHOD_CHAIN(10);

		private final int value;

		Priority(int value)
		{
			this.value = value;
		}

		int getValue()
		{
			return value;
		}
	}

	private final SourcePosition position;
	private final Priority priority;
	private final String context;

	/**
	 * Creates a new break point.
	 *
	 * @param position the source position where the break can occur, never {@code null}
	 * @param priority the priority level for this break point, never {@code null}
	 * @param context descriptive context about this break point, never {@code null}
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	BreakPoint(SourcePosition position, Priority priority, String context)
	{
		requireThat(position, "position").isNotNull();
		requireThat(priority, "priority").isNotNull();
		requireThat(context, "context").isNotNull();

		this.position = position;
		this.priority = priority;
		this.context = context;
	}

	/**
	 * Returns the source position of this break point.
	 *
	 * @return the position, never {@code null}
	 */
	SourcePosition getPosition()
	{
		return position;
	}

	/**
	 * Returns the priority of this break point.
	 *
	 * @return the priority, never {@code null}
	 */
	Priority getPriority()
	{
		return priority;
	}

	/**
	 * Returns the context description for this break point.
	 *
	 * @return the context, never {@code null}
	 */
	String getContext()
	{
		return context;
	}

	@Override
	public int compareTo(BreakPoint other)
	{
		int priorityComparison = Integer.compare(other.priority.value, this.priority.value);
		if (priorityComparison != 0)
		{
			return priorityComparison;
		}
		return position.compareTo(other.position);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		BreakPoint that = (BreakPoint) obj;
		return Objects.equals(position, that.position) &&
		       priority == that.priority &&
		       Objects.equals(context, that.context);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(position, priority, context);
	}

	@Override
	public String toString()
	{
		return String.format("BreakPoint[position=%s, priority=%s, context='%s']",
			position, priority, context);
	}
}
