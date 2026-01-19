# Plan: validate-spring-boot-parsing

## Problem

The parser has not been validated against Spring Boot codebase. Spring Boot uses different
patterns than Spring Framework and may expose additional parser edge cases.

## Satisfies

None - validation/quality gate task

## Reproduction Code

```bash
# Run parser validation against Spring Boot
./mvnw exec:java -pl parser -Dexec.mainClass=com.stazsoftware.styler.parser.ParserCli \
  -Dexec.args="check ~/spring-boot"
```

## Expected vs Actual

- **Expected:** All files parse successfully (0 failures)
- **Actual:** Unknown - needs initial validation run

## Root Cause

Parser may not handle patterns specific to Spring Boot that differ from Spring Framework.

## Fix Approach Outlines

### Conservative

Run validation, document failures, create separate bugfix tasks for each error pattern.
- **Risk:** LOW
- **Tradeoff:** Multiple follow-up tasks may be needed

### Balanced

Run validation, fix common error patterns inline, create tasks only for complex issues.
- **Risk:** MEDIUM
- **Tradeoff:** Some fixes may be rushed

### Aggressive

Run validation, fix all issues in a single pass.
- **Risk:** HIGH
- **Tradeoff:** Large scope, potential for regressions

## Selected Approach

Conservative

## Detailed Fix

### Risk Assessment

- **Risk Level:** LOW
- **Regression Risk:** None - validation only, no code changes
- **Mitigation:** Each bugfix will have its own targeted tests

### Execution Steps

1. **Download Spring Boot source:**
   - Clone or download Spring Boot repository
   - Verify: `ls ~/spring-boot/spring-boot-project`

2. **Run initial validation:**
   - Execute parser check on Spring Boot
   - Verify: Command completes and reports file counts

3. **Analyze failures:**
   - Document error patterns and counts
   - Group similar errors by root cause
   - Verify: All failure types categorized

4. **Create bugfix tasks:**
   - Create targeted tasks for each error pattern
   - Add dependencies to this validation task
   - Verify: Tasks exist in .claude/cat/v0/v0.5/task/

5. **Re-validate after fixes:**
   - Run parser check again after bugfix tasks complete
   - Verify: Failed count = 0
