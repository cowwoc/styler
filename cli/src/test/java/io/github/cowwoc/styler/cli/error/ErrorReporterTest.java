package io.github.cowwoc.styler.cli.error;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.FormattingViolation;
import io.github.cowwoc.styler.formatter.api.ViolationSeverity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Unit tests for ErrorReporter functionality.
 * Validates error collection, formatting, and reporting capabilities.
 */
public class ErrorReporterTest
{
	private ErrorReporter humanReporter;
	private ErrorReporter machineReporter;
	private Path testFile;
	private String testSource;

	/**
	 * Sets up test fixtures with human and machine error reporters and sample test data.
	 */
	@BeforeMethod
	public void setUp()
	{
		humanReporter = new ErrorReporter(new HumanErrorFormatter(false)); // No colors for tests
		machineReporter = new ErrorReporter(new MachineErrorFormatter());
		testFile = Paths.get("TestFile.java");
		testSource = "public class TestFile {\n" +
		             "    public void method() {\n" +
		             "        System.out.println(\"test\");\n" +
		             "    }\n" +
		             "}";
	}

	/**
	 * Verifies that parse errors are correctly reported and categorized.
	 */
	@Test
	public void reportParseError()
	{
		Exception parseException = new RuntimeException("Syntax error at line 2, column 5");

		humanReporter.reportParseError(parseException, testFile, testSource);

		requireThat(humanReporter.getErrorCount(), "errorCount").isEqualTo(1);
		requireThat(humanReporter.hasErrors(), "hasErrors").isTrue();

		ErrorContext error = humanReporter.getErrors().get(0);
		requireThat(error.category(), "errorCategory").isEqualTo(ErrorCategory.PARSE);
		requireThat(error.severity(), "errorSeverity").isEqualTo(ErrorSeverity.ERROR);
		requireThat(error.filePath(), "errorFilePath").isEqualTo(testFile);
	}

	/**
	 * Verifies that configuration errors are correctly reported and categorized.
	 */
	@Test
	public void reportConfigError()
	{
		String configText = "invalid_key = \"value\"\n[section]\nkey = value";
		Exception configException = new RuntimeException("Unknown configuration key 'invalid_key' at line 1");

		humanReporter.reportConfigError(configException, testFile, configText);

		requireThat(humanReporter.getErrorCount(), "errorCount").isEqualTo(1);
		ErrorContext error = humanReporter.getErrors().get(0);
		requireThat(error.category(), "errorCategory").isEqualTo(ErrorCategory.CONFIG);
		requireThat(error.severity(), "errorSeverity").isEqualTo(ErrorSeverity.ERROR);
	}

	/**
	 * Verifies that formatting violations are correctly reported with location and suggested fixes.
	 */
	@Test
	public void reportFormattingViolation()
	{
		SourceRange location = new SourceRange(
			new SourcePosition(2, 5),
			new SourcePosition(2, 20));

		FormattingViolation violation = new FormattingViolation(
			"LineLength",
			location,
			"Line too long: 125 characters (max 120)",
			ViolationSeverity.WARNING,
			"Break line at method call");

		humanReporter.reportViolation(violation, testFile, testSource);

		requireThat(humanReporter.getErrorCount(), "errorCount").isEqualTo(1);
		ErrorContext error = humanReporter.getErrors().get(0);
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
		Exception systemException = new RuntimeException("Permission denied accessing file");

		humanReporter.reportSystemError(systemException, testFile);

		requireThat(humanReporter.getErrorCount(), "errorCount").isEqualTo(1);
		ErrorContext error = humanReporter.getErrors().get(0);
		requireThat(error.category(), "errorCategory").isEqualTo(ErrorCategory.SYSTEM);
		requireThat(error.severity(), "errorSeverity").isEqualTo(ErrorSeverity.ERROR);
	}

	/**
	 * Verifies that the error reporter respects the maximum error limit and halts processing.
	 */
	@Test
	public void maxErrorsLimit()
	{
		humanReporter.setMaxErrors(2);

		// Report 3 errors
		Exception exception = new RuntimeException("Test error");
		humanReporter.reportSystemError(exception, testFile);
		humanReporter.reportSystemError(exception, testFile);
		humanReporter.reportSystemError(exception, testFile);

		// Should stop at max limit
		requireThat(humanReporter.shouldHaltProcessing(), "shouldHaltProcessing").isTrue();
		requireThat(humanReporter.getErrorCount(), "errorCount").isEqualTo(2);
	}

	/**
	 * Verifies that clearing errors resets the error count and processing state.
	 */
	@Test
	public void clearErrors()
	{
		Exception exception = new RuntimeException("Test error");
		humanReporter.reportSystemError(exception, testFile);

		requireThat(humanReporter.hasErrors(), "hasErrors").isTrue();

		humanReporter.clearErrors();

		requireThat(humanReporter.hasErrors(), "hasErrorsAfterClear").isFalse();
		requireThat(humanReporter.getErrorCount(), "errorCountAfterClear").isEqualTo(0);
		requireThat(humanReporter.shouldHaltProcessing(), "shouldHaltProcessingAfterClear").isFalse();
	}

	/**
	 * Verifies that error reports are formatted correctly with file and error information.
	 */
	@Test
	public void formatErrorReport()
	{
		Exception parseException = new RuntimeException("Parse error at line 1");
		humanReporter.reportParseError(parseException, testFile, testSource);

		String report = humanReporter.formatErrorReport();

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
		Exception parseException = new RuntimeException("Parse error");
		humanReporter.reportParseError(parseException, testFile, testSource);

		String summary = humanReporter.formatSummary("format");

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
		Exception parseException = new RuntimeException("Parse error at line 1");
		machineReporter.reportParseError(parseException, testFile, testSource);

		String report = machineReporter.formatErrorReport();

		requireThat(report, "report").isNotNull();
		requireThat(report.contains("{"), "reportContainsOpenBrace").isTrue();
		requireThat(report.contains("\"type\":"), "reportContainsTypeField").isTrue();
		requireThat(report.contains("\"error-report\""), "reportContainsErrorReport").isTrue();
		requireThat(machineReporter.getMimeType(), "mimeType").isEqualTo("application/json");
		requireThat(machineReporter.supportsColors(), "supportsColors").isFalse();
	}

	/**
	 * Verifies that human-readable output uses plain text format.
	 */
	@Test
	public void humanReadableOutput()
	{
		requireThat(humanReporter.getMimeType(), "mimeType").isEqualTo("text/plain");
		// Color support depends on constructor parameter
		requireThat(humanReporter.supportsColors(), "supportsColors").isFalse(); // Disabled in test setup
	}

	/**
	 * Verifies that line and column numbers are correctly extracted from exception messages.
	 */
	@Test
	public void locationExtractionFromException()
	{
		Exception exceptionWithLocation = new RuntimeException("Syntax error at line 15, column 23");
		humanReporter.reportParseError(exceptionWithLocation, testFile, testSource);

		ErrorContext error = humanReporter.getErrors().get(0);
		requireThat(error.getLineNumber(), "errorLineNumber").isEqualTo(15);
		requireThat(error.getColumnNumber(), "errorColumnNumber").isEqualTo(23);
	}

	/**
	 * Verifies that location extraction falls back to line 1, column 1 when location is not found.
	 */
	@Test
	public void locationExtractionFallback()
	{
		Exception exceptionWithoutLocation = new RuntimeException("Generic parse error");
		humanReporter.reportParseError(exceptionWithoutLocation, testFile, testSource);

		ErrorContext error = humanReporter.getErrors().get(0);
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
		humanReporter.setMaxErrors(0);
	}

	/**
	 * Verifies that reporting a null parse exception throws an exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullParseExceptionThrows()
	{
		humanReporter.reportParseError(null, testFile, testSource);
	}

	/**
	 * Verifies that reporting a null formatting violation throws an exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullViolationThrows()
	{
		humanReporter.reportViolation(null, testFile, testSource);
	}

	/**
	 * Verifies that formatting a summary with a null operation type throws an exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullOperationTypeThrows()
	{
		humanReporter.formatSummary(null);
	}
}