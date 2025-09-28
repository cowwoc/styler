package io.github.cowwoc.styler.cli.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Utility class for processing files and directories with pattern matching.
 * <p>
 * This class provides functionality for discovering Java source files,
 * applying include/exclude patterns, and traversing directory structures
 * safely and efficiently.
 */
public class FileProcessor
{
	private static final String DEFAULT_JAVA_PATTERN = "**/*.java";

	/**
	 * Discovers Java source files from the given input paths.
	 *
	 * @param inputPaths the paths to scan (files or directories)
	 * @param includePatterns glob patterns for files to include
	 * @param excludePatterns glob patterns for files to exclude
	 * @return list of discovered Java files
	 * @throws IOException if file system access fails
	 */
	public static List<Path> discoverJavaFiles(List<Path> inputPaths,
	                                           List<String> includePatterns,
	                                           List<String> excludePatterns) throws IOException
	{
		List<Path> javaFiles = new ArrayList<>();

		// Use default pattern if no includes specified
		List<String> includes = (includePatterns != null && !includePatterns.isEmpty())
			? includePatterns
			: List.of(DEFAULT_JAVA_PATTERN);

		List<String> excludes = excludePatterns != null ? excludePatterns : List.of();

		// Create path matchers for patterns
		List<PathMatcher> includeMatchers = createPathMatchers(includes);
		List<PathMatcher> excludeMatchers = createPathMatchers(excludes);

		// Process each input path
		for (Path inputPath : inputPaths)
		{
			if (!Files.exists(inputPath))
			{
				throw new IOException("Input path does not exist: " + inputPath);
			}

			if (Files.isRegularFile(inputPath))
			{
				// Single file - check if it matches patterns
				if (matchesPatterns(inputPath, includeMatchers, excludeMatchers))
				{
					javaFiles.add(inputPath);
				}
			}
			else if (Files.isDirectory(inputPath))
			{
				// Directory - walk and collect matching files
				javaFiles.addAll(walkDirectory(inputPath, includeMatchers, excludeMatchers));
			}
		}

		return javaFiles;
	}

	/**
	 * Creates PathMatcher instances for the given glob patterns.
	 *
	 * @param patterns the glob patterns
	 * @return list of path matchers
	 */
	private static List<PathMatcher> createPathMatchers(List<String> patterns)
	{
		FileSystem fileSystem = FileSystems.getDefault();
		return patterns.stream()
			.map(pattern -> fileSystem.getPathMatcher("glob:" + pattern))
			.toList();
	}

	/**
	 * Walks a directory tree and collects files matching the patterns.
	 *
	 * @param directory the directory to walk
	 * @param includeMatchers patterns for files to include
	 * @param excludeMatchers patterns for files to exclude
	 * @return list of matching files
	 * @throws IOException if directory walking fails
	 */
	private static List<Path> walkDirectory(Path directory,
	                                        List<PathMatcher> includeMatchers,
	                                        List<PathMatcher> excludeMatchers) throws IOException
	{
		List<Path> matchingFiles = new ArrayList<>();

		Files.walkFileTree(directory, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			{
				if (matchesPatterns(file, includeMatchers, excludeMatchers))
				{
					matchingFiles.add(file);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
			{
				// Log warning and continue processing
				System.err.println("Warning: Could not access file: " + file + " (" + exc.getMessage() + ")");
				return FileVisitResult.CONTINUE;
			}
		});

		return matchingFiles;
	}

	/**
	 * Checks if a file path matches the include/exclude patterns.
	 *
	 * @param filePath the file path to check
	 * @param includeMatchers patterns for files to include
	 * @param excludeMatchers patterns for files to exclude
	 * @return true if the file should be included
	 */
	private static boolean matchesPatterns(Path filePath,
	                                       List<PathMatcher> includeMatchers,
	                                       List<PathMatcher> excludeMatchers)
	{
		// Check exclude patterns first (they take precedence)
		for (PathMatcher excludeMatcher : excludeMatchers)
		{
			if (excludeMatcher.matches(filePath))
			{
				return false;
			}
		}

		// Check include patterns
		for (PathMatcher includeMatcher : includeMatchers)
		{
			if (includeMatcher.matches(filePath))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Validates that all input paths exist and are accessible.
	 *
	 * @param inputPaths the paths to validate
	 * @throws IOException if any path is invalid or inaccessible
	 */
	public static void validateInputPaths(List<Path> inputPaths) throws IOException
	{
		for (Path path : inputPaths)
		{
			if (!Files.exists(path))
			{
				throw new IOException("Path does not exist: " + path);
			}

			if (!Files.isReadable(path))
			{
				throw new IOException("Path is not readable: " + path);
			}

			// Additional security check for directory traversal
			Path normalized = path.normalize().toAbsolutePath();
			if (!normalized.startsWith(Paths.get("").toAbsolutePath()))
			{
				// Allow paths outside current directory, but warn about potential issues
				System.err.println("Warning: Processing path outside current directory: " + normalized);
			}
		}
	}

	/**
	 * Checks if a file appears to be a Java source file based on content.
	 *
	 * @param filePath the file to check
	 * @return true if the file appears to be Java source
	 * @throws IOException if file reading fails
	 */
	public static boolean isJavaSourceFile(Path filePath) throws IOException
	{
		if (!Files.isRegularFile(filePath))
		{
			return false;
		}

		// Check file extension
		String fileName = filePath.getFileName().toString();
		if (!fileName.endsWith(".java"))
		{
			return false;
		}

		// Basic content validation (check for Java keywords in first few lines)
		try
		{
			List<String> firstLines = Files.readAllLines(filePath).stream()
				.limit(10)
				.toList();

			String content = String.join(" ", firstLines).toLowerCase();

			// Look for common Java keywords/patterns
			return content.contains("package ") ||
			       content.contains("import ") ||
			       content.contains("class ") ||
			       content.contains("interface ") ||
			       content.contains("enum ") ||
			       content.contains("public ") ||
			       content.contains("private ");
		}
		catch (IOException e)
		{
			// If we can't read the file, assume it's not Java
			return false;
		}
	}
}