---
description: >
  Optimize documentation for conciseness using fact-based validation: remove redundancy while preserving all information
---

# Optimize Documentation Command

**Task**: Optimize the documentation file: `{{arg}}`

**Idempotent Design**: Can run multiple times - each pass makes further improvements until document is optimally concise.

**⚠️ CRITICAL - Agent Type Restriction**:
- **MUST use** `subagent_type: "general-purpose"` for ALL Task tool invocations
- **DO NOT use** any other agent type (optimizer, hacker, designer, builder, etc.)
- This command is specifically designed for general-purpose agents only
- Using other agent types will cause execution failures or suboptimal behavior

---

## Part 1: Scope & Principles

### 🚨 DOCUMENT TYPE VALIDATION

#### Claude-Facing Documents (ALLOWED)
- `.claude/` configuration files (agents, commands, hooks, settings)
- `CLAUDE.md` and project instructions for Claude
- `docs/project/` development protocol documentation (task-protocol-*, agent-*, *-guide.md)
- `docs/code-style/*-claude.md` style detection patterns for Claude

#### Human-Facing Documents (FORBIDDEN)
- `README.md` files (user-facing project documentation)
- `changelog.md`, `CHANGELOG.md` (user-facing release notes)
- `docs/studies/` research and analysis documents
- `docs/decisions/` historical decision records
- `docs/performance/` performance documentation
- `docs/optional-modules/` potentially user-facing guides
- `todo.md` (working task list)
- `docs/code-style/*-human.md` human explanations
- Any documentation in project root intended for human developers

#### Validation Procedure

**BEFORE optimizing, check the file path:**

1. **If path matches forbidden patterns, REFUSE**:
   ```
   This command only optimizes Claude-facing documentation.

   The file `{{arg}}` appears to be human-facing documentation (README, changelog, studies, etc.).
   ```

2. **If path matches allowed patterns, PROCEED** with optimization

### Core Objective

**Conciseness vs Correctness Hierarchy** (priority order):

1. **CORRECTNESS** (highest priority)
   - Can Claude execute correctly without this content?
   - Does removing this introduce ambiguity or misinterpretation?
2. **EFFICIENCY** (medium priority)
   - Does removing this make instructions faster to scan?
   - Does condensing reduce cognitive load?
3. **CONCISENESS** (lowest priority)
   - Does this reduce line count?
   - Does this tighten prose?

**🚨 PRIORITY RULE**: Never sacrifice correctness for conciseness. Always sacrifice conciseness for correctness.

### Success Criteria: Optimal State

Document is optimally concise when ALL measurable criteria pass:

1. **Zero Narrative Redundancy**
   - Test: `grep -n 'command will' {{arg}} | wc -l` = 0 (explanations restating commands)
   - No instruction immediately followed by "This command...", "This will...", "This means..."

2. **Minimal Pure Rationale**
   - Test: Count "Benefits:", "This is important because" not in execution-critical sections
   - Threshold: ≤3 pure rationale blocks (Type A from Category 5)
   - Execution-critical context (Type B) is protected

3. **Maximum 2 Examples Per Concept**
   - Test: No concept demonstrated 3+ times with trivial variations
   - Distinct boundaries (❌ vs ✅) exempt from limit
   - Each example must show different edge case or parameter combination

4. **Zero Duplicate Definitions**
   - Test: Each term defined once only (grep for "**X**: " or "**X** -")
   - Exception: Contextually different aspects are not duplicates

5. **Zero Pedagogical Repetition**
   - Test: Key rules stated once in reference docs
   - Convergence boundary: If same line range cataloged 2+ iterations → stop removing

---

## Part 2: Understanding Content Types

### Content Classification: Instructions vs Examples

#### INSTRUCTION (NEVER remove)
These are the actionable requirements that Claude must execute:

- **Commands to execute**: `grep -i "keywords" file.md`
- **Questions to ask**: "Ask: 'Is there an imported data sheet?'"
- **Specific actions**: "Close Excel, wait 2 seconds, retry"
- **Conditions to check**: "If grep returns results → do X"

**Identifying test**: Would removing this prevent execution? (i.e., lose a command to run, condition to check, or action to perform)
- YES → It's an INSTRUCTION, keep it
- NO → It's an EXAMPLE, evaluate further

#### EXAMPLE (May remove if instruction is already clear)
- **Demonstrations**: "For example: grep -i 'foo'"
- **Sample outputs**: "You should see: [output]"
- **Illustrative scenarios**: "Like this: [scenario]"

### Protected Content Categories

#### 1. Executable Syntax
Always keep content that provides exact syntax needed for execution:

- **Commands**: Bash, jq, git workflows
- **Data formats**: JSON schemas, file paths, configuration structures
- **Numeric constraints**: Thresholds ("6-30 seconds"), limits ("100MB max"), counts ("at least 3")

#### 2. Decision Boundaries
Always keep examples that show where rules start/stop applying:

- **Wrong vs correct patterns**: Shows what NOT to do vs what TO do
- **Edge cases**: Demonstrates boundary location (where rule starts/stops applying)
- **Ambiguity resolvers**: Examples defining vague terms in instructions

#### 3. Workflow Sequences
Always keep content that defines execution order:

- **Sequential steps**: Numbered lists where order matters for correctness
- **Temporal dependencies**: Before/after relationships, WHEN conditions
- **Blocking checkpoints**: STOP, WAIT, ONLY THEN, NOW markers

#### 4. Pattern Extraction
- **Recognition rules**: "This shows that X means Y", "When you see A, it indicates B"
- **Generalization**: Extracts reusable decision principle from specific example
- **NOT rationale**: Does NOT explain WHY rule exists, only HOW to recognize pattern

**Examples**:
- ✅ KEEP: "→ Shows that 'delete' means remove lines, not change checkbox"
- ❌ REMOVE: "This is important because it prevents errors"

### 🚨 Reference Replacement Rules

#### ❌ NEVER Replace with References

1. **Content within sequential workflows** (Steps 1→2→3)
2. **Success criteria at decision points**

#### ✅ OK to Replace with References

1. **Explanatory content that appears in multiple places**
2. **Content at document boundaries** (intro/conclusion)
3. **Cross-referencing related but distinct concepts**

---

## Part 3: Evaluation Framework

### Redundancy Categories

#### Category 1: Narrative Redundancy
**Pattern**: Instruction followed by explanation restating the same information

**Example**:
```
Run `grep -i 'keywords' file.md` to search the file.
This command will search the file for keywords in a case-insensitive manner.
```

**Fix**: Remove the explanatory restatement, keep the instruction

---

#### Category 2: Example Redundancy
**Pattern**: Multiple examples demonstrating same principle

**Detection**: 3+ code blocks showing same concept

**Fix Strategy**:

**For Identical Examples** (zero meaningful variation):
- Keep the 1 BEST example using these criteria (in order):
  1. Most complete? (shows all parameters, full workflow)
  2. Shows success criteria? (what output/result to expect)
  3. Demonstrates boundary? (wrong vs right, when to apply)
- First example satisfying ALL 3 criteria wins
- If none satisfy all 3, keep first example encountered

**For Similar Examples** (variations present):
- Check if variations are meaningful:
  - **Meaningful variations** (distinct edge cases/boundaries) → Keep all distinct
  - **Trivial variations** (just rephrasing same idea) → Keep max 2

---

#### Category 3: Definition Redundancy
**Pattern**: Term defined multiple times in different sections

**Detection**: Compare definitions - are they identical, similar with different detail, or contextually different?

**Fix Strategy**:
- **Identical definitions** → Keep in primary location, replace others with reference (unless mid-workflow)
- **One detailed, one brief** → Remove brief version (just inferior), keep detailed, then optimize detailed
- **Contextually different** (different aspects/use cases) → Keep both (not redundant)

---

#### Category 4: Procedural Redundancy
**Pattern**: Same procedure described in multiple places

**Fix**: Keep detailed procedure in one place, reference from others (unless interrupts workflow)

---

#### Category 5: Rationale Redundancy
**Pattern**: Explanations of WHY something is required

**CRITICAL DISTINCTION** - Two types of "WHY" content:

**Type A: Pure Rationale (REMOVABLE)**
- Explains benefits/importance AFTER stating the rule
- Pedagogical emphasis without execution impact
- Examples:
  - "This is important because it prevents errors"
  - "Benefits: Reduces false positives, improves accuracy"
  - "This matters because..." (when rule is already clear)

**Type B: Execution-Critical Context (PROTECTED)**
- Explains WHEN to apply a rule (conditional execution)
- Explains HOW to distinguish between options (decision logic)
- Clarifies ambiguous terms in instructions
- Examples:
  - "Purpose: Distinguish protected pattern instances from removable text" (explains WHAT to distinguish)
  - "Problem: Pattern matching alone produces false positives" (explains WHEN workaround needed)
  - "Why Both Needed: Positive alone means agent might guess..." (explains decision boundary)

**Detection Test**:
1. Remove the "WHY" content
2. Q: "Can Claude still execute correctly?"
   - If execution is clear → Type A (remove)
   - If execution becomes ambiguous → Type B (keep)

**Fix**: Remove Type A (pure rationale), keep Type B (execution-critical context)

---

#### Category 6: Pedagogical Redundancy
**Pattern**: Key rules stated multiple times for emphasis

**Fix**: Remove in reference docs - state rules once

---

### Instruction Clarity Evaluation

#### Test 1: Completeness
Are all parameters/inputs specified?

- ❌ FAIL: "Check grep" (which grep command? which file?)
- ✅ PASS: "Run `grep -i 'keywords' file.md`"

#### Test 2: Unambiguous
Could this be interpreted in only ONE way?

- ❌ FAIL: "Fix if possible" (what determines possible?)
- ✅ PASS: "Fix if deterministic (formula bug, wrong reference)"

#### Test 3: Self-sufficient
Can Claude execute without guessing or inferring?

- ❌ FAIL: "Use the script" (which script? what parameters?)
- ✅ PASS: "Use `claude/scripts/recalculate_excel.ps1 workbook.xlsx`"

#### Test 4: Preserved context
Is when/why to apply this clear?

- ❌ FAIL: "Import first" (import what? when?)
- ✅ PASS: "Before creating ACB schedule: Import transaction data to separate sheet"

### Removal Decision Process

#### Decision Tree

```
1. Is this an INSTRUCTION?
   YES → KEEP (never remove instructions)
   NO → Continue to question 2

2. Is this part of a Negative Constraint Pair?
   → Search within ±5 lines for paired positive/negative constraints
   → Check if both mention same concept (see Negative Constraint Pairing Rule)
   YES → KEEP BOTH (removing one breaks decision boundary)
   NO → Continue to question 3

3. Is this protected content? (Executable syntax, decision boundaries, workflow sequences, pattern extraction)
   YES → KEEP (automatic preservation)
   NO → Continue to question 4

4. Can Claude execute correctly without this content?
   NO → KEEP (execution-critical)
   YES → Continue to question 5

5. Does the instruction pass all 4 Clarity Tests without this content?
   NO → KEEP (needed for clarity)
   YES → Continue to question 6

6. Does this content explain WHY (rationale/educational)?
   YES → Check for protected patterns within this content:
         → Scan for: Sequential emphasis (STOP, WAIT, CRITICAL, MANDATORY, etc.)
         → Scan for: Template variables ({{...}})
         → Scan for: Boundary demonstrations (❌ paired with ✅)
         → Scan for: Decision criteria patterns (Test:, Ask:, Check:)
         → Scan for: Negative constraints (DO NOT, NEVER, FORBIDDEN)
         → Scan for: Execution context (How it works:, Where to look:, Fix:)
         → Scan for: Decision hierarchy questions (priority, correctness checks)
         → Scan for: Meta-categorization headers (Protected Content Categories, etc.)
         → Scan for: Category preambles (Always keep content that..., All content that...)
         IF protected patterns found → KEEP (protected content takes precedence)
         IF no protected patterns → REMOVE (pure rationale)
   NO → Continue to question 7

7. Does this content show WHAT "correct" looks like OR teach HOW to recognize patterns?
   YES → KEEP (success criteria or pattern extraction)
   NO → Continue to question 8

8. Is this redundant content? (Categories 1-6)
   NO → KEEP (unique content)
   YES → Check for protected patterns within this content:
         → Scan for: Sequential emphasis (STOP, WAIT, CRITICAL, MANDATORY, etc.)
         → Scan for: Template variables ({{...}})
         → Scan for: Boundary demonstrations (❌ paired with ✅)
         → Scan for: Decision criteria patterns (Test:, Ask:, Check:)
         → Scan for: Negative constraints (DO NOT, NEVER, FORBIDDEN)
         → Scan for: Execution context (How it works:, Where to look:, Fix:)
         → Scan for: Decision hierarchy questions (priority, correctness checks)
         → Scan for: Meta-categorization headers (Protected Content Categories, etc.)
         → Scan for: Category preambles (Always keep content that..., All content that...)
         IF protected patterns found → KEEP (protected content takes precedence)
         IF no protected patterns → Safe to remove, proceed with removal
```

#### Special Cases

**If instruction is VAGUE** (fails any Clarity Test):
1. DO NOT REMOVE EXAMPLES YET
2. First strengthen the instruction:
   - Replace subjective terms with explicit criteria
   - Convert narrative to numbered steps
   - Add measurable thresholds or boundaries
   - Define what "success" looks like
3. KEEP all examples until instruction is strengthened
4. Re-evaluate after strengthening

**If removing would interrupt workflow**:
- Don't replace with reference if content is within sequential steps (Steps 1→2→3)
- Don't replace with reference if content is success criteria at decision point
- See "Reference Replacement Rules" above

**Common Protection Examples**:
- ✅ KEEP: "**⚠️ CRITICAL - Agent Type Restriction**" (Sequential emphasis #4, Agent type restrictions #13)
- ✅ KEEP: "How Convergence Works: The iterative process naturally converges..." (Correctness guarantees #11)
- ✅ KEEP: "MUST use `subagent_type: general-purpose`" (Decision criteria #2, Agent type restrictions #13)

### Never-Remove List (Automatic Preservation)

The following 8 content categories are NEVER removed, regardless of redundancy:

**1. Executable Specifications**
All content needed to execute commands correctly:
- Template variables: `{{arg}}`, `{{...}}`
- Commands, file paths, jq expressions (in backticks/code blocks)
- Numeric thresholds: "6-30 seconds", "at least 3", "maximum 100MB"
- Data formats: JSON schemas, configuration structures
- Concrete benchmarks: "17-second gap", specific test results

**2. Decision Logic**
All content that guides choices and conditionals:
- Decision criteria: "Test:", "Ask:", "Check:", "Q:", "A:"
- Conditional patterns: "If X → Y", "When X, do Y"
- Boundary demonstrations: "❌ WRONG" paired with "✅ CORRECT"
- Agent/tool restrictions: "MUST use X", "DO NOT use Y" (both if paired)
- Special case handling: "If [condition] fails:" + numbered steps
- **Pairing Rule**: If positive + negative constraints about same topic → KEEP BOTH

**3. Execution Flow Control**
All content that defines sequence and timing:
- Sequential emphasis (context-aware): "STOP", "WAIT", "ONLY THEN", "NOW", "FIRST", "BEFORE", "AFTER"
- Workflow steps: "Step N:", numbered lists with dependencies
- Blocking checkpoints: "⚠️ CRITICAL", "MANDATORY"
- **Context-aware**: Only count in code blocks, headers, emphasis markers, pattern demonstrations
- **Exclude**: Plain text like "This is critical for success"

**4. Success Verification**
All content that defines correct outcomes:
- Expected outputs: "Expected output:", "Should see:", "Result:"
- Exit codes and states: "Exit code:", file state criteria
- Pattern extraction: "This shows that X means Y" (teaches recognition)
- Correctness guarantees: "Convergence", "Termination guarantee"

**5. Execution Context** (NEW - Prevents Oscillation)
All content that explains WHEN/HOW to apply instructions:
- Purpose statements: "**Purpose**: Distinguish X from Y"
- Problem statements: "**Problem**: X alone produces false positives"
- When-to-apply: "**When to use**:", prerequisite relationships
- Detection strategies: "**Detection**:" rules for pattern recognition
- Decision boundaries: "Why Both Needed: X alone means..."
- Mechanism descriptions: "**How it works**:", "**How Convergence Works**:"
- Search instructions: "**Where to look**:" (executable search steps)
- Fix strategies: "**Fix**:", "**Fix Strategy**:" (actionable solutions)
- Decision hierarchy questions: Questions that guide priority decisions (e.g., "Can Claude execute correctly?")
- Meta-categorization sections: Headers that categorize decision logic (e.g., "Protected Content Categories", "Redundancy Categories")
- **Test**: Remove it - does execution become ambiguous? If yes → PROTECTED

**6. Workflow Phase Dependencies**
All content explaining prerequisite relationships between phases:
- Phase purpose: Headers with "Phase", workflow dependency explanations
- Convergence mechanisms: How iterative processes terminate
- Safety mechanisms: Prevents infinite loops, data loss
- Conditional workflows: Headers "Special Cases", "Edge Cases", "Exceptions" + all branches

**7. Structural Metadata**
All content that labels algorithmic structure and coordinated improvements:
- OPTION labels: "OPTION N IMPROVEMENT:", "Step X.Y (OPTION N IMPROVEMENT)"
- Algorithmic markers showing coordinated features that work together
- **NOT alternatives**: All OPTION improvements execute together, not mutually exclusive
- **Distinguish from retrospective metadata**: OPTION labels show algorithm structure (forward-looking), not historical "why added" (retrospective)

**8. Category Definition Preambles**
Definitional text that enables generalization from examples (prevents oscillation):
- Category preambles: "Always keep content that...", "All content that...", "All content needed to..."
- Pattern: Appears immediately after category headers (#### N. Category Name)
- Purpose: Defines decision criteria for applying category to novel cases
- Example: "Always keep content that provides exact syntax needed for execution:" (Category 1)
- Example: "Always keep examples that show where rules start/stop applying:" (Category 2)
- Example: "Always keep content that defines execution order:" (Category 3)
- **Why protected**: Without preambles, agents pattern-match examples instead of generalizing principles
- **Oscillation risk**: Looks like pedagogical redundancy, will be repeatedly catalogued for removal

### 🚨 Negative Constraint Pairing Rule

**Pattern**: Positive requirement + negative constraint about same topic

**Examples**:
- "MUST use X" + "DO NOT use Y" (where Y = alternatives to X)
- "Include A" + "Never include B" (where B = anti-pattern of A)
- "Allowed: X, Y, Z" + "Forbidden: A, B, C"

**Detection**:
1. Find positive constraint (MUST, REQUIRED, ALWAYS)
2. Find negative constraint (DO NOT, NEVER, FORBIDDEN) in nearby lines (within 5 lines)
3. Both mention same concept (agent types, file patterns, commands, etc.)

**Rule**:
- If both constraints exist → Both are PROTECTED (Never-Remove List #13)
- Removing positive while keeping negative → Creates vague prohibition
- Removing negative while keeping positive → Loses boundary information

**Why Both Needed**:
- Positive alone: Agent might guess alternatives are allowed
- Negative alone: Agent doesn't know what TO do, only what NOT to do
- Together: Creates unambiguous decision boundary

**Validation Test**:
- Q: "What are ALL valid options?"
- A: Must be answerable from positive constraint alone
- Q: "What are ALL invalid options?"
- A: Must be answerable from negative constraint alone
- If either answer is "Cannot determine from document" → Missing constraint

**Examples in Practice**:

✅ CORRECT (both present):
```
MUST use `subagent_type: "general-purpose"`
DO NOT use optimizer, hacker, designer, builder
```
→ Valid options: {general-purpose}
→ Invalid options: {optimizer, hacker, designer, builder}

❌ INCOMPLETE (positive only):
```
MUST use `subagent_type: "general-purpose"`
```
→ Valid options: {general-purpose}
→ Invalid options: Cannot determine from document (are other types forbidden or just not preferred?)

❌ INCOMPLETE (negative only):
```
DO NOT use optimizer, hacker, designer, builder
```
→ Valid options: Cannot determine from document
→ Invalid options: {optimizer, hacker, designer, builder}

### Context-Aware Pattern Detection

**Purpose**: Distinguish truly protected pattern instances from removable explanatory text.

**Problem**: Pattern matching alone produces false positives:
- "CRITICAL" in code block → Actually protected (controls execution)
- "The word CRITICAL signals importance" → Just explanation (can be removed if redundant)

**Protected Contexts** (pattern instance MUST be preserved):

1. **Code blocks**: Text within triple backticks (\`\`\`)
   ```bash
   if [ "$STATUS" = "CRITICAL" ]; then  # ← Protected
   ```

2. **Inline code**: Text within single backticks (\`)
   - Example: \`CRITICAL\` in command syntax ← Protected

3. **Block quotes**: Text within `>` markers
   - Example: `> STOP and verify` ← Protected

4. **Command syntax**: Part of command structure
   - Example: `git commit -m "MANDATORY review"` ← Protected

5. **Headers**: Section titles (lines starting with `#`)
   - Example: `### CRITICAL Requirements` ← Protected

6. **Pattern demonstrations**: Teaching what patterns mean
   - Example: `- Detection: "CRITICAL", "MANDATORY"` ← Protected (defines pattern)
   - Example: Lines containing "Pattern:", "Detection:", "Example:" ← Protected

7. **Emphasis markers**: Styled text
   - Example: `**CRITICAL**:` or `⚠️ CRITICAL` ← Protected

**Removable Contexts** (pattern instance CAN be removed if redundant):

1. **Plain explanatory text**: Regular prose explaining concept
   - Example: "This approach is critical for success" ← Can remove if redundant
   - Example: "Remember to stop before proceeding" ← Can remove if pedagogical

2. **Narrative transitions**: Connecting text between sections
   - Example: "Now that we've covered the critical points..." ← Can remove

3. **Pedagogical rationale**: Explaining WHY something matters
   - Example: "This is critical because it prevents errors" ← Can remove if WHY redundant

**Detection Strategy** (in proactive pattern scanning):

1. Extract line containing pattern instance
2. Check context:
   ```
   IF line contains backticks → Protected
   IF line starts with ">" → Protected
   IF line starts with "#" → Protected
   IF line contains "Detection:", "Pattern:", "Example:" → Protected
   IF line contains "**" or "⚠️" → Protected
   IF none of above → Removable (subject to redundancy check)
   ```
3. Count only protected instances for pattern validation
4. Allow removal of pedagogical/explanatory instances

**Benefits**:
- Reduces false positives in pattern detection
- Allows removal of explanatory text that happens to contain keywords
- Still protects actual functional usage of patterns
- More accurate optimization with fewer violations

**Example Application**:

```markdown
### Protected Instances (count toward validation):

- Detection: "STOP", "CRITICAL" (← teaching pattern syntax)
- `if [ "$STATUS" = "CRITICAL" ]` (← in code block)
- **CRITICAL**: Verify first (← emphasis marker)

### Removable Instances (don't count):

- This approach is critical for success (← plain text explanation)
- Remember to stop before proceeding (← pedagogical narrative)
```

In this example:
- Pattern count = 3 (only protected instances)
- If "critical for success" removed → No violation (wasn't counted)
- If \`CRITICAL\` in code removed → Violation (was counted)

---

## Part 4: Execution Workflow

**Workflow Phases**:
- **Preparation Phase** (Steps 1-3): Setup and initialization
- **Optimization Phase** (Steps 4-7): Identify and remove redundancy
- **Validation Phase** (Steps 8-11): Verify facts preserved, validate protected patterns, restore if needed
- **Completion Phase** (Steps 12-13): Commit and report

### Preparation Phase

#### Step 1: Read Document

**Read** the document: `{{arg}}`

---

#### Step 2: Extract Session ID and Create Backup

**📋 Session ID Usage**:
- Extract session ID from system reminder context
- Use for all temp file naming: `/tmp/*-${SESSION_ID}`

**SESSION ID EXTRACTION** (from conversation context):

1. Search conversation for text `Session ID:` (appears in system reminder messages)
2. Extract the 36-character UUID following `Session ID:` (format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)
3. Assign UUID to SESSION_ID variable below

**Where to look** (check in order until found):
1. System reminder messages at conversation start (first 10 messages)
2. Messages containing "SessionStart hook additional context"
3. SlashCommand tool output for this optimize-doc command

**Fallback if not found**: Generate timestamp-based ID using `date +%Y%m%d-%H%M%S` (e.g., "20251109-183045")

**Example**: `Session ID: a3457736-ae82-46c9-b098-3146a66e2506` → Extract `a3457736-ae82-46c9-b098-3146a66e2506`

```bash
# Assign session ID extracted from system reminder above
# Replace the example UUID below with actual session ID from YOUR conversation
SESSION_ID="a3457736-ae82-46c9-b098-3146a66e2506"  # ← REPLACE with your session ID

BACKUP_FILE="{{arg}}.backup-${SESSION_ID}"
cp "{{arg}}" "$BACKUP_FILE"
echo "Backup created: $BACKUP_FILE"
echo "Session ID: $SESSION_ID"
```

---

#### Step 3: Initialize Iteration Tracking

```bash
ITERATION=1
MAX_ITERATIONS=10
RECOVERY_ATTEMPT=0
MAX_RECOVERY_ATTEMPTS=10

# Capture original line count before optimization begins
ORIGINAL_LINE_COUNT=$(wc -l < "{{arg}}")

# Track candidates that caused violations (smart recovery strategy)
# Format: Comma-separated list of candidate numbers
# Example: "2,4,7" means candidates 2, 4, and 7 caused violations in previous attempts
EXCLUDED_CANDIDATES=""

# OPTION 4 IMPROVEMENT: Candidate Deduplication (Convergence Detection)
# Prevents oscillation where same candidates are cataloged repeatedly across iterations
# Track candidate line ranges across iterations
# If same lines cataloged 2+ iterations → Stop removing (boundary case - neither clearly removable nor protected)
PREVIOUS_CANDIDATE_RANGES=""  # Format: "45-48,102-105,234-240" (comma-separated)
OSCILLATION_THRESHOLD=2  # How many times same candidate can repeat before marking as boundary

# How it works:
# - Step 6 checks each candidate's line range against PREVIOUS_CANDIDATE_RANGES
# - If range found in history → Skip (convergence boundary detected)
# - Step 6 appends current iteration's ranges to PREVIOUS_CANDIDATE_RANGES
# - This guarantees convergence: repeated candidates eventually get skipped
```

---

### Optimization Phase

#### Step 4: Check Optimal State (Idempotency Gate)

```bash
echo "=== STEP 4: OPTIMAL STATE CHECK ==="

# Convergence check: On iteration 2+, check if previous iteration made changes
# Skip this check on iteration 1 (backup just created, always identical)
if [ "$ITERATION" -gt 1 ]; then
  if diff -q "{{arg}}" "$BACKUP_FILE" > /dev/null 2>&1; then
    echo "✅ CONVERGENCE DETECTED"
    echo "File unchanged from previous iteration - optimization complete"
    echo "Document reached optimal state after $((ITERATION - 1)) iteration(s)"

    # Set metrics for convergence case
    LINES_AFTER=$ORIGINAL_LINE_COUNT
    LINES_REMOVED=0
    OPTIMAL_STATE="OPTIMAL"

    echo "Proceeding to Step 14 (Cleanup and Commit)..."
    # Skip to Step 14 - no more changes to make
  fi
fi

# If not converged, invoke agent to evaluate
if [ "$OPTIMAL_STATE" != "OPTIMAL" ]; then
  # Invoke Task tool with these parameters:
  #   subagent_type: "general-purpose"
  #   model: "sonnet"
  #   description: "Evaluate optimal state"
  #   prompt: <full prompt below>

  Prompt: "You are evaluating whether a documentation file is optimally concise.

  **FILE TO EVALUATE**: {{arg}}

  Read the file completely. Evaluate against the 5 optimal state criteria:
  1. Narrative Redundancy: Instruction followed by restatement
  2. Verbose Rationale: WHY explanations >2 sentences
  3. Example Excess: 3+ similar examples per concept (unless showing distinct boundaries)
  4. Duplicate Definitions: Same term defined multiple times
  5. Pedagogical Repetition: Key rules stated 3+ times

  **OPTION 2 IMPROVEMENT: Complete Cataloging Requirement**

  When evaluating, you MUST identify ALL instances of each failing criterion, not just examples.
  Your assessment should provide:
  - Total count of violations for each criterion
  - List of specific line ranges for ALL violations (not just a few examples)

  This ensures cataloging in Step 6 can find ALL removal candidates in a single pass.

  **OUTPUT FORMAT**:

  ## Optimal State Evaluation

  ### Overall Assessment
  OPTIMAL / SUBOPTIMAL

  [If SUBOPTIMAL, list which criteria fail with counts AND complete list of line ranges]"

  # Extract assessment from agent response
  OPTIMAL_STATE="SUBOPTIMAL"  # ← REPLACE with OPTIMAL or SUBOPTIMAL from agent response

  if [ "$OPTIMAL_STATE" = "OPTIMAL" ]; then
    echo "✅ DOCUMENT ALREADY OPTIMAL"
    echo "No redundancy found - document is optimally concise"

    # Set metrics (no changes made this iteration)
    LINES_AFTER=$ORIGINAL_LINE_COUNT
    LINES_REMOVED=0

    echo "Proceeding to Step 14 (Cleanup and Commit)..."
    # Skip to Step 14 - iteration loop terminates
  else
    echo "⚠️ DOCUMENT SUBOPTIMAL: Optimization needed"
    echo "Proceeding to Step 5 (Pre-Cataloging Validation)..."
  fi
fi
```

---

#### Step 5: Pre-Cataloging Validation (Never-Remove List Enforcement)

**Before cataloging any removal candidates, validate document for protected patterns**:

**⚠️ IMPORTANT: Context-Aware Counting**

Count ONLY patterns in protected contexts (see Context-Aware Pattern Detection in Part 3):
- ✅ Count: In code blocks, headers, inline code, quotes, pattern demonstrations, emphasis markers
- ❌ Don't count: In plain explanatory text, narrative transitions, pedagogical rationale

**Why**: Allows removal of explanatory text containing keywords while protecting functional usage.

```bash
echo "=== STEP 5: PRE-CATALOGING VALIDATION ==="

# Count protected patterns in current document with context-aware filtering

# Template variables (always in protected context)
TEMPLATE_VARS=$(grep -oE '\{\{[^}]+\}\}' "{{arg}}" | wc -l)

# Decision criteria (line-start patterns)
DECISION_CRITERIA=$(grep -E '^(Test:|Ask:|Check:|Q:)' "{{arg}}" | wc -l)

# Sequential emphasis - CONTEXT-AWARE IMPLEMENTATION
# Count only in protected contexts: code blocks, headers, emphasis markers, pattern demonstrations
# Exclude plain text explanations
SEQUENTIAL_EMPHASIS=$(grep -n 'STOP\|WAIT\|ONLY THEN\|NOW\|FIRST\|BEFORE\|AFTER\|CRITICAL\|MANDATORY' "{{arg}}" | \
  grep '`\|^[0-9]*:#\|^\*\*\|⚠️\|Detection:\|Pattern:\|Example:' | wc -l)

# Boundary demonstrations (always paired)
BOUNDARY_DEMOS=$(grep -c '❌.*:' "{{arg}}")

# Negative constraints
NEGATIVE_CONSTRAINTS=$(grep -c 'DO NOT\|NEVER\|FORBIDDEN' "{{arg}}")

# Execution context markers (Purpose, Problem, When sections that explain WHEN to apply rules)
EXECUTION_CONTEXT=$(grep -c '^\*\*Purpose\*\*:\|^\*\*Problem\*\*:\|^\*\*When to\|^\*\*Detection\*\*:' "{{arg}}")

echo "Protected pattern inventory (context-aware):"
echo "  Template variables: $TEMPLATE_VARS"
echo "  Decision criteria: $DECISION_CRITERIA"
echo "  Sequential emphasis (protected contexts only): $SEQUENTIAL_EMPHASIS"
echo "  Boundary demonstrations: $BOUNDARY_DEMOS"
echo "  Negative constraints: $NEGATIVE_CONSTRAINTS"
echo "  Execution context markers: $EXECUTION_CONTEXT"

# These counts will be validated after removals in Step 11
# If counts decrease → Protected content was removed (restoration required)
```

---

#### Step 6: Catalog Removals with Fact Extraction

**⚠️ CRITICAL - Fact Extraction Decision Tree** (read this BEFORE cataloging):

```
Before extracting facts, categorize the removal candidate:

Q: Does this content contain FACTUAL information?
   (commands, thresholds, counts, parameters, syntax, patterns)

   YES → FACTUAL CONTENT
         - Extract facts as questions/answers
         - Mark for fact-based validation
         - Example: "Retry 3-5 times" → Q: "How many retries?" A: "3-5"

   NO → NON-FACTUAL CONTENT
        - Mark "Facts: NONE"
        - Skip fact-based validation
        - Remove if passes Decision Tree
        - Example: "This is important because it prevents errors"

Non-factual patterns to recognize:
- Rationale: "This is important because..."
- Narrative: "Let's now move to..."
- Transitions: "Having covered X, we now..."
- Pedagogical emphasis: "Remember that..."
```

**Cataloging Procedure** - For EACH removal candidate:
1. **CHECK EXCLUSION LIST**: If candidate number is in EXCLUDED_CANDIDATES, skip it (do not catalog for removal)
   - This candidate caused violations in previous attempt
   - Example: If EXCLUDED_CANDIDATES="2,4,7", skip candidates 2, 4, and 7
2. **CHECK CONVERGENCE**: If this line range appeared in PREVIOUS_CANDIDATE_RANGES, skip it
   - Example: If candidate is "Lines 403-410" and PREVIOUS_CANDIDATE_RANGES contains "403-410" → SKIP
   - Rationale: Same content cataloged 2+ iterations = boundary case (neither clearly removable nor clearly protected)
   - Mark as "CONVERGENCE BOUNDARY" in notes
3. Identify redundancy type (Categories 1-6, see Part 3)
4. Apply Decision Tree (see Part 3) - skip if should be kept
5. **BEFORE extracting facts**: Categorize as factual or non-factual (use decision tree above)
6. Extract testable facts from content (what information does this convey?)
7. Record: line range, content, category, facts

```bash
echo "=== STEP 6: CATALOGING WITH FACT EXTRACTION ==="
echo "Iteration $ITERATION of $MAX_ITERATIONS"

# Report excluded candidates from previous violations
if [ -n "$EXCLUDED_CANDIDATES" ]; then
  echo "Excluding candidates from previous violations: $EXCLUDED_CANDIDATES"
  echo "These candidates will NOT be cataloged for removal in this attempt"
fi

# Build list of removal candidates with extracted facts
# STRATEGY: Maintain catalog in working memory (mental notes) within this response
# Optional: Use temporary file /tmp/catalog-${SESSION_ID}.txt if persistence needed
# Format:
#
# Candidate 1 (Lines 45-48):
#   Content: "Retry 3-5 times with 2-second delays"
#   Category: Narrative Redundancy
#   Type: FACTUAL
#   Facts:
#     Q: What is the minimum number of retries?
#     A: 3
#     Q: What is the maximum number of retries?
#     A: 5
#     Q: How long to wait between retries?
#     A: 2 seconds
#
# Candidate 2 (Lines 102-105):
#   Content: "Use grep -i for case-insensitive search"
#   Category: Example Redundancy
#   Type: FACTUAL
#   Facts:
#     Q: Which grep flag enables case-insensitive search?
#     A: -i
#
# Candidate 3 (Lines 150-152):
#   Content: "This approach is important because it prevents errors."
#   Category: Rationale (WHY explanation)
#   Type: NON-FACTUAL
#   Facts: NONE (skip validation)
#
# [Continue for all candidates...]

# Count candidates and facts
# Replace <count> placeholders below with actual numeric values:
CANDIDATE_COUNT=<count>  # Replace with number of removal candidates identified
TOTAL_FACTS=<count>      # Replace with total facts extracted (from FACTUAL candidates only)

echo "Identified $CANDIDATE_COUNT removal candidates"
echo "Extracted $TOTAL_FACTS testable facts"

# Update convergence tracking for next iteration
# Extract line ranges from cataloged candidates and append to tracking variable
# Example: If cataloged "Lines 403-410" and "Lines 500-505", add "403-410,500-505"
# Format: Comma-separated list of line ranges
CURRENT_RANGES="<line-ranges>"  # Replace with actual ranges: "45-48,102-105,234-240"
if [ -n "$PREVIOUS_CANDIDATE_RANGES" ]; then
  PREVIOUS_CANDIDATE_RANGES="$PREVIOUS_CANDIDATE_RANGES,$CURRENT_RANGES"
else
  PREVIOUS_CANDIDATE_RANGES="$CURRENT_RANGES"
fi

echo "Convergence tracking: Added $CANDIDATE_COUNT ranges to history"
```

---

#### Step 7: Pre-Removal Protected Pattern Scan (OPTION 1 IMPROVEMENT)

**Purpose**: Validate each candidate for protected patterns BEFORE removal to eliminate remove-restore cycles.

**Rationale**: In previous runs, 5 out of 7 candidates had to be restored after removal due to protected pattern violations. This step prevents those violations by pre-scanning candidates.

```bash
echo "=== STEP 7: PRE-REMOVAL PROTECTED PATTERN SCAN ==="

# For each cataloged candidate, extract content and check for protected patterns
# If ANY protected patterns found, mark candidate as EXCLUDED (don't remove)

# This replaces the remove-then-restore cycle with proactive filtering

# Pattern checks to run on each candidate's extracted text:
# 1. Template variables: \{\{[^}]+\}\}
# 2. Decision criteria: ^(Test:|Ask:|Check:|Q:|A:)
# 3. Sequential emphasis (context-aware): STOP|WAIT|ONLY THEN|NOW|FIRST|BEFORE|AFTER|CRITICAL|MANDATORY in protected contexts
# 4. Boundary demonstrations: ❌.*:
# 5. Negative constraints: DO NOT|NEVER|FORBIDDEN
# 6. Execution context markers: ^\*\*Purpose\*\*:|^\*\*Problem\*\*:|^\*\*When to|^\*\*Detection\*\*:

# Example implementation for each candidate:
#
# CANDIDATE_TEXT=$(sed -n "${START_LINE},${END_LINE}p" "{{arg}}")
#
# # Check for template variables
# TEMPLATE_VARS_IN_CANDIDATE=$(echo "$CANDIDATE_TEXT" | grep -oE '\{\{[^}]+\}\}' | wc -l)
#
# # Check for decision criteria
# DECISION_CRITERIA_IN_CANDIDATE=$(echo "$CANDIDATE_TEXT" | grep -E '^(Test:|Ask:|Check:|Q:|A:)' | wc -l)
#
# # Check for sequential emphasis (context-aware - only in protected contexts)
# SEQUENTIAL_EMPHASIS_IN_CANDIDATE=$(echo "$CANDIDATE_TEXT" | grep -n 'STOP\|WAIT\|ONLY THEN\|NOW\|FIRST\|BEFORE\|AFTER\|CRITICAL\|MANDATORY' | grep '`\|^[0-9]*:#\|^\*\*\|⚠️\|Detection:\|Pattern:\|Example:' | wc -l)
#
# # Check for boundary demonstrations
# BOUNDARY_DEMOS_IN_CANDIDATE=$(echo "$CANDIDATE_TEXT" | grep -c '❌.*:')
#
# # Check for negative constraints
# NEGATIVE_CONSTRAINTS_IN_CANDIDATE=$(echo "$CANDIDATE_TEXT" | grep -c 'DO NOT\|NEVER\|FORBIDDEN')
#
# # Check for execution context markers
# EXECUTION_CONTEXT_IN_CANDIDATE=$(echo "$CANDIDATE_TEXT" | grep -c '^\*\*Purpose\*\*:\|^\*\*Problem\*\*:\|^\*\*When to\|^\*\*Detection\*\*:')
#
# # If ANY protected patterns found, exclude this candidate
# if [ "$TEMPLATE_VARS_IN_CANDIDATE" -gt 0 ] || \
#    [ "$DECISION_CRITERIA_IN_CANDIDATE" -gt 0 ] || \
#    [ "$SEQUENTIAL_EMPHASIS_IN_CANDIDATE" -gt 0 ] || \
#    [ "$BOUNDARY_DEMOS_IN_CANDIDATE" -gt 0 ] || \
#    [ "$NEGATIVE_CONSTRAINTS_IN_CANDIDATE" -gt 0 ] || \
#    [ "$EXECUTION_CONTEXT_IN_CANDIDATE" -gt 0 ]; then
#   echo "⚠️  Candidate $N contains protected patterns - EXCLUDING from removal"
#   echo "  - Template variables: $TEMPLATE_VARS_IN_CANDIDATE"
#   echo "  - Decision criteria: $DECISION_CRITERIA_IN_CANDIDATE"
#   echo "  - Sequential emphasis: $SEQUENTIAL_EMPHASIS_IN_CANDIDATE"
#   echo "  - Boundary demonstrations: $BOUNDARY_DEMOS_IN_CANDIDATE"
#   echo "  - Negative constraints: $NEGATIVE_CONSTRAINTS_IN_CANDIDATE"
#   echo "  - Execution context markers: $EXECUTION_CONTEXT_IN_CANDIDATE"
#
#   # Add to exclusion list
#   if [ -n "$EXCLUDED_CANDIDATES" ]; then
#     EXCLUDED_CANDIDATES="${EXCLUDED_CANDIDATES},${N}"
#   else
#     EXCLUDED_CANDIDATES="${N}"
#   fi
# else
#   echo "✅ Candidate $N passed protected pattern check - safe to remove"
# fi

# After scanning all candidates:
SAFE_CANDIDATE_COUNT=<count>  # Number of candidates that passed pre-scan
EXCLUDED_COUNT=<count>         # Number of candidates excluded due to protected patterns

echo "Pre-scan complete:"
echo "  - Safe to remove: $SAFE_CANDIDATE_COUNT candidates"
echo "  - Excluded (protected patterns): $EXCLUDED_COUNT candidates"
echo "  - Success rate: $((SAFE_CANDIDATE_COUNT * 100 / CANDIDATE_COUNT))%"

# Update CANDIDATE_COUNT to only include safe candidates
CANDIDATE_COUNT=$SAFE_CANDIDATE_COUNT

echo "Proceeding to Step 8 with $CANDIDATE_COUNT safe candidates..."
```

**Expected Impact**: This step should eliminate the 29% success rate problem observed in previous runs. By filtering out protected patterns before removal, we should achieve near 100% success rate.

---

#### Step 8: Execute All Removals

```bash
echo "=== STEP 8: EXECUTING REMOVALS ==="

# Count lines before editing
LINES_BEFORE=$(wc -l < "{{arg}}")

# Read current document state
# Use Read tool: {{arg}}

# Perform all removals from Step 6 catalog
# APPROACH: Reconstruct document in memory, omitting cataloged sections
# 1. Process document line by line from Read output above
# 2. Skip lines identified in removal candidates
# 3. Preserve all other content exactly as-is
# 4. Write optimized version in ONE atomic Write operation (below)
# Use Write tool: {{arg}}

LINES_AFTER=$(wc -l < "{{arg}}")
LINES_REMOVED=$((LINES_BEFORE - LINES_AFTER))

echo "Removed $LINES_REMOVED lines ($CANDIDATE_COUNT candidates)"

# Generate diff for audit
git diff "{{arg}}"
```

---

### Validation Phase

#### Step 9: Fact-Based Validation

```bash
echo "=== STEP 9: FACT-BASED VALIDATION ==="

# Edge case: No facts to validate (all removals were narrative/rationale)
if [ "$TOTAL_FACTS" -eq 0 ]; then
  echo "No facts to validate (all removals are narrative/rationale)"
  echo "Skipping fact validation - proceeding to Step 11 (Protected Pattern Validation)..."
  # Skip to Step 11 - no validator invocation needed
else
  # Invoke validator via Task tool
  # Parameters:
  #   subagent_type: "general-purpose"
  #   model: "sonnet"
  #   description: "Fact-based validation"
  #   prompt: <full prompt below>

  Prompt: "You are validating information preservation in a documentation file.

**FILE TO ANALYZE**: {{arg}}

**YOUR TASK**: Answer specific questions testing information preservation.

Read the file completely. For each question below, answer ONLY from information in the file:

[List all questions from Step 6 here, numbered. Example questions shown below - replace with actual:]

1. What is the minimum number of retries?
2. What is the maximum number of retries?
3. How long should you wait between retries?
4. Which grep flag enables case-insensitive search?
[... continue for all $TOTAL_FACTS questions from your Step 6 catalog ...]

**OUTPUT FORMAT**:

For EACH question, provide:
- Question number
- Answer (from document, or \"Cannot determine from document\")
- Confidence: HIGH / MEDIUM / LOW

**CRITICAL**:
- If information is clear and explicit: Provide answer with confidence HIGH
- If information is present but requires interpretation: Provide answer with confidence MEDIUM
- If information is unclear or requires inference: Provide answer with confidence LOW
- If information is NOT in the document: Answer \"Cannot determine from document\" with confidence LOW
- Do NOT guess or infer - answer only from explicit information

**Example**:
1. What is the minimum number of retries?
   Answer: 3
   Confidence: HIGH

2. What is the maximum number of retries?
   Answer: Cannot determine from document
   Confidence: LOW

3. How long to wait between retries?
   Answer: 2 seconds
   Confidence: LOW"

  # Extract validator response and count unanswerable questions
  echo "Proceeding to Step 10 (Compare Expected vs Actual Answers)..."
fi
```

---

#### Step 10: Compare Expected vs Actual Answers

```bash
echo "=== STEP 10: COMPARING ANSWERS ==="

# IMPLEMENTATION PROCEDURE:
# 1. Extract validator response from Step 9 Task tool output
# 2. For EACH question, parse validator's answer and confidence level
# 3. Compare to expected answer from Step 6 catalog
# 4. Categorize each comparison result:
#
#    MATCH (fact preserved):
#    - Validator answer == expected answer AND confidence HIGH/MEDIUM
#
#    CLARITY ISSUE (fact preserved but needs improvement):
#    - Validator answer == expected answer AND confidence LOW
#    - Section is CORRECT but UNCLEAR - flag for refactoring
#
#    MISMATCH (fact lost):
#    - Validator answer != expected answer
#    - OR answer == "Cannot determine from document"
#    - Content must be restored
#
# 5. For MISMATCH results, record which question failed and from which
#    removal candidate (for restoration in Step 13)

# Count results
# Replace <count> placeholders below with actual numeric values:
MATCH_COUNT=<count>           # Replace with number of correctly preserved facts
CLARITY_COUNT=<count>         # Replace with number needing clarity improvement
MISMATCH_COUNT=<count>        # Replace with number of lost facts requiring restoration

echo "Facts preserved: $MATCH_COUNT / $TOTAL_FACTS"
echo "Facts needing clarity improvement: $CLARITY_COUNT"
echo "Facts lost: $MISMATCH_COUNT"

# List lost facts (for restoration):
# Example:
# Lost facts:
#   - Q2: "What is the maximum number of retries?" (from Candidate 1, Lines 45-48)
#   - Q7: "How long before timeout?" (from Candidate 3, Lines 150-152)

echo "Proceeding to Step 11 (Protected Pattern Validation)..."
```

---

#### Step 11: Protected Pattern Validation

```bash
echo "=== STEP 11: PROTECTED PATTERN VALIDATION ==="

# Re-count protected patterns after removals
TEMPLATE_VARS_AFTER=$(grep -oE '\{\{[^}]+\}\}' "{{arg}}" | wc -l)
DECISION_CRITERIA_AFTER=$(grep -E '^(Test:|Ask:|Check:|Q:)' "{{arg}}" | wc -l)
SEQUENTIAL_EMPHASIS_AFTER=$(grep -oE '(STOP|WAIT|ONLY THEN|NOW|FIRST|BEFORE|AFTER|CRITICAL|MANDATORY)' "{{arg}}" | wc -l)
BOUNDARY_DEMOS_AFTER=$(grep -c '❌.*:' "{{arg}}")
NEGATIVE_CONSTRAINTS_AFTER=$(grep -c 'DO NOT\|NEVER\|FORBIDDEN' "{{arg}}")

# Compare before/after
PATTERN_VIOLATIONS=0

if [ "$TEMPLATE_VARS_AFTER" -lt "$TEMPLATE_VARS" ]; then
  echo "⚠️  Template variables removed ($TEMPLATE_VARS → $TEMPLATE_VARS_AFTER)"
  PATTERN_VIOLATIONS=$((PATTERN_VIOLATIONS + 1))
fi

if [ "$DECISION_CRITERIA_AFTER" -lt "$DECISION_CRITERIA" ]; then
  echo "⚠️  Decision criteria removed ($DECISION_CRITERIA → $DECISION_CRITERIA_AFTER)"
  PATTERN_VIOLATIONS=$((PATTERN_VIOLATIONS + 1))
fi

if [ "$SEQUENTIAL_EMPHASIS_AFTER" -lt "$SEQUENTIAL_EMPHASIS" ]; then
  echo "⚠️  Sequential emphasis removed ($SEQUENTIAL_EMPHASIS → $SEQUENTIAL_EMPHASIS_AFTER)"
  PATTERN_VIOLATIONS=$((PATTERN_VIOLATIONS + 1))
fi

if [ "$BOUNDARY_DEMOS_AFTER" -lt "$BOUNDARY_DEMOS" ]; then
  echo "⚠️  Boundary demonstrations removed ($BOUNDARY_DEMOS → $BOUNDARY_DEMOS_AFTER)"
  PATTERN_VIOLATIONS=$((PATTERN_VIOLATIONS + 1))
fi

if [ "$NEGATIVE_CONSTRAINTS_AFTER" -lt "$NEGATIVE_CONSTRAINTS" ]; then
  echo "⚠️  Negative constraints removed ($NEGATIVE_CONSTRAINTS → $NEGATIVE_CONSTRAINTS_AFTER)"
  PATTERN_VIOLATIONS=$((PATTERN_VIOLATIONS + 1))
fi

# If violations detected, identify specific missing patterns
if [ "$PATTERN_VIOLATIONS" -gt 0 ]; then
  echo ""
  echo "Identifying specific missing patterns..."

  # Invoke pattern comparison agent via Task tool
  # Parameters:
  #   subagent_type: "general-purpose"
  #   model: "sonnet"
  #   description: "Identify missing protected patterns"
  #   prompt: <full prompt below>

  Prompt: "You are identifying specific protected patterns that were removed during optimization.

**TASK**: Compare backup file with current file to identify exact missing pattern instances.

**FILES TO COMPARE**:
- Backup file: $BACKUP_FILE
- Current file: {{arg}}

**PROTECTED PATTERNS TO CHECK**:

1. **Template variables**: Pattern \`{{.*}}\`
2. **Decision criteria**: Lines starting with \"Test:\", \"Ask:\", \"Check:\", \"Q:\", \"A:\"
3. **Sequential emphasis**: Words STOP, WAIT, ONLY THEN, NOW, FIRST, BEFORE, AFTER, CRITICAL, MANDATORY
4. **Boundary demonstrations**: Lines matching \"❌.*:\"
5. **Negative constraints**: Lines containing \"DO NOT\", \"NEVER\", \"FORBIDDEN\"

**INSTRUCTIONS**:

1. Read backup file completely
2. Read current file completely
3. For EACH pattern type with count decrease:
   - Extract ALL instances from backup file (with line numbers)
   - Extract ALL instances from current file (with line numbers)
   - Identify which specific instances are missing
   - For each missing instance, record:
     - Pattern type
     - Missing instance (exact text)
     - Line number in backup
     - Full line content
     - 2-3 lines of context before
     - 2-3 lines of context after
     - **ANALYZE**: Why was this pattern likely removed? (root cause)
     - **SUGGEST**: How to prevent this type of violation in future?

**OUTPUT FORMAT** (JSON):

{
  \"violations\": [
    {
      \"pattern_type\": \"sequential_emphasis\",
      \"missing_instance\": \"CRITICAL\",
      \"line_in_backup\": 308,
      \"line_content\": \"- ✅ KEEP: \\\"**⚠️ CRITICAL - Agent Type Restriction**\\\" (Sequential emphasis #4)\",
      \"context_before\": [
        \"### Never-Remove List (Automatic Preservation)\",
        \"\",
        \"The following content types are NEVER removed:\"
      ],
      \"context_after\": [
        \"- ✅ KEEP: \\\"How Convergence Works\\\" (Correctness guarantees #11)\",
        \"- ✅ KEEP: \\\"MUST use subagent_type: general-purpose\\\" (Agent type restrictions #13)\"
      ],
      \"likely_cause\": \"Line was in pedagogical example section demonstrating protected patterns. Agent categorized entire example section as redundant without scanning individual lines for protected patterns.\",
      \"suggestion\": \"Enhance Decision Tree question 8 (redundancy check) to scan content for protected patterns before removal. If redundant content contains protected patterns, mark as KEEP instead of REMOVE.\"
    }
  ]
}

**CRITICAL**: Output ONLY the JSON structure, no additional text."

  # Extract pattern violations from agent response and count
  # Replace <count> placeholder below with actual value:
  PATTERN_VIOLATION_COUNT=<count>  # Replace with number of specific violations identified

  echo "Identified $PATTERN_VIOLATION_COUNT specific pattern violations"

  # Pattern violations will be handled in Step 13 along with fact mismatches
else
  echo "✅ All protected patterns preserved"
  PATTERN_VIOLATION_COUNT=0
fi

echo "Proceeding to Step 12 (Generalization Validation)..."
```

---

#### Step 12: Generalization Validation

**Purpose**: Test if agent can still generalize category principles after removing preambles/explanations.

```bash
echo "=== STEP 12: GENERALIZATION VALIDATION ==="

# This tests whether the agent can apply category principles to novel examples
# (not listed in document) after optimization removed preambles/explanations

# Invoke generalization validator via Task tool
# Parameters:
#   subagent_type: "general-purpose"
#   model: "sonnet"
#   description: "Test generalization capability"
#   prompt: <full prompt below>

Prompt: "You are testing if an optimized document still enables generalization from examples to principles.

**TASK**: Read the optimized document and answer questions about novel examples NOT in the document.

**DOCUMENT**: {{arg}}

**GENERALIZATION TESTS**:

Test each question below. For each:
- Read document to understand the category principles
- Apply principles to the novel example
- Provide your answer with confidence level (HIGH/MEDIUM/LOW)

**Questions**:

1. **Category 1 Test (Executable Specifications)**:
   Novel example: \`grep -E '^v[0-9]+\$' branches.txt\`
   Question: Should this be protected as \"Executable Specification\"? Why or why not?

2. **Category 2 Test (Decision Boundaries)**:
   Novel example: \"Use merge for feature branches. Use rebase for cleanup commits.\"
   Question: Should this be protected as \"Decision Boundary\"? Why or why not?

3. **Category 3 Test (Workflow Sequences)**:
   Novel example: \"Run \`git stash\` BEFORE switching branches\"
   Question: Should this be protected as \"Workflow Sequence\"? Why or why not?

4. **Category 4 Test (Success Verification)**:
   Novel example: \"Command should exit with code 0 on success\"
   Question: Should this be protected as \"Success Verification\"? Why or why not?

5. **Category 5 Test (Execution Context)**:
   Novel example: \"When to use this approach: If file size exceeds 100MB\"
   Question: Should this be protected as \"Execution Context\"? Why or why not?

**CRITICAL**:
- Base answers on PRINCIPLES extracted from document, not memorized knowledge
- If you can derive the principle from examples alone → Answer with confidence HIGH
- If you need the category preamble to understand principle → Answer with confidence LOW
- Answer format:

{
  \"test_1\": {
    \"answer\": \"Yes, this is executable specification because...\",
    \"confidence\": \"HIGH\",
    \"reasoning\": \"Document shows commands/syntax are protected. This is exact grep syntax.\"
  },
  \"test_2\": {
    \"answer\": \"Yes, this is decision boundary because...\",
    \"confidence\": \"LOW\",
    \"reasoning\": \"Examples show boundaries but without preamble 'shows where rules start/stop', unclear if this qualifies.\"
  },
  ...
}

**Output ONLY JSON, no additional text.**"

# Extract validator response and check confidence levels
# If ANY test has confidence LOW → Generalization capability degraded
# Count how many tests show LOW confidence:
GENERALIZATION_VIOLATIONS=<count>  # Replace with number of LOW confidence answers

if [ "$GENERALIZATION_VIOLATIONS" -gt 0 ]; then
  echo "⚠️  Generalization capability degraded: $GENERALIZATION_VIOLATIONS tests show LOW confidence"
  echo "Agent cannot reliably apply category principles to novel examples"
  echo "This indicates category preambles or key explanations were removed"
else
  echo "✅ Generalization capability preserved: Agent can apply principles to novel cases"
fi

echo "Proceeding to Step 13 (Handle Lost Facts and Pattern Violations)..."
```

---

#### Step 13: Handle Lost Facts and Pattern Violations

```bash
echo "=== STEP 13: HANDLING LOST FACTS AND PATTERN VIOLATIONS ==="

# Calculate total violations needing restoration
TOTAL_VIOLATIONS=$((MISMATCH_COUNT + PATTERN_VIOLATION_COUNT + GENERALIZATION_VIOLATIONS))

if [ "$TOTAL_VIOLATIONS" -eq 0 ]; then
  echo "✅ ALL FACTS PRESERVED, ALL PATTERNS PRESERVED, GENERALIZATION CAPABILITY MAINTAINED"

  # Check if we should continue optimizing or stop
  if [ "$ITERATION" -lt "$MAX_ITERATIONS" ]; then
    ITERATION=$((ITERATION + 1))
    RECOVERY_ATTEMPT=0  # Reset recovery counter for next optimization pass
    echo "Iteration $ITERATION: Looking for more optimizations..."
    echo "Returning to Step 4 (Check Optimal State)..."
    # Loop back to Step 4 to find more redundancies
  else
    echo "✅ MAX ITERATIONS REACHED: Optimization complete"
    echo "Proceeding to Step 14 (Cleanup and Commit)..."
    # Terminate iteration process - skip to Step 14
  fi
fi

if [ "$TOTAL_VIOLATIONS" -gt 0 ]; then
  if [ "$RECOVERY_ATTEMPT" -lt "$MAX_RECOVERY_ATTEMPTS" ]; then
    RECOVERY_ATTEMPT=$((RECOVERY_ATTEMPT + 1))
    echo "⚠️ $MISMATCH_COUNT facts lost, $PATTERN_VIOLATION_COUNT patterns lost - performing selective restoration (recovery attempt $RECOVERY_ATTEMPT/$MAX_RECOVERY_ATTEMPTS)..."

    # SELECTIVE RESTORATION STRATEGY:
    #
    # Instead of restoring entire file from backup, surgically restore ONLY the
    # specific removal candidates that caused violations. Keep all successful removals.
    #
    # Benefits:
    # - Preserves optimization work from successful removals
    # - Only reverts problematic removals
    # - Faster convergence to optimal state
    # - Leverages surgical restoration capability built in Step 11

    # STEP A: IDENTIFY VIOLATED CANDIDATES
    #
    # Build list of candidates that need restoration:
    # - From fact violations (Step 9): Validator agent identifies "Candidate N"
    # - From pattern violations (Step 11): Map line numbers back to candidates using catalog
    #
    # Example:
    # VIOLATED_CANDIDATES=(1 3 5)  # Candidates 1, 3, and 5 contained violations
    # KEEP_REMOVED=(2 4 6 7)       # Candidates 2, 4, 6, 7 were successfully removed

    echo "Analyzing violations to identify affected candidates..."

    # Parse validator agent response to extract candidate numbers
    # Pattern: Look for "Candidate N" in mismatch responses
    # Example: "Cannot find in document: What is retry count? (from Candidate 1)"

    # Parse pattern comparison agent response to map line numbers to candidates
    # Use the catalog from Step 6 to determine which candidate covered which line range
    # Example: If violation at line 308, check catalog: "Candidate 2 (Lines 305-310)"
    #
    # Report diagnostic information from pattern violations:
    # - For each violation, display: likely_cause and suggestion
    # - This helps understand WHY violations occurred
    # - Suggestions can be used to improve Decision Tree or Never-Remove List
    #
    # Example output:
    # "Violation: CRITICAL at line 308 (Candidate 2)"
    # "  Likely cause: Pedagogical example section not scanned for protected patterns"
    # "  Suggestion: Enhance Decision Tree question 8 with pattern scanning"

    # STEP B: RESTORE VIOLATED CANDIDATES ONLY
    #
    # For each violated candidate:
    # 1. EXTRACT: Read backup file to get original content for that candidate
    #    - Use line range from catalog: "Candidate N (Lines X-Y)"
    # 2. LOCATE INSERTION POINT using contextual search:
    #    a) Identify 2-3 lines BEFORE the removed section (from backup)
    #    b) Identify 2-3 lines AFTER the removed section (from backup)
    #    c) Search current file using Grep to find context markers
    #    d) Insertion point = between these context markers
    # 3. RESTORE: Use Edit tool to insert content at correct location
    #    - Match existing indentation/formatting
    #    - Preserve surrounding whitespace
    # 4. VERIFY: Confirm restoration succeeded
    #
    # IMPORTANT: Do NOT restore candidates that weren't violated
    # - Those removals were successful and should remain removed
    # - This preserves optimization progress
    #
    # Example restoration sequence:
    # - Candidate 1 contained lost fact → RESTORE
    # - Candidate 2 successfully removed → KEEP REMOVED
    # - Candidate 3 contained protected pattern → RESTORE
    # - Candidate 4 successfully removed → KEEP REMOVED

    # RESTORATION PROCEDURE for lost facts:
    #
    # 1. IDENTIFY: Which removal candidate from Step 6 contained this fact
    #    - Parse validator agent response for "Candidate N" references
    #    - Add N to violated candidates list
    # 2. EXTRACT: Read backup file to get original content for that candidate
    #    - Use line range from catalog: "Candidate N (Lines X-Y)"
    # 3. LOCATE INSERTION POINT using contextual search strategy:
    #    a) Identify 2-3 lines BEFORE the removed section (from backup)
    #    b) Identify 2-3 lines AFTER the removed section (from backup)
    #    c) Search current file using Grep to find the before/after context
    #    d) Insertion point = between these context markers
    # 4. RESTORE: Use Edit tool to insert content at correct location
    #    - Match existing indentation/formatting
    #    - Preserve surrounding whitespace
    #
    # HANDLING AMBIGUOUS CONTEXT:
    # - If context appears multiple times, use additional surrounding lines
    # - If context was also removed, use nearest available markers
    # - Verify restoration with targeted grep after Edit

    # Example restoration:
    # Lost fact: "What is the maximum number of retries?" (from Candidate 1)
    # 1. Extract from backup lines 45-48: "Retry 3-5 times with 2-second delays"
    # 2. Context before (lines 43-44): "## Error Handling\n\nWhen errors occur:"
    # 3. Context after (lines 49-50): "After retries exhausted, report failure."
    # 4. Grep current file for "When errors occur:" to find insertion point
    # 5. Edit to insert between context markers
    # 6. Candidate 1 now restored, but Candidates 2, 4, 6, 7 remain removed

    # RESTORATION PROCEDURE for pattern violations:
    #
    # For each pattern violation from Step 11 agent response:
    # 1. MAP TO CANDIDATE: Use catalog to find which candidate covered this line
    #    - Pattern violation at line 308
    #    - Check catalog: "Candidate 2 (Lines 305-310)" → Violation in Candidate 2
    #    - Add Candidate 2 to violated candidates list
    # 2. EXTRACT: Get line_content, context_before, context_after from violation JSON
    # 3. LOCATE INSERTION POINT:
    #    a) Search current file for context_before lines using Grep
    #    b) Search current file for context_after lines using Grep
    #    c) Insertion point = between these context markers
    # 4. RESTORE: Use Edit tool to insert line_content at correct location
    #    - Match existing indentation/formatting
    #    - Preserve surrounding whitespace
    #
    # HANDLING AMBIGUOUS CONTEXT:
    # - If context appears multiple times, use line numbers as hint
    # - If context was also removed, use adjacent available markers
    # - Verify restoration by re-running Step 11 pattern counts
    #
    # Example restoration:
    # Pattern violation: sequential_emphasis "CRITICAL" at line 308 (in Candidate 2)
    # Line content: "- ✅ KEEP: \"**⚠️ CRITICAL - Agent Type Restriction**\""
    # Context before: "The following content types are NEVER removed:"
    # Context after: "- ✅ KEEP: \"How Convergence Works\""
    # 1. Grep for "The following content types" to find insertion area
    # 2. Grep for "How Convergence Works" to confirm location
    # 3. Edit to insert line between context markers
    # 4. Candidate 2 now restored, but Candidates 1, 3, 4, 5 remain removed

    # STEP C: TRACK EXCLUDED CANDIDATES FOR NEXT ITERATION
    #
    # After restoration, mark violated candidates as "problematic"
    # In next recovery attempt, these candidates should be EXCLUDED from removal
    # This implements smart recovery strategy (task #2 from pending list)
    #
    # Example:
    # - First attempt: Remove candidates 1-5
    # - Violations: Candidates 2, 4 caused violations
    # - Second attempt: Remove only candidates 1, 3, 5 (exclude 2, 4)
    # - If still violations: Remove only candidates 1, 3 (progressively narrow)
    #
    # Implementation:
    # 1. Build list of violated candidate numbers from Steps A and B
    #    Example: VIOLATED_CANDIDATES="2 4"
    # 2. Merge with existing EXCLUDED_CANDIDATES (if any)
    #    Example: If EXCLUDED_CANDIDATES="7" and new violations "2 4"
    #             → EXCLUDED_CANDIDATES="2 4 7"
    # 3. These candidates will be skipped in next iteration's Step 6 cataloging
    #
    # Update EXCLUDED_CANDIDATES variable:
    # EXCLUDED_CANDIDATES="$EXCLUDED_CANDIDATES 2 4"  # Add violated candidates
    # EXCLUDED_CANDIDATES=$(echo "$EXCLUDED_CANDIDATES" | tr ' ' '\n' | sort -u | tr '\n' ' ')  # Deduplicate

    echo "Selective restoration complete - only violated candidates restored"
    echo "Updated exclusion list for next attempt: $EXCLUDED_CANDIDATES"
    echo "Successful removals preserved"
    echo "Re-validating after restoration..."
    echo "Returning to Step 4 (Check Optimal State)..."
    # Loop back to Step 4 with recovery attempt tracked
    # Step 4 will re-check if document is optimal, then proceed to Step 5 if not

  else
    echo "❌ RECOVERY LIMIT REACHED ($MAX_RECOVERY_ATTEMPTS attempts)"
    echo "Facts still lost: $MISMATCH_COUNT"
    echo "Patterns still lost: $PATTERN_VIOLATION_COUNT"
    echo "Restoring from backup..."
    cp "$BACKUP_FILE" "{{arg}}"
    echo "Optimization failed - file restored to original"
    exit 1
  fi
fi
```

**How Convergence Works**:

The command uses two iteration mechanisms:

1. **Optimization iterations** (max 10): After each successful removal pass, loops back to Step 4 to find more redundancies. Continues until document is OPTIMAL or max iterations reached.

2. **Recovery attempts** (max 10): When facts are lost, attempts to restore content and retry. If recovery fails after 10 attempts, restores entire file from backup.

**Termination guarantees**:
- Normal completion: Step 4 returns OPTIMAL (no more redundancies found)
- Safety limits: 10 optimization iterations or 10 recovery attempts
- All paths preserve information or restore from backup


---

### Completion Phase

#### Step 14: Cleanup and Commit

```bash
echo "=== STEP 14: CLEANUP AND COMMIT ==="

# Check if file actually changed
if diff -q "{{arg}}" "$BACKUP_FILE" > /dev/null 2>&1; then
  echo "✅ No changes needed - document already optimal"
  rm "$BACKUP_FILE"
  exit 0
fi

# Calculate metrics for commit message
TOTAL_REDUCTION=$((ORIGINAL_LINE_COUNT - LINES_AFTER))

# Build commit message
COMMIT_MSG="Optimize {{arg}} (fact-based validation)

Removed redundancy while preserving all information.

Iterations: $ITERATION
Total line change: $ORIGINAL_LINE_COUNT → $LINES_AFTER lines ($TOTAL_REDUCTION lines removed)
Validation: PASS
- All facts preserved
- 0 information loss

🤖 Generated with Claude Code
Co-Authored-By: Claude <noreply@anthropic.com>"

# Commit changes
git add "{{arg}}"
git commit -m "$COMMIT_MSG"

# Remove backup
rm "$BACKUP_FILE"

echo "✅ Changes committed"
```

---

#### Step 15: Report

**⚠️ IMPORTANT**: Replace ALL placeholders `[N]`, `[count]`, `[X]` with actual numeric values from your execution. Do NOT output literal placeholders.

```
## Document Optimization Summary

**Optimization**: Fact-based validation
- Iterations: [N]               ← Replace with actual iteration count
- Candidates cataloged: [count] ← Replace with CANDIDATE_COUNT
- Facts extracted: [count]      ← Replace with TOTAL_FACTS
- Facts preserved: [count]/[count] ← Replace with MATCH_COUNT/TOTAL_FACTS

**Metrics**:
- Lines removed: [N]            ← Replace with LINES_REMOVED
- Net reduction: [N] lines ([X]%) ← Calculate percentage

**Validation**:
- Information loss: 0
- All facts preserved: ✓
```
