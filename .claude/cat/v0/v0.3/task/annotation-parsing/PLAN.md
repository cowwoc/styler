# Task Plan: annotation-parsing

## Objective

Add comprehensive annotation parsing support: element defaults, package annotations, and nested values.

## Tasks

### Part A: Annotation Element Defaults
1. Add DEFAULT token handling in parseMethodRest() after throws clause
2. Parse expression after default keyword

### Part B: Package Annotations
1. Add hasPackageLevelAnnotations() lookahead
2. Add isAnnotationTypeDeclaration() helper
3. Extend PACKAGE_DECLARATION to include annotations

### Part C: Nested Annotation Values
1. Modify parseAnnotation() to return NodeIndex
2. Add AT case in parsePrimary() for nested annotations
3. Extract helper methods

## Verification

- [ ] `String name() default "test";` parses
- [ ] Array and class literal defaults work
- [ ] `@Foo package com.example;` parses
- [ ] Multiple annotations before package work
- [ ] `@Repeatable(@Container)` parses
- [ ] Deeply nested annotations work

