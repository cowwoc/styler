#!/bin/bash
set -euo pipefail

# enforce-user-approval.sh
# Enforces BOTH mandatory user approval checkpoints using lock file state tracking:
# 1. After SYNTHESIS (before implementation) - state: SYNTHESIS_AWAITING_APPROVAL
# 2. After REVIEW (before merge) - state: REVIEW_AWAITING_APPROVAL

# Find active task lock file
LOCK_FILE=$(find /workspace/locks -name "*.json" -type f 2>/dev/null | head -1)

# Exit early if no lock file exists
if [ -z "$LOCK_FILE" ] || [ ! -f "$LOCK_FILE" ]; then
    exit 0
fi

# Configuration for state transitions that require checkpoints
SYNTHESIS_COMPLETE_PATTERNS=(
  "synthesis.*complete"
  "implementation plan.*complete"
  "requirements.*consolidated"
  "unified.*architecture.*plan"
)

PROCEEDING_TO_IMPLEMENTATION_PATTERNS=(
  "proceeding.*context.*phase"
  "proceeding.*implementation"
  "starting.*implementation"
  "autonomous.*implementation"
  "moving.*to.*context"
)

REVIEW_COMPLETE_PATTERNS=(
  "unanimous.*approval"
  "all.*stakeholder.*approved"
  "all.*agents.*approved"
  "[5-9]/[5-9].*approved"
  "stakeholder.*reviews.*approved"
)

PROCEEDING_TO_COMPLETE_PATTERNS=(
  "proceeding.*to.*complete"
  "proceeding.*to.*phase.*7"
  "moving.*to.*complete"
  "merging.*to.*main"
)

# Read assistant's last response from stdin
RESPONSE=$(cat)

# Get current state from lock file if it exists
CURRENT_STATE=""
if [ -n "$LOCK_FILE" ] && [ -f "$LOCK_FILE" ]; then
  CURRENT_STATE=$(jq -r '.state // "UNKNOWN"' "$LOCK_FILE" 2>/dev/null)
fi

# === CHECKPOINT 1: SYNTHESIS → SYNTHESIS_AWAITING_APPROVAL ===

# Check if SYNTHESIS was just completed
SYNTHESIS_COMPLETE=false
for pattern in "${SYNTHESIS_COMPLETE_PATTERNS[@]}"; do
  if echo "$RESPONSE" | grep -iE "$pattern" > /dev/null 2>&1; then
    SYNTHESIS_COMPLETE=true
    break
  fi
done

# Check if trying to proceed to implementation without approval
PROCEEDING_TO_IMPL=false
for pattern in "${PROCEEDING_TO_IMPLEMENTATION_PATTERNS[@]}"; do
  if echo "$RESPONSE" | grep -iE "$pattern" > /dev/null 2>&1; then
    PROCEEDING_TO_IMPL=true
    break
  fi
done

# VIOLATION: Completed SYNTHESIS but not in awaiting state
if [ "$SYNTHESIS_COMPLETE" = "true" ] && [ "$CURRENT_STATE" != "SYNTHESIS_AWAITING_APPROVAL" ]; then
  cat <<'SYNTHESIS_VIOLATION'

## 🚨 CRITICAL VIOLATION: SYNTHESIS Checkpoint

You completed SYNTHESIS but did NOT transition to SYNTHESIS_AWAITING_APPROVAL state.

**MANDATORY STATE TRANSITION:**
After presenting the implementation plan, you MUST update the lock file state:

```bash
jq '.state = "SYNTHESIS_AWAITING_APPROVAL"' /workspace/locks/implement-security-controls.json > /tmp/lock.json && mv /tmp/lock.json /workspace/locks/implement-security-controls.json
```

**CORRECT WORKFLOW:**
1. Complete SYNTHESIS (consolidate requirements into plan)
2. Present implementation plan to user in markdown format
3. **Update lock file state to SYNTHESIS_AWAITING_APPROVAL**
4. **STOP and wait for user response**
5. User approves → Update state to CONTEXT and proceed
6. User rejects → Update state back to SYNTHESIS and revise

**YOU MUST STOP NOW** - Update lock state and wait for user approval.

SYNTHESIS_VIOLATION
  echo "$RESPONSE"
  exit 0
fi

# VIOLATION: Trying to proceed to implementation without approval
if [ "$PROCEEDING_TO_IMPL" = "true" ] && [ "$CURRENT_STATE" != "CONTEXT" ]; then
  cat <<IMPL_WITHOUT_APPROVAL

## 🚨 CRITICAL VIOLATION: Proceeding Without User Approval

You are trying to proceed to CONTEXT/IMPLEMENTATION but lock file state is: $CURRENT_STATE

**Required state for implementation**: CONTEXT
**Current state**: $CURRENT_STATE

**If you just presented a plan:**
1. Update lock state to SYNTHESIS_AWAITING_APPROVAL
2. STOP and wait for user approval

**If user approved:**
1. Update lock state to CONTEXT
2. Then proceed with implementation

**NEVER proceed to implementation without:**
- Lock file state = CONTEXT
- User explicitly approving the plan

IMPL_WITHOUT_APPROVAL
  echo "$RESPONSE"
  exit 0
fi

# === CHECKPOINT 2: REVIEW → REVIEW_AWAITING_APPROVAL ===

# Check if REVIEW was just completed
REVIEW_COMPLETE=false
for pattern in "${REVIEW_COMPLETE_PATTERNS[@]}"; do
  if echo "$RESPONSE" | grep -iE "$pattern" > /dev/null 2>&1; then
    REVIEW_COMPLETE=true
    break
  fi
done

# Check if trying to proceed to COMPLETE without approval
PROCEEDING_TO_COMPLETE=false
for pattern in "${PROCEEDING_TO_COMPLETE_PATTERNS[@]}"; do
  if echo "$RESPONSE" | grep -iE "$pattern" > /dev/null 2>&1; then
    PROCEEDING_TO_COMPLETE=true
    break
  fi
done

# VIOLATION: Completed REVIEW but not in awaiting state
if [ "$REVIEW_COMPLETE" = "true" ] && [ "$CURRENT_STATE" != "REVIEW_AWAITING_APPROVAL" ]; then
  # Check if commit already created
  COMMIT_SHOWN=false
  if echo "$RESPONSE" | grep -iE "(git commit|git show|implementation complete.*ready for review)" > /dev/null 2>&1; then
    COMMIT_SHOWN=true
  fi

  if [ "$COMMIT_SHOWN" = "false" ]; then
    cat <<'REVIEW_VIOLATION'

## 🚨 CRITICAL VIOLATION: REVIEW Checkpoint

You achieved unanimous stakeholder approval but did NOT:
1. Create git commit
2. Update lock file state to REVIEW_AWAITING_APPROVAL

**MANDATORY ACTIONS:**

1. **Create implementation commit:**
   ```bash
   git add -A
   git commit -m "Implementation message..."
   git show --stat
   ```

2. **Update lock file state:**
   ```bash
   jq '.state = "REVIEW_AWAITING_APPROVAL"' /workspace/locks/implement-security-controls.json > /tmp/lock.json && mv /tmp/lock.json /workspace/locks/implement-security-controls.json
   ```

3. **Present to user and STOP**

**CORRECT WORKFLOW:**
1. Achieve unanimous approval ✅ (you are here)
2. Create commit + update state to REVIEW_AWAITING_APPROVAL
3. STOP and wait for user
4. User approves → Update state to COMPLETE and proceed
5. User rejects → Update state back to CONVERGENCE and revise

REVIEW_VIOLATION
    echo "$RESPONSE"
    exit 0
  fi
fi

# VIOLATION: Trying to proceed to COMPLETE without approval
if [ "$PROCEEDING_TO_COMPLETE" = "true" ] && [ "$CURRENT_STATE" != "COMPLETE" ]; then
  cat <<COMPLETE_WITHOUT_APPROVAL

## 🚨 CRITICAL VIOLATION: Proceeding to COMPLETE Without Approval

Lock file state is: $CURRENT_STATE (must be COMPLETE to proceed)

**If you just created a commit:**
1. Update lock state to REVIEW_AWAITING_APPROVAL
2. STOP and wait for user approval

**If user approved:**
1. Update lock state to COMPLETE
2. Then proceed with merge/cleanup

COMPLETE_WITHOUT_APPROVAL
  echo "$RESPONSE"
  exit 0
fi

# No violations detected - don't output anything
