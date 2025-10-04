package io.github.cowwoc.styler.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Immutable result of file discovery containing discovered files and any warnings.
 *
 * <p>This value object encapsulates the outcome of a file discovery operation,
 * separating successfully discovered files from non-fatal warnings that occurred
 * during traversal.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * FileDiscovery discovery = new FileDiscovery(sanitizer, validator, depthTracker);
 * DiscoveryResult result = discovery.discover(paths, includePatterns, excludePatterns);
 *
 * // Process discovered files
 * for (Path file : result.files()) {
 *     processFile(file);
 * }
 *
 * // Log warnings
 * for (DiscoveryWarning warning : result.warnings()) {
 *     System.err.println("WARNING: " + warning.message());
 * }
 * }</pre>
 *
 * @param files the list of discovered files (immutable)
 * @param warnings the list of warnings encountered during discovery (immutable)
 * @see FileDiscovery
 * @see DiscoveryWarning
 */
public record DiscoveryResult(List<Path> files, List<DiscoveryWarning> warnings)
{
	/**
	 * Compact constructor with validation and defensive copying.
	 *
	 * @throws NullPointerException if files or warnings is {@code null}
	 */
	public DiscoveryResult
	{
		Objects.requireNonNull(files, "files must not be null");
		Objects.requireNonNull(warnings, "warnings must not be null");

		// Defensive copy to ensure immutability
		files = List.copyOf(files);
		warnings = List.copyOf(warnings);
	}

	/**
	 * Returns {@code true} if discovery completed without warnings.
	 *
	 * @return {@code true} if no warnings were encountered
	 */
	public boolean isClean()
	{
		return warnings.isEmpty();
	}

	/**
	 * Returns the total number of files discovered.
	 *
	 * @return file count
	 */
	public int fileCount()
	{
		return files.size();
	}

	/**
	 * Returns the total number of warnings encountered.
	 *
	 * @return warning count
	 */
	public int warningCount()
	{
		return warnings.size();
	}
}
