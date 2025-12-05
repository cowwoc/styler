package io.github.cowwoc.styler.errorcatalog.test;

import io.github.cowwoc.styler.errorcatalog.Audience;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link Audience} detection logic.
 */
public final class AudienceDetectionTest
{
	/**
	 * Tests that Audience.detect() returns a valid audience.
	 */
	@Test
	void shouldDetectValidAudience()
	{
		Audience detected = Audience.detect();

		requireThat(detected, "detected").isNotNull();
		assertTrue(detected == Audience.AI || detected == Audience.HUMAN,
			"detected must be AI or HUMAN");
	}
}
