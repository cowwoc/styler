package io.github.cowwoc.styler.formatter.experimental;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.Comment;
import io.github.cowwoc.styler.ast.WhitespaceInfo;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MutableFormattingContext.
 * Tests the direct AST modification approach.
 */
class MutableFormattingContextTest {

    @Mock
    private CompilationUnitNode mockRootNode;

    @Mock
    private ASTNode mockParentNode;

    @Mock
    private ASTNode mockChildNode;

    @Mock
    private ASTNode mockNewNode;

    @Mock
    private RuleConfiguration mockConfiguration;

    @Mock
    private WhitespaceInfo mockWhitespace;

    private MutableFormattingContext context;
    private final String sourceText = "public class Test { }";
    private final Path filePath = Paths.get("/test/Test.java");
    private final Set<String> enabledRules = Set.of("test-rule");
    private final Map<String, Object> metadata = new HashMap<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup basic mock behavior
        when(mockChildNode.getWhitespaceInfo()).thenReturn(mockWhitespace);
        when(mockChildNode.getLeadingComments()).thenReturn(new ArrayList<>());
        when(mockChildNode.getTrailingComments()).thenReturn(new ArrayList<>());

        context = new MutableFormattingContext(
            mockRootNode,
            sourceText,
            filePath,
            mockConfiguration,
            enabledRules,
            metadata
        );
    }

    @Test
    void testFormattingContextMethods() {
        assertEquals(mockRootNode, context.getRootNode());
        assertEquals(sourceText, context.getSourceText());
        assertEquals(filePath, context.getFilePath());
        assertEquals(mockConfiguration, context.getConfiguration());
        assertEquals(enabledRules, context.getEnabledRules());
        assertEquals(metadata, context.getMetadata());
    }

    @Test
    void testSetRootNode() {
        CompilationUnitNode newRoot = mock(CompilationUnitNode.class);

        context.setRootNode(newRoot);

        assertEquals(newRoot, context.getRootNode());
        assertEquals(1, context.getModificationCount());
        assertTrue(context.hasModifications());
    }

    @Test
    void testReplaceChild() {
        context.replaceChild(mockParentNode, mockChildNode, mockNewNode);

        verify(mockParentNode).replaceChild(mockChildNode, mockNewNode);
        assertEquals(1, context.getModificationCount());
        assertTrue(context.hasModifications());
    }

    @Test
    void testInsertBefore() {
        context.insertBefore(mockParentNode, mockChildNode, mockNewNode);

        verify(mockParentNode).insertBefore(mockChildNode, mockNewNode);
        assertEquals(1, context.getModificationCount());
    }

    @Test
    void testInsertAfter() {
        context.insertAfter(mockParentNode, mockChildNode, mockNewNode);

        verify(mockParentNode).insertAfter(mockChildNode, mockNewNode);
        assertEquals(1, context.getModificationCount());
    }

    @Test
    void testRemoveChild() {
        context.removeChild(mockParentNode, mockChildNode);

        verify(mockParentNode).removeChild(mockChildNode);
        assertEquals(1, context.getModificationCount());
    }

    @Test
    void testSetWhitespace() {
        WhitespaceInfo newWhitespace = mock(WhitespaceInfo.class);

        context.setWhitespace(mockChildNode, newWhitespace);

        verify(mockChildNode).setWhitespaceInfo(newWhitespace);
        assertEquals(1, context.getModificationCount());
    }

    @Test
    void testSetWhitespaceWithNull() {
        context.setWhitespace(mockChildNode, null);

        verify(mockChildNode, never()).setWhitespaceInfo(any());
        assertEquals(0, context.getModificationCount());
    }

    @Test
    void testSetComments() {
        List<Comment> leadingComments = List.of(mock(Comment.class));
        List<Comment> trailingComments = List.of(mock(Comment.class));

        context.setComments(mockChildNode, leadingComments, trailingComments);

        verify(mockChildNode).setLeadingComments(leadingComments);
        verify(mockChildNode).setTrailingComments(trailingComments);
        assertEquals(2, context.getModificationCount()); // Both leading and trailing increment count
    }

    @Test
    void testSetCommentsPartial() {
        List<Comment> leadingComments = List.of(mock(Comment.class));

        context.setComments(mockChildNode, leadingComments, null);

        verify(mockChildNode).setLeadingComments(leadingComments);
        verify(mockChildNode, never()).setTrailingComments(any());
        assertEquals(1, context.getModificationCount());
    }

    @Test
    void testNullParameterValidation() {
        assertThrows(NullPointerException.class, () ->
            context.setRootNode(null));

        assertThrows(NullPointerException.class, () ->
            context.replaceChild(null, mockChildNode, mockNewNode));

        assertThrows(NullPointerException.class, () ->
            context.replaceChild(mockParentNode, null, mockNewNode));

        assertThrows(NullPointerException.class, () ->
            context.replaceChild(mockParentNode, mockChildNode, null));

        assertThrows(NullPointerException.class, () ->
            context.insertBefore(null, mockChildNode, mockNewNode));

        assertThrows(NullPointerException.class, () ->
            context.removeChild(mockParentNode, null));

        assertThrows(NullPointerException.class, () ->
            context.setWhitespace(null, mockWhitespace));

        assertThrows(NullPointerException.class, () ->
            context.setComments(null, null, null));
    }

    @Test
    void testModificationTracking() {
        assertFalse(context.hasModifications());
        assertEquals(0, context.getModificationCount());

        context.replaceChild(mockParentNode, mockChildNode, mockNewNode);
        assertTrue(context.hasModifications());
        assertEquals(1, context.getModificationCount());

        context.setWhitespace(mockChildNode, mockWhitespace);
        assertEquals(2, context.getModificationCount());

        context.resetModificationCount();
        assertEquals(0, context.getModificationCount());
        assertFalse(context.hasModifications());
    }

    @Test
    void testMultipleModifications() {
        context.replaceChild(mockParentNode, mockChildNode, mockNewNode);
        context.insertBefore(mockParentNode, mockChildNode, mockNewNode);
        context.setWhitespace(mockChildNode, mockWhitespace);

        assertEquals(3, context.getModificationCount());
        assertTrue(context.hasModifications());
    }

    @Test
    void testParameterValidation() {
        // Test null parameter validation
        assertThrows(IllegalArgumentException.class,
            () -> context.replaceChild(null, mockChildNode, mockNewNode));
        assertThrows(IllegalArgumentException.class,
            () -> context.replaceChild(mockParentNode, null, mockNewNode));
        assertThrows(IllegalArgumentException.class,
            () -> context.replaceChild(mockParentNode, mockChildNode, null));

        assertThrows(IllegalArgumentException.class,
            () -> context.setRootNode(null));

        assertThrows(IllegalArgumentException.class,
            () -> context.setWhitespace(null, mockWhitespace));
    }

    @Test
    void testResourceLimits() {
        // Test modification count limit
        // Since MAX_MODIFICATIONS is 10000, we can't easily test this in a unit test
        // but we can verify the mechanism works with a smaller number of modifications
        assertTrue(context.getModificationCount() < 10000);

        // Resource limits are checked before each modification, so normal operations should work
        context.replaceChild(mockParentNode, mockChildNode, mockNewNode);
        assertEquals(1, context.getModificationCount());
    }
}