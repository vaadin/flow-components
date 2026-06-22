---
name: code-modernization
description: Framework for evaluating and executing code modernization in a long-lived codebase. Use when asked to "modernize", "use modern features", "make this look like the modern X", or when introducing newer language features to legacy code. Encodes the lessons learned the hard way — that "modern" is context-dependent and unconstrained suggestions are often wrong.
---

# Code modernization

Modernization looks deceptively mechanical from the outside ("use
records!", "switch expressions!", "remove the broad catch!") but in
practice the easy suggestions are also the easy wrong-suggestions. A
modernization proposal that doesn't first map the actual constraints
will produce a flood of changes that either don't compile, break the
wire format, or get rejected in review because they couldn't have
worked. This skill is the framework for not doing that.

## The core lesson

**Do not offer modernization moves until you have mapped the
constraints.** Even seemingly internal code can be:

- Shared with a constrained build target (an older platform, a
  cross-compiler with limited language support, an embedded
  consumer with restricted feature set).
- Part of a serialized wire format where the byte-shape is
  load-bearing.
- A public API surface with hand-written semantics (hand-coded
  `equals`, custom serialization, documented null contracts) that
  a "modern equivalent" would silently change.
- Subject to subclass-extension contracts where tightening
  visibility or finality breaks consumers.

The single highest-leverage move in any modernization is **investigating
the build pipeline, target platform, and contract boundaries BEFORE
suggesting any specific change**. Skip this step and you're producing
speculation, not refactoring.

## When to invoke

- User asks to "modernize", "use modern Java/TypeScript/etc.", "use
  records", "make this idiomatic".
- User asks "what improvements could we make beyond X" where X is the
  current focus.
- A code-review or migration is being planned that touches language
  features (e.g., bumping a target version, adopting a new pattern).

## Workflow

### Step 1 — Map the actual constraints (BEFORE proposing anything)

For every codebase, answer these concretely:

1. **Build pipeline.** What compilers / transpilers / code-generators
   sit between source and the artifact users consume? Each has a
   feature support matrix. Read the relevant pom / package.json /
   build.gradle and CITE the version numbers.

2. **Source-level targets per module.** A monorepo can have multiple
   modules targeting different language levels. The "modernize Java"
   ask for one module may be invalid for another that's compiled
   through a constrained compiler. Find the actual settings (search
   for `source level`, `target`, `--release`, `compileSdkVersion`,
   etc.).

3. **Cross-boundary types.** Which types travel through serialization
   (JSON, protobuf, Avro), reflection, or a wire protocol? These have
   hidden contracts — `equals` semantics, field ordering, default
   values. A naive record migration will silently change them.

4. **Subclass-extension contracts.** Which classes have public /
   protected members that subclasses might override? Tightening
   visibility or sealing a hierarchy breaks unknown external code.

5. **Reference point.** What does the same ecosystem's "modern
   equivalent" look like? Find a comparable module in the same repo
   or organization that uses the modern pattern. Cite specific
   files/directories.

If you cannot answer all five concretely, you're not ready to propose
modernization moves. Read more code first.

### Step 2 — Identify the modernization boundary

Classify every candidate move into one of three buckets:

- **Pure internal (server / process / non-shared).** No cross-boundary
  contract. Modernization is safe within the source-level target. Most
  language-feature moves (switch expressions, pattern matching, var,
  text blocks) live here.

- **Crosses a contract.** Types that flow over a wire, get persisted,
  or are part of an external API. Even "metadata-only" changes here
  (annotations, default values, equals semantics) can break consumers.
  Modernization requires preserving the observable contract — verified
  by a payload snapshot or characterization test against the wire
  shape.

- **Constrained by build target.** Code that runs through a limited
  compiler / transpiler / older target. The available features are a
  subset of what the source-level target documents.

Each bucket gets a different modernization strategy.

### Step 3 — Tier moves by risk × value

A modernization PR full of unrelated changes is a review nightmare.
Tier the work:

- **Tier 1: pure internal, mechanical.** Switch expressions, pattern
  matching, var, text blocks, sealed types, serialVersionUID,
  nullness annotations. One slice per category usually. The test suite
  catches any behavior leak.

- **Tier 2: wire / contract changes that preserve external shape.**
  Consolidating duplicate types, replacing custom serialization with
  standard libraries, narrowing visibility, type unification. Higher
  risk; needs the wire-format snapshot as load-bearing safety net.

- **Tier 3: transformative, replacing the build pipeline / target.**
  Migrating off a legacy compiler, rewriting a client widget in a
  modern toolchain, changing the deployment target. Usually a separate
  project, not a "refactor".

State the tier explicitly when proposing a move. "We could migrate X
to a record" is incomplete — the right statement is "this is a Tier 1
move; the wire-format snapshot will catch any regression; one slice".

### Step 4 — Preserve observable contracts during the move

Modernization is a behavior-preserving refactor unless explicitly
declared otherwise. To verify:

- **Run the existing tests.** The Phase-2 characterization tests we
  build precisely so a refactor can be checked against the pinned
  behavior. If the change touches wire format, the payload snapshot
  is the test that matters most.
- **Check observable contracts for divergence.** A record's
  auto-generated `equals` is value equality. If the existing class
  used reference equality, by-position equality (e.g. a cell DTO
  whose `equals` ignores its value field on purpose), or any other
  hand-written semantics — a record migration changes the contract
  even though it looks like "just modernization".
- **Diff the build output for cross-boundary types.** For JSON DTOs,
  the field-order / null-omission / default-value behavior is part
  of the contract.
- **Pin the contract before the change** if no test exists. Write a
  characterization test or payload snapshot first, then modernize.
- **Persisted-data backward compatibility is non-negotiable** —
  but the contract has TWO levels that must be distinguished, or
  the rule reads as "you can never rewrite a renderer," which is
  wrong. If the codebase consumes files, blobs, database rows,
  or any other durable artifact produced by an earlier version of
  itself OR by an external tool (Excel files, CSVs, exported
  configs, serialized caches), the modernized code MUST honor the
  following:

  **Level 1 — Content + styling-intent preservation
  (truly non-negotiable):**
  - **Old artifacts must load.** The parser / loader must accept
    every previously-accepted input. Narrowing the accepted set
    is a breaking change.
  - **Data must be preserved bit-equivalently.** Cell values,
    formulas, named ranges, sheet structure — what was in the
    file is what comes out the other side.
  - **Styling intent must be honored.** If the file says "bold
    red text with a 2pt border," the rendered output must be
    bold AND red AND have a visible border. The semantic claim
    "this cell is styled like X" must hold across implementations.
  - **Writing remains forward-compatible.** A file written by the
    modernized code must be readable by the un-modernized
    previous version (unless an explicit major-version contract
    change is being declared).

  **Level 2 — Render-pixel-equivalence (NOT promised across UI
  tech changes):**
  - Browser font rendering, 1px-vs-0.5px border thickness on
    different `devicePixelRatio` displays, scrollbar appearance,
    selection-highlight color, exact emulation of every quirk
    the previous UI accumulated over its lifetime — these can
    SHIFT during a renderer rewrite.
  - The safety net here is **golden-screenshot diff testing**
    with acceptable-tolerance thresholds (e.g., per-pixel diff
    under N%; structural-similarity above M), NOT byte-identical
    pixel equality.
  - A rewrite that produces "visually equivalent but not
    pixel-identical" output is still honoring the contract. A
    rewrite that loses styling intent (bold becomes regular, red
    becomes black) is breaking it.

  **Test fixtures for back-compat live in the repo.** If you
  don't already have a fixtures directory with sample artifacts
  produced by older versions, that's the gap to close first —
  modernization without it is flying blind. Bonus: pair each
  artifact with a golden-screenshot of the CURRENT renderer's
  output, so a future rewrite can diff against it.

  **The two levels have different implications for ROI:**
  - A loader / serializer change is Level 1 — high stakes, hard
    to verify, every regression is user-visible. Move slowly.
  - A renderer change is Level 1 ON THE SEMANTICS (you must not
    drop bold-red-with-border) but Level 2 ON THE PIXELS (the
    exact rendering of bold-red-with-border can shift). Move
    with a visual safety net, not a byte-equality safety net.

If the contract genuinely should change, that's a separate commit
with its own justification — not snuck into a "modernization" diff.

### Step 5 — Compare against the in-ecosystem reference

The strongest "modern" target is what the same ecosystem has already
adopted. For a Vaadin Flow component, the reference is what newer Flow
components look like. For a Spring service, it's what the latest
Spring guides recommend. For an iOS app, it's the most recent Apple
sample code in the same vertical.

Don't propose modernization toward an abstract "modern" that isn't
already adopted somewhere in the same context — that's just personal
preference dressed up as a refactor.

## Output format for a modernization proposal

After mapping constraints, produce a tiered survey:

```
## What I observed (concrete, cited)

- Build pipeline: <version, plugin, source-level constraint>
- Reference point: <comparable module path, what it does differently>
- Cross-boundary types: <list, with their roles>
- Hand-written contract divergences from "modern" defaults: <list>

## Tier 1 — pure internal, mechanical (safe-in-isolation)

For each candidate:
- What changes
- Concrete sites (file:line refs)
- Effort: 1 session / multi-session / sweep
- Test impact: zero / pin-then-modernize

## Tier 2 — contract-preserving consolidation

Same shape; explicitly call out the snapshot or test that's the
safety net.

## Tier 3 — transformative (separate project)

Frame as a separate effort; identify the prerequisites.

## Recommended sequencing + dependencies

Diagram or list of which moves block which.
```

If you cannot fill in all five fields under "What I observed", do
more investigation before proposing.

## Pitfalls

- **The "all our libraries already support Java 21" fallacy.** Even
  when the runtime supports it, intermediate compilers (cross-compilers,
  source-to-source transpilers, code generators) often don't. The
  source-level constraint comes from whoever's the bottleneck.
- **The "it's just metadata" fallacy.** Annotations, `serialVersionUID`,
  `@Nullable` — these change wire format and runtime behavior for
  consumers that read them. Not always safe.
- **The records-over-DTOs reflex.** Records have specific
  `equals`/`hashCode`/`toString` semantics. Pre-existing DTOs often
  override these for valid reasons. Check the existing methods before
  proposing a record migration; the divergence is usually load-bearing.
  Additional record-blocker patterns to check before recommending a
  migration:
  - **Extends a non-record class** (e.g. `extends ComponentEvent`,
    `extends BaseEntity`, `extends AbstractFoo`). Records can only
    implement interfaces and extend `Record` — they cannot extend any
    other class. The most common modernization-plan mistake is naming
    framework-event classes as record candidates; they're all blocked.
  - **Post-construction field mutation.** A class can LOOK
    record-shaped (public fields + ctor that sets them) but if any
    caller writes to a field after construction, records are blocked.
    Grep `\.fieldName\s*=` to verify before proposing.
  - **Builder-style population.** Classes whose fields are set
    incrementally (typically by a reader/parser that doesn't know all
    field values up front) cannot become records without restructuring
    the producer to gather all values before construction.
  - **Anonymous-subclass usage.** `new MyClass() { ... }` is blocked
    because records are implicitly final.
  - **Mutators with side effects.** A setter that's "just" updating
    state but ALSO calls `element.setProperty(...)` or fires an event
    is doing work the record's accessor methods can't replicate.
  - **Read-into-mutate pattern.** Classes with a `read(Cell)` or
    `populate(Foo)` method that sets multiple fields are doing
    builder-style mutation. Records require all values at construction.
  Real audit experience: a typical mid-size codebase has many
  record-LOOKING classes (public fields, no equals overrides) but
  ALMOST NONE actually meet all the criteria. Expect "audit found one"
  outcomes; don't promise large-scale record migrations until you've
  greppped for the blockers above on every candidate.
- **Sealing hierarchies you don't fully own.** A `sealed` interface
  closes the implementor set. If external consumers might subclass,
  you've made a breaking change. Verify zero external implementations
  exist (e.g. by inspecting Maven Central downstream usage) OR sequence
  the sealing behind an API version bump.
- **The "modern way" tunnel.** When two modern patterns are both
  viable (e.g. `Optional<T>` vs `@Nullable T`, records vs Lombok),
  pick the one the rest of the codebase / ecosystem already uses.
  Inventing a third option is not modernization, it's churn.
- **Speed of modernization vs review burden.** A 50-file mechanical
  switch-expression sweep is one diff. A 50-file "modernize everything
  here" diff is unreviewable. Slice by category, not by file count.

## When modernization is the WRONG move

Sometimes the right answer is "keep it old". Reasons:

- The code is in maintenance-only mode, scheduled for replacement.
  Modernizing it has zero ROI and adds churn.
- The "modern" feature has subtle differences (records' `equals`,
  pattern matching's variable scope) that the existing code depends
  on the absence of.
- The team is mid-migration on a different track; introducing a
  third pattern dilutes consistency.
- The "old" pattern is actively idiomatic in the ecosystem
  (e.g. POJOs with public fields are still idiomatic in JS-interop
  scenarios; "wrong" only by Java-only standards).

If asked to modernize in any of these contexts, surface the
counter-argument explicitly. Do not silently produce a modernization
PR.

## Companion artifacts

- A modernization PLAN document under `refactor-artifacts/` that
  enumerates the tiers, dependencies, status — like a journal entry
  but cross-cutting. Update it as work progresses.
- For each Tier 1 slice, the standard refactor / test / docs commit
  split — same shape as Phase-1 work.
- For Tier 2 work, a wire-format snapshot test that must pass before
  and after the change.
- For Tier 3 work, a project brief, not a slice plan.

## Porting between languages (a Tier 3 sub-pattern)

When the modernization isn't "use modern Java features" but "rewrite
this in another language entirely" (GWT-Java → TS, ObjC → Swift,
Python → Go), the same Tier 3 framing applies but with extra moves
specific to porting:

### Local source over JAR / online docs

Read the source you're porting FROM in the local repo, not via
artifact extraction. JAR sources via `m2 unzip` work but miss surrounding
context (sibling files, test usage patterns, recent commits that
explain "why"). Sibling repos like `../flow`, `../docs`,
`../web-components` are almost always cheaper to grep than chasing
an extracted source jar. Confirm before each port chunk that the
upstream repo isn't already gone — sometimes the "modernized"
target's repo holds the answer for "what should this look like in
the new lang", which JARs never do.

### Byte-equivalence is the default; document deviations with QUIRK callouts

Pure-algorithm code should port byte-equivalent — same inputs
produce same outputs, including for edge-case quirks. When you
encounter source code that looks like an off-by-one or has
suspicious-looking conditions:

- **Preserve verbatim** in the port. Don't "fix" what looks wrong;
  the upstream may depend on the quirk in non-obvious ways.
- **Document with a `QUIRK` callout** in javadoc / docstring.
  Example: `// QUIRK: GWT uses 'row >= length', NOT '>', so a length-N
  array provides heights for rows 1..N-1 only. Preserved for
  byte-equivalence; flag for human audit.`
- **Add a regression test that pins the quirk.** Future-you will be
  tempted to "fix" the code; the test prevents that. Name the test
  to explain the quirk: `getRowHeightPx_at_arrayLength_returnsDefault_NOT_lastEntry`.

If you're CERTAIN the quirk is a bug AND the original is unused
elsewhere, declare it intentionally. The pattern from slice 119
was: preserve the `[A-z]` regex character class verbatim (genuine
upstream intent — wider character set) BUT escape regex metachars
in sheet names (real bug not relied upon). Both moves documented
explicitly in the source.

### Tests must ACTUALLY RUN — `tsc --noEmit` is not a runtime check

A TypeScript file that compiles cleanly under strict mode can
still have semantic bugs that only a runtime test catches. The
canonical example: `if (row > definedRowHeightsPx.length)` is
syntactically valid TS and types check perfectly — but it's a `>`
vs `>=` off-by-one that only a unit test exposes. Compile-checks
verify shape; tests verify behavior. Always run the tests for
ported code.

This implies: **verify the test runner actually executed the
tests** before celebrating "all green". A test runner returning
"8 test files | 0 passed, 0 failed" exits with code 0 but ran
nothing. Verify the test count matches what you wrote (~N tests
expected → N reported, ±1 for hooks). See `refactor-verify` Gate 4.

### Use the same in-ecosystem reference as for code modernization

The same Step 5 applies — port toward what the ecosystem has
already adopted. For a Vaadin GWT → TS port, the reference is
`vaadin-grid-flow`'s `gridConnector.ts` (modern Lit + TS in the
same repo). Cite the reference file in the port's source comments
so future readers can audit "is this still the canonical pattern."
