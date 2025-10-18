# Agent Common Patterns

Common patterns and requirements shared across all stakeholder agents.

## 🚨 SCOPE ENFORCEMENT PROTOCOL

### Two-Mode Operation

**MODE 1: TASK-SPECIFIC ANALYSIS** (Default - Restrictive Scope):
- **TRIGGER**: When executing specific tasks with `../context.md`
- **SCOPE**: ONLY files explicitly listed in context.md scope section
- **ENFORCEMENT**: VIOLATION = IMMEDIATE TASK FAILURE
- **RESTRICTIONS**:
  - ABSOLUTELY FORBIDDEN to scan files outside context.md scope
  - No "related files", "dependency analysis", "architecture context gathering" outside scope
  - Do NOT review the entire system
  - Do NOT analyze files not mentioned in context.md
  - Do NOT explore adjacent packages or modules unless explicitly listed
  - STOP IMMEDIATELY if attempting to access files outside authorized scope
- **VERIFICATION**: Must include scope compliance verification in report

**MODE 2: COMPREHENSIVE ANALYSIS** (Full Scope):
- **TRIGGER**: When explicitly authorized with "COMPREHENSIVE ANALYSIS MODE" in prompt
- **PURPOSE**: Discover new actionable items when TODO list exhausted
- **SCOPE**: Full project analysis permitted
- **OBJECTIVE**: Add new findings to todo.md
- **AUTHORIZATION**: Only when no active tasks and explicitly requested

### Scope Detection Protocol

1. **CHECK PROMPT**: Look for "COMPREHENSIVE ANALYSIS MODE" authorization
2. **IF AUTHORIZED**: Proceed with full project scope for TODO discovery
3. **IF NOT AUTHORIZED**: Follow restrictive task-specific scope (Mode 1)
4. **DEFAULT ASSUMPTION**: If context.md exists, assume MODE 1 (task-specific) unless told otherwise
5. **ALWAYS VERIFY**: Include scope mode and compliance verification in report

**SCOPE COMPLIANCE STATEMENT** (Required in every report):
```
**SCOPE COMPLIANCE**: Files analyzed: [list] (MODE 1: Task-specific | MODE 2: Comprehensive)
```

## 📋 WORKFLOW REQUIREMENTS

Before beginning analysis, agents MUST:

1.  **MANDATORY FOUNDATIONAL READING**: Read these project documents to understand constraints and
   requirements:
   - **`docs/project/scope.md`**: Project scope, architectural guidelines, and technical constraints
   - **`docs/code-style-human.md`**: Code formatting and development standards

2.  **MANDATORY FIRST STEP**: Read the task's context.md file at `../context.md` to understand the task
   objectives and EXACT scope boundaries

3. **VERIFY SCOPE**: List out EXACTLY which files you are authorized to analyze from context.md

4.  **SCOPE VIOLATION CHECK**: If you find yourself needing to analyze files NOT in context.md, STOP and
   report the limitation

5. **FORBIDDEN ACTIONS**: NEVER scan, read, or analyze files outside context.md scope

6. Read ALL agent reports referenced in context.md to understand previous findings

7. **ARCHITECTURAL CONSTRAINT VERIFICATION**: Ensure all recommendations align with:
   - Stateless server architecture (docs/project/scope.md)
   - Client-side state management requirements (docs/project/scope.md)
   - Java code formatter focus (docs/project/scope.md)
   - Prohibited technologies and patterns (docs/project/scope.md)

8. After completing analysis, create a detailed report using the agent-report template

9. Update context.md with a summary of your work and reference to your report

## 🗂️ TEMPORARY FILE MANAGEMENT

**MANDATORY**: Use isolated temporary directory for all agent artifacts:

```bash
# Get temporary directory (set up by task protocol)
TEMP_DIR=$(cat .temp_dir 2>/dev/null || echo "/tmp/fallback-$$")

# Use for agent-specific artifacts:
# - Test data: "$TEMP_DIR/test_data_*.json"
# - Analysis scripts: "$TEMP_DIR/analyze_*.sh"
# - Benchmark data: "$TEMP_DIR/benchmark_*.log"
# - Security test payloads: "$TEMP_DIR/payload_*.txt"
# - Performance profiling: "$TEMP_DIR/profile_*.hprof"
```

**PROHIBITED**: Never create temporary files in:
- Git repository directories
- Project source directories
- System locations outside designated temp directory

This prevents accidental commits of test artifacts and maintains repository cleanliness.
