package io.github.cowwoc.styler.errorcatalog.test;

import io.github.cowwoc.styler.errorcatalog.FixSuggestion;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link FixSuggestion} record.
 */
public final class FixSuggestionTest
{
	/**
	 * Tests successful creation with valid description and steps.
	 */
	@Test
	void shouldCreateWithValidData()
	{
		String descriptionValue = "Fix the syntax error";
		List<String> stepsValue = List.of("Check line 42", "Add semicolon");

		FixSuggestion suggestion = new FixSuggestion(descriptionValue, stepsValue);

		requireThat(suggestion.description(), "description").isEqualTo(descriptionValue);
		requireThat(suggestion.steps(), "steps").isEqualTo(stepsValue);
	}

	/**
	 * Tests that null description is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullDescription()
	{
		List<String> steps = List.of("Step 1");
		new FixSuggestion(null, steps);
	}

	/**
	 * Tests that empty description is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectEmptyDescription()
	{
		List<String> steps = List.of("Step 1");
		new FixSuggestion("", steps);
	}

	/**
	 * Tests that null steps list is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullSteps()
	{
		new FixSuggestion("Description", null);
	}

	/**
	 * Tests that empty steps list is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectEmptySteps()
	{
		new FixSuggestion("Description", List.of());
	}
}
