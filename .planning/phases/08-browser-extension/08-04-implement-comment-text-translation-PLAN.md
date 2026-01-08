# Plan: implement-comment-text-translation

## Objective
Translate line references in comment text.

## Tasks
1. Detect line reference patterns (line 45, L45, :45, etc.)
2. Exclude code blocks and version numbers
3. Translate repo lines to display lines
4. Add subtle styling for translated numbers
5. Support bidirectional translation (viewing and posting)

## Dependencies
- implement-line-mapping
- create-github-pr-extension (basic structure)

## Verification
- [ ] All common patterns detected
- [ ] Exclusions work correctly
- [ ] Tooltips show original line numbers
