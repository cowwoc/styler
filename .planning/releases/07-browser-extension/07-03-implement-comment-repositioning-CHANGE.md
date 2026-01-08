# Change: implement-comment-repositioning

## Objective
Reposition PR comments using line mapping.

## Tasks
1. Parse GitHub comment anchors from DOM
2. Look up display line using lineMapping
3. Move comment marker to display line
4. Add tooltip showing original repo line
5. Handle edge cases (one-to-many, many-to-one, deleted lines)

## Dependencies
- implement-line-mapping
- create-github-pr-extension (basic structure)

## Verification
- [ ] Comments appear on correct display lines
- [ ] Visual regression tests pass
