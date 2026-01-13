# Task Plan: module-support

## Objective

Add complete JPMS module support: module imports (JEP 511) and module-info.java parsing.

## Tasks

### Part A: Module Import Declarations
1. Add MODULE token type and keyword mapping in Lexer
2. Add MODULE_IMPORT_DECLARATION to NodeType enum
3. Create ModuleImportAttribute record
4. Extend parseImportDeclaration() for module imports
5. Update ImportExtractor and ImportGrouper

### Part B: Module-Info Parsing
1. Create ModuleParser helper class
2. Implement module declaration parsing with open module support
3. Add requires directive with transitive/static modifiers
4. Add exports/opens directives with qualified targets
5. Add uses directive for service declarations
6. Add provides directive with multiple implementations
7. Add isModuleDeclarationStart() lookahead

## Verification

- [ ] `import module java.base;` parses correctly
- [ ] Mixed with regular/static imports works
- [ ] Basic `module foo.bar { }` parses
- [ ] All directive types work
- [ ] Modifiers (transitive, static) work

