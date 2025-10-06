package io.github.cowwoc.styler.plugin.engine;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.FormattingRule;
import io.github.cowwoc.styler.plugin.config.PluginConfiguration;
import org.apache.maven.plugin.MojoExecutionException;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for file processing strategies using Template Method pattern.
 * Defines common algorithm for parsing source, loading rules, and applying rules.
 * Subclasses implement specific result processing (violations vs edits).
 * Thread-safe and stateless for Maven parallel builds.
 */
public abstract class AbstractProcessingStrategy implements FileProcessingStrategy
{
	/** Plugin configuration containing Maven project metadata. */
	protected final PluginConfiguration config;
	/** AST parser for source files. */
	protected final SourceParser parser;
	/** Builder for creating formatting contexts. */
	protected final FormattingContextBuilder contextBuilder;
	/** Loader for formatting rules. */
	protected final FormattingRuleLoader ruleLoader;

	/**
	 * Creates processing strategy with required components.
	 *
	 * @param config plugin configuration containing Maven project metadata
	 * @param parser AST parser for source files
	 * @param contextBuilder creates formatting contexts
	 * @param ruleLoader loads formatting rules
	 * @throws NullPointerException if any parameter is null
	 */
	protected AbstractProcessingStrategy(PluginConfiguration config, SourceParser parser,
		FormattingContextBuilder contextBuilder, FormattingRuleLoader ruleLoader)
	{
		this.config = Objects.requireNonNull(config, "config cannot be null");
		this.parser = Objects.requireNonNull(parser, "parser cannot be null");
		this.contextBuilder = Objects.requireNonNull(contextBuilder, "contextBuilder cannot be null");
		this.ruleLoader = Objects.requireNonNull(ruleLoader, "ruleLoader cannot be null");
	}

	/**
	 * Template method defining the processing algorithm.
	 * Parses source to AST, loads rules, applies rules, and processes results.
	 *
	 * @param sourcePath path to source file being processed
	 * @param sourceText content of source file
	 * @return processing result with violations or edits
	 * @throws MojoExecutionException if processing fails
	 */
	@Override
	public final ProcessingResult process(Path sourcePath, String sourceText) throws MojoExecutionException
	{
		Objects.requireNonNull(sourcePath, "sourcePath cannot be null");
		Objects.requireNonNull(sourceText, "sourceText cannot be null");

		try
		{
			// Parse source to AST
			CompilationUnitNode ast = parser.parse(sourceText, sourcePath.toString());

			// Load all formatting rules
			List<FormattingRule> rules = ruleLoader.loadRules();

			// Apply all rules and collect results
			ResultCollector collector = createResultCollector();

			for (FormattingRule rule : rules)
			{
				FormattingContext context = contextBuilder.createContext(config, ast, sourceText, sourcePath, rule);

				// Run rule and collect results
				FormattingResult result = rule.apply(context);
				collector.collect(rule, result);
			}

			// Process collected results (subclass-specific)
			return processResults(collector, sourcePath, sourceText);
		}
		catch (Exception e)
		{
			throw new MojoExecutionException(
				"Failed to process file: " + sourcePath + "\nError: " + e.getMessage(), e);
		}
	}

	/**
	 * Creates result collector for this strategy type.
	 * Hook method for subclasses to provide appropriate collector.
	 *
	 * @return result collector for violations or edits
	 */
	protected abstract ResultCollector createResultCollector();

	/**
	 * Processes collected results and creates final processing result.
	 * Hook method for subclasses to implement strategy-specific result handling.
	 *
	 * @param collector result collector with gathered violations or edits
	 * @param sourcePath path to source file
	 * @param sourceText content of source file
	 * @return final processing result
	 * @throws Exception if result processing fails
	 */
	protected abstract ProcessingResult processResults(ResultCollector collector, Path sourcePath,
		String sourceText) throws Exception;

	/**
	 * Base interface for collecting rule application results.
	 * Subclasses provide implementations for violation or edit collection.
	 */
	@FunctionalInterface
	protected interface ResultCollector
	{
		/**
		 * Collects result from a formatting rule application.
		 *
		 * @param rule the formatting rule that was applied
		 * @param result the result from applying the rule
		 */
		void collect(FormattingRule rule, FormattingResult result);
	}
}
