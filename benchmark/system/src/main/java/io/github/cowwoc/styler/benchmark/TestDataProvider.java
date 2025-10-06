package io.github.cowwoc.styler.benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides test data for performance benchmarks with caching support.
 * Implements stratified file size distribution for realistic performance testing.
 */
public class TestDataProvider
{
	private static final Path CACHE_DIR = Paths.get(System.getProperty("user.home"), ".styler-benchmark", "test-data");

	/**
	 * File size categories for stratified sampling.
	 */
	public enum FileSize
	{
		/**
		 * Small files: less than 1 KB.
		 */
		SMALL(1024),
		/**
		 * Medium files: 1-10 KB.
		 */
		MEDIUM(10 * 1024),
		/**
		 * Large files: greater than 10 KB.
		 */
		LARGE(Long.MAX_VALUE);

		private final long maxBytes;

		FileSize(long maxBytes)
		{
			this.maxBytes = maxBytes;
		}

		/**
		 * Categorizes a file based on its size.
		 *
		 * @param bytes the file size in bytes
		 * @return the file size category
		 */
		public static FileSize categorize(long bytes)
		{
			if (bytes < SMALL.maxBytes)
			{
				return SMALL;
			}
			if (bytes < MEDIUM.maxBytes)
			{
				return MEDIUM;
			}
			return LARGE;
		}
	}

	/**
	 * Loads test files from the project's source code with stratified sampling.
	 *
	 * @param maxFiles maximum number of files to load
	 * @return list of test file paths
	 * @throws IOException if files cannot be accessed
	 */
	public List<Path> loadTestFiles(int maxFiles) throws IOException
	{
		Path projectRoot = findProjectRoot();
		if (projectRoot == null)
		{
			throw new IOException("Project root not found");
		}

		Path srcDir = projectRoot.resolve("src");
		if (!Files.exists(srcDir))
		{
			throw new IOException("Source directory not found: " + srcDir);
		}

		return loadFilesWithStratification(srcDir, maxFiles);
	}

	/**
	 * Loads files with stratified sampling by file size.
	 *
	 * @param directory the directory to search
	 * @param maxFiles maximum number of files to load
	 * @return list of file paths with balanced size distribution
	 * @throws IOException if file access fails
	 */
	private List<Path> loadFilesWithStratification(Path directory, int maxFiles) throws IOException
	{
		List<Path> smallFiles = new ArrayList<>();
		List<Path> mediumFiles = new ArrayList<>();
		List<Path> largeFiles = new ArrayList<>();

		try (Stream<Path> paths = Files.walk(directory))
		{
			paths.filter(Files::isRegularFile)
				.filter(path -> path.toString().endsWith(".java"))
				.forEach(path ->
				{
					try
					{
						long bytes = Files.size(path);
						FileSize category = FileSize.categorize(bytes);
						switch (category)
						{
							case SMALL -> smallFiles.add(path);
							case MEDIUM -> mediumFiles.add(path);
							case LARGE -> largeFiles.add(path);
						}
					}
					catch (IOException e)
					{
						// Skip files that cannot be accessed
					}
				});
		}

		// Distribute files proportionally across categories
		int filesPerCategory = maxFiles / 3;
		List<Path> result = new ArrayList<>();

		addFilesUpToLimit(result, smallFiles, filesPerCategory);
		addFilesUpToLimit(result, mediumFiles, filesPerCategory);
		addFilesUpToLimit(result, largeFiles, filesPerCategory);

		// If we didn't get enough files, add remaining from any category
		if (result.size() < maxFiles)
		{
			int remaining = maxFiles - result.size();
			addFilesUpToLimit(result, smallFiles, remaining);
			addFilesUpToLimit(result, mediumFiles, remaining);
			addFilesUpToLimit(result, largeFiles, remaining);
		}

		if (result.isEmpty())
		{
			throw new IOException("No Java files found in " + directory);
		}

		return result;
	}

	/**
	 * Adds files to the result list up to the specified limit.
	 *
	 * @param result the result list to append to
	 * @param source the source list to take files from
	 * @param limit maximum number of files to add
	 */
	private void addFilesUpToLimit(List<Path> result, List<Path> source, int limit)
	{
		int toAdd = Math.min(limit, source.size());
		for (int i = 0; i < toAdd; ++i)
		{
			Path file = source.get(i);
			if (!result.contains(file))
			{
				result.add(file);
			}
		}
	}

	/**
	 * Finds the project root directory by looking for pom.xml.
	 *
	 * @return the project root path, or null if not found
	 */
	private Path findProjectRoot()
	{
		Path current = Paths.get("").toAbsolutePath();
		while (current != null)
		{
			if (Files.exists(current.resolve("pom.xml")))
			{
				return current;
			}
			current = current.getParent();
		}
		return null;
	}

	/**
	 * Gets the cache directory for external test data.
	 * Creates the directory if it doesn't exist.
	 *
	 * @return the cache directory path
	 * @throws IOException if the directory cannot be created
	 */
	public Path getCacheDirectory() throws IOException
	{
		if (!Files.exists(CACHE_DIR))
		{
			Files.createDirectories(CACHE_DIR);
		}
		return CACHE_DIR;
	}

	/**
	 * Checks if cached test data exists for the specified project.
	 *
	 * @param projectName the project name (e.g., "spring-framework", "guava")
	 * @return true if cached data exists
	 * @throws IOException if cache directory cannot be accessed
	 */
	public boolean hasCachedData(String projectName) throws IOException
	{
		Path projectCache = getCacheDirectory().resolve(projectName);
		return Files.exists(projectCache) && Files.isDirectory(projectCache);
	}

	/**
	 * Loads cached test files for the specified project.
	 *
	 * @param projectName the project name
	 * @param maxFiles maximum number of files to load
	 * @return list of cached file paths
	 * @throws IOException if files cannot be accessed
	 */
	public List<Path> loadCachedFiles(String projectName, int maxFiles) throws IOException
	{
		Path projectCache = getCacheDirectory().resolve(projectName);
		if (!Files.exists(projectCache))
		{
			throw new IOException("Cached data not found for project: " + projectName);
		}

		return loadFilesWithStratification(projectCache, maxFiles);
	}
}
