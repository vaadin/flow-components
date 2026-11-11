# Phases 4–6 — Report, share, cleanup

## 1. The report

Copy [`assets/summary-template.md`](../assets/summary-template.md) to `<ROOT>/repro-<issue>-summary.md`. The summary becomes the issue comment, so a maintainer must get the result in a few seconds.

**Visible part**: about 6 lines, in this order. Reference another repository's issue or PR as `vaadin/<repo>#N`; a bare `#N` links to this one.

1. The verdict, with the branch and version you observed it on. When you also ran the View on `main`, add that result, and the fixing PR when `main` is fixed.
2. The regression status: `Not a regression`, `Regression: worked in <ver>, broke in <ver>, introduced by #<PR>` (§5), or `Regression status unknown`. Base it on what you observed, not only on the issue text. When no introducing PR is known, omit that part.
3. For a partial reproduction, one sentence on which part of the report did not reproduce.
4. The root cause in 2 to 4 sentences: what the code does, why that breaks, and what triggers it.
5. The root-cause permalink (§4) on its own line.
6. The reproduction branch as pushed (§3, including any `-2` suffix) and the route.

**`<details>` block**: only evidence that the visible part cannot carry. When there is none, delete the block.

- A `| Case | Result |` table, when variants behave differently or the trigger differs from the one in the issue.
- The exact gesture or timing, when the steps in the issue do not trigger the bug.
- A screenshot (§4) for a visual bug.
- The name of the video file on the branch, for a motion bug.
- A longer root-cause explanation, when the short one skips a necessary step.
- Anything you did not verify in the browser, labelled as not verified.

**Exclude from the summary**:

- Anything the issue already says: expected behavior, the reporter's steps, the reporter's code.
- A fix proposal or a list of fix options. A suggested fix biases the person who takes the issue.
- Placeholders such as `n/a` or `none found`. Delete the line instead.
- Environment details, the scaffold source, and run instructions. The branch carries them.
- Dev-server noise. Cite only console output that belongs to the bug.

**Not reproduced or works as designed**: say so plainly, and do not force a positive result. Use the template's **Reason** line instead of the root cause, with one of:

- fixed on `main`, with the fixing PR from fix archaeology
- version-specific
- obsolete API
- missing context
- intended behavior, with the javadoc or documentation that defines it

Delete the permalink and the reproduction line when they do not apply.

**Duplicate**: keep the template's duplicate line after the verdict. §3 defines its content.

**Chat reply is a digest, not a copy**: verdict + regression classification + branch (if pushed) + 3–5 bullets of what was observed, then point at the summary file. Don't restate the full summary in chat — the file is canonical and becomes the comment.

Report in chat the run details that the summary excludes:

- variations tried
- dead ends
- dependencies added to the IT pom

## 2. End every run with a question — never prose

The outward action is the user's; present it as an `AskUserQuestion` (2–4 options, recommended first), never a prose "want me to post…?":

- **Reproduced:** `Push + post + label` · `Push branch only` · `Report only`.
- **Not reproduced / fixed / duplicate:** the first option cites everything fix archaeology found — `Post close comment "Duplicate of <repo>#N, fixed by #PR"` (or just the PR / duplicate when only one is known); only when nothing was found offer the generic `Post "Could not reproduce using the latest Vaadin version. Closing as stale."`. Always include `Report only`.
- **Reproduced but works-as-designed / already tracked by an enhancement:** `Report only` · `Push + post noting it`.

Posting the comment is yours on approval; **closing the issue stays the user's action** unless they explicitly ask.

## 3. Share the reproduction branch (reproduced only)

Push from `<ROOT>`, under the `repro/` prefix so branches sort together.

1. Note the starting branch: `git -C <ROOT> rev-parse --abbrev-ref HEAD`.
2. Branch from the exact HEAD you reproduced on (e.g. a maintenance branch): `git -C <ROOT> checkout -b repro/<issue>`.
3. **Stage only the files you created/edited** — scaffold, summary, demo screenshot/`*.webm` (copy from `/tmp` first), any IT-pom edit. **Never `git add -A`.**
   ```bash
   git -C <ROOT> add <…>/Repro<issue>View.java <…>/integration-tests/pom.xml repro-<issue>-summary.md repro-<issue>.png
   git -C <ROOT> commit -m "test: reproduce #<issue> (<component>)"
   ```
4. Push: `git -C <ROOT> push -u origin repro/<issue>`. If the remote branch exists, use a suffix (`repro/<issue>-2`) rather than force-pushing.

**Posting the comment** goes to a public upstream issue. **Show the rendered comment and get ONE explicit confirmation covering both the comment AND the `ai repro` label** — a separate label request after posting can be denied by permission tooling. Post via file input:

```bash
gh api repos/vaadin/<repo>/issues/<issue>/comments -F body=@repro-<issue>-summary.md
```

Capture the returned `html_url`. Keep the `> [!WARNING]` disclaimer first. Then label (same confirmation): `gh issue edit <issue> --repo vaadin/<repo> --add-label "ai repro"`. The `ai repro` label exists in `vaadin/flow-components`; `vaadin/flow` currently has none — check `gh label list --repo vaadin/<repo> --search "ai repro"` and skip gracefully, noting it in the digest.

**Duplicate:** state it in the comment and recommend closing as a duplicate of #N (link both; note if #N is fixed). Surface the close command and run it only on explicit approval — never close yourself:

```bash
gh issue close <issue> --repo vaadin/<repo> --reason "not planned" --comment "Duplicate of #N"
```

## 4. Screenshot embedding + permalinks

Reference a committed screenshot by **commit SHA**, not branch name — a slashed branch name breaks raw URLs. Add to the summary's `<details>` block before posting:

```markdown
![<caption>](https://raw.githubusercontent.com/vaadin/<repo>/<commit-sha>/repro-<issue>.png)
```

where `<commit-sha>` is `git -C <ROOT> rev-parse HEAD`. A video can't embed via `gh` — surface the local `.webm` path so the approver can drag-drop it.

Point at a suspected root cause as a **permalink pinned to a commit SHA**, not a bare `file:line`. On its own line it renders as an inline snippet:

```
https://github.com/vaadin/<repo>/blob/<SHA>/<path>#L<start>-L<end>
```

Anchor it to the commit you reproduced on (`git -C <ROOT> rev-parse HEAD`); your branch leaves `src/` untouched, so line numbers match.

## 5. Introducing PR (regression only)

When Phase 1 classified the issue as a regression, name the PR that introduced it. Start from the root-cause lines of Phase 4.

1. List the commits between the last working release and the first broken release that changed the root-cause file. Release tags are plain versions, such as `25.3.5`.
   ```bash
   git -C <ROOT> log --oneline <last-working-tag>..<first-broken-tag> -- <path>
   ```
2. When the list is long, narrow it to the root-cause lines or to a distinctive token.
   ```bash
   git -C <ROOT> log -s --format='%h %ad %s' --date=short -L<start>,<end>:<path>
   git -C <ROOT> log --oneline -S "<distinctive token from the root cause>" -- <path>
   ```
3. Take the PR number from the commit subject. The first `(#NNNN)` is the original PR. A second `(#NNNN)` after `(CP: <branch>)` is the backport, so do not cite it.
4. Read the candidate diff (`git -C <ROOT> show <sha>`) and confirm that it causes the behavior. A matching title can mislead.
5. When the root cause is in the web component or in Flow core, run the same search in `<WC_ROOT>` or `<FLOW_CORE>` with the release tags of that repository.
6. When no commit fits, write the range without a PR and say "introducing PR not found" in chat. Do not guess, and do not switch branches to search further.

## 6. Cleanup

Kill the server per [reproduce.md](reproduce.md) §9 (`jetty:stop`, then verify the port is free — never kill the background Maven task, that orphans the forked JVM).

**If it reproduced and you pushed `repro/<issue>`** — scaffold, summary, and IT-pom edit are committed on that branch, so the working line is already clean. Just switch back:

```bash
git -C <ROOT> checkout <starting-branch>
git -C <ROOT> branch -D repro/<issue>      # optional; preserved on the remote
git -C <ROOT> branch -D <major>.<minor>    # drop any temp maintenance branch
```

**If it did not reproduce (no branch pushed)** — order matters: a tracked edit (the IT `pom.xml`) makes `git checkout <branch>` abort, so discard tracked edits first, delete untracked scaffold, then switch back:

```bash
git -C <ROOT> checkout -- <path/to/edited/pom.xml>   # discard tracked edits (if any)
rm -f <path/to/Repro<issue>View.java>                # delete untracked scaffold
git -C <ROOT> checkout <starting-branch>
git -C <ROOT> branch -D <major>.<minor>              # drop any temp maintenance branch
```

Confirm `git -C <ROOT> status --porcelain` matches the Phase 0 baseline — your scaffold files gone, pre-existing entries untouched — and the repo is on the branch it started on. Delete any `/tmp` screenshot. Leave the pushed `repro/*` branches — they are the shared artifacts.
