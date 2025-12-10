# Task Protocol - Risk Assessment & Agent Selection

> **Version:** 1.0 | **Last Updated:** 2025-12-10
> **Parent Document:** [task-protocol-core.md](task-protocol-core.md)
> **Related Documents:** [task-protocol-operations.md](task-protocol-operations.md) •
[task-protocol-multi-agent.md](task-protocol-multi-agent.md)

**PURPOSE**: Risk classification, agent selection, and workflow variant determination.

**WHEN TO READ**: During CLASSIFIED state when determining task risk level and selecting agents.

---

## RISK-BASED AGENT SELECTION ENGINE {#risk-based-agent-selection-engine}

### Agent Classification {#agent-classification}

**STAKEHOLDER AGENTS** (for task implementation):
- `architect`, `architect`
- `style`, `style`
- `quality`, `quality`
- `test`, `test`
- `build`, `build`
- `security`, `security`
- `performance`, `performance`
- `usability`, `usability`

**NON-STAKEHOLDER AGENTS** (excluded from task classification):
- `config`, `config` - Claude Code configuration management only
- `parse-conversation-timeline skill`, `audit-protocol-compliance skill`, `audit-protocol-efficiency skill` -
  Audit pipeline only

**CRITICAL RULE**: When performing CLASSIFIED state agent selection, ONLY select from stakeholder agents.
NEVER include config-* or process-* agents in task worktree agent selection. These agents serve
meta-purposes (configuration management, process auditing) and are NOT involved in task implementation.

### Automatic Risk Classification {#automatic-risk-classification}

**Input**: File paths from modification request
**Process**: Pattern matching → Escalation trigger analysis → Agent set determination
**Output**: Risk level (HIGH/MEDIUM/LOW) + Required agent set from STAKEHOLDER AGENTS only

### HIGH-RISK FILES (Complete Validation Required) {#high-risk-files-complete-validation-required}

**Patterns:**
- `src/**/*.java` (core implementation)
- `pom.xml`, `**/pom.xml` (build configuration)
- `.github/**` (CI/CD workflows)
- `**/security/**` (security components)
- `checkstyle.xml`, `**/checkstyle*.xml` (style enforcement)
- `CLAUDE.md` (critical configuration)
- `docs/project/task-protocol.md` (protocol configuration)
- `docs/project/critical-rules.md` (safety rules)

**Required Agents**: architect, style, quality, build
**Additional Agents**: security (if security-related), performance (if performance-critical),
test (if new functionality), usability (if user-facing)

### MEDIUM-RISK FILES (Domain Validation Required) {#medium-risk-files-domain-validation-required}

**Patterns:**
- `src/test/**/*.java` (test files)
- `docs/code-style/**` (style documentation)
- `**/resources/**/*.properties` (configuration)
- `**/*Test.java`, `**/*Tests.java` (test classes)

**Required Agents**: architect, quality
**Additional Agents**: style (if style files), security (if config files),
performance (if benchmarks)

### LOW-RISK FILES (Minimal Validation Required) {#low-risk-files-minimal-validation-required}

**Patterns:**
- `*.md` (except CLAUDE.md, task-protocol.md, critical-rules.md)
- `docs/**/*.md` (general documentation)
- `todo.md` (task tracking)
- `*.txt`, `*.log` (text files)
- `**/README*` (readme files)

**Required Agents**: None (unless escalation triggered)

### Escalation Triggers {#escalation-triggers}

**Keywords**: "security", "architecture", "breaking", "performance", "concurrent", "database", "api",
"state", "dependency"
**Content Analysis**: Cross-module dependencies, security implications, architectural changes
**Action**: Force escalation to next higher risk level

### Manual Overrides {#manual-overrides}

**Force Full Protocol**: `--force-full-protocol` flag for critical changes
**Explicit Risk Level**: `--risk-level=HIGH|MEDIUM|LOW` to override classification
**Escalation Keywords**: "security", "architecture", "breaking" in task description

### Risk Assessment Audit Trail {#risk-assessment-audit-trail}

**Required Logging:**
- Risk level selected (HIGH/MEDIUM/LOW)
- Classification method (pattern match, keyword trigger, manual override)
- Escalation triggers activated (if any)
- Workflow variant executed (FULL/ABBREVIATED/STREAMLINED)
- Agent set selected for review
- Final outcome (approved/rejected/deferred)

**Implementation**: Log in state.json file and commit messages for audit purposes

---

## WORKFLOW VARIANTS BY RISK LEVEL {#workflow-variants-by-risk-level}

### HIGH_RISK_WORKFLOW (Complete Validation) {#high_risk_workflow-complete-validation}

**States Executed**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW →
COMPLETE → CLEANUP
**Stakeholder Agents**: All agents based on task requirements
**Isolation**: Mandatory worktree isolation
**Review**: Complete stakeholder validation
**Use Case**: Core implementation, build configuration, security, CI/CD
**Conditional Skips**: None - all validation required

### MEDIUM_RISK_WORKFLOW (Domain Validation) {#medium_risk_workflow-domain-validation}

**States Executed**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW →
COMPLETE → CLEANUP
**Stakeholder Agents**: Based on change characteristics
- Base: architect (always required)
- +formatter: If style/formatting files modified
- +security: If any configuration or resource files modified
- +performance: If test performance or benchmarks affected
- +engineer: Always included for code quality validation
**Isolation**: Worktree isolation for multi-file changes
**Review**: Domain-appropriate stakeholder validation
**Use Case**: Test files, style documentation, configuration files
**Conditional Skips**: May skip IMPLEMENTATION/VALIDATION states if only documentation changes

### LOW_RISK_WORKFLOW (Streamlined Validation) {#low_risk_workflow-streamlined-validation}

**States Executed**: INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → COMPLETE → CLEANUP
**Stakeholder Agents**: None (unless escalation triggered)
**Isolation**: Required for multi-file changes, optional for single documentation file
**Review**: Evidence-based validation and automated checks
**Safety Gates**:
- Verify no cross-references to modified files in src/
- Confirm no build configuration impact
- Validate no security-sensitive content changes
**Use Case**: Documentation updates, todo.md, README files
**Conditional Skips**: Skip IMPLEMENTATION, VALIDATION, REVIEW states entirely

### Conditional State Transition Logic {#conditional-state-transition-logic}

```python
def determine_state_path(risk_level, change_type):
    """Determine which states to execute based on risk and change type"""

    base_states = ["INIT", "CLASSIFIED", "REQUIREMENTS", "SYNTHESIS"]

    if risk_level == "HIGH":
        return base_states + ["IMPLEMENTATION", "VALIDATION", "REVIEW", "COMPLETE", "CLEANUP"]

    elif risk_level == "MEDIUM":
        if change_type in ["documentation_only", "config_only"]:
            return base_states + ["COMPLETE", "CLEANUP"]
        else:
            return base_states + ["IMPLEMENTATION", "VALIDATION", "REVIEW", "COMPLETE", "CLEANUP"]

    elif risk_level == "LOW":
        return base_states + ["COMPLETE", "CLEANUP"]

    else:
        # Default to HIGH risk if uncertain
        return base_states + ["IMPLEMENTATION", "VALIDATION", "REVIEW", "COMPLETE", "CLEANUP"]
```

### Skip Condition Examples {#skip-condition-examples}

**IMPLEMENTATION/VALIDATION State Skip Conditions:**
- Maven dependency additions (configuration only)
- Build plugin configuration changes
- Documentation updates without code references
- Property file modifications
- Version bumps without code changes
- README and markdown file updates
- Todo.md task tracking updates

**Full Validation Required Conditions:**
- Any source code modifications (*.java, *.js, *.py, etc.)
- Runtime behavior changes expected
- Security-sensitive configuration changes
- Build system modifications affecting compilation
- API contract modifications

---

## AGENT SELECTION DECISION TREE {#agent-selection-decision-tree}

### Comprehensive Agent Selection Framework {#comprehensive-agent-selection-framework}

**Input**: Task description and file modification patterns
**Available Agents**: architect, usability, performance, security,
formatter, engineer, tester, builder

**Processing Logic:**

**🚨 CORE AGENTS (Always Required):**
- **architect**: MANDATORY for ALL file modification tasks (provides implementation requirements)

**🔍 FUNCTIONAL AGENTS (Code Implementation):**
- IF NEW CODE created: add formatter, engineer, builder
- IF IMPLEMENTATION (not just config): add tester
- IF MAJOR FEATURES completed: add usability (MANDATORY after completion)

**🛡️ SECURITY AGENTS (Actual Security Concerns):**
- IF AUTHENTICATION/AUTHORIZATION changes: add security
- IF EXTERNAL API/DATA integration: add security
- IF ENCRYPTION/CRYPTOGRAPHIC operations: add security
- IF INPUT VALIDATION/SANITIZATION: add security

**⚡ PERFORMANCE AGENTS (Performance Critical):**
- IF ALGORITHM optimization tasks: add performance
- IF DATABASE/QUERY optimization: add performance
- IF MEMORY/CPU intensive operations: add performance

**🔧 FORMATTING AGENTS (Code Quality):**
- IF PARSER LOGIC modified: add performance, security
- IF AST TRANSFORMATION changed: add engineer, tester
- IF FORMATTING RULES affected: add formatter

**❌ AGENTS NOT NEEDED FOR SIMPLE OPERATIONS:**
- Maven module renames: NO performance
- Configuration file updates: NO security unless changing auth
- Directory/file renames: NO performance
- Documentation updates: Usually only architect

**📊 ANALYSIS AGENTS (Research/Study Tasks):**
- IF ARCHITECTURAL ANALYSIS: add architect
- IF PERFORMANCE ANALYSIS: add performance
- IF UX/INTERFACE ANALYSIS: add usability
- IF SECURITY ANALYSIS: add security
- IF CODE QUALITY REVIEW: add engineer
- IF PARSER/FORMATTER PERFORMANCE ANALYSIS: add performance

**Agent Selection Verification Checklist:**
- [ ] NEW CODE task → formatter included?
- [ ] Source files created/modified → builder included?
- [ ] Performance-critical code → performance included?
- [ ] Security-sensitive features → security included?
- [ ] User-facing interfaces → usability included?
- [ ] Post-implementation refactoring → engineer included?
- [ ] AST parsing/code formatting → performance included?

**Special Agent Usage Patterns:**
- **formatter**: Apply ALL manual style guide rules from docs/code-style/ (Java, common, and
  language-specific patterns)
- **builder**: For style/formatting tasks, triggers linters (checkstyle, PMD, ESLint) through builder
  system
- **builder**: Use alongside formatter to ensure comprehensive validation (automated + manual
  rules)
- **engineer**: Post-implementation refactoring and best practices enforcement
- **tester**: Business logic validation and comprehensive test creation
- **security**: Data handling and storage compliance review
- **performance**: Algorithmic efficiency and resource optimization
- **usability**: User experience design and interface evaluation
- **architect**: System architecture and implementation guidance

---

## COMPLETE STYLE VALIDATION FRAMEWORK {#complete-style-validation-framework}

### Three-Component Style Validation {#three-component-style-validation}

**MANDATORY PROCESS**: When style validation is required, ALL THREE components must pass:

1. **Automated Linters** (via build):
   - `checkstyle`: Java coding conventions and formatting
   - `PMD`: Code quality and best practices
   - `ESLint`: JavaScript/TypeScript style (if applicable)

2. **Manual Style Rules** (via formatter):
   - Apply ALL detection patterns from `docs/code-style/*-claude.md`
   - Java-specific patterns (naming, structure, comments)
   - Common patterns (cross-language consistency)
   - Language-specific patterns as applicable

3. **Build Integration** (via builder):
   - Automated fixing when conflicts detected (LineLength vs UnderutilizedLines)
   - Use `checkstyle/fixers` module for AST-based consolidate-then-split strategy
   - Comprehensive testing validates fixing logic before application

### Complete Style Validation Gate Pattern {#complete-style-validation-gate-pattern}

```bash
# MANDATORY: Never assume checkstyle-only validation
# CRITICAL ERROR PATTERN: Checking only checkstyle and declaring "no violations found"
# when PMD/manual violations exist

validate_complete_style_compliance() {
    echo "=== COMPLETE STYLE VALIDATION GATE ==="

    # Component 1: Automated linters via build system
    echo "Validating automated linters..."
    ./mvnw checkstyle:check || return 1
    ./mvnw pmd:check || return 1

    # Component 2: Manual style rules via formatter agent
    echo "Validating manual style rules..."
    invoke_style_auditor_with_manual_detection_patterns || return 1

    # Component 3: Automated fixing integration if conflicts
    echo "Checking for LineLength vs UnderutilizedLines conflicts..."
    if detect_style_conflicts; then
        echo "Applying automated AST-based fixes..."
        apply_automated_style_fixes || return 1
        # Re-validate after automated fixes
        validate_complete_style_compliance
    fi

    echo "✅ Complete style validation passed: checkstyle + PMD + manual rules"
    return 0
}
```

---

## BATCH PROCESSING AND CONTINUOUS MODE {#batch-processing-and-continuous-mode}

### Batch Processing Restrictions {#batch-processing-restrictions}

**PROHIBITED PATTERNS:**
- Processing multiple tasks sequentially without individual protocol execution
- "Work on all Phase 1 tasks until done" - Must select ONE specific task
- "Complete these 5 tasks" - Each requires separate lock acquisition and worktree
- Assuming research tasks can bypass protocol because they create "only" study files

**MANDATORY SINGLE-TASK PROCESSING:**
1. Select ONE specific task from todo.md
2. Acquire atomic lock for THAT specific task only
3. Create isolated worktree for THAT task only
4. Execute full state machine protocol for THAT task only
5. Complete CLEANUP state before starting any other task

### Automatic Continuous Mode Translation {#automatic-continuous-mode-translation}

**When users request batch operations:**

**AUTOMATIC TRANSLATION PROTOCOL:**
1. **ACKNOWLEDGE**: "I understand you want to work on multiple tasks efficiently..."
2. **AUTO-TRANSLATE**: "I'll interpret this as a request to work on the todo list in continuous mode,
   processing each task with full protocol isolation..."
3. **EXECUTE**: Automatically trigger continuous workflow mode without requiring user to rephrase

**Batch Request Patterns to Auto-Translate:**
- "Work on all [phase/type] tasks"
- "Complete these tasks until done"
- "Process the todo list"
- "Work on multiple tasks"
- Any request mentioning multiple specific tasks

**Task Filtering for Continuous Mode:**
When batch requests specify subsets:
1. **Phase-based filtering**: Process only tasks in specified phases
2. **Type-based filtering**: Process only tasks matching specified types
3. **Name-based filtering**: Process only specifically mentioned task names
4. **Default behavior**: Process all available tasks if no filter mentioned

---

## 🧠 PROTOCOL INTERPRETATION MODE {#protocol-interpretation-mode}

**ENHANCED ANALYTICAL RIGOR**: Parent agent must apply deeper analysis when interpreting and following the
task protocol workflow. Rather than surface-level interpretations, carefully analyze what the protocol truly
requires for the specific task context.

**Critical Thinking Requirements:**
- Question assumptions about task scope and complexity
- Verify all transition conditions are genuinely met
- Apply evidence-based validation rather than procedural compliance
- Consider edge cases and alternative approaches
- Maintain skeptical evaluation of "good enough" solutions
