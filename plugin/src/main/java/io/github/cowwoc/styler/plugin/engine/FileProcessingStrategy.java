package io.github.cowwoc.styler.plugin.engine;

import org.apache.maven.plugin.MojoExecutionException;
import java.nio.file.Path;
import java.util.List;

/**
 * Strategy for processing source files with formatting rules.
 * Defines the contract for check-only vs format-and-write operations.
 * Thread-safe and stateless for Maven parallel builds.
 */
public interface FileProcessingStrategy
{
	/**
	 * Processes a single source file according to strategy's behavior.
	 *
	 * @param sourcePath absolute path to source file
	 * @param sourceText original source code text
	 * @return processing result containing violations/edits
	 * @throws MojoExecutionException if processing fails
	 */
	ProcessingResult process(Path sourcePath, String sourceText) throws MojoExecutionException;

	/**
	 * Returns human-readable description of this strategy's behavior.
	 *
	 * @return strategy description (e.g., "Check for violations only")
	 */
	String getDescription();

	/**
	 * Result of file processing operation.
	 * Contains violations found and/or edits applied.
	 *
	 * @param violationCount number of violations found
	 * @param violations list of violation messages
	 * @param editCount number of edits applied (0 for check-only)
	 * @param modified whether file was modified
	 */
	record ProcessingResult(
		int violationCount,
		List<String> violations,
		int editCount,
		boolean modified)
	{
		/**
		 * Creates a result indicating no violations found.
		 *
		 * @return processing result with no violations or edits
		 */
		public static ProcessingResult clean()
		{
			return new ProcessingResult(0, List.of(), 0, false);
		}

		/**
		 * Creates a result for check-only operation with violations.
		 *
		 * @param violations list of violation messages found
		 * @return processing result with violations
		 */
		public static ProcessingResult withViolations(List<String> violations)
		{
			return new ProcessingResult(violations.size(), violations, 0, false);
		}

		/**
		 * Creates a result for format operation with edits applied.
		 *
		 * @param editCount number of edits applied to source file
		 * @return processing result with edit count
		 */
		public static ProcessingResult withEdits(int editCount)
		{
			return new ProcessingResult(0, List.of(), editCount, editCount > 0);
		}
	}
}
