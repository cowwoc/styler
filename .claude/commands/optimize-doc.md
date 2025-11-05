---
description: >
  Optimize documentation for conciseness and clarity by strengthening vague instructions and removing
  redundancy
---

# Optimize Documentation Command

**Task**: Optimize the documentation file: `{{arg}}`

## 🚨 DOCUMENT TYPE VALIDATION

**Optimizes ONLY Claude-facing documentation. Refuse human-facing documents.**

### Claude-Facing Documents (ALLOWED)
- `.claude/` configuration files (agents, commands, hooks, settings)
- `CLAUDE.md` and project instructions for Claude
- `docs/project/` development protocol documentation (task-protocol-*, agent-*, *-guide.md)
- `docs/code-style/*-claude.md` style detection patterns for Claude

### Human-Facing Documents (FORBIDDEN)
- `README.md` files (user-facing project documentation)
- `changelog.md`, `CHANGELOG.md` (user-facing release notes)
- `docs/studies/` research and analysis documents
- `docs/decisions/` historical decision records
- `docs/performance/` performance documentation
- `docs/optional-modules/` potentially user-facing guides
- `todo.md` (working task list)
- `docs/code-style/*-human.md` human explanations
- Any documentation in project root intended for human developers

### Validation Procedure

**BEFORE optimizing, check the file path:**

1. **If path matches forbidden patterns, REFUSE**:
   ```
   This command only optimizes Claude-facing documentation.

   The file `{{arg}}` appears to be human-facing documentation (README, changelog, studies, etc.).

   Human-facing documents should not be optimized by this command as they serve external
   audiences and may require specific formatting, marketing language, or pedagogical content.
   ```

2. **If path matches allowed patterns, PROCEED** with optimization

**Detection Patterns**:
- FORBIDDEN: `**/README.md`, `**/changelog.md`, `**/CHANGELOG.md`, `docs/studies/**`, `docs/decisions/**`, `docs/performance/**`, `docs/optional-modules/**`, `todo.md`, `**/*-human.md`
- ALLOWED: `.claude/**`, `CLAUDE.md`, `docs/project/task-protocol-*`, `docs/project/agent-*`, `docs/project/*-guide.md`, `docs/code-style/*-claude.md`

## Objective

Make documentation more concise and clear without introducing vagueness.

**Optimization Goals** (in priority order):
1. **Eliminate vagueness**: Strengthen instructions with explicit criteria and measurable steps
2. **Increase conciseness**: Remove redundancy while preserving all necessary information
3. **Preserve clarity AND meaning**: Never sacrifice understanding or semantic accuracy for brevity

**Critical Constraint**: Instructions (text + examples) should only be updated if the new version retains BOTH
the same meaning AND the same clarity as the old version. If optimization reduces clarity or changes meaning,
reject the change.

**Idempotent Design**: This command can be run multiple times on the same document:
- **First pass**: Strengthens vague instructions, removes obvious redundancy
- **Second pass**: Further conciseness improvements if instructions are now self-sufficient
- **Subsequent passes**: No changes if already optimized

## Analysis Methodology

For each instruction section in the document:

### Step 1: Distinguish Instructions from Examples (DO THIS FIRST)

**First, identify INSTRUCTION vs EXAMPLE:**

**INSTRUCTION** (NEVER remove):
- Commands to execute: `grep -i "keywords" file.md`
- Questions to ask: "Ask: 'Is there an imported data sheet?'"
- Specific actions: "Close Excel, wait 2 seconds, retry"
- Conditions to check: "If grep returns results → do X"

**EXAMPLE** (May remove if instruction clear):
- Demonstrations of the instruction: "For example: grep -i 'foo'"
- Sample outputs: "You should see: [output]"
- Illustrative scenarios: "Like this: [scenario]"

**Test**: Would removing this eliminate a concrete action/command/check?
- YES → It's an INSTRUCTION, keep it
- NO → It's an EXAMPLE, evaluate per Step 2

**Common mistake to avoid:**
❌ WRONG: Treating "grep -i 'keywords' file.md" as an example of grepping
✅ RIGHT: Recognizing this IS the instruction (what command to run)

### Step 2: Evaluate for Vagueness/Ambiguity

**Is the instruction clear WITHOUT the examples?**

Cover the examples and read ONLY the instruction. Then apply these tests:

**Clarity Tests** (ALL must pass):
1. **Completeness**: Are all parameters/inputs specified?
   - ❌ FAIL: "Check grep" (which grep command? which file?)
   - ✅ PASS: "Run `grep -i 'keywords' file.md`"

2. **Unambiguous**: Could this be interpreted in only ONE way?
   - ❌ FAIL: "Fix if possible" (what determines possible?)
   - ✅ PASS: "Fix if deterministic (formula bug, wrong reference)"

3. **Self-sufficient**: Can Claude execute without guessing or inferring?
   - ❌ FAIL: "Use the script" (which script? what parameters?)
   - ✅ PASS: "Use `claude/scripts/recalculate_excel.ps1 workbook.xlsx`"

4. **Preserved context**: Is when/why to apply this clear?
   - ❌ FAIL: "Import first" (import what? when?)
   - ✅ PASS: "Before creating ACB schedule: Import transaction data to separate sheet"

**Decision Tree**:
```
ALL 4 tests pass?
├─ YES → Instruction is CLEAR → Proceed to Step 3 (may remove examples)
└─ NO → Instruction is VAGUE → Proceed to Step 4 (strengthen instruction)
```

**If ANY test fails, the instruction is VAGUE**, not clear.

### Step 3: If Clear (Examples Not Needed for Understanding)

**Proceed only if instruction is clear without examples.**

1. Identify examples following the instruction
2. **Apply Execution Test**: Can Claude execute correctly without this example?
   - If NO (example defines ambiguous term) → **KEEP**
   - If YES → Proceed to step 3
3. Determine if examples serve operational purpose:
   - ✅ Defines what "correct" looks like → **KEEP**
   - ✅ Shows exact commands with success criteria → **KEEP**
   - ✅ Sequential workflows where order matters → **KEEP**
   - ✅ Resolves ambiguity in instruction wording → **KEEP**
   - ✅ Data structures (JSON formats) → **KEEP**
   - ❌ Explains WHY (educational/rationale) → **REMOVE**
   - ❌ Only restates already-clear instruction → **REMOVE**

### Step 4: If Vague (Examples Needed for Understanding)

**DO NOT REMOVE EXAMPLES YET - Strengthen instruction first.**

1. Identify the source of vagueness:
   - Subjective terms without definition
   - Missing criteria or measurements
   - Unclear boundaries or edge cases
   - Narrative description instead of explicit steps

2. Strengthen the instruction:
   - Replace subjective terms with explicit criteria
   - Convert narrative to numbered steps
   - Add measurable thresholds or boundaries
   - Define what "success" looks like

3. **KEEP all examples** - They're needed until instruction is strengthened

4. **Mark for next pass**: After strengthening, examples can be re-evaluated in next optimization pass

## Categories of Examples to KEEP (Even with Clear Instructions)

1. **Executable Commands**: Bash scripts, jq commands, git workflows
2. **Data Structures**: JSON formats, configuration schemas, API contracts
3. **Boundary Demonstrations**: Prohibited vs permitted patterns, edge cases
4. **Concept Illustrations**: Examples that show what a vague term means (e.g., "contextual" JavaDoc)
5. **Templates**: Reusable formats for structured responses
6. **Prevention Examples**: Wrong vs right patterns for frequently violated rules
7. **Pattern Extraction Rules**: Annotations that generalize examples into reusable decision principles

## 🚨 ADDITIONAL PROTECTED CONTENT TYPES

### 1. Content Specification Instructions (NEVER REMOVE)

**Instructions that specify what content to include in outputs are OPERATIONAL, not explanatory.**

**Examples**:
- "Reference the mistake that prompted the update" ← Specifies required content
- "Include brief history of what problems patterns prevent" ← Specifies required content
- "Document context for why specific patterns were added" ← Specifies required content
- "Add inline comments explaining pattern evolution" ← Specifies required content

**Test**: Does this instruction tell the agent what content to produce/include?
- YES → It's a content specification, KEEP IT
- NO → It may be condensable

**Common Mistake**:
❌ WRONG: Treating "Reference the mistake" as explanatory context
✅ RIGHT: Recognizing this specifies required output content

**In Practice**:
```markdown
Before optimization:
3. **Draft Update**:
   - Write clear, specific guidance
   - Include concrete examples (✅ vs ❌)
   - Reference the mistake that prompted the update  ← CONTENT SPECIFICATION
   - Add validation steps
   - Use consistent terminology

After optimization (CORRECT):
3. **Draft** clear guidance with ✅/❌ examples, validation steps,
   consistent terminology, reference triggering mistake
   ^^^ Keep "reference triggering mistake" - specifies required content
```

### 2. Methodology Guidance (PRESERVE PEDAGOGICAL VALUE)

**Sub-bullets that show HOW to execute are different from sub-bullets that list WHAT to collect.**

**Methodology Guidance** (shows process/method):
- Questions that guide thinking: "Was the correct path documented?"
- First step instructions: "Read the agent's invocation prompt"
- Process steps: "Check what files they had access to"
- Diagnostic questions: "Was guidance not prominent enough?"

**Simple Lists** (just enumerates items):
- "Agent prompt, files accessed, missing context" ← Just a list
- No guidance about process or methodology

**Test**: Does this show HOW to think about or approach the task?
- YES → It's methodology guidance, preserve pedagogical value
- NO → It's just a list, may be condensable

**Example**:

❌ **TOO AGGRESSIVE** (loses methodology):
```markdown
Before:
**What information was available to the agent?**
- Read the agent's invocation prompt  ← First step
- Check what files they had access to  ← Second step
- Identify what context was missing    ← Third step

After (LOSES PROCESS):
**Available Information**: Agent prompt, files accessed, missing context
^^^ Lost: The sequential process of HOW to investigate
```

✅ **BETTER** (preserves methodology):
```markdown
Before:
**What information was available to the agent?**
- Read the agent's invocation prompt
- Check what files they had access to
- Identify what context was missing

After (ACCEPTABLE):
**Available Information** (read prompt, check file access, identify gaps):
Agent prompt, files accessed, missing context
^^^ Preserved: The process in parenthetical guidance
```

✅ **BEST** (keeps sub-bullets when they show process):
```markdown
**Available Information**:
1. Read the agent's invocation prompt
2. Check what files they had access to
3. Identify what context was missing
^^^ Preserved: Clear sequential process
```

### 3. Sequencing and Ordering Information (NEVER REMOVE)

**Temporal ordering and prerequisites are execution-critical.**

**Sequencing Indicators** (ALWAYS PRESERVE):
- "Once basic test passes, test variations" ← Sequence: do X, THEN Y
- "Before doing X, verify Y" ← Prerequisite
- "After applying fixes, attempt reproduction" ← Order dependency
- "First... then... finally..." ← Explicit sequence
- "Step 1... Step 2... Step 3..." ← Numbered workflow

**Test**: Does this specify WHEN to do something relative to other steps?
- YES → It's sequencing information, KEEP IT
- NO → May be condensable

**Examples**:

❌ **WRONG** (loses sequencing):
```markdown
Before:
8. **Test Edge Cases**:
   Once basic reproduction test passes, test variations:
   ^^^ WHEN to do this
   - Different agents encountering same scenario
   - Slight variations of the mistake pattern

After (LOSES TIMING):
8. **Edge Cases**: Test variations, different agents
^^^ Lost: Do this AFTER basic test passes
```

✅ **CORRECT** (preserves sequencing):
```markdown
After:
8. **Edge Cases** (after basic test passes): Test variations, different agents
^^^ Preserved: Temporal ordering
```

### 4. Checklist Item Distinctness (PRESERVE UNIQUE CRITERIA)

**Each checklist item should represent a distinct validation criterion.**

**Before Merging/Removing Checklist Items, Verify**:
1. Is this criterion covered by another item?
   - "Concrete examples" vs "Examples are concrete" → Same criterion
   - "Aligns with project conventions" vs "Matches existing format" → Different criteria
2. Does removing this lose a validation angle?
3. Can the remaining items fully cover this check?

**Example**:

⚠️ **QUESTIONABLE** (may lose coverage):
```markdown
Before:
- [ ] No conflicts with other agent configs
- [ ] Aligns with project conventions  ← Broader than just format
- [ ] Terminology is consistent
- [ ] Format matches existing sections

After (POTENTIALLY INCOMPLETE):
- [ ] No conflicts with other configs
- [ ] Consistent terminology
- [ ] Matches existing format
^^^ Lost: "Aligns with project conventions" is broader than format matching
```

✅ **BETTER** (preserves all criteria):
```markdown
After:
- [ ] No conflicts with other configs
- [ ] Aligns with conventions (terminology, format, structure)
^^^ Preserved: Broader convention alignment concept
```

## Categories of Examples to REMOVE

1. **Redundant Clarification**: Examples that restate the instruction in different words
2. **Obvious Applications**: Examples showing trivial applications of clear rules
3. **Duplicate Templates**: Multiple versions of the same template
4. **Verbose Walkthroughs**: Step-by-step narratives when numbered instructions exist

## 🚨 EXECUTION-CRITICAL CONTENT (NEVER CONDENSE)

The following content types are necessary for CORRECT EXECUTION - preserve even if instructions are
technically clear:

### 1. **Concrete Examples Defining "Correct"**
- Examples showing EXACT correct vs incorrect patterns when instruction uses abstract terms
- Specific file paths, line numbers, or command outputs showing what success looks like
- **Test**: Does the example define something ambiguous in the instruction?

**KEEP when instruction says "delete" but example shows this means "remove entire entry, not mark complete"**:
```bash
# ❌ WRONG: Marking complete in todo.md
vim todo.md  # Changed - [ ] to - [x]
git commit -m "..." todo.md  # Result: Still in todo.md

# ✅ CORRECT: Delete from todo.md, add to changelog.md
vim todo.md  # DELETE entire task entry
vim changelog.md  # ADD under ## 2025-10-08
```

**REMOVE if instruction already says "remove entire entry" explicitly** - example becomes redundant.

### 2. **Sequential Steps for State Machines**
- Numbered workflows where order matters for correctness
- State transition sequences where skipping/reordering causes failures
- **Test**: Can steps be executed in different order and still work?

**KEEP numbered sequence** when order is mandatory:
```
1. Complete SYNTHESIS phase
2. Present plan to user
3. Update lock: `jq '.state = "SYNTHESIS_AWAITING_APPROVAL"'`
4. STOP - wait for user
5. On approval: Update lock to `CONTEXT` and proceed
```

**REMOVE numbering** if steps are independent checks that can run in any order.

### 3. **Inline Comments That Specify WHAT to Verify**
- Comments explaining what output to expect or check
- Annotations specifying exact conditions for success/failure
- **Test**: Does comment specify success criteria not in the instruction?

**KEEP comments specifying criteria**:
```bash
# Before rewriting: git rev-list --count HEAD
# After rewriting: git rev-list --count HEAD
# Compare counts - should match unless you explicitly intended to drop commits
```

**REMOVE comments explaining WHY** (e.g., "This prevents data loss because..." is educational, not
operational).

### 4. **Disambiguation Examples**
- Multiple examples showing boundary between prohibited/permitted when rule uses subjective terms
- Examples that resolve ambiguity in instruction wording
- **Test**: Can the instruction be misinterpreted without this example?

**KEEP examples that clarify ambiguous instructions**.
**REMOVE examples that just restate clear instructions**.

### 5. **Pattern Extraction Rules**
- Annotations that generalize specific examples into reusable decision principles
- Text that teaches how to apply the same reasoning to future cases
- **Test**: Does this text extract a general rule from a specific example?

**KEEP pattern extraction annotations**:
```
[Specific example code block]
→ Shows that "delete" means remove lines, not change checkbox.
```
The arrow extracts the general principle (what "delete" means) from the specific example.

**REMOVE pure commentary**:
```
[Example code block]
→ This is a good practice to follow.
```
Generic praise without extracting a reusable decision rule.

**Critical Distinction**:
- ✅ **KEEP**: "→ Specifies exactly what success looks like" (teaches pattern recognition)
- ❌ **REMOVE**: "This example helps you understand the concept" (generic educational)
- ✅ **KEEP**: "→ Claude doesn't need to know why" (generalizes when to remove content)
- ❌ **REMOVE**: "This is important because it prevents errors" (explains WHY, not WHAT)

**Test**: If removed, would Claude lose the ability to apply this reasoning to NEW examples not in the
document? If YES → KEEP (it's pattern extraction, not commentary).

## 🚨 REFERENCE-BASED CONDENSING RULES

**When consolidating duplicate content via references:**

### ❌ NEVER Replace with References

1. **Content within sequential workflows** (Steps 1→2→3)
   - Jumping mid-workflow breaks execution flow
   - Keep operational content inline even if duplicated elsewhere

2. **Quick-reference lists in methodology sections**
   - Simple scannable lists serve different purpose than detailed explanations
   - Both can coexist: brief list for scanning, detailed section for depth

3. **Success criteria at decision points**
   - Content needed AT THE MOMENT of decision must be inline
   - Don't force jumping to verify each criterion

### ✅ OK to Replace with References

1. **Explanatory content that appears in multiple places**
   - Rationale sections
   - Background information
   - Historical context

2. **Content at document boundaries** (intro/conclusion)
   - References acceptable when introducing/summarizing
   - User not mid-execution at these points

3. **Cross-referencing related but distinct concepts**
   - "See also" style references
   - Not replacing direct duplication

### 🔍 Semantic Equivalence Test

**Before replacing content with reference, verify:**

1. **Same information**: Referenced section contains EXACT same information
   - ❌ WRONG: Replace "Goals: A, B, C" with reference to "Priority: C > B > A"
   - ✅ RIGHT: Replace duplicate "Goals: A, B, C" with reference to other "Goals: A, B, C"

2. **Same context**: Referenced section serves same purpose
   - ❌ WRONG: Replace "do X" with reference to "when to do X"
   - ✅ RIGHT: Replace "do X" with reference to "do X"

3. **Same level of detail**: No precision lost in referenced content
   - ❌ WRONG: Replace 7-item checklist with reference to 3-item summary
   - ✅ RIGHT: Replace 7-item checklist with reference to same 7-item checklist

### 📋 Duplication Taxonomy

**Type 1: Quick-Reference + Detailed** (KEEP BOTH)
- Simple list (3-5 words per item) for fast scanning
- Detailed section with tests, examples, edge cases
- **Purpose**: Different use cases - quick lookup vs deep understanding

**Type 2: Exact Duplication** (CONSOLIDATE)
- Same information, same level of detail, same context
- Appearing in multiple places with no contextual justification
- **Purpose**: Genuine redundancy - consolidate to single source

**Type 3: Pedagogical Repetition** (CONTEXT-DEPENDENT)
- Key rules stated multiple times for emphasis
- Summary + detailed explanation
- **Purpose**: Learning/retention - keep if document is pedagogical, remove if reference doc

### 🔍 Pre-Consolidation Verification

**Before removing ANY content for consolidation:**

1. ✅ Content is byte-for-byte duplicate OR semantically equivalent
2. ✅ Replacement reference doesn't interrupt sequential workflow
3. ✅ Referenced section is same level of detail
4. ✅ Consolidation doesn't remove quick-reference value
5. ✅ Verify by test: Can user execute task with reference-based version as easily as inline version?

**If ANY check fails → Keep duplicate inline**

## 🚨 DECISION RULE: The Execution Test

**Before removing ANY content, ask ALL questions in sequence:**

1. **Can Claude execute the instruction CORRECTLY without this content?**
   - If NO → KEEP (execution-critical)
   - If YES → Proceed to question 2

2. **Does this specify what CONTENT to include in outputs?** (NEW)
   - Examples: "Reference the mistake", "Include brief history", "Document context"
   - If YES → KEEP (content specification instruction)
   - If NO → Proceed to question 3

3. **Does this show HOW to execute (methodology), not just WHAT to collect?** (NEW)
   - Examples: First step instructions, diagnostic questions, process guidance
   - If YES → KEEP (methodology guidance) OR preserve in parenthetical
   - If NO → Proceed to question 4

4. **Does this specify WHEN to do something (sequencing/ordering)?** (NEW)
   - Examples: "Once X passes, do Y", "Before doing X", "After applying fixes"
   - If YES → KEEP (temporal ordering)
   - If NO → Proceed to question 5

5. **Is this a distinct checklist criterion not covered by other items?** (NEW)
   - Check: Does removing this lose a unique validation angle?
   - If YES → KEEP (distinct criterion)
   - If NO → Proceed to question 6

6. **Does this content explain WHY (rationale/educational)?**
   - If YES → REMOVE (not needed for execution)
   - If NO → KEEP (operational detail)

7. **Does this content show WHAT "correct" looks like (success criteria)?**
   - If YES → KEEP (execution-critical)
   - If NO → Proceed to question 8

8. **Does this content extract a general decision rule from a specific example?**
   - If YES → KEEP (pattern extraction for future cases)
   - If NO → May remove if redundant

**Critical**: Questions 2-5 are NEW protections added to prevent loss of operational instructions, methodology, sequencing, and distinct validation criteria.

### Examples Applying the Test

**REMOVE THIS** (explains WHY):
```
**RATIONALE**: Git history rewriting can silently drop commits or changes,
especially during interactive rebases where "pick" lines might be accidentally
deleted or conflicts might be resolved incorrectly. Manual verification is the
only reliable way to ensure no data loss occurred.
```
→ Claude doesn't need to know why; just needs to know to verify.

**KEEP THIS** (defines WHAT "correct" means):
```
**ARCHIVAL SUCCESS CRITERIA**:
- `git diff todo.md` shows ONLY deletions
- `git diff changelog.md` shows ONLY additions under today's date
- Both files in SAME commit
- `grep task-name todo.md` returns no matches
```
→ Specifies exactly what success looks like; needed for correct execution.

**REMOVE THIS** (restates clear instruction):
```
When lock acquisition fails, you should not delete the lock file.
Instead, select an alternative task to work on.
```
→ If instruction already says "If lock acquisition fails: Select alternative task, do NOT delete lock"

**KEEP THIS** (resolves ambiguity in "delete"):
```bash
# ❌ WRONG: Marking complete in todo.md
vim todo.md  # Changed - [ ] to - [x]

# ✅ CORRECT: Delete from todo.md
vim todo.md  # DELETE entire task entry
```
→ Shows that "delete" means remove lines, not change checkbox.

### Examples Applying NEW Tests (Questions 2-5)

**KEEP THIS** (Question 2: Content specification):
```markdown
3. **Draft Update**:
   - Write clear, specific guidance
   - Include concrete examples (✅ vs ❌)
   - Reference the mistake that prompted the update  ← CONTENT SPECIFICATION
   - Add validation steps
```
→ "Reference the mistake" specifies required output content, not just explanation.

**KEEP THIS** (Question 3: Methodology guidance):
```markdown
**What information was available to the agent?**
- Read the agent's invocation prompt     ← First step (HOW)
- Check what files they had access to    ← Second step (HOW)
- Identify what context was missing      ← Third step (HOW)
```
→ Sub-bullets show sequential process, not just list of items.

**ACCEPTABLE CONDENSATION** (Question 3: Methodology preserved in parenthetical):
```markdown
**Available Information** (read prompt, check file access, identify gaps):
Agent prompt, files accessed, missing context
```
→ Process guidance preserved in parenthetical form.

**KEEP THIS** (Question 4: Sequencing information):
```markdown
8. **Test Edge Cases**:
   Once basic reproduction test passes, test variations:
   ^^^ WHEN to do this (temporal ordering)
```
→ Specifies to do edge cases AFTER basic test passes.

**KEEP THIS** (Question 5: Distinct checklist criterion):
```markdown
Validation Checklist:
- [ ] No conflicts with other agent configs
- [ ] Aligns with project conventions  ← Broader than just format
- [ ] Terminology is consistent
- [ ] Format matches existing sections
```
→ "Aligns with project conventions" is distinct from and broader than "Format matches".

## 🚨 CONCISENESS vs CORRECTNESS HIERARCHY

**Priority order** when deciding optimizations:

1. **CORRECTNESS** (highest priority)
   - Can Claude execute the instruction correctly without this?
   - Does this resolve ambiguity that would cause wrong execution?

2. **EFFICIENCY** (medium priority)
   - Does removing this make instructions faster to scan?
   - Does condensing reduce cognitive load?

3. **CONCISENESS** (lowest priority)
   - Does this reduce line count?
   - Does this tighten prose?

**Rule**: Never sacrifice correctness for conciseness. Always sacrifice conciseness for correctness.

## Conciseness Strategies

**Apply these techniques to make instructions more concise:**

1. **Eliminate Redundancy**:
   - Remove repeated information across sections
   - Consolidate overlapping instructions
   - Replace verbose phrases with precise terms

2. **Tighten Language**:
   - Replace "you MUST execute" with "execute"
   - Replace "in order to" with "to"
   - Remove filler words ("clearly", "obviously", "simply")

3. **Use Structure Over Prose**:
   - Convert narrative paragraphs to bulleted lists
   - Use numbered steps for sequential processes
   - Use tables for multi-dimensional information

4. **Preserve Essential Elements**:
   - Keep all executable commands (bash, jq)
   - Keep all data structure formats (JSON)
   - Keep all boundary demonstrations (wrong vs right)
   - Keep all measurable criteria and success definitions

**Warning**: Do NOT sacrifice these for conciseness:
- **Semantic metadata headers**: Labels like "**TARGET AUDIENCE**:", "**OUTPUT FORMAT**:", "**INPUT REQUIREMENT**:" provide explicit categorization and scannability
- **Scannability**: Vertical lists are clearer than comma-separated concatenations
- **Pattern recognition**: Checkmarks/bullets for required actions are clearer than prose
- Explicit criteria ("ALL", "at least ONE", "NEVER")
- Measurable thresholds (counts, file paths, exact strings)
- Prevention patterns (prohibited vs required)
- Error condition definitions

**Anti-Pattern Examples** (clarity violations to avoid):
- ❌ Removing semantic metadata headers: "**TARGET AUDIENCE**: Claude AI" → "Claude AI" (loses explicit categorization)
- ❌ Converting vertical list of prohibited phrases to slash-separated concatenation
- ❌ Converting checkmarked action items (✅) to comma-separated prose
- ❌ Removing section headers that aid navigation
- ❌ Consolidating distinct concepts into single run-on sentences
- ❌ Replacing inline workflow criteria with "see section X" mid-execution
- ❌ Replacing "Goals: A, B, C" with reference to "Priority: C > B > A" (not semantically equivalent)
- ❌ Removing quick-reference lists because detailed section exists elsewhere

## Redundancy Taxonomy (Formalized)

**Purpose**: Systematic identification and classification of redundant content types.

**Structure**: Each category includes Pattern, Detection, Fix, and Clarity Impact.

### Category 1: Narrative Redundancy
**Pattern**: Instruction followed by explanation restating the same information

**Detection**:
- Paragraph structure: Statement → "This means..." → Restatement
- Trigger phrases: "In other words", "That is to say", "This means that"
- Semantic test: Second sentence adds no new information

**Examples**:
```markdown
❌ REDUNDANT:
"Execute the validation script before committing. This means that you must
run the script prior to making a commit. In other words, always validate first."
→ 3 sentences saying same thing

✅ CONDENSED:
"Execute the validation script before committing."
→ Single clear instruction
```

**Fix**: Remove explanation sentences, keep instruction
**Clarity Impact**: None (instruction is self-sufficient)

### Category 2: Example Redundancy
**Pattern**: Multiple examples demonstrating identical principle without added value

**Detection**:
- 3+ code blocks showing same concept with trivial variations
- Examples differ only in variable names, not behavior
- No boundary demonstration (all examples clearly permitted or all clearly prohibited)

**Examples**:
```markdown
❌ REDUNDANT (3 examples of same thing):
Example 1: `git status` shows changes
Example 2: `git status` displays modifications
Example 3: `git status` lists uncommitted files
→ All three show: git status reveals uncommitted work

✅ CONDENSED (1 example sufficient):
Example: `git status` shows uncommitted changes
```

**Fix**: Keep 1-2 best examples that show range, remove redundant instances
**Clarity Impact**: Verify via Execution Test - if principle still clear, safe to remove

### Category 3: Definition Redundancy
**Pattern**: Term defined multiple times in different sections

**Detection**:
- Same term (e.g., "OPTIMAL", "SAFE", "VALIDATION") defined in multiple places
- Definitions are semantically equivalent (not context-specific)
- No reference between definitions

**Examples**:
```markdown
❌ REDUNDANT:
Section A: "OPTIMAL: Complete, maintainable, follows best practices"
Section B: "OPTIMAL solution means: Complete implementation, maintainable code, best practices"
→ Same definition in two sections

✅ CONSOLIDATED:
Section A: "OPTIMAL: Complete, maintainable, follows best practices"
Section B: "Use OPTIMAL solution (see terminology definition)"
→ Define once, reference elsewhere
```

**Fix**: Keep definition in primary location, replace others with reference
**Clarity Impact**: ONLY if reference doesn't interrupt workflow (see Reference-Based Condensing Rules)

### Category 4: Procedural Redundancy
**Pattern**: Same procedure described in multiple places with equivalent steps

**Detection**:
- Identical step sequences (1→2→3) in different sections
- Same commands, just rephrased
- No contextual difference justifying duplication

**Examples**:
```markdown
❌ REDUNDANT:
Section "Validation":
1. Run checkstyle
2. Fix violations
3. Re-run to verify

Section "Quality Gates":
1. Execute checkstyle
2. Address violations
3. Verify all pass
→ Same 3-step procedure, different wording

✅ CONSOLIDATED:
Section "Validation": [Full procedure]
Section "Quality Gates": "Follow validation procedure (see Validation section)"
```

**Fix**: Keep detailed procedure in one place, reference from others
**Clarity Impact**: Safe IF reference is at section boundary, UNSAFE if mid-execution

### Category 5: Rationale Redundancy (ALWAYS REMOVABLE)
**Pattern**: Explanations of WHY something is required (educational content)

**Detection**:
- Paragraphs starting with "This is because...", "The reason is...", "This helps by..."
- Describes benefits or historical context
- Doesn't specify WHAT to do or HOW to do it

**Examples**:
```markdown
❌ REDUNDANT (explanatory):
"Verify commit count before and after rebase.

RATIONALE: Git history rewriting can silently drop commits, especially during
interactive rebases where lines might be deleted. Manual verification is the
only reliable way to ensure no data loss."
→ Instruction + WHY explanation

✅ CONDENSED (operational only):
"Verify commit count before and after rebase:
  # Before: git rev-list --count HEAD
  # After: git rev-list --count HEAD
  # Should match unless you intended to drop commits"
→ Instruction + WHAT to check
```

**Fix**: Remove RATIONALE sections, keep instruction + success criteria
**Clarity Impact**: None (Claude doesn't need to know WHY to execute correctly)

### Category 6: Pedagogical Redundancy (CONTEXT-DEPENDENT)
**Pattern**: Key rules stated multiple times for emphasis or learning

**Detection**:
- Same rule appears in summary, section header, and instruction body
- Repetition serves learning/memory (not execution)
- Common in tutorial-style documents

**Examples**:
```markdown
❌ REDUNDANT (pedagogical emphasis):
Summary: "Never commit without testing"
Section: "Pre-Commit Requirements: Never commit without testing"
Instruction: "Before committing, run tests (never commit without testing)"
→ Rule stated 3 times for emphasis

✅ FOR REFERENCE DOC (condensed):
Summary: "Testing required before commits"
Section: "Pre-Commit Requirements"
Instruction: "Before committing, run tests"
→ Rule stated once per context

✅ FOR TUTORIAL (keep repetition):
[Same as redundant example - repetition aids learning]
```

**Fix**:
- Reference documents: Remove pedagogical repetition
- Tutorials/guides: Keep repetition for learning
- Decision: Based on document type

**Clarity Impact**: None for reference docs, potentially harmful for tutorials

### Detection Decision Tree

```
Found repeated content?
├─ Same instruction in multiple places?
│  ├─ YES → Category 3 (Definition) or 4 (Procedural)
│  └─ NO → Continue
├─ Instruction + explanation restating it?
│  ├─ YES → Category 1 (Narrative)
│  └─ NO → Continue
├─ Multiple examples of same concept?
│  ├─ YES → Category 2 (Example)
│  └─ NO → Continue
├─ Explains WHY (rationale/benefits)?
│  ├─ YES → Category 5 (Rationale) - ALWAYS REMOVE
│  └─ NO → Continue
└─ Repetition for emphasis/learning?
   ├─ YES → Category 6 (Pedagogical) - CONTEXT-DEPENDENT
   └─ NO → Not redundancy, likely serves distinct purpose
```

### Application Workflow

For each section during optimization:

1. **Identify redundancy type** using detection patterns above
2. **Apply category-specific fix** (remove, consolidate, or reference)
3. **Verify clarity preserved** using 4-part test
4. **Document in reporting**: Track which categories eliminated

## Optimization Strategy

**Single-Pass Approach** (when possible):
- Strengthen vague instructions AND remove obvious redundancy in one pass
- Commit: "Optimize [filename] for conciseness and clarity"

**Multi-Pass Approach** (for complex documents):
- **First pass**: Strengthen vague instructions + remove obvious redundancy
- **Second pass**: Further conciseness improvements now that instructions are self-sufficient
- **Subsequent passes**: No changes if already optimized

**User Workflow**:
```bash
# First invocation: Strengthens and removes redundancy
/optimize-doc docs/some-file.md

# Review changes, then optional second invocation for further optimization
/optimize-doc docs/some-file.md

# Subsequent invocations: No changes if already optimized
/optimize-doc docs/some-file.md
```

## Execution Instructions

1. **Read** the document specified: `{{arg}}`
2. **Create Backup** (MANDATORY before making changes):
   ```bash
   # Create backup with timestamp
   BACKUP_FILE="{{arg}}.backup-$(date +%s)"
   cp "{{arg}}" "$BACKUP_FILE"
   echo "Created backup: $BACKUP_FILE"

   # Store backup path for later cleanup
   echo "$BACKUP_FILE" > /tmp/optimize-doc-backup.txt
   ```
3. **Analyze** each section using the methodology above
4. **Optimize** directly:
   - Strengthen vague instructions with explicit criteria
   - Remove redundant content while preserving clarity
   - Apply conciseness strategies where beneficial
5. **Validate** each condensed section:
   - **Re-read the condensed instruction independently** (without context of original)
   - **Apply the 4 Clarity Tests** from Step 2
   - **If any test fails**: Restore enough content to pass all 4 tests
   - **Document validation**: Note which sections were validated and passed

5B. **Category-Specific Validation** (Enhanced):

After condensing content, apply validation specific to redundancy type being removed:

**For Narrative Redundancy Removal**:
- [ ] Is instruction still complete without explanation?
- [ ] All parameters specified
- [ ] Success criteria clear
- [ ] No ambiguous terms undefined
- **Test**: Can Claude execute instruction without explanation?

**For Example Redundancy Removal**:
- [ ] Does remaining example(s) demonstrate full concept range?
- [ ] Shows boundary between correct/incorrect (if applicable)
- [ ] Covers edge cases (if applicable)
- [ ] Pattern extraction still possible
- **Test**: Can Claude recognize this pattern in new contexts?

**For Definition Redundancy Consolidation**:
- [ ] Is reference clear and non-disruptive?
- [ ] Reference at section boundary (not mid-workflow)
- [ ] Referenced definition is complete
- [ ] No circular references
- **Test**: Can Claude find definition without frustration?

**For Procedural Redundancy Consolidation**:
- [ ] Does consolidated procedure cover all use cases?
- [ ] Primary procedure has all steps from duplicates
- [ ] Context differences documented
- [ ] References don't break workflow
- **Test**: Can Claude execute from consolidated version?

**For Rationale Redundancy Removal**:
- [ ] Does instruction still convey success criteria?
- [ ] WHAT to do: Clear
- [ ] HOW to do it: Clear
- [ ] WHAT "correct" looks like: Clear
- [ ] WHY to do it: Removed (not needed)
- **Test**: Can Claude execute without understanding rationale?

**For Pedagogical Redundancy Removal**:
- [ ] Document type appropriate for removal?
- [ ] Reference document: Safe to remove repetition
- [ ] Tutorial/guide: Keep repetition for learning
- [ ] Mixed: Keep in learning sections, remove in reference sections
- **Test**: Does document type justify repetition?

**Validation Failure Recovery**:

If category-specific validation fails:
1. Identify which check failed
2. Restore sufficient content to pass check
3. Re-run validation until all checks pass
4. Document in self-review: "Partial removal, preserved X due to Y"

6. **Self-Review Changes** (MANDATORY before reporting):
   - **Stage changes**: `git add {{arg}}`
   - **Review diff**: `git diff --cached {{arg}} | head -200`
   - **Apply Critical Questions to ALL changes**:

     **Question 1: Was any meaning lost?**
     - Review each deleted line/section in the diff
     - Check: Does removed content specify what to produce, how to execute, or when to do it?
     - Check: Are any content specifications, methodology steps, or sequencing indicators removed?
     - If YES → **RESTORE** that content

     **Question 2: Was any vagueness introduced?**
     - Review each condensed instruction
     - Apply 4 Clarity Tests (Completeness, Unambiguous, Self-sufficient, Context preserved)
     - Check: Can this be interpreted in only ONE way without the original?
     - If NO → **RESTORE** specificity or add clarifying details

     **Question 3: Was any ability to execute properly lost?**
     - Review each modified workflow, checklist, or command sequence
     - Check: Can Claude execute this correctly without removed content?
     - Check: Are all parameters, file paths, and success criteria present?
     - Check: Is temporal ordering preserved (before/after/once relationships)?
     - If NO → **RESTORE** execution-critical content

   - **If ANY question reveals issues**: Use Edit tool to restore necessary content
   - **Validation**: Re-run self-review after fixes until all 3 questions pass
   - **Unstage if major issues found**: `git reset {{arg}}` and return to step 4

   - **Rollback if needed** (if major issues found that can't be easily fixed):
     ```bash
     # Restore from backup
     BACKUP_FILE=$(cat /tmp/optimize-doc-backup.txt)
     cp "$BACKUP_FILE" "{{arg}}"
     echo "Restored from backup: $BACKUP_FILE"
     # Return to step 4 to try again
     ```

7. **Remove Backup** (once self-review confirms changes are safe):
   ```bash
   # Read backup path
   BACKUP_FILE=$(cat /tmp/optimize-doc-backup.txt)

   # Remove backup
   rm "$BACKUP_FILE"
   rm /tmp/optimize-doc-backup.txt
   echo "Backup removed: Changes confirmed safe"
   ```

8. **Report** changes made AND self-review results:
   ```
   ## Optimization Summary
   [Standard summary format]

   ## Self-Review

   **Meaning Preservation**: ✅ No operational instructions lost
   **Vagueness Check**: ✅ All condensed sections pass 4 Clarity Tests
   **Executability**: ✅ All workflows remain executable

   [Or if issues found and fixed:]

   **Issues Found and Fixed**:
   - Line X: Restored "Reference the mistake" (content specification)
   - Line Y: Added parenthetical methodology guidance
   - Line Z: Restored sequencing "Once X passes, do Y"

   **Backup Status**: ✅ Removed (changes confirmed safe)
   ```

9. **Commit** the optimized document with descriptive message

## Quality Standards

**Every change must satisfy ALL criteria:**
- ✅ **Meaning preserved**: Instructions mean exactly the same thing
- ✅ **Executability preserved**: Claude can execute correctly without removed content
- ✅ **Success criteria intact**: What "correct" looks like is still clear
- ✅ **Ambiguity resolved**: Any ambiguous terms still have defining examples
- ✅ **Conciseness increased**: Redundancy eliminated or prose tightened

**Verification Happens at TWO Stages**:

### Stage 1: Per-Section Validation (Step 4)

Apply during optimization as each section is modified:

1. ✅ **Completeness preserved**: All commands, file paths, parameters present
2. ✅ **Unambiguous**: Can only be interpreted one way
3. ✅ **Self-sufficient**: No guessing required to execute
4. ✅ **Context preserved**: When/why to apply is clear

**Spot-check method**:
- Pick 5 random condensed sections
- Read ONLY the condensed version (cover original)
- Ask: "Could I execute this correctly without the original?"
- If NO for any → Restore content until YES

### Stage 2: Final Self-Review (Step 5 - MANDATORY)

After ALL changes complete, review the full git diff:

**The 3 Critical Questions**:
1. **Was any meaning lost?** (operational instructions, content specs, methodology, sequencing)
2. **Was any vagueness introduced?** (4 Clarity Tests: Completeness, Unambiguous, Self-sufficient, Context)
3. **Was any ability to execute properly lost?** (workflows, parameters, success criteria, temporal ordering)

**Apply the 8-question Execution Test** (see "DECISION RULE: The Execution Test" section above) **to each change. If ANY test fails for execution-critical content → RESTORE**

**Result**: Self-review report with ✅ confirmations or list of issues found and fixed

**Change Summary Format** (in your response):
```
## Optimization Summary

**Changes Made**:
1. [Section Name] (Lines X-Y): [Brief description of change]
   - Before: [Key issue - vagueness, redundancy, verbosity]
   - After: [How it was improved]

2. [Section Name] (Lines A-B): [Brief description]
   - ...

**Metrics**:
- Lines removed: N
- Sections strengthened: M
- Redundancy eliminated: [specific examples]

**Next Steps**:
- [If further optimization possible] Run /optimize-doc again
- [If complete] Document fully optimized
```

## Success Criteria

- Document is more concise (fewer lines, tighter prose)
- Instructions are clearer (explicit criteria, measurable steps)
- All necessary information preserved (no loss of meaning)
- User can execute instructions without ambiguity
