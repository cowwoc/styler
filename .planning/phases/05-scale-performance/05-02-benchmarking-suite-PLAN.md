# Plan: benchmarking-suite

## Objective
Create comprehensive JMH benchmarks to validate performance claims and compare approaches.

## Tasks

### Part A: JMH Benchmarks
1. Create JMH benchmark module
2. Implement ParsingThroughputBenchmark (≥10,000 tokens/sec)
3. Implement MemoryUsageBenchmark (≤512MB per 1000 files)
4. Implement FormattingThroughputBenchmark (≥100 files/sec)
5. Implement CoreScalingBenchmark (≥60% efficiency at 8 cores)
6. Implement VirtualThreadComparisonBenchmark
7. Implement RealWorldProjectBenchmark (Spring, Guava, JUnit5)

### Part B: Concurrency Model Comparison
1. Set up benchmark infrastructure for both models
2. Benchmark thread-per-file (baseline)
3. Implement and benchmark thread-per-block variant
4. Compare metrics: throughput, memory, CPU utilization
5. Test across file size variations and concurrency levels

### Part C: Tool Comparison
1. Set up equivalent rule configurations across tools
2. Benchmark line length checking
3. Benchmark import ordering
4. Benchmark brace placement
5. Compare throughput, memory usage, startup time

## Dependencies
- implement-pipeline-stages (complete)
- create-maven-plugin (complete)
- implement-virtual-thread-processing (complete)
- add-cli-parallel-processing

## Prior Work (on branch create-jmh-benchmarks)
- JMH benchmark module implemented with 7 benchmark classes
- Memory validated: 351 MB per 1000 files
- CoreScalingBenchmark validated: 62.4% efficiency at 8 cores

## Decision Criteria
Implement thread-per-block only if >20% improvement over thread-per-file baseline.

## Verification
- [ ] All benchmarks pass with 95% confidence intervals
- [ ] Real-world projects parse and format successfully
- [ ] Statistical rigor with JMH methodology
- [ ] Clear recommendation with benchmark report
- [ ] Fair comparison with equivalent configurations
- [ ] Styler is competitive with established tools
