# Task Plan: fix-misc-expression-edge-cases

## Objective

Fix parser to handle remaining expression edge cases discovered in Spring Framework validation.

## Problem Analysis

Two files fail with unique edge cases:

### SpringJUnit4ConcurrencyTests.java
- Error: "Unexpected token in expression: RIGHT_BRACE"
- Need to investigate the specific pattern causing this

### SpelCompilationCoverageTests.java
- Error: "Expected IDENTIFIER but found DOT"
- Likely a complex generic or method reference pattern

## Error Messages

- "Unexpected token in expression: RIGHT_BRACE"
- "Expected IDENTIFIER but found DOT"

## Approach

1. Examine each failing file to identify the specific problematic patterns
2. Create minimal reproducer tests
3. Fix each edge case individually

## Execution Steps

1. Clone Spring Framework and examine the two failing files
2. Identify exact lines/patterns causing parse failures
3. Write failing test cases for each pattern
4. Fix parser to handle each pattern
5. Verify fixes against the affected files

## Success Criteria

- [ ] SpringJUnit4ConcurrencyTests.java parses successfully
- [ ] SpelCompilationCoverageTests.java parses successfully
- [ ] Existing expression tests still pass
