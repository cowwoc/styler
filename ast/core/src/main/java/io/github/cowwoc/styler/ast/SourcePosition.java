package io.github.cowwoc.styler.ast;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents a position in source code with line and column information.
 * This immutable record provides precise location tracking for AST nodes.
 *
 * @param line the line number (1-based)
 * @param column the column number (1-based)
 */
public record SourcePosition(int line, int column) implements Comparable<SourcePosition> {
	public SourcePosition {
		requireThat(line, "line").isPositive();
		requireThat(column, "column").isPositive();
	}

	/**
	 * Creates a source position representing the beginning of a file.
	 * @return a SourcePosition at line 1, column 1
	 */
	public static SourcePosition start() {
		return new SourcePosition(1, 1);
	}

	/**
	 * Advances this position by the specified number of columns.
	 * @param columns the number of columns to advance
	 * @return a new SourcePosition with advanced column
	 */
	public SourcePosition advanceColumn(int columns) {
		requireThat(columns, "columns").isNotNegative();
		return new SourcePosition(line, column + columns);
	}

	/**
	 * Advances this position to the next line.
	 * @return a new SourcePosition at the beginning of the next line
	 */
	public SourcePosition nextLine() {
		return new SourcePosition(line + 1, 1);
	}

	/**
	 * Compares this source position with another based on line and column.
	 * @param other the other source position to compare with
	 * @return negative if this position comes before other, positive if after, zero if equal
	 */
	@Override
	public int compareTo(SourcePosition other) {
		int lineComparison = Integer.compare(this.line, other.line);
		if (lineComparison != 0) {
			return lineComparison;
		}
		return Integer.compare(this.column, other.column);
	}
}
