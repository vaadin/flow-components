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
Python → Go), the same Tier 3 framing applies — with one critical
distinction that flips the default approach. The flip is between
two RADICALLY different port modes, and getting them confused is
the most expensive mistake in this domain.

### The two port modes

**Mode A — Protocol port** (byte-equivalence required):

For code that ENCODES a wire format the other side depends on —
JSON DTOs, RPC interface method names, file-format readers/writers,
HTTP request shapes, the discriminator strings in event unions —
the port MUST preserve byte-equivalence with the original. The
"other side" (the server, the storage layer, the external consumer)
expects the bytes in the format the original emits; deviation is a
breaking wire-format change, not a refactor.

**Mode B — Algorithm port** (behavior contract, clean-room rewrite):

For code that IMPLEMENTS behavior — math, parsing, formatting,
state machines, visibility calculations — the port should be a
CLEAN-ROOM REWRITE that preserves the behavior CONTRACT (same
documented inputs → same documented outputs) without copying the
implementation. This is harder than verbatim porting but produces
code that:

- Looks like the target ecosystem's modern idiom, not translated
  source-language code.
- Drops upstream bugs / accidents / off-by-ones instead of
  carrying them forward.
- Avoids licensing/copyright signals that a verbatim port carries
  (more on this below).
- Is maintainable by the target-language team without reference
  to the source-language original.

### Which mode applies?

| Type of code | Mode |
| --- | --- |
| JSON DTOs (`CellData`, `MergedRegion`) | **A — protocol** |
| RPC interface method names (`SpreadsheetClientApi`) | **A — protocol** |
| Event-type discriminators (`'cellSelected'`, `'popupButtonClick'`) | **A — protocol** |
| CSS class names hardcoded by external testers | **A — protocol** |
| File-format parsers (e.g. .xlsx reader if applicable) | **A — protocol** |
| Pixel-math algorithms (`computeViewportBounds`) | **B — algorithm** |
| Coordinate parsers (`parseCellRef`, `parseColRow`) | **B — algorithm** |
| Visibility / membership predicates (`isCellSelected`) | **B — algorithm** |
| Formatters (`convertPointsToPixel`, TSV serialize) | **B — algorithm** |
| State helpers (`SelectionState`, `MergedRegion` factory) | **B — algorithm** |

**When in doubt, ask: "does the OTHER SIDE see the bytes of this
output?"** If yes, Mode A. If no — if the output is consumed
internally by other code in the same module — Mode B.

### Mode A — protocol port: byte-equivalence guidance

For Mode A code, the original guidance from earlier versions of
this skill applies:

- **Preserve verbatim**. Field order, default values, null
  handling, integer vs string typing.
- **Pin with a wire-snapshot test** that asserts the exact bytes
  produced for a known input. This is the load-bearing safety net.
- **Document the protocol source** — "this is the wire format the
  server emits; see XYZ.java for the producer side".
- **Audit cross-language interface boundaries with `comm`** (see
  next subsection — that section applies most strongly to Mode A).

### Mode B — algorithm port: clean-room rewrite guidance

For Mode B code, FLIP the defaults:

- **Don't read the source-language file when writing the target-
  language file.** Read it once to understand the BEHAVIOR; close
  it. Then write the target version from the behavior description
  + the test cases. This is the "clean-room" technique used to
  avoid derivative-work copyright issues — and it's also how you
  end up with code that doesn't look like translated source code.
- **Start from the modern target-ecosystem reference.** For a TS
  port in a Vaadin Flow codebase, the reference is
  `vaadin-grid-flow/src/.../gridConnector.ts`. Open it BEFORE the
  source-language file. Match its module structure, naming
  conventions, error-handling patterns, test style.
- **Derive behavior from tests, not from the source.** Write the
  tests first (or extract them from the source language's test
  suite). Implement against the tests, using the source-language
  file only as a cross-check when a test surprises you.
- **Treat apparent bugs as bugs.** If the source-language version
  has an off-by-one, a typo in a regex character class, an
  obviously-permissive parser that accepts inputs the contract
  doesn't allow — those go AWAY in the clean-room rewrite. The
  visual-regression / integration safety net catches the rare case
  where downstream actually depended on the bug.
- **Document the BEHAVIOR contract, not the implementation.**
  Comments cite "Excel's clipboard format truncates newlines" or
  "POI rows are 1-indexed", NOT "ported from `SheetWidget.java:4472`".
  The latter framing reads as derivative work; the former reads as
  re-derivation from the same source spec.

### When verbatim might still be tempting in Mode B

The seductive case: "this 6-LOC inner loop is so tight, the clean
version is identical anyway". Resist. Even when the resulting code
LOOKS identical to a translation, the framing matters:

- **Source comments** should cite the behavior contract, not the
  source file.
- **Variable names** should follow target-ecosystem conventions
  (e.g. `definedRowHeightsPx` instead of `definedRowHeights`
  with a comment "always in pixels").
- **Test names** should describe the behavior, not the source
  method's name.

If after all that the code is still character-identical to a
translation, that's fine — the behavior is constrained enough that
there's only one correct implementation. But the comments + tests
+ surrounding structure make clear it's a re-derivation.

### Local source over JAR / online docs (applies to both modes)

Read the source you're investigating in the local repo, not via
artifact extraction. JAR sources via `m2 unzip` work but miss
surrounding context (sibling files, test usage patterns, recent
commits that explain "why"). Sibling repos like `../flow`,
`../docs`, `../web-components` are almost always cheaper to grep
than chasing an extracted source jar.

In Mode B specifically, the LOCAL SOURCE is still valuable as a
cross-reference — just don't have it open when actually writing
the new code.

### Lit elements without decorators: the class-field-shadowing gotcha

When the test-runner toolchain doesn't enable TS decorators
(esbuild via `@web/dev-server-esbuild` with default settings
doesn't), Lit's `@property` / `@customElement` decorators can't
be used + the `static properties = {...}` API is the fallback.
But that fallback has a subtle gotcha: a class field with an
initializer:

```ts
export class MyEl extends LitElement {
    static properties = { foo: {} };
    foo: string = 'default';  // ⚠️ shadows Lit's reactive accessor
}
```

… creates a native class field that OVERWRITES Lit's installed
accessor at construction time. Lit detects this and throws at
runtime: "The following properties on element X will not trigger
updates as expected because they are set using class fields".

The fix: use `declare` (no initializer) + initialize in the
constructor instead:

```ts
export class MyEl extends LitElement {
    static properties = { foo: {} };
    declare foo: string;
    constructor() {
        super();
        this.foo = 'default';
    }
}
```

The `declare` tells TS the field exists for type-checking but
emits no runtime code, so Lit's accessor stays intact. The
constructor's `this.foo = ...` assignment goes through the
accessor (since the prototype already has the setter installed
by Lit at class-evaluation time), which is the desired path.

This is a one-time gotcha but burns ~10-30 minutes when first
encountered (the failure mode is a runtime warning that obscures
the actual rendering issue — the element loads fine, the property
just doesn't trigger re-renders). Bank the pattern in your project
when first introducing Lit elements + reference it on the next
addition.

Reference: <https://lit.dev/msg/class-field-shadowing> + slice 132
of the spreadsheet refactor (the case study).

### Design slice before implementation slice, when ecosystem-alignment matters

When the rewrite target is "match the ecosystem's modern style" + the
ecosystem has an existing reference (vaadin-grid-flow for Vaadin
Flow components), DON'T start implementing the new code without
first studying the reference's architecture + writing a design doc
that locks in the deviations + alignments.

The pattern:

1. **Survey the reference** with an Explore agent or focused
   reads. Answer: file layout, naming, state propagation,
   initialization lifecycle, server↔client interaction.
2. **Write a design doc** that:
   - Lists what the new code adopts from the reference (with
     concrete file/line citations).
   - Lists what's necessarily different (with the architectural
     constraint that forces the deviation explicitly named).
   - Maps the implementation to 5-10 follow-up slices in order.
3. **Implement in subsequent slices** with the design doc as the
   authority — when an implementation choice surprises you, the
   doc is the place that explains the why.

Why this is leverage:

- An implementation slice that starts cold may produce code that's
  90% right but architecturally divergent. Five follow-up slices
  re-align it.
- The design doc is a public artifact that future contributors
  read instead of re-discovering the rationale from git archeology.
- The "what's necessarily different" section forestalls the
  recurring "why don't we just do it like X?" question by naming
  the constraint upfront.

The trap to avoid: design slices that produce abstract diagrams
without naming concrete files. The design doc must reference real
file paths and real line numbers in the reference, OR it's just
opinion dressed as plan.

The slice 131 → 132 boundary in the spreadsheet Tier 3 refactor is
the case study. Slice 131 surveyed vaadin-grid-flow's `gridConnector.ts`
+ wrote `refactor-artifacts/CELL_GRID_DESIGN.md` cataloging which
of grid's patterns the spreadsheet's cell-grid adopts (`$connector`
naming, `initLazy` lifecycle, batched state application) vs what's
forced different (the wrapper element is structurally required —
spreadsheet has no published web component to patch like grid does).
Slice 132 implements against that doc.

### Cleanup pattern: if you've already shipped Mode B work as Mode A, do an explicit "execute the audit" slice

The verbatim-port → clean-room transition isn't free — once you've
shipped algorithm code in the byte-equivalent style, the comments,
variable names, and javadoc framings carry the source-language
signal forward. The cleanup is mechanical but worth a dedicated
slice rather than being folded into the next port:

1. **Audit doc first** (one slice). Catalog every QUIRK / "preserved
   verbatim" / "ported byte-equivalent from..." marker in the
   existing code. Classify each as KEEP (real-world contract;
   rewrite the comment to cite the contract), DROP (apparent
   source-language bug; the new behavior matches the spec, not the
   bug), or REPHRASE (behavior stays, framing changes).

2. **Execution slice** (one slice). Walk the audit + apply each
   resolution. Update test expectations for DROPs. Run the test
   suite per file as you go, not at the end — a DROP test fix
   on file X can pass against the original code AND the corrected
   code if the test data doesn't actually exercise the diff. (E.g.
   in the spreadsheet refactor, the `row >= length` quirk's test
   used row 5 against a length-5 array — the only row that
   triggered the quirk. Removing the quirk meant flipping
   `.to.equal(18)` to `.to.equal(20)` on exactly that test.)

3. **Visual-regression as the safety net for behavior DROPs.** When
   the cleanup changes user-observable behavior (e.g. removing a
   regex char class drops 6 characters from token-matching), the
   unit tests may not catch the downstream effect. The
   visual-regression IT (capture + diff against committed
   baselines) is the deferred check; flag it explicitly in the
   slice notes + run before the next user-facing release.

4. **Don't try to do this incrementally during normal slices.**
   The temptation is "I'll fix this comment while I'm here". Don't —
   the audit framing keeps the cleanup focused + the test changes
   reviewable in isolation. Mixing cleanup with port work makes
   both reviews harder.

### Why clean-room (Mode B) matters: licensing + maintenance

The reason the byte-equivalent + QUIRK-callout pattern is wrong-
direction for algorithms isn't aesthetic — it's two concrete
project-level costs:

1. **Legacy maintenance burden.** A verbatim port preserves every
   accident the source-language version had: the wrong-feeling
   off-by-ones, the over-permissive parsers, the JSNI-influenced
   structure, the source-language idioms expressed in target-
   language syntax. All of it becomes the new team's burden to
   maintain. The codebase grows old AT BIRTH.

2. **Licensing / copyright.** A clean-room rewrite is a recognized
   technique for producing derivative work that doesn't infringe
   on the original's copyright. A "ported byte-equivalent + QUIRK
   callout" implementation effectively redistributes the source-
   language work in another syntax — which carries the original's
   license forward AND signals "this is the same code". For
   relicensing scenarios, organizational changes, or just risk
   minimization, the clean-room version is the better artifact.

In the Vaadin Spreadsheet refactor (Tier 3, slices 117+), both
drivers applied. The user clarified the direction in slice 129
after slices 118-125 had ported ~5 algorithm subsystems in the
old "byte-equivalent" style. `refactor-artifacts/QUIRKS_AUDIT.md`
catalogs each banked quirk + classifies as KEEP / DROP / REPHRASE
for cleanup in subsequent slices.

### Tests must ACTUALLY RUN — `tsc --noEmit` is not a runtime check

(Applies to both modes.) A TypeScript file that compiles cleanly
under strict mode can still have semantic bugs that only a runtime
test catches. The canonical example: `if (row > defs.length)` vs
`if (row >= defs.length)` — both type-check, only the runtime
catches the off-by-one. Compile-checks verify shape; tests verify
behavior. Always run the tests for ported code.

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

### Audit cross-language interface boundaries with `comm`, not visual scanning

When the port involves an INTERFACE that the consumer (e.g. a JS
wrapper) calls into, the port's `implements MyInterface` clause
is only as protective as the interface is complete. An interface
that's a SUBSET of what the consumer actually calls is worse than
no interface — it gives false confidence: `tsc` passes, the port
ships, then the consumer hits the un-declared methods at runtime.

The fix is mechanical and exhaustive: extract the callsite list from
the consumer + extract the declared-method list from the interface,
then `comm -23` to find what's called but not declared.

```sh
# Wrapper's calls
grep -oE "this\.api\.[a-zA-Z]+" wrapper.js | sed 's/this\.api\.//' | sort -u > /tmp/calls.txt

# Interface's declarations
grep -oE "^    [a-z][A-Za-z]+\(" my-interface.ts | sed 's/    //;s/($//' | sort -u > /tmp/decls.txt

# Methods called but NOT declared:
comm -23 /tmp/calls.txt /tmp/decls.txt
```

Run this BEFORE shipping an interface that claims "compile-time
contract enforcement". The slice 120 → slice 126 arc in the
spreadsheet refactor lost 5 slices' worth of "this is enforced"
confidence because the original interface survey scanned ONE
wrapper section visually, missed a second section, and shipped a
50-method interface against a 105-method actual surface. The
slices that came after (121-125) cited the (broken) safety net to
justify scope decisions.

If the consumer has MULTIPLE callsite sections (a property-change
block AND a separate initialization block AND a teardown block),
each must be included in the grep. Visual scanning of one section
+ trusting it's "the only one" is exactly how the slice 120 gap
happened. The `grep | sort -u | comm` pipeline is exhaustive by
construction — every line that contains `this.api.` is captured
regardless of where it appears.

The "subset interface = false confidence" rule generalizes beyond
TS: ANY contract assertion (Protocol Buffer schemas, GraphQL
SDLs, Avro schemas, OpenAPI specs) is only protective when it's
complete. The audit move is the same shape — mechanically extract
both sides, diff, surface the gap.

**Audit BOTH directions when the boundary has them.** A wrapper
that calls into the new code AND listens for events from it
gives you two interfaces to audit, not one. Slice 126 fixed the
server→client API direction (the wrapper's `this.api.X(...)`
calls vs the `SpreadsheetClientApi` interface). Slice 128 then
ran the symmetric audit on the OPPOSITE direction (events the
wrapper dispatches via `createEvent('eventName', data)` vs the
`SpreadsheetEventType` discriminated union) — and found a
matching gap. The pattern: if you fix gap A, ASK whether B
exists, and audit it before shipping.

**Envelope events trip up the naive `event.type` check.** Some
wrappers package all events into a single outer `CustomEvent`
type (e.g. `'spreadsheet-event'`) with the discriminator inside
`detail.type`. A test that builds `new CustomEvent('eventName',
{detail})` won't match the narrower's `event.detail.type`
inspection. Read the wrapper's `createEvent` definition before
constructing test events.
