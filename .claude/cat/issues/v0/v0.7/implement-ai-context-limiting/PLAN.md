# Task Plan: implement-ai-context-limiting

## Objective

Limit violation output to preserve AI agent context window.

## Tasks

1. Add --ai-mode or --max-violations flag
2. Implement priority-based violation selection (severity, frequency, locality)
3. Provide terse summary for remaining violations
4. Auto-detect AI agent environment

## Dependencies

- implement-ai-violation-output (complete)

## Verification

- [ ] Top N violations shown with full details
- [ ] Summary for remaining violations
- [ ] Benchmark context savings

