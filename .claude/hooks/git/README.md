# Git Hooks

This directory contains git hooks for the project.

## Setup

After cloning the repository, register the hooks by running:

```bash
git config --local core.hooksPath .claude/hooks/git
```

## Hooks

### pre-commit

Runs automatically before each commit to validate:

- **Gutted hooks detection** - Blocks commits that accidentally remove functional code from hook files
- **Checkstyle validation** - Blocks commits with Java style violations
- **PMD validation** - Blocks commits with static analysis violations
