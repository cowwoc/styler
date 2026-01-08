# Plan: fix-block-comment-in-member-declaration

## Objective
Handle block comments between class/interface/enum member declarations.

## Tasks
1. Identify call sites not calling parseComments() before parseMemberDeclaration()
2. Add parseComments() call at start of parseMemberDeclaration()
3. Add test coverage

## Verification
- [ ] Block comments between members parse
- [ ] All call sites uniformly handle leading comments
