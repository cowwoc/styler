package io.github.cowwoc.styler.discovery.test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.discovery.test.TestUtils.deleteDirectoryRecursively;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.discovery.DiscoveryConfiguration;
import io.github.cowwoc.styler.discovery.FileDiscovery;
import io.github.cowwoc.styler.security.FileValidator;
import io.github.cowwoc.styler.security.PathSanitizer;
import io.github.cowwoc.styler.security.SecurityConfig;

/**
 * Performance tests for FileDiscovery.
 *
 * These tests validate discovery performance on large directory structures.
 * Mark with @Test(groups = "performance") for optional execution.
 */
public final class FileDiscoveryPerformanceTest
{
	private FileDiscovery createDiscovery()
	{
		return new FileDiscovery(new PathSanitizer(), new FileValidator());
	}

	private List<Path> discoverFiles(Path root, DiscoveryConfiguration config)
	{
		return createDiscovery().discover(List.of(root), config, SecurityConfig.DEFAULT).files();
	}

	@Test(groups = "performance")
	public void performance1000FilesCompletesUnder5Seconds() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-perf-1000-");
		try
		{
			// Create 1000 files distributed across 50 subdirectories
			int filesPerDir = 20;
			for (int i = 0; i < 50; ++i)
			{
				Path subdir = Files.createDirectory(tempDir.resolve("dir" + i));
				for (int j = 0; j < filesPerDir; ++j)
				{
					Files.createFile(subdir.resolve("File" + j + ".java"));
				}
			}

			Instant startTime = Instant.now();
			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);
			Duration elapsed = Duration.between(startTime, Instant.now());

			assertEquals(files.size(), 1000, "Should discover all 1000 files");
			requireThat(elapsed, "elapsed").isLessThan(Duration.ofSeconds(5));
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test(groups = "performance")
	public void performance10000FilesCompletesUnder30Seconds() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-perf-10000-");
		try
		{
			// Create 10000 files distributed across 500 subdirectories
			int filesPerDir = 20;
			for (int i = 0; i < 500; ++i)
			{
				Path subdir = Files.createDirectory(tempDir.resolve("dir" + i));
				for (int j = 0; j < filesPerDir; ++j)
				{
					Files.createFile(subdir.resolve("File" + j + ".java"));
				}
			}

			Instant startTime = Instant.now();
			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);
			Duration elapsed = Duration.between(startTime, Instant.now());

			assertEquals(files.size(), 10000, "Should discover all 10000 files");
			requireThat(elapsed, "elapsed").isLessThan(Duration.ofSeconds(30));
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test(groups = "performance")
	public void performanceWideDirectory1000SubdirectoriesCompletesReasonably()
		throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-perf-wide-");
		try
		{
			// Create 1000 immediate subdirectories with one file each
			for (int i = 0; i < 1000; ++i)
			{
				Path subdir = Files.createDirectory(tempDir.resolve("dir" + i));
				Files.createFile(subdir.resolve("File.java"));
			}

			Instant startTime = Instant.now();
			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);
			Duration elapsed = Duration.between(startTime, Instant.now());

			assertEquals(files.size(), 1000, "Should discover all 1000 files");
			requireThat(elapsed, "elapsed").isLessThan(Duration.ofSeconds(10));
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test(groups = "performance")
	public void performanceDeepDirectory100LevelsCompletesWithoutStackOverflow()
		throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-perf-deep-");
		try
		{
			// Create directory 100 levels deep with one file at each level
			Path current = tempDir;
			for (int i = 0; i < 100; ++i)
			{
				current = Files.createDirectory(current.resolve("level" + i));
				Files.createFile(current.resolve("File" + i + ".java"));
			}

			Instant startTime = Instant.now();
			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);
			Duration elapsed = Duration.between(startTime, Instant.now());

			// All files should be discoverable up to maxDepth (default 100)
			requireThat(files.size(), "files.size()").isGreaterThanOrEqualTo(1);
			requireThat(elapsed, "elapsed").isLessThan(Duration.ofSeconds(10));
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test(groups = "performance")
	public void performanceLargeGitignore1000PatternsNoSignificantSlowdown()
		throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-perf-gitignore-");
		try
		{
			// Create .gitignore with 1000 patterns
			StringBuilder gitignoreContent = new StringBuilder();
			for (int i = 0; i < 1000; ++i)
			{
				gitignoreContent.append("*.pattern").append(i).append("\n");
			}
			Path gitignorePath = Files.createFile(tempDir.resolve(".gitignore"));
			Files.writeString(gitignorePath, gitignoreContent.toString());

			// Create 100 Java files
			for (int i = 0; i < 100; ++i)
			{
				Files.createFile(tempDir.resolve("File" + i + ".java"));
			}

			Instant startTime = Instant.now();
			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);
			Duration elapsed = Duration.between(startTime, Instant.now());

			assertEquals(files.size(), 100, "Should discover all 100 Java files");
			requireThat(elapsed, "elapsed").isLessThan(Duration.ofSeconds(5));
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}
}
