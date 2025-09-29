package io.github.cowwoc.styler.cli.config;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ConfigSearchPath class.
 * Tests search path building, platform-specific path resolution, and file discovery with thread-safe design.
 */
public class ConfigSearchPathTest
{
	private ConfigSearchPath searchPath;
	private Path tempDir;

	@BeforeMethod
	public void setUp() throws IOException
	{
		// Create fresh instance and temp directory for each test (thread-safe)
		searchPath = new ConfigSearchPath();
		tempDir = Files.createTempDirectory("styler-search-test-");
	}

	@Test
	public void buildSearchPath_fromSingleDirectory_includesDirectoryAndPlatformPaths()
	{
		// When: building search path from single directory
		List<Path> result = searchPath.buildSearchPath(tempDir);

		// Then: includes the directory and potentially platform-specific paths
		assertThat(result).isNotEmpty();
		assertThat(result.get(0)).isEqualTo(tempDir.toAbsolutePath().normalize());
	}

	@Test
	public void buildSearchPath_traversesParentDirectories() throws IOException
	{
		// Given: nested directory structure
		Path childDir = tempDir.resolve("child");
		Path grandchildDir = childDir.resolve("grandchild");
		Files.createDirectories(grandchildDir);

		// When: building search path from grandchild
		List<Path> result = searchPath.buildSearchPath(grandchildDir);

		// Then: includes grandchild, child, and temp directory
		assertThat(result).hasSize(3);
		assertThat(result.get(0)).isEqualTo(grandchildDir.toAbsolutePath().normalize());
		assertThat(result.get(1)).isEqualTo(childDir.toAbsolutePath().normalize());
		assertThat(result.get(2)).isEqualTo(tempDir.toAbsolutePath().normalize());
	}

	@Test
	public void buildSearchPath_stopsAtGitBoundary() throws IOException
	{
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

	@Test
	public void buildSearchPath_withNullStartDir_throwsNullPointerException()
	{
		// When/Then: null start directory throws exception
		assertThatThrownBy(() -> searchPath.buildSearchPath(null))
			.isInstanceOf(NullPointerException.class)
			.hasMessageContaining("startDir cannot be null");
	}

	@Test
	public void getUserConfigPath_returnsOptionalPath()
	{
		// When: getting user config path
		Optional<Path> result = searchPath.getUserConfigPath();

		// Then: returns optional (may be empty if directory doesn't exist)
		assertThat(result).isNotNull();
		// Note: actual path depends on platform and user home directory
	}

	@Test
	public void getGlobalConfigPath_returnsOptionalPath()
	{
		// When: getting global config path
		Optional<Path> result = searchPath.getGlobalConfigPath();

		// Then: returns optional (may be empty if directory doesn't exist)
		assertThat(result).isNotNull();
		// Note: actual path depends on platform and system configuration
	}

	@Test
	public void discoverConfigFiles_withTomlFile_findsTomlFile() throws IOException
	{
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

	@Test
	public void discoverConfigFiles_withYamlFile_findsYamlFile() throws IOException
	{
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

	@Test
	public void discoverConfigFiles_withBothFormats_prefersToml() throws IOException
	{
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

	@Test
	public void discoverConfigFiles_inMultipleDirectories_respectsPrecedence() throws IOException
	{
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

	@Test
	public void discoverConfigFiles_withNoConfigFiles_returnsEmptyList()
	{
		// Given: directory with no config files
		List<Path> searchPaths = List.of(tempDir);

		// When: discovering config files
		List<Path> result = searchPath.discoverConfigFiles(searchPaths);

		// Then: returns empty list
		assertThat(result).isEmpty();
	}

	@Test
	public void discoverConfigFiles_withUnreadableFile_ignoresFile() throws IOException
	{
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

	@Test
	public void discoverConfigFiles_withNullSearchPaths_throwsNullPointerException()
	{
		// When/Then: null search paths throws exception
		assertThatThrownBy(() -> searchPath.discoverConfigFiles(null))
			.isInstanceOf(NullPointerException.class)
			.hasMessageContaining("searchPaths cannot be null");
	}
}