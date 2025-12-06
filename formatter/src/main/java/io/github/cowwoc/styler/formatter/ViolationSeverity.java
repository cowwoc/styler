package io.github.cowwoc.styler.formatter;

/**
 * Severity levels for formatting violations.
 * <p>
 * Ordered from least severe (INFO) to most severe (ERROR). The natural ordering via
 * {@link #compareTo(ViolationSeverity)} reflects severity precedence: {@code ERROR > WARNING > INFO}.
 * <p>
 * Each severity has an associated weight for priority calculations:
 * <ul>
 *   <li>{@link #ERROR} = 10</li>
 *   <li>{@link #WARNING} = 5</li>
 *   <li>{@link #INFO} = 1</li>
 * </ul>
 */
public enum ViolationSeverity
{
	/**
	 * Informational suggestion for improvement.
	 * Examples: style preferences, optional enhancements.
	 */
	INFO(1),

	/**
	 * Issue that should be fixed but does not break the code.
	 * Examples: line length violations, improper indentation.
	 */
	WARNING(5),

	/**
	 * Critical issue that must be fixed.
	 * Examples: syntax-breaking changes, semantic alterations.
	 */
	ERROR(10);

	private final int weight;

	ViolationSeverity(int weight)
	{
		this.weight = weight;
	}

	/**
	 * Returns the weight multiplier for priority calculations.
	 *
	 * @return the weight (ERROR=10, WARNING=5, INFO=1)
	 */
	public int weight()
	{
		return weight;
	}
}
