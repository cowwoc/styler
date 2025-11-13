# Git Best Practices

Before performing a rebase operation, you must establish a backup branch. This ensures you can recover if something goes wrong.

After completing the rebase, validation is critical. Use `git diff` to verify all changes are present.

Force-pushing to the main branch is prohibited under all circumstances.

Once your changes are successfully merged, remember to delete the backup branches to keep the repository clean.
