package io.github.cowwoc.styler.cli.test.pipeline.progress;

import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.progress.ConcurrentProgressObserver;
import io.github.cowwoc.styler.cli.pipeline.progress.ProgressObserver;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ConcurrentProgressObserver}.
 * <p>
 * Verifies thread safety, atomic counter updates, and correct progress tracking
 * under concurrent access from multiple threads.
 */
public final class ConcurrentProgressObserverTest
{
	/**
	 * Verifies that completed count is incremented atomically across threads.
	 */
	@Test
	public void completedCountIsThreadSafe() throws InterruptedException
	{
		ProgressObserver delegate = ProgressObserver.noOp();
		ConcurrentProgressObserver observer = new ConcurrentProgressObserver(delegate, 100);

		// Simulate 100 files completing from 10 threads concurrently
		try (ExecutorService executor = Executors.newFixedThreadPool(10))
		{
			CountDownLatch latch = new CountDownLatch(100);

			for (int i = 0; i < 100; ++i)
			{
				int fileIndex = i;
				executor.submit(() ->
				{
					try
					{
						Path file = Paths.get("file-" + fileIndex + ".java");
						observer.onProcessingCompleted(file);
					}
					finally
					{
						latch.countDown();
					}
				});
			}

			latch.await(10, TimeUnit.SECONDS);
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		}

		assertThat(observer.getCompletedCount()).isEqualTo(100);
		assertThat(observer.getErrorCount()).isZero();
		assertThat(observer.getRemainingCount()).isZero();
		assertThat(observer.getProgressPercentage()).isEqualTo(100.0);
	}

	/**
	 * Verifies that error count is incremented atomically across threads.
	 */
	@Test
	public void errorCountIsThreadSafe() throws InterruptedException
	{
		ProgressObserver delegate = ProgressObserver.noOp();
		ConcurrentProgressObserver observer = new ConcurrentProgressObserver(delegate, 50);

		try (ExecutorService executor = Executors.newFixedThreadPool(10))
		{
			CountDownLatch latch = new CountDownLatch(50);

			for (int i = 0; i < 50; ++i)
			{
				int fileIndex = i;
				executor.submit(() ->
				{
					try
					{
						Path file = Paths.get("file-" + fileIndex + ".java");
						PipelineException exception = new PipelineException(
							"Test error",
							file,
							"test-stage",
							new IllegalStateException("Test cause"));
						observer.onProcessingFailed(file, "test-stage", exception);
					}
					finally
					{
						latch.countDown();
					}
				});
			}

			latch.await(10, TimeUnit.SECONDS);
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		}

		assertThat(observer.getCompletedCount()).isEqualTo(50);
		assertThat(observer.getErrorCount()).isEqualTo(50);
		assertThat(observer.getRemainingCount()).isZero();
		assertThat(observer.getProgressPercentage()).isEqualTo(100.0);
	}

	/**
	 * Verifies mixed success and failure updates are tracked correctly.
	 */
	@Test
	public void mixedSuccessAndFailureIsTrackedCorrectly() throws InterruptedException
	{
		ProgressObserver delegate = ProgressObserver.noOp();
		ConcurrentProgressObserver observer = new ConcurrentProgressObserver(delegate, 100);

		try (ExecutorService executor = Executors.newFixedThreadPool(10))
		{
			CountDownLatch latch = new CountDownLatch(100);

			for (int i = 0; i < 100; ++i)
			{
				int fileIndex = i;
				boolean shouldFail = fileIndex % 3 == 0;

				executor.submit(() ->
				{
					try
					{
						Path file = Paths.get("file-" + fileIndex + ".java");
						if (shouldFail)
						{
							PipelineException exception = new PipelineException(
								"Test error",
								file,
								"test-stage",
								new IllegalStateException("Test cause"));
							observer.onProcessingFailed(file, "test-stage", exception);
						}
						else
						{
							observer.onProcessingCompleted(file);
						}
					}
					finally
					{
						latch.countDown();
					}
				});
			}

			latch.await(10, TimeUnit.SECONDS);
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		}

		assertThat(observer.getCompletedCount()).isEqualTo(100);
		assertThat(observer.getErrorCount()).isEqualTo(34); // 0, 3, 6, 9, ..., 99 = 34 failures
		assertThat(observer.getRemainingCount()).isZero();
		assertThat(observer.getProgressPercentage()).isEqualTo(100.0);
	}

	/**
	 * Verifies progress percentage is calculated correctly.
	 */
	@Test
	public void progressPercentageIsCalculatedCorrectly()
	{
		ProgressObserver delegate = ProgressObserver.noOp();
		ConcurrentProgressObserver observer = new ConcurrentProgressObserver(delegate, 200);

		assertThat(observer.getProgressPercentage()).isEqualTo(0.0);

		// Complete 50 files (25%)
		for (int i = 0; i < 50; ++i)
		{
			observer.onProcessingCompleted(Paths.get("file-" + i + ".java"));
		}
		assertThat(observer.getProgressPercentage()).isEqualTo(25.0);

		// Complete another 50 files (50% total)
		for (int i = 50; i < 100; ++i)
		{
			observer.onProcessingCompleted(Paths.get("file-" + i + ".java"));
		}
		assertThat(observer.getProgressPercentage()).isEqualTo(50.0);

		// Fail another 100 files (100% total)
		for (int i = 100; i < 200; ++i)
		{
			Path file = Paths.get("file-" + i + ".java");
			PipelineException exception = new PipelineException(
				"Test error",
				file,
				"test-stage",
				new RuntimeException("Test cause"));
			observer.onProcessingFailed(file, "test-stage", exception);
		}
		assertThat(observer.getProgressPercentage()).isEqualTo(100.0);
	}

	/**
	 * Verifies throughput calculation is accurate.
	 */
	@Test
	public void throughputIsCalculatedCorrectly() throws InterruptedException
	{
		ProgressObserver delegate = ProgressObserver.noOp();
		ConcurrentProgressObserver observer = new ConcurrentProgressObserver(delegate, 100);

		// Process files over time
		for (int i = 0; i < 10; ++i)
		{
			observer.onProcessingCompleted(Paths.get("file-" + i + ".java"));
		}

		// Allow some time to pass
		Thread.sleep(100);

		double throughput = observer.getThroughput();
		assertThat(throughput).isGreaterThan(0.0);
		assertThat(throughput).isLessThan(1000.0); // Sanity check
	}

	/**
	 * Verifies remaining count decreases as files complete.
	 */
	@Test
	public void remainingCountDecreasesAsFilesComplete()
	{
		ProgressObserver delegate = ProgressObserver.noOp();
		ConcurrentProgressObserver observer = new ConcurrentProgressObserver(delegate, 50);

		assertThat(observer.getRemainingCount()).isEqualTo(50);

		for (int i = 0; i < 25; ++i)
		{
			observer.onProcessingCompleted(Paths.get("file-" + i + ".java"));
		}
		assertThat(observer.getRemainingCount()).isEqualTo(25);

		for (int i = 25; i < 50; ++i)
		{
			observer.onProcessingCompleted(Paths.get("file-" + i + ".java"));
		}
		assertThat(observer.getRemainingCount()).isZero();
	}

	/**
	 * Verifies delegate observer receives all events.
	 */
	@Test
	public void delegateObserverReceivesAllEvents()
	{
		AtomicInteger startedCount = new AtomicInteger(0);
		AtomicInteger completedCount = new AtomicInteger(0);
		AtomicInteger failedCount = new AtomicInteger(0);

		ProgressObserver delegate = new ProgressObserver()
		{
			@Override
			public void onProcessingStarted(Path file, int totalStages)
			{
				startedCount.incrementAndGet();
			}

			@Override
			public void onStageStarted(Path file, String stageName, int stageIndex)
			{
				// Not tested
			}

			@Override
			public void onStageCompleted(Path file, String stageName, int stageIndex)
			{
				// Not tested
			}

			@Override
			public void onProcessingCompleted(Path file)
			{
				completedCount.incrementAndGet();
			}

			@Override
			public void onProcessingFailed(Path file, String stageName, PipelineException exception)
			{
				failedCount.incrementAndGet();
			}

			@Override
			public void onPipelineClosed()
			{
				// Not tested
			}
		};

		ConcurrentProgressObserver observer = new ConcurrentProgressObserver(delegate, 10);

		// Simulate processing
		for (int i = 0; i < 5; ++i)
		{
			Path file = Paths.get("file-" + i + ".java");
			observer.onProcessingStarted(file, 3);
			observer.onProcessingCompleted(file);
		}

		for (int i = 5; i < 10; ++i)
		{
			Path file = Paths.get("file-" + i + ".java");
			observer.onProcessingStarted(file, 3);
			PipelineException exception = new PipelineException(
				"Test error",
				file,
				"test-stage",
				new RuntimeException("Test cause"));
			observer.onProcessingFailed(file, "test-stage", exception);
		}

		assertThat(startedCount.get()).isEqualTo(10);
		assertThat(completedCount.get()).isEqualTo(5);
		assertThat(failedCount.get()).isEqualTo(5);
	}

	/**
	 * Verifies zero total files returns zero progress.
	 */
	@Test
	public void zeroTotalFilesReturnsZeroProgress()
	{
		ProgressObserver delegate = ProgressObserver.noOp();
		ConcurrentProgressObserver observer = new ConcurrentProgressObserver(delegate, 0);

		assertThat(observer.getProgressPercentage()).isEqualTo(0.0);
		assertThat(observer.getCompletedCount()).isZero();
		assertThat(observer.getErrorCount()).isZero();
		assertThat(observer.getRemainingCount()).isZero();
	}

	/**
	 * Verifies constructor validates inputs.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullDelegateThrowsException()
	{
		new ConcurrentProgressObserver(null, 10);
	}

	/**
	 * Verifies constructor validates total files is non-negative.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void negativeTotalFilesThrowsException()
	{
		ProgressObserver delegate = ProgressObserver.noOp();
		new ConcurrentProgressObserver(delegate, -1);
	}
}
