package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.OutputHandler;
import io.github.cowwoc.styler.pipeline.output.OutputFormat;
import org.testng.annotations.Test;

/**
 * Unit tests for {@code OutputHandler} null validation.
 */
public class OutputHandlerTest
{
	/**
	 * Tests that {@code OutputHandler} rejects {@code null} results with
	 * {@code NullPointerException}.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void outputHandlerWithNullResultThrowsNullPointerException()
	{
		OutputHandler outputHandler = new OutputHandler();
		outputHandler.render(null, OutputFormat.HUMAN, 0);
	}
}
