# Task Plan: fix-comment-in-member-declaration

## Objective

Fix parser to handle comments appearing between modifiers and member declarations.

## Problem Analysis

**Error**: "Unexpected token in member declaration: LINE_COMMENT" (434 occurrences in Spring Framework)

Comments between modifiers (like `@Override`) and declarations cause parse failures.

## Example Failing Code

```java
@Override
// Some comment explaining the method
public void doSomething() { }

@SuppressWarnings("unchecked")
/* Block comment */
private List<String> items;
```

## Tasks

1. [ ] Identify all locations in `parseClassBodyDeclaration()` where comments can appear
2. [ ] Add `parseComments()` calls after parsing modifiers/annotations
3. [ ] Add `parseComments()` calls before parsing method/field declarations
4. [ ] Handle both LINE_COMMENT and BLOCK_COMMENT tokens
5. [ ] Add tests for comments in various positions

## Technical Approach

In `parseClassBodyDeclaration()`:
1. After parsing annotations, call `parseComments()`
2. After parsing access modifiers, call `parseComments()`
3. Before parsing the actual declaration (method/field/type), call `parseComments()`

## Verification

- [ ] Comments after annotations parse correctly
- [ ] Comments after modifiers parse correctly
- [ ] Both line and block comments handled
- [ ] Spring Framework error count reduced

