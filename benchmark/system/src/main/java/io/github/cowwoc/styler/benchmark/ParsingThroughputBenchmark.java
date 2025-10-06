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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Benchmarks parser throughput (tokens per second).
 * Measures how fast the lexer can tokenize Java source files.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(5)
@State(Scope.Thread)
public class ParsingThroughputBenchmark
{
	private List<Path> testFiles;

	/**
	 * Sets up test data before the benchmark trial.
	 *
	 * @throws IOException if test files cannot be loaded
	 */
	@Setup(Level.Trial)
	public void setup() throws IOException
	{
		testFiles = loadTestFiles();
	}

	/**
	 * Benchmarks parsing throughput on small files.
	 * Measures tokens processed per second.
	 *
	 * @return the number of tokens processed
	 * @throws IOException if file reading fails
	 */
	@Benchmark
	public long parseSmallFiles() throws IOException
	{
		long totalTokens = 0;

		for (Path file : testFiles)
		{
			String content = Files.readString(file);
			int tokens = countTokens(content);
			totalTokens += tokens;
		}

		return totalTokens;
	}

	/**
	 * Loads test files from the project's source code.
	 * Uses the project's own Java files as test data.
	 *
	 * @return list of test file paths
	 * @throws IOException if files cannot be accessed
	 */
	private List<Path> loadTestFiles() throws IOException
	{
		Path projectRoot = findProjectRoot();
		if (projectRoot == null)
		{
			throw new IOException("Project root not found");
		}

		Path srcDir = projectRoot.resolve("src");
		if (!Files.exists(srcDir))
		{
			throw new IOException("Source directory not found: " + srcDir);
		}

		List<Path> files = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(srcDir))
		{
			paths.filter(Files::isRegularFile)
				.filter(path -> path.toString().endsWith(".java"))
				.limit(10) // Limit to 10 files for fast benchmarking
				.forEach(files::add);
		}

		if (files.isEmpty())
		{
			throw new IOException("No Java files found in " + srcDir);
		}

		return files;
	}

	/**
	 * Finds the project root directory by looking for pom.xml.
	 *
	 * @return the project root path, or null if not found
	 */
	private Path findProjectRoot()
	{
		Path current = Paths.get("").toAbsolutePath();
		while (current != null)
		{
			if (Files.exists(current.resolve("pom.xml")))
			{
				return current;
			}
			current = current.getParent();
		}
		return null;
	}

	/**
	 * Counts the number of tokens in Java source code using the Styler lexer.
	 *
	 * <p><strong>TODO</strong>: This method requires integration with the Styler parser module.
	 * The benchmark module currently depends on styler-cli, but needs to depend on
	 * styler-parser to access the JavaLexer for actual token counting.
	 *
	 * <p>Required implementation:
	 * <pre>{@code
	 * Lexer lexer = new Lexer(content);
	 * int count = 0;
	 * while (lexer.hasNext()) {
	 *     lexer.next();
	 *     ++count;
	 * }
	 * return count;
	 * }</pre>
	 *
	 * @param content the Java source code
	 * @return the number of tokens parsed
	 */
	private int countTokens(String content)
	{
		throw new UnsupportedOperationException(
			"Token counting requires Styler parser integration. " +
			"Update module-info.java to 'requires io.github.cowwoc.styler.parser;' " +
			"and implement using JavaLexer. See JavaDoc for implementation example.");
	}
}
