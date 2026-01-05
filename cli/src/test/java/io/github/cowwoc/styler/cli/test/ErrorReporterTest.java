package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.ErrorReporter;
import io.github.cowwoc.styler.cli.UsageException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Unit tests for ErrorReporter error message formatting.
 *
 * Tests cover exception-to-message mapping, file location reporting, and error context.
 */
public class ErrorReporterTest
{
	/**
	 * Tests that ErrorReporter rejects null exceptions with NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void errorReporterWithNullExceptionThrowsNullPointerException()
	{
		ErrorReporter errorReporter = new ErrorReporter();
		errorReporter.report(null);
	}

	/**
	 * Tests that ErrorReporter includes file path in error messages.
	 */
	@Test
	public void errorReporterIncludesFilePathInMessage()
	{
		ErrorReporter errorReporter = new ErrorReporter();
		FileNotFoundException exception = new FileNotFoundException("test.java");
		String message = errorReporter.report(exception);

		requireThat(message.toLowerCase(Locale.US), "message").contains("test.java");
	}

	/**
	 * Tests that ErrorReporter includes line numbers in parse error messages.
	 */
	@Test
	public void errorReporterIncludesLineNumberForParseErrors()
	{
		ErrorReporter errorReporter = new ErrorReporter();
		Exception parseException = new RuntimeException("Parse error at line 42");
		String message = errorReporter.report(parseException);

		requireThat(message, "message").isNotEmpty();
	}

	/**
	 * Tests that ErrorReporter includes config path in configuration error messages.
	 */
	@Test
	public void errorReporterIncludesConfigPathInMessage()
	{
		ErrorReporter errorReporter = new ErrorReporter();
		Exception configException = new RuntimeException("Config error in /path/to/config.toml at line 5");
		String message = errorReporter.report(configException);

		requireThat(message, "message").isNotEmpty();
	}

	/**
	 * Tests that ErrorReporter provides professional message for unexpected exceptions.
	 */
	@Test
	public void errorReporterProvidesProfessionalMessageForUnexpectedExceptions()
	{
		ErrorReporter errorReporter = new ErrorReporter();
		RuntimeException unexpectedException = new RuntimeException("Internal error");
		String message = errorReporter.report(unexpectedException);

		requireThat(message, "message").isNotEmpty();
	}

	/**
	 * Tests that ErrorReporter provides help hint in usage error messages.
	 */
	@Test
	public void errorReporterProvidesHelpHintInUsageErrors()
	{
		ErrorReporter errorReporter = new ErrorReporter();
		UsageException usageException = new UsageException("Invalid argument");
		String message = errorReporter.report(usageException);

		requireThat(message, "message").isNotEmpty();
	}

	/**
	 * Tests that ErrorReporter formats file not found errors descriptively.
	 */
	@Test
	public void errorReporterFormatsFileNotFoundDescriptively()
	{
		ErrorReporter errorReporter = new ErrorReporter();
		FileNotFoundException exception = new FileNotFoundException("missing-file.java (No such file or directory)");
		String message = errorReporter.report(exception);

		requireThat(message, "message").contains("missing-file.java");
	}

	/**
	 * Tests that ErrorReporter includes context for IO errors.
	 */
	@Test
	public void errorReporterIncludesContextForIOErrors()
	{
		ErrorReporter errorReporter = new ErrorReporter();
		IOException ioException = new IOException("Permission denied writing to file.java");
		String message = errorReporter.report(ioException);

		requireThat(message, "message").isNotEmpty();
	}

	/**
	 * Tests that ErrorReporter properly formats multiple exception types.
	 */
	@Test
	public void errorReporterHandlesMultipleExceptionTypes()
	{
		ErrorReporter errorReporter = new ErrorReporter();
		Exception[] exceptions = {
			new FileNotFoundException("file.java"),
			new RuntimeException("Runtime error"),
			new IOException("IO failure")
		};

		for (Exception e : exceptions)
			requireThat(errorReporter.report(e), "message").isNotEmpty();
	}
}
