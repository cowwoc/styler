package io.github.cowwoc.styler.benchmarks.memory;

import io.github.cowwoc.styler.benchmarks.internal.BenchmarkTransformationContext;
import io.github.cowwoc.styler.benchmarks.util.SampleCodeGenerator;
import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.LineLengthFormattingRule;
import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
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
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks memory usage and heap efficiency.
 *
 * Validates the performance claim of <=512MB heap usage per 1000 files processed. Uses JMH's
 * GC profiler to capture garbage collection overhead and peak memory consumption. This benchmark
 * measures single-run behavior rather than throughput to assess memory characteristics accurately.
 *
 * Methodology: Uses SingleShotTime mode with multiple iterations (no warmup) to measure initial
 * and peak memory usage. Results help identify memory leaks and inefficient data structures.
 */
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 3, jvmArgs = {"-Xms64m", "-Xmx768m", "-XX:+UseG1GC"})
@Warmup(iterations = 0)
@Measurement(iterations = 5)
public class MemoryUsageBenchmark
{
	/**
	 * Number of files to process: 100, 500, or 1000.
	 */
	@Param({"100", "500", "1000"})
	private int fileCount;

	private List<String> sourceFiles;
	private FormattingRule lineLengthRule = new LineLengthFormattingRule();
	private List<FormattingConfiguration> configs = List.of(LineLengthConfiguration.defaultConfig());

	/**
	 * Generates sample source files for memory benchmarking.
	 */
	@Setup(Level.Trial)
	public void setup()
	{
		sourceFiles = SampleCodeGenerator.generateFiles(fileCount, SampleCodeGenerator.Size.MEDIUM);
	}

	/**
	 * Measures peak heap memory usage when processing files.
	 *
	 * Tests memory efficiency by tracking heap consumption before and after processing a batch
	 * of files. Uses explicit garbage collection to isolate memory consumed by the processing
	 * operation itself.
	 *
	 * @param blackhole JMH blackhole to prevent dead-code elimination
	 * @return peak memory bytes consumed
	 */
	@Benchmark
	public long measurePeakMemory(Blackhole blackhole)
	{
		Runtime runtime = Runtime.getRuntime();
		System.gc();
		long beforeMemory = runtime.totalMemory() - runtime.freeMemory();

		// Parse and format files using actual Styler APIs
		List<TransformationContext> contexts = new ArrayList<>(sourceFiles.size());
		for (String source : sourceFiles)
		{
			TransformationContext context = new BenchmarkTransformationContext(source);
			contexts.add(context);
			String result = lineLengthRule.format(context, configs);
			blackhole.consume(result);
		}
		blackhole.consume(contexts);

		System.gc();
		long afterMemory = runtime.totalMemory() - runtime.freeMemory();
		return afterMemory - beforeMemory;
	}

	/**
	 * Measures average memory per file.
	 *
	 * Divides peak memory usage by file count to understand per-file memory footprint.
	 *
	 * @param blackhole JMH blackhole to prevent dead-code elimination
	 * @return average memory bytes per file
	 */
	@Benchmark
	public long measureAverageMemoryPerFile(Blackhole blackhole)
	{
		long peakMemory = measurePeakMemory(blackhole);
		if (fileCount > 0)
		{
			return peakMemory / fileCount;
		}
		return 0;
	}
}
