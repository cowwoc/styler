package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.node.AnnotationDeclarationNode;
import io.github.cowwoc.styler.ast.node.CompactMainMethodNode;
import io.github.cowwoc.styler.ast.node.FlexibleConstructorBodyNode;
import io.github.cowwoc.styler.ast.node.InstanceMainMethodNode;
import io.github.cowwoc.styler.ast.node.ModuleDeclarationNode;
import io.github.cowwoc.styler.ast.node.UnnamedClassNode;
import io.github.cowwoc.styler.formatter.impl.NodeCategory;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NodeCategory}.
 * <p>
 * Validates node categorization for brace formatting rules, ensuring all AST node types handled by
 * BraceNodeCollector are correctly mapped to formatting categories. Includes bug reproduction tests
 * for previously missing node types (modules, annotations, unnamed classes, flexible constructor bodies,
 * compact main methods, and instance main methods).
 * <p>
 * <strong>Thread Safety:</strong> All tests are thread-safe and support parallel execution.
 */
public final class NodeCategoryTest
{
	/**
	 * Verifies that ModuleDeclarationNode categorizes as CLASS_DECLARATION.
	 * <p>
	 * Bug Reproduction: Previously threw IllegalArgumentException when BraceFormatterFormattingRule
	 * processed module-info.java files because NodeCategory.categorize() did not recognize
	 * ModuleDeclarationNode.
	 */
	@Test
	public void categorizeModuleDeclarationNodeReturnsClassDeclaration()
	{
		ModuleDeclarationNode moduleNode = createMinimalModuleDeclarationNode();

		NodeCategory category = NodeCategory.categorize(moduleNode);

		assertThat(category).isEqualTo(NodeCategory.CLASS_DECLARATION);
	}

	/**
	 * Verifies that AnnotationDeclarationNode categorizes as CLASS_DECLARATION.
	 * <p>
	 * Bug Reproduction: Previously threw IllegalArgumentException when BraceFormatterFormattingRule
	 * processed annotation type declarations because NodeCategory.categorize() did not recognize
	 * AnnotationDeclarationNode.
	 */
	@Test
	public void categorizeAnnotationDeclarationNodeReturnsClassDeclaration()
	{
		AnnotationDeclarationNode annotationNode = createMinimalAnnotationDeclarationNode();

		NodeCategory category = NodeCategory.categorize(annotationNode);

		assertThat(category).isEqualTo(NodeCategory.CLASS_DECLARATION);
	}

	/**
	 * Verifies that UnnamedClassNode categorizes as CLASS_DECLARATION.
	 * <p>
	 * Bug Reproduction: Previously threw IllegalArgumentException when BraceFormatterFormattingRule
	 * processed unnamed class declarations (JEP 445) because NodeCategory.categorize() did not recognize
	 * UnnamedClassNode.
	 */
	@Test
	public void categorizeUnnamedClassNodeReturnsClassDeclaration()
	{
		UnnamedClassNode unnamedClassNode = createMinimalUnnamedClassNode();

		NodeCategory category = NodeCategory.categorize(unnamedClassNode);

		assertThat(category).isEqualTo(NodeCategory.CLASS_DECLARATION);
	}

	/**
	 * Verifies that FlexibleConstructorBodyNode categorizes as CONSTRUCTOR_DECLARATION.
	 * <p>
	 * Bug Reproduction: Previously threw IllegalArgumentException when BraceFormatterFormattingRule
	 * processed flexible constructor bodies (JEP 482) because NodeCategory.categorize() did not recognize
	 * FlexibleConstructorBodyNode.
	 */
	@Test
	public void categorizeFlexibleConstructorBodyNodeReturnsConstructorDeclaration()
	{
		FlexibleConstructorBodyNode flexibleConstructorNode = createMinimalFlexibleConstructorBodyNode();

		NodeCategory category = NodeCategory.categorize(flexibleConstructorNode);

		assertThat(category).isEqualTo(NodeCategory.CONSTRUCTOR_DECLARATION);
	}

	/**
	 * Verifies that CompactMainMethodNode categorizes as METHOD_DECLARATION.
	 * <p>
	 * Bug Reproduction: Previously threw IllegalArgumentException when BraceFormatterFormattingRule
	 * processed compact main methods (JEP 477) because NodeCategory.categorize() did not recognize
	 * CompactMainMethodNode.
	 */
	@Test
	public void categorizeCompactMainMethodNodeReturnsMethodDeclaration()
	{
		CompactMainMethodNode compactMainNode = createMinimalCompactMainMethodNode();

		NodeCategory category = NodeCategory.categorize(compactMainNode);

		assertThat(category).isEqualTo(NodeCategory.METHOD_DECLARATION);
	}

	/**
	 * Verifies that InstanceMainMethodNode categorizes as METHOD_DECLARATION.
	 * <p>
	 * Bug Reproduction: Previously threw IllegalArgumentException when BraceFormatterFormattingRule
	 * processed instance main methods (JEP 445) because NodeCategory.categorize() did not recognize
	 * InstanceMainMethodNode.
	 */
	@Test
	public void categorizeInstanceMainMethodNodeReturnsMethodDeclaration()
	{
		InstanceMainMethodNode instanceMainNode = createMinimalInstanceMainMethodNode();

		NodeCategory category = NodeCategory.categorize(instanceMainNode);

		assertThat(category).isEqualTo(NodeCategory.METHOD_DECLARATION);
	}

	/**
	 * Verifies that MODULE_DECLARATION categorization is consistent with other structural declarations.
	 */
	@Test
	public void moduleDeclarationCategoryConsistentWithOtherStructuralDeclarations()
	{
		// Verify that modules use the same category as annotations and unnamed classes
		ModuleDeclarationNode moduleNode = createMinimalModuleDeclarationNode();
		AnnotationDeclarationNode annotationNode = createMinimalAnnotationDeclarationNode();
		UnnamedClassNode unnamedClassNode = createMinimalUnnamedClassNode();

		NodeCategory moduleCategory = NodeCategory.categorize(moduleNode);
		NodeCategory annotationCategory = NodeCategory.categorize(annotationNode);
		NodeCategory unnamedClassCategory = NodeCategory.categorize(unnamedClassNode);

		assertThat(moduleCategory).isEqualTo(annotationCategory);
		assertThat(moduleCategory).isEqualTo(unnamedClassCategory);
		assertThat(moduleCategory).isEqualTo(NodeCategory.CLASS_DECLARATION);
	}

	/**
	 * Verifies that CLASS_DECLARATION category maps to "class" configuration key.
	 */
	@Test
	public void classDeclarationCategoryMapsToClassConfigurationKey()
	{
		String configKey = NodeCategory.CLASS_DECLARATION.getConfigurationKey();

		assertThat(configKey).isEqualTo("class");
	}

	/**
	 * Verifies that METHOD_DECLARATION category maps to "method" configuration key.
	 */
	@Test
	public void methodDeclarationCategoryMapsToMethodConfigurationKey()
	{
		String configKey = NodeCategory.METHOD_DECLARATION.getConfigurationKey();

		assertThat(configKey).isEqualTo("method");
	}

	/**
	 * Verifies that CONSTRUCTOR_DECLARATION category maps to "method" configuration key.
	 */
	@Test
	public void constructorDeclarationCategoryMapsToMethodConfigurationKey()
	{
		String configKey = NodeCategory.CONSTRUCTOR_DECLARATION.getConfigurationKey();

		assertThat(configKey).isEqualTo("method");
	}

	/**
	 * Verifies that categorize() throws NullPointerException when node parameter is null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void categorizeWithNullNodeThrowsNullPointerException()
	{
		NodeCategory.categorize(null);
	}

	/**
	 * Creates minimal ModuleDeclarationNode for testing.
	 *
	 * @return minimal valid ModuleDeclarationNode instance
	 */
	private static ModuleDeclarationNode createMinimalModuleDeclarationNode()
	{
		SourcePosition start = new SourcePosition(1, 1);
		SourcePosition end = new SourcePosition(1, 20);
		SourceRange range = new SourceRange(start, end);
		return new ModuleDeclarationNode.Builder().
			setRange(range).
			build();
	}

	/**
	 * Creates minimal AnnotationDeclarationNode for testing.
	 *
	 * @return minimal valid AnnotationDeclarationNode instance
	 */
	private static AnnotationDeclarationNode createMinimalAnnotationDeclarationNode()
	{
		SourcePosition start = new SourcePosition(1, 1);
		SourcePosition end = new SourcePosition(1, 20);
		SourceRange range = new SourceRange(start, end);
		return new AnnotationDeclarationNode.Builder().
			setRange(range).
			build();
	}

	/**
	 * Creates minimal UnnamedClassNode for testing.
	 *
	 * @return minimal valid UnnamedClassNode instance
	 */
	private static UnnamedClassNode createMinimalUnnamedClassNode()
	{
		SourcePosition start = new SourcePosition(1, 1);
		SourcePosition end = new SourcePosition(1, 20);
		SourceRange range = new SourceRange(start, end);
		return new UnnamedClassNode.Builder().
			setRange(range).
			build();
	}

	/**
	 * Creates minimal FlexibleConstructorBodyNode for testing.
	 *
	 * @return minimal valid FlexibleConstructorBodyNode instance
	 */
	private static FlexibleConstructorBodyNode createMinimalFlexibleConstructorBodyNode()
	{
		SourcePosition start = new SourcePosition(1, 1);
		SourcePosition end = new SourcePosition(1, 20);
		SourceRange range = new SourceRange(start, end);
		return new FlexibleConstructorBodyNode.Builder().
			setRange(range).
			build();
	}

	/**
	 * Creates minimal CompactMainMethodNode for testing.
	 *
	 * @return minimal valid CompactMainMethodNode instance
	 */
	private static CompactMainMethodNode createMinimalCompactMainMethodNode()
	{
		SourcePosition start = new SourcePosition(1, 1);
		SourcePosition end = new SourcePosition(1, 20);
		SourceRange range = new SourceRange(start, end);
		return new CompactMainMethodNode.Builder().
			setRange(range).
			build();
	}

	/**
	 * Creates minimal InstanceMainMethodNode for testing.
	 *
	 * @return minimal valid InstanceMainMethodNode instance
	 */
	private static InstanceMainMethodNode createMinimalInstanceMainMethodNode()
	{
		SourcePosition start = new SourcePosition(1, 1);
		SourcePosition end = new SourcePosition(1, 20);
		SourceRange range = new SourceRange(start, end);
		return new InstanceMainMethodNode.Builder().
			setRange(range).
			build();
	}
}
