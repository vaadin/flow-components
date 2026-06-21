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

### Step 2 — **DROPPED**: Sealed `Command` hierarchy is not safe

Originally planned but discovered to be unsafe on inspection
(2026-06-22):

- `Command` and `ValueChangeCommand` are `public` interfaces in the
  `command/` package, documented with `@since 1.0`. They are part of
  the publicly-supported Vaadin Spreadsheet API.
- Sealing the interface would make downstream user implementations
  (`MyCustomCommand implements Command`) a binary-incompatible
  breaking change.
- The interfaces are also heavily Mockito-mocked across the test
  suite (`mock(Command.class)` at 12+ sites in `CommandStackTest`
  alone). Mockito's standard subclass mock-maker can't generate a
  permitted subclass for a sealed type.

Per the `code-modernization` skill's pitfall list: "Sealing
hierarchies you don't fully own ... If external consumers might
subclass, you've made a breaking change. Verify zero external
implementations exist OR sequence the sealing behind an API version
bump." Neither precondition is met here. **Dropped.**

If sealing ever becomes desirable, the correct path is: (a)
deprecate `Command` as an extension point in a major version, (b)
introduce a new `sealed` internal hierarchy for the internal
discriminated-dispatch use cases, (c) keep `Command` un-sealed for
external consumers.

### Step 2 (replacement) — Pattern matching for `instanceof` ✓ slice 100

Java 16+ allows `if (x instanceof Y y) { y.foo(); }` to bind the
narrowed reference in one step. Eliminates the `instanceof` + cast
double-pattern.

**Status**: completed in slice 100. ~40 cast-after-check sites
swept across 13 files (SpreadsheetFactory, RuleEvaluator,
Spreadsheet, CellValueManager, SpreadsheetUtil, CellBorderStyleBuilder,
XSSFColorConverter, DefaultHyperlinkCellClickHandler, FontStyleComposer,
CellDataBuilder, ConditionalFormatter, SpreadsheetStyleFactory,
TableLifecycle). Sites left untouched: bare `instanceof` guards that
never cast inside the branch (anchor type checks in `SheetOverlayWrapper`
and `SheetOverlayVisibility`, the `POIReflectionTools` reflection guards
whose cast targets are `getFieldVal`'s return — not the instanceof
target, the FormulaFormatter token checks that call only the supertype
`toString`), and the two reflection-only branches in
`CellValueManager.removeHyperlink` that don't bind the type.

**Test impact**: zero. The pattern-variable binding produces
byte-identical bytecode to the cast-after-check; the suite remained
1050/0/0/4 green.

**Two inverted-form rewrites worth noting**:
- `loadSheetTables` + `loadGrouping` + `Spreadsheet.expandOrCollapseRowsOrColumns`
  switched from `if (instanceof HSSF) return; cast-to-XSSF` to
  `if (!(instanceof XSSFSheet xssf)) return;` — one fewer downcast.
- `loadHSSFPicture` had its parameter narrowed from `HSSFShape` to
  `HSSFPicture` since pattern-matching at the only callsite made the
  widen-then-cast dance redundant.

### Step 3 — drop `@SuppressWarnings("serial")` sweep ✓ slice 101

**Original framing (rejected after deeper look):** "replace 57
suppressions with explicit `serialVersionUID = 1L` declarations".
That direction would diverge from the in-ecosystem reference.

**Ecosystem reference:** modern Vaadin Flow components in this same
repo (Grid, Button, TextField, Combo, etc.) implement `Serializable`
with **neither** a `serialVersionUID` **nor** a
`@SuppressWarnings("serial")` — the annotation is silencing a
warning that the project's compiler config doesn't even raise
(verified: `mvn compile` produced no serial warnings).

**Status**: completed in slice 101. 84 `@SuppressWarnings("serial")`
annotations deleted (83 in `src/main`, 1 in `src/test`). No
`serialVersionUID` declarations added — matching the ecosystem
direction. Suite stayed 1050/0/0/4 green; spotless + checkstyle
clean. Each deletion removes one line of noise; no behavioral
effect.

**Why not add UIDs**:
- The default-computed UID is unstable across compilations anyway
  (changes when fields/methods change), so explicit `1L` would only
  matter for cross-version session deserialization — a contract
  this project hasn't been honoring at all (refactor slices have
  freely renamed fields and added methods for ~100 commits).
- Adding 83 declarations would be 83 lines of architectural noise
  with zero functional benefit and zero ecosystem precedent.

**Test impact**: zero (metadata-only deletion).

### Step 4 — JSpecify `@Nullable` on documented null sites ✓ slice 102 (scoped)

**Ecosystem reference**: upstream Vaadin Flow (`flow-server`,
85 files) uses `org.jspecify.annotations` with `@NullMarked` at the
package level + per-site `@Nullable`. JSpecify 1.0 is **already on
the classpath** transitively (verified via `mvn dependency:tree`),
no new dependency needed. Flow-components siblings (Grid, Button,
TextField, etc.) haven't adopted JSpecify yet — spreadsheet is the
first in the components monorepo, but aligned with the framework
direction.

**Status**: completed in slice 102, but **scoped down** from the
original framing.

**Original framing (rejected as too broad)**: "annotate the entire
Spreadsheet public API". That's ~200 public methods on Spreadsheet
alone, plus ~50 helpers — multi-session work and high risk of
mis-annotating subtle null contracts I haven't audited.

**Corrected framing (executed)**: annotate ONLY the methods where
the javadoc already documents the null contract. Pure
javadoc-to-type-annotation conversion. Zero risk of being wrong
because the truth is already in the javadoc.

**Sites annotated**: 6 `getCell` overloads (returns `@Nullable
Cell`, with two also accepting `@Nullable CellReference` / `@Nullable
String sheetName` per their javadoc), and `getCommentAuthorProvider`
(returns `@Nullable CommentAuthorProvider`). Plus the import +
alphabetized ordering.

**Why no `@NullMarked` package-info yet**: that would be an
unaudited contract claim for every other method/field/param in
the package. Until someone runs a full audit pass, the
documentary value comes from the per-site `@Nullable` markers
only.

**Test impact**: zero (annotations are metadata; not enforced
because no NullAway/Checker is configured). Suite stayed
1050/0/0/4 green.

**Follow-up work** (deferred): full `@NullMarked` audit of the
spreadsheet package + sweep of public APIs on Spreadsheet.java
that aren't yet javadoc-documented. Best done after a NullAway
config is added so inconsistencies become compile errors.

### Step 5 — Pattern matching for instanceof ✓ (same as Step 2)

Already completed in slice 100 as the replacement for the dropped
Step 2 (sealed-Command). The original Step 5 framing was identical
to the eventual Step 2 execution; treat this as one step.

### Step 6 — POIReflectionTools audit against POI 5+ ✓ slice 103

**POI version on the classpath**: 5.5.1 (verified via
`mvn dependency:tree`). Audited all 4 helper methods in
`POIReflectionTools` against POI 5.5.1's public API.

**Status**: completed in slice 103. Two of the four reflection sites
dropped, two stay with rationale recorded.

**Sites dropped — now use public POI API**:
- `getOrderedRuleList`: priority comparator was reading
  `(CTCfRule) getFieldVal(rule, "_cfRule").getPriority()` via
  reflection on a private `_cfRule` field. POI 5+ exposes
  `XSSFConditionalFormattingRule.getPriority()` directly. The
  six-line anonymous-class comparator collapses to
  `Comparator.comparingInt(XSSFConditionalFormattingRule::getPriority)
  .reversed()`.
- `stopHere`: was reading the same `_cfRule._stopIfTrue` field via
  reflection. POI 5+ exposes
  `XSSFConditionalFormattingRule.getStopIfTrue()` directly. The
  five-line if-block collapses to a single
  `rule instanceof XSSFConditionalFormattingRule xssfRule
  && xssfRule.getStopIfTrue()` (pattern-matching binds the cast in
  the same line — matches slice 100's idiom).

**Sites kept — semantic mismatch with POI's public API**:
- `isBorderSet`: needs per-side `isSetLeft()` / `isSetTop()` / etc.
  on `CTBorder` to distinguish "border was explicitly set in the XML
  (even to 'none')" from "border was never declared". POI's public
  `XSSFBorderFormatting.getBorderLeft()` etc. return a `BorderStyle`
  enum (with NONE as the default) — they collapse those two states
  into one. The CF semantics ("explicitly-set NONE overrides the
  previous rule's border") need the original distinction.
- `hasStrikeThrough`: needs "ANY entry in `CTFont.getStrikeList()` has
  `val=true`". POI's public `XSSFFontFormatting.isStruckout()`
  inspects only the first entry. Real Excel files only have one
  entry in practice, but the existing implementation is defensive;
  changing semantics here would be a behavior change, not pure
  modernization.

**`getFieldVal` stays** — still used by the two reflection sites
above.

**Class javadoc updated** to reflect the now-narrower scope (POI 5+
exposes priority and stopIfTrue; reflection remains for the
border-isSet and multi-strike-entry edge cases).

**Test impact**: zero. The slice-79 `POIReflectionToolsTest` pins
observable behavior (priority-descending order; stopIfTrue read
correctly) — those pass unchanged because the public API returns
the same values reflection used to extract. Suite stayed
1050/0/0/4 green.

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

## File-format backward compatibility (non-negotiable)

**Existing `.xlsx` / `.xls` files produced by any prior version of
this component, or by Excel / other tooling, MUST continue to load
and render identically after modernization.** This is the strongest
contract in the entire effort — stronger than the wire format,
stronger than the public API — because the artifacts are out in the
wild and can't be re-issued by users.

Specifically:

- **Load surface stays as-wide.** Any input the un-modernized code
  accepted, the modernized code must also accept. POI version bumps,
  reflection removals, and parser refactors must preserve the
  accepted-input set.
- **Style and layout rendering is identical.** Cell styles, fonts,
  borders, alignment, conditional formatting, merged regions, freeze
  panes, charts, images, hyperlinks, comments — everything that was
  visible in the un-modernized component must look pixel-equivalent
  in the modernized one. Visual / golden-snapshot regression tests
  on a representative fixture set are the only reliable safety net.
- **Write-back compatibility.** A file saved by the modernized
  component must be readable by the previous version unless a major
  contract change is explicitly declared and versioned.
- **Test fixtures.** If the repo doesn't already carry sample
  `.xlsx` / `.xls` fixtures produced by older versions, that gap
  must be closed before any tier-2 / tier-3 modernization moves
  start. The current ITs cover some rendering paths; a deliberate
  back-compat fixture set is a missing artifact.

This applies to every move in every tier. For tier 1 mechanical
refactors (switch expressions, pattern matching) it's automatic — the
bytecode behavior is unchanged and POI is untouched. For tier 2 wire-
protocol consolidation it requires the payload snapshot AND the
visual regression tests. For tier 3 client rewrite it's the central
release-gate.

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
