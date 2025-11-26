package io.github.cowwoc.styler.formatter;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Default immutable implementation of FormattingViolation.
 *
 * @param ruleId         the rule that detected this violation
 * @param severity       the severity level of this violation
 * @param message        a human-readable message describing the violation
 * @param filePath       the source file path where the violation occurred
 * @param startPosition  the start position (character offset) in the source
 * @param endPosition    the end position (character offset) in the source
 * @param lineNumber     the line number (1-based) where the violation starts
 * @param columnNumber   the column number (1-based) where the violation starts
 * @param nodeIndex      the AST node associated with this violation, if available
 * @param suggestedFixes suggested fixes for this violation, ordered by priority
 */
public record DefaultFormattingViolation(
	String ruleId,
	ViolationSeverity severity,
	String message,
	Path filePath,
	int startPosition,
	int endPosition,
	int lineNumber,
	int columnNumber,
	Optional<NodeIndex> nodeIndex,
	List<FixStrategy> suggestedFixes) implements FormattingViolation
{
	/**
	 * Creates a formatting violation with validated parameters.
	 */
	public DefaultFormattingViolation
	{
		requireThat(ruleId, "ruleId").isNotEmpty().isStripped();
		requireThat(severity, "severity").isNotNull();
		requireThat(message, "message").isNotEmpty().isStripped();
		requireThat(filePath, "filePath").isNotNull();
		requireThat(startPosition, "startPosition").isNotNegative();
		requireThat(endPosition, "endPosition").isGreaterThanOrEqualTo(startPosition);
		requireThat(lineNumber, "lineNumber").isPositive();
		requireThat(columnNumber, "columnNumber").isPositive();
		requireThat(nodeIndex, "nodeIndex").isNotNull();
		requireThat(suggestedFixes, "suggestedFixes").isNotNull();
		suggestedFixes = List.copyOf(suggestedFixes);
	}
}
