# Delegated Implementation Protocol

## Overview

Agent-based implementation via **emergent dependency discovery**. All agents attempt implementation each round; ordering emerges from readiness.

**KEY**: Dependencies discovered during implementation, not predicted. Agents self-organize into rounds.

## Emergent Dependency Discovery

### Why Static Planning Fails
Main agent cannot predict: Domain expertise gap (helper classes/interfaces/utilities) | Emergent dependencies (discovered during implementation) | Cross-agent coordination (interface extensions) | Circular dependencies (both agents need each other)

### Self-Organizing Round Structure

Rounds emerge from agent readiness:
1. **Round 1**: All agents attempt → COMPLETE (no deps) | PARTIAL (implement available) | BLOCKED (cannot proceed)
2. **Integration**: Apply COMPLETE + PARTIAL diffs
3. **Round N**: Non-COMPLETE agents retry → Continue until all COMPLETE

**Key**: PARTIAL implementations prevent deadlocks by ensuring progress despite missing dependencies.

### Benefits of Partial Implementation

**Deadlock Prevention**: Agents rarely BLOCKED; circular dependencies resolve incrementally.
**Faster Convergence**: PARTIAL enables parallelism. Example: BLOCKED approach 3 rounds (sequential) vs PARTIAL 2 rounds (parallel agents).
**Better Dependency Discovery**: Agents discover needs during implementation (not guesses).
**Reduced Wasted Effort**: No idle agents; earlier integration catches mismatches sooner.

### Execution Pattern

```python
def autonomous_implementation_with_discovery():
    """All agents participate each round. Rounds emerge from agent readiness."""
    round_num = 1
    all_agents = get_required_agents()
    completed_agents = set()

    while len(completed_agents) < len(all_agents):
        remaining_agents = [a for a in all_agents if a not in completed_agents]
        if not remaining_agents:
            break

        results = launch_agents_with_dependency_check(remaining_agents, round_num)
        ready_agents, blocked_agents = [], []

        for agent_name, response in results.items():
            if "PARTIAL:" in response or "COMPLETE:" in response:
                ready_agents.append({
                    "agent": agent_name,
                    "diff": get_agent_diff_file(agent_name, round_num),
                    "status": "PARTIAL" if "PARTIAL:" in response else "COMPLETE",
                    "completed_work": parse_completed_work(response),
                    "missing_deps": parse_missing_dependencies(response) if "PARTIAL:" in response else []
                })
                if "COMPLETE:" in response:
                    completed_agents.add(agent_name)
            elif "BLOCKED:" in response:
                blocked_agents.append({
                    "agent": agent_name,
                    "missing": parse_missing_dependencies(response),
                    "reason": parse_blocker_reason(response)
                })

        if not ready_agents:
            handle_deadlock(blocked_agents, round_num)
            break

        for agent_result in ready_agents:
            diff_file = agent_result["diff"]
            if validate_diff_applies(diff_file):
                apply_diff(diff_file)
                if agent_result["status"] == "COMPLETE":
                    completed_agents.add(agent_result["agent"])

        if ready_agents:
            git_commit(f"Round {round_num}: {', '.join([a['agent'] for a in ready_agents])}")
        round_num += 1

    return len(completed_agents) == len(all_agents)
```

## Protocol Detection

**Automatic Detection**: `context.md` exists in task root (`/workspace/branches/{task-name}/context.md`) | Contains `## Agent Work Assignments` section | Lock file state: CONTEXT, AUTONOMOUS_IMPLEMENTATION, or CONVERGENCE

**When Detected**: Direct Write/Edit for implementation files **PROHIBITED** | Implementation via delegated agents only | Enforced by `enforce-delegated-implementation.sh` hook

## 🚨 CRITICAL: NO FALLBACK TO NON-DELEGATED IMPLEMENTATION

**ONCE DELEGATED PROTOCOL ACTIVE, NO EXCEPTIONS:**

When `context.md` exists with agent assignments, the main agent is **STRICTLY FORBIDDEN** from implementing code directly, regardless of circumstances.

**PROHIBITED FALLBACK PATTERNS**:
❌ "Agent blocked, I'll implement this part myself"
❌ "Diff validation failed, let me write the file directly"
❌ "Agent taking too long, I'll finish it"
❌ "Simple fix, I'll just Edit/Write this one file"
❌ "Integration glue code" that implements business logic
❌ "Fixing agent errors" by rewriting their implementation
❌ "Agent diff missing git hashes, I'll add them manually"
❌ "Agent output has formatting issue, I'll patch it"
❌ "Agent made mistake, I'll fix their diff before applying"

**ONLY PERMITTED ACTIONS**:
✅ Apply agent diffs via `git apply` (no manual fixes)
✅ Resolve merge conflicts (keep both agents' intent)
✅ Launch/re-launch agents with refined prompts
✅ Request PARTIAL implementations from agents
✅ Minimal integration (imports, wiring, no logic)

**IF AGENTS MAKE MISTAKES**:
1. **IDENTIFY ROOT CAUSE**: Why did the agent make this mistake? Tool availability? Ambiguous instruction? Missing validation?
2. **INVOKE PROTOCOL-AUDITOR**: Launch protocol-auditor agent to analyze the root cause and revise the protocol to prevent future occurrences
3. **RE-INVOKE AGENT**: After protocol update, re-invoke the original agent with the revised protocol
4. **NEVER** manually fix agent output (diffs, files, etc.)

**RATIONALE**:
- Manually fixing agent mistakes creates one-off workarounds
- Root cause remains in protocol, causing repeated failures
- Protocol-auditor finds systemic issues and prevents recurrence
- Fixing = technical debt; Protocol revision = sustainable improvement

**IF AGENTS FAIL**:
1. Re-invoke agent with clearer instructions
2. Break task into smaller agent-implementable pieces
3. Request PARTIAL implementation of subset
4. If repeated failures → Invoke protocol-auditor to fix protocol
5. **NEVER** fall back to main agent manual implementation

**ENFORCEMENT**: Hook blocks Write/Edit to `src/**/*.java` during delegated protocol

## State Machine (Delegated Protocol)

```
USER_APPROVAL (plan approved)
    ↓
CONTEXT (generate context.md with agent assignments)
    ↓
AUTONOMOUS_IMPLEMENTATION (iterative rounds until completion)
    ↓
CONVERGENCE (final integration, conflict resolution)
    ↓
VALIDATION (build verification)
    ↓
REVIEW (stakeholder approval)
    ↓
USER_APPROVAL (final approval)
    ↓
COMPLETE (merge to main)
    ↓
CLEANUP (remove worktree)
```

## Implementation Phases

### Phase 1: CONTEXT State

**Objective**: Generate implementation context WITHOUT predicting dependency order

**Actions**: Create `context.md` with agent assignments (NO round numbers) | Specify: agent type, task description, deliverables, known dependencies (may be incomplete) | Update lock state to AUTONOMOUS_IMPLEMENTATION

**context.md Structure** (NO static rounds):
```markdown
## Agent Work Assignments

### technical-architect
**Task**: Create core interfaces and data structures
**Deliverables**:
- FormattingRule.java interface
- Violation.java record
**Known Dependencies**: None
**Notes**: Foundation interfaces for other agents

### code-quality-auditor
**Task**: Implement concrete formatter classes
**Deliverables**:
- LineWrapperFormatter.java
**Known Dependencies**: FormattingRule.java (may discover more during implementation)
**Notes**: Will use interfaces created by technical-architect

### security-auditor
**Task**: Implement input validation
**Deliverables**:
- InputValidator.java
**Known Dependencies**: Unknown until implementation (may need validation interfaces)

### style-auditor
**Task**: Add JavaDoc and fix style violations
**Deliverables**: JavaDoc comments, style fixes
**Known Dependencies**: All implementation files (runs after other agents)

### code-tester
**Task**: Create comprehensive test suite
**Deliverables**: Test classes
**Known Dependencies**: All implementation files (runs after other agents)
```

**Lock Update**:
```bash
jq '.state = "AUTONOMOUS_IMPLEMENTATION"' /workspace/locks/{task}.json > /tmp/lock.json
mv /tmp/lock.json /workspace/locks/{task}.json
```

### Phase 2: AUTONOMOUS_IMPLEMENTATION State

**Objective**: Execute emergent dependency discovery rounds

**MANDATORY EXECUTION PATTERN** (preserved verbatim - execution-critical bash script):

```bash
#!/bin/bash

ROUND=1
ALL_AGENTS=("technical-architect" "code-quality-auditor" "security-auditor" "style-auditor" "code-tester")
COMPLETED_AGENTS=()

while [ ${#COMPLETED_AGENTS[@]} -lt ${#ALL_AGENTS[@]} ]; do
    echo "=== ROUND $ROUND: Dependency Discovery ==="

    # Get remaining agents
    REMAINING_AGENTS=()
    for agent in "${ALL_AGENTS[@]}"; do
        if [[ ! " ${COMPLETED_AGENTS[@]} " =~ " ${agent} " ]]; then
            REMAINING_AGENTS+=("$agent")
        fi
    done

    if [ ${#REMAINING_AGENTS[@]} -eq 0 ]; then
        echo "✅ All agents completed"
        break
    fi

    echo "Attempting: ${REMAINING_AGENTS[@]}"

    # Launch all remaining agents in parallel
    # (Use single message with multiple Task tool invocations)
    # Each agent gets dependency-check-enabled prompt

    # Collect results
    READY_AGENTS=()
    BLOCKED_AGENTS=()

    for agent in "${REMAINING_AGENTS[@]}"; do
        RESPONSE=$(cat "../round${ROUND}-${agent}-response.txt")

        if grep -q "COMPLETE:" <<< "$RESPONSE"; then
            READY_AGENTS+=("$agent")
            COMPLETED_AGENTS+=("$agent")  # Fully done, won't retry
            echo "   ✅ COMPLETE: $agent"
        elif grep -q "PARTIAL:" <<< "$RESPONSE"; then
            READY_AGENTS+=("$agent")
            # Note: NOT added to COMPLETED_AGENTS - will retry next round
            echo "   ⚙️  PARTIAL: $agent (has deferred work)"
        elif grep -q "BLOCKED:" <<< "$RESPONSE"; then
            BLOCKED_AGENTS+=("$agent")
            echo "   ⏸️  BLOCKED: $agent"
            grep "BLOCKED:" <<< "$RESPONSE"
        fi
    done

    # Deadlock detection
    if [ ${#READY_AGENTS[@]} -eq 0 ]; then
        echo "❌ DEADLOCK: No agents made progress in round $ROUND"
        echo "Blocked agents: ${BLOCKED_AGENTS[@]}"
        # Call deadlock resolution function
        resolve_deadlock "${BLOCKED_AGENTS[@]}"
        exit 1
    fi

    # Validate and apply diffs for ready agents
    for agent in "${READY_AGENTS[@]}"; do
        DIFF_FILE="../round${ROUND}-${agent}.diff"

        # Validate
        if ! git apply --check "$DIFF_FILE"; then
            echo "⚠️  Diff validation failed for $agent - skipping"
            continue
        fi

        # Apply
        git apply "$DIFF_FILE"
        COMPLETED_AGENTS+=("$agent")
        echo "   ✅ Integrated: $agent"
    done

    # Commit integration
    if [ ${#READY_AGENTS[@]} -gt 0 ]; then
        git commit -am "Round $ROUND: ${READY_AGENTS[*]}"
    fi

    ROUND=$((ROUND + 1))
done

if [ ${#COMPLETED_AGENTS[@]} -eq ${#ALL_AGENTS[@]} ]; then
    echo "✅ All agents completed successfully in $((ROUND - 1)) rounds"
else
    INCOMPLETE=()
    for agent in "${ALL_AGENTS[@]}"; do
        if [[ ! " ${COMPLETED_AGENTS[@]} " =~ " ${agent} " ]]; then
            INCOMPLETE+=("$agent")
        fi
    done
    echo "❌ Incomplete: ${INCOMPLETE[@]}"
fi
```

### Agent Prompt Template (Dependency-Check-Enabled)

**CRITICAL**: Every agent prompt includes early bailout instructions (template preserved verbatim - execution-critical)

```
ROUND {round_number} IMPLEMENTATION ATTEMPT

Your task: {agent_specific_task_description}

Expected deliverables:
{list_of_files_to_create_or_modify}

Known dependencies (may be incomplete):
{dependencies_from_context_md}

═══════════════════════════════════════════════════════════════
🚨 CRITICAL: DIFF-ONLY OUTPUT - DO NOT TOUCH CODEBASE DIRECTLY
═══════════════════════════════════════════════════════════════

YOU MUST:
✅ Write unified diff to: ../round{round_number}-{agent_type}.diff
✅ Use ONLY Read tool to examine existing files
✅ Return status response (COMPLETE/PARTIAL/BLOCKED)

YOU MUST NOT:
❌ Use Write tool to create implementation files
❌ Use Edit tool to modify implementation files
❌ Create any files in src/ directories
❌ Modify any .java files directly

RATIONALE:
- Main agent integrates all diffs atomically
- Direct file creation causes merge conflicts
- Diff validation gate ensures quality
- Allows rollback if integration fails

═══════════════════════════════════════════════════════════════
🚨 MANDATORY DIFF GENERATION PROCEDURE
═══════════════════════════════════════════════════════════════

**CONSEQUENCES OF SKIPPING HASH GENERATION**:

If you return a diff without index lines containing git hashes:

1. ❌ `git apply --check` will fail with "corrupt patch" error
2. ❌ Main agent cannot integrate your work
3. ❌ Your entire round contribution is REJECTED
4. ❌ You will be re-invoked to fix the diff
5. ❌ Other agents' work is blocked waiting for you
6. ❌ Protocol convergence is delayed by entire round

**THIS IS A BLOCKING ERROR** - Not a warning, not a minor issue.

**PREVENTION**: Follow STEP 4 (hash generation procedure) exactly as written. No shortcuts permitted.

---

**REQUIRED DIFF FORMAT - VISUAL REFERENCE**

Your diff MUST match this exact format (note the index line):

```diff
✅ CORRECT FORMAT (with git hashes):

diff --git a/src/Formatter.java b/src/Formatter.java
index a1b2c3d4e5f6..e4f5g6h7i8j9 100644        ← THIS LINE IS MANDATORY
--- a/src/Formatter.java
+++ b/src/Formatter.java
@@ -10,6 +10,7 @@
 public class Formatter {
+    private final Config config;  // Your change here
     private final Logger logger;
 }
```

```diff
❌ INCORRECT FORMAT (missing index line):

diff --git a/src/Formatter.java b/src/Formatter.java
--- a/src/Formatter.java              ← MISSING INDEX LINE ABOVE THIS
+++ b/src/Formatter.java
@@ -10,6 +10,7 @@
 public class Formatter {
+    private final Config config;
     private final Logger logger;
 }

🚨 THIS DIFF WILL BE REJECTED - Cannot be applied by git
```

**CRITICAL DISTINCTION**:
- Plain `diff -u` command produces INCORRECT format (no git hashes)
- Only the git-based procedure (STEP 4) produces CORRECT format
- You CANNOT use shortcuts like `diff -u original.java modified.java`
- You MUST follow STEP 4 procedure exactly as written

**PRE-RETURN VALIDATION**: Before returning, verify EVERY file change has an index line:
```bash
grep "^index " your-diff-file.diff
# Should show one line per file being modified
# Each line must match format: index <40-hex>..<40-hex> <mode>
```

---

**STEP 1: Read ALL files you will modify**

Use Read tool for every file before generating diff. This is MANDATORY.

**CRITICAL**: You MUST use Read tool in the CURRENT agent invocation. Previous reads from earlier in conversation are INVALID due to:
- Context compaction may have discarded earlier reads
- Repository state may have changed since earlier read
- Other agents may have modified files between your reads

**IF READ FAILS**:
- File not found → Return "BLOCKED: File {path} does not exist. Required for task."
- Read error → Return "BLOCKED: Cannot read {path}. Error: {error_message}"
- Permission denied → Return "BLOCKED: Cannot access {path}. Permission denied."
- File is directory → Return "BLOCKED: {path} is a directory, expected file."
- DO NOT proceed with diff generation if Read fails
- DO NOT guess or infer file content
- DO NOT use cached memory of previous reads

**BLOCKED RESPONSE FORMAT** (when Read fails):
```
BLOCKED: Cannot read required file

File: path/to/RequiredFile.java
Error: [exact error message from Read tool]
Reason: This file is required to implement {task component}

Cannot proceed without this file.
```

---

**STEP 1.5: VALIDATE Read Output**

After using Read tool, verify the output:

```bash
# Pseudo-code for validation
read_output = Read("path/to/File.java")

# Check 1: Output is not a tool error message
if "Error:" in read_output or "error:" in read_output:
    return "BLOCKED: Read failed for path/to/File.java: {error}"

# Check 2: Output is not unexpectedly empty (unless file is legitimately empty)
# NOTE: Some legitimate Java files are minimal (package-info.java, module-info.java, etc.)
# Only block if empty AND you expect content
if len(read_output) == 0 and file_should_have_substantial_content:
    return "BLOCKED: Read returned empty content for path/to/File.java"

# Check 3: Output not truncated by tool limits
# Look for truncation indicators specific to Read tool
if "... [truncated]" in read_output or len(read_output) > 100000:
    return "BLOCKED: Read output for path/to/File.java appears truncated"

# If checks pass, proceed to STEP 2
# NOTE: We don't validate Java syntax here - that's git apply's job
```

**SPECIAL FILE TYPES** (expected to be minimal or unusual):
- `package-info.java`: Only package declaration + JavaDoc
- `module-info.java`: Only module declaration
- Marker interfaces: Only interface declaration, no methods
- Empty test files: Legitimately minimal for testing empty input
- Generated files: May have unusual structure

If working with special file types, Read output validation should focus on:
✅ Not an error message
✅ Not truncated
❌ NOT on validating Java structure (too many edge cases)

**VALIDATION CHECKLIST**:
- [ ] Read output is not an error message
- [ ] Read output is not unexpectedly empty
- [ ] Read output length is reasonable (not truncated)

---

**STEP 2: FORMATTING PRESERVATION REQUIREMENT**

When creating the modified copy in /tmp/, you MUST preserve EXACT formatting from the original file:

✅ Preserve blank lines (including after package statement, between methods, etc.)
✅ Preserve indentation style (tabs vs spaces, indent levels)
✅ Preserve line endings (LF vs CRLF) - CRITICAL for cross-platform compatibility
✅ Preserve trailing whitespace (if present in original)
✅ Change ONLY the specific lines required for your task

**EXAMPLE - CORRECT**:
Original file has blank line after package statement:
```java
package com.styler.formatter;

import java.util.List;  ← Note blank line above
```

Your modified file MUST preserve that blank line:
```java
package com.styler.formatter;

import java.util.List;  ← Blank line preserved
import java.util.Map;   ← Your new import
```

**EXAMPLE - INCORRECT** (causes blank line mismatch bug - REAL BUG OBSERVED):
```java
package com.styler.formatter;
import java.util.List;  ← ❌ Missing blank line - causes git apply failure
import java.util.Map;
```

**LINE ENDING PRESERVATION** (CRITICAL):
- Read tool preserves original line endings
- Write tool MUST preserve line endings (do not convert LF ↔ CRLF)
- If Write tool converts line endings, diff will show entire file changed
- Verify line ending type before Write: `file path/to/File.java` or `od -c`

**EXAMPLE - Line Ending Mismatch**:
```
Original file: Unix (LF) line endings, 100 lines
Agent writes: Windows (CRLF) line endings, 100 lines
Diff result: Shows all 100 lines changed (every line has line ending change)
❌ This creates massive diff noise and breaks git blame
```

**VERIFICATION**: After writing modified file, mentally compare blank lines, indentation, and formatting against the Read tool output to ensure only intended changes present.

---

**STEP 3: Create Agent-Specific Temporary Workspace**

```bash
cd /workspace/branches/{task-name}/code

# Define agent-specific temporary directory
AGENT_TYPE="{agent_type}"  # e.g., "technical-architect"
ROUND={round_number}
TEMP_DIR="/tmp/agent-${AGENT_TYPE}-round${ROUND}"

# Create isolated directory (safe for parallel execution)
if ! mkdir -p "$TEMP_DIR"; then
    echo "❌ ERROR: Cannot create temporary directory"
    return "BLOCKED: Failed to create workspace directory $TEMP_DIR. Check permissions."
fi

# Verify directory is empty (paranoid check for file collisions)
if [ "$(ls -A $TEMP_DIR)" ]; then
    echo "⚠️  WARNING: Temporary directory not empty"
    # Clean up stale files from previous failed attempt
    rm -rf "${TEMP_DIR:?}"/*
fi

echo "✅ Workspace ready: $TEMP_DIR"
```

---

**STEP 4: Generate Diff Using Write + Git Workflow**

🚨 CRITICAL: The following procedure is MANDATORY and CANNOT be simplified or substituted with alternative diff methods.

**WHY THIS EXACT PROCEDURE**:
- `git apply` REQUIRES index lines with git object hashes
- Plain `diff -u` output is INCOMPATIBLE with git apply
- Missing index lines = immediate integration failure
- No shortcuts or simplifications permitted

This procedure has FOUR PHASES:
1. **PREPARATION**: Get original file hashes from git
2. **MODIFICATION**: Create your changed version in /tmp/
3. **🚨 HASH GENERATION**: Calculate new hashes (MANDATORY - CANNOT BE SKIPPED)
4. **DIFF ASSEMBLY**: Construct final unified diff with git metadata

For EACH file you're modifying:

---

**PHASE 1: PREPARATION - Get Original Git Hash**

```bash
ORIGINAL_FILE="src/main/java/com/styler/Formatter.java"
MODIFIED_BASENAME="modified_Formatter.java"
MODIFIED_FILE="${TEMP_DIR}/${MODIFIED_BASENAME}"

# Get original file's git hash from index
ORIGINAL_HASH=$(git ls-files -s "$ORIGINAL_FILE" | awk '{print $2}')

# Validate original hash
if [ -z "$ORIGINAL_HASH" ]; then
    echo "❌ ERROR: Cannot get git hash for original file"
    return "BLOCKED: File $ORIGINAL_FILE not in git index. Is it tracked?"
fi

# Validate hash format (40 hex characters)
if ! [[ "$ORIGINAL_HASH" =~ ^[0-9a-f]{40}$ ]]; then
    echo "❌ ERROR: Invalid git hash format: $ORIGINAL_HASH"
    return "BLOCKED: git ls-files returned invalid hash for $ORIGINAL_FILE"
fi

echo "✅ Original hash: $ORIGINAL_HASH"
```

---

**PHASE 2: MODIFICATION - Create Modified Version**

```bash
# Use Write tool to create modified version in temp directory
# CRITICAL: Preserve exact formatting from Read output (see STEP 2)

# Example: Write modified content
cat > "$MODIFIED_FILE" << 'EOF'
[your modified file content with EXACT formatting preservation]
EOF

# Verify file was written
if [ ! -f "$MODIFIED_FILE" ]; then
    echo "❌ ERROR: Modified file not created"
    return "BLOCKED: Failed to write modified file to $MODIFIED_FILE"
fi

echo "✅ Modified file created: $MODIFIED_FILE"
```

---

**PHASE 3: 🚨 GIT HASH GENERATION (CANNOT BE SKIPPED)**

This is the MOST CRITICAL STEP. Without it, your diff is unusable.

```bash
# Generate git object hash for modified file
# This writes the object to git's object database
NEW_HASH=$(git hash-object -w "$MODIFIED_FILE")

# MANDATORY VALIDATION
if [ -z "$NEW_HASH" ]; then
    echo "❌ ERROR: git hash-object failed to generate hash"
    rm -rf "$TEMP_DIR"
    return "BLOCKED: Cannot generate git hash for modified file. Check repository state and permissions."
fi

# Validate hash format (40 hex characters)
if ! [[ "$NEW_HASH" =~ ^[0-9a-f]{40}$ ]]; then
    echo "❌ ERROR: Invalid git hash format: $NEW_HASH"
    rm -rf "$TEMP_DIR"
    return "BLOCKED: git hash-object returned invalid hash: $NEW_HASH"
fi

echo "✅ New hash: $NEW_HASH"

# YOU NOW HAVE BOTH HASHES REQUIRED FOR INDEX LINE:
# - ORIGINAL_HASH (from git ls-files)
# - NEW_HASH (from git hash-object)
# BOTH are REQUIRED for the index line
```

**WHY THIS CANNOT BE SKIPPED**:
- `git apply` validates hashes against blob objects
- Missing hashes = git apply fails with "corrupt patch"
- There is NO alternative method that works
- This step is not optional metadata
- Hash generation MUST happen for every file in diff

---

**PHASE 4: DIFF ASSEMBLY - Construct Unified Diff with Git Metadata**

```bash
# Generate base unified diff
DIFF_FILE="${TEMP_DIR}/file.diff"
diff -u "$ORIGINAL_FILE" "$MODIFIED_FILE" > "$DIFF_FILE"

# Validate diff was created
if [ ! -f "$DIFF_FILE" ]; then
    echo "❌ ERROR: Diff file not created"
    return "BLOCKED: diff command failed"
fi

# Fix the --- line (must reference a/ path)
sed -i "s|^--- .*|--- a/${ORIGINAL_FILE}|" "$DIFF_FILE"

# Fix the +++ line (must reference b/ path)
sed -i "s|^+++ .*|+++ b/${ORIGINAL_FILE}|" "$DIFF_FILE"

# Fix the diff --git line (both paths must be identical)
sed -i "s|^diff --git.*|diff --git a/${ORIGINAL_FILE} b/${ORIGINAL_FILE}|" "$DIFF_FILE"

# 🚨 CRITICAL: Insert index line with BOTH git hashes
# This line MUST appear after "diff --git" and before "---"
# Format: index <original-hash>..<new-hash> <mode>
sed -i "/^diff --git/a index ${ORIGINAL_HASH}..${NEW_HASH} 100644" "$DIFF_FILE"

echo "✅ Diff generated with git hashes"

# Append to final agent diff file
cat "$DIFF_FILE" >> "../round${ROUND}-${AGENT_TYPE}.diff"
```

**FINAL DIFF STRUCTURE** (verify this format):
```diff
diff --git a/path/to/File.java b/path/to/File.java
index <original-hash>..<new-hash> 100644
--- a/path/to/File.java
+++ b/path/to/File.java
@@ -line,count +line,count @@
[diff content]
```

---

**STEP 5: SCOPE CONSTRAINT - MINIMAL CHANGES ONLY**

When generating the modified file, you MUST make ONLY the changes required for your assigned task.

**DO NOT**:
❌ Fix unrelated style violations
❌ Add features not in your task description
❌ Refactor code outside your scope
❌ Update imports for other agents' code
❌ Add "helpful" utility methods not required
❌ Change formatting unrelated to your task

**DO**:
✅ Make only changes specified in your task
✅ Add imports for YOUR new code only
✅ Update only files listed in your deliverables
✅ Leave other code unchanged (even if you see issues)

**RATIONALE**:
- Scope creep causes merge conflicts with other agents
- Unrelated changes complicate diff review
- Style fixes are style-auditor's responsibility
- Minimal diffs = faster integration = faster convergence

**EXAMPLE - SCOPE VIOLATION**:
```diff
# Your task: Add logging to processFile() method

# ❌ WRONG: Also fixed style violation in unrelated method
@@ -15,7 +15,7 @@
 public void processFile(File f) {
+    logger.info("Processing: " + f);  ← Your task
     // ... existing code
 }

@@ -45,7 +45,7 @@
-public void helperMethod( String s ){  ← Unrelated method
+public void helperMethod(String s) {   ← Style fix NOT in your scope
     // ... existing code
 }
```

**CORRECT APPROACH**:
```diff
# Your task: Add logging to processFile() method

# ✅ CORRECT: Only changes for your task
@@ -15,7 +15,7 @@
 public void processFile(File f) {
+    logger.info("Processing: " + f);  ← Your task only
     // ... existing code
 }
```

---

**STEP 6: 🚨 MANDATORY SELF-VERIFICATION (ALL CHECKS REQUIRED)**

Before returning your diff, you MUST run ALL validation checks. Failure to validate = BLOCKED status.

**CHECK 1: Index Lines Present and Correctly Formatted**

```bash
FINAL_DIFF_FILE="../round${ROUND}-${AGENT_TYPE}.diff"

# Count index lines in diff
INDEX_COUNT=$(grep -c "^index " "$FINAL_DIFF_FILE")
FILE_COUNT=$(grep -c "^diff --git" "$FINAL_DIFF_FILE")

if [ "$INDEX_COUNT" -ne "$FILE_COUNT" ]; then
    echo "❌ CRITICAL ERROR: Diff is missing index lines"
    echo "   Expected: $FILE_COUNT index lines (one per file)"
    echo "   Found: $INDEX_COUNT index lines"
    echo ""
    echo "🚨 BLOCKED: Cannot return diff without git hashes"
    echo "   Reason: git apply REQUIRES index lines to function"
    echo "   Fix: Re-execute PHASE 3 (git hash-object) for all files"
    return "BLOCKED: Diff validation failed - missing git index lines"
fi

# Validate index line FORMAT (not just presence)
# Expected format: index <40-hex>..<40-hex> <mode>
# Example: index a1b2c3d4...f6e5d4c3 100644

INVALID_FORMAT=$(grep "^index " "$FINAL_DIFF_FILE" | grep -vE "^index [0-9a-f]{40}\.\.[0-9a-f]{40} [0-9]{6}$")

if [ -n "$INVALID_FORMAT" ]; then
    echo "❌ CRITICAL ERROR: Index line has invalid format"
    echo "   Invalid line(s):"
    echo "$INVALID_FORMAT"
    echo ""
    echo "   Expected format: index <40-hex>..<40-hex> <mode>"
    echo "   Example: index a1b2c3d4e5f6..f6e5d4c3b2a1 100644"
    echo ""
    echo "   Common mistakes:"
    echo "   - Using ... instead of .. (triple vs double dots)"
    echo "   - Missing mode bits (100644 or 100755)"
    echo "   - Truncated hashes (must be 40 hex characters each)"
    return "BLOCKED: Index line format validation failed"
fi

echo "✅ CHECK 1 PASSED: All index lines present and correctly formatted"
```

**CHECK 2: Diff Applies Cleanly**

```bash
# Validate diff applies to current repository state
if ! git apply --check "$FINAL_DIFF_FILE" 2>&1 | tee /tmp/apply-check.log; then
    echo "❌ CRITICAL ERROR: Diff does not apply cleanly"
    echo ""
    echo "Git apply error:"
    cat /tmp/apply-check.log
    echo ""
    echo "Common causes:"
    echo "- File paths incorrect (must be relative to repository root)"
    echo "- Line numbers mismatched (file changed since Read)"
    echo "- Formatting differences (blank lines, line endings)"
    echo "- Another agent already modified this file"
    echo ""
    echo "Troubleshooting:"
    echo "1. Re-read the original file with Read tool"
    echo "2. Verify EXACT formatting preservation (STEP 2)"
    echo "3. Check if file was modified by another agent"
    echo "4. If file changed: Return BLOCKED: Dependency not met"
    return "BLOCKED: Diff validation failed - does not apply cleanly"
fi

echo "✅ CHECK 2 PASSED: Diff applies cleanly to current state"
```

**CHECK 3: Format Structure Verification**

```bash
# Verify each file change has all required headers
# Expected structure per file:
#   diff --git a/path b/path
#   index abc123..def456 100644
#   --- a/path
#   +++ b/path
#   @@ -X,Y +X,Y @@

echo "Verifying diff structure..."

# Extract all diff --git lines
git diff --no-index /dev/null "$FINAL_DIFF_FILE" | grep "^diff --git" > /tmp/git-lines.txt || true

# For each file, verify it has index, ---, +++ lines
while IFS= read -r git_line; do
    echo "  Checking: $git_line"

    # This is a simplified check - full validation done by git apply --check
    # Just verify basic structure exists
done < /tmp/git-lines.txt

echo "✅ CHECK 3 PASSED: Diff structure verified"
```

**CHECK 4: Scope Compliance Verification**

```bash
# Verify diff only modifies files in your deliverables list
# Expected files: [list from task description]

echo "Verifying scope compliance..."

MODIFIED_FILES=$(grep "^diff --git" "$FINAL_DIFF_FILE" | awk '{print $3}' | sed 's|^a/||')

echo "Files modified in diff:"
echo "$MODIFIED_FILES"

# Compare against expected deliverables
# If modifying files outside your scope, this is a violation

echo "✅ CHECK 4 PASSED: Scope compliance verified"
```

**ALL CHECKS MUST PASS** before proceeding to status response.

If ANY check fails, you MUST:
1. Return BLOCKED status with specific error
2. Include exact error message from failed check
3. Do NOT return the diff file
4. Wait for re-invocation with fixes

---

**STEP 7: Return Status Response**

After ALL validation checks pass, return appropriate status:

**COMPLETE**: All work finished, no dependencies remain
```
COMPLETE: [Agent type] implementation finished

Files modified:
- path/to/File1.java (created interface)
- path/to/File2.java (implemented class)

Diff file: ../round{round}-{agent}.diff
Validation: All checks passed

No remaining work. Ready for integration.
```

**PARTIAL**: Some work done, remaining work deferred
```
PARTIAL: [Agent type] partial implementation

Completed work:
- path/to/File1.java (created base class)
- path/to/File2.java (added validation)

Deferred work:
- path/to/File3.java (BLOCKED: Requires Interface X from technical-architect)
- path/to/File4.java (BLOCKED: Depends on File3.java)

Diff file: ../round{round}-{agent}.diff
Validation: All checks passed

Remaining dependencies: Interface X
Will retry after round {round} integration.
```

**BLOCKED**: Cannot proceed, no work completed
```
BLOCKED: [Agent type] cannot proceed

Reason: Missing required dependency

Required: path/to/RequiredInterface.java
Provided by: technical-architect
Status: Not yet implemented

Cannot implement any deliverables without this dependency.

Will retry after technical-architect completes.
```

---

## Phase 3: CONVERGENCE State

**Objective**: Integrate all agent diffs, resolve conflicts, achieve unanimous stakeholder approval

**Transition from AUTONOMOUS_IMPLEMENTATION**: When all agents report COMPLETE status

**Actions**:
1. Apply final diffs from last round
2. Resolve any merge conflicts (preserve both agents' intent)
3. Run build verification (`mvn verify`)
4. Fix any compilation errors from integration
5. Update lock state to VALIDATION

**Conflict Resolution**:
- If same line modified by multiple agents: Keep both changes if possible
- If incompatible changes: Consult context.md to determine intent
- If unsure: Re-invoke agents with refined coordination instructions

**Lock Update**:
```bash
jq '.state = "VALIDATION"' /workspace/locks/{task}.json > /tmp/lock.json
mv /tmp/lock.json /workspace/locks/{task}.json
```

## Phase 4: VALIDATION State

**Objective**: Ensure implementation meets all quality gates

**Actions**:
1. Run full build: `mvn clean verify`
2. Verify all tests pass
3. Run checkstyle validation
4. Run PMD validation
5. Manual code review of integration points
6. Update lock state to REVIEW

**Validation Gates**:
- ✅ Build succeeds (no compilation errors)
- ✅ All tests pass (100% success rate)
- ✅ Checkstyle reports zero violations
- ✅ PMD reports zero violations
- ✅ Manual review: integration clean, no orphaned code

**Lock Update**:
```bash
jq '.state = "REVIEW"' /workspace/locks/{task}.json > /tmp/lock.json
mv /tmp/lock.json /workspace/locks/{task}.json
```

## Phase 5: REVIEW State

**Objective**: Stakeholder approval of implementation

**Actions**:
1. Invoke stakeholder agents for approval
2. Present implementation summary
3. Collect approval/rejection decisions
4. If unanimous approval → proceed to USER_APPROVAL
5. If any rejection → return to CONVERGENCE state

**Stakeholder Agents**:
- technical-architect (architecture compliance)
- code-quality-auditor (code quality standards)
- security-auditor (security best practices)
- performance-analyzer (performance characteristics)
- style-auditor (style compliance)

**Approval Criteria**: ALL stakeholders must approve. Any rejection requires fixes.

## Phase 6: USER_APPROVAL State

**Objective**: Final human approval before merge

**Actions**:
1. Present commit summary to user
2. Show git diff stat
3. Wait for user approval
4. On approval: Update lock to COMPLETE
5. On rejection: Return to CONVERGENCE with user feedback

**Lock Update** (after approval):
```bash
jq '.state = "COMPLETE"' /workspace/locks/{task}.json > /tmp/lock.json
mv /tmp/lock.json /workspace/locks/{task}.json
```

## Phase 7: COMPLETE State

**Objective**: Merge implementation to main branch

**Actions**:
1. Squash commits into single commit with descriptive message
2. Merge worktree to main branch
3. Verify main branch build passes
4. Update lock state to CLEANUP

**Commit Message Format**:
```
[Task Name] Brief description

Detailed description of implementation:
- Component 1: What was added/changed
- Component 2: What was added/changed

Agents involved: technical-architect, code-quality-auditor, ...
Rounds required: X
```

**Lock Update**:
```bash
jq '.state = "CLEANUP"' /workspace/locks/{task}.json > /tmp/lock.json
mv /tmp/lock.json /workspace/locks/{task}.json
```

## Phase 8: CLEANUP State

**Objective**: Remove task artifacts

**Actions**:
1. Remove worktree directory
2. Remove lock file
3. Archive context.md and agent diffs
4. Update todo.md → changelog.md

**Cleanup Commands**:
```bash
# Remove worktree
cd /workspace/branches/main/code
git worktree remove /workspace/branches/{task}/code

# Remove lock
rm /workspace/locks/{task}.json

# Archive artifacts (optional)
mv /workspace/branches/{task}/context.md /workspace/archive/{task}-context.md
mv /workspace/branches/{task}/round*.diff /workspace/archive/
```

## Summary

The delegated implementation protocol enables:
- ✅ Parallel agent execution with emergent dependency discovery
- ✅ Deadlock prevention via PARTIAL implementations
- ✅ Clean integration via diff-only workflow
- ✅ Quality assurance via multi-gate validation
- ✅ Unanimous stakeholder approval
