# Contributing

## Setup ownership

- Dilukshan (`gitcivilian`): Member 1 backend and repository foundation.
- Ganamoorthy (`Ganamoorthy`): Member 2 frontend foundation.
- Member 3 work is unstarted.

Use branch labels and PR descriptions to identify task ownership. Commit using the actual contributor's Git identity; task ownership does not change commit authorship.

## Feature workflow

```powershell
git switch main
git pull --ff-only
git switch -c feat/your-task
# Implement and verify
git add <specific-files>
git commit -m "feat: describe the completed task"
git push -u origin feat/your-task
```

Open a pull request and merge it after checking the diff and relevant builds. Use meaningful `chore:`, `feat:`, `fix:`, `docs:` and `test:` messages.

## Checks

Run `.\mvnw.cmd clean verify` in `backend` and `npm run build` in `frontend`. Keep credentials, local data, dependency folders and build output out of Git.

Update the README/API notes when behavior changes. Record actual meetings and demonstrator feedback in `docs/MEETING_LOG.md`. Use four-space Java indentation and two-space frontend indentation.
