# Plan: add-annotation-element-defaults

## Objective
Handle `default value` clause in annotation type element declarations.

## Tasks
1. Add DEFAULT token handling in parseMethodRest()
2. Parse expression after default keyword

## Verification
- [ ] `String name() default "test";` parses
- [ ] Array and class literal defaults work
