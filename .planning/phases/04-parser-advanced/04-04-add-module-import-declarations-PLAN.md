# Plan: add-module-import-declarations

## Objective
Parse JEP 511 module import declarations (`import module java.base;`).

## Tasks
1. Add MODULE token type and keyword mapping in Lexer
2. Add MODULE_IMPORT_DECLARATION to NodeType enum
3. Create ModuleImportAttribute record
4. Extend parseImportDeclaration() for module imports
5. Update ImportExtractor and ImportGrouper

## Verification
- [ ] `import module java.base;` parses correctly
- [ ] Mixed with regular/static imports works
- [ ] Error handling for missing semicolon/module name
