# Plan: add-wildcard-type-annotation-support

## Goal
Add parser support for type-use annotations on wildcard type arguments, enabling parsing of patterns
like `Consumer<@Nullable ? super T>` and `Function<@NonNull ?, R>`.

## Satisfies
None - parser edge case fix discovered during Spring Boot validation

## Problem
The parser fails when encountering type-use annotations before wildcard `?` in generic type arguments:

```java
// Currently fails with: "Expected identifier but found QUESTION_MARK"
public void to(Consumer<@Nullable ? super T> consumer) { ... }
Supplier<@Nullable ?> supplier;
```

## Root Cause
When parsing generic type arguments, the parser doesn't expect an annotation before the `?` wildcard
token. The annotation is consumed but then `?` is unexpected in that position.

## Risk Assessment
- **Risk Level:** LOW
- **Regression Risk:** Other generic type argument parsing
- **Mitigation:** Test with existing generic type tests, add specific wildcard annotation tests

## Files to Modify
- `parser/src/main/java/io/github/cowwoc/styler/parser/internal/TypeParser.java` - Handle annotation
  before wildcard in type arguments
- `parser/src/test/java/.../WildcardTypeAnnotationParserTest.java` - New test file

## Acceptance Criteria
- [ ] Parser handles `<@Nullable ?>` wildcard
- [ ] Parser handles `<@Nullable ? extends T>` upper-bounded wildcard
- [ ] Parser handles `<@Nullable ? super T>` lower-bounded wildcard
- [ ] All existing tests pass
- [ ] Spring Boot validation shows reduced failures

## Execution Steps
1. **Locate wildcard parsing logic**
   - Files: TypeParser.java, look for QUESTION_MARK handling in generic type arguments
   - Verify: Understand current flow

2. **Add annotation handling before wildcard**
   - Parse annotations when encountered before `?`
   - Continue with normal wildcard parsing
   - Verify: Unit tests pass

3. **Add test cases**
   - Create WildcardTypeAnnotationParserTest.java
   - Cover: simple wildcard, upper bound, lower bound
   - Verify: All tests green
