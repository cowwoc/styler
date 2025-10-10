#!/bin/bash
# detect-review-completion.sh
# Detects when Phase 6 REVIEW completes with unanimous approval
# and injects reminder to create commit for user review

# Configuration
TRIGGER_PATTERNS=(
  "unanimous.*approval"
  "all.*stakeholder.*approved"
  "all.*agents.*approved"
  "[5-9]/[5-9].*approved"
  "stakeholder.*reviews.*approved"
)

SKIP_IF_ALREADY_SHOWN=(
  "git commit"
  "git show --stat"
  "implementation complete.*ready for review"
  "please review the changes"
)

# Read assistant's last response from stdin
RESPONSE=$(cat)

# Check if response contains any trigger patterns
TRIGGERED=false
for pattern in "${TRIGGER_PATTERNS[@]}"; do
  if echo "$RESPONSE" | grep -iE "$pattern" > /dev/null 2>&1; then
    TRIGGERED=true
    break
  fi
done

# Exit early if no trigger detected
if [ "$TRIGGERED" = "false" ]; then
  exit 0
fi

# Check if commit workflow already shown
for skip_pattern in "${SKIP_IF_ALREADY_SHOWN[@]}"; do
  if echo "$RESPONSE" | grep -iE "$skip_pattern" > /dev/null 2>&1; then
    # Already showed commit workflow - don't inject again
    exit 0
  fi
done

# Check if asking for approval without showing commit
ASKING_APPROVAL=false
APPROVAL_PATTERNS=(
  "awaiting.*approval"
  "ready for.*phase 7"
  "proceed.*phase 7"
  "approval.*proceed"
)

for pattern in "${APPROVAL_PATTERNS[@]}"; do
  if echo "$RESPONSE" | grep -iE "$pattern" > /dev/null 2>&1; then
    ASKING_APPROVAL=true
    break
  fi
done

# Only inject if asking for approval without creating commit
if [ "$ASKING_APPROVAL" = "true" ]; then
  cat <<'INJECTION'

## 🚨 MANDATORY CHECKPOINT: Create Commit for User Review

You have achieved unanimous stakeholder approval. Before proceeding, you MUST:

1. **Create implementation commit** with all changes:
   ```bash
   # Stage all modified files
   git add -A

   # Create commit with comprehensive message (see CLAUDE.md § Checkpoint 2)
   git commit -m "$(cat <<'EOF'
   [Task name]: [Brief summary]

   [Detailed implementation description]

   Changes:
   - [Major changes]
   - [File modifications]
   - [Test coverage]

   Stakeholder Approvals:
   - Technical Architect: ✅ APPROVED
   - Security Auditor: ✅ APPROVED
   - Code Quality Auditor: ✅ APPROVED
   - Performance Analyzer: ✅ APPROVED
   - Style Auditor: ✅ APPROVED

   Test Results: [X/X passing]

   🤖 Generated with [Claude Code](https://claude.com/claude-code)

   Co-Authored-By: Claude <noreply@anthropic.com>
   EOF
   )"

   # Show commit summary
   git show --stat

   # Show detailed diff
   git diff HEAD~1
   ```

2. **Present commit to user:**
   ```
   ## Implementation Complete - Ready for Review

   I have completed the [task-name] implementation with unanimous stakeholder approval.

   **Stakeholder Reviews:** [5/5 approved]
   **Test Results:** [X tests passing]
   **Files Modified:** [list key files]

   **Commit Created:** [commit hash]

   Please review the changes above. Reply with:
   - "Approved" or "LGTM" to proceed with merge to main
   - "Rejected" or specific feedback to make revisions
   ```

3. **Wait for explicit user approval** before Phase 7 (COMPLETE)

**DO NOT:**
- ❌ Say "awaiting approval" without showing what you're asking approval FOR
- ❌ Proceed to Phase 7 without user approval message
- ❌ Skip commit creation

**CORRECT SEQUENCE:**
1. Achieve unanimous stakeholder approval ✅ (you are here)
2. Create commit and show changes ⏳ (DO THIS NOW)
3. Present to user and wait for approval ⏳ (THEN THIS)
4. Only after user approves: proceed to Phase 7 ⏳

**Pattern to follow:** See CLAUDE.md § "PHASE 6 → USER APPROVAL → PHASE 7 EXAMPLE" for the exact correct pattern.

INJECTION
  echo "$RESPONSE"
  exit 0
fi

# No violations detected - don't output anything
