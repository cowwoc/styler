package io.github.cowwoc.styler.formatter.impl;

/**
 * Strategy interface for analyzing brace placement according to different coding styles.
 *
 * <p>This interface defines the contract for brace style implementations (K&amp;R, Allman, GNU, etc.).
 * Each strategy determines whether opening and closing braces comply with its specific placement rules.
 *
 * <p><strong>Supported Styles:</strong>
 * <ul>
 *   <li><strong>K&amp;R (Kernighan &amp; Ritchie):</strong> Opening brace on same line,
 *   closing aligned with declaration</li>
 *   <li><strong>Allman:</strong> Both braces on new lines, same indentation as declaration</li>
 *   <li><strong>GNU:</strong> Opening brace indented one level from declaration, closing aligned with declaration</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> Implementations must be stateless and thread-safe.
 *
 * @see KAndRStrategy
 * @see AllmanStrategy
 * @see GnuStrategy
 */
public interface BraceStyleStrategy
{
	/**
	 * Analyzes opening brace placement for compliance with this style.
	 *
	 * <p>This method determines if the opening brace is positioned correctly according to the style's rules.
	 * For example, K&amp;R requires opening braces on the same line as the declaration, while Allman requires
	 * them on a new line.
	 *
	 * @param context the brace context containing position information, never {@code null}
	 * @param sourceText the source code text, never {@code null}
	 * @param indentationCalculator calculator for determining proper indentation levels, never {@code null}
	 * @return {@code true} if opening brace placement is correct, {@code false} if it violates the style
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	boolean isOpeningBraceCorrect(BraceContext context, String sourceText,
		IndentationCalculator indentationCalculator);

	/**
	 * Analyzes closing brace placement for compliance with this style.
	 *
	 * <p>This method determines if the closing brace is positioned correctly according to the style's rules.
	 * Most styles require closing braces to be aligned with the declaration that opened the block.
	 *
	 * @param context the brace context containing position information, never {@code null}
	 * @param sourceText the source code text, never {@code null}
	 * @param indentationCalculator calculator for determining proper indentation levels, never {@code null}
	 * @return {@code true} if closing brace placement is correct, {@code false} if it violates the style
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	boolean isClosingBraceCorrect(BraceContext context, String sourceText,
		IndentationCalculator indentationCalculator);

	/**
	 * Returns the name of this brace style.
	 *
	 * @return style name (e.g., {@code "K&R"}, {@code "Allman"}, {@code "GNU"})
	 */
	String getStyleName();

	/**
	 * Returns a human-readable description of the expected opening brace placement.
	 *
	 * @return description of opening brace placement (e.g., {@code "same line as declaration"})
	 */
	String getOpeningBraceDescription();

	/**
	 * Returns a human-readable description of the expected closing brace placement.
	 *
	 * @return description of closing brace placement (e.g., {@code "aligned with declaration"})
	 */
	String getClosingBraceDescription();
}
