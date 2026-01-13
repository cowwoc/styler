# Task Plan: fix-switch-expression-case-parsing

## Objective

Handle complex switch expression patterns.

## Tasks

1. [x] Analyze failing switch patterns from Spring Framework
2. [x] Fix "Unexpected token in expression: CASE" errors
3. [x] Fix "Unexpected token in expression: ELSE" errors
4. [x] Ensure all JDK 21+ patterns supported

## Context

Real-world compatibility issue discovered when running on Spring Framework 6.2.1.
Affected files: CodeEmitter.java, ConcurrentReferenceHashMap.java

## Verification

- [x] Complex switch expressions parse correctly
- [x] All JDK 21+ patterns supported

