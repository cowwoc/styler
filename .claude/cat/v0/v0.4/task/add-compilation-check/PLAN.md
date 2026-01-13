# Task Plan: add-compilation-check

## Objective

Validate source files are compiled before formatting.

## Tasks

1. Create CompilationValidator class for validation logic
2. Create CompilationValidationResult sealed interface
3. Add validateCompilation() to FileProcessingPipeline
4. Implement timestamp comparison

## Verification

- [ ] Missing class files detected
- [ ] Stale class files (older than source) detected
- [ ] Valid compilation passes

