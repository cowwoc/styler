package io.github.cowwoc.styler.cli.error;

import java.util.List;
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

		StringBuilder json = new StringBuilder();
		json.append("{\n");
		json.append(JSON_INDENT).append("\"type\": \"error\",\n");
		json.append(JSON_INDENT).append("\"file\": \"").append(escapeJson(errorContext.filePath().toString())).append("\",\n");
		json.append(JSON_INDENT).append("\"line\": ").append(errorContext.getLineNumber()).append(",\n");
		json.append(JSON_INDENT).append("\"column\": ").append(errorContext.getColumnNumber()).append(",\n");
		json.append(JSON_INDENT).append("\"category\": \"").append(errorContext.category().name().toLowerCase()).append("\",\n");
		json.append(JSON_INDENT).append("\"severity\": \"").append(errorContext.severity().name().toLowerCase()).append("\",\n");
		json.append(JSON_INDENT).append("\"code\": \"").append(escapeJson(errorContext.errorCode())).append("\",\n");
		json.append(JSON_INDENT).append("\"message\": \"").append(escapeJson(errorContext.message())).append("\"");

		if (errorContext.hasSuggestedFix())
		{
			json.append(",\n");
			json.append(JSON_INDENT).append("\"suggestedFix\": \"").append(escapeJson(errorContext.suggestedFix())).append("\"");
		}

		// Add source context for debugging
		String snippet = SourceSnippetExtractor.extractInlineSnippet(
			errorContext.sourceText(), errorContext.location());
		if (!snippet.trim().isEmpty())
		{
			json.append(",\n");
			json.append(JSON_INDENT).append("\"sourceSnippet\": \"").append(escapeJson(snippet)).append("\"");
		}

		json.append("\n}");
		return json.toString();
	}

	@Override
	public String formatErrorReport(List<ErrorContext> errors)
	{
		Objects.requireNonNull(errors, "Errors list cannot be null");

		StringBuilder json = new StringBuilder();
		json.append("{\n");
		json.append(JSON_INDENT).append("\"type\": \"error-report\",\n");
		json.append(JSON_INDENT).append("\"timestamp\": \"").append(java.time.Instant.now().toString()).append("\",\n");
		json.append(JSON_INDENT).append("\"errorCount\": ").append(errors.size()).append(",\n");
		json.append(JSON_INDENT).append("\"errors\": [\n");

		for (int i = 0; i < errors.size(); i++)
		{
			ErrorContext error = errors.get(i);
			String errorJson = formatError(error);

			// Indent each line of the error JSON
			String indentedError = errorJson.lines()
				.map(line -> JSON_INDENT + JSON_INDENT + line)
				.collect(Collectors.joining("\n"));

			json.append(indentedError);

			if (i < errors.size() - 1)
			{
				json.append(",");
			}
			json.append("\n");
		}

		json.append(JSON_INDENT).append("]\n");
		json.append("}");
		return json.toString();
	}

	@Override
	public String formatSummary(ErrorSummary summary)
	{
		Objects.requireNonNull(summary, "Summary cannot be null");

		StringBuilder json = new StringBuilder();
		json.append("{\n");
		json.append(JSON_INDENT).append("\"type\": \"error-summary\",\n");
		json.append(JSON_INDENT).append("\"timestamp\": \"").append(java.time.Instant.now().toString()).append("\",\n");
		json.append(JSON_INDENT).append("\"operation\": \"").append(escapeJson(summary.operationType())).append("\",\n");
		json.append(JSON_INDENT).append("\"processingTimeMs\": ").append(summary.processingTimeMs()).append(",\n");
		json.append(JSON_INDENT).append("\"success\": ").append(summary.isSuccess()).append(",\n");
		json.append(JSON_INDENT).append("\"shouldHalt\": ").append(summary.shouldHaltProcessing()).append(",\n");
		json.append(JSON_INDENT).append("\"totalErrors\": ").append(summary.totalErrors()).append(",\n");
		json.append(JSON_INDENT).append("\"criticalErrors\": ").append(summary.criticalErrors()).append(",\n");
		json.append(JSON_INDENT).append("\"errorBreakdown\": {\n");
		json.append(JSON_INDENT).append(JSON_INDENT).append("\"parse\": ").append(summary.parseErrors()).append(",\n");
		json.append(JSON_INDENT).append(JSON_INDENT).append("\"config\": ").append(summary.configErrors()).append(",\n");
		json.append(JSON_INDENT).append(JSON_INDENT).append("\"format\": ").append(summary.formatViolations()).append(",\n");
		json.append(JSON_INDENT).append(JSON_INDENT).append("\"validation\": ").append(summary.validationErrors()).append(",\n");
		json.append(JSON_INDENT).append(JSON_INDENT).append("\"system\": ").append(summary.systemErrors()).append("\n");
		json.append(JSON_INDENT).append("}\n");
		json.append("}");
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
	 * @param input the string to escape, may be null
	 * @return the escaped string, or "null" if input is null
	 */
	private String escapeJson(String input)
	{
		if (input == null)
		{
			return "null";
		}

		StringBuilder escaped = new StringBuilder();
		for (int i = 0; i < input.length(); i++)
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
				default -> {
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
	 * @param errorContext the error to format, never null
	 * @throws IllegalArgumentException if {@code errorContext} is null
	 * @return a compact JSON representation of the error, never null
	 */
	public String formatCompactError(ErrorContext errorContext)
	{
		Objects.requireNonNull(errorContext, "Error context cannot be null");

		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"file\":\"").append(escapeJson(errorContext.filePath().toString())).append("\",");
		json.append("\"line\":").append(errorContext.getLineNumber()).append(",");
		json.append("\"column\":").append(errorContext.getColumnNumber()).append(",");
		json.append("\"category\":\"").append(errorContext.category().name().toLowerCase()).append("\",");
		json.append("\"severity\":\"").append(errorContext.severity().name().toLowerCase()).append("\",");
		json.append("\"code\":\"").append(escapeJson(errorContext.errorCode())).append("\",");
		json.append("\"message\":\"").append(escapeJson(errorContext.message())).append("\"");

		if (errorContext.hasSuggestedFix())
		{
			json.append(",\"fix\":\"").append(escapeJson(errorContext.suggestedFix())).append("\"");
		}

		json.append("}");
		return json.toString();
	}

	/**
	 * Creates structured error data for integration with external systems.
	 *
	 * @param errors the errors to format, never null
	 * @throws IllegalArgumentException if {@code errors} is null
	 * @return a structured JSON object with metadata for external consumption, never null
	 */
	public String formatStructuredReport(List<ErrorContext> errors)
	{
		Objects.requireNonNull(errors, "Errors list cannot be null");

		StringBuilder json = new StringBuilder();
		json.append("{\n");
		json.append(JSON_INDENT).append("\"version\": \"1.0\",\n");
		json.append(JSON_INDENT).append("\"generator\": \"styler-error-reporter\",\n");
		json.append(JSON_INDENT).append("\"timestamp\": \"").append(java.time.Instant.now().toString()).append("\",\n");

		// Group errors by file for structured reporting
		var errorsByFile = errors.stream()
			.collect(Collectors.groupingBy(ErrorContext::filePath));

		json.append(JSON_INDENT).append("\"files\": [\n");
		boolean firstFile = true;
		for (var entry : errorsByFile.entrySet())
		{
			if (!firstFile) json.append(",\n");
			firstFile = false;

			var filePath = entry.getKey();
			var fileErrors = entry.getValue();

			json.append(JSON_INDENT).append(JSON_INDENT).append("{\n");
			json.append(JSON_INDENT).append(JSON_INDENT).append(JSON_INDENT).append("\"path\": \"").append(escapeJson(filePath.toString())).append("\",\n");
			json.append(JSON_INDENT).append(JSON_INDENT).append(JSON_INDENT).append("\"errorCount\": ").append(fileErrors.size()).append(",\n");
			json.append(JSON_INDENT).append(JSON_INDENT).append(JSON_INDENT).append("\"errors\": [\n");

			for (int i = 0; i < fileErrors.size(); i++)
			{
				String compactError = formatCompactError(fileErrors.get(i));
				json.append(JSON_INDENT).append(JSON_INDENT).append(JSON_INDENT).append(JSON_INDENT).append(compactError);
				if (i < fileErrors.size() - 1) json.append(",");
				json.append("\n");
			}

			json.append(JSON_INDENT).append(JSON_INDENT).append(JSON_INDENT).append("]\n");
			json.append(JSON_INDENT).append(JSON_INDENT).append("}");
		}

		json.append("\n").append(JSON_INDENT).append("]\n");
		json.append("}");
		return json.toString();
	}
}