package io.github.cowwoc.styler.cli.error;

import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

/**
 * Unit tests for ErrorCollector batch processing functionality.
 */
public class ErrorCollectorTest
{
	@Test
	public void testBatchProcessing() throws Exception
	{
		AtomicInteger batchCount = new AtomicInteger(0);
		List<List<ErrorContext>> processedBatches = new ArrayList<>();

		try (ErrorCollector collector = new ErrorCollector(3, batch -> {
			batchCount.incrementAndGet();
			processedBatches.add(new ArrayList<>(batch));
		}))
		{
			// Add errors that will trigger batch processing
			for (int i = 0; i < 7; i++)
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

	@Test
	public void testFlush() throws Exception
	{
		List<ErrorContext> processedErrors = new ArrayList<>();

		try (ErrorCollector collector = new ErrorCollector(5, processedErrors::addAll))
		{
			// Add fewer errors than batch size
			for (int i = 0; i < 3; i++)
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

	@Test
	public void testAddMultipleErrors() throws Exception
	{
		AtomicInteger batchCount = new AtomicInteger(0);

		try (ErrorCollector collector = new ErrorCollector(2, batch -> {
			batchCount.incrementAndGet();
		}))
		{
			List<ErrorContext> errors = new ArrayList<>();
			for (int i = 0; i < 5; i++)
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

	@Test
	public void testGetAllErrors() throws Exception
	{
		try (ErrorCollector collector = new ErrorCollector(10, batch -> {}))
		{
			List<ErrorContext> originalErrors = new ArrayList<>();
			for (int i = 0; i < 5; i++)
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

	@Test
	public void testClear()
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

	@Test
	public void testForReporter()
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

	@Test
	public void testCreateSimple()
	{
		try (ErrorCollector collector = ErrorCollector.createSimple())
		{
			ErrorContext error = ErrorContext.systemError(Paths.get("test.java"), "Test error");
			collector.addError(error);

			assertEquals(collector.getErrorCount(), 1);
			assertTrue(collector.hasErrors());
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidBatchSize()
	{
		new ErrorCollector(0, batch -> {});
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullBatchProcessor()
	{
		new ErrorCollector(5, null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullError()
	{
		try (ErrorCollector collector = ErrorCollector.createSimple())
		{
			collector.addError(null);
		}
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testAddAfterClose()
	{
		ErrorCollector collector = ErrorCollector.createSimple();
		collector.close();

		ErrorContext error = ErrorContext.systemError(Paths.get("test.java"), "Test error");
		collector.addError(error);
	}

	@Test
	public void testCloseIdempotent()
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