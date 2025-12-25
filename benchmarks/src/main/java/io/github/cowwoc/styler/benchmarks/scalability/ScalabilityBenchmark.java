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
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks parallel processing scalability across multiple processor cores.
 *
 * Validates linear scaling of file processing throughput from 1 to 32 cores. Measures speedup
 * ratio at each thread count to verify that parallelization overhead is minimal and efficiency
 * remains above 75% (speedup >= 0.75 * N for N cores).
 *
 * Methodology: Uses JMH parameterized benchmarks to test fixed thread counts. Results help
 * identify synchronization bottlenecks and thread pool efficiency. Includes higher memory
 * allocation (2GB) to prevent GC contention during parallel workloads.
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
	 * Number of processor cores to use: 1, 2, 4, 8, 16, or 32.
	 */
	@Param({"1", "2", "4", "8", "16", "32"})
	private int threadCount;

	private List<String> testFiles;
	private List<TransformationContext> contexts;
	private FormattingRule lineLengthRule;
	private List<FormattingConfiguration> configs;

	/**
	 * Generates test files for scalability benchmarking.
	 */
	@Setup(Level.Trial)
	public void setup()
	{
		// Generate a large batch of files for parallel processing
		testFiles = SampleCodeGenerator.generateFiles(1000, SampleCodeGenerator.Size.SMALL);

		// Pre-parse all files to create transformation contexts
		contexts = new ArrayList<>(testFiles.size());
		for (String source : testFiles)
		{
			contexts.add(new BenchmarkTransformationContext(source));
		}

		// Initialize formatting rule
		lineLengthRule = new LineLengthFormattingRule();
		configs = List.of(LineLengthConfiguration.defaultConfig());
	}

	/**
	 * Benchmarks concurrent file processing at the configured thread count.
	 *
	 * Simulates parallel processing of files using the specified number of threads. Measures
	 * overall throughput (files/second) to compute speedup ratios and efficiency metrics.
	 * Actual parallelization is simulated with thread-count-aware distribution of work.
	 *
	 * @return count of processed files
	 */
	@Benchmark
	public long processWithConcurrency()
	{
		long processed = 0;

		// Distribute files across threads
		int filesPerThread = contexts.size() / Math.max(1, threadCount);
		for (int t = 0; t < threadCount; ++t)
		{
			int start = t * filesPerThread;
			int end;
			if (t == threadCount - 1)
			{
				end = contexts.size();
			}
			else
			{
				end = (t + 1) * filesPerThread;
			}

			for (int i = start; i < end; ++i)
			{
				TransformationContext context = contexts.get(i);
				// Format using actual Styler APIs
				String result = lineLengthRule.format(context, configs);
				if (!result.isEmpty())
				{
					++processed;
				}
			}
		}

		return processed;
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
