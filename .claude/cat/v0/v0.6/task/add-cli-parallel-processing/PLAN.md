# Task Plan: add-cli-parallel-processing

## Objective

Use BatchProcessor for multi-file CLI operations to achieve 100+ files/sec throughput.

## Tasks

1. Add maxConcurrency field to CLIOptions
2. Add --max-concurrency flag to ArgumentParser (default: calculated from memory)
3. Modify CliMain to use BatchProcessor for parallel file processing

## Dependencies

- implement-virtual-thread-processing (complete)
- BatchProcessor (complete)
- fix-classpath-scanner-per-file-overhead (complete)

## Verification

- [ ] Throughput improves from 27 files/sec to 100+ files/sec
- [ ] --max-concurrency=1 processes files sequentially
- [ ] CLI integration tests pass

