# Plan: consolidate-contextual-keyword-tests

## Goal

Consolidate 3 contextual keyword test files into a single `ContextualKeywordParserTest`.

## Approach

Use `ContextualKeywordExpressionTest` as the base (largest file), merge in tests from the other two files with section comments, then rename.

## Risk Assessment

- **Risk Level:** LOW
- **Concerns:** Tests cover different contexts (expressions, identifiers, method calls)
- **Mitigation:** Use clear section comments to group related tests

## Execution Steps

### Step 1: Review source files
**Files:** ContextualKeywordExpressionTest.java, ContextualKeywordIdentifierTest.java, ContextualKeywordMethodCallTest.java
**Action:** Identify all test methods and their imports from each file
**Verify:** List all test method names
**Done:** All test methods identified

### Step 2: Merge into base file
**Files:** `parser/src/test/java/.../ContextualKeywordExpressionTest.java`
**Action:** Add tests from IdentifierTest and MethodCallTest with section comments:
- "// Expression context tests" (existing)
- "// Identifier context tests"
- "// Method call context tests"
**Verify:** File compiles, all tests present
**Done:** All tests merged

### Step 3: Rename to consolidated name
**Files:** ContextualKeywordExpressionTest.java → ContextualKeywordParserTest.java
**Action:** Rename file and update class name
**Verify:** File compiles with new name
**Done:** File renamed

### Step 4: Delete source files
**Files:** ContextualKeywordIdentifierTest.java, ContextualKeywordMethodCallTest.java
**Action:** Delete the now-redundant files
**Verify:** Files no longer exist
**Done:** Source files removed

### Step 5: Verify tests pass
**Files:** None
**Action:** Run `./mvnw -pl parser test -Dtest=ContextualKeywordParserTest`
**Verify:** All tests pass
**Done:** 0 test failures

## Acceptance Criteria

- [ ] All test methods from source files exist in ContextualKeywordParserTest
- [ ] 2 source files deleted, 1 renamed
- [ ] All tests pass
- [ ] No test coverage lost
