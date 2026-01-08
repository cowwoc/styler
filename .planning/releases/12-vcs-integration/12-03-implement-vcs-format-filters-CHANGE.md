# Change: implement-vcs-format-filters

## Objective
Auto-format on checkout (user style) and commit (project style).

## Tasks
1. Implement smudge/decode filter (checkout → user style)
2. Implement clean/encode filter (commit → project style)
3. Create Git merge driver
4. Create merge tool wrapper
5. Create setup command (styler vcs-hooks install)
6. Add Mercurial encode/decode support

## Dependencies
- Core formatting rules (complete)
- implement-line-mapping
- implement-original-preserving-clean

## User Value
Work in preferred style locally, maintain project standards in repo.

## Verification
- [ ] Git filter configuration works
- [ ] Mercurial configuration works
- [ ] Merge conflicts resolved correctly
