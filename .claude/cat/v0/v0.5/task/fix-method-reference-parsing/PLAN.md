# Task Plan: fix-method-reference-parsing

## Objective

Fix parser to handle method references and complex type argument contexts that
currently fail with "Expected SEMICOLON but found IDENTIFIER".

## Problem Analysis

**Error:** `Expected SEMICOLON but found IDENTIFIER`
**Occurrences:** 54 in Spring Framework 6.2.1

Affected files include:
- RuntimeHintsAgentTests.java
- ReflectionInvocationsTests.java
- MockHttpServletRequestBuilderTests.java

## Approach

1. Examine specific failing files to identify exact construct
2. Reproduce the error with minimal test case
3. Fix parser logic
4. Verify fix

## Execution Steps

1. Download and analyze one of the failing files
2. Identify the specific Java construct causing the error
3. Create failing test case
4. Implement fix
5. Verify all tests pass

## Success Criteria

- [ ] Error pattern identified with certainty
- [ ] Minimal reproduction test case created
- [ ] Parser fix implemented
- [ ] All existing tests pass
