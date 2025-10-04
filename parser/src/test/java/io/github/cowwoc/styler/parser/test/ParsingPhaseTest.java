package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ParsingPhase;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link ParsingPhase} enum.
 * Validates all 8 semantic parsing phases and their descriptions.
 */
public final class ParsingPhaseTest
{
	/**
	 * Verifies that all parsing phases have non-null, non-empty descriptions.
	 */
	@Test
	public void getDescriptionForAllPhasesReturnsNonNullDescription()
	{
		for (ParsingPhase phase : ParsingPhase.values())
		{
			String description = phase.getDescription();
			requireThat(description, "description_" + phase.name()).isNotNull();
			requireThat(description.isEmpty(), "isEmpty_" + phase.name()).isFalse();
		}
	}

	/**
	 * Verifies that the ParsingPhase enum contains exactly 8 phases.
	 */
	@Test
	public void valuesReturnsAllPhasesContainsEightPhases()
	{
		ParsingPhase[] phases = ParsingPhase.values();
		requireThat(phases.length, "phases.length").isEqualTo(8);
	}

	/**
	 * Verifies that valueOf returns the correct phase constant for a valid phase name.
	 */
	@Test
	public void valueOfWithValidPhaseNameReturnsCorrectPhase()
	{
		ParsingPhase phase = ParsingPhase.valueOf("CONSTRUCTOR_BODY");
		requireThat(phase, "phase").isEqualTo(ParsingPhase.CONSTRUCTOR_BODY);
	}

	/**
	 * Verifies that phase constants have their expected human-readable descriptions.
	 */
	@Test
	public void phaseConstantsHaveExpectedDescriptions()
	{
		requireThat(ParsingPhase.TOP_LEVEL.getDescription(), "TOP_LEVEL_description").
			isEqualTo("Top-level declarations");
		requireThat(ParsingPhase.CLASS_BODY.getDescription(), "CLASS_BODY_description").
			isEqualTo("Class body");
		requireThat(ParsingPhase.CONSTRUCTOR_BODY.getDescription(), "CONSTRUCTOR_BODY_description").
			isEqualTo("Constructor body");
		requireThat(ParsingPhase.METHOD_BODY.getDescription(), "METHOD_BODY_description").
			isEqualTo("Method body");
	}

	/**
	 * Verifies that enum constants compare equal when retrieved through different methods.
	 */
	@Test
	public void phaseEqualitySamePhaseReturnsTrue()
	{
		ParsingPhase phase1 = ParsingPhase.CONSTRUCTOR_BODY;
		ParsingPhase phase2 = ParsingPhase.valueOf("CONSTRUCTOR_BODY");
		requireThat(phase1, "phase1").isEqualTo(phase2);
	}

	/**
	 * Verifies that enum ordering matches the declaration order in the source code.
	 */
	@Test
	public void enumOrderingMaintainsDeclarationOrder()
	{
		ParsingPhase[] phases = ParsingPhase.values();
		requireThat(phases[0], "firstPhase").isEqualTo(ParsingPhase.TOP_LEVEL);
		requireThat(phases[1], "secondPhase").isEqualTo(ParsingPhase.CLASS_BODY);
	}

	/**
	 * Verifies that all enum constants are unique with no duplicates.
	 */
	@Test
	public void allPhasesUniqueNoDuplicates()
	{
		ParsingPhase[] phases = ParsingPhase.values();
		for (int i = 0; i < phases.length; i += 1)
		{
			for (int j = i + 1; j < phases.length; j += 1)
			{
				requireThat(phases[i], "phase_" + i + "_vs_" + j).isNotEqualTo(phases[j]);
			}
		}
	}
}
