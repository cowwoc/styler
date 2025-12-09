package io.github.cowwoc.styler.discovery;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Path;
import java.util.Map;
import java.util.List;

/**
 * Result of file discovery operation.
 * <p>
 * Contains discovered files and any errors encountered during discovery. Errors do not stop
 * discovery - other paths continue to be processed.
 *
 * @param files successfully discovered Java files (sorted by path)
 * @param errors map of path to error message for failed paths
 * @param filesScanned total number of files examined
 * @param directoriesScanned total number of directories traversed
 */
public record FileDiscoveryResult(
	List<Path> files,
	Map<Path, String> errors,
	int filesScanned,
	int directoriesScanned)
{
	/**
	 * Compact constructor for validation.
	 *
	 * @param files list of discovered files (non-null)
	 * @param errors map of errors (non-null)
	 * @param filesScanned number of files scanned
	 * @param directoriesScanned number of directories scanned
	 * @throws NullPointerException if {@code files} or {@code errors} is {@code null}
	 * @throws IllegalArgumentException if scan counts are negative
	 */
	public FileDiscoveryResult
	{
		requireThat(files, "files").isNotNull();
		requireThat(errors, "errors").isNotNull();
		requireThat(filesScanned, "filesScanned").isNotNegative();
		requireThat(directoriesScanned, "directoriesScanned").isNotNegative();

		files = List.copyOf(files);
		errors = Map.copyOf(errors);
	}

	/**
	 * Indicates whether the discovery operation completed successfully without errors.
	 *
	 * @return true if any errors were encountered during discovery
	 */
	public boolean hasErrors()
	{
		return !errors.isEmpty();
	}
}
