# Change: fix-array-creation-expression-parsing

## Objective
Fix array creation expression parsing (`new int[5]`, `new int[]{1,2,3}`).

## Tasks
1. Create parseTypeWithoutArrayDimensions() helper method
2. Modify parseNewExpression() to use new helper
3. Allow array dimensions and initializers to be parsed correctly

## Verification
- [ ] `new int[5]` parses correctly
- [ ] `new int[]{1,2,3}` initializer syntax works
- [ ] Multi-dimensional arrays work
