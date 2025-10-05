package io.github.cowwoc.styler.plugin.engine;

import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.FormattingRule;
import io.github.cowwoc.styler.formatter.api.FormattingViolation;
import io.github.cowwoc.styler.plugin.config.PluginConfiguration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Check-only strategy that validates source files without modifying them.
 * Reports violations found by formatting rules.
 * Thread-safe and stateless for Maven parallel builds.
 */
public final class ValidationStrategy extends AbstractProcessingStrategy
{
	/**
	 * Creates a validation strategy with required components.
	 *
	 * @param config plugin configuration containing Maven project metadata
	 * @param parser AST parser for source files
	 * @param contextBuilder creates formatting contexts
	 * @param ruleLoader loads formatting rules
	 * @throws NullPointerException if any parameter is null
	 */
	public ValidationStrategy(PluginConfiguration config, SourceParser parser,
		FormattingContextBuilder contextBuilder, FormattingRuleLoader ruleLoader)
	{
		super(config, parser, contextBuilder, ruleLoader);
	}

	@Override
	protected ResultCollector createResultCollector()
	{
		return new ViolationCollector();
	}

	@Override
	protected ProcessingResult processResults(ResultCollector collector, Path sourcePath,
		String sourceText)
	{
		ViolationCollector violationCollector = (ViolationCollector) collector;
		List<String> violations = violationCollector.getViolations();

		if (violations.isEmpty())
		{
			return ProcessingResult.clean();
		}
		return ProcessingResult.withViolations(violations);
	}

	@Override
	public String getDescription()
	{
		return "Check for formatting violations only (no modifications)";
	}

	/**
	 * Collects violations from formatting rule applications.
	 */
	private static final class ViolationCollector implements ResultCollector
	{
		private final List<String> violations = new ArrayList<>();

		@Override
		public void collect(FormattingRule rule, FormattingResult result)
		{
			for (FormattingViolation violation : result.getViolations())
			{
				String message = String.format("[%s] %s at %s: %s",
					rule.getRuleId(),
					violation.getSeverity(),
					violation.getLocation(),
					violation.getMessage());
				violations.add(message);
			}
		}

		/**
		 * Gets all collected violations.
		 *
		 * @return list of violation messages
		 */
		public List<String> getViolations()
		{
			return violations;
		}
	}
}
