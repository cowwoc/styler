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
	@Test
	public void getDescription_forAllPhases_returnsNonNullDescription()
	{
		for (ParsingPhase phase : ParsingPhase.values())
		{
			String description = phase.getDescription();
			requireThat(description, "description_" + phase.name()).isNotNull();
			requireThat(description.isEmpty(), "isEmpty_" + phase.name()).isFalse();
		}
	}

	@Test
	public void values_returnsAllPhases_containsEightPhases()
	{
		ParsingPhase[] phases = ParsingPhase.values();
		requireThat(phases.length, "phases.length").isEqualTo(8);
	}

	@Test
	public void valueOf_withValidPhaseName_returnsCorrectPhase()
	{
		ParsingPhase phase = ParsingPhase.valueOf("CONSTRUCTOR_BODY");
		requireThat(phase, "phase").isEqualTo(ParsingPhase.CONSTRUCTOR_BODY);
	}

	@Test
	public void phaseConstants_haveExpectedDescriptions()
	{
		requireThat(ParsingPhase.TOP_LEVEL.getDescription(), "TOP_LEVEL_description")
			.isEqualTo("Top-level declarations");
		requireThat(ParsingPhase.CLASS_BODY.getDescription(), "CLASS_BODY_description")
			.isEqualTo("Class body");
		requireThat(ParsingPhase.CONSTRUCTOR_BODY.getDescription(), "CONSTRUCTOR_BODY_description")
			.isEqualTo("Constructor body");
		requireThat(ParsingPhase.METHOD_BODY.getDescription(), "METHOD_BODY_description")
			.isEqualTo("Method body");
	}

	@Test
	public void phaseEquality_samePhase_returnsTrue()
	{
		ParsingPhase phase1 = ParsingPhase.CONSTRUCTOR_BODY;
		ParsingPhase phase2 = ParsingPhase.valueOf("CONSTRUCTOR_BODY");
		requireThat(phase1, "phase1").isEqualTo(phase2);
	}

	@Test
	public void enumOrdering_maintainsDeclarationOrder()
	{
		ParsingPhase[] phases = ParsingPhase.values();
		requireThat(phases[0], "firstPhase").isEqualTo(ParsingPhase.TOP_LEVEL);
		requireThat(phases[1], "secondPhase").isEqualTo(ParsingPhase.CLASS_BODY);
	}

	@Test
	public void allPhasesUnique_noDuplicates()
	{
		ParsingPhase[] phases = ParsingPhase.values();
		for (int i = 0; i < phases.length; i++)
		{
			for (int j = i + 1; j < phases.length; j++)
			{
				requireThat(phases[i], "phase_" + i + "_vs_" + j).isNotEqualTo(phases[j]);
			}
		}
	}
}
