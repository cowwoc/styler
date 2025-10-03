package io.github.cowwoc.styler.cli.error;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Human-readable error formatter for terminal output with colors and visual formatting.
 * <p>
 * This formatter produces error messages optimized for developer readability with
 * ANSI color codes, clear visual hierarchy, code snippets, and actionable fix
 * suggestions. Based on <a href="https://en.wikipedia.org/wiki/ANSI_escape_code">ANSI X3.64 standard</a>
 */
public final class HumanErrorFormatter implements ErrorFormatter
{
	private final boolean enableColors;

	/**
	 * Creates a human error formatter with the specified color support.
	 *
	 * @param enableColors whether to use ANSI color codes in output
	 */
	public HumanErrorFormatter(boolean enableColors)
	{
		this.enableColors = enableColors;
	}

	/**
	 * Creates a human error formatter with auto-detected color support.
	 */
	public HumanErrorFormatter()
	{
		this(isColorTerminal());
	}

	@Override
	public String formatError(ErrorContext errorContext)
	{
		Objects.requireNonNull(errorContext, "Error context cannot be null");

		StringBuilder output = new StringBuilder();

		// File path, location, severity, category, and message
		output.append(colorize(errorContext.filePath().toString(), AnsiColor.CYAN)).
			append(':').
			append(colorize(String.valueOf(errorContext.getLineNumber()), AnsiColor.YELLOW)).
			append(':').
			append(colorize(String.valueOf(errorContext.getColumnNumber()), AnsiColor.YELLOW)).
			append(' ').
			append(formatSeverity(errorContext.severity())).
			append(' ').
			append(colorize("[" + errorContext.errorCode() + "]", AnsiColor.GRAY)).
			append(' ').
			append(errorContext.message()).
			append('\n');

		// Code snippet with context
		String snippet = SourceSnippetExtractor.extractSnippet(
			errorContext.sourceText(), errorContext.location());
		if (!snippet.isBlank())
		{
			output.append('\n').
				append(colorize(snippet, AnsiColor.GRAY));
		}

		// Suggested fix if available
		if (errorContext.hasSuggestedFix())
		{
			output.append(colorize("💡 Fix: ", AnsiColor.GREEN)).
				append(errorContext.suggestedFix()).
				append('\n');
		}

		return output.toString();
	}

	@Override
	public String formatErrorReport(List<ErrorContext> errors)
	{
		Objects.requireNonNull(errors, "Errors list cannot be null");

		if (errors.isEmpty())
		{
			return colorize("✅ No errors found", AnsiColor.GREEN) + "\n";
		}

		StringBuilder report = new StringBuilder();

		// Group errors by file for organized presentation
		Map<Path, List<ErrorContext>> errorsByFile = errors.stream().
			collect(Collectors.groupingBy(ErrorContext::filePath));

		for (Map.Entry<Path, List<ErrorContext>> entry : errorsByFile.entrySet())
		{
			Path filePath = entry.getKey();
			List<ErrorContext> fileErrors = entry.getValue();

			// File header
			report.append(colorize("📄 " + filePath, AnsiColor.BOLD_CYAN)).
				append(" (").
				append(fileErrors.size()).
				append(" error");
			if (fileErrors.size() != 1)
			{
				report.append('s');
			}
			report.append(")\n");

			// Errors for this file
			for (ErrorContext error : fileErrors)
			{
				String formattedError = formatError(error);
				// Indent error details
				String[] lines = formattedError.split("\n");
				for (String line : lines)
				{
					if (!line.isBlank())
					{
						report.append("  ").append(line).append('\n');
					}
				}
				report.append('\n');
			}
		}

		return report.toString();
	}

	@Override
	public String formatSummary(ErrorSummary summary)
	{
		Objects.requireNonNull(summary, "Summary cannot be null");

		StringBuilder output = new StringBuilder(1024);

		// Header and operation info
		output.append(colorize("═══ Error Summary ═══", AnsiColor.BOLD)).
			append('\n').
			append("Operation: ").
			append(summary.operationType()).
			append('\n').
			append("Processing time: ").
			append(formatTime(summary.processingTimeMs())).
			append('\n').
			append('\n');

		// Error counts by category
		if (summary.totalErrors() == 0)
		{
			output.append(colorize("✅ No errors found", AnsiColor.GREEN));
		}
		else
		{
			AnsiColor totalErrorColor;
			if (summary.criticalErrors() > 0)
			{
				totalErrorColor = AnsiColor.RED;
			}
			else
			{
				totalErrorColor = AnsiColor.YELLOW;
			}
			output.append("Total errors: ").
				append(colorize(String.valueOf(summary.totalErrors()), totalErrorColor)).
				append('\n');

			if (summary.parseErrors() > 0)
			{
				output.append("  Parse errors: ").
					append(colorize(String.valueOf(summary.parseErrors()), AnsiColor.RED)).
					append('\n');
			}
			if (summary.configErrors() > 0)
			{
				output.append("  Config errors: ").
					append(colorize(String.valueOf(summary.configErrors()), AnsiColor.RED)).
					append('\n');
			}
			if (summary.formatViolations() > 0)
			{
				output.append("  Format violations: ").
					append(colorize(String.valueOf(summary.formatViolations()), AnsiColor.YELLOW)).
					append('\n');
			}
			if (summary.validationErrors() > 0)
			{
				output.append("  Validation errors: ").
					append(colorize(String.valueOf(summary.validationErrors()), AnsiColor.YELLOW)).
					append('\n');
			}
			if (summary.systemErrors() > 0)
			{
				output.append("  System errors: ").
					append(colorize(String.valueOf(summary.systemErrors()), AnsiColor.RED)).
					append('\n');
			}

			// Critical error indicator
			if (summary.criticalErrors() > 0)
			{
				output.append('\n').
					append(colorize("❌ " + summary.criticalErrors() +
						" critical error(s) require attention", AnsiColor.BOLD_RED));
			}
		}

		output.append('\n');
		return output.toString();
	}

	@Override
	public String getMimeType()
	{
		return "text/plain";
	}

	@Override
	public boolean supportsColors()
	{
		return enableColors;
	}

	/**
	 * Formats severity level with appropriate color coding.
	 *
	 * @param severity the error severity to format
	 * @return the formatted severity string with color codes
	 */
	private String formatSeverity(ErrorSeverity severity)
	{
		return switch (severity)
		{
			case ERROR -> colorize("error", AnsiColor.RED);
			case WARNING -> colorize("warn", AnsiColor.YELLOW);
			case INFO -> colorize("info", AnsiColor.BLUE);
			case DEBUG -> colorize("debug", AnsiColor.GRAY);
		};
	}

	/**
	 * Formats processing time in human-readable form.
	 *
	 * @param milliseconds the time in milliseconds to format
	 * @return the formatted time string (e.g., "{@code 1}.5s", "2m 30s")
	 */
	private String formatTime(long milliseconds)
	{
		if (milliseconds < 1000)
		{
			return milliseconds + "ms";
		}
		if (milliseconds < 60_000)
		{
			return String.format("%.1fs", milliseconds / 1000.0);
		}
		long minutes = milliseconds / 60_000;
		long seconds = (milliseconds % 60_000) / 1000;
		return String.format("%dm %ds", minutes, seconds);
	}

	/**
	 * Applies color formatting if colors are enabled.
	 *
	 * @param text the text to colorize
	 * @param color the ANSI color to apply
	 * @return the colorized text, or original text if colors are disabled
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
	 * Based on common environment variables that indicate color support.
	 *
	 * @return {@code true} if the terminal supports colors, {@code false} otherwise
	 */
	private static boolean isColorTerminal()
	{
		String term = System.getenv("TERM");
		String colorTerm = System.getenv("COLORTERM");
		String ci = System.getenv("CI");

		// Most modern terminals support colors
		return (term != null && !term.equals("dumb")) ||
		       (colorTerm != null && !colorTerm.isEmpty()) ||
		       ci == null; // Assume color support unless in CI
	}

	/**
	 * ANSI color codes for terminal output.
	 * Based on <a href="https://en.wikipedia.org/wiki/ANSI_escape_code">ANSI X3.64 standard</a>
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
		GRAY("\u001B[90m"),
		BOLD_RED("\u001B[1;31m"),
		BOLD_CYAN("\u001B[1;36m");

		final String code;

		AnsiColor(String code)
		{
			this.code = code;
		}
	}
}