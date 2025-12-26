package io.github.cowwoc.styler.benchmarks.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Generates synthetic Java source code for benchmarking.
 *
 * This class creates reproducible, size-configurable Java code samples ranging from small (100 tokens)
 * to large (10000+ tokens) to test parser and formatter performance across different code complexity
 * levels. All generation uses a fixed seed for reproducibility.
 */
public class SampleCodeGenerator
{
	/**
	 * Supported code sizes.
	 */
	public enum Size
	{
		/**
		 * Small code sample (~100 tokens).
		 */
		SMALL(100),

		/**
		 * Medium code sample (~1000 tokens).
		 */
		MEDIUM(1000),

		/**
		 * Large code sample (~10000 tokens).
		 */
		LARGE(10000);

		private final int expectedTokens;

		Size(int expectedTokens)
		{
			this.expectedTokens = expectedTokens;
		}

		/**
		 * Returns the expected token count for this size category.
		 *
		 * @return expected token count
		 */
		public int getExpectedTokens()
		{
			return expectedTokens;
		}
	}

	private static final int FIXED_SEED = 42;
	private static final String[] PRIMITIVE_TYPES = {"int", "long", "float", "double", "boolean", "String"};
	private static final String[] METHOD_MODIFIERS = {"public", "private", "protected", "static", "final"};

	/**
	 * Generates a small Java source file (~100 tokens).
	 *
	 * @return generated source code
	 */
	public static String generateSmallFile()
	{
		return generateFile(Size.SMALL);
	}

	/**
	 * Generates a medium Java source file (~1000 tokens).
	 *
	 * @return generated source code
	 */
	public static String generateMediumFile()
	{
		return generateFile(Size.MEDIUM);
	}

	/**
	 * Generates a large Java source file (~10000 tokens).
	 *
	 * @return generated source code
	 */
	public static String generateLargeFile()
	{
		return generateFile(Size.LARGE);
	}

	/**
	 * Generates a Java source file of specified size.
	 *
	 * @param size desired code size category
	 * @return generated source code
	 */
	public static String generateFile(Size size)
	{
		requireThat(size, "size").isNotNull();
		Random random = new Random(FIXED_SEED);
		StringBuilder code = new StringBuilder();

		// Package declaration
		code.append("package com.example;\n\n");

		// Imports
		code.append("import java.util.*;\n");
		code.append("import java.io.*;\n");
		code.append("import java.nio.file.*;\n\n");

		// Class declaration
		code.append("public class SampleClass\n");
		code.append("{\n");

		// Add methods based on size
		int methodCount = switch (size)
		{
			case SMALL -> 3;
			case MEDIUM -> 10;
			case LARGE -> 30;
		};

		for (int i = 0; i < methodCount; ++i)
		{
			generateMethod(code, random, i);
		}

		code.append("}\n");
		return code.toString();
	}

	/**
	 * Generates multiple Java source files of specified size.
	 *
	 * @param count number of files to generate
	 * @param size desired code size for each file
	 * @return list of generated source codes
	 */
	public static List<String> generateFiles(int count, Size size)
	{
		requireThat(count, "count").isPositive();
		requireThat(size, "size").isNotNull();

		List<String> files = new ArrayList<>();
		Random random = new Random(FIXED_SEED);

		for (int i = 0; i < count; ++i)
		{
			StringBuilder code = new StringBuilder();
			code.append("package com.example.gen").append(i).append(";\n\n");

			// Imports
			code.append("import java.util.*;\n");
			code.append("import java.io.*;\n\n");

			// Class declaration
			code.append("public class SampleClass").append(i).append("\n");
			code.append("{\n");

			int methodCount = switch (size)
			{
				case SMALL -> 3;
				case MEDIUM -> 10;
				case LARGE -> 30;
			};

			for (int j = 0; j < methodCount; ++j)
			{
				generateMethod(code, random, j);
			}

			code.append("}\n");
			files.add(code.toString());
		}

		return files;
	}

	private static void generateMethod(StringBuilder code, Random random, int index)
	{
		code.append("\t");
		code.append(METHOD_MODIFIERS[random.nextInt(METHOD_MODIFIERS.length)]).append(" ");
		code.append(PRIMITIVE_TYPES[random.nextInt(PRIMITIVE_TYPES.length)]).append(" ");
		code.append("method").append(index).append("(");

		// Method parameters
		int paramCount = random.nextInt(3);
		for (int i = 0; i < paramCount; ++i)
		{
			if (i > 0)
				code.append(", ");
			code.append(PRIMITIVE_TYPES[random.nextInt(PRIMITIVE_TYPES.length)]);
			code.append(" param").append(i);
		}
		code.append(")\n");
		code.append("\t{\n");

		// Method body with some statements
		int statements = random.nextInt(5) + 2;
		for (int i = 0; i < statements; ++i)
		{
			code.append("\t\tint variable").append(i).append(" = ").append(random.nextInt(100)).append(";\n");
		}

		code.append("\t\treturn variable0;\n");
		code.append("\t}\n\n");
	}
}
