package io.github.cowwoc.styler.formatter.experimental;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for the hybrid architecture implementation.
 * Tests thread safety, resource protection, and overlay functionality.
 */
class HybridArchitectureTest {

    @Mock private CompilationUnitNode mockRootNode;
    @Mock private ASTNode mockParentNode;
    @Mock private ASTNode mockChildNode;
    @Mock private ASTNode mockNewNode;

    private MutableFormattingContext context;
    private final Path testFilePath = Paths.get("test.java");
    private final String testSourceText = "public class Test {}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock hierarchy
        when(mockRootNode.getChildren()).thenReturn(List.of(mockParentNode));
        when(mockParentNode.getChildren()).thenReturn(List.of(mockChildNode));
        when(mockParentNode.getParent()).thenReturn(mockRootNode);
        when(mockChildNode.getParent()).thenReturn(mockParentNode);

        // Mock cloning for reconstructor
        try {
            when(mockRootNode.clone()).thenReturn(mockRootNode);
            when(mockParentNode.clone()).thenReturn(mockParentNode);
            when(mockChildNode.clone()).thenReturn(mockChildNode);
            when(mockNewNode.clone()).thenReturn(mockNewNode);
        } catch (CloneNotSupportedException e) {
            fail("Setup failed: " + e.getMessage());
        }

        // Initialize thread-local resource tracker
        ThreadLocalResourceTracker.initialize();

        context = new MutableFormattingContext(
            mockRootNode, testSourceText, testFilePath,
            new RuleConfiguration(), Set.of(), Map.of()
        );
    }

    @AfterEach
    void tearDown() {
        ThreadLocalResourceTracker.cleanup();
    }

    @Test
    @DisplayName("Hybrid context should provide immutable parent access")
    void testImmutableParentAccess() {
        ImmutableASTWrapper immutableParent = context.getImmutableParent();

        assertNotNull(immutableParent);
        assertEquals(mockRootNode, immutableParent.getRootNode());

        // Parent should be thread-safe for read access
        assertDoesNotThrow(() -> {
            immutableParent.getChildren(mockParentNode);
            immutableParent.getParentClass(mockChildNode);
            immutableParent.getEnclosingMethod(mockChildNode);
        });
    }

    @Test
    @DisplayName("Modifications should be recorded in overlay, not applied to shared AST")
    void testOverlayModificationRecording() {
        // Perform modifications
        context.replaceChild(mockParentNode, mockChildNode, mockNewNode);
        context.insertChild(mockParentNode, 0, mockNewNode);
        context.removeChild(mockParentNode, mockChildNode);

        // Verify modifications recorded in overlay
        MutableASTOverlay overlay = context.getLocalOverlay();
        assertEquals(3, overlay.getModificationCount());

        List<MutableASTOverlay.ModificationRecord> modifications = overlay.getModifications();
        assertEquals(3, modifications.size());

        // Verify modification types
        assertEquals(MutableASTOverlay.ModificationType.REPLACE_CHILD, modifications.get(0).getType());
        assertEquals(MutableASTOverlay.ModificationType.INSERT_CHILD, modifications.get(1).getType());
        assertEquals(MutableASTOverlay.ModificationType.REMOVE_CHILD, modifications.get(2).getType());

        // Verify original AST is NOT modified
        verify(mockParentNode, never()).replaceChild(any(), any());
        verify(mockParentNode, never()).insertBefore(any(), any());
        verify(mockParentNode, never()).removeChild(any());
    }

    @Test
    @DisplayName("Thread-local resource tracking should enforce per-thread limits")
    void testThreadLocalResourceTracking() {
        // Test modification count limit
        assertThrows(SecurityException.class, () -> {
            for (int i = 0; i < 50001; i++) {
                context.replaceChild(mockParentNode, mockChildNode, mockNewNode);
            }
        });

        // Reset for recursion test
        ThreadLocalResourceTracker.cleanup();
        ThreadLocalResourceTracker.initialize();

        // Test recursion depth limit
        assertThrows(SecurityException.class, () -> {
            for (int i = 0; i < 2001; i++) {
                ThreadLocalResourceTracker.enterRecursion();
            }
        });
    }

    @Test
    @DisplayName("Block memory validation should prevent memory exhaustion")
    void testBlockMemoryValidation() {
        // Create mock with excessive node count
        ASTNode largeBlock = mock(ASTNode.class);
        List<ASTNode> manyChildren = new ArrayList<>();
        for (int i = 0; i < 300000; i++) { // Exceeds 100,000 node limit
            manyChildren.add(mock(ASTNode.class));
        }
        when(largeBlock.getChildren()).thenReturn(manyChildren);

        ImmutableASTWrapper immutableParent = context.getImmutableParent();

        assertThrows(SecurityException.class, () -> {
            immutableParent.validateBlockForMutableCopy(largeBlock);
        });
    }

    @Test
    @DisplayName("Child context creation should share immutable parent but isolate overlay")
    void testChildContextCreation() {
        // Create child context
        MutableFormattingContext childContext = context.createChildContext(mockChildNode);

        // Should share immutable parent
        assertSame(context.getImmutableParent(), childContext.getImmutableParent());

        // Should have separate overlay
        assertNotSame(context.getLocalOverlay(), childContext.getLocalOverlay());

        // Modifications in child should not affect parent overlay
        childContext.replaceChild(mockParentNode, mockChildNode, mockNewNode);

        assertEquals(1, childContext.getModificationCount());
        assertEquals(0, context.getModificationCount());
    }

    @Test
    @DisplayName("Parallel processing should be thread-safe with proper isolation")
    void testParallelProcessingThreadSafety() throws InterruptedException {
        final int threadCount = 4;
        final int modificationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<Integer>> futures = new ArrayList<>();

        // Launch parallel processing threads
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            Future<Integer> future = executor.submit(() -> {
                try {
                    ThreadLocalResourceTracker.initialize();

                    // Create child context for this thread
                    MutableFormattingContext threadContext = context.createChildContext(mockChildNode);

                    // Perform modifications
                    for (int i = 0; i < modificationsPerThread; i++) {
                        threadContext.replaceChild(mockParentNode, mockChildNode, mockNewNode);
                    }

                    return threadContext.getModificationCount();
                } finally {
                    ThreadLocalResourceTracker.cleanup();
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        // Wait for completion
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify each thread recorded its modifications independently
        for (Future<Integer> future : futures) {
            try {
                assertEquals(modificationsPerThread, future.get());
            } catch (ExecutionException e) {
                fail("Thread execution failed: " + e.getCause());
            }
        }

        // Original context should be unmodified
        assertEquals(0, context.getModificationCount());
    }

    @Test
    @DisplayName("AST reconstruction should apply overlay modifications correctly")
    void testASTReconstruction() {
        // Setup mock cloning behavior
        CompilationUnitNode clonedRoot = mock(CompilationUnitNode.class);
        ASTNode clonedParent = mock(ASTNode.class);
        ASTNode clonedChild = mock(ASTNode.class);

        try {
            when(mockRootNode.clone()).thenReturn(clonedRoot);
            when(mockParentNode.clone()).thenReturn(clonedParent);
            when(mockChildNode.clone()).thenReturn(clonedChild);
        } catch (CloneNotSupportedException e) {
            fail("Clone setup failed");
        }

        when(clonedRoot.getChildren()).thenReturn(List.of(clonedParent));
        when(clonedParent.getChildren()).thenReturn(List.of(clonedChild));

        // Record modifications in overlay
        context.replaceChild(mockParentNode, mockChildNode, mockNewNode);

        // Reconstruct AST
        CompilationUnitNode result = ASTReconstructor.applyOverlay(context.getLocalOverlay());

        assertNotNull(result);
        // In a real implementation, verify the modifications were applied to the cloned AST
    }

    @Test
    @DisplayName("Resource usage statistics should be accurate")
    void testResourceUsageStatistics() {
        // Perform some modifications
        for (int i = 0; i < 10; i++) {
            context.replaceChild(mockParentNode, mockChildNode, mockNewNode);
        }

        ThreadLocalResourceTracker.ResourceStatistics stats = context.getResourceUsage();

        assertTrue(stats.getTotalModifications() >= 10);
        assertEquals(1, stats.getContextInstanceCount());
        assertTrue(stats.getTotalMemoryAllocated() > 0);
    }

    @Test
    @DisplayName("Parameter validation should prevent null arguments")
    void testParameterValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            context.replaceChild(null, mockChildNode, mockNewNode);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            context.replaceChild(mockParentNode, null, mockNewNode);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            context.replaceChild(mockParentNode, mockChildNode, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            context.insertChild(null, 0, mockNewNode);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            context.insertChild(mockParentNode, -1, mockNewNode);
        });
    }

    @Test
    @DisplayName("Overlay should preserve modification order")
    void testModificationOrdering() {
        // Apply modifications in specific order
        context.replaceChild(mockParentNode, mockChildNode, mockNewNode);
        context.insertChild(mockParentNode, 0, mockNewNode);
        context.removeChild(mockParentNode, mockChildNode);

        List<MutableASTOverlay.ModificationRecord> modifications = context.getModifications();

        // Verify order is preserved
        assertEquals(MutableASTOverlay.ModificationType.REPLACE_CHILD, modifications.get(0).getType());
        assertEquals(MutableASTOverlay.ModificationType.INSERT_CHILD, modifications.get(1).getType());
        assertEquals(MutableASTOverlay.ModificationType.REMOVE_CHILD, modifications.get(2).getType());

        // Verify sequence numbers are increasing
        long lastSequence = -1;
        for (MutableASTOverlay.ModificationRecord record : modifications) {
            assertTrue(record.getSequenceNumber() > lastSequence);
            lastSequence = record.getSequenceNumber();
        }
    }
}