# Benchmark Execution Guide

## Overview

The Styler project includes JMH (Java Microbenchmark Harness) benchmark infrastructure for measuring system performance.

**Current Status**: The `benchmark/system` module provides an **architectural template** for JMH benchmarks. The `ParsingThroughputBenchmark` class demonstrates the structure and configuration but requires Styler parser integration to produce meaningful measurements.

## Running Benchmarks

### Quick Start

Run all benchmarks:
```bash
mvn clean install -pl benchmark/system
cd benchmark/system
java --modulepath target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q) \
  org.openjdk.jmh.Main
```

### Maven Integration

The benchmarks are integrated with Maven via the `exec-maven-plugin`:

```bash
mvn integration-test -pl benchmark/system
```

## Benchmark Suite

### ParsingThroughputBenchmark

Measures tokenization throughput (tokens per second) for Java source files.

**Metrics**:
- Throughput (operations per second)
- Average time per operation

**Test Data**: Uses the project's own source code as test data

## Interpreting Results

JMH outputs results in the following format:

```
Benchmark                                  Mode  Cnt    Score    Error  Units
ParsingThroughputBenchmark.parseSmallFiles  thrpt   50  127.345 ±  3.210  ops/s
```

- **Mode**: Measurement mode (thrpt = throughput)
- **Cnt**: Number of measurement iterations
- **Score**: Average measurement result
- **Error**: Error margin (95% confidence interval)
- **Units**: Measurement units

## Configuration

Benchmark configuration is controlled via JMH annotations:

- `@Warmup`: Number of warmup iterations (default: 5)
- `@Measurement`: Number of measurement iterations (default: 10)
- `@Fork`: Number of JVM forks (default: 5)

## Performance Targets

Based on `scope.md` requirements:

- **Parser throughput**: ≥10,000 tokens/sec
- **Formatter throughput**: ≥100 files/sec
- **Memory efficiency**: ≤512MB per 1000 files

## Implementation Status

### Current State

The benchmark module provides:
- ✅ JMH framework integration (v1.37)
- ✅ Module structure following `benchmark/parser` pattern
- ✅ Maven build integration with exec-maven-plugin
- ✅ JPMS module descriptor
- ✅ Basic documentation

### Required for Functional Benchmarks

The following work is required to make benchmarks functional:

1. **Parser Integration** (4-6 hours):
   - Update `module-info.java`: `requires io.github.cowwoc.styler.parser;`
   - Replace `UnsupportedOperationException` in `countTokens()` with actual `JavaLexer` integration
   - See JavaDoc in `ParsingThroughputBenchmark.java` for implementation example

2. **Test Data Management** (2-3 hours):
   - Implement `TestDataProvider` utility for cached test data
   - Download real-world projects (Spring, Guava, Commons)
   - Create stratified file size distribution (small/medium/large)

3. **Additional Benchmark Scenarios** (6-8 hours):
   - FormattingThroughputBenchmark
   - MemoryUsageBenchmark
   - ScalabilityBenchmark
   - VirtualThreadComparisonBenchmark

4. **Statistical Analysis** (3-4 hours):
   - Implement `RegressionDetector` with baseline comparison
   - Add `BenchmarkReporter` for JSON/Markdown outputs
   - Create performance baselines

5. **CI/CD Integration** (1-2 hours):
   - Configure Maven profiles for opt-in execution
   - Add performance gates to CI pipeline

**Total Estimated Effort**: 16-23 hours

### Next Steps

See `todo.md` for the follow-up task: "Complete performance benchmark implementation"
