# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** []
- **Last Updated:** 2026-01-14

## Error Pattern

**~30 occurrences** in Spring Framework 6.2.1

Multiple errors involving contextual keywords used as identifiers in expression context:

| Keyword | Count | Example |
|---------|-------|---------|
| VAR | 9 | `this.var = var;` |
| MODULE | 5 | `module.getResourceAsStream(name)` |
| TO | 3 | `this.to = to;` |
| WITH | 1 | `return with(Arrays.asList(delegates))` |
| OPEN | 1 | `this.open = open;` |
| RECORD | 2 | `RfcUriParser.UriRecord record = ...` |
| REQUIRES | 2 | `moduleVisitor.visitRequire(requires, ...)` |

The parser recognizes these as contextual keywords but doesn't allow them
in all expression contexts where identifiers are valid.
