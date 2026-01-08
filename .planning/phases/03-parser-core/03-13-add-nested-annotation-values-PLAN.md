# Plan: add-nested-annotation-values

## Objective
Handle nested annotations as annotation values: `@Foo(@Bar)`.

## Tasks
1. Modify parseAnnotation() to return NodeIndex
2. Add AT case in parsePrimary() for nested annotations
3. Extract helper methods

## Verification
- [ ] `@Repeatable(@Container)` parses
- [ ] Deeply nested annotations work
