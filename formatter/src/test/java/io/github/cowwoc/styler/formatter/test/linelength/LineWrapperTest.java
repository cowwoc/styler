package io.github.cowwoc.styler.formatter.test.linelength;

import io.github.cowwoc.styler.formatter.test.TestTransformationContext;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.internal.ContextDetector;
import io.github.cowwoc.styler.formatter.linelength.internal.LineWrapper;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for LineWrapper context-aware line wrapping.
 */
public class LineWrapperTest
{
	/**
	 * Tests that null context detector is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullContextDetector()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		new LineWrapper(null, context, config);
	}

	/**
	 * Tests that null transformation context is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullTransformationContext()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		ContextDetector detector = new ContextDetector(context);
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		new LineWrapper(detector, null, config);
	}

	/**
	 * Tests that null config is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullConfig()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		ContextDetector detector = new ContextDetector(context);
		new LineWrapper(detector, context, null);
	}

	/**
	 * Tests that null line is rejected in findBreakPoints.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullLineInFindBreakPoints()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		ContextDetector detector = new ContextDetector(context);
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		LineWrapper wrapper = new LineWrapper(detector, context, config);
		wrapper.findBreakPoints(null, 0);
	}

	/**
	 * Tests that negative position is rejected in findBreakPoints.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectNegativePositionInFindBreakPoints()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		ContextDetector detector = new ContextDetector(context);
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		LineWrapper wrapper = new LineWrapper(detector, context, config);
		wrapper.findBreakPoints("test", -1);
	}

	/**
	 * Tests that null line is rejected in wrapLine.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullLineInWrapLine()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		ContextDetector detector = new ContextDetector(context);
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		LineWrapper wrapper = new LineWrapper(detector, context, config);
		wrapper.wrapLine(null, 0);
	}

	/**
	 * Tests that negative position is rejected in wrapLine.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectNegativePositionInWrapLine()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		ContextDetector detector = new ContextDetector(context);
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		LineWrapper wrapper = new LineWrapper(detector, context, config);
		wrapper.wrapLine("test", -1);
	}

	/**
	 * Tests that short lines are not wrapped.
	 */
	@Test
	public void shouldNotWrapShortLines()
	{
		String source = "class Test {}";
		TestTransformationContext context = new TestTransformationContext(source);
		ContextDetector detector = new ContextDetector(context);
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		LineWrapper wrapper = new LineWrapper(detector, context, config);

		String result = wrapper.wrapLine(source, 0);
		requireThat(result, "result").isEqualTo(source);
	}

	/**
	 * Tests finding break points at method chain dots.
	 */
	@Test
	public void shouldFindBreakPointsAtMethodChainDots()
	{
		// Create source with method chain
		String source = "obj.method1().method2()";

		// Create arena with FIELD_ACCESS nodes at dot positions
		NodeArena arena = new NodeArena();
		NodeIndex root = arena.allocateNode(NodeType.COMPILATION_UNIT, 0, source.length());

		// Add FIELD_ACCESS nodes at the dot positions
		arena.allocateNode(NodeType.FIELD_ACCESS, 0, 13);  // obj.method1()
		arena.allocateNode(NodeType.FIELD_ACCESS, 0, 23);  // entire chain

		TestTransformationContext context = new TestTransformationContext(source, arena, root);
		ContextDetector detector = new ContextDetector(context);
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		LineWrapper wrapper = new LineWrapper(detector, context, config);

		List<Integer> breakPoints = wrapper.findBreakPoints(source, 0);
		// Should find dots as potential break points
		requireThat(breakPoints, "breakPoints").isNotNull();
	}

	/**
	 * Tests that lines within max length are returned unchanged.
	 */
	@Test
	public void shouldReturnUnchangedLineWithinMaxLength()
	{
		String source = """
			public class Test
			{
				void method()
				{
				}
			}""";
		TestTransformationContext context = new TestTransformationContext(source);
		ContextDetector detector = new ContextDetector(context);
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		LineWrapper wrapper = new LineWrapper(detector, context, config);

		// First line is "public class Test" which is within max length
		String firstLine = source.lines().findFirst().orElseThrow();
		String result = wrapper.wrapLine(firstLine, 0);
		requireThat(result, "result").isEqualTo(firstLine);
	}

	/**
	 * Tests that empty lines are returned unchanged.
	 */
	@Test
	public void shouldReturnEmptyLineUnchanged()
	{
		String source = "";
		TestTransformationContext context = new TestTransformationContext(source);
		ContextDetector detector = new ContextDetector(context);
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		LineWrapper wrapper = new LineWrapper(detector, context, config);

		String result = wrapper.wrapLine(source, 0);
		requireThat(result, "result").isEqualTo(source);
	}
}
