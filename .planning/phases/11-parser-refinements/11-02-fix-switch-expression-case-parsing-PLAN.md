# Plan: fix-switch-expression-case-parsing

## Objective
Handle complex switch expression patterns.

## Tasks
1. Analyze failing switch patterns from Spring Framework
2. Fix "Unexpected token in expression: CASE" errors
3. Fix "Unexpected token in expression: ELSE" errors
4. Ensure all JDK 21+ patterns supported

## Context
Real-world compatibility issue discovered when running on Spring Framework 6.2.1.
Affected files: CodeEmitter.java, ConcurrentReferenceHashMap.java

## Verification
- [ ] Complex switch expressions parse correctly
- [ ] All JDK 21+ patterns supported
