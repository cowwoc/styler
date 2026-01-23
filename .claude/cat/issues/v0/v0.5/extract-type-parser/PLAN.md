# Plan: extract-type-parser

## Objective
Create TypeParser class to handle type declaration parsing (class, interface, enum, record, annotation).

## Approach
Extract type declaration parsing methods from Parser.java into TypeParser.
TypeParser takes ParserAccess in constructor.

## Files to Create/Modify

### Create: `parser/src/main/java/io/github/cowwoc/styler/parser/internal/TypeParser.java`

```java
package io.github.cowwoc.styler.parser.internal;

/**
 * Helper class for parsing Java type declarations.
 * Handles class, interface, enum, record, and annotation type declarations.
 */
public final class TypeParser
{
    private final ParserAccess parser;

    public TypeParser(ParserAccess parser)
    {
        this.parser = parser;
    }

    // All type declaration parsing methods...
}
```

### Methods to Extract (~400 lines)

**Type declarations:**
- parseTypeDeclaration, isModifier
- parseClassDeclaration, parseInterfaceDeclaration
- parseEnumDeclaration, parseAnnotationDeclaration
- parseRecordDeclaration, parseImplicitClassDeclaration

**Generic parameters:**
- parseTypeParameters, parseTypeParameter
- parseTypeArguments, parseTypeArgument

**Type bodies:**
- parseClassBody, parseEnumBody, parseEnumConstant

**Member declarations:**
- parseMemberDeclaration, skipMemberModifiers
- parseNestedTypeDeclaration, parseMemberBody
- parseIdentifierMember, parsePrimitiveTypedMember

**Method/field parsing:**
- parseMethodRest, parseParameter, parseFieldRest

### Modify: Parser.java

1. Add field: `private final TypeParser typeParser;`
2. Initialize in constructor: `this.typeParser = new TypeParser(this);`
3. Add delegation methods for public-facing type parsing
4. Remove extracted method bodies

## Verification

```bash
./mvnw compile -pl parser -q
./mvnw test -pl parser -q
```

## Acceptance Criteria
- [ ] TypeParser created with all type declaration methods
- [ ] Parser delegates to TypeParser
- [ ] All existing tests pass
- [ ] No public API changes
