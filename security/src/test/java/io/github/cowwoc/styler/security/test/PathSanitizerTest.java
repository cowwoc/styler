package io.github.cowwoc.styler.security.test;

import io.github.cowwoc.styler.security.*;

import io.github.cowwoc.styler.security.exceptions.PathTraversalException;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.*;

/**
 * Tests for PathSanitizer path traversal protection.
 */
public class PathSanitizerTest
{
	/**
	 * Verifies that {@code normalize()} removes {@code .} and {@code ..} segments.
	 */
	@Test
	public void normalizeRemovesDotSegments()
	{
		PathSanitizer sanitizer = new PathSanitizer();
		Path path = Path.of("/foo/./bar/../baz");
		Path normalized = sanitizer.normalize(path);

		assertEquals(normalized.toString(), Path.of("/foo/baz").toString());
	}

	/**
	 * Verifies that path traversal attacks using {@code ../} sequences are blocked.
	 *
	 * @throws IOException if an I/O error occurs during test setup or cleanup
	 */
	@Test(expectedExceptions = PathTraversalException.class)
	public void pathTraversalAttackIsBlocked() throws IOException
	{
		PathSanitizer sanitizer = new PathSanitizer();
		Path allowedRoot = Files.createTempDirectory("path-sanitizer-test");

		try
		{
			Path maliciousPath = allowedRoot.resolve("../../../etc/passwd");
			sanitizer.sanitize(maliciousPath, allowedRoot);
		}
		finally
		{
			cleanupDirectory(allowedRoot);
		}
	}

	/**
	 * Verifies that paths within a subdirectory of the allowed root are accepted.
	 *
	 * @throws IOException if an I/O error occurs during test setup or cleanup
	 */
	@Test
	public void validSubdirectoryPathIsAllowed() throws IOException
	{
		PathSanitizer sanitizer = new PathSanitizer();
		Path allowedRoot = Files.createTempDirectory("path-sanitizer-test");

		try
		{
			Path subdir = Files.createDirectory(allowedRoot.resolve("subdir"));
			Path validPath = subdir.resolve("file.java");
			Files.createFile(validPath);

			Path sanitized = sanitizer.sanitize(validPath, allowedRoot);
			assertTrue(sanitized.startsWith(allowedRoot.toRealPath()));
		}
		finally
		{
			cleanupDirectory(allowedRoot);
		}
	}

	/**
	 * Verifies that paths directly within the allowed root directory are accepted.
	 *
	 * @throws IOException if an I/O error occurs during test setup or cleanup
	 */
	@Test
	public void rootDirectoryPathIsAllowed() throws IOException
	{
		PathSanitizer sanitizer = new PathSanitizer();
		Path allowedRoot = Files.createTempDirectory("path-sanitizer-test");

		try
		{
			Path rootPath = allowedRoot.resolve("file.java");
			Files.createFile(rootPath);

			Path sanitized = sanitizer.sanitize(rootPath, allowedRoot);
			assertEquals(sanitized.getParent(), allowedRoot.toRealPath());
		}
		finally
		{
			cleanupDirectory(allowedRoot);
		}
	}

	/**
	 * Verifies that a null path throws {@link NullPointerException}.
	 *
	 * @throws IOException if an I/O error occurs during test setup or cleanup
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullPathThrowsException() throws IOException
	{
		PathSanitizer sanitizer = new PathSanitizer();
		Path allowedRoot = Files.createTempDirectory("path-sanitizer-test");

		try
		{
			sanitizer.sanitize(null, allowedRoot);
		}
		finally
		{
			cleanupDirectory(allowedRoot);
		}
	}

	/**
	 * Verifies that a null allowed root throws {@link NullPointerException}.
	 *
	 * @throws IOException if an I/O error occurs during test setup or cleanup
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullRootThrowsException() throws IOException
	{
		PathSanitizer sanitizer = new PathSanitizer();
		Path allowedRoot = Files.createTempDirectory("path-sanitizer-test");

		try
		{
			Path path = allowedRoot.resolve("file.java");
			sanitizer.sanitize(path, null);
		}
		finally
		{
			cleanupDirectory(allowedRoot);
		}
	}

	/**
	 * Verifies that symbolic links pointing outside the allowed root are detected.
	 *
	 * @throws IOException          if an I/O error occurs during test setup or cleanup
	 * @throws InterruptedException if the thread is interrupted while creating a directory junction
	 */
	@Test
	public void symbolicLinkTraversalIsDetected() throws IOException, InterruptedException
	{
		PathSanitizer sanitizer = new PathSanitizer();
		Path allowedRoot = Files.createTempDirectory("path-sanitizer-test");
		Path outside = Files.createTempDirectory("outside");

		try
		{
			Path link = allowedRoot.resolve("link");
			if (!createLink(link, outside))
				throw new SkipException("Cannot create symbolic links or directory junctions on this system");

			Path targetFile = outside.resolve("target.txt");
			Files.createFile(targetFile);

			Path maliciousPath = link.resolve("target.txt");

			try
			{
				sanitizer.sanitize(maliciousPath, allowedRoot);
				fail("Expected PathTraversalException");
			}
			catch (PathTraversalException e)
			{
				// Expected
			}
		}
		finally
		{
			cleanupDirectory(allowedRoot);
			cleanupDirectory(outside);
		}
	}

	/**
	 * Creates a link from source to target directory.
	 * Tries symbolic link first, falls back to directory junction on Windows.
	 *
	 * @param link   the link path to create
	 * @param target the target directory
	 * @return true if link was created, false if not possible on this system
	 */
	private boolean createLink(Path link, Path target) throws IOException, InterruptedException
	{
		try
		{
			Files.createSymbolicLink(link, target);
			return true;
		}
		catch (FileSystemException e)
		{
			// Windows requires elevated privileges for symbolic links
			// Fall back to directory junction which doesn't require elevation
			if (e.getMessage() == null || !e.getMessage().contains("privilege"))
				throw e;
		}

		// Try directory junction on Windows (no elevation required)
		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
		if (!isWindows)
			return false;

		ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "mklink", "/J",
			link.toString(), target.toString());
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		int exitCode = process.waitFor();
		return exitCode == 0 && Files.exists(link);
	}

	private void cleanupDirectory(Path directory) throws IOException
	{
		if (directory != null && Files.exists(directory))
			Files.walk(directory)
				.sorted((a, b) -> -a.compareTo(b))
				.forEach(path -> {
					try
					{
						Files.deleteIfExists(path);
					}
					catch (IOException e)
					{
						// Ignore cleanup errors
					}
				});
	}
}
