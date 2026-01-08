# Plan: resolve-wildcard-imports

## Objective
Enhance import organization with wildcard resolution.

## Tasks
1. Create WildcardResolver to scan classpath
2. Extend ImportAnalyzer to use classpath for wildcard analysis
3. Detect unused wildcard imports when classpath available
4. Optionally expand wildcards to explicit imports
5. Fall back to conservative mode without classpath

## Dependencies
- implement-import-organization (complete)
- add-classpath-support (complete)

## Context
Spring Framework 6.2.1 test showed ~400 warnings about unresolved wildcard imports.

## Verification
- [ ] Wildcard imports resolved correctly
- [ ] Expansion to explicit imports works
- [ ] Comprehensive tests with sample classpaths
