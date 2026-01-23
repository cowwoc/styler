# Plan: add-type-use-annotation-support

## Goal

Add support for type-use annotations (JSR 308, Java 8+) which allow annotations on any type use,
not just declarations. This enables parsing of modern Java codebases like Spring Boot that use
`@Nullable`, `@NonNull`, and similar annotations in type positions.

## Satisfies

None - infrastructure task to support real-world Java codebases.

## Problem Analysis

The parser currently only handles annotations at declaration sites. Type-use annotations (JSR 308)
allow annotations in additional positions that the parser doesn't recognize:

**Patterns that fail (94 files in Spring Boot):**

```java
// Field type annotation (71 occurrences)
private final @Nullable Integer dirMode;

// Return type annotation
public @Nullable String getAuthHeader() { ... }

// Generic type parameter annotation
Function<String, @Nullable CredentialHelper> factory;

// Method return type with generic
private <T> @Nullable T read(JsonParser parser, Class<T> type) { ... }

// Array component annotation
String @NonNull [] names;
```

**Error messages seen:**
- `Unexpected token in member declaration: AT_SIGN` (71 occurrences)
- `Expected SEMICOLON but found IDENTIFIER` (9 occurrences)
- `Expected RIGHT_PARENTHESIS but found IDENTIFIER` (6 occurrences)

## Risk Assessment

- **Risk Level:** MEDIUM
- **Concerns:** Type parsing is complex with generics, arrays, and wildcards
- **Mitigation:** Add comprehensive tests for each annotation position; run Spring Boot validation

## Files to Modify

- `parser/src/main/java/.../TypeParser.java` - Handle annotations before type names
- `parser/src/main/java/.../MemberParser.java` - Handle annotations in field/method type positions
- `parser/src/test/java/.../TypeUseAnnotationTest.java` - New test class for type-use annotations

## Acceptance Criteria

- [ ] Field type annotations parse: `private @Nullable String name;`
- [ ] Return type annotations parse: `public @Nullable T get()`
- [ ] Generic parameter annotations parse: `List<@NonNull String>`
- [ ] Array component annotations parse: `String @NonNull []`
- [ ] Spring Boot validation passes (0 failures from type-use annotations)
- [ ] All existing parser tests pass (no regressions)

## Execution Steps

1. **Add type annotation parsing to TypeParser**
   - After parsing modifiers, check for annotations before the type name
   - Store type-use annotations in the AST node
   - Verify: Unit tests for basic type-use annotation patterns

2. **Handle type-use annotations in generic type parameters**
   - In parseTypeArgument(), allow annotations before type names
   - Support `List<@NonNull String>` and `Map<String, @Nullable Value>`
   - Verify: Unit tests for annotated generic parameters

3. **Handle type-use annotations in method return types**
   - Parse annotations after modifiers but before return type
   - Support `public @Nullable T method()` pattern
   - Verify: Unit tests for annotated return types

4. **Handle type-use annotations in array components**
   - Support `String @NonNull []` (annotation on array, not element)
   - Support `@NonNull String[]` (annotation on element type)
   - Verify: Unit tests for both array annotation positions

5. **Run Spring Boot validation**
   - Execute validation against ~/spring-boot
   - Target: 0 failures from type-use annotation patterns
   - Verify: `validate-spring-boot-parsing` task can complete

## Test Cases

```java
// Basic field type annotation
private @Nullable String name;

// Final field with type annotation
private final @NonNull List<String> items;

// Generic with annotated parameter
Map<String, @Nullable Object> cache;

// Nested generic with annotations
List<Map<@NonNull String, @Nullable Integer>> nested;

// Method return type annotation
public @Nullable String getValue() { return null; }

// Generic method with annotated return
public <T> @Nullable T find(Class<T> type) { return null; }

// Array with type-use annotation
String @NonNull [] names;
@Nullable String[] values;
```
