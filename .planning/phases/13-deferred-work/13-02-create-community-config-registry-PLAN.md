# Plan: create-community-config-registry

## Objective
Peer repository for community-contributed style configs (DefinitelyStyled).

## Tasks
1. Create separate GitHub repo (definitely-styled or styler-configs)
2. Define repository structure (configs/org/project.toml)
3. Implement registry lookup in styler init
4. Implement config download/caching
5. Create contribution guidelines
6. Set up CI validation for contributed configs

## Dependencies
- implement-config-inference

## User Value
Like DefinitelyTyped for TypeScript - community maintains configs for projects.

## Verification
- [ ] Git remote detection works
- [ ] Config resolution priority correct
- [ ] CI validates contributed configs
