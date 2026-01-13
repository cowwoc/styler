# Task Plan: array-parsing-features

## Objective

Add JSR 308 type annotations on array dimensions and array type method references.

## Tasks

### Part A: Array Dimension Annotations
1. Add parseArrayDimensionsWithAnnotations() helper method
2. Modify parseType() to call new helper
3. Modify tryCastExpression() for annotated array casts

### Part B: Array Type Method References
1. Modify parsePrimitiveClassLiteral() to check for DOUBLE_COLON after brackets
2. Modify parseArrayAccessOrClassLiteral() similarly for reference types
3. Return ARRAY_TYPE node for parsePostfix() to handle

## Verification

- [ ] `String @NonNull []` parses correctly
- [ ] `int @A [] @B []` per-dimension annotations work
- [ ] Cast expressions with annotated arrays work
- [ ] `int[]::new` parses as method reference
- [ ] `String[][]::new` multi-dimensional works

