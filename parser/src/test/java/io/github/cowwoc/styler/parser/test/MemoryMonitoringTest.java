package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.MemoryArena;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Test memory monitoring to prevent exhaustion attacks.
 */
public class MemoryMonitoringTest {

    @Test
    public void testArenaMemoryMonitoring() {
        // Create a small arena to test capacity limits
        MemoryArena arena = new MemoryArena(1024); // Very small 1KB arena

        try {
            // Try to allocate many nodes to exceed arena capacity
            for (int i = 0; i < 10000; i++) {
                arena.allocateNode((byte) 1, i, 10, -1);
            }
            fail("Expected IllegalStateException for arena memory limit");
        } catch (IllegalStateException e) {
            // Should hit either the node count limit or memory monitoring
            assertEquals(true, e.getMessage().contains("Arena is full") ||
                              e.getMessage().contains("memory"));
        }
    }

    @Test
    public void testMemoryUsageTracking() {
        MemoryArena arena = new MemoryArena();

        // Initial memory usage should be zero
        assertEquals(0L, arena.getMemoryUsage());

        // Allocate some nodes and verify memory usage increases
        arena.allocateNode((byte) 1, 0, 10, -1);
        arena.allocateNode((byte) 2, 10, 20, 0);

        // Memory usage should be positive after allocations
        assertEquals(true, arena.getMemoryUsage() > 0);

        // Node count should match allocations
        assertEquals(2, arena.getNodeCount());
    }

    @Test
    public void testArenaReset() {
        MemoryArena arena = new MemoryArena();

        // Allocate some nodes
        arena.allocateNode((byte) 1, 0, 10, -1);
        arena.allocateNode((byte) 2, 10, 20, 0);

        assertEquals(2, arena.getNodeCount());
        assertEquals(true, arena.getMemoryUsage() > 0);

        // Reset arena
        arena.reset();

        // Should be back to initial state
        assertEquals(0, arena.getNodeCount());
        assertEquals(0L, arena.getMemoryUsage());
    }

    @Test
    public void testNormalMemoryUsageWorks() {
        MemoryArena arena = new MemoryArena();

        // Normal usage should not trigger memory monitoring
        for (int i = 0; i < 100; i++) {
            arena.allocateNode((byte) 1, i * 10, 10, -1);
        }

        // Should complete without exception
        assertEquals(100, arena.getNodeCount());
        assertEquals(true, arena.getMemoryUsage() > 0);
    }

    @Test
    public void testMemoryMonitoringInterval() {
        MemoryArena arena = new MemoryArena();

        // The memory check happens every 1000 allocations
        // So allocating 999 should not trigger a check
        for (int i = 0; i < 999; i++) {
            arena.allocateNode((byte) 1, i, 1, -1);
        }

        // Should complete without memory check
        assertEquals(999, arena.getNodeCount());

        // The 1000th allocation should trigger memory check (but should still pass)
        arena.allocateNode((byte) 1, 999, 1, -1);
        assertEquals(1000, arena.getNodeCount());
    }
}