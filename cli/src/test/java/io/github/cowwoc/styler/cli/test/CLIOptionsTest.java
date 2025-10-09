package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.CLIOptions;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link CLIOptions} immutable record and Builder.
 * <p>
 * All tests are parallel-safe with no shared mutable state.
 */
public class CLIOptionsTest
{
	// ========== Builder Construction Tests ==========

	/**
	 * Validates that building with valid inputs creates immutable options with correct values.
	 */
	@Test
	public void buildWithValidInputsCreatesImmutableOptions()
	{
		// Arrange
		CLIOptions.Builder builder = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			setCheckMode(true);

		// Act
		CLIOptions options = builder.build();

		// Assert
		requireThat(options.inputPaths().size(), "inputPaths.size()").isEqualTo(1);
		requireThat(options.inputPaths().getFirst(), "inputPaths.getFirst()").
			isEqualTo(Path.of("test.java"));
		requireThat(options.checkMode(), "checkMode").isTrue();
		requireThat(options.fixMode(), "fixMode").isFalse();
		requireThat(options.configPath().isEmpty(), "value").isTrue();
	}

	/**
	 * Validates that builder stores all added input paths in order.
	 */
	@Test
	public void buildWithMultipleInputPathsStoresAllPaths()
	{
		// Arrange & Act
		CLIOptions options = new CLIOptions.Builder().
			addInputPath(Path.of("File1.java")).
			addInputPath(Path.of("File2.java")).
			addInputPath(Path.of("src/")).
			build();

		// Assert
		requireThat(options.inputPaths().size(), "inputPaths.size()").isEqualTo(3);
		requireThat(options.inputPaths().get(0), "inputPaths[0]").
			isEqualTo(Path.of("File1.java"));
		requireThat(options.inputPaths().get(1), "inputPaths[1]").
			isEqualTo(Path.of("File2.java"));
		requireThat(options.inputPaths().get(2), "inputPaths[2]").isEqualTo(Path.of("src/"));
	}

	/**
	 * Validates that config path is present when set via builder.
	 */
	@Test
	public void buildWithConfigPathStoresConfigPath()
	{
		// Arrange & Act
		CLIOptions options = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			setConfigPath(Path.of("config.xml")).
			build();

		// Assert
		requireThat(options.configPath().isPresent(), "configPath.isPresent()").isTrue();
		requireThat(options.configPath().get(), "configPath.get()").
			isEqualTo(Path.of("config.xml"));
	}

	/**
	 * Validates that config path is empty when not set via builder.
	 */
	@Test
	public void buildWithoutConfigPathHasEmptyConfigPath()
	{
		// Arrange & Act
		CLIOptions options = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			build();

		// Assert
		requireThat(options.configPath().isEmpty(), "value").isTrue();
	}

	/**
	 * Validates that check mode can be enabled and fix mode remains disabled.
	 */
	@Test
	public void buildWithCheckModeSetsCheckModeTrue()
	{
		// Arrange & Act
		CLIOptions options = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			setCheckMode(true).
			build();

		// Assert
		requireThat(options.checkMode(), "checkMode").isTrue();
		requireThat(options.fixMode(), "fixMode").isFalse();
	}

	/**
	 * Validates that fix mode can be enabled and check mode remains disabled.
	 */
	@Test
	public void buildWithFixModeSetsFixModeTrue()
	{
		// Arrange & Act
		CLIOptions options = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			setFixMode(true).
			build();

		// Assert
		requireThat(options.fixMode(), "fixMode").isTrue();
		requireThat(options.checkMode(), "checkMode").isFalse();
	}

	/**
	 * Validates that both modes are false when neither is explicitly set.
	 */
	@Test
	public void buildWithNeitherCheckNorFixModeSetsBothFalse()
	{
		// Arrange & Act
		CLIOptions options = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			build();

		// Assert
		requireThat(options.checkMode(), "checkMode").isFalse();
		requireThat(options.fixMode(), "fixMode").isFalse();
	}

	// ========== Validation Tests ==========

	/**
	 * Validates that enabling both check and fix modes throws
	 * {@link IllegalArgumentException}.
	 */
	@Test
	public void buildWithBothCheckAndFixModeThrowsException()
	{
		// Arrange, Act & Assert
		try
		{
			new CLIOptions.Builder().
				addInputPath(Path.of("test.java")).
				setCheckMode(true).
				setFixMode(true).
				build();
			throw new AssertionError("Expected IllegalArgumentException to be thrown");
		}
		catch (IllegalArgumentException e)
		{
			String message = e.getMessage();
			requireThat(message.contains("check mode") || message.contains("fix mode"),
				"value").isTrue();
		}
	}

	/**
	 * Validates that building without input paths throws
	 * {@link IllegalArgumentException}.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void buildWithNoInputPathsThrowsException()
	{
		// Arrange, Act & Assert
		new CLIOptions.Builder().build();
	}

	/**
	 * Validates that adding a null input path throws {@link NullPointerException}.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void addInputPathWithNullPathThrowsException()
	{
		// Arrange, Act & Assert
		new CLIOptions.Builder().addInputPath(null);
	}

	// ========== Immutability Tests ==========

	/**
	 * Validates that the returned input paths list is unmodifiable.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void inputPathsModifyingReturnedListDoesNotAffectOriginal()
	{
		// Arrange
		CLIOptions options = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			build();

		// Act & Assert
		options.inputPaths().add(Path.of("new.java"));
	}

	/**
	 * Validates that modifying builder after build() does not affect built options
	 * (defensive copying).
	 */
	@Test
	public void inputPathsModifyingBuilderListAfterBuildDoesNotAffectOptions()
	{
		// Arrange
		List<Path> builderPaths = new ArrayList<>();
		builderPaths.add(Path.of("test.java"));

		CLIOptions.Builder builder = new CLIOptions.Builder();
		builderPaths.forEach(builder::addInputPath);
		CLIOptions options = builder.build();

		// Act
		builder.addInputPath(Path.of("new.java"));

		// Assert
		requireThat(options.inputPaths().size(), "inputPaths.size()").isEqualTo(1);
		requireThat(options.inputPaths().getFirst(), "inputPaths.getFirst()").
			isEqualTo(Path.of("test.java"));
	}

	// ========== Record Equality Tests ==========

	/**
	 * Validates that record equality works correctly when all fields match.
	 */
	@Test
	public void equalsWithSameValuesReturnsTrue()
	{
		// Arrange
		CLIOptions options1 = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			setCheckMode(true).
			build();

		CLIOptions options2 = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			setCheckMode(true).
			build();

		// Act & Assert
		requireThat(options1, "options1").isEqualTo(options2);
		requireThat(options1.hashCode(), "options1.hashCode()").
			isEqualTo(options2.hashCode());
	}

	/**
	 * Validates that records with different input paths are not equal.
	 */
	@Test
	public void equalsWithDifferentInputPathsReturnsFalse()
	{
		// Arrange
		CLIOptions options1 = new CLIOptions.Builder().
			addInputPath(Path.of("test1.java")).
			build();

		CLIOptions options2 = new CLIOptions.Builder().
			addInputPath(Path.of("test2.java")).
			build();

		// Act & Assert
		requireThat(options1, "options1").isNotEqualTo(options2);
	}

	/**
	 * Validates that records with different check mode values are not equal.
	 */
	@Test
	public void equalsWithDifferentCheckModeReturnsFalse()
	{
		// Arrange
		CLIOptions options1 = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			setCheckMode(true).
			build();

		CLIOptions options2 = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			setCheckMode(false).
			build();

		// Act & Assert
		requireThat(options1, "options1").isNotEqualTo(options2);
	}

	// ========== Builder Fluency Tests ==========

	/**
	 * Validates that builder methods return the builder instance for method chaining.
	 */
	@Test
	public void builderFluentInterfaceReturnsBuilderForChaining()
	{
		// Arrange & Act
		CLIOptions options = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			setConfigPath(Path.of("config.xml")).
			setCheckMode(true).
			build();

		// Assert
		requireThat(options.inputPaths().size(), "inputPaths.size()").isEqualTo(1);
		requireThat(options.configPath().get(), "configPath.get()").
			isEqualTo(Path.of("config.xml"));
		requireThat(options.checkMode(), "checkMode").isTrue();
	}

	// ========== Edge Case Tests ==========

	/**
	 * Validates that setting config path to null results in empty optional.
	 */
	@Test
	public void buildWithNullConfigPathHasEmptyOptional()
	{
		// Arrange & Act
		CLIOptions options = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			setConfigPath(null).
			build();

		// Assert
		requireThat(options.configPath().isEmpty(), "value").isTrue();
	}

	/**
	 * Validates that setting a mode multiple times uses the last value set.
	 */
	@Test
	public void buildWithModeSetToFalseThenTrueStoresTrue()
	{
		// Arrange & Act
		CLIOptions options = new CLIOptions.Builder().
			addInputPath(Path.of("test.java")).
			setCheckMode(false).
			setCheckMode(true).
			build();

		// Assert
		requireThat(options.checkMode(), "checkMode").isTrue();
	}
}
