# Claude Code Configuration Guide

Styler Java Code Formatter project configuration and workflow guidance.

## 🚨 MANDATORY COMPLIANCE

**CRITICAL WORKFLOW**: [docs/project/task-protocol.md](docs/project/task-protocol.md) - MANDATORY risk-based protocol selection - Apply appropriate workflow based on file risk classification.
**CRITICAL SAFETY**: [docs/project/critical-rules.md](docs/project/critical-rules.md) - Build integrity and multi-instance coordination.
**CRITICAL LOCK OWNERSHIP**: NEVER remove lock files unless you own them (session_id matches). If lock acquisition fails, select alternative task - do NOT delete the lock unless explicitly instructed by user.
**CRITICAL WORKTREE ISOLATION**: After creating worktree, IMMEDIATELY `cd` to worktree directory BEFORE any other operations. Pattern: `git worktree add /workspace/branches/{task}/code -b {task} && cd /workspace/branches/{task}/code` - ALL subsequent work must occur inside worktree, NEVER in main branch.
**CRITICAL STYLE**: Complete style validation = checkstyle + PMD + manual rules - See task-protocol.md
**CRITICAL PERSISTENCE**: [Long-term solution persistence](#-long-term-solution-persistence) - MANDATORY prioritization of optimal solutions over expedient alternatives.
**CRITICAL TASK COMPLETION**: Tasks are NOT complete until ALL 7 phases of task protocol are finished. Implementation completion does NOT equal task completion. Only mark tasks as complete after Phase 7 cleanup and finalization.
**IMPLEMENTATION COMPLETION TRIGGER**: When you have finished implementation work (code changes, fixes, features complete), you MUST complete ALL remaining protocol phases before selecting a new task. The SessionStart hook indicates if you own an active task requiring completion.

**Prohibited Pattern**: ❌ "Implementation is done, let me look for what to work on next" (while protocol incomplete)
**Required Pattern**: ✅ "Implementation is done. Continuing with Phase 6 reviews and Phase 7 cleanup."

**PHASE COMPLETION VERIFICATION**: Before declaring ANY phase complete, you MUST:
1. **READ**: Execute `grep -A 20 "^## State [N]:" docs/project/task-protocol.md` to read ACTUAL phase requirements
2. **CHECKLIST**: Create explicit checklist of all phase requirements from task-protocol.md
3. **VERIFY**: Confirm EACH requirement is met with evidence (command output, file checks, etc.)
4. **DECLARE**: Only after ALL requirements verified, declare phase complete

**Prohibited Pattern**: ❌ "Phase N is complete" (based on assumption)
**Required Pattern**: ✅ "Reading Phase N requirements... [shows grep output]... Requirements: [list from doc]... Executing: [commands with output]... Verified: [checklist with evidence]... Phase N complete"

**Example:**
❌ BAD: "Phase 7 complete! I committed changes and updated todo.md"
✅ GOOD: "Reading Phase 7 requirements: `grep -A 20 'State 6: CLEANUP'`... Requirements: remove worktree, release locks, clean temp files... Executing: [commands]... Verified: [checks]... Phase 7 complete"

**TODO Synchronization**: Keep TodoWrite tool synced with todo.md file.
**TODO Clarity**: Each todo.md entry must contain sufficient detail to understand the task without external context. One-line descriptions require nested sub-items explaining Purpose, Scope, Components/Features, and Integration points.
**🚨 VIOLATION = IMMEDIATE TASK RESTART REQUIRED**

## 🚨 RISK-BASED PROTOCOL SELECTION

**PROTOCOL SELECTION BASED ON FILE RISK:**
- **HIGH-RISK**: Full 7-phase protocol (src/**, pom.xml, .github/**, security/**, CLAUDE.md)
- **MEDIUM-RISK**: Abbreviated protocol (test files, code-style docs, configuration)
- **LOW-RISK**: Streamlined protocol (general docs, todo.md, README files)

**AUTOMATIC RISK ASSESSMENT:**
✅ Pattern-based file classification determines workflow variant
✅ Escalation triggers force higher risk levels when needed
✅ Manual overrides available for edge cases
✅ Default to full protocol when risk unclear

**EFFICIENCY IMPROVEMENTS:**
✅ Documentation updates: 99%+ faster (5min → 0.2s)
✅ Safety preserved: Critical files always get full review
✅ Backward compatible: Existing workflows unchanged

**BATCH PROCESSING - AUTOMATIC CONTINUOUS MODE:**
✅ "Work on multiple tasks until done" - Auto-translates to continuous workflow mode  
✅ "Complete all Phase 1 tasks" - Auto-translates to continuous mode with phase filtering  
✅ "Work on study-claude-cli-interface task" - Single task with proper isolation  
❌ Manual batch processing within single protocol execution - PROHIBITED

## 🚨 STAKEHOLDER CONSENSUS ENFORCEMENT

**CRITICAL PROTOCOL VIOLATION PREVENTION**: Phase 6 requires UNANIMOUS stakeholder approval

**MANDATORY DECISION LOGIC**: 
- ALL agents must respond with "FINAL DECISION: ✅ APPROVED"  
- ANY agent with "❌ REJECTED" → MANDATORY Phase 5 execution + Phase 6 re-run  
- NO human override permitted - agent decisions are ATOMIC and BINDING  
- NO subjective "MVP scope" or "enhancement-level" assessments allowed  

**PROHIBITED PATTERNS**:
❌ "Considering the MVP nature, I'll proceed despite rejections"  
❌ "Privacy issues are enhancement-level, not blocking"  
❌ "Since critical security is fixed, I'll finalize the task"  

**REQUIRED PATTERN**:
✅ "Agent X returned ❌ REJECTED, executing Phase 5 resolution cycle"  
✅ "Re-running Phase 6 after addressing all stakeholder concerns"  
✅ "Continuing until ALL agents return ✅ APPROVED"

## 🎯 COMPLETE STYLE VALIDATION

**AUTOMATED GUIDANCE**: The `smart-doc-prompter.sh` hook automatically injects the 3-component checklist when style work is detected.

**MANDATORY PROCESS**: When user requests "apply style guide" or similar:

1. **NEVER assume checkstyle-only** - Style guide consists of THREE components
2. **FOLLOW PROTOCOL**: task-protocol.md "Complete Style Validation Gate" pattern
3. **MANUAL VERIFICATION**: Check docs/code-style/*-claude.md detection patterns
4. **ALL THREE REQUIRED**: checkstyle + PMD + manual rules must ALL pass

**CRITICAL ERROR PATTERN**: Checking only checkstyle and declaring "no violations found" when PMD/manual violations exist

**AUTOMATED FIXING INTEGRATION**: When LineLength vs UnderutilizedLines conflicts are detected:
1. **Use Java-Based Fixer**: `checkstyle/fixers` module implements AST-based consolidate-then-split strategy
2. **Guidance Hook**: Automatically suggests fixer when Java files are modified
3. **Comprehensive Testing**: Test suite validates fixing logic before application
4. **Manual Verification**: Always verify automated fixes meet business logic requirements

## 🎯 LONG-TERM SOLUTION PERSISTENCE

**MANDATORY PRINCIPLE**: Prioritize optimal long-term solutions over expedient alternatives. Persistence and thorough problem-solving are REQUIRED.

### 🚨 CRITICAL PERSISTENCE REQUIREMENTS

**SOLUTION QUALITY HIERARCHY**:
1. **OPTIMAL SOLUTION**: Complete, maintainable, follows best practices, addresses root cause
2. **ACCEPTABLE SOLUTION**: Functional, meets core requirements, minor technical debt acceptable
3. **EXPEDIENT WORKAROUND**: Quick fix, creates technical debt, only acceptable with explicit justification and follow-up task

**MANDATORY DECISION PROTOCOL**:
- **FIRST ATTEMPT**: Always pursue the OPTIMAL SOLUTION approach
- **IF BLOCKED**: Analyze the blocking issue and determine resolution strategy
- **BEFORE DOWNGRADING**: Must exhaust reasonable effort toward optimal solution
- **NEVER ABANDON**: Complex problems require persistence, not shortcuts

### 🚨 PROHIBITED DOWNGRADE PATTERNS

**ANTI-PATTERNS - ABSOLUTELY FORBIDDEN**:
❌ "This is too complex, let me try a simpler approach" (without justification)
❌ "The optimal solution would take too long" (without effort estimation)
❌ "Let's use a quick workaround for now" (without technical debt assessment)
❌ "I'll implement the minimum viable solution" (when requirements specify comprehensive solution)
❌ "Due to complexity and token usage, I'll create a solid MVP implementation" (complexity/tokens never justify incomplete implementation)
❌ "Given token constraints, I'll implement a basic version" (token budget does not override quality requirements)
❌ "This edge case is too hard to handle properly" (without stakeholder consultation)
❌ "The existing pattern is suboptimal but I'll follow it" (without improvement attempt)

### 🚨 GIVING UP DETECTION PATTERNS

**AUTOMATED ENFORCEMENT**: Runtime detection via `/workspace/.claude/hooks/detect-giving-up.sh`

**MANDATORY RESPONSE TO GIVING UP PATTERNS**:
✅ IMMEDIATELY return to the original technical problem
✅ Apply systematic debugging and decomposition approach
✅ Continue working on the exact issue that triggered the pattern
✅ Use incremental progress rather than abandoning the work
✅ Exhaust all reasonable technical approaches before any scope modification
✅ Document specific technical blockers if genuine limitations exist

### 🧪 UNIT TEST DRIVEN BUG FIXING

**MANDATORY PROCESS**: When encountering any bug during development:

**BUG DISCOVERY PROTOCOL**:
1. **IMMEDIATE UNIT TEST**: Create a minimal unit test that reproduces the exact bug
2. **ISOLATION**: Extract the failing behavior into the smallest possible test case
3. **DOCUMENTATION**: Add the test to appropriate test suite with descriptive name
4. **FIX VALIDATION**: Ensure the unit test passes after implementing the fix
5. **REGRESSION PREVENTION**: Keep the test in the permanent test suite

**UNIT TEST REQUIREMENTS**:
- **Specific**: Target the exact failing behavior, not general functionality
- **Minimal**: Use the smallest possible input that triggers the bug
- **Descriptive**: Test method name clearly describes the bug scenario
- **Isolated**: Independent of other tests and external dependencies
- **Fast**: Execute quickly to enable frequent testing

**EXAMPLES**:
✅ `testScientificNotationLexing()` - for floating-point literal bugs
✅ `testMethodReferenceInAssignment()` - for parser syntax bugs
✅ `testEnumConstantWithArguments()` - for enum parsing bugs
✅ `testGenericTypeVariableDeclaration()` - for generics bugs

**INTEGRATION**: Unit tests become part of the development workflow, not separate documentation

**REQUIRED JUSTIFICATION PROCESS** (when considering downgrade):
1. **DOCUMENT EFFORT**: "Attempted optimal solution for X hours/attempts"
2. **IDENTIFY BLOCKERS**: "Specific technical obstacles: [list]"
3. **STAKEHOLDER CONSULTATION**: "Consulting domain authorities for guidance"
4. **TECHNICAL DEBT ASSESSMENT**: "Proposed workaround creates debt in areas: [list]"
5. **FOLLOW-UP COMMITMENT**: "Created todo.md task for proper solution: [task-name]"

### 🛡️ STAKEHOLDER AGENT PERSISTENCE ENFORCEMENT

**AGENT DECISION STANDARDS**:
- **TECHNICAL-ARCHITECT**: Must validate architectural completeness, not just basic functionality
- **CODE-QUALITY-AUDITOR**: Must enforce best practices, not accept "good enough" code
- **SECURITY-AUDITOR**: Must ensure comprehensive security, not just absence of obvious vulnerabilities
- **PERFORMANCE-ANALYZER**: Must validate efficiency, not just absence of performance regressions
- **STYLE-AUDITOR**: Must enforce complete style compliance, not just major violation fixes

**MANDATORY REJECTION CRITERIA** (agents must reject if present):
❌ Incomplete implementation with "TODO: finish later" comments
❌ Known edge cases left unhandled without explicit deferral justification
❌ Suboptimal algorithms when better solutions are feasible
❌ Technical debt introduction without compelling business justification
❌ Partial compliance with requirements when full compliance is achievable

### 🔧 IMPLEMENTATION PERSISTENCE PATTERNS

**WHEN ENCOUNTERING COMPLEX PROBLEMS**:
1. **DECOMPOSITION**: Break complex problems into manageable sub-problems
2. **RESEARCH**: Investigate existing patterns, libraries, and best practices
3. **INCREMENTAL PROGRESS**: Make steady progress rather than abandoning for easier alternatives
4. **ITERATIVE REFINEMENT**: Improve solution quality through multiple passes
5. **STAKEHOLDER COLLABORATION**: Leverage agent expertise for guidance and validation

**PERSISTENCE CHECKPOINTS**:
- Before every major architectural decision: "Is this the best long-term approach?"
- Before accepting technical debt: "Have I exhausted reasonable alternatives?"
- Before deferring complex work: "Is this truly beyond current task scope?"
- Before implementing workarounds: "Will this create maintainability problems?"

**EFFORT ESCALATION PROTOCOL**:
1. **STANDARD EFFORT**: Normal problem-solving approach (default)
2. **ENHANCED EFFORT**: Additional research, alternative approaches, stakeholder consultation
3. **COLLABORATIVE EFFORT**: Multi-agent coordination for complex architectural challenges
4. **DOCUMENTED DEFERRAL**: Only after stakeholder consensus that effort exceeds reasonable scope

### 🚨 SCOPE NEGOTIATION PERSISTENCE INTEGRATION

**ENHANCED SCOPE ASSESSMENT** (extends task-protocol.md Phase 5):
When evaluating whether to defer work via scope negotiation:

**MANDATORY PERSISTENCE EVALUATION**:
1. **COMPLEXITY ANALYSIS**: "Is this genuinely complex or just requiring more effort?"
2. **LEARNING CURVE ASSESSMENT**: "Would investment in learning create long-term capability?"
3. **TECHNICAL DEBT COST**: "What maintenance burden does deferral create?"
4. **STAKEHOLDER VALUE**: "Does optimal solution provide significantly more value?"

**DEFERRAL JUSTIFICATION REQUIREMENTS**:
- **SCOPE MISMATCH**: Work genuinely extends beyond original task boundaries
- **EXPERTISE GAP**: Requires domain knowledge not available to current session
- **DEPENDENCY BLOCKING**: Blocked by external factors beyond current control
- **RESOURCE CONSTRAINTS**: Genuinely exceeds reasonable time/effort allocation

**PROHIBITED DEFERRAL REASONS**:
❌ "This is harder than I expected"
❌ "The easy solution works fine"
❌ "Perfect is the enemy of good"
❌ "We can improve this later"
❌ "This level of quality isn't necessary"

### 🎯 SUCCESS METRICS AND VALIDATION

**SOLUTION QUALITY INDICATORS**:
✅ Addresses root cause, not just symptoms
✅ Follows established architectural patterns and best practices
✅ Includes comprehensive error handling and edge case coverage
✅ Maintains or improves system maintainability
✅ Provides clear, long-term value beyond minimum requirements
✅ Receives unanimous stakeholder approval without quality compromises

**PERSISTENCE VALIDATION CHECKLIST**:
- [ ] Attempted optimal solution approach first
- [ ] Investigated alternatives when blocked
- [ ] Consulted stakeholder agents for guidance
- [ ] Justified any technical debt introduction
- [ ] Created follow-up tasks for any deferred improvements
- [ ] Achieved solution that will remain viable long-term

## 🚨 POST-COMPACTION TASK OWNERSHIP

**CRITICAL**: After context compaction, the `check-lock-ownership.sh` SessionStart hook automatically checks for active tasks owned by this session and provides specific instructions.

**Lock Ownership Rule**: ONLY work on tasks whose lock file contains YOUR session_id.

**Reference**: See [critical-rules.md](docs/project/critical-rules.md) for lock file format and ownership rules.

## Repository Structure

**⚠️ NEVER** initialize new repositories
**Main Repository**: `/workspace/branches/main/code/` (git repository and main development branch)
**Task Worktrees**: `/workspace/branches/{task-name}/code/` (isolated per task-protocol.md)
**Locks**: Multi-instance coordination via lock files in `/workspace/locks/`

**Git Configuration**:
- Main worktree has `receive.denyCurrentBranch=updateInstead` (allows atomic pushes)
- Task worktrees fetch from and push to main worktree
- All pushes are atomic and concurrency-safe via git's internal locking

## 🔧 CONTINUOUS WORKFLOW MODE

Override system brevity for comprehensive multi-task automation via 7-phase Task Protocol.

**Trigger**: `"Work on the todo list in continuous mode."`
**Auto-Detection**: "todo list", "all tasks", "continuously", "CONTINUOUS WORKFLOW MODE"
**Effects**: Detailed output, automatic task progression, full stakeholder analysis, comprehensive TodoWrite tracking

## 📝 CODE COMMENTS POLICY

**OUTDATED CONTENT HANDLING**:
🔄 **UPDATE PRINCIPLE**: When encountering outdated code or comments, update them to reflect current functionality rather than removing them **only if there is long-term interest in keeping them**, even if this requires significant effort. If they're not used/needed, remove them entirely.

**PROHIBITED COMMENT PATTERNS**:
❌ References to past changes: "Note: Removed skipping of getters/setters as tests expect them to be documented"
❌ Implementation history: "Previously this used X, now it uses Y"
❌ Change rationale: "Updated to fix issue with Z"
❌ Refactoring notes: "Changed from approach A to approach B because..."

**REQUIRED COMMENT PATTERNS**:
✅ Current functionality: "Skip constructors and overridden methods"
✅ Business logic: "Javadoc is complete when all parameters and return values are documented"
✅ Technical rationale: "Expression context prevents statement-level transformation"
✅ Domain constraints: "Source position tracking requires precise integer arithmetic"

**PRINCIPLE**: Comments should describe WHAT the code does and WHY it works that way, never WHAT it used to do or HOW it changed. When comments become outdated, update them to accurately reflect current behavior.

## 📚 JAVADOC COMMENT REQUIREMENTS

**CRITICAL MANUAL PROCESS**: JavaDoc comments must NEVER be populated using automated scripts or template-based tools.

**MANDATORY APPROACH**:
✅ Read and understand the actual business logic being implemented
✅ Write JavaDoc that explains WHAT is being validated/implemented based on context
✅ Use meaningful descriptions that add value beyond the method name
✅ Each JavaDoc must be crafted individually based on the specific code's purpose

**PROHIBITED APPROACH**:
❌ Using scripts to bulk-add generic JavaDoc like "Test method" or "Validates X"
❌ Template-based JavaDoc generation without understanding business context
❌ Copy-paste JavaDoc patterns without adapting to specific use case
❌ Generic descriptions that could apply to any method

**EXAMPLE - BAD**:
```java
/**
 * Test method.
 */
@Test
public void canHandleWithJava25ReturnsTrue()
```

**EXAMPLE - GOOD**:
```java
/**
 * Verifies that FlexibleConstructorBodiesStrategy handles constructor bodies in Java 25 with LBRACE token.
 */
@Test
public void canHandleWithJava25ReturnsTrue()
```

**RATIONALE**: JavaDoc comments require understanding the business domain and technical context. Automated scripts cannot comprehend what a method actually does or why it matters, leading to meaningless documentation that provides no value to developers.

## 🧪 TESTNG TEST REQUIREMENTS

**CRITICAL THREAD SAFETY**: All tests must be thread-safe to support parallel execution.

**PROHIBITED PATTERNS**:
❌ `@BeforeMethod` - Creates mutable shared state between test methods
❌ `@AfterMethod` - Cleanup methods that assume sequential execution
❌ Mutable instance fields shared across test methods
❌ `assertThatThrownBy()` - Use TestNG's native exception handling instead

**REQUIRED PATTERNS**:
✅ `@Test(expectedExceptions = ExceptionType.class)` - For exception testing
✅ Local variables within each test method
✅ Static helper methods for test data creation
✅ Each test method is completely independent and self-contained

**LOOP INCREMENT STYLE**:
✅ Use `++i` for pre-increment (standard style)
❌ Do NOT use `i++` or `i += 1`

**EXAMPLE - BAD**:
```java
public class MyTest {
    private Configuration config;  // Shared mutable state

    @BeforeMethod
    public void setUp() {
        config = new Configuration();  // Not thread-safe
    }

    @Test
    public void testFeature() {
        assertThatThrownBy(() -> config.validate())
            .isInstanceOf(ValidationException.class);
    }
}
```

**EXAMPLE - GOOD**:
```java
public class MyTest {
    @Test(expectedExceptions = ValidationException.class)
    public void testFeatureThrowsValidationException() {
        Configuration config = new Configuration();  // Local state
        config.validate();
    }

    @Test
    public void testAnotherFeature() {
        Configuration config = createTestConfig();  // Independent
        assertThat(config.isValid()).isTrue();
    }

    private static Configuration createTestConfig() {
        return new Configuration();
    }
}
```

**RATIONALE**: TestNG runs tests in parallel by default. Using `@BeforeMethod` or mutable instance fields creates race conditions and non-deterministic test failures. Each test method must be completely independent.

## 🛠️ TOOL USAGE BEST PRACTICES

**CRITICAL ERROR PREVENTION**: Common tool usage patterns that cause failures and how to prevent them.

### File Editing with the Edit Tool

**PROBLEM**: "String to replace not found in file" errors due to whitespace character mismatches.

**ROOT CAUSE**: The Read tool displays output visually but doesn't distinguish between tabs and spaces. Copying text from Read output may preserve visual formatting but not actual characters.

**PREVENTION STRATEGIES**:

1. **Verify whitespace before editing**:
```bash
# Show exact whitespace characters (^I = tab, spaces shown as-is)
cat -A file.java | grep -A 2 "methodName"

# Show character codes (\t = tab)
od -c file.java | grep methodName

# Quick check for file's indentation style
head -20 file.java | cat -A | grep "^[[:space:]]" | head -3
```

2. **Use smallest unique strings**:
```java
// ❌ BAD: Include indentation (tabs vs spaces mismatch risk)
old_string: "\tpublic void methodName()"

// ✅ GOOD: Exclude leading whitespace
old_string: "public void methodName()"
```

3. **Alternative tools for whitespace-heavy edits**:
```bash
# Use sed for bulk replacements
sed -i 's/oldPattern/newPattern/g' file.java

# Use awk for complex transformations
awk '{gsub(/pattern/, "replacement")}1' file.java > temp && mv temp file.java
```

4. **Preview before editing**:
```bash
# Test regex patterns before applying
grep -n "pattern" file.java  # See what matches
sed -n 's/old/new/p' file.java  # Preview replacements without modifying
```

**CHECKLIST BEFORE USING EDIT TOOL**:
- [ ] Is the old_string unique in the file? (Use grep to verify)
- [ ] Does old_string contain leading/trailing whitespace? (Minimize if possible)
- [ ] Have I verified the actual whitespace characters? (Use cat -A if uncertain)
- [ ] Would sed/awk be safer for this edit? (Consider for bulk changes)

### Bash Command Execution

**PROBLEM**: "No such file or directory" errors when running executables like `./mvnw`.

**ROOT CAUSE**: Bash tool maintains working directory between invocations. Commands may be executed from unexpected directories.

**PREVENTION STRATEGIES**:

1. **Always use absolute paths for executables**:
```bash
# ❌ BAD: Relative path depends on current directory
./mvnw verify

# ✅ GOOD: Absolute path works from anywhere
/workspace/branches/task-name/code/mvnw verify
```

2. **Include directory change in same command**:
```bash
# ✅ GOOD: Change directory and run command atomically
cd /workspace/branches/task-name/code && ./mvnw verify

# ✅ GOOD: Multiple dependent commands
cd /workspace && ls -la && cat file.txt
```

3. **Verify location when uncertain**:
```bash
# Check current directory before running command
pwd && ./mvnw verify

# Or combine verification with execution
pwd && ls -la mvnw && ./mvnw verify
```

4. **Use full paths for files**:
```bash
# ❌ BAD: Relative path may fail if directory changed
cat README.md

# ✅ GOOD: Absolute path always works
cat /workspace/branches/task-name/code/README.md
```

**CHECKLIST BEFORE RUNNING BASH COMMANDS**:
- [ ] Am I using an absolute path OR including `cd` in the command?
- [ ] If using relative paths, do I know the current working directory?
- [ ] For multi-step commands, are they chained with `&&` (not separate invocations)?
- [ ] For path-dependent operations, have I verified the path exists?

### Pattern Matching and Replacement

**PROBLEM**: Over-broad pattern matching causes unintended replacements (e.g., replacing inside helper methods that use the same pattern).

**ROOT CAUSE**: Global search-and-replace without considering all possible match locations.

**PREVENTION STRATEGIES**:

1. **Preview all matches before replacing**:
```bash
# See all lines that would be affected
grep -n "pattern" file.java

# Preview replacements without modifying file
sed -n 's/old/new/p' file.java

# Count matches to verify expected number
grep -c "pattern" file.java
```

2. **Use line number ranges for targeted changes**:
```bash
# Only replace in specific line range
sed -i '100,200s/old/new/g' file.java

# Multiple specific lines
sed -i '50s/old/new/; 75s/old/new/; 120s/old/new/' file.java
```

3. **Use context-aware patterns**:
```bash
# Only replace in specific context
sed -i '/visitVariableDeclaration/,/^[[:space:]]*}/ s/recordIndentation/visitAndRecordOnly/' file.java

# Only replace when pattern appears with specific context
sed -i '/public.*visitMethod/s/recordIndentation/visitAndRecordOnly/' file.java
```

4. **Make patterns more specific**:
```bash
# ❌ BAD: Too broad, matches everywhere
sed -i 's/recordIndentation/visitAndRecordOnly/g' file.java

# ✅ GOOD: More specific pattern
sed -i 's/recordIndentation(node, depth);$/visitAndRecordOnly(node, depth);/g' file.java
```

5. **Test on copy first**:
```bash
# Test replacement on a copy
cp file.java file.java.backup
sed -i 's/pattern/replacement/g' file.java
diff file.java.backup file.java  # Review changes
```

**CHECKLIST BEFORE PATTERN REPLACEMENT**:
- [ ] Have I previewed all matches? (`grep -n` or `sed -n ...p`)
- [ ] Is the pattern specific enough to avoid unintended matches?
- [ ] Should I use line ranges to limit scope?
- [ ] Have I considered context-aware matching?
- [ ] For critical files, should I test on a copy first?

### General Tool Usage Principles

1. **READ FIRST, EDIT SECOND**: Always read a file before editing to understand its structure and whitespace style
2. **VERIFY BEFORE EXECUTING**: Use preview/dry-run options when available
3. **ABSOLUTE OVER RELATIVE**: Prefer absolute paths to eliminate directory confusion
4. **SPECIFIC OVER BROAD**: Make patterns as specific as possible to avoid unintended matches
5. **TEST BEFORE COMMIT**: For bulk operations, test on a subset or copy first

## Essential References

[docs/project/architecture.md](docs/project/architecture.md) - Project architecture and features
[docs/project/scope.md](docs/project/scope.md) - Family configuration and development philosophy
[docs/project/build-system.md](docs/project/build-system.md) - Build configuration and commands
[docs/project/git-workflow.md](docs/project/git-workflow.md) - Git workflows and commit squashing procedures
[docs/code-style-human.md](docs/code-style-human.md) - Code style master guide
[docs/code-style/](docs/code-style/) - Code style files (*-claude.md detection patterns, *-human.md explanations)

## File Organization

### Report Types and Lifecycle

**Stakeholder Reports** (`../` from code directory): 
- Temporary workflow artifacts for 7-phase task protocol
- Examples: `{task-name}-technical-architect-requirements.md`, `{task-name}-style-auditor-review.md`
- **Lifecycle**: Created during task execution, cleaned up with worktree in Phase 7
- **Purpose**: Process documentation for protocol compliance

**Empirical Studies** (`docs/studies/{topic}.md`):
- Temporary research cache for pending implementation tasks
- Examples: `docs/studies/claude-cli-interface.md`, `docs/studies/claude-startup-sequence.md`  
- **Lifecycle**: Persist until ALL dependent todo.md tasks consume them as input
- **Purpose**: Behavioral analysis and research studies based on empirical testing
- **Cleanup Rule**: Remove after all dependent tasks complete implementation

**Project Code**: Task code directory (`src/`, `pom.xml`, etc.)

### Report File Naming Convention
See **"MANDATORY OUTPUT REQUIREMENT"** patterns in [docs/project/task-protocol.md](docs/project/task-protocol.md) for exact agent report naming conventions by phase.

**Note**: The `../` path writes reports to `/workspace/branches/{task-name}/` (task root), not inside the code directory.