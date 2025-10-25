#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in protocol-and-role-reminder.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Protocol and Role Reminder Hook
# Trigger: SessionStart
# Purpose: Provide unified protocol and role guidance on every session start/resume
# Consolidates: session-start-role-reminder.sh, task-protocol-reminder.sh

cat << 'EOF'
╔═══════════════════════════════════════════════════════════════╗
║  TASK PROTOCOL & MAIN AGENT ROLE                              ║
╟───────────────────────────────────────────────────────────────╢
║  PROTOCOL SCOPE                                               ║
║  • Todo.md tasks → MUST use task protocol                     ║
║  • Ad-hoc work → Protocol NOT required                        ║
║  • See: protocol-scope-specification.md for full rules        ║
╟───────────────────────────────────────────────────────────────╢
║  MAIN AGENT ROLE BOUNDARIES                                   ║
║  ✅ CAN: Coordinate, configure, document, git operations      ║
║  ❌ CANNOT: Create .java/.ts/.py in IMPLEMENTATION state      ║
║           → Delegate to stakeholder agents instead            ║
╚═══════════════════════════════════════════════════════════════╝
EOF

exit 0
