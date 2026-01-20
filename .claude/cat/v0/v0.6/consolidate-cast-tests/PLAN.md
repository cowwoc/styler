# Plan: consolidate-cast-tests

## Goal

Merge `CastLambdaExpressionParserTest` into `CastExpressionParserTest` to consolidate cast-related tests.

## Approach

Copy test methods from `CastLambdaExpressionParserTest` into `CastExpressionParserTest`, then delete the source file.

## Risk Assessment

- **Risk Level:** LOW
- **Concerns:** Cast lambda tests are conceptually related to both cast and lambda parsing
- **Mitigation:** Add section comment in target file to group lambda-related cast tests

## Execution Steps

### Step 1: Review source file
**Files:** `parser/src/test/java/.../CastLambdaExpressionParserTest.java`
**Action:** Identify all test methods and their imports
**Verify:** List test method names
**Done:** All test methods identified

### Step 2: Merge tests into target
**Files:** `parser/src/test/java/.../CastExpressionParserTest.java`
**Action:** Copy test methods with section comment "// Cast lambda expression tests", add missing imports
**Verify:** File compiles, no duplicate methods
**Done:** All tests present in target file

### Step 3: Delete source file
**Files:** `parser/src/test/java/.../CastLambdaExpressionParserTest.java`
**Action:** Delete the now-redundant file
**Verify:** File no longer exists
**Done:** Source file removed

### Step 4: Verify tests pass
**Files:** None
**Action:** Run `./mvnw -pl parser test -Dtest=CastExpressionParserTest`
**Verify:** All tests pass including merged ones
**Done:** 0 test failures

## Acceptance Criteria

- [ ] All test methods from CastLambdaExpressionParserTest exist in CastExpressionParserTest
- [ ] CastLambdaExpressionParserTest.java deleted
- [ ] All tests pass
- [ ] No test coverage lost
