# Plan: create-jmh-benchmarks

## Objective
Validate performance claims with JMH benchmarks.

## Tasks
1. Create JMH benchmark module
2. Implement ParsingThroughputBenchmark (≥10,000 tokens/sec)
3. Implement MemoryUsageBenchmark (≤512MB per 1000 files)
4. Implement FormattingThroughputBenchmark (≥100 files/sec)
5. Implement CoreScalingBenchmark (≥60% efficiency at 8 cores)
6. Implement VirtualThreadComparisonBenchmark
7. Implement RealWorldProjectBenchmark (Spring, Guava, JUnit5)

## Dependencies
- implement-pipeline-stages (complete)
- create-maven-plugin (complete)
- implement-virtual-thread-processing (complete)
- add-cli-parallel-processing

## Prior Work (on branch create-jmh-benchmarks)
- JMH benchmark module implemented with 7 benchmark classes
- Memory validated: 351 MB per 1000 files
- CoreScalingBenchmark validated: 62.4% efficiency at 8 cores

## Verification
- [ ] All benchmarks pass with 95% confidence intervals
- [ ] Real-world projects parse and format successfully
