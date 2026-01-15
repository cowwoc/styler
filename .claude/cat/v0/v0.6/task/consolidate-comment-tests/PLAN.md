# Plan: consolidate-comment-tests

## Goal

Consolidate 10 comment-related test files into a single `CommentParserTest` to improve test organization.

## Approach

Use `EnumCommentParserTest` as the base (largest file at 492 lines), merge all other comment tests with section comments, then rename.

## Risk Assessment

- **Risk Level:** MEDIUM
- **Concerns:** Large merge (10 files, ~1858 lines total)
- **Mitigation:** Merge one category at a time, run tests after each merge

## Execution Steps

### Step 1: Review source files
**Files:** All 10 *Comment*.java test files
**Action:** Identify all test methods and categorize
**Verify:** List all test method names by category
**Done:** All test methods identified

### Step 2: Merge block comment tests
**Files:** EnumCommentParserTest.java ← BlockCommentParserTest.java
**Action:** Add tests with section comment "// Block comment tests"
**Verify:** Tests pass
**Done:** Block comment tests merged

### Step 3: Merge expression comment tests
**Files:** EnumCommentParserTest.java ← CommentInExpressionTest.java, CommentInTypeContextTest.java
**Action:** Add tests with section comments for each category
**Verify:** Tests pass
**Done:** Expression comment tests merged

### Step 4: Merge control flow comment tests
**Files:** EnumCommentParserTest.java ← ControlFlowCommentParserTest.java, ElseIfCommentParserTest.java
**Action:** Add tests with section comments
**Verify:** Tests pass
**Done:** Control flow comment tests merged

### Step 5: Merge remaining comment tests
**Files:** EnumCommentParserTest.java ← ArrayInitializerCommentParserTest.java, ExtendsImplementsCommentParserTest.java, LambdaCommentParserTest.java, MemberCommentParserTest.java
**Action:** Add tests with section comments for each category
**Verify:** Tests pass
**Done:** Remaining tests merged

### Step 6: Rename to consolidated name
**Files:** EnumCommentParserTest.java → CommentParserTest.java
**Action:** Rename file and update class name
**Verify:** File compiles with new name
**Done:** File renamed

### Step 7: Delete source files
**Files:** All 9 other source files
**Action:** Delete the now-redundant files
**Verify:** Files no longer exist
**Done:** Source files removed

### Step 8: Final verification
**Files:** None
**Action:** Run `./mvnw -pl parser test -Dtest=CommentParserTest`
**Verify:** All tests pass
**Done:** 0 test failures

## Acceptance Criteria

- [ ] All test methods from 10 source files exist in CommentParserTest
- [ ] 9 source files deleted, 1 renamed
- [ ] All tests pass
- [ ] No test coverage lost
- [ ] Section comments organize tests by category
