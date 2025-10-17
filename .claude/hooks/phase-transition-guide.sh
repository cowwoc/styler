#!/bin/bash
# Phase-specific guidance injection with brevity optimization
# Triggered at phase transitions via UserPromptSubmit
# Performance: ~5ms (state file lookup + conditional output)

set -euo pipefail

TASK_NAME="${1:-unknown}"
NEW_STATE="${2:-UNKNOWN}"
LOCK_FILE="/workspace/tasks/${TASK_NAME}/task.json"
VISIT_TRACKER="/workspace/tasks/${TASK_NAME}/.phase-visits"

# Initialize visit tracker if not exists
if [[ ! -f "$VISIT_TRACKER" ]]; then
	echo '{}' > "$VISIT_TRACKER"
fi

# Get visit count for this phase
VISIT_COUNT=$(jq -r ".\"${NEW_STATE}\" // 0" "$VISIT_TRACKER")
NEW_VISIT_COUNT=$((VISIT_COUNT + 1))

# Update visit tracker
jq ".\"${NEW_STATE}\" = ${NEW_VISIT_COUNT}" "$VISIT_TRACKER" > /tmp/visits.tmp
mv /tmp/visits.tmp "$VISIT_TRACKER"

# Determine verbosity level
if [[ $NEW_VISIT_COUNT -eq 1 ]]; then
	VERBOSITY="FULL"
else
	VERBOSITY="BRIEF"
fi

# Phase-specific guidance
case "$NEW_STATE" in
	"INIT")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📋 INIT PHASE - Task Initialization
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REQUIRED ACTIONS:
1. ✅ Create lock file at /workspace/tasks/{task-name}/task.json
   - Include: session_id, task_name, state, created_at

2. ✅ Create isolated worktree
   - Pattern: git worktree add /workspace/tasks/{task-name}/code -b {task-name}
   - IMMEDIATELY cd to worktree after creation

3. ✅ Create task.md placeholder
   - Location: /workspace/tasks/{task-name}/task.md
   - Will be populated in CLASSIFIED state

4. ✅ Verify isolation
   - Run: pwd | grep -q "/workspace/tasks/{task-name}/code$"

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "📋 INIT (visit $NEW_VISIT_COUNT): Lock + Worktree + CD to worktree + task.md placeholder"
		fi
		;;

	"CLASSIFIED")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔍 CLASSIFIED PHASE - Risk Assessment & Agent Identification
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REQUIRED ACTIONS:
1. ✅ Determine risk level (HIGH/MEDIUM/LOW)
   - HIGH-RISK: src/**, pom.xml, security/**, .github/**, CLAUDE.md
   - MEDIUM-RISK: test files, code-style docs, configuration
   - LOW-RISK: general docs, todo.md, README

2. ✅ Identify required stakeholder agents
   - HIGH-RISK: technical-architect, security-auditor, code-quality-auditor,
                performance-analyzer, style-auditor
   - MEDIUM-RISK: domain-specific (style-auditor for code-style docs, etc.)
   - LOW-RISK: build-validator only (if build changes)

3. ✅ Write task.md with classification
   - Risk level
   - Task overview
   - Required agents list
   - Detailed requirements

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "🔍 CLASSIFIED (visit $NEW_VISIT_COUNT): Risk level + Agent list + Update task.md"
		fi
		;;

	"REQUIREMENTS")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 REQUIREMENTS PHASE - Stakeholder Consultation
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REQUIRED ACTIONS:
1. ✅ Invoke ALL identified stakeholder agents IN PARALLEL
   - Use single message with multiple Task tool calls
   - Pass task.md as input context

2. ✅ Collect agent requirement reports
   - Each agent writes: {task-name}-{agent-type}-requirements.md
   - Location: /workspace/tasks/{task-name}/ (task root)

3. ✅ Consolidate requirements into task.md
   - Add "Stakeholder Requirements Summary" section
   - Include key points from each agent
   - Preserve full agent reports for SYNTHESIS phase

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "📝 REQUIREMENTS (visit $NEW_VISIT_COUNT): Invoke agents in parallel + Collect reports + Update task.md"
		fi
		;;

	"SYNTHESIS")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎯 SYNTHESIS PHASE - Implementation Planning
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REQUIRED ACTIONS:
1. ✅ Read ALL stakeholder requirement reports
   - technical-architect: Architecture & design
   - security-auditor: Security requirements
   - code-quality-auditor: Quality standards
   - performance-analyzer: Performance constraints
   - style-auditor: Style compliance needs

2. ✅ Create comprehensive implementation plan
   - Architecture approach
   - Component breakdown
   - Implementation sequence
   - Testing strategy
   - Integration points

3. ✅ Write plan to task.md
   - Add "Implementation Plan" section
   - Include all architectural decisions
   - Document dependency order

4. ✅ Present plan to user for approval
   - Clear, readable markdown format
   - Wait for explicit approval before IMPLEMENTATION

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "🎯 SYNTHESIS (visit $NEW_VISIT_COUNT): Read agent reports + Create plan + Update task.md + Get user approval"
		fi
		;;

	"IMPLEMENTATION")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			# Get risk level from lock file
			RISK_LEVEL=$(jq -r '.risk_level // "UNKNOWN"' "$LOCK_FILE" 2>/dev/null || echo "UNKNOWN")

			if [[ "$RISK_LEVEL" == "HIGH-RISK" ]]; then
				cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️  IMPLEMENTATION PHASE - HIGH-RISK (Agent Delegation Required)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

MANDATORY APPROACH:
1. ✅ DELEGATE to technical-architect agent
   - Agent creates isolated worktree at:
     /workspace/tasks/{task-name}/agents/technical-architect/code/
   - Agent implements all code in THEIR worktree
   - Agent merges to task branch when complete

2. ❌ PROHIBITED ACTIONS
   - DO NOT write code files (.java, .ts, .py, etc.) yourself
   - DO NOT use Write/Edit tools for implementation code
   - DO NOT implement in task worktree directly

3. ✅ DELEGATION PATTERN
   Task("technical-architect", "Implement according to plan in task.md.
   Work in your isolated worktree. Merge to task branch when complete.")

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
			elif [[ "$RISK_LEVEL" == "MEDIUM-RISK" ]]; then
				cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ IMPLEMENTATION PHASE - MEDIUM-RISK (Direct Implementation)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

PERMITTED APPROACH:
1. ✅ Implement directly in task worktree
   - Use Write/Edit tools for code files
   - Follow implementation plan from task.md

2. ✅ Validation during implementation
   - Run ./mvnw compile after each component
   - Run ./mvnw test to verify changes
   - Fix issues immediately (fail-fast)

3. ✅ Domain-specific review required after completion
   - Will invoke relevant agents in REVIEW phase

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
			else
				cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ IMPLEMENTATION PHASE - LOW-RISK (Streamlined)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

STREAMLINED APPROACH:
1. ✅ Implement changes directly
2. ✅ Basic validation (formatting, links)
3. ✅ Build verification if applicable

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
			fi
		else
			echo "⚙️  IMPLEMENTATION (visit $NEW_VISIT_COUNT): Follow risk-appropriate pattern (check .claude/hooks/check-implementation-pattern.sh)"
		fi
		;;

	"VALIDATION")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🧪 VALIDATION PHASE - Build & Test Verification
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REQUIRED ACTIONS:
1. ✅ Full build verification
   - Run: ./mvnw clean compile
   - Must complete without errors

2. ✅ Test execution
   - Run: ./mvnw test
   - All tests must pass

3. ✅ Style validation (3-component check)
   - Checkstyle: ./mvnw checkstyle:check
   - PMD: ./mvnw pmd:check
   - Manual rules: Review docs/code-style/*-claude.md patterns

4. ✅ Fix any violations before proceeding to REVIEW

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "🧪 VALIDATION (visit $NEW_VISIT_COUNT): Build + Tests + Style (checkstyle + PMD + manual)"
		fi
		;;

	"REVIEW")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
👥 REVIEW PHASE - Stakeholder Agent Approval
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REQUIRED ACTIONS:
1. ✅ Invoke ALL stakeholder agents IN PARALLEL
   - Same agents from REQUIREMENTS phase
   - Each reviews implementation against their requirements

2. ✅ Collect review reports
   - Each agent writes: {task-name}-{agent-type}-review.md
   - Location: /workspace/tasks/{task-name}/ (task root)

3. ✅ Check for unanimous approval
   - ALL agents must respond: "FINAL DECISION: ✅ APPROVED"
   - ANY "❌ REJECTED" → Return to IMPLEMENTATION
   - Address feedback and re-run REVIEW

4. ✅ Only proceed to AWAITING_USER_APPROVAL after unanimous approval

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "👥 REVIEW (visit $NEW_VISIT_COUNT): Invoke agents in parallel + Check unanimous approval + Fix if rejected"
		fi
		;;

	"AWAITING_USER_APPROVAL")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✋ AWAITING_USER_APPROVAL PHASE - User Checkpoint
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REQUIRED ACTIONS:
1. ✅ Present implementation summary
   - Files created/modified
   - Test results
   - Agent approval status
   - Any notable decisions

2. ✅ Wait for explicit user approval
   - User must say: "approved", "LGTM", "proceed", etc.
   - If rejected: Return to IMPLEMENTATION and fix issues

3. ✅ Create approval marker when approved
   - File: /workspace/tasks/{task-name}/user-approval-obtained.flag
   - Required before COMPLETE state transition

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "✋ AWAITING_USER_APPROVAL (visit $NEW_VISIT_COUNT): Present summary + Wait for approval"
		fi
		;;

	"COMPLETE")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎉 COMPLETE PHASE - Finalization
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REQUIRED ACTIONS:
1. ✅ Merge task branch to main
   - Checkout main branch
   - Merge with squash: git merge --squash {task-name}
   - Commit with descriptive message

2. ✅ Update project documentation
   - REMOVE task from todo.md (delete entire entry)
   - ADD to changelog.md with completion details
   - Verify: git diff todo.md shows ONLY deletions

3. ✅ Commit all changes atomically
   - Include: implementation + todo.md + changelog.md
   - Format message with Co-Authored-By: Claude

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "🎉 COMPLETE (visit $NEW_VISIT_COUNT): Merge to main + Remove from todo.md + Add to changelog.md + Commit"
		fi
		;;

	"CLEANUP")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🧹 CLEANUP PHASE - Resource Deallocation
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REQUIRED ACTIONS:
1. ✅ Navigate to main worktree FIRST
   - CRITICAL: cd /workspace/tasks/main/code
   - NEVER remove worktree while inside it

2. ✅ Remove task worktree
   - Run: git worktree remove /workspace/tasks/{task-name}/code

3. ✅ Remove agent worktrees (if exist)
   - Pattern: git worktree remove /workspace/tasks/{task-name}/agents/*/code/

4. ✅ Remove lock file
   - rm /workspace/tasks/{task-name}/task.json

5. ✅ Remove stakeholder reports
   - rm /workspace/tasks/{task-name}/*-requirements.md
   - rm /workspace/tasks/{task-name}/*-review.md

6. ✅ Remove task.md
   - rm /workspace/tasks/{task-name}/task.md

7. ✅ Remove phase visit tracker
   - rm /workspace/tasks/{task-name}/.phase-visits

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "🧹 CLEANUP (visit $NEW_VISIT_COUNT): CD to main + Remove worktrees + Remove lock/reports/task.md"
		fi
		;;

	*)
		echo "⚠️  Unknown phase: $NEW_STATE"
		;;
esac
