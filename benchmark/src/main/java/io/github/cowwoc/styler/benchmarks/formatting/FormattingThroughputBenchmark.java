package io.github.cowwoc.styler.benchmarks.formatting;

import io.github.cowwoc.styler.benchmarks.internal.BenchmarkTransformationContext;
import io.github.cowwoc.styler.benchmarks.util.SampleCodeGenerator;
import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.brace.BraceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.brace.BraceFormattingRule;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingConfiguration;
import io.github.cowwoc.styler.formatter.indentation.IndentationFormattingRule;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks formatting throughput with various rule configurations.
 *
 * Validates the performance claim of >=100 files/second formatting throughput. Tests formatting
 * with different rule sets (line length, brace style, indentation, or all combined) to measure
 * throughput across different formatter complexity levels.
 *
 * Methodology: Uses JMH throughput mode with 3 forks, 5 warmup iterations, and 10 measurement
 * iterations. Tests are performed on in-memory representations of source files to isolate
 * formatter performance from I/O overhead.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 3, jvmArgs = {"-Xms512m", "-Xmx512m"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
public class FormattingThroughputBenchmark
{
	/**
	 * Rule set to apply: lineLength, braceStyle, indentation, or all.
	 */
	@Param({"lineLength", "braceStyle", "indentation", "all"})
	private String ruleSet;

	private List<TransformationContext> contexts = SampleCodeGenerator.
		generateFiles(50, SampleCodeGenerator.Size.MEDIUM).stream().
		<TransformationContext>map(BenchmarkTransformationContext::new).
		toList();
	private FormattingRule lineLengthRule = new LineLengthFormattingRule();
	private FormattingRule braceRule = new BraceFormattingRule();
	private FormattingRule indentationRule = new IndentationFormattingRule();
	private List<FormattingConfiguration> lineLengthConfigs =
		List.of(LineLengthConfiguration.defaultConfig());
	private List<FormattingConfiguration> braceConfigs =
		List.of(BraceFormattingConfiguration.defaultConfig());
	private List<FormattingConfiguration> indentationConfigs =
		List.of(IndentationFormattingConfiguration.defaultConfig());
	private List<FormattingConfiguration> allConfigs = List.of(
		LineLengthConfiguration.defaultConfig(),
		BraceFormattingConfiguration.defaultConfig(),
		IndentationFormattingConfiguration.defaultConfig());

	/**
	 * Benchmarks formatting with specified rule set.
	 *
	 * Processes a batch of files through the actual Styler formatter with the configured rule set.
	 * The rule set parameter determines which formatting rules are applied: single rules test
	 * focused performance, while "all" tests maximum formatter complexity.
	 *
	 * @return count of successfully formatted files
	 */
	@Benchmark
	public int formatFiles()
	{
		int formatted = 0;
		for (TransformationContext context : contexts)
		{
			String result = switch (ruleSet)
			{
				case "lineLength" -> lineLengthRule.format(context, lineLengthConfigs);
				case "braceStyle" -> braceRule.format(context, braceConfigs);
				case "indentation" -> indentationRule.format(context, indentationConfigs);
				case "all" -> applyAllRules(context);
				default -> throw new IllegalArgumentException("Unknown rule set: " + ruleSet);
			};
			if (!result.isEmpty())
			{
				++formatted;
			}
		}
		return formatted;
	}

	private String applyAllRules(TransformationContext context)
	{
		// Apply all formatting rules in sequence using actual Styler formatters
		String result = lineLengthRule.format(context, allConfigs);
		// Create new context for each subsequent rule with the formatted result
		TransformationContext lineLengthFormatted = new BenchmarkTransformationContext(result);
		result = braceRule.format(lineLengthFormatted, allConfigs);
		TransformationContext braceFormatted = new BenchmarkTransformationContext(result);
		return indentationRule.format(braceFormatted, allConfigs);
	}
}
