package io.github.cowwoc.styler.benchmark.test;

import io.github.cowwoc.styler.benchmark.TestDataProvider;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Validates TestDataProvider functionality for benchmark test data management.
 */
public class TestDataProviderTest
{
	/**
	 * Verifies test data provider loads files from project source.
	 */
	@Test
	public void loadTestFilesFromProjectSource() throws IOException
	{
		TestDataProvider provider = new TestDataProvider();
		List<Path> files = provider.loadTestFiles(10);

		assertNotNull(files, "Loaded files should not be null");
		assertFalse(files.isEmpty(), "Should load at least one file");
		assertTrue(files.size() <= 10, "Should not exceed maxFiles limit");

		for (Path file : files)
		{
			assertTrue(Files.exists(file), "File should exist: " + file);
			assertTrue(file.toString().endsWith(".java"), "File should be Java source: " + file);
		}
	}

	/**
	 * Verifies file size categorization logic.
	 */
	@Test
	public void fileSizeCategorization()
	{
		assertEquals(TestDataProvider.FileSize.categorize(500), TestDataProvider.FileSize.SMALL, "500 bytes should be SMALL");
		assertEquals(TestDataProvider.FileSize.categorize(1024), TestDataProvider.FileSize.MEDIUM, "1KB should be MEDIUM");
		assertEquals(TestDataProvider.FileSize.categorize(5000), TestDataProvider.FileSize.MEDIUM, "5KB should be MEDIUM");
		assertEquals(TestDataProvider.FileSize.categorize(15000), TestDataProvider.FileSize.LARGE, "15KB should be LARGE");
	}

	/**
	 * Verifies cache directory creation.
	 */
	@Test
	public void cacheDirectoryCreation() throws IOException
	{
		TestDataProvider provider = new TestDataProvider();
		Path cacheDir = provider.getCacheDirectory();

		assertNotNull(cacheDir, "Cache directory should not be null");
		assertTrue(Files.exists(cacheDir), "Cache directory should exist");
		assertTrue(Files.isDirectory(cacheDir), "Cache should be a directory");
	}

	/**
	 * Verifies cached data detection for non-existent projects.
	 */
	@Test
	public void hasCachedDataReturnsFalseForNonExistentProject() throws IOException
	{
		TestDataProvider provider = new TestDataProvider();
		boolean hasCached = provider.hasCachedData("non-existent-project-xyz");

		assertFalse(hasCached, "Should return false for non-existent project");
	}

	/**
	 * Verifies stratified sampling distributes files across size categories.
	 */
	@Test
	public void stratifiedSamplingDistributesFiles() throws IOException
	{
		TestDataProvider provider = new TestDataProvider();
		List<Path> files = provider.loadTestFiles(30);

		// Verify we get a reasonable distribution (not all from one category)
		assertNotNull(files, "Files should not be null");
		assertTrue(files.size() <= 30, "Should not exceed max files");

		if (files.size() >= 3)
		{
			// If we have enough files, verify they're not all the same size
			long minSize = Long.MAX_VALUE;
			long maxSize = 0;

			for (Path file : files)
			{
				long size = Files.size(file);
				minSize = Math.min(minSize, size);
				maxSize = Math.max(maxSize, size);
			}

			assertTrue(maxSize > minSize, "Should have files of varying sizes");
		}
	}
}
