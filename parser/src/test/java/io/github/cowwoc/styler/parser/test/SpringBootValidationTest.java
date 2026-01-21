package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.Parser;
import io.github.cowwoc.styler.parser.ParseResult;
import org.testng.annotations.Test;

import java.nio.file.*;
import java.util.concurrent.atomic.*;

/**
 * Validation test for parsing Spring Boot codebase.
 */
public class SpringBootValidationTest
{
	/**
	 * Validates that the parser can parse all Java files in Spring Boot.
	 * This test checks for type-use annotation support and other edge cases.
	 */
	@Test
	public void shouldParseAllSpringBootFiles() throws Exception
	{
		Path springBoot = Path.of("/home/node/spring-boot");
		if (!Files.exists(springBoot))
		{
			System.out.println("Spring Boot not found at " + springBoot + ", skipping validation");
			return;
		}

		AtomicInteger total = new AtomicInteger();
		AtomicInteger success = new AtomicInteger();
		AtomicInteger failed = new AtomicInteger();

		long startTime = System.currentTimeMillis();

		Files.walk(springBoot)
			.filter(p -> p.toString().endsWith(".java"))
			.parallel()
			.forEach(file -> {
				total.incrementAndGet();
				try
				{
					String source = Files.readString(file);
					try (Parser parser = new Parser(source))
					{
						ParseResult result = parser.parse();
						if (result instanceof ParseResult.Success)
							success.incrementAndGet();
						else
						{
							failed.incrementAndGet();
							System.err.println(file + ": " + result);
						}
					}
				}
				catch (Exception e)
				{
					failed.incrementAndGet();
					System.err.println(file + ": " + e.getMessage());
				}
			});

		long elapsed = System.currentTimeMillis() - startTime;
		System.out.println("\n=== VALIDATION RESULTS ===");
		System.out.println("Total: " + total.get());
		System.out.println("Success: " + success.get());
		System.out.println("Failed: " + failed.get());
		System.out.println("Time: " + elapsed + "ms");
		System.out.println("Throughput: " + String.format("%.1f", (total.get() * 1000.0 / elapsed)) + " files/sec");
		System.out.println("Success rate: " + String.format("%.2f", (success.get() * 100.0 / total.get())) + "%");

		// This test should pass with 0 failures
		if (failed.get() > 0)
			throw new AssertionError("Failed to parse " + failed.get() + " files");
	}
}
