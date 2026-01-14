# Plan: create-parser-access-interface

## Objective
Create a `ParserAccess` interface in the internal package that exposes Parser's internal methods
to helper classes without making them public on the Parser class itself.

## Approach
Use the "accessor interface" pattern where:
1. `ParserAccess` interface declares methods internal classes need
2. Parser implements ParserAccess
3. Internal classes receive ParserAccess (not Parser) in constructor
4. Parser's internal methods stay private; interface provides controlled access

## Files to Create/Modify

### Create: `parser/src/main/java/io/github/cowwoc/styler/parser/internal/ParserAccess.java`

```java
package io.github.cowwoc.styler.parser.internal;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;

import java.util.List;

/**
 * Internal accessor interface for Parser internals.
 * Allows helper classes in the internal package to access Parser methods
 * without exposing them in the public API.
 */
public interface ParserAccess
{
    // Token navigation
    List<Token> getTokens();
    int getPosition();
    void setPosition(int position);
    Token currentToken();
    Token previousToken();

    // Token consumption
    void consume();
    boolean match(TokenType type);
    void expect(TokenType type);
    void expectIdentifierOrContextualKeyword();
    void expectGTInGeneric();

    // AST construction
    NodeArena getArena();

    // Depth tracking (security)
    void enterDepth();
    void exitDepth();

    // Comment handling
    void parseComments();

    // Type parsing (needed by expression/statement parsers)
    void parseType();
    void parseTypeWithoutArrayDimensions();
    boolean parseArrayDimensionsWithAnnotations();

    // Helper methods
    boolean isPrimitiveType(TokenType type);
    boolean isIdentifierOrContextualKeyword();
    boolean isContextualKeyword(TokenType type);

    // Annotation parsing
    NodeIndex parseAnnotation();

    // Qualified names
    void parseQualifiedName();

    // Block parsing (for statement parser)
    void parseBlock();

    // Expression parsing entry point (for cross-references)
    NodeIndex parseExpression();
}
```

### Modify: `parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java`

Add `implements ParserAccess` to class declaration.
Ensure all interface methods are implemented (most already exist as private methods).

### Modify existing internal classes to use ParserAccess

Update ModuleParser and StatementParser constructors to take ParserAccess instead of Parser.

## Verification

```bash
./mvnw compile -pl parser -q
./mvnw test -pl parser -q
```

## Acceptance Criteria
- [ ] ParserAccess interface created with all needed methods
- [ ] Parser implements ParserAccess
- [ ] ModuleParser updated to use ParserAccess
- [ ] StatementParser updated to use ParserAccess
- [ ] All existing tests pass
- [ ] No public API changes to Parser class
