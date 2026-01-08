# Change: add-semantic-validation

## Objective
Add semantic analysis pass for context-sensitive validation.

## Tasks
1. Create SemanticValidator class as AST visitor
2. Validate control flow (yield in switch, break/continue in loops)
3. Validate labels (break label exists and reachable)
4. Validate modifiers (static not on local classes)
5. Validate annotations (@Override on overriding methods)
6. Validate expressions (this not in static context)
7. Validate declarations (duplicate variable names)
8. Collect all errors (don't stop at first)

## Dependencies
- None (parser enhancement)

## Verification
- [ ] Tests for each semantic constraint category
- [ ] Integration with parse pipeline
