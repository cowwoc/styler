package io.github.cowwoc.styler.cli;

import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

import java.nio.file.Path;
import java.util.List;

/**
 * Centralized factory methods for creating picocli command specifications programmatically.
 * <p>
 * This class provides the programmatic equivalent of annotation-based command definitions,
 * eliminating reflection overhead and improving startup performance. It defines all command
 * structures, options, and parameters used by the Styler CLI.
 * <p>
 * <b>Design Pattern</b>: All factory methods return fully configured {@link CommandSpec}
 * instances that can be used directly with {@link picocli.CommandLine} for parsing.
 */
public final class CommandSpecifications
{
	// Option name constants for consistency and refactoring safety
	public static final String OPT_VERBOSE_SHORT = "-v";
	public static final String OPT_VERBOSE_LONG = "--verbose";
	public static final String OPT_QUIET_SHORT = "-q";
	public static final String OPT_QUIET_LONG = "--quiet";
	public static final String OPT_CONFIG_SHORT = "-c";
	public static final String OPT_CONFIG_LONG = "--config";
	public static final String OPT_OUTPUT_SHORT = "-o";
	public static final String OPT_OUTPUT_LONG = "--output";
	public static final String OPT_DRY_RUN = "--dry-run";
	public static final String OPT_INCLUDE = "--include";
	public static final String OPT_EXCLUDE = "--exclude";
	public static final String OPT_JSON = "--json";
	public static final String OPT_FAIL_ON_CHANGES = "--fail-on-changes";
	public static final String OPT_MAX_CONCURRENT_SHORT = "-t";
	public static final String OPT_MAX_CONCURRENT_LONG = "--max-concurrent-files";
	public static final String OPT_FIX = "--fix";
	public static final String OPT_REPORT = "--report";

	/**
	 * Prevents instantiation of utility class.
	 */
	private CommandSpecifications()
	{
	}

	/**
	 * Creates the root command specification for the Styler CLI.
	 * <p>
	 * The root command provides global options (verbose, quiet) and coordinates
	 * subcommand execution (format, check, config).
	 *
	 * @return fully configured root command specification
	 */
	public static CommandSpec createRootCommandSpec()
	{
		CommandSpec spec = CommandSpec.create();
		spec.name("styler");
		spec.usageMessage().description("Java Code Formatter with AI-friendly output");
		spec.addOption(createVerboseOption());
		spec.addOption(createQuietOption());
		spec.mixinStandardHelpOptions(true);
		return spec;
	}

	/**
	 * Creates the format subcommand specification.
	 * <p>
	 * The format command applies code formatting to Java source files, supporting
	 * in-place modification or output to specified directories.
	 *
	 * @return fully configured format command specification
	 */
	public static CommandSpec createFormatCommandSpec()
	{
		CommandSpec spec = CommandSpec.create();
		spec.name("format");
		spec.usageMessage().description("Format Java source files according to configured rules");
		spec.addPositional(createFileParametersSpec());
		spec.addOption(createConfigOption());
		spec.addOption(createOutputOption());
		spec.addOption(createDryRunOption());
		spec.addOption(createIncludeOption());
		spec.addOption(createExcludeOption());
		spec.addOption(createJsonOption());
		spec.addOption(createFailOnChangesOption());
		spec.addOption(createMaxConcurrentFilesOption());
		spec.mixinStandardHelpOptions(true);
		return spec;
	}

	/**
	 * Creates the check subcommand specification.
	 * <p>
	 * The check command validates Java source files against configured rules
	 * without modifying files.
	 *
	 * @return fully configured check command specification
	 */
	public static CommandSpec createCheckCommandSpec()
	{
		CommandSpec spec = CommandSpec.create();
		spec.name("check");
		spec.usageMessage().description("Check Java source files for formatting violations");
		spec.addPositional(createFileParametersSpec());
		spec.addOption(createConfigOption());
		spec.addOption(createIncludeOption());
		spec.addOption(createExcludeOption());
		spec.addOption(createJsonOption());
		spec.addOption(createFixOption());
		spec.addOption(createReportOption());
		spec.mixinStandardHelpOptions(true);
		return spec;
	}

	/**
	 * Creates the config subcommand specification.
	 * <p>
	 * The config command manages configuration files and displays current settings.
	 *
	 * @return fully configured config command specification
	 */
	public static CommandSpec createConfigCommandSpec()
	{
		CommandSpec spec = CommandSpec.create();
		spec.name("config");
		spec.usageMessage().description("Manage Styler configuration");
		spec.mixinStandardHelpOptions(true);
		return spec;
	}

	/**
	 * Creates the verbose option specification used in root command.
	 *
	 * @return configured verbose option
	 */
	private static OptionSpec createVerboseOption()
	{
		return OptionSpec.builder(OPT_VERBOSE_SHORT, OPT_VERBOSE_LONG).
			type(boolean.class).description("Enable verbose logging").build();
	}

	/**
	 * Creates the quiet option specification used in root command.
	 *
	 * @return configured quiet option
	 */
	private static OptionSpec createQuietOption()
	{
		return OptionSpec.builder(OPT_QUIET_SHORT, OPT_QUIET_LONG).
			type(boolean.class).description("Suppress all output except errors").build();
	}

	/**
	 * Creates the config file option specification used across multiple commands.
	 *
	 * @return configured config option
	 */
	private static OptionSpec createConfigOption()
	{
		return OptionSpec.builder(OPT_CONFIG_SHORT, OPT_CONFIG_LONG).
			type(Path.class).description("Configuration file path (default: auto-discover)").build();
	}

	/**
	 * Creates the output directory option specification for format command.
	 *
	 * @return configured output option
	 */
	private static OptionSpec createOutputOption()
	{
		return OptionSpec.builder(OPT_OUTPUT_SHORT, OPT_OUTPUT_LONG).
			type(Path.class).description("Output directory (default: format in-place)").build();
	}

	/**
	 * Creates the dry-run option specification for format command.
	 *
	 * @return configured dry-run option
	 */
	private static OptionSpec createDryRunOption()
	{
		return OptionSpec.builder(OPT_DRY_RUN).
			type(boolean.class).description("Show what would be formatted without making changes").build();
	}

	/**
	 * Creates the include patterns option specification.
	 *
	 * @return configured include option
	 */
	private static OptionSpec createIncludeOption()
	{
		return OptionSpec.builder(OPT_INCLUDE).
			type(List.class).
			auxiliaryTypes(String.class).
			paramLabel("<pattern>").
			description("File patterns to include (default: **/*.java)").
			build();
	}

	/**
	 * Creates the exclude patterns option specification.
	 *
	 * @return configured exclude option
	 */
	private static OptionSpec createExcludeOption()
	{
		return OptionSpec.builder(OPT_EXCLUDE).
			type(List.class).
			auxiliaryTypes(String.class).
			paramLabel("<pattern>").
			description("File patterns to exclude").
			build();
	}

	/**
	 * Creates the JSON output option specification.
	 *
	 * @return configured JSON option
	 */
	private static OptionSpec createJsonOption()
	{
		return OptionSpec.builder(OPT_JSON).
			type(boolean.class).description("Output results in JSON format for machine processing").build();
	}

	/**
	 * Creates the fail-on-changes option specification for format command.
	 *
	 * @return configured fail-on-changes option
	 */
	private static OptionSpec createFailOnChangesOption()
	{
		return OptionSpec.builder(OPT_FAIL_ON_CHANGES).
			type(boolean.class).description("Exit with non-zero code if any files would be changed").build();
	}

	/**
	 * Creates the max-concurrent-files option specification for format command.
	 *
	 * @return configured max-concurrent-files option
	 */
	private static OptionSpec createMaxConcurrentFilesOption()
	{
		return OptionSpec.builder(OPT_MAX_CONCURRENT_SHORT, OPT_MAX_CONCURRENT_LONG).
			type(int.class).
			paramLabel("<N>").
			defaultValue("1").
			description("Maximum concurrent files to process (default: 1 for sequential processing)").
			build();
	}

	/**
	 * Creates the fix option specification for check command.
	 *
	 * @return configured fix option
	 */
	private static OptionSpec createFixOption()
	{
		return OptionSpec.builder(OPT_FIX).
			type(boolean.class).description("Automatically fix violations where possible").build();
	}

	/**
	 * Creates the report option specification for check command.
	 *
	 * @return configured report option
	 */
	private static OptionSpec createReportOption()
	{
		return OptionSpec.builder(OPT_REPORT).
			type(Path.class).paramLabel("<file>").description("Write report to specified file").build();
	}

	/**
	 * Creates the file parameters specification used by format and check commands.
	 * <p>
	 * Accepts one or more file paths or directories as positional arguments.
	 *
	 * @return configured positional parameter specification
	 */
	private static PositionalParamSpec createFileParametersSpec()
	{
		return PositionalParamSpec.builder().
			index("0..*").
			type(List.class).
			auxiliaryTypes(Path.class).
			paramLabel("<files>").
			description("Java source files or directories to format").
			arity("1..*").
			build();
	}
}
