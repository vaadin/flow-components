---
name: refactor-spec-write
description: Write a Phase 3 natural-language spec for an extracted helper. Pins what the block does — inputs, outputs, side effects, invariants — in human-readable form, complementing the Phase 2 tests (which pin the behavior in code). Use when an extracted helper has Phase 2 coverage but no spec yet, or when a Phase 4 architectural rename lands a new pattern that should be named in prose.
argument-hint: <helper-class-file> [<existing-test-file>]
disable-model-invocation: true
allowed-tools: Read, Write, Bash(find:*), Bash(grep:*), Bash(wc:*)
---

# Refactor spec write

Produce a Phase 3 natural-language spec for an extracted helper. The
spec is a `.md` file under `refactor-artifacts/specs/` that pins the
helper's contract in prose — what it does, what it depends on, what
invariants the tests verify.

The Phase 2 tests pin behavior in code (assertions against actual
output). The Phase 3 spec pins behavior in prose (why the assertions
are what they are, what the helper's contract WITH the rest of the
system is). Both are safety nets at different abstraction levels.

## Inputs

- `$0` — absolute path to the extracted helper. Required.
- `$1` — absolute path to the existing characterization tests.
  Optional; the skill greps for `{ClassName}Test.java` if omitted.

## When to invoke

- The Phase 2 backlog shows a helper with green characterization
  tests but no spec under `refactor-artifacts/specs/`.
- A Phase 4 slice landed a new architectural pattern
  (e.g. `MirroredProperty`, `ClientReloadFlag`) that deserves a
  named prose home.
- A reader asked "what does this helper actually do?" and the
  source javadoc was insufficient.

## When NOT to invoke

- The helper has no tests yet — write tests first
  (`refactor-characterize`), THEN spec. A spec without a test is
  speculation; a spec without prose is just code.
- The helper is a one-line value object (e.g. an enum or a record).
  The class itself is the spec.
- The helper was just extracted in this session and the journal
  entry already covers it. Specs are for cross-session memory;
  same-session readers have the journal.

## The spec template

Every spec follows this skeleton. Use the same section names so
specs are uniformly grep-able.

```markdown
# Block: {ClassName}

> Phase 3 spec. Drafted from the source (`{ClassName}.java`), the
> characterization tests (`{ClassName}Test.java`, N tests, slice X),
> and the journal entry that introduced it (slice X, date).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

What this block exists to do, in one paragraph. Frame the
*concern* — what state does it own, what decision does it make,
what dispatching does it perform. If there's a known design
pattern (deferred-flush, named seam, mirror property), say so
explicitly so future readers can use the name to find sibling
implementations.

## Inputs

Per-method (or per-coherent-group-of-methods). For each:

- Argument names and types.
- What "valid" means for each argument — null tolerance, range,
  format. Quote the source javadoc if it says something
  non-obvious; flag any disagreement between javadoc and code.
- Any side-channel inputs (reads from a shared field, a static,
  the system clock).

## Outputs

What the method returns. Include type AND meaning (a boolean is
two states; spell which is which). For void methods, write "void
— side effects below."

## Side effects

What changes outside the method's return value. Be specific:
"writes to instance field X", "calls collaborator.doY(...)", "fires
event Z." If the order matters, list in order. If the order is
load-bearing (a test pins it via `InOrder`), say so.

## Invariants

Promises the block makes that the rest of the system relies on.
Each invariant should be:

- Testable (or pinned by an existing test) — if a regression
  could violate it, the safety net should catch it.
- Named, not just described — give the invariant a phrase so it
  can be referenced from other specs.

Example:
- **The accumulator maps are temp state — they're reset at the
  start of every full-workbook style pass via `resetAccumulators`,
  populated by `processCustomStyling`, and consumed exactly once
  by `flattenAccumulators`.**

## Open questions

Things the spec author noticed but isn't sure about:

- A test coverage gap that future work should close.
- A javadoc-vs-code disagreement that needs human resolution.
- A design choice that looks load-bearing but whose justification
  isn't documented.
- A TODO comment in the source that the spec author chose not to
  resolve.

Open questions are NOT bugs — they're a punch list for the
operator. Don't omit them; don't promise to fix them.
```

## Steps

### 1. Read the helper source

End-to-end. Note:

- Public / package-private method shapes.
- Instance fields and what they cache.
- Constructor dependencies (the collaborators).
- Javadoc — but treat as untrusted (legacy comments lie; the spec
  records observed behavior, not promised behavior).

### 2. Read the characterization tests

What scenarios are pinned? Each test corresponds to a behavioral
contract. The spec should mention every load-bearing invariant
the tests pin (even if the test itself is small).

### 3. Find the journal entry for the slice that extracted the helper

```sh
grep "Slice .* — .*$1" SPREADSHEET_REFACTOR_LOG.md
```

The journal entry usually names the *why* — what motivated the
extraction. Pull the rationale into the Purpose section.

### 4. Write the spec

Match the template. **Concision over completeness** — a 60-line
spec a reader will actually read beats a 200-line spec they'll
skim. Aim for ~60-120 lines.

For each section, the question to answer:

- Purpose: *why does this exist?*
- Inputs: *what does the caller need to know?*
- Outputs: *what does the caller get?*
- Side effects: *what changes that the caller didn't directly
  ask for?*
- Invariants: *what does this block PROMISE that other code
  relies on?*
- Open questions: *what would I want to know if I were going to
  modify this?*

### 5. Place the file

`refactor-artifacts/specs/{ClassName}.md`. No subdirectories;
specs are searched by name.

### 6. Stop

Report:

- Path to the spec file.
- Sections present (Purpose / Inputs / Outputs / Side effects /
  Invariants / Open questions — flag any omitted with reason).
- Open-question count.

The operator's next step is usually:

- Continue with the next gap, OR
- Submit the spec for human review (specs ship as draft; status
  flips from "initial draft" to "reviewed" once a human operator
  has read it).

## Pitfalls

- **Treating the javadoc as ground truth.** Legacy javadocs lie.
  Read the code; if the javadoc claims something the code doesn't
  do, the spec records what the code does AND notes the
  disagreement in Open questions.
- **Re-stating the source verbatim.** The spec should ADD value:
  invariants, design intent, cross-references to sibling specs.
  If the spec reads like a method-by-method documentation, it's a
  worse javadoc — delete and start over with the Purpose
  section's "concern" framing.
- **Promising more than the tests pin.** If an invariant isn't
  test-covered, either add a test (`refactor-characterize`) or
  flag it explicitly: "Pinned by inspection, not by test —
  candidate for audit-fix."
- **Listing every test as an invariant.** Some tests are happy-
  path checks; the invariants are the load-bearing contracts.
  Distinguish: 14 tests might pin 6 invariants.
- **Forgetting cross-references.** A spec for `SheetOverlayManager`
  should mention `SheetOverlayStorage` / `SheetOverlayVisibility`
  / `SheetOverlayInfoBuilder` (its collaborators) by name —
  these are the breadcrumbs that connect related specs.
- **Over-specifying the implementation.** "Uses a HashMap" is
  implementation; "the cache key is the cell-style index, NOT
  the cell-position" is contract. Spec the contract.

## When to push back

- **The helper isn't a single concern.** If you can't write a
  single Purpose paragraph because the helper does several
  unrelated things, write a Phase 1 slice plan
  (`refactor-slice-plan`) to split it first, THEN spec the
  pieces.
- **The tests are too sparse to support a spec.** If you'd be
  speculating on most invariants, the right move is to run
  `refactor-characterize` first to pin the missing behavior,
  then write the spec.
- **The helper is going away.** If a Tier 3 rewrite will replace
  it, a spec is wasted effort. Note the planned obsolescence and
  skip.

## Companion artifacts

- The spec sits in `refactor-artifacts/specs/`.
- The Phase 2 tests it complements sit in
  `src/test/java/.../{ClassName}Test.java`.
- The journal entry that motivated it sits in
  `SPREADSHEET_REFACTOR_LOG.md` (find via `grep "Slice .*
  {ClassName}"`).

Together: code (helper) + tests (Phase 2) + spec (Phase 3) +
journal entry. Four artifacts, one shape change.
