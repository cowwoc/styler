# Plan: add-array-type-method-references

## Objective
Parse array type constructor references like `int[]::new` and `String[][]::new`.

## Tasks
1. Modify parsePrimitiveClassLiteral() to check for DOUBLE_COLON after brackets
2. Modify parseArrayAccessOrClassLiteral() similarly for reference types
3. Return ARRAY_TYPE node for parsePostfix() to handle

## Verification
- [ ] `int[]::new` parses as method reference
- [ ] `String[][]::new` multi-dimensional works
- [ ] Error cases handled (primitive without array)
