# Task Plan: comment-parsing-fixes

## Objective

Add parseComments() calls to support comments in all parser locations.

## Tasks

### Part A: Remaining Comment Gaps
1. Identify all parser methods missing comment handling
2. Add parseComments() calls for array initializers
3. Add parseComments() calls for lambda expressions
4. Add parseComments() calls for control flow statements
5. Add parseComments() calls for block handling

### Part B: Block Comments in Member Declarations
1. Identify call sites not calling parseComments() before parseMemberDeclaration()
2. Add parseComments() call at start of parseMemberDeclaration()
3. Add test coverage

## Verification

- [ ] Comments in array initializers parse
- [ ] Comments in lambdas parse
- [ ] Comments around control flow parse
- [ ] Block comments between members parse
- [ ] All call sites uniformly handle leading comments

