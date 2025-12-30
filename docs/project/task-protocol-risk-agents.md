# Task Protocol - Risk Assessment & Agent Selection

> **Parent:** [task-protocol-core.md](task-protocol-core.md)

**PURPOSE**: Risk classification, agent selection, workflow variant determination.
**WHEN TO READ**: During CLASSIFIED state when determining task risk level and selecting agents.

---

## RISK-BASED AGENT SELECTION ENGINE {#risk-based-agent-selection-engine}

### Agent Classification {#agent-classification}

**STAKEHOLDER AGENTS** (task implementation): architect, style, quality, test, build, security, performance, usability

**NON-STAKEHOLDER AGENTS** (excluded from task classification):
- `config` - Claude Code configuration management only
- Audit pipeline skills: `parse-conversation-timeline`, `audit-protocol-compliance`, `audit-protocol-efficiency`

**CRITICAL RULE**: CLASSIFIED state agent selection uses ONLY stakeholder agents. NEVER include config or audit agents in task worktree selection. **Violation consequence**: Task protocol failure, invalid agent invocation.

### Automatic Risk Classification {#automatic-risk-classification}

**Input**: File paths from modification request
**Process**: Pattern matching → Escalation trigger analysis → Agent set determination
**Output**: Risk level (HIGH/MEDIUM/LOW) + Required agent set (stakeholder agents only)

### HIGH-RISK FILES (Complete Validation Required) {#high-risk-files-complete-validation-required}

**Patterns**:
- `src/**/*.java` (core implementation)
- `pom.xml`, `**/pom.xml` (build configuration)
- `.github/**` (CI/CD workflows)
- `**/security/**` (security components)
- `checkstyle.xml`, `**/checkstyle*.xml` (style enforcement)
- `CLAUDE.md` (critical configuration)
- `docs/project/task-protocol.md` (protocol configuration)
- `docs/project/critical-rules.md` (safety rules)

**Required Agents**: architect, style, quality, build
**Additional Agents**: security (security-related), performance (performance-critical), test (new functionality), usability (user-facing)

### MEDIUM-RISK FILES (Domain Validation Required) {#medium-risk-files-domain-validation-required}

**Patterns**:
- `src/test/**/*.java` (test files)
- `docs/code-style/**` (style documentation)
- `**/resources/**/*.properties` (configuration)
- `**/*Test.java`, `**/*Tests.java` (test classes)

**Required Agents**: architect, quality
**Additional Agents**: style (style files), security (config files), performance (benchmarks)

### LOW-RISK FILES (Minimal Validation Required) {#low-risk-files-minimal-validation-required}

**Patterns**:
- `*.md` (except CLAUDE.md, task-protocol.md, critical-rules.md)
- `docs/**/*.md` (general documentation)
- `todo.md` (task tracking)
- `*.txt`, `*.log` (text files)
- `**/README*` (readme files)

**Required Agents**: None (unless escalation triggered)

### Escalation Triggers {#escalation-triggers}

**Keywords that trigger escalation**:
- "security" → escalate LOW→MEDIUM or MEDIUM→HIGH
- "architecture" → escalate LOW→MEDIUM or MEDIUM→HIGH
- "breaking" → escalate LOW→MEDIUM or MEDIUM→HIGH
- "performance" → escalate LOW→MEDIUM or MEDIUM→HIGH
- "concurrent" → escalate LOW→MEDIUM or MEDIUM→HIGH
- "database" → escalate LOW→MEDIUM or MEDIUM→HIGH
- "api" → escalate LOW→MEDIUM or MEDIUM→HIGH
- "state" → escalate LOW→MEDIUM or MEDIUM→HIGH
- "dependency" → escalate LOW→MEDIUM or MEDIUM→HIGH

**Content Analysis triggers**: Cross-module dependencies, security implications, architectural changes

**Escalation Action**: Force escalation to NEXT HIGHER risk level (LOW→MEDIUM, MEDIUM→HIGH). HIGH cannot escalate further.

### Manual Overrides {#manual-overrides}

- `--force-full-protocol`: Force HIGH risk for critical changes
- `--risk-level=HIGH|MEDIUM|LOW`: Override automatic classification
- Escalation keywords in task description: "security", "architecture", "breaking" → force escalation

### Risk Assessment Audit Trail {#risk-assessment-audit-trail}

**Required Logging** (in state.json and commit messages):
- Risk level selected (HIGH/MEDIUM/LOW)
- Classification method (pattern match, keyword trigger, manual override)
- Escalation triggers activated (if any)
- Workflow variant executed (FULL/ABBREVIATED/STREAMLINED)
- Agent set selected for review
- Final outcome (approved/rejected/deferred)

---

## WORKFLOW VARIANTS BY RISK LEVEL {#workflow-variants-by-risk-level}

### HIGH_RISK_WORKFLOW (Complete Validation) {#high_risk_workflow-complete-validation}

**States**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW → COMPLETE → CLEANUP
**Agents**: All agents based on task requirements
**Isolation**: Mandatory worktree
**Review**: Complete stakeholder validation
**Use Case**: Core implementation, build configuration, security, CI/CD
**Conditional Skips**: None - all validation required

### MEDIUM_RISK_WORKFLOW (Domain Validation) {#medium_risk_workflow-domain-validation}

**States**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW → COMPLETE → CLEANUP
**Agents**:
- Base: architect (always required)
- +formatter: If style/formatting files modified
- +security: If configuration or resource files modified
- +performance: If test performance or benchmarks affected
- +engineer: Always included for code quality validation

**Isolation**: Worktree for multi-file changes
**Review**: Domain-appropriate stakeholder validation
**Use Case**: Test files, style documentation, configuration files
**Conditional Skips**: May skip IMPLEMENTATION/VALIDATION if change_type is "documentation_only" OR "config_only"

### LOW_RISK_WORKFLOW (Streamlined Validation) {#low_risk_workflow-streamlined-validation}

**States**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → COMPLETE → CLEANUP
**Agents**: None (unless escalation triggered - then escalate to MEDIUM and re-evaluate agents)
**Isolation**: Required for multi-file changes, optional for single documentation file
**Review**: Evidence-based validation and automated checks
**Safety Gates**:
- Verify no cross-references to modified files in src/
- Confirm no build configuration impact
- Validate no security-sensitive content changes

**Use Case**: Documentation updates, todo.md, README files
**Conditional Skips**: Skip IMPLEMENTATION, VALIDATION, REVIEW entirely

### State Path Logic {#conditional-state-transition-logic}

```python
def determine_state_path(risk_level, change_type):
    base = ["INIT", "CLASSIFIED", "REQUIREMENTS", "SYNTHESIS"]
    full = base + ["IMPLEMENTATION", "VALIDATION", "REVIEW", "COMPLETE", "CLEANUP"]
    minimal = base + ["COMPLETE", "CLEANUP"]

    if risk_level == "HIGH":
        return full  # Always full validation, no skips
    elif risk_level == "MEDIUM":
        if change_type in ["documentation_only", "config_only"]:
            return minimal  # Skip IMPLEMENTATION/VALIDATION/REVIEW
        else:
            return full  # Full validation for code changes
    elif risk_level == "LOW":
        return minimal  # Always minimal path
    else:
        return full  # Default to HIGH risk (full validation) if uncertain
```

### Skip Conditions {#skip-condition-examples}

**IMPLEMENTATION/VALIDATION State Skip Conditions** (allows minimal path):
- Maven dependency additions (configuration only)
- Build plugin configuration changes
- Documentation updates without code references
- Property file modifications
- Version bumps without code changes
- README and markdown file updates
- Todo.md task tracking updates

**Full Validation Required Conditions** (forces full path):
- Any source code modifications (*.java, *.js, *.py, etc.)
- Runtime behavior changes expected
- Security-sensitive configuration changes
- Build system modifications affecting compilation
- API contract modifications

---

## AGENT SELECTION DECISION TREE {#agent-selection-decision-tree}

### Selection Framework {#comprehensive-agent-selection-framework}

**Input**: Task description and file modification patterns

**CORE AGENTS (Always Required)**:
- **architect**: MANDATORY for ALL file modification tasks

**FUNCTIONAL AGENTS (Code Implementation)**:
- IF NEW CODE created: add formatter, engineer, builder
- IF IMPLEMENTATION (not just config): add tester
- IF MAJOR FEATURES completed: add usability (MANDATORY after completion)

**SECURITY AGENTS (Actual Security Concerns)**:
- IF AUTHENTICATION/AUTHORIZATION changes: add security
- IF EXTERNAL API/DATA integration: add security
- IF ENCRYPTION/CRYPTOGRAPHIC operations: add security
- IF INPUT VALIDATION/SANITIZATION: add security

**PERFORMANCE AGENTS**:
- IF ALGORITHM optimization tasks: add performance
- IF DATABASE/QUERY optimization: add performance
- IF MEMORY/CPU intensive operations: add performance

**FORMATTING AGENTS**:
- IF PARSER LOGIC modified: add performance, security
- IF AST TRANSFORMATION changed: add engineer, tester
- IF FORMATTING RULES affected: add formatter

**AGENTS NOT NEEDED FOR SIMPLE OPERATIONS**:
- Maven module renames: NO performance
- Configuration file updates: NO security unless changing auth
- Directory/file renames: NO performance
- Documentation updates: Usually only architect

**ANALYSIS AGENTS (Research/Study Tasks)**:
- ARCHITECTURAL ANALYSIS: add architect
- PERFORMANCE ANALYSIS: add performance
- UX/INTERFACE ANALYSIS: add usability
- SECURITY ANALYSIS: add security
- CODE QUALITY REVIEW: add engineer
- PARSER/FORMATTER PERFORMANCE ANALYSIS: add performance

**Agent Selection Verification Checklist**:
- [ ] NEW CODE task → formatter included?
- [ ] Source files created/modified → builder included?
- [ ] Performance-critical code → performance included?
- [ ] Security-sensitive features → security included?
- [ ] User-facing interfaces → usability included?
- [ ] Post-implementation refactoring → engineer included?
- [ ] AST parsing/code formatting → performance included?

**Agent Roles**:
- **formatter**: Apply ALL manual style guide rules from docs/code-style/
- **builder**: Triggers linters (checkstyle, PMD, ESLint) through build system
- **engineer**: Post-implementation refactoring, best practices enforcement
- **tester**: Business logic validation, comprehensive test creation
- **security**: Data handling, storage compliance review
- **performance**: Algorithmic efficiency, resource optimization
- **usability**: UX design, interface evaluation
- **architect**: System architecture, implementation guidance

---

## COMPLETE STYLE VALIDATION FRAMEWORK {#complete-style-validation-framework}

### Three-Component Style Validation {#three-component-style-validation}

**MANDATORY**: When style validation required, ALL THREE components must pass. **Violation consequence**: Incomplete validation, style issues in merged code.

1. **Automated Linters** (via build):
   - `checkstyle`: Java coding conventions and formatting
   - `PMD`: Code quality and best practices
   - `ESLint`: JavaScript/TypeScript style (if applicable)

2. **Manual Style Rules** (via formatter):
   - Apply ALL detection patterns from `docs/code-style/*-claude.md`
   - Java-specific patterns (naming, structure, comments)
   - Common patterns (cross-language consistency)

3. **Build Integration** (via builder):
   - Automated fixing when conflicts detected (LineLength vs UnderutilizedLines)
   - Use `checkstyle/fixers` module for AST-based consolidate-then-split strategy
   - Comprehensive testing validates fixing logic before application

**CRITICAL ERROR PATTERN**: Checking ONLY checkstyle and declaring "no violations found" when PMD or manual violations exist. This is a validation failure.

### Complete Style Validation Gate Pattern {#complete-style-validation-gate-pattern}

```bash
validate_complete_style_compliance() {
    # Component 1: Automated linters via build system
    ./mvnw checkstyle:check || return 1
    ./mvnw pmd:check || return 1

    # Component 2: Manual style rules via formatter agent
    invoke_style_auditor_with_manual_detection_patterns || return 1

    # Component 3: Automated fixing integration if conflicts
    if detect_style_conflicts; then
        apply_automated_style_fixes || return 1
        # Re-validate after automated fixes
        validate_complete_style_compliance
    fi
    return 0
}
```

---

## BATCH PROCESSING AND CONTINUOUS MODE {#batch-processing-and-continuous-mode}

### Batch Processing Restrictions {#batch-processing-restrictions}

**PROHIBITED PATTERNS** (violation consequence: protocol failure, corrupted task state):
- Processing multiple tasks without individual protocol execution
- "Work on all Phase 1 tasks until done" - Must select ONE specific task
- "Complete these 5 tasks" - Each requires separate lock and worktree
- Assuming research tasks can bypass protocol

**MANDATORY SINGLE-TASK PROCESSING**:
1. Select ONE specific task from todo.md
2. Acquire atomic lock for THAT specific task only
3. Create isolated worktree for THAT task only
4. Execute full state machine protocol for THAT task only
5. Complete CLEANUP state before starting any other task

**Consequence of violation**: Lock contention, state corruption, incomplete task isolation, merge conflicts.

### Automatic Continuous Mode Translation {#automatic-continuous-mode-translation}

**Batch requests auto-translate to continuous mode**:
1. **ACKNOWLEDGE**: "I understand you want to work on multiple tasks..."
2. **AUTO-TRANSLATE**: "I'll process each task with full protocol isolation..."
3. **EXECUTE**: Trigger continuous workflow mode

**Patterns to Auto-Translate**: "Work on all [phase/type] tasks", "Complete these tasks until done", "Process the todo list", "Work on multiple tasks", any request mentioning multiple specific tasks

**Task Filtering**:
1. Phase-based filtering: Process only tasks in specified phases
2. Type-based filtering: Process only tasks matching specified types
3. Name-based filtering: Process only specifically mentioned task names
4. Default behavior: Process all available tasks if no filter specified

---

## PROTOCOL INTERPRETATION MODE {#protocol-interpretation-mode}

**Apply deeper analysis when following task protocol**:
- Question assumptions about task scope and complexity
- Verify all transition conditions are genuinely met
- Evidence-based validation over procedural compliance
- Consider edge cases and alternative approaches
- Skeptical evaluation of "good enough" solutions
