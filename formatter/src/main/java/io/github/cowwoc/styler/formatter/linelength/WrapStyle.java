package io.github.cowwoc.styler.formatter.linelength;

/**
 * Wrapping style for breaking long lines.
 * Specifies where to place the line break relative to a separator (dot, comma, operator, etc.).
 * The specific separator type is determined by the configuration field using this style.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public enum WrapStyle
{
	/**
	 * Break before the separator.
	 * Example for method chains: {@code obj.method1()\n    .method2()}.
	 * Example for binary expressions: {@code value1\n    + value2}.
	 */
	BEFORE,

	/**
	 * Break after the separator.
	 * Example for method chains: {@code obj.method1().\n    method2()}.
	 * Example for arguments: {@code method(arg1,\n    arg2)}.
	 */
	AFTER,

	/**
	 * Place each item on its own line.
	 * Example: {@code method(\n    arg1,\n    arg2\n)}.
	 */
	ONE_PER_LINE,

	/**
	 * Never wrap - keep on the same line regardless of length.
	 */
	NEVER
}
