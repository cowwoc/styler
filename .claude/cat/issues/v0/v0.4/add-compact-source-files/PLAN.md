# Task Plan: add-compact-source-files

## Objective

Parse JEP 512 implicit classes (JDK 25 feature).

## Tasks

1. Add IMPLICIT_CLASS_DECLARATION to NodeType enum
2. Add allocateImplicitClassDeclaration() to NodeArena
3. Add isTypeDeclarationStart() with lookahead
4. Add isMemberDeclarationStart() helper
5. Modify parseCompilationUnit() to detect implicit class scenarios
6. Add parseImplicitClassDeclaration() method

## Verification

- [ ] Files without explicit class declarations parse
- [ ] Instance main methods (`void main()`) work
- [ ] Mixed with package/imports works

