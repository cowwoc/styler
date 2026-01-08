# Plan: create-virtual-format-plugin

## Objective
IDE plugin for VCS systems without filter support (SVN, Perforce, TFVC).

## Tasks
1. Implement IntelliJ plugin with virtual document layer
2. Implement VS Code extension with TextDocumentContentProvider
3. Support bidirectional editing (view in user style, save in project style)
4. Per-user configuration support

## Dependencies
- Core formatting rules (complete)
- implement-vcs-format-filters

## Priority
LOW - Only needed for SVN/Perforce/TFVC users (~2% of market).

## Verification
- [ ] IntelliJ plugin works
- [ ] VS Code extension works
- [ ] Bidirectional editing correct
