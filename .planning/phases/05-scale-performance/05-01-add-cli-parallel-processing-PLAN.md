# Plan: add-cli-parallel-processing

## Objective
Use BatchProcessor for multi-file CLI operations to achieve 100+ files/sec throughput.

## Tasks
1. Modify CliMain.processFiles() to use BatchProcessor
2. Add --parallel flag (default: true)
3. Add --max-concurrency flag
4. Integrate progress callback for --verbose mode

## Dependencies
- implement-virtual-thread-processing (complete)
- BatchProcessor (complete)
- fix-classpath-scanner-per-file-overhead (complete)

## Verification
- [ ] Throughput improves from 27 files/sec to 100+ files/sec
- [ ] --parallel flag works correctly
- [ ] CLI integration tests pass
