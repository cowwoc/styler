package io.github.cowwoc.styler.cli;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.ParseResult;

import java.nio.file.Path;
import java.util.List;

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

		// Handle empty args as help request
		if (args.length == 0)
		{
			throw new HelpRequestedException(helpFormatter.formatHelp());
		}

		// Create command specification programmatically
		CommandSpec spec = CommandSpec.create();
		spec.name("styler");

		// Add options
		OptionSpec configOption = OptionSpec.builder("--config").
			type(Path.class).
			description("Configuration file path override").
			build();

		OptionSpec checkOption = OptionSpec.builder("--check").
			type(Boolean.class).
			description("Validation-only mode").
			build();

		OptionSpec fixOption = OptionSpec.builder("--fix").
			type(Boolean.class).
			description("Auto-fix mode").
			build();

		OptionSpec helpOption = OptionSpec.builder("--help").
			type(Boolean.class).
			description("Display help message").
			build();

		OptionSpec versionOption = OptionSpec.builder("--version").
			type(Boolean.class).
			description("Display version information").
			build();

		spec.addOption(configOption);
		spec.addOption(checkOption);
		spec.addOption(fixOption);
		spec.addOption(helpOption);
		spec.addOption(versionOption);

		// Add positional parameters (file paths) - variadic parameter
		PositionalParamSpec filesParam = PositionalParamSpec.builder().
			index("0..*").
			type(String.class).
			arity("1..*").
			description("Files or directories to process").
			build();

		spec.addPositional(filesParam);

		// Parse arguments
		CommandLine commandLine = new CommandLine(spec);
		commandLine.setOverwrittenOptionsAllowed(true);
		ParseResult parseResult;

		try
		{
			parseResult = commandLine.parseArgs(args);
		}
		catch (CommandLine.ParameterException e)
		{
			throw new UsageException(e.getMessage(), e);
		}

		// Check for help/version flags
		if (parseResult.hasMatchedOption("--help"))
		{
			throw new HelpRequestedException(helpFormatter.formatHelp());
		}

		if (parseResult.hasMatchedOption("--version"))
		{
			throw new HelpRequestedException(helpFormatter.formatVersion());
		}

		// Build CLIOptions from parse result
		CLIOptions.Builder builder = new CLIOptions.Builder();

		// Add file paths - get all matched positionals as strings and convert to Path
		List<String> positionals = List.of();
		if (!parseResult.matchedPositionals().isEmpty())
		{
			positionals = parseResult.matchedPositionals().getFirst().originalStringValues();
		}

		if (positionals.isEmpty())
		{
			throw new UsageException("No input files or directories specified. " +
				"Use --help for usage information.");
		}

		for (String pathString : positionals)
		{
			builder.addInputPath(Path.of(pathString));
		}

		// Add config path if specified
		if (parseResult.hasMatchedOption("--config"))
		{
			Path configPath = parseResult.matchedOptionValue("--config", null);
			builder.setConfigPath(configPath);
		}

		boolean checkMode = parseResult.hasMatchedOption("--check");
		boolean fixMode = parseResult.hasMatchedOption("--fix");

		builder.setCheckMode(checkMode);
		builder.setFixMode(fixMode);

		// Build and return (validation happens in CLIOptions constructor)
		try
		{
			return builder.build();
		}
		catch (IllegalArgumentException e)
		{
			throw new UsageException(e.getMessage(), e);
		}
	}
}
