# TDD Implementation Workflow

**MANDATORY for ALL Java development. Production code edits are BLOCKED without this skill.**

**Auto-activates when:**
- Adding features to Java code
- Fixing bugs in Java code
- Implementing new validation/detection logic
- Creating prevention mechanisms

---

## ⚠️ THIS IS A GATEKEEPER SKILL

The `tdd-skill-gate.sh` and `tdd-bash-guard.sh` hooks BLOCK all edits to `src/main/java/` unless this
skill is active.

When you try to edit Java production code without this skill, you'll see:
```
❌ TDD SKILL GATE - BLOCKED ❌
You must invoke: Skill: tdd-implementation
```

---

## How This Skill Works

This skill manages a **state machine** with verified phase transitions:

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                              TDD STATE MACHINE                                    │
├──────────────────────────────────────────────────────────────────────────────────┤
│                                                                                   │
│   [START] ──► [RED] ──► [GREEN] ──► [REFACTOR] ──► [VERIFY] ──► [COMPLETE]       │
│               │  ▲       │           │              │                             │
│               ▼  │       ▼           ▼              ▼                             │
│           Write  │   Write impl   Clean up     Run ORIGINAL                      │
│           test   │   Run test     Run tests    use-case!                         │
│           MUST   │   MUST PASS    MUST PASS       │                              │
│           FAIL   │                                │                              │
│                  │                    ┌───────────┴───────────┐                  │
│                  │                    ▼                       ▼                  │
│                  │               STILL FAILS?             WORKS!                 │
│                  │                    │                       │                  │
│                  └────────────────────┘                       ▼                  │
│                   Test didn't capture                    [COMPLETE]              │
│                   the REAL bug - write                                           │
│                   NEW test that fails                                            │
│                   for same reason as                                             │
│                   original use-case                                              │
│                                                                                   │
└──────────────────────────────────────────────────────────────────────────────────┘
```

**Phase transitions are VERIFIED by actually running tests.**

---

## STEP 1: INITIALIZE TDD MODE

When this skill activates, get the session ID from the hook:

**Method: Attempt an edit and read the session ID from the error message**

The hook will show you the exact session ID and command to run:
```
Session ID: 5a875ef2-7bff-4355-a9dc-ebf77a9eb2a0
Required file: /tmp/tdd_skill_active_5a875ef2-7bff-4355-a9dc-ebf77a9eb2a0

Run this command to initialize TDD mode (copy-paste ready):
echo '{"phase":"GREEN",...}' > /tmp/tdd_skill_active_...
```

**Then create the TDD mode file with the session ID shown:**

```bash
# Use the EXACT session ID shown in the hook error message
SESSION_ID="<session-id-from-hook-error>"

# Create TDD mode file
cat > /tmp/tdd_skill_active_${SESSION_ID} << EOF
{
  "phase": "RED",
  "session_id": "${SESSION_ID}",
  "target_class": "YOUR_CLASS_NAME",
  "test_class": "YOUR_CLASS_NAMETest",
  "test_failed": false,
  "test_passed": false,
  "started_at": "$(date -Iseconds)"
}
EOF
```

**Replace YOUR_CLASS_NAME with the actual class you're working on.**

**⚠️ CRITICAL: The session_id field MUST match the current session.**
Unknown or mismatched session IDs are treated as invalid and will BLOCK edits.

---

## ⚠️ CHANGING EXISTING BEHAVIOR vs NEW FEATURES

**CRITICAL: Do NOT create duplicate tests when changing behavior.**

### Adding NEW Feature:
- Create a NEW test method
- Test should FAIL because feature doesn't exist
- Implement feature → test passes

### Changing EXISTING Behavior (Bug Fix / Behavior Change):
- **UPDATE** the existing test to reflect the NEW expected behavior, OR
- **DELETE** the old test and create a new one

**WRONG approach (creates conflicts):**
```java
// Old test expects white font
testAutoFixesWithWhiteFont() { ... expects white ... }

// New test expects font index 0
testAutoFixUsesFontIndexZero() { ... expects index 0 ... }
// ❌ Now you have TWO conflicting tests!
```

**CORRECT approach:**
```java
// Update the existing test to reflect new behavior
testAutoFixesFont() {
    ... expects font index 0 (the NEW correct behavior) ...
}
// ✓ One test, one source of truth
```

### How to Identify:
- **New feature**: No existing test covers this functionality
- **Behavior change**: Existing test will PASS with old code, FAIL with new code
  - Find the existing test first
  - Update its assertions to expect the NEW behavior
  - Now it fails (RED) → implement → passes (GREEN)

---

## STEP 2: RED PHASE - Write Failing Test

### Actions:
1. Create/modify test file in `src/test/java/`
2. **If changing behavior**: Find and UPDATE existing test, don't create duplicate
3. Write test that defines expected behavior
4. Run the test

### Run Test Command:
```bash
mvn test -Dtest=YourClassNameTest 2>&1
```

### Verify Test FAILS:
- Look for `Tests run: X, Failures: Y` where Y > 0
- Or `BUILD FAILURE` with test failures

### Update TDD Mode File:
```bash
# After verifying test fails, update the mode file
cat > /tmp/tdd_skill_active_${SESSION_ID} << EOF
{
  "phase": "GREEN",
  "session_id": "${SESSION_ID}",
  "target_class": "YOUR_CLASS_NAME",
  "test_class": "YOUR_CLASS_NAMETest",
  "test_failed": true,
  "test_passed": false,
  "started_at": "$(date -Iseconds)"
}
EOF
```

**⚠️ CRITICAL: You CANNOT proceed to implementation until test_failed is true.**

The `tdd-skill-gate.sh` hook checks this and will BLOCK production edits.

---

## STEP 3: GREEN PHASE - Implement Code

### Actions:
1. NOW you can edit production code in `src/main/java/`
2. Write minimal code to make test pass
3. Run the test again

### Run Test Command:
```bash
mvn test -Dtest=YourClassNameTest 2>&1
```

### Verify Test PASSES:
- Look for `Tests run: X, Failures: 0, Errors: 0`
- And `BUILD SUCCESS`

### Update TDD Mode File:
```bash
cat > /tmp/tdd_skill_active_${SESSION_ID} << EOF
{
  "phase": "REFACTOR",
  "session_id": "${SESSION_ID}",
  "target_class": "YOUR_CLASS_NAME",
  "test_class": "YOUR_CLASS_NAMETest",
  "test_failed": true,
  "test_passed": true,
  "started_at": "$(date -Iseconds)"
}
EOF
```

---

## STEP 4: REFACTOR PHASE - Clean Up

### Actions:
1. Clean up implementation if needed
2. Remove any debug code
3. Run full test suite to check for regressions

### Run Full Test Suite:
```bash
mvn test 2>&1
```

### Verify All Tests Pass:
- `BUILD SUCCESS` with no failures

---

## STEP 5: VERIFY AGAINST ORIGINAL USE-CASE

**⚠️ CRITICAL: Test passing ≠ bug is fixed. Don't assume RED test captured the bug correctly.**

After your test passes, you MUST verify the fix works against the **original failing use-case**:

### Verification Steps:
1. **Return to the original scenario** - The exact inputs/conditions that exposed the bug
2. **Run the original use-case** - Not your new test, but the ORIGINAL failing scenario
3. **Confirm it now works** - The specific behavior that was broken is now correct
4. **Check for side effects** - The fix didn't break related functionality

### Why This Matters:
- Your RED test is a *hypothesis* about what the bug is
- The test might pass while the original bug remains (tested wrong thing)
- Simplified test cases may miss edge cases in the real scenario
- Only the original use-case proves the bug is truly fixed

### Examples:
```bash
# If bug was discovered via CLI usage:
java -jar app.jar --the-exact-args-that-failed

# If bug was discovered in a specific test:
mvn test -Dtest=TheOriginalFailingTest

# If bug was user-reported with specific input:
# Reproduce with that EXACT input, not a simplified version
```

### If Original Use-Case Still Fails:

**Your RED test wasn't capturing the ACTUAL bug. Return to RED phase:**

1. **Analyze why the fix didn't work**:
   - The test passed but the bug persists → test was testing the wrong thing
   - The test was an approximation, not a faithful reproduction
   - You fixed a symptom, not the root cause

2. **Reset TDD mode file to RED phase**:
   ```bash
   cat > /tmp/tdd_skill_active_${SESSION_ID} << EOF
   {
     "phase": "RED",
     "session_id": "${SESSION_ID}",
     "target_class": "YOUR_CLASS_NAME",
     "test_class": "YOUR_CLASS_NAMETest",
     "test_failed": false,
     "test_passed": false,
     "started_at": "$(date -Iseconds)"
   }
   EOF
   ```

3. **Write a NEW test** that:
   - Fails for the SAME reason as the original use-case
   - Uses the exact inputs/conditions that exposed the bug
   - Is a faithful reproduction, not an approximation

4. **Repeat the full cycle**: RED → GREEN → REFACTOR → VERIFY

**This loop continues until the ORIGINAL use-case works.**

---

## STEP 6: COMPLETE - Clean Up TDD Mode

### Actions:
```bash
# Remove TDD mode file
rm -f /tmp/tdd_skill_active_${SESSION_ID}
```

Report completion to user.

---

## Quick Reference: Session ID

Get session ID for TDD mode file:
```bash
# Check for injected session ID first
if [[ -n "$CLAUDE_SESSION_ID" ]]; then
    SESSION_ID="$CLAUDE_SESSION_ID"
else
    # Fallback: look for most recent session tracker
    SESSION_ID=$(ls -t /tmp/tdd_test_mods_* 2>/dev/null | head -1 | sed 's/.*_//')
    if [[ -z "$SESSION_ID" ]]; then
        SESSION_ID="manual_$(date +%s)"
    fi
fi
echo "Session ID: $SESSION_ID"
```

---

## Example: Adding Feature

### 1. Initialize TDD Mode
```bash
SESSION_ID="abc123"
cat > /tmp/tdd_skill_active_${SESSION_ID} << EOF
{"phase":"RED","session_id":"${SESSION_ID}","target_class":"MyClass","test_class":"MyClassTest","test_failed":false,"test_passed":false}
EOF
```

### 2. Write Test (RED)
```java
// src/test/java/com/example/MyClassTest.java
@Test
public void testNewFeature() {
    // Arrange
    // Act
    // Assert - this should FAIL because feature doesn't exist
}
```

### 3. Run Test - Verify Failure
```bash
mvn test -Dtest=MyClassTest
# Expected: BUILD FAILURE or test failures
```

### 4. Update Mode File
```bash
cat > /tmp/tdd_skill_active_${SESSION_ID} << EOF
{"phase":"GREEN","session_id":"${SESSION_ID}","target_class":"MyClass","test_class":"MyClassTest","test_failed":true,"test_passed":false}
EOF
```

### 5. Implement Feature (GREEN)
Now edits to `src/main/java/.../MyClass.java` are ALLOWED.

### 6. Run Test - Verify Success
```bash
mvn test -Dtest=MyClassTest
# Expected: BUILD SUCCESS
```

### 7. Clean Up
```bash
rm -f /tmp/tdd_skill_active_${SESSION_ID}
```

---

## Troubleshooting

### "TDD SKILL GATE - BLOCKED" but skill is active
Check the mode file exists and has correct phase:
```bash
cat /tmp/tdd_skill_active_*
```

### "TDD SESSION MISMATCH - BLOCKED"
The TDD mode file exists but has a different session_id. This happens when:
- A previous session's TDD mode file wasn't cleaned up
- You're using a stale mode file

**Fix:**
```bash
# Remove stale files
rm -f /tmp/tdd_skill_active_*
# Reinitialize with current session
```

### Test won't fail (already passes)
- Your test isn't testing the right thing
- The feature might already exist
- Write a MORE SPECIFIC test

### Can't find session ID
Use manual fallback:
```bash
SESSION_ID="manual_$(date +%s)"
```

---

## Exceptions: NONE

There are NO exceptions to this workflow:
- ❌ "Small changes" still need tests
- ❌ "Bug fixes" still need test cases
- ❌ "Urgent" is not an excuse
- ❌ "I'll add tests later" - the hook will BLOCK you

---

## Why This Works

Previous enforcement relied on:
- Passive documentation (ignored)
- Post-hoc detection (too late)
- Warning messages (dismissed)

This skill works because:
- **Gatekeeper hook** physically BLOCKS production edits
- **State machine** requires verified transitions
- **Test execution** proves RED→GREEN cycle
- **No bypass** - the correct path is the only path
