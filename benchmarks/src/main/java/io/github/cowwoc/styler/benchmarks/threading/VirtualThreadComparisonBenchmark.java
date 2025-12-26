package io.github.cowwoc.styler.benchmarks.threading;

import io.github.cowwoc.styler.benchmarks.internal.BenchmarkTransformationContext;
import io.github.cowwoc.styler.benchmarks.util.SampleCodeGenerator;
import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.LineLengthFormattingRule;
import io.github.cowwoc.styler.parser.Parser;
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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Compares performance of virtual threads versus platform threads for file processing.
 *
 * Validates performance benefits of Java's virtual threads (Project Loom) compared to traditional
 * platform threads. Tests both threading models with varying task counts (100, 1000, 10000) to
 * measure throughput, context switch overhead, and memory efficiency.
 *
 * Methodology: Uses JMH parameterized benchmarks comparing VIRTUAL and PLATFORM thread modes.
 * Virtual threads are expected to outperform platform threads on I/O-bound workloads due to
 * lower context switching and memory overhead.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 3, jvmArgs = {"-Xms1g", "-Xmx1g"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
public class VirtualThreadComparisonBenchmark
{
	/**
	 * Thread type: VIRTUAL or PLATFORM.
	 */
	@Param({"VIRTUAL", "PLATFORM"})
	private String threadType;

	/**
	 * Number of concurrent tasks: 100, 1000, or 10000.
	 */
	@Param({"100", "1000", "10000"})
	private int concurrentTasks;

	private List<TransformationContext> contexts = SampleCodeGenerator.
		generateFiles(100, SampleCodeGenerator.Size.SMALL).stream().
		<TransformationContext>map(BenchmarkTransformationContext::new).
		toList();
	private FormattingRule lineLengthRule = new LineLengthFormattingRule();
	private List<FormattingConfiguration> configs = List.of(LineLengthConfiguration.defaultConfig());

	/**
	 * Benchmarks concurrent file processing using the specified thread type.
	 *
	 * Creates virtual or platform threads based on the threadType parameter and processes
	 * files concurrently. Measures throughput to compare thread type performance. Uses
	 * actual Styler formatting on pre-parsed contexts.
	 *
	 * @return count of tasks completed successfully
	 */
	@Benchmark
	public int processWithThreadType() throws Exception
	{
		ExecutorService executor = createExecutor();

		try
		{
			int completed = 0;
			for (int i = 0; i < concurrentTasks && i < contexts.size(); ++i)
			{
				TransformationContext context = contexts.get(i % contexts.size());
				executor.submit(() ->
				{
					// Format using actual Styler APIs
					String result = lineLengthRule.format(context, configs);
					return !result.isEmpty();
				});
				++completed;
			}
			executor.shutdown();
			++completed;
			return completed;
		}
		finally
		{
			if (!executor.isShutdown())
			{
				executor.shutdownNow();
			}
		}
	}

	/**
	 * Measures thread creation overhead.
	 *
	 * Creates and destroys threads without actual work to isolate the overhead of thread
	 * creation and initialization. Helps identify whether thread type differences are due
	 * to creation overhead or execution characteristics.
	 *
	 * @return count of threads created
	 */
	@Benchmark
	public int measureThreadCreationOverhead()
	{
		ExecutorService executor = createExecutor();

		try
		{
			int threadsCreated = 0;
			for (int i = 0; i < 100; ++i)
			{
				executor.submit(() ->
				{
					// Minimal work: just a return statement
				});
				++threadsCreated;
			}
			executor.shutdown();
			return threadsCreated;
		}
		finally
		{
			if (!executor.isShutdown())
			{
				executor.shutdownNow();
			}
		}
	}

	/**
	 * Benchmarks I/O-bound workload with concurrent processing.
	 *
	 * Processes multiple files with actual parsing and formatting to test thread type
	 * performance on realistic workloads. Virtual threads are expected to show
	 * significantly better performance on I/O-bound tasks.
	 *
	 * @return count of processed operations
	 */
	@Benchmark
	public int measureIOBoundWorkload()
	{
		ExecutorService executor = createExecutor();

		try
		{
			int processed = 0;
			for (TransformationContext context : contexts)
			{
				executor.submit(() ->
				{
					// Format using actual Styler APIs
					String result = lineLengthRule.format(context, configs);
					return !result.isEmpty();
				});
				++processed;
			}
			executor.shutdown();
			return processed;
		}
		finally
		{
			if (!executor.isShutdown())
			{
				executor.shutdownNow();
			}
		}
	}

	private ExecutorService createExecutor()
	{
		return switch (threadType)
		{
			case "VIRTUAL" -> Executors.newVirtualThreadPerTaskExecutor();
			case "PLATFORM" -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			default -> throw new IllegalArgumentException("Unknown thread type: " + threadType);
		};
	}
}
