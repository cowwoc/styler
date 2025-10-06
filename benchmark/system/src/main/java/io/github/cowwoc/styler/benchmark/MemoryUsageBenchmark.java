package io.github.cowwoc.styler.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks memory usage for parser operations.
 * Measures heap consumption while processing multiple files.
 */
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
public class MemoryUsageBenchmark
{
	private List<Path> testFiles;
	private Runtime runtime;

	/**
	 * Sets up test data and runtime for memory measurements.
	 *
	 * @throws IOException if test files cannot be loaded
	 */
	@Setup(Level.Trial)
	public void setup() throws IOException
	{
		TestDataProvider provider = new TestDataProvider();
		testFiles = provider.loadTestFiles(100);
		runtime = Runtime.getRuntime();
	}

	/**
	 * Benchmarks memory usage for parsing multiple files.
	 * Tracks heap usage before and after parsing.
	 *
	 * @return memory delta in bytes
	 * @throws IOException if file reading fails
	 */
	@Benchmark
	public long measureParsingMemory() throws IOException
	{
		// Force GC and measure baseline
		runtime.gc();
		long beforeMemory = runtime.totalMemory() - runtime.freeMemory();

		// Parse all files
		for (Path file : testFiles)
		{
			String content = Files.readString(file);
			TokenCountingUtil.countTokens(content);
		}

		// Measure memory after parsing
		long afterMemory = runtime.totalMemory() - runtime.freeMemory();

		return afterMemory - beforeMemory;
	}
}
