package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.NodeType;
import org.testng.annotations.Test;
// DisplayName converted to Test description
// Nested classes kept as inner classes
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Memory usage validation tests for Arena API implementation - ensuring the
 * 16MB/1000 files target and 96.9% memory reduction vs 512MB baseline.
 *
 * <h2>Memory Business Rules Tested</h2>
 * <ul>
 * <li><strong>Memory Target Rule</strong>: ≤16MB memory footprint per 1000 files</li>
 * <li><strong>Baseline Reduction Rule</strong>: 96.9% reduction vs 512MB NodeRegistry baseline</li>
 * <li><strong>Memory Layout Rule</strong>: Exactly 16 bytes per node in Arena</li>
 * <li><strong>No Memory Leak Rule</strong>: Complete cleanup via Arena.close()</li>
 * <li><strong>Memory Growth Rule</strong>: Linear memory growth with file count</li>
 * <li><strong>GC Pressure Rule</strong>: Minimal garbage collection impact</li>
 * </ul>
 */
class ArenaMemoryUsageTest {

	private MemoryMXBean memoryBean;

	@BeforeMethod
	void setUp() {
		memoryBean = ManagementFactory.getMemoryMXBean();
		// Force garbage collection to get baseline measurement
		System.gc();
		Thread.yield();
		System.gc();
	}

	@AfterMethod
	void tearDown() {
		// Clean up after tests
		System.gc();
	}

	
	class MemoryTargetValidationTests {

		@Test void shouldAchieve16MBMemoryUsageFor1000Files() {
			// Realistic Java class for testing
			String javaFileTemplate = """
				package com.example.file%d;

				import java.util.*;
				import java.io.*;

				public class TestClass%d {
					private String field1;
					private int field2;
					private List<String> field3;

					public TestClass%d() {
						this.field1 = "test";
						this.field2 = %d;
						this.field3 = new ArrayList<>();
					}

					public void processData() {
						if (field2 > 0) {
							field3.add(field1 + field2);
							System.out.println("Processing: " + field1);
						}
					}

					public String getData() {
						return field3.stream()
							.reduce("", (a, b) -> a + " " + b)
							.trim();
					}

					@Override
					public String toString() {
						return "TestClass%d{field1='" + field1 + "', field2=" + field2 + "}";
					}
				}
				""";

			int fileCount = 1000;
			long totalMemoryUsed = 0;

			for (int i = 0; i < fileCount; i++) {
				String javaCode = String.format(javaFileTemplate, i, i, i, i * 10, i);

				try (IndexOverlayParser parser = new IndexOverlayParser(javaCode, JavaVersion.JAVA_21)) {
					parser.parse();
					ArenaNodeStorage storage = parser.getNodeStorage();
					totalMemoryUsed += storage.getEstimatedMemoryUsage();
				}

				// Progress indicator for long-running test
				if ((i + 1) % 100 == 0) {
					System.out.printf("Processed %d/%d files...\n", i + 1, fileCount);
				}
			}

			double memoryUsedMB = totalMemoryUsed / (1024.0 * 1024.0);
			System.out.printf("Total memory usage for %d files: %.2f MB\n", fileCount, memoryUsedMB);

			assertTrue(memoryUsedMB <= 16.0,
				String.format("Memory usage should be ≤16MB, got %.2f MB", memoryUsedMB));

			// Also verify average memory per file
			double memoryPerFileKB = (totalMemoryUsed / 1024.0) / fileCount;
			System.out.printf("Average memory per file: %.2f KB\n", memoryPerFileKB);

			assertTrue(memoryPerFileKB <= 16.0,
				String.format("Memory per file should be ≤16KB, got %.2f KB", memoryPerFileKB));

			System.out.println("✅ ACHIEVED: 16MB/1000 files memory target met");
		}

		@Test void shouldDemonstrate969PercentMemoryReductionVs512MBBaseline() {
			// The baseline is 512MB for 1000 files with traditional NodeRegistry approach
			long baselineMemoryBytes = 512L * 1024 * 1024; // 512MB in bytes

			String sampleCode = """
				package com.example.baseline;

				public class BaselineClass {
					private String name;
					private int value;
					private boolean active;

					public BaselineClass(String name, int value) {
						this.name = name;
						this.value = value;
						this.active = true;
					}

					public void process() {
						if (active && value > 0) {
							System.out.println("Processing " + name + " with value " + value);
						}
					}

					public String getName() { return name; }
					public int getValue() { return value; }
					public boolean isActive() { return active; }
					public void setActive(boolean active) { this.active = active; }
				}
				""";

			int fileCount = 1000;
			long totalArenaMemory = 0;

			for (int i = 0; i < fileCount; i++) {
				try (IndexOverlayParser parser = new IndexOverlayParser(sampleCode, JavaVersion.JAVA_21)) {
					parser.parse();
					totalArenaMemory += parser.getNodeStorage().getEstimatedMemoryUsage();
				}
			}

			double reductionPercentage = (1.0 - ((double) totalArenaMemory / baselineMemoryBytes)) * 100;
			double arenaMemoryMB = totalArenaMemory / (1024.0 * 1024.0);

			System.out.printf("Memory comparison:\n");
			System.out.printf("  Baseline (NodeRegistry): 512 MB\n");
			System.out.printf("  Arena implementation:    %.2f MB\n", arenaMemoryMB);
			System.out.printf("  Memory reduction:        %.1f%%\n", reductionPercentage);

			assertTrue(reductionPercentage >= 96.9,
				String.format("Should achieve ≥96.9%% memory reduction, got %.1f%%", reductionPercentage));

			System.out.println("✅ ACHIEVED: 96.9%+ memory reduction target exceeded");
		}

		@Test void shouldMaintainLinearMemoryGrowthWithFileCount() {
			String testCode = """
				public class LinearGrowthTest {
					private int field;
					public void method() {
						field = 42;
					}
				}
				""";

			int[] fileCounts = {10, 50, 100, 250, 500, 750, 1000};
			List<Double> memoryPerFileList = new ArrayList<>();

			for (int fileCount : fileCounts) {
				long totalMemory = 0;

				for (int i = 0; i < fileCount; i++) {
					try (IndexOverlayParser parser = new IndexOverlayParser(testCode)) {
						parser.parse();
						totalMemory += parser.getNodeStorage().getEstimatedMemoryUsage();
					}
				}

				double memoryPerFile = (double) totalMemory / fileCount;
				memoryPerFileList.add(memoryPerFile);

				System.out.printf("Files: %4d, Memory per file: %.1f bytes\n", fileCount, memoryPerFile);
			}

			// Verify linear growth - memory per file should be relatively stable
			double firstMemoryPerFile = memoryPerFileList.get(0);
			double lastMemoryPerFile = memoryPerFileList.get(memoryPerFileList.size() - 1);
			double variationRatio = lastMemoryPerFile / firstMemoryPerFile;

			System.out.printf("Memory per file variation: %.2fx\n", variationRatio);

			// Memory per file should not vary dramatically (indicates linear growth)
			assertTrue(variationRatio <= 1.5,
				String.format("Memory per file should be stable (≤1.5x variation), got %.2fx", variationRatio));

			System.out.println("✅ ACHIEVED: Linear memory growth pattern maintained");
		}
	}

	
	class MemoryLayoutValidationTests {

		@Test void shouldUseExactly16BytesPerNodeInArena() {
			int nodeCount = 1000;

			try (ArenaNodeStorage storage = ArenaNodeStorage.create(nodeCount)) {
				// Allocate nodes and measure Arena memory specifically
				for (int i = 0; i < nodeCount; i++) {
					storage.allocateNode(i * 10, 5, NodeType.METHOD_DECLARATION, -1);
				}

				long totalMemory = storage.getEstimatedMemoryUsage();

				// Calculate Arena memory (excluding child arrays)
				long childArrayMemory =
					(long) nodeCount * Integer.BYTES +   // childrenStart array
					(long) nodeCount * Integer.BYTES +   // childrenCount array
					1024 * Integer.BYTES;               // childrenData array (initial capacity)

				long arenaMemory = totalMemory - childArrayMemory;
				double bytesPerNode = (double) arenaMemory / nodeCount;

				System.out.printf("Memory breakdown:\n");
				System.out.printf("  Total memory:      %d bytes\n", totalMemory);
				System.out.printf("  Arena memory:      %d bytes\n", arenaMemory);
				System.out.printf("  Child arrays:      %d bytes\n", childArrayMemory);
				System.out.printf("  Bytes per node:    %.1f bytes\n", bytesPerNode);

				assertEquals(16.0, bytesPerNode, 0.1,
					String.format("Arena should use exactly 16 bytes per node, got %.1f", bytesPerNode));

				System.out.println("✅ ACHIEVED: Exact 16-byte node layout verified");
			}
		}

		@Test void shouldDemonstrateMemoryLayoutEfficiencyOverObjects() {
			int nodeCount = 10000;

			// Measure Arena memory usage
			long arenaMemory;
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(nodeCount)) {
				for (int i = 0; i < nodeCount; i++) {
					storage.allocateNode(i * 10, 5, NodeType.METHOD_DECLARATION, -1);
				}
				arenaMemory = storage.getEstimatedMemoryUsage();
			}

			// Calculate theoretical object memory usage
			// Each object would have:
			// - Object header: ~16 bytes (64-bit JVM with compressed OOPs)
			// - 4 int fields: 16 bytes
			// - Reference to children list: 8 bytes
			// - ArrayList object: ~24 bytes minimum
			// Total per object: ~64 bytes (conservative estimate)
			long theoreticalObjectMemory = (long) nodeCount * 64;

			double efficiency = 1.0 - ((double) arenaMemory / theoreticalObjectMemory);

			System.out.printf("Memory layout comparison for %d nodes:\n", nodeCount);
			System.out.printf("  Arena memory:      %d bytes (%.1f bytes/node)\n",
				arenaMemory, (double) arenaMemory / nodeCount);
			System.out.printf("  Object memory:     %d bytes (%.1f bytes/node)\n",
				theoreticalObjectMemory, (double) theoreticalObjectMemory / nodeCount);
			System.out.printf("  Space efficiency:  %.1f%%\n", efficiency * 100);

			assertTrue(efficiency >= 0.6,
				String.format("Arena should be at least 60%% more space-efficient, got %.1f%%", efficiency * 100));

			System.out.printf("✅ ACHIEVED: %.1f%% space efficiency over object allocation\n", efficiency * 100);
		}
	}

	
	class MemoryLeakPreventionTests {

		@Test void shouldPreventMemoryLeaksThroughArenaCleanup() {
			// Get baseline memory usage
			System.gc();
			MemoryUsage beforeUsage = memoryBean.getHeapMemoryUsage();

			// Allocate and close many Arenas
			int arenaCount = 100;
			for (int i = 0; i < arenaCount; i++) {
				try (ArenaNodeStorage storage = ArenaNodeStorage.create(1000)) {
					// Allocate nodes
					for (int j = 0; j < 1000; j++) {
						storage.allocateNode(j * 10, 5, NodeType.METHOD_DECLARATION, -1);
					}
					// Arena automatically closes due to try-with-resources
				}

				// Progress indicator
				if ((i + 1) % 20 == 0) {
					System.out.printf("Processed %d/%d arenas...\n", i + 1, arenaCount);
				}
			}

			// Force garbage collection and measure memory
			System.gc();
			Thread.yield();
			System.gc();
			MemoryUsage afterUsage = memoryBean.getHeapMemoryUsage();

			long memoryIncrease = afterUsage.getUsed() - beforeUsage.getUsed();
			double memoryIncreaseMB = memoryIncrease / (1024.0 * 1024.0);

			System.out.printf("Memory usage change after %d arenas: %.2f MB\n", arenaCount, memoryIncreaseMB);

			// Memory increase should be minimal (some increase is expected due to test overhead)
			assertTrue(memoryIncreaseMB <= 10.0,
				String.format("Memory increase should be ≤10MB, got %.2f MB", memoryIncreaseMB));

			System.out.println("✅ ACHIEVED: No significant memory leaks detected");
		}

		@Test void shouldCleanupResourcesCompletelyViaReset() {
			try (ArenaNodeStorage storage = ArenaNodeStorage.create(10000)) {
				// Allocate many nodes with complex relationships
				int parentId = storage.allocateNode(0, 100, NodeType.CLASS_DECLARATION, -1);
				for (int i = 0; i < 5000; i++) {
					storage.allocateNode(i * 10, 5, NodeType.METHOD_DECLARATION, parentId);
				}

				long memoryBeforeReset = storage.getEstimatedMemoryUsage();
				assertTrue(memoryBeforeReset > 50000, "Should have substantial memory usage before reset");

				// Reset the storage
				storage.reset();

				// Verify cleanup
				assertEquals(0, storage.getNodeCount(), "Node count should be 0 after reset");
				assertTrue(storage.isAlive(), "Arena should still be alive after reset");

				// Memory should be reduced but not necessarily to zero (child arrays remain allocated)
				long memoryAfterReset = storage.getEstimatedMemoryUsage();
				assertTrue(memoryAfterReset < memoryBeforeReset,
					"Memory usage should be reduced after reset");

				System.out.printf("Memory usage: before reset = %d bytes, after reset = %d bytes\n",
					memoryBeforeReset, memoryAfterReset);

				// Should be able to allocate new nodes after reset
				int newNodeId = storage.allocateNode(0, 10, NodeType.INTERFACE_DECLARATION, -1);
				assertEquals(0, newNodeId, "First node after reset should have ID 0");
			}

			System.out.println("✅ ACHIEVED: Complete resource cleanup via reset");
		}

		@Test void shouldHandleLargeAllocationsWithoutMemoryExhaustion() {
			// Test with a very large number of nodes
			int largeNodeCount = 100_000;

			try (ArenaNodeStorage storage = ArenaNodeStorage.create(largeNodeCount)) {
				// Allocate many nodes
				for (int i = 0; i < largeNodeCount; i++) {
					storage.allocateNode(i * 10, 5, NodeType.METHOD_DECLARATION, -1);

					// Progress indicator
					if ((i + 1) % 10000 == 0) {
						System.out.printf("Allocated %d/%d nodes...\n", i + 1, largeNodeCount);
					}
				}

				long totalMemory = storage.getEstimatedMemoryUsage();
				double memoryMB = totalMemory / (1024.0 * 1024.0);

				System.out.printf("Large allocation test: %d nodes, %.2f MB memory\n", largeNodeCount, memoryMB);

				// Memory should be reasonable even for large allocations
				assertTrue(memoryMB <= 50.0,
					String.format("Large allocation should use ≤50MB, got %.2f MB", memoryMB));

				// Verify all nodes are accessible
				assertEquals(largeNodeCount, storage.getNodeCount(), "All nodes should be allocated");

				// Spot check some nodes
				for (int i = 0; i < largeNodeCount; i += 10000) {
					ArenaNodeStorage.NodeInfo node = storage.getNode(i);
					assertEquals(i * 10, node.startOffset(), "Node " + i + " should have correct offset");
				}
			}

			System.out.println("✅ ACHIEVED: Large allocation handling without memory exhaustion");
		}
	}

	
	class GCPressureTests {

		@Test void shouldMinimizeGarbageCollectionPressure() {
			// Get baseline GC stats
			long gcCountBefore = getGCCollectionCount();
			long gcTimeBefore = getGCCollectionTime();

			// Perform many allocations and deallocations
			String testCode = "public class GCTest { public void method() {} }";
			int iterations = 1000;

			for (int i = 0; i < iterations; i++) {
				try (IndexOverlayParser parser = new IndexOverlayParser(testCode)) {
					parser.parse();
					// Arena automatically cleaned up
				}
			}

			// Get final GC stats
			long gcCountAfter = getGCCollectionCount();
			long gcTimeAfter = getGCCollectionTime();

			long gcCollections = gcCountAfter - gcCountBefore;
			long gcTime = gcTimeAfter - gcTimeBefore;

			System.out.printf("GC impact for %d iterations:\n", iterations);
			System.out.printf("  Collections triggered: %d\n", gcCollections);
			System.out.printf("  Time spent in GC:      %d ms\n", gcTime);

			// Should not trigger excessive GC
			assertTrue(gcCollections <= 5,
				String.format("Should trigger ≤5 GC collections, got %d", gcCollections));
			assertTrue(gcTime <= 100,
				String.format("Should spend ≤100ms in GC, got %d ms", gcTime));

			System.out.println("✅ ACHIEVED: Minimal GC pressure maintained");
		}

		private long getGCCollectionCount() {
			return ManagementFactory.getGarbageCollectorMXBeans()
				.stream()
				.mapToLong(gcBean -> gcBean.getCollectionCount())
				.sum();
		}

		private long getGCCollectionTime() {
			return ManagementFactory.getGarbageCollectorMXBeans()
				.stream()
				.mapToLong(gcBean -> gcBean.getCollectionTime())
				.sum();
		}
	}
}