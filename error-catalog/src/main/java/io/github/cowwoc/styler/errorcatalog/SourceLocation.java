package io.github.cowwoc.styler.errorcatalog;

/**
 * Location in source code where an error occurred.
 *
 * @param filePath  the file path where error occurred
 * @param line      1-based line number
 * @param column    1-based column number
 * @param endColumn end column for highlighting range (inclusive)
 */
public record SourceLocation(String filePath, int line, int column, int endColumn)
{
	/**
	 * Creates a location without end column (single character).
	 *
	 * @param filePath the file path where error occurred
	 * @param line     1-based line number
	 * @param column   1-based column number
	 */
	public SourceLocation(String filePath, int line, int column)
	{
		this(filePath, line, column, column);
	}
}
