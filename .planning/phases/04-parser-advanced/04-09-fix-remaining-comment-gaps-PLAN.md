# Plan: fix-remaining-comment-gaps

## Objective
Add parseComments() calls to support comments in more locations.

## Tasks
1. Identify all parser methods missing comment handling
2. Add parseComments() calls for array initializers
3. Add parseComments() calls for lambda expressions
4. Add parseComments() calls for control flow statements
5. Add parseComments() calls for block handling

## Verification
- [ ] Comments in array initializers parse
- [ ] Comments in lambdas parse
- [ ] Comments around control flow parse
