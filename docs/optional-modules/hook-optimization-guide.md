# Hook Optimization and Instrumentation Guide

> **Version:** 1.0
> **Last Updated:** 2025-11-11
> **Status:** Reference Documentation

## Overview

This guide documents performance optimization techniques and instrumentation for git and Claude hooks. Implements timing measurement, JSON caching, and shell optimization patterns to reduce hook execution overhead.

## Performance Baseline

**Problem**: Hook execution adds latency to every tool invocation and git operation.

**Typical Overhead**:
- Simple hooks: 10-30ms
- Complex hooks (multiple jq calls): 50-150ms
- Hooks with file scanning: 100-300ms
- Multiple hooks per trigger: Cumulative overhead

**Target**: Reduce hook overhead by 50-70% through caching and optimization.

## Optimization Libraries

### 1. Hook Timer (`lib/hook-timer.sh`)

**Purpose**: Measure hook execution time and identify bottlenecks.

**Usage**:
```bash
#!/bin/bash
source "$(dirname "$0")/lib/hook-timer.sh"

timer_start "my-hook"

# Hook logic here
timer_checkpoint "validation-complete"

# More logic
timer_checkpoint "processing-done"

timer_end "my-hook" "SUCCESS"
```

**Output**:
```
[2025-11-11T10:30:45Z] [my-hook] [validation-complete] 15ms (total: 15ms)
[2025-11-11T10:30:45Z] [my-hook] [processing-done] 8ms (total: 23ms)
[2025-11-11T10:30:45Z] [my-hook] [COMPLETE] 25ms [SUCCESS]
```

**Metrics Files**:
- `/workspace/.metrics/hook-timings.log` - Detailed checkpoint timings
- `/workspace/.metrics/hook-summary.jsonl` - Aggregated statistics

**Analysis Commands**:
```bash
# View statistics for specific hook
source .claude/hooks/lib/hook-timer.sh
timer_stats "enforce-checkpoints"

# View all hook statistics (sorted by total time)
timer_stats

# Generate performance report
timer_report
```

### 2. JSON Cache (`lib/json-cache.sh`)

**Purpose**: Eliminate redundant jq invocations by caching parsed JSON.

**Problem Pattern** (inefficient):
```bash
# Each line invokes jq separately
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id')
TOOL_PARAMS=$(echo "$INPUT" | jq -r '.tool_input')
# Result: 3 jq processes for same JSON
```

**Optimized Pattern**:
```bash
source "$(dirname "$0")/lib/json-cache.sh"

# Parse once, cache results
json_cache_parse_hook_input "$INPUT"

# Retrieve from cache (no jq calls)
TOOL_NAME=$(json_cache_get "hook_input" ".tool_name")
SESSION_ID=$(json_cache_get "hook_input" ".session_id")
TOOL_PARAMS=$(json_cache_get "hook_input" ".tool_input")
# Result: 1 jq process, 3 cache hits
```

**Performance**: 50-70% reduction in jq invocations for complex hooks.

**Cache Operations**:
```bash
# Store JSON in cache
json_cache_set "my_data" "$json_string"

# Retrieve value with jq query
value=$(json_cache_get "my_data" ".field.nested" "default_value")

# Load file directly to cache
json_cache_load_file "task_json" "/workspace/tasks/my-task/task.json"

# Check if cached
if json_cache_exists "my_data"; then
    # Use cached data
fi

# Clear specific cache
json_cache_clear "my_data"

# Clear all caches
json_cache_clear_all
```

**Convenience Functions**:
```bash
# Parse and cache hook input automatically
json_cache_parse_hook_input "$INPUT"

# Load and cache task.json with common queries
json_cache_load_task "/workspace/tasks/my-task/task.json"
STATE=$(json_cache_get "task_json" ".state")
```

### 3. Hook Optimization Utilities (`lib/hook-optimize.sh`)

**Purpose**: Optimized common operations for task context detection and file operations.

**Features**:
- Task context detection with caching
- Reduced subshell usage
- Batch file operations
- Pattern matching optimizations

**Usage**:
```bash
source "$(dirname "$0")/lib/hook-optimize.sh"

# Find task from current directory (cached)
task=$(find_task_from_pwd)

# Find task by session ID (cached, 5-second TTL)
task=$(find_task_by_session "$SESSION_ID")

# Get task state (uses JSON cache)
state=$(get_task_state "$task_name")

# Check if in task context
if is_task_context; then
    # Task-specific logic
fi

# Batch file existence checks
if all_files_exist "file1.txt" "file2.txt" "file3.txt"; then
    # All files exist
fi

if any_file_exists "file1.txt" "file2.txt"; then
    # At least one file exists
fi
```

## Shell Optimization Techniques

### 1. Reduce Subshell Invocations

**Before** (creates subshell):
```bash
task_name=$(basename "$task_dir")
```

**After** (uses bash built-in):
```bash
task_name="${task_dir##*/}"
```

**Impact**: 2-5ms saved per operation.

### 2. Pattern Matching Without External Commands

**Before** (spawns grep process):
```bash
if echo "$path" | grep -q "/workspace/tasks/"; then
```

**After** (bash regex):
```bash
if [[ "$path" =~ /workspace/tasks/ ]]; then
```

**Impact**: 3-8ms saved per check.

### 3. Capture Groups for Extraction

**Before** (multiple operations):
```bash
if echo "$path" | grep -q "/workspace/tasks/"; then
    task=$(echo "$path" | sed 's|/workspace/tasks/\([^/]*\)/.*|\1|')
fi
```

**After** (single regex with capture):
```bash
if [[ "$path" =~ /workspace/tasks/([^/]+) ]]; then
    task="${BASH_REMATCH[1]}"
fi
```

**Impact**: 5-10ms saved, more readable.

### 4. Batch Operations

**Before** (loop with multiple git calls):
```bash
for pattern in "${patterns[@]}"; do
    matches=$(git diff --cached --name-only | grep "$pattern" || true)
    # Process matches
done
```

**After** (single grep with combined pattern):
```bash
combined_pattern="(pattern1|pattern2|pattern3)"
all_matches=$(git diff --cached --name-only | grep -E "$combined_pattern" || true)
```

**Impact**: 30-50% faster for multiple patterns.

### 5. Parallel Execution

**Before** (sequential):
```bash
check_hardcoded_references
check_retrospective_docs
check_generic_javadoc
```

**After** (parallel):
```bash
check_hardcoded_references &
PID1=$!

check_retrospective_docs &
PID2=$!

check_generic_javadoc &
PID3=$!

wait $PID1 $PID2 $PID3
```

**Impact**: Near-linear speedup for independent checks.

## Complete Optimization Example

See `/workspace/.claude/hooks/enforce-checkpoints.sh` for a fully optimized hook implementation.

**Key Optimizations Applied**:
1. JSON caching for all input parsing
2. Timing instrumentation at checkpoints
3. Task context caching
4. Reduced jq invocations (from ~10 to ~3)
5. Bash built-ins instead of external commands

**Performance Result**: 60-70% faster than original implementation.

## Git Hook Optimizations

See `/workspace/main/.git/hooks/pre-commit-optimized` for optimized git hook.

**Optimizations Applied**:
1. **Cache staged files list**: Single `git diff` instead of multiple
2. **Parallel validation**: Independent checks run concurrently
3. **Combined regex patterns**: Single grep instead of loop
4. **Early exit**: Skip work when no files staged
5. **Timing instrumentation**: Measure performance at each stage

**Performance Result**: 30-50% faster for typical commits.

## Benchmarking

Use the benchmarking script to compare original vs optimized hooks:

```bash
# Benchmark specific hook
/workspace/.claude/scripts/benchmark-hooks.sh enforce-checkpoints 100

# Benchmark all optimized hooks
/workspace/.claude/scripts/benchmark-hooks.sh all 50
```

**Output Example**:
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Comparing: enforce-checkpoints
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Original version:
Benchmarking enforce-checkpoints (100 iterations)... Done
  Avg:   68ms | Min:   52ms | Max:   95ms | Total:  6800ms

Optimized version:
Benchmarking enforce-checkpoints (100 iterations)... Done
  Avg:   23ms | Min:   18ms | Max:   35ms | Total:  2300ms

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Performance Improvement: 66% (2.96x speedup)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

## Optimization Checklist

When creating or optimizing a hook:

- [ ] Source `hook-timer.sh` and add timing instrumentation
- [ ] Use `json-cache.sh` for all JSON parsing
- [ ] Source `hook-optimize.sh` for task context operations
- [ ] Replace external commands with bash built-ins where possible
- [ ] Use regex pattern matching instead of grep/sed
- [ ] Parallelize independent validation checks
- [ ] Cache repeated operations (file lists, JSON queries)
- [ ] Add early exit conditions to skip unnecessary work
- [ ] Benchmark against original to verify improvements

## Metrics and Monitoring

### Viewing Hook Performance

**Check timing logs**:
```bash
# Recent hook executions
tail -50 /workspace/.metrics/hook-timings.log

# Hooks sorted by execution time
cat /workspace/.metrics/hook-summary.jsonl | jq -s 'sort_by(.duration_ms) | reverse'
```

**Identify bottlenecks**:
```bash
# Find slowest checkpoints
grep checkpoint /workspace/.metrics/hook-timings.log | sort -t' ' -k5 -rn | head -20
```

**Generate report**:
```bash
source .claude/hooks/lib/hook-timer.sh
timer_report
cat /workspace/.metrics/timing-report.md
```

### Cache Statistics

```bash
source .claude/hooks/lib/json-cache.sh
json_cache_stats
```

**Output**:
```
JSON Cache Statistics:
  Cached documents: 5
  Cached queries: 23
  Cache hit ratio: 78%
```

## Best Practices

### 1. Always Instrument New Hooks

```bash
#!/bin/bash
source "$(dirname "$0")/lib/hook-timer.sh"
timer_start "my-new-hook"

# Hook logic
timer_checkpoint "key-operation"

timer_end "my-new-hook" "SUCCESS"
```

### 2. Cache JSON Early

```bash
# Parse input once at start
INPUT=$(cat)
json_cache_parse_hook_input "$INPUT"

# Use cached queries throughout
TOOL=$(json_cache_get "hook_input" ".tool_name")
```

### 3. Measure Before Optimizing

```bash
# Run benchmark before changes
./benchmark-hooks.sh my-hook 100 > before.txt

# Apply optimizations

# Run benchmark after changes
./benchmark-hooks.sh my-hook 100 > after.txt

# Compare results
diff before.txt after.txt
```

### 4. Profile Specific Sections

```bash
timer_checkpoint "before-expensive-operation"
# Expensive operation here
timer_checkpoint "after-expensive-operation"

# Later, check timing logs to see duration
grep "expensive-operation" /workspace/.metrics/hook-timings.log
```

## Common Performance Issues

### Issue 1: Multiple jq Calls on Same JSON

**Symptom**: Hook takes 50-100ms with multiple jq invocations.

**Solution**: Use JSON cache library.

**Before**: 10 jq calls = ~80ms
**After**: 1 jq call + cache = ~25ms (68% improvement)

### Issue 2: Repeated File System Scans

**Symptom**: Hook searches `/workspace/tasks` multiple times.

**Solution**: Use `find_task_by_session` with caching.

**Before**: 3 directory scans = ~45ms
**After**: 1 scan + cache (5s TTL) = ~15ms (67% improvement)

### Issue 3: Sequential Execution of Independent Checks

**Symptom**: Multiple validation checks run one after another.

**Solution**: Run checks in parallel with background jobs.

**Before**: 30ms + 25ms + 20ms = 75ms
**After**: max(30ms, 25ms, 20ms) = 30ms (60% improvement)

### Issue 4: Inefficient Pattern Matching

**Symptom**: Loops with external grep/sed commands.

**Solution**: Use bash regex and built-in string manipulation.

**Before**: Loop with grep = ~50ms
**After**: Bash regex = ~10ms (80% improvement)

## Migration Guide

To optimize an existing hook:

1. **Add timing instrumentation**:
   ```bash
   source "$(dirname "$0")/lib/hook-timer.sh"
   timer_start "hook-name"
   # ... existing code ...
   timer_end "hook-name"
   ```

2. **Run benchmark to establish baseline**:
   ```bash
   ./benchmark-hooks.sh hook-name 100
   ```

3. **Identify bottlenecks from timing logs**:
   ```bash
   grep "hook-name" /workspace/.metrics/hook-timings.log
   ```

4. **Apply optimizations** (JSON caching, bash built-ins, parallelization)

5. **Re-benchmark and verify improvement**:
   ```bash
   ./benchmark-hooks.sh hook-name 100
   ```

6. **Deploy optimized version** (rename or replace original)

## See Also

- [Hook Script Standards](../../CLAUDE.md#hook-script-standards) - Error handling requirements
- [Hook Registration](../../CLAUDE.md#hook-registration) - settings.json configuration
- `safe-remove-code` skill - Guided workflow for removing code patterns safely
