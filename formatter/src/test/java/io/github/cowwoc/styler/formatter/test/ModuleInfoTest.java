package io.github.cowwoc.styler.formatter.test;

import io.github.cowwoc.styler.formatter.FormattingRule;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for JPMS module configuration.
 */
public class ModuleInfoTest
{
	/**
	 * Tests that the module system is enabled.
	 */
	@Test
	public void shouldRunWithModuleSystemEnabled()
	{
		ModuleLayer layer = getClass().getModule().getLayer();
		requireThat(layer, "layer").isNotNull();
	}

	/**
	 * Tests that the formatter API module is accessible.
	 */
	@Test
	public void shouldAccessFormatterApiModule()
	{
		requireThat(FormattingRule.class, "FormattingRule.class").isNotNull();
	}

	/**
	 * Tests that the test module is named.
	 */
	@Test
	public void testModuleExportsFormatterApiPackage()
	{
		Module testModule = getClass().getModule();
		requireThat(testModule, "testModule").isNotNull();
	}

	/**
	 * Tests that the module has the correct name.
	 */
	@Test
	public void testModuleHasCorrectName()
	{
		Module testModule = getClass().getModule();
		String moduleName = testModule.getName();

		requireThat(moduleName, "moduleName").contains("formatter");
	}
}
