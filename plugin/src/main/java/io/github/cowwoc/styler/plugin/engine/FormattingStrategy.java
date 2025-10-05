package io.github.cowwoc.styler.plugin.engine;

import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.FormattingRule;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.plugin.config.PluginConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Formatting strategy that applies formatting rules and writes changes to files.
 * Modifies source files in-place with formatted code.
 * Thread-safe and stateless for Maven parallel builds.
 */
public final class FormattingStrategy extends AbstractProcessingStrategy
{
	private final TextEditApplicator editApplicator;

	/**
	 * Creates a formatting strategy with required components.
	 *
	 * @param config plugin configuration containing Maven project metadata
	 * @param parser AST parser for source files
	 * @param contextBuilder creates formatting contexts
	 * @param ruleLoader loads formatting rules
	 * @param editApplicator applies text edits to source
	 * @throws NullPointerException if any parameter is null
	 */
	public FormattingStrategy(PluginConfiguration config, SourceParser parser,
		FormattingContextBuilder contextBuilder, FormattingRuleLoader ruleLoader,
		TextEditApplicator editApplicator)
	{
		super(config, parser, contextBuilder, ruleLoader);
		this.editApplicator = Objects.requireNonNull(editApplicator, "editApplicator cannot be null");
	}

	@Override
	protected ResultCollector createResultCollector()
	{
		return new EditCollector();
	}

	@Override
	protected ProcessingResult processResults(ResultCollector collector, Path sourcePath,
		String sourceText) throws IOException
	{
		EditCollector editCollector = (EditCollector) collector;
		List<TextEdit> allEdits = editCollector.getEdits();

		if (allEdits.isEmpty())
		{
			return ProcessingResult.clean();
		}

		// Apply all edits to source text
		String formattedText = editApplicator.applyEdits(sourceText, allEdits);

		// Write formatted text back to file
		Files.writeString(sourcePath, formattedText);

		return ProcessingResult.withEdits(allEdits.size());
	}

	@Override
	public String getDescription()
	{
		return "Apply formatting rules and write changes to files";
	}

	/**
	 * Collects text edits from formatting rule applications.
	 */
	private static final class EditCollector implements ResultCollector
	{
		private final List<TextEdit> edits = new ArrayList<>();

		@Override
		public void collect(FormattingRule rule, FormattingResult result)
		{
			edits.addAll(result.getEdits());
		}

		/**
		 * Gets all collected edits.
		 *
		 * @return list of text edits
		 */
		public List<TextEdit> getEdits()
		{
			return edits;
		}
	}
}
