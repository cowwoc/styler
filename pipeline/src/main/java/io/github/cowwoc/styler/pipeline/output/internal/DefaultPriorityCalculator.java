package io.github.cowwoc.styler.pipeline.output.internal;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.pipeline.output.PriorityCalculator;

/**
 * Default implementation of PriorityCalculator that counts violations per rule.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class DefaultPriorityCalculator implements PriorityCalculator
{
	/**
	 * Creates a DefaultPriorityCalculator.
	 */
	public DefaultPriorityCalculator()
	{
	}

	@Override
	public Map<String, Integer> countByRule(List<FormattingViolation> violations)
	{
		requireThat(violations, "violations").isNotNull();

		if (violations.isEmpty())
		{
			return Map.of();
		}

		Map<String, Integer> counts = violations.stream().
			collect(Collectors.groupingBy(
				FormattingViolation::ruleId,
				Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

		return Map.copyOf(counts);
	}
}
