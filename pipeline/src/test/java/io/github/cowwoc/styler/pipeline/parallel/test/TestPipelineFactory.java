package io.github.cowwoc.styler.pipeline.parallel.test;

import java.util.ArrayList;
import java.util.List;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.WrapStyle;
import io.github.cowwoc.styler.pipeline.FileProcessingPipeline;
import io.github.cowwoc.styler.security.SecurityConfig;

/**
 * Factory for creating test pipelines.
 */
public final class TestPipelineFactory
{
	private TestPipelineFactory()
	{
		// Prevent instantiation
	}

	/**
	 * Creates a default pipeline for testing.
	 *
	 * @return a default {@code FileProcessingPipeline}
	 */
	public static FileProcessingPipeline createDefaultPipeline()
	{
		SecurityConfig securityConfig = new SecurityConfig.Builder().build();
		List<FormattingRule> rules = new ArrayList<>();
		FormattingConfiguration formattingConfig = createDefaultFormattingConfig();

		return FileProcessingPipeline.builder().
			securityConfig(securityConfig).
			formattingRules(rules).
			formattingConfig(formattingConfig).
			build();
	}

	/**
	 * Creates a pipeline with custom security configuration.
	 *
	 * @param securityConfig the security configuration
	 * @return a configured {@code FileProcessingPipeline}
	 */
	public static FileProcessingPipeline createPipelineWithSecurityConfig(SecurityConfig securityConfig)
	{
		List<FormattingRule> rules = new ArrayList<>();
		FormattingConfiguration formattingConfig = createDefaultFormattingConfig();

		return FileProcessingPipeline.builder().
			securityConfig(securityConfig).
			formattingRules(rules).
			formattingConfig(formattingConfig).
			build();
	}

	/**
	 * Creates a default formatting configuration for testing.
	 *
	 * @return a default {@code FormattingConfiguration}
	 */
	private static FormattingConfiguration createDefaultFormattingConfig()
	{
		return new LineLengthConfiguration(
			"line-length",
			120,  // maxLineLength
			4,    // tabWidth
			4,    // indentContinuationLines
			WrapStyle.AFTER,  // methodChainWrap
			WrapStyle.AFTER,  // methodArgumentsWrap
			WrapStyle.AFTER,  // binaryExpressionWrap
			WrapStyle.AFTER,  // methodParametersWrap
			WrapStyle.AFTER,  // ternaryExpressionWrap
			WrapStyle.AFTER,  // arrayInitializerWrap
			WrapStyle.AFTER,  // annotationArgumentsWrap
			WrapStyle.AFTER,  // genericTypeArgsWrap
			true);  // wrapLongStrings
	}
}
