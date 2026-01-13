# Task Plan: implement-line-length-formatter

## Objective

Build line length checking and fixing with context-aware breaks.

## Context

First formatting rule. Must detect lines exceeding max length and
suggest/apply fixes at appropriate break points.

## Tasks

1. Create LineLengthFormattingRule
2. Implement ContextDetector for smart break points
3. Add LineLengthViolation with fix suggestions
4. Integrate with FormattingConfiguration

## Verification

- [ ] Detects lines over configured max
- [ ] Suggests breaks at operators, method calls
- [ ] Auto-fix produces valid Java
- [ ] Works with all expression types

## Files

- `formatter/src/main/java/.../formatter/linelength/LineLengthFormattingRule.java`
- `formatter/src/main/java/.../formatter/linelength/internal/ContextDetector.java`

