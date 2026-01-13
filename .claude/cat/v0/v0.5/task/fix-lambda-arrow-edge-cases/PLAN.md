# Task Plan: fix-lambda-arrow-edge-cases

## Objective

Fix parser edge cases where lambda arrow (`->`) causes "Expected SEMICOLON but
found ARROW" errors.

## Problem Analysis

**Error:** `Expected SEMICOLON but found ARROW`
**Occurrences:** 16 in Spring Framework 6.2.1

The error suggests the parser expects a statement terminator but finds an arrow,
indicating lambda detection is failing in certain contexts.

Possible patterns:
- Field initializers with lambdas
- Static initializers with lambdas
- Method chains returning lambdas

## Approach

1. Examine specific failing files to identify pattern
2. Reproduce with minimal test case
3. Fix parser logic
4. Verify fix

## Execution Steps

1. Download and analyze MockServerWebExchange.java
2. Identify the specific construct causing the error
3. Create failing test case
4. Implement fix
5. Verify all tests pass

## Success Criteria

- [ ] Error pattern identified with certainty
- [ ] Minimal reproduction test case created
- [ ] Parser fix implemented
- [ ] All existing tests pass
