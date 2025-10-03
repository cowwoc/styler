package io.github.cowwoc.styler.cli.error;

import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for ErrorCollector batch processing functionality.
 */
public class ErrorCollectorTest
{
	/**
	 * Verifies that errors are automatically batched and processed when the batch size threshold is reached.
	 */
	@Test
	public void batchProcessing() throws Exception
	{
		AtomicInteger batchCount = new AtomicInteger(0);
		List<List<ErrorContext>> processedBatches = new ArrayList<>();

		try (ErrorCollector collector = new ErrorCollector(3, batch ->
		{
			batchCount.incrementAndGet();
			processedBatches.add(new ArrayList<>(batch));
		}))
		{
			// Add errors that will trigger batch processing
			for (int i = 0; i < 7; ++i)
			{
				ErrorContext error = ErrorContext.systemError(
					Paths.get("test" + i + ".java"), "Test error " + i);
				collector.addError(error);
			}

			// Allow some time for async processing
			Thread.sleep(100);

			assertEquals(collector.getErrorCount(), 7);
			assertEquals(batchCount.get(), 2); // Two complete batches of 3
			assertEquals(processedBatches.size(), 2);
			assertEquals(processedBatches.get(0).size(), 3);
			assertEquals(processedBatches.get(1).size(), 3);
		}
	}

	/**
	 * Verifies that flush() processes all pending errors even if the batch size threshold has not been reached.
	 */
	@Test
	public void flush() throws Exception
	{
		List<ErrorContext> processedErrors = new ArrayList<>();

		try (ErrorCollector collector = new ErrorCollector(5, processedErrors::addAll))
		{
			// Add fewer errors than batch size
			for (int i = 0; i < 3; ++i)
			{
				ErrorContext error = ErrorContext.systemError(
					Paths.get("test" + i + ".java"), "Test error " + i);
				collector.addError(error);
			}

			assertEquals(collector.getErrorCount(), 3);
			assertTrue(processedErrors.isEmpty()); // No batch processed yet

			collector.flush();
			Thread.sleep(50); // Allow async processing

			assertEquals(processedErrors.size(), 3);
		}
	}

	/**
	 * Verifies that addErrors() correctly processes a list of errors and triggers batch processing.
	 */
	@Test
	public void addMultipleErrors() throws Exception
	{
		AtomicInteger batchCount = new AtomicInteger(0);

		try (ErrorCollector collector = new ErrorCollector(2, batch ->
		{
			batchCount.incrementAndGet();
		}))
		{
			List<ErrorContext> errors = new ArrayList<>();
			for (int i = 0; i < 5; ++i)
			{
				errors.add(ErrorContext.systemError(
					Paths.get("test" + i + ".java"), "Test error " + i));
			}

			collector.addErrors(errors);
			Thread.sleep(100);

			assertEquals(collector.getErrorCount(), 5);
			assertEquals(batchCount.get(), 2); // 2 complete batches
		}
	}

	/**
	 * Verifies that getAllErrors() returns all collected errors in order.
	 */
	@Test
	public void getAllErrors() throws Exception
	{
		try (ErrorCollector collector = new ErrorCollector(10, batch -> {}))
		{
			List<ErrorContext> originalErrors = new ArrayList<>();
			for (int i = 0; i < 5; ++i)
			{
				ErrorContext error = ErrorContext.systemError(
					Paths.get("test" + i + ".java"), "Test error " + i);
				originalErrors.add(error);
				collector.addError(error);
			}

			List<ErrorContext> allErrors = collector.getAllErrors();
			assertEquals(allErrors.size(), 5);
		}
	}

	/**
	 * Verifies that clear() removes all errors and resets the error count.
	 */
	@Test
	public void clear()
	{
		try (ErrorCollector collector = new ErrorCollector(10, batch -> {}))
		{
			ErrorContext error = ErrorContext.systemError(Paths.get("test.java"), "Test error");
			collector.addError(error);

			assertTrue(collector.hasErrors());
			assertEquals(collector.getErrorCount(), 1);

			collector.clear();

			assertFalse(collector.hasErrors());
			assertEquals(collector.getErrorCount(), 0);
		}
	}

	/**
	 * Verifies that forReporter() creates a collector that forwards errors to the specified reporter.
	 */
	@Test
	public void forReporter()
	{
		ErrorReporter reporter = new ErrorReporter(new MachineErrorFormatter());

		try (ErrorCollector collector = ErrorCollector.forReporter(reporter, 2))
		{
			ErrorContext error1 = ErrorContext.systemError(Paths.get("test1.java"), "Error 1");
			ErrorContext error2 = ErrorContext.systemError(Paths.get("test2.java"), "Error 2");

			collector.addError(error1);
			collector.addError(error2);

			// Errors should be forwarded to reporter
			assertEquals(reporter.getErrorCount(), 2);
		}
	}

	/**
	 * Verifies that createSimple() creates a basic error collector without batch processing.
	 */
	@Test
	public void createSimple()
	{
		try (ErrorCollector collector = ErrorCollector.createSimple())
		{
			ErrorContext error = ErrorContext.systemError(Paths.get("test.java"), "Test error");
			collector.addError(error);

			assertEquals(collector.getErrorCount(), 1);
			assertTrue(collector.hasErrors());
		}
	}

	/**
	 * Verifies that creating a collector with invalid batch size (0 or negative) throws IllegalArgumentException.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void invalidBatchSize()
	{
		new ErrorCollector(0, batch -> {});
	}

	/**
	 * Verifies that creating a collector with null batch processor throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullBatchProcessor()
	{
		new ErrorCollector(5, null);
	}

	/**
	 * Verifies that adding a null error throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullError()
	{
		try (ErrorCollector collector = ErrorCollector.createSimple())
		{
			collector.addError(null);
		}
	}

	/**
	 * Verifies that adding errors after closing the collector throws IllegalStateException.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	@SuppressWarnings("PMD.CloseResource")
	public void addAfterClose()
	{
		ErrorCollector collector = ErrorCollector.createSimple();
		collector.close();

		ErrorContext error = ErrorContext.systemError(Paths.get("test.java"), "Test error");
		collector.addError(error);
	}

	/**
	 * Verifies that close() is idempotent and can be called multiple times safely.
	 */
	@Test
	@SuppressWarnings("PMD.CloseResource")
	public void closeIdempotent()
	{
		ErrorCollector collector = ErrorCollector.createSimple();

		assertFalse(collector.isClosed());
		collector.close();
		assertTrue(collector.isClosed());

		// Second close should not throw
		collector.close();
		assertTrue(collector.isClosed());
	}
}