# Task Plan: add-nested-annotation-type-support

## Objective

Support `@interface` annotation type declarations nested inside classes.

## Problem Analysis

**Error**: "Expected IDENTIFIER but found INTERFACE" (109 occurrences in Spring Framework)

Parser doesn't recognize `@interface` as a valid member declaration inside class bodies.
Nested annotation types are valid Java and commonly used.

## Example Failing Code

```java
public class OuterClass {
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RequestScoped {
    }
}
```

## Tasks

1. [ ] Add `AT_SIGN` + `INTERFACE` handling in `parseClassBodyDeclaration()`
2. [ ] Implement `parseAnnotationTypeDeclaration()` for annotation type bodies
3. [ ] Handle annotation type elements (methods with default values)
4. [ ] Support annotation type constants
5. [ ] Add tests for nested annotation types

## Technical Approach

In `parseClassBodyDeclaration()`:
1. When seeing `AT_SIGN`, check if next token is `INTERFACE`
2. If so, parse as annotation type declaration:
   - Parse modifiers and `@interface` keyword
   - Parse annotation type name
   - Parse annotation type body (elements with defaults, constants)

## Verification

- [ ] `@interface` inside class parses correctly
- [ ] Annotation elements with defaults work
- [ ] Nested annotations within @interface work
- [ ] Spring Framework error count reduced

