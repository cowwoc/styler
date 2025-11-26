package io.github.cowwoc.styler.formatter;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Immutable representation of a formatting violation.
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 */
public interface FormattingViolation
{
	/**
	 * Returns the rule that detected this violation.
	 *
	 * @return the rule identifier
	 */
	String ruleId();

	/**
	 * Returns the severity of this violation.
	 *
	 * @return the severity level
	 */
	ViolationSeverity severity();

	/**
	 * Returns a human-readable message describing the violation.
	 *
	 * @return the violation message
	 */
	String message();

	/**
	 * Returns the source file path where the violation occurred.
	 *
	 * @return the file path
	 */
	Path filePath();

	/**
	 * Returns the start position (character offset) in the source.
	 * The value is non-negative.
	 *
	 * @return the start position
	 */
	int startPosition();

	/**
	 * Returns the end position (character offset) in the source.
	 * The value is greater than or equal to the start position.
	 *
	 * @return the end position
	 */
	int endPosition();

	/**
	 * Returns the line number (1-based) where the violation starts.
	 *
	 * @return the line number
	 */
	int lineNumber();

	/**
	 * Returns the column number (1-based) where the violation starts.
	 *
	 * @return the column number
	 */
	int columnNumber();

	/**
	 * Returns the AST node associated with this violation, if available.
	 *
	 * @return empty if the violation is not node-specific
	 */
	Optional<NodeIndex> nodeIndex();

	/**
	 * Returns suggested fixes for this violation, ordered by priority.
	 *
	 * @return an empty list if no fixes are available
	 */
	List<FixStrategy> suggestedFixes();
}
