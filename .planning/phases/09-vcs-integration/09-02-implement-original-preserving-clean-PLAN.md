# Plan: implement-original-preserving-clean

## Objective
Clean filter preserves formatting of unchanged code.

## Tasks
1. Get original file from Git
2. Parse both original and modified into AST
3. Diff ASTs to find changed nodes
4. Preserve original formatting for unchanged nodes
5. Apply repo style for changed/new nodes
6. Merge to produce final output

## Dependencies
- implement-ast-diff

## Benefits
- No need to support 100% of arbitrary styles
- Minimal diffs (semantic changes only)
- Graceful degradation

## Verification
- [ ] Mixed-style files preserved correctly
- [ ] Edge cases handled (new files, moved code)
