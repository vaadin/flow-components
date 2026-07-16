---
name: repro
description: Reproduce a Vaadin Flow component bug from a GitHub issue in vaadin/flow-components or a component-specific issue in vaadin/flow. Builds a minimal integration-test View, confirms the bug in a running browser with playwright-cli, points at the likely root cause, pushes a shareable repro/<issue> branch, and (after confirmation) posts a verification-pending summary comment on the issue.
argument-hint: <issue-url>
allowed-tools: Read, Write, Edit, Glob, Grep, Bash(gh:*), Bash(mvn:*), Bash(playwright-cli:*), Bash(npx:*), Bash(git:*), Bash(curl:*), Bash(lsof:*), Bash(kill:*)
---

<!-- In-repo Flow-only port of the ds-tools `repro` skill (v1.4.0). -->

You are a tester reproducing a Vaadin Flow component bug. Input `$0` is a GitHub issue URL in `vaadin/flow-components` or `vaadin/flow`. Work the phases in order. **Never claim a bug is reproduced until you have seen it in a running browser.**

Detailed instructions live in references — read each one the **first time** a phase needs it. They stay in context: on later runs in the same session, do not re-read them.

| Reference | Covers |
| --- | --- |
| [`references/issue-analysis.md`](references/issue-analysis.md) | Phase 1 in full |
| [`references/reproduce.md`](references/reproduce.md) | Phases 2–3: View scaffold, running the server, maintenance branches |
| [`references/verify.md`](references/verify.md) | browser-observation discipline |
| [`references/share.md`](references/share.md) | Phases 4–6 and cleanup |

## Phase 0 — Setup (once per session)

- Resolve `<ROOT>` = `git rev-parse --show-toplevel` (this flow-components checkout). Prefix every later command with absolute paths (`git -C <ROOT> …`) — env vars don't persist between shell calls.
- `<FLOW_CORE>` = `<dirname of ROOT>/flow`, optional — only for root-cause search on `vaadin/flow` issues. If absent, note it and continue.
- Preflight: `playwright-cli --version 2>/dev/null || npx --no-install playwright-cli --version 2>/dev/null` (if neither prints a version, stop and ask the user to run `npm install -g @playwright/cli@latest && playwright-cli install --skills`, resume once confirmed) and `gh auth status`.
- Record the starting branch and `git status --porcelain` as the **baseline** — cleanup compares against it, not against an empty tree.

Skip all of this when already done earlier in this session.

## Phase 1 — Understand the bug

Follow issue-analysis.md: fetch issue + comments in one `gh` call, note the affected version, **classify the regression** (`worked in <ver> / broke in <ver>` | `not a regression` | `unknown`), resolve every named component to its real source, run **fix archaeology** on old issues, write the hypothesis line — **"The bug is X, triggered by Y, observable as Z"** — and search for duplicates.

## Phase 2 — Build the reproduction

The smallest View that exercises the hypothesis, starting from the reporter's example, named after the issue (never overwrite an existing file): `Repro<issue>View.java` with `@Route("repro-<issue>")` per reproduce.md. **Design for iteration** (ids on assertion targets, control buttons, parallel variants, query-parameter switches) so Java-edit restarts stay rare. Prefer a **minimal pair**: the failing case plus a control that isolates the trigger.

## Phase 3 — Run and reproduce

1. Start the server in the background and wait for real readiness per reproduce.md — Jetty does not hot-reload Java.
2. Drive the browser per verify.md. Look for the exact signal Z; the verdict comes from what you **saw** — snapshot, console, screenshot — not the issue text. **Guard:** a client `TypeError` from `jar-resources/*Connector.*` is a stale dev bundle, never the bug — regenerate per reproduce.md before trusting the result.
3. **Iterate before concluding "not reproduced"** — the trigger is often precise (gesture, attach/detach cycle, property combination, timing). Record every attempted variation.
4. Minimize, re-verify after each removal, and capture the demo artifact: screenshot for static failures, short video for motion (recipes in verify.md).

## Phase 4 — Report

Fill `assets/summary-template.md` → `<ROOT>/repro-<issue>-summary.md` with **every** field. Chat reply = verdict + regression + branch + 3–5 essential bullets + pointer to the file; do **not** restate the full summary. Details in share.md.

## Phase 5 — Locate the root cause

Point at the suspected cause as a **permalink pinned to a commit SHA** (format in share.md), searching the layer the evidence implicates — component module, connector JS, or Flow core (`<FLOW_CORE>`). If the evidence implicates the underlying web component itself, say so and point at `vaadin/web-components` source — reproducing there is out of scope. Don't fix the bug unless asked.

## Phase 6 — Decide the disposition (always ask)

**End every run with an `AskUserQuestion` — never a prose "want me to…?".** Options per verdict are in share.md. Never close an issue yourself.

## Cleanup

Follow share.md. Not reproduced → no branch: archive the scaffold to your scratchpad, delete it from the repo, revert any pom edit.
