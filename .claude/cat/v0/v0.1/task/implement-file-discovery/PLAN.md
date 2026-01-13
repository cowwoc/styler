# Task Plan: implement-file-discovery

## Objective

Build Java file discovery with gitignore support.

## Context

CLI needs to find all .java files in directories while respecting
.gitignore patterns to skip generated code and dependencies.

## Tasks

1. Create FileDiscovery for directory traversal
2. Implement GitignoreParser for pattern matching
3. Add PatternMatcher for glob patterns
4. Integrate with SecurityValidator for path validation

## Verification

- [ ] Finds all .java files in directory tree
- [ ] Respects .gitignore patterns
- [ ] Handles symlinks safely
- [ ] Performance acceptable for large projects

## Files

- `discovery/src/main/java/.../discovery/FileDiscovery.java`
- `discovery/src/main/java/.../discovery/GitignoreParser.java`
- `discovery/src/main/java/.../discovery/PatternMatcher.java`

