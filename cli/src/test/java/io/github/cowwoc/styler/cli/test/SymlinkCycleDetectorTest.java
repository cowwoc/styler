package io.github.cowwoc.styler.cli.test;
import io.github.cowwoc.styler.cli.SymlinkCycleDetector;


import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Unit tests for SymlinkCycleDetector.
 */
public class SymlinkCycleDetectorTest
{
	/**
	 * Verifies that constructor validates positive max depth.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void constructorRejectsNonPositiveDepth()
	{
		new SymlinkCycleDetector(0);
	}

	/**
	 * Verifies that non-symlink files are not considered cycles.
	 */
	@Test
	public void nonSymlinkIsNotCycle() throws IOException
	{
		SymlinkCycleDetector detector = new SymlinkCycleDetector(10);
		Path regularFile = Files.createTempFile("test", ".txt");

		try
		{
			requireThat(detector.isCycle(regularFile), "isCycle").isFalse();
		}
		finally
		{
			Files.deleteIfExists(regularFile);
		}
	}

	/**
	 * Verifies that reset clears visited paths.
	 */
	@Test
	public void resetClearsVisitedPaths() throws IOException
	{
		SymlinkCycleDetector detector = new SymlinkCycleDetector(10);
		Path file1 = Files.createTempFile("test1", ".txt");
		Path file2 = Files.createTempFile("test2", ".txt");

		try
		{
			detector.isCycle(file1);
			detector.reset();

			requireThat(detector.getCurrentSymlinkDepth(), "symlinkDepth").isEqualTo(0);
		}
		finally
		{
			Files.deleteIfExists(file1);
			Files.deleteIfExists(file2);
		}
	}

	/**
	 * Verifies that clear removes thread-local state.
	 */
	@Test
	public void clearRemovesThreadLocalState() throws IOException
	{
		SymlinkCycleDetector detector = new SymlinkCycleDetector(10);
		Path file = Files.createTempFile("test", ".txt");

		try
		{
			detector.isCycle(file);
			detector.clear();

			// After clear, new context should be created
			requireThat(detector.getCurrentSymlinkDepth(), "symlinkDepth").isEqualTo(0);
		}
		finally
		{
			Files.deleteIfExists(file);
		}
	}

	/**
	 * Verifies that symlink depth tracking works correctly.
	 */
	@Test
	public void symlinkDepthTracking() throws IOException
	{
		if (!supportsSymlinks())
		{
			// Skip test if symlinks not supported
			return;
		}

		SymlinkCycleDetector detector = new SymlinkCycleDetector(50);
		Path targetDir = Files.createTempDirectory("target");
		Path targetFile = Files.createFile(targetDir.resolve("target.txt"));
		Path link1 = targetDir.resolve("link1");

		try
		{
			Files.createSymbolicLink(link1, targetFile);

			detector.isCycle(link1);
			requireThat(detector.getCurrentSymlinkDepth(), "symlinkDepth").isGreaterThan(0);

			detector.reset();
			requireThat(detector.getCurrentSymlinkDepth(), "symlinkDepthAfterReset").isEqualTo(0);
		}
		finally
		{
			Files.deleteIfExists(link1);
			Files.deleteIfExists(targetFile);
			Files.deleteIfExists(targetDir);
		}
	}

	/**
	 * Verifies that same symlink visited twice is tracked correctly.
	 */
	@Test
	public void tracksRepeatedSymlinkVisits() throws IOException
	{
		if (!supportsSymlinks())
		{
			// Skip test if symlinks not supported
			return;
		}

		SymlinkCycleDetector detector = new SymlinkCycleDetector(50);
		Path targetDir = Files.createTempDirectory("target");
		Path targetFile = Files.createFile(targetDir.resolve("target.txt"));
		Path link1 = targetDir.resolve("link1");

		try
		{
			Files.createSymbolicLink(link1, targetFile);

			// First visit: not a cycle (symlink is new)
			boolean firstVisit = detector.isCycle(link1);

			// Second visit: should be tracked (same canonical path visited again)
			boolean secondVisit = detector.isCycle(link1);

			requireThat(firstVisit, "firstVisit").isFalse();
			requireThat(secondVisit, "secondVisit").isTrue();
		}
		finally
		{
			Files.deleteIfExists(link1);
			Files.deleteIfExists(targetFile);
			Files.deleteIfExists(targetDir);
		}
	}

	/**
	 * Checks if the file system supports symbolic links.
	 *
	 * @return {@code true} if symlinks are supported
	 */
	private static boolean supportsSymlinks()
	{
		try
		{
			Path tempDir = Files.createTempDirectory("symlink-test");
			Path link = tempDir.resolve("test-link");
			Path target = tempDir.resolve("target");
			Files.createFile(target);
			Files.createSymbolicLink(link, target);
			Files.deleteIfExists(link);
			Files.deleteIfExists(target);
			Files.deleteIfExists(tempDir);
			return true;
		}
		catch (IOException | UnsupportedOperationException e)
		{
			return false;
		}
	}
}
