# Task Plan: add-array-initializer-in-annotation-support

## Objective

Parse array initializers in annotations.

## Tasks

1. Extend annotation parser to handle array element values
2. Support @Annotation({value1, value2}) syntax
3. Add tests with ASM-style annotations

## Context

Real-world compatibility issue discovered when running on Spring Framework 6.2.1.
Affected files: LocalVariablesSorter.java, ObjectUtils.java

## Verification

- [ ] @Annotation({value1, value2}) parses correctly
- [ ] Real-world code in affected files parses

