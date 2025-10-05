package io.github.cowwoc.styler.cli.test.pipeline;
import io.github.cowwoc.styler.cli.pipeline.ProcessingContext;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link ProcessingContext}.
 */
@SuppressWarnings("PMD.MethodNamingConventions") // Test methods use descriptive_scenario_outcome pattern
public final class ProcessingContextTest
{
	/**
	 * Verifies that builder creates context with minimal configuration.
	 */
	@Test
	public void builder_minimalConfiguration_createsContext()
	{
		Path sourceFile = Paths.get("test.java");

		ProcessingContext context = ProcessingContext.builder(sourceFile).build();

		requireThat(context.sourceFile(), "sourceFile").isEqualTo(sourceFile);
		requireThat(context.configuration(), "configuration").isEmpty();
		requireThat(context.metadata(), "metadata").isEmpty();
	}

	/**
	 * Verifies that builder creates context with custom configuration.
	 */
	@Test
	public void builder_withConfiguration_includesConfiguration()
	{
		Path sourceFile = Paths.get("test.java");
		Map<String, Object> config = Map.of("key", "value");

		ProcessingContext context = ProcessingContext.builder(sourceFile).
			configuration(config).
			build();

		requireThat(context.configuration(), "configuration").isEqualTo(config);
	}

	/**
	 * Verifies that builder creates context with custom metadata.
	 */
	@Test
	public void builder_withMetadata_includesMetadata()
	{
		Path sourceFile = Paths.get("test.java");
		Map<String, Object> metadata = Map.of("timestamp", System.currentTimeMillis());

		ProcessingContext context = ProcessingContext.builder(sourceFile).
			metadata(metadata).
			build();

		requireThat(context.metadata(), "metadata").isEqualTo(metadata);
	}

	/**
	 * Verifies that configuration map is immutable.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void context_modifyConfiguration_throwsUnsupportedOperationException()
	{
		Path sourceFile = Paths.get("test.java");
		ProcessingContext context = ProcessingContext.builder(sourceFile).build();

		context.configuration().put("key", "value");
	}

	/**
	 * Verifies that metadata map is immutable.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void context_modifyMetadata_throwsUnsupportedOperationException()
	{
		Path sourceFile = Paths.get("test.java");
		ProcessingContext context = ProcessingContext.builder(sourceFile).build();

		context.metadata().put("key", "value");
	}

	/**
	 * Verifies that toBuilder preserves all context data.
	 */
	@Test
	public void context_toBuilder_preservesData()
	{
		Path sourceFile = Paths.get("test.java");
		Map<String, Object> config = Map.of("key1", "value1");
		Map<String, Object> metadata = Map.of("key2", "value2");

		ProcessingContext original = ProcessingContext.builder(sourceFile).
			configuration(config).
			metadata(metadata).
			build();

		ProcessingContext copy = original.toBuilder().build();

		requireThat(copy.sourceFile(), "sourceFile").isEqualTo(original.sourceFile());
		requireThat(copy.configuration(), "configuration").isEqualTo(original.configuration());
		requireThat(copy.metadata(), "metadata").isEqualTo(original.metadata());
	}

	/**
	 * Verifies that toBuilder allows modifications.
	 */
	@Test
	public void context_toBuilderWithChanges_createsModifiedContext()
	{
		Path sourceFile = Paths.get("test.java");
		ProcessingContext original = ProcessingContext.builder(sourceFile).build();

		Map<String, Object> newConfig = Map.of("newKey", "newValue");
		ProcessingContext modified = original.toBuilder().
			configuration(newConfig).
			build();

		requireThat(modified.sourceFile(), "sourceFile").isEqualTo(original.sourceFile());
		requireThat(modified.configuration(), "configuration").isEqualTo(newConfig);
		requireThat(original.configuration(), "original.configuration").isEmpty();
	}
}
