# Plan: add-multi-catch-support

## Objective
Add JDK 7+ multi-catch syntax: `catch (IOException | SQLException e)`.

## Tasks
1. Add UNION_TYPE to NodeType enum
2. Create parseCatchParameter() method
3. Handle `|` operator between exception types

## Verification
- [ ] Multi-catch with 2+ exception types parses
- [ ] Single-exception catch still works
