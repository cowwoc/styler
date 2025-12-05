package io.github.cowwoc.styler.errorcatalog;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Actionable fix suggestion for an error.
 *
 * @param description brief description of the fix approach
 * @param steps       ordered list of specific steps to apply the fix
 */
public record FixSuggestion(String description, List<String> steps)
{
	/**
	 * Creates a new fix suggestion.
	 *
	 * @param description brief description of the fix approach
	 * @param steps       ordered list of specific steps to apply the fix
	 * @throws NullPointerException     if any argument is null
	 * @throws IllegalArgumentException if description is empty or steps list is empty
	 */
	public FixSuggestion
	{
		requireThat(description, "description").isNotEmpty();
		requireThat(steps, "steps").isNotEmpty();
	}
}
