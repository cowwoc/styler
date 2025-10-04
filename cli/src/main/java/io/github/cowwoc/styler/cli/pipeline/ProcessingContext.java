package io.github.cowwoc.styler.cli.pipeline;

import java.nio.file.Path;
import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Immutable context object passed between pipeline stages.
 * <p>
 * The processing context carries shared state and configuration through the pipeline without
 * requiring direct coupling between stages. All fields are immutable and the context itself
 * is immutable.
 * <p>
 * Use {@link Builder} to create new contexts or derive modified contexts from existing ones.
 *
 * @param sourceFile     the source file being processed (never {@code null})
 * @param configuration  pipeline configuration settings (never {@code null}, may be empty)
 * @param metadata      arbitrary metadata for pipeline extensions (never {@code null}, may be empty)
 */
public record ProcessingContext(
	Path sourceFile,
	Map<String, Object> configuration,
	Map<String, Object> metadata)
{
	/**
	 * Compact constructor enforcing invariants.
	 *
	 * @throws NullPointerException if {@code sourceFile}, {@code configuration}, or {@code metadata} is {@code null}
	 */
	public ProcessingContext
	{
		requireThat(sourceFile, "sourceFile").isNotNull();
		requireThat(configuration, "configuration").isNotNull();
		requireThat(metadata, "metadata").isNotNull();

		// Defensive copies to ensure immutability
		configuration = Map.copyOf(configuration);
		metadata = Map.copyOf(metadata);
	}

	/**
	 * Returns a builder for creating a new processing context.
	 *
	 * @param sourceFile the source file being processed (never {@code null})
	 * @return a new builder instance (never {@code null})
	 * @throws NullPointerException if {@code sourceFile} is {@code null}
	 */
	public static Builder builder(Path sourceFile)
	{
		return new Builder(sourceFile);
	}

	/**
	 * Returns a builder initialized with this context's values.
	 * <p>
	 * Use this to create a derived context with modified fields while preserving others.
	 *
	 * @return a builder pre-populated with this context's values (never {@code null})
	 */
	public Builder toBuilder()
	{
		return new Builder(sourceFile).
			configuration(configuration).
			metadata(metadata);
	}

	/**
	 * Builder for creating {@link ProcessingContext} instances.
	 * <p>
	 * The builder uses a fluent API for convenient context construction and supports
	 * incremental configuration and metadata additions.
	 */
	public static final class Builder
	{
		private final Path sourceFile;
		private Map<String, Object> configuration = Map.of();
		private Map<String, Object> metadata = Map.of();

		private Builder(Path sourceFile)
		{
			requireThat(sourceFile, "sourceFile").isNotNull();
			this.sourceFile = sourceFile;
		}

		/**
		 * Sets the configuration map.
		 *
		 * @param configuration the configuration settings (never {@code null})
		 * @return this builder for method chaining (never {@code null})
		 * @throws NullPointerException if {@code configuration} is {@code null}
		 */
		public Builder configuration(Map<String, Object> configuration)
		{
			requireThat(configuration, "configuration").isNotNull();
			this.configuration = configuration;
			return this;
		}

		/**
		 * Sets the metadata map.
		 *
		 * @param metadata the metadata map (never {@code null})
		 * @return this builder for method chaining (never {@code null})
		 * @throws NullPointerException if {@code metadata} is {@code null}
		 */
		public Builder metadata(Map<String, Object> metadata)
		{
			requireThat(metadata, "metadata").isNotNull();
			this.metadata = metadata;
			return this;
		}

		/**
		 * Builds a new {@link ProcessingContext} instance.
		 *
		 * @return a new immutable processing context (never {@code null})
		 */
		public ProcessingContext build()
		{
			return new ProcessingContext(sourceFile, configuration, metadata);
		}
	}
}
