# Plan: benchmark-concurrency-models

## Objective
Compare thread-per-file vs thread-per-block parallelism.

## Tasks
1. Set up benchmark infrastructure for both models
2. Benchmark thread-per-file (baseline)
3. Implement and benchmark thread-per-block variant
4. Compare metrics: throughput, memory, CPU utilization
5. Test across file size variations and concurrency levels

## Dependencies
- implement-pipeline-stages (complete)
- implement-virtual-thread-processing (complete)

## Decision Criteria
Implement thread-per-block only if >20% improvement over thread-per-file baseline.

## Verification
- [ ] Statistical rigor with JMH methodology
- [ ] Clear recommendation with benchmark report
