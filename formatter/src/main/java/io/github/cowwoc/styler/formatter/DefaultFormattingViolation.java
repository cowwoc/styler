package io.github.cowwoc.styler.formatter;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Default immutable implementation of FormattingViolation.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class DefaultFormattingViolation implements FormattingViolation
{
	private final String ruleId;
	private final ViolationSeverity severity;
	private final String message;
	private final Path filePath;
	private final int startPosition;
	private final int endPosition;
	private final int lineNumber;
	private final int columnNumber;
	private final NodeIndex nodeIndex;
	private final List<FixStrategy> suggestedFixes;

	/**
	 * Creates a formatting violation without an associated AST node.
	 *
	 * @param ruleId         the rule that detected this violation
	 * @param severity       the severity level
	 * @param message        a human-readable message describing the violation
	 * @param filePath       the source file path
	 * @param startPosition  the start position (character offset)
	 * @param endPosition    the end position (character offset)
	 * @param lineNumber     the line number (1-based)
	 * @param columnNumber   the column number (1-based)
	 * @param suggestedFixes suggested fixes for this violation
	 * @throws NullPointerException     if any of the arguments are {@code null}
	 * @throws IllegalArgumentException <ul>
	 *                                    <li>if {@code ruleId} or {@code message} are empty or contain
	 *                                    leading/trailing whitespace</li>
	 *                                    <li>if {@code startPosition} is negative</li>
	 *                                    <li>if {@code endPosition} is less than {@code startPosition}</li>
	 *                                    <li>if {@code lineNumber} or {@code columnNumber} is not positive</li>
	 *                                  </ul>
	 */
	public DefaultFormattingViolation(
		String ruleId,
		ViolationSeverity severity,
		String message,
		Path filePath,
		int startPosition,
		int endPosition,
		int lineNumber,
		int columnNumber,
		List<FixStrategy> suggestedFixes)
	{
		validateCommonParameters(ruleId, severity, message, filePath, startPosition, endPosition,
			lineNumber, columnNumber, suggestedFixes);

		this.ruleId = ruleId;
		this.severity = severity;
		this.message = message;
		this.filePath = filePath;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.nodeIndex = null;
		this.suggestedFixes = List.copyOf(suggestedFixes);
	}

	/**
	 * Creates a formatting violation with an associated AST node.
	 *
	 * @param ruleId         the rule that detected this violation
	 * @param severity       the severity level
	 * @param message        a human-readable message describing the violation
	 * @param filePath       the source file path
	 * @param startPosition  the start position (character offset)
	 * @param endPosition    the end position (character offset)
	 * @param lineNumber     the line number (1-based)
	 * @param columnNumber   the column number (1-based)
	 * @param nodeIndex      the AST node associated with this violation
	 * @param suggestedFixes suggested fixes for this violation
	 * @throws NullPointerException     if any of the arguments are {@code null}
	 * @throws IllegalArgumentException <ul>
	 *                                    <li>if {@code ruleId} or {@code message} are empty or contain
	 *                                    leading/trailing whitespace</li>
	 *                                    <li>if {@code startPosition} is negative</li>
	 *                                    <li>if {@code endPosition} is less than {@code startPosition}</li>
	 *                                    <li>if {@code lineNumber} or {@code columnNumber} is not positive</li>
	 *                                  </ul>
	 */
	public DefaultFormattingViolation(
		String ruleId,
		ViolationSeverity severity,
		String message,
		Path filePath,
		int startPosition,
		int endPosition,
		int lineNumber,
		int columnNumber,
		NodeIndex nodeIndex,
		List<FixStrategy> suggestedFixes)
	{
		validateCommonParameters(ruleId, severity, message, filePath, startPosition, endPosition,
			lineNumber, columnNumber, suggestedFixes);
		requireThat(nodeIndex, "nodeIndex").isNotNull();

		this.ruleId = ruleId;
		this.severity = severity;
		this.message = message;
		this.filePath = filePath;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.nodeIndex = nodeIndex;
		this.suggestedFixes = List.copyOf(suggestedFixes);
	}

	/**
	 * Validates parameters common to all constructors.
	 *
	 * @param ruleId         the rule that detected this violation
	 * @param severity       the severity level
	 * @param message        a human-readable message describing the violation
	 * @param filePath       the source file path
	 * @param startPosition  the start position (character offset)
	 * @param endPosition    the end position (character offset)
	 * @param lineNumber     the line number (1-based)
	 * @param columnNumber   the column number (1-based)
	 * @param suggestedFixes suggested fixes for this violation
	 * @throws NullPointerException     if any of the arguments are {@code null}
	 * @throws IllegalArgumentException <ul>
	 *                                    <li>if {@code ruleId} or {@code message} are empty or contain
	 *                                    leading/trailing whitespace</li>
	 *                                    <li>if {@code startPosition} is negative</li>
	 *                                    <li>if {@code endPosition} is less than {@code startPosition}</li>
	 *                                    <li>if {@code lineNumber} or {@code columnNumber} is not positive</li>
	 *                                  </ul>
	 */
	private static void validateCommonParameters(
		String ruleId,
		ViolationSeverity severity,
		String message,
		Path filePath,
		int startPosition,
		int endPosition,
		int lineNumber,
		int columnNumber,
		List<FixStrategy> suggestedFixes)
	{
		requireThat(ruleId, "ruleId").isNotEmpty().isStripped();
		requireThat(severity, "severity").isNotNull();
		requireThat(message, "message").isNotEmpty().isStripped();
		requireThat(filePath, "filePath").isNotNull();
		requireThat(startPosition, "startPosition").isNotNegative();
		requireThat(endPosition, "endPosition").isGreaterThanOrEqualTo(startPosition);
		requireThat(lineNumber, "lineNumber").isPositive();
		requireThat(columnNumber, "columnNumber").isPositive();
		requireThat(suggestedFixes, "suggestedFixes").isNotNull();
	}

	@Override
	public String ruleId()
	{
		return ruleId;
	}

	@Override
	public ViolationSeverity severity()
	{
		return severity;
	}

	@Override
	public String message()
	{
		return message;
	}

	@Override
	public Path filePath()
	{
		return filePath;
	}

	@Override
	public int startPosition()
	{
		return startPosition;
	}

	@Override
	public int endPosition()
	{
		return endPosition;
	}

	@Override
	public int lineNumber()
	{
		return lineNumber;
	}

	@Override
	public int columnNumber()
	{
		return columnNumber;
	}

	@Override
	public Optional<NodeIndex> nodeIndex()
	{
		return Optional.ofNullable(nodeIndex);
	}

	@Override
	public List<FixStrategy> suggestedFixes()
	{
		return suggestedFixes;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof DefaultFormattingViolation other))
			return false;
		return startPosition == other.startPosition &&
			endPosition == other.endPosition &&
			lineNumber == other.lineNumber &&
			columnNumber == other.columnNumber &&
			Objects.equals(ruleId, other.ruleId) &&
			severity == other.severity &&
			Objects.equals(message, other.message) &&
			Objects.equals(filePath, other.filePath) &&
			Objects.equals(nodeIndex, other.nodeIndex) &&
			Objects.equals(suggestedFixes, other.suggestedFixes);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(ruleId, severity, message, filePath, startPosition, endPosition,
			lineNumber, columnNumber, nodeIndex, suggestedFixes);
	}

	@Override
	public String toString()
	{
		return "DefaultFormattingViolation[" +
			"ruleId=" + ruleId +
			", severity=" + severity +
			", message=" + message +
			", filePath=" + filePath +
			", startPosition=" + startPosition +
			", endPosition=" + endPosition +
			", lineNumber=" + lineNumber +
			", columnNumber=" + columnNumber +
			", nodeIndex=" + nodeIndex +
			", suggestedFixes=" + suggestedFixes +
			']';
	}
}
