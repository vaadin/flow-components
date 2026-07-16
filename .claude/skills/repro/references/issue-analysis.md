# Phase 1 — Understanding the bug

Trackers in scope: `vaadin/flow-components` and `vaadin/flow` — component-specific bugs are frequently filed in the Flow tracker (e.g. a `ComponentRenderer` or `Grid` bug). The tracker doesn't decide the owning layer; the reproducing code path does.

## 1. Fetch the issue

One call (`gh issue view --comments` prints nothing non-interactively):

```bash
gh issue view <n> --repo vaadin/<repo> --json number,title,state,body,labels,comments
```

- Keep any `--jq` simple — no nested double quotes inside a double-quoted command; build strings with `+`.
- Read for a code example, reproduction steps, expected vs. actual behavior.
- A "works for me" / "could not reproduce" comment does **not** cancel the attempt — build and run it yourself, and mine the comment for variations (version, theme, browser, data set, exact gesture). Report not-reproduced only after genuine Phase 3 iteration.
- **Fetch attached reproduction projects** — they can decide the verdict (misuse vs. bug). Zips linked as `github.com/.../files/...` download with `curl -sL`; linked repos read via `gh api "repos/<user>/<repo>/git/trees/HEAD?recursive=1" --jq '...'` and `gh api repos/<user>/<repo>/contents/<path> --jq .content | base64 -d`.

## 2. Affected version + regression classification

Note the affected version if stated. Check the current line (`<flow.version>` / `<vaadin.version>` in the repo `pom.xml`). If the bug is reportedly fixed in a newer line or the range is below the checkout, switch to the matching maintenance branch — see [reproduce.md](reproduce.md).

**Classify the regression:** `worked in <ver> / broke in <ver>` when there is evidence of both ends; `not a regression` when broken since the feature shipped; `unknown` otherwise. Recorded in the report for the team — informational only.

## 3. Resolve every named component to its real source — never assume API from memory

Tags, attributes, and APIs drift across versions; a wrong guess sends the reproduction at the wrong artifact. Find the Java class under `vaadin-<name>-flow-parent/`, and use the API the way existing integration-test views use it. Never assume markup or API from memory.

## 4. Fix archaeology (issues more than ~2 years old)

Old issues are often already fixed, the fix never linked back. Before scaffolding:

1. Grep the component's IT module for an existing regression view/test matching the symptom.
2. `git log --oneline -S "<distinctive symbol or error string>" -- <suspect paths>` to find the fixing commit/PR; record `fixing PR: <#N | none found>` for the summary. Many fixes are broad reworks with no single greppable PR — "none found" is a normal answer.
3. Check the reported API still exists (e.g. `TemplateRenderer` was removed) — a removed API trends the verdict toward "obsolete".
4. Before citing a candidate fixing PR, confirm it touched the relevant module — `git show <sha> --stat`. A matching title can mislead.

A found fix does **not** skip browser verification, but it lets the view stay minimal and the report cite "fixed by #PR (with regression test)" — far more closeable than "could not reproduce".

## 5. Intended behavior, then hypothesis

Check what the component should do (docs, `src/` API, javadoc contracts, tests), not just what the reporter expected — if it works as designed, the verdict is "works as designed (likely misuse)". Write one line: **"The bug is X, triggered by Y, observable as Z"**, where Z is the exact failure signal to look for.

## 6. Duplicates

Search open and closed issues for the component plus a distinctive symptom across the trackers:

```bash
gh search issues "<component> <distinctive token>" --repo vaadin/flow-components --repo vaadin/web-components --repo vaadin/flow
```

`--repo vaadin/web-components` is included on purpose — a Flow component bug may duplicate a web-component issue. Confirm a real match only after reproducing — same root cause, stack trace, and trigger, not a similar title. A confirmed match makes the verdict "duplicate of #N".
