package io.github.cowwoc.styler.config.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility methods for test file system operations.
 * <p>
 * This class provides helper methods for managing temporary files and directories
 * in test scenarios.
 */
public final class TestFileSystemUtils
{
	private TestFileSystemUtils()
	{
		throw new AssertionError("Utility class should not be instantiated");
	}

	/**
	 * Deletes a directory and all its contents.
	 * <p>
	 * Performs depth-first deletion, removing files before their containing directories.
	 * Silently ignores failures during cleanup to avoid masking test failures.
	 *
	 * @param dir the directory to delete
	 * @throws IOException if directory traversal fails
	 */
	public static void deleteDirectory(Path dir) throws IOException
	{
		if (Files.exists(dir))
			Files.walk(dir)
				.sorted((a, b) -> b.compareTo(a))  // Delete files before directories
				.forEach(path ->
				{
					try
					{
						Files.delete(path);
					}
					catch (IOException e)
					{
						// Ignore cleanup failures
					}
				});
	}
}
