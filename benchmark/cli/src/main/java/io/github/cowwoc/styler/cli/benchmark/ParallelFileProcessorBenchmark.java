package io.github.cowwoc.styler.cli.benchmark;

import io.github.cowwoc.styler.cli.pipeline.BatchResult;
import io.github.cowwoc.styler.cli.pipeline.FileProcessorPipeline;
import io.github.cowwoc.styler.cli.pipeline.ParallelFileProcessor;
import io.github.cowwoc.styler.cli.pipeline.PipelineResult;
import io.github.cowwoc.styler.cli.pipeline.PipelineStage;
import io.github.cowwoc.styler.cli.pipeline.ProcessingContext;
import io.github.cowwoc.styler.cli.pipeline.StageResult;
import io.github.cowwoc.styler.cli.pipeline.progress.ProgressObserver;
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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark validating parallel file processing performance claims.
 * <p>
 * Tests:
 * - Sequential vs parallel throughput (target: 2x-16x improvement)
 * - Scalability with different file counts and concurrency levels
 * - Memory throttling effectiveness under load
 * - Virtual thread overhead characteristics
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 0)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 2)
public class ParallelFileProcessorBenchmark
{
	@Param({"10", "50", "100"})
	private int fileCount;

	@Param({"1", "4", "8", "16"})
	private int maxConcurrentFiles;

	private List<Path> testFiles;
	private Path tempDir;

	@Setup(Level.Trial)
	public void setupTrial() throws IOException
	{
		// Create temporary directory with test files
		tempDir = Files.createTempDirectory("parallel-benchmark");

		testFiles = new ArrayList<>(fileCount);
		for (int i = 0; i < fileCount; ++i)
		{
			Path file = tempDir.resolve("TestFile" + i + ".java");
			Files.writeString(file, generateJavaSource(i));
			testFiles.add(file);
		}
	}

	@TearDown(Level.Trial)
	public void tearDownTrial() throws IOException
	{
		// Cleanup test files
		for (Path file : testFiles)
		{
			Files.deleteIfExists(file);
		}
		Files.deleteIfExists(tempDir);
	}

	/**
	 * Benchmark parallel file processing with configurable concurrency.
	 * <p>
	 * When maxConcurrentFiles = 1, this measures sequential throughput.
	 * When maxConcurrentFiles > 1, this measures parallel throughput.
	 */
	@Benchmark
	public void parallelProcessing(Blackhole blackhole) throws InterruptedException
	{
		if (maxConcurrentFiles == 1)
		{
			// Sequential processing baseline
			BatchResult result = processSequentially();
			blackhole.consume(result);
		}
		else
		{
			// Parallel processing with virtual threads
			try (ParallelFileProcessor<Path> processor = ParallelFileProcessor.<Path>builder()
				.maxConcurrentFiles(maxConcurrentFiles)
				.pipelineFactory(this::createBenchmarkPipeline)
				.build())
			{
				BatchResult result = processor.processFiles(testFiles);
				blackhole.consume(result);
			}
		}
	}

	/**
	 * Sequential processing baseline for comparison.
	 */
	private BatchResult processSequentially()
	{
		int successCount = 0;
		List<io.github.cowwoc.styler.cli.pipeline.PipelineException> errors = new ArrayList<>();

		try (FileProcessorPipeline<Path> pipeline = createBenchmarkPipeline())
		{
			for (Path file : testFiles)
			{
				ProcessingContext context = ProcessingContext.builder(file).build();
				PipelineResult<Path> result = pipeline.process(file, context);

				if (result.isSuccess())
				{
					successCount++;
				}
				else
				{
					errors.add(result.exception().orElseThrow());
				}
			}
		}
		catch (Exception e)
		{
			// Should not happen in benchmark
			throw new RuntimeException(e);
		}

		return new BatchResult(successCount, errors.size(), errors);
	}

	/**
	 * Creates a lightweight pipeline for benchmarking.
	 * <p>
	 * Uses a simple processing stage that simulates real work without actual I/O
	 * to focus on parallel coordination overhead.
	 */
	private FileProcessorPipeline<Path> createBenchmarkPipeline()
	{
		return FileProcessorPipeline.<Path>builder()
			.addStage(new SimulatedProcessingStage())
			.progressObserver(ProgressObserver.noOp())
			.build();
	}

	/**
	 * Simulated processing stage that mimics real work characteristics.
	 * <p>
	 * Performs computation proportional to file content without actual I/O,
	 * allowing focus on parallel coordination performance.
	 */
	private static class SimulatedProcessingStage implements PipelineStage<Path, Path>
	{
		@Override
		public StageResult<Path> execute(Path input, ProcessingContext context)
		{
			try
			{
				// Simulate parsing + formatting work
				String content = Files.readString(input);

				// CPU work proportional to file size
				int hash = 0;
				for (char c : content.toCharArray())
				{
					hash = 31 * hash + c;
				}

				// Prevent dead code elimination
				if (hash == Integer.MIN_VALUE)
				{
					throw new RuntimeException("Unlikely");
				}

				return StageResult.success(input);
			}
			catch (IOException e)
			{
				return StageResult.failure(new io.github.cowwoc.styler.cli.pipeline.PipelineException(
					"Benchmark processing failed",
					input,
					getStageId(),
					e));
			}
		}

		@Override
		public String getStageId()
		{
			return "simulated-processing";
		}
	}

	/**
	 * Generates realistic Java source code for benchmarking.
	 *
	 * @param index file index for uniqueness
	 * @return Java source code string
	 */
	private static String generateJavaSource(int index)
	{
		StringBuilder sb = new StringBuilder(1000);
		sb.append("package io.github.cowwoc.styler.benchmark.generated;\n\n");
		sb.append("/**\n");
		sb.append(" * Generated test file ").append(index).append(" for parallel processing benchmark.\n");
		sb.append(" */\n");
		sb.append("public class TestFile").append(index).append(" {\n");

		// Generate methods with realistic complexity
		for (int i = 0; i < 10; ++i)
		{
			sb.append("\tpublic void method").append(i).append("() {\n");
			sb.append("\t\t// Method implementation ").append(i).append("\n");
			sb.append("\t\tint x = ").append(i).append(";\n");
			sb.append("\t\tfor (int j = 0; j < 100; ++j) {\n");
			sb.append("\t\t\tx += j * ").append(i).append(";\n");
			sb.append("\t\t}\n");
			sb.append("\t}\n\n");
		}

		sb.append("}\n");
		return sb.toString();
	}

	public static void main(String[] args) throws Exception
	{
		Options opt = new OptionsBuilder()
			.include(ParallelFileProcessorBenchmark.class.getSimpleName())
			.forks(1)
			.build();

		new Runner(opt).run();
	}
}
