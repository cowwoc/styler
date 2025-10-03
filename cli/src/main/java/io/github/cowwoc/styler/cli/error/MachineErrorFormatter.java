package io.github.cowwoc.styler.cli.error;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Machine-readable error formatter for structured JSON output.
 * <p>
 * This formatter produces error messages in JSON format optimized for consumption
 * by AI agents, CI/CD systems, and automated tools. The output follows a consistent
 * schema for predictable parsing and integration.
 */
public final class MachineErrorFormatter implements ErrorFormatter
{
	private static final String JSON_INDENT = "  ";

	@Override
	public String formatError(ErrorContext errorContext)
	{
		Objects.requireNonNull(errorContext, "Error context cannot be null");

		StringBuilder json = new StringBuilder(512);
		json.append("{\n").
			append(JSON_INDENT).append("\"type\": \"error\",\n");
		String filePath = escapeJson(errorContext.filePath().toString());
	json.append(JSON_INDENT).append("\"file\": \"").append(filePath).append("\",\n").
			append(JSON_INDENT).append("\"line\": ").append(errorContext.getLineNumber()).append(",\n").
			append(JSON_INDENT).append("\"column\": ").append(errorContext.getColumnNumber()).append(",\n");
		String category = errorContext.category().name().toLowerCase(Locale.ROOT);
	json.append(JSON_INDENT).append("\"category\": \"").append(category).append("\",\n");
		String severity = errorContext.severity().name().toLowerCase(Locale.ROOT);
	json.append(JSON_INDENT).append("\"severity\": \"").append(severity).append("\",\n").
			append(JSON_INDENT).append("\"code\": \"").append(escapeJson(errorContext.errorCode())).append("\",\n").
			append(JSON_INDENT).append("\"message\": \"").append(escapeJson(errorContext.message())).append('\"');

		if (errorContext.hasSuggestedFix())
		{
			json.append(",\n");
			String suggestedFix = escapeJson(errorContext.suggestedFix());
		json.append(JSON_INDENT).append("\"suggestedFix\": \"").append(suggestedFix).append('\"');
		}

		// Add source context for debugging
		String snippet = SourceSnippetExtractor.extractInlineSnippet(
			errorContext.sourceText(), errorContext.location());
		if (!snippet.isBlank())
		{
			json.append(",\n").
				append(JSON_INDENT).append("\"sourceSnippet\": \"").append(escapeJson(snippet)).append('\"');
		}

		json.append("\n}");
		return json.toString();
	}

	@Override
	public String formatErrorReport(List<ErrorContext> errors)
	{
		Objects.requireNonNull(errors, "Errors list cannot be null");

		StringBuilder json = new StringBuilder(512);
		json.append("{\n").
			append(JSON_INDENT).append("\"type\": \"error-report\",\n");
		String timestamp = java.time.Instant.now().toString();
	json.append(JSON_INDENT).append("\"timestamp\": \"").append(timestamp).append("\",\n").
			append(JSON_INDENT).append("\"errorCount\": ").append(errors.size()).append(",\n").
			append(JSON_INDENT).append("\"errors\": [\n");

		for (int i = 0; i < errors.size(); ++i)
		{
			ErrorContext error = errors.get(i);
			String errorJson = formatError(error);

			// Indent each line of the error JSON
			String indentedError = errorJson.lines().
				map(line -> JSON_INDENT + JSON_INDENT + line).
				collect(Collectors.joining("\n"));

			json.append(indentedError);

			if (i < errors.size() - 1)
			{
				json.append(',');
			}
			json.append('\n');
		}

		json.append(JSON_INDENT).append("]\n").
			append('}');
		return json.toString();
	}

	@Override
	public String formatSummary(ErrorSummary summary)
	{
		Objects.requireNonNull(summary, "Summary cannot be null");

		StringBuilder json = new StringBuilder(512);
		json.append("{\n").
			append(JSON_INDENT).append("\"type\": \"error-summary\",\n");
		String timestamp = java.time.Instant.now().toString();
	json.append(JSON_INDENT).append("\"timestamp\": \"").append(timestamp).append("\",\n");
		String operation = escapeJson(summary.operationType());
	json.append(JSON_INDENT).append("\"operation\": \"").append(operation).append("\",\n").
			append(JSON_INDENT).append("\"processingTimeMs\": ").append(summary.processingTimeMs()).append(",\n").
			append(JSON_INDENT).append("\"success\": ").append(summary.isSuccess()).append(",\n").
			append(JSON_INDENT).append("\"shouldHalt\": ");
	json.append(summary.shouldHaltProcessing()).append(",\n").
			append(JSON_INDENT).append("\"totalErrors\": ").append(summary.totalErrors()).append(",\n").
			append(JSON_INDENT).append("\"criticalErrors\": ");
	json.append(summary.criticalErrors()).append(",\n").
			append(JSON_INDENT).append("\"errorBreakdown\": {\n").
			append(JSON_INDENT).append(JSON_INDENT).append("\"parse\": ");
	json.append(summary.parseErrors()).append(",\n").
			append(JSON_INDENT).append(JSON_INDENT).append("\"config\": ");
	json.append(summary.configErrors()).append(",\n").
			append(JSON_INDENT).append(JSON_INDENT).append("\"format\": ");
	json.append(summary.formatViolations()).append(",\n").
			append(JSON_INDENT).append(JSON_INDENT).append("\"validation\": ");
	json.append(summary.validationErrors()).append(",\n").
			append(JSON_INDENT).append(JSON_INDENT).append("\"system\": ").append(summary.systemErrors()).append('\n').
			append(JSON_INDENT).append("}\n").
			append('}');
		return json.toString();
	}

	@Override
	public String getMimeType()
	{
		return "application/json";
	}

	@Override
	public boolean supportsColors()
	{
		return false;
	}

	/**
	 * Escapes special characters in JSON strings.
	 *
	 * @param input the string to escape, may be {@code null}
	 * @return the escaped string, or "{@code null}" if input is {@code null}
	 */
	private String escapeJson(String input)
	{
		if (input == null)
		{
			return "null";
		}

		StringBuilder escaped = new StringBuilder();
		for (int i = 0; i < input.length(); ++i)
		{
			char c = input.charAt(i);
			switch (c)
			{
				case '"' -> escaped.append("\\\"");
				case '\\' -> escaped.append("\\\\");
				case '\b' -> escaped.append("\\b");
				case '\f' -> escaped.append("\\f");
				case '\n' -> escaped.append("\\n");
				case '\r' -> escaped.append("\\r");
				case '\t' -> escaped.append("\\t");
				default ->
				{
					if (c < 0x20 || c > 0x7E)
					{
						escaped.append(String.format("\\u%04x", (int) c));
					}
					else
					{
						escaped.append(c);
					}
				}
			}
		}
		return escaped.toString();
	}

	/**
	 * Creates a compact single-line JSON error format for inline use.
	 *
	 * @param errorContext the error to format, never {@code null}
	 * @throws IllegalArgumentException if {@code errorContext} is {@code null}
	 * @return a compact JSON representation of the error, never {@code null}
	 */
	public String formatCompactError(ErrorContext errorContext)
	{
		Objects.requireNonNull(errorContext, "Error context cannot be null");

		StringBuilder json = new StringBuilder(512);
		json.append('{').
			append("\"file\":\"").append(escapeJson(errorContext.filePath().toString())).append("\",").
			append("\"line\":").append(errorContext.getLineNumber()).append(',').
			append("\"column\":").append(errorContext.getColumnNumber()).append(',').
			append("\"category\":\"").append(errorContext.category().name().toLowerCase(Locale.ROOT)).append("\",").
			append("\"severity\":\"").append(errorContext.severity().name().toLowerCase(Locale.ROOT)).append("\",").
			append("\"code\":\"").append(escapeJson(errorContext.errorCode())).append("\",").
			append("\"message\":\"").append(escapeJson(errorContext.message())).append('\"');

		if (errorContext.hasSuggestedFix())
		{
			json.append(",\"fix\":\"").append(escapeJson(errorContext.suggestedFix())).append('\"');
		}

		json.append('}');
		return json.toString();
	}

	/**
	 * Creates structured error data for integration with external systems.
	 *
	 * @param errors the errors to format, never {@code null}
	 * @throws IllegalArgumentException if {@code errors} is {@code null}
	 * @return a structured JSON object with metadata for external consumption, never {@code null}
	 */
	public String formatStructuredReport(List<ErrorContext> errors)
	{
		Objects.requireNonNull(errors, "Errors list cannot be null");

		StringBuilder json = new StringBuilder(512);
		json.append("{\n").
			append(JSON_INDENT).append("\"version\": \"1.0\",\n").
			append(JSON_INDENT).append("\"generator\": \"styler-error-reporter\",\n");
		String timestamp = java.time.Instant.now().toString();
	json.append(JSON_INDENT).append("\"timestamp\": \"").append(timestamp).append("\",\n");

		// Group errors by file for structured reporting
		Map<Path, List<ErrorContext>> errorsByFile = errors.stream().
			collect(Collectors.groupingBy(ErrorContext::filePath));

		json.append(JSON_INDENT).append("\"files\": [\n");
		boolean firstFile = true;
		for (Map.Entry<Path, List<ErrorContext>> entry : errorsByFile.entrySet())
		{
			if (!firstFile) json.append(",\n");
			firstFile = false;

			Path filePath = entry.getKey();
			List<ErrorContext> fileErrors = entry.getValue();

			json.append(JSON_INDENT).append(JSON_INDENT).append("{\n");
			String pathString = escapeJson(filePath.toString());
		json.append(JSON_INDENT).append(JSON_INDENT).append(JSON_INDENT).
			append("\"path\": \"").append(pathString).append("\",\n").
				append(JSON_INDENT).append(JSON_INDENT).append(JSON_INDENT).
			append("\"errorCount\": ").append(fileErrors.size()).append(",\n").
				append(JSON_INDENT).append(JSON_INDENT).append(JSON_INDENT).append("\"errors\": [\n");

			for (int i = 0; i < fileErrors.size(); ++i)
			{
				String compactError = formatCompactError(fileErrors.get(i));
				json.append(JSON_INDENT).append(JSON_INDENT).append(JSON_INDENT).append(JSON_INDENT).
					append(compactError);
				if (i < fileErrors.size() - 1) json.append(',');
				json.append('\n');
			}

			json.append(JSON_INDENT).append(JSON_INDENT).append(JSON_INDENT).append("]\n").
				append(JSON_INDENT).append(JSON_INDENT).append('}');
		}

		json.append('\n').append(JSON_INDENT).append("]\n").
			append('}');
		return json.toString();
	}
}