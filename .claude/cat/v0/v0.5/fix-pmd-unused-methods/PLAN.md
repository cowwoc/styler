# Plan: fix-pmd-unused-methods

## Problem

Main branch fails `./mvnw verify` with 52 PMD violations:
- 51 UnusedPrivateMethod violations in Parser.java
- 1 UnusedLocalVariable in Lexer.java

These are method stubs left behind when parsing logic was refactored to helper classes.

## Root Cause

During the split-parser-into-multiple-classes refactoring, delegate methods were created in Parser.java
that forward to helper classes. The original private methods were not removed, leaving dead code.

## Fix Approach

Delete all unused private methods identified by PMD. These methods are dead code - their functionality
has been moved to StatementParser, ExpressionParser, TypeParser, or ModuleParser.

## Risk Assessment

- **Risk Level:** LOW
- **Regression Risk:** None - methods are unused (PMD verified)
- **Mitigation:** Run full test suite after removal

## Execution Steps

### Step 1: Delete unused methods in Parser.java
**Files:** `parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java`
**Action:** Remove all 51 unused private methods identified by PMD
**Verify:** `./mvnw pmd:check -pl parser` passes
**Done:** No UnusedPrivateMethod violations

### Step 2: Delete unused variable in Lexer.java
**Files:** `parser/src/main/java/io/github/cowwoc/styler/parser/Lexer.java`
**Action:** Remove unused `secondDigit` variable at line 781
**Verify:** `./mvnw pmd:check -pl parser` passes
**Done:** No UnusedLocalVariable violations

### Step 3: Verify full build
**Files:** (all)
**Action:** Run complete build with all checks
**Verify:** `./mvnw verify` passes
**Done:** Main branch green

## Verification

```bash
# Check PMD passes
./mvnw pmd:check -pl parser

# Full verification
./mvnw verify
```

## Commit Message

```
refactor: remove unused private methods from Parser.java

Delete 51 unused private method stubs that were left behind during the
refactoring that split Parser into helper classes (StatementParser,
ExpressionParser, TypeParser, ModuleParser). Also remove 1 unused local
variable in Lexer.java.

These methods are dead code - their functionality now lives in the
helper classes and is accessed through ParserAccess delegation.

Fixes 52 PMD violations blocking main branch build.
```
