# Plan: add-package-annotations

## Objective
Parse annotations before `package` keyword in package-info.java files.

## Tasks
1. Add hasPackageLevelAnnotations() lookahead
2. Add isAnnotationTypeDeclaration() helper
3. Extend PACKAGE_DECLARATION to include annotations

## Verification
- [ ] `@Foo package com.example;` parses
- [ ] Multiple annotations before package work
