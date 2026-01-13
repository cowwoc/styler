# Task Plan: implement-ai-violation-output

## Objective

Build structured JSON output optimized for AI agent consumption.

## Context

Primary differentiator: AI agents need structured, actionable violation data
to learn and improve code generation.

## Tasks

1. Create ViolationReportRenderer with JSON format
2. Design violation schema for AI consumption
3. Add contextual information (surrounding code)
4. Implement machine-readable error codes

## Verification

- [ ] JSON output valid and parseable
- [ ] Violations include location, message, fix
- [ ] Context helps AI understand issue
- [ ] Error codes enable categorization

## Files

- `formatter/src/main/java/.../formatter/ViolationReportRenderer.java`
- `formatter/src/main/java/.../formatter/FormattingViolation.java`

