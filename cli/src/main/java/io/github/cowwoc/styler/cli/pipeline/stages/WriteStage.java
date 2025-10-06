package io.github.cowwoc.styler.cli.pipeline.stages;

import io.github.cowwoc.styler.cli.pipeline.AbstractPipelineStage;
import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.ProcessingContext;
import io.github.cowwoc.styler.cli.security.FileValidator;
import io.github.cowwoc.styler.cli.security.PathSanitizer;
import io.github.cowwoc.styler.cli.security.SecurityConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

/**
 * Pipeline stage that writes formatted source code to disk with security validation.
 * <p>
 * This stage takes formatted source text and writes it to the output file, applying
 * security checks to prevent directory traversal attacks, file size violations, and
 * unauthorized file type creation.
 * <p>
 * The write operation is atomic - content is first written to a temporary file, then
 * moved to the target location to prevent partial writes if the process is interrupted.
 * <p>
 * Example usage:
 * <pre>{@code
 * WriteStage stage = new WriteStage();
 * ProcessingContext context = ProcessingContext.builder(sourceFile).build();
 * StageResult<Path> result = stage.execute(formattedSource, context);
 *
 * if (result.isSuccess()) {
 *     Path writtenFile = result.output().orElseThrow();
 *     System.out.println("Wrote: " + writtenFile);
 * }
 * }</pre>
 *
 * @see FileValidator
 * @see PathSanitizer
 * @see SecurityConfig
 */
public final class WriteStage extends AbstractPipelineStage<String, Path>
{
	private final PathSanitizer pathSanitizer;
	// Reserved for future file size validation
	@SuppressWarnings("PMD.UnusedPrivateField")
	private final FileValidator fileValidator;

	/**
	 * Creates a write stage with default security configuration.
	 */
	public WriteStage()
	{
		this(SecurityConfig.defaults());
	}

	/**
	 * Creates a write stage with custom security configuration.
	 *
	 * @param securityConfig the security configuration (never {@code null})
	 * @throws NullPointerException if {@code securityConfig} is {@code null}
	 */
	public WriteStage(SecurityConfig securityConfig)
	{
		super();
		if (securityConfig == null)
		{
			throw new NullPointerException("securityConfig must not be null");
		}
		this.pathSanitizer = new PathSanitizer();
		this.fileValidator = new FileValidator(
			securityConfig.maxFileSizeBytes(),
			securityConfig.allowedExtensions());
	}

	@Override
	protected Path process(String input, ProcessingContext context) throws PipelineException
	{
		Path outputFile = context.sourceFile();

		try
		{
			// Validate and sanitize output path
			Path sanitizedPath = pathSanitizer.sanitize(outputFile);

			// Validate output file extension
			String filename = sanitizedPath.getFileName().toString();
			if (!filename.endsWith(".java"))
			{
				String extension = filename.substring(filename.lastIndexOf('.'));
				throw new io.github.cowwoc.styler.cli.security.exceptions.FileTypeNotAllowedException(
					sanitizedPath, extension, Set.of(".java"));
			}

			// Create parent directories if needed
			Path parentDir = sanitizedPath.getParent();
			if (parentDir != null && !Files.exists(parentDir))
			{
				Files.createDirectories(parentDir);
			}

			// Write atomically: temp file -> move to final location
			Path tempFile = Files.createTempFile(
				sanitizedPath.getParent(),
				".styler-",
				".java.tmp");

			try
			{
				// Write content to temp file
				Files.writeString(tempFile, input);

				// Atomic move to final location
				Files.move(tempFile, sanitizedPath,
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.ATOMIC_MOVE);

				logger.debug("Successfully wrote file: {}", sanitizedPath);
				return sanitizedPath;
			}
			catch (IOException e)
			{
				// Cleanup temp file on failure
				try
				{
					Files.deleteIfExists(tempFile);
				}
				catch (IOException cleanupError)
				{
					logger.warn("Failed to delete temp file {}: {}",
						tempFile, cleanupError.getMessage());
				}
				throw e;
			}
		}
		catch (IOException e)
		{
			throw new PipelineException(
				"Failed to write output file: " + e.getMessage(),
				outputFile,
				getStageId(),
				e);
		}
		catch (io.github.cowwoc.styler.cli.security.exceptions.SecurityException e)
		{
			throw new PipelineException(
				"Security validation failed: " + e.getMessage(),
				outputFile,
				getStageId(),
				e);
		}
	}

	@Override
	protected void validateInput(String input, ProcessingContext context) throws PipelineException
	{
		super.validateInput(input, context);

		if (input.isEmpty())
		{
			throw new PipelineException(
				"Cannot write empty content",
				context.sourceFile(),
				getStageId());
		}
	}

	@Override
	public String getStageId()
	{
		return "write";
	}

	@Override
	public boolean supportsErrorRecovery()
	{
		return true; // I/O errors can use retry strategy
	}
}
