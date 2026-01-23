package io.github.cowwoc.styler.formatter;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * An example demonstrating correct and incorrect code for a formatting rule.
 *
 * @param description a brief description of what this example demonstrates
 * @param incorrect   the incorrect code that violates the rule
 * @param correct     the correct code that follows the rule
 */
public record RuleExample(String description, String incorrect, String correct)
{
	/**
	 * Creates a new rule example.
	 *
	 * @param description a brief description of what this example demonstrates
	 * @param incorrect   the incorrect code that violates the rule
	 * @param correct     the correct code that follows the rule
	 * @throws NullPointerException     if any argument is null
	 * @throws IllegalArgumentException if any argument is empty
	 */
	public RuleExample
	{
		requireThat(description, "description").isNotEmpty();
		requireThat(incorrect, "incorrect").isNotEmpty();
		requireThat(correct, "correct").isNotEmpty();
	}
}
