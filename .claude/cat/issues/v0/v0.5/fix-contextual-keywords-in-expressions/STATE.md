# State

- **Status:** completed
- **Progress:** 100%
- **Resolution:** implemented
- **Dependencies:** []
- **Completed:** 2026-01-15
- **Last Updated:** 2026-01-15

## Notes

Fixed parsePrimary() to recognize contextual keywords as expression starters
using existing isIdentifierOrContextualKeyword() helper.

Note: The test for `yield` as assignment target was removed because `yield`
has special handling at the statement level (parseStatement intercepts it
before parsePrimary is called). This is correct behavior for Java 14+ where
`yield` is a restricted keyword inside switch expressions.
