package io.github.cowwoc.styler.formatter.api.plugin;

import io.github.cowwoc.styler.formatter.api.FormattingRule;
import io.github.cowwoc.styler.formatter.api.MutableFormattingContext;

/**
 * A formatting rule that can modify the AST through a mutable context.
 * <p>
 * This interface enables direct AST transformation for optimal single-thread performance. Rules implementing
 * this interface can directly modify the AST using the provided context.
 */
public interface MutableFormattingRule extends FormattingRule
{
	/**
	 * Applies formatting transformations to the AST using the mutable context.
	 * <p>
	 * Rules can directly modify the AST through the context methods: - setRootNode() to replace the entire AST
	 * - replaceChild() to replace specific nodes - insertBefore()/insertAfter() to add new nodes -
	 * removeChild() to remove nodes - setWhitespace()/setComments() to adjust formatting
	 *
	 * @param context the mutable formatting {@code context} providing AST access and modification methods
	 */
	void format(MutableFormattingContext context);
}