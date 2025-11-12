---
description: >
  Shrink documentation using semantic role analysis: remove redundancy while preserving all information
---

# Shrink Documentation Command

**Task**: Optimize the documentation file: `{{arg}}`

**Idempotent Design**: Can run multiple times - each pass makes further improvements until document is optimally concise.

**⚠️ CRITICAL - Agent Type Restriction**:
- **MUST use** `subagent_type: "general-purpose"` for ALL Task tool invocations
- **DO NOT use** any other agent type (optimizer, hacker, designer, builder, etc.)

---

## Part 1: Semantic Framework

### 🚨 DOCUMENT TYPE VALIDATION

#### Claude-Facing Documents (ALLOWED)
- `.claude/` configuration files (agents, commands, hooks, settings)
- `CLAUDE.md` and project instructions for Claude
- `docs/project/` development protocol documentation
- `docs/code-style/*-claude.md` style detection patterns

#### Human-Facing Documents (FORBIDDEN)
- `README.md`, `changelog.md`, `CHANGELOG.md`
- `docs/studies/`, `docs/decisions/`, `docs/performance/`
- `docs/optional-modules/` (potentially user-facing)
- `todo.md`, `docs/code-style/*-human.md`

**Validation**: BEFORE optimizing, check file path. If forbidden pattern, REFUSE:
```
This command only optimizes Claude-facing documentation.
The file `{{arg}}` appears to be human-facing documentation.
```

---

### Core Principle: Semantic Role Classification

**Replace pattern matching with semantic analysis**. Content protection is determined by its functional role in agent execution, not by keywords or formatting.

---

### Semantic Role Taxonomy

#### 1. DIRECTIVE Content (ALWAYS PRESERVE)

**Definition**: Content that directly controls agent actions or decisions.

**Recognition Tests**:
- **Removal prevents execution**: Without this, agent cannot complete the task
- **Removal creates ambiguity**: Agent has multiple valid interpretations of what to do
- **Specifies executable details**: Commands, parameters, thresholds, constraints, conditions
- **Defines terms used in directives**: Without this definition, agent cannot understand directives that use this term

**Examples**:
```markdown
✅ DIRECTIVE (preserve):
- "Run `grep -i 'pattern' file.txt`" (command with parameters)
- "If X fails → do Y" (conditional logic)
- "MUST use subagent_type: 'general-purpose'" (constraint)
- "Retry 3-5 times" (threshold)
- "Step 1: X, Step 2: Y, Step 3: Z" (sequence)
- "STOP and wait for approval" (blocking checkpoint)
- "Valid options: {A, B, C}" (enumerated choices)
- "**DIRECTIVE**: Content that controls agent actions" (term definition - defines what to identify)
- "**Validation Tests**: Check X, Y, Z" (test methodology - specifies what to check)
```

**Sub-types**:
- **Executable Specifications**: Commands, syntax, file paths, numeric thresholds
- **Control Flow**: Conditionals, sequences, blocking checkpoints (STOP, WAIT, ONLY THEN)
- **Decision Boundaries**: Valid/invalid options, boundary conditions (❌ vs ✅)
- **Constraints**: MUST/NEVER rules that define valid behavior
- **Term Definitions**: Definitions of concepts/roles/terms used in directives

---

#### 2. CONTEXTUAL Content (PRESERVE IF NEEDED FOR CLARITY)

**Definition**: Content that clarifies WHEN, HOW, or WHY to apply directives. Not executable itself, but resolves ambiguity in directives.

**Recognition Tests**:
- **Removes ambiguity**: Without this, directive has multiple interpretations
- **Defines application scope**: Clarifies when/where directive applies (during execution)
- **Distinguishes options**: Explains how to choose between alternatives (within workflow)
- **Clarifies methodology/approach**: Explains the fundamental approach, affecting how all directives are applied
- **Prevents misapplication**: Resolves potential confusion about how to classify or apply content
- **Describes usage patterns**: Clarifies when/how to execute workflow steps (NOT pre-execution tool choice)

**Examples**:
```markdown
✅ CONTEXTUAL (preserve if needed):
- "**Purpose**: Distinguish X from Y" (clarifies decision goal)
- "**When to use**: If file size exceeds 100MB" (application scope)
- "**Problem**: Pattern matching alone produces false positives" (explains why alternative needed)
- "**Detection**: Look for lines starting with '#'" (recognition strategy)
- "Both needed: Positive alone means agent might guess..." (explains pairing requirement)
- "Replace pattern matching with semantic analysis" (methodology clarification)
- "Can run multiple times - each pass makes improvements" (usage pattern)
- "**Note**: Some 'WHY' content is CONTEXTUAL if it explains WHEN" (prevents misapplication)

❌ NOT CONTEXTUAL (explanatory - see #4):
- "This is important because it prevents errors" (pure rationale, no execution impact)
- "Benefits: Reduces false positives" (outcome, not application scope)
- "This approach works well in practice" (outcome only, doesn't affect choices)
- "When to use /shrink-doc vs /optimize-doc" (pre-execution tool choice, not workflow guidance)
```

**Clarity Test**: Remove the contextual content. Ask:
1. Can agent still execute the directive correctly?
2. Is there only ONE valid interpretation remaining?
3. Does agent understand the methodology/approach clearly?
4. Can agent avoid misapplication without this clarification?

- If NO to any → PRESERVE (contextual)
- If YES to all → REMOVE (explanatory)

**Sub-types**:
- **Application Scope**: When/where directives apply
- **Decision Criteria**: How to choose between options
- **Recognition Patterns**: How to identify relevant content
- **Problem Statements**: Why alternative approach needed (clarifies motivation)
- **Methodology Clarifications**: Fundamental approach affecting all directives
- **Usage Patterns**: When/how to use the tool or procedure
- **Clarifying Notes**: Content preventing misclassification or misapplication

**Special Case - Numeric Thresholds and Boundaries**:

Content containing numeric boundaries or thresholds is ALMOST ALWAYS CONTEXTUAL, not EXPLANATORY.

**Recognition Patterns**:
- Range boundaries: `6-30 seconds`, `3-5 retries`, `100-200 MB`
- Comparison operators: `>30 seconds`, `<10%`, `≥ 3 attempts`
- Explicit thresholds: `ACCEPTABLE if X < N`, `VIOLATION if Y > M`
- Timing specifications: `<30s = ACCEPTABLE`, `>30s = sequential work`

**Classification Rule**:
- If content specifies numeric bounds → Test if removal makes directive ambiguous
- If agent must guess the number without this content → CONTEXTUAL
- Pure numeric examples without decision impact → DEMONSTRATIVE

**Example**:
```markdown
✅ CONTEXTUAL: "6-30 second gaps = ACCEPTABLE, >30 seconds = sequential work"
   - Specifies boundary for parallel agent invocation timing
   - Without this, agent doesn't know if 25s is acceptable

❌ NOT CONTEXTUAL: "Build typically takes 120-180 seconds"
   - Informational timing, doesn't affect decision boundaries
   - DEMONSTRATIVE (expected duration) or EXPLANATORY (FYI)
```

---

#### 3. DEMONSTRATIVE Content (PRESERVE UNIQUE CASES ONLY)

**Definition**: Content that shows directive application through examples, demonstrations, or sample outputs.

**Recognition Tests**:
- **Shows distinct case**: Demonstrates boundary, edge case, or parameter variation not shown elsewhere
- **Provides success criteria**: Shows what correct execution looks like
- **Resolves vague terms**: Makes abstract directive concrete

**Uniqueness Test**: For multiple demonstrations of same concept:
- **Keep max 2 examples** unless they show distinct boundaries/edge cases
- **Boundary pairs always preserved**: "❌ WRONG" + "✅ CORRECT" pairs show decision boundary
- **Trivial variations removed**: Examples differing only in variable names/trivial details

**Examples**:
```markdown
✅ DEMONSTRATIVE (preserve if unique):
- First example showing command syntax with parameters
- Example showing edge case not covered by other examples
- "❌ WRONG: X" + "✅ CORRECT: Y" (boundary pair - always preserve both)
- "Expected output: [sample]" (success criteria)

❌ DEMONSTRATIVE (remove if redundant):
- Third example of same pattern with different variable names
- Example showing case already demonstrated
- Sample output when success criteria already clear
```

---

#### 4. EXPLANATORY Content (REMOVE IF REDUNDANT)

**Definition**: Content that explains WHY directives exist, benefits, importance, or rationale. Provides pedagogical context but doesn't affect execution correctness.

**Recognition Tests**:
- **Pure rationale**: Explains benefits/importance without execution impact
- **Pedagogical emphasis**: Repeats key concepts for learning
- **Narrative transitions**: Connects sections for readability
- **Outcome explanations**: Describes what happens, not what to do

**Examples**:
```markdown
❌ EXPLANATORY (remove if redundant):
- "This is important because it prevents errors"
- "Benefits: Reduces false positives, improves accuracy"
- "This matters because..." (when rule is already clear)
- "Having covered X, we now move to Y" (narrative transition)
- "Remember that..." (pedagogical repetition)
- "This approach works well in practice" (outcome, not directive)
```

**Note**: Some "WHY" content is actually CONTEXTUAL if it explains WHEN to apply a rule. Use the Clarity Test to distinguish.

---

### Semantic Classification Procedure

For each content block, apply tests in order:

```
0. Special Pattern Check (applies to all classifications):
   - Paired Constraints: "MUST X" + "NEVER Y" about same topic (±5 lines) → Preserve BOTH
   - Workflow Sequences: Numbered steps with dependencies → Preserve ALL steps, no external references
   - Context-Aware Keywords: "CRITICAL", "STOP" in commands/headers → DIRECTIVE; in narrative → May be EXPLANATORY

1. DIRECTIVE Test:
   Q: Does removal prevent execution or create ambiguity?
   Q: Does this define a term used in directives?
   Q: Does this specify WHAT to check/identify?
   YES to any → Classify as DIRECTIVE, PRESERVE
   NO to all → Continue to 2

2. CONTEXTUAL Test:
   Q: Without this, does a directive become ambiguous?
   Q: Does this clarify the methodology/approach?
   Q: Does this prevent misapplication?
   Q: Does this describe when/how to use the tool?
   Q: Does this explain WHY an alternative exists (clarifying WHEN to use it)?
   YES to any → Classify as CONTEXTUAL, apply Clarity Test
   NO to all → Continue to 3

3. DEMONSTRATIVE Test:
   Q: Does this show directive application?
   YES → Apply Uniqueness Test (preserve if unique, remove if redundant)
   NO → Continue to 4

4. EXPLANATORY (default):
   Classify as EXPLANATORY
   Remove if redundant (doesn't add execution information)
```

---

### 🚨 CRITICAL - Common Misclassification Errors

**When analyzing instructional documents, watch for these semantic misclassifications:**

**Error 1: Confusing Term Definitions with Explanatory Content**
```
❌ WRONG: "**Definition**: X is content that does Y → This restates the Recognition Tests → EXPLANATORY"
✅ CORRECT: "Definition specifies WHAT the term means → Without it, directives using this term are unclear → DIRECTIVE"
```
**Test**: Do other directives use this term? If yes, the definition is DIRECTIVE (defines terms used in directives).

**Error 2: Confusing Methodology Clarifications with Pedagogical Content**
```
❌ WRONG: "Core Principle: Use semantic analysis → This is pedagogical introduction → EXPLANATORY"
✅ CORRECT: "Core Principle clarifies the fundamental approach → Affects HOW all directives are applied → CONTEXTUAL"
```
**Test**: Does this affect HOW you apply the directives? If yes, it's CONTEXTUAL (methodology clarification).

**Error 3: Confusing Usage Patterns with Benefits**
```
❌ WRONG: "Can run multiple times → This explains a benefit → EXPLANATORY"
✅ CORRECT: "Can run multiple times → Clarifies WHEN to rerun the tool → CONTEXTUAL"
```
**Test**: Does this clarify WHEN or HOW to use the tool? If yes, it's CONTEXTUAL (usage pattern).

**Error 4: Confusing Clarifying Notes with Redundancy**
```
❌ WRONG: "**Note**: Some WHY content is CONTEXTUAL if it explains WHEN → Redundant with taxonomy → EXPLANATORY"
✅ CORRECT: "Note prevents misapplication of the classification rules → CONTEXTUAL"
```
**Test**: Does this prevent misclassification or misapplication? If yes, it's CONTEXTUAL (clarifying note).

**Error 5: Confusing WHY-for-Clarity with WHY-for-Pedagogy**
```
❌ WRONG: "Problem: Pattern matching produces false positives → This explains WHY → EXPLANATORY"
✅ CORRECT: "Problem statement clarifies WHY an alternative exists → Affects WHEN to use alternative → CONTEXTUAL"

❌ WRONG: "This is important because it prevents errors → Explains WHY → EXPLANATORY"
✅ CORRECT: "Pure rationale with no execution impact → EXPLANATORY"
```
**Test**: Does the "WHY" clarify WHEN to apply something or just emphasize importance? Clarifies WHEN → CONTEXTUAL. Emphasizes importance → EXPLANATORY.

**Error 6: Confusing Tool Selection (BEFORE) with Workflow Guidance (AFTER)**
```
❌ WRONG: "When to Use Each Command" section → Contains "WHEN" keyword → CONTEXTUAL"
✅ CORRECT: Helps choose which tool to run (BEFORE invocation) → EXPLANATORY"

❌ WRONG: "Comparison with /optimize-doc" → Helps choose tools → CONTEXTUAL
✅ CORRECT: Tool selection advice, not workflow instructions → EXPLANATORY"
```
**Timeline Test**:
- Content used BEFORE running command (tool selection) → EXPLANATORY
- Content used WHILE running command (workflow guidance) → CONTEXTUAL

**Examples**:
- ❌ EXPLANATORY: "Use /shrink-doc for Claude-facing docs" (decide which command to invoke)
- ✅ CONTEXTUAL: "Skip validation if total_facts = 0" (branching within /shrink-doc workflow)

**Error 7: Confusing Complementary DIRECTIVE Sections with Redundancy**
```
❌ WRONG: "**Validation Tests** section → Redundant with validation procedure → EXPLANATORY"
✅ CORRECT: "Test definitions + Workflow = Complementary DIRECTIVE content, not redundant"
```
**Complementarity Test**:
1. Does Section A define WHAT to check (test/criteria definitions)?
2. Does Section B define WHEN/HOW to apply (workflow/procedure)?
3. Both needed? → Both are DIRECTIVE (complementary, not redundant)

**Example**: Document with "**Recognition Tests**: Check X, Y, Z" + "Apply tests: 1. X → 2. Y → 3. Z"
- First defines individual tests (methodology)
- Second defines test sequence (workflow)
- Both are DIRECTIVE - removing either breaks execution

**Error 8: Using Surface Features Instead of Semantic Tests**

**🚨 CRITICAL - HIGHEST PRIORITY MISTAKE TO AVOID**

This mistake has occurred MULTIPLE times even after being documented. Error 8 was added to prevent
misclassification, but agents still made the exact same mistakes in subsequent sessions. DO NOT let this
happen again.

**The Mistake Pattern**:
```
❌ WRONG: Section titled "Why X Matters" → Contains "Why" → EXPLANATORY
✅ CORRECT: Apply semantic test: Does this explain enforcement mechanisms? → CONTEXTUAL

❌ WRONG: "Clarification on Timing" → Sounds pedagogical → EXPLANATORY
✅ CORRECT: Apply semantic test: Does this specify timing boundaries (6-30s acceptable)? → CONTEXTUAL

❌ WRONG: "Required Justification Process" → Sounds like explanation → EXPLANATORY
✅ CORRECT: Apply semantic test: Is this a 5-step mandatory procedure? → DIRECTIVE

❌ WRONG: "Correct Pattern" examples → Third example of same concept → Remove
✅ CORRECT: Apply uniqueness test: Do examples show distinct boundaries (WRONG vs CORRECT)? → Preserve pairs
```

**Root Cause**: Agent classified based on section titles, tone, or pedagogical-sounding language instead of
applying the semantic role tests.

**Why This Keeps Happening**: Agents read the guidance but don't internalize it. They see "Why" in a section
title and jump to "EXPLANATORY" without actually reading the content and applying tests.

**Mandatory Prevention Protocol**:

1. **DO NOT classify based on titles/tone** - Titles are NOT semantic tests
2. **ALWAYS answer ALL semantic test questions** - Document your answers, don't skip
3. **Red flag keywords trigger mandatory double-check**:
   - "Why", "Clarification", "Purpose", "Problem" → High misclassification risk
   - "Required", "Mandatory", "Critical" → Often DIRECTIVE, not EXPLANATORY
   - "State Machine", "Hook", "Enforcement" → Often CONTEXTUAL, not EXPLANATORY
4. **When in doubt, classify as CONTEXTUAL** - Better to preserve than wrongly remove
5. **Check Error 8 examples BEFORE classifying** - Do your candidates match these patterns?

**The Semantic Test Protocol** (apply to EVERY candidate):

Step 1: Read the CONTENT, not just the title
Step 2: Answer DIRECTIVE questions:
  - Does removal prevent execution or create ambiguity? (If YES → DIRECTIVE)
  - Does this define terms used in directives? (If YES → DIRECTIVE)
Step 3: Answer CONTEXTUAL questions:
  - Without this, does a directive become ambiguous? (If YES → CONTEXTUAL)
  - Does this clarify methodology/approach affecting execution? (If YES → CONTEXTUAL)
  - Does this prevent misapplication? (If YES → CONTEXTUAL)
  - Does this specify boundaries/thresholds for execution? (If YES → CONTEXTUAL)
Step 4: If all NO → Check DEMONSTRATIVE, then default to EXPLANATORY

**Actual Examples from Failed Session** (2025-11-12 18:27):

The agent classified these as EXPLANATORY despite Error 8 being present:

1. **"Why State Machine Matters"** - Removed as EXPLANATORY
   - ❌ Agent thought: Title says "Why" → EXPLANATORY
   - ✅ Semantic test: Explains enforcement mechanisms (hooks validate based on state) → **CONTEXTUAL**
   - Impact: Lost critical context about how protocol is enforced

2. **"Clarification on Timing"** - Removed as EXPLANATORY
   - ❌ Agent thought: Sounds pedagogical → EXPLANATORY
   - ✅ Semantic test: Specifies timing boundaries (6-30s = ACCEPTABLE, >30s = VIOLATION) → **CONTEXTUAL**
   - Impact: Lost execution-critical threshold specifications

3. **"Required Justification Process"** - Removed as EXPLANATORY
   - ❌ Agent thought: Sounds like explanation → EXPLANATORY
   - ✅ Semantic test: Is this a 5-step mandatory procedure? → **DIRECTIVE**
   - Impact: Lost required workflow steps

4. **Infrastructure vs Feature examples** - Removed as redundant
   - ❌ Agent thought: Third example of same concept → Remove
   - ✅ Uniqueness test: Shows critical boundary (when main agent can create .java files) → **DEMONSTRATIVE (unique)**
   - Impact: Lost critical decision boundary

**Key Principle**: Classification is determined by **functional role in agent execution**, NOT by surface
features like section titles or tone.

**If you find yourself thinking "This sounds like EXPLANATORY"**, STOP. That's surface-feature thinking. Ask
the semantic test questions instead.

## Part 2: Optimization Workflow

### Overview

**Phases**:
1. **Preparation** (Steps 1-3): Setup, backup, initialize
2. **Classification** (Steps 4-5): Identify semantic roles, validate documentation, catalog removals
   - **Step 4**: Agent classifies content with mandatory semantic test documentation
   - **Step 4.5**: Validate classification output (forcing function - catches skipped tests)
   - **Step 5**: Catalog removals with fact extraction
3. **Execution** (Steps 6-7): Remove redundant content and apply word-wrapping
4. **Validation** (Steps 8-11): Verify information preserved, validate semantic integrity and generalization
5. **Recovery** (Step 12): Restore if violations detected
6. **Completion** (Steps 13-14): Commit and report

**Iteration Strategy**:
- After successful validation, loop back to Step 4 to find more redundancies
- Continue until document is optimal (no more removals possible)
- Max 10 optimization iterations
- Max 10 recovery attempts per iteration

---

### Preparation Phase

#### Step 1: Read Document

**Read** the document: `{{arg}}`

---

#### Step 2: Extract Session ID and Create Backup

**Session ID Extraction** (from conversation context):

1. Search conversation for text `Session ID:` (appears in system reminder messages)
2. Extract the 36-character UUID following `Session ID:` (format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)
3. Assign UUID to SESSION_ID variable below

**Where to look**:
1. System reminder messages at conversation start
2. Messages containing "SessionStart hook additional context"
3. SlashCommand tool output for this shrink-doc command

**Fallback if not found**: Generate timestamp-based ID using `date +%Y%m%d-%H%M%S`

**Example**: `Session ID: a3457736-ae82-46c9-b098-3146a66e2506` → Extract `a3457736-ae82-46c9-b098-3146a66e2506`

```bash
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

# Capture original line count
ORIGINAL_LINE_COUNT=$(wc -l < "{{arg}}")

# Track violated candidates across recovery attempts
# Format: "2,4,7" means candidates 2, 4, 7 caused violations
EXCLUDED_CANDIDATES=""

# Track candidate line ranges to detect convergence
# If same lines cataloged 2+ iterations → boundary case (stop removing)
PREVIOUS_CANDIDATE_RANGES=""
OSCILLATION_THRESHOLD=2
```

---

### Classification Phase

#### Step 4: Semantic Role Classification

**Invoke agent to classify content by semantic role:**

```bash
echo "=== STEP 4: SEMANTIC ROLE CLASSIFICATION ==="

# On iteration 2+, check for convergence
if [ "$ITERATION" -gt 1 ]; then
  if diff -q "{{arg}}" "$BACKUP_FILE" > /dev/null 2>&1; then
    echo "✅ CONVERGENCE: File unchanged from previous iteration"
    LINES_AFTER=$ORIGINAL_LINE_COUNT
    LINES_REMOVED=0
    OPTIMAL_STATE="OPTIMAL"
    echo "Proceeding to Step 13 (Cleanup and Commit)..."
    # Skip to Step 13
  fi
fi

# Invoke classification agent
# Task tool parameters:
#   subagent_type: "general-purpose"
#   model: "sonnet"
#   description: "Classify content by semantic role"
#   prompt: <below>
```

**Agent Prompt**:
```
You are classifying documentation content by semantic role to identify removable redundancy.

**FILE TO ANALYZE**: {{arg}}

**YOUR TASK**: Apply the semantic role taxonomy and classification procedure defined in Part 1 of this command file.

**🚨 CRITICAL - MANDATORY SEMANTIC TEST DOCUMENTATION**:

You MUST document semantic test results for EVERY candidate classified as EXPLANATORY or redundant DEMONSTRATIVE.
Simply asserting "Semantic Role: EXPLANATORY" without showing test results is PROHIBITED and will cause validation
failures.

**PROCEDURE**:

1. Read the complete target file ({{arg}})
2. Apply the **Semantic Classification Procedure** from Part 1:
   - Step 0: Check Special Patterns (paired constraints, workflow sequences, context-aware keywords)
   - Step 1: DIRECTIVE Test (execution-critical content)
   - Step 2: CONTEXTUAL Test (clarifies WHEN/HOW to apply directives)
   - Step 3: DEMONSTRATIVE Test (examples, check uniqueness)
   - Step 4: EXPLANATORY (pure rationale/pedagogy)
3. Reference **Common Misclassification Errors** (Part 1) to avoid known mistakes
4. **FOR EACH CANDIDATE**: Document semantic test results (see output format below)
5. Check candidates against Red Flag Keywords list (see below)
6. Identify content blocks that can be safely removed (EXPLANATORY redundancy, redundant DEMONSTRATIVE examples)

**🚨 RED FLAG KEYWORDS - RISK LEVELS**:

If candidate contains ANY of these patterns, classify by risk level and apply corresponding protocol:

**HIGHEST RISK** (95%+ chance of misclassification - assume CONTEXTUAL/DIRECTIVE):
- **"Clarification"** - Almost always CONTEXTUAL (disambiguates directives, specifies boundaries)
- **"Boundary"**, **"Threshold"** - Specifies decision boundaries
- **Numeric patterns** - `N-M`, `>N`, `<N` with units (e.g., "6-30 seconds", ">30 seconds")
- **Timing specifications** - Any content specifying when something is acceptable/valid

**HIGH RISK** (70%+ chance of misclassification - extra scrutiny required):
- **"Why"**, **"Purpose"** - May explain WHEN to use something (CONTEXTUAL), not just rationale
- **"Problem"** - May clarify motivation for alternative (CONTEXTUAL), not just background
- **"Required"**, **"Mandatory"** - Often DIRECTIVE (workflow steps), not EXPLANATORY

**MEDIUM RISK** (40%+ chance of misclassification - standard protocol):
- **"Critical"**, **"Important"** - May be directive emphasis vs. pure rationale
- **"Enforcement"**, **"Validation"**, **"Protocol"** - Often CONTEXTUAL (execution mechanisms)
- **"Test"**, **"Check"**, **"Verify"** - May define test methodology (DIRECTIVE)

**Handling Protocol**:
- **HIGHEST RISK**: Classify as CONTEXTUAL/DIRECTIVE unless proven otherwise with explicit semantic tests
- **HIGH RISK**: Document why NOT contextual/directive (burden of proof reversed)
- **MEDIUM RISK**: Apply standard semantic test procedure
- **ALL LEVELS**: Must provide Red Flag Justification explaining classification

**Red Flag Justification**: Must answer "Why is this EXPLANATORY/redundant despite containing execution-related keywords?" Include specific semantic test results showing negative answers.

**🚨 SPECIAL ATTENTION - TIMING BOUNDARIES & NUMERIC THRESHOLDS**:

If candidate contains ANY of these patterns, it is VERY LIKELY CONTEXTUAL (not EXPLANATORY):
- Numeric ranges: "6-30 seconds", "3-5 retries", "100-200 MB"
- Comparison operators: ">30 seconds", "<10 attempts", "≥ 3 iterations"
- Keywords: "Clarification on Timing", "Timing boundaries", "Acceptable timing", "Threshold"

**Mandatory Check for Numeric Content**:
1. Does this content specify a numeric boundary or threshold?
2. If you removed this, would agent need to guess the acceptable number/range?
3. If YES to both → Classify as CONTEXTUAL (specifies execution boundary), NOT EXPLANATORY

**Example from CLAUDE.md** (this exact pattern was misclassified in session bc6b74a7):
```markdown
**Clarification on Timing**:
- Focus on avoiding user interruptions, not microsecond timing
- 6-30 second gaps within same turn = ACCEPTABLE (achieves coordination)
- >30 second delays suggest sequential work rather than parallel coordination
- Evidence: Session 3fa4e964 had 17-31 second spreads, marked COMPLIANT
```

**Correct Classification**: CONTEXTUAL
**Reason**: Specifies timing boundaries (6-30s = OK, >30s = NOT OK). Without this, agent doesn't know if 25-second gap is acceptable for parallel invocation.

**DO NOT classify as EXPLANATORY just because**:
- Section title sounds pedagogical ("Clarification")
- Contains evidence or examples (may clarify boundary interpretation)
- Seems like it "adds context" (boundary context IS execution-critical)

**OUTPUT FORMAT**:

## Classification Results

### Optimal State: OPTIMAL / SUBOPTIMAL

[If SUBOPTIMAL, list removal candidates below]

### Removal Candidates

Candidate 1 (Lines X-Y):
  Content Preview: [first line...]
  Red Flags: [NONE | list keywords found]

  Semantic Test Results:
    DIRECTIVE Test:
      Q: Does removal prevent execution or create ambiguity?
      A: NO - [explain why not]
      Q: Does this define terms used in directives?
      A: NO - [explain why not]

    CONTEXTUAL Test:
      Q: Without this, does a directive become ambiguous?
      A: NO - [explain why not]
      Q: Does this clarify methodology/approach affecting execution?
      A: NO - [explain why not]
      Q: Does this prevent misapplication?
      A: NO - [explain why not]

    DEMONSTRATIVE Test:
      Q: Does this show directive application?
      A: NO | If YES: Apply uniqueness test

    Classification: EXPLANATORY
    Reason: [Specific reason - narrative redundancy/pure rationale/pedagogical repetition]

  Red Flag Justification: [If red flags present, explain why EXPLANATORY despite execution-related keywords]

Candidate 2 (Lines A-B):
  Content Preview: [first line...]
  Red Flags: [NONE | list keywords found]

  Semantic Test Results:
    [Same format as above - MUST show test results for EVERY candidate]

    Classification: DEMONSTRATIVE (redundant)
    Reason: Third example of same pattern, no new boundary shown

[Continue for all candidates with FULL test documentation...]

### Statistics
- Total candidates: N
- DIRECTIVE blocks: N (preserved)
- CONTEXTUAL blocks: N (preserved)
- DEMONSTRATIVE (unique): N (preserved)
- DEMONSTRATIVE (redundant): N (removal candidates)
- EXPLANATORY (redundant): N (removal candidates)
- Red flag candidates requiring extra scrutiny: N
```

**Extract classification results**:
```bash
# Parse agent response
OPTIMAL_STATE="SUBOPTIMAL"  # ← REPLACE with agent's assessment

if [ "$OPTIMAL_STATE" = "OPTIMAL" ]; then
  echo "✅ DOCUMENT ALREADY OPTIMAL"
  LINES_AFTER=$ORIGINAL_LINE_COUNT
  LINES_REMOVED=0
  echo "Proceeding to Step 13 (Cleanup and Commit)..."
  # Skip to Step 13
else
  echo "⚠️ DOCUMENT SUBOPTIMAL: Proceeding to validation..."
fi
```

---

#### Step 4.5: Validate Classification Documentation (MANDATORY)

**🚨 CRITICAL**: This step validates that Step 4 agent actually applied semantic tests and didn't skip documentation.

**Validation Checklist** - ALL must pass:

```bash
echo "=== STEP 4.5: VALIDATE CLASSIFICATION DOCUMENTATION ==="

# Check 1: Semantic Test Documentation Present
echo "Checking for semantic test documentation..."

# For EACH candidate, verify agent documented:
# - DIRECTIVE Test: Q&A with explanations
# - CONTEXTUAL Test: Q&A with explanations
# - DEMONSTRATIVE Test: Q&A with explanations
# - Classification: Final result with specific reason
#
# FAIL if ANY candidate shows:
# - "Semantic Role: EXPLANATORY" without test Q&A
# - "Classification: EXPLANATORY" without showing negative test results
# - Missing answers to "Does removal prevent execution?"
# - Missing answers to "Without this, does directive become ambiguous?"

TEST_DOCS_PRESENT=true  # ← Set to false if documentation missing

if [ "$TEST_DOCS_PRESENT" = false ]; then
  echo "❌ VALIDATION FAILED: Semantic test documentation missing for candidates"
  echo "Agent MUST show test Q&A for every EXPLANATORY/redundant classification"
  echo "See Step 4 output format - documentation is MANDATORY, not optional"
  echo ""
  echo "Re-invoke Step 4 agent with emphasis on documentation requirements"
  exit 1
fi

echo "✅ Check 1 passed: Semantic tests documented for all candidates"

# Check 2: Red Flag Keywords Addressed
echo "Checking red flag keyword handling..."

# For candidates containing red flags (Why, Clarification, Purpose, Problem, etc.):
# - Verify "Red Flags:" field lists detected keywords
# - Verify "Red Flag Justification:" explains why EXPLANATORY despite keywords
#
# FAIL if:
# - Candidate contains "Why X Matters" but Red Flags: NONE
# - Candidate contains "Purpose:" but no Red Flag Justification
# - Justification is generic ("because it's explanatory") vs specific

RED_FLAGS_HANDLED=true  # ← Set to false if red flags not addressed

if [ "$RED_FLAGS_HANDLED" = false ]; then
  echo "❌ VALIDATION FAILED: Red flag keywords not properly addressed"
  echo "Candidates containing execution-related keywords need extra justification"
  echo "See Step 4 Red Flag Keywords list and justification requirements"
  echo ""
  echo "Re-invoke Step 4 agent with emphasis on red flag detection"
  exit 1
fi

echo "✅ Check 2 passed: Red flag keywords properly addressed"

# Check 3: Known Failure Patterns
echo "Checking for known misclassification patterns..."

# From Error 8, check if agent repeated these mistakes:
# - Classified "Why State Machine Matters" as EXPLANATORY (should be CONTEXTUAL)
# - Classified "Clarification on Timing" as EXPLANATORY (should be CONTEXTUAL)
# - Classified "Required Justification Process" as EXPLANATORY (should be DIRECTIVE)
# - Removed "Correct Pattern" examples that show boundaries (should preserve pairs)
#
# Check agent's semantic test answers:
# - If "Why State Machine" candidate: Did they answer "NO" to "explains enforcement"?
# - If timing clarification: Did they check if it specifies boundaries/thresholds?

KNOWN_PATTERNS_AVOIDED=true  # ← Set to false if repeating known mistakes

if [ "$KNOWN_PATTERNS_AVOIDED" = false ]; then
  echo "❌ VALIDATION FAILED: Agent repeated known misclassification patterns from Error 8"
  echo "Review Error 8 in Part 1 for specific examples of surface-feature misclassification"
  echo ""
  echo "Re-invoke Step 4 agent with explicit reference to Error 8 examples"
  exit 1
fi

echo "✅ Check 3 passed: Known failure patterns avoided"

# Check 4: Error 8 Pattern Matching (MANDATORY)
echo "Checking for Error 8 pattern matches..."

# Specific Error 8 patterns that must be detected:
# Pattern 1: "Why.*Matters" + "State Machine"/"Hook"/"Enforcement" → CONTEXTUAL
# Pattern 2: "Clarification on Timing" + numeric boundaries → CONTEXTUAL
# Pattern 3: "Required.*Process" + numbered steps → DIRECTIVE
# Pattern 4: "Infrastructure vs Feature" + examples showing main agent boundaries → DEMONSTRATIVE

# For each removal candidate:
# - Extract section header from agent's "Candidate N (Lines X-Y): Content Preview: [...]"
# - Check if header matches Error 8 patterns
# - If match found AND classified as EXPLANATORY/redundant → FAIL
#
# Manual check required - review agent's classification output
# Look for candidates with these headers classified incorrectly:
# - "Clarification on Timing" → Must be CONTEXTUAL if contains numeric boundaries
# - "Why [X] Matters" with enforcement/protocol content → Must be CONTEXTUAL
# - "Required [X] Process" with numbered steps → Must be DIRECTIVE
# - Examples showing "CORRECT vs VIOLATION" for main agent → Must be DEMONSTRATIVE

ERROR_8_PATTERN_MISMATCH=false  # ← Set to true if pattern match with wrong classification

if [ "$ERROR_8_PATTERN_MISMATCH" = true ]; then
  echo "❌ VALIDATION FAILED: Candidate matches Error 8 pattern but misclassified"
  echo "Agent classified content matching Error 8 example as EXPLANATORY/redundant"
  echo "Review classification output against Error 8 examples in Part 1"
  echo ""
  echo "Common patterns:"
  echo "  - 'Clarification on Timing' with numeric boundaries → CONTEXTUAL"
  echo "  - 'Why X Matters' explaining enforcement → CONTEXTUAL"
  echo "  - 'Required X Process' with steps → DIRECTIVE"
  exit 1
fi

echo "✅ Check 4 passed: No Error 8 pattern mismatches"
echo ""
echo "✅ ALL CLASSIFICATION VALIDATION CHECKS PASSED"
echo "Proceeding to cataloging..."
```

---

#### Step 5: Catalog Removals with Fact Extraction

**For each removal candidate from Step 4:**

1. **CHECK EXCLUSION LIST**: Skip if candidate number in EXCLUDED_CANDIDATES
2. **CHECK CONVERGENCE**: Skip if line range in PREVIOUS_CANDIDATE_RANGES
3. **EXTRACT FACTS**: What testable information does this content convey?
   - Factual content: Commands, thresholds, parameters → Extract as Q/A pairs
   - Non-factual content: Rationale, narrative → Mark "Facts: NONE"
4. **PRE-SCAN FOR PROTECTED PATTERNS**: Check if candidate contains:
   - Template variables (`{{...}}`)
   - Paired constraints (MUST + NEVER about same topic within candidate)
   - Workflow sequence markers (Step N, BEFORE/AFTER within sequence)
   - If protected patterns found → Exclude this candidate

```bash
echo "=== STEP 5: CATALOGING WITH FACT EXTRACTION ==="

# Report exclusions
if [ -n "$EXCLUDED_CANDIDATES" ]; then
  echo "Excluding candidates from previous violations: $EXCLUDED_CANDIDATES"
fi

# Build catalog (maintain in working memory)
# Format:
# Candidate 1 (Lines 45-48):
#   Semantic Role: EXPLANATORY
#   Type: FACTUAL
#   Facts:
#     Q: How many retries?
#     A: 3-5
#   Pre-scan: PASS
#
# Candidate 2 (Lines 102-105):
#   Semantic Role: EXPLANATORY
#   Type: NON-FACTUAL
#   Facts: NONE
#   Pre-scan: PASS
#
# Candidate 3 (Lines 200-205):
#   Semantic Role: DEMONSTRATIVE
#   Type: FACTUAL
#   Facts:
#     Q: Which flag for case-insensitive?
#     A: -i
#   Pre-scan: FAIL (contains template variable {{arg}})
#   Status: EXCLUDED

CANDIDATE_COUNT=<count>      # Number passing pre-scan
EXCLUDED_COUNT=<count>       # Number excluded by pre-scan
TOTAL_FACTS=<count>          # Facts from FACTUAL candidates

echo "Cataloging complete:"
echo "  - Safe to remove: $CANDIDATE_COUNT candidates"
echo "  - Excluded (protected patterns): $EXCLUDED_COUNT candidates"
echo "  - Facts to validate: $TOTAL_FACTS"

# Update convergence tracking
CURRENT_RANGES="<line-ranges>"  # "45-48,102-105,234-240"
if [ -n "$PREVIOUS_CANDIDATE_RANGES" ]; then
  PREVIOUS_CANDIDATE_RANGES="$PREVIOUS_CANDIDATE_RANGES,$CURRENT_RANGES"
else
  PREVIOUS_CANDIDATE_RANGES="$CURRENT_RANGES"
fi
```

---

### Execution Phase

#### Step 6: Execute All Removals

```bash
echo "=== STEP 6: EXECUTING REMOVALS ==="

LINES_BEFORE=$(wc -l < "{{arg}}")

# Read current document state
# Use Read tool: {{arg}}

# Reconstruct document without cataloged candidates
# 1. Process line by line from Read output
# 2. Skip lines in removal candidates
# 3. Preserve all other content exactly
# 4. Write optimized version in ONE atomic operation
# Use Write tool: {{arg}}

LINES_AFTER=$(wc -l < "{{arg}}")
LINES_REMOVED=$((LINES_BEFORE - LINES_AFTER))

echo "Removed $LINES_REMOVED lines ($CANDIDATE_COUNT candidates)"

# Generate diff for audit
git diff "{{arg}}"
```

---

#### Step 7: Apply Word-Wrapping

**Apply word-wrapping to lines >110 characters while preserving markdown structure:**

```bash
echo "=== STEP 7: APPLY WORD-WRAPPING ==="

# Invoke word-wrapping agent
# Task tool parameters:
#   subagent_type: "general-purpose"
#   model: "sonnet"
#   description: "Apply word-wrapping to long lines"
#   prompt: <below>
```

**Word-Wrapping Agent Prompt**:
```
You are applying word-wrapping to a documentation file to improve readability.

**FILE TO PROCESS**: {{arg}}

**YOUR TASK**: Wrap lines longer than 110 characters while preserving markdown structure and meaning.

**PROCEDURE**:

1. Read the complete file ({{arg}})
2. Process line by line, identifying lines >110 characters
3. Apply word-wrapping rules (see below)
4. Write the wrapped version using Write tool

**WRAPPING RULES**:

**PRESERVE (do NOT wrap)**:
- Frontmatter between `---` markers
- Code blocks between ``` markers
- URLs (keep entire URL on one line)
- Bash commands in code blocks
- Table rows
- YAML/JSON structures
- Lines with template variables ({{...}})
- Command examples with parameters
- File paths

**WRAP (apply wrapping)**:
- Regular text paragraphs (narrative content)
- List items with long descriptions
- Markdown text outside code blocks
- Long sentences in CONTEXTUAL/EXPLANATORY sections

**WRAPPING TECHNIQUE**:
- Break at natural boundaries: spaces, punctuation
- Preserve markdown formatting (**, *, `, etc.)
- Maintain list indentation
- Keep markdown list markers (-, *, 1.) on first line
- Wrapped continuation lines: indent +2 spaces for readability

**EXAMPLES**:

✅ **CORRECT - Wrap long paragraph**:
```markdown
Before (125 chars):
This is a very long paragraph that exceeds the 110 character limit and needs to be wrapped at a natural boundary for readability.

After (wrapped at ~110 chars):
This is a very long paragraph that exceeds the 110 character limit and needs to be wrapped at a natural
boundary for readability.
```

✅ **CORRECT - Wrap long list item**:
```markdown
Before (130 chars):
- **Recognition Tests**: Without this content, the directive has multiple valid interpretations and the agent cannot determine which to apply

After (wrapped with +2 indent):
- **Recognition Tests**: Without this content, the directive has multiple valid interpretations and the
  agent cannot determine which to apply
```

❌ **WRONG - Do NOT wrap URL**:
```markdown
KEEP AS-IS (do not wrap):
See [documentation](https://very-long-domain-name.com/path/to/documentation/with/many/segments/file.md)
```

❌ **WRONG - Do NOT wrap code block**:
```markdown
KEEP AS-IS (do not wrap):
```bash
command --very-long-parameter-name value --another-parameter --yet-another-parameter --final-parameter
```
```

❌ **WRONG - Do NOT wrap command example**:
```markdown
KEEP AS-IS (do not wrap):
`git branch -D {task-name} {task-name}-architect {task-name}-engineer {task-name}-formatter`
```

**OUTPUT**:
Write the word-wrapped version to {{arg}} using Write tool.
```

**After word-wrapping**:
```bash
# Verify file still readable
wc -l < "{{arg}}"

echo "Word-wrapping applied to lines >110 characters"
echo "Proceeding to validation..."
```

---

### Validation Phase

#### Step 8: Fact-Based Validation

```bash
echo "=== STEP 8: FACT-BASED VALIDATION ==="

if [ "$TOTAL_FACTS" -eq 0 ]; then
  echo "No facts to validate (all removals are narrative/rationale)"
  echo "Proceeding to Step 9 (Semantic Integrity Validation)..."
  # Skip to Step 9
else
  # Invoke validator
  # Task tool parameters:
  #   subagent_type: "general-purpose"
  #   model: "sonnet"
  #   description: "Validate facts preserved"
  #   prompt: <below>
```

**Validator Prompt**:
```
You are validating information preservation in a documentation file.

**FILE TO ANALYZE**: {{arg}}

**YOUR TASK**: Answer specific questions testing information preservation.

Read the file completely. For each question below, answer ONLY from information in the file:

[List all questions from Step 5, numbered]

**OUTPUT FORMAT**:

For EACH question:
- Question number
- Answer (from document, or "Cannot determine from document")
- Confidence: HIGH / MEDIUM / LOW

**CRITICAL**:
- HIGH: Information is explicit and clear
- MEDIUM: Information present but requires interpretation
- LOW: Information unclear or requires inference
- If NOT in document: "Cannot determine from document" with LOW confidence
- Do NOT guess or infer

**Example**:
1. How many retries?
   Answer: 3-5
   Confidence: HIGH
```

**Compare expected vs actual answers**:
```bash
# Parse validator response
# For each question:
#   MATCH: Answer correct + confidence HIGH/MEDIUM → Fact preserved
#   CLARITY ISSUE: Answer correct + confidence LOW → Preserved but unclear
#   MISMATCH: Answer wrong or "Cannot determine" → Fact lost

MATCH_COUNT=<count>
CLARITY_COUNT=<count>
MISMATCH_COUNT=<count>

echo "Facts preserved: $MATCH_COUNT / $TOTAL_FACTS"
echo "Facts needing clarity: $CLARITY_COUNT"
echo "Facts lost: $MISMATCH_COUNT"

# Record which candidates caused mismatches (for restoration)
```

---

#### Step 9: Semantic Integrity Validation

**Verify semantic role distribution remains valid:**

```bash
echo "=== STEP 9: SEMANTIC INTEGRITY VALIDATION ==="

# Invoke integrity checker
# Task tool parameters:
#   subagent_type: "general-purpose"
#   model: "sonnet"
#   description: "Validate semantic integrity"
#   prompt: <below>
```

**Integrity Checker Prompt**:
```
You are validating that a documentation file maintains semantic integrity after optimization.

**FILES TO COMPARE**:
- Backup: $BACKUP_FILE
- Current: {{arg}}

**YOUR TASK**: Verify all DIRECTIVE and essential CONTEXTUAL content preserved.

**PROCEDURE**:

1. Read backup file completely
2. Identify all DIRECTIVE content in backup (use semantic tests):
   - Commands, syntax, thresholds, parameters
   - Conditionals, sequences, blocking checkpoints
   - Constraints (MUST/NEVER rules)
   - Decision boundaries (valid/invalid options)

3. Read current file completely
4. For each DIRECTIVE block from backup:
   - Check if present in current file
   - If missing: Record violation

5. Check for ambiguity introduced:
   - Are there directives that became ambiguous after removals?
   - Are there multiple interpretations where there was one before?

**OUTPUT FORMAT** (JSON):

{
  "directive_violations": [
    {
      "missing_directive": "Run grep -i 'pattern' file.txt",
      "line_in_backup": 123,
      "context": "Error handling section",
      "impact": "Agent cannot perform case-insensitive search"
    }
  ],
  "ambiguity_violations": [
    {
      "ambiguous_directive": "Retry if failure occurs",
      "line_in_current": 456,
      "issue": "No retry count specified after removal",
      "interpretations": ["Retry once", "Retry indefinitely", "Retry N times"]
    }
  ],
  "paired_constraint_violations": [
    {
      "issue": "Negative constraint removed but positive remains",
      "line_in_backup": 789,
      "content": "DO NOT use optimizer, hacker agents"
    }
  ]
}

Output ONLY JSON, no additional text.
```

**Extract violations**:
```bash
# Parse integrity checker response
DIRECTIVE_VIOLATIONS=<count>
AMBIGUITY_VIOLATIONS=<count>
PAIRED_VIOLATIONS=<count>

SEMANTIC_VIOLATIONS=$((DIRECTIVE_VIOLATIONS + AMBIGUITY_VIOLATIONS + PAIRED_VIOLATIONS))

if [ "$SEMANTIC_VIOLATIONS" -gt 0 ]; then
  echo "⚠️ Semantic integrity violations detected: $SEMANTIC_VIOLATIONS"
  echo "  - Missing directives: $DIRECTIVE_VIOLATIONS"
  echo "  - Ambiguity introduced: $AMBIGUITY_VIOLATIONS"
  echo "  - Broken constraint pairs: $PAIRED_VIOLATIONS"
else
  echo "✅ Semantic integrity preserved"
fi
```

---

#### Step 10: Clarity Threshold Validation

**Test if directives meet clarity requirements:**

```bash
echo "=== STEP 10: CLARITY THRESHOLD VALIDATION ==="

# Invoke clarity validator
# Task tool parameters:
#   subagent_type: "general-purpose"
#   model: "sonnet"
#   description: "Validate directive clarity"
#   prompt: <below>
```

**Clarity Validator Prompt**:
```
You are validating that directives in documentation remain clear after optimization.

**FILE TO ANALYZE**: {{arg}}

**YOUR TASK**: Test each directive for clarity threshold compliance.

**CLARITY TESTS** (directive must pass all 3):

1. **Completeness**: All parameters/inputs specified
   - FAIL: "Check grep" (which command? which file?)
   - PASS: "Run `grep -i 'pattern' file.txt`"

2. **Unambiguity**: Only ONE valid interpretation
   - FAIL: "Fix if possible" (what determines possible?)
   - PASS: "Fix if deterministic (formula bug, wrong reference)"

3. **Self-sufficiency**: Agent can execute without guessing
   - FAIL: "Use the script" (which script? what parameters?)
   - PASS: "Use `.claude/scripts/recalculate.sh workbook.xlsx`"

**PROCEDURE**:

1. Read file completely
2. Identify all directives (commands, instructions, steps)
3. For each directive, apply all 3 clarity tests
4. Record failures

**OUTPUT FORMAT** (JSON):

{
  "clarity_failures": [
    {
      "directive": "Retry on failure",
      "line": 234,
      "failed_tests": ["Completeness", "Unambiguity"],
      "issues": [
        "No retry count specified",
        "No failure type specified - which failures?"
      ],
      "likely_cause": "Contextual content was removed that specified '3-5 retries on network errors'"
    }
  ]
}

Output ONLY JSON, no additional text.
```

**Extract failures**:
```bash
# Parse clarity validator response
CLARITY_FAILURES=<count>

if [ "$CLARITY_FAILURES" -gt 0 ]; then
  echo "⚠️ Clarity threshold violations: $CLARITY_FAILURES directives became unclear"
else
  echo "✅ All directives meet clarity threshold"
fi
```

---

#### Step 11: Generalization Validation

**Test if removals made content too abstract:**

```bash
echo "=== STEP 11: GENERALIZATION VALIDATION ==="

# Invoke generalization validator
# Task tool parameters:
#   subagent_type: "general-purpose"
#   model: "sonnet"
#   description: "Validate removal didn't over-generalize"
#   prompt: <below>
```

**Generalization Validator Prompt**:
```
You are validating that documentation optimization didn't make content too abstract.

**FILES TO COMPARE**:
- Backup: $BACKUP_FILE
- Current: {{arg}}

**YOUR TASK**: Check if removals created overgeneralization using semantic analysis.

**Apply semantic tests to detect overgeneralization:**

1. **Lost Execution Specificity**: Did removal make a DIRECTIVE vaguer?
   - Test: Can agent execute directive with same precision as before?
   - Example FAIL: "Retry a few times" (was: "Retry 3-5 times")
   - Semantic impact: Agent must guess numeric bounds

2. **Lost Contextual Scope**: Did removal make CONTEXTUAL content vaguer?
   - Test: Does agent know WHEN/WHERE to apply directive as precisely as before?
   - Example FAIL: "Validate changes" (was: "Validate changes BEFORE committing")
   - Semantic impact: Agent doesn't know timing

3. **Lost Demonstrative Distinction**: Did removal eliminate a critical boundary example?
   - Test: Can agent distinguish correct from incorrect as clearly as before?
   - Example FAIL: Removed only "❌ WRONG" example, kept only "✅ CORRECT"
   - Semantic impact: Agent doesn't see decision boundary

4. **Lost Solution Specification**: Did removal make fix/resolution steps vaguer?
   - Test: Can agent resolve issue with same specificity as before?
   - Example FAIL: "Fix the error" (was: "Restore from backup using cp $BACKUP_FILE {{arg}}")
   - Semantic impact: Agent doesn't know HOW to fix

**PROCEDURE**:

1. Read backup file completely
2. Identify all DIRECTIVE and CONTEXTUAL content in backup
3. Read current file completely
4. For each DIRECTIVE/CONTEXTUAL in backup:
   - Find corresponding content in current
   - Apply semantic tests above
   - If current version lost execution-critical specificity → Record overgeneralization

**DO NOT flag as overgeneralization**:
- Removed EXPLANATORY content (benefits, rationale, pedagogy)
- Removed redundant DEMONSTRATIVE examples (third+ example of same concept)
- Simplified wording that preserves execution specificity

**OUTPUT FORMAT** (JSON):

{
  "overgeneralizations": [
    {
      "backup_content": "Retry 3-5 times with exponential backoff",
      "current_content": "Retry if needed",
      "line_in_backup": 123,
      "line_in_current": 98,
      "semantic_role": "DIRECTIVE",
      "lost_specificity": "numeric bounds (3-5) and retry strategy (exponential backoff)",
      "execution_impact": "Agent doesn't know how many retries or what delay strategy to use"
    }
  ]
}

Output ONLY JSON, no additional text.
```

**Extract overgeneralizations**:
```bash
# Parse validator response
OVERGENERALIZATIONS=<count>

if [ "$OVERGENERALIZATIONS" -gt 0 ]; then
  echo "⚠️ Overgeneralization detected: $OVERGENERALIZATIONS cases"
else
  echo "✅ Specificity preserved"
fi
```

---

### Recovery Phase

#### Step 12: Handle Violations

```bash
echo "=== STEP 12: HANDLING VIOLATIONS ==="

TOTAL_VIOLATIONS=$((MISMATCH_COUNT + SEMANTIC_VIOLATIONS + CLARITY_FAILURES + OVERGENERALIZATIONS))

if [ "$TOTAL_VIOLATIONS" -eq 0 ]; then
  echo "✅ ALL VALIDATIONS PASSED"

  if [ "$ITERATION" -lt "$MAX_ITERATIONS" ]; then
    ITERATION=$((ITERATION + 1))
    RECOVERY_ATTEMPT=0
    echo "Iteration $ITERATION: Looking for more optimizations..."
    echo "Returning to Step 4 (Semantic Role Classification)..."
    # Loop back to Step 4
  else
    echo "✅ MAX ITERATIONS REACHED: Optimization complete"
    echo "Proceeding to Step 13 (Cleanup and Commit)..."
    # Skip to Step 13
  fi
fi

if [ "$TOTAL_VIOLATIONS" -gt 0 ]; then
  if [ "$RECOVERY_ATTEMPT" -lt "$MAX_RECOVERY_ATTEMPTS" ]; then
    RECOVERY_ATTEMPT=$((RECOVERY_ATTEMPT + 1))
    echo "⚠️ Violations detected - performing selective restoration (attempt $RECOVERY_ATTEMPT/$MAX_RECOVERY_ATTEMPTS)..."

    # SELECTIVE RESTORATION STRATEGY:
    # 1. Identify which candidates caused violations
    #    - Fact mismatches: Parse validator response for "Candidate N"
    #    - Semantic violations: Map backup line numbers to candidates using catalog
    #    - Clarity failures: Trace "likely_cause" to removed contextual content
    #
    # 2. For each violated candidate:
    #    a. Extract original content from backup (use line range from catalog)
    #    b. Locate insertion point using contextual search:
    #       - Identify 2-3 lines BEFORE removal (from backup)
    #       - Identify 2-3 lines AFTER removal (from backup)
    #       - Use Grep to find context markers in current file
    #       - Insertion point = between context markers
    #    c. Use Edit tool to restore content
    #    d. Verify restoration succeeded
    #
    # 3. Add violated candidates to EXCLUDED_CANDIDATES
    #    - These will be skipped in next iteration's Step 5
    #    - Format: "2,4,7" (comma-separated candidate numbers)
    #
    # 4. Keep successful removals (don't restore non-violated candidates)

    # Example:
    # VIOLATED_CANDIDATES="2 5 7"
    # Update exclusion list: EXCLUDED_CANDIDATES="$EXCLUDED_CANDIDATES,$VIOLATED_CANDIDATES"

    echo "Selective restoration complete"
    echo "Updated exclusion list: $EXCLUDED_CANDIDATES"
    echo "Returning to Step 4 (Semantic Role Classification)..."
    # Loop back to Step 4

  else
    echo "❌ RECOVERY LIMIT REACHED ($MAX_RECOVERY_ATTEMPTS attempts)"
    echo "Restoring from backup..."
    cp "$BACKUP_FILE" "{{arg}}"
    echo "Optimization failed - file restored to original"
    exit 1
  fi
fi
```

**How Convergence Works**:

1. **Optimization Loop**: After successful validation, iterate to find more redundancies (max 10)
2. **Recovery Loop**: If violations detected, restore and retry with exclusions (max 10 per iteration)
3. **Termination Guarantees**:
   - Normal: No more removals possible (optimal state reached)
   - Safety: Iteration/recovery limits prevent infinite loops
   - Fallback: Full restoration from backup if recovery fails

---

### Completion Phase

#### Step 13: Cleanup and Commit

```bash
echo "=== STEP 13: CLEANUP AND COMMIT ==="

# Check if file actually changed
if diff -q "{{arg}}" "$BACKUP_FILE" > /dev/null 2>&1; then
  echo "✅ No changes needed - document already optimal"
  rm "$BACKUP_FILE"
  exit 0
fi

# Calculate metrics
TOTAL_REDUCTION=$((ORIGINAL_LINE_COUNT - LINES_AFTER))

# Build commit message
COMMIT_MSG="Shrink {{arg}} (semantic role analysis)

Removed redundancy while preserving all information.

Optimization method: Semantic role classification
Iterations: $ITERATION
Line change: $ORIGINAL_LINE_COUNT → $LINES_AFTER lines ($TOTAL_REDUCTION removed)
Validation: PASS
- All facts preserved
- Semantic integrity maintained
- Clarity threshold met

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

#### Step 14: Report

**⚠️ IMPORTANT**: Replace ALL placeholders with actual values from execution.

```
## Document Shrink Summary

**Method**: Semantic role classification
- Iterations: [N]
- Candidates cataloged: [count]
- Facts extracted: [count]
- Facts preserved: [count]/[count] (100%)

**Semantic Distribution** (final state):
- DIRECTIVE content: [count] blocks preserved
- CONTEXTUAL content: [count] blocks preserved
- DEMONSTRATIVE (unique): [count] blocks preserved
- EXPLANATORY (redundant): [count] blocks removed

**Metrics**:
- Lines removed: [N]
- Net reduction: [N] lines ([X]%)

**Validation Results**:
✅ All facts preserved
✅ Semantic integrity maintained
✅ Clarity threshold met
✅ Specificity preserved (no overgeneralization)
✅ Information loss: 0
```

---

## Appendix: Quick Reference

### Common Classification Ambiguities

**Template Variables in Examples**:
```markdown
- "Use {{arg}} for the file path" → DIRECTIVE (shows parameter substitution)
- Examples containing {{...}} are protected (agents need to see template syntax)
```

**Methodology Sections**:
```markdown
- "**Recognition Tests**: Check X, Y, Z" → DIRECTIVE (defines what to check)
- "Apply tests in order: 1. X → 2. Y → 3. Z" → DIRECTIVE (defines workflow)
- Both are complementary (test definitions + workflow), not redundant
```

**WHY Content Classification**:
```markdown
- "Problem: X produces false positives" → CONTEXTUAL (clarifies when alternative needed)
- "This is important because it prevents errors" → EXPLANATORY (pure rationale)
- Test: Does WHY clarify WHEN to apply? → CONTEXTUAL. Just emphasize importance? → EXPLANATORY.
```
