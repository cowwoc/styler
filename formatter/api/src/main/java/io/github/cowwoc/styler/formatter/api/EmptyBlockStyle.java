package io.github.cowwoc.styler.formatter.api;

/**
 * Formatting styles for empty code blocks.
 *
 * <p>Defines how empty blocks (blocks containing no statements) should be formatted,
 * particularly concerning the positioning of opening and closing braces.
 *
 * <p><strong>Style Definitions:</strong>
 * <ul>
 *   <li><strong>SAME_LINE</strong>: Opening and closing braces on the same line, typically
 *       {@code {}}. Compact formatting for empty blocks.</li>
 *   <li><strong>NEW_LINE</strong>: Opening brace on one line, closing brace on the next line.
 *       Follows the same brace placement rules as non-empty blocks.</li>
 *   <li><strong>PRESERVE</strong>: Maintains the existing formatting of empty blocks without
 *       modification. Useful for codebases with mixed empty block styles.</li>
 * </ul>
 *
 * <p><strong>Example - SAME_LINE:</strong>
 * <pre>{@code
 * public void method() {}
 *
 * if (condition) {}
 * }</pre>
 *
 * <p><strong>Example - NEW_LINE with Allman Style:</strong>
 * <pre>{@code
 * public void method()
 * {
 * }
 *
 * if (condition)
 * {
 * }
 * }</pre>
 *
 * <p><strong>Example - PRESERVE:</strong>
 * <pre>{@code
 * // Mixed styles preserved as-is
 * public void method1() {}
 *
 * public void method2()
 * {
 * }
 * }</pre>
 *
 * @see BraceStyle
 * @see BraceFormatterRuleConfiguration
 */
public enum EmptyBlockStyle
{
	/**
	 * Format empty blocks with braces on the same line.
	 *
	 * <p>The opening and closing braces are placed on the same line with no space or newline
	 * between them, resulting in {@code {}}. This is the most compact formatting for empty
	 * blocks.
	 *
	 * <p><strong>Example:</strong>
	 * <pre>{@code
	 * public void emptyMethod() {}
	 *
	 * if (flag) {}
	 *
	 * try {} catch (Exception e) {}
	 * }</pre>
	 */
	SAME_LINE,

	/**
	 * Format empty blocks with braces on separate lines.
	 *
	 * <p>The opening and closing braces follow the same positioning rules as non-empty blocks,
	 * determined by the configured {@link BraceStyle}. The closing brace appears on a new line.
	 *
	 * <p><strong>Example (with Allman style):</strong>
	 * <pre>{@code
	 * public void emptyMethod()
	 * {
	 * }
	 *
	 * if (flag)
	 * {
	 * }
	 * }</pre>
	 */
	NEW_LINE,

	/**
	 * Preserve the existing formatting of empty blocks.
	 *
	 * <p>Empty blocks are not reformatted; their original brace positioning is maintained
	 * exactly as written. This option is useful when working with codebases that have
	 * intentionally mixed empty block styles or when empty block formatting is not a priority.
	 *
	 * <p><strong>Example:</strong>
	 * <pre>{@code
	 * // These remain unchanged regardless of configured BraceStyle
	 * public void method1() {}
	 *
	 * public void method2()
	 * {
	 * }
	 *
	 * public void method3() {
	 * }
	 * }</pre>
	 */
	PRESERVE
}
