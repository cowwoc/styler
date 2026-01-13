# Task Plan: implement-configuration-system

## Objective

Build TOML-based configuration system with hierarchical discovery.

## Context

Styler needs user-configurable formatting rules. TOML chosen for readability.
Hierarchical discovery allows project-level and user-level configs.

## Tasks

1. Create Config record with formatting rule settings
2. Implement ConfigurationLoader with TOML parsing
3. Add ConfigDiscovery for hierarchical config file lookup
4. Integrate Jackson TOML for parsing

## Verification

- [ ] .styler.toml files parsed correctly
- [ ] Hierarchical lookup works (project > user > default)
- [ ] All formatting rules configurable

## Files

- `config/src/main/java/.../config/Config.java`
- `config/src/main/java/.../config/ConfigurationLoader.java`
- `config/src/main/java/.../config/ConfigDiscovery.java`

