# Plan: add-array-dimension-annotations

## Objective
Parse JSR 308 type annotations on array dimensions (JDK 8+).

## Tasks
1. Add parseArrayDimensionsWithAnnotations() helper method
2. Modify parseType() to call new helper
3. Modify tryCastExpression() for annotated array casts

## Verification
- [ ] `String @NonNull []` parses correctly
- [ ] `int @A [] @B []` per-dimension annotations work
- [ ] Cast expressions with annotated arrays work
