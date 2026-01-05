package io.github.cowwoc.styler.discovery;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import io.github.cowwoc.styler.discovery.internal.GlobPatternMatcher;
import io.github.cowwoc.styler.security.FileValidator;
import io.github.cowwoc.styler.security.PathSanitizer;
import io.github.cowwoc.styler.security.SecurityConfig;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Discovers Java source files in directories with filtering support.
 * <p>
 * Supports glob pattern filtering, .gitignore rules, and security validation. File discovery is
 * recursive with configurable depth limits and symlink policies.
 *
 * <b>Thread-safety</b>: This class is thread-safe. Each discovery operation is independent.
 */
public final class FileDiscovery
{
	private static final String JAVA_EXTENSION = ".java";
	private static final String GITIGNORE_FILENAME = ".gitignore";

	private final PathSanitizer pathSanitizer;
	private final FileValidator fileValidator;
	private final GitignoreParser gitignoreParser;

	/**
	 * Creates a FileDiscovery instance.
	 *
	 * @param pathSanitizer for path security validation
	 * @param fileValidator for file security validation
	 * @throws NullPointerException if any argument is {@code null}
	 */
	public FileDiscovery(PathSanitizer pathSanitizer, FileValidator fileValidator)
	{
		requireThat(pathSanitizer, "pathSanitizer").isNotNull();
		requireThat(fileValidator, "fileValidator").isNotNull();

		this.pathSanitizer = pathSanitizer;
		this.fileValidator = fileValidator;
		this.gitignoreParser = new GitignoreParser();
	}

	/**
	 * Discovers all Java files in the given paths.
	 * <p>
	 * For files: validates and returns if valid Java file. For directories: recursively walks,
	 * applies filters, returns matching files.
	 *
	 * @param paths input paths (files or directories)
	 * @param config discovery configuration
	 * @param securityConfig security limits
	 * @return discovery result with files and any errors
	 * @throws NullPointerException if any argument is {@code null}
	 */
	public FileDiscoveryResult discover(
		List<Path> paths,
		DiscoveryConfiguration config,
		SecurityConfig securityConfig)
	{
		requireThat(paths, "paths").isNotNull();
		requireThat(config, "config").isNotNull();
		requireThat(securityConfig, "securityConfig").isNotNull();

		List<Path> discoveredFiles = new ArrayList<>();
		Map<Path, String> errors = new HashMap<>();
		DiscoveryContext context = new DiscoveryContext(config, securityConfig);

		for (Path path : paths)
		{
			try
			{
				if (!Files.exists(path))
				{
					errors.put(path, "Path does not exist");
					continue;
				}

				if (Files.isDirectory(path))
					discoverDirectory(path, discoveredFiles, errors, context);
				else if (Files.isRegularFile(path))
				{
					try
					{
						fileValidator.validate(path, securityConfig);
						if (path.toString().endsWith(JAVA_EXTENSION))
						{
							discoveredFiles.add(path);
							++context.filesScanned;
						}
					}
					catch (Exception e)
					{
						errors.put(path, e.getMessage());
					}
				}
				else
				{
					// Dangling symlinks, device files, named pipes, sockets
					errors.put(path, "Unsupported file type (not a regular file or directory)");
				}
			}
			catch (Exception e)
			{
				errors.put(path, e.getMessage());
			}
		}

		// Sort discovered files
		Collections.sort(discoveredFiles);

		return new FileDiscoveryResult(
			discoveredFiles,
			errors,
			context.filesScanned,
			context.directoriesScanned);
	}

	/**
	 * Recursively discovers files in a directory.
	 *
	 * @param directory the directory to traverse
	 * @param discoveredFiles list to accumulate discovered files
	 * @param errors map to accumulate errors
	 * @param context discovery context
	 */
	private void discoverDirectory(
		Path directory,
		List<Path> discoveredFiles,
		Map<Path, String> errors,
		DiscoveryContext context)
	{
		try
		{
			// Validate directory path
			pathSanitizer.sanitize(directory, directory);

			EnumSet<FileVisitOption> options = EnumSet.noneOf(FileVisitOption.class);
			if (context.config.followSymlinks())
				options.add(FileVisitOption.FOLLOW_LINKS);

			int maxDepth = context.config.maxDepth();
			FileVisitor visitor = new FileVisitor(context, discoveredFiles, errors, directory);
			Files.walkFileTree(directory, options, maxDepth, visitor);
		}
		catch (UncheckedIOException e)
		{
			// Handle permission denied errors gracefully
			Throwable cause = e.getCause();
			if (cause instanceof AccessDeniedException)
				errors.put(directory, "Access denied: " + cause.getMessage());
			else
				errors.put(directory, "Failed to traverse directory: " + e.getMessage());
		}
		catch (Exception e)
		{
			errors.put(directory, "Failed to traverse directory: " + e.getMessage());
		}
	}

	/**
	 * File visitor implementation for recursive directory traversal.
	 */
	private final class FileVisitor extends SimpleFileVisitor<Path>
	{
		private final DiscoveryContext context;
		private final List<Path> discoveredFiles;
		private final Map<Path, String> errors;
		private final Path rootDirectory;
		private final Map<Path, List<GitignoreRule>> gitignoreCache = new HashMap<>();
		private int currentDepth = 0;

		/**
		 * Creates the file visitor.
		 *
		 * @param context discovery context
		 * @param discoveredFiles list to accumulate discovered files
		 * @param errors map to accumulate errors
		 * @param rootDirectory root directory being traversed
		 */
		FileVisitor(
			DiscoveryContext context,
			List<Path> discoveredFiles,
			Map<Path, String> errors,
			Path rootDirectory)
		{
			this.context = context;
			this.discoveredFiles = discoveredFiles;
			this.errors = errors;
			this.rootDirectory = rootDirectory;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
		{
			try
			{
				// Check depth limit
				if (currentDepth >= context.config.maxDepth())
					return FileVisitResult.SKIP_SUBTREE;

				++currentDepth;
				++context.directoriesScanned;

				// Load .gitignore for this directory if needed
				if (context.config.respectGitignore())
				{
					Path gitignorePath = dir.resolve(GITIGNORE_FILENAME);
					if (Files.exists(gitignorePath))
					{
						try
						{
							List<GitignoreRule> rules = gitignoreParser.parse(gitignorePath);
							gitignoreCache.put(dir, rules);
						}
						catch (IOException e)
						{
							errors.put(gitignorePath, "Failed to parse .gitignore: " + e.getMessage());
						}
					}
				}

				return FileVisitResult.CONTINUE;
			}
			catch (Exception e)
			{
				errors.put(dir, "Error processing directory: " + e.getMessage());
				return FileVisitResult.SKIP_SUBTREE;
			}
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		{
			try
			{
				++context.filesScanned;

				// Check if file matches .java extension
				if (!file.toString().endsWith(JAVA_EXTENSION))
					return FileVisitResult.CONTINUE;

				// Check gitignore rules
				if (context.config.respectGitignore())
				{
					Path relativePath = rootDirectory.relativize(file);
					if (isIgnoredByGitignore(file, relativePath))
						return FileVisitResult.CONTINUE;
				}

				// Check include patterns
				if (!context.config.includePatterns().isEmpty())
				{
					Path relativePath = rootDirectory.relativize(file);
					if (!matchesAnyPattern(relativePath, context.config.includePatterns()))
						return FileVisitResult.CONTINUE;
				}

				// Check exclude patterns
				if (!context.config.excludePatterns().isEmpty())
				{
					Path relativePath = rootDirectory.relativize(file);
					if (matchesAnyPattern(relativePath, context.config.excludePatterns()))
						return FileVisitResult.CONTINUE;
				}

				// Validate file with security validator
				try
				{
					fileValidator.validate(file, context.securityConfig);
					discoveredFiles.add(file);
				}
				catch (Exception e)
				{
					errors.put(file, e.getMessage());
				}

				return FileVisitResult.CONTINUE;
			}
			catch (Exception e)
			{
				errors.put(file, "Error processing file: " + e.getMessage());
				return FileVisitResult.CONTINUE;
			}
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc)
		{
			--currentDepth;
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc)
		{
			errors.put(file, "Failed to visit file: " + exc.getMessage());
			return FileVisitResult.CONTINUE;
		}

		/**
		 * Tests if a path is ignored by .gitignore rules in parent directories.
		 *
		 * @param file the file path (absolute)
		 * @param relativePath the path relative to root directory
		 * @return true if the file should be ignored
		 */
		private boolean isIgnoredByGitignore(Path file, Path relativePath)
		{
			Path current = file.getParent();
			while (current != null && !current.equals(rootDirectory.getParent()))
			{
				List<GitignoreRule> rules = gitignoreCache.get(current);
				if (rules != null)
				{
					Path relativeToGitignore = current.relativize(file);
					if (gitignoreParser.isIgnored(relativeToGitignore, rules))
						return true;
				}
				current = current.getParent();
			}
			return false;
		}

		/**
		 * Tests if a path matches any pattern in the list.
		 *
		 * @param path the path to test
		 * @param patterns list of glob patterns
		 * @return true if path matches any pattern
		 */
		private boolean matchesAnyPattern(Path path, List<String> patterns)
		{
			for (String pattern : patterns)
			{
				try
				{
					PatternMatcher matcher = new GlobPatternMatcher(pattern);
					if (matcher.matches(path))
						return true;
				}
				catch (IllegalArgumentException e)
				{
					// Invalid pattern, skip it
				}
			}
			return false;
		}
	}

	/**
	 * Context for a discovery operation.
	 */
	private static final class DiscoveryContext
	{
		final DiscoveryConfiguration config;
		final SecurityConfig securityConfig;
		int filesScanned;
		int directoriesScanned;

		DiscoveryContext(DiscoveryConfiguration config, SecurityConfig securityConfig)
		{
			this.config = config;
			this.securityConfig = securityConfig;
			this.filesScanned = 0;
			this.directoriesScanned = 0;
		}
	}
}
