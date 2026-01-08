# Summary: add-qualified-class-instantiation

## Status: COMPLETE
**Completed**: 2025-12-30

## What Was Built
- Added NEW token handling in `parseDotExpression()` for qualified instantiation
- Extracted `parseDotExpression()` helper method to reduce NCSS complexity
- Converted `parseNestedTypeDeclaration()` to switch expression

## Supported Syntax
- `outer.new Inner()` - simple qualified instantiation
- `outer.new Inner().method()` - chained method call
- `getOuter().new Inner()` - expression qualifier
- `outer.new Inner(1, 2)` - with constructor arguments
- `Outer.this.new Inner()` - qualified this
- `outer.new Inner() { }` - with anonymous class body

## Files Modified
- `parser/src/main/java/.../parser/Parser.java`

## Files Created
- `parser/src/test/java/.../parser/test/QualifiedInstantiationParserTest.java` - 6 tests

## Quality
- All tests passing
- Zero Checkstyle/PMD violations
