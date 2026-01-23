# Plan: add-type-parameter-bound-annotations

## Goal
Add parser support for type-use annotations on type parameter declarations and their bounds, enabling
parsing of patterns like `<@Nullable T extends @Nullable Object>`.

## Satisfies
None - parser edge case fix discovered during Spring Boot validation

## Problem
The parser fails when encountering type-use annotations on type parameters or their bounds:

```java
// Currently fails with: "Expected IDENTIFIER but found AT_SIGN"
interface Accessor<S, @Nullable P extends @Nullable Object> { ... }
class Box<@NonNull T> { ... }
```

## Root Cause
When parsing generic type parameter declarations (`<T extends Foo>`), the parser doesn't expect
annotations before the type parameter name or before the bound type. JSR 308 allows annotations
in both positions.

## Risk Assessment
- **Risk Level:** MEDIUM
- **Regression Risk:** Type parameter parsing, generic declarations
- **Mitigation:** Comprehensive tests for various type parameter configurations

## Files to Modify
- `parser/src/main/java/io/github/cowwoc/styler/parser/internal/TypeParser.java` - Handle annotations
  in type parameter declarations
- `parser/src/test/java/.../TypeParameterAnnotationParserTest.java` - New test file

## Acceptance Criteria
- [ ] Parser handles `<@Annotation T>`
- [ ] Parser handles `<T extends @Annotation Bound>`
- [ ] Parser handles `<@Ann1 T extends @Ann2 Bound>`
- [ ] Parser handles multiple type parameters with annotations
- [ ] All existing tests pass
- [ ] Spring Boot validation shows reduced failures

## Execution Steps
1. **Locate type parameter parsing logic**
   - Files: TypeParser.java, look for LESS_THAN handling for generic declarations
   - Verify: Understand current flow for `<T extends Foo>`

2. **Add annotation handling in type parameter declaration**
   - Before parsing type parameter name, check for annotations
   - Before parsing bound type, check for annotations
   - Verify: Unit tests pass

3. **Add test cases**
   - Create TypeParameterAnnotationParserTest.java
   - Cover: annotated parameter, annotated bound, multiple parameters
   - Verify: All tests green
