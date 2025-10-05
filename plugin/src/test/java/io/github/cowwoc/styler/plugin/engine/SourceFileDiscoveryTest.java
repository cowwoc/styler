package io.github.cowwoc.styler.plugin.engine;

import org.testng.annotations.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests for {@link SourceFileDiscovery}.
 * Validates file discovery with include/exclude patterns, edge cases, and error handling.
 * All tests are thread-safe (TestNG parallel execution compatible).
 */
public class SourceFileDiscoveryTest
{
	/**
	 * Verifies that **\/*.java pattern matches all Java files recursively.
	 *
	 * @throws IOException if temp directory operations fail
	 */
	@Test
	public void testIncludePatternMatchingAllJavaFiles() throws IOException
	{
		// Create temporary directory structure with Java files
		Path tempDir = Files.createTempDirectory("test-discovery");
		try
		{
			Path srcDir = tempDir.resolve("src");
			Files.createDirectories(srcDir);
			Path nestedDir = srcDir.resolve("nested");
			Files.createDirectories(nestedDir);

			Path file1 = Files.createFile(srcDir.resolve("File1.java"));
			Path file2 = Files.createFile(nestedDir.resolve("File2.java"));
			Files.createFile(srcDir.resolve("NotJava.txt")); // Should be excluded

			SourceFileDiscovery discovery = new SourceFileDiscovery();
			List<Path> result = discovery.discoverFiles(srcDir, List.of("**/*.java"), List.of());

			assertEquals(result.size(), 2, "Should discover exactly 2 Java files");
			assertTrue(result.contains(file1), "Should include File1.java");
			assertTrue(result.contains(file2), "Should include nested File2.java");
		}
		finally
		{
			// Cleanup
			deleteDirectory(tempDir);
		}
	}

	/**
	 * Verifies that **\/generated\/** pattern excludes generated directory.
	 *
	 * @throws IOException if temp directory operations fail
	 */
	@Test
	public void testExcludePatternMatchingGeneratedDirectory() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-exclude");
		try
		{
			Path srcDir = tempDir.resolve("src");
			Files.createDirectories(srcDir);
			Path generatedDir = srcDir.resolve("generated");
			Files.createDirectories(generatedDir);

			Path file1 = Files.createFile(srcDir.resolve("Source.java"));
			Path file2 = Files.createFile(generatedDir.resolve("Generated.java"));

			SourceFileDiscovery discovery = new SourceFileDiscovery();
			List<Path> result = discovery.discoverFiles(srcDir, List.of("**/*.java"), List.of("**/generated/**"));

			assertEquals(result.size(), 1, "Should discover only 1 file (exclude generated)");
			assertTrue(result.contains(file1), "Should include Source.java");
			assertFalse(result.contains(file2), "Should exclude generated/Generated.java");
		}
		finally
		{
			deleteDirectory(tempDir);
		}
	}

	/**
	 * Verifies that empty directory returns empty list (not an error).
	 *
	 * @throws IOException if temp directory operations fail
	 */
	@Test
	public void testEmptyDirectoryHandling() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-empty");
		try
		{
			SourceFileDiscovery discovery = new SourceFileDiscovery();
			List<Path> result = discovery.discoverFiles(tempDir, List.of("**/*.java"), List.of());

			assertNotNull(result, "Result should not be null");
			assertTrue(result.isEmpty(), "Empty directory should return empty list");
		}
		finally
		{
			deleteDirectory(tempDir);
		}
	}

	/**
	 * Verifies that non-existent directory returns empty list (not an error).
	 *
	 * @throws IOException if discovery fails unexpectedly
	 */
	@Test
	public void testNonExistentDirectoryHandling() throws IOException
	{
		Path nonExistent = Path.of("/nonexistent/directory/that/does/not/exist");

		SourceFileDiscovery discovery = new SourceFileDiscovery();
		List<Path> result = discovery.discoverFiles(nonExistent, List.of("**/*.java"), List.of());

		assertNotNull(result, "Result should not be null");
		assertTrue(result.isEmpty(), "Non-existent directory should return empty list");
	}

	/**
	 * Verifies that null sourceDirectory throws NullPointerException.
	 *
	 * @throws IOException if discovery fails (not expected due to null check)
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testNullSourceDirectoryThrowsException() throws IOException
	{
		SourceFileDiscovery discovery = new SourceFileDiscovery();
		discovery.discoverFiles(null, List.of("**/*.java"), List.of());
	}

	/**
	 * Verifies that null includes throws IllegalArgumentException.
	 *
	 * @throws IOException if discovery fails
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testNullIncludesThrowsException() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-null");
		try
		{
			SourceFileDiscovery discovery = new SourceFileDiscovery();
			discovery.discoverFiles(tempDir, null, List.of());
		}
		finally
		{
			deleteDirectory(tempDir);
		}
	}

	/**
	 * Verifies that null excludes throws NullPointerException.
	 *
	 * @throws IOException if temp directory operations fail
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testNullExcludesThrowsException() throws IOException
	{
		Path tempDir = null;
		try
		{
			tempDir = Files.createTempDirectory("test-null");
			SourceFileDiscovery discovery = new SourceFileDiscovery();
			discovery.discoverFiles(tempDir, List.of("**/*.java"), null);
		}
		finally
		{
			if (tempDir != null)
				deleteDirectory(tempDir);
		}
	}

	/**
	 * Helper method to recursively delete directory and all contents.
	 *
	 * @param directory directory to delete
	 * @throws IOException if deletion fails
	 */
	private static void deleteDirectory(Path directory) throws IOException
	{
		if (Files.exists(directory))
		{
			Files.walk(directory).
				sorted((a, b) -> -a.compareTo(b)). // Delete files before directories
				forEach(path ->
				{
					try
					{
						Files.delete(path);
					}
					catch (IOException ignored)
					{
						// Intentionally ignore cleanup failures in test teardown
					}
				});
		}
	}
}
