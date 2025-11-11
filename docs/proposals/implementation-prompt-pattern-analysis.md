# Implementation Prompt: Cross-Session Pattern Analysis System

> **Purpose**: Standalone prompt for Claude instances to implement cross-session pattern learning
> **Created**: 2025-11-11
> **Target**: Fresh Claude session with no prior context
> **⭐ FULLY SELF-CONTAINED**: All information needed is in this single file

---

## 📋 TASK OVERVIEW

You are implementing a **cross-session pattern analysis system** for a Claude Code project that learns from historical usage patterns to proactively suggest batch operations, reducing round-trips by 50-70%.

**Current State**: Sequential tool usage detection may or may not exist yet
**Target State**: System predicts patterns *proactively* (suggests batch before 1st call)

**Your Deliverables**:
1. Implement `extract-patterns` skill (parse session logs for patterns)
2. Implement `update-pattern-database` skill (aggregate patterns across sessions)
3. Create `pattern-database.json` schema (store cross-session patterns)
4. Implement `predict-batch-opportunity.sh` hook (proactive suggestions)
5. Test system on historical sessions
6. Document findings and accuracy metrics

---

## 🏗️ PROJECT CONTEXT

### Repository Structure

```
/workspace/main/                           # Main git repository
├── .claude/
│   ├── hooks/
│   │   └── predict-batch-opportunity.sh  # Proactive prediction (TO BUILD)
│   ├── skills/
│   │   ├── extract-patterns/             # Pattern extraction (TO BUILD)
│   │   └── update-pattern-database/      # Database updates (TO BUILD)
│   └── pattern-database.json             # Cross-session patterns (TO CREATE)
└── docs/
    └── proposals/
        └── [this file]                    # Complete implementation guide
```

**Note**: Other Claude Code infrastructure (skills, hooks, scripts) may or may not exist in your environment. This prompt is self-contained and doesn't depend on them.

### Session ID Location

Session IDs are provided at SessionStart via system reminder:
```
✅ Session ID: 6b6da2b0-cabd-41e4-baf2-c809b5406120
```

Session conversation logs stored at:
```
~/.config/projects/-workspace/{session-id}.jsonl
```

---

## 📖 SYSTEM ARCHITECTURE OVERVIEW

### The Problem

Current state: Claude instances execute operations sequentially
- User: Read file1 → Wait → Read file2 → Wait → Read file3
- Result: 3 round-trips = ~7.5 seconds, 12,000 tokens

Desired state: System learns patterns and suggests batch operations
- System: "Pattern detected! Use batch-read skill instead"
- Result: 1 round-trip = ~2.5 seconds, 4,000 tokens
- Savings: 67% time, 67% tokens

### The Solution: 3-Component System

**Component 1: Pattern Extraction**
- Parse session conversation logs (~/.config/projects/-workspace/{session-id}.jsonl)
- Identify recurring sequences (Read → Read → Read, Task → Task → Task)
- Generalize file paths (src/main/java/Foo.java → **/*.java)
- Output: Structured pattern JSON with metadata

**Component 2: Pattern Database**
- Aggregate patterns across ALL sessions (not just current)
- Track frequency (how many times seen), confidence, last_seen
- Calculate impact metrics (time savings, token savings)
- Prune low-frequency patterns (< 3 occurrences in 30 days)
- Store at: `/workspace/main/.claude/pattern-database.json`

**Component 3: Proactive Prediction**
- Hook triggers BEFORE tool execution (PreToolUse)
- Check if current tool matches start of known pattern
- If high confidence (>0.8) and high frequency (>10): Show suggestion
- Track prediction accuracy (true positives vs false positives)

### Session Conversation Log Format

Sessions are stored as JSONL (JSON Lines) at `~/.config/projects/-workspace/{session-id}.jsonl`

**Key entry types**:
```json
{"type":"message","role":"user","content":"Read file1.md","timestamp":1730331600000}
{"type":"message","role":"assistant","content":"I'll read that...","timestamp":1730331610000}
{"type":"tool_use","name":"Read","input":{"file_path":"file1.md"},"timestamp":1730331615000}
{"type":"tool_result","tool_use_id":"xyz","content":"[file contents]","timestamp":1730331618000}
```

You'll parse these to extract operation sequences and timing.

### Hook Script Standards

All hooks MUST include this boilerplate:
```bash
#!/bin/bash
set -euo pipefail

trap 'echo "ERROR in script-name.sh at line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Rest of script...
```

**Why**:
- `set -euo pipefail`: Exit on errors, undefined variables, pipe failures
- `trap`: Catch errors and output diagnostic information to stderr
- Stderr (`>&2`): Required for Claude Code to see hook errors

### Skill Documentation Standards

All skills MUST use this frontmatter:
```yaml
---
name: skill-name
description: Clear description under 100 chars
allowed-tools: Tool1, Tool2, Tool3
---

# Skill Title

**Purpose**: [Clear purpose statement]
**When to Use**: [3-5 bullet points]

## Skill Workflow
[Detailed steps...]
```

---

## 🎯 IMPLEMENTATION TASKS

### Phase 1: Pattern Extraction (Week 1)

#### Task 1.1: Create extract-patterns Skill

**Location**: `/workspace/main/.claude/skills/extract-patterns/SKILL.md`

**Requirements**:
- Parse timeline JSON (from parse-conversation-timeline skill)
- Detect sequential tool patterns (Read → Read → Read)
- Detect tool + validation patterns (Write → Bash compile)
- Detect agent invocation patterns (Task → Task → Task)
- Generalize file paths to patterns (src/.../Foo.java → **/*.java)
- Output structured JSON with pattern metadata

**Algorithm** (from design doc § Component 6):
```python
def extract_patterns(timeline):
    patterns = []
    window_size = 10

    for i in range(len(timeline) - window_size):
        window = timeline[i:i+window_size]
        ops = extract_operations(window)

        if is_sequential_reads(ops):
            patterns.append(create_pattern("sequential_reads", ops))
        elif is_write_validate(ops):
            patterns.append(create_pattern("write_validate", ops))
        elif is_agent_invocation_sequential(ops):
            patterns.append(create_pattern("agent_invocation", ops))

    return patterns
```

**Output Format**:
```json
{
  "session_id": "...",
  "extracted_patterns": [
    {
      "pattern_id": "read-multiple-protocol-files",
      "type": "sequential_reads",
      "sequence": [
        {"tool": "Read", "file_pattern": "docs/project/*.md", "delay_ms": 2500},
        {"tool": "Read", "file_pattern": "docs/project/*.md", "delay_ms": 2300}
      ],
      "context": {
        "state": "INIT",
        "purpose": "loading_protocol_documentation"
      },
      "potential_batching": true,
      "estimated_savings_ms": 4600
    }
  ]
}
```

**Test Cases**:
- Sequential reads of protocol files
- Sequential agent invocations
- Write + compile + checkstyle sequences
- Grep + multiple reads sequences

#### Task 1.2: Create Pattern Database Schema

**Location**: `/workspace/main/.claude/pattern-database.json`

**Schema** (from design doc § Component 7):
```json
{
  "version": "1.0",
  "last_updated": "2025-11-11T10:30:00Z",
  "metadata": {
    "total_sessions_analyzed": 0,
    "total_patterns_tracked": 0
  },
  "patterns": {},
  "anti_patterns": []
}
```

**Initialize**:
- Create empty database with schema
- Add version tracking
- Add metadata section

#### Task 1.3: Test on Historical Session

**Process**:
```bash
# Step 1: Get a session ID (look for recent task execution)
ls -lt ~/.config/projects/-workspace/*.jsonl | head -5

# Step 2: Parse timeline
Skill: parse-conversation-timeline
# Input: session_id from step 1

# Step 3: Extract patterns
Skill: extract-patterns
# Input: timeline.json from step 2

# Step 4: Review extracted patterns
# Verify patterns make sense
# Check pattern generalization (paths → patterns)
```

**Success Criteria**:
- Extracts at least 3 pattern types
- Correctly generalizes file paths
- Identifies batchable vs non-batchable patterns
- Calculates estimated savings

---

### Phase 2: Database Updates (Week 1-2)

#### Task 2.1: Create update-pattern-database Skill

**Location**: `/workspace/main/.claude/skills/update-pattern-database/SKILL.md`

**Requirements**:
- Load existing pattern-database.json
- Merge new patterns from extract-patterns
- Update frequency counts
- Track first_seen and last_seen timestamps
- Calculate prediction accuracy (if predictions exist)
- Prune low-frequency patterns (< 3 occurrences in 30 days)
- Save updated database

**Algorithm** (from design doc § Component 9):
```python
def update_pattern_database(new_patterns):
    db = load_database()

    for pattern in new_patterns:
        signature = pattern["pattern_signature"]

        if signature in db["patterns"]:
            # Update existing
            db["patterns"][signature]["frequency"]["total_occurrences"] += 1
            db["patterns"][signature]["frequency"]["last_seen"] = now()
        else:
            # Create new
            db["patterns"][signature] = initialize_pattern(pattern)

    # Prune old patterns
    db["patterns"] = prune_low_frequency(db["patterns"])

    save_database(db)
```

**Test**:
- Run on 5 historical sessions
- Verify patterns aggregate correctly
- Check frequency counts increase
- Verify pruning works

#### Task 2.2: Populate Database with Historical Data

**Process**:
```bash
# Get list of recent sessions (last 30 days recommended)
ls -lt ~/.config/projects/-workspace/*.jsonl | head -20

# For each session:
# 1. Parse timeline
# 2. Extract patterns
# 3. Update database

# Create script to automate this:
for session_file in ~/.config/projects/-workspace/*.jsonl; do
  session_id=$(basename "$session_file" .jsonl)
  echo "Processing session: $session_id"

  # Parse timeline
  Skill: parse-conversation-timeline
  # Extract patterns
  Skill: extract-patterns
  # Update database
  Skill: update-pattern-database
done
```

**Success Criteria**:
- Database contains 10+ patterns
- Patterns have realistic frequency counts
- No duplicate pattern entries
- Database size reasonable (< 500KB)

---

### Phase 3: Proactive Prediction (Week 2)

#### Task 3.1: Create predict-batch-opportunity.sh Hook

**Location**: `/workspace/main/.claude/hooks/predict-batch-opportunity.sh`

**Requirements**:
- Trigger on PreToolUse for Read, Glob, Grep, Task, WebFetch
- Load pattern-database.json
- Match current tool call against pattern starts
- Check confidence and frequency thresholds
- Display suggestion if high confidence
- Track prediction (for accuracy measurement)
- Follow hook script standards (set -euo pipefail, trap, stderr)

**Algorithm** (from design doc § Component 8):
```bash
#!/bin/bash
set -euo pipefail

trap 'echo "ERROR in predict-batch-opportunity.sh at line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

PATTERN_DB="/workspace/main/.claude/pattern-database.json"
INPUT=$(cat)

TOOL_NAME=$(echo "$INPUT" | jq -r '.tool.name')

# Find patterns starting with this tool
MATCHING_PATTERNS=$(jq --arg tool "$TOOL_NAME" \
  '.patterns[] | select(.pattern_signature | startswith($tool))' \
  "$PATTERN_DB")

if [ -n "$MATCHING_PATTERNS" ]; then
  CONFIDENCE=$(echo "$MATCHING_PATTERNS" | jq -r '.batchability.confidence')
  FREQUENCY=$(echo "$MATCHING_PATTERNS" | jq -r '.frequency.total_occurrences')

  if (( $(echo "$CONFIDENCE > 0.8" | bc -l) )) && (( FREQUENCY > 10 )); then
    # Show suggestion
    cat <<EOF
## 💡 BATCH OPPORTUNITY DETECTED
[suggestion details...]
EOF
  fi
fi

exit 0
```

**Hook Registration** (add to `.claude/settings.json`):
```json
"PreToolUse": [
  {
    "matcher": "Read|Glob|Grep|Task|WebFetch",
    "hooks": [
      {
        "type": "command",
        "command": "/workspace/.claude/hooks/predict-batch-opportunity.sh"
      }
    ]
  }
]
```

**Test**:
- Restart Claude Code (required for settings.json changes)
- Execute Read tool (should check pattern database)
- Execute Task tool (should check for agent patterns)
- Verify suggestions appear for high-frequency patterns
- Verify no suggestions for low-frequency patterns

#### Task 3.2: Track Prediction Accuracy

**Requirements**:
- When prediction made, log it to temp file
- When session completes, check if pattern actually occurred
- Update pattern database with accuracy metrics
- Calculate true positives, false positives, false negatives

**Implementation**:
```bash
# In predict-batch-opportunity.sh, when showing suggestion:
PREDICTION_LOG="/tmp/pattern-predictions-$SESSION_ID.json"
echo "$MATCHING_PATTERN" >> "$PREDICTION_LOG"

# In update-pattern-database skill:
# Check if predicted patterns actually occurred
# Update prediction_accuracy metrics in database
```

---

### Phase 4: Validation & Documentation (Week 2-3)

#### Task 4.1: Validate System End-to-End

**Requirements**:
1. **Test Full Cycle**: Extract → Update → Predict → Measure accuracy
2. **Verify Suggestions Appear**: Hook triggers and shows relevant patterns
3. **Check Database Growth**: Patterns accumulate correctly over time
4. **Measure Impact**: Calculate actual token/time savings

**Validation Steps**:
```bash
# 1. Run pattern extraction on 10+ historical sessions
# 2. Verify database contains 10+ patterns
# 3. Trigger prediction hook with Read tool
# 4. Confirm suggestion appears for high-frequency patterns
# 5. Track prediction accuracy over multiple sessions
```

#### Task 4.2: Create Pattern Visualization Queries

**Purpose**: Make pattern database queryable for insights

**Queries to Create**:
```bash
# Most impactful patterns
jq '.patterns | sort_by(.impact.time_savings_ms) | reverse | .[0:5]' \
  pattern-database.json

# Patterns needing batch skills
jq '.patterns[] | select(.frequency.total_occurrences > 15 and
    .suggested_batch_operation.implemented == false)' \
  pattern-database.json

# Average prediction accuracy
jq '.patterns | map(.prediction_accuracy.accuracy_rate) | add / length' \
  pattern-database.json

# Emerging patterns (recent but not frequent)
jq '.patterns[] | select(.frequency.total_occurrences < 10 and
    (now - (.frequency.last_seen | fromdateiso8601)) < 604800)' \
  pattern-database.json
```

**Documentation**:
- Add queries to pattern-database.md
- Create examples with sample output
- Document interpretation guide

---

## 🧪 TESTING & VALIDATION

### Test Suite Requirements

#### Unit Tests

**Test 1: Pattern Extraction**
- Input: Timeline JSON with 3 sequential Read calls
- Expected: Extract "sequential_reads" pattern
- Verify: Correct delay_ms, file_pattern generalization

**Test 2: Pattern Matching**
- Input: Tool call "Read: docs/architecture.md"
- Expected: Match pattern "read-multiple-protocol-files"
- Verify: Confidence > 0.8, show suggestion

**Test 3: Database Update**
- Input: New pattern + existing database
- Expected: Increment frequency, update last_seen
- Verify: No duplicates, correct counts

**Test 4: Pattern Pruning**
- Input: Pattern with 2 occurrences, last_seen 45 days ago
- Expected: Pattern removed from database
- Verify: Active patterns remain

#### Integration Tests

**Test 5: End-to-End Pattern Learning**
```bash
# Session 1: Execute sequential reads
Read: file1.md
Read: file2.md
Read: file3.md
# Extract patterns, update database

# Session 2: Start to execute Read
Read: file1.md
# Expected: Hook suggests batch operation
# Verify: Suggestion appears with correct frequency
```

**Test 6: Prediction Accuracy Tracking**
```bash
# Session with prediction
# Prediction: "You'll likely read 3 files"
# Actual: User reads 3 files
# Expected: Accuracy updated to true positive

# Session with false positive
# Prediction: "You'll likely read 3 files"
# Actual: User reads 1 file only
# Expected: Accuracy updated to false positive
```

#### Performance Tests

**Test 7: Database Query Performance**
- Load database with 100 patterns
- Query for matching patterns
- Expected: < 100ms response time

**Test 8: Hook Execution Performance**
- Trigger predict-batch-opportunity.sh
- Expected: < 50ms execution time
- Verify: Doesn't block tool execution

### Validation Criteria

#### Pattern Extraction Accuracy
- ✅ Correctly identifies 90%+ of sequential tool patterns
- ✅ Generalizes file paths appropriately (no overfitting)
- ✅ Distinguishes batchable from non-batchable patterns

#### Database Quality
- ✅ No duplicate patterns
- ✅ Frequency counts accurate
- ✅ Pattern signatures unique and descriptive
- ✅ Timestamps in ISO 8601 format

#### Prediction Quality
- ✅ Confidence thresholds prevent false positives (< 10%)
- ✅ High-frequency patterns trigger suggestions (> 10 occurrences)
- ✅ Suggestions are actionable (reference existing skills or provide commands)
- ✅ Prediction accuracy tracked and > 80%

#### Hook Behavior
- ✅ Follows hook script standards (error handling, stderr)
- ✅ Registered in settings.json correctly
- ✅ Triggers on correct tools only
- ✅ Doesn't block or slow down normal operations

---

## 📊 SUCCESS METRICS

### Adoption Metrics

Track these over 30 days:
- **Prediction Acceptance Rate**: % of predictions user follows
  - Target: > 60%
- **Skill Usage**: Count of batch skill invocations
  - Target: 10+ uses per week
- **Pattern Growth**: Patterns in database over time
  - Target: Steady growth, plateau around 30-50 patterns

### Impact Metrics

Measure actual savings:
- **Token Savings**: Total tokens saved via batch operations
  - Calculate: (sequential_tokens - batch_tokens) × uses
  - Target: 50,000+ tokens saved per week
- **Time Savings**: Total time saved via parallel execution
  - Calculate: (sequential_time - batch_time) × uses
  - Target: 5+ minutes saved per session
- **Error Reduction**: Decrease in sequential tool warnings
  - Target: 50% reduction in detect-sequential-tools.sh warnings

### Quality Metrics

Monitor system accuracy:
- **Prediction Accuracy**: True positives / (TP + FP)
  - Target: > 80%
  - Track per pattern
- **Coverage**: % of sessions with ≥1 batch opportunity
  - Target: > 40%
- **False Positive Rate**: Bad predictions / total predictions
  - Target: < 10%

### Reporting

Create weekly report:
```json
{
  "week": "2025-11-11",
  "adoption": {
    "predictions_made": 47,
    "predictions_accepted": 31,
    "acceptance_rate": 0.66
  },
  "impact": {
    "tokens_saved": 124000,
    "time_saved_seconds": 1850,
    "sessions_optimized": 18
  },
  "quality": {
    "prediction_accuracy": 0.84,
    "false_positive_rate": 0.08,
    "coverage": 0.45
  },
  "top_patterns": [
    {"pattern_id": "read-multiple-protocol-files", "uses": 12},
    {"pattern_id": "agent-invocation-sequential", "uses": 8}
  ]
}
```

---

## 🚨 CRITICAL REQUIREMENTS

### Mandatory Compliance

**Hook Script Standards** (from CLAUDE.md):
```bash
#!/bin/bash
set -euo pipefail

trap 'echo "ERROR in script-name.sh at line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Rest of script...
```

**Hook Registration**:
- Create hook script in `.claude/hooks/`
- Make executable: `chmod +x .claude/hooks/script.sh`
- Register in `.claude/settings.json`
- **RESTART Claude Code** (settings.json changes require restart)
- Test hook triggers correctly

**Skill Documentation Standards**:
```yaml
---
name: skill-name
description: Clear description under 100 chars
allowed-tools: Tool1, Tool2, Tool3
---

# Skill Title

**Purpose**: [Clear purpose statement]

**When to Use**: [3-5 bullet points]

## Skill Workflow
[Detailed steps...]
```

**Pattern Database Location**:
- Store at `/workspace/main/.claude/pattern-database.json`
- Add to `.gitignore` (contains session-specific data)
- Never commit to git

**Error Handling**:
- All skills must handle missing files gracefully
- Pattern database missing → create empty with schema
- Session logs missing → report error, suggest alternatives
- Parse failures → log error, continue with other patterns

---

## 📝 DELIVERABLES CHECKLIST

### Phase 1: Foundation
- [ ] extract-patterns skill implemented and tested
- [ ] pattern-database.json schema created
- [ ] Tested on 5 historical sessions
- [ ] Documentation complete with examples

### Phase 2: Database
- [ ] update-pattern-database skill implemented
- [ ] Database populated with historical data (10+ patterns)
- [ ] Frequency counts validated
- [ ] Pruning logic tested

### Phase 3: Prediction
- [ ] predict-batch-opportunity.sh hook implemented
- [ ] Hook registered in settings.json
- [ ] Tested with live tool usage
- [ ] Prediction accuracy tracking working

### Phase 4: Validation & Documentation
- [ ] End-to-end system validation complete
- [ ] Pattern visualization queries documented
- [ ] Weekly metrics report template created
- [ ] All components tested and working

### Documentation
- [ ] All skills have complete SKILL.md files
- [ ] Hook scripts follow standards (error handling, comments)
- [ ] Usage examples provided for each component
- [ ] Success metrics baseline established

### Testing
- [ ] All unit tests passing
- [ ] Integration tests passing
- [ ] Performance tests meeting targets
- [ ] Validation criteria met

---

## 🎓 LEARNING RESOURCES

### Key Concepts to Understand

**Pattern Signature**:
```
Format: Tool1(pattern) delay → Tool2(pattern) delay → Tool3(pattern)
Example: "Read(docs/project/*.md) 2.5s → Read(docs/project/*.md) 2.3s → Read(docs/project/*.md)"
Purpose: Unique identifier for pattern matching
```

**Pattern Generalization**:
```
Specific: /workspace/main/src/main/java/io/github/cowwoc/styler/Formatter.java
General: src/main/java/**/*.java
Purpose: Match similar patterns across sessions
```

**Confidence Score**:
```
Range: 0.0 to 1.0
Calculation: frequency / max_expected_frequency
Threshold: 0.8 for showing suggestions
Purpose: Prevent false positives
```

**Batchability Assessment**:
```
SAFE: Operations are independent, no side effects
CONDITIONAL: Operations batchable under certain conditions
UNSAFE: Operations have dependencies, cannot batch
```

### Example Patterns to Recognize

**Pattern 1: Protocol File Loading (BATCHABLE)**
```
Read: main-agent-coordination.md
→ delay 2.5s
Read: task-protocol-core.md
→ delay 2.3s
Read: task-protocol-operations.md

Signature: "Read(docs/project/*.md) → Read(docs/project/*.md) → Read(docs/project/*.md)"
Batch Skill: batch-read-protocol-files
```

**Pattern 2: Agent Invocation (BATCHABLE)**
```
Task: architect (REQUIREMENTS mode)
→ delay 90s
Task: tester (REQUIREMENTS mode)
→ delay 85s
Task: formatter (REQUIREMENTS mode)

Signature: "Task(architect) → Task(tester) → Task(formatter)"
Batch Skill: gather-requirements (already exists)
```

**Pattern 3: Write and Validate (SEQUENTIAL - NOT BATCHABLE)**
```
Write: FormattingRule.java
→ delay 0s
Bash: ./mvnw compile
→ delay 3.5s
Bash: ./mvnw checkstyle:check

Signature: "Write(*.java) → Bash(mvn compile) → Bash(checkstyle)"
Batch Skill: write-and-validate-java-file (atomic operation)
Note: NOT parallelizable (must write before validate), but can be atomic
```

---

## 💬 QUESTIONS & SUPPORT

### If You Get Stuck

**Q: Can't find session ID**
A: Look for system reminder at SessionStart: `✅ Session ID: {uuid}`

**Q: Session logs not readable**
A: Check path: `~/.config/projects/-workspace/{session-id}.jsonl`
   Verify session ID is correct (from system reminder)

**Q: Pattern database growing too large**
A: Implement pruning: Remove patterns with < 3 occurrences in 30 days
   Set max patterns limit (e.g., 50 most frequent)

**Q: Hook not triggering**
A: 1) Check settings.json registration
   2) Verify hook is executable: `chmod +x .claude/hooks/script.sh`
   3) **RESTART Claude Code** (required for settings.json changes)
   4) Check hook stderr output for errors

**Q: Predictions are inaccurate (high false positive rate)**
A: Increase confidence threshold (0.8 → 0.9)
   Increase frequency threshold (10 → 20)
   Improve pattern signature specificity

**Q: How to debug pattern extraction**
A: Enable verbose logging in extract-patterns skill
   Output intermediate results (window analysis, operation extraction)
   Validate against known patterns manually

### Validation Questions

Before considering implementation complete, answer:

- [ ] Can the system learn from new sessions automatically?
- [ ] Do predictions appear before the first tool call?
- [ ] Is prediction accuracy > 80%?
- [ ] Is false positive rate < 10%?
- [ ] Does the pattern database grow appropriately?
- [ ] Are batch skills suggested when patterns match?
- [ ] Does the system handle missing files gracefully?
- [ ] Are all hooks following script standards?
- [ ] Is documentation complete and clear?

---

## 🎯 FINAL SUCCESS CRITERIA

### System is Complete When:

1. **Pattern Learning Works**
   - Extract patterns from any session
   - Update database automatically
   - Frequency counts accurate
   - Pattern pruning prevents bloat

2. **Prediction Works**
   - Hook triggers on correct tools
   - Suggestions appear for high-confidence patterns
   - User can follow suggestion or ignore
   - Accuracy tracked per pattern

3. **Integration Works**
   - Audit skill references pattern database
   - Existing batch skills are suggested
   - No conflicts with existing hooks
   - Performance acceptable (< 100ms overhead)

4. **Quality Acceptable**
   - Prediction accuracy > 80%
   - False positive rate < 10%
   - Coverage > 40% of sessions
   - Token savings measurable and significant

5. **Documentation Complete**
   - All skills documented
   - All hooks commented
   - Usage examples provided
   - Metrics baseline established

---

## 📦 OPTIONAL REFERENCE FILES

If these files exist in your environment, they provide helpful context (but are NOT required):
- `/workspace/main/docs/proposals/cross-session-pattern-analysis.md` - Detailed design spec
- `/workspace/main/.claude/skills/audit-protocol-efficiency/SKILL.md` - Example efficiency audit
- `/workspace/main/CLAUDE.md` - Project coding standards

**This prompt is self-contained** - you have everything needed to implement the system without reading those files.

---

## ⚡ QUICK START

### Recommended Implementation Order

**Day 1**: Foundation
```bash
1. Review this prompt's architecture section (30 min)
2. Implement extract-patterns skill (4 hours)
3. Create pattern-database.json schema (1 hour)
4. Test on 1 historical session (1.5 hours)
5. Optionally read reference files if available (1 hour)
```

**Day 2**: Database Population
```bash
1. Implement update-pattern-database skill (3 hours)
2. Process 10 historical sessions (2 hours)
3. Validate pattern quality (1 hour)
4. Document findings (1 hour)
```

**Day 3**: Prediction Hook
```bash
1. Implement predict-batch-opportunity.sh (4 hours)
2. Register in settings.json + restart Claude Code (1 hour)
3. Test with live sessions (2 hours)
4. Tune thresholds (1 hour)
```

**Day 4**: Validation & Testing
```bash
1. Run end-to-end system validation (3 hours)
2. Create visualization queries (1 hour)
3. Run full test suite (2 hours)
4. Fix issues, polish documentation (2 hours)
```

**Day 5**: Validation
```bash
1. Run integration tests (2 hours)
2. Measure baseline metrics (2 hours)
3. Create metrics report (2 hours)
4. Final documentation review (2 hours)
```

---

## 🎉 COMPLETION

When you complete this implementation:

1. **Report Metrics**: Provide baseline metrics report
2. **Share Examples**: Show 3-5 pattern examples from database
3. **Demonstrate Prediction**: Show hook suggesting batch operation
4. **Document Lessons**: What worked well, what was challenging
5. **Suggest Improvements**: Ideas for Phase 2 enhancements

**Good luck! This is a high-impact project that will significantly improve the efficiency of the entire system.**

---

**Document Version**: 1.0
**Last Updated**: 2025-11-11
**Estimated Effort**: 5 days (40 hours)
**Difficulty**: Advanced
**Impact**: High (50-70% operation speedup potential)
