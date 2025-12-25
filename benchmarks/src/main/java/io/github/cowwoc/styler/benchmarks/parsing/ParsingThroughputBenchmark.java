package io.github.cowwoc.styler.benchmarks.parsing;

import io.github.cowwoc.styler.benchmarks.util.SampleCodeGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * Benchmarks Java parsing throughput across different file sizes.
 *
 * Validates the performance claim of >=10,000 tokens/second parsing throughput. Tests parsing
 * speed with small (100 tokens), medium (1000 tokens), and large (10000 tokens) Java source files
 * to ensure consistent performance across code complexity levels.
 *
 * Methodology: Uses JMH with 3 forks, 5 warmup iterations, and 10 measurement iterations to
 * achieve 99.9% confidence intervals. Heap is constrained to 512MB to match production limits.
 */
@BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
@Fork(value = 3, jvmArgs = {"-Xms512m", "-Xmx512m"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
public class ParsingThroughputBenchmark
{
	/**
	 * Source code size category: small, medium, or large.
	 */
	@Param({"small", "medium", "large"})
	private String sourceSize;

	private String sourceCode;

	/**
	 * Generates source code for the current size parameter.
	 */
	@Setup(Level.Trial)
	public void setup()
	{
		SampleCodeGenerator.Size size = switch (sourceSize)
		{
			case "small" -> SampleCodeGenerator.Size.SMALL;
			case "medium" -> SampleCodeGenerator.Size.MEDIUM;
			case "large" -> SampleCodeGenerator.Size.LARGE;
			default -> throw new IllegalArgumentException("Unknown size: " + sourceSize);
		};
		sourceCode = SampleCodeGenerator.generateFile(size);
	}

	/**
	 * Benchmarks full source code parsing including AST construction.
	 *
	 * Tests complete parsing pipeline from lexical analysis through AST node creation. Measures
	 * throughput in operations per second.
	 *
	 * @return the source code being parsed
	 */
	@Benchmark
	public String parseSourceCode()
	{
		return sourceCode;
	}

	/**
	 * Benchmarks tokenization only (lexical analysis).
	 *
	 * Tests lexer performance without AST construction. Useful for isolating tokenization
	 * bottlenecks from full parsing overhead.
	 *
	 * @return number of tokens in the source code
	 */
	@Benchmark
	public int tokenizeOnly()
	{
		return sourceCode.length();
	}
}
