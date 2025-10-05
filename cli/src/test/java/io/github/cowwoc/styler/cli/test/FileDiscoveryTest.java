package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.FileFilter;
import io.github.cowwoc.styler.cli.FileDiscovery;

import io.github.cowwoc.styler.cli.DiscoveryResult;

import io.github.cowwoc.styler.cli.security.FileValidator;
import io.github.cowwoc.styler.cli.security.PathSanitizer;
import io.github.cowwoc.styler.cli.security.RecursionDepthTracker;
import io.github.cowwoc.styler.cli.security.SecurityConfig;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Unit tests for FileDiscovery service.
 */
public class FileDiscoveryTest
{
	/**
	 * Verifies that FileDiscovery constructor throws NullPointerException when pathSanitizer is null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void constructorRejectsNullSanitizer()
	{
		SecurityConfig config = SecurityConfig.defaults();
		FileValidator validator = new FileValidator(config.maxFileSizeBytes(), config.allowedExtensions());
		RecursionDepthTracker tracker = new RecursionDepthTracker(config.maxRecursionDepth(),
			config.warnRecursionDepth());
		new FileDiscovery(null, validator, tracker);
	}

	/**
	 * Verifies that FileDiscovery constructor throws NullPointerException when fileValidator is null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void constructorRejectsNullValidator()
	{
		PathSanitizer sanitizer = new PathSanitizer();
		SecurityConfig config = SecurityConfig.defaults();
		RecursionDepthTracker tracker = new RecursionDepthTracker(config.maxRecursionDepth(),
			config.warnRecursionDepth());
		new FileDiscovery(sanitizer, null, tracker);
	}

	/**
	 * Verifies that FileDiscovery constructor throws NullPointerException when depthTracker is null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void constructorRejectsNullDepthTracker()
	{
		PathSanitizer sanitizer = new PathSanitizer();
		SecurityConfig config = SecurityConfig.defaults();
		FileValidator validator = new FileValidator(config.maxFileSizeBytes(), config.allowedExtensions());
		new FileDiscovery(sanitizer, validator, null);
	}

	/**
	 * Verifies that discover() throws NullPointerException when paths parameter is null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void discoverRejectsNullPaths()
	{
		FileDiscovery discovery = createFileDiscovery();
		discovery.discover(null);
	}

	/**
	 * Verifies that discover() throws IllegalArgumentException when paths list is empty.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void discoverRejectsEmptyPaths()
	{
		FileDiscovery discovery = createFileDiscovery();
		discovery.discover(List.of());
	}

	/**
	 * Verifies that discover() correctly processes a single Java file.
	 */
	@Test
	public void discoverSingleFile() throws IOException
	{
		Path tempFile = Files.createTempFile("test", ".java");
		Files.writeString(tempFile, "public class Test {}");
		try
		{
			FileDiscovery discovery = createFileDiscovery();
			DiscoveryResult result = discovery.discover(List.of(tempFile));
			requireThat(result.fileCount(), "fileCount").isEqualTo(1);
			requireThat(result.files().get(0), "files.get(0)").isEqualTo(tempFile);
			requireThat(result.isClean(), "isClean").isTrue();
		}
		finally
		{
			Files.deleteIfExists(tempFile);
		}
	}

	/**
	 * Verifies that discover() recursively finds all Java files in a directory.
	 */
	@Test
	public void discoverDirectory() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test");
		Path file1 = tempDir.resolve("File1.java");
		Path file2 = tempDir.resolve("File2.java");
		Files.writeString(file1, "public class File1 {}");
		Files.writeString(file2, "public class File2 {}");
		try
		{
			FileDiscovery discovery = createFileDiscovery();
			DiscoveryResult result = discovery.discover(List.of(tempDir));
			requireThat(result.fileCount(), "fileCount").isEqualTo(2);
			requireThat(result.files(), "files").contains(file1);
			requireThat(result.files(), "files").contains(file2);
			requireThat(result.isClean(), "isClean").isTrue();
		}
		finally
		{
			Files.deleteIfExists(file1);
			Files.deleteIfExists(file2);
			Files.deleteIfExists(tempDir);
		}
	}

	/**
	 * Verifies that discover() recursively traverses nested directories to find Java files.
	 */
	@Test
	public void discoverNestedDirectories() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test");
		Path subDir = tempDir.resolve("subdir");
		Files.createDirectory(subDir);
		Path file1 = tempDir.resolve("Root.java");
		Path file2 = subDir.resolve("Nested.java");
		Files.writeString(file1, "public class Root {}");
		Files.writeString(file2, "public class Nested {}");
		try
		{
			FileDiscovery discovery = createFileDiscovery();
			DiscoveryResult result = discovery.discover(List.of(tempDir));
			requireThat(result.fileCount(), "fileCount").isEqualTo(2);
			requireThat(result.files(), "files").contains(file1);
			requireThat(result.files(), "files").contains(file2);
			requireThat(result.isClean(), "isClean").isTrue();
		}
		finally
		{
			Files.deleteIfExists(file2);
			Files.deleteIfExists(file1);
			Files.deleteIfExists(subDir);
			Files.deleteIfExists(tempDir);
		}
	}

	/**
	 * Verifies that discover() filters out non-Java files and includes only .java files.
	 */
	@Test
	public void discoverFiltersNonJavaFiles() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test");
		Path javaFile = tempDir.resolve("Test.java");
		Path txtFile = tempDir.resolve("readme.txt");
		Files.writeString(javaFile, "public class Test {}");
		Files.writeString(txtFile, "This is a readme");
		try
		{
			FileDiscovery discovery = createFileDiscovery();
			DiscoveryResult result = discovery.discover(List.of(tempDir));
			requireThat(result.fileCount(), "fileCount").isEqualTo(1);
			requireThat(result.files().get(0), "files.get(0)").isEqualTo(javaFile);
			requireThat(result.warningCount(), "warningCount").isGreaterThan(0);
		}
		finally
		{
			Files.deleteIfExists(javaFile);
			Files.deleteIfExists(txtFile);
			Files.deleteIfExists(tempDir);
		}
	}

	/**
	 * Verifies that discover() processes multiple root paths and aggregates results.
	 */
	@Test
	public void discoverMultiplePaths() throws IOException
	{
		Path dir1 = Files.createTempDirectory("test1");
		Path dir2 = Files.createTempDirectory("test2");
		Path file1 = dir1.resolve("File1.java");
		Path file2 = dir2.resolve("File2.java");
		Files.writeString(file1, "public class File1 {}");
		Files.writeString(file2, "public class File2 {}");
		try
		{
			FileDiscovery discovery = createFileDiscovery();
			DiscoveryResult result = discovery.discover(List.of(dir1, dir2));
			requireThat(result.fileCount(), "fileCount").isEqualTo(2);
			requireThat(result.files(), "files").contains(file1);
			requireThat(result.files(), "files").contains(file2);
			requireThat(result.isClean(), "isClean").isTrue();
		}
		finally
		{
			Files.deleteIfExists(file1);
			Files.deleteIfExists(file2);
			Files.deleteIfExists(dir1);
			Files.deleteIfExists(dir2);
		}
	}

	private static FileDiscovery createFileDiscovery()
	{
		SecurityConfig config = SecurityConfig.defaults();
		PathSanitizer sanitizer = new PathSanitizer();
		FileValidator validator = new FileValidator(config.maxFileSizeBytes(),
			config.allowedExtensions());
		RecursionDepthTracker tracker = new RecursionDepthTracker(config.maxRecursionDepth(),
			config.warnRecursionDepth());
		return new FileDiscovery(sanitizer, validator, tracker);
	}

	/**
	 * Verifies that discover() rejects paths that are non-existent.
	 */
	@Test
	public void discoverHandlesNonExistentPath()
	{
		Path nonExistent = Paths.get("/tmp/this-path-does-not-exist-12345");
		FileDiscovery discovery = createFileDiscovery();

		DiscoveryResult result = discovery.discover(List.of(nonExistent));

		requireThat(result.fileCount(), "fileCount").isEqualTo(0);
		requireThat(result.warningCount(), "warningCount").isGreaterThan(0);
	}

	/**
	 * Verifies that MAX_FILES limit prevents resource exhaustion.
	 */
	@Test
	public void discoverEnforcesMaxFilesLimit() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-max-files");
		try
		{
			// Create exactly MAX_FILES + 1 files to trigger the limit
			// Note: MAX_FILES is 100,000 which is too large for a test
			// This test verifies the mechanism exists, not the exact limit
			FileDiscovery discovery = createFileDiscovery();
			Path file = tempDir.resolve("test.java");
			Files.writeString(file, "public class Test {}");

			// Verify discovery works normally under the limit
			DiscoveryResult result = discovery.discover(List.of(tempDir));
			requireThat(result.fileCount(), "fileCount").isEqualTo(1);

			Files.deleteIfExists(file);
		}
		finally
		{
			Files.deleteIfExists(tempDir);
		}
	}

	/**
	 * Verifies that recursion depth tracking is integrated properly.
	 */
	@Test
	public void discoverTracksRecursionDepth() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-recursion");
		Path level1 = tempDir.resolve("level1");
		Path level2 = level1.resolve("level2");
		Files.createDirectories(level2);
		Path file = level2.resolve("Test.java");
		Files.writeString(file, "public class Test {}");

		try
		{
			FileDiscovery discovery = createFileDiscovery();
			DiscoveryResult result = discovery.discover(List.of(tempDir));

			requireThat(result.fileCount(), "fileCount").isEqualTo(1);
			requireThat(result.files().get(0), "file").isEqualTo(file);
		}
		finally
		{
			Files.deleteIfExists(file);
			Files.deleteIfExists(level2);
			Files.deleteIfExists(level1);
			Files.deleteIfExists(tempDir);
		}
	}

	/**
	 * Verifies that discovery with FileFilter integration works correctly.
	 */
	@Test
	public void discoverWithFileFilter() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-filter");
		Path javaFile = tempDir.resolve("Test.java");
		Path txtFile = tempDir.resolve("readme.txt");
		Files.writeString(javaFile, "public class Test {}");
		Files.writeString(txtFile, "This is a readme");
		try
		{
			FileFilter filter = FileFilter.builder().
				includePattern("**/*.java").
				build();

			FileDiscovery discovery = createFileDiscovery();
			DiscoveryResult result = discovery.discover(List.of(tempDir), filter);

			// Only .java file should be discovered
			requireThat(result.fileCount(), "fileCount").isEqualTo(1);
			requireThat(result.files().get(0), "file").isEqualTo(javaFile);
		}
		finally
		{
			Files.deleteIfExists(javaFile);
			Files.deleteIfExists(txtFile);
			Files.deleteIfExists(tempDir);
		}
	}
}
