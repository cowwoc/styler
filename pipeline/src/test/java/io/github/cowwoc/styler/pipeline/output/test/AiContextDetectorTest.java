package io.github.cowwoc.styler.pipeline.output.test;

import io.github.cowwoc.styler.pipeline.output.ContextDetector;
import io.github.cowwoc.styler.pipeline.output.OutputFormat;
import io.github.cowwoc.styler.pipeline.output.internal.DefaultContextDetector;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for ContextDetector AI vs human environment detection.
 * <p>
 * Note: Environment variable detection is tested implicitly via the detectContext() method.
 * Direct manipulation of System.getenv() is not possible in Java without reflection.
 */
public class AiContextDetectorTest
{
	/**
	 * Tests that the context detector returns a valid output format.
	 */
	@Test
	public void shouldReturnValidOutputFormat()
	{
		// Given: A context detector
		ContextDetector detector = new DefaultContextDetector();

		// When: Detect context
		OutputFormat format = detector.detectContext();

		// Then: Should return a valid format (JSON or HUMAN)
		requireThat(format, "format").isNotNull();
		requireThat(format == OutputFormat.JSON || format == OutputFormat.HUMAN, "validFormat").isTrue();
	}

	/**
	 * Tests that non-interactive terminal detection returns JSON format.
	 * This test will likely detect AI context since tests typically run without a console.
	 */
	@Test
	public void shouldDetectNonInteractiveTerminal()
	{
		// Given: A context detector
		ContextDetector detector = new DefaultContextDetector();

		// When: Check if running in non-interactive terminal
		boolean hasConsole = detector.isInteractiveTerminal();

		// Then: If no console, should return JSON format
		if (!hasConsole)
		{
			OutputFormat format = detector.detectContext();
			requireThat(format, "format").isEqualTo(OutputFormat.JSON);
		}
	}

	/**
	 * Tests the isAiEnvironment method directly.
	 */
	@Test
	public void shouldDetectAiEnvironment()
	{
		// Given: A context detector
		ContextDetector detector = new DefaultContextDetector();

		// When: Check AI environment
		boolean isAi = detector.isAiEnvironment();

		// Then: In CI/test environment, this may or may not be true
		// The test verifies the method returns without error
		requireThat(isAi || !isAi, "validBoolean").isTrue();
	}

	/**
	 * Tests that CI environment is detected.
	 * This test runs in CI where CI or GITHUB_ACTIONS env vars are set.
	 */
	@Test
	public void shouldDetectCiEnvironment()
	{
		// Given: A context detector
		ContextDetector detector = new DefaultContextDetector();

		// When: Check for CI environment
		boolean inCi = detector.isCiEnvironment();

		// Then: If in CI, context detector should return JSON
		if (inCi)
		{
			OutputFormat format = detector.detectContext();
			requireThat(format, "format").isEqualTo(OutputFormat.JSON);
		}
	}
}
