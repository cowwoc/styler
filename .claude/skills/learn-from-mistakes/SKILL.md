---
name: learn-from-mistake
description: Investigate agent mistakes, perform root cause analysis, and update configurations to prevent recurrence
allowed-tools: Read, Write, Edit, Bash, Grep, Glob
---

# Learn From Mistake Skill

**Purpose**: Investigate agent mistakes, perform root cause analysis, update agent configurations, and TEST fixes by reproducing the original mistake to verify prevention.

**When to Use**:
- After an agent makes a mistake that causes delays, rework, or violations
- When a pattern of similar mistakes is observed across tasks
- To proactively improve agent behavior based on lessons learned
- When protocol violations occur that should have been caught

## Skill Workflow

**Overview**: The skill follows an 8-phase process:

1. **Mistake Identification** - Gather context about what went wrong
2. **Conversation Analysis** - Review logs to understand mistake manifestation
3. **Root Cause Analysis** - Categorize and investigate why it happened
4. **Configuration Updates** - Create prevention measures (hooks, docs, examples)
5. **Implement Updates** - Apply changes to agent configs and documentation
6. **Validation** - Review updates for completeness and consistency
7. **Apply Fixes and Test by Reproduction** - ⚠️ TEST by attempting to reproduce the mistake
8. **Documentation** - Add inline comments and git commit messages

**Critical Difference**: Unlike mental simulation, Phase 7 requires ACTUALLY attempting to reproduce the original mistake after applying fixes to verify they work.

### Phase 1: Mistake Identification

**Input Requirements**:
- Agent name that made the mistake (e.g., `architecture-updater`, `style-reviewer`, `main`)
- Task name if applicable (e.g., `implement-formatter-api`)
- Description of what went wrong
- Conversation ID or timestamp range (optional - will search recent if not provided)

**Questions to Ask User**:
1. Which agent made the mistake?
2. What task were they working on?
3. What specifically went wrong?
4. Do you have the conversation ID? (Can skip - will search recent)

### Phase 2: Conversation Analysis

**Access Conversation Logs**:
```bash
# Find recent conversations for this project
PROJECT_DIR=~/.config/projects/$(basename $(pwd))
CONVERSATIONS_DIR="$PROJECT_DIR/conversations"

# If conversation ID provided, use it directly
if [ -n "$CONVERSATION_ID" ]; then
  CONV_FILE="$CONVERSATIONS_DIR/$CONVERSATION_ID/conversation.jsonl"
else
  # Find most recent conversations (last 5)
  CONV_FILES=$(ls -t "$CONVERSATIONS_DIR"/*/conversation.jsonl | head -5)
fi

# Read and parse conversation
for file in $CONV_FILES; do
  # Look for mentions of the agent name
  grep -A 50 "$AGENT_NAME" "$file" | jq -r '.message.content // .content'
done
```

**What to Extract**:

1. **Agent Invocation Context**:
   - What prompt was given to the agent
   - What files/context were available
   - What state the task was in
   - Working directory specified

2. **Agent Actions**:
   - Sequence of tool calls made
   - Files read, written, edited
   - Commands executed
   - Directories changed to

3. **Mistake Manifestation**:
   - Error messages
   - Incorrect output
   - Protocol violations
   - Build failures
   - Rework needed

4. **Recovery Actions**:
   - What was done to fix the mistake
   - How much rework was required
   - What additional context was needed

### Phase 3: Root Cause Analysis

**Categorize the Mistake**:

#### A. Missing Information
- Agent prompt lacked necessary context
- Required files not mentioned
- Assumptions not stated explicitly
- Working directory not clear

#### B. Misunderstood Requirements
- Ambiguous protocol documentation
- Conflicting instructions
- Unclear success criteria
- No examples for this scenario

#### C. Tool Usage Errors
- Wrong tool for the job
- Incorrect parameters
- Missing validation steps
- Tool limitations not understood

#### D. Logic Errors
- Incorrect algorithm
- Missing edge cases
- Faulty assumptions
- Incomplete understanding of domain

#### E. Protocol Violations
- Skipped required steps
- Wrong state for action
- Missing validations
- Worktree confusion

#### F. Configuration Gaps
- Agent config missing examples
- No guidance for this scenario
- Anti-patterns not documented
- Missing checklists

**Root Cause Investigation**:

For each category, investigate:

1. **What information was available to the agent?**
   - Read the agent's invocation prompt
   - Check what files they had access to
   - Identify what context was missing

2. **What should the agent have done instead?**
   - Identify the correct action sequence
   - Note what additional information was needed
   - Determine what checks were missing

3. **Why did the agent choose the wrong path?**
   - Was the correct path documented?
   - Were there ambiguous instructions?
   - Was there a misleading example?
   - Was there insufficient validation?

### Phase 4: Configuration Updates

**Update Strategy by Category**:

#### A. Missing Information → Update Agent Prompt Template

**File**: `.claude/agents/{agent-name}.md`

**Update Section**: "Required Context" or "Before You Begin"

**Add**:
- Explicit checklist of information to verify
- Required file reads before starting
- Questions to ask if context unclear
- Working directory verification steps

**Example**:
```markdown
## Before You Begin - MANDATORY CHECKS

Before ANY implementation work:

- [ ] Verify working directory: `pwd`
- [ ] Confirm you're in: `/workspace/tasks/{task}/agents/{agent-name}/code/`
- [ ] Read task requirements: `/workspace/tasks/{task}/task.md`
- [ ] Check task state: `cat /workspace/tasks/{task}/task.json`
- [ ] Verify dependencies available

If ANY check fails: STOP and report the issue.
```

#### B. Misunderstood Requirements → Clarify Documentation

**File**: `docs/project/{relevant-protocol}.md`

**Updates**:
- Add explicit requirement statements
- Include concrete examples (✅ CORRECT vs ❌ WRONG)
- Document edge cases
- Add anti-pattern warnings
- Create decision trees for complex scenarios

**Example**:
```markdown
## Working Directory Requirements

⚠️ **CRITICAL**: Sub-agents MUST work in their agent worktree

**Your worktree**: `/workspace/tasks/{task}/agents/{agent-name}/code/`
**NOT the task worktree**: `/workspace/tasks/{task}/code/` ← WRONG

✅ **CORRECT**:
\```bash
cd /workspace/tasks/implement-api/agents/architecture-updater/code
Write: src/main/java/MyClass.java
\```

❌ **WRONG** (Protocol Violation):
\```bash
cd /workspace/tasks/implement-api/code
Write: src/main/java/MyClass.java  # Creates in task worktree!
\```
```

#### C. Tool Usage Errors → Add Tool Guidance

**File**: `.claude/agents/{agent-name}.md`

**Add Section**: "Tool Usage Patterns for {Agent Name}"

**Include**:
- When to use each tool
- Common pitfalls
- Validation steps required
- Example sequences
- Error recovery procedures

**Example**:
```markdown
## Tool Usage Patterns

### Write Tool
**When**: Creating new files
**Before**: Verify directory with `pwd`
**After**: Verify file created with `ls -la {file}`
**Validation**: File must be in agent worktree

### Edit Tool
**When**: Modifying existing files
**Before**: Read file first to see exact content
**After**: Read file again to verify changes
**Pitfall**: Don't include line numbers in old_string
```

#### D. Logic Errors → Add Examples and Tests

**File**: `.claude/agents/{agent-name}.md`

**Add**:
- Worked examples for similar scenarios
- Edge cases to consider
- Validation checklist
- Self-test questions
- Common pitfalls

**Example**:
```markdown
## Common Scenarios

### Scenario: Creating New Maven Module

**Context**: Task requires new Maven module

**Steps**:
1. Create directory: `mkdir -p {module}/src/main/java`
2. Create POM: Use template from existing module
3. Add to parent POM: `<module>{module}</module>`
4. Create module-info.java: Define exports
5. Verify: `mvn compile -pl {module}`

**Edge Cases**:
- Module dependencies on other modules
- JPMS transitive requirements
- Test module descriptor

**Self-Test**:
- [ ] Did I update parent POM?
- [ ] Does module compile?
- [ ] Are exports correct?
```

#### E. Protocol Violations → Add Validation Hooks

**File**: `.claude/hooks/pre-{action}.sh`

**Create Hook**:
- Pre-action validation
- Check prerequisites
- Verify state requirements
- Block violation with helpful message

**Example**:
```bash
#!/bin/bash
# .claude/hooks/pre-write.sh
# Validates worktree before Write tool use

set -euo pipefail

# Extract agent name from context
AGENT_NAME=${CLAUDE_AGENT_NAME:-main}

# For sub-agents, verify working in agent worktree
if [[ "$AGENT_NAME" != "main" ]]; then
  EXPECTED_PATH="/workspace/tasks/.*/agents/$AGENT_NAME/code"
  if [[ ! "$PWD" =~ $EXPECTED_PATH ]]; then
    echo "❌ ERROR: Agent $AGENT_NAME in wrong directory" >&2
    echo "Current: $PWD" >&2
    echo "Expected: /workspace/tasks/{task}/agents/$AGENT_NAME/code/" >&2
    echo "" >&2
    echo "SOLUTION: cd to your agent worktree first" >&2
    exit 1
  fi
fi
```

#### F. Configuration Gaps → Enhance Agent Config

**File**: `.claude/agents/{agent-name}.md`

**Add Sections**:
- "Common Scenarios" with examples
- "Anti-Patterns to Avoid"
- "Decision Trees" for complex choices
- "Verification Steps" before completion
- "Self-Validation Checklist"

**Example**:
```markdown
## Anti-Patterns to Avoid

### ❌ Creating files without directory verification
**Problem**: Files created in wrong location
**Solution**: Always `pwd` before Write/Edit

### ❌ Skipping build verification
**Problem**: Broken code merged to task branch
**Solution**: Run `mvn compile` before merge

### ❌ Assuming file locations
**Problem**: Files not found or created in wrong place
**Solution**: Read files to confirm paths

## Self-Validation Checklist

Before reporting completion:
- [ ] All files in correct worktree
- [ ] Code compiles: `mvn compile`
- [ ] Tests pass: `mvn test`
- [ ] No protocol violations
- [ ] Status.json updated
```

### Phase 5: Implement Updates

**For Each Update Identified**:

1. **Read Current Configuration**:
   ```bash
   Read: .claude/agents/{agent-name}.md
   # or
   Read: docs/project/{protocol-file}.md
   ```

2. **Identify Update Location**:
   - Find relevant section
   - Determine if adding new section or enhancing existing
   - Check for conflicts with existing guidance

3. **Draft Update**:
   - Write clear, specific guidance
   - Include concrete examples (✅ vs ❌)
   - Reference the mistake that prompted the update
   - Add validation steps
   - Use consistent terminology

4. **Apply Update**:
   ```bash
   Edit: .claude/agents/{agent-name}.md
   # Add new section or enhance existing
   ```

5. **Verify Update**:
   - Read updated file
   - Confirm changes are clear and actionable
   - Check no conflicts with existing guidance
   - Ensure examples are concrete

### Phase 6: Validation and Testing

**Validation Steps**:

1. **Completeness Check**:
   - [ ] Update addresses root cause
   - [ ] Examples are concrete and actionable
   - [ ] Validation steps are clear
   - [ ] Anti-patterns are explicit
   - [ ] Self-test questions included

2. **Consistency Check**:
   - [ ] No conflicts with other agent configs
   - [ ] Aligns with project conventions
   - [ ] Terminology is consistent
   - [ ] Format matches existing sections

### Phase 7: Apply Fixes and Test by Reproduction

**⚠️ CRITICAL: Test fixes by attempting to reproduce the original mistake**

This phase ensures your configuration updates actually prevent the mistake from recurring.

**Testing Workflow**:

1. **Apply All Configuration Updates**:
   ```bash
   # Ensure all edits are saved
   Read: .claude/agents/{agent-name}.md  # Verify changes applied
   Read: .claude/hooks/{hook-name}.sh    # Verify hook created/updated
   ```

2. **Make Hooks Executable** (if new hooks created):
   ```bash
   chmod +x .claude/hooks/{hook-name}.sh
   ```

3. **Verify Hook Registration** (if applicable):
   ```bash
   # Check hook is registered in settings.json
   grep -A5 "{hook-name}" /workspace/.claude/settings.json
   ```

4. **Attempt to Reproduce the Original Mistake**:

   **Goal**: Try to make the same mistake that triggered this skill invocation

   **Method**: Execute the same action sequence that caused the original mistake

   **Examples**:

   - **If mistake was wrong worktree**: Try to create a file in the wrong worktree
   - **If mistake was missing validation**: Try to skip the validation step
   - **If mistake was incorrect tool usage**: Try to use the tool incorrectly
   - **If mistake was protocol violation**: Try to violate the protocol

   **Expected Outcome**: Hook should BLOCK the mistake or guidance should prevent it

5. **Verify Prevention Mechanism**:

   **For Hook-Based Prevention**:
   ```bash
   # Example: Testing pre-write hook prevents wrong worktree
   cd /workspace/tasks/test-task/code  # Wrong worktree for sub-agent

   # Try to trigger Write tool (should be blocked by hook)
   # Hook should output error message and prevent the action
   ```

   **For Documentation-Based Prevention**:
   - Read the updated agent config as if you were that agent
   - Follow the workflow described
   - Verify the guidance is clear enough to prevent the mistake
   - Check that examples show the correct path

6. **Interpret Test Results**:

   **✅ SUCCESS - Fix is Effective**:
   - Hook blocks the incorrect action with clear error message
   - OR guidance clearly directs agent to correct approach
   - Error message explains what went wrong and how to fix it

   **❌ FAILURE - Fix is Ineffective**:
   - Mistake can still be reproduced
   - Hook doesn't trigger or has wrong condition
   - Guidance is unclear or ambiguous
   - Error message is confusing

   **Action on Failure**: Return to Phase 4 (Configuration Updates) and refine

7. **Iteration on Failed Tests**:

   If the test fails (mistake can still be reproduced):

   a. **Diagnose Why Fix Failed**:
      - Was hook condition too narrow?
      - Was guidance not prominent enough?
      - Did agent have a valid reason to bypass?
      - Is there an edge case not covered?

   b. **Refine the Fix**:
      - Broaden hook detection patterns
      - Make guidance more prominent (⚠️ CRITICAL)
      - Add more specific examples
      - Cover the edge case

   c. **Re-test**: Repeat reproduction attempt

   d. **Iterate**: Continue until test succeeds

8. **Test Edge Cases**:

   Once basic reproduction test passes, test variations:
   - Different agents encountering same scenario
   - Slight variations of the mistake pattern
   - Legitimate use cases that should NOT be blocked
   - Ensure no false positives

**Testing Checklist**:

- [ ] All configuration updates applied and verified
- [ ] New hooks are executable and registered
- [ ] Attempted to reproduce original mistake
- [ ] Prevention mechanism activated correctly
- [ ] Error messages are clear and actionable
- [ ] Legitimate use cases not blocked (no false positives)
- [ ] Edge cases tested
- [ ] If test failed: Iterated and refined until successful

**Documentation of Test Results**:

Include test results in git commit message:

```
Add worktree validation to pre-write hook

**Fix Applied**: Created pre-write hook to verify agent worktree

**Testing**: Attempted to create file in wrong worktree:
- cd /workspace/tasks/test/code (task worktree, not agent worktree)
- Attempted Write tool
- ✅ Hook correctly blocked with error: "Agent must work in agent worktree"
- ✅ Error message clearly explains expected path

**Verified**: Fix prevents recurrence of original mistake
```

### Phase 8: Documentation

**⚠️ CRITICAL: NO RETROSPECTIVE DOCUMENTS**

Per CLAUDE.md "RETROSPECTIVE DOCUMENTATION POLICY": Do NOT create standalone retrospective documents chronicling mistakes or fixes.

**CORRECT Documentation Approach**:

1. **Inline Comments in Updated Files** ✅
   - Add comments to hook/config files explaining pattern evolution
   - Document context for why specific patterns were added
   - Include brief history of what problems patterns prevent

   **Example** (in `.claude/hooks/detect-giving-up.sh`):
   ```bash
   # PATTERN EVOLUTION HISTORY:
   # - Initial patterns: Explicit giving-up phrases ("too hard", "let's skip")
   # - 2025-10-30: Added rationalization patterns after main agent attempted to
   #   remove dependencies using "pragmatic decision" language to disguise
   #   giving up on compilation debugging
   ```

2. **Git Commit Message** ✅
   - Detailed explanation of what was fixed
   - Why the change prevents recurrence
   - Context about the original mistake

   **Example**:
   ```
   Add rationalization pattern detection to prevent-giving-up hook

   Added 9 new patterns to detect disguised giving-up using "pragmatic"
   language. Prevents: attempting to remove dependencies instead of
   debugging compilation issues while framing it as "architectural choice"
   or "considering time constraints".

   Context: Main agent encountered empty JAR issue and attempted to
   simplify formatter API instead of investigating why security/config
   modules weren't compiling.
   ```

3. **Code Comments for Rationale** ✅
   - Explain WHY certain approaches exist
   - Document alternatives considered
   - Note edge cases handled

**PROHIBITED**:
- ❌ `docs/project/lessons-learned.md` (retrospective chronicle)
- ❌ Standalone markdown files documenting development process
- ❌ "Lessons learned" documents
- ❌ Multi-phase retrospective reports

**Summary of Documentation**:
- Inline comments: Pattern evolution and context
- Git commits: Detailed rationale
- CLAUDE.md: Universal guidance if broadly applicable
- Protocol docs: If protocol-specific changes needed

## Implementation Example

### Example: Architecture-Updater Created Files in Wrong Worktree

**Mistake**: Agent created source files in task worktree instead of agent worktree, causing protocol violation.

**Root Cause Analysis**:
1. **Category**: E. Protocol Violations (wrong worktree)
2. **Available Information**: Agent prompt mentioned working directory but didn't emphasize the distinction
3. **What Happened**: Agent used `cd /workspace/tasks/{task}/code/` instead of `cd /workspace/tasks/{task}/agents/architecture-updater/code/`
4. **Why**: Prompt said "working directory" but didn't make the agent worktree requirement critical

**Updates Applied**:

1. **Agent Config** (`.claude/agents/architecture-updater.md`):
   ```markdown
   ## ⚠️ CRITICAL: Working Directory

   **YOU MUST WORK IN YOUR AGENT WORKTREE**

   **Your worktree**: `/workspace/tasks/{task-name}/agents/architecture-updater/code/`
   **NOT the task worktree**: `/workspace/tasks/{task-name}/code/` ← PROTOCOL VIOLATION

   **Before ANY Write/Edit tool use**:
   1. Verify current directory: `pwd`
   2. Confirm you're in `/workspace/tasks/{task}/agents/architecture-updater/code/`
   3. If not, this is a CRITICAL ERROR - stop and report

   **Example**:
   ```bash
   # ✅ CORRECT
   cd /workspace/tasks/implement-api/agents/architecture-updater/code
   Write: src/main/java/MyClass.java

   # ❌ WRONG - PROTOCOL VIOLATION
   cd /workspace/tasks/implement-api/code
   Write: src/main/java/MyClass.java  # Creates in task worktree!
   ```
   ```

2. **Validation Hook** (`.claude/hooks/pre-write.sh`):
   ```bash
   #!/bin/bash
   # Pre-Write hook: Verify agent is in correct worktree

   set -euo pipefail
   trap 'echo "ERROR in pre-write.sh at line $LINENO" >&2; exit 1' ERR

   AGENT_NAME=${CLAUDE_AGENT_NAME:-main}

   if [[ "$AGENT_NAME" != "main" ]]; then
     EXPECTED_PATH="/workspace/tasks/.*/agents/$AGENT_NAME/code"
     if [[ ! "$PWD" =~ $EXPECTED_PATH ]]; then
       echo "❌ ERROR: Agent $AGENT_NAME in wrong directory" >&2
       echo "Current: $PWD" >&2
       echo "Expected: /workspace/tasks/{task}/agents/$AGENT_NAME/code/" >&2
       exit 1
     fi
   fi
   ```

3. **Documentation** (`docs/project/task-protocol-agents.md`):
   ```markdown
   ## ⚠️ CRITICAL: Agent Worktree Isolation

   **Each agent MUST work in their own worktree**:
   - Main agent: `/workspace/tasks/{task}/code/`
   - Sub-agent: `/workspace/tasks/{task}/agents/{agent-name}/code/`

   **Common Mistake**: Creating files in task worktree instead of agent worktree

   **Prevention**:
   1. Always `cd` to your agent worktree first
   2. Verify with `pwd` before Write/Edit
   3. Check path includes `agents/{agent-name}/code/`

   **Verification**: Pre-write hook blocks if in wrong directory
   ```

4. **Inline Comments in Hook** (`.claude/hooks/pre-write.sh`):
   ```bash
   #!/bin/bash
   # Pre-Write hook: Verify agent is in correct worktree
   #
   # ADDED: 2025-10-30 after architecture-updater created files in task worktree
   # instead of agent worktree during implement-formatter-api task.
   # PREVENTS: Protocol violations from wrong working directory

   set -euo pipefail

   AGENT_NAME=${CLAUDE_AGENT_NAME:-main}

   # For sub-agents, verify working in agent worktree
   if [[ "$AGENT_NAME" != "main" ]]; then
     EXPECTED_PATH="/workspace/tasks/.*/agents/$AGENT_NAME/code"
     if [[ ! "$PWD" =~ $EXPECTED_PATH ]]; then
       echo "❌ ERROR: Agent $AGENT_NAME in wrong directory" >&2
       echo "Current: $PWD" >&2
       echo "Expected: /workspace/tasks/{task}/agents/$AGENT_NAME/code/" >&2
       exit 1
     fi
   fi
   ```

5. **Git Commit Message**:
   ```
   Add worktree validation to pre-write hook

   **Fix Applied**:
   - Updated: `.claude/agents/architecture-updater.md` (added critical warning)
   - Created: `.claude/hooks/pre-write.sh` (worktree validation)
   - Enhanced: `docs/project/task-protocol-agents.md` (common mistakes section)

   **Prevention**:
   - Pre-write hook blocks creation in wrong worktree
   - Agent config emphasizes worktree requirement
   - Examples show correct vs wrong patterns

   **Verification**:
   Hook tested by attempting write in wrong directory - correctly blocked
   ```

## Success Criteria

Skill execution is successful when:

1. ✅ **Root cause identified**: Clear understanding of why mistake occurred
2. ✅ **Updates applied**: Agent config/documentation enhanced with specific guidance
3. ✅ **Fixes tested by reproduction**: Attempted to reproduce original mistake after applying fixes
4. ✅ **Prevention verified**: Reproduction test confirms mistake is now blocked/prevented
5. ✅ **Documentation added**: Inline comments in hooks/configs + git commit message with test results
6. ✅ **No side effects**: Updates don't break existing functionality or create false positives
7. ✅ **Examples included**: Concrete ✅/❌ examples added
8. ✅ **Validation added**: Hooks or checklists created where appropriate
9. ✅ **No retrospective docs**: Did NOT create standalone lessons-learned.md or similar

## Usage

### Interactive Mode
```
/learn-from-mistake

Then answer the prompts:
- Agent: architecture-updater
- Task: implement-formatter-api
- Issue: Created files in wrong worktree
```

### Direct Invocation
```
Learn from the mistake where architecture-updater created source files in the task worktree instead of their agent worktree during implement-formatter-api task. This caused a protocol violation and required rework.
```

### Via Skill Tool
```
Skill: "learn-from-mistake"
```

## Output Files

After execution, expect these files to be created/modified:

1. **Agent Configuration**: `.claude/agents/{agent-name}.md` (updated with examples)
2. **Protocol Documentation**: `docs/project/{relevant-file}.md` (enhanced if protocol-related)
3. **Validation Hooks**: `.claude/hooks/pre-{action}.sh` (created/updated with inline comments)
4. **Git Commit**: Detailed commit message documenting the fix and rationale

**NO retrospective documents created** - all documentation is inline or in git commits.

## Integration with Audit

This skill complements the audit system:

- **Audit identifies** → Skill fixes
- **Audit measures** → Skill prevents
- **Audit finds patterns** → Skill updates configs

**Continuous Improvement Loop**:
```
Execute Task → Audit → Learn → Update → Improved Execution
```

## Notes

- **Focus on systemic improvements**, not one-off fixes
- **Prioritize high-impact mistakes** (protocol violations, significant delays)
- **Keep updates specific and actionable** with concrete examples
- **Include examples from actual mistakes** not hypothetical scenarios
- **Verify changes don't create conflicts** with existing guidance
- **Document reasoning** for future reference
- **Test hooks** if created to ensure they work correctly

## When NOT to Use This Skill

❌ **Skip for**:
- One-time errors unlikely to recur
- Issues already well-documented with examples
- Mistakes with no clear systemic fix
- Minor delays (<10 minutes) with no pattern
- User errors (not agent mistakes)

✅ **Use for**:
- Protocol violations requiring rework
- Repeated mistakes across tasks
- Missing documentation gaps
- Systematic tool usage errors
- Configuration ambiguities
