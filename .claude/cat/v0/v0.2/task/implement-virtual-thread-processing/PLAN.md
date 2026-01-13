# Task Plan: implement-virtual-thread-processing

## Objective

Build parallel file processing using virtual threads for throughput.

## Context

Need 100+ files/sec throughput. Virtual threads (JDK 21+) provide
efficient concurrency for I/O-bound file processing.

## Tasks

1. Create BatchProcessor with virtual thread executor
2. Implement FileProcessingPipeline with parallel stages
3. Add progress reporting for verbose mode
4. Handle thread-safe result aggregation

## Verification

- [ ] 100+ files/sec throughput achieved
- [ ] Thread-safe processing
- [ ] Progress callback works
- [ ] Errors handled per-file

## Files

- `pipeline/src/main/java/.../pipeline/BatchProcessor.java`
- `pipeline/src/main/java/.../pipeline/FileProcessingPipeline.java`

