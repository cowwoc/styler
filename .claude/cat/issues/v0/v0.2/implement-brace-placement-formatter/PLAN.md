# Task Plan: implement-brace-placement-formatter

## Objective

Build brace placement checking for configurable style (same-line vs next-line).

## Context

Brace placement is highly configurable. Support both K&R style (same-line)
and Allman style (next-line) for classes, methods, and control flow.

## Tasks

1. Create BraceFormattingRule
2. Implement brace detection in AST
3. Add configurable placement options
4. Handle different contexts (class, method, if, for, etc.)

## Verification

- [ ] Detects incorrect brace placement
- [ ] Supports same-line and next-line styles
- [ ] Different rules for different contexts work
- [ ] Auto-fix moves braces correctly

## Files

- `formatter/src/main/java/.../formatter/brace/BraceFormattingRule.java`

