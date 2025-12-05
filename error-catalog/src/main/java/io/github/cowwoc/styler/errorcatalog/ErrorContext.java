package io.github.cowwoc.styler.errorcatalog;

/**
 * Rich context for error formatting.
 * <p>
 * All fields are non-null. Use empty strings when a field has no value.
 *
 * @param location      where the error occurred
 * @param sourceSnippet surrounding source lines for display
 * @param expected      what was expected
 * @param actual        what was found
 * @param specificFix   context-specific fix suggestion
 */
public record ErrorContext(
	SourceLocation location,
	String sourceSnippet,
	String expected,
	String actual,
	String specificFix)
{
}
