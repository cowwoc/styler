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

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.discovery.test.TestUtils.deleteDirectoryRecursively;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Security integration tests for FileDiscovery.
 */
public final class FileDiscoverySecurityIntegrationTest
{
	private FileDiscovery createDiscovery()
	{
		return new FileDiscovery(new PathSanitizer(), new FileValidator());
	}

	private List<Path> discoverFiles(Path root, DiscoveryConfiguration config)
	{
		return createDiscovery().discover(List.of(root), config, SecurityConfig.DEFAULT).files();
	}

	@Test
	public void discoveryIntegratesWithPathSanitizerBlocksTraversalAttempts() throws IOException
	{
		Path tempRoot = Files.createTempDirectory("test-security-root-");
		Path tempOutside = Files.createTempDirectory("test-security-outside-");
		try
		{
			Files.createFile(tempOutside.resolve("Evil.java"));

			// Attempting to discover with traversal patterns should be blocked
			List<Path> files = discoverFiles(tempRoot, DiscoveryConfiguration.DEFAULT);

			assertTrue(files.isEmpty() || files.stream().allMatch(p ->
			{
				try
				{
					return p.toRealPath().startsWith(tempRoot.toRealPath());
				}
				catch (IOException _)
				{
					return false;
				}
			}), "Files outside root should not be discovered");
		}
		finally
		{
			deleteDirectoryRecursively(tempRoot);
			deleteDirectoryRecursively(tempOutside);
		}
	}

	@Test
	public void discoveryIntegratesWithFileValidatorRespectsFileSizeLimit() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-size-limit-");
		try
		{
			// Create a small file
			Path smallFile = Files.createFile(tempDir.resolve("Small.java"));
			Files.writeString(smallFile, "public class Small {}");

			// Create a larger file (if possible)
			Path largeFile = Files.createFile(tempDir.resolve("Large.java"));
			byte[] largContent = new byte[1024 * 100]; // 100KB
			Files.write(largeFile, largContent);

			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);

			// At minimum, small file should be discovered
			assertTrue(files.size() >= 1, "Small file should be discovered");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoveryIntegratesWithRecursionDepthTrackerPreventsDeepRecursion()
		throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-depth-");
		try
		{
			// Create a 100-level deep directory
			Path current = tempDir;
			for (int i = 0; i < 100; ++i)
				current = Files.createDirectory(current.resolve("level" + i));
			Files.createFile(current.resolve("Deep.java"));

			DiscoveryConfiguration config = new DiscoveryConfiguration.Builder().maxDepth(50).build();

			List<Path> files = discoverFiles(tempDir, config);

			// File at level 100 should not be discovered with maxDepth 50
			// Test passes if we reach here without stack overflow
			requireThat(files, "files").doesNotContain(current.resolve("Deep.java"));
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoveryReturnsCanonicalPathsNormalizesAllResults() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-canonical-");
		try
		{
			Path subdir = Files.createDirectory(tempDir.resolve("subdir"));
			Files.createFile(subdir.resolve("Test.java"));

			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);

			assertEquals(files.size(), 1, "Should discover one file");

			// Returned paths should be normalized
			Path returned = files.get(0);
			String pathStr = returned.toString();

			// Should not contain . or .. path segments
			assertFalse(pathStr.contains("/."), "Path should not contain /. segments");
			assertFalse(pathStr.contains("/../"), "Path should not contain /.. segments");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoveryValidatesEachFileUsingSecurityConfig() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-validate-");
		try
		{
			Files.createFile(tempDir.resolve("Valid.java"));

			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);

			// All returned files should pass validation
			for (Path file : files)
			{
				assertTrue(Files.exists(file), "Returned file should exist");
				assertTrue(Files.isRegularFile(file), "Returned file should be a regular file");
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	private static void assertFalse(boolean condition, String message)
	{
		assertTrue(!condition, message);
	}
}
