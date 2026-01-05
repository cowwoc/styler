package io.github.cowwoc.styler.config.test;

import io.github.cowwoc.styler.config.ConfigDiscovery;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link ConfigDiscovery} focusing on hierarchical search and .git boundary logic.
 * <p>
 * Priority 1: Business logic tests validate git boundary protection prevents cross-repo
 * configuration pollution.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class ConfigDiscoveryTest
{
	private final ConfigDiscovery discovery = new ConfigDiscovery();

	/**
	 * RISK: .git boundary not respected → leaks config across repositories
	 * IMPACT: Critical - nested repos get parent repo's config (security/correctness issue)
	 */
	@Test
	public void discover_stopsAtGitBoundary_preventsConfigLeakage() throws IOException
	{
		Path tempDir = Files.createTempDirectory("styler-test");
		try
		{
			// Create directory structure:
			// temp/
			//   .styler.toml        <- Should NOT be found (beyond .git boundary)
			//   project/
			//     .git/             <- Boundary
			//     nested/
			//       .styler.toml    <- Should be found
			Path parentConfig = tempDir.resolve(".styler.toml");
			Path gitDir = tempDir.resolve("project").resolve(".git");
			Path nestedConfig = tempDir.resolve("project").resolve("nested").resolve(".styler.toml");

			Files.createDirectories(gitDir);
			Files.createDirectories(nestedConfig.getParent());
			Files.writeString(parentConfig, "maxLineLength = 80");
			Files.writeString(nestedConfig, "maxLineLength = 100");

			// Start discovery from nested directory
			List<Path> found = discovery.discover(nestedConfig.getParent());

			// Should only find nested config, NOT parent (blocked by .git)
			requireThat(found.size(), "foundConfigsSize").isEqualTo(1);
			requireThat(found.getFirst(), "configPath").isEqualTo(nestedConfig);
		}
		finally
		{
			deleteRecursively(tempDir);
		}
	}

	/**
	 * RISK: Parent traversal stops too early → misses valid parent configs
	 * IMPACT: High - user config in parent directories ignored
	 */
	@Test
	public void discover_walksParents_untilGitBoundary() throws IOException
	{
		Path tempDir = Files.createTempDirectory("styler-test");
		try
		{
			// Create directory structure:
			// temp/
			//   .git/                    <- Boundary
			//   .styler.toml             <- Should be found (before boundary)
			//   project/
			//     subdir/
			//       deep/
			//         .styler.toml       <- Should be found
			Path gitDir = tempDir.resolve(".git");
			Path rootConfig = tempDir.resolve(".styler.toml");
			Path deepConfig = tempDir.resolve("project").resolve("subdir").resolve("deep").
				resolve(".styler.toml");

			Files.createDirectories(gitDir);
			Files.createDirectories(deepConfig.getParent());
			Files.writeString(rootConfig, "maxLineLength = 80");
			Files.writeString(deepConfig, "maxLineLength = 100");

			// Start discovery from deep directory
			List<Path> found = discovery.discover(deepConfig.getParent());

			// Should find both: deep (nearest) and root (parent before .git)
			requireThat(found.size(), "foundConfigsSize").isEqualTo(2);
			requireThat(found.get(0), "nearestConfig").isEqualTo(deepConfig);
			requireThat(found.get(1), "parentConfig").isEqualTo(rootConfig);
		}
		finally
		{
			deleteRecursively(tempDir);
		}
	}

	/**
	 * RISK: Discovery order wrong → farthest config first instead of nearest
	 * IMPACT: Critical - breaks precedence logic in ConfigMerger
	 */
	@Test
	public void discover_ordersNearestFirst_forMergerPrecedence() throws IOException
	{
		Path tempDir = Files.createTempDirectory("styler-test");
		try
		{
			// Create multiple configs at different levels
			Path level1 = tempDir.resolve(".styler.toml");
			Path level2 = tempDir.resolve("dir1").resolve(".styler.toml");
			Path level3 = tempDir.resolve("dir1").resolve("dir2").resolve(".styler.toml");

			Files.createDirectories(level3.getParent());
			Files.writeString(level1, "maxLineLength = 80");
			Files.writeString(level2, "maxLineLength = 100");
			Files.writeString(level3, "maxLineLength = 120");

			// Start from deepest level
			List<Path> found = discovery.discover(level3.getParent());

			// Should be ordered: nearest (level3) → parent (level2) → grandparent (level1)
			requireThat(found.size(), "foundConfigsSize").isEqualTo(3);
			requireThat(found.get(0), "nearest").isEqualTo(level3);
			requireThat(found.get(1), "parent").isEqualTo(level2);
			requireThat(found.get(2), "grandparent").isEqualTo(level1);
		}
		finally
		{
			deleteRecursively(tempDir);
		}
	}

	/**
	 * RISK: No config files → throws exception instead of returning empty list
	 * IMPACT: Medium - breaks graceful degradation when no configs exist
	 */
	@Test
	public void discover_noConfigFiles_returnsEmptyList() throws IOException
	{
		Path tempDir = Files.createTempDirectory("styler-test");
		try
		{
			List<Path> found = discovery.discover(tempDir);

			requireThat(found, "foundConfigs").isEmpty();
		}
		finally
		{
			deleteRecursively(tempDir);
		}
	}

	/**
	 * RISK: .git file (not directory) treated as boundary
	 * IMPACT: Low - edge case in submodules, but .git should be directory
	 */
	@Test
	public void discover_gitFile_notTreatedAsBoundary() throws IOException
	{
		Path tempDir = Files.createTempDirectory("styler-test");
		try
		{
			// Create .git as file (submodule pattern)
			Path gitFile = tempDir.resolve("project").resolve(".git");
			Path config = tempDir.resolve("project").resolve("subdir").resolve(".styler.toml");

			Files.createDirectories(config.getParent());
			Files.writeString(gitFile, "gitdir: ../.git/modules/project");
			Files.writeString(config, "maxLineLength = 100");

			List<Path> found = discovery.discover(config.getParent());

			// Should find config (git file doesn't block traversal)
			requireThat(found, "foundConfigs").isNotEmpty();
		}
		finally
		{
			deleteRecursively(tempDir);
		}
	}

	private void deleteRecursively(Path path) throws IOException
	{
		if (Files.isDirectory(path))
			try (var stream = Files.list(path))
			{
				for (Path child : stream.toList())
					deleteRecursively(child);
			}
		Files.deleteIfExists(path);
	}
}
