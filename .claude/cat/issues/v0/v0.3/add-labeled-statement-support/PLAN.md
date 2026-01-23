# Task Plan: add-labeled-statement-support

## Objective

Add labeled statement parsing: `label: statement`.

## Tasks

1. Add LABELED_STATEMENT to NodeType enum
2. Add IDENTIFIER:COLON lookahead in parseStatement()
3. Create parseLabeledStatement() method

## Verification

- [ ] `outer: for(...)` parses
- [ ] `break outer;` works with labels

