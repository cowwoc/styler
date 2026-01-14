# Plan: extract-expression-parser

## Objective
Create ExpressionParser class to handle all expression parsing, reducing Parser.java size.

## Approach
Extract all expression-related parsing methods from Parser.java into ExpressionParser.
ExpressionParser takes ParserAccess in constructor to access Parser internals.

## Files to Create/Modify

### Create: `parser/src/main/java/io/github/cowwoc/styler/parser/internal/ExpressionParser.java`

```java
package io.github.cowwoc.styler.parser.internal;

/**
 * Helper class for parsing Java expressions.
 * Handles operators, literals, lambdas, method calls, switch expressions, etc.
 */
public final class ExpressionParser
{
    private final ParserAccess parser;

    public ExpressionParser(ParserAccess parser)
    {
        this.parser = parser;
    }

    // All expression parsing methods...
}
```

### Methods to Extract (~800 lines)

**Core expression methods:**
- parseExpression, parseLambdaBody, parseMultiParamLambda
- parseParenthesizedOrLambda, parseAssignment, isAssignmentOperator
- parseTernary, parseBinaryExpression, matchesAny

**Operator precedence chain:**
- parseLogicalOr, parseLogicalAnd
- parseBitwiseOr, parseBitwiseXor, parseBitwiseAnd
- parseEquality, parseRelational, parseShift
- parseAdditive, parseMultiplicative

**Unary and postfix:**
- parseUnary, parsePostfix, parseDotExpression
- parseArrayAccessOrClassLiteral

**Primary expressions:**
- parsePrimary, parseLiteralExpression, parsePrimitiveClassLiteral

**Object/array creation:**
- parseNewExpression, parseArrayCreation, parseObjectCreation, parseArrayInitializer

**Cast and lambda detection:**
- tryCastExpression, parseCastOperand, lookaheadIsArrow, isMultiParamLambda
- canStartUnaryExpressionNotPlusMinus, canStartUnaryExpression

**Switch expression:**
- parseSwitchExpression

### Modify: Parser.java

1. Add field: `private final ExpressionParser expressionParser;`
2. Initialize in constructor: `this.expressionParser = new ExpressionParser(this);`
3. Add delegation method: `public NodeIndex parseExpression() { return expressionParser.parseExpression(); }`
4. Remove extracted method bodies (keep only delegation)

## Verification

```bash
./mvnw compile -pl parser -q
./mvnw test -pl parser -q
```

## Acceptance Criteria
- [ ] ExpressionParser created with all expression methods
- [ ] Parser delegates parseExpression() to ExpressionParser
- [ ] All existing tests pass
- [ ] No public API changes
