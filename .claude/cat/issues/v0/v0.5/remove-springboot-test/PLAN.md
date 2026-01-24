# Plan: remove-springboot-test

## Current State
SpringBootValidationTest has external dependency on Spring Boot source code and Checkstyle violations.

## Target State
Test removed. Parser validation should use internal test infrastructure only.

## Satisfies
- None (cleanup task)

## Risk Assessment
- **Risk Level:** LOW
- **Breaking Changes:** None - test is optional and skips when Spring Boot not present
- **Mitigation:** Build passes after removal

## Files to Modify
- parser/src/test/java/io/github/cowwoc/styler/parser/test/SpringBootValidationTest.java - Delete

## Execution Steps
1. **Step 1:** Delete SpringBootValidationTest.java
   - Verify: ./mvnw test-compile -pl parser
