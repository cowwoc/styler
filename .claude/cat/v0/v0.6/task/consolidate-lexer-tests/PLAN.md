# Plan: consolidate-lexer-tests

## Goal

Consolidate 8 specialized lexer test files into the main `LexerTest` to reduce file proliferation.

## Approach

Merge tests from all 8 specialized files into `LexerTest.java` with section comments for each category, then delete source files.

## Risk Assessment

- **Risk Level:** MEDIUM
- **Concerns:** Large merge (8 files, ~1846 lines added to existing 720)
- **Mitigation:** Merge one category at a time, run tests after each merge

## Execution Steps

### Step 1: Review source files
**Files:** All 8 Lexer*Test.java files (excluding LexerTest.java itself)
**Action:** Identify all test methods and categorize by type
**Verify:** List all test method names by category
**Done:** All test methods identified

### Step 2: Merge binary literal tests
**Files:** LexerTest.java ← LexerBinaryLiteralTest.java
**Action:** Add tests with section comment "// Binary literal tests"
**Verify:** `./mvnw -pl parser test -Dtest=LexerTest` passes
**Done:** Binary tests merged

### Step 3: Merge hex literal tests
**Files:** LexerTest.java ← LexerHexLiteralTest.java, LexerHexFloatLiteralTest.java
**Action:** Add tests with section comments "// Hex literal tests", "// Hex float literal tests"
**Verify:** Tests pass
**Done:** Hex tests merged

### Step 4: Merge octal literal tests
**Files:** LexerTest.java ← LexerOctalLiteralTest.java
**Action:** Add tests with section comment "// Octal literal tests"
**Verify:** Tests pass
**Done:** Octal tests merged

### Step 5: Merge floating point tests
**Files:** LexerTest.java ← LexerFloatingPointWithoutLeadingZeroTest.java
**Action:** Add tests with section comment "// Floating point without leading zero tests"
**Verify:** Tests pass
**Done:** Floating point tests merged

### Step 6: Merge unicode tests
**Files:** LexerTest.java ← LexerUnicodeEscapeTest.java, LexerUnicodeEscapeOutsideLiteralsTest.java, LexerUnicodePreprocessingTest.java
**Action:** Add tests with section comments for each unicode category
**Verify:** Tests pass
**Done:** Unicode tests merged

### Step 7: Delete source files
**Files:** All 8 source files
**Action:** Delete the now-redundant files
**Verify:** Files no longer exist
**Done:** Source files removed

### Step 8: Final verification
**Files:** None
**Action:** Run full test suite `./mvnw -pl parser test`
**Verify:** All tests pass
**Done:** 0 test failures

## Acceptance Criteria

- [ ] All test methods from 8 source files exist in LexerTest
- [ ] All 8 source files deleted
- [ ] All tests pass
- [ ] No test coverage lost
- [ ] Section comments organize tests by category
