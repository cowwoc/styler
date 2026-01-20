# Task Plan: fix-else-if-chain-parsing

## Objective

Fix parser to handle else-if chains that currently fail with "Unexpected token
in expression: ELSE".

## Problem Analysis

**Error:** `Unexpected token in expression: ELSE`
**Occurrences:** 18 in Spring Framework 6.2.1

The error suggests `else` is being parsed in an expression context rather than
statement context. This may occur with:
- Braceless if-else statements
- Nested conditionals in certain contexts
- Lambda body parsing where else is misinterpreted

## Approach

1. Examine specific failing files to identify pattern
2. Reproduce with minimal test case
3. Fix parser logic
4. Verify fix

## Execution Steps

1. Download and analyze TestContextResourceUtils.java
2. Identify the specific construct causing the error
3. Create failing test case
4. Implement fix
5. Verify all tests pass

## Success Criteria

- [ ] Error pattern identified with certainty
- [ ] Minimal reproduction test case created
- [ ] Parser fix implemented
- [ ] All existing tests pass
