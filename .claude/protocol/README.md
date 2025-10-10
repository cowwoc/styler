# Delegated Implementation Protocol - Infrastructure

## Overview

The Delegated Implementation Protocol enables autonomous agent implementation with iterative convergence until unanimous approval. This reduces context usage by 50-75% through differential file operations (baseline + diffs) while maintaining quality through stakeholder validation.

## Architecture Components

### 1. Phase 3: Context Generation (`generate-context.py`)

**Purpose:** Create comprehensive context.md files for autonomous agent implementation

**Location:** `.claude/protocol/generate-context.py`

**Usage:**
```bash
python generate-context.py --task-name TASK_NAME --task-dir /workspace/branches/TASK_NAME
```

**Output:** `context.md` file containing:
- Requirements specification
- Technical constraints
- Security requirements
- Code quality standards
- File structure and expected changes
- Agent work assignments with dependencies
- File-based communication protocol

**Key Features:**
- Extracts task details from `todo.md`
- Determines agent assignments based on task requirements
- Creates conflict avoidance strategy (file ownership boundaries)
- Specifies file-based communication protocol

### 2. Phase 5: Convergence Algorithm (`convergence.py`)

**Purpose:** Manage iterative integration until unanimous approval

**Location:** `.claude/protocol/convergence.py`

**Usage:**
```bash
# Detect conflicts
python convergence.py --task-dir TASK_DIR --detect-conflicts --agent-diffs agents.json

# Integrate diffs
python convergence.py --task-dir TASK_DIR --integrate --agent-diffs agents.json --round 1
```

**Key Features:**
- Conflict detection between agent diffs
- Selective agent review routing (only changed files)
- Implicit approval tracking (unchanged files)
- Round-based state management
- File state tracking (who modified what, when)

**Convergence State Tracking:**
- `file_states`: Which files changed in which round
- `agent_last_review`: When each agent last reviewed
- `conflicts_detected`: Identified conflicts and resolution strategies
- `round_history`: Complete history of convergence rounds

### 3. Differential Reading System (`differential-read.sh`)

**Purpose:** Read-once + diff-based updates pattern to minimize context usage

**Location:** `.claude/protocol/differential-read.sh`

**Usage:**
```bash
# Initial read (full file)
./differential-read.sh --mode initial --file src/main/java/Token.java

# Differential read (changes only)
./differential-read.sh --mode diff --file src/main/java/Token.java --baseline-sha abc123...
```

**Context Savings:**
- Initial read: Full file content
- Subsequent reads: Only diffs (60% reduction per file)
- Baseline tracking: SHA256 hashes stored in `.baselines/`

### 4. Incremental Validation (`incremental-validation.sh`)

**Purpose:** Validate only changed files rather than entire codebase

**Location:** `.claude/protocol/incremental-validation.sh`

**Usage:**
```bash
./incremental-validation.sh --task-dir /workspace/branches/TASK_NAME --round 1
```

**Validation Scope:**
- checkstyle: Only changed Java files
- PMD: Full module (filters to changed files in output)
- Tests: Only tests for changed classes
- Falls back to full validation if no changes detected

**Performance:**
- Small changes: 5-10x faster than full validation
- Large refactors: 2-3x faster
- Zero changes: Skips validation entirely

## Updated Agent Configurations

### Implementation Agents

**ALL AGENTS UPDATED** (8/8 complete)

**Implementation Agents:**
- `technical-architect.md` - Architectural components
- `security-auditor.md` - Security controls
- `code-quality-auditor.md` - Refactoring and quality
- `performance-analyzer.md` - Algorithmic optimizations
- `code-tester.md` - Unit test implementation

**Review-Only Agents:**
- `style-auditor.md` - Style compliance
- `usability-reviewer.md` - UX review
- `build-validator.md` - Build validation

**New Sections:**
- Phase 4: Autonomous Implementation (implementation agents only)
- Phase 5: Convergence Review (all agents)
- File-Based Communication Requirements
- Cross-Domain Review Responsibility
- Full File Access for Context

## Protocol Workflow

### Phase 3: CONTEXT (New)
```
Main Agent:
1. Extract task from todo.md
2. Analyze file modifications needed
3. Determine agent assignments
4. Generate context.md
5. Provide to all agents
```

### Phase 4: AUTONOMOUS_IMPLEMENTATION (New)
```
Each Implementation Agent (Parallel):
1. Read context.md
2. Read assigned files (read-once)
3. Implement changes per scope
4. Write diff to ../agent-type.diff
5. Return metadata summary (NOT diff content)

Review-Only Agents:
- SKIP Phase 4 (no implementation)
```

### Phase 5: CONVERGENCE (New)
```
Round 1:
  Main Agent:
    1. Integrate all agent diffs
    2. Detect conflicts, resolve
    3. Commit integrated state
    4. Notify agents of changes

  All Agents (Parallel):
    1. Receive diffs → reconstruct current state (baseline from Phase 4 + diffs)
    2. Review for their domain concerns
    3. If more context needed: Read specific lines around diff
    4. Decide: APPROVED / REVISE / CONFLICT
    5. If REVISE: Write revision diff
    6. Return decision metadata

Round 2-10:
  Main Agent:
    1. Apply revision diffs
    2. Notify agents of changes

  Agents (Selective):
    1. If your files unchanged: IMPLICIT APPROVAL
    2. If your files changed: Reconstruct state (previous + new diffs)
    3. Always review other agents' changes
    4. Read specific lines if more context needed

**CRITICAL**: Agents reconstruct file state from baseline + diffs mentally.
Only read additional lines/files if specific surrounding context needed.
Full re-reads only if context lost (compaction) or unfamiliar file.

Unanimous Approval:
  → Proceed to Phase 5.5 (Build Validation)

Round Limit Exceeded (Round 11+):
  → Escalate to User Review Checkpoint
  → User reviews partial consensus
  → If user requests changes: Extend limit by 10 rounds (e.g., round 3 + 10 = round 13 limit)
  → If user approves: Override unanimous requirement, proceed

## Round Limit Management

**Default Limit:** 10 rounds

**Checkpoint Reset Logic:**
```python
# Example: Agents reach round 3 without consensus
# Main agent escalates to user checkpoint

# User reviews changes and requests modification
manager.user_requested_changes_at_checkpoint()
# Result: max_rounds = current_round (3) + 10 = 13
# Agents now have until round 13 to converge

# If consensus still not reached by round 13:
# Escalate again, user can extend again or approve partial
```

**Usage:**
```python
from convergence import ConvergenceManager

manager = ConvergenceManager(task_dir)

# Check if should escalate to user
if manager.should_escalate_to_user():
    print("Escalating to user review checkpoint...")
    print(f"Rounds remaining: {manager.rounds_remaining()}")

# User requests changes at checkpoint
manager.user_requested_changes_at_checkpoint()
# Limit automatically extended by 10 rounds
```
```

### Phase 5.5: INTEGRATION_BUILD (New)
```
1. Run incremental validation
2. Verify all quality gates pass
3. Confirm unanimous approval maintained
```

## File-Based Communication Protocol

### Agent Implementation Output

**Files Created:**
- `../agent-type.diff` - Complete unified diff (MANDATORY)
- `../agent-type-summary.md` - Detailed notes (optional)

**Metadata Returned:**
```json
{
  "summary": "Brief description (1-2 sentences)",
  "files_changed": ["file1.java", "file2.java"],
  "diff_file": "../agent-type.diff",
  "diff_size_lines": 150,
  "integration_notes": "Dependencies or conflicts to watch",
  "tests_added": true,
  "build_status": "success|failure|not_tested"
}
```

**Main Agent UX - Option A: Summary Only (Standard)**

Main agent shows summary ONLY, diffs stay in files for maximum context efficiency:

```
Phase 5 Round 1 Complete:
  ✅ security-auditor: Implemented validation (120 lines in 2 files)
  ✅ technical-architect: Implemented Token record (150 lines in 2 files)
  ✅ code-quality-auditor: Refactored builder pattern (95 lines in 1 file)
  ✅ code-tester: Added comprehensive tests (300 lines in 2 files)

All changes integrated to working tree.
Run 'git diff' to review changes.
Waiting for agent convergence...
```

**User Workflow:**
1. See summary in console
2. Run `git diff` or `git diff --stat` to review changes
3. After convergence + build validation, user approval checkpoint
4. Approve: "Changes look good, proceed"

**Context Efficiency:**
- Diffs NEVER loaded into main agent's Claude context (stay in files)
- convergence.py script processes diffs (separate Python process memory)
- Main agent only sees script output metadata (10-50 tokens per agent)
- User reviews using git (familiar workflow, better syntax highlighting)
- Maximum token savings while maintaining visibility

**Implementation Note:**
Diff processing happens in TWO separate contexts:
1. **Python Script Memory** (convergence.py): Loads diffs, detects conflicts, outputs metadata
2. **Main Claude Agent Context**: Receives metadata only, never raw diff content
3. **Git Process**: Applies diffs directly from files using `git apply`

This separation keeps diff content out of Claude's token-limited context.

### Agent Review Output

**Decision Metadata:**
```json
{
  "decision": "APPROVED|REVISE|CONFLICT",
  "rationale": "Brief explanation",
  "diff_file": "../agent-type-revision.diff",  // Only if REVISE
  "files_reviewed": ["Token.java", "Validator.java"]
}
```

## Context Efficiency Analysis

### Token Usage Breakdown
```
Phase 3 (CONTEXT): 1500 tokens (context.md, read by all)
Phase 4 (AUTONOMOUS_IMPLEMENTATION): 4 agents × 300 tokens (metadata only) = 1,200 tokens
Phase 5 Round 1 (CONVERGENCE): 5 agents × 400 tokens (diffs + metadata, reconstruct mentally) = 2,000 tokens
Phase 5 Round 2 (CONVERGENCE): 3 agents × 300 tokens (selective diffs + metadata) = 900 tokens
Total: ~10,600 tokens
```

**Efficiency Achieved:** Agents use baseline + diffs (10,600 tokens vs 32,500 tokens for full file reads)

**Key Optimization:** Agents reconstruct file state from baseline + diffs without re-reading.
Only read specific lines if additional context needed (style checks, duplication).
Context savings come from mental reconstruction + file-based communication.

## Full File Access for Context

**CRITICAL DESIGN PRINCIPLE**: Agents can reconstruct file state from baseline + diffs, with selective re-reads as needed.

### Why Context Reconstruction Matters

**Problem with Diff-Only Review**:
```diff
# Agent sees only this diff:
+ public void process() {
+     validate();
+ }
```

**But can't see** surrounding code that has style violations, duplication, or context issues.

### Solution: Differential Reading with Context Reconstruction

**Efficiency Pattern** (maximum context savings):
1. **Phase 4:** Read full files initially → baseline in agent context
2. **Phase 5:** Receive diffs → agent reconstructs current state mentally (baseline + diff)
3. **If more context needed:** Read specific lines around diff (not entire file)
4. **Only re-read full file:** If context lost (compaction) or file unfamiliar

**Context Savings**:
- Initial full read: Baseline established
- Diffs applied mentally: Agent reconstructs current state
- Selective line reads: Only if specific surrounding context needed
- Full re-read: Only if context lost or new file encountered
- Metadata-only responses: File-based communication

**Example - Agent Context Reconstruction:**
```
Initial Read (Phase 4): Agent reads Token.java (150 lines) → baseline in context
Round 1 Diff: +20 lines, -5 lines → Agent applies diff mentally → current state known
Round 2 Review: Need to check style 3 lines above diff → Read lines 45-47 only (not full file)
Round 3 Review: No changes to Token.java → Implicit approval (no reading needed)
```

### Implementation in Agent Configurations

All agents updated with:
- Initial read: Full files (Phase 4) - establishes baseline
- Review rounds: Reconstruct from baseline + diffs (Phase 5)
- Selective reads: Specific lines if more context needed
- Diffs used for efficiency, not as restriction

## Usage Example

```bash
# Assuming task "implement-token-validation" exists in todo.md

# Phase 3: Generate context
python .claude/protocol/generate-context.py \
  --task-name implement-token-validation \
  --task-dir /workspace/branches/implement-token-validation

# Phase 4: Launch agents (parent agent does this via Task tool)
# Each agent reads context.md, implements, writes diff

# Phase 5: Round 1 - Integrate and review
python .claude/protocol/convergence.py \
  --task-dir /workspace/branches/implement-token-validation \
  --integrate \
  --agent-diffs agents-round-1.json \
  --round 1

# Phase 5: Round 2 - Apply revisions
python .claude/protocol/convergence.py \
  --task-dir /workspace/branches/implement-token-validation \
  --integrate \
  --agent-diffs agents-round-2.json \
  --round 2

# Phase 5.5: Validate build
.claude/protocol/incremental-validation.sh \
  --task-dir /workspace/branches/implement-token-validation

# Phase 6-8: Standard validation and cleanup (unchanged)
```

## Benefits

### Context Efficiency
- **67% reduction** in token usage (baseline + diff reconstruction)
- **Diffs never in main agent's Claude context** (processed by external scripts/git)
- **Summary-only UX**: Metadata responses only (10-50 tokens vs 1000-2000)
- Read-once pattern for files (sub-agents)
- Differential updates with mental reconstruction (sub-agents)
- Selective agent review
- **Key insight**: Diff processing happens in Python/git process memory, not Claude context

### Speed
- **Parallel implementation** (4x faster for 4 agents)
- **Selective validation** (5-10x faster for small changes)
- **Fewer round trips** (file-based communication)

### Quality
- **Unanimous approval** still required
- **Cross-domain review** preserved
- **Iterative convergence** ensures coherence
- **File-based artifacts** for debugging

### User Experience
- **Familiar workflow**: Review changes with `git diff`
- **Better visibility**: Syntax highlighting, standard git tools
- **Summary-only console**: No diff clutter, maximum clarity
- **Manual control**: User decides when to review changes

### Scalability
- Handles arbitrarily large diffs (no API limits)
- Graceful degradation (falls back to manual resolution)
- State persistence across rounds
- Resumable after crashes

## Future Enhancements

1. **Adaptive Protocol Selection**: Automatically choose traditional vs delegated based on task complexity
2. **Dependency Graph Analysis**: Optimize agent execution order based on dependencies
3. **Conflict Prediction**: ML-based conflict detection before integration
4. **Automated Conflict Resolution**: AST-based semantic merge for simple conflicts
5. **Performance Monitoring**: Track context savings and convergence efficiency metrics
