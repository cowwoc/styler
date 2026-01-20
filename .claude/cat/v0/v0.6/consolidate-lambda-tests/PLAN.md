# Plan: consolidate-lambda-tests

## Goal

Consolidate 3 lambda-related test files into a single `LambdaParserTest` to improve test organization.

## Approach

Create new `LambdaParserTest.java` by combining tests from all three source files with section comments, then delete the source files.

## Risk Assessment

- **Risk Level:** LOW
- **Concerns:** Tests cover different lambda aspects (arrow edge cases, comments, multi-param)
- **Mitigation:** Use clear section comments to group related tests

## Execution Steps

### Step 1: Review source files
**Files:** LambdaArrowEdgeCaseParserTest.java, LambdaCommentParserTest.java, MultiParamLambdaParserTest.java
**Action:** Identify all test methods and their imports from each file
**Verify:** List all test method names
**Done:** All test methods identified

### Step 2: Create consolidated file
**Files:** `parser/src/test/java/.../LambdaParserTest.java`
**Action:** Create new file combining all tests with section comments:
- "// Arrow edge case tests"
- "// Comment handling tests"
- "// Multi-parameter lambda tests"
**Verify:** File compiles, all tests present
**Done:** Consolidated file created

### Step 3: Delete source files
**Files:** LambdaArrowEdgeCaseParserTest.java, LambdaCommentParserTest.java, MultiParamLambdaParserTest.java
**Action:** Delete the now-redundant files
**Verify:** Files no longer exist
**Done:** Source files removed

### Step 4: Verify tests pass
**Files:** None
**Action:** Run `./mvnw -pl parser test -Dtest=LambdaParserTest`
**Verify:** All tests pass
**Done:** 0 test failures

## Acceptance Criteria

- [ ] All test methods from source files exist in LambdaParserTest
- [ ] All 3 source files deleted
- [ ] All tests pass
- [ ] No test coverage lost
