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
 * OPTIMIZED JMH benchmark comparing transformation context architectures.
 *
 * Eliminates wasteful duplicate runs by using separate benchmarks for:
 * - Current architecture: Only tests file size variations (thread count irrelevant)
 * - Hybrid architecture: Tests both file size AND thread count variations
 *
 * Uses VIRTUAL THREADS for multi-threading to eliminate platform thread overhead
 * and provide realistic parallel processing performance measurements.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 4, timeUnit = TimeUnit.SECONDS)
public class OptimizedArchitectureBenchmark {

    // ===== CURRENT ARCHITECTURE BENCHMARKS (FILE SIZE ONLY) =====

    @State(Scope.Benchmark)
    public static class CurrentArchitectureState {
        @Param({"100", "1000", "10000"}) // 3 file sizes including larger one
        public int fileSize;

        public int operationsCount;
        public int nodeCount;

        @Setup(Level.Trial)
        public void setup() {
            operationsCount = Math.max(5, fileSize / 20);
            nodeCount = Math.max(10, fileSize / 10);
        }
    }

    @Benchmark
    public long currentArchitecture_ContextCreation(CurrentArchitectureState state, Blackhole bh) {
        // Current context creation - simple and fast
        int work = simulateSimpleContextCreation(state.fileSize);
        bh.consume(work);
        return work;
    }

    @Benchmark
    public long currentArchitecture_SingleThreadProcessing(CurrentArchitectureState state, Blackhole bh) {
        // Current approach: Direct AST modification (always single-threaded)
        long totalWork = 0;
        for (int i = 0; i < state.operationsCount; i++) {
            totalWork += simulateDirectModification(i, state.fileSize);
        }
        bh.consume(totalWork);
        return totalWork;
    }

    // ===== HYBRID ARCHITECTURE BENCHMARKS (FILE SIZE + THREAD COUNT) =====

    @State(Scope.Benchmark)
    public static class HybridArchitectureState {
        @Param({"100", "1000", "10000"}) // 3 file sizes
        public int fileSize;

        @Param({"1", "4", "8"}) // 3 thread counts including more threads
        public int threadCount;

        public int operationsCount;
        public int nodeCount;

        @Setup(Level.Trial)
        public void setup() {
            operationsCount = Math.max(5, fileSize / 20);
            nodeCount = Math.max(10, fileSize / 10);
        }
    }

    @Benchmark
    public long hybridArchitecture_ContextCreation(HybridArchitectureState state, Blackhole bh) {
        // Hybrid context creation - wrapper + overlay setup
        int work = simulateHybridContextCreation(state.fileSize);
        bh.consume(work);
        return work;
    }

    @Benchmark
    public long hybridArchitecture_SingleThreadProcessing(HybridArchitectureState state, Blackhole bh) {
        // Hybrid approach: Overlay recording (single-threaded)
        long totalWork = 0;
        for (int i = 0; i < state.operationsCount; i++) {
            totalWork += simulateOverlayRecording(i, state.fileSize);
        }
        // Add reconstruction overhead
        totalWork += simulateReconstruction(state.operationsCount, state.fileSize);
        bh.consume(totalWork);
        return totalWork;
    }

    @Benchmark
    public long hybridArchitecture_MultiThreadProcessing(HybridArchitectureState state, Blackhole bh)
            throws InterruptedException {

        if (state.threadCount == 1) {
            return hybridArchitecture_SingleThreadProcessing(state, bh);
        }

        // Multi-threaded hybrid processing using virtual threads for optimal performance
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Long>> futures = new ArrayList<>();
        int operationsPerThread = Math.max(1, state.operationsCount / state.threadCount);

        try {
            // Submit work to threads
            for (int t = 0; t < state.threadCount; t++) {
                final int threadIndex = t;
                futures.add(executor.submit(() -> {
                    long threadWork = 0;
                    int startOp = threadIndex * operationsPerThread;
                    int endOp = Math.min(startOp + operationsPerThread, state.operationsCount);

                    for (int i = startOp; i < endOp; i++) {
                        threadWork += simulateOverlayRecording(i, state.fileSize);
                    }
                    return threadWork;
                }));
            }

            // Collect results
            long totalWork = 0;
            for (Future<Long> future : futures) {
                totalWork += future.get();
            }

            // Add overlay merging overhead (scales with thread count)
            totalWork += simulateMerging(state.threadCount, state.operationsCount);

            bh.consume(totalWork);
            return totalWork;

        } catch (Exception e) {
            throw new RuntimeException("Multi-threading failed", e);
        } finally {
            executor.shutdown();
        }
    }

    // ===== SIMULATION METHODS =====

    private long simulateDirectModification(int operationIndex, int fileSize) {
        long work = 0;
        int baseCost = 20 + (fileSize / 1000); // Scales with file size
        for (int i = 0; i < baseCost; i++) {
            work += i * 31L + ThreadLocalRandom.current().nextInt(5);
        }
        return work;
    }

    private long simulateOverlayRecording(int operationIndex, int fileSize) {
        long work = 0;
        int baseCost = 25 + (fileSize / 800); // Better scaling, higher base cost
        int threadSafetyCost = 5;
        for (int i = 0; i < baseCost + threadSafetyCost; i++) {
            work += i * 31L + ThreadLocalRandom.current().nextInt(3);
        }
        return work;
    }

    private long simulateReconstruction(int operationCount, int fileSize) {
        long work = 0;
        int reconstructionCost = Math.max(5, operationCount / 4) + (fileSize / 2000);
        for (int i = 0; i < reconstructionCost; i++) {
            work += i * 23L;
        }
        return work;
    }

    private long simulateMerging(int threadCount, int operationCount) {
        long work = 0;
        int mergingCost = threadCount * 3 + Math.max(2, operationCount / 10);
        for (int i = 0; i < mergingCost; i++) {
            work += i * 17L;
        }
        return work;
    }

    private int simulateSimpleContextCreation(int fileSize) {
        int work = 0;
        int creationCost = 5 + (fileSize / 5000);
        for (int i = 0; i < creationCost * 10; i++) {
            work += i;
        }
        return work;
    }

    private int simulateHybridContextCreation(int fileSize) {
        int work = 0;
        int wrapperCost = 8 + (fileSize / 3000);
        int overlayCost = 6 + (fileSize / 4000);
        int trackerCost = 4;
        int totalCost = (wrapperCost + overlayCost + trackerCost) * 10;
        for (int i = 0; i < totalCost; i++) {
            work += i;
        }
        return work;
    }

    // ===== MAIN METHOD =====

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 OPTIMIZED TRANSFORMATION CONTEXT BENCHMARK");
        System.out.println("==============================================");
        System.out.println("EFFICIENT benchmark design eliminates duplicate measurements:");
        System.out.println("VIRTUAL THREADS used for realistic multi-threading performance:");
        System.out.println();
        System.out.println("📊 Current Architecture (Single-threaded):");
        System.out.println("   - File sizes: 100, 1000, 10000 lines");
        System.out.println("   - Thread count: N/A (always single-threaded)");
        System.out.println("   - Benchmarks: 2 methods × 3 file sizes = 6 runs");
        System.out.println();
        System.out.println("📊 Hybrid Architecture (Multi-threading capable):");
        System.out.println("   - File sizes: 100, 1000, 10000 lines");
        System.out.println("   - Thread counts: 1, 4, 8 threads");
        System.out.println("   - Benchmarks: 3 methods × 3 file sizes × 3 thread counts = 27 runs");
        System.out.println();
        System.out.println("📊 Total: 6 + 27 = 33 efficient benchmark runs");
        System.out.println("📊 Runtime: ~6 minutes (18s per run)");
        System.out.println();

        Options opt = new OptionsBuilder()
            .include(OptimizedArchitectureBenchmark.class.getSimpleName())
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
        System.out.println("1. Context Creation: Current vs Hybrid overhead by file size");
        System.out.println("2. Single-thread Processing: Performance comparison across file sizes");
        System.out.println("3. Multi-thread Scaling: Speedup with 1 vs 4 vs 8 threads");
        System.out.println("4. File Size Impact: How performance scales from small to large files");
        System.out.println();
    }
}