package io.github.cowwoc.styler.formatter.impl;

/**
 * Indentation mode for Java source code formatting.
 * <p>
 * This enum defines the three standard indentation modes supported by the
 * indentation formatter. Each mode has specific use cases and trade-offs:
 * <ul>
 *   <li>{@link #SPACES} - Uses only space characters for indentation. This is the most
 *       portable option and displays consistently across all editors and viewers.</li>
 *   <li>{@link #TABS} - Uses only tab characters for indentation. This allows developers
 *       to configure their preferred visual width in their editor while maintaining
 *       compact file sizes.</li>
 *   <li>{@link #MIXED} - Uses tabs for structural indentation and spaces for alignment.
 *       This combines the configurability of tabs with the precision of spaces for
 *       alignment within a line.</li>
 * </ul>
 * <p>
 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.6">JLS §3.6</a>,
 * both space (U+0020) and horizontal tab (U+0009) are white space characters.
 */
public enum IndentationMode
{
	/**
	 * Use only space characters for all indentation.
	 * <p>
	 * This mode ensures consistent visual appearance across all editors and platforms
	 * but does not allow per-developer width customization.
	 */
	SPACES,

	/**
	 * Use only tab characters for all indentation.
	 * <p>
	 * This mode allows developers to configure their preferred tab width in their
	 * editor while keeping structural indentation intact. However, mixing tabs with
	 * spaces for alignment can cause visual inconsistencies.
	 */
	TABS,

	/**
	 * Use tabs for structural indentation and spaces for alignment.
	 * <p>
	 * This mode uses tab characters to represent each indentation level (e.g., class body,
	 * method body) and space characters for fine-grained alignment within a line (e.g.,
	 * aligning method parameters or array elements). This provides the configurability
	 * of tabs while maintaining precise alignment.
	 * <p>
	 * Example with tabWidth=4:
	 * <pre>{@code
	 * class Example {
	 * →   void method(String param1,
	 * →   ············String param2) {  // Tab + 12 spaces for alignment
	 * →   →   statement();               // 2 tabs for nesting
	 * →   }
	 * }
	 * }</pre>
	 */
	MIXED
}
