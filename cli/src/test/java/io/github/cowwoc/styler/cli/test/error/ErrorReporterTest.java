package io.github.cowwoc.styler.cli.test.error;
import io.github.cowwoc.styler.cli.error.ErrorSeverity;
import io.github.cowwoc.styler.cli.error.MachineErrorFormatter;
import io.github.cowwoc.styler.cli.error.HumanErrorFormatter;
import io.github.cowwoc.styler.cli.error.ErrorCategory;
import io.github.cowwoc.styler.cli.error.ErrorContext;

import io.github.cowwoc.styler.cli.error.ErrorReporter;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.FormattingViolation;
import io.github.cowwoc.styler.formatter.api.ViolationSeverity;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Unit tests for ErrorReporter functionality.
 * Validates error collection, formatting, and reporting capabilities.
 * <p>
 * Thread-safe: All tests use method-local variables for reporters and test data.
 */
public class ErrorReporterTest
{
	/**
	 * Creates test fixtures for use in test methods.
	 */
	private static class TestFixtures
	{
		final ErrorReporter humanReporter;
		final ErrorReporter machineReporter;
		final Path testFile;
		final String testSource;

		TestFixtures()
		{
			this.humanReporter = new ErrorReporter(new HumanErrorFormatter(false)); // No colors for tests
			this.machineReporter = new ErrorReporter(new MachineErrorFormatter());
			this.testFile = Paths.get("TestFile.java");
			this.testSource = "public class TestFile {\n" +
			                  "    public void method() {\n" +
			                  "        System.out.println(\"test\");\n" +
			                  "    }\n" +
			                  "}";
		}
	}

	/**
	 * Verifies that parse errors are correctly reported and categorized.
	 */
	@Test
	public void reportParseError()
	{
		TestFixtures fixtures = new TestFixtures();
		Exception parseException = new RuntimeException("Syntax error at line 2, column 5");

		fixtures.humanReporter.reportParseError(parseException, fixtures.testFile, fixtures.testSource);

		requireThat(fixtures.humanReporter.getErrorCount(), "errorCount").isEqualTo(1);
		requireThat(fixtures.humanReporter.hasErrors(), "hasErrors").isTrue();

		ErrorContext error = fixtures.humanReporter.getErrors().get(0);
		requireThat(error.category(), "errorCategory").isEqualTo(ErrorCategory.PARSE);
		requireThat(error.severity(), "errorSeverity").isEqualTo(ErrorSeverity.ERROR);
		requireThat(error.filePath(), "errorFilePath").isEqualTo(fixtures.testFile);
	}

	/**
	 * Verifies that configuration errors are correctly reported and categorized.
	 */
	@Test
	public void reportConfigError()
	{
		TestFixtures fixtures = new TestFixtures();
		String configText = "invalid_key = \"value\"\n[section]\nkey = value";
		Exception configException = new RuntimeException("Unknown configuration key 'invalid_key' at line 1");

		fixtures.humanReporter.reportConfigError(configException, fixtures.testFile, configText);

		requireThat(fixtures.humanReporter.getErrorCount(), "errorCount").isEqualTo(1);
		ErrorContext error = fixtures.humanReporter.getErrors().get(0);
		requireThat(error.category(), "errorCategory").isEqualTo(ErrorCategory.CONFIG);
		requireThat(error.severity(), "errorSeverity").isEqualTo(ErrorSeverity.ERROR);
	}

	/**
	 * Verifies that formatting violations are correctly reported with location and suggested fixes.
	 */
	@Test
	public void reportFormattingViolation()
	{
		TestFixtures fixtures = new TestFixtures();
		SourceRange location = new SourceRange(
			new SourcePosition(2, 5),
			new SourcePosition(2, 20));

		FormattingViolation violation = new FormattingViolation(
			"LineLength",
			location,
			"Line too long: 125 characters (max 120)",
			ViolationSeverity.WARNING,
			"Break line at method call");

		fixtures.humanReporter.reportViolation(violation, fixtures.testFile, fixtures.testSource);

		requireThat(fixtures.humanReporter.getErrorCount(), "errorCount").isEqualTo(1);
		ErrorContext error = fixtures.humanReporter.getErrors().get(0);
		requireThat(error.category(), "errorCategory").isEqualTo(ErrorCategory.FORMAT);
		requireThat(error.severity(), "errorSeverity").isEqualTo(ErrorSeverity.WARNING);
		requireThat(error.getLineNumber(), "errorLineNumber").isEqualTo(2);
		requireThat(error.getColumnNumber(), "errorColumnNumber").isEqualTo(5);
		requireThat(error.hasSuggestedFix(), "errorHasSuggestedFix").isTrue();
	}

	/**
	 * Verifies that system errors are correctly reported and categorized.
	 */
	@Test
	public void reportSystemError()
	{
		TestFixtures fixtures = new TestFixtures();
		Exception systemException = new RuntimeException("Permission denied accessing file");

		fixtures.humanReporter.reportSystemError(systemException, fixtures.testFile);

		requireThat(fixtures.humanReporter.getErrorCount(), "errorCount").isEqualTo(1);
		ErrorContext error = fixtures.humanReporter.getErrors().get(0);
		requireThat(error.category(), "errorCategory").isEqualTo(ErrorCategory.SYSTEM);
		requireThat(error.severity(), "errorSeverity").isEqualTo(ErrorSeverity.ERROR);
	}

	/**
	 * Verifies that the error reporter respects the maximum error limit and halts processing.
	 */
	@Test
	public void maxErrorsLimit()
	{
		TestFixtures fixtures = new TestFixtures();
		fixtures.humanReporter.setMaxErrors(2);

		// Report 3 errors
		Exception exception = new RuntimeException("Test error");
		fixtures.humanReporter.reportSystemError(exception, fixtures.testFile);
		fixtures.humanReporter.reportSystemError(exception, fixtures.testFile);
		fixtures.humanReporter.reportSystemError(exception, fixtures.testFile);

		// Should stop at max limit
		requireThat(fixtures.humanReporter.shouldHaltProcessing(), "shouldHaltProcessing").isTrue();
		requireThat(fixtures.humanReporter.getErrorCount(), "errorCount").isEqualTo(2);
	}

	/**
	 * Verifies that clearing errors resets the error count and processing state.
	 */
	@Test
	public void clearErrors()
	{
		TestFixtures fixtures = new TestFixtures();
		Exception exception = new RuntimeException("Test error");
		fixtures.humanReporter.reportSystemError(exception, fixtures.testFile);

		requireThat(fixtures.humanReporter.hasErrors(), "hasErrors").isTrue();

		fixtures.humanReporter.clearErrors();

		requireThat(fixtures.humanReporter.hasErrors(), "hasErrorsAfterClear").isFalse();
		requireThat(fixtures.humanReporter.getErrorCount(), "errorCountAfterClear").isEqualTo(0);
		requireThat(fixtures.humanReporter.shouldHaltProcessing(), "shouldHaltProcessingAfterClear").isFalse();
	}

	/**
	 * Verifies that error reports are formatted correctly with file and error information.
	 */
	@Test
	public void formatErrorReport()
	{
		TestFixtures fixtures = new TestFixtures();
		Exception parseException = new RuntimeException("Parse error at line 1");
		fixtures.humanReporter.reportParseError(parseException, fixtures.testFile, fixtures.testSource);

		String report = fixtures.humanReporter.formatErrorReport();

		requireThat(report, "report").isNotNull();
		requireThat(report.isBlank(), "reportIsBlank").isFalse();
		requireThat(report.contains("TestFile.java"), "reportContainsFilename").isTrue();
		requireThat(report.contains("Parse error"), "reportContainsErrorType").isTrue();
	}

	/**
	 * Verifies that error summaries include operation type and error counts.
	 */
	@Test
	public void formatSummary()
	{
		TestFixtures fixtures = new TestFixtures();
		Exception parseException = new RuntimeException("Parse error");
		fixtures.humanReporter.reportParseError(parseException, fixtures.testFile, fixtures.testSource);

		String summary = fixtures.humanReporter.formatSummary("format");

		requireThat(summary, "summary").isNotNull();
		requireThat(summary.isBlank(), "summaryIsBlank").isFalse();
		requireThat(summary.contains("format"), "summaryContainsOperation").isTrue();
		requireThat(summary.contains("1"), "summaryContainsCount").isTrue();
	}

	/**
	 * Verifies that machine-readable output is formatted as valid JSON.
	 */
	@Test
	public void machineReadableOutput()
	{
		TestFixtures fixtures = new TestFixtures();
		Exception parseException = new RuntimeException("Parse error at line 1");
		fixtures.machineReporter.reportParseError(parseException, fixtures.testFile, fixtures.testSource);

		String report = fixtures.machineReporter.formatErrorReport();

		requireThat(report, "report").isNotNull();
		requireThat(report.contains("{"), "reportContainsOpenBrace").isTrue();
		requireThat(report.contains("\"type\":"), "reportContainsTypeField").isTrue();
		requireThat(report.contains("\"error-report\""), "reportContainsErrorReport").isTrue();
		requireThat(fixtures.machineReporter.getMimeType(), "mimeType").isEqualTo("application/json");
		requireThat(fixtures.machineReporter.supportsColors(), "supportsColors").isFalse();
	}

	/**
	 * Verifies that human-readable output uses plain text format.
	 */
	@Test
	public void humanReadableOutput()
	{
		TestFixtures fixtures = new TestFixtures();
		requireThat(fixtures.humanReporter.getMimeType(), "mimeType").isEqualTo("text/plain");
		// Color support depends on constructor parameter
		requireThat(fixtures.humanReporter.supportsColors(), "supportsColors").isFalse(); // Disabled in test setup
	}

	/**
	 * Verifies that line and column numbers are correctly extracted from exception messages.
	 */
	@Test
	public void locationExtractionFromException()
	{
		TestFixtures fixtures = new TestFixtures();
		Exception exceptionWithLocation = new RuntimeException("Syntax error at line 15, column 23");
		fixtures.humanReporter.reportParseError(exceptionWithLocation, fixtures.testFile, fixtures.testSource);

		ErrorContext error = fixtures.humanReporter.getErrors().get(0);
		requireThat(error.getLineNumber(), "errorLineNumber").isEqualTo(15);
		requireThat(error.getColumnNumber(), "errorColumnNumber").isEqualTo(23);
	}

	/**
	 * Verifies that location extraction falls back to line 1, column 1 when location is not found.
	 */
	@Test
	public void locationExtractionFallback()
	{
		TestFixtures fixtures = new TestFixtures();
		Exception exceptionWithoutLocation = new RuntimeException("Generic parse error");
		fixtures.humanReporter.reportParseError(exceptionWithoutLocation, fixtures.testFile, fixtures.testSource);

		ErrorContext error = fixtures.humanReporter.getErrors().get(0);
		requireThat(error.getLineNumber(), "errorLineNumber").isEqualTo(1);
		requireThat(error.getColumnNumber(), "errorColumnNumber").isEqualTo(1);
	}

	/**
	 * Verifies that all ErrorReporter constructor variants create properly configured instances.
	 */
	@Test
	public void constructorVariants()
	{
		// Test default constructor
		ErrorReporter defaultReporter = new ErrorReporter();
		requireThat(defaultReporter.getMimeType(), "defaultMimeType").isEqualTo("text/plain");

		// Test parameterized constructor
		ErrorReporter humanReporter = new ErrorReporter(false, true);
		requireThat(humanReporter.getMimeType(), "humanMimeType").isEqualTo("text/plain");

		ErrorReporter machineReporter = new ErrorReporter(true, false);
		requireThat(machineReporter.getMimeType(), "machineMimeType").isEqualTo("application/json");
	}

	/**
	 * Verifies that passing a null formatter to the constructor throws an exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullFormatterThrows()
	{
		new ErrorReporter(null);
	}

	/**
	 * Verifies that setting max errors to zero or negative throws an exception.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void invalidMaxErrorsThrows()
	{
		TestFixtures fixtures = new TestFixtures();
		fixtures.humanReporter.setMaxErrors(0);
	}

	/**
	 * Verifies that reporting a null parse exception throws an exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullParseExceptionThrows()
	{
		TestFixtures fixtures = new TestFixtures();
		fixtures.humanReporter.reportParseError(null, fixtures.testFile, fixtures.testSource);
	}

	/**
	 * Verifies that reporting a null formatting violation throws an exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullViolationThrows()
	{
		TestFixtures fixtures = new TestFixtures();
		fixtures.humanReporter.reportViolation(null, fixtures.testFile, fixtures.testSource);
	}

	/**
	 * Verifies that formatting a summary with a null operation type throws an exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullOperationTypeThrows()
	{
		TestFixtures fixtures = new TestFixtures();
		fixtures.humanReporter.formatSummary(null);
	}
}
