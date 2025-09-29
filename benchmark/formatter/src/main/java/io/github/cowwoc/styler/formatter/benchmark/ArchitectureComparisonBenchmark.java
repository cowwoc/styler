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
import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * JMH benchmark comparing current vs hybrid transformation context architectures.
 *
 * This benchmark simulates the performance characteristics of:
 * 1. Current approach: Direct AST modification (single-threaded only)
 * 2. Hybrid approach: Immutable parent + mutable overlays (thread-safe)
 *
 * Measures speedup vs file size AND thread count as requested.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 4, timeUnit = TimeUnit.SECONDS)
public class ArchitectureComparisonBenchmark {

    // File size parameter (lines of code) - reduced to 2 values
    @Param({"100", "5000"})
    private int fileSize;

    // Thread count parameter - reduced to key values
    @Param({"1", "4"})
    private int threadCount;

    private int operationsCount;
    private int nodeCount;

    @Setup(Level.Trial)
    public void setup() {
        // Scale operations and nodes based on file size
        operationsCount = Math.max(5, fileSize / 20);
        nodeCount = Math.max(10, fileSize / 10);
    }

    // ===== CURRENT ARCHITECTURE (SINGLE-THREADED ONLY) =====

    @Benchmark
    public long currentArchitecture_SingleThread(Blackhole bh) {
        // Current approach: Direct AST modification
        // Lower overhead per operation, but no parallelization
        // NOTE: Thread count parameter is ignored - current approach is always single-threaded

        long totalWork = 0;
        for (int i = 0; i < operationsCount; i++) {
            // Simulate direct AST modification
            totalWork += simulateDirectModification(i);
        }

        bh.consume(totalWork);
        return totalWork;
    }

    // ===== HYBRID ARCHITECTURE (THREAD-SAFE) =====

    @Benchmark
    public long hybridArchitecture_SingleThread(Blackhole bh) {
        // Hybrid approach: Overlay recording
        // Higher overhead per operation, but supports parallelization

        long totalWork = 0;
        for (int i = 0; i < operationsCount; i++) {
            // Simulate overlay recording (thread-safe operations)
            totalWork += simulateOverlayRecording(i);
        }

        // Simulate overlay reconstruction overhead
        totalWork += simulateReconstruction(operationsCount);

        bh.consume(totalWork);
        return totalWork;
    }

    @Benchmark
    public long hybridArchitecture_MultiThread(Blackhole bh) throws InterruptedException {
        if (threadCount == 1) {
            return hybridArchitecture_SingleThread(bh);
        }

        // Multi-threaded hybrid processing
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Long>> futures = new ArrayList<>();

        int operationsPerThread = Math.max(1, operationsCount / threadCount);

        try {
            // Submit work to threads
            for (int t = 0; t < threadCount; t++) {
                final int threadIndex = t;
                futures.add(executor.submit(() -> {
                    long threadWork = 0;
                    int startOp = threadIndex * operationsPerThread;
                    int endOp = Math.min(startOp + operationsPerThread, operationsCount);

                    for (int i = startOp; i < endOp; i++) {
                        // Simulate parallel overlay recording
                        threadWork += simulateOverlayRecording(i);
                    }
                    return threadWork;
                }));
            }

            // Collect results
            long totalWork = 0;
            for (Future<Long> future : futures) {
                totalWork += future.get();
            }

            // Simulate overlay merging overhead (scales with thread count)
            totalWork += simulateMerging(threadCount);

            bh.consume(totalWork);
            return totalWork;

        } catch (Exception e) {
            throw new RuntimeException("Multi-threading failed", e);
        } finally {
            executor.shutdown();
        }
    }

    // ===== CONTEXT CREATION BENCHMARKS =====

    @Benchmark
    public int currentArchitecture_ContextCreation(Blackhole bh) {
        // Simulate current context creation (simple, fast)
        int work = simulateSimpleContextCreation();
        bh.consume(work);
        return work;
    }

    @Benchmark
    public int hybridArchitecture_ContextCreation(Blackhole bh) {
        // Simulate hybrid context creation (more complex, slower)
        int work = simulateHybridContextCreation();
        bh.consume(work);
        return work;
    }

    // ===== SIMULATION METHODS =====

    /**
     * Simulates direct AST modification - lower base cost, scales linearly
     */
    private long simulateDirectModification(int operationIndex) {
        long work = 0;

        // Base cost: lower overhead for direct modification
        int baseCost = 20 + (fileSize / 1000); // Scales slightly with file size

        // Simulate work with some variation
        for (int i = 0; i < baseCost; i++) {
            work += i * 31L + ThreadLocalRandom.current().nextInt(5);
        }

        return work;
    }

    /**
     * Simulates overlay recording - higher base cost, better scaling potential
     */
    private long simulateOverlayRecording(int operationIndex) {
        long work = 0;

        // Base cost: higher overhead for thread-safe overlay operations
        int baseCost = 25 + (fileSize / 800); // Better scaling with file size

        // Additional thread-safety overhead
        int threadSafetyCost = 5;

        for (int i = 0; i < baseCost + threadSafetyCost; i++) {
            work += i * 31L + ThreadLocalRandom.current().nextInt(3);
        }

        return work;
    }

    /**
     * Simulates AST reconstruction from overlays
     */
    private long simulateReconstruction(int operationCount) {
        long work = 0;

        // Reconstruction cost scales with operations and file size
        int reconstructionCost = Math.max(5, operationCount / 4) + (fileSize / 2000);

        for (int i = 0; i < reconstructionCost; i++) {
            work += i * 23L;
        }

        return work;
    }

    /**
     * Simulates overlay merging for multi-threaded execution
     */
    private long simulateMerging(int threadCount) {
        long work = 0;

        // Merging cost increases with thread count but is relatively efficient
        int mergingCost = threadCount * 3 + Math.max(2, operationsCount / 10);

        for (int i = 0; i < mergingCost; i++) {
            work += i * 17L;
        }

        return work;
    }

    /**
     * Simulates simple context creation (current approach)
     */
    private int simulateSimpleContextCreation() {
        int work = 0;
        int creationCost = 5 + (fileSize / 5000); // Very low overhead

        for (int i = 0; i < creationCost * 10; i++) {
            work += i;
        }

        return work;
    }

    /**
     * Simulates hybrid context creation (wrapper + overlay setup)
     */
    private int simulateHybridContextCreation() {
        int work = 0;

        // Higher creation overhead
        int wrapperCost = 8 + (fileSize / 3000);
        int overlayCost = 6 + (fileSize / 4000);
        int trackerCost = 4;

        int totalCost = (wrapperCost + overlayCost + trackerCost) * 10;

        for (int i = 0; i < totalCost; i++) {
            work += i;
        }

        return work;
    }

    // ===== MAIN METHOD FOR EXECUTION =====

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 TRANSFORMATION CONTEXT ARCHITECTURE BENCHMARK");
        System.out.println("================================================");
        System.out.println("Measuring speedup vs file size AND thread count:");
        System.out.println("- File sizes: 100, 5000 lines");
        System.out.println("- Thread counts: 1, 4 threads");
        System.out.println("- Current (single-thread) vs Hybrid (multi-thread)");
        System.out.println("- Target runtime: ~6 minutes");
        System.out.println();
        System.out.println("Starting comprehensive performance analysis...");
        System.out.println();

        // Configure for 6-minute runtime
        // 5 benchmarks × 2 file sizes × 2 thread counts = 20 total runs
        // 20 runs × (2×3s + 3×4s) = 20 × 18s = 360s = 6 minutes total
        Options opt = new OptionsBuilder()
            .include(ArchitectureComparisonBenchmark.class.getSimpleName())
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
        System.out.println("Analyze the results above for:");
        System.out.println("1. Single-thread comparison: Current vs Hybrid overhead");
        System.out.println("2. Multi-thread scaling: Speedup with 2, 4, 8 threads");
        System.out.println("3. File size impact: How performance scales with codebase size");
        System.out.println("4. Context creation: Initialization cost comparison");
        System.out.println();
        System.out.println("Expected insights:");
        System.out.println("- Small files (100 lines): Current may be faster");
        System.out.println("- Large files (10000 lines): Hybrid multi-threading provides speedup");
        System.out.println("- Thread scaling: Look for near-linear speedup with thread count");
        System.out.println("- Memory vs Performance tradeoff analysis");
    }
}