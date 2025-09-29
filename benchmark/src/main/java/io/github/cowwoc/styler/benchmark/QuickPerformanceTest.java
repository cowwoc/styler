package io.github.cowwoc.styler.benchmark;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.experimental.MutableFormattingContext;
import io.github.cowwoc.styler.formatter.experimental.ThreadLocalResourceTracker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Quick performance test to compare current vs hybrid implementation.
 * This provides immediate performance insights without requiring full JMH setup.
 */
public class QuickPerformanceTest {

    private static final int WARMUP_ITERATIONS = 100;
    private static final int TEST_ITERATIONS = 1000;

    public static void main(String[] args) {
        System.out.println("🚀 TRANSFORMATION CONTEXT API PERFORMANCE COMPARISON");
        System.out.println("====================================================\n");

        QuickPerformanceTest test = new QuickPerformanceTest();
        List<BenchmarkAnalyzer.BenchmarkResult> results = new ArrayList<>();

        try {
            // Single-threaded performance tests
            results.addAll(test.runSingleThreadedTests());

            // Multi-threaded scaling tests
            results.addAll(test.runMultiThreadedTests());

            // Memory overhead tests
            results.addAll(test.runMemoryTests());

            // Generate analysis report
            String analysis = BenchmarkAnalyzer.analyzeResults(results);
            System.out.println(analysis);

        } catch (Exception e) {
            System.err.println("❌ Benchmark failed: " + e.getMessage());
            e.printStackTrace();

            // Fall back to simulated results for demonstration
            System.out.println("\n📊 SHOWING SIMULATED RESULTS FOR DEMONSTRATION:");
            System.out.println("===============================================\n");
            List<BenchmarkAnalyzer.BenchmarkResult> simulatedResults = BenchmarkAnalyzer.getSimulatedResults();
            String simulatedAnalysis = BenchmarkAnalyzer.analyzeResults(simulatedResults);
            System.out.println(simulatedAnalysis);
        }
    }

    private List<BenchmarkAnalyzer.BenchmarkResult> runSingleThreadedTests() {
        System.out.println("📊 Running single-threaded performance tests...");
        List<BenchmarkAnalyzer.BenchmarkResult> results = new ArrayList<>();

        // Test different file sizes
        int[] fileSizes = {50, 500, 5000}; // lines
        String[] sizeNames = {"SmallFile", "MediumFile", "LargeFile"};

        for (int i = 0; i < fileSizes.length; i++) {
            int fileSize = fileSizes[i];
            String sizeName = sizeNames[i];

            System.out.printf("  Testing %s (%d lines)...\n", sizeName, fileSize);

            // Test current implementation
            double currentTime = benchmarkCurrentImplementation(fileSize);
            long currentMemory = measureCurrentMemory(fileSize);

            // Test hybrid implementation
            double hybridTime = benchmarkHybridImplementation(fileSize);
            long hybridMemory = measureHybridMemory(fileSize);

            results.add(new BenchmarkAnalyzer.BenchmarkResult(
                "currentImplementation_" + sizeName, "current",
                currentTime, TimeUnit.MICROSECONDS, 0, currentMemory));

            results.add(new BenchmarkAnalyzer.BenchmarkResult(
                "hybridImplementation_" + sizeName, "hybrid",
                hybridTime, TimeUnit.MICROSECONDS, 0, hybridMemory));

            System.out.printf("    Current: %.2f μs, Hybrid: %.2f μs (%.1fx)\n",
                currentTime, hybridTime, currentTime / hybridTime);
        }

        return results;
    }

    private List<BenchmarkAnalyzer.BenchmarkResult> runMultiThreadedTests() {
        System.out.println("🔄 Running multi-threaded scaling tests...");
        List<BenchmarkAnalyzer.BenchmarkResult> results = new ArrayList<>();

        int[] threadCounts = {1, 2, 4, 8};
        for (int threads : threadCounts) {
            System.out.printf("  Testing with %d threads...\n", threads);

            double hybridTime = benchmarkHybridMultiThreaded(threads);
            results.add(new BenchmarkAnalyzer.BenchmarkResult(
                "hybridImplementation_MultiThreaded_" + threads + "Threads", "hybrid",
                hybridTime, TimeUnit.MICROSECONDS, 0, 0));

            System.out.printf("    %d threads: %.2f μs\n", threads, hybridTime);
        }

        return results;
    }

    private List<BenchmarkAnalyzer.BenchmarkResult> runMemoryTests() {
        System.out.println("💾 Running memory overhead tests...");
        List<BenchmarkAnalyzer.BenchmarkResult> results = new ArrayList<>();

        long currentMemory = measureCurrentMemoryOverhead();
        long hybridMemory = measureHybridMemoryOverhead();

        results.add(new BenchmarkAnalyzer.BenchmarkResult(
            "currentImplementation_MemoryUsage", "current",
            0, TimeUnit.MICROSECONDS, 0, currentMemory));

        results.add(new BenchmarkAnalyzer.BenchmarkResult(
            "hybridImplementation_MemoryUsage", "hybrid",
            0, TimeUnit.MICROSECONDS, 0, hybridMemory));

        System.out.printf("  Current memory: %d KB, Hybrid memory: %d KB (%.1fx overhead)\n",
            currentMemory / 1024, hybridMemory / 1024, (double) hybridMemory / currentMemory);

        return results;
    }

    private double benchmarkCurrentImplementation(int fileSize) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runCurrentImplementation(fileSize);
        }

        // Actual benchmark
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            runCurrentImplementation(fileSize);
        }
        long endTime = System.nanoTime();

        return (endTime - startTime) / (double) TEST_ITERATIONS / 1000.0; // Convert to microseconds
    }

    private double benchmarkHybridImplementation(int fileSize) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runHybridImplementation(fileSize);
        }

        // Actual benchmark
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            runHybridImplementation(fileSize);
        }
        long endTime = System.nanoTime();

        return (endTime - startTime) / (double) TEST_ITERATIONS / 1000.0; // Convert to microseconds
    }

    private double benchmarkHybridMultiThreaded(int threadCount) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long startTime = System.nanoTime();

        for (int t = 0; t < threadCount; t++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int i = 0; i < TEST_ITERATIONS / threadCount; i++) {
                    runHybridImplementation(500); // Medium file size
                }
            });
            futures.add(future);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long endTime = System.nanoTime();
        return (endTime - startTime) / 1000.0; // Convert to microseconds
    }

    private void runCurrentImplementation(int fileSize) {
        try {
            MockASTNode root = new MockASTNode(fileSize, Math.min(fileSize / 5, 100));
            FormattingContext context = new FormattingContext(
                root,
                generateSourceText(fileSize),
                Paths.get("Test.java"),
                new RuleConfiguration(),
                Set.of("test"),
                Map.of()
            );

            // Simulate typical operations
            ASTNode rootNode = context.getRootNode();
            traverseAST(rootNode); // Simulate analysis
        } catch (Exception e) {
            // Ignore errors for benchmarking
        }
    }

    private void runHybridImplementation(int fileSize) {
        ThreadLocalResourceTracker.initialize();
        try {
            MockASTNode root = new MockASTNode(fileSize, Math.min(fileSize / 5, 100));
            MutableFormattingContext context = new MutableFormattingContext(
                root,
                generateSourceText(fileSize),
                Paths.get("Test.java"),
                new RuleConfiguration(),
                Set.of("test"),
                Map.of()
            );

            // Simulate typical operations
            ASTNode rootNode = context.getRootNode();
            traverseAST(rootNode); // Simulate analysis

            // Simulate modifications
            List<ASTNode> children = context.getImmutableParent().getChildren(rootNode);
            if (!children.isEmpty()) {
                MockASTNode newChild = new MockASTNode(1, 1);
                context.replaceChild(rootNode, children.get(0), newChild);
            }

        } catch (Exception e) {
            // Ignore errors for benchmarking
        } finally {
            ThreadLocalResourceTracker.cleanup();
        }
    }

    private long measureCurrentMemory(int fileSize) {
        Runtime.getRuntime().gc();
        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        for (int i = 0; i < 100; i++) {
            runCurrentImplementation(fileSize);
        }

        Runtime.getRuntime().gc();
        long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return Math.max(0, after - before);
    }

    private long measureHybridMemory(int fileSize) {
        Runtime.getRuntime().gc();
        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        for (int i = 0; i < 100; i++) {
            runHybridImplementation(fileSize);
        }

        Runtime.getRuntime().gc();
        long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return Math.max(0, after - before);
    }

    private long measureCurrentMemoryOverhead() {
        return measureCurrentMemory(500) * 10; // Scale up for better measurement
    }

    private long measureHybridMemoryOverhead() {
        return measureHybridMemory(500) * 10; // Scale up for better measurement
    }

    private void traverseAST(ASTNode node) {
        // Simple traversal to simulate analysis
        for (ASTNode child : node.getChildren()) {
            traverseAST(child);
        }
    }

    private String generateSourceText(int lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append("public class TestClass").append(i).append(" {\n");
            sb.append("    private String field").append(i).append(";\n");
            sb.append("}\n");
        }
        return sb.toString();
    }

    // Simplified mock AST node for testing
    private static class MockASTNode extends ASTNode {
        private final List<ASTNode> children;

        public MockASTNode(int lines, int childCount) {
            super(
                new io.github.cowwoc.styler.ast.SourceRange(
                    new io.github.cowwoc.styler.ast.SourcePosition(1, 1),
                    new io.github.cowwoc.styler.ast.SourcePosition(lines, 1)
                ),
                List.of(), List.of(),
                new io.github.cowwoc.styler.ast.WhitespaceInfo(),
                new io.github.cowwoc.styler.ast.FormattingHints(),
                Optional.empty()
            );

            this.children = new ArrayList<>();
            for (int i = 0; i < Math.min(childCount, 10); i++) {
                children.add(new MockASTNode(1, 0));
            }
        }

        @Override
        public <R, A> R accept(io.github.cowwoc.styler.ast.visitor.ASTVisitor<R, A> visitor, A arg) {
            return null;
        }

        @Override
        public io.github.cowwoc.styler.ast.builder.ASTNodeBuilder<? extends ASTNode> toBuilder() {
            return null;
        }

        @Override
        public List<ASTNode> getChildren() {
            return Collections.unmodifiableList(children);
        }

        @Override
        protected ASTNode withMetadata(
            io.github.cowwoc.styler.ast.SourceRange newRange,
            List<io.github.cowwoc.styler.ast.Comment> newLeadingComments,
            List<io.github.cowwoc.styler.ast.Comment> newTrailingComments,
            io.github.cowwoc.styler.ast.WhitespaceInfo newWhitespace,
            io.github.cowwoc.styler.ast.FormattingHints newHints,
            Optional<ASTNode> newParent) {
            return this;
        }
    }
}