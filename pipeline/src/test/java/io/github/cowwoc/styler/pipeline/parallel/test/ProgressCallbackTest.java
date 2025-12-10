package io.github.cowwoc.styler.pipeline.parallel.test;

import static org.testng.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.parallel.ProgressCallback;

/**
 * Unit tests for {@code ProgressCallback}.
 */
public class ProgressCallbackTest
{
	/**
	 * Tests that progress callbacks are invoked correctly.
	 */
	@Test
	public void shouldAcceptProgressCallbacks()
	{
		AtomicInteger callCount = new AtomicInteger(0);
		ProgressCallback callback = (completed, total, file) -> callCount.incrementAndGet();

		callback.onProgress(1, 10, Paths.get("test.java"));

		assertEquals(callCount.get(), 1);
	}

	/**
	 * Tests that progress is tracked correctly across multiple invocations.
	 */
	@Test
	public void shouldTrackProgressCorrectly()
	{
		List<String> progressUpdates = new ArrayList<>();
		ProgressCallback callback = (completed, total, file) ->
			progressUpdates.add(String.format("%d/%d", completed, total));

		callback.onProgress(1, 5, Paths.get("test1.java"));
		callback.onProgress(2, 5, Paths.get("test2.java"));
		callback.onProgress(3, 5, Paths.get("test3.java"));

		assertEquals(progressUpdates.size(), 3);
		assertEquals(progressUpdates.get(0), "1/5");
		assertEquals(progressUpdates.get(1), "2/5");
		assertEquals(progressUpdates.get(2), "3/5");
	}

	/**
	 * Tests that file paths are received correctly in the callback.
	 */
	@Test
	public void shouldReceiveCorrectFilePath()
	{
		List<Path> receivedFiles = new ArrayList<>();
		ProgressCallback callback = (completed, total, file) -> receivedFiles.add(file);

		Path file1 = Paths.get("test1.java");
		Path file2 = Paths.get("test2.java");

		callback.onProgress(1, 2, file1);
		callback.onProgress(2, 2, file2);

		assertEquals(receivedFiles.size(), 2);
		assertEquals(receivedFiles.get(0), file1);
		assertEquals(receivedFiles.get(1), file2);
	}

	/**
	 * Tests that progress callback is thread-safe.
	 */
	@Test
	public void shouldBeThreadSafe()
	{
		AtomicInteger callCount = new AtomicInteger(0);
		ProgressCallback callback = (completed, total, file) -> callCount.incrementAndGet();

		int threadCount = 10;
		Thread[] threads = new Thread[threadCount];

		for (int i = 0; i < threadCount; ++i)
		{
			final int index = i;
			threads[i] = new Thread(() ->
			{
				for (int j = 0; j < 10; ++j)
				{
					callback.onProgress(index * 10 + j, 100, Paths.get("test" + index + "_" + j + ".java"));
				}
			});
		}

		// Start all threads
		for (Thread thread : threads)
		{
			thread.start();
		}

		// Wait for all threads to complete
		for (Thread thread : threads)
		{
			try
			{
				thread.join();
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}

		assertEquals(callCount.get(), 100);
	}
}
