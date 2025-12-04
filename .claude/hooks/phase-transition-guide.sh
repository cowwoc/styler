#!/bin/bash
# Phase-specific guidance with targeted MD file reading instructions
# Provides just-in-time protocol guidance without requiring upfront MD reading
# Performance: ~5ms (state file lookup + conditional output)

set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in phase-transition-guide.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source documentation reference resolver
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/lib/doc-reference-resolver.sh" 2>/dev/null || true

TASK_NAME="${1:-unknown}"
NEW_STATE="${2:-UNKNOWN}"

# Exit silently if no task context (not in task protocol)
if [[ "$TASK_NAME" == "unknown" ]]; then
	exit 0
fi

LOCK_FILE="/workspace/tasks/${TASK_NAME}/task.json"
VISIT_TRACKER="/workspace/tasks/${TASK_NAME}/.phase-visits"

# Exit silently if task directory doesn't exist
if [[ ! -d "/workspace/tasks/${TASK_NAME}" ]]; then
	exit 0
fi

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

# Phase-specific guidance with targeted reading instructions
case "$NEW_STATE" in
	"INIT")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			INIT_REF=$(resolve_doc_ref "task-protocol-core.md#init-classified" 2>/dev/null || echo "Read /workspace/main/docs/project/task-protocol-core.md section #init-classified")
			cat << EOF
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📋 INIT PHASE - Task Initialization
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   ${INIT_REF}
   (Section: "INIT → CLASSIFIED")

REQUIRED ACTIONS:
1. ✅ Create lock file at /workspace/tasks/{task-name}/task.json
   - Include: session_id, task_name, state: "INIT", created_at

2. ✅ Create isolated worktree
   - Pattern: git worktree add /workspace/tasks/{task-name}/code -b {task-name}
   - IMMEDIATELY cd to worktree after creation

3. ✅ Create task.md placeholder
   - Location: /workspace/tasks/{task-name}/task.md
   - Content: Empty or basic template

4. ✅ Verify isolation
   - Run: pwd
   - Expected: /workspace/tasks/{task-name}/code

✅ PHASE COMPLETE WHEN:
   - Lock file exists with INIT state
   - Task worktree created and is current directory
   - task.md placeholder exists

➡️  NEXT PHASE: CLASSIFIED
   Transition: Update lock state:
   jq '.state = "CLASSIFIED"' /workspace/tasks/{task-name}/task.json > /tmp/lock.tmp
   mv /tmp/lock.tmp /workspace/tasks/{task-name}/task.json

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "📋 INIT (visit $NEW_VISIT_COUNT): Lock + Worktree + CD + task.md | Next: CLASSIFIED"
		fi
		;;

	"CLASSIFIED")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			CLASSIFIED_REF=$(resolve_doc_ref "task-protocol-core.md#classified-requirements" 2>/dev/null || echo "Read /workspace/main/docs/project/task-protocol-core.md section #classified-requirements")
			cat << EOF
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔍 CLASSIFIED PHASE - Risk Assessment & Agent Identification
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   ${CLASSIFIED_REF}
   (Section: "CLASSIFIED → REQUIREMENTS")

REQUIRED ACTIONS:
1. ✅ Determine risk level (HIGH/MEDIUM/LOW)
   - HIGH-RISK: src/**, pom.xml, security/**, .github/**, CLAUDE.md
   - MEDIUM-RISK: test files, code-style docs, configuration
   - LOW-RISK: general docs, todo.md, README

2. ✅ Identify required stakeholder agents
   - HIGH-RISK: architect, hacker, engineer,
                optimizer, formatter, tester
   - MEDIUM-RISK: domain-specific subset
   - LOW-RISK: builder only (if build changes)

3. ✅ Write task.md with classification
   - Risk level
   - Task overview
   - Required agents list
   - Detailed requirements

✅ PHASE COMPLETE WHEN:
   - task.md contains risk level and agent list
   - All required agents identified

➡️  NEXT PHASE: REQUIREMENTS
   Transition: Update lock state to REQUIREMENTS

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "🔍 CLASSIFIED (visit $NEW_VISIT_COUNT): Risk level + Agents + task.md | Next: REQUIREMENTS"
		fi
		;;

	"REQUIREMENTS")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			REQUIREMENTS_REF=$(resolve_doc_ref "task-protocol-core.md#requirements-state-exit-verification-procedure" 2>/dev/null || echo "Read /workspace/main/docs/project/task-protocol-core.md section #requirements-state-exit-verification-procedure")
			cat << EOF
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 REQUIREMENTS PHASE - Stakeholder Consultation
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   ${REQUIREMENTS_REF}
   (Sections: "REQUIREMENTS → SYNTHESIS" and "REQUIREMENTS State Exit Verification")

REQUIRED ACTIONS:
1. ✅ Invoke ALL identified stakeholder agents IN PARALLEL
   - Use single message with multiple Task tool calls
   - Pass task.md as input context
   - Example: Task("architect", "..."), Task("engineer", "...")

2. ✅ Wait for ALL agents to complete
   - Monitor agent completion status
   - Collect requirement reports from /workspace/tasks/{task-name}/

3. ✅ Consolidate requirements into task.md
   - Add "Stakeholder Requirements Summary" section
   - Include key points from each agent
   - Preserve full reports for SYNTHESIS phase

✅ PHASE COMPLETE WHEN:
   - All stakeholder agents have completed
   - All requirement reports collected
   - task.md updated with consolidated requirements

➡️  NEXT PHASE: SYNTHESIS
   Transition: Update lock state to SYNTHESIS

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "📝 REQUIREMENTS (visit $NEW_VISIT_COUNT): Invoke agents parallel + Collect + Consolidate | Next: SYNTHESIS"
		fi
		;;

	"SYNTHESIS")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			SYNTHESIS_REF=$(resolve_doc_ref "task-protocol-core.md#implementation-state-entry-guards-critical-enforcement" 2>/dev/null || echo "Read /workspace/main/docs/project/task-protocol-core.md section #implementation-state-entry-guards-critical-enforcement")
			cat << EOF
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎯 SYNTHESIS PHASE - Implementation Planning
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   ${SYNTHESIS_REF}
   (Sections: "IMPLEMENTATION State Entry Guards" and "SYNTHESIS → IMPLEMENTATION")

REQUIRED ACTIONS:
1. ✅ Read ALL stakeholder requirement reports
   - {task-name}-architect-requirements.md
   - {task-name}-engineer-requirements.md
   - {task-name}-formatter-requirements.md
   - etc.

2. ✅ Create comprehensive implementation plan
   - Architecture approach and design patterns
   - Component breakdown and file structure
   - Implementation sequence and dependencies
   - Testing strategy and validation criteria
   - Integration points and interfaces

3. ✅ Write plan to task.md
   - Add "Implementation Plan" section
   - Include all architectural decisions
   - Document dependency order

4. ✅ Present plan to user for approval
   - Clear, readable markdown format
   - Explain approach and rationale
   - Wait for explicit approval before IMPLEMENTATION

✅ PHASE COMPLETE WHEN:
   - Implementation plan written to task.md
   - User has explicitly approved plan
   - Ready to begin implementation

➡️  NEXT PHASE: IMPLEMENTATION
   Transition: After user approval, update lock state to IMPLEMENTATION

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "🎯 SYNTHESIS (visit $NEW_VISIT_COUNT): Read reports + Create plan + Get approval | Next: IMPLEMENTATION"
		fi
		;;

	"IMPLEMENTATION")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			# Get risk level from lock file
			RISK_LEVEL=$(jq -r '.risk_level // "UNKNOWN"' "$LOCK_FILE" 2>/dev/null || echo "UNKNOWN")
			IMPLEMENTATION_REF=$(resolve_doc_ref "task-protocol-core.md#multi-agent-implementation-workflow" 2>/dev/null || echo "Read /workspace/main/docs/project/task-protocol-core.md section #multi-agent-implementation-workflow")

			cat << EOF
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚙️  IMPLEMENTATION PHASE - Risk Level: ${RISK_LEVEL}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   ${IMPLEMENTATION_REF}
   (Sections: "SYNTHESIS → IMPLEMENTATION" and "MULTI-AGENT IMPLEMENTATION WORKFLOW")
   Also read: task-protocol-operations.md for implementation patterns

EOF

			if [[ "$RISK_LEVEL" == "HIGH-RISK" ]]; then
				cat << 'EOF'
⚠️  HIGH-RISK IMPLEMENTATION - MANDATORY DELEGATION:
1. ✅ DELEGATE to stakeholder agents in IMPLEMENTATION mode (NOT implement yourself)
   - Invoke stakeholder agents based on requirements
   - Each agent works in: /workspace/tasks/{task}/agents/{agent}/code/
   - Agents implement, validate, then merge to task branch

2. ❌ PROHIBITED ACTIONS
   - DO NOT write code files (.java, .ts, .py, etc.) yourself
   - DO NOT use Write/Edit tools for implementation code
   - Main agent COORDINATES only

3. ✅ COORDINATION PATTERN
   - Invoke stakeholder agents in IMPLEMENTATION mode in parallel
   - Monitor status.json files for completion
   - Collect merged changes on task branch
   - Iterate rounds until all agents report COMPLETE

EOF
			elif [[ "$RISK_LEVEL" == "MEDIUM-RISK" ]]; then
				cat << 'EOF'
✅ MEDIUM-RISK IMPLEMENTATION - DIRECT WITH VALIDATION:
1. ✅ Implement directly in task worktree
   - Use Write/Edit tools for code files
   - Follow implementation plan from task.md

2. ✅ Continuous validation during implementation
   - Run ./mvnw compile after each component
   - Run ./mvnw test to verify changes
   - Fix issues immediately (fail-fast approach)

EOF
			else
				cat << 'EOF'
✅ LOW-RISK IMPLEMENTATION - STREAMLINED:
1. ✅ Implement changes directly
2. ✅ Basic validation (formatting, links, build if applicable)

EOF
			fi

			cat << 'EOF'
✅ PHASE COMPLETE WHEN:
   - All implementation complete per plan
   - Code compiles successfully
   - Ready for validation

➡️  NEXT PHASE: VALIDATION
   Transition: Update lock state to VALIDATION

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "⚙️  IMPLEMENTATION (visit $NEW_VISIT_COUNT): Follow plan + Validate continuously | Next: VALIDATION"
		fi
		;;

	"VALIDATION")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			VALIDATION_REF=$(resolve_doc_ref "task-protocol-core.md#implementation-validation" 2>/dev/null || echo "Read /workspace/main/docs/project/task-protocol-core.md section #implementation-validation")
			cat << EOF
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🧪 VALIDATION PHASE - Build & Test Verification
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   ${VALIDATION_REF}
   (Section: "IMPLEMENTATION → VALIDATION")

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

4. ✅ Fix any violations
   - Return to IMPLEMENTATION if validation fails
   - Fix and re-validate

✅ PHASE COMPLETE WHEN:
   - Build succeeds
   - All tests pass
   - All style checks pass

➡️  NEXT PHASE: REVIEW (if HIGH/MEDIUM risk) or COMPLETE (if LOW risk)
   Transition: Update lock state based on risk level

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "🧪 VALIDATION (visit $NEW_VISIT_COUNT): Build + Tests + Style checks | Next: REVIEW or COMPLETE"
		fi
		;;

	"REVIEW")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			REVIEW_REF=$(resolve_doc_ref "task-protocol-core.md#validation-review-conditional-path" 2>/dev/null || echo "Read /workspace/main/docs/project/task-protocol-core.md section #validation-review-conditional-path")
			cat << EOF
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
👥 REVIEW PHASE - Stakeholder Agent Approval
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   ${REVIEW_REF}
   (Section: "VALIDATION → REVIEW")

REQUIRED ACTIONS:
1. ✅ Invoke ALL stakeholder agents in VALIDATION mode IN PARALLEL
   - Same agents from REQUIREMENTS phase
   - Each reviews implementation against their requirements
   - Pattern: Task("architect", "..."), Task("engineer", "...")

2. ✅ Collect review reports
   - Each agent writes status.json with decision: APPROVED or REJECTED
   - Location: /workspace/tasks/{task-name}/agents/{agent}/status.json

3. ✅ Check for unanimous approval
   - ALL agents must have decision: "APPROVED"
   - ANY "REJECTED" → Invoke corresponding stakeholder agents in IMPLEMENTATION mode
   - Implementation agents fix issues, merge, validation agents re-review
   - Repeat until all APPROVED

4. ✅ Iterative rounds until consensus
   - Round N: Validation agents review → some REJECT with feedback
   - Round N+1: Implementation agents fix → merge → validation agents re-review
   - Continue until unanimous approval

✅ PHASE COMPLETE WHEN:
   - All stakeholder agents in VALIDATION mode report decision: APPROVED
   - No REJECTED decisions remain

➡️  NEXT PHASE: COMPLETE
   Transition: Update lock state to COMPLETE

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "👥 REVIEW (visit $NEW_VISIT_COUNT): Invoke reviewers + Check approval + Iterate if needed | Next: COMPLETE"
		fi
		;;

	"COMPLETE")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			COMPLETE_REF=$(resolve_doc_ref "task-protocol-core.md#complete-cleanup" 2>/dev/null || echo "Read /workspace/main/docs/project/task-protocol-core.md section #complete-cleanup")
			cat << EOF
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎉 COMPLETE PHASE - Finalization
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   ${COMPLETE_REF}
   (Section: "COMPLETE → CLEANUP")
   Also read: /workspace/main/docs/project/git-workflow.md for merge procedures

REQUIRED ACTIONS:
1. ✅ Squash commits on task branch (include archival)
   - cd /workspace/tasks/{task-name}/code
   - Update todo.md and changelog.md
   - git rebase -i (squash all commits into ONE)

2. ✅ Merge task branch to main with --ff-only
   - cd /workspace/main (main worktree)
   - git merge --ff-only {task-name}
   - (No new commit - fast-forward only)

✅ PHASE COMPLETE WHEN:
   - Changes merged to main branch
   - todo.md updated (task removed)
   - changelog.md updated (completion recorded)
   - All changes committed

➡️  NEXT PHASE: CLEANUP
   Transition: Update lock state to CLEANUP

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "🎉 COMPLETE (visit $NEW_VISIT_COUNT): Merge + Update todo/changelog + Commit | Next: CLEANUP"
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
   - CRITICAL: cd /workspace/main/code
   - NEVER remove worktree while inside it

2. ✅ Remove task worktree
   - Run: git worktree remove /workspace/tasks/{task-name}/code

3. ✅ Remove agent worktrees (if exist)
   - Pattern: git worktree remove /workspace/tasks/{task-name}/agents/*/code/

4. ✅ Remove lock file
   - rm /workspace/tasks/{task-name}/task.json

5. ✅ Remove stakeholder reports and artifacts
   - rm /workspace/tasks/{task-name}/*-requirements.md
   - rm /workspace/tasks/{task-name}/*-review.md
   - rm /workspace/tasks/{task-name}/agents/*/status.json
   - rm /workspace/tasks/{task-name}/task.md
   - rm /workspace/tasks/{task-name}/.phase-visits

✅ PHASE COMPLETE WHEN:
   - All worktrees removed
   - All task artifacts deleted
   - Task directory clean

➡️  TASK COMPLETE
   No further state transitions. Task protocol execution finished.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
		else
			echo "🧹 CLEANUP (visit $NEW_VISIT_COUNT): CD to main + Remove worktrees + Delete artifacts | TASK COMPLETE"
		fi
		;;

	*)
		DOC_REF=$(resolve_doc_ref "task-protocol-core.md#core-states" 2>/dev/null || echo "/workspace/main/docs/project/task-protocol-core.md")
		echo "⚠️  Unknown phase: $NEW_STATE - Read $DOC_REF for guidance"
		;;
esac
