package io.github.cowwoc.styler.security.test;

import io.github.cowwoc.styler.security.*;

import io.github.cowwoc.styler.security.exceptions.PathTraversalException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.*;

/**
 * Tests for PathSanitizer path traversal protection.
 * Thread-safe: each test creates its own temp directories.
 */
public class PathSanitizerTest
{
	@Test
	public void normalizeRemovesDotSegments()
	{
		PathSanitizer sanitizer = new PathSanitizer();
		Path path = Path.of("/foo/./bar/../baz");
		Path normalized = sanitizer.normalize(path);

		assertEquals(normalized.toString(), Path.of("/foo/baz").toString());
	}

	@Test(expectedExceptions = PathTraversalException.class)
	public void pathTraversalAttackIsBlocked() throws Exception
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

	@Test
	public void validSubdirectoryPathIsAllowed() throws Exception
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

	@Test
	public void rootDirectoryPathIsAllowed() throws Exception
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

	@Test(expectedExceptions = NullPointerException.class)
	public void nullPathThrowsException() throws Exception
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

	@Test(expectedExceptions = NullPointerException.class)
	public void nullRootThrowsException() throws Exception
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

	@Test
	public void symbolicLinkTraversalIsDetected() throws Exception
	{
		PathSanitizer sanitizer = new PathSanitizer();
		Path allowedRoot = Files.createTempDirectory("path-sanitizer-test");
		Path outside = Files.createTempDirectory("outside");

		try
		{
			Path link = allowedRoot.resolve("link");
			Files.createSymbolicLink(link, outside);

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

	private void cleanupDirectory(Path directory) throws IOException
	{
		if (directory != null && Files.exists(directory))
		{
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
}
