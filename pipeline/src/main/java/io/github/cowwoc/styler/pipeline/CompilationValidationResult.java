package io.github.cowwoc.styler.pipeline;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Path;
import java.util.List;

/**
 * Result of compilation validation for source files.
 * <p>
 * Indicates whether source files have been compiled and whether the class files are up-to-date.
 * When validation fails, provides details about missing or stale class files along with
 * actionable guidance for the user.
 * <p>
 * <b>Thread-safety</b>: All implementations are immutable and thread-safe.
 */
public sealed interface CompilationValidationResult
		permits CompilationValidationResult.Valid, CompilationValidationResult.Invalid
{
	/**
	 * Indicates all source files have valid, up-to-date class files on the classpath.
	 */
	record Valid() implements CompilationValidationResult
	{
	}

	/**
	 * Indicates one or more source files have missing or stale class files.
	 * <p>
	 * Contains details about which class files are missing (not found on classpath) or stale
	 * (older than the source file).
	 *
	 * @param missingClasses list of fully-qualified names for class files that do not exist on the classpath
	 * @param staleClasses list of fully-qualified names for class files older than the source file
	 * @param sourceFile the source file that failed validation, or {@code null} for aggregated results
	 */
	record Invalid(
			List<String> missingClasses,
			List<String> staleClasses,
			Path sourceFile) implements CompilationValidationResult
	{
		/**
		 * Creates an Invalid result with details about missing and stale classes.
		 *
		 * @param missingClasses list of fully-qualified names for missing class files
		 * @param staleClasses list of fully-qualified names for stale class files
		 * @param sourceFile the source file that failed validation, or {@code null} for aggregated results
		 * @throws NullPointerException if {@code missingClasses} or {@code staleClasses} is {@code null}
		 * @throws IllegalArgumentException if both {@code missingClasses} and {@code staleClasses} are empty
		 */
		public Invalid
		{
			requireThat(missingClasses, "missingClasses").isNotNull();
			requireThat(staleClasses, "staleClasses").isNotNull();
			// sourceFile can be null for aggregated results
			if (missingClasses.isEmpty() && staleClasses.isEmpty())
			{
				throw new IllegalArgumentException("At least one of missingClasses or staleClasses must " +
					"be non-empty");
			}
			missingClasses = List.copyOf(missingClasses);
			staleClasses = List.copyOf(staleClasses);
		}

		/**
		 * Returns a user-friendly error message describing the validation failure.
		 * <p>
		 * The message includes:
		 * <ul>
		 *   <li>The source file path (if available)</li>
		 *   <li>List of missing class files (if any)</li>
		 *   <li>List of stale class files (if any)</li>
		 *   <li>Guidance to run compilation</li>
		 * </ul>
		 *
		 * @return formatted error message suitable for display to users
		 */
		@SuppressWarnings("PMD.ConsecutiveAppendsShouldReuse")
		public String getErrorMessage()
		{
			// Estimate size: header + per-class entries + footer
			int estimatedSize = 200 + (missingClasses.size() + staleClasses.size()) * 50;
			StringBuilder message = new StringBuilder(estimatedSize);

			if (sourceFile != null)
				message.append("Compilation required for: ").append(sourceFile).append('\n');
			else
				message.append("Compilation required before formatting.\n");

			if (!missingClasses.isEmpty())
			{
				message.append("Missing class files:\n");
				for (String className : missingClasses)
					message.append("  - ").append(className).append('\n');
			}

			if (!staleClasses.isEmpty())
			{
				message.append("Stale class files (source is newer):\n");
				for (String className : staleClasses)
					message.append("  - ").append(className).append('\n');
			}

			message.append("\nRun 'mvn compile' or 'javac' before formatting.");
			return message.toString();
		}
	}
}
