#!/bin/bash
# Clean up invalid lock files (non-.json extensions)
# Run manually or as part of maintenance

LOCKS_DIR="/workspace/locks"

# Find all non-.json files
INVALID_FILES=$(find "$LOCKS_DIR" -type f ! -name "*.json" 2>/dev/null)

if [ -z "$INVALID_FILES" ]; then
  echo "✅ No invalid lock files found"
  exit 0
fi

echo "🚨 Found invalid lock files (non-.json extension):"
echo "$INVALID_FILES"
echo ""
echo "These files have NEVER been valid and are ignored by all scripts."
echo ""
read -p "Delete these files? (y/N): " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
  echo "$INVALID_FILES" | xargs rm -v
  echo "✅ Cleanup complete"
else
  echo "❌ Cleanup cancelled"
fi
