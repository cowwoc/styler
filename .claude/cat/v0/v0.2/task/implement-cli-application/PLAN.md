# Task Plan: implement-cli-application

## Objective

Build command-line interface for styler check and format operations.

## Context

CLI is primary interface for AI agents and developers. Must support
check mode (report violations) and format mode (auto-fix).

## Tasks

1. Create CliMain entry point with argument parsing
2. Implement ArgumentParser for CLI options
3. Add OutputHandler for human and JSON output
4. Integrate with FileProcessingPipeline

## Verification

- [ ] `styler check src/` works
- [ ] `styler format src/` works
- [ ] Exit codes match scope.md specification
- [ ] JSON output parseable

## Files

- `cli/src/main/java/.../cli/CliMain.java`
- `cli/src/main/java/.../cli/ArgumentParser.java`
- `cli/src/main/java/.../cli/CLIOptions.java`
- `cli/src/main/java/.../cli/OutputHandler.java`

