package io.github.cowwoc.styler.plugin.engine;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.LineLengthRuleConfiguration;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.WrapConfiguration;
import io.github.cowwoc.styler.plugin.config.PluginConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Builds {@link FormattingContext} instances from Maven project metadata.
 * Maps Maven configuration parameters to formatter API configuration model.
 * Thread-safe and stateless for Maven parallel builds.
 */
public final class FormattingContextBuilder
{
	/**
	 * Creates FormattingContext from Maven project metadata and parsed AST.
	 *
	 * @param config validated plugin configuration containing Maven project metadata
	 * @param ast parsed AST root node (CompilationUnit)
	 * @param sourceText original source code text
	 * @param sourcePath path to source file being formatted
	 * @return immutable formatting context ready for rule processing
	 * @throws MojoExecutionException if {@code config}, {@code ast}, {@code sourceText}, or {@code sourcePath} are null
	 */
	public FormattingContext createContext(PluginConfiguration config, CompilationUnitNode ast,
		String sourceText, Path sourcePath) throws MojoExecutionException
	{
		Objects.requireNonNull(config, "config cannot be null");
		Objects.requireNonNull(ast, "ast cannot be null");
		Objects.requireNonNull(sourceText, "sourceText cannot be null");
		Objects.requireNonNull(sourcePath, "sourcePath cannot be null");

		try
		{
			RuleConfiguration ruleConfig = createRuleConfiguration();
			Map<String, Object> metadata = createMetadata(config);
			WrapConfiguration wrapConfig = WrapConfiguration.createDefault();

			return new FormattingContext(
				ast,
				sourceText,
				sourcePath,
				ruleConfig,
				Set.of("line-length", "brace-style", "indentation", "import-organizer", "whitespace"),
				metadata,
				wrapConfig);
		}
		catch (Exception e)
		{
			throw new MojoExecutionException(
				"Failed to create FormattingContext for: " + sourcePath +
				"\nEncoding: " + config.encoding() +
				"\nSource directory: " + config.sourceDirectory(),
				e);
		}
	}

	/**
	 * Creates rule configuration from Maven plugin parameters.
	 *
	 * @return rule configuration with defaults
	 */
	private RuleConfiguration createRuleConfiguration()
	{
		return new LineLengthRuleConfiguration();
	}

	/**
	 * Creates metadata map from Maven project information.
	 * Metadata provides context for rule execution and reporting.
	 *
	 * @param config plugin configuration
	 * @return metadata map
	 */
	private Map<String, Object> createMetadata(PluginConfiguration config)
	{
		return Map.of(
			"project.groupId", config.project().getGroupId(),
			"project.artifactId", config.project().getArtifactId(),
			"project.version", config.project().getVersion(),
			"source.directory", config.sourceDirectory().toString(),
			"encoding", config.encoding());
	}
}
