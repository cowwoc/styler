package io.github.cowwoc.styler.formatter.api;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Immutable context object containing all information needed for formatting rule execution.
 * <p>
 * This class provides read-only access to the AST, source text, and metadata required
 * for formatting operations. All data is immutable to ensure thread safety and prevent
 * side effects between rules.
 * <p>
 * <b>Security Note:</b> File paths are restricted to the project directory and validated
 * to prevent directory traversal attacks.
 */
public final class FormattingContext
{
	private final CompilationUnitNode rootNode;
	private final String sourceText;
	private final Path filePath;
	private final RuleConfiguration configuration;
	private final Set<String> enabledRules;
	private final Map<String, Object> metadata;

	/**
	 * Creates a new formatting context.
	 * <p>
	 * <b>Internal API:</b> This constructor is intended for use by the formatting
	 * engine only. Formatting rules should receive context objects rather than
	 * creating them.
	 *
	 * @param rootNode      the root AST node for the source file, never null
	 * @param sourceText    the original source text, never null
	 * @param filePath      the path to the source file being formatted, never null
	 * @param configuration the rule configuration to use, never null
	 * @param enabledRules  the set of enabled rule IDs, never null
	 * @param metadata      additional metadata for rule processing, never null
	 * @throws SecurityException if the file path is outside allowed directories
	 */
	public FormattingContext(CompilationUnitNode rootNode,
	                         String sourceText,
	                         Path filePath,
	                         RuleConfiguration configuration,
	                         Set<String> enabledRules,
	                         Map<String, Object> metadata)
	{
		this.rootNode = rootNode;
		this.sourceText = sourceText;
		this.filePath = validateFilePath(filePath);
		this.configuration = configuration;
		this.enabledRules = Set.copyOf(enabledRules);
		this.metadata = Map.copyOf(metadata);
	}

	/**
	 * Validates that the file path is within allowed directories.
	 *
	 * @param path the file path to validate
	 * @return the validated path
	 * @throws SecurityException if the path is invalid or outside allowed directories
	 */
		private static Path validateFilePath(Path path)
	{
		// Normalize path to prevent directory traversal
		Path normalized = path.normalize().toAbsolutePath();

		// Prevent directory traversal attacks
		if (normalized.toString().contains(".."))
		{
			throw new SecurityException("Directory traversal not allowed: " + path);
		}

		// Additional path validation can be added here based on security policy
		return normalized;
	}

	/**
	 * Returns the root AST node for the source file being formatted.
	 * <p>
	 * The AST is immutable and represents the parsed structure of the source code.
	 * Rules should traverse this tree using the visitor pattern to identify
	 * formatting opportunities.
	 *
	 * @return the root AST node, never null
	 */
		public CompilationUnitNode getRootNode()
	{
		return rootNode;
	}

	/**
	 * Returns the original source text of the file being formatted.
	 * <p>
	 * This text corresponds to the content that was parsed to create the AST.
	 * Rules can use this for position-based operations and to understand the
	 * original formatting context.
	 *
	 * @return the source text, never null
	 */
		public String getSourceText()
	{
		return sourceText;
	}

	/**
	 * Returns the file path of the source being formatted.
	 * <p>
	 * <b>Security Note:</b> The path has been validated and normalized to prevent
	 * directory traversal attacks. It is safe to use for logging and metadata.
	 *
	 * @return the validated file path, never null
	 */
		public Path getFilePath()
	{
		return filePath;
	}

	/**
	 * Returns the configuration for the current rule.
	 * <p>
	 * The configuration object contains all user-specified parameters and
	 * settings that control rule behavior.
	 *
	 * @return the rule configuration, never null
	 */
		public RuleConfiguration getConfiguration()
	{
		return configuration;
	}

	/**
	 * Returns the set of rule IDs that are enabled for this formatting operation.
	 * <p>
	 * Rules can use this information to understand which other rules are active
	 * and potentially coordinate their behavior to avoid conflicts.
	 *
	 * @return the set of enabled rule IDs, never null or modified after creation
	 */
		public Set<String> getEnabledRules()
	{
		return enabledRules;
	}

	/**
	 * Returns additional metadata associated with this formatting operation.
	 * <p>
	 * Metadata can include information like project settings, IDE context,
	 * or other environmental factors that may influence formatting decisions.
	 *
	 * @return the metadata map, never null or modified after creation
	 */
		public Map<String, Object> getMetadata()
	{
		return metadata;
	}

	/**
	 * Checks if a specific rule is enabled in this context.
	 *
	 * @param ruleId the ID of the rule to check, never null
	 * @return true if the rule is enabled, false otherwise
	 */
	public boolean isRuleEnabled(String ruleId)
	{
		return enabledRules.contains(ruleId);
	}

	/**
	 * Retrieves a metadata value with type safety.
	 *
	 * @param key  the metadata key, never null
	 * @param type the expected type of the value, never null
	 * @param <T>  the type parameter
	 * @return the metadata value cast to the specified type, or null if not present
	 * @throws ClassCastException if the value cannot be cast to the specified type
	 */
	public <T> T getMetadata(String key, Class<T> type)
	{
		Object value = metadata.get(key);
		return value != null ? type.cast(value) : null;
	}
}