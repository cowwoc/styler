# Plan: add-pattern-matching-instanceof-support

## Goal
Add parser support for pattern matching in instanceof expressions (Java 16+), enabling parsing of
patterns like `obj instanceof String s` and `input instanceof Module module`.

## Satisfies
None - parser edge case fix discovered during Spring Boot validation

## Problem
The parser fails when encountering a binding variable after the type in an instanceof expression:

```java
// Currently fails with: "Expected RIGHT_PARENTHESIS but found MODULE/IDENTIFIER"
(input instanceof Module module) ? module : new Module((String) input)
if (obj instanceof String s) { ... }
```

## Root Cause
The parser treats `instanceof` as expecting only a type, not a type pattern with optional binding
variable. Java 16 introduced pattern matching for instanceof (JEP 394) which adds an optional
variable declaration after the type.

## Risk Assessment
- **Risk Level:** LOW
- **Regression Risk:** Existing instanceof expressions without pattern
- **Mitigation:** Preserve backward compatibility for simple instanceof

## Files to Modify
- `parser/src/main/java/io/github/cowwoc/styler/parser/internal/ExpressionParser.java` - Handle
  optional binding variable after instanceof type
- `parser/src/test/java/.../InstanceofPatternParserTest.java` - New test file

## Acceptance Criteria
- [ ] Parser handles `obj instanceof Type var`
- [ ] Parser handles `obj instanceof Type` (backward compatible)
- [ ] Parser handles contextual keywords as binding var (`module`, `record`, etc.)
- [ ] All existing tests pass
- [ ] Spring Boot validation shows reduced failures

## Execution Steps
1. **Locate instanceof parsing logic**
   - Files: ExpressionParser.java, look for INSTANCEOF token handling
   - Verify: Understand current flow

2. **Add optional binding variable**
   - After parsing the type, check for identifier
   - If identifier follows (not operator), parse as binding variable
   - Verify: Unit tests pass

3. **Add test cases**
   - Create InstanceofPatternParserTest.java
   - Cover: simple pattern, contextual keywords, no pattern (backward compat)
   - Verify: All tests green
