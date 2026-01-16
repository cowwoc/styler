# State

- **Status:** completed
- **Progress:** 100%
- **Resolution:** implemented
- **Dependencies:** integrate-expression-parser
- **Estimated Tokens:** 25000
- **Created:** 2026-01-16
- **Completed:** 2026-01-16

## Description

Fix parser errors where `IDENTIFIER` or `RECORD` is found when `SEMICOLON` was expected. These occur
in variable declarations or statements involving contextual keywords.

**Error patterns (6 files):**
- Expected SEMICOLON but found IDENTIFIER (4 files) - e.g., RuntimeHintsAgentTests.java
- Expected SEMICOLON but found RECORD (2 files) - e.g., RuntimeHintsRecorder.java

## Analysis

The parser expects a semicolon to end a statement but encounters an identifier or the `record`
keyword. This likely occurs when:
1. Contextual keyword `record` is used as a type name (pre-Java 16 code)
2. Multi-declaration statements like `Type a, b;` are not fully handled
3. Pattern matching with identifiers in unexpected positions

The existing `fix-contextual-keywords-as-identifiers` task resolved most cases, but these files
contain edge cases not covered.

## Acceptance Criteria

- [x] All 6 affected Spring Framework files parse successfully
- [x] No regression in other Spring Framework files
- [x] Tests added for the specific patterns
