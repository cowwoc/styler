package io.github.cowwoc.styler.cli;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.ParseResult;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Parses command-line arguments for the Styler code formatter.
 * <p>
 * Uses picocli programmatic API (non-reflection based) for efficient
 * argument parsing. Supports file paths, configuration overrides,
 * and operational modes (check vs fix).
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe and can be used concurrently.
 *
 * @see CLIOptions for parsed argument representation
 * @see HelpFormatter for usage text generation
 */
public final class ArgumentParser
{
	private final HelpFormatter helpFormatter = new HelpFormatter();

	/**
	 * Parse command-line arguments and return immutable options.
	 * <p>
	 * This method parses the provided arguments and constructs an immutable
	 * {@code CLIOptions} object. If the {@code --help} or {@code --version}
	 * flags are present, throws {@code HelpRequestedException}. If arguments
	 * are invalid, throws {@code UsageException}.
	 *
	 * @param args command-line arguments from main(). If {@code args} is empty,
	 *             displays help text.
	 * @return immutable {@code CLIOptions} object
	 * @throws UsageException          if {@code args} contains invalid arguments
	 * @throws HelpRequestedException  if --help or --version requested
	 * @throws NullPointerException    if {@code args} is null
	 */
	public CLIOptions parse(String[] args) throws CLIException
	{
		requireThat(args, "args").isNotNull();

		if (args.length == 0)
			throw new HelpRequestedException(helpFormatter.formatHelp());

		CommandSpec spec = createCommandSpec();
		ParseResult parseResult = parseArguments(spec, args);
		checkForHelpOrVersion(parseResult);
		return buildOptions(parseResult);
	}

	/**
	 * Creates the command specification with all options and parameters.
	 *
	 * @return the configured command specification
	 */
	private CommandSpec createCommandSpec()
	{
		CommandSpec spec = CommandSpec.create();
		spec.name("styler");

		spec.addOption(OptionSpec.builder("--config").
			type(Path.class).
			description("Configuration file path override").
			build());

		spec.addOption(OptionSpec.builder("--check").
			type(Boolean.class).
			description("Validation-only mode").
			build());

		spec.addOption(OptionSpec.builder("--fix").
			type(Boolean.class).
			description("Auto-fix mode").
			build());

		spec.addOption(OptionSpec.builder("--help").
			type(Boolean.class).
			description("Display help message").
			build());

		spec.addOption(OptionSpec.builder("--version").
			type(Boolean.class).
			description("Display version information").
			build());

		spec.addOption(OptionSpec.builder("--classpath", "-cp").
			type(String.class).
			description("Classpath entries for type resolution (separator: " + File.pathSeparator + ")").
			build());

		spec.addOption(OptionSpec.builder("--module-path", "-p").
			type(String.class).
			description("Module path entries for type resolution (separator: " + File.pathSeparator + ")").
			build());

		spec.addOption(OptionSpec.builder("--max-concurrency").
			type(Integer.class).
			description("Maximum files to process concurrently (default: based on available memory)").
			build());

		spec.addOption(OptionSpec.builder("--max-violations").
			type(Integer.class).
			description("Maximum violations to show (0 = summary only, AI default: 50, human default: 100)").
			build());

		spec.addPositional(PositionalParamSpec.builder().
			index("0..*").
			type(String.class).
			arity("1..*").
			description("Files or directories to process").
			build());

		return spec;
	}

	/**
	 * Parses arguments using the command specification.
	 *
	 * @param spec the command specification
	 * @param args the arguments to parse
	 * @return the parse result
	 * @throws UsageException if parsing fails
	 */
	private ParseResult parseArguments(CommandSpec spec, String[] args) throws UsageException
	{
		CommandLine commandLine = new CommandLine(spec);
		commandLine.setOverwrittenOptionsAllowed(true);

		try
		{
			return commandLine.parseArgs(args);
		}
		catch (CommandLine.ParameterException e)
		{
			throw new UsageException(e.getMessage(), e);
		}
	}

	/**
	 * Checks for help or version flags and throws appropriate exception if found.
	 *
	 * @param parseResult the parse result to check
	 * @throws HelpRequestedException if --help or --version was specified
	 */
	private void checkForHelpOrVersion(ParseResult parseResult) throws HelpRequestedException
	{
		if (parseResult.hasMatchedOption("--help"))
			throw new HelpRequestedException(helpFormatter.formatHelp());
		if (parseResult.hasMatchedOption("--version"))
			throw new HelpRequestedException(helpFormatter.formatVersion());
	}

	/**
	 * Builds CLIOptions from the parse result.
	 *
	 * @param parseResult the parse result
	 * @return the built CLI options
	 * @throws UsageException if options are invalid
	 */
	private CLIOptions buildOptions(ParseResult parseResult) throws UsageException
	{
		CLIOptions.Builder builder = new CLIOptions.Builder();

		addInputPaths(parseResult, builder);
		addConfigPath(parseResult, builder);
		addModeFlags(parseResult, builder);
		addClasspathEntries(parseResult, builder);
		addModulepathEntries(parseResult, builder);
		addMaxConcurrency(parseResult, builder);
		addMaxViolations(parseResult, builder);

		try
		{
			return builder.build();
		}
		catch (IllegalArgumentException e)
		{
			throw new UsageException(e.getMessage(), e);
		}
	}

	/**
	 * Adds input paths from positional arguments to the builder.
	 *
	 * @param parseResult the parse result
	 * @param builder     the options builder
	 * @throws UsageException if no input paths are specified
	 */
	private void addInputPaths(ParseResult parseResult, CLIOptions.Builder builder) throws UsageException
	{
		List<String> positionals = List.of();
		if (!parseResult.matchedPositionals().isEmpty())
			positionals = parseResult.matchedPositionals().getFirst().originalStringValues();

		if (positionals.isEmpty())
			throw new UsageException("No input files or directories specified. " +
				"Use --help for usage information.");

		for (String pathString : positionals)
			builder.addInputPath(Path.of(pathString));
	}

	/**
	 * Adds config path if specified.
	 *
	 * @param parseResult the parse result
	 * @param builder     the options builder
	 */
	private void addConfigPath(ParseResult parseResult, CLIOptions.Builder builder)
	{
		if (parseResult.hasMatchedOption("--config"))
			builder.setConfigPath(parseResult.matchedOptionValue("--config", null));
	}

	/**
	 * Adds check/fix mode flags.
	 *
	 * @param parseResult the parse result
	 * @param builder     the options builder
	 */
	private void addModeFlags(ParseResult parseResult, CLIOptions.Builder builder)
	{
		builder.setCheckMode(parseResult.hasMatchedOption("--check"));
		builder.setFixMode(parseResult.hasMatchedOption("--fix"));
	}

	/**
	 * Adds classpath entries if specified.
	 *
	 * @param parseResult the parse result
	 * @param builder     the options builder
	 */
	private void addClasspathEntries(ParseResult parseResult, CLIOptions.Builder builder)
	{
		if (parseResult.hasMatchedOption("--classpath"))
			builder.setClasspathEntries(parsePathList(parseResult.matchedOptionValue("--classpath", "")));
		else
			builder.setClasspathEntries(List.of());
	}

	/**
	 * Adds modulepath entries if specified.
	 *
	 * @param parseResult the parse result
	 * @param builder     the options builder
	 */
	private void addModulepathEntries(ParseResult parseResult, CLIOptions.Builder builder)
	{
		if (parseResult.hasMatchedOption("--module-path"))
			builder.setModulepathEntries(parsePathList(parseResult.matchedOptionValue("--module-path", "")));
		else
			builder.setModulepathEntries(List.of());
	}

	/**
	 * Adds max concurrency if specified.
	 *
	 * @param parseResult the parse result
	 * @param builder     the options builder
	 * @throws UsageException if the value is not a positive integer
	 */
	private void addMaxConcurrency(ParseResult parseResult, CLIOptions.Builder builder) throws UsageException
	{
		if (parseResult.hasMatchedOption("--max-concurrency"))
		{
			Integer value = parseResult.matchedOptionValue("--max-concurrency", null);
			if (value == null || value <= 0)
			{
				throw new UsageException(
					"--max-concurrency requires a positive integer value");
			}
			builder.setMaxConcurrency(value);
		}
	}

	/**
	 * Adds max violations if specified.
	 *
	 * @param parseResult the parse result
	 * @param builder     the options builder
	 * @throws UsageException if the value is negative
	 */
	private void addMaxViolations(ParseResult parseResult, CLIOptions.Builder builder) throws UsageException
	{
		if (parseResult.hasMatchedOption("--max-violations"))
		{
			Integer value = parseResult.matchedOptionValue("--max-violations", null);
			if (value == null || value < 0)
			{
				throw new UsageException(
					"--max-violations requires a non-negative integer value");
			}
			builder.setMaxViolations(value);
		}
	}

	/**
	 * Parses a path-separated string into a list of paths.
	 *
	 * @param pathListString the path-separated string (uses platform separator)
	 * @return list of paths, empty if input is null or blank
	 */
	private List<Path> parsePathList(String pathListString)
	{
		if (pathListString == null || pathListString.isBlank())
			return List.of();
		return Arrays.stream(pathListString.split(Pattern.quote(File.pathSeparator))).
			map(String::strip).
			filter(s -> !s.isEmpty()).
			map(Path::of).
			toList();
	}
}
