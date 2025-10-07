package io.github.cowwoc.styler.formatter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Analyzes brace contexts for style violations using configurable strategies.
 *
 * <p>This analyzer orchestrates brace style strategies (K&amp;R, Allman, GNU) to detect formatting
 * violations. It selects the appropriate strategy based on node category and configuration, then
 * analyzes both opening and closing brace placement.
 *
 * <p><strong>Strategy Selection:</strong>
 * <ul>
 *   <li>Class declarations use configured class brace style</li>
 *   <li>Method/constructor declarations use configured method brace style</li>
 *   <li>Control structures use configured control brace style</li>
 *   <li>Lambda/anonymous class/empty blocks use general brace style</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> This class is stateless and thread-safe, provided the strategy
 * implementations are also thread-safe.
 */
public final class BraceStyleAnalyzer
{
	private final Map<String, BraceStyleStrategy> strategies;
	private final String sourceText;
	private final IndentationCalculator indentationCalculator;

	/**
	 * Creates a new brace style analyzer.
	 *
	 * @param strategies map of configuration keys to brace style strategies, never {@code null}
	 * @param sourceText the source code text being analyzed, never {@code null}
	 * @param indentationCalculator calculator for indentation levels, never {@code null}
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	public BraceStyleAnalyzer(Map<String, BraceStyleStrategy> strategies, String sourceText,
		IndentationCalculator indentationCalculator)
	{
		requireThat(strategies, "strategies").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();
		requireThat(indentationCalculator, "indentationCalculator").isNotNull();

		this.strategies = Map.copyOf(strategies);
		this.sourceText = sourceText;
		this.indentationCalculator = indentationCalculator;
	}

	/**
	 * Analyzes a single brace context for style violations.
	 *
	 * <p>This method selects the appropriate strategy based on the node category, then checks both
	 * opening and closing brace placement. Returns a list of violations found (may be empty).
	 *
	 * @param context the brace context to analyze, never {@code null}
	 * @return list of violations found (empty if no violations), never {@code null}
	 * @throws NullPointerException if {@code context} is {@code null}
	 */
	public List<BraceViolation> analyzeNode(BraceContext context)
	{
		requireThat(context, "context").isNotNull();

		List<BraceViolation> violations = new ArrayList<>();

		// Select strategy based on node category
		String configKey = context.category().getConfigurationKey();
		BraceStyleStrategy strategy = strategies.get(configKey);

		if (strategy == null)
		{
			// No strategy configured for this category - skip analysis
			return violations;
		}

		// Analyze opening brace
		if (!strategy.isOpeningBraceCorrect(context, sourceText, indentationCalculator))
		{
			BraceViolation violation = new BraceViolation(
				context,
				"opening",
				strategy.getStyleName(),
				describeExpectedOpeningBrace(strategy, context),
				describeActualOpeningBrace(context),
				context.openingBraceRange());
			violations.add(violation);
		}

		// Analyze closing brace
		if (!strategy.isClosingBraceCorrect(context, sourceText, indentationCalculator))
		{
			BraceViolation violation = new BraceViolation(
				context,
				"closing",
				strategy.getStyleName(),
				describeExpectedClosingBrace(strategy, context),
				describeActualClosingBrace(context),
				context.closingBraceRange());
			violations.add(violation);
		}

		return violations;
	}

	/**
	 * Describes the expected placement for an opening brace according to the strategy.
	 *
	 * @param strategy the brace style strategy, never {@code null}
	 * @param context the brace context, never {@code null}
	 * @return human-readable description of expected placement
	 */
	// Context reserved for context-specific descriptions
	@SuppressWarnings("PMD.UnusedFormalParameter")
	private String describeExpectedOpeningBrace(BraceStyleStrategy strategy, BraceContext context)
	{
		return strategy.getOpeningBraceDescription();
	}

	/**
	 * Describes the expected placement for a closing brace according to the strategy.
	 *
	 * @param strategy the brace style strategy, never {@code null}
	 * @param context the brace context, never {@code null}
	 * @return human-readable description of expected placement
	 */
	// Parameters reserved for context-specific descriptions
	@SuppressWarnings("PMD.UnusedFormalParameter")
	private String describeExpectedClosingBrace(BraceStyleStrategy strategy, BraceContext context)
	{
		return strategy.getClosingBraceDescription();
	}

	/**
	 * Describes the actual placement of an opening brace.
	 *
	 * @param context the brace context, never {@code null}
	 * @return human-readable description of actual placement
	 */
	private String describeActualOpeningBrace(BraceContext context)
	{
		int declarationLine = context.node().getRange().start().line();
		int braceLine = context.getOpeningBraceLine();

		if (braceLine == declarationLine)
		{
			return "same line as declaration";
		}
		return "new line";
	}

	/**
	 * Describes the actual placement of a closing brace.
	 *
	 * @param context the brace context, never {@code null}
	 * @return human-readable description of actual placement
	 */
	private String describeActualClosingBrace(BraceContext context)
	{
		if (context.isSingleLineBraces())
		{
			return "same line as opening brace";
		}
		return "new line";
	}
}
