package io.github.cowwoc.styler.cli.output;

import java.util.stream.Collectors;

/**
 * Human-readable output formatter for CLI results.
 * <p>
 * Formats output for terminal display with colors, indentation, and
 * clear visual hierarchy. Optimized for developer readability and
 * quick problem identification.
 */
public class HumanOutputFormatter implements OutputFormatter
{
	private final boolean enableColors;

	/**
	 * Creates a human output formatter.
	 *
	 * @param enableColors whether to use ANSI color codes
	 */
	public HumanOutputFormatter(boolean enableColors)
	{
		this.enableColors = enableColors;
	}

	/**
	 * Creates a human output formatter with auto-detected color support.
	 */
	public HumanOutputFormatter()
	{
		this(isColorTerminal());
	}

	@Override
	public String formatResults(FormattingResults results)
	{
		StringBuilder output = new StringBuilder();

		if (!results.violations().isEmpty())
		{
			output.append(formatViolationsSection(results.violations()));
			output.append("\n");
		}

		output.append(formatSummary(results.summary()));

		return output.toString();
	}

	@Override
	public String formatViolation(FormattingViolation violation)
	{
		StringBuilder sb = new StringBuilder();

		// File path with line/column
		sb.append(formatPath(violation.filePath()));
		sb.append(":");
		sb.append(violation.line());
		sb.append(":");
		sb.append(violation.column());

		// Severity indicator
		sb.append(" ");
		sb.append(formatSeverity(violation.severity()));

		// Rule ID
		sb.append(" [");
		sb.append(violation.ruleId());
		sb.append("]");

		// Message
		sb.append(" ");
		sb.append(violation.message());

		// Suggested fix (if available)
		if (violation.suggestedFix() != null && !violation.suggestedFix().isEmpty())
		{
			sb.append("\n  ");
			sb.append(colorize("Fix: " + violation.suggestedFix(), AnsiColor.CYAN));
		}

		return sb.toString();
	}

	@Override
	public String formatSummary(OperationSummary summary)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(colorize("=== Summary ===", AnsiColor.BOLD));
		sb.append("\n");

		// Operation type
		sb.append("Operation: ").append(summary.operationType()).append("\n");

		// File counts
		sb.append("Files processed: ").append(summary.processedFiles())
			.append("/").append(summary.totalFiles()).append("\n");

		// Violation count with color coding
		if (summary.violationCount() > 0)
		{
			String violationText = summary.violationCount() + " violations found";
			sb.append(colorize(violationText, AnsiColor.YELLOW)).append("\n");
		}
		else
		{
			sb.append(colorize("No violations found", AnsiColor.GREEN)).append("\n");
		}

		// Error count
		if (summary.errorCount() > 0)
		{
			String errorText = summary.errorCount() + " errors occurred";
			sb.append(colorize(errorText, AnsiColor.RED)).append("\n");
		}

		// Processing time
		sb.append("Processing time: ").append(formatTime(summary.processingTimeMs())).append("\n");

		return sb.toString();
	}

	@Override
	public String getMimeType()
	{
		return "text/plain";
	}

	/**
	 * Formats the violations section with grouping by file.
	 */
	private String formatViolationsSection(java.util.List<FormattingViolation> violations)
	{
		StringBuilder sb = new StringBuilder();

		// Group violations by file
		var violationsByFile = violations.stream()
			.collect(Collectors.groupingBy(FormattingViolation::filePath));

		for (var entry : violationsByFile.entrySet())
		{
			String filePath = entry.getKey();
			var fileViolations = entry.getValue();

			sb.append(colorize(filePath, AnsiColor.BOLD)).append("\n");

			for (FormattingViolation violation : fileViolations)
			{
				sb.append("  ").append(formatViolationLine(violation)).append("\n");
			}

			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * Formats a single violation line (without file path).
	 */
	private String formatViolationLine(FormattingViolation violation)
	{
		StringBuilder sb = new StringBuilder();

		// Line:column
		sb.append(violation.line()).append(":").append(violation.column());

		// Severity
		sb.append(" ").append(formatSeverity(violation.severity()));

		// Rule and message
		sb.append(" [").append(violation.ruleId()).append("] ");
		sb.append(violation.message());

		return sb.toString();
	}

	/**
	 * Formats file path with appropriate styling.
	 */
	private String formatPath(String path)
	{
		return colorize(path, AnsiColor.CYAN);
	}

	/**
	 * Formats severity level with color coding.
	 */
	private String formatSeverity(SeverityLevel severity)
	{
		return switch (severity)
		{
			case ERROR -> colorize("error", AnsiColor.RED);
			case WARN -> colorize("warn", AnsiColor.YELLOW);
			case INFO -> colorize("info", AnsiColor.BLUE);
			case DEBUG -> colorize("debug", AnsiColor.GRAY);
		};
	}

	/**
	 * Formats processing time in human-readable form.
	 */
	private String formatTime(long milliseconds)
	{
		if (milliseconds < 1000)
		{
			return milliseconds + "ms";
		}
		else if (milliseconds < 60000)
		{
			return String.format("%.1fs", milliseconds / 1000.0);
		}
		else
		{
			long minutes = milliseconds / 60000;
			long seconds = (milliseconds % 60000) / 1000;
			return String.format("%dm %ds", minutes, seconds);
		}
	}

	/**
	 * Applies color formatting if colors are enabled.
	 */
	private String colorize(String text, AnsiColor color)
	{
		if (!enableColors)
		{
			return text;
		}
		return color.code + text + AnsiColor.RESET.code;
	}

	/**
	 * Detects if the current terminal supports colors.
	 */
	private static boolean isColorTerminal()
	{
		// Check common environment variables that indicate color support
		String term = System.getenv("TERM");
		String colorTerm = System.getenv("COLORTERM");

		// Most modern terminals support colors
		return (term != null && !term.equals("dumb")) ||
		       (colorTerm != null && !colorTerm.isEmpty()) ||
		       System.getenv("CI") == null; // Assume color support unless in CI
	}

	/**
	 * ANSI color codes for terminal output.
	 */
	private enum AnsiColor
	{
		RESET("\u001B[0m"),
		BOLD("\u001B[1m"),
		RED("\u001B[31m"),
		GREEN("\u001B[32m"),
		YELLOW("\u001B[33m"),
		BLUE("\u001B[34m"),
		CYAN("\u001B[36m"),
		GRAY("\u001B[90m");

		final String code;

		AnsiColor(String code)
		{
			this.code = code;
		}
	}
}