# Task Protocol

**CRITICAL**: This workflow applies to ALL tasks that create, modify, or delete files, with RISK-BASED PROTOCOL SELECTION for optimal efficiency

**TARGET AUDIENCE**: Claude AI instances executing tasks
**OPTIMIZATION**: Tool usage patterns, context preservation, violation prevention, efficiency via risk stratification

## RISK-BASED PROTOCOL SELECTION

**PROTOCOL SELECTION ENGINE:**
1. Extract file paths from modification request
2. Apply pattern matching against risk classifications
3. Select highest risk level if multiple files affected
4. Route to appropriate workflow variant

### HIGH-RISK FILES (Full 7-Phase Protocol Required)
**PATTERNS:**
- `src/**/*.java` (core implementation)
- `pom.xml`, `**/pom.xml` (build configuration)
- `.github/**` (CI/CD workflows)
- `**/security/**` (security components)
- `checkstyle.xml`, `**/checkstyle*.xml` (style enforcement)
- `CLAUDE.md` (critical configuration)
- `docs/project/task-protocol.md` (protocol configuration)
- `docs/project/critical-rules.md` (safety rules)

### MEDIUM-RISK FILES (Abbreviated Protocol: Phases 1,2,5,6,7)
**PATTERNS:**
- `src/test/**/*.java` (test files)
- `docs/code-style/**` (style documentation)
- `**/resources/**/*.properties` (configuration)
- `**/*Test.java`, `**/*Tests.java` (test classes)

### LOW-RISK FILES (Streamlined Protocol: Phases 1,2,7)
**PATTERNS:**
- `*.md` (except CLAUDE.md, task-protocol.md, critical-rules.md)
- `docs/**/*.md` (general documentation)
- `todo.md` (task tracking)
- `*.txt`, `*.log` (text files)
- `**/README*` (readme files)

### AUTOMATIC ESCALATION TRIGGERS
**File Content Analysis:**
- Cross-risk-tier dependencies detected → escalate to highest tier
- Security implications identified → force HIGH-RISK
- Build system impact detected → force HIGH-RISK
- Architectural pattern changes → force HIGH-RISK

**Task Description Keywords:**
- "security", "authentication", "authorization" → force HIGH-RISK
- "architecture", "breaking", "compatibility" → force HIGH-RISK
- "database", "schema", "migration" → force HIGH-RISK
- "api", "contract", "interface" → force HIGH-RISK
- "concurrent", "thread", "parallel", "sync" → force HIGH-RISK
- "performance", "cache", "memory", "optimization" → force HIGH-RISK
- "state", "singleton", "global", "shared" → force HIGH-RISK
- "dependency", "external", "integration" → force HIGH-RISK

**Change Scope Analysis:**
- Multiple file types affected → escalate to highest tier
- Cross-module changes detected → force HIGH-RISK
- Runtime behavior modifications → force MEDIUM-RISK minimum

**COMPATIBILITY**: Existing CLAUDE.md triggers preserved, default to FULL_PROTOCOL when risk unclear

## WORKFLOW VARIANTS

### FULL_PROTOCOL (High-Risk Files)
**Phases Executed**: 1, 2, 3, 4, 5, 6, 7
**Stakeholder Agents**: All agents based on task requirements
**Isolation**: Mandatory worktree isolation
**Review**: Complete stakeholder validation
**Use Case**: Core implementation, build configuration, security, CI/CD

### ABBREVIATED_PROTOCOL (Medium-Risk Files)
**Phases Executed**: 1, 2, 5, 6, 7
**Stakeholder Agents**: Based on change characteristics
- Base: technical-architect (always required)
- +style-auditor: If style/formatting files modified
- +security-auditor: If any configuration or resource files modified
- +performance-analyzer: If test performance or benchmarks affected
- +code-quality-auditor: Always included for code quality validation
**Isolation**: Worktree isolation for multi-file changes
**Review**: Domain-appropriate stakeholder validation
**Use Case**: Test files, style documentation, configuration files

### STREAMLINED_PROTOCOL (Low-Risk Files)
**Phases Executed**: 1, 2, 7
**Stakeholder Agents**: None (unless escalation triggered)
**Isolation**: Required for multi-file changes, optional for single documentation file
**Review**: Evidence-based validation and automated checks
**Safety Gates**:
- Verify no cross-references to modified files in src/
- Confirm no build configuration impact
- Validate no security-sensitive content changes
**Use Case**: Documentation updates, todo.md, README files

### MANUAL OVERRIDES
**Force Full Protocol**: `--force-full-protocol` flag for critical changes
**Explicit Risk Level**: `--risk-level=HIGH|MEDIUM|LOW` to override classification
**Escalation Keywords**: "security", "architecture", "breaking" in task description

### RISK ASSESSMENT AUDIT TRAIL
**Required Logging**:
- Risk level selected (HIGH/MEDIUM/LOW)
- Classification method (pattern match, keyword trigger, manual override)
- Escalation triggers activated (if any)
- Workflow variant executed (FULL/ABBREVIATED/STREAMLINED)
- Agent set selected for review
- Final outcome (approved/rejected/deferred)

**Implementation**: Log in TodoWrite tool and task commit messages for audit purposes

## 🧠 PROTOCOL INTERPRETATION MODE

**THINK HARDER MODE**: Parent agent must apply enhanced analytical rigor when interpreting and following the task protocol workflow. Rather than following obvious or surface-level interpretations, carefully analyze what the protocol truly requires for the specific task context.

## BATCH PROCESSING RESTRICTIONS

**PROHIBITED PATTERNS:**
- Processing multiple tasks sequentially without individual protocol execution
- "Work on all Phase 1 tasks until done" - Must select ONE specific task
- "Complete these 5 tasks" - Each requires separate lock acquisition and worktree
- Assuming research tasks can bypass protocol because they create "only" study files

**MANDATORY SINGLE-TASK PROCESSING:**
1. Select ONE specific task from todo.md (e.g., "study-claude-cli-interface")
2. Acquire atomic lock for THAT specific task only
3. Create isolated worktree for THAT task only
4. Execute full 7-phase protocol for THAT task only
5. Complete Phase 7 cleanup before starting any other task

**CORRECT TASK SELECTION EXAMPLES:**
✅ "Work on study-claude-cli-interface task"
✅ "Execute implement-read-tool task"
❌ "Work on all Phase 1 analysis tasks"
❌ "Complete the research tasks until done"

### HANDLING BATCH REQUESTS - AUTOMATIC CONTINUOUS MODE TRANSLATION

**When users request batch operations (e.g., "Work on all Phase 1 tasks until done"):**

**AUTOMATIC TRANSLATION PROTOCOL:**
1. **ACKNOWLEDGE**: "I understand you want to work on multiple tasks efficiently..."
2. **AUTO-TRANSLATE**: "I'll interpret this as a request to work on the todo list in continuous mode, processing each task with full protocol isolation..."
3. **EXECUTE**: Automatically trigger continuous workflow mode without requiring user to rephrase their request

**BATCH REQUEST PATTERNS TO AUTO-TRANSLATE:**
- "Work on all [phase/type] tasks"
- "Complete these tasks until done"  
- "Process the todo list"
- "Work on multiple tasks"
- Any request mentioning multiple specific tasks

**TRANSLATION IMPLEMENTATION:**
```
USER REQUEST: "Work on all Phase 1 analysis tasks until done"
AUTO-TRANSLATION: Interpret as "Work on the todo list in continuous mode"
EXECUTION: Begin continuous workflow mode with appropriate task filtering
```

**TASK FILTERING FOR CONTINUOUS MODE:**
When batch requests specify subsets (e.g., "Phase 1 tasks"), apply filtering in continuous mode:
1. **Phase-based filtering**: Process only tasks in specified phases
2. **Type-based filtering**: Process only tasks matching specified types (ANALYZE, IMPLEMENT, etc.)
3. **Name-based filtering**: Process only specifically mentioned task names
4. **Default behavior**: Process all available tasks if no specific filter mentioned

**CONTINUOUS WORKFLOW MODE REFERENCE:**
Batch requests are seamlessly translated to continuous workflow mode, maintaining protocol safety while providing the multi-task processing experience users expect.

## VIOLATION PREVENTION PATTERNS

### Pattern: Pre-Task Validation Block
```
BEFORE ANY TASK: Execute this exact tool sequence:
1. Bash: export SESSION_ID="${CLAUDE_SESSION_ID:-$(whoami)-$$}"  
2. Bash: pwd (verify location)
3. Read: todo.md (extract exact task name)
4. Grep: task name in todo.md (verify existence)
VIOLATION CHECK: All commands must succeed
```

### Pattern: Comprehensive Agent Selection Decision Tree
```
INPUT: Task description
AVAILABLE AGENTS: technical-architect, usability-reviewer, performance-analyzer, security-auditor, style-auditor, code-quality-auditor, code-tester, build-validator

PROCESSING:
🚨 CORE AGENTS (Always Required):
  technical-architect: MANDATORY for ALL file modification tasks (provides implementation requirements)

🔍 FUNCTIONAL AGENTS (Code Implementation):
  IF NEW CODE created: add style-auditor, code-quality-auditor, build-validator
  IF IMPLEMENTATION (not just config): add code-tester
  IF MAJOR FEATURES completed: add usability-reviewer (MANDATORY after completion)

🛡️ SECURITY AGENTS (Actual Security Concerns):
  IF AUTHENTICATION/AUTHORIZATION changes: add security-auditor
  IF EXTERNAL API/DATA integration: add security-auditor
  IF ENCRYPTION/CRYPTOGRAPHIC operations: add security-auditor
  IF INPUT VALIDATION/SANITIZATION: add security-auditor

⚡ PERFORMANCE AGENTS (Performance Critical):
  IF ALGORITHM optimization tasks: add performance-analyzer
  IF DATABASE/QUERY optimization: add performance-analyzer
  IF MEMORY/CPU intensive operations: add performance-analyzer

🔧 FORMATTING AGENTS (Code Quality):
  IF PARSER LOGIC modified: add performance-analyzer, security-auditor
  IF AST TRANSFORMATION changed: add code-quality-auditor, code-tester
  IF FORMATTING RULES affected: add style-auditor

❌ AGENTS NOT NEEDED FOR SIMPLE OPERATIONS:
  - Maven module renames: NO performance-analyzer
  - Configuration file updates: NO security-auditor unless changing auth
  - Directory/file renames: NO performance-analyzer
  - Documentation updates: Usually only technical-architect
  
📊 ANALYSIS AGENTS (Research/Study Tasks):
  IF task involves ARCHITECTURAL ANALYSIS: add technical-architect
  IF task involves PERFORMANCE ANALYSIS: add performance-analyzer  
  IF task involves UX/INTERFACE ANALYSIS: add usability-reviewer
  IF task involves SECURITY ANALYSIS: add security-auditor
  IF task involves CODE QUALITY REVIEW: add code-quality-auditor
  IF task involves PARSER/FORMATTER PERFORMANCE ANALYSIS: add performance-analyzer
OUTPUT: Exact agent list for Task tool calls

🚨 CRITICAL RULES:
- style-auditor is MANDATORY for any task creating or modifying source code files
- performance-analyzer is MANDATORY for any task involving parser/formatter optimization
- ALL EIGHT AGENTS available for selection based on task requirements
- NO agent should be excluded from consideration without task-specific justification

SPECIAL AGENT USAGE PATTERNS:
- style-auditor: ALWAYS include for NEW CODE tasks (style requirements inform architecture)
- style-auditor: Execute in Phases 1 (requirements), 3, 4, 6 (implementation/test review)  
- style-auditor: Apply ALL manual style guide rules from docs/code-style/ (Java, common, and any language-specific patterns)
- build-validator: For style/formatting tasks, triggers linters (checkstyle, PMD, ESLint) through build system
- build-validator: Use alongside style-auditor to ensure comprehensive validation (automated + manual rules)
- code-quality-auditor: Post-implementation refactoring and best practices enforcement
- code-tester: Business logic validation and comprehensive test creation
- security-auditor: Data handling and storage compliance review
- performance-analyzer: Algorithmic efficiency and resource optimization
- security-auditor: Vulnerability assessment and security boundary verification
- usability-reviewer: User experience design and interface evaluation  
- technical-architect: System architecture and implementation guidance

AGENT SELECTION VERIFICATION CHECKLIST:
- [ ] NEW CODE task → style-auditor included?
- [ ] Source files created/modified → build-validator included?
- [ ] Performance-critical code → performance-analyzer included?
- [ ] Security-sensitive features → security-auditor included?
- [ ] User-facing interfaces → usability-reviewer included?
- [ ] Post-implementation refactoring → code-quality-auditor included?
- [ ] AST parsing/code formatting → performance-analyzer included?
```

### Pattern: Atomic Lock Acquisition
```
BASH COMMAND SEQUENCE (must execute as single command):
mkdir -p ../../../locks; (set -C; echo '{"session_id": "'${SESSION_ID}'", "start_time": "'$(date '+%Y-%m-%d %H:%M:%S %Z')'"}' > ../../../locks/{task-name}.json) 2>/dev/null && echo "LOCK_SUCCESS" || echo "LOCK_FAILED"

VIOLATION CHECK: Output must contain "LOCK_SUCCESS"
IF "LOCK_FAILED": Determine failure cause and respond appropriately:
  - IF another instance holds lock (check ../../../locks/{task-name}.json exists): Select different available task
  - IF permission/I/O error (mkdir or echo command fails): ABORT task execution - system-level issue requires manual intervention
```

### Pattern: Worktree Isolation Setup  
```
BASH COMMAND SEQUENCE:
cd /workspace/branches/main/code
git worktree add -b {task-name} /workspace/branches/{task-name}/code 2>/dev/null && cd /workspace/branches/{task-name}/code && pwd

VIOLATION CHECK: pwd output must match /workspace/branches/{task-name}/code
IF mismatch: TERMINATE task

CRITICAL NOTE: Worktrees MUST be created as siblings to main branch, NOT nested within main/code/
This ensures proper isolation and prevents embedded repository issues during multi-instance coordination.
```

### Pattern: Mandatory Context.md Creation
```
REQUIRED BEFORE ANY STAKEHOLDER CONSULTATION:

BASH COMMAND: Create context.md in task root directory
Write: ../context.md with following mandatory content:

---
# Task Context: {task-name}

## Task Objective
{task-description}

## Scope Definition
**FILES IN SCOPE:**
- [List exact files/directories that stakeholder agents are authorized to analyze]
- [Example: parser/src/main/java/io/github/cowwoc/styler/parser/]
- [Example: parser/src/test/java/io/github/cowwoc/styler/parser/test/]

**FILES OUT OF SCOPE:**
- [List directories/files that are explicitly excluded from analysis]
- [Example: All files outside the parser/ directory]
- [Example: Broader project infrastructure, lexer modules, etc.]

## Stakeholder Agent Reports
**Phase 1 Requirements:**
- technical-architect-requirements.md (when completed)
- [other-agent]-requirements.md (when completed)

**Implementation Reviews:**
- technical-architect-review1.md (when completed)
- [other-agent]-review1.md (when completed)

## Implementation Status
- [ ] Phase 1: Requirements Analysis
- [ ] Phase 2: Requirements Synthesis
- [ ] Phase 3: Implementation
- [ ] Phase 4: Testing
- [ ] Phase 5: Issue Resolution (if needed)
- [ ] Phase 6: Final Review
- [ ] Phase 7: Cleanup

## Current Focus
[Current phase and specific work being performed]
---

VIOLATION CHECK: context.md must exist before invoking any stakeholder agents
IF context.md missing: HALT workflow, create context.md first
PURPOSE: Ensures all agents operate within consistent, documented scope boundaries
```

### Pattern: Phase 1 Parallel Agent Execution
```
TOOL CALL STRUCTURE (single message, multiple invocations):
<invoke name="Task">
<parameter name="subagent_type">technical-architect</parameter>
<parameter name="description">Requirements analysis for {task}</parameter>
<parameter name="prompt">Task: {task-description}\nMode: REQUIREMENTS_ANALYSIS\nPrimary Focus: architectural decisions\nSuccess Criteria: design approach validation\nToken Budget: 800 tokens\nScope Limitation: architecture only, not implementation

CRITICAL SCOPE ENFORCEMENT: ONLY analyze files explicitly listed in ../context.md scope section. ABSOLUTELY FORBIDDEN to scan files outside context.md scope. STOP IMMEDIATELY if attempting to access unauthorized files.

CRITICAL PERSISTENCE REQUIREMENT: Follow CLAUDE.md "Long-term Solution Persistence" principles. Prioritize optimal solutions over expedient alternatives. Apply "Solution Quality Hierarchy" and reject incomplete implementations.

MANDATORY OUTPUT REQUIREMENT: Provide your complete analysis report in your response. The parent agent will write this report to ../{agent-name}-requirements.md file.</parameter>
</invoke>
<invoke name="Task">
<parameter name="subagent_type">performance-analyzer</parameter>
<parameter name="description">Performance requirements analysis</parameter>
<parameter name="prompt">Task: {task-description}\nMode: REQUIREMENTS_ANALYSIS\nPrimary Focus: performance characteristics\nSuccess Criteria: performance requirements\nToken Budget: 600 tokens\nScope Limitation: performance only, not implementation

MANDATORY OUTPUT REQUIREMENT: Provide your complete analysis report in your response. The parent agent will write this report to ../{agent-name}-requirements.md file.</parameter>
</invoke>

VIOLATION CHECK: All agents must return completion status AND provide complete reports
IF any agent fails: HALT workflow
IF any agent doesn't provide complete report: Re-invoke with report requirement
PARENT AGENT RESPONSIBILITY: Write all agent reports to designated files after collection

AGENT REPORT NAMING CONVENTION:
- Phase 1 (Requirements): ../{agent-name}-requirements.md
- Phase 3 (Implementation Review): ../{agent-name}-review1.md  
- Phase 4 (Test Review): ../{agent-name}-review2.md
- Phase 6 (Final System Review): ../{agent-name}-review3.md
- Additional rounds increment the number (review4.md, review5.md, etc.)

EXAMPLES:
- technical-architect requirements: ../technical-architect-requirements.md
- style-auditor first review: ../style-auditor-review1.md
- security-auditor final review: ../security-auditor-review3.md
```

### Pattern: Phase 2 Requirements Synthesis
```
MANDATORY AFTER Phase 1 completion:
1. Read: All Phase 1 agent reports (stakeholder-requirements.md, performance-requirements.md, etc.)
2. CONFLICT RESOLUTION: Identify competing requirements between domains
3. ARCHITECTURE PLANNING: Design approach satisfying all constraints
4. REQUIREMENT MAPPING: Document how each stakeholder requirement is addressed
5. TRADE-OFF ANALYSIS: Record decisions and compromises

IMPLEMENTATION EXECUTION:
- Write complete, functional code addressing all requirements
- Document design decisions and rationale
- Follow established project patterns
- Edit: todo.md (mark task complete) - MUST be included in same commit as task deliverables

VIOLATION CHECK: All Phase 1 requirements addressed or exceptions documented

⚠️ CRITICAL COMMIT PATTERN VIOLATION: Never create separate commits for todo.md updates
MANDATORY: todo.md task completion update MUST be committed with task deliverables in single atomic commit
ANTI-PATTERN: git commit deliverables → git commit todo.md (VIOLATES PROTOCOL)
CORRECT PATTERN: git add deliverables todo.md → git commit (single atomic commit)
```

### Pattern: Temporary File Management Setup
```
BEFORE IMPLEMENTATION BEGINS (Mandatory for all tasks):
1. TEMP_DIRECTORY_CREATION: Set up isolated temporary file space
   - Bash: TASK_NAME=$(basename $(dirname $(pwd)))
   - Bash: TEMP_DIR="/tmp/task-${TASK_NAME}-$(date +%s)-$$" && mkdir -p "$TEMP_DIR"
   - Bash: echo "$TEMP_DIR" > .temp_dir && echo "TEMP_SETUP_SUCCESS: $TEMP_DIR"
   - PURPOSE: All agents use consistent temporary file location outside git repository
   - USAGE: Agents read temp directory with: TEMP_DIR=$(cat .temp_dir 2>/dev/null || echo "/tmp/fallback-$$")

VIOLATION CHECK: Output must contain "TEMP_SETUP_SUCCESS"
IF temp directory creation fails: Use task directory as fallback, document limitation

AGENT INTEGRATION: All agents should use temporary directory for:
- Analysis scripts and automation tools
- Performance benchmarking artifacts  
- Security testing payloads and samples
- Debug logs and intermediate processing files
- Generated test data and mock objects
```

### Pattern: Phase 3 Dual Track Validation
```
PARALLEL EXECUTION (two simultaneous tracks):

TRACK A - Implementation Review:
<invoke name="Task">
<parameter name="subagent_type">technical-architect</parameter>
<parameter name="description">Review implementation against Phase 1 requirements</parameter>
<parameter name="prompt">Task: {task-description}\nMode: IMPLEMENTATION_REVIEW\nYour Phase 1 Requirements: {link-to-phase1-report}\nImplementation Files: {modified-files}\nInstructions: Review implementation against YOUR Phase 1 requirements.

CRITICAL SCOPE ENFORCEMENT: ONLY analyze files explicitly listed in ../context.md scope section. ABSOLUTELY FORBIDDEN to scan files outside context.md scope. STOP IMMEDIATELY if attempting to access unauthorized files.

CRITICAL PERSISTENCE ENFORCEMENT: Apply CLAUDE.md "Stakeholder Agent Persistence Enforcement" standards. Validate architectural COMPLETENESS, not just basic functionality. Use "Mandatory Rejection Criteria" - reject incomplete implementations, suboptimal algorithms when better solutions are feasible, and partial compliance when full compliance is achievable.

MANDATORY OUTPUT REQUIREMENT: Provide your complete review report in your response. The parent agent will write this report to ../{agent-name}-review{round}.md file.

MANDATORY FINAL DECISION: Must end response with exactly one of:
- "FINAL DECISION: ✅ APPROVED - Implementation meets all requirements"
- "FINAL DECISION: ❌ REJECTED - [specific issues] require resolution"</parameter>
</invoke>
<invoke name="Task">
<parameter name="subagent_type">style-auditor</parameter>
<parameter name="description">Review code style compliance</parameter>
<parameter name="prompt">Task: {task-description}\nMode: STYLE_COMPLIANCE_REVIEW\nImplementation Files: {modified-files}\nInstructions: Apply ALL manual style guide rules from docs/code-style/. Check Java, common, and language-specific patterns.\n\nMANDATORY OUTPUT REQUIREMENT: Provide your complete style review report in your response. The parent agent will write this report to ../{agent-name}-review{round}.md file.\n\nMANDATORY FINAL DECISION: Must end response with exactly one of:\n- "FINAL DECISION: ✅ APPROVED - All style requirements satisfied"\n- "FINAL DECISION: ❌ REJECTED - [violation count] violations require fixes"</parameter>
</invoke>
[Repeat for all selected agents in parallel]

TRACK B - Test Creation:
<invoke name="Task">
<parameter name="subagent_type">code-tester</parameter>
<parameter name="description">Create comprehensive test suite</parameter>
<parameter name="prompt">Task: {task-description}\nMode: TEST_CREATION\nImplementation Files: {modified-files}\nInstructions: Create comprehensive test suite covering all business logic, edge cases, and stakeholder requirements.

MANDATORY OUTPUT REQUIREMENT: Provide your complete test creation report in your response. The parent agent will write this report to ../{agent-name}-review{round}.md file.</parameter>
</invoke>

VIOLATION CHECK: All agents must end with "FINAL DECISION: ✅ APPROVED" or "FINAL DECISION: ❌ REJECTED"\nIF any agent response lacks mandatory format: TERMINATE task, restart from Phase 1
```

### Pattern: Phase 4 Test-Only Review
```
PARALLEL EXECUTION - All Phase 1 agents review TEST CODE only:
<invoke name="Task">
<parameter name="subagent_type">technical-architect</parameter>
<parameter name="description">Review test quality and coverage</parameter>
<parameter name="prompt">Task: {task-description}\nMode: TEST_REVIEW\nTest Files: {test-files}\nInstructions: Review ONLY test code quality and domain coverage. Verify tests validate your Phase 1 requirements.

CRITICAL SCOPE ENFORCEMENT: ONLY analyze files explicitly listed in ../context.md scope section. ABSOLUTELY FORBIDDEN to scan files outside context.md scope. STOP IMMEDIATELY if attempting to access unauthorized files.

MANDATORY OUTPUT REQUIREMENT: Provide your complete test review report in your response. The parent agent will write this report to ../{agent-name}-review{round}.md file.

MANDATORY FINAL DECISION: Must end response with exactly one of:\n- "FINAL DECISION: ✅ APPROVED - Tests adequately cover all requirements"\n- "FINAL DECISION: ❌ REJECTED - [specific gaps] in test coverage"</parameter>
</invoke>
[Repeat for all Phase 1 agents in parallel]

VIOLATION CHECK: All agents must end with "FINAL DECISION: ✅ APPROVED"\nIF any agent lacks mandatory format or shows REJECTED: Execute Phase 5
```

### Pattern: Phase 3-5 Execution Condition (Conditional)
```
TRIGGER CONDITION:
IF (source code files modified) OR (runtime behavior changes expected):
  Execute Phase 3: Dual Track Validation
  Execute Phase 4: Test-Only Review
  IF (Any Phase 3 agent == "REJECTED") OR (Any Phase 4 agent == "REJECTED"):
    Execute Phase 5: Issue Resolution
    Return to Phase 6 after resolution
  ELSE:
    Skip to Phase 6
ELSE:
  Skip directly to Phase 6 with build validation confirmation
  
EXAMPLES OF PHASE 3-5 SKIP CONDITIONS:
- Maven dependency additions (configuration only)
- Build plugin configuration changes
- Documentation updates
- Property file modifications
- Version bumps without code changes

EXAMPLES REQUIRING PHASE 3-5 EXECUTION:
- New Java classes or methods
- Modified algorithms or business logic  
- Database schema changes
- API endpoint modifications
- Configuration affecting runtime behavior
```

### Pattern: Phase 5 Issue Resolution (Conditional - Only if Phase 3-4 Executed)
```
TRIGGER CONDITION:
IF (Any Phase 3 agent == "REJECTED") OR (Any Phase 4 agent == "REJECTED"):
  Execute Phase 5
ELSE:
  Skip to Phase 6
  
NOTE: Phase 5 only applies when Phases 3-4 were executed. Configuration-only tasks skip directly from Phase 2 to Phase 6.

MANDATORY SCOPE ASSESSMENT:
1. Analyze rejection feedback complexity and effort estimation
2. APPLY PERSISTENCE EVALUATION: Use CLAUDE.md "Enhanced Scope Assessment" - evaluate if complexity is genuine or just requiring more effort, assess learning curve value, technical debt cost, and stakeholder value of optimal solution
3. IF estimated resolution effort > 2x original task scope AND persistence evaluation shows genuine scope mismatch:
   Execute Scope Negotiation Protocol
4. ELSE:
   Execute Standard Issue Resolution with persistence focus

SCOPE NEGOTIATION PROTOCOL:
When resolution appears to require extensive work beyond core task scope:

1. CATEGORIZE ISSUES by domain authority:
   - BLOCKING ISSUES (must resolve):
     * Compilation/build failures (build-validator authority)
     * Security vulnerabilities (security-auditor absolute veto)
     * Privacy violations (security-auditor absolute veto)
     * Architectural incompleteness (technical-architect authority)
     * Critical performance issues (performance-analyzer authority)
   - DEFERRABLE ISSUES (can todo.md):
     * Style violations (style-auditor can defer if functionality preserved)
     * Documentation gaps (code-quality-auditor can defer if basic quality maintained)
     * Test coverage improvements (code-tester decides adequacy)
     * UX enhancements (usability-reviewer can defer if core functionality accessible)
   - NEGOTIABLE ISSUES (requires domain authority consultation):
     * Performance optimization requirements
     * Implementation completeness scope
     * Testing strategy adequacy
     * Code quality standards application

2. DOMAIN AUTHORITY ASSIGNMENTS:
   - build-validator: Final authority on "can this be built/deployed safely?"
   - security-auditor: Absolute veto on security vulnerabilities and attack vectors
   - technical-architect: Final authority on "does this meet architectural requirements?"
   - style-auditor: Can defer style issues to todo.md if core functionality preserved
   - code-quality-auditor: Can defer documentation/testing to todo.md if basic quality maintained
   - security-auditor: Absolute veto on data privacy and personal information handling violations
   - performance-analyzer: Final authority on performance requirements and scalability concerns
   - code-tester: Final authority on test coverage adequacy for business logic validation
   - usability-reviewer: Can defer UX improvements to todo.md if core functionality accessible

3. SECURITY AUDITOR CONFIGURATION:
   **CRITICAL: Use Project-Specific Security Model from scope.md**

   For Parser Implementation Tasks:
   - **Attack Model**: Single-user code formatting scenarios (see docs/project/scope.md)
   - **Security Focus**: Resource exhaustion prevention, system stability
   - **NOT in Scope**: Information disclosure, data exfiltration, multi-user attacks
   - **Usability Priority**: Error messages should prioritize debugging assistance
   - **Appropriate Limits**: Reasonable protection for legitimate code formatting use cases

   **MANDATORY**: security-auditor MUST reference docs/project/scope.md "Security Model for Parser Operations" before conducting any parser security review.

4. AUTHORITY HIERARCHY FOR DOMAIN CONFLICTS:
   When agent authorities overlap or conflict:
   - Security/Privacy conflicts: Joint veto power (both security-auditor AND security-auditor must approve)
   - Architecture/Performance conflicts: technical-architect decides with performance-analyzer input
   - Tax/Security conflicts: Both have absolute veto (highest safety standard applies)
   - Tax/Privacy conflicts: Both have absolute veto (highest compliance standard applies)
   - Testing/Quality conflicts: code-tester decides coverage adequacy, code-quality-auditor decides standards
   - Build/Architecture conflicts: build-validator has final authority (deployment safety priority)
   - Usability/Architecture conflicts: technical-architect decides with usability-reviewer input
   - Style/Quality conflicts: code-quality-auditor decides (quality encompasses style)

4. SCOPE NEGOTIATION EXECUTION:
   Invoke scope assessment with rejecting agents:
   <invoke name="Task">
   <parameter name="subagent_type">{rejecting-agent}</parameter>
   <parameter name="description">Scope negotiation for {task}</parameter>
   <parameter name="prompt">Task: {task-description}
   Mode: SCOPE_NEGOTIATION
   Rejection Feedback: {your-previous-rejection-feedback}
   Core Task Scope: {original-task-definition}

   CRITICAL PERSISTENCE REQUIREMENTS: Apply CLAUDE.md "Deferral Justification Requirements" and "Prohibited Deferral Reasons". Only defer if work genuinely extends beyond task boundaries, requires unavailable expertise, blocked by external factors, or genuinely exceeds reasonable allocation. NEVER defer because "this is harder than expected" or "the easy solution works fine".

   SCOPE DECISION REQUEST: Given the original task scope, classify your rejected items:
   - BLOCKING (must resolve now): Issues that prevent core functionality OR compromise long-term solution quality within achievable scope
   - DEFERRABLE (can add to todo.md): Issues that enhance but don't block core task AND genuinely exceed reasonable task boundaries

   MANDATORY RESPONSE FORMAT:
   BLOCKING ISSUES: [list specific items that absolutely must be resolved]
   DEFERRABLE ISSUES: [list items that can become follow-up tasks]
   PERSISTENCE JUSTIFICATION: [explain why deferrable items genuinely exceed scope vs. requiring more effort]
   SCOPE DECISION: DEFER or RESOLVE_NOW

   If DEFER: Provide exact todo.md task entries for deferred work
   If RESOLVE_NOW: Confirm all issues must be resolved in current task</parameter>
   </invoke>

5. SCOPE DECISION CONSOLIDATION:
   - IF ALL rejecting agents choose DEFER: Add deferred tasks to todo.md, proceed to modified Phase 6
   - IF ANY agent chooses RESOLVE_NOW for BLOCKING issues: Execute full resolution
   - IF mixed decisions: Domain authority agents make final call per their expertise area

STANDARD ISSUE RESOLUTION:
TRACK A - Implementation Updates:
1. Collect all ❌ REJECTED feedback from Phase 3
2. Priority analysis by severity and domain
3. Apply fixes addressing all stakeholder concerns
4. Verify fixes don't introduce new issues

TRACK B - Test Updates:
1. Collect all ❌ REJECTED feedback from Phase 4
2. Add missing test scenarios per domain feedback
3. Improve test quality per stakeholder recommendations
4. Ensure enhanced tests maintain passing status

VIOLATION CHECK: All identified issues from Phase 3 and 4 resolved OR properly deferred to todo.md
```

### Pattern: Phase 6 Complete System Review
```
FINAL PARALLEL VALIDATION - All Phase 1 agents review integrated system:
<invoke name="Task">
<parameter name="subagent_type">technical-architect</parameter>
<parameter name="description">Final system validation</parameter>
<parameter name="prompt">Task: {task-description}\nMode: COMPLETE_SYSTEM_REVIEW\nAll Files: {implementation-and-test-files}\nPhase 5 Updates: {summary-of-changes}\nInstructions: Review implementation + tests as integrated system.

CRITICAL PERSISTENCE VALIDATION: Apply CLAUDE.md "Solution Quality Indicators" and "Persistence Validation Checklist". Verify solution addresses root cause, follows best practices, includes comprehensive error handling, and provides long-term value. Reject solutions that are merely functional if optimal solutions were achievable within scope.

MANDATORY OUTPUT REQUIREMENT: Provide your complete final system review report in your response. The parent agent will write this report to ../{agent-name}-review{round}.md file.

MANDATORY FINAL DECISION: Must end response with exactly one of:
- "FINAL DECISION: ✅ APPROVED - All requirements satisfied"
- "FINAL DECISION: ❌ REJECTED - [specific issues] require resolution"

CRITICAL: No other conclusion format accepted. This decision determines workflow continuation.</parameter>
</invoke>
[Repeat for all Phase 1 agents in parallel]

DECISION LOGIC:
IF all agents end with "FINAL DECISION: ✅ APPROVED": Task Complete
ELSE: Return to Phase 5 with new issue list, then re-execute Phase 6

VIOLATION CHECK: Agent response must contain "FINAL DECISION: ✅ APPROVED"
IF missing or contains "❌ REJECTED": MANDATORY Phase 5 execution
IF ambiguous response format: TERMINATE task, restart from Phase 1

🚨 CRITICAL ENFORCEMENT: NO HUMAN OVERRIDE PERMITTED
PROHIBITED BYPASS PATTERNS:
❌ "Considering MVP scope, proceeding despite rejections"
❌ "Issues are enhancement-level, not blocking"  
❌ "Critical security fixed, finalizing task"
❌ "Privacy concerns addressed sufficiently"

MANDATORY RESPONSE TO ANY ❌ REJECTED:
✅ "Agent X returned ❌ REJECTED, executing mandatory Phase 5 resolution"
✅ "Cannot proceed to Phase 7 until ALL agents return ✅ APPROVED"
✅ "Re-executing Phase 6 after addressing stakeholder concerns"

⚠️ SCOPE NEGOTIATION TRIGGER:
IF resolution effort appears > 2x original task scope:
✅ "Multiple agents rejected with extensive scope - executing scope negotiation protocol"
✅ "Estimated resolution effort exceeds core task boundary - consulting domain authorities"
✅ "Invoking scope assessment to determine BLOCKING vs DEFERRABLE issues"
```

### Pattern: Implementation Safety Guards
```
BEFORE ANY Write/Edit/MultiEdit OPERATION:
1. Bash: pwd (must output /workspace/branches/{task-name}/code)
2. Bash: git status --porcelain | grep -E "(dist/|node_modules/|target/|\.jar$|\.temp_dir$)" (must be empty or only .temp_dir)
3. IF checks fail: ABORT file operations

AFTER ANY file creation:
4. Bash: git status (verify expected files only, .temp_dir acceptable)
5. IF unexpected files: INVESTIGATE before proceeding
```

### Pattern: Build Validation Gate
```
MANDATORY AFTER IMPLEMENTATION:
Bash: ./mvnw clean compile test -q
VIOLATION CHECK: Exit code must be 0
IF non-zero exit: Implementation FAILED, return to Phase 2
CONTINUE only after BUILD SUCCESS

MANDATORY FOR TASK COMPLETION:
Bash: ./mvnw clean verify -q
VIOLATION CHECK: Exit code must be 0 (full build success including style, tests, packaging)
IF non-zero exit: Task CANNOT be marked complete - must resolve ALL build issues
TASK COMPLETION BLOCKED until "mvnw verify" returns success

🚨 CRITICAL BUILD INTEGRITY RULE:
NO task can be marked as completed if "mvnw verify" fails
NO commits to main branch if build verification fails
NO Phase 7 cleanup until build passes completely
```

### Pattern: Git Safety Sequence
```
MANDATORY LINEAR HISTORY REQUIREMENT:
All task completion must result in linear commit history on main branch.

BEFORE ANY GIT COMMIT:
1. Bash: rm -f .temp_dir (remove temporary directory reference before commit)
2. Bash: git status --porcelain | grep -E "(dist/|node_modules/|target/|\.jar$|\.temp_dir$)" (must be empty)
3. Bash: git rebase main
4. Bash: cd /workspace/branches/main/code && git merge --ff-only {task-name}

ALTERNATIVE LINEAR MERGE PATTERN (if rebase not feasible):
1. cd /workspace/branches/main/code
2. git cherry-pick {task-commit-sha}  # Direct linear application
3. git branch -D {task-name}         # Clean up task branch

VIOLATION CHECK: Each command must succeed
IF any command fails: Git workflow VIOLATED, investigate before proceeding

⚠️ CRITICAL: Final history must be linear - no merge commits allowed on main branch
ANTI-PATTERN: git merge --no-ff (creates merge commits)
CORRECT PATTERN: --ff-only or cherry-pick for linear history
```

### Pattern: Phase 7 - Lock Release and Cleanup
```
MANDATORY WORK PRESERVATION REQUIREMENT:
Phase 7 can ONLY execute after successful work preservation. ALL changes must be committed and merged to main branch before cleanup.

PREREQUISITE VERIFICATION:
1. Verify Phase 6 reached ✅ APPROVED consensus OR scope negotiation completed with work properly deferred to todo.md
2. Confirm all deliverables committed to task branch
3. Confirm task branch merged to main branch via Git Safety Sequence

🚨 CRITICAL WORK PRESERVATION CHECK:
BEFORE any cleanup operations, execute verification:
- Bash: cd /workspace/branches/main/code && git log --oneline -5
- Bash: git diff main {task-name} --stat
- IF any differences exist: ABORT cleanup, execute Git Safety Sequence first

FINAL CLEANUP SEQUENCE (Only after work preservation verified):
1. Bash: rm -f ../../../locks/{task-name}.json
2. TEMPORARY_DIRECTORY_CLEANUP: Remove isolated temporary files
   - Bash: TEMP_DIR=$(cat .temp_dir 2>/dev/null) && [ -n "$TEMP_DIR" ] && [ -d "$TEMP_DIR" ] && rm -rf "$TEMP_DIR" && echo "TEMP_CLEANUP_SUCCESS" || echo "TEMP_CLEANUP_SKIPPED"
   - Rationale: Safe cleanup of task-specific temporary files without repository interaction
3. Bash: cd /workspace/branches/main/code
4. Bash: git worktree remove /workspace/branches/{task-name}/code
5. Bash: git branch -d {task-name}
6. Bash: rm -rf /workspace/branches/{task-name}

NOTE: todo.md update should have been committed with task deliverables in Phase 2, not here

VIOLATION CHECK: All commands succeed, locks removed, worktree cleaned up, directory deleted

⚠️ ANTI-PATTERN PREVENTION:
PROHIBITED: Executing Phase 7 cleanup without first preserving work via git merge/cherry-pick
PROHIBITED: Deleting worktrees containing uncommitted or unmerged changes
PROHIBITED: "Abandoning work due to extensive scope" - must use scope negotiation protocol instead

CRITICAL SAFETY NOTE: Directory deletion (step 5) is IRREVERSIBLE. Only execute after:
- Confirming git merge --ff-only succeeded (step 4)
- Verifying all task changes are in main branch
- Ensuring no important files remain in task directory
```

## STAGE TRANSITION ENFORCEMENT

### Transition Guard Pattern
```
BETWEEN EACH STAGE: Execute verification block
TodoWrite: Update current stage to "completed"
EXPLICIT VERIFICATION: State what was completed and verified
PROCEED only after explicit verification statement
```

### Decision Parsing Enforcement Pattern
```
MANDATORY AFTER ANY AGENT INVOCATION:
1. Grep: "FINAL DECISION:" in agent response
2. IF not found: Re-invoke same agent with clarification prompt:
   "Your previous response was missing the required decision format. Please provide a clear final decision using exactly one of:
   - 'FINAL DECISION: ✅ APPROVED - [brief reason]'
   - 'FINAL DECISION: ❌ REJECTED - [specific issues]'"
3. IF second attempt also lacks format: Assume ❌ REJECTED status + add to violation report for human review
4. IF found "✅ APPROVED": Continue workflow
5. IF found "❌ REJECTED": Execute appropriate Phase 5 resolution

GRACEFUL ERROR HANDLING:
- Retry mechanism preserves context and prior work
- Format violations don't destroy substantive progress
- Human reports focus on actual decision content, not formatting glitches
- System continues functioning while ensuring compliance
- Violation tracking enables protocol improvement over time
```

### Protocol Violation Reporting Pattern
```
MAINTAIN THROUGHOUT TASK EXECUTION:
1. Track all format violations in TodoWrite tool as separate item
2. Record: Agent type, phase, violation type, retry outcome
3. Include in final task summary for human review

VIOLATION REPORT FORMAT:
"Protocol Violations Detected:
- [Agent]: [Phase] - Missing decision format (resolved via retry)
- [Agent]: [Phase] - Malformed decision (assumed REJECTED)
- Total violations: X, Auto-resolved: Y, Manual review needed: Z"

PURPOSE:
- Identify agent prompt issues needing refinement
- Track protocol compliance trends over time  
- Enable proactive protocol improvements
- Maintain audit trail for debugging
```

### Phase Sequence Enforcement
```
MANDATORY ORDER: Agent Selection → Worktree Setup → Phase 1 → Phase 2 → [Phase 3-5 Conditional] → Phase 6 → Phase 7
VIOLATION CHECK: TodoWrite tool shows phases completed in order
IF out of order: RESTART from Phase 1
```

## CONTEXT PRESERVATION RULES

### Single Session Continuity
```
MAINTAIN CONTEXT: All task execution in single Claude session
NO HANDOFFS: Complete task without waiting for user input
IF session interrupted: Restart task from beginning with new session
```

### Tool Call Batching  
```
BATCH RELATED OPERATIONS: Use multiple tool calls in single message
EXAMPLE: All Phase 1 agents in one message
EXAMPLE: All safety checks in one message
REDUCES CONTEXT FRAGMENTATION
```

### State Persistence Patterns
```
CRITICAL STATE TRACKING:
- Current stage in TodoWrite tool
- Session ID in environment  
- Working directory verification
- Lock status confirmation
VERIFY STATE before each major operation
```

## ERROR RECOVERY PROTOCOLS

### Violation Detection Triggers
```
AUTOMATIC VIOLATION DETECTION:
- Wrong directory (pwd check fails)
- Missing locks (lock file check fails)  
- Build failures (non-zero exit codes)
- Git operation failures (rebase/merge fails)
- Prohibited files detected (status check fails)

RECOVERY ACTION: TERMINATE current task, restart from Stage 1
```

### Multi-Instance Conflict Resolution
```
LOCK CONFLICT DETECTED:
1. Log conflict: "Task {name} owned by another instance"  
2. Read: todo.md (find different available task)
3. NEVER wait or retry lock acquisition
4. Select alternative task immediately
```

### Partial Completion Recovery
```
IF TASK INTERRUPTED:
1. Check existing locks for session ID
2. IF own session: Resume from last completed stage
3. IF foreign session: Select different task  
4. NEVER force-remove foreign locks
```

## COMPLIANCE VERIFICATION PATTERNS

### Pre-Execution Compliance Check
```
BEFORE STARTING ANY TASK:
Read: docs/project/critical-rules.md
Grep: "VIOLATION\|MANDATORY\|CRITICAL" in critical-rules.md  
VERIFY: Understanding of violation consequences
PROCEED only after compliance review
```

### Continuous Compliance Monitoring
```
AFTER EACH STAGE:
TodoWrite: Document stage completion with verification
VERIFY: All mandatory procedures completed
CHECK: No prohibited operations performed
CONFIRM: All safety guards passed
```

### Final Compliance Audit
```
BEFORE TASK COMPLETION:
AUDIT CHECKLIST:
- All stages completed sequentially ✓
- All locks properly managed ✓  
- No prohibited files committed ✓
- Build validation passed ✓
- Git workflow followed ✓
- TodoWrite tracking complete ✓
COMPLETE only after full audit passes
```

## TOOL-SPECIFIC OPTIMIZATION PATTERNS

### Bash Tool Usage
```
COMBINE RELATED OPERATIONS: Use && and || for safety
EXAMPLE: command1 && echo "SUCCESS" || echo "FAILED" 
VERIFY OUTPUTS: Check command success before proceeding
AVOID INTERACTIVE COMMANDS: No -i flags, no prompts
```

### Read Tool Usage  
```
BATCH READS: Read multiple related sections in sequence
TARGET SPECIFIC LINES: Use offset/limit for large files
VERIFY EXISTENCE: Check file exists before reading
```

### Task Tool Usage
```
PARALLEL AGENT CALLS: Multiple agents in single message
STRUCTURED PROMPTS: Consistent template format for agents
CLEAR SCOPE LIMITS: Explicit boundaries for each agent
```

### TodoWrite Tool Usage
```
FREQUENT UPDATES: Track progress after each major stage
CONSISTENT FORMAT: Use same content/activeForm patterns
STATE VERIFICATION: Use tool to verify workflow position
```

**END OF CLAUDE EXECUTION FRAMEWORK**

This version optimizes for Claude's actual processing patterns: tool sequencing, context preservation, and violation prevention through structured decision trees rather than human-readable checklists.