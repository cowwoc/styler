# Claude Code Configuration Guide

Styler Java Code Formatter project configuration and workflow guidance.

## 🚨 MANDATORY COMPLIANCE

**CRITICAL WORKFLOW**: [docs/project/task-protocol.md](docs/project/task-protocol.md) - MANDATORY risk-based protocol selection - Apply appropriate workflow based on file risk classification.
**CRITICAL SAFETY**: [docs/project/critical-rules.md](docs/project/critical-rules.md) - Build integrity and multi-instance coordination.
**CRITICAL STYLE**: Complete style validation = checkstyle + PMD + manual rules - See task-protocol.md
**CRITICAL PERSISTENCE**: [Long-term solution persistence](#-long-term-solution-persistence) - MANDATORY prioritization of optimal solutions over expedient alternatives.
**CRITICAL TASK COMPLETION**: Tasks are NOT complete until ALL 7 phases of task protocol are finished. Implementation completion does NOT equal task completion. Only mark tasks as complete after Phase 7 cleanup and finalization.
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
❌ "This edge case is too hard to handle properly" (without stakeholder consultation)
❌ "The existing pattern is suboptimal but I'll follow it" (without improvement attempt)

### 🚨 GIVING UP DETECTION PATTERNS

**CRITICAL FORBIDDEN PHRASES** - AUTOMATIC TASK CONTINUATION REQUIRED:
❌ "Given the complexity of properly implementing..."
❌ "Given the evidence that this requires significant changes..."
❌ "Let me focus on completing the task protocol instead..."
❌ "Let me focus on features that provide more immediate value..."
❌ "This would require significant architectural changes..."
❌ "Rather than diving deeper into this complex issue..."
❌ "Instead of implementing the full solution, let me..."
❌ "Due to the complexity, I'll defer this to..."
❌ "This appears to be beyond the current scope..."
❌ "Let me move on to easier tasks..."

**MANDATORY RESPONSE TO GIVING UP PATTERNS**:
✅ IMMEDIATELY return to the original technical problem
✅ Apply systematic debugging and decomposition approach
✅ Continue working on the exact issue that triggered the giving up pattern
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

## Repository Structure

**⚠️ NEVER** initialize new repositories  
**Repository Location**: `/workspace/branches/main/code/` (git repository and main development branch)  
**Tasks**: Task-specific worktrees (isolated per task-protocol.md)  
**Locks**: Multi-instance coordination via lock files

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