# Plan: add-module-info-parsing

## Objective
Parse JPMS module declarations (module-info.java files).

## Tasks
1. Create ModuleParser helper class
2. Implement module declaration parsing with open module support
3. Add requires directive with transitive/static modifiers
4. Add exports/opens directives with qualified targets
5. Add uses directive for service declarations
6. Add provides directive with multiple implementations
7. Add isModuleDeclarationStart() lookahead

## Verification
- [ ] Basic `module foo.bar { }` parses
- [ ] All directive types work
- [ ] Modifiers (transitive, static) work
