package io.github.cowwoc.styler.plugin.engine;

import io.github.cowwoc.styler.formatter.api.FormattingRule;
import io.github.cowwoc.styler.formatter.impl.BraceFormatterFormattingRule;
import io.github.cowwoc.styler.formatter.impl.ImportOrganizerFormattingRule;
import io.github.cowwoc.styler.formatter.impl.IndentationFormattingRule;
import io.github.cowwoc.styler.formatter.impl.LineLengthFormattingRule;
import io.github.cowwoc.styler.formatter.impl.WhitespaceFormatter;
import org.apache.maven.plugin.MojoExecutionException;
import java.util.List;

/**
 * Loads {@link FormattingRule} instances from formatter-rules module.
 * Instantiates all available formatting rules with default configuration.
 * Thread-safe and stateless for Maven parallel builds.
 * Non-final to enable test mocking.
 */
public class FormattingRuleLoader
{
	/**
	 * Loads all available formatting rules from classpath.
	 * Rules are instantiated with default configuration.
	 *
	 * @return list of formatting rule instances (immutable)
	 * @throws MojoExecutionException if no rules can be loaded
	 */
	public List<FormattingRule> loadRules() throws MojoExecutionException
	{
		try
		{
			List<FormattingRule> rules = List.of(
				new LineLengthFormattingRule(),
				new BraceFormatterFormattingRule(),
				new IndentationFormattingRule(),
				new ImportOrganizerFormattingRule(),
				new WhitespaceFormatter());

			if (rules.isEmpty())
			{
				throw new MojoExecutionException(
					"No formatting rules found in classpath.\n" +
					"Verify styler-formatter-rules dependency is present in plugin/pom.xml");
			}

			return rules;
		}
		catch (Exception e)
		{
			throw new MojoExecutionException(
				"Failed to load formatting rules: " + e.getMessage(), e);
		}
	}
}
