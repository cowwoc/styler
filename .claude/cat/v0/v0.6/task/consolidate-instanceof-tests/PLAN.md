# Plan: consolidate-instanceof-tests

## Goal

Merge `FinalPatternMatchingInstanceofTest` into `PatternMatchingInstanceofTest` to reduce test file proliferation.

## Approach

Copy test methods from `FinalPatternMatchingInstanceofTest` into `PatternMatchingInstanceofTest`, then delete the source file.

## Risk Assessment

- **Risk Level:** LOW
- **Concerns:** Test methods might have naming conflicts
- **Mitigation:** Verify unique method names, run tests after merge

## Execution Steps

### Step 1: Review source file
**Files:** `parser/src/test/java/.../FinalPatternMatchingInstanceofTest.java`
**Action:** Identify all test methods and their imports
**Verify:** List test method names
**Done:** All test methods identified

### Step 2: Merge tests into target
**Files:** `parser/src/test/java/.../PatternMatchingInstanceofTest.java`
**Action:** Copy test methods from FinalPatternMatchingInstanceofTest, add any missing imports
**Verify:** File compiles, no duplicate methods
**Done:** All tests present in target file

### Step 3: Delete source file
**Files:** `parser/src/test/java/.../FinalPatternMatchingInstanceofTest.java`
**Action:** Delete the now-redundant file
**Verify:** File no longer exists
**Done:** Source file removed

### Step 4: Verify tests pass
**Files:** None
**Action:** Run `./mvnw -pl parser test -Dtest=PatternMatchingInstanceofTest`
**Verify:** All tests pass including merged ones
**Done:** 0 test failures

## Acceptance Criteria

- [ ] All test methods from FinalPatternMatchingInstanceofTest exist in PatternMatchingInstanceofTest
- [ ] FinalPatternMatchingInstanceofTest.java deleted
- [ ] All tests pass
- [ ] No test coverage lost
