package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.ExitCode;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Unit tests for {@link ExitCode} enum verifying meaningful relationships and invariants.
 * <p>
 * These tests focus on behavioral contracts rather than duplicating enum definitions. They verify:
 * <ul>
 * <li>Success codes ({@link ExitCode#SUCCESS}, {@link ExitCode#HELP}) share exit code 0</li>
 * <li>All exit codes are non-negative</li>
 * <li>Enum invariants (non-null values and names)</li>
 * </ul>
 */
public class ExitCodeTest
{
	/**
	 * Verifies that {@link ExitCode#SUCCESS} and {@link ExitCode#HELP} share exit code 0.
	 * <p>
	 * Both represent successful operations that do not require process error handling. This test
	 * ensures they maintain the Unix convention of 0 for success while remaining distinct enum values.
	 */
	@Test
	public void successCodesShareZeroValue()
	{
		requireThat(ExitCode.SUCCESS.code(), "SUCCESS.code()").isEqualTo(0);
		requireThat(ExitCode.HELP.code(), "HELP.code()").isEqualTo(0);
		requireThat(ExitCode.SUCCESS, "SUCCESS").isNotEqualTo(ExitCode.HELP);
	}

	/**
	 * Verifies that all exit codes are non-negative.
	 * <p>
	 * This is a fundamental invariant - exit codes cannot be negative in Unix systems as they are
	 * represented as unsigned 8-bit values (0-255).
	 */
	@Test
	public void allExitCodesAreNonNegative()
	{
		for (ExitCode code : ExitCode.values())
			requireThat(code.code(), code.name()).isGreaterThanOrEqualTo(0);
	}

	/**
	 * Verifies that all enum constants have non-null names.
	 * <p>
	 * This ensures the enum contract is maintained - all enum values should be properly initialized
	 * with names accessible via the {@link Enum#name()} method.
	 */
	@Test
	public void allEnumValuesHaveNonNullNames()
	{
		for (ExitCode code : ExitCode.values())
			requireThat(code.name(), code + ".name()").isNotNull();
	}
}
