# Plan: extract-expr-binary-operators

## Objective
Extract binary expression and operator precedence methods from Parser.java to ExpressionParser.java.

## Parent Task
Decomposed from: extract-expression-parser (sequence 2 of 6)

## Scope

### Methods to Extract (~120 lines)

From Parser.java lines 2794-2907:

| Method | Lines | Purpose |
|--------|-------|---------|
| parseBinaryExpression | 2794-2807 | Generic binary expression parser |
| matchesAny | 2815-2825 | Token matching helper |
| parseLogicalOr | 2827-2830 | Logical OR precedence level |
| parseLogicalAnd | 2832-2835 | Logical AND precedence level |
| parseBitwiseOr | 2837-2840 | Bitwise OR precedence level |
| parseBitwiseXor | 2842-2845 | Bitwise XOR precedence level |
| parseBitwiseAnd | 2847-2850 | Bitwise AND precedence level |
| parseEquality | 2852-2855 | Equality precedence level |
| parseRelational | 2857-2891 | Relational and instanceof |
| parseShift | 2893-2897 | Shift operators |
| parseAdditive | 2899-2902 | Addition and subtraction |
| parseMultiplicative | 2904-2907 | Multiplication, division, modulo |

### Prerequisites
- create-parser-access-interface completed (ParserAccess interface exists)
- ExpressionParser skeleton exists

**Note:** This task can run in parallel with other extract-expr-* subtasks.

### Implementation Steps

1. Copy methods to ExpressionParser.java
2. Transform field accesses (same as subtask 1)
3. These methods call each other in a chain - verify internal calls work
4. Add delegation stubs in Parser.java
5. Verify compilation

## Verification

```bash
./mvnw compile -pl parser -q
```

## Acceptance Criteria
- [ ] 12 methods extracted to ExpressionParser
- [ ] Operator precedence chain intact
- [ ] Parser compiles successfully
