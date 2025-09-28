package io.github.cowwoc.styler.cli.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;

/**
 * JSON output formatter for machine-readable CLI results.
 * <p>
 * Produces structured JSON output suitable for CI/CD systems, IDEs,
 * and other automated tools that need to parse formatting results
 * programmatically.
 */
public class JsonOutputFormatter implements OutputFormatter
{
	private final ObjectMapper objectMapper;
	private final boolean prettyPrint;

	/**
	 * Creates a JSON output formatter.
	 *
	 * @param prettyPrint whether to format JSON with indentation
	 */
	public JsonOutputFormatter(boolean prettyPrint)
	{
		this.objectMapper = new ObjectMapper();
		this.prettyPrint = prettyPrint;
	}

	/**
	 * Creates a JSON output formatter with compact output.
	 */
	public JsonOutputFormatter()
	{
		this(false);
	}

	@Override
	public String formatResults(FormattingResults results)
	{
		try
		{
			ObjectNode root = objectMapper.createObjectNode();

			// Metadata
			root.put("format", "styler-results");
			root.put("version", "1.0");
			root.put("timestamp", Instant.now().toString());

			// Summary information
			ObjectNode summaryNode = formatSummaryNode(results.summary());
			root.set("summary", summaryNode);

			// Violations array
			ArrayNode violationsArray = objectMapper.createArrayNode();
			for (FormattingViolation violation : results.violations())
			{
				violationsArray.add(formatViolationNode(violation));
			}
			root.set("violations", violationsArray);

			// Processed files array
			ArrayNode filesArray = objectMapper.createArrayNode();
			for (String file : results.processedFiles())
			{
				filesArray.add(file);
			}
			root.set("processedFiles", filesArray);

			// Serialize to JSON
			if (prettyPrint)
			{
				return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
			}
			else
			{
				return objectMapper.writeValueAsString(root);
			}
		}
		catch (Exception e)
		{
			// Fallback to error JSON
			return createErrorJson("Failed to format results: " + e.getMessage());
		}
	}

	@Override
	public String formatViolation(FormattingViolation violation)
	{
		try
		{
			ObjectNode violationNode = formatViolationNode(violation);

			if (prettyPrint)
			{
				return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(violationNode);
			}
			else
			{
				return objectMapper.writeValueAsString(violationNode);
			}
		}
		catch (Exception e)
		{
			return createErrorJson("Failed to format violation: " + e.getMessage());
		}
	}

	@Override
	public String formatSummary(OperationSummary summary)
	{
		try
		{
			ObjectNode summaryNode = formatSummaryNode(summary);

			if (prettyPrint)
			{
				return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(summaryNode);
			}
			else
			{
				return objectMapper.writeValueAsString(summaryNode);
			}
		}
		catch (Exception e)
		{
			return createErrorJson("Failed to format summary: " + e.getMessage());
		}
	}

	@Override
	public String getMimeType()
	{
		return "application/json";
	}

	/**
	 * Creates an ObjectNode for a violation.
	 */
	private ObjectNode formatViolationNode(FormattingViolation violation)
	{
		ObjectNode node = objectMapper.createObjectNode();

		node.put("file", violation.filePath());
		node.put("line", violation.line());
		node.put("column", violation.column());
		node.put("ruleId", violation.ruleId());
		node.put("message", violation.message());
		node.put("severity", violation.severity().name().toLowerCase());

		if (violation.suggestedFix() != null && !violation.suggestedFix().isEmpty())
		{
			node.put("suggestedFix", violation.suggestedFix());
		}

		return node;
	}

	/**
	 * Creates an ObjectNode for a summary.
	 */
	private ObjectNode formatSummaryNode(OperationSummary summary)
	{
		ObjectNode node = objectMapper.createObjectNode();

		node.put("operationType", summary.operationType());
		node.put("totalFiles", summary.totalFiles());
		node.put("processedFiles", summary.processedFiles());
		node.put("violationCount", summary.violationCount());
		node.put("errorCount", summary.errorCount());
		node.put("processingTimeMs", summary.processingTimeMs());

		// Additional computed fields
		node.put("success", summary.errorCount() == 0);

		if (summary.processingTimeMs() > 0)
		{
			double filesPerSecond = (summary.processedFiles() * 1000.0) / summary.processingTimeMs();
			node.put("throughput", Math.round(filesPerSecond * 100.0) / 100.0);
		}

		return node;
	}

	/**
	 * Creates a simple error JSON response.
	 */
	private String createErrorJson(String errorMessage)
	{
		try
		{
			ObjectNode errorNode = objectMapper.createObjectNode();
			errorNode.put("error", true);
			errorNode.put("message", errorMessage);
			errorNode.put("timestamp", Instant.now().toString());

			return objectMapper.writeValueAsString(errorNode);
		}
		catch (Exception e)
		{
			// Ultimate fallback
			return "{\"error\":true,\"message\":\"JSON formatting failed\"}";
		}
	}

	/**
	 * Sets whether to pretty-print JSON output.
	 *
	 * @param prettyPrint true to enable pretty printing
	 * @return a new formatter instance with the specified setting
	 */
	public JsonOutputFormatter withPrettyPrint(boolean prettyPrint)
	{
		return new JsonOutputFormatter(prettyPrint);
	}
}