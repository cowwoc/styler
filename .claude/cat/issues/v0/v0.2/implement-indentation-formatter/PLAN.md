# Task Plan: implement-indentation-formatter

## Objective

Build indentation checking for tabs vs spaces and consistent depth.

## Context

Indentation consistency is fundamental. Support tabs or spaces with
configurable indent size. Track nesting depth.

## Tasks

1. Create IndentationFormattingRule
2. Implement indentation detection
3. Add depth tracking for nested structures
4. Handle continuation line indentation

## Verification

- [ ] Detects incorrect indentation
- [ ] Supports tabs and spaces
- [ ] Configurable indent size
- [ ] Continuation lines handled

## Files

- `formatter/src/main/java/.../formatter/indentation/IndentationFormattingRule.java`

