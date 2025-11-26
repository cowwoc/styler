package io.github.cowwoc.styler.formatter;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Default immutable implementation of FixStrategy.
 *
 * @param description      a human-readable description of the fix
 * @param isAutoApplicable whether the fix can be applied automatically
 * @param replacementText  the text to replace the violating code with
 * @param replacementStart the start position of the replacement range
 * @param replacementEnd   the end position of the replacement range
 */
public record DefaultFixStrategy(
	String description,
	boolean isAutoApplicable,
	String replacementText,
	int replacementStart,
	int replacementEnd) implements FixStrategy
{
	/**
	 * Creates a fix strategy with validated parameters.
	 */
	public DefaultFixStrategy
	{
		requireThat(description, "description").isNotEmpty().isStripped();
		requireThat(replacementText, "replacementText").isStripped();
		requireThat(replacementStart, "replacementStart").isNotNegative();
		requireThat(replacementEnd, "replacementEnd").isGreaterThanOrEqualTo(replacementStart);
	}
}
