# Plan: add-unicode-escape-preprocessing

## Objective
Implement JLS §3.3 Unicode escape preprocessing.

## Tasks
1. Add decodedText field to Token record
2. Add tryParseUnicodeEscape() with checkpoint/rollback
3. Support multiple-u syntax
4. Update Parser to use decodedText() for semantic operations
5. Remove redundant getText(sourceCode) method

## Verification
- [ ] `int \u0041 = 1;` parses (Unicode for 'A')
- [ ] Multiple-u syntax works (`\uuu0041`)
- [ ] Original source preserved for formatter
