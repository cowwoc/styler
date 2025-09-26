package io.github.styler.parser;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Comprehensive benchmark comparing Index-Overlay parallel arrays architecture
 * vs traditional Java object references for AST node representation.
 *
 * Evidence-based testing of the claims from the parser architecture study:
 * - "3-5x memory reduction"
 * - "better cache locality"
 * - "improved parallel processing performance"
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 2, warmups = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
public class IndexOverlayBenchmark {

    @Param({"1000", "10000", "50000"})
    private int nodeCount;

    private IndexOverlayAST indexOverlayAST;
    private ObjectReferenceAST objectReferenceAST;
    private Random random;

    @Setup(Level.Trial)
    public void setupTrial() {
        random = new Random(42); // Fixed seed for reproducibility

        // Build identical AST structures for fair comparison
        indexOverlayAST = buildIndexOverlayAST(nodeCount);
        objectReferenceAST = buildObjectReferenceAST(nodeCount);

        // Initialize concurrent structures
        concurrentMap = new java.util.concurrent.ConcurrentHashMap<>();
        atomicArray = new java.util.concurrent.atomic.AtomicReferenceArray<>(nodeCount);

        // Pre-populate concurrent structures for fair comparison
        for (int i = 0; i < nodeCount; i++) {
            long value = (long)indexOverlayAST.getStartOffset(i) + indexOverlayAST.getLength(i);
            concurrentMap.put(i, value);
            atomicArray.set(i, value);
        }
    }

    // ===== INDEX-OVERLAY IMPLEMENTATION =====
    static class IndexOverlayAST {
        private final int[] startOffsets;
        private final int[] lengths;
        private final byte[] nodeTypes;
        private final int[] parentIds;
        private final int[] childrenStart;
        private final int[] childrenCount;
        private final int[] childrenData;
        private final int nodeCount;

        public IndexOverlayAST(int capacity) {
            this.startOffsets = new int[capacity];
            this.lengths = new int[capacity];
            this.nodeTypes = new byte[capacity];
            this.parentIds = new int[capacity];
            this.childrenStart = new int[capacity];
            this.childrenCount = new int[capacity];
            this.childrenData = new int[capacity * 2]; // Estimate 2 children per node
            this.nodeCount = capacity;
            Arrays.fill(childrenStart, -1);
        }

        public void setNode(int nodeId, int startOffset, int length, byte nodeType, int parentId) {
            startOffsets[nodeId] = startOffset;
            lengths[nodeId] = length;
            nodeTypes[nodeId] = nodeType;
            parentIds[nodeId] = parentId;
        }

        public void setChildren(int nodeId, int[] children) {
            if (children.length == 0) return;

            childrenStart[nodeId] = nodeId * 2; // Simplified placement
            childrenCount[nodeId] = Math.min(children.length, 2);
            for (int i = 0; i < childrenCount[nodeId]; i++) {
                childrenData[nodeId * 2 + i] = children[i];
            }
        }

        public int getStartOffset(int nodeId) { return startOffsets[nodeId]; }
        public int getLength(int nodeId) { return lengths[nodeId]; }
        public byte getNodeType(int nodeId) { return nodeTypes[nodeId]; }
        public int getParentId(int nodeId) { return parentIds[nodeId]; }
        public int getNodeCount() { return nodeCount; }

        public int[] getChildren(int nodeId) {
            if (childrenStart[nodeId] == -1) return new int[0];
            int start = childrenStart[nodeId];
            int count = childrenCount[nodeId];
            return Arrays.copyOfRange(childrenData, start, start + count);
        }

        public long getMemoryUsage() {
            return (nodeCount * 5 * Integer.BYTES) + // 5 int arrays
                   (nodeCount * Byte.BYTES) +        // 1 byte array
                   (childrenData.length * Integer.BYTES); // children data
        }
    }

    // ===== TRADITIONAL OBJECT REFERENCE IMPLEMENTATION =====
    static class ObjectReferenceAST {
        static class Node {
            final int startOffset;
            final int length;
            final byte nodeType;
            final int nodeId;
            Node parent;
            List<Node> children;

            Node(int nodeId, int startOffset, int length, byte nodeType) {
                this.nodeId = nodeId;
                this.startOffset = startOffset;
                this.length = length;
                this.nodeType = nodeType;
                this.children = new ArrayList<>(2);
            }

            public void addChild(Node child) {
                children.add(child);
                child.parent = this;
            }
        }

        private final Node[] nodes;
        private final int nodeCount;

        public ObjectReferenceAST(int capacity) {
            this.nodes = new Node[capacity];
            this.nodeCount = capacity;
        }

        public void setNode(int nodeId, int startOffset, int length, byte nodeType, int parentId) {
            nodes[nodeId] = new Node(nodeId, startOffset, length, nodeType);
            if (parentId >= 0 && parentId < nodeId) {
                nodes[parentId].addChild(nodes[nodeId]);
            }
        }

        public Node getNode(int nodeId) { return nodes[nodeId]; }
        public int getNodeCount() { return nodeCount; }

        // Estimate memory usage (approximate)
        public long getMemoryUsage() {
            // Object header (16 bytes) + fields (24 bytes) + ArrayList overhead (~24 bytes)
            return nodeCount * (16 + 24 + 24) + (nodes.length * 8); // Reference array
        }
    }

    // ===== DATA GENERATION =====
    private IndexOverlayAST buildIndexOverlayAST(int nodeCount) {
        IndexOverlayAST ast = new IndexOverlayAST(nodeCount);

        for (int i = 0; i < nodeCount; i++) {
            int startOffset = i * 10;
            int length = 5 + random.nextInt(10);
            byte nodeType = (byte)(1 + random.nextInt(5));
            int parentId = i > 0 ? random.nextInt(i) : -1;

            ast.setNode(i, startOffset, length, nodeType, parentId);

            // Add some children
            if (i < nodeCount - 1) {
                int childCount = random.nextInt(3);
                int[] children = new int[childCount];
                for (int j = 0; j < childCount && i + j + 1 < nodeCount; j++) {
                    children[j] = i + j + 1;
                }
                ast.setChildren(i, children);
            }
        }

        return ast;
    }

    private ObjectReferenceAST buildObjectReferenceAST(int nodeCount) {
        ObjectReferenceAST ast = new ObjectReferenceAST(nodeCount);

        for (int i = 0; i < nodeCount; i++) {
            int startOffset = i * 10;
            int length = 5 + random.nextInt(10);
            byte nodeType = (byte)(1 + random.nextInt(5));
            int parentId = i > 0 ? random.nextInt(i) : -1;

            ast.setNode(i, startOffset, length, nodeType, parentId);
        }

        return ast;
    }

    // ===== BENCHMARKS =====

    @Benchmark
    public void indexOverlay_SequentialAccess(Blackhole bh) {
        long sum = 0;
        for (int i = 0; i < indexOverlayAST.getNodeCount(); i++) {
            sum += indexOverlayAST.getStartOffset(i) + indexOverlayAST.getLength(i);
        }
        bh.consume(sum);
    }

    @Benchmark
    public void objectReference_SequentialAccess(Blackhole bh) {
        long sum = 0;
        for (int i = 0; i < objectReferenceAST.getNodeCount(); i++) {
            ObjectReferenceAST.Node node = objectReferenceAST.getNode(i);
            sum += node.startOffset + node.length;
        }
        bh.consume(sum);
    }

    @Benchmark
    public void indexOverlay_RandomAccess(Blackhole bh) {
        long sum = 0;
        for (int i = 0; i < 1000; i++) {
            int nodeId = random.nextInt(indexOverlayAST.getNodeCount());
            sum += indexOverlayAST.getStartOffset(nodeId) + indexOverlayAST.getLength(nodeId);
        }
        bh.consume(sum);
    }

    @Benchmark
    public void objectReference_RandomAccess(Blackhole bh) {
        long sum = 0;
        for (int i = 0; i < 1000; i++) {
            int nodeId = random.nextInt(objectReferenceAST.getNodeCount());
            ObjectReferenceAST.Node node = objectReferenceAST.getNode(nodeId);
            sum += node.startOffset + node.length;
        }
        bh.consume(sum);
    }

    @Benchmark
    public void indexOverlay_TypeFiltering(Blackhole bh) {
        long count = 0;
        byte targetType = 3;
        for (int i = 0; i < indexOverlayAST.getNodeCount(); i++) {
            if (indexOverlayAST.getNodeType(i) == targetType) {
                count++;
            }
        }
        bh.consume(count);
    }

    @Benchmark
    public void objectReference_TypeFiltering(Blackhole bh) {
        long count = 0;
        byte targetType = 3;
        for (int i = 0; i < objectReferenceAST.getNodeCount(); i++) {
            if (objectReferenceAST.getNode(i).nodeType == targetType) {
                count++;
            }
        }
        bh.consume(count);
    }

    @Benchmark
    public void indexOverlay_ParallelProcessing(Blackhole bh) {
        long sum = IntStream.range(0, indexOverlayAST.getNodeCount())
            .parallel()
            .mapToLong(i -> indexOverlayAST.getLength(i))
            .sum();
        bh.consume(sum);
    }

    @Benchmark
    public void objectReference_ParallelProcessing(Blackhole bh) {
        long sum = IntStream.range(0, objectReferenceAST.getNodeCount())
            .parallel()
            .mapToLong(i -> objectReferenceAST.getNode(i).length)
            .sum();
        bh.consume(sum);
    }

    @Benchmark
    public void indexOverlay_TreeTraversal(Blackhole bh) {
        long visitedNodes = 0;
        // Simple BFS-style traversal using indices
        for (int i = 0; i < indexOverlayAST.getNodeCount(); i++) {
            visitedNodes++;
            int[] children = indexOverlayAST.getChildren(i);
            for (int childId : children) {
                if (childId < indexOverlayAST.getNodeCount()) {
                    visitedNodes++;
                }
            }
        }
        bh.consume(visitedNodes);
    }

    @Benchmark
    public void objectReference_TreeTraversal(Blackhole bh) {
        long visitedNodes = 0;
        // Simple traversal using object references
        for (int i = 0; i < objectReferenceAST.getNodeCount(); i++) {
            ObjectReferenceAST.Node node = objectReferenceAST.getNode(i);
            if (node != null) {
                visitedNodes++;
                for (ObjectReferenceAST.Node child : node.children) {
                    visitedNodes++;
                }
            }
        }
        bh.consume(visitedNodes);
    }

    // ===== SYNCHRONIZATION OVERHEAD BENCHMARKS =====
    // Compare synchronized vs unsynchronized access patterns

    private final Object lockObject = new Object();

    @Benchmark
    public void indexOverlay_UnsynchronizedAccess(Blackhole bh) {
        // Simulate single-threaded-per-file access (no synchronization)
        long sum = 0;
        for (int i = 0; i < indexOverlayAST.getNodeCount(); i++) {
            // Direct array access without synchronization
            sum += indexOverlayAST.getStartOffset(i) + indexOverlayAST.getLength(i);
        }
        bh.consume(sum);
    }

    @Benchmark
    public void indexOverlay_SynchronizedAccess(Blackhole bh) {
        // Simulate thread-safe access with synchronization overhead
        long sum = 0;
        for (int i = 0; i < indexOverlayAST.getNodeCount(); i++) {
            synchronized (lockObject) {
                // Same operation but with synchronization overhead
                sum += indexOverlayAST.getStartOffset(i) + indexOverlayAST.getLength(i);
            }
        }
        bh.consume(sum);
    }

    @Benchmark
    public void objectReference_UnsynchronizedAccess(Blackhole bh) {
        // Simulate single-threaded-per-file access (no synchronization)
        long sum = 0;
        for (int i = 0; i < objectReferenceAST.getNodeCount(); i++) {
            ObjectReferenceAST.Node node = objectReferenceAST.getNode(i);
            // Direct field access without synchronization
            sum += node.startOffset + node.length;
        }
        bh.consume(sum);
    }

    @Benchmark
    public void objectReference_SynchronizedAccess(Blackhole bh) {
        // Simulate thread-safe access with synchronization overhead
        long sum = 0;
        for (int i = 0; i < objectReferenceAST.getNodeCount(); i++) {
            synchronized (lockObject) {
                ObjectReferenceAST.Node node = objectReferenceAST.getNode(i);
                // Same operation but with synchronization overhead
                sum += node.startOffset + node.length;
            }
        }
        bh.consume(sum);
    }

    private volatile int volatileField = 0; // Class-level volatile field

    @Benchmark
    public void indexOverlay_VolatileFieldAccess(Blackhole bh) {
        // Simulate volatile field access overhead (memory barriers)
        // This approximates the cost of thread-safe reading with memory visibility guarantees
        long sum = 0;
        for (int i = 0; i < indexOverlayAST.getNodeCount(); i++) {
            // Force memory barrier by accessing volatile field
            volatileField = i; // Simulates volatile write cost
            int barrier = volatileField; // Simulates volatile read cost
            sum += indexOverlayAST.getStartOffset(i) + indexOverlayAST.getLength(i);
            bh.consume(barrier); // Prevent optimization
        }
        bh.consume(sum);
    }

    @Benchmark
    public void indexOverlay_AtomicAccess(Blackhole bh) {
        // Simulate atomic operations overhead
        java.util.concurrent.atomic.AtomicLong atomicSum = new java.util.concurrent.atomic.AtomicLong(0);
        for (int i = 0; i < indexOverlayAST.getNodeCount(); i++) {
            // Atomic operations simulate thread-safe accumulation
            long value = indexOverlayAST.getStartOffset(i) + indexOverlayAST.getLength(i);
            atomicSum.addAndGet(value);
        }
        bh.consume(atomicSum.get());
    }

    // Lock-free concurrent data structures
    private java.util.concurrent.ConcurrentHashMap<Integer, Long> concurrentMap;
    private java.util.concurrent.atomic.AtomicReferenceArray<Long> atomicArray;

    @Benchmark
    public void indexOverlay_ConcurrentHashMapAccess(Blackhole bh) {
        // Simulate lock-free concurrent map access
        long sum = 0;
        for (int i = 0; i < indexOverlayAST.getNodeCount(); i++) {
            Long value = concurrentMap.get(i);
            if (value != null) {
                sum += value;
            }
        }
        bh.consume(sum);
    }

    @Benchmark
    public void indexOverlay_AtomicReferenceArrayAccess(Blackhole bh) {
        // Simulate atomic reference array access (lock-free)
        long sum = 0;
        for (int i = 0; i < indexOverlayAST.getNodeCount(); i++) {
            Long value = atomicArray.get(i);
            if (value != null) {
                sum += value;
            }
        }
        bh.consume(sum);
    }

    @Benchmark
    public void indexOverlay_CompareAndSetAccess(Blackhole bh) {
        // Simulate compare-and-set operations (lock-free updates)
        java.util.concurrent.atomic.AtomicLong counter = new java.util.concurrent.atomic.AtomicLong(0);
        for (int i = 0; i < indexOverlayAST.getNodeCount(); i++) {
            long value = indexOverlayAST.getStartOffset(i) + indexOverlayAST.getLength(i);
            // Simulate lock-free increment with CAS
            long current, updated;
            do {
                current = counter.get();
                updated = current + value;
            } while (!counter.compareAndSet(current, updated));
        }
        bh.consume(counter.get());
    }

    @Benchmark
    public void indexOverlay_StampedLockOptimisticRead(Blackhole bh) {
        // Simulate StampedLock optimistic read (Java 8+ lock-free reads)
        java.util.concurrent.locks.StampedLock lock = new java.util.concurrent.locks.StampedLock();
        long sum = 0;
        for (int i = 0; i < indexOverlayAST.getNodeCount(); i++) {
            long stamp = lock.tryOptimisticRead();
            long value = indexOverlayAST.getStartOffset(i) + indexOverlayAST.getLength(i);
            if (!lock.validate(stamp)) {
                // Fallback to read lock if validation fails
                stamp = lock.readLock();
                try {
                    value = indexOverlayAST.getStartOffset(i) + indexOverlayAST.getLength(i);
                } finally {
                    lock.unlockRead(stamp);
                }
            }
            sum += value;
        }
        bh.consume(sum);
    }

    // ===== MEMORY ALLOCATION BENCHMARKS =====
    // These benchmarks measure actual memory allocation during construction
    // Use with JMH memory profilers: -prof gc or -prof mempool

    @Benchmark
    public IndexOverlayAST memoryAllocation_IndexOverlay() {
        // Measure memory allocation during construction
        IndexOverlayAST ast = new IndexOverlayAST(nodeCount);
        Random random = new Random(42);

        // Build the data structure (this is where memory allocation happens)
        for (int i = 0; i < nodeCount; i++) {
            int startOffset = i * 10;
            int length = 5 + random.nextInt(10);
            byte nodeType = (byte)(1 + random.nextInt(5));
            int parentId = i > 0 ? random.nextInt(i) : -1;
            ast.setNode(i, startOffset, length, nodeType, parentId);
        }

        // Add children relationships
        for (int i = 0; i < nodeCount - 1; i++) {
            int childCount = random.nextInt(3);
            if (childCount > 0) {
                List<Integer> childList = new ArrayList<>();
                for (int j = 1; j <= childCount && i + j < nodeCount; j++) {
                    childList.add(i + j);
                }
                ast.setChildren(i, childList.stream().mapToInt(Integer::intValue).toArray());
            }
        }

        return ast;
    }

    @Benchmark
    public ObjectReferenceAST memoryAllocation_ObjectReference() {
        // Measure memory allocation during construction
        ObjectReferenceAST ast = new ObjectReferenceAST(nodeCount);
        Random random = new Random(42);

        // Build the data structure (this is where memory allocation happens)
        for (int i = 0; i < nodeCount; i++) {
            int startOffset = i * 10;
            int length = 5 + random.nextInt(10);
            byte nodeType = (byte)(1 + random.nextInt(5));
            int parentId = i > 0 ? random.nextInt(i) : -1;
            ast.setNode(i, startOffset, length, nodeType, parentId);
        }

        // ObjectReferenceAST handles children relationships via parent-child links in setNode
        // No additional children setup needed - relationships created during node construction

        return ast;
    }

    // ===== MEMORY FOOTPRINT COMPARISON =====
    // Static method for memory footprint comparison (not a JMH benchmark)
    // Called from main() method to show estimated memory usage

    public static void compareMemoryFootprints(IndexOverlayAST indexAST, ObjectReferenceAST objectAST) {
        long indexMemory = indexAST.getMemoryUsage();
        long objectMemory = objectAST.getMemoryUsage();

        System.out.printf("=== ESTIMATED MEMORY FOOTPRINT ===%n");
        System.out.printf("Index-Overlay AST:    %,d bytes%n", indexMemory);
        System.out.printf("Object-Reference AST: %,d bytes%n", objectMemory);
        System.out.printf("Memory Efficiency:    %.2fx %s%n",
            Math.max((double)objectMemory / indexMemory, (double)indexMemory / objectMemory),
            indexMemory < objectMemory ? "better (Index-Overlay)" : "better (Object-Reference)");
        System.out.println();
        System.out.println("NOTE: Use -prof gc or -prof mempool with JMH for actual allocation measurement");
    }

    // ===== MAIN METHOD FOR STANDALONE EXECUTION =====
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("memory-test")) {
            // Quick validation of memory footprint estimates
            IndexOverlayBenchmark benchmark = new IndexOverlayBenchmark();
            benchmark.nodeCount = 1000;
            benchmark.setupTrial();

            compareMemoryFootprints(benchmark.indexOverlayAST, benchmark.objectReferenceAST);

            System.out.println("=== JMH MEMORY PROFILING COMMANDS ===");
            System.out.println("For actual allocation measurement, use:");
            System.out.println("  java -jar jmh-benchmarks.jar \".*memoryAllocation.*\" -prof gc");
            System.out.println("  java -jar jmh-benchmarks.jar \".*memoryAllocation.*\" -prof mempool");
            return;
        }

        // Run JMH benchmarks
        Options opt = new OptionsBuilder()
            .include(IndexOverlayBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(2)
            .measurementIterations(3)
            .build();

        new Runner(opt).run();
    }
}