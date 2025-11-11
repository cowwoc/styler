---
name: gather-requirements
description: Invoke stakeholder agents in parallel for requirements gathering
allowed-tools: Task, Bash, Read
---

# Gather Requirements Skill

**Purpose**: Invoke all stakeholder agents (architect, tester, formatter) in parallel for requirements gathering, enforcing proper coordination and verification.

**Performance**: Reduces requirements phase from hours (sequential) to minutes (parallel)

## When to Use This Skill

### ✅ Use gather-requirements When:

- Task is in CLASSIFIED state
- Ready to gather stakeholder requirements
- Need to transition to REQUIREMENTS phase
- Want to ensure proper parallel coordination

### ❌ Do NOT Use When:

- Task not yet initialized (use task-init first)
- Still in INIT state (transition to CLASSIFIED first)
- Requirements already gathered (check for requirement reports)
- Ad-hoc work outside task protocol

## What This Skill Does

### 1. Validates Preconditions

```bash
# Checks before invocation:
- Task exists
- Task in CLASSIFIED state
- Agent worktrees exist
- No existing requirement reports (prevents duplicates)
```

### 2. Invokes Stakeholder Agents in Parallel

**CRITICAL**: All three agents MUST be invoked in a single message with multiple Task tool calls.

```markdown
Single assistant message containing:
- Task tool call #1: architect agent
- Task tool call #2: tester agent
- Task tool call #3: formatter agent
```

This ensures true parallel coordination (all agents start concurrently).

### 3. Monitors Completion

```bash
# Waits for all agents to complete
# Verifies all requirement reports created:
- {task-name}-architect-requirements.md
- {task-name}-tester-requirements.md
- {task-name}-formatter-requirements.md
```

### 4. Validates Reports

```bash
# Checks each report:
- File exists
- Non-empty content
- Contains required sections
- Proper markdown format
```

## Required Agent Prompts

### Architect Agent Prompt Template

```markdown
You are the architect stakeholder agent for task: {task-name}

**PHASE**: REQUIREMENTS (NOT implementation)

**YOUR ROLE**: Analyze and document requirements only. DO NOT write code.

**TASK DESCRIPTION**:
{task-description-from-todo.md}

**WORKING DIRECTORY**:
/workspace/tasks/{task-name}/agents/architect/code

**MANDATORY OUTPUT REQUIREMENT**:
Write your analysis to: `/workspace/tasks/{task-name}/{task-name}-architect-requirements.md`

**ANALYSIS SCOPE**:
1. Dependencies and integration points
2. Design patterns applicable
3. Architecture considerations
4. API design recommendations
5. Module structure recommendations

**CRITICAL**: You are in REQUIREMENTS mode. Write ONLY the requirements report.
DO NOT implement code. DO NOT create source files.

Report file MUST be created at the specified path.
```

### Tester Agent Prompt Template

```markdown
You are the tester stakeholder agent for task: {task-name}

**PHASE**: REQUIREMENTS (NOT implementation)

**YOUR ROLE**: Analyze testing requirements only. DO NOT write tests yet.

**TASK DESCRIPTION**:
{task-description-from-todo.md}

**WORKING DIRECTORY**:
/workspace/tasks/{task-name}/agents/tester/code

**MANDATORY OUTPUT REQUIREMENT**:
Write your analysis to: `/workspace/tasks/{task-name}/{task-name}-tester-requirements.md`

**ANALYSIS SCOPE**:
1. Test coverage needs
2. Test strategy (unit, integration, edge cases)
3. Business logic validation requirements
4. Edge cases to cover
5. Test data requirements

**CRITICAL**: You are in REQUIREMENTS mode. Write ONLY the requirements report.
DO NOT implement tests. DO NOT create test files.

Report file MUST be created at the specified path.
```

### Formatter Agent Prompt Template

```markdown
You are the formatter stakeholder agent for task: {task-name}

**PHASE**: REQUIREMENTS (NOT implementation)

**YOUR ROLE**: Analyze documentation and style requirements only. DO NOT implement yet.

**TASK DESCRIPTION**:
{task-description-from-todo.md}

**WORKING DIRECTORY**:
/workspace/tasks/{task-name}/agents/formatter/code

**MANDATORY OUTPUT REQUIREMENT**:
Write your analysis to: `/workspace/tasks/{task-name}/{task-name}-formatter-requirements.md`

**ANALYSIS SCOPE**:
1. Documentation requirements (JavaDoc, comments)
2. Code style standards to enforce
3. Naming convention requirements
4. File organization requirements
5. Checkstyle/PMD considerations

**CRITICAL**: You are in REQUIREMENTS mode. Write ONLY the requirements report.
DO NOT implement code. DO NOT create source files.

Report file MUST be created at the specified path.
```

## Usage

### Basic Requirements Gathering

```bash
# Step 1: Ensure task in CLASSIFIED state
cd /workspace/main
TASK_NAME="implement-formatter-api"

jq '.state = "CLASSIFIED" | .phase = "requirements"' \
  /workspace/tasks/$TASK_NAME/task.json > tmp.json
mv tmp.json /workspace/tasks/$TASK_NAME/task.json

# Step 2: Invoke skill
# Skill will use Task tool to invoke all 3 agents in parallel
```

### With Task Description

```bash
# Extract task description from todo.md
TASK_NAME="implement-formatter-api"
DESCRIPTION=$(grep -A 2 "^- \[ \] $TASK_NAME" /workspace/main/todo.md | tail -n 1)

# Pass to skill for agent prompts
# Skill will incorporate description into agent prompts
```

## Parallel Invocation Pattern

### ✅ CORRECT Pattern (True Parallel)

```markdown
Assistant: Invoking all stakeholder agents for requirements gathering...

[Single message with 3 Task tool calls:]

Task tool call #1:
- subagent_type: architect
- prompt: [architect prompt with REQUIREMENTS emphasis]
- model: sonnet

Task tool call #2:
- subagent_type: tester
- prompt: [tester prompt with REQUIREMENTS emphasis]
- model: sonnet

Task tool call #3:
- subagent_type: formatter
- prompt: [formatter prompt with REQUIREMENTS emphasis]
- model: sonnet
```

**Result**: All agents start work concurrently, requirements gathered in parallel.

### ❌ WRONG Pattern (Sequential)

```markdown
Assistant: Invoking architect agent...
[Task tool call: architect]
[Wait for completion]