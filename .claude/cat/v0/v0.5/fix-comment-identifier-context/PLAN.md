# Task Plan: fix-comment-identifier-context

## Objective

Fix parser to handle comments appearing where identifiers are expected.

## Problem Analysis

**Error:** `Expected identifier but found LINE_COMMENT`
**Occurrences:** 13 in Spring Framework 6.2.1

The error indicates comments appear in contexts where the parser expects an
identifier token. Unlike member declaration comments, these may be in:
- Type parameter bounds
- Generic type arguments
- Method parameter lists
- Extends/implements clauses

Note: Some failures (like MsgOrBuilder.java) may be protobuf-generated code
with unusual formatting patterns.

## Approach

1. Examine specific failing files to identify pattern
2. Reproduce with minimal test case
3. Add comment skipping in appropriate identifier contexts
4. Verify fix

## Execution Steps

1. Download and analyze BridgeMethodResolverTests.java
2. Identify the specific construct causing the error
3. Create failing test case
4. Implement fix (likely add parseComments() call before identifier expectation)
5. Verify all tests pass

## Success Criteria

- [ ] Error pattern identified with certainty
- [ ] Minimal reproduction test case created
- [ ] Parser fix implemented
- [ ] All existing tests pass
