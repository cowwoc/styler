# Task Plan: implement-index-overlay-ast

## Objective

Build memory-efficient AST using Arena API for zero-GC-pressure parsing.

## Context

Traditional AST implementations create millions of small objects causing GC pressure.
Index-Overlay pattern stores nodes in contiguous memory with Arena allocation.

## Tasks

1. Create NodeArena with Arena-based allocation
2. Implement NodeType enum for all Java AST node types
3. Create NodeIndex for type-safe node references
4. Implement node attribute records (ImportAttribute, TypeAttribute, etc.)

## Verification

- [ ] Arena allocation works with try-with-resources
- [ ] All node types allocatable
- [ ] Memory usage validated at 512MB per 1000 files

## Files

- `ast/core/src/main/java/.../ast/core/NodeArena.java`
- `ast/core/src/main/java/.../ast/core/NodeType.java`
- `ast/core/src/main/java/.../ast/core/NodeIndex.java`

