# Release Claude Code Plugin

**Purpose**: Release a new version of a Claude Code plugin by merging to main, tagging, and preparing
the next version branch.

**When to Use**:
- After completing work on a plugin version branch
- When ready to publish a new plugin version
- Before starting work on the next version

## Prerequisites

- Current branch is the version branch to release (e.g., `v2.0.4`)
- All changes committed and tested
- Both `package.json` and `.claude-plugin/plugin.json` have matching versions

## Release Process

### 1. Verify Version Consistency

Before releasing, verify both JSON files have the same version:

```bash
echo "package.json:" && jq '.version' package.json
echo "plugin.json:" && jq '.version' .claude-plugin/plugin.json
```

**If versions don't match**, fix them before proceeding.

### 2. Merge to Main

```bash
# Ensure you're on the version branch
git branch --show-current

# Checkout main and merge with fast-forward
git checkout main
git merge v{CURRENT_VERSION} --ff-only
```

**If fast-forward fails**: The branches have diverged. Either rebase the version branch onto main first,
or investigate why main has commits not in the version branch.

### 3. Create Release Tag

```bash
# Create tag pointing to current HEAD (which is now the merged version)
git tag v{CURRENT_VERSION}

# Verify tag points to correct commit
git log --oneline -1 v{CURRENT_VERSION}
```

### 4. Create Next Version Branch and Delete Released Branch

```bash
# Create and checkout new version branch
git checkout -b v{NEXT_VERSION}

# Delete the released version branch locally (prevents accidental post-release commits)
git branch -d v{CURRENT_VERSION}
```

**Why delete the branch?** Once tagged and merged, the version branch serves no purpose. Keeping it
allows accidental post-release commits that won't be in the tag, causing confusion.

### 5. Bump Version Numbers

Update both JSON files to the next version:

```bash
# Update package.json
jq '.version = "{NEXT_VERSION}"' package.json > /tmp/package.json && mv /tmp/package.json package.json

# Update plugin.json
jq '.version = "{NEXT_VERSION}"' .claude-plugin/plugin.json > /tmp/plugin.json && mv /tmp/plugin.json .claude-plugin/plugin.json

# Verify updates
echo "package.json:" && jq '.version' package.json
echo "plugin.json:" && jq '.version' .claude-plugin/plugin.json
```

### 6. Update CHANGELOG.md (if exists)

If the project has a CHANGELOG.md with a "Current Version" table, update it:

```bash
# Update current version in CHANGELOG
sed -i 's/| Combined Plugin.*|.*|/| Combined Plugin (CAT) | {NEXT_VERSION} | {DATE} |/' CHANGELOG.md
```

### 7. Commit Version Bump

```bash
git add package.json .claude-plugin/plugin.json CHANGELOG.md
git commit -m "chore: bump version to {NEXT_VERSION}"
```

### 8. Push Everything

```bash
# Push main branch
git push origin main

# Push the release tag
git push origin refs/tags/v{CURRENT_VERSION}

# Delete the released version branch from remote
git push origin --delete v{CURRENT_VERSION}

# Push new version branch
git push origin v{NEXT_VERSION}
```

**Note**: Deleting the remote branch eliminates ambiguous refs (branch and tag with same name).

## Complete Example

Releasing v2.0.4 and preparing v2.0.5:

```bash
# 1. Verify versions match
jq '.version' package.json           # Should show "2.0.4"
jq '.version' .claude-plugin/plugin.json  # Should show "2.0.4"

# 2. Merge to main
git checkout main
git merge v2.0.4 --ff-only

# 3. Create release tag
git tag v2.0.4

# 4. Create next version branch and delete released branch
git checkout -b v2.0.5
git branch -d v2.0.4

# 5. Bump versions
jq '.version = "2.0.5"' package.json > /tmp/package.json && mv /tmp/package.json package.json
jq '.version = "2.0.5"' .claude-plugin/plugin.json > /tmp/plugin.json && mv /tmp/plugin.json .claude-plugin/plugin.json

# 6. Update CHANGELOG (if applicable)
sed -i 's/| Combined Plugin.*|.*|/| Combined Plugin (CAT) | 2.0.5 | 2026-01-14 |/' CHANGELOG.md

# 7. Commit
git add package.json .claude-plugin/plugin.json CHANGELOG.md
git commit -m "chore: bump version to 2.0.5"

# 8. Push everything
git push origin main
git push origin refs/tags/v2.0.4
git push origin --delete v2.0.4
git push origin v2.0.5
```

## Cleaning Up Ambiguous Refs

If you encounter both a branch and tag with the same name (e.g., `v2.0.4` branch and `v2.0.4` tag),
clean it up:

```bash
# Check what refs exist
git show-ref v{VERSION}

# If branch exists and tag already released, delete the branch
git branch -d v{VERSION}           # local
git push origin --delete v{VERSION}  # remote

# To push with ambiguous refs (temporary workaround)
git push origin refs/tags/v{VERSION}     # tag
git push origin HEAD:refs/heads/v{VERSION}  # branch
```

## Verification Checklist

After release, verify:

- [ ] `git show main:package.json | jq '.version'` shows released version
- [ ] `git show main:.claude-plugin/plugin.json | jq '.version'` shows released version
- [ ] `git tag -l` includes the new tag
- [ ] `git show v{VERSION}:package.json | jq '.version'` matches tag name
- [ ] Released version branch deleted: `git branch -l v{CURRENT_VERSION}` returns nothing
- [ ] Remote branch deleted: `git ls-remote origin refs/heads/v{CURRENT_VERSION}` returns nothing
- [ ] New version branch exists and is checked out
- [ ] Version files show next version number

## Troubleshooting

### Fast-forward merge fails

```bash
# Check what's different
git log main..v{VERSION} --oneline
git log v{VERSION}..main --oneline

# If main has commits not in version branch, rebase:
git checkout v{VERSION}
git rebase main
git checkout main
git merge v{VERSION} --ff-only
```

### Tag already exists

```bash
# Delete and recreate
git tag -d v{VERSION}
git tag v{VERSION}

# Force push tag
git push origin refs/tags/v{VERSION} --force
```

### Version mismatch between files

```bash
# Check both files
jq '.version' package.json
jq '.version' .claude-plugin/plugin.json

# Fix the one that's wrong
jq '.version = "{CORRECT_VERSION}"' {FILE} > /tmp/temp.json && mv /tmp/temp.json {FILE}
git add {FILE}
git commit --amend --no-edit
```

## Hotfix Workflow

If you need to patch a released version after the branch was deleted:

```bash
# Create hotfix branch from the release tag
git checkout -b v{VERSION}-hotfix v{VERSION}

# Make fixes, then release as a new patch version
# Example: hotfix for v2.0.4 becomes v2.0.5
```

**Important**: Do NOT recreate the original version branch. Always increment the version number for
any changes, even small fixes.

## Related

- Plugin installation: `claude plugin add {repo-url}`
- Plugin update: `claude plugin update {plugin-name}`
- Plugin marketplace registry
