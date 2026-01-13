# Task Plan: add-qualified-this-super

## Objective

Add qualified `this` and `super` expressions for inner classes.

## Tasks

1. Add THIS/SUPER handling in parsePostfix() after DOT
2. Update error message for expected tokens

## Verification

- [ ] `Outer.this` parses
- [ ] `Outer.super.method()` parses

