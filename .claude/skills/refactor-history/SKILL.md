---
name: refactor-history
description: Mine git history, PRs, issues, and review comments across one or more repos to surface the reasoning behind a piece of legacy code. Use this when planning a slice, drafting a spec, or trying to understand why a god-class method looks the way it does. Inputs a target (file, method, or commit range); outputs a filtered timeline of the most informative PRs / issues / comments with extracted reasoning.
argument-hint: <target-file-or-method-or-commit-range>
disable-model-invocation: true
allowed-tools: Read, Bash(git:*), Bash(gh:*), Bash(grep:*), Bash(wc:*), Bash(awk:*), Bash(sed:*)
---

# Refactor history

Mine the repo history for context the source code doesn't carry. Source code shows *what is*; history shows *what was intended, what was tried and rejected, what bugs shaped which choices, who has the domain knowledge*. These are the four signals every spec-writer / refactor-planner needs and the four signals static analysis alone can't provide.

## Inputs

- `$0` — target. One of:
  - A path to a file or directory.
  - A `path:line` reference to pin a specific method.
  - A commit range like `HEAD~50..HEAD`.
  - A method name pattern like `pruneRowsRange`.

## Setup check (start here)

Check what's available before doing any actual work:

```sh
which gh && gh auth status 2>&1 | head -3   # GitHub CLI for PR/issue lookup
git --version                                # always present
```

There are three useful tiers, each degrading gracefully if the tier above isn't available:

- **Best — git + `gh` authenticated.** Full PR/issue lookup is possible; the timeline includes review comments and linked issues.
- **OK — git only (no `gh`).** Commit history with author + timestamps + commit messages. Often enough for a quick overview, especially when commits reference legacy issue IDs in their messages (e.g. `vaadin/spreadsheet#16637`).
- **Limited — repo is a shallow clone.** `git log --follow` may stop short of the file's actual history. Run `git rev-parse --is-shallow-repository`; if true, request an unshallow.

State which tier the operator is on at the top of the output so they know how much to trust the timeline.

## The principles this skill enforces

Inlined here so the skill is self-contained.

### Trust the source closer to runtime

Multiple signals will tell different stories. When two disagree, **prefer the signal closer to runtime**: live behavior > tests > types > code > commit messages > PR descriptions > issues > old design docs. Commit messages and PR descriptions are *claims* about intent; they can be wrong even when sincere.

### Filter for high human-writing density

Most commits are noise. A 3-line "wip" message is a low-signal commit even if it touches the file. A 100-word PR review comment that says "I considered approach X but ran into bug Y" is gold. Filter aggressively:

- **Skip** dependabot / Renovate / GitHub-actions commits and PRs.
- **Skip** auto-generated PR descriptions (e.g. ones that just list commits).
- **Skip** merge commits (traverse to the underlying commits).
- **Prioritize** PR descriptions over 100 words.
- **Prioritize** issues with explicit reproducers (steps to reproduce, error messages, screenshots).
- **Prioritize** PR-review comments over 50 words (those are where alternatives get debated).
- **Prioritize** commits whose message contains "fix #N" / "resolves #N" / "see #N" (they link to richer context).

### Multi-repo is the same history graph

A long-lived project's history may live in multiple repos: the current monorepo plus a legacy repo where the code used to live. For any single file or method, the full history is the union of: post-migration commits in the current repo, pre-migration commits in the legacy repo, and the migration commit itself (if it preserved history).

For the running example, Vaadin Spreadsheet's history lives in:

- `vaadin/flow-components` — current location, post-Flow-port history.
- `vaadin/spreadsheet` — legacy, pre-Flow-port, archive.

Always check both unless the operator has said otherwise.

## Steps

### 1. Resolve the target

If `$0` is a file path, the target's git scope is straightforward.

If it's `path:line` or a method name, find the method's bounds:

```sh
# Java
grep -nE "^\s+(public|protected|private)\s+\S+\s+{method-name}\s*\(" {path}
# locate the closing brace by indentation match
```

Then run `git log` with `-L:method-name:path` to get a per-method history slice (Git 2.x has this; older Git needs `-L /start-pattern/,/end-pattern/:path`).

### 2. Build the full timeline across repos

```sh
# Current repo
git log --follow --no-merges --oneline -- {path}

# Legacy repo (if applicable)
cd ../legacy-repo && git log --follow --no-merges --oneline -- {legacy-path}
```

Merge both into one chronologically sorted list. Note authors and timestamps; both signal who has domain knowledge.

For the running example, look at the multi-repo memory entry:
`/Users/ugursaglam/.claude/projects/-Users-ugursaglam-Documents-GitHub-flow-components/memory/spreadsheet_history_repo.md` (if it exists in this session's auto-memory) for the legacy repo path.

### 3. Find the PRs

Each commit either has a PR (the common case for public projects) or doesn't (rare). Use `gh` to fetch PR metadata:

```sh
# Find the PR that introduced a commit
gh search prs --repo {owner}/{repo} {commit-sha}
# Or: gh pr view {pr-number}
```

For each PR:

- Read the description.
- Check the linked issues (`Fixes #N`, `Closes #N`, `Part of #N`).
- List the review comments; flag any over 50 words.

### 4. Filter aggressively

Apply the filtering rules from the principles above. The goal is to **end with at most 10 high-signal entries per target** for the operator to read. If you have 50 entries, you haven't filtered enough — look harder for the noise.

### 5. Produce the timeline

Output format:

```
# History timeline — {target}

## Repos checked
- {repo-1}: {N} commits in scope
- {repo-2}: {M} commits in scope

## High-signal entries

### {YYYY-MM-DD} — {repo}#{issue-or-pr} — {one-line description}
**Authors:** {names}
**Reasoning extracted:**
- {bullet — one extracted insight per line, with direct quote from the source.}
- ...

**Links:**
- {URL to PR or issue}
- {URL to relevant review comment, if any}

### {next entry}
...

## Filtered out (summary only)
- {N} dependabot / actions commits — skipped.
- {N} "wip" / "fix" commits without PR context — skipped (low signal).
- {N} merge commits — traversed to underlying.

## Domain experts (by commit count over the last 2 years)
- {name} — {N} commits, {areas touched}
- ...
```

Each "Reasoning extracted" bullet should be a fact the operator can act on. **Avoid speculation.** If a PR description says "the formula evaluator was slow because of repeated style lookups," that's a fact. If the PR description is silent on the why and you're guessing — don't include it.

### 6. Stop

The operator reads the timeline and decides what to do with it. The skill doesn't act on the findings.

## When the history is thin

Three cases worth flagging explicitly in the output:

1. **No PRs found, only commits with terse messages.** The repo was developed without PR discipline. Surface the commit timeline only; mark the section "low-signal" so the operator's expectations match.

2. **PR descriptions exist but are all auto-generated.** Look at the *review comments* instead — sometimes the only human writing on the PR is in the review threads.

3. **Recent commits have rich context, old commits don't.** Common for projects that adopted PR review mid-life. State the date boundary in the output ("Pre-2019 history is low-signal; post-2019 entries are detailed").

## When not to use this skill

- For methods that are obviously pure utilities with no domain meaning (`SpreadsheetUtil.toKey`). The history would be just "introduced in initial commit." Waste of cycles.
- For brand-new code (< 6 months old). The author is probably the operator; just ask.
- When the target is a directory containing > 30 files. The signal is too diluted; narrow the target first.

## Output

Write the timeline to standard output. Optionally save to `<journal>/history/<target-slug>.md` if the operator wants it for future reference — the user-facing `refactor-slice-plan` skill cites these files when their context becomes load-bearing.
