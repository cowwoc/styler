package io.github.cowwoc.styler.discovery.test;

import io.github.cowwoc.styler.discovery.DiscoveryConfiguration;
import io.github.cowwoc.styler.discovery.FileDiscovery;
import io.github.cowwoc.styler.discovery.FileDiscoveryResult;
import io.github.cowwoc.styler.security.FileValidator;
import io.github.cowwoc.styler.security.PathSanitizer;
import io.github.cowwoc.styler.security.SecurityConfig;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.github.cowwoc.styler.discovery.test.TestUtils.deleteDirectoryRecursively;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for FileDiscovery class.
 */
public final class FileDiscoveryTest
{
	private FileDiscovery createDiscovery()
	{
		return new FileDiscovery(new PathSanitizer(), new FileValidator());
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void discoverNullPathsThrowsNullPointerException()
	{
		FileDiscovery discovery = createDiscovery();
		discovery.discover(null, DiscoveryConfiguration.DEFAULT, SecurityConfig.DEFAULT);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void discoverNullConfigThrowsNullPointerException() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-");
		try
		{
			FileDiscovery discovery = createDiscovery();
			discovery.discover(List.of(tempDir), null, SecurityConfig.DEFAULT);
		}
		finally
		{
			Files.deleteIfExists(tempDir);
		}
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void discoverNullSecurityConfigThrowsNullPointerException() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-");
		try
		{
			FileDiscovery discovery = createDiscovery();
			discovery.discover(List.of(tempDir), DiscoveryConfiguration.DEFAULT, null);
		}
		finally
		{
			Files.deleteIfExists(tempDir);
		}
	}

	@Test
	public void discoverEmptyDirectoryReturnsEmptyList() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-empty-");
		try
		{
			FileDiscovery discovery = createDiscovery();
			FileDiscoveryResult result = discovery.discover(
				List.of(tempDir), DiscoveryConfiguration.DEFAULT, SecurityConfig.DEFAULT);

			assertNotNull(result.files(), "Result files should not be null");
			assertTrue(result.files().isEmpty(), "Empty directory should return empty list");
		}
		finally
		{
			Files.deleteIfExists(tempDir);
		}
	}

	@Test
	public void discoverNonExistentDirectoryReturnsError()
	{
		FileDiscovery discovery = createDiscovery();
		Path nonExistentPath = Path.of("/nonexistent/path/that/does/not/exist");
		FileDiscoveryResult result = discovery.discover(
			List.of(nonExistentPath), DiscoveryConfiguration.DEFAULT, SecurityConfig.DEFAULT);

		assertTrue(result.files().isEmpty(), "Should have no files for non-existent path");
		assertFalse(result.errors().isEmpty(), "Should have error for non-existent path");
		assertTrue(result.errors().containsKey(nonExistentPath), "Error should be for the non-existent path");
	}

	@Test
	public void discoverSingleJavaFileReturnsSingleFile() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-single-");
		try
		{
			Path javaFile = Files.createFile(tempDir.resolve("Test.java"));
			Files.writeString(javaFile, "public class Test {}");

			FileDiscovery discovery = createDiscovery();
			FileDiscoveryResult result = discovery.discover(
				List.of(tempDir), DiscoveryConfiguration.DEFAULT, SecurityConfig.DEFAULT);

			assertEquals(result.files().size(), 1, "Should discover exactly one file");
			assertTrue(result.files().get(0).getFileName().toString().equals("Test.java"),
				"File should be named Test.java");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoverMultipleJavaFilesReturnsAllFiles() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-multiple-");
		try
		{
			Files.createFile(tempDir.resolve("A.java"));
			Files.createFile(tempDir.resolve("B.java"));
			Files.createFile(tempDir.resolve("C.java"));

			FileDiscovery discovery = createDiscovery();
			FileDiscoveryResult result = discovery.discover(
				List.of(tempDir), DiscoveryConfiguration.DEFAULT, SecurityConfig.DEFAULT);

			assertEquals(result.files().size(), 3, "Should discover all three files");
			assertTrue(result.files().stream().anyMatch(p -> p.getFileName().toString().equals("A.java")));
			assertTrue(result.files().stream().anyMatch(p -> p.getFileName().toString().equals("B.java")));
			assertTrue(result.files().stream().anyMatch(p -> p.getFileName().toString().equals("C.java")));
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoverMixedFileTypesReturnsOnlyJavaFiles() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-mixed-");
		try
		{
			Files.createFile(tempDir.resolve("Test.java"));
			Files.createFile(tempDir.resolve("readme.txt"));
			Files.createFile(tempDir.resolve("config.xml"));
			Files.createFile(tempDir.resolve("Another.java"));

			FileDiscovery discovery = createDiscovery();
			FileDiscoveryResult result = discovery.discover(
				List.of(tempDir), DiscoveryConfiguration.DEFAULT, SecurityConfig.DEFAULT);

			assertEquals(result.files().size(), 2, "Should discover only Java files");
			assertTrue(result.files().stream().allMatch(p -> p.getFileName().toString().endsWith(".java")),
				"All files should be .java files");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoverNestedDirectoriesReturnsAllJavaFilesRecursively() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-nested-");
		try
		{
			Files.createFile(tempDir.resolve("A.java"));

			Path subdir1 = Files.createDirectory(tempDir.resolve("subdir1"));
			Files.createFile(subdir1.resolve("B.java"));

			Path subsubdir = Files.createDirectory(subdir1.resolve("subsubdir"));
			Files.createFile(subsubdir.resolve("C.java"));

			Path subdir2 = Files.createDirectory(tempDir.resolve("subdir2"));
			Files.createFile(subdir2.resolve("D.java"));

			FileDiscovery discovery = createDiscovery();
			FileDiscoveryResult result = discovery.discover(
				List.of(tempDir), DiscoveryConfiguration.DEFAULT, SecurityConfig.DEFAULT);

			assertEquals(result.files().size(), 4, "Should discover all four Java files recursively");
			assertTrue(result.files().stream().anyMatch(p -> p.getFileName().toString().equals("A.java")));
			assertTrue(result.files().stream().anyMatch(p -> p.getFileName().toString().equals("B.java")));
			assertTrue(result.files().stream().anyMatch(p -> p.getFileName().toString().equals("C.java")));
			assertTrue(result.files().stream().anyMatch(p -> p.getFileName().toString().equals("D.java")));
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoverMaxDepthOneReturnsOnlyTopLevelFiles() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-maxdepth-");
		try
		{
			Files.createFile(tempDir.resolve("TopLevel.java"));

			Path subdir = Files.createDirectory(tempDir.resolve("subdir"));
			Files.createFile(subdir.resolve("Nested.java"));

			DiscoveryConfiguration config = new DiscoveryConfiguration.Builder().maxDepth(1).build();

			FileDiscovery discovery = createDiscovery();
			FileDiscoveryResult result = discovery.discover(
				List.of(tempDir), config, SecurityConfig.DEFAULT);

			assertEquals(result.files().size(), 1, "Should discover only top-level file");
			assertTrue(result.files().get(0).getFileName().toString().equals("TopLevel.java"),
				"Should be TopLevel.java only");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}
}
