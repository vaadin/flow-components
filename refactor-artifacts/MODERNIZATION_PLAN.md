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

### What the wire actually looks like (scoping investigation, 2026-06-22)

The Tier 2 framing in the previous revision of this plan was
hand-wavy. A scoping investigation produced concrete data:

**Topology**

- **Server → client** is *already* on the modern Vaadin Flow API:
  - 11 methods on `SpreadsheetClientRpc` (server-side façade
    interface in `rpc/SpreadsheetClientRpc.java`). Each method's
    implementation in `Spreadsheet.java` lines 679–755 is a one-line
    wrapper that calls `getElement().callJsFunction("name", args)`.
    There is no legacy Vaadin 8 ClientRpc in the path. The
    `SpreadsheetClientRpc` interface is a typed façade kept so the
    client-side GWT connector can see the same method names; it does
    NOT mean Vaadin 8 RPC is still in use.
  - 21 `MirroredProperty<T>` instances on Spreadsheet for the
    property-mirror lane.
  - 6 remaining inline `getElement().setProperty(...)` sites
    (`id`, `cellStyleToCSSStyle`, `displayGridlines`,
    `displayRowColHeadings`, `appId`, `dirty`), each with rationale
    recorded in slices 94–96.
- **Client → server** is *already* on the modern DomEvent API:
  - Single `@DomEvent("spreadsheet-event")` declared on
    `Spreadsheet.SpreadsheetEvent` (line 631). The event carries
    `type` (`String`) + `data` (`JsonNode`) via `@EventData`.
  - The client-side `vaadin-spreadsheet.js` dispatches
    `CustomEvent('spreadsheet-event', {detail: {type, data}})`
    from 5+ sites (lines 441, 445, 449, 453, 457).
  - Server-side dispatch: `SpreadsheetEventListener` →
    `SpreadsheetHandlerImpl` → typed methods (the
    `SpreadsheetServerRpc extends SpreadsheetHandler` interface
    chain is JUST a Java-interface façade for the dispatcher; not
    Vaadin 8 ServerRpc).

**What's "still legacy" then?**

The wire-protocol RPC migration was completed BEFORE this plan was
written — the previous Step 10 ("Vaadin 8 RPC → Element API") is
already done. What remains as genuinely legacy:

1. **Parallel-DTO problem** (the central pain point). 6 DTOs are
   maintained as hand-synced copies between server-side and the
   GWT-compiled client module:
   - `CellData` (14 fields)
   - `MergedRegion` (6 fields)
   - `OverlayInfo` (9 fields)
   - `SpreadsheetActionDetails` (5 fields)
   - `PopupButtonState` (server `shared/`, client `client/`)
   - `GroupingData` (server `shared/`, client `client/`)

   Every wire-format field addition or rename has to be done in two
   places by hand. A bug here would manifest as silent serialization
   drift, not a compile error.

2. **`SpreadsheetState` — vestigial Vaadin 8 shared-state class**
   (66 public fields). Server-side it's defined but NEVER
   instantiated. Client-side (`SpreadsheetJsApi` + `SpreadsheetConnector`)
   it's STILL consumed by the GWT connector. So it's half-dead — the
   server side has already migrated to the property/RPC lanes, but the
   client side still references the old state-snapshot shape.

3. **Handler-interface chain duplicated for the GWT client**.
   `SpreadsheetHandler` (server-side `client/SpreadsheetHandler.java`)
   defines the methods callable from the GWT widget. Its purpose was
   to give the GWT connector a typed Java interface to compile against
   — but with the wire now being a single `spreadsheet-event` DOM
   event, the typed-interface duplication is mostly carrying its own
   weight. Concrete dispatching is via `SpreadsheetEventListener` →
   `SpreadsheetHandlerImpl`, NOT via the interface.

4. **`Serializer.java`** — 30-line Jackson wrapper. Configures
   `NON_DEFAULT` inclusion and logs errors. 28 server-side call
   sites. Audit conclusion: it earns its keep (the two pieces of
   non-trivial config wouldn't survive a straight inline). Keep,
   don't expand.

**What CAN'T be done in Tier 2 without touching the GWT client**

The GWT client (`vaadin-spreadsheet-flow-client`, 105 files,
~26K LOC) deserializes JSON arrays directly into the @JsType-annotated
mirror classes. As long as that module exists, the field shapes of the
mirror DTOs are load-bearing on the wire. Therefore:

- We can keep the wire format byte-stable while improving the
  server-side authoring experience (generate one side from the
  other, drop dead code, etc.).
- We CANNOT remove the parallel-DTO maintenance burden entirely
  without dropping the GWT client (= Tier 3).

That re-shapes Tier 2 into: *make the maintenance-of-parallel-DTOs
problem visible, automated, or both, without changing wire bytes*.

### Step 8 — `Serializer.java` audit ✓ (audit done; no change)

**Investigation**: 30-line Jackson wrapper. Two pieces of non-trivial
behavior: (a) configures `JsonInclude.Include.NON_DEFAULT` globally,
(b) catches `JacksonException`, logs at error, returns `null`.
28 server-side call sites; ALL pass DTOs/collections, NOT primitive
values, so the `value == null ? "" : ...` guard is reachable but
not load-bearing.

**Conclusion**: keep as-is. The audit asks "is the wrapper still
needed" — yes, both pieces of non-trivial behavior are. Inlining 28
call sites to use `JsonMapper.builder()...writeValueAsString(value)`
with try/catch would *expand* duplication, not reduce it.

**Test impact**: zero, no change recommended.

### Step 9 — DTO consolidation (the real Tier 2 work)

The 6 parallel-DTO pairs are the central target. Two paths, recorded
during the original plan; the scoping investigation sharpens them:

- **9a — generate one side from the other (lower risk, ship-ready)**.
  The server-side classes are the source of truth. Add a build step
  that emits the client-side `@JsType` versions from them.
  Concretely:
  - 6 DTOs × ~10 fields each = 60 fields to keep in lockstep.
  - The fields are public, the equality semantics differ in some
    cases (e.g. `CellData.equals` is by-position). The generator
    must preserve method bodies, not just field declarations.
  - Implementation option A: an annotation processor that scans
    server-side classes annotated `@WireMirror` and emits the
    client-side `@JsType` copy at build time. Pros: type-safe,
    fails at compile time. Cons: new annotation processor to
    maintain.
  - Implementation option B: a Maven plugin or Maven exec step
    that copies server-side files into the client module with a
    package-rewrite + `@JsType` annotation injection. Pros:
    simpler, no Java tooling. Cons: text-based, brittle to
    formatting.
  - Suggested choice: **option A**, an `@WireMirror` annotation
    processor that emits the client-side file at every server-side
    edit. Bias toward `option B` only if the annotation processor
    proves unwieldy in the GWT build pipeline.

- **9b — drop typed client-side copies, switch GWT widget to consume
  `JsonObject` directly (higher risk, higher payoff)**. The wire
  bytes don't change because JSON is JSON. The GWT widget would read
  `obj.getString("value")` instead of `obj.value`. Pros: drops 6 copy
  files and the JsInterop dependency. Cons: every site in the GWT
  client that reads a DTO field changes. The audit must enumerate
  read sites before estimating cost.

**Slice shape for 9a**: single PR. New `@WireMirror` annotation +
processor + generated-sources directory in the client module. The
6 existing client copies are deleted; the regenerated files are
byte-identical so the wire is unchanged. The acceptance gate is the
existing `SpreadsheetHistoryManagerPayloadTest` snapshot — must pass
unchanged before and after.

**Slice shape for 9b**: 6 sub-slices, one DTO at a time. Each
sub-slice converts the corresponding GWT-side reads from
`obj.fieldName` to `obj.getString("fieldName")` etc. (and removes
the typed mirror class). Each sub-slice must keep the snapshot test
green. Total scope ~6 sessions; significantly higher than 9a.

**Recommend 9a now; 9b deferred to Tier 3 prep** (since dropping
typed mirrors is partial overlap with the GWT-widget rewrite).

### Step 10 — ~~Vaadin 8 RPC → Element API~~ ✓ (already done)

The original Step 10 framing was based on an outdated mental
model. Both directions of the wire are already on Vaadin Flow's
modern API:

- Server → client: `getElement().callJsFunction(...)` via
  `SpreadsheetClientRpc` façade.
- Client → server: `@DomEvent("spreadsheet-event")` carrying
  type + JsonNode data.

The `SpreadsheetClientRpc` / `SpreadsheetServerRpc` / `SpreadsheetHandler`
interface chain remains as a typed-API façade — NOT legacy Vaadin 8
RPC. They could be collapsed/renamed as a cosmetic cleanup (slice
9c) but no migration is needed.

**No work in Tier 2.** Optional cosmetic cleanup deferred.

### Step 11 — `SpreadsheetState` cleanup (replaces "complete MirroredProperty rollout")

The MirroredProperty rollout the previous plan listed as Step 11 is
essentially complete (21 instances live + 6 documented exceptions —
slices 26–37, 94–96). What IS still legacy on the property-mirror
lane is `SpreadsheetState`:

- Server-side: `shared/SpreadsheetState.java` exists with 66 public
  fields. The class is **never instantiated** server-side (verified
  via grep). It's pure dead code waiting to be deleted.
- Client-side: `vaadin-spreadsheet-flow-client/.../shared/SpreadsheetState.java`
  is **actively consumed** by `SpreadsheetConnector` and
  `SpreadsheetJsApi`. The connector deserializes the wire's
  state-snapshot into this class.

**Slice shape**: two-step.
- **11a (server-side only, safe)**: delete the unused server-side
  `SpreadsheetState.java`. Suite stays green (no callers). Cleans
  up the half-dead artifact on the server side.
- **11b (cross-module, harder)**: the client-side `SpreadsheetState`
  consumption is part of the GWT connector's wire deserialization. If
  modern properties already cover the same data (e.g. through
  `MirroredProperty<T>`), the client-side state-snapshot path is
  also vestigial — but VERIFYING this requires reading the GWT
  connector code (`SpreadsheetConnector.java` in the client module)
  to confirm none of the 66 fields are still consumed.

**Test impact for 11a**: zero (deletion of unreferenced server-side
code). **Test impact for 11b**: needs golden snapshot + the GWT-build
+ ITs as the safety net; same shape as a Tier 2 wire change.

**Recommend 11a as a stand-alone slice**; 11b deferred until the
GWT-side audit is run.

### Tier 2 — revised step list

| # | Step | State | Estimate |
|---|------|-------|----------|
| 8 | `Serializer.java` audit | ✓ done (audited; keep as-is) | done |
| 9a | DTO consolidation: `@WireMirror` annotation processor | open | 1 session |
| 9b | Drop typed client mirrors → JsonObject reads | deferred to Tier 3 prep | 6 sessions |
| 9c | Rename/collapse `*ClientRpc` / `*ServerRpc` façade interfaces | cosmetic, optional | 0.5 session |
| 10 | Vaadin 8 RPC → Element API | ✓ already done | done |
| 11a | Delete unused server-side `SpreadsheetState` | open | 0.5 session |
| 11b | Drop client-side `SpreadsheetState` (if redundant) | needs GWT audit | 1–2 sessions |

**Total open Tier 2 work**: roughly 2 focused sessions for the safe
slices (9a + 11a), plus 1–2 more if 11b's audit shows redundancy.
9b is correctly held back as a Tier 3 prerequisite.

**The wire format stays byte-identical** across all of these — the
existing `SpreadsheetHistoryManagerPayloadTest` snapshot is the
acceptance gate for each slice.

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
