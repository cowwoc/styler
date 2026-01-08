package io.github.cowwoc.styler.cli;

/**
 * Generates formatted help text and version information for the Styler CLI.
 * <p>
 * Follows Unix help text conventions with clear examples and exit code
 * documentation.
 *
 * @see ArgumentParser for argument parsing logic
 */
public final class HelpFormatter
{
	private static final String USAGE_TEXT = """
		Usage: styler [OPTIONS] <file-or-directory>...

		An unopinionated Java code formatter that preserves your code style choices.

		OPTIONS:
		  --config <file>       Configuration file path override
		  --check               Validation-only mode (exit code 1 if changes needed)
		  --fix                 Auto-fix mode (modify files in-place)
		  --classpath, -cp      Classpath entries for type resolution (platform separator)
		  --module-path, -p     Module path entries for type resolution (platform separator)
		  --max-concurrency <n> Maximum files to process concurrently (default: auto)
		  --help                Display this help message
		  --version             Display version information

		ARGUMENTS:
		  <file-or-directory>...    One or more files or directories to process

		EXAMPLES:
		  styler src/main/java                  # Format all files in directory
		  styler --check src/                   # Check formatting without changes
		  styler --fix MyClass.java             # Fix formatting in file
		  styler --config custom.xml src/       # Use custom configuration
		  styler --classpath lib/a.jar src/     # Use classpath for type resolution
		  styler --max-concurrency 1 src/       # Process files sequentially

		EXIT CODES:
		  0    Success (no formatting issues or all fixed)
		  1    Formatting issues found (--check mode only)
		  2    Invalid usage or arguments

		For more information, visit: https://github.com/cowwoc/styler
		""";

	/**
	 * Generate full help text with usage, options, and examples.
	 * <p>
	 * This method returns formatted help text following Unix conventions,
	 * including usage syntax, option descriptions, examples, and exit codes.
	 *
	 * @return formatted help text following Unix conventions
	 */
	public String formatHelp()
	{
		return USAGE_TEXT;
	}

	/**
	 * Generate version information including Java runtime and build date.
	 * <p>
	 * This method returns version information for the Styler CLI, including
	 * the application version, Java version, and JVM details.
	 *
	 * @return version information string
	 */
	public String formatVersion()
	{
		String stylerVersion = getModuleVersion();
		String javaVersion = System.getProperty("java.version");
		String javaVendor = System.getProperty("java.vendor");
		String javaVmVersion = System.getProperty("java.vm.version");

		return String.format("""
			Styler Java Code Formatter %s
			Java version: %s (%s)
			Java VM version: %s
			""", stylerVersion, javaVersion, javaVendor, javaVmVersion);
	}

	/**
	 * Returns the module version from the module descriptor.
	 *
	 * @return the module version, or "UNKNOWN" if not available
	 */
	private String getModuleVersion()
	{
		Module module = getClass().getModule();
		if (module != null && module.getDescriptor() != null)
			return module.getDescriptor().rawVersion().orElse("UNKNOWN");
		return "UNKNOWN";
	}
}
