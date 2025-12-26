package io.github.cowwoc.styler.benchmarks.memory;

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
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks memory usage and heap efficiency.
 *
 * <p>
 * Validates the performance claim of {@literal <=}512MB heap usage per 1000 files processed.
 * Run with JMH's GC profiler to capture allocation rates and GC overhead:
 * <pre>{@code
 * java -jar benchmarks.jar MemoryUsageBenchmark -prof gc
 * }</pre>
 *
 * <p>
 * Key metrics reported by the GC profiler:
 * <ul>
 *   <li>{@code gc.alloc.rate.norm} - bytes allocated per operation</li>
 *   <li>{@code gc.alloc.rate} - allocation rate in MB/sec</li>
 *   <li>{@code gc.count} / {@code gc.time} - GC frequency and pause time</li>
 * </ul>
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
	 * Processes files to measure memory allocation via JMH profiler.
	 *
	 * <p>
	 * Run with {@code -prof gc} to get allocation metrics. The profiler reports bytes allocated
	 * per operation ({@code gc.alloc.rate.norm}), which divided by {@code fileCount} gives
	 * per-file memory usage.
	 *
	 * @param blackhole JMH blackhole to prevent dead-code elimination
	 * @return count of processed files
	 */
	@Benchmark
	public int processFiles(Blackhole blackhole)
	{
		int processed = 0;
		for (String source : sourceFiles)
		{
			TransformationContext context = new BenchmarkTransformationContext(source);
			String result = lineLengthRule.format(context, configs);
			blackhole.consume(context);
			blackhole.consume(result);
			++processed;
		}
		return processed;
	}
}
