# State

- **Status:** completed
- **Progress:** 100%
- **Resolution:** implemented
- **Completed:** 2026-01-16
- **Dependencies:** integrate-expression-parser
- **Estimated Tokens:** 25000
- **Created:** 2026-01-16

## Description

Fix remaining miscellaneous parser errors found in Spring Framework validation that don't fit other
categories.

**Error patterns (4 files):**
- Unexpected RIGHT_BRACE in expression (1 file) - SpringJUnit4ConcurrencyTests.java
- Expected SEMICOLON but found BLOCK_COMMENT (1 file) - BridgeMethodResolver.java
- Expected SEMICOLON but found ARROW (1 file) - RouterFunctionsTests.java
- Expected SEMICOLON but found LESS_THAN (1 file) - SpelCompilationCoverageTests.java

## Analysis

These are unique edge cases:
1. **RIGHT_BRACE in expression:** Likely a lambda or switch expression parsing issue where `}` appears
   unexpectedly
2. **BLOCK_COMMENT after expression:** Comment placement between statement parts confusing the parser
3. **ARROW where SEMICOLON expected:** Lambda in statement context not recognized
4. **LESS_THAN where SEMICOLON expected:** Generic type in unexpected position

Each requires individual investigation.

## Acceptance Criteria

- [ ] All 4 affected Spring Framework files parse successfully
- [ ] No regression in other Spring Framework files
- [ ] Tests added for each specific pattern
