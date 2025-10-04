package io.github.cowwoc.styler.formatter.api;

/**
 * Brace placement styles for Java code formatting.
 *
 * <p>Defines industry-standard brace positioning conventions used across different coding
 * style guides and organizations.
 *
 * <p><strong>Style Definitions:</strong>
 * <ul>
 *   <li><strong>K&R (Kernighan & Ritchie)</strong>: Opening brace on same line as declaration,
 *       closing brace on new line aligned with declaration. Commonly used in C-style languages.</li>
 *   <li><strong>Allman</strong>: Opening and closing braces on separate lines, both aligned with
 *       the declaration indentation. Named after Eric Allman, popular in C# and other languages.</li>
 *   <li><strong>GNU</strong>: Opening brace on new line, indented one level from declaration.
 *       Closing brace aligned with opening brace. Used in GNU project coding standards.</li>
 * </ul>
 *
 * <p><strong>Example - K&R Style:</strong>
 * <pre>{@code
 * public void method() {
 *     statement;
 * }
 * }</pre>
 *
 * <p><strong>Example - Allman Style:</strong>
 * <pre>{@code
 * public void method()
 * {
 *     statement;
 * }
 * }</pre>
 *
 * <p><strong>Example - GNU Style:</strong>
 * <pre>{@code
 * public void method()
 *   {
 *     statement;
 *   }
 * }</pre>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Indentation_style">Wikipedia: Indentation Style</a>
 * @see BraceFormatterRuleConfiguration
 */
public enum BraceStyle
{
	/**
	 * Kernighan & Ritchie style: opening brace on same line as declaration.
	 *
	 * <p>Opening brace appears on the same line as the declaration or control structure,
	 * preceded by a single space. Closing brace on a new line aligned with the start of the
	 * declaration.
	 *
	 * <p><strong>Example:</strong>
	 * <pre>{@code
	 * class Example {
	 *     void method() {
	 *         if (condition) {
	 *             statement;
	 *         }
	 *     }
	 * }
	 * }</pre>
	 */
	K_AND_R,

	/**
	 * Allman style: opening brace on new line, same indentation as declaration.
	 *
	 * <p>Both opening and closing braces are placed on separate lines, aligned with the
	 * indentation level of the declaration or control structure.
	 *
	 * <p><strong>Example:</strong>
	 * <pre>{@code
	 * class Example
	 * {
	 *     void method()
	 *     {
	 *         if (condition)
	 *         {
	 *             statement;
	 *         }
	 *     }
	 * }
	 * }</pre>
	 */
	ALLMAN,

	/**
	 * GNU style: opening brace on new line, indented one level from declaration.
	 *
	 * <p>Opening brace is placed on a new line and indented one level from the declaration.
	 * Closing brace is aligned with the opening brace. Block contents are indented relative
	 * to the opening brace.
	 *
	 * <p><strong>Example:</strong>
	 * <pre>{@code
	 * class Example
	 *   {
	 *     void method()
	 *       {
	 *         if (condition)
	 *           {
	 *             statement;
	 *           }
	 *       }
	 *   }
	 * }</pre>
	 */
	GNU
}
