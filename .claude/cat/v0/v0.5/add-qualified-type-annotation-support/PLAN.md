# Plan: add-qualified-type-annotation-support

## Goal
Add parser support for type-use annotations after qualified type names, enabling parsing of patterns
like `java.security.@Nullable Principal` and `LoaderZipEntries.@Nullable WrittenEntries`.

## Satisfies
None - parser edge case fix discovered during Spring Boot validation

## Problem
The parser fails when encountering type-use annotations between a package/outer-type qualifier and
the simple type name:

```java
// Currently fails with various errors
Supplier<java.security.@Nullable Principal> principalSupplier;
private LoaderZipEntries.@Nullable WrittenEntries writtenLoaderEntries;
```

## Root Cause
After parsing a qualified name like `java.security`, the parser doesn't expect an annotation before
the final type name. The JSR 308 spec allows annotations at any type-use position, including after
the dot in qualified types.

## Risk Assessment
- **Risk Level:** MEDIUM
- **Regression Risk:** Qualified type name parsing, annotation handling
- **Mitigation:** Comprehensive tests for qualified types with and without annotations

## Files to Modify
- `parser/src/main/java/io/github/cowwoc/styler/parser/internal/TypeParser.java` - Handle annotation
  after dot in qualified types
- `parser/src/test/java/.../QualifiedTypeAnnotationParserTest.java` - New test file

## Acceptance Criteria
- [ ] Parser handles `pkg.@Annotation Type`
- [ ] Parser handles `Outer.@Annotation Inner`
- [ ] Parser handles nested qualifiers `a.b.@Annotation c.Type`
- [ ] All existing tests pass
- [ ] Spring Boot validation shows reduced failures

## Execution Steps
1. **Locate qualified type parsing logic**
   - Files: TypeParser.java, look for DOT handling in type references
   - Verify: Understand current flow for `a.b.Type`

2. **Add annotation handling after dot**
   - After consuming DOT, check for AT_SIGN
   - Parse annotations, then continue with identifier
   - Verify: Unit tests pass

3. **Add test cases**
   - Create QualifiedTypeAnnotationParserTest.java
   - Cover: package-qualified, nested type, multiple annotations
   - Verify: All tests green
