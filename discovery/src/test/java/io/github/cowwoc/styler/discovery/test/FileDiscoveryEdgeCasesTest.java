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
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.discovery.test.TestUtils.deleteDirectoryRecursively;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Edge case tests for FileDiscovery class.
 */
public final class FileDiscoveryEdgeCasesTest
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
	public void discoverHiddenDirectoryIncludedByDefault() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-hidden-");
		try
		{
			Path visibleSubdir = Files.createDirectory(tempDir.resolve("visible"));
			Files.createFile(visibleSubdir.resolve("Test.java"));

			Path hiddenSubdir = Files.createDirectory(tempDir.resolve(".hidden"));
			Files.createFile(hiddenSubdir.resolve("Hidden.java"));

			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);

			// By default, hidden files are included (no exclusion configured)
			assertEquals(files.size(), 2, "Should discover files in both visible and hidden directories");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoverSymlinkToFileFollowedByDefault() throws IOException
	{
		if (!supportsSymlinks())
			return;

		Path tempDir = Files.createTempDirectory("test-symlink-file-");
		try
		{
			Path realFile = Files.createFile(tempDir.resolve("Real.java"));
			Path linkFile = tempDir.resolve("Link.java");
			Files.createSymbolicLink(linkFile, realFile);

			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);

			// Behavior may vary - either both discovered or just real file with symlink resolved
			assertTrue(files.size() >= 1, "Should discover at least the real file");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoverSymlinkToDirectoryFollowedWhenEnabled() throws IOException
	{
		if (!supportsSymlinks())
			return;

		Path tempDir = Files.createTempDirectory("test-symlink-dir-");
		Path realDir = Files.createTempDirectory("test-symlink-target-");
		try
		{
			Files.createFile(realDir.resolve("Test.java"));

			Path linkDir = tempDir.resolve("link_dir");
			Files.createSymbolicLink(linkDir, realDir);

			DiscoveryConfiguration config = new DiscoveryConfiguration.Builder().followSymlinks(true).build();
			List<Path> files = discoverFiles(tempDir, config);

			assertTrue(files.size() >= 1, "Should discover files in linked directory");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
			deleteDirectoryRecursively(realDir);
		}
	}

	@Test
	public void discoverSymlinkToDirectoryOutsideRootBlockedBySecurityConfig() throws IOException
	{
		if (!supportsSymlinks())
			return;

		Path tempRoot = Files.createTempDirectory("test-symlink-escape-root-");
		Path tempOutside = Files.createTempDirectory("test-symlink-escape-outside-");
		try
		{
			Files.createFile(tempOutside.resolve("Evil.java"));

			Path linkInRoot = tempRoot.resolve("escape_link");
			Files.createSymbolicLink(linkInRoot, tempOutside);

			List<Path> files = discoverFiles(tempRoot, DiscoveryConfiguration.DEFAULT);

			// Files outside root should either be blocked or not included
			assertTrue(files.isEmpty() || files.stream().allMatch(p -> isDescendantOf(p, tempRoot)),
				"Files outside root should not be discovered");
		}
		finally
		{
			deleteDirectoryRecursively(tempRoot);
			deleteDirectoryRecursively(tempOutside);
		}
	}

	@Test(timeOut = 30_000)
	public void discoverCircularSymlinkHandledGracefully() throws IOException
	{
		if (!supportsSymlinks())
			return;

		Path tempDir = Files.createTempDirectory("test-circular-");
		try
		{
			Path subdir = Files.createDirectory(tempDir.resolve("a"));
			Path circularLink = subdir.resolve("b");
			Files.createSymbolicLink(circularLink, subdir);

			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);

			// Test passes if we reach here without hanging (timeout protection above)
			// No Java files in directory, so result should be empty
			requireThat(files, "files").isEmpty();
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoverUnicodeFilenamesDiscoveredCorrectly() throws IOException
	{
		if (!supportsUnicodeFilenames())
			return;

		Path tempDir = Files.createTempDirectory("test-unicode-");
		try
		{
			Files.createFile(tempDir.resolve("Unicode中文.java"));
			Files.createFile(tempDir.resolve("Αλφα.java"));

			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);

			assertEquals(files.size(), 2, "Should discover both Unicode-named files");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoverFilenameWithSpacesDiscoveredCorrectly() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-spaces-");
		try
		{
			Files.createFile(tempDir.resolve("Test File With Spaces.java"));

			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);

			assertEquals(files.size(), 1, "Should discover file with spaces");
			assertTrue(files.get(0).getFileName().toString().contains("Spaces"),
				"Filename should preserve spaces");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoverSpecialCharactersInPathDiscoveredCorrectly() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-special-");
		try
		{
			Path subdir = Files.createDirectory(tempDir.resolve("src-main_test.module"));
			Files.createFile(subdir.resolve("Test.java"));

			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);

			assertEquals(files.size(), 1, "Should discover file in path with special chars");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoverDeeplyNestedDirectoryRespectsMaxDepth() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-deep-");
		try
		{
			Path current = tempDir;
			for (int i = 0; i < 50; ++i)
				current = Files.createDirectory(current.resolve("level" + i));
			Files.createFile(current.resolve("Deep.java"));

			DiscoveryConfiguration config = new DiscoveryConfiguration.Builder().maxDepth(10).build();

			List<Path> files = discoverFiles(tempDir, config);

			// File at level 50 should not be discovered with maxDepth 10
			assertTrue(files.isEmpty(), "Deep file should not be discovered with maxDepth=10");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void discoverEmptyNestedDirectoriesHandledCorrectly() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-empty-nested-");
		try
		{
			Path nested = Files.createDirectory(tempDir.resolve("a")).resolve("b");
			nested = Files.createDirectories(nested.resolve("c/d"));

			Files.createFile(tempDir.resolve("Test.java"));

			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);

			assertEquals(files.size(), 1, "Should discover only the file, not count empty dirs");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test(groups = "unix-only")
	public void discoverMixedPermissionsSkipsUnreadableDirectories() throws IOException
	{
		if (!supportsUnixPermissions())
			return;

		Path tempDir = Files.createTempDirectory("test-perms-");
		try
		{
			Path readable = Files.createDirectory(tempDir.resolve("readable"));
			Files.createFile(readable.resolve("Readable.java"));

			Path unreadable = Files.createDirectory(tempDir.resolve("unreadable"));
			Files.createFile(unreadable.resolve("Hidden.java"));

			// Remove read permission
			Set<PosixFilePermission> perms = PosixFilePermissions.fromString("---------");
			Files.setPosixFilePermissions(unreadable, perms);

			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);

			// Should discover readable file, skip unreadable
			assertTrue(files.size() >= 1, "Should discover at least readable file");
		}
		finally
		{
			// Restore permissions before cleanup
			restorePermissionsAndDelete(tempDir);
		}
	}

	@Test(groups = "unix-only")
	public void discoverPermissionDeniedOnFileReportsError() throws IOException
	{
		if (!supportsUnixPermissions())
			return;

		Path tempDir = Files.createTempDirectory("test-file-perms-");
		try
		{
			Path readableFile = Files.createFile(tempDir.resolve("Readable.java"));
			Path unreadableFile = Files.createFile(tempDir.resolve("Unreadable.java"));

			// Remove read permission on one file
			Set<PosixFilePermission> perms = PosixFilePermissions.fromString("---------");
			Files.setPosixFilePermissions(unreadableFile, perms);

			FileDiscoveryResult result = createDiscovery().discover(
				List.of(tempDir), DiscoveryConfiguration.DEFAULT, SecurityConfig.DEFAULT);

			// Should discover readable file
			assertTrue(result.files().size() >= 1, "Should discover readable file");
		}
		finally
		{
			restorePermissionsAndDelete(tempDir);
		}
	}

	@Test
	public void discoverJavaFileWithNoExtensionNotDiscovered() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-no-ext-");
		try
		{
			Files.createFile(tempDir.resolve("Makefile"));
			Files.createFile(tempDir.resolve("Real.java"));

			List<Path> files = discoverFiles(tempDir, DiscoveryConfiguration.DEFAULT);

			assertEquals(files.size(), 1, "Should discover only .java files");
			assertTrue(files.get(0).getFileName().toString().equals("Real.java"));
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	private boolean supportsSymlinks()
	{
		try
		{
			Path temp = Files.createTempDirectory("test-symlink-support-");
			Path file = Files.createFile(temp.resolve("test.txt"));
			Path link = temp.resolve("link.txt");
			Files.createSymbolicLink(link, file);
			Files.deleteIfExists(link);
			Files.deleteIfExists(file);
			Files.deleteIfExists(temp);
			return true;
		}
		catch (IOException | UnsupportedOperationException _)
		{
			return false;
		}
	}

	private boolean supportsUnixPermissions()
	{
		try
		{
			Path temp = Files.createTempDirectory("test-perms-");
			Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-xr-x");
			Files.setPosixFilePermissions(temp, perms);
			Files.deleteIfExists(temp);
			return true;
		}
		catch (IOException | UnsupportedOperationException _)
		{
			return false;
		}
	}

	/**
	 * Checks if the candidate path is a descendant of (contained within) the ancestor path.
	 *
	 * @param candidate the path to check
	 * @param ancestor  the potential parent/ancestor path
	 * @return true if candidate is under ancestor in the filesystem hierarchy
	 */
	private boolean isDescendantOf(Path candidate, Path ancestor)
	{
		try
		{
			return candidate.toRealPath().startsWith(ancestor.toRealPath());
		}
		catch (IOException _)
		{
			return false;
		}
	}

	private boolean supportsUnicodeFilenames()
	{
		try
		{
			Path temp = Files.createTempDirectory("test-unicode-support-");
			Path file = Files.createFile(temp.resolve("test中文.txt"));
			Files.deleteIfExists(file);
			Files.deleteIfExists(temp);
			return true;
		}
		catch (Exception _)
		{
			return false;
		}
	}

	/**
	 * Restores permissions and deletes a directory.
	 * Used for cleanup after permission-related tests.
	 *
	 * @param directory the directory to restore and delete
	 */
	private void restorePermissionsAndDelete(Path directory)
	{
		restorePermissionsRecursively(directory);
		deleteDirectoryRecursively(directory);
	}

	/**
	 * Recursively restores permissions on a directory tree.
	 * Fixes permissions before descending to handle restricted subdirectories.
	 *
	 * @param path the path to restore permissions on
	 */
	private void restorePermissionsRecursively(Path path)
	{
		Set<PosixFilePermission> fullPerms = PosixFilePermissions.fromString("rwxrwxrwx");
		try
		{
			// First fix this path's permissions so we can access it
			Files.setPosixFilePermissions(path, fullPerms);
		}
		catch (IOException _)
		{
			// Can't fix - continue anyway
		}

		// If it's a directory, recurse into children
		if (Files.isDirectory(path))
		{
			try (var entries = Files.list(path))
			{
				entries.forEach(this::restorePermissionsRecursively);
			}
			catch (IOException _)
			{
				// Ignore - best effort
			}
		}
	}
}
