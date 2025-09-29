package io.github.cowwoc.styler.benchmark;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.experimental.MutableFormattingContext;
import io.github.cowwoc.styler.formatter.experimental.ThreadLocalResourceTracker;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Performance benchmark comparing current FormattingContext vs. hybrid architecture.
 *
 * This benchmark suite measures:
 * 1. Single-threaded performance
 * 2. Multi-threaded scaling
 * 3. Memory overhead
 * 4. Large file processing
 * 5. Rule execution overhead
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
public class TransformationContextBenchmark {

    // Test data scenarios
    private ASTNode smallJavaFile;      // ~50 lines, 10 nodes
    private ASTNode mediumJavaFile;     // ~500 lines, 100 nodes
    private ASTNode largeJavaFile;      // ~5000 lines, 1000 nodes
    private ASTNode extraLargeJavaFile; // ~50000 lines, 10000 nodes

    private RuleConfiguration configuration;
    private Set<String> enabledRules;
    private Map<String, Object> metadata;
    private Path testFilePath;

    @Setup
    public void setup() {
        testFilePath = Paths.get("TestFile.java");
        configuration = new RuleConfiguration();
        enabledRules = Set.of("LineLength", "Indentation", "Whitespace");
        metadata = Map.of("version", "1.0");

        // Create test AST nodes of different sizes
        smallJavaFile = createMockAST(50, 10);
        mediumJavaFile = createMockAST(500, 100);
        largeJavaFile = createMockAST(5000, 1000);
        extraLargeJavaFile = createMockAST(50000, 10000);
    }

    // ===========================================
    // SINGLE-THREADED PERFORMANCE BENCHMARKS
    // ===========================================

    @Benchmark
    public void currentImplementation_SmallFile(Blackhole bh) {
        FormattingContext context = new FormattingContext(
            smallJavaFile, generateSourceText(50), testFilePath,
            configuration, enabledRules, metadata);

        // Simulate typical formatting operations
        performTypicalFormattingOperations(context, bh);
    }

    @Benchmark
    public void hybridImplementation_SmallFile(Blackhole bh) {
        ThreadLocalResourceTracker.initialize();
        try {
            MutableFormattingContext context = new MutableFormattingContext(
                smallJavaFile, generateSourceText(50), testFilePath,
                configuration, enabledRules, metadata);

            // Simulate typical formatting operations
            performHybridFormattingOperations(context, bh);
        } finally {
            ThreadLocalResourceTracker.cleanup();
        }
    }

    @Benchmark
    public void currentImplementation_MediumFile(Blackhole bh) {
        FormattingContext context = new FormattingContext(
            mediumJavaFile, generateSourceText(500), testFilePath,
            configuration, enabledRules, metadata);

        performTypicalFormattingOperations(context, bh);
    }

    @Benchmark
    public void hybridImplementation_MediumFile(Blackhole bh) {
        ThreadLocalResourceTracker.initialize();
        try {
            MutableFormattingContext context = new MutableFormattingContext(
                mediumJavaFile, generateSourceText(500), testFilePath,
                configuration, enabledRules, metadata);

            performHybridFormattingOperations(context, bh);
        } finally {
            ThreadLocalResourceTracker.cleanup();
        }
    }

    @Benchmark
    public void currentImplementation_LargeFile(Blackhole bh) {
        FormattingContext context = new FormattingContext(
            largeJavaFile, generateSourceText(5000), testFilePath,
            configuration, enabledRules, metadata);

        performTypicalFormattingOperations(context, bh);
    }

    @Benchmark
    public void hybridImplementation_LargeFile(Blackhole bh) {
        ThreadLocalResourceTracker.initialize();
        try {
            MutableFormattingContext context = new MutableFormattingContext(
                largeJavaFile, generateSourceText(5000), testFilePath,
                configuration, enabledRules, metadata);

            performHybridFormattingOperations(context, bh);
        } finally {
            ThreadLocalResourceTracker.cleanup();
        }
    }

    // ===========================================
    // MULTI-THREADED SCALING BENCHMARKS
    // ===========================================

    @Benchmark
    @Threads(2)
    public void hybridImplementation_MultiThreaded_2Threads(Blackhole bh) {
        runMultiThreadedHybridBenchmark(bh);
    }

    @Benchmark
    @Threads(4)
    public void hybridImplementation_MultiThreaded_4Threads(Blackhole bh) {
        runMultiThreadedHybridBenchmark(bh);
    }

    @Benchmark
    @Threads(8)
    public void hybridImplementation_MultiThreaded_8Threads(Blackhole bh) {
        runMultiThreadedHybridBenchmark(bh);
    }

    // ===========================================
    // MEMORY OVERHEAD BENCHMARKS
    // ===========================================

    @Benchmark
    public long currentImplementation_MemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();

        // Create multiple contexts to measure memory overhead
        for (int i = 0; i < 100; i++) {
            FormattingContext context = new FormattingContext(
                mediumJavaFile, generateSourceText(500), testFilePath,
                configuration, enabledRules, metadata);
            performTypicalFormattingOperations(context, null);
        }

        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        return afterMemory - beforeMemory;
    }

    @Benchmark
    public long hybridImplementation_MemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();

        // Create multiple contexts to measure memory overhead
        for (int i = 0; i < 100; i++) {
            ThreadLocalResourceTracker.initialize();
            try {
                MutableFormattingContext context = new MutableFormattingContext(
                    mediumJavaFile, generateSourceText(500), testFilePath,
                    configuration, enabledRules, metadata);
                performHybridFormattingOperations(context, null);
            } finally {
                ThreadLocalResourceTracker.cleanup();
            }
        }

        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        return afterMemory - beforeMemory;
    }

    // ===========================================
    // CONTEXT CREATION OVERHEAD BENCHMARKS
    // ===========================================

    @Benchmark
    public void currentImplementation_ContextCreation(Blackhole bh) {
        FormattingContext context = new FormattingContext(
            mediumJavaFile, generateSourceText(500), testFilePath,
            configuration, enabledRules, metadata);
        bh.consume(context);
    }

    @Benchmark
    public void hybridImplementation_ContextCreation(Blackhole bh) {
        ThreadLocalResourceTracker.initialize();
        try {
            MutableFormattingContext context = new MutableFormattingContext(
                mediumJavaFile, generateSourceText(500), testFilePath,
                configuration, enabledRules, metadata);
            bh.consume(context);
        } finally {
            ThreadLocalResourceTracker.cleanup();
        }
    }

    @Benchmark
    public void hybridImplementation_ChildContextCreation(Blackhole bh) {
        ThreadLocalResourceTracker.initialize();
        try {
            MutableFormattingContext parentContext = new MutableFormattingContext(
                mediumJavaFile, generateSourceText(500), testFilePath,
                configuration, enabledRules, metadata);

            // Benchmark child context creation (for parallel processing)
            List<ASTNode> blocks = getChildBlocks(mediumJavaFile);
            for (ASTNode block : blocks) {
                MutableFormattingContext childContext = parentContext.createChildContext(block);
                bh.consume(childContext);
            }
        } finally {
            ThreadLocalResourceTracker.cleanup();
        }
    }

    // ===========================================
    // PARALLEL PROCESSING SIMULATION
    // ===========================================

    @Benchmark
    public void hybridImplementation_ParallelBlockProcessing(Blackhole bh) {
        ThreadLocalResourceTracker.initialize();
        try {
            MutableFormattingContext parentContext = new MutableFormattingContext(
                largeJavaFile, generateSourceText(5000), testFilePath,
                configuration, enabledRules, metadata);

            // Simulate parallel block processing
            List<ASTNode> blocks = getChildBlocks(largeJavaFile);

            List<CompletableFuture<Integer>> futures = blocks.stream()
                .map(block -> CompletableFuture.supplyAsync(() -> {
                    ThreadLocalResourceTracker.initialize();
                    try {
                        MutableFormattingContext childContext = parentContext.createChildContext(block);
                        performHybridFormattingOperations(childContext, null);
                        return childContext.getModificationCount();
                    } finally {
                        ThreadLocalResourceTracker.cleanup();
                    }
                }))
                .toList();

            // Gather results
            int totalModifications = futures.stream()
                .mapToInt(CompletableFuture::join)
                .sum();

            bh.consume(totalModifications);
        } finally {
            ThreadLocalResourceTracker.cleanup();
        }
    }

    // ===========================================
    // HELPER METHODS
    // ===========================================

    private void runMultiThreadedHybridBenchmark(Blackhole bh) {
        ThreadLocalResourceTracker.initialize();
        try {
            MutableFormattingContext context = new MutableFormattingContext(
                mediumJavaFile, generateSourceText(500), testFilePath,
                configuration, enabledRules, metadata);

            performHybridFormattingOperations(context, bh);
        } finally {
            ThreadLocalResourceTracker.cleanup();
        }
    }

    private void performTypicalFormattingOperations(FormattingContext context, Blackhole bh) {
        // Simulate typical read operations
        ASTNode root = context.getRootNode();
        if (bh != null) bh.consume(root);

        // Simulate traversal and analysis
        traverseAST(root, bh);
    }

    private void performHybridFormattingOperations(MutableFormattingContext context, Blackhole bh) {
        // Simulate typical read operations
        ASTNode root = context.getRootNode();
        if (bh != null) bh.consume(root);

        // Simulate modifications
        List<ASTNode> children = context.getImmutableParent().getChildren(root);
        if (!children.isEmpty()) {
            // Simulate some modifications
            for (int i = 0; i < Math.min(10, children.size()); i++) {
                ASTNode child = children.get(i);
                ASTNode newChild = createMockAST(1, 1); // Small replacement
                context.replaceChild(root, child, newChild);
            }
        }

        if (bh != null) {
            bh.consume(context.getModificationCount());
            bh.consume(context.getResourceUsage());
        }
    }

    private void traverseAST(ASTNode node, Blackhole bh) {
        if (bh != null) bh.consume(node);

        for (ASTNode child : node.getChildren()) {
            traverseAST(child, bh);
        }
    }

    private ASTNode createMockAST(int lines, int nodeCount) {
        // Create a mock AST node for testing
        // This would need to be implemented with actual AST creation
        return new MockASTNode(lines, nodeCount);
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

    private List<ASTNode> getChildBlocks(ASTNode root) {
        // Simulate breaking AST into blocks for parallel processing
        return root.getChildren().stream()
            .limit(4) // Simulate 4 blocks for parallel processing
            .toList();
    }

    // Mock AST node for testing
    private static class MockASTNode extends ASTNode {
        private final int nodeCount;
        private final List<ASTNode> children;

        public MockASTNode(int lines, int nodeCount) {
            super(
                new io.github.cowwoc.styler.ast.SourceRange(
                    new io.github.cowwoc.styler.ast.SourcePosition(1, 1),
                    new io.github.cowwoc.styler.ast.SourcePosition(lines, 1)
                ),
                List.of(), List.of(),
                new io.github.cowwoc.styler.ast.WhitespaceInfo(),
                new io.github.cowwoc.styler.ast.FormattingHints(),
                java.util.Optional.empty()
            );
            this.nodeCount = nodeCount;
            this.children = IntStream.range(0, Math.min(nodeCount - 1, 5))
                .mapToObj(i -> new MockASTNode(1, 1))
                .map(node -> (ASTNode) node)
                .toList();
        }

        @Override
        public <R, A> R accept(io.github.cowwoc.styler.ast.visitor.ASTVisitor<R, A> visitor, A arg) {
            return null; // Simplified for benchmarking
        }

        @Override
        public io.github.cowwoc.styler.ast.builder.ASTNodeBuilder<? extends ASTNode> toBuilder() {
            return null; // Simplified for benchmarking
        }

        @Override
        public List<ASTNode> getChildren() {
            return children;
        }

        @Override
        protected ASTNode withMetadata(
            io.github.cowwoc.styler.ast.SourceRange newRange,
            List<io.github.cowwoc.styler.ast.Comment> newLeadingComments,
            List<io.github.cowwoc.styler.ast.Comment> newTrailingComments,
            io.github.cowwoc.styler.ast.WhitespaceInfo newWhitespace,
            io.github.cowwoc.styler.ast.FormattingHints newHints,
            java.util.Optional<ASTNode> newParent) {
            return this; // Simplified for benchmarking
        }
    }
}