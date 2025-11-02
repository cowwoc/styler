---
name: audit-protocol-efficiency
description: Analyze session execution patterns for efficiency improvements (execution time, token usage, quality)
allowed-tools: Read, Write, Bash, Grep, Skill
---

# Audit Protocol Efficiency Skill

**Purpose**: Identify opportunities to improve protocol efficiency by reducing execution time, token usage, and increasing deliverable quality through pattern analysis.

**When to Use**:
- After protocol compliance audit passes (no CRITICAL/HIGH violations)
- To optimize frequently executed workflow patterns
- When identifying bottlenecks in task execution
- For continuous protocol improvement

## MANDATORY PRECONDITION

**Check compliance first**:
```bash
# Run audit-protocol-compliance FIRST
Skill: audit-protocol-compliance

# IF violations contain CRITICAL or HIGH severity:
#   SKIP efficiency audit
#   OUTPUT: {
#     "status": "SKIPPED",
#     "reason": "Major protocol violations must be fixed before efficiency optimization"
#   }
# ELSE:
#   PROCEED with efficiency analysis
```

## Skill Workflow

**Overview**: Parse timeline → Analyze efficiency patterns → Recommend improvements → Quantify impact

### Phase 1: Get Structured Timeline

**Invoke parse-conversation-timeline skill**:
```bash
Skill: parse-conversation-timeline
```

### Phase 2: Analyze Execution Time Opportunities

**Query timeline for sequential operations that could be parallel**:

```bash
# Find sequential Task calls
jq '.timeline[] | select(.type == "tool_use" and .tool.name == "Task")' timeline.json

# Count messages containing Task tools
# IF Task tools spread across >1 message for same purpose:
#   → Parallelization opportunity

# Example: 3 separate messages for architect, quality, style
# → Should be single message with 3 parallel Task calls
# → Savings: (3 - 1) × avg_message_tokens
```

**Detect redundant verifications**:
```bash
# Find duplicate git status checks
jq '.timeline[] | select(.tool.name == "Bash" and (.tool.input.command | contains("git status")))' timeline.json

# Count occurrences
# IF > 3 git status calls in same state:
#   → Redundancy opportunity
```

**Identify late error detection**:
```bash
# Find validation after all work done
# Pattern: Multiple file creations followed by single checkstyle check
# Better: Validate after each component for fresh context
```

### Phase 3: Analyze Token Usage Opportunities

**Find large tool outputs**:
```bash
# Query timeline for tool results with large content
jq '.timeline[] | select(.type == "tool_result") | {tool_use_id, content_preview, estimated_tokens}' timeline.json

# IF tool result > 5000 tokens AND not essential:
#   → Context reduction opportunity
```

**Detect duplicate Read operations**:
```bash
# Find files read multiple times
jq '.timeline[] | select(.type == "tool_use" and .tool.name == "Read") | .tool.input.file_path' timeline.json | sort | uniq -c

# IF same file read 3+ times:
#   → Caching opportunity (read once, reference later)
```

**Identify late protocol file loading**:
```bash
# Find protocol document reads during IMPLEMENTATION (should be in INIT)
jq '.timeline[] | select(.type == "tool_use" and .tool.name == "Read" and (.tool.input.file_path | contains("docs/project")))' timeline.json

# IF protocol files loaded after INIT:
#   → Prefetching opportunity
```

### Phase 4: Analyze Quality Opportunities

**Find missing validation before major operations**:
```bash
# Pattern: Merge without prior checkstyle validation
# Pattern: Commit without verifying build success
# → Fail-fast opportunities
```

**Detect unclear protocol sequences**:
```bash
# Count retries, failed attempts, corrections
# IF high retry rate on specific operation:
#   → Clarity improvement opportunity
```

### Phase 5: Generate Recommendations

For EACH opportunity, provide:
- Specific protocol change
- Quantified impact (tokens, time, quality)
- Priority (HIGH/MEDIUM/LOW)
- Implementation guidance

## Output Format

```json
{
  "status": "COMPLETED",
  "precondition": {
    "compliance_check": "No CRITICAL/HIGH violations",
    "safe_to_optimize": true
  },
  "efficiency_opportunities": [
    {
      "id": "EFF-1",
      "category": "execution_time",
      "priority": "HIGH",
      "issue": "3 separate messages launching agents sequentially",
      "impact": "67% more round-trips than necessary",
      "recommended_changes": [
        {
          "type": "PROCEDURE",
          "file": "docs/project/main-agent-coordination.md",
          "section": "REQUIREMENTS phase",
          "change": "Add requirement: 'MUST launch all independent agents in single message with parallel Task calls'",
          "rationale": "Reduce message overhead"
        },
        {
          "type": "EXAMPLE",
          "file": "docs/project/main-agent-coordination.md",
          "section": "REQUIREMENTS phase",
          "change": "Add example showing single message with multiple Task calls",
          "rationale": "Provide clear implementation pattern"
        }
      ],
      "quantified_impact": {
        "token_savings": 8000,
        "time_savings": "67% fewer round-trips",
        "quality_impact": "None (parallelization maintains quality)"
      }
    },
    {
      "id": "EFF-2",
      "category": "quality",
      "priority": "MEDIUM",
      "issue": "Late validation after creating all files (53 violations)",
      "impact": "Fixes applied with stale context, 15% slower",
      "recommended_changes": [
        {
          "type": "PROCEDURE",
          "file": "docs/project/main-agent-coordination.md",
          "section": "IMPLEMENTATION best practices",
          "change": "Add fail-fast requirement: 'Validate after each logical component, not after all files'",
          "rationale": "Fresh context = faster fixes, prevent cascading errors"
        }
      ],
      "quantified_impact": {
        "token_savings": 0,
        "time_savings": "15% faster fixes",
        "quality_impact": "Earlier error detection prevents cascading issues"
      }
    },
    {
      "id": "EFF-3",
      "category": "token_usage",
      "priority": "HIGH",
      "issue": "Protocol files read during IMPLEMENTATION (should be INIT)",
      "impact": "4 extra round-trips for predictable dependencies",
      "recommended_changes": [
        {
          "type": "PROCEDURE",
          "file": "docs/project/task-protocol-core.md",
          "section": "INIT phase",
          "change": "Add prefetching requirement: 'Load all protocol files during INIT before agent invocation'",
          "rationale": "Avoid mid-execution reads that cause round-trips"
        }
      ],
      "quantified_impact": {
        "token_savings": 12000,
        "time_savings": "4 fewer round-trips per task",
        "quality_impact": "Better context for decision-making"
      }
    }
  ],
  "summary": {
    "total_opportunities": 3,
    "high_priority": 2,
    "medium_priority": 1,
    "estimated_total_token_savings": 20000,
    "estimated_time_improvement": "50% fewer round-trips per task"
  }
}
```

## Optimization Categories

### 1. Parallelization
**Pattern**: Independent operations executed sequentially
**Detection**: Multiple Read/Task calls in separate messages
**Fix**: Batch in single message
**Savings**: (messages_before - 1) × avg_tokens_per_message

### 2. Prefetching
**Pattern**: Late loading of predictable dependencies
**Detection**: Protocol files read during IMPLEMENTATION (not INIT)
**Fix**: Load all predictable files during INIT
**Savings**: Avoided round-trips × (read_tokens + response_tokens)

### 3. Fail-Fast Validation
**Pattern**: Validate after all work done (late error detection)
**Detection**: Single validation at end with many violations
**Fix**: Validate after each component
**Savings**: 15% time reduction from fresh context

### 4. Context Reduction
**Pattern**: Verbose outputs, duplicate reads
**Detection**: Large tool results, repeated file reads
**Fix**: Summarize outputs, cache reads
**Savings**: Reduced token count per message

### 5. Procedural Clarity
**Pattern**: Multiple retries, confusion, corrections
**Detection**: High retry rate on specific operations
**Fix**: Add clearer documentation, examples, fail-fast checks
**Savings**: Reduced rework iterations

## Quantification Methodology

**Token Savings**:
```
Parallelization: (messages_eliminated) × (avg_message_tokens)
Prefetching: (avoided_reads) × (read_tokens + response_tokens)
Context Reduction: (original_tokens - optimized_tokens) × (frequency)
```

**Time Savings**:
```
Round-trip reduction: (eliminated_round_trips) / (total_round_trips) × 100%
Fail-fast improvement: estimated 10-20% per iteration
```

**Quality Impact**:
```
Earlier error detection: Prevents cascading issues
Better context: Improved decision accuracy
Clearer procedures: Reduced confusion and retries
```

## Verification Checklist

Before outputting efficiency recommendations:
- [ ] Compliance audit passed (no CRITICAL/HIGH violations)
- [ ] Timeline analyzed for parallelization opportunities
- [ ] Token usage patterns identified
- [ ] Quality improvement opportunities found
- [ ] Each recommendation has quantified impact
- [ ] Priority assigned (HIGH/MEDIUM/LOW)
- [ ] Implementation guidance provided
- [ ] JSON is valid

## Related Skills

- **audit-protocol-compliance**: Must pass before efficiency audit
- **parse-conversation-timeline**: Provides structured data for analysis
- **/optimize-doc**: Optimizes documentation clarity (complementary, different purpose)

## Key Difference from /optimize-doc

- **audit-protocol-efficiency**: Optimizes EXECUTION patterns (how tasks are performed)
- **/optimize-doc**: Optimizes DOCUMENTATION clarity (how instructions are written)

Both are needed - this skill improves protocol procedures, /optimize-doc improves documentation readability.
