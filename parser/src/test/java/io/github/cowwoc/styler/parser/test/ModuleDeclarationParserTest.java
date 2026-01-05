package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.ExportsDirectiveAttribute;
import io.github.cowwoc.styler.ast.core.ModuleDeclarationAttribute;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.OpensDirectiveAttribute;
import io.github.cowwoc.styler.ast.core.ProvidesDirectiveAttribute;
import io.github.cowwoc.styler.ast.core.RequiresDirectiveAttribute;
import io.github.cowwoc.styler.ast.core.UsesDirectiveAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing module-info.java module declarations and directives.
 */
public final class ModuleDeclarationParserTest
{
	// ==================== Phase 1: Core Module Declaration Tests ====================

	/**
	 * Validates parsing of a minimal module declaration with empty body.
	 * Tests the fundamental module syntax: {@code module name { }}.
	 */
	@Test
	public void shouldParseMinimalModuleDeclaration()
	{
		// Character positions:
		// "module com.example.app\n" = 0-22 (23 chars)
		// "{\n" = 23-24 (2 chars)
		// "}\n" = 25-26 (2 chars, end at 27)
		String source = """
			module com.example.app
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "com.example.app" at positions 7-22
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 22);
			expected.allocateModuleDeclaration(0, 26, new ModuleDeclarationAttribute("com.example.app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a module with single-segment name.
	 * Tests that module names can be simple identifiers without dots.
	 */
	@Test
	public void shouldParseSingleSegmentModuleName()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "}\n" = 13-14 (2 chars, end at 15)
		String source = """
			module app
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app" at positions 7-10
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			expected.allocateModuleDeclaration(0, 14, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a module with multi-segment qualified name.
	 * Tests that module names with multiple dot-separated segments are correctly parsed.
	 */
	@Test
	public void shouldParseMultiSegmentModuleName()
	{
		// Character positions:
		// "module com.example.myapp.core.api\n" = 0-33 (34 chars)
		// "{\n" = 34-35 (2 chars)
		// "}\n" = 36-37 (2 chars, end at 38)
		String source = """
			module com.example.myapp.core.api
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "com.example.myapp.core.api" at positions 7-33
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 33);
			expected.allocateModuleDeclaration(0, 37,
				new ModuleDeclarationAttribute("com.example.myapp.core.api", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Phase 2: Requires Directive Tests ====================

	/**
	 * Validates parsing of a basic requires directive.
	 * Tests the fundamental dependency declaration without modifiers.
	 */
	@Test
	public void shouldParseBasicRequires()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\trequires java.base;\n" = 13-33 (21 chars, requires starts at 14)
		// "}\n" = 34-35
		String source = """
			module app
			{
				requires java.base;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app" at positions 7-10
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "java.base" at positions 23-32
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 32);
			// "requires java.base;" is from position 14 to 33 (semicolon at 32)
			expected.allocateRequiresDirective(14, 33, new RequiresDirectiveAttribute("java.base", false, false));
			expected.allocateModuleDeclaration(0, 35, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a requires directive with transitive modifier.
	 * Transitive dependencies are re-exported to dependent modules.
	 */
	@Test
	public void shouldParseTransitiveRequires()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\trequires transitive java.sql;\n" = 13-43 (31 chars, requires starts at 14)
		// "}\n" = 44-45
		String source = """
			module app
			{
				requires transitive java.sql;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app" at positions 7-10
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "java.sql" at positions 34-42
			expected.allocateNode(NodeType.QUALIFIED_NAME, 34, 42);
			// "requires transitive java.sql;" ends with semicolon at position 42
			expected.allocateRequiresDirective(14, 43, new RequiresDirectiveAttribute("java.sql", true, false));
			expected.allocateModuleDeclaration(0, 45, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a requires directive with static modifier.
	 * Static dependencies are required only at compile time.
	 */
	@Test
	public void shouldParseStaticRequires()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\trequires static java.compiler;\n" = 13-44 (32 chars, requires starts at 14)
		// "}\n" = 45-46
		String source = """
			module app
			{
				requires static java.compiler;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app" at positions 7-10
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "java.compiler" at positions 30-43
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 43);
			// "requires static java.compiler;" ends at position 44
			expected.allocateRequiresDirective(14, 44, new RequiresDirectiveAttribute("java.compiler", false, true));
			expected.allocateModuleDeclaration(0, 46, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a requires directive with both transitive and static modifiers.
	 * Combined modifiers create transitive compile-time-only dependencies.
	 */
	@Test
	public void shouldParseCombinedModifiersRequires()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\trequires transitive static java.logging;\n" = 13-54 (42 chars)
		// "}\n" = 55-56
		String source = """
			module app
			{
				requires transitive static java.logging;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app" at positions 7-10
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "java.logging" at positions 41-53
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 53);
			expected.allocateRequiresDirective(14, 54, new RequiresDirectiveAttribute("java.logging", true, true));
			expected.allocateModuleDeclaration(0, 56, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of multiple requires directives.
	 * Tests that multiple dependency declarations are correctly parsed in sequence.
	 */
	@Test
	public void shouldParseMultipleRequires()
	{
		// Character positions (with tabs as 1 char):
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\trequires java.base;\n" = 13-33 (21 chars)
		// "\trequires java.sql;\n" = 34-53 (20 chars)
		// "\trequires java.logging;\n" = 54-77 (24 chars)
		// "}\n" = 78-79
		String source = """
			module app
			{
				requires java.base;
				requires java.sql;
				requires java.logging;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "java.base"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 32);
			expected.allocateRequiresDirective(14, 33, new RequiresDirectiveAttribute("java.base", false, false));
			// QUALIFIED_NAME for "java.sql"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 52);
			expected.allocateRequiresDirective(35, 53, new RequiresDirectiveAttribute("java.sql", false, false));
			// QUALIFIED_NAME for "java.logging"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 64, 76);
			expected.allocateRequiresDirective(55, 77, new RequiresDirectiveAttribute("java.logging", false, false));
			expected.allocateModuleDeclaration(0, 79, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Phase 3: Exports Directive Tests ====================

	/**
	 * Validates parsing of an unqualified exports directive.
	 * Unqualified exports make a package accessible to all modules.
	 */
	@Test
	public void shouldParseUnqualifiedExports()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\texports com.example.api;\n" = 13-38 (26 chars)
		// "}\n" = 39-40
		String source = """
			module app
			{
				exports com.example.api;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "com.example.api"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 37);
			expected.allocateExportsDirective(14, 38,
				new ExportsDirectiveAttribute("com.example.api", List.of()));
			expected.allocateModuleDeclaration(0, 40, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a qualified exports directive to a single target module.
	 * Qualified exports restrict package visibility to specific modules.
	 */
	@Test
	public void shouldParseQualifiedExportsSingleTarget()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\texports com.example.internal to com.example.test;\n" = 13-63 (51 chars)
		// "}\n" = 64-65
		String source = """
			module app
			{
				exports com.example.internal to com.example.test;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "com.example.internal"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 42);
			// QUALIFIED_NAME for "com.example.test"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 62);
			expected.allocateExportsDirective(14, 63,
				new ExportsDirectiveAttribute("com.example.internal", List.of("com.example.test")));
			expected.allocateModuleDeclaration(0, 65, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a qualified exports directive to multiple target modules.
	 * Tests that comma-separated module lists are correctly parsed.
	 */
	@Test
	public void shouldParseQualifiedExportsMultipleTargets()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\texports com.example.internal to framework, testlib, integration;\n" = 13-78 (66 chars)
		// "}\n" = 79-80
		String source = """
			module app
			{
				exports com.example.internal to framework, testlib, integration;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "com.example.internal"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 42);
			// QUALIFIED_NAME for "framework"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 55);
			// QUALIFIED_NAME for "testlib"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 64);
			// QUALIFIED_NAME for "integration"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 66, 77);
			expected.allocateExportsDirective(14, 78,
				new ExportsDirectiveAttribute("com.example.internal", List.of("framework", "testlib", "integration")));
			expected.allocateModuleDeclaration(0, 80, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of multiple exports directives.
	 * Tests that multiple package exports are correctly parsed in sequence.
	 */
	@Test
	public void shouldParseMultipleExports()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\texports com.example.api;\n" = 13-38 (26 chars)
		// "\texports com.example.spi;\n" = 39-64 (26 chars)
		// "\texports com.example.internal to testlib;\n" = 65-106 (42 chars)
		// "}\n" = 107-108
		String source = """
			module app
			{
				exports com.example.api;
				exports com.example.spi;
				exports com.example.internal to testlib;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "com.example.api"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 37);
			expected.allocateExportsDirective(14, 38,
				new ExportsDirectiveAttribute("com.example.api", List.of()));
			// QUALIFIED_NAME for "com.example.spi"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 63);
			expected.allocateExportsDirective(40, 64,
				new ExportsDirectiveAttribute("com.example.spi", List.of()));
			// QUALIFIED_NAME for "com.example.internal"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 74, 94);
			// QUALIFIED_NAME for "testlib"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 98, 105);
			expected.allocateExportsDirective(66, 106,
				new ExportsDirectiveAttribute("com.example.internal", List.of("testlib")));
			expected.allocateModuleDeclaration(0, 108, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Phase 4: Opens Directive Tests ====================

	/**
	 * Validates parsing of an unqualified opens directive.
	 * Unqualified opens grants reflection access to all modules.
	 */
	@Test
	public void shouldParseUnqualifiedOpens()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\topens com.example.internal;\n" = 13-41 (29 chars)
		// "}\n" = 42-43
		String source = """
			module app
			{
				opens com.example.internal;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "com.example.internal"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 20, 40);
			expected.allocateOpensDirective(14, 41,
				new OpensDirectiveAttribute("com.example.internal", List.of()));
			expected.allocateModuleDeclaration(0, 43, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a qualified opens directive to a single target module.
	 * Qualified opens restricts reflection access to specific modules.
	 */
	@Test
	public void shouldParseQualifiedOpensSingleTarget()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\topens com.example.impl to framework;\n" = 13-50 (38 chars)
		// "}\n" = 51-52
		String source = """
			module app
			{
				opens com.example.impl to framework;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "com.example.impl"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 20, 36);
			// QUALIFIED_NAME for "framework"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 49);
			expected.allocateOpensDirective(14, 50,
				new OpensDirectiveAttribute("com.example.impl", List.of("framework")));
			expected.allocateModuleDeclaration(0, 52, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a qualified opens directive to multiple target modules.
	 * Tests that comma-separated module lists are correctly parsed.
	 */
	@Test
	public void shouldParseQualifiedOpensMultipleTargets()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\topens com.example.impl to framework, testlib;\n" = 13-59 (47 chars)
		// "}\n" = 60-61
		String source = """
			module app
			{
				opens com.example.impl to framework, testlib;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "com.example.impl"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 20, 36);
			// QUALIFIED_NAME for "framework"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 49);
			// QUALIFIED_NAME for "testlib"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 51, 58);
			expected.allocateOpensDirective(14, 59,
				new OpensDirectiveAttribute("com.example.impl", List.of("framework", "testlib")));
			expected.allocateModuleDeclaration(0, 61, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Phase 5: Uses and Provides Directive Tests ====================

	/**
	 * Validates parsing of a uses directive.
	 * Uses declares that this module consumes a service interface.
	 */
	@Test
	public void shouldParseUsesDirective()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\tuses com.example.spi.Service;\n" = 13-43 (31 chars)
		// "}\n" = 44-45
		String source = """
			module app
			{
				uses com.example.spi.Service;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "com.example.spi.Service"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 19, 42);
			expected.allocateUsesDirective(14, 43,
				new UsesDirectiveAttribute("com.example.spi.Service"));
			expected.allocateModuleDeclaration(0, 45, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of multiple uses directives.
	 * Tests that a module can consume multiple service interfaces.
	 */
	@Test
	public void shouldParseMultipleUses()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\tuses com.example.spi.Service;\n" = 13-43 (31 chars)
		// "\tuses com.example.spi.Provider;\n" = 44-75 (32 chars)
		// "}\n" = 76-77
		String source = """
			module app
			{
				uses com.example.spi.Service;
				uses com.example.spi.Provider;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "com.example.spi.Service"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 19, 42);
			expected.allocateUsesDirective(14, 43,
				new UsesDirectiveAttribute("com.example.spi.Service"));
			// QUALIFIED_NAME for "com.example.spi.Provider"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 74);
			expected.allocateUsesDirective(45, 75,
				new UsesDirectiveAttribute("com.example.spi.Provider"));
			expected.allocateModuleDeclaration(0, 77, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a provides directive with a single implementation.
	 * Provides declares that this module provides a service implementation.
	 */
	@Test
	public void shouldParseProvidesWithSingleImplementation()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\tprovides com.example.spi.Service with com.example.impl.ServiceImpl;\n" = 13-82 (70 chars)
		// "}\n" = 83-84
		String source = """
			module app
			{
				provides com.example.spi.Service with com.example.impl.ServiceImpl;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "com.example.spi.Service"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 46);
			// QUALIFIED_NAME for "com.example.impl.ServiceImpl"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 52, 80);
			expected.allocateProvidesDirective(14, 81,
				new ProvidesDirectiveAttribute("com.example.spi.Service", List.of("com.example.impl.ServiceImpl")));
			expected.allocateModuleDeclaration(0, 83, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a provides directive with multiple implementations.
	 * Tests that comma-separated implementation lists are correctly parsed.
	 */
	@Test
	public void shouldParseProvidesWithMultipleImplementations()
	{
		// Character positions:
		// "module app\n" = 0-10 (11 chars)
		// "{\n" = 11-12 (2 chars)
		// "\tprovides Service with Impl1, Impl2, Impl3;\n" = 13-56 (44 chars)
		// "}\n" = 57-58
		String source = """
			module app
			{
				provides Service with Impl1, Impl2, Impl3;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "Service"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 23, 30);
			// QUALIFIED_NAME for "Impl1"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 36, 41);
			// QUALIFIED_NAME for "Impl2"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 48);
			// QUALIFIED_NAME for "Impl3"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 55);
			expected.allocateProvidesDirective(14, 56,
				new ProvidesDirectiveAttribute("Service", List.of("Impl1", "Impl2", "Impl3")));
			expected.allocateModuleDeclaration(0, 58, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Phase 6: Integration and Real-World Tests ====================

	/**
	 * Validates parsing of a complete module with all directive types.
	 * Tests that a module can contain requires, exports, opens, uses, and provides directives.
	 */
	@Test
	public void shouldParseCompleteModuleWithAllDirectives()
	{
		String source = """
			module com.example.app
			{
				requires java.base;
				requires transitive java.sql;
				exports com.example.api;
				opens com.example.internal to framework;
				uses com.example.spi.Service;
				provides com.example.spi.Service with com.example.impl.ServiceImpl;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "com.example.app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 22);
			// QUALIFIED_NAME for "java.base"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 35, 44);
			expected.allocateRequiresDirective(26, 45, new RequiresDirectiveAttribute("java.base", false, false));
			// QUALIFIED_NAME for "java.sql"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 67, 75);
			expected.allocateRequiresDirective(47, 76, new RequiresDirectiveAttribute("java.sql", true, false));
			// QUALIFIED_NAME for "com.example.api"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 86, 101);
			expected.allocateExportsDirective(78, 102, new ExportsDirectiveAttribute("com.example.api", List.of()));
			// QUALIFIED_NAME for "com.example.internal"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 110, 130);
			// QUALIFIED_NAME for "framework"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 134, 143);
			expected.allocateOpensDirective(104, 144,
				new OpensDirectiveAttribute("com.example.internal", List.of("framework")));
			// QUALIFIED_NAME for "com.example.spi.Service" (uses)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 151, 174);
			expected.allocateUsesDirective(146, 175, new UsesDirectiveAttribute("com.example.spi.Service"));
			// QUALIFIED_NAME for "com.example.spi.Service" (provides)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 186, 209);
			// QUALIFIED_NAME for "com.example.impl.ServiceImpl"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 215, 243);
			expected.allocateProvidesDirective(177, 244,
				new ProvidesDirectiveAttribute("com.example.spi.Service", List.of("com.example.impl.ServiceImpl")));
			expected.allocateModuleDeclaration(0, 246, new ModuleDeclarationAttribute("com.example.app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of directives in arbitrary order.
	 * Tests that the parser does not enforce directive ordering as per JLS.
	 */
	@Test
	public void shouldParseDirectivesInArbitraryOrder()
	{
		String source = """
			module app
			{
				exports api;
				requires base;
				provides Service with Impl;
				opens internal;
				uses Provider;
				requires transitive sql;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "app"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 10);
			// QUALIFIED_NAME for "api"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 22, 25);
			expected.allocateExportsDirective(14, 26, new ExportsDirectiveAttribute("api", List.of()));
			// QUALIFIED_NAME for "base"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 41);
			expected.allocateRequiresDirective(28, 42, new RequiresDirectiveAttribute("base", false, false));
			// QUALIFIED_NAME for "Service"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 53, 60);
			// QUALIFIED_NAME for "Impl"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 66, 70);
			expected.allocateProvidesDirective(44, 71, new ProvidesDirectiveAttribute("Service", List.of("Impl")));
			// QUALIFIED_NAME for "internal"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 79, 87);
			expected.allocateOpensDirective(73, 88, new OpensDirectiveAttribute("internal", List.of()));
			// QUALIFIED_NAME for "Provider"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 95, 103);
			expected.allocateUsesDirective(90, 104, new UsesDirectiveAttribute("Provider"));
			// QUALIFIED_NAME for "sql"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 126, 129);
			expected.allocateRequiresDirective(106, 130, new RequiresDirectiveAttribute("sql", true, false));
			expected.allocateModuleDeclaration(0, 132, new ModuleDeclarationAttribute("app", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a JDK-style module like java.sql.
	 * Tests that the parser correctly handles real JDK module patterns.
	 */
	@Test
	public void shouldParseJdkStyleModule()
	{
		String source = """
			module java.sql
			{
				requires transitive java.logging;
				requires transitive java.xml;
				exports java.sql;
				exports javax.sql;
				uses java.sql.Driver;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "java.sql" (module name)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 15);
			// QUALIFIED_NAME for "java.logging"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 51);
			expected.allocateRequiresDirective(19, 52, new RequiresDirectiveAttribute("java.logging", true, false));
			// QUALIFIED_NAME for "java.xml"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 74, 82);
			expected.allocateRequiresDirective(54, 83, new RequiresDirectiveAttribute("java.xml", true, false));
			// QUALIFIED_NAME for "java.sql" (exports package)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 93, 101);
			expected.allocateExportsDirective(85, 102, new ExportsDirectiveAttribute("java.sql", List.of()));
			// QUALIFIED_NAME for "javax.sql"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 112, 121);
			expected.allocateExportsDirective(104, 122, new ExportsDirectiveAttribute("javax.sql", List.of()));
			// QUALIFIED_NAME for "java.sql.Driver"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 129, 144);
			expected.allocateUsesDirective(124, 145, new UsesDirectiveAttribute("java.sql.Driver"));
			expected.allocateModuleDeclaration(0, 147, new ModuleDeclarationAttribute("java.sql", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a framework-style module with service providers.
	 * Tests the common pattern used by plugin-based frameworks.
	 */
	@Test
	public void shouldParseFrameworkModuleWithServiceProviders()
	{
		String source = """
			module framework
			{
				requires java.base;
				exports framework.spi;
				uses framework.spi.Plugin;
				uses framework.spi.Extension;
				provides framework.spi.Plugin with framework.internal.CorePlugin;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// QUALIFIED_NAME for "framework" (module name)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 7, 16);
			// QUALIFIED_NAME for "java.base"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 38);
			expected.allocateRequiresDirective(20, 39, new RequiresDirectiveAttribute("java.base", false, false));
			// QUALIFIED_NAME for "framework.spi"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 62);
			expected.allocateExportsDirective(41, 63, new ExportsDirectiveAttribute("framework.spi", List.of()));
			// QUALIFIED_NAME for "framework.spi.Plugin"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 70, 90);
			expected.allocateUsesDirective(65, 91, new UsesDirectiveAttribute("framework.spi.Plugin"));
			// QUALIFIED_NAME for "framework.spi.Extension"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 98, 121);
			expected.allocateUsesDirective(93, 122, new UsesDirectiveAttribute("framework.spi.Extension"));
			// QUALIFIED_NAME for "framework.spi.Plugin" (provides)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 133, 153);
			// QUALIFIED_NAME for "framework.internal.CorePlugin"
			expected.allocateNode(NodeType.QUALIFIED_NAME, 159, 188);
			expected.allocateProvidesDirective(124, 189,
				new ProvidesDirectiveAttribute("framework.spi.Plugin", List.of("framework.internal.CorePlugin")));
			expected.allocateModuleDeclaration(0, 191, new ModuleDeclarationAttribute("framework", false));
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Phase 7: Error Handling Tests ====================

	/**
	 * Validates that a module declaration with a missing semicolon is rejected.
	 * The syntax {@code requires java.base} without semicolon is invalid.
	 */
	@Test
	public void shouldRejectModuleWithMissingSemicolon()
	{
		assertParseFails("""
			module app
			{
				requires java.base
			}
			""");
	}

	/**
	 * Validates that a module declaration with a missing name is rejected.
	 * The syntax {@code module { }} without a module name is invalid.
	 */
	@Test
	public void shouldRejectModuleWithMissingName()
	{
		assertParseFails("""
			module
			{
			}
			""");
	}

	/**
	 * Validates that an exports directive with a missing package name is rejected.
	 * The syntax {@code exports ;} without a package name is invalid.
	 */
	@Test
	public void shouldRejectExportsWithMissingPackage()
	{
		assertParseFails("""
			module app
			{
				exports ;
			}
			""");
	}

	/**
	 * Validates that a provides directive with a missing {@code with} clause is rejected.
	 * The syntax {@code provides Service;} without implementations is invalid.
	 */
	@Test
	public void shouldRejectProvidesWithMissingWith()
	{
		assertParseFails("""
			module app
			{
				provides Service;
			}
			""");
	}

	/**
	 * Validates that a module declaration with an invalid name is rejected.
	 * Module names must follow Java identifier rules.
	 */
	@Test
	public void shouldRejectInvalidModuleName()
	{
		assertParseFails("""
			module 123invalid
			{
			}
			""");
	}
}
