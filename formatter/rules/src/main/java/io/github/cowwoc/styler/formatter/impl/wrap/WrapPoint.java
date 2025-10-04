package io.github.cowwoc.styler.formatter.impl.wrap;

import io.github.cowwoc.styler.ast.SourcePosition;

import java.util.Objects;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents a potential location where a line can be wrapped to meet length constraints.
 * <p>
 * Wrap points are identified by their position in source code and assigned a
 * priority based on semantic significance. Higher priority wrap points produce
 * more readable wrapped code.
 * <p>
 * Wrap points are detected by {@link WrapPointDetector} based on syntactic analysis
 * of source code.
 */
public final class WrapPoint implements Comparable<WrapPoint>
{
	/**
	 * Priority levels for wrap point selection.
	 * <p>
	 * Higher priority wrap points are preferred when multiple options exist.
	 */
	public enum Priority
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
		 * Highest priority for method chain wrap points.
		 */
		METHOD_CHAIN(10);

		private final int value;

		Priority(int value)
		{
			this.value = value;
		}

		public int getValue()
		{
			return value;
		}
	}

	private final SourcePosition position;
	private final Priority priority;
	private final String context;

	/**
	 * Creates a new wrap point.
	 *
	 * @param position the source position where the wrap can occur, never {@code null}
	 * @param priority the priority level for this wrap point, never {@code null}
	 * @param context descriptive context about this wrap point, never {@code null}
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	public WrapPoint(SourcePosition position, Priority priority, String context)
	{
		requireThat(position, "position").isNotNull();
		requireThat(priority, "priority").isNotNull();
		requireThat(context, "context").isNotNull();

		this.position = position;
		this.priority = priority;
		this.context = context;
	}

	/**
	 * Returns the source position of this wrap point.
	 *
	 * @return the position, never {@code null}
	 */
	public SourcePosition getPosition()
	{
		return position;
	}

	/**
	 * Returns the priority of this wrap point.
	 *
	 * @return the priority, never {@code null}
	 */
	public Priority getPriority()
	{
		return priority;
	}

	/**
	 * Returns the context description for this wrap point.
	 *
	 * @return the context, never {@code null}
	 */
	public String getContext()
	{
		return context;
	}

	@Override
	public int compareTo(WrapPoint other)
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

		WrapPoint that = (WrapPoint) obj;
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
		return String.format("WrapPoint[position=%s, priority=%s, context='%s']",
			position, priority, context);
	}
}
