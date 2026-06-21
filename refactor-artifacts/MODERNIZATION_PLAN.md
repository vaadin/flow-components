# Spreadsheet modernization plan — three tiers

> Drafted 2026-06-22 after the deep-investigation into the GWT
> compilation pipeline (see journal: slice 99 enumeration). Supersedes
> the off-the-cuff "modernization" suggestions made in conversation
> before the constraints were mapped. This plan is the durable record
> of WHAT moves are safe, WHEN they're safe, and HOW they relate to
> each other.

## What "modern" means here

Aligning the Spreadsheet component with the rest of the
`flow-components` repo: server-side Java 21, no GWT, TypeScript / Lit
on the client, a clean JSON wire protocol, single source of truth for
shared types. The reference shape is `vaadin-grid-flow-parent` — three
submodules, zero `.gwt.xml`, TypeScript connector files, web-component
loaded as an npm dependency.

## The actual constraint map (NOT what I assumed earlier)

- **Server-side Java code** in `vaadin-spreadsheet-flow/.../*` —
  **NO GWT constraint.** Java 21 fully available. ~5K LOC on
  `Spreadsheet.java` + ~1K on `CellValueManager.java` + ~30 extracted
  helpers + everything else.
- **GWT-compiled client code** in `vaadin-spreadsheet-flow-client/.../*` —
  **Constrained to Java 11 source level** (GWT 2.10.0 plugin
  limitation). 105 files, ~26K LOC. The actual grid widget,
  formula bar, selection handling.
- **Wire-layer DTOs** in `vaadin-spreadsheet-flow/.../client/*` (7
  files: `CellData`, `MergedRegion`, `OverlayInfo`,
  `SpreadsheetHandler`, `MergedRegionUtil`,
  `SpreadsheetActionDetails`, `GroupingHandler`) — **server-only**
  Java BUT each has a **mirror copy with `@JsType` annotations on the
  GWT side**. Two parallel class hierarchies. A change to one without
  the other breaks the wire format.

The Lit wrapper at `vaadin-spreadsheet.js` is already modern; the
legacy island is the GWT-compiled `spreadsheet-export.js`.

## Tier 1 — Server-side modernization

**Scope**: Pure server-side Java only. No wire-format change. No GWT
involvement. Anything in this tier could be merged in isolation.

**Why first**: highest ROI, lowest risk. Most moves are mechanical;
the diff is small per slice; the test suite (now 1,050 strong) catches
regressions immediately.

### Step 1 — Switch expressions for cell-type dispatchers

Java 14+ switch expressions replace the current `switch (cellType) {
case X: result = ...; break; }` pattern with assignment-form
`return switch (cellType) { case X -> ...; }`. Eliminates the
`break`/`fall-through` foot-gun, gives compile-time exhaustiveness
checking on enum dispatch.

**Sites (concrete, identified in the spreadsheet package):**
- `CellNumericFormatter.getOriginalCellValue` — 8-arm switch over
  `CellType` returning a string
- `CellNumericFormatter.getCachedFormulaCellValue` — 5-arm switch over
  `CellType` setting a local `result`
- `CellValueShifter.shift` — 6-arm switch dispatching to per-type
  helpers
- Several `if (cell.getCellType() == X) else if ...` chains that
  could become switch expressions for consistency
- ~13 sites total per the inventory

**Test impact**: zero. The behavior is preserved verbatim; the
existing characterization tests pin the dispatch.

**Slice shape**: one slice covering all 13 sites, OR split into
"safe-already-using-switch" + "if-chain-rewrites" — depends on
diff size. Single commit if under 200 LOC delta.

### Step 2 — Sealed `Command` and `ValueChangeCommand` hierarchies

Currently `Command` is an interface in `command/Command.java` with
multiple known implementations. Making it `sealed interface Command
permits CellValueCommand, ...` enforces the closed set at compile time
and unlocks exhaustive pattern matching in callers.

**Sites:**
- `command/Command.java` → `sealed interface`
- `command/ValueChangeCommand.java` → `sealed interface extends Command`
- All implementations get `non-sealed` or `final`
- `CommandReplayer.fireCellValueChangeEvent` does `instanceof
  ValueChangeCommand` — could become a pattern match

**Test impact**: zero. Sealing is API-tightening but no behavior change.
Subclassing from outside the spreadsheet package is already
package-private-effective.

### Step 3 — `serialVersionUID` audit

57 `@SuppressWarnings("serial")` annotations across the package, zero
`serialVersionUID` declarations. Vaadin Flow session replication
depends on the contract; the suppression should be replaced with
an explicit ID and a one-line justification comment.

**Sites:** every class implementing `Serializable` in
`vaadin-spreadsheet-flow/.../*` (use `grep -l "implements Serializable"`).

**Test impact**: zero. The change is metadata-only. The payload
snapshot in `SpreadsheetHistoryManagerPayloadTest` is the safety net.

**Slice shape**: mechanical sweep, single commit, no characterization
tests needed (no behavior change).

### Step 4 — JSpecify (or similar) nullness annotations on Spreadsheet public API

The public `Spreadsheet` API has many methods accepting `null` as a
sentinel without documentation. JSpecify's `@Nullable` /
`@NonNull` makes the contract explicit at the type level. Modern IDEs
+ build-time checkers (NullAway, Checker Framework) consume them.

**Sites:** start with the public API of `Spreadsheet.java` — the
setters that mirror `null` to client property (`setOverlays`,
`setSheetNames`, `setHyperlinksTooltips`, etc.).

**Test impact**: zero. Annotations are metadata only. Compile-time
warnings would surface inconsistencies; fix as they appear.

### Step 5 — Pattern matching for instanceof

Java 16+ allows `if (x instanceof Y y) { y.foo(); }` to bind the
narrowed reference in one step. Eliminates the `instanceof` + cast
double-pattern.

**Sites:** ~20+ occurrences across the package. Mechanical sweep.

**Test impact**: zero.

### Step 6 — POIReflectionTools audit against POI 5+

The `POIReflectionTools` class uses reflection to access POI internals.
POI 5.x has expanded the public API for several of these operations.
Audit whether the reflection is still necessary; replace with public
API where possible.

**Test impact**: existing characterization tests pin behavior; the
goal is to drop reflection while preserving the observable contract.

### Step 7 — Records for SERVER-ONLY DTOs (selective, NOT the wire mirrors)

The 7 wire-mirror DTOs cannot become records — they have parallel GWT
copies AND in some cases hand-written `equals` semantics
(`CellData.equals` is by-position-only as a dedup contract). But the
PURE server-side DTOs that don't cross the wire CAN become records.
Audit per-class.

**Sites to evaluate:**
- `Spreadsheet.CellValueChangeEvent` — server-only event, candidate
- `Spreadsheet.SheetChangeEvent` — same
- Smaller value objects discovered during the audit

**Test impact**: per-class. Records change `equals`/`hashCode`. Where
the existing implementation diverges (e.g. by-position equality), the
migration is a behavior change and needs justification.

## Tier 2 — Wire-protocol modernization

**Scope**: Server changes that preserve the wire format. The GWT
client keeps working. The parallel-DTO problem is the central target.

**Why second**: depends on Tier 1's modernized server-side patterns
being in place. Higher risk than Tier 1 because the wire format is
load-bearing for the GWT client.

### Step 8 — `Serializer.java` audit

The custom `Serializer.serialize(...)` wraps Jackson. Audit whether
it's still needed or if direct Jackson usage would simplify the call
sites. Document any custom serialization logic that wouldn't survive a
straight replacement.

### Step 9 — DTO consolidation

The 7 mirror-pair DTOs are the central pain point. Two paths:

- **9a (lower risk)**: Maven plugin to generate the `@JsType`
  GWT-side copies from the server-side originals. Single source of
  truth, automated sync, no behavior change.
- **9b (higher risk, higher payoff)**: Drop the GWT-side typed copies
  entirely. Have the GWT widget consume plain JSON (`JsonObject`)
  rather than JsType-annotated POJOs. Lets us eventually drop the
  client module's JsInterop dependency.

Decide which path during the audit. 9a is safer; 9b unlocks Tier 3.

### Step 10 — Vaadin 8 RPC → Vaadin Flow Element API

Some server→client messages still use Vaadin 8 RPC interfaces
(`SpreadsheetClientRpc`). Modern Vaadin Flow components use Element
API (`element.setProperty`, `element.callJsFunction`) instead. Migrate
where possible.

### Step 11 — Complete the MirroredProperty rollout

Most server→client property mirrors now go through
`MirroredProperty<T>`. A handful remain inline (`dirty` timestamp,
`appId`, etc.). Audit and migrate the remainder OR document why each
deliberately stays inline.

## Tier 3 — TS/Lit client rewrite

**Scope**: Replace the GWT-compiled spreadsheet widget with a
TypeScript-based Lit web component. Drop the GWT client module
entirely.

**Why last**: requires Tier 2 to be complete (clean JSON wire protocol
without parallel DTOs). Frame as a separate project, not a refactor.

**Effort estimate** (per the deep investigation): 6–12 weeks for a
focused team of 2–3 people. ~80% of the logic transfers conceptually
(grid virtualization, scrolling, selection, formula bar, copy/paste
shape); ~20% needs careful reimplementation (custom cell editors,
formula-aware paste, merged-cell rendering nuance).

**Pre-conditions** (must be true before starting):
1. Tier 2 step 9 done (DTOs consolidated, clean JSON wire protocol).
2. The Spreadsheet component has a comprehensive integration-test
   suite (golden screenshots, keyboard navigation tests, etc.) — the
   ITs are the only safety net for a UI rewrite.
3. Vaadin's stable Lit + TypeScript tooling targets are documented.

## Dependencies and ordering

```
Tier 1 (server-side mods) ────► safe in isolation, parallelizable
  │
  └────► Step 8 (Serializer audit)
            │
            └────► Step 9 (DTO consolidation, two paths)
                      │
                      └────► Step 10 + 11 (full wire-protocol cleanup)
                                │
                                └────► Tier 3 (client rewrite)
```

## Slice-shape contract

Every step lands as one or more "slice" commits following the
established pattern (refactor / test / docs split). Every slice ends
green on the unit suite + the payload-snapshot test
(`SpreadsheetHistoryManagerPayloadTest`). For Tier 2 changes the
payload snapshot is the load-bearing safety net — every diff must be
verifiable as wire-format-preserving.

## What's explicitly OUT of scope

- **J2CL migration** — Vaadin has standardized on TS/Lit. J2CL changes
  the compiler but not the architectural problem.
- **Continuing Phase-1 god-class extraction on Spreadsheet.java
  beyond what's already done** — diminishing returns, not what
  modernization means.
- **Speculative architecture changes** (e.g. introducing DI containers,
  switching to GraphQL, etc.) — no concrete payoff in this context.

## Status as of 2026-06-22

- **Tier 1 step 1 (switch expressions)** — starting now (slice 99).
- All other tiers — planned, not started.
