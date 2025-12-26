package io.github.cowwoc.styler.benchmarks.scalability;

import io.github.cowwoc.styler.benchmarks.internal.BenchmarkTransformationContext;
import io.github.cowwoc.styler.benchmarks.util.SampleCodeGenerator;
import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.LineLengthFormattingRule;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Benchmarks parallel processing scalability with virtual threads.
 * <p>
 * Validates linear scaling of file processing throughput using virtual threads with
 * semaphore-based concurrency control (matching the main codebase's VirtualThreadExecutor).
 * Measures speedup ratio at each concurrency level to verify that parallelization overhead
 * is minimal.
 * <p>
 * Methodology: Uses JMH parameterized benchmarks with virtual threads and semaphore permits
 * to control concurrency (1, 2, 4, 8, 16, or 32 concurrent tasks). Results help identify
 * synchronization bottlenecks and concurrency efficiency.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 3, jvmArgs = {"-Xms2g", "-Xmx2g"})
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
public class ScalabilityBenchmark
{
	/**
	 * Maximum concurrent tasks (semaphore permits): 1, 2, 4, 8, 16, or 32.
	 */
	@Param({"1", "2", "4", "8", "16", "32"})
	private int maxConcurrency;

	private List<TransformationContext> contexts = SampleCodeGenerator.
		generateFiles(1000, SampleCodeGenerator.Size.SMALL).stream().
		<TransformationContext>map(BenchmarkTransformationContext::new).
		toList();
	private FormattingRule lineLengthRule = new LineLengthFormattingRule();
	private List<FormattingConfiguration> configs = List.of(LineLengthConfiguration.defaultConfig());

	/**
	 * Benchmarks concurrent file processing using virtual threads with semaphore control.
	 * <p>
	 * Uses virtual threads with a semaphore to limit concurrency, matching the pattern used
	 * in the main codebase's {@code VirtualThreadExecutor}. Each file is processed by its own
	 * virtual thread, with the semaphore controlling how many run simultaneously.
	 *
	 * @return count of processed files
	 * @throws Exception if thread execution fails
	 */
	@Benchmark
	public long processWithConcurrency() throws Exception
	{
		ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
		Semaphore semaphore = new Semaphore(maxConcurrency);
		AtomicLong processed = new AtomicLong(0);
		List<Future<?>> futures = new ArrayList<>();

		try
		{
			for (TransformationContext context : contexts)
			{
				futures.add(executor.submit(() ->
				{
					try
					{
						semaphore.acquire();
						try
						{
							String result = lineLengthRule.format(context, configs);
							if (!result.isEmpty())
							{
								processed.incrementAndGet();
							}
						}
						finally
						{
							semaphore.release();
						}
					}
					catch (InterruptedException e)
					{
						Thread.currentThread().interrupt();
					}
				}));
			}

			// Wait for all tasks to complete
			for (Future<?> future : futures)
			{
				future.get();
			}
		}
		finally
		{
			executor.shutdown();
		}

		return processed.get();
	}

	/**
	 * Baseline single-threaded processing.
	 *
	 * Processes all files sequentially to establish the single-core baseline. Speedup calculations
	 * compare multi-threaded throughput to this baseline.
	 *
	 * @return count of processed files
	 */
	@Benchmark
	public long processSequentially()
	{
		long processed = 0;
		for (TransformationContext context : contexts)
		{
			// Format using actual Styler APIs
			String result = lineLengthRule.format(context, configs);
			if (!result.isEmpty())
			{
				++processed;
			}
		}
		return processed;
	}
}
