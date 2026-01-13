# Task Plan: try-catch-enhancements

## Objective

Add JDK 7+ multi-catch syntax and JDK 9+ try-with-resources variable references.

## Tasks

### Part A: Multi-Catch Support
1. Add UNION_TYPE to NodeType enum
2. Create parseCatchParameter() method
3. Handle `|` operator between exception types

### Part B: Try-Resource Variable References
1. Add peekToken() for lookahead
2. Add isResourceVariableReference() detection
3. Create parseResourceVariableReference() method

## Verification

- [ ] Multi-catch with 2+ exception types parses
- [ ] Single-exception catch still works
- [ ] `try (existingVar)` parses without declaration
- [ ] Mixed declarations and references work

