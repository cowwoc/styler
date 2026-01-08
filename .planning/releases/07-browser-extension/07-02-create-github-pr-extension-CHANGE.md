# Change: create-github-pr-extension

## Objective
Browser extension to view GitHub PRs in user's preferred style.

## Tasks
1. Create PR detection for GitHub diff pages
2. Implement GitHub API integration for raw content
3. Integrate Styler formatter (via WASM or bundled JS)
4. Implement DOM replacement for reformatted code
5. Update line numbers to match reformatted code
6. Add user configuration storage
7. Package for Chrome and Firefox

## Dependencies
- Core formatting rules (complete)
- implement-line-mapping

## User Value
Unique differentiator - no competitor offers this.

## Verification
- [ ] Chrome extension works
- [ ] Firefox extension works
- [ ] User preferences saved
