#!/bin/bash
# Phase-specific guidance with targeted MD file reading instructions
# Provides just-in-time protocol guidance without requiring upfront MD reading
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

# Phase-specific guidance with targeted reading instructions
case "$NEW_STATE" in
	"INIT")
		if [[ "$VERBOSITY" == "FULL" ]]; then
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📋 INIT PHASE - Task Initialization
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   Read /workspace/main/docs/project/task-protocol-core.md lines 1583-1626
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
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔍 CLASSIFIED PHASE - Risk Assessment & Agent Identification
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   Read /workspace/main/docs/project/task-protocol-core.md lines 1986-2053
   (Section: "CLASSIFIED → REQUIREMENTS")

REQUIRED ACTIONS:
1. ✅ Determine risk level (HIGH/MEDIUM/LOW)
   - HIGH-RISK: src/**, pom.xml, security/**, .github/**, CLAUDE.md
   - MEDIUM-RISK: test files, code-style docs, configuration
   - LOW-RISK: general docs, todo.md, README

2. ✅ Identify required stakeholder agents
   - HIGH-RISK: architecture-reviewer, security-reviewer, quality-reviewer,
                performance-reviewer, style-reviewer, test-reviewer
   - MEDIUM-RISK: domain-specific subset
   - LOW-RISK: build-reviewer only (if build changes)

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
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 REQUIREMENTS PHASE - Stakeholder Consultation
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   Read /workspace/main/docs/project/task-protocol-core.md lines 2205-2388
   (Sections: "REQUIREMENTS → SYNTHESIS" and "REQUIREMENTS State Exit Verification")

REQUIRED ACTIONS:
1. ✅ Invoke ALL identified stakeholder agents IN PARALLEL
   - Use single message with multiple Task tool calls
   - Pass task.md as input context
   - Example: Task("architecture-reviewer", "..."), Task("quality-reviewer", "...")

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
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎯 SYNTHESIS PHASE - Implementation Planning
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   Read /workspace/main/docs/project/task-protocol-core.md lines 2736-2881
   (Sections: "IMPLEMENTATION State Entry Guards" and "SYNTHESIS → IMPLEMENTATION")

REQUIRED ACTIONS:
1. ✅ Read ALL stakeholder requirement reports
   - {task-name}-architecture-reviewer-requirements.md
   - {task-name}-quality-reviewer-requirements.md
   - {task-name}-style-reviewer-requirements.md
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

			cat << EOF
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚙️  IMPLEMENTATION PHASE - Risk Level: ${RISK_LEVEL}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   Read /workspace/main/docs/project/task-protocol-core.md lines 2881-3368
   (Sections: "SYNTHESIS → IMPLEMENTATION" and "MULTI-AGENT IMPLEMENTATION WORKFLOW")
   Also read: task-protocol-operations.md for implementation patterns

EOF

			if [[ "$RISK_LEVEL" == "HIGH-RISK" ]]; then
				cat << 'EOF'
⚠️  HIGH-RISK IMPLEMENTATION - MANDATORY DELEGATION:
1. ✅ DELEGATE to updater agents (NOT implement yourself)
   - Invoke {domain}-updater agents based on requirements
   - Each agent works in: /workspace/tasks/{task}/agents/{agent}-updater/code/
   - Agents implement, validate, then merge to task branch

2. ❌ PROHIBITED ACTIONS
   - DO NOT write code files (.java, .ts, .py, etc.) yourself
   - DO NOT use Write/Edit tools for implementation code
   - Main agent COORDINATES only

3. ✅ COORDINATION PATTERN
   - Invoke updater agents in parallel
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
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🧪 VALIDATION PHASE - Build & Test Verification
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   Read /workspace/main/docs/project/task-protocol-core.md lines 3368-3431
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
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
👥 REVIEW PHASE - Stakeholder Agent Approval
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   Read /workspace/main/docs/project/task-protocol-core.md lines 3431-3513
   (Section: "VALIDATION → REVIEW")

REQUIRED ACTIONS:
1. ✅ Invoke ALL stakeholder reviewer agents IN PARALLEL
   - Same agents from REQUIREMENTS phase
   - Each reviews implementation against their requirements
   - Pattern: Task("architecture-reviewer", "..."), Task("quality-reviewer", "...")

2. ✅ Collect review reports
   - Each agent writes status.json with decision: APPROVED or REJECTED
   - Location: /workspace/tasks/{task-name}/agents/{agent}-reviewer/status.json

3. ✅ Check for unanimous approval
   - ALL agents must have decision: "APPROVED"
   - ANY "REJECTED" → Invoke corresponding updater agents
   - Updaters fix issues, merge, reviewers re-review
   - Repeat until all APPROVED

4. ✅ Iterative rounds until consensus
   - Round N: Reviewers review → some REJECT with feedback
   - Round N+1: Updaters fix → merge → reviewers re-review
   - Continue until unanimous approval

✅ PHASE COMPLETE WHEN:
   - All reviewer agents report decision: APPROVED
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
			cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎉 COMPLETE PHASE - Finalization
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 DETAILED PROTOCOL:
   Read /workspace/main/docs/project/task-protocol-core.md lines 3647-3673
   (Section: "COMPLETE → CLEANUP")
   Also read: /workspace/main/docs/project/git-workflow.md for merge procedures

REQUIRED ACTIONS:
1. ✅ Merge task branch to main
   - cd /workspace/main/code (main worktree)
   - git merge --squash {task-name}
   - Commit with descriptive message + Co-Authored-By: Claude

2. ✅ Update project documentation
   - REMOVE task from todo.md (delete entire task entry)
   - ADD to changelog.md with completion details
   - Verify: git diff todo.md shows ONLY deletions

3. ✅ Commit all changes atomically
   - Include: implementation + todo.md + changelog.md
   - Single atomic commit

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
		echo "⚠️  Unknown phase: $NEW_STATE - Read /workspace/main/docs/project/task-protocol-core.md for guidance"
		;;
esac
