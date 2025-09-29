package io.github.cowwoc.styler.cli.error;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.FormattingViolation;
import io.github.cowwoc.styler.formatter.api.ViolationSeverity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.*;

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

	@Test
	public void testReportParseError()
	{
		Exception parseException = new RuntimeException("Syntax error at line 2, column 5");

		humanReporter.reportParseError(parseException, testFile, testSource);

		assertEquals(humanReporter.getErrorCount(), 1);
		assertTrue(humanReporter.hasErrors());

		ErrorContext error = humanReporter.getErrors().get(0);
		assertEquals(error.category(), ErrorCategory.PARSE);
		assertEquals(error.severity(), ErrorSeverity.ERROR);
		assertEquals(error.filePath(), testFile);
	}

	@Test
	public void testReportConfigError()
	{
		String configText = "invalid_key = \"value\"\n[section]\nkey = value";
		Exception configException = new RuntimeException("Unknown configuration key 'invalid_key' at line 1");

		humanReporter.reportConfigError(configException, testFile, configText);

		assertEquals(humanReporter.getErrorCount(), 1);
		ErrorContext error = humanReporter.getErrors().get(0);
		assertEquals(error.category(), ErrorCategory.CONFIG);
		assertEquals(error.severity(), ErrorSeverity.ERROR);
	}

	@Test
	public void testReportFormattingViolation()
	{
		SourceRange location = new SourceRange(
			new SourcePosition(2, 5),
			new SourcePosition(2, 20)
		);

		FormattingViolation violation = new FormattingViolation(
			"LineLength",
			location,
			"Line too long: 125 characters (max 120)",
			ViolationSeverity.WARNING,
			"Break line at method call"
		);

		humanReporter.reportViolation(violation, testFile, testSource);

		assertEquals(humanReporter.getErrorCount(), 1);
		ErrorContext error = humanReporter.getErrors().get(0);
		assertEquals(error.category(), ErrorCategory.FORMAT);
		assertEquals(error.severity(), ErrorSeverity.WARNING);
		assertEquals(error.getLineNumber(), 2);
		assertEquals(error.getColumnNumber(), 5);
		assertTrue(error.hasSuggestedFix());
	}

	@Test
	public void testReportSystemError()
	{
		Exception systemException = new RuntimeException("Permission denied accessing file");

		humanReporter.reportSystemError(systemException, testFile);

		assertEquals(humanReporter.getErrorCount(), 1);
		ErrorContext error = humanReporter.getErrors().get(0);
		assertEquals(error.category(), ErrorCategory.SYSTEM);
		assertEquals(error.severity(), ErrorSeverity.ERROR);
	}

	@Test
	public void testMaxErrorsLimit()
	{
		humanReporter.setMaxErrors(2);

		// Report 3 errors
		Exception exception = new RuntimeException("Test error");
		humanReporter.reportSystemError(exception, testFile);
		humanReporter.reportSystemError(exception, testFile);
		humanReporter.reportSystemError(exception, testFile);

		// Should stop at max limit
		assertTrue(humanReporter.shouldHaltProcessing());
		assertEquals(humanReporter.getErrorCount(), 2);
	}

	@Test
	public void testClearErrors()
	{
		Exception exception = new RuntimeException("Test error");
		humanReporter.reportSystemError(exception, testFile);

		assertTrue(humanReporter.hasErrors());

		humanReporter.clearErrors();

		assertFalse(humanReporter.hasErrors());
		assertEquals(humanReporter.getErrorCount(), 0);
		assertFalse(humanReporter.shouldHaltProcessing());
	}

	@Test
	public void testFormatErrorReport()
	{
		Exception parseException = new RuntimeException("Parse error at line 1");
		humanReporter.reportParseError(parseException, testFile, testSource);

		String report = humanReporter.formatErrorReport();

		assertNotNull(report);
		assertFalse(report.trim().isEmpty());
		assertTrue(report.contains("TestFile.java"));
		assertTrue(report.contains("Parse error"));
	}

	@Test
	public void testFormatSummary()
	{
		Exception parseException = new RuntimeException("Parse error");
		humanReporter.reportParseError(parseException, testFile, testSource);

		String summary = humanReporter.formatSummary("format");

		assertNotNull(summary);
		assertFalse(summary.trim().isEmpty());
		assertTrue(summary.contains("format"));
		assertTrue(summary.contains("1"));
	}

	@Test
	public void testMachineReadableOutput()
	{
		Exception parseException = new RuntimeException("Parse error at line 1");
		machineReporter.reportParseError(parseException, testFile, testSource);

		String report = machineReporter.formatErrorReport();

		assertNotNull(report);
		assertTrue(report.contains("{"));
		assertTrue(report.contains("\"type\":"));
		assertTrue(report.contains("\"error-report\""));
		assertEquals(machineReporter.getMimeType(), "application/json");
		assertFalse(machineReporter.supportsColors());
	}

	@Test
	public void testHumanReadableOutput()
	{
		assertEquals(humanReporter.getMimeType(), "text/plain");
		// Color support depends on constructor parameter
		assertFalse(humanReporter.supportsColors()); // Disabled in test setup
	}

	@Test
	public void testLocationExtractionFromException()
	{
		Exception exceptionWithLocation = new RuntimeException("Syntax error at line 15, column 23");
		humanReporter.reportParseError(exceptionWithLocation, testFile, testSource);

		ErrorContext error = humanReporter.getErrors().get(0);
		assertEquals(error.getLineNumber(), 15);
		assertEquals(error.getColumnNumber(), 23);
	}

	@Test
	public void testLocationExtractionFallback()
	{
		Exception exceptionWithoutLocation = new RuntimeException("Generic parse error");
		humanReporter.reportParseError(exceptionWithoutLocation, testFile, testSource);

		ErrorContext error = humanReporter.getErrors().get(0);
		assertEquals(error.getLineNumber(), 1);
		assertEquals(error.getColumnNumber(), 1);
	}

	@Test
	public void testConstructorVariants()
	{
		// Test default constructor
		ErrorReporter defaultReporter = new ErrorReporter();
		assertEquals(defaultReporter.getMimeType(), "text/plain");

		// Test parameterized constructor
		ErrorReporter humanReporter = new ErrorReporter(false, true);
		assertEquals(humanReporter.getMimeType(), "text/plain");

		ErrorReporter machineReporter = new ErrorReporter(true, false);
		assertEquals(machineReporter.getMimeType(), "application/json");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullFormatterThrows()
	{
		new ErrorReporter(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidMaxErrorsThrows()
	{
		humanReporter.setMaxErrors(0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullParseExceptionThrows()
	{
		humanReporter.reportParseError(null, testFile, testSource);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullViolationThrows()
	{
		humanReporter.reportViolation(null, testFile, testSource);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullOperationTypeThrows()
	{
		humanReporter.formatSummary(null);
	}
}