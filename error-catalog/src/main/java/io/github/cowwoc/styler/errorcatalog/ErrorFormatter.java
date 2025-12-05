package io.github.cowwoc.styler.errorcatalog;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Formats exceptions for different audiences.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class ErrorFormatter
{
	/**
	 * Formats error message for AI agents (structured, machine-readable JSON).
	 *
	 * @param error the exception to format
	 * @return formatted error message optimized for AI parsing
	 * @throws NullPointerException  if error is {@code null}
	 * @throws IllegalStateException if error doesn't implement ContextualException
	 */
	public String formatForAi(Throwable error)
	{
		requireThat(error, "error").isNotNull();
		ErrorContext ctx = getContext(error);

		StringBuilder json = new StringBuilder(256).
			append("{\"exception\": \"").append(error.getClass().getSimpleName()).append('"').
			append(", \"message\": \"").append(escapeJson(error.getMessage())).append('"').
			append(", \"location\": {").
			append("\"file\": \"").append(escapeJson(ctx.location().filePath())).append("\", ").
			append("\"line\": ").append(ctx.location().line()).append(", ").
			append("\"column\": ").append(ctx.location().column()).append(", ").
			append("\"endColumn\": ").append(ctx.location().endColumn()).
			append('}');

		if (!ctx.sourceSnippet().isEmpty())
			json.append(", \"source\": \"").append(escapeJson(ctx.sourceSnippet())).append('"');
		if (!ctx.expected().isEmpty())
			json.append(", \"expected\": \"").append(escapeJson(ctx.expected())).append('"');
		if (!ctx.actual().isEmpty())
			json.append(", \"actual\": \"").append(escapeJson(ctx.actual())).append('"');
		if (!ctx.specificFix().isEmpty())
			json.append(", \"fix\": \"").append(escapeJson(ctx.specificFix())).append('"');

		json.append('}');
		return json.toString();
	}

	/**
	 * Formats error message for human developers (narrative, contextual).
	 *
	 * @param error the exception to format
	 * @return formatted error message optimized for human reading
	 * @throws NullPointerException  if error is {@code null}
	 * @throws IllegalStateException if error doesn't implement ContextualException
	 */
	public String formatForHuman(Throwable error)
	{
		requireThat(error, "error").isNotNull();
		ErrorContext ctx = getContext(error);

		StringBuilder output = new StringBuilder(512).
			append(error.getClass().getSimpleName()).append("\n\n").
			append(error.getMessage()).append("\n\n").
			append("File: ").append(ctx.location().filePath()).append('\n').
			append("Line: ").append(ctx.location().line()).
			append(", Column: ").append(ctx.location().column()).append("\n\n");

		if (!ctx.sourceSnippet().isEmpty())
			output.append(formatSourceSnippet(ctx)).append('\n');

		if (!ctx.expected().isEmpty())
		{
			output.append("Expected: ").append(ctx.expected()).append('\n');
			if (!ctx.actual().isEmpty())
				output.append("Found: ").append(ctx.actual()).append('\n');
			output.append('\n');
		}

		if (!ctx.specificFix().isEmpty())
			output.append("Fix: ").append(ctx.specificFix()).append('\n');

		return output.toString();
	}

	/**
	 * Gets fix suggestion for an error.
	 *
	 * @param error the exception to analyze
	 * @return fix suggestion, or {@code null} if no fix available
	 * @throws NullPointerException  if error is {@code null}
	 * @throws IllegalStateException if error doesn't implement ContextualException
	 */
	public FixSuggestion getFixSuggestion(Throwable error)
	{
		requireThat(error, "error").isNotNull();
		ErrorContext ctx = getContext(error);

		if (ctx.specificFix().isEmpty())
			return null;
		return new FixSuggestion(ctx.specificFix(), List.of(ctx.specificFix()));
	}

	private ErrorContext getContext(Throwable error)
	{
		if (!(error instanceof ContextualException contextual))
			throw new IllegalStateException("Exception must implement ContextualException: " +
				error.getClass().getName());
		return contextual.getErrorContext();
	}

	private String escapeJson(String text)
	{
		if (text == null)
			return "null";
		return text.replace("\"", "\\\"").
			replace("\n", "\\n").
			replace("\r", "\\r").
			replace("\t", "\\t");
	}

	private String formatSourceSnippet(ErrorContext context)
	{
		if (context.sourceSnippet().isEmpty())
			return "";

		StringBuilder formatted = new StringBuilder();
		String[] lines = context.sourceSnippet().split("\n");
		int errorLine = context.location().line();

		for (int i = 0; i < lines.length; i += 1)
		{
			int lineNum = errorLine - (lines.length / 2) + i;
			if (lineNum > 0)
				formatted.append(String.format("%4d: %s%n", lineNum, lines[i]));
		}

		// Add pointer line
		int column = context.location().column();
		int endColumn = context.location().endColumn();
		formatted.append("      ");
		for (int i = 1; i < column; i += 1)
			formatted.append(' ');
		formatted.append('^');
		for (int i = column; i < endColumn; i += 1)
			formatted.append('~');
		formatted.append('\n');

		return formatted.toString();
	}
}
