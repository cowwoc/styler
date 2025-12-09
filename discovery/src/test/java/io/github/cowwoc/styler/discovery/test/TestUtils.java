package io.github.cowwoc.styler.discovery.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility methods for test classes.
 */
public final class TestUtils
{
	/**
	 * Prevents instantiation.
	 */
	private TestUtils()
	{
	}

	/**
	 * Recursively deletes a directory and all its contents.
	 * <p>
	 * Traverses the directory tree in reverse order (deepest first) to ensure
	 * child files and directories are deleted before their parents.
	 *
	 * @param directory the directory to delete (may be null for no-op)
	 */
	public static void deleteDirectoryRecursively(Path directory)
	{
		if (directory == null)
		{
			return;
		}
		try
		{
			Files.walk(directory).sorted((a, b) -> b.compareTo(a)).forEach(path ->
			{
				try
				{
					Files.deleteIfExists(path);
				}
				catch (IOException _)
				{
					// Ignore cleanup errors - best effort deletion
				}
			});
		}
		catch (IOException _)
		{
			// Ignore - directory may not exist or be inaccessible
		}
	}
}
