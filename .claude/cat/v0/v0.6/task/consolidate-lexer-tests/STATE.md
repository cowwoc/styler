# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** []
- **Last Updated:** 2026-01-15

## Scope

Consolidate 8 lexer literal test files (1846 lines) into the main LexerTest (720 lines).

**Files to merge into LexerTest.java:**
- `LexerBinaryLiteralTest.java` (158 lines) → delete
- `LexerFloatingPointWithoutLeadingZeroTest.java` (154 lines) → delete
- `LexerHexFloatLiteralTest.java` (226 lines) → delete
- `LexerHexLiteralTest.java` (193 lines) → delete
- `LexerOctalLiteralTest.java` (175 lines) → delete
- `LexerUnicodeEscapeOutsideLiteralsTest.java` (34 lines) → delete
- `LexerUnicodeEscapeTest.java` (238 lines) → delete
- `LexerUnicodePreprocessingTest.java` (668 lines) → delete

**Result:** Single LexerTest.java (~2566 lines)
