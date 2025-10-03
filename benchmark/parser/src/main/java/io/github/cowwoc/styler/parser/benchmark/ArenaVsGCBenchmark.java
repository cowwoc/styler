package io.github.cowwoc.styler.parser.benchmark;
import io.github.cowwoc.styler.parser.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparing Java's new Arena API (Project Panama) vs traditional GC-based allocation
 * for parser node allocation patterns.
 *
 * This benchmark tests the hypothesis from the user that Arena API might be beneficial
 * for temporary parser data structures vs relying on GC.
 *
 * Note: Requires JDK 21+ with Project Panama enabled:
 * --enable-preview --add-modules jdk.incubator.foreign
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class ArenaVsGCBenchmark {

	private static final int NODE_COUNT = 1000;
	private static final String SAMPLE_CODE = """
		package com.example;

		public class BenchmarkTest {
			private int field1 = 42;
			private String field2 = "test";

			public void method1() {
				for (int i = 0; i < 100; i += 1) {
					System.out.println("Hello " + i);
				}
			}

			public int method2(int param) {
				return param * 2 + field1;
			}
		}
		""";

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void parseWithTraditionalGC(Blackhole bh) {
		IndexOverlayParser parser = new IndexOverlayParser(SAMPLE_CODE);
		int rootId = parser.parse();

		// Access nodes to simulate formatting work
		ArenaNodeStorage.NodeInfo root = parser.getNode(rootId);
		bh.consume(root);

		// Simulate traversal
		for (int i = 0; i < parser.getNodeStorage().getNodeCount(); i += 1) {
			try {
				ArenaNodeStorage.NodeInfo node = parser.getNode(i);
				bh.consume(node.nodeType());
				bh.consume(node.startOffset());
				bh.consume(node.length());
			} catch (Exception e) {
				// Skip invalid nodes
			}
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void simulateArenaAllocation(Blackhole bh) {
		try (Arena arena = Arena.ofConfined()) {
			// Simulate node allocation pattern using Arena
			// Each node: 4 ints (start, length, type, parent) = 16 bytes
			MemorySegment nodeStorage = arena.allocate(NODE_COUNT * 16L);

			// Simulate parser creating nodes
			for (int i = 0; i < NODE_COUNT; i += 1) {
				long offset = i * 16L;
				nodeStorage.set(ValueLayout.JAVA_INT, offset, i * 10);      // startOffset
				nodeStorage.set(ValueLayout.JAVA_INT, offset + 4, 20);      // length
				nodeStorage.set(ValueLayout.JAVA_INT, offset + 8, (byte) 1); // nodeType
				nodeStorage.set(ValueLayout.JAVA_INT, offset + 12, i - 1);   // parentId
			}

			// Simulate traversal
			for (int i = 0; i < NODE_COUNT; i += 1) {
				long offset = i * 16L;
				int startOffset = nodeStorage.get(ValueLayout.JAVA_INT, offset);
				int length = nodeStorage.get(ValueLayout.JAVA_INT, offset + 4);
				byte nodeType = (byte) nodeStorage.get(ValueLayout.JAVA_INT, offset + 8);
				int parentId = nodeStorage.get(ValueLayout.JAVA_INT, offset + 12);

				bh.consume(startOffset);
				bh.consume(length);
				bh.consume(nodeType);
				bh.consume(parentId);
			}
		} // Arena automatically freed here
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void simulateTraditionalObjects(Blackhole bh) {
		List<MockNode> nodes = new ArrayList<>(NODE_COUNT);

		// Simulate node creation
		for (int i = 0; i < NODE_COUNT; i += 1) {
			nodes.add(new MockNode(i * 10, 20, (byte) 1, i - 1));
		}

		// Simulate traversal
		for (MockNode node : nodes) {
			bh.consume(node.startOffset);
			bh.consume(node.length);
			bh.consume(node.nodeType);
			bh.consume(node.parentId);
		}

		// Let GC handle cleanup
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void parseMultipleFilesGC(Blackhole bh) {
		// Simulate parsing multiple files (common batch operation)
		String[] sources = {SAMPLE_CODE, SAMPLE_CODE, SAMPLE_CODE, SAMPLE_CODE, SAMPLE_CODE};

		for (String source : sources) {
			IndexOverlayParser parser = new IndexOverlayParser(source);
			int rootId = parser.parse();

			ArenaNodeStorage.NodeInfo root = parser.getNode(rootId);
			bh.consume(root);
		}
		// Multiple parsers create garbage for GC
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void parseMultipleFilesArenaSimulated(Blackhole bh) {
		String[] sources = {SAMPLE_CODE, SAMPLE_CODE, SAMPLE_CODE, SAMPLE_CODE, SAMPLE_CODE};

		try (Arena arena = Arena.ofConfined()) {
			for (String source : sources) {
				// Simulate arena-based parser (simplified)
				MemorySegment nodeStorage = arena.allocate(NODE_COUNT * 16L);

				for (int i = 0; i < NODE_COUNT; i += 1) {
					long offset = i * 16L;
					nodeStorage.set(ValueLayout.JAVA_INT, offset, i * 10);
					nodeStorage.set(ValueLayout.JAVA_INT, offset + 4, 20);
					nodeStorage.set(ValueLayout.JAVA_INT, offset + 8, 1);
					nodeStorage.set(ValueLayout.JAVA_INT, offset + 12, i - 1);
				}

				// Simulate some processing
				int sum = 0;
				for (int i = 0; i < NODE_COUNT; i += 1) {
					long offset = i * 16L;
					sum += nodeStorage.get(ValueLayout.JAVA_INT, offset);
				}
				bh.consume(sum);
			}
		} // All arena memory freed at once
	}

	/**
	 * Memory pressure test - rapid allocation/deallocation cycles
	 */
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void memoryPressureGC(Blackhole bh) {
		for (int cycle = 0; cycle < 10; cycle += 1) {
			List<MockNode> nodes = new ArrayList<>(NODE_COUNT);

			for (int i = 0; i < NODE_COUNT; i += 1) {
				nodes.add(new MockNode(i, i * 2, (byte) (i % 10), i - 1));
			}

			// Process nodes
			int sum = 0;
			for (MockNode node : nodes) {
				sum += node.startOffset + node.length;
			}
			bh.consume(sum);

			// Nodes become eligible for GC after each cycle
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void memoryPressureArena(Blackhole bh) {
		for (int cycle = 0; cycle < 10; cycle += 1) {
			try (Arena arena = Arena.ofConfined()) {
				MemorySegment nodeStorage = arena.allocate(NODE_COUNT * 16L);

				for (int i = 0; i < NODE_COUNT; i += 1) {
					long offset = i * 16L;
					nodeStorage.set(ValueLayout.JAVA_INT, offset, i);
					nodeStorage.set(ValueLayout.JAVA_INT, offset + 4, i * 2);
					nodeStorage.set(ValueLayout.JAVA_INT, offset + 8, i % 10);
					nodeStorage.set(ValueLayout.JAVA_INT, offset + 12, i - 1);
				}

				// Process nodes
				int sum = 0;
				for (int i = 0; i < NODE_COUNT; i += 1) {
					long offset = i * 16L;
					sum += nodeStorage.get(ValueLayout.JAVA_INT, offset) +
						   nodeStorage.get(ValueLayout.JAVA_INT, offset + 4);
				}
				bh.consume(sum);
			} // Arena memory freed at end of cycle
		}
	}

	// Mock node class for traditional object comparison
	private static class MockNode {
		final int startOffset;
		final int length;
		final byte nodeType;
		final int parentId;

		MockNode(int startOffset, int length, byte nodeType, int parentId) {
			this.startOffset = startOffset;
			this.length = length;
			this.nodeType = nodeType;
			this.parentId = parentId;
		}
	}

	public static void main(String[] args) throws Exception {
		// Check if Arena API is available
		try {
			Arena.ofConfined();
			System.out.println("Arena API is available - running benchmarks");
		} catch (Exception e) {
			System.err.println("Arena API not available: " + e.getMessage());
			System.err.println("Requires JDK 21+ with --enable-preview --add-modules jdk.incubator.foreign");
			return;
		}

		Options opt = new OptionsBuilder()
				.include(ArenaVsGCBenchmark.class.getSimpleName())
				.build();

		new Runner(opt).run();
	}
}