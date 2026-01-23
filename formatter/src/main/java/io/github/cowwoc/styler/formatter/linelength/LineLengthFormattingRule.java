package io.github.cowwoc.styler.formatter.linelength;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.linelength.internal.ContextDetector;
import io.github.cowwoc.styler.formatter.linelength.internal.LineAnalyzer;
import io.github.cowwoc.styler.formatter.linelength.internal.LineWrapper;

import io.github.cowwoc.styler.formatter.RuleExample;
import io.github.cowwoc.styler.formatter.RuleProperty;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Formatting rule for enforcing line length limits with context-aware wrapping.
 * <p>
 * This rule detects lines exceeding the configured maximum length and can apply
 * context-specific wrapping strategies based on the AST context of each position.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class LineLengthFormattingRule implements FormattingRule
{
	/**
	 * Unique identifier for this rule.
	 */
	public static final String RULE_ID = "line-length";

	/**
	 * Default maximum line length (120 characters).
	 */
	public static final int DEFAULT_MAX_LENGTH = 120;

	/**
	 * Human-readable name for this rule.
	 */
	private static final String RULE_NAME = "Line Length";

	/**
	 * Description of what this rule checks.
	 */
	private static final String RULE_DESCRIPTION =
		"Enforces maximum line length with context-aware wrapping strategies";

	/**
	 * Creates a new line length formatting rule instance.
	 */
	public LineLengthFormattingRule()
	{
		// No-arg constructor for instantiation
	}

	@Override
	public String getId()
	{
		return RULE_ID;
	}

	@Override
	public String getName()
	{
		return RULE_NAME;
	}

	@Override
	public String getDescription()
	{
		return RULE_DESCRIPTION;
	}

	@Override
	public ViolationSeverity getDefaultSeverity()
	{
		return ViolationSeverity.WARNING;
	}

	@Override
	public List<RuleExample> getExamples()
	{
		return List.of(
			new RuleExample(
				"Method call chain wrapping",
				"""
					String result = builder.append("hello").append("world").append("foo").append("bar").toString();""",
				"""
					String result = builder.append("hello").
						append("world").
						append("foo").
						append("bar").
						toString();"""));
	}

	@Override
	public List<RuleProperty> getProperties()
	{
		return List.of(
			new RuleProperty(
				"maxLineLength",
				"int",
				"120",
				"Maximum allowed line length"),
			new RuleProperty(
				"methodChainWrap",
				"WrapStyle",
				"AFTER",
				"Wrap method chains after the dot"));
	}

	@Override
	public List<FormattingViolation> analyze(TransformationContext context,
		List<FormattingConfiguration> configs)
	{
		requireThat(context, "context").isNotNull();
		requireThat(configs, "configs").isNotNull();

		LineLengthConfiguration lineConfig = FormattingConfiguration.findConfig(
			configs, LineLengthConfiguration.class, LineLengthConfiguration.defaultConfig());

		return LineAnalyzer.analyze(context, lineConfig);
	}

	@Override
	public String format(TransformationContext context, List<FormattingConfiguration> configs)
	{
		requireThat(context, "context").isNotNull();
		requireThat(configs, "configs").isNotNull();

		LineLengthConfiguration lineConfig = FormattingConfiguration.findConfig(
			configs, LineLengthConfiguration.class, LineLengthConfiguration.defaultConfig());

		// Create context detector for AST-based analysis
		ContextDetector detector = new ContextDetector(context);
		LineWrapper wrapper = new LineWrapper(detector, context, lineConfig);

		// Process each line
		String sourceCode = context.sourceCode();
		String[] lines = sourceCode.split("\n", -1);  // Keep trailing empty lines
		StringBuilder result = new StringBuilder();

		int currentPosition = 0;
		for (int i = 0; i < lines.length; ++i)
		{
			String line = lines[i];
			String wrappedLine = wrapper.wrapLine(line, currentPosition);
			result.append(wrappedLine);

			if (i < lines.length - 1)
				result.append('\n');

			// Update position: original line length plus newline
			currentPosition += line.length() + 1;
		}

		return result.toString();
	}
}
