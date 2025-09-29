package io.github.cowwoc.styler.benchmark;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Analyzes benchmark results to compare current vs hybrid implementation.
 * Provides recommendations based on performance data.
 */
public class BenchmarkAnalyzer {

    public static class BenchmarkResult {
        private final String testName;
        private final String implementation;
        private final double averageTime;
        private final TimeUnit timeUnit;
        private final double throughput;
        private final long memoryUsage;

        public BenchmarkResult(String testName, String implementation, double averageTime,
                             TimeUnit timeUnit, double throughput, long memoryUsage) {
            this.testName = testName;
            this.implementation = implementation;
            this.averageTime = averageTime;
            this.timeUnit = timeUnit;
            this.throughput = throughput;
            this.memoryUsage = memoryUsage;
        }

        // Getters
        public String getTestName() { return testName; }
        public String getImplementation() { return implementation; }
        public double getAverageTime() { return averageTime; }
        public TimeUnit getTimeUnit() { return timeUnit; }
        public double getThroughput() { return throughput; }
        public long getMemoryUsage() { return memoryUsage; }
    }

    public static class PerformanceComparison {
        private final BenchmarkResult current;
        private final BenchmarkResult hybrid;
        private final double speedupFactor;
        private final double memoryOverhead;
        private final String recommendation;

        public PerformanceComparison(BenchmarkResult current, BenchmarkResult hybrid) {
            this.current = current;
            this.hybrid = hybrid;
            this.speedupFactor = current.getAverageTime() / hybrid.getAverageTime();
            this.memoryOverhead = (double) hybrid.getMemoryUsage() / current.getMemoryUsage();
            this.recommendation = generateRecommendation();
        }

        private String generateRecommendation() {
            StringBuilder rec = new StringBuilder();

            if (speedupFactor > 1.2) {
                rec.append("✅ HYBRID FASTER: ").append(String.format("%.1fx speedup", speedupFactor));
            } else if (speedupFactor < 0.8) {
                rec.append("❌ HYBRID SLOWER: ").append(String.format("%.1fx slower", 1/speedupFactor));
            } else {
                rec.append("⚖️ SIMILAR PERFORMANCE: ").append(String.format("%.1fx", speedupFactor));
            }

            if (memoryOverhead > 1.5) {
                rec.append(" | ⚠️ HIGH MEMORY OVERHEAD: ").append(String.format("%.1fx more memory", memoryOverhead));
            } else if (memoryOverhead > 1.2) {
                rec.append(" | 📊 MODERATE MEMORY OVERHEAD: ").append(String.format("%.1fx more memory", memoryOverhead));
            } else {
                rec.append(" | ✅ LOW MEMORY OVERHEAD: ").append(String.format("%.1fx memory", memoryOverhead));
            }

            return rec.toString();
        }

        public double getSpeedupFactor() { return speedupFactor; }
        public double getMemoryOverhead() { return memoryOverhead; }
        public String getRecommendation() { return recommendation; }
        public BenchmarkResult getCurrent() { return current; }
        public BenchmarkResult getHybrid() { return hybrid; }
    }

    /**
     * Analyzes a complete set of benchmark results and provides recommendations.
     */
    public static String analyzeResults(List<BenchmarkResult> results) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("# TRANSFORMATION CONTEXT API PERFORMANCE ANALYSIS\n\n");

        // Group results by test scenario
        Map<String, List<BenchmarkResult>> grouped = new HashMap<>();
        for (BenchmarkResult result : results) {
            String scenario = extractScenario(result.getTestName());
            grouped.computeIfAbsent(scenario, k -> new ArrayList<>()).add(result);
        }

        List<PerformanceComparison> comparisons = new ArrayList<>();

        // Analyze each scenario
        for (Map.Entry<String, List<BenchmarkResult>> entry : grouped.entrySet()) {
            String scenario = entry.getKey();
            List<BenchmarkResult> scenarioResults = entry.getValue();

            analysis.append("## ").append(scenario).append(" Performance\n\n");

            BenchmarkResult current = findImplementation(scenarioResults, "current");
            BenchmarkResult hybrid = findImplementation(scenarioResults, "hybrid");

            if (current != null && hybrid != null) {
                PerformanceComparison comparison = new PerformanceComparison(current, hybrid);
                comparisons.add(comparison);

                analysis.append("**Results:**\n");
                analysis.append(String.format("- Current: %.2f %s\n",
                    current.getAverageTime(), current.getTimeUnit().name().toLowerCase()));
                analysis.append(String.format("- Hybrid: %.2f %s\n",
                    hybrid.getAverageTime(), hybrid.getTimeUnit().name().toLowerCase()));
                analysis.append(String.format("- **Assessment: %s**\n\n", comparison.getRecommendation()));
            }
        }

        // Overall recommendation
        analysis.append("## OVERALL PERFORMANCE VERDICT\n\n");
        String overallRecommendation = generateOverallRecommendation(comparisons);
        analysis.append(overallRecommendation);

        return analysis.toString();
    }

    private static String extractScenario(String testName) {
        if (testName.contains("SmallFile")) return "Small File Processing";
        if (testName.contains("MediumFile")) return "Medium File Processing";
        if (testName.contains("LargeFile")) return "Large File Processing";
        if (testName.contains("MultiThreaded")) return "Multi-threaded Scaling";
        if (testName.contains("MemoryUsage")) return "Memory Overhead";
        if (testName.contains("ContextCreation")) return "Context Creation";
        if (testName.contains("ParallelBlock")) return "Parallel Block Processing";
        return "Other";
    }

    private static BenchmarkResult findImplementation(List<BenchmarkResult> results, String implementation) {
        return results.stream()
            .filter(r -> r.getImplementation().toLowerCase().contains(implementation))
            .findFirst()
            .orElse(null);
    }

    private static String generateOverallRecommendation(List<PerformanceComparison> comparisons) {
        if (comparisons.isEmpty()) {
            return "❓ **INSUFFICIENT DATA** - No valid comparisons found\n";
        }

        double avgSpeedup = comparisons.stream()
            .mapToDouble(PerformanceComparison::getSpeedupFactor)
            .average()
            .orElse(1.0);

        double avgMemoryOverhead = comparisons.stream()
            .mapToDouble(PerformanceComparison::getMemoryOverhead)
            .average()
            .orElse(1.0);

        long fasterCount = comparisons.stream()
            .mapToLong(c -> c.getSpeedupFactor() > 1.1 ? 1 : 0)
            .sum();

        long slowerCount = comparisons.stream()
            .mapToLong(c -> c.getSpeedupFactor() < 0.9 ? 1 : 0)
            .sum();

        StringBuilder recommendation = new StringBuilder();

        // Performance verdict
        if (fasterCount > slowerCount && avgSpeedup > 1.2) {
            recommendation.append("🚀 **HYBRID ARCHITECTURE RECOMMENDED**\n\n");
            recommendation.append(String.format("**Key Benefits:**\n"));
            recommendation.append(String.format("- Average speedup: %.1fx faster\n", avgSpeedup));
            recommendation.append(String.format("- Faster in %d/%d scenarios\n", fasterCount, comparisons.size()));
            recommendation.append("- Thread-safe parallel processing capability\n");
            recommendation.append("- Better scalability for large codebases\n\n");

            if (avgMemoryOverhead > 1.3) {
                recommendation.append("⚠️ **Trade-offs:**\n");
                recommendation.append(String.format("- Memory overhead: %.1fx more memory usage\n", avgMemoryOverhead));
                recommendation.append("- Higher complexity for maintenance\n\n");
            }

        } else if (slowerCount > fasterCount || avgSpeedup < 0.8) {
            recommendation.append("🔄 **CURRENT IMPLEMENTATION RECOMMENDED**\n\n");
            recommendation.append("**Reasons:**\n");
            recommendation.append(String.format("- Current implementation is %.1fx faster on average\n", 1/avgSpeedup));
            recommendation.append(String.format("- Slower hybrid performance in %d/%d scenarios\n", slowerCount, comparisons.size()));
            recommendation.append("- Lower memory overhead\n");
            recommendation.append("- Simpler codebase maintenance\n\n");

        } else {
            recommendation.append("⚖️ **MIXED RESULTS - CONTEXT-DEPENDENT CHOICE**\n\n");
            recommendation.append("**Analysis:**\n");
            recommendation.append(String.format("- Performance is similar (%.1fx average)\n", avgSpeedup));
            recommendation.append(String.format("- Memory overhead: %.1fx\n", avgMemoryOverhead));
            recommendation.append("\n**Recommendation depends on use case:**\n");
            recommendation.append("- Choose **HYBRID** for: Multi-threaded workloads, large codebases, future scalability\n");
            recommendation.append("- Choose **CURRENT** for: Single-threaded use, memory-constrained environments, simplicity\n\n");
        }

        // Threading consideration
        boolean hasMultiThreadData = comparisons.stream()
            .anyMatch(c -> c.getCurrent().getTestName().contains("MultiThreaded"));

        if (hasMultiThreadData) {
            recommendation.append("**Multi-threading Analysis:**\n");
            recommendation.append("- Hybrid architecture enables parallel processing\n");
            recommendation.append("- Current implementation is single-threaded only\n");
            recommendation.append("- For multi-core utilization, hybrid is the only viable option\n\n");
        }

        recommendation.append("---\n");
        recommendation.append("*This analysis is based on microbenchmarks. Real-world performance may vary.*");

        return recommendation.toString();
    }

    /**
     * Simulated benchmark results for demonstration.
     * In real usage, this would be populated from actual JMH results.
     */
    public static List<BenchmarkResult> getSimulatedResults() {
        return List.of(
            // Small file processing
            new BenchmarkResult("currentImplementation_SmallFile", "current", 45.2, TimeUnit.MICROSECONDS, 0, 1024),
            new BenchmarkResult("hybridImplementation_SmallFile", "hybrid", 52.8, TimeUnit.MICROSECONDS, 0, 1536),

            // Medium file processing
            new BenchmarkResult("currentImplementation_MediumFile", "current", 285.7, TimeUnit.MICROSECONDS, 0, 8192),
            new BenchmarkResult("hybridImplementation_MediumFile", "hybrid", 198.3, TimeUnit.MICROSECONDS, 0, 12288),

            // Large file processing
            new BenchmarkResult("currentImplementation_LargeFile", "current", 2847.6, TimeUnit.MICROSECONDS, 0, 65536),
            new BenchmarkResult("hybridImplementation_LargeFile", "hybrid", 1923.4, TimeUnit.MICROSECONDS, 0, 98304),

            // Memory usage
            new BenchmarkResult("currentImplementation_MemoryUsage", "current", 0, TimeUnit.MICROSECONDS, 0, 524288),
            new BenchmarkResult("hybridImplementation_MemoryUsage", "hybrid", 0, TimeUnit.MICROSECONDS, 0, 786432),

            // Context creation
            new BenchmarkResult("currentImplementation_ContextCreation", "current", 12.4, TimeUnit.MICROSECONDS, 0, 256),
            new BenchmarkResult("hybridImplementation_ContextCreation", "hybrid", 18.7, TimeUnit.MICROSECONDS, 0, 384)
        );
    }
}