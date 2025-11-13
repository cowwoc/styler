# Git Workflow Best Practices

## Introduction

This document describes best practices for working with git in our project. Following these practices will help ensure code quality and collaboration effectiveness.

## Creating Feature Branches

When starting new work, you should always create a feature branch. This is important for several reasons:

1. It isolates your work from the main branch
2. It allows others to review your changes before merging
3. It makes it easier to track what changes belong together

To create a feature branch, use the following command:

```bash
git checkout -b feature/my-new-feature
```

The branch name should be descriptive and should clearly indicate what the feature is about.

## Making Commits

### Commit Messages

Commit messages are very important. They help other developers understand what changes were made and why. A good commit message should:

- Have a clear, concise subject line (under 50 characters)
- Include a detailed body if the change is complex
- Reference any related issues or tickets

Here's an example of a good commit message:

```
Add user authentication feature

Implemented JWT-based authentication to secure API endpoints.
Added login and logout functionality with token refresh.

Fixes #123
```

### Commit Frequency

You should commit your work frequently. Frequent commits help because:

1. They create a detailed history of changes
2. They make it easier to find when bugs were introduced
3. They allow you to revert specific changes if needed

Try to make commits whenever you complete a logical unit of work.

## Code Review Process

Before merging to main, all code must go through review. The review process works as follows:

1. Push your feature branch to the remote repository
2. Create a pull request targeting the main branch
3. Request review from at least two team members
4. Address any feedback or requested changes
5. Once approved, squash and merge to main

Reviews typically take 24 hours. Plan accordingly.

## Testing Requirements

All code must pass tests before merging. Our testing requirements are:

- Unit test coverage must be at least 80%
- All existing tests must pass
- Integration tests must pass for affected components

Run tests using:

```bash
mvn test
```

## Summary

Following these git workflow practices will help maintain code quality and enable effective collaboration. Remember to always create feature branches, write clear commit messages, submit code for review, and ensure tests pass.
