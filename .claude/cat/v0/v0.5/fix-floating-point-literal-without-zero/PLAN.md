# Task Plan: fix-floating-point-literal-without-zero

## Objective

Fix parsing of floating-point literals without leading zero (e.g., `.0025`, `.5`).

## Problem Analysis

The lexer doesn't recognize floating-point literals that start with a decimal point.
Per JLS §3.10.2, these are valid:
- `.5` → 0.5
- `.0025` → 0.0025
- `.99e10` → 0.99e10

## Affected Files

- `spring-test/...XmlContentAssertionTests.java` - `.0025`
- `spring-test/...XpathAssertionTests.java` - `.0025`
- Plus 4 more files with same pattern

## Approach

Modify the lexer to recognize DOT followed by digits as the start of a floating-point literal.

## Execution Steps

1. Add test case for floating-point literal without leading zero
2. Locate floating-point tokenization in Lexer.java
3. Add handling for DOT at expression start when followed by digits
4. Verify all 6 affected files now parse correctly

## Success Criteria

- [ ] Parser accepts `.0025` as valid floating-point literal
- [ ] Parser accepts `.5`, `.99`, `.1e10` variants
- [ ] All 6 Spring Framework files with this pattern parse successfully
