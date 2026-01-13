# Task Plan: add-binary-hex-literals

## Objective

Add binary (0b), hexadecimal (0x), and octal literal support.

## Tasks

1. Enhance consumeNumber() for 0b/0B/0x/0X prefixes
2. Add consumeBinaryNumber() for binary literals
3. Add consumeHexNumber() for hex literals

## Verification

- [ ] `0b1010`, `0xFF`, `0755` tokenize correctly
- [ ] Underscores in literals work

