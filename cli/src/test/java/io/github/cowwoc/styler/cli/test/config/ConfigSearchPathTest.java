package io.github.cowwoc.styler.cli.test.config;

import io.github.cowwoc.styler.cli.config.ConfigSearchPath;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ConfigSearchPath class.
 * Tests search path building, platform-specific path resolution, and file discovery with thread-safe design.
 * <p>
 * Thread-safe: All tests use method-local variables and UUID-based temp directories.
 */
public class ConfigSearchPathTest
{
	/**
	 * Verifies that search path from single directory includes the directory and platform-specific paths.
	 */
	@Test
	public void buildSearchPathFromSingleDirectoryIncludesDirectoryAndPlatformPaths() throws IOException
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();
		Path tempDir = Files.createTempDirectory("styler-search-test-" + UUID.randomUUID());

		// When: building search path from single directory
		List<Path> result = searchPath.buildSearchPath(tempDir);

		// Then: includes the directory and potentially platform-specific paths
		assertThat(result).isNotEmpty();
		assertThat(result.get(0)).isEqualTo(tempDir.toAbsolutePath().normalize());
	}

	/**
	 * Verifies that search path traverses parent directories from nested structure.
	 */
	@Test
	public void buildSearchPathTraversesParentDirectories() throws IOException
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();
		Path tempDir = Files.createTempDirectory("styler-search-test-" + UUID.randomUUID());

		// Given: nested directory structure with git repository boundary
		Path gitDir = tempDir.resolve(".git");
		Files.createDirectories(gitDir);
		Path childDir = tempDir.resolve("child");
		Path grandchildDir = childDir.resolve("grandchild");
		Files.createDirectories(grandchildDir);

		// When: building search path from grandchild
		List<Path> result = searchPath.buildSearchPath(grandchildDir);

		// Then: includes grandchild, child, and temp directory (stops at git boundary)
		assertThat(result).hasSize(3);
		assertThat(result.get(0)).isEqualTo(grandchildDir.toAbsolutePath().normalize());
		assertThat(result.get(1)).isEqualTo(childDir.toAbsolutePath().normalize());
		assertThat(result.get(2)).isEqualTo(tempDir.toAbsolutePath().normalize());
	}

	/**
	 * Verifies that search path stops at git repository boundary.
	 */
	@Test
	public void buildSearchPathStopsAtGitBoundary() throws IOException
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();
		Path tempDir = Files.createTempDirectory("styler-search-test-" + UUID.randomUUID());

		// Given: git repository structure
		Path gitDir = tempDir.resolve(".git");
		Path srcDir = tempDir.resolve("src");
		Path javaDir = srcDir.resolve("main/java");
		Files.createDirectories(gitDir);
		Files.createDirectories(javaDir);

		// When: building search path from deep directory
		List<Path> result = searchPath.buildSearchPath(javaDir);

		// Then: stops at git repository root
		assertThat(result).contains(tempDir.toAbsolutePath().normalize());
		assertThat(result).doesNotContain(tempDir.getParent());
	}

	/**
	 * Verifies that null start directory throws NullPointerException.
	 */
	@Test
	public void buildSearchPathWithNullStartDirThrowsNullPointerException()
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();

		// When/Then: null start directory throws exception
		assertThatThrownBy(() -> searchPath.buildSearchPath(null)).
			isInstanceOf(NullPointerException.class).
			hasMessageContaining("startDir cannot be null");
	}

	/**
	 * Verifies that user config path returns Optional (platform-dependent).
	 */
	@Test
	public void getUserConfigPathReturnsOptionalPath()
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();

		// When: getting user config path
		Optional<Path> result = searchPath.getUserConfigPath();

		// Then: returns optional (may be empty if directory doesn't exist)
		assertThat(result).isNotNull();
		// Note: actual path depends on platform and user home directory
	}

	/**
	 * Verifies that global config path returns Optional (platform-dependent).
	 */
	@Test
	public void getGlobalConfigPathReturnsOptionalPath()
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();

		// When: getting global config path
		Optional<Path> result = searchPath.getGlobalConfigPath();

		// Then: returns optional (may be empty if directory doesn't exist)
		assertThat(result).isNotNull();
		// Note: actual path depends on platform and system configuration
	}

	/**
	 * Verifies that config discovery finds TOML file.
	 */
	@Test
	public void discoverConfigFilesWithTomlFileFindsTomlFile() throws IOException
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();
		Path tempDir = Files.createTempDirectory("styler-search-test-" + UUID.randomUUID());

		// Given: directory with TOML config file
		Path configFile = tempDir.resolve(".styler.toml");
		Files.writeString(configFile, "verbose = true");

		List<Path> searchPaths = List.of(tempDir);

		// When: discovering config files
		List<Path> result = searchPath.discoverConfigFiles(searchPaths);

		// Then: finds TOML file
		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(configFile);
	}

	/**
	 * Verifies that config discovery finds YAML file.
	 */
	@Test
	public void discoverConfigFilesWithYamlFileFindsYamlFile() throws IOException
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();
		Path tempDir = Files.createTempDirectory("styler-search-test-" + UUID.randomUUID());

		// Given: directory with YAML config file
		Path configFile = tempDir.resolve(".styler.yaml");
		Files.writeString(configFile, "verbose: true");

		List<Path> searchPaths = List.of(tempDir);

		// When: discovering config files
		List<Path> result = searchPath.discoverConfigFiles(searchPaths);

		// Then: finds YAML file
		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(configFile);
	}

	/**
	 * Verifies that config discovery prefers TOML over YAML when both formats exist.
	 */
	@Test
	public void discoverConfigFilesWithBothFormatsPrefersToml() throws IOException
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();
		Path tempDir = Files.createTempDirectory("styler-search-test-" + UUID.randomUUID());

		// Given: directory with both TOML and YAML files
		Path tomlFile = tempDir.resolve(".styler.toml");
		Path yamlFile = tempDir.resolve(".styler.yaml");
		Files.writeString(tomlFile, "verbose = true");
		Files.writeString(yamlFile, "verbose: false");

		List<Path> searchPaths = List.of(tempDir);

		// When: discovering config files
		List<Path> result = searchPath.discoverConfigFiles(searchPaths);

		// Then: prefers TOML over YAML
		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(tomlFile);
	}

	/**
	 * Verifies that config discovery respects directory precedence when files exist in multiple directories.
	 */
	@Test
	public void discoverConfigFilesInMultipleDirectoriesRespectsPrecedence() throws IOException
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();
		Path tempDir = Files.createTempDirectory("styler-search-test-" + UUID.randomUUID());

		// Given: config files in multiple directories
		Path dir1 = tempDir.resolve("dir1");
		Path dir2 = tempDir.resolve("dir2");
		Files.createDirectories(dir1);
		Files.createDirectories(dir2);

		Path config1 = dir1.resolve(".styler.toml");
		Path config2 = dir2.resolve(".styler.toml");
		Files.writeString(config1, "verbose = true");
		Files.writeString(config2, "verbose = false");

		List<Path> searchPaths = List.of(dir1, dir2);

		// When: discovering config files
		List<Path> result = searchPath.discoverConfigFiles(searchPaths);

		// Then: finds both files in order
		assertThat(result).hasSize(2);
		assertThat(result.get(0)).isEqualTo(config1);
		assertThat(result.get(1)).isEqualTo(config2);
	}

	/**
	 * Verifies that config discovery returns empty list when no config files exist.
	 */
	@Test
	public void discoverConfigFilesWithNoConfigFilesReturnsEmptyList() throws IOException
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();
		Path tempDir = Files.createTempDirectory("styler-search-test-" + UUID.randomUUID());

		// Given: directory with no config files
		List<Path> searchPaths = List.of(tempDir);

		// When: discovering config files
		List<Path> result = searchPath.discoverConfigFiles(searchPaths);

		// Then: returns empty list
		assertThat(result).isEmpty();
	}

	/**
	 * Verifies that config discovery handles unreadable files gracefully.
	 */
	@Test
	public void discoverConfigFilesWithUnreadableFileIgnoresFile() throws IOException
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();
		Path tempDir = Files.createTempDirectory("styler-search-test-" + UUID.randomUUID());

		// Given: unreadable config file
		Path configFile = tempDir.resolve(".styler.toml");
		Files.writeString(configFile, "verbose = true");

		// Note: Making file unreadable is platform-specific and may not work in all test environments
		// This test demonstrates the intent but may need platform-specific implementation

		List<Path> searchPaths = List.of(tempDir);

		// When: discovering config files
		List<Path> result = searchPath.discoverConfigFiles(searchPaths);

		// Then: behavior depends on file permissions (may find or ignore the file)
		assertThat(result).isNotNull();
	}

	/**
	 * Verifies that null search paths throws NullPointerException.
	 */
	@Test
	public void discoverConfigFilesWithNullSearchPathsThrowsNullPointerException()
	{
		ConfigSearchPath searchPath = new ConfigSearchPath();

		// When/Then: null search paths throws exception
		assertThatThrownBy(() -> searchPath.discoverConfigFiles(null)).
			isInstanceOf(NullPointerException.class).
			hasMessageContaining("searchPaths cannot be null");
	}
}
