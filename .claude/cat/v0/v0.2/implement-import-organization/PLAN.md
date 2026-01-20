# Task Plan: implement-import-organization

## Objective

Build import sorting and grouping with unused import detection.

## Context

Import organization is critical for readable code. Must sort imports,
group by package, detect unused imports, and handle static imports.

## Tasks

1. Create ImportOrganizerFormattingRule
2. Implement ImportExtractor to find imports in AST
3. Add ImportGrouper for configurable grouping
4. Implement UnusedImportDetector
5. Add classpath support for type resolution

## Verification

- [ ] Imports sorted alphabetically within groups
- [ ] Groups separated by blank lines
- [ ] Unused imports detected
- [ ] Static imports handled correctly

## Files

- `formatter/src/main/java/.../formatter/importorg/ImportOrganizerFormattingRule.java`
- `formatter/src/main/java/.../formatter/importorg/internal/ImportExtractor.java`
- `formatter/src/main/java/.../formatter/importorg/internal/ImportGrouper.java`

