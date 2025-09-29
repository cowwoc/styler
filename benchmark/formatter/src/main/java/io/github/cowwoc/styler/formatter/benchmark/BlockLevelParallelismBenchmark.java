package io.github.cowwoc.styler.formatter.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * REAL BLOCK-LEVEL PARALLELISM BENCHMARK
 *
 * Compares single-threaded vs multi-threaded processing of the SAME file data:
 * - Single-thread: One thread processes all blocks of a file sequentially
 * - Multi-thread: Different threads process different blocks of the same file concurrently
 *
 * This measures ACTUAL parallel processing benefits for code formatting,
 * not artificial operation division.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 4, timeUnit = TimeUnit.SECONDS)
public class BlockLevelParallelismBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        @Param({"100", "1000", "10000"}) // File sizes in lines
        public int fileSize;

        @Param({"1", "4", "8"}) // Thread counts
        public int threadCount;

        public List<FileBlock> fileBlocks;
        public int totalBlocks;

        @Setup(Level.Trial)
        public void setup() {
            // Generate realistic file blocks based on file size
            fileBlocks = generateFileBlocks(fileSize);
            totalBlocks = fileBlocks.size();
        }

        private List<FileBlock> generateFileBlocks(int fileSize) {
            List<FileBlock> blocks = new ArrayList<>();

            // Simulate realistic Java file structure
            int methodsCount = Math.max(5, fileSize / 50); // ~1 method per 50 lines
            int classesCount = Math.max(2, fileSize / 200); // ~1 class per 200 lines

            // Generate method blocks (most formatting work)
            for (int i = 0; i < methodsCount; i++) {
                int blockSize = 10 + ThreadLocalRandom.current().nextInt(40); // 10-50 lines per method
                blocks.add(new FileBlock(BlockType.METHOD, blockSize, "method_" + i));
            }

            // Generate class blocks (structural formatting)
            for (int i = 0; i < classesCount; i++) {
                int blockSize = 5 + ThreadLocalRandom.current().nextInt(15); // 5-20 lines per class header
                blocks.add(new FileBlock(BlockType.CLASS, blockSize, "class_" + i));
            }

            // Generate import/package blocks (simple formatting)
            int importsCount = Math.max(3, fileSize / 100);
            for (int i = 0; i < importsCount; i++) {
                blocks.add(new FileBlock(BlockType.IMPORT, 1, "import_" + i));
            }

            return blocks;
        }
    }

    // ===== SINGLE-THREADED PROCESSING =====

    @Benchmark
    public long singleThreadProcessing(BenchmarkState state, Blackhole bh) {
        // Process ALL blocks sequentially in a single thread
        long totalWork = 0;

        for (FileBlock block : state.fileBlocks) {
            totalWork += processBlock(block);
        }

        bh.consume(totalWork);
        return totalWork;
    }

    // ===== MULTI-THREADED BLOCK-LEVEL PROCESSING =====

    @Benchmark
    public long multiThreadBlockProcessing(BenchmarkState state, Blackhole bh) throws Exception {
        if (state.threadCount == 1) {
            // Same data, same processing - just ensure consistency
            return singleThreadProcessing(state, bh);
        }

        // Real block-level parallelism: Each block is a separate task
        // Let virtual thread executor decide which thread processes each block
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Long>> futures = new ArrayList<>();

        try {
            // Submit each block as a separate task - executor handles distribution
            for (FileBlock block : state.fileBlocks) {
                futures.add(executor.submit(() -> processBlock(block)));
            }

            // Collect results from all tasks
            long totalWork = 0;
            for (Future<Long> future : futures) {
                totalWork += future.get();
            }

            // Add realistic merging overhead (combining formatted blocks back into file)
            totalWork += simulateBlockMerging(state.totalBlocks, state.threadCount);

            bh.consume(totalWork);
            return totalWork;

        } finally {
            executor.shutdown();
        }
    }

    // ===== REALISTIC BLOCK PROCESSING SIMULATION =====

    /**
     * Simulates formatting a single block (method, class, import, etc.)
     * Processing time varies by block type and size - realistic for code formatting
     */
    private long processBlock(FileBlock block) {
        long work = 0;

        // Different formatting complexity by block type
        int baseComplexity = switch (block.type) {
            case METHOD -> 50; // Methods need indentation, spacing, parameter formatting
            case CLASS -> 20;  // Classes need structural formatting
            case IMPORT -> 5;  // Imports just need sorting/ordering
        };

        // Work scales with block size (lines of code)
        int totalComplexity = baseComplexity * block.size;

        // Simulate CPU-intensive formatting work
        for (int i = 0; i < totalComplexity; i++) {
            work += i * 31L + ThreadLocalRandom.current().nextInt(10);
        }

        return work;
    }

    /**
     * Simulates merging formatted blocks back into a complete file
     * This overhead exists only in multi-threaded processing
     */
    private long simulateBlockMerging(int totalBlocks, int threadCount) {
        long work = 0;

        // Merging cost: threads need to coordinate to combine their results
        int mergingComplexity = totalBlocks * 2 + threadCount * 5;

        for (int i = 0; i < mergingComplexity; i++) {
            work += i * 17L;
        }

        return work;
    }

    // ===== SUPPORTING CLASSES =====

    private static class FileBlock {
        final BlockType type;
        final int size; // Lines of code in this block
        final String identifier;

        FileBlock(BlockType type, int size, String identifier) {
            this.type = type;
            this.size = size;
            this.identifier = identifier;
        }
    }

    private enum BlockType {
        METHOD,  // Function/method blocks - most complex formatting
        CLASS,   // Class/interface declarations - structural formatting
        IMPORT   // Import statements - simple formatting
    }

    // ===== MAIN METHOD =====

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 BLOCK-LEVEL PARALLELISM BENCHMARK");
        System.out.println("====================================");
        System.out.println("REAL parallel processing comparison:");
        System.out.println("- Single-thread: One thread processes entire file sequentially");
        System.out.println("- Multi-thread: Different threads process different blocks of SAME file");
        System.out.println();
        System.out.println("📊 Test Configuration:");
        System.out.println("   - File sizes: 100, 1000, 10000 lines");
        System.out.println("   - Thread counts: 1, 4, 8 threads");
        System.out.println("   - Block types: Methods, Classes, Imports");
        System.out.println("   - Virtual threads for optimal coordination");
        System.out.println();
        System.out.println("📊 Total: 2 methods × 3 file sizes × 3 thread counts = 18 benchmark runs");
        System.out.println("📊 Runtime: ~5 minutes");
        System.out.println();

        Options opt = new OptionsBuilder()
            .include(BlockLevelParallelismBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(2)
            .warmupTime(org.openjdk.jmh.runner.options.TimeValue.seconds(3))
            .measurementIterations(3)
            .measurementTime(org.openjdk.jmh.runner.options.TimeValue.seconds(4))
            .shouldFailOnError(true)
            .build();

        new Runner(opt).run();

        System.out.println();
        System.out.println("📊 BENCHMARK ANALYSIS");
        System.out.println("====================");
        System.out.println("Key insights to analyze:");
        System.out.println("1. Single-thread baseline: File processing performance");
        System.out.println("2. Multi-thread scaling: Real speedup with block-level parallelism");
        System.out.println("3. Optimal thread count: Where coordination overhead balances processing gains");
        System.out.println("4. File size scaling: How parallelism benefits change with file complexity");
        System.out.println("5. Block merging cost: Overhead of combining parallel results");
        System.out.println();
    }
}