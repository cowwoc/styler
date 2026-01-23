package io.github.cowwoc.styler.cli;

import io.github.cowwoc.styler.errorcatalog.Audience;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.RuleExample;
import io.github.cowwoc.styler.formatter.RuleProperty;

import java.util.List;
import java.util.Locale;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Exports formatting rules documentation in Markdown format.
 * <p>
 * This class provides the implementation for the {@code --explain-rules} CLI flag,
 * allowing users and AI agents to understand the available formatting rules before
 * processing files.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe and stateless.
 */
public final class RulesSummaryExporter
{
	// Estimated size per rule in Markdown format
	private static final int MARKDOWN_BYTES_PER_RULE = 500;
	// Markdown header overhead
	private static final int MARKDOWN_OVERHEAD = 200;

	/**
	 * Creates a new rules summary exporter.
	 */
	public RulesSummaryExporter()
	{
	}

	/**
	 * Exports formatting rules documentation in Markdown format.
	 *
	 * @param rules    the formatting rules to document
	 * @param audience the target audience (affects framing, not format)
	 * @return the Markdown documentation string
	 * @throws NullPointerException if any argument is null
	 */
	public String export(List<FormattingRule> rules, Audience audience)
	{
		requireThat(rules, "rules").isNotNull();
		requireThat(audience, "audience").isNotNull();

		int estimatedSize = MARKDOWN_OVERHEAD + rules.size() * MARKDOWN_BYTES_PER_RULE;
		StringBuilder markdown = new StringBuilder(estimatedSize);

		markdown.append("# Styler Formatting Rules\n\n");
		if (audience == Audience.AI)
		{
			markdown.append("When generating Java code, apply these formatting rules to avoid violations.\n\n");
		}
		else
		{
			markdown.append("The following rules are applied by the Styler code formatter.\n\n");
		}

		for (FormattingRule rule : rules)
		{
			markdown.append("## ").append(rule.getName()).append("\n\n").
				append("- **ID**: `").append(rule.getId()).append("`\n").
				append("- **Default Severity**: ").
				append(rule.getDefaultSeverity().name().toLowerCase(Locale.ROOT)).
				append('\n').
				append("- **Description**: ").append(rule.getDescription()).append('\n').
				append('\n');

			appendMarkdownProperties(markdown, rule.getProperties());
			appendMarkdownExamples(markdown, rule.getExamples());
		}

		return markdown.toString();
	}

	/**
	 * Appends properties section to Markdown output.
	 *
	 * @param markdown   the StringBuilder to append to
	 * @param properties the list of properties to append
	 */
	private void appendMarkdownProperties(StringBuilder markdown, List<RuleProperty> properties)
	{
		if (properties.isEmpty())
			return;

		markdown.append("### Properties\n\n").
			append("| Property | Type | Default | Description |\n").
			append("|----------|------|---------|-------------|\n");
		for (RuleProperty property : properties)
		{
			markdown.append("| ").append(property.name()).
				append(" | ").append(property.type()).
				append(" | ").append(property.defaultValue()).
				append(" | ").append(property.description()).
				append(" |\n");
		}
		markdown.append('\n');
	}

	/**
	 * Appends examples section to Markdown output.
	 *
	 * @param markdown the StringBuilder to append to
	 * @param examples the list of examples to append
	 */
	private void appendMarkdownExamples(StringBuilder markdown, List<RuleExample> examples)
	{
		if (examples.isEmpty())
			return;

		markdown.append("### Examples\n\n");
		for (RuleExample example : examples)
		{
			markdown.append("**").append(example.description()).append("**\n\n").
				append("Incorrect:\n```java\n").append(example.incorrect()).append("\n```\n\n").
				append("Correct:\n```java\n").append(example.correct()).append("\n```\n\n");
		}
	}
}
