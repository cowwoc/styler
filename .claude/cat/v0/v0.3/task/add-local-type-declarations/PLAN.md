# Task Plan: add-local-type-declarations

## Objective

Parse local type declarations inside method bodies.

## Tasks

1. Add isLocalTypeDeclarationStart() lookahead
2. Add parseLocalTypeDeclaration() dispatch
3. Add skipBalancedParens() helper

## Verification

- [ ] Local class/interface/enum/record parse
- [ ] Works in all contexts (method, constructor, lambda)

