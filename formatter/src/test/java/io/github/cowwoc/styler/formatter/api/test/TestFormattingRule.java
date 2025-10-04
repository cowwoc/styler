package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.FormattingRule;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ValidationResult;

import java.time.Duration;

/**
 * Concrete implementation of FormattingRule for testing interface contracts.
 * <p>
 * This class provides a configurable test double (not a mock) that implements
 * the FormattingRule interface for validating contract behavior.
 */
public class TestFormattingRule implements FormattingRule
{
	private final String ruleId;
	private final int priority;
	private final RuleConfiguration defaultConfig;
	private final Duration maxExecutionTime;
	private final long maxMemoryUsage;

	/**
	 * Creates a test formatting rule with specified parameters.
	 *
	 * @param ruleId the rule identifier
	 * @param priority the rule priority
	 */
	public TestFormattingRule(String ruleId, int priority)
	{
		this.ruleId = ruleId;
		this.priority = priority;
		this.defaultConfig = TestUtilities.createTestConfiguration();
		this.maxExecutionTime = Duration.ofSeconds(5);
		this.maxMemoryUsage = 100 * 1024 * 1024; // 100MB
	}

	@Override
	public String getRuleId()
	{
		return ruleId;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public RuleConfiguration getDefaultConfiguration()
	{
		return defaultConfig;
	}

	@Override
	public Duration getMaxExecutionTime()
	{
		return maxExecutionTime;
	}

	@Override
	public long getMaxMemoryUsage()
	{
		return maxMemoryUsage;
	}

	@Override
	public ValidationResult validate(FormattingContext context)
	{
		if (context == null)
		{
			return ValidationResult.failure("Context cannot be null");
		}
		if (context.getRootNode() == null)
		{
			return ValidationResult.failure("Root node cannot be null");
		}
		if (context.getSourceText() == null || context.getSourceText().isEmpty())
		{
			return ValidationResult.failure("Source text cannot be null or empty");
		}
		return ValidationResult.success();
	}

	@Override
	public FormattingResult apply(FormattingContext context)
	{
		ValidationResult validationResult = validate(context);
		if (validationResult.isFailure())
		{
			return FormattingResult.empty();
		}

		// Simple test behavior: return empty result (no edits)
		// Individual tests can verify this behavior
		return FormattingResult.empty();
	}
}
