# Plan: add-try-resource-variable

## Objective
Add JDK 9+ effectively-final variable reference support in try-with-resources.

## Tasks
1. Add peekToken() for lookahead
2. Add isResourceVariableReference() detection
3. Create parseResourceVariableReference() method

## Verification
- [ ] `try (existingVar)` parses without declaration
- [ ] Mixed declarations and references work
