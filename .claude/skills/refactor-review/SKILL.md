---
name: refactor-review
description: LLM code-review pass on a refactor slice's diff. Checks the structural invariants that automated gates can't — no behavior change hiding in a switch-case, no constant drift, no comments that lie about what the code now does, no helper that quietly grew a third responsibility. Use this before opening a PR for the slice, after refactor-verify is green.
argument-hint: <commit-or-ref>
disable-model-invocation: true
allowed-tools: Read, Bash(git:*), Bash(grep:*), Bash(wc:*), Bash(awk:*)
---

# Refactor review

Read the slice's diff. Check it against the structural invariants below. Report a fixed-shape verdict: each invariant gets `pass`, `flag`, or `fail`, plus a one-line justification for anything that isn't `pass`.

This skill exists because automated gates only check syntactic and observable invariants. The structural invariants (Rules A–D below) need a reader who can *recognize the pattern* in code. That's the LLM's job here.

## Inputs

- `$0` — git ref or commit hash for the slice. Optional. Defaults to `HEAD` if omitted.

## The invariants this skill checks

Inlined verbatim — this skill is self-contained.

### Rule A — No new public/protected members on visibility-frozen classes

A refactor must not widen the published surface of a class that's already public. The diff must show:

- Zero new `public` or `protected` methods on classes that existed before the slice and are not annotated as internal.
- Zero new `public` or `protected` fields on the same.
- Zero changes from `private` to `public`/`protected` (only `private` → package-private is allowed).
- Public/protected members that disappeared from a god class must reappear as **thin delegators** in the same class — typically one to four lines that forward to a helper. They must not be removed outright; doing so is a breaking change for subclasses.

**How to check in the diff:**

```sh
git diff $0~..$0 -- '*.java' | grep -E '^\+' | grep -E '^\+    (public|protected) '
```

For each match, decide: did this member exist before (same signature on a different class)? Is the class it lives on visibility-frozen? If both yes → `flag`. If both no (member is on a new package-private helper) → `pass`.

### Rule B — Logic decomposed, not just decoupled

The slice must have actually broken logic into smaller pieces. Check by reading the diff for these anti-patterns:

- **Interface-in-front-of-monolith** — a new interface is added, but the implementation class still has the same methods doing the same things. The logic moved nowhere.
- **Facade pattern** — a new helper has methods that just call back into the original class for everything. The original still owns the logic.
- **Rename-only move** — a top-level extraction of a previously-anonymous inner class with no other changes. That's a move, not a split.

**How to check:** for each new helper file, look at the method bodies. If > 80% of lines in the helper are calls back to the parent (`spreadsheet.foo()`, `parent.bar()`), the helper is a facade, not a decomposition. Flag.

### Rule C — Multi-class slice (3–5 helpers per slice)

A slice that adds only one helper is a sign decomposition stopped at the first level. The new helper is probably itself a smaller god class.

**How to check:**

```sh
git diff --name-status $0~..$0 | grep -E '^A\s+.*\.(java|ts|py|go)$' | grep -v Test
```

Count the new non-test files. Fewer than 3 → flag (unless the slice was a deliberate "single concern" extraction, in which case the operator should have noted it). More than 5 → flag (slice is doing too much).

### Rule D — Behavior preserved (the silent-change checks)

The diff must not change observable behavior. Common silent changes to grep for:

- **Constant drift** — a numeric literal or string constant changed in the move. Even a `14` → `20` in a "padding pixel" constant is a behavior change.
- **Conditional flip** — `if (a && b)` becomes `if (a || b)`, `>=` becomes `>`, `!= null` becomes `== null`. These are easy to make accidentally when retyping a moved block.
- **Switch fall-through change** — a `case` that had a `break` no longer does, or vice versa. The new code's switch must have the same `break` / fall-through pattern as the old.
- **Statement reordering** — `a(); b();` becoming `b(); a();` is a behavior change if `a` and `b` share state. Specifically watch for:
  - Style update before vs after value clear (matters for cache invalidation).
  - Event fire before vs after state mutation (matters for listener observation).
- **Reassignment vs in-place mutation** — if the original setter was `this.x = y`, the helper's setter must also *replace* the reference, not `x.putAll(y)` / `x.addAll(y)` / `x.merge(y)`. The original behavior is "callers can expect a fresh map every call"; merging silently changes that to "callers' old map references stay live and grow."
- **Exception type or message** — a `catch (IOException)` becoming `catch (Exception)`, or a thrown exception's message text changing.
- **Null-check addition or removal** — adding a `if (x == null) return;` to "be safe" is a behavior change; removing one because "it can't happen" is also a behavior change.
- **String trimming / lowercasing added** — moving a `s.trim()` from one place to another changes when the trim applies.

**How to check:** read every moved block in the diff side by side with its pre-image (`git show $0~:<path>` for the old version). For every line that doesn't match exactly, ask: is this a meaning change?

### Rule E — Comments updated to reflect the new structure

When a method body moves out of a class, its surrounding comments and javadoc should:

- Move with it (if they describe what the method does).
- Be removed from the old location (if the old location is now a delegator).
- Be updated to reference the new helper if they explain "see also" relationships.

Stale comments saying "this method calls foo() to compute X" when foo() has moved to a helper are misleading. Flag stale comments.

### Rule F — No new TODOs / FIXMEs / XXXs

A refactor slice that adds `TODO` comments has either smuggled in incomplete work or surfaced a defect that should be a separate ticket.

```sh
git diff $0~..$0 | grep -E '^\+' | grep -E '(TODO|FIXME|XXX)\b'
```

Any match → flag. The operator should either complete the TODO in the same slice or file it as a follow-up before merging.

### Rule G — Tests updated alongside moved code

If a test class reflectively accesses a field that the slice moved (`getDeclaredField("foo")`), the test must be updated in the same commit. A passing test suite proves this — but `refactor-verify` already ran. This rule is about the **reverse**: if tests *weren't* updated and they still pass, did the test silently lose its assertion?

**How to check:** for any test file in the diff, look at what changed. Did an assertion get replaced with a weaker assertion (`assertNotNull` instead of `assertEquals`)? Did a test get `@Disabled` / `.skip()`? Flag those.

## Steps

### 1. Identify the slice

```sh
git show $0 --stat
git diff --name-status $0~..$0
```

Confirm what changed: files added, modified, deleted. Note the god class file (large reduction expected) and the new helper files (added).

### 2. Run the seven invariant checks

For each rule A–G above:

- Run the suggested grep / diff check.
- Read the resulting output (and the surrounding code where needed — don't judge from grep alone).
- Mark `pass` / `flag` / `fail`.

`pass` = no concern.
`flag` = potential issue, operator should look. Provide a one-line description.
`fail` = clear violation, must be fixed before merge. Provide the exact code location.

### 3. Read every moved block side by side

For each block of code that moved from the god class to a helper:

- Find it in the old file (`git show $0~:<path>`).
- Find it in the new file.
- Diff them mentally. Every line that differs gets one of:
  - **Stylistic** (whitespace, naming, formatting) — OK.
  - **Structural** (added or removed line, reordering) — flag unless clearly equivalent.
  - **Semantic** (literal change, operator change, condition flip) — fail.

This is the highest-value check the LLM contributes. Automated tools won't catch a subtle operator change buried in a 50-line move.

### 4. Output a fixed-shape verdict

```
Rule A (no new public API):           pass
Rule B (logic decomposed):            pass
Rule C (multi-class slice):           pass — 5 new helpers
Rule D (behavior preserved):          flag — see comment on src/.../X.java:88
Rule E (comments updated):            pass
Rule F (no new TODOs):                pass
Rule G (tests still strong):          pass

Overall: 1 flag, 0 fails. Operator review needed before merge.

Flag details:
- src/.../X.java:88 — `clearCell` reorders style-update before value-clear; the pre-image had value-clear first. May affect POI's cache invalidation. Worth a manual trace.
```

### 5. Stop

The skill does not edit. It reads and reports. The operator decides whether to act on flags.

## When to escalate instead of flag

If the diff shows a clear breaking change — a removed public method, a changed return type, a new thrown exception — escalate to a `fail` with explicit reasoning. Don't soften it to a `flag` to avoid friction. The point of this skill is to catch what the gates can't; a softened verdict is the inverse of useful.
